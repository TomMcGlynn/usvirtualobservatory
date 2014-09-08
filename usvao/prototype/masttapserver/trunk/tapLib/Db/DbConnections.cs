using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace tapLib.Db
{
    /// <summary>
    /// This is a utility class to contain the servers and their connection strings.
    /// It may be augmented to provide DataContexts at some future time.
    /// </summary>
    public class DbConnections {
        static private List<DbConnectionInfo> _connections = new List<DbConnectionInfo>();
        
        public class DbConnectionInfo {
            private DB_NAME _name;
            private String _connection;

            // properties
            public DB_NAME name { get { return _name; } }
            public String connection { get { return _connection; } }

            internal DbConnectionInfo(DB_NAME name, String connection) {
                _name = name;
                _connection = connection;
            }            
        }       

        // Indicates which DB to open
        public enum DB_NAME {
            DSS,
            HLADEV,
            HLATEST
        }

        public static DbConnectionInfo getConnectionInfo(DB_NAME dbName) {            
            return _connections.Where(info => info.name.Equals(dbName)).Single();
        }

        static DbConnections() {
        }

    }
}