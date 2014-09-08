using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using System.Xml;
using System.Xml.Serialization;
using System.Collections;

using tapLib.Args.ParamQuery;

namespace tapLib.Config {
    /// <summary>
    /// This class provides a configuration database for the Tap Service.
    /// It provides convenient methods that allow the TAP service to validate
    /// requests according to what is supported in the service. blah blah
    /// TapConfiguration is implemented as a Singleton
    /// Access is through the TapConfiguration.Instance property.
    /// It's necessary to set the path to the XML config file before the first
    /// Instance access with the TapConfiguration.setConfigFilePath method.
    /// </summary>
    public class TapConfiguration {
        static private String _filePath;
        private static NewConfig _config;

        // Public static property
        public static TapConfiguration Instance { get { return SingletonCreator.instance; } }

        // Other properties

        // private constructor
        private TapConfiguration()
        {
            if (_filePath == null) throw new NoNullAllowedException("Config file name");

            try
            {
                XmlTextReader xmlReader = new XmlTextReader(_filePath);
                XmlSerializer xs = new XmlSerializer(typeof (NewConfig));
                _config = (NewConfig) xs.Deserialize(xmlReader);
                xmlReader.Close();
            } catch (Exception ex) {
                Console.WriteLine("Load of config file: " + _filePath);
                Console.WriteLine("Failed with exception: " + ex);
                throw new FileLoadException("Config file load failed: " + ex);
            }
        }

        public NewConfig config { get { return _config; } }

        // Nested class for lazy initialization
        class SingletonCreator {
            //static SingletonCreator() {}

            // Private object instantiated with private constructor
            internal static readonly TapConfiguration instance = new TapConfiguration();
        }

        /// <summary>
        /// Set the path to the configuration file.  Should be set before referencing
        /// Instance or an exception will be thrown
        /// </summary>
        /// <param name="filePath"></param>
        public static void setConfigFilePath(String filePath) {
            _filePath = filePath;
        }

        /// <summary>
        /// Returns the connection string for a database with name \"databaseName\"
        /// </summary>
        /// <param name="databaseName"></param>
        /// <returns>database name or null if database isn't supported</returns>
        public String ConnectionString(String databaseName) {
            Database db;
            if (!_isSupportedDatabase(databaseName, out db)) return null;

            return db.connection.value;
        }

        /// <summary>
        /// Returns the number of configured databases
        /// </summary>
        /// <returns></returns>
        public int NumberDatabases() {
            return _config.databases.Count();
        }

        /// <summary>
        /// Returns the number of configured tables in the database with name databaseName.
        /// </summary>
        /// <param name="databaseName"></param>
        /// <returns>number of databases or -1 if the database is not supported</returns>
        public int NumberTables(String databaseName) {
            Database db;
            if (!_isSupportedDatabase(databaseName, out db)) return -1;

            return db.tables.Count();
        }

        /// <summary>
        /// Returns true if the database is supported by this service
        /// according to the configuration file.
        /// </summary>
        /// <param name="databaseName"></param>
        /// <returns></returns>
        public Boolean isSupportedDatabase(String databaseName) {
            Database db;
            return _isSupportedDatabase(databaseName, out db);
        }

        /// <summary>
        /// Returns true if the table with tableName is supported in database with
        /// databaseName.
        /// </summary>
        /// <param name="databaseName"></param>
        /// <param name="tableName"></param>
        /// <returns>True if supported or false if table or database isn't supported</returns>
        public Boolean isSupportedTable(String databaseName, String tableName) {
            Database db;
            if (!_isSupportedDatabase(databaseName, out db)) return false;
            if (db.tables.SingleOrDefault(t => t.name == tableName) != null) return true;
            else
                return (db.tables.SingleOrDefault(t => t.internalName == tableName) != null);
        }

        /// <summary>
        /// This method takes a database name and tries to find a database to host it
        /// This assumes somewhat that the names of tables published are unique within a service
        /// </summary>
        /// <param name="tableName"></param>
        /// <returns></returns>
        public String DatabaseForTable(String tableName) {
            foreach (Database each in _config.databases)
            {
                if (_getTable(each, tableName) != null) return each.database;
            }

            foreach (Database each in _config.databases)
            {
                if (_getTableByInternalName(each, tableName) != null) return each.database;
            }

            return null;
        }

