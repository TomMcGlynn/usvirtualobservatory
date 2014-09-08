/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



package ipac.stk.mapreduce.job;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;

import ipac.stk.io.ChunkIndex;
import ipac.stk.io.ChunkFile;
import ipac.stk.mapreduce.lib.input.OneLineInputFormat;


/**
 * Map/reduce job that reads that merges chunk files and top level chunk
 * index files. This allows efficient incremental loading of chunk indexes.
 *
 * @see ipac.stk.io.ChunkIndex
 * @see ipac.stk.io.ChunkFile
 */
public final class MergeChunks {

  private MergeChunks() { }


  /**
   * Mapper which receives a line of comma separated chunk file paths
   * and merges them.
   */
  public static final class MergeMapper
    extends Mapper<LongWritable, Text, NullWritable, NullWritable> {

    private ChunkIndex index = null;
    private Path outputDir = null;
    private FileSystem fs = null;
    private Pattern chunkFilePattern = null;

    @Override protected void setup(Context context)
      throws IOException, InterruptedException {

      Configuration conf = context.getConfiguration();
      index = new ChunkIndex(conf);
      outputDir = FileOutputFormat.getWorkOutputPath(context);
      fs = outputDir.getFileSystem(conf);
      chunkFilePattern = Pattern.compile(
        ".*/(chunk_([0-9]+)_([0-9]+).cf)$");
    }

    @SuppressWarnings("unchecked") @Override
    public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

      String[] files = StringUtils.split(value.toString());
      // Extract chunk and stripe number from first chunk file name
      Matcher m = chunkFilePattern.matcher(files[0]);
      if (!m.find()) {
        throw new IOException("Invalid chunk file name: " + files[0]);
      }
      MatchResult r = m.toMatchResult();
      String name = r.group(1);
      int stripe = Integer.parseInt(r.group(2));
      int chunk = Integer.parseInt(r.group(3));
      // Make sure chunk files have the expected names
      List<Path> paths = new ArrayList<Path>();
      for (String file : files) {
        if (!file.endsWith(name)) {
          throw new IOException(String.format(
            "Invalid chunk file name %1$s, expecting ...%2$s", file, name));
        }
        paths.add(new Path(file));
      }
      // Merge input chunk files
      ChunkFile cf = new ChunkFile(outputDir, fs, index, stripe, chunk);
      cf.merge(paths, context.getConfiguration(), context);
    }
  }

  private static Options buildOptions() {
    Options opts = new Options();
    Option help = OptionBuilder.withLongOpt("help")
      .withDescription("Print usage information")
      .create("h");
    opts.addOption(help);
    return opts;
  }

  private static void printUsage(Options options, OutputStream out) {
    HelpFormatter help = new HelpFormatter();
    PrintWriter writer = new PrintWriter(out);
    final int width = 80;
    final int leftPad = 4;
    final int descPad = 8;
    help.printHelp(
      writer, width,
      "merge_chunks <chunk_merge_file> <index_merge_file> <output directory>",
      "Merges chunk files listed in <chunk_merge_file>, storing " +
      "results into the given output directory. Each line of this " +
      "CSV file must consist of a comma separated list of distinct load " +
      "chunk file names, i.e. chunk files produced by create_chunks - no " +
      "merged chunk file names may be included. One merged chunk file is " +
      "produced for each line; all the files on a line must contain " +
      "disjoint point sets for a common region (chunk) of the unit sphere." +
      "\n\n" +
      "The <index_merge_file> is a list of load chunk index file names, i.e. " +
      "chunk index files produced by create_chunks - no merged index file " +
      "names may be included. One file name is expected per line. These " +
      "files are also merged into the output directory. The chunk index " +
      "file for every load (invocation of create_chunks) to become a part " +
      "of the merged index should be listed." +
      "\n\n" +
      "This is notably different from <chunk_merge_file>, which should only " +
      "contain entries for chunks that must actually be merged. This is " +
      "typically a small subset of the total number of chunks in an index, " +
      "since loads are expected to have good spatial locality. Building a " +
      "complete view of all chunks in a merged index (by copying or " +
      "hard/soft linking to previously merged chunks) is not handled by " +
      "this program.",
      options, leftPad, descPad, "");
    writer.flush();
  }

  private static void checkChunkIndexFileName(String name) {
    if (!name.endsWith("index.ci")) {
      System.err.println(
        "Chunk index files to merge must all be named index.ci");
      System.exit(2);
    }
  }

  /**
   * Launches MergeChunks map-reduce jobs.
   */
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Options opts = buildOptions();
    GenericOptionsParser parser = new GenericOptionsParser(conf, opts, args);
    CommandLine cmd = parser.getCommandLine();
    if (cmd.hasOption("help")) {
      printUsage(opts, System.out);
      System.exit(0);
    }
    String[] names = parser.getRemainingArgs();
    if (names.length != 3) {
      System.err.println(
        "A chunk merge file, chunk index merge file, and output " +
        "directory must be specified. Use -help for usage details.");
      System.exit(2);
    }
    // extract paths from input
    Path chunkMergeFile = new Path(names[0]);
    Path indexMergeFile = new Path(names[1]);
    Path outputDir = new Path(names[2]);
    FileSystem fs = outputDir.getFileSystem(conf);
    // read and parse the index merge file
    ArrayList<Path> indexPaths = new ArrayList<Path>();
    FSDataInputStream inStream =
      indexMergeFile.getFileSystem(conf).open(indexMergeFile);
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(
        new InputStreamReader(inStream, Charset.forName("UTF-8")));
      for (String line = reader.readLine(); line != null;
           line = reader.readLine()) {
        indexPaths.add(new Path(line));
      }
    } finally {
      inStream.close();
    }
    if (indexPaths.size() < 2) {
      System.err.println(
        "The chunk index merge file must contain paths for at " +
        "least 2 load chunk index files");
      System.exit(2);
    }

    Path p = indexPaths.get(0);
    ChunkIndex index = new ChunkIndex(p, p.getFileSystem(conf));
    index.storeConfiguration(conf);

    // setup map-only job to merge chunk index files
    Job job = new Job(conf, "ipac.stk.mapreduce.job.MergeChunks");
    job.setNumReduceTasks(0);
    job.setJarByClass(MergeChunks.class);
    job.setInputFormatClass(OneLineInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setMapperClass(MergeMapper.class);
    job.setMapOutputKeyClass(NullWritable.class);
    job.setMapOutputValueClass(NullWritable.class);
    FileInputFormat.addInputPath(job, chunkMergeFile);
    FileOutputFormat.setOutputPath(job, outputDir);
    try {
      // launch job and wait for completion
      if (!job.waitForCompletion(true)) {
        System.exit(1);
      }
      // merge chunk index files together
      for (int i = 1; i < indexPaths.size(); ++i) {
        p = indexPaths.get(i);
        ChunkIndex other = new ChunkIndex(p, p.getFileSystem(conf));
        index.merge(other);
      }
      // write out merged chunk index file
      Path outputFile = ChunkIndex.indexPath(outputDir, true);
      FSDataOutputStream outStream = fs.create(outputFile, false);
      try {
        index.write(outStream);
      } finally {
        outStream.close();
      }
    } catch (Exception ex) {
      // delete the output directory and all its contents
      try {
        fs.delete(outputDir, true);
      } catch (Exception nex) {
        // swallow
      }
      throw ex;
    }
    System.exit(0);
  }
}
