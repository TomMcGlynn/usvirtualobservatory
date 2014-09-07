package org.usvo.openid.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Test a login & password via a local command. */
public class ExternalProcessKit {
    private static final Log log = LogFactory.getLog(ExternalProcessKit.class);

    /** Define this interface to use run(). */
    public interface ExternalProducer<T> {
        /** Interpret the result of the external process.
         *  @param exitValue the process's exit value -- 0 typically means success etc.
         *  @param output a concatenation of stdout and stderr, with linefeeds between lines
         *  @return a meaningful result of the external process */
        T produce(int exitValue, List<String> output);
        /** Interpret the occurence of an exception. */
        T produce(Throwable e);
        /** What should be passed to the external process's standard input? If null, don't pass anything. */
        String getStdIn();
        /** What is the command to run? */
        String[] getCommand();
        /** A description of this process, for use in log files. */
        String getDescription();
    }

    /** Run an external process and produce a meaningful result object.
     *  @param producer tells us what command to run and also interprets the outcome.
     *  @param secondsToWait how long to wait for the external process to run before giving up on it. */
    public static <T> T run(ExternalProducer<T> producer, int secondsToWait) {
        Process process = null;
        try {
            log.debug(producer.getDescription());
            ProcessBuilder builder = new ProcessBuilder(producer.getCommand());
            process = builder.start();

            List<String> lines = new ArrayList<String>();
            CountDownLatch done = new CountDownLatch(4);
            final Process tmpProc = process;
            final int[] exitValue = { -1 };
            pool.execute(new Doer(done) {
                void doit() throws IOException, InterruptedException {
                    exitValue[0] = tmpProc.waitFor();
                }
            });
            String in = producer.getStdIn();
            if (in != null)
                pool.execute(new Writer(in, process.getOutputStream(), done));
            pool.execute(new Reader(null, lines, process.getInputStream(), done));
            pool.execute(new Reader(null, lines, process.getErrorStream(), done));

            // if false, timed out
            done.await(secondsToWait, TimeUnit.SECONDS);

            boolean successful = (exitValue[0] == 0);
            String message = ParseKit.printLines(lines);
            if (!successful)
                log.info("Unsuccessful: " + producer.getDescription() + ": " + message);
            else
                log.debug("Successful: " + producer.getDescription() + ": " + message);
            return producer.produce(exitValue[0], lines);
        } catch (Exception e) {
            log.warn("Exception during " + producer.getDescription(), e);
            return producer.produce(e);
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static void shutdown() {
        log.info("Shutting down thread pool.");
        pool.shutdown();
    }

    private static ExecutorService pool = Executors.newFixedThreadPool(8);
    private abstract static class Doer implements Runnable {
        private CountDownLatch done;
        private boolean interrupted = false;
        Doer(CountDownLatch done) { this.done = done; }
        public void run() {
            try {
                doit(); // TODO: time out
            } catch (InterruptedException e) {
                interrupted = true;
            } catch (IOException e) {
                log.warn(e); // TODO handle exception -- pass back up?
            } finally {
                done.countDown();
            }
        }
        @SuppressWarnings({"UnusedDeclaration"})
        public boolean isInterrupted() { return interrupted; }
        abstract void doit() throws IOException, InterruptedException;
    }

    private static class Reader extends Doer {
        private InputStream in;
        private String prefix;
        private final List<String> lines;
        Reader(String prefix, List<String> lines, InputStream in, CountDownLatch done) {
            super(done);
            this.prefix = prefix; this.lines = lines; this.in = in;
        }
        void doit() throws IOException {
            List<String> localLines = new ArrayList<String>();
            getLines(localLines, prefix, new LineNumberReader(new InputStreamReader(in)));
            synchronized(lines) { lines.addAll(localLines); }
        }
    }

    private static class Writer extends Doer {
        private OutputStream out;
        private String message;
        protected Writer(String message, OutputStream out, CountDownLatch done) {
            super(done);
            this.out = out; this.message = message;
        }
        void doit() throws IOException {
            PrintWriter writer = new PrintWriter(out);
            writer.println(message);
            writer.close();
        }
    }

    private static void getLines
            (List<String> lines, String stderrPrefix, LineNumberReader reader)
            throws IOException
    {
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                if (stderrPrefix != null) log.info(stderrPrefix + line);
                lines.add(line);
            }
            else break;
        }
    }
}