        /// <summary>
        ///  Returns a List of standard columns for a specific database and table
        /// </summary>
        /// <param name="databaseName"></param>
        /// <param name="tableName"></param>
        /// <returns>List of database column names or the empty list</returns>
        public List<String> StdColumns(String databaseName, String tableName) {
            return _getColumnNames(databaseName, tableName, true);
        }

        /// <summary>
        /// Returns a list of All column names for a specific database and table
        /// </summary>
        /// <param name="databaseName"></param>
        /// <param name="tableName"></param>
        /// <returns>List of all supported column names or an empty List</returns>
        public List<String> AllColumns(String databaseName, String tableName) {
            return _getInternalColumnNamesByAlias(databaseName, tableName, false);
        }

        // Private methods
        private static Database _getDatabase(String databaseName) {
            return _config.databases.SingleOrDefault(d => d.database == databaseName);
        }

        private static Boolean _isSupportedDatabase(String databaseName, out Database db) {
            db = _getDatabase(databaseName);
            return db != null;
        }

        private static Table _getTable(Database db, String tableName) {
            return db.tables.SingleOrDefault(t => t.name == tableName);
        }

        private static Table _getTableByInternalName(Database db, String tableName)
        {
            return db.tables.SingleOrDefault(t => t.internalName == tableName);
        }

        //uses table and column aliases where defined.
        private static List<String> _getInternalColumnNamesByAlias(String databaseName, String tableName, Boolean stdOnly)
        {
            List<String> fields = new List<string>();
            Database db;
            if (!_isSupportedDatabase(databaseName, out db)) return fields;

            Table tbl = _getTable(db, tableName);
            if (tbl == null) tbl = _getTableByInternalName(db, tableName);            
            if (tbl == null)
                return fields;

            foreach (Column each in tbl.columns)
            {
                if (stdOnly)
                {
                    if (each.std)
                    {
                        if (each.internalName != string.Empty)
                            fields.Add(each.internalName);
                        else
                            fields.Add(each.name);
                    }
                }
                else
                {
                    if (each.internalName != string.Empty)
                        fields.Add(each.internalName);
                    else
                        fields.Add(each.name);
                }
            }
            return fields;
        }

        private static List<String> _getColumnNames(String databaseName, String tableName, Boolean stdOnly) {
            List<String> fields = new List<string>();
            Database db;
            if (!_isSupportedDatabase(databaseName, out db)) return fields;

            Table tbl = _getTable(db, tableName);
            if (tbl == null) tbl = _getTableByInternalName(db, tableName);
            if (tbl == null) return fields;
            foreach (Column each in tbl.columns) {
                if (stdOnly)
                {
                    if (each.std)
                    {
                        fields.Add(each.name);
                    }
                }
                else {
                    fields.Add(each.name);
                }
            }
            return fields;
        }

        private List<string> GetTableNamesConstraint(QueryArg args)
        {
            List<string> tables = new List<string>();
            List<FieldConstraintGroup> constraints = args.constraintGroups();

            foreach (FieldConstraintGroup constraint in constraints)
            {
                if (constraint.fieldName.ToUpper() == "TABLENAME")
                {
                    foreach (string name in constraint.constraints)
                        tables.Add(name);
                }
            }

            return tables;
        }

        //aliases are not case-sensitive. Slow in the case of many tables/db's, may want to fix.
        public Boolean _getTableNameByAlias(String alias, ref String internalName)
        {
            Boolean success = false;
            alias = alias.ToUpper();

            foreach (Database db in _config.databases)
            {
                foreach (Table tbl in db.tables)
                {
                    if (tbl.name.ToUpper() == alias)
                    {
                        internalName = tbl.internalName;
                        success = true;
                        break;
                    }
                }
            }
            return success;
        }

        //aliases are not case-sensitive
        public Boolean _getColumnNameByAlias(String dbName, String internalTableName, String alias, ref String internalColumnName)
        {
            Boolean success = false;
            alias = alias.ToUpper();

            Table tbl = _getTableByInternalName(_getDatabase(dbName), internalTableName);
            foreach (Column col in tbl.columns)
            {
                if (col.name.ToUpper() == alias)
                {
                    internalColumnName = col.internalName;
                    success = true;
                    break;
                }
            }
            return success;
        }

        public bool TestDBConnections()
        {
            tapLib.Db.ConnectionTester test = new tapLib.Db.ConnectionTester();
            foreach (Database db in _config.databases)
            {
                if (test.Test(db.connection.value) == false)
                {
                    return false;
                }
            }
            return true;
        }

        private void QueryTables(QueryArg args, ref DataTable build)
        {
            DataColumn schemaColumn = new DataColumn();
            schemaColumn.DataType = System.Type.GetType("System.String");
            schemaColumn.ColumnName = "schema_name";
            DataColumn nameColumn = new DataColumn();
            nameColumn.DataType = System.Type.GetType("System.String");
            nameColumn.ColumnName = "table_name";
            DataColumn typeColumn = new DataColumn();
            typeColumn.DataType = System.Type.GetType("System.String");
            typeColumn.ColumnName = "table_type";
            DataColumn descColumn = new DataColumn();
            descColumn.DataType = System.Type.GetType("System.String");
            descColumn.ColumnName = "description";
            DataColumn utypeColumn = new DataColumn();
            utypeColumn.DataType = System.Type.GetType("System.String");
            utypeColumn.ColumnName = "utype";

            bool all = false;
            if (args.select.ToLower().Contains("$std") || args.select.ToLower().Contains("$all")) all = true;

            if (all || args.select.ToLower().Contains("schema_name"))
                build.Columns.Add(schemaColumn);
            if (all || args.select.ToLower().Contains("table_name"))
                build.Columns.Add(nameColumn);
            if (all || args.select.ToLower().Contains("description"))
                build.Columns.Add(descColumn);
            if (all || args.select.ToLower().Contains("table_type"))
                build.Columns.Add(typeColumn);
            if (all || args.select.ToLower().Contains("utype"))
                build.Columns.Add(utypeColumn);
            
            if (build.Columns.Count == 0)
            {
                args._AddProblem("invalid column selection from TAP_SCHEMA.tables");
                return;
            }

            List<string> tableNames = GetTableNamesConstraint(args);
            foreach (Database db in _config.databases)
            {
                foreach (Table table in db.tables)
                {
                    if (tableNames.Count == 0 || tableNames.Contains(table.name))
                    {
                        DataRow newRow = build.NewRow();

                        if (build.Columns.Contains("schema_name"))
                            newRow["schema_name"] = db.schemaName;
                        if (build.Columns.Contains("table_name"))
                            newRow["table_name"] = table.name;
                        if (build.Columns.Contains("description"))
                            newRow["description"] = table.description;
                        if (build.Columns.Contains("table_type"))
                            newRow["table_type"] = table.tableType;
                        if (build.Columns.Contains("utype"))
                            newRow["utype"] = table.utype;

                        build.Rows.Add(newRow);
                    }
                }               
            }
        }

