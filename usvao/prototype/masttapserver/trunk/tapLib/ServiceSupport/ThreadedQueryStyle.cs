using System;
using System.Collections.Generic;
using System.Data;
using tapLib.Args;
using tapLib.Config;
using tapLib.Db.ParamQuery;
using tapLib.Exec;

namespace tapLib.ServiceSupport
{
    /// <summary>
    /// This class shows how to implement multiple position queries using
    /// an injected Configuration, TapQueryArgs, and ISqlGenerator
    /// </summary>
    public class ThreadedQueryStyle : ITapQueryExecutor {
        private readonly TapQueryArgs _args;
        private readonly TapConfiguration _config;
        private readonly ISqlGeneratorFactory _generatorFactory;
        private List<DatabaseTableQuery> _queryList;
        private DataSet _result;

        // Property for ITapQueryExecutor
        public DataSet results { get { return _result; } }

        /// <summary>
        ///  This is present only to provide a list of IWorkers for execution
        /// </summary>
        private IList<IWorker> workers {
            get { return _queryList.ConvertAll(t => (IWorker)t); }
        }

        public ThreadedQueryStyle(TapConfiguration config, TapQueryArgs args, ISqlGeneratorFactory generatorFactory) {
            _config = config;
            _args = args;
            _generatorFactory = generatorFactory;
        }

        private void _setupPositions(ISqlGenerator generator, String connectionString) {
            // First build the list of queries to execute
            // Get a new list
            _queryList = new List<DatabaseTableQuery>(_args.pos.posCount);
            // Need to improve error handling
            int idCounter = 1;
            foreach (TapPos each in _args.pos.posList) {
                // This is needed to verify if there is a position associated with this query
                String sql = each.isEmpty ? generator.ToSQL() : generator.ToSQL(each, _args.size, _args.region, _args.mtime);

                DatabaseTableQuery dtq = new DatabaseTableQuery(idCounter++, connectionString, sql, DiagArg.ON);
                _queryList.Add(dtq);
            }
        }

        private void _setupADQL(String connectionString)
        {
            //tdower - can this ever be > 1 with adql statements?
            _queryList = new List<DatabaseTableQuery>(1);
            DatabaseTableQuery dtq = new DatabaseTableQuery(connectionString, _args.adql, DiagArg.ON);
            _queryList.Add(dtq);
        }

        public Boolean Execute() {
            // Get the connection string for the database
            // TODO: Need to use TD's config here
            String databaseName = _config.DatabaseForTable(_args.query.tableName);
            if (databaseName == null)
            {
                _args.query._AddProblem("Invalid table: " + _args.query.tableName + '.');
                return false;
            }

            String connectionString = _config.ConnectionString(databaseName);
            if (connectionString == null)
            {
                _args.query._AddProblem("Cannot configure connection to database for table: " + _args.query.tableName + '.');
                return false;
            }

            if ( _args.adql != null && _args.adql.Length > 0 )
            {
                _setupADQL(connectionString);
            }
            else
            {
                // Need errors here
                ISqlGenerator generator = _generatorFactory.create(_args.query.from);
                if (!generator.generateSQL(_args))
                {
                    _args.query._AddProblem("SQL generation failed.");
                    Console.WriteLine("Generation failed.");
                    return false;
                }

                // First build the list of queries to execute
                // Need to improve error handling
                _setupPositions(generator, connectionString);
            }

            // Now Execute them
            _result = new DataSet("TapQueryResultSet");
            ThreadedQueryExecutor threadedExecutor = new ThreadedQueryExecutor(workers);
            threadedExecutor.Execute();

            bool allvalid = true;
            foreach (DatabaseTableQuery each in _queryList) 
            {
                Console.WriteLine("For: " + each.id + " there are: " + each.result.Rows.Count  + " xresults.");
                _result.Tables.Add(each.result);
                if (each.isValid == false)
                {
                    allvalid = false;
                    _args.query.problems.Add(each.problem);
                }
            }
            return allvalid;
        }
    }
}