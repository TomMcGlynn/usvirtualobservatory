using System;
using System.Collections;
using System.Data;
using System.Data.SqlClient;
using tapLib.Args;

namespace tapLib.Exec
{
    /// <summary>
    /// This class executes a single SQL query based on the given SQL statement
    /// and connection string.  A new instance is created for each new query
    /// but the same instance can be executed multiple times with the same query.
    /// A new dataset is created each time the class is executed.
    /// The class is thread-capable and works with the ThreadedQueryExecutor.
    /// 
    /// </summary>
    public class DatabaseTableQuery : IEnumerable, IWorker {
        private readonly String _connection;
        private readonly String _sqlQuery;
        private readonly DiagArg _diagArg;
        private Boolean _isValid = true;
        private String _problem = String.Empty;
        private Boolean _isExecuted;
        private DataTable _result;
        private int _id = 1;

        // Properties
        // This is part of the IWorker interface (along with id above)
        public int id { get { return _id; } set { _id = value; } }
        public DataTable result { get { return _result; } }
        public Worker worker { get { return Execute; } }

        // Others
        public String connection { get { return _connection; } }
        public String query { get { return _sqlQuery; } }
        public Boolean isExecuted { get { return _isExecuted; } }
        public Boolean isValid { get { return _isValid; } }
        public String problem { get { return _problem; } }
        private DiagArg diag { get { return _diagArg; } }

        public DatabaseTableQuery(String connection, String sqlQuery) : this(connection, sqlQuery, DiagArg.DEFAULT) {}

        public DatabaseTableQuery(String connection, String sqlQuery, DiagArg diagArg) : this(1, connection, sqlQuery, diagArg) {}

        public DatabaseTableQuery(int id, String connection, String sqlQuery, DiagArg diagArg) {
            if (connection == null) throw new ArgumentNullException("connection");
            if (sqlQuery == null) throw new ArgumentNullException("sqlQuery");
            _id = id;
            _connection = connection;
            _sqlQuery = sqlQuery;
            _diagArg = diagArg;
        }

        /// <summary>
        /// This method executes an SQL method on an SQL database
        /// A contained DataSet is available after a successful query.  It is available
        /// with the "result" property.
        /// If the Execute method fails, the property "isValid" is false, Execute returns false
        /// and the "problem" property contains an error.
        /// </summary>
        /// <returns>True if successful, otherwise false if the query failed.</returns>
        public void Execute() {
            if (diag.isOn()) Console.WriteLine("SQL is: {0}", _sqlQuery);
            using (var conn = new SqlConnection(_connection)) {
                conn.Open();
                using (var cmd = new SqlCommand(_sqlQuery, conn)) {
                    try {
                        _result = new DataTable(_id.ToString());
                        cmd.CommandType = CommandType.Text;
                        var da = new SqlDataAdapter(cmd);
                        da.Fill(_result);
                    }
                    catch (SqlException ex) {
                        _problem = ex.ToString();
                        _isValid = false;
                        return;
                    }
                }
            }
            _isExecuted = true;
            _isValid = true;
        }

        // Convenience Methods
        // Returns the number of rows in the final result
        public int Count() {
            // Should always succeed because DataSet is created empty with 0 rows
            return _result.Rows.Count;
        }

        /// <summary>
        /// Access rows through an Enumerator.  Returned class is System.Data.DataRow.
        /// </summary>
        /// <returns></returns>
        public IEnumerator GetEnumerator() {
            return _result.Rows.GetEnumerator();
        }

    }
}