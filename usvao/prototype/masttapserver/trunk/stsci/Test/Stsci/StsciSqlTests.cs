using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using NUnit.Framework;
using tapLib.Config;
using tapLib.Db;
using tapLib.Exec;

namespace tapLib.Test.Stsci {
    [TestFixture]
    public class StsciSqlTests {
        private String TEST_FILE_LOCATION = "";
        private const string REL_LOCATION = @"\..\..\KimTableConfig.xml";

        [SetUp]
        public void testSetup() {
            // Set up the path to the test file
            String dir = Directory.GetCurrentDirectory();
            TEST_FILE_LOCATION = Path.GetFullPath(dir + REL_LOCATION);
            TapConfiguration.setConfigFilePath(TEST_FILE_LOCATION);
        }

        [Test]
        public void testHLAPosWrapping() {
            const string sql1 = "SELECT {0} " +
                                "FROM (" +
                                "SELECT DISTINCT t0.regionid " +
                                "FROM dbo.fRegionsContainingPointEq_htm({1}, {2}, '{3}', {4}) AS t0" +
                                ") AS t1 " +
                                "CROSS JOIN dbo.Region AS t2 " +
                                "CROSS JOIN dbo.hlascience AS t3 " +
                                "WHERE (t2.id = t3.objid) AND (t2.regionid = t1.regionid) AND {5}";

            const string tableName = "hlascience";
            const string dalSelect = "pi_last_name, prop_id, exposuretime";
            const string dalWhere = "(exposuretime > 400)";
            const string ra = "202.48";
            const string dec = "47.23";
            const string r = "4.5";
            const string level = "ACS/Level1 WFPC2/Stack";

            string sql = String.Format(sql1, dalSelect, ra, dec, level, r, dalWhere);
            String databaseName = TapConfiguration.Instance.DatabaseForTable(tableName);
            String connection = TapConfiguration.Instance.ConnectionString(databaseName);
            DatabaseTableQuery q = new DatabaseTableQuery(connection, sql);

            q.Execute();
            if (!q.isValid)
            {
                Console.WriteLine("Query Failed: " + q.problem);
                return;
            }
            Console.WriteLine("Result Count: " + q.Count());
            DataTable result = q.result;
            _printResult(result);

        }

        [Test]
        public void testCatalogWrapping() {
            const string sql1 = "SELECT {0} " +
                                "FROM PhotoPrimary AS t0 INNER JOIN dbo.fGetNearbyObjEq({1}, {2}, {3}) AS t1 ON t0.objID=t1.objID " +
                                "WHERE {4} ORDER BY distance";

            const string tableName = "PhotoPrimary";
            const string dalSelect = "ra, dec, JMag, VMag";
            const string dalWhere = "VMag < 99";
            const string ra = "202.48";
            const string dec = "47.23";
            const string r = "4.5";

            string sql = String.Format(sql1, dalSelect, ra, dec, r, dalWhere);
            String databaseName = TapConfiguration.Instance.DatabaseForTable(tableName);
            String connection = TapConfiguration.Instance.ConnectionString(databaseName);
            DatabaseTableQuery q = new DatabaseTableQuery(connection, sql);

            q.Execute();
            if (!q.isValid) {
                Console.WriteLine("Query Failed: " + q.problem);
                return;
            }
            Console.WriteLine("Result Count: " + q.Count());
            DataTable result = q.result;
            _printResult(result);

        }

        private static void _printResult(DataTable r)
        {
            List<String> columnNames = new List<String>();
            int columnCount = r.Columns.Count;
            for(int i=0; i<columnCount; i++) 
            {
                columnNames.Add(r.Columns[i].ColumnName);
            }
            foreach (String each in columnNames)
            {
                Console.Write(String.Format("{0,-25}", each));
            }
            Console.WriteLine();
            foreach (DataRow each in r.Rows) {
                foreach (String columnName in columnNames)
                {
                    Console.Write(String.Format("{0,-25}", each[columnName]));
                }
                Console.Write("\n");
            }
            Console.WriteLine("DONE");
        }

        [Test]
        public void testHLARegionQOnly() {
            const string sql1 = "SELECT DISTINCT t0.regionid " +
                                "FROM dbo.fRegionsContainingPointEq_htm({0}, {1}, '{2}', {3}) AS t0";                          

            const string ra = "202.48";
            const string dec = "47.23";
            const string r = "4.5";
            const string level = "ACS/Level1";

            String sql = String.Format(sql1, ra, dec, level, r);
            DatabaseTableQuery q = new DatabaseTableQuery(DbConnections.getConnectionInfo(DbConnections.DB_NAME.HLATEST).connection, sql);

            q.Execute();
            if (!q.isValid) {
                Console.WriteLine("Query Failed: " + q.problem);
                return;
            }
            Console.WriteLine("Result Count: " + q.Count());
            foreach (var each in q) {
                Console.WriteLine("Result: " + each);
            }

        }
    }
}