        private void QueryTableColumns(QueryArg args, ref DataTable build)
        {
            #region all possible datacolumns
            DataColumn nameColumn = new DataColumn();
            nameColumn.DataType = System.Type.GetType("System.String");
            nameColumn.ColumnName = "column_name";
            DataColumn tableNameColumn = new DataColumn();
            tableNameColumn.DataType = System.Type.GetType("System.String");
            tableNameColumn.ColumnName = "table_name";
            DataColumn descriptionColumn = new DataColumn();
            descriptionColumn.DataType = System.Type.GetType("System.String");
            descriptionColumn.ColumnName = "description";
            DataColumn unitColumn = new DataColumn();
            unitColumn.DataType = System.Type.GetType("System.String");
            unitColumn.ColumnName = "unit";
            DataColumn ucdColumn = new DataColumn();
            ucdColumn.DataType = System.Type.GetType("System.String");
            ucdColumn.ColumnName = "ucd";
            DataColumn utypeColumn = new DataColumn();
            utypeColumn.DataType = System.Type.GetType("System.String");
            utypeColumn.ColumnName = "utype";
            DataColumn datatypeColumn = new DataColumn();
            datatypeColumn.DataType = System.Type.GetType("System.String");
            datatypeColumn.ColumnName = "datatype";
            DataColumn arraysizeColumn = new DataColumn();
            arraysizeColumn.DataType = System.Type.GetType("System.String");
            arraysizeColumn.ColumnName = "size";
            DataColumn primaryColumn = new DataColumn();
            primaryColumn.DataType = System.Type.GetType("System.Boolean");
            primaryColumn.ColumnName = "primary";
            DataColumn indexedColumn = new DataColumn();
            indexedColumn.DataType = System.Type.GetType("System.Boolean");
            indexedColumn.ColumnName = "indexed";
            DataColumn stdColumn = new DataColumn();
            stdColumn.DataType = System.Type.GetType("System.Boolean");
            stdColumn.ColumnName = "standard";

            #endregion

            #region known select options
            bool all = false;
            if (args.select.ToLower().Contains("$std") || args.select.ToLower().Contains("$all")) all = true;

            if (all || args.select.ToLower().Contains("column_name"))
                build.Columns.Add(nameColumn);
            if (all || args.select.ToLower().Contains("table_name"))
                build.Columns.Add(tableNameColumn);
            if (all || args.select.ToLower().Contains("description"))
                build.Columns.Add(descriptionColumn);
            if (all || args.select.ToLower().Contains("unit"))
                build.Columns.Add(unitColumn);
            if (all || args.select.ToLower().Contains("ucd"))
                build.Columns.Add(ucdColumn);
            if (all || args.select.ToLower().Contains("utype"))
                build.Columns.Add(utypeColumn);
            if (all || args.select.ToLower().Contains("datatype"))
                build.Columns.Add(datatypeColumn);
            if (all || args.select.ToLower().Contains("size"))
                build.Columns.Add(arraysizeColumn);
            if (all || args.select.ToLower().Contains("primary"))
                build.Columns.Add(primaryColumn);
            if (all || args.select.ToLower().Contains("indexed"))
                build.Columns.Add(indexedColumn);
            if (all || args.select.ToLower().Contains("standard") || args.select.ToLower().Contains("std"))
                build.Columns.Add(stdColumn);
            
            #endregion

            if (build.Columns.Count == 0)
            {
                args._AddProblem("invalid column selection from TAP_SCHEMA.columns");
                return;
            }


            #region now build the rows as requested.
            List<string> tableNames = GetTableNamesConstraint(args);
            foreach (Database db in config.databases)
            {
                foreach (Table table in db.tables)
                {
                    if (tableNames.Count == 0 || tableNames.Contains(table.name))
                    {

                        foreach (Column col in table.columns)
                        {
                            DataRow newRow = build.NewRow();

                            //required elements and elements with defaults.
                            if (build.Columns.Contains("column_name"))
                                newRow["column_name"] = col.name;
                            if (build.Columns.Contains("table_name"))
                                newRow["table_name"] = table.name;
                            if (build.Columns.Contains("description"))
                                newRow["description"] = col.description;
                            if (build.Columns.Contains("primary"))
                                newRow["primary"] = col.primary;
                            if (build.Columns.Contains("indexed"))
                                newRow["indexed"] = col.indexed;
                            if (build.Columns.Contains("standard"))
                                newRow["standard"] = col.std;

                            //optional elements without defaults
                            if (build.Columns.Contains("ucd"))
                            {
                                if (col.ucd != null)
                                    newRow["ucd"] = col.ucd;
                                else
                                    newRow["ucd"] = String.Empty;
                            }
                            if (build.Columns.Contains("utype"))
                            {
                                if (col.utype != null)
                                    newRow["utype"] = col.utype;
                                else
                                    newRow["utype"] = String.Empty;
                            }
                            if (build.Columns.Contains("datatype"))
                            {
                                if (col.datatype != null)
                                    newRow["datatype"] = col.datatype;
                                else
                                    newRow["datatype"] = String.Empty;
                            }
                            if (build.Columns.Contains("unit"))
                            {
                                if (col.unit != null)
                                    newRow["unit"] = col.unit;
                                else
                                    newRow["unit"] = string.Empty;
                            }
                            if (build.Columns.Contains("size"))
                            {
                                if (col.arraysize != null)
                                    newRow["size"] = col.arraysize;
                                else
                                    newRow["size"] = string.Empty;
                            }

                            build.Rows.Add(newRow);
                        }
                    }
                }
            }
            #endregion
        }

