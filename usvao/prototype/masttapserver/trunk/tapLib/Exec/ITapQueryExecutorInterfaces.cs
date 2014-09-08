using System;
using System.Collections.Generic;
using System.Data;

namespace tapLib.Exec
{
    /// <summary>
    /// This interface is implemented by classes that execute IWorkers.
    /// There is only one class currently implementing this:  ThreadedQueryExecutor.
    /// </summary>
    public interface ITapQueryExecutor {
        /// <summary>
        /// This method executes the query/ies and fills the xresults.  It starts all of 
        /// </summary>
        /// <returns></returns>
        Boolean Execute();

        /// <summary>
        /// This method returns the result of the entire query as a DataSet with a DataTable
        /// for each query.  The name of the dataset is a number: 1, 2, 3 through the number
        /// of positions in the query.
        /// </summary>
        DataSet results { get; }
    }
    /**
    /// <summary>
    /// This interface is implemented by classes that provide clients with one query result.
    /// Each implementation should provide an id (so the result can be identified with a position
    /// in the result votable) and the DataTable result.
    /// There is only one class currently implementing this:  DatabaseTableQuery
    /// </summary>
    public interface IOneQueryResult {
        int id { get; }
        DataTable result { get; }
    }
    **/

    /// <summary>
    /// The IWorker interface is implemented by classes that can be executed in the 
    /// ThreadedQueryExecutor.  A Worker is a delegate that takes no arguments and returns void.
    /// The id must be provided as an indentifier for threads.
    /// There is only one class currently implementing this:  DatabaseTableQuery
    /// </summary>
    public interface IWorker {
        Worker worker { get; }
        int id { get; }
        DataTable result { get; }
    }

    public delegate void Worker();

}