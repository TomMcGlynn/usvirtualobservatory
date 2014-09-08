using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using tapLib.Args;

namespace tapLib.Exec {
    /// <summary>
    /// This class executes a set of DatabaseTableQuery instances in a ThreadPool.
    /// The class is created with a list of DatabaseTableQuery instances.  When
    /// the Execute method is called, all the queries are executed in their own
    /// thread under the policy of the ThreadPool.  Execute waits until all the
    /// threads have completed (successfully or in error).
    /// </summary>
    public class ThreadedQueryExecutor {
        private readonly IList<IWorker> _queries;
        private readonly int _numberOfQueries;
        private readonly DiagArg _diagArg;

        // Properties
        public int numberOfQueries { get { return _numberOfQueries; } }
        private DiagArg diag { get { return _diagArg; } }

        public ThreadedQueryExecutor(IList<IWorker> queries) : this(queries, DiagArg.DEFAULT) {}

        public ThreadedQueryExecutor(IList<IWorker> queries, DiagArg diagArg) {
            _queries = queries;
            _numberOfQueries = _queries.Count();
            _diagArg = diagArg;
        }

        public void Execute() {
            // One event is used for each query
            ManualResetEvent[] doneEvents = new ManualResetEvent[numberOfQueries];

            try {
                if (diag.isOn()) Console.WriteLine("launching {0} tasks...", numberOfQueries);
                // Configure and launch threads using ThreadPool:
                for (var i = 0; i < numberOfQueries; i++) {
                    IWorker q = _queries[i];                    
                    doneEvents[i] = new ManualResetEvent(false);
                    ThreadExecuteData ted = new ThreadExecuteData(q.id, q.worker, doneEvents[i]);
                    ThreadPool.QueueUserWorkItem(ThreadPoolCallback, ted);
                }

                // Wait for all threads in pool to calculation...
                WaitHandle.WaitAll(doneEvents);
                if (diag.isOn()) Console.WriteLine("All queries are complete.");
            }
            finally {
                // GC will catch these, but this speeds things up I guess
                foreach (ManualResetEvent each in doneEvents) each.Close();
            }
        }

        // Wrapper method for use with thread pool.
        public void ThreadPoolCallback(Object threadContext) {
            ThreadExecuteData data = (ThreadExecuteData)threadContext;

            ManualResetEvent doneEvent = data.evt;
            if (diag.isOn()) {
                Console.WriteLine("thread {0} started...", data.id);
            }
            data.worker();
            if (diag.isOn()) {
                Console.WriteLine("thread {0} result calculated...", data.id);
            }
            doneEvent.Set();
        }

        public class ThreadExecuteData {
            private readonly ManualResetEvent _doneEvent;
            private readonly int _threadID;
            private readonly Worker _worker;

            // Properties
            public ManualResetEvent evt { get { return _doneEvent; } }
            public Worker worker { get { return _worker; } }

            public int id { get { return _threadID; } }

            public ThreadExecuteData(int threadID, Worker worker, ManualResetEvent doneEvent) {
                _threadID = threadID;
                _worker = worker;
                _doneEvent = doneEvent;
            }
        }
    }
}