        private void QueryTableSchemas(QueryArg args, ref DataTable build)
        {
            DataColumn nameColumn = new DataColumn();
            nameColumn.DataType = System.Type.GetType("System.String");
            nameColumn.ColumnName = "schema_name";

            DataColumn descColumn = new DataColumn();
            descColumn.DataType = System.Type.GetType("System.String");
            descColumn.ColumnName = "description";

            DataColumn typeColumn = new DataColumn();
            typeColumn.DataType = System.Type.GetType("System.String");
            typeColumn.ColumnName = "utype";


            #region known select options
            bool all = false;
            if (args.select.ToLower().Contains("$std") || args.select.ToLower().Contains("$all")) all = true;
            if (all || args.select.ToLower().Contains("schema_name"))
                 build.Columns.Add(nameColumn);
            if (all || args.select.ToLower().Contains("description"))
                 build.Columns.Add(descColumn);
            if (all || args.select.ToLower().Contains("utype"))
                 build.Columns.Add(typeColumn);            
            #endregion

            #region now build the table
            foreach (Database db in _config.databases)
            {
                DataRow newRow = build.NewRow();
                if (build.Columns.Contains("schema_name"))
                    newRow["schema_name"] = db.schemaName;
                if (build.Columns.Contains("description"))
                    newRow["description"] = db.description;
                if (build.Columns.Contains("utype"))
                    newRow["utype"] = db.utype;

                build.Rows.Add(newRow);
            }
            #endregion
        }

        private void QueryTableKeys(QueryArg args, ref DataTable build)
        {
            DataColumn idColumn = new DataColumn();
            idColumn.DataType = System.Type.GetType("System.String");
            idColumn.ColumnName = "key_id";

            DataColumn fromColumn = new DataColumn();
            fromColumn.DataType = System.Type.GetType("System.String");
            fromColumn.ColumnName = "from_table";

            DataColumn targetColumn = new DataColumn();
            targetColumn.DataType = System.Type.GetType("System.String");
            targetColumn.ColumnName = "target_table";

            DataColumn typeColumn = new DataColumn();
            typeColumn.DataType = System.Type.GetType("System.String");
            typeColumn.ColumnName = "utype";

            DataColumn descColumn = new DataColumn();
            descColumn.DataType = System.Type.GetType("System.String");
            descColumn.ColumnName = "description";

            bool all = false;
            if (args.select.ToLower().Contains("$std") || args.select.ToLower().Contains("$all")) all = true;

            if (all || args.select.ToLower().Contains("key_id"))
                build.Columns.Add(idColumn);
            if (all || args.select.ToLower().Contains("from_table"))
                build.Columns.Add(fromColumn);
            if (all || args.select.ToLower().Contains("target_table"))
                build.Columns.Add(targetColumn);
            if (all || args.select.ToLower().Contains("utype"))
                build.Columns.Add(typeColumn);
            if (all || args.select.ToLower().Contains("description"))
                build.Columns.Add(descColumn);
        }

        public DataSet ExecuteConfigQuery(QueryArg args)
        {
            DataSet results = new DataSet("TapQueryResultSet");
            DataTable build = results.Tables.Add();

            if (args.from.ToUpper().Contains("TAP_SCHEMA.TABLES"))
                QueryTables(args, ref build);
            else if (args.from.ToUpper().Contains("TAP_SCHEMA.COLUMNS"))
                QueryTableColumns(args, ref build);
            else if (args.from.ToUpper().Contains("TAP_SCHEMA.SCHEMAS"))
                QueryTableSchemas(args, ref build);
            else if (args.from.ToUpper().Contains("TAP_SCHEMA.KEYS"))
                QueryTableKeys(args, ref build);
            else
            {
                args._AddProblem("invalid TAP_SCHEMA. argument");
                results.Tables.Remove(build);
            }


            return results;
        }

        public void  ExecuteVOSIConfigQuery(ref DataSet tables, ref DataSet columns)
        {
            tables = new DataSet("TapQueryTableNames");
            DataTable buildnames = tables.Tables.Add();
            QueryArg tablenameargs = new QueryArg("request=doquery&select=$ALL&lang=ADQL&QUERY=");
            QueryTables(tablenameargs, ref buildnames);

            columns = new DataSet("TapQueryResultSet");
            DataTable build = columns.Tables.Add();
            QueryArg args = new QueryArg("request=doquery&select=$ALL&lang=ADQL&QUERY=");
            QueryTableColumns(args, ref build);

            return;
        }
    }
}
