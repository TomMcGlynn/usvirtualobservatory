using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using NUnit.Framework;
using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Config;
using tapLib.Db.ParamQuery;
using tapLib.Exec;
using tapLib.ServiceSupport;
using tapLib.Stsci;

namespace tapLib.Test.Service {
    [TestFixture]
    public class TapQueryExecutorTests {
        private String TEST_FILE_LOCATION = "";
        // This would need to be changed if directories move
        private const string REL_LOCATION = @"\..\..\KimTableConfig.xml";
        private ISqlGeneratorFactory _generatorFactory;

        [SetUp]
        public void testSetup() {
            // Set up the path to the test file
            String dir = Directory.GetCurrentDirectory();
            TEST_FILE_LOCATION = Path.GetFullPath(dir + REL_LOCATION);
            TapConfiguration.setConfigFilePath(TEST_FILE_LOCATION);

            // Setup the generator factory
            _generatorFactory = new SqlGeneratorFactory();
           // _generatorFactory.publish("hlascience", typeof (HlaScienceGenerator).FullName);
           // _generatorFactory.publish("PhotoPrimary", typeof(PhotoPrimaryGenerator).FullName);
        }

        [Test]
        public void testConfigProper() {
            TapConfiguration config = TapConfiguration.Instance;
            Assert.IsNotNull(config);
        }

        [Test]
        public void testOneQuery()
        {
            const string test1 = "FROM=hlascience&WHERE=pi_last_name,M*tain&SELECT=objid,ra,dec,pi_last_name";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            TapQueryArgs tqa = new TapQueryArgs(qa);

            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, tqa, _generatorFactory);

            if (!ex.Execute())
            {
                Console.WriteLine("Execute failed");
                return;
            }

            _printResults(ex.results);
        }

        [Test]
        public void testOneQueryWithPos() {
            const string test1 = "FROM=hlascience&WHERE=exposureTime,100/&SELECT=objid,ra,dec,exposureTime";

            TapPosArg pos = new TapPosArg("210.8,54.35");
            TapSizeArg size = new TapSizeArg(".24");
            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            TapQueryArgs queryArgs = new TapQueryArgs(pos, size, qa);

            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, queryArgs, _generatorFactory);

            if (!ex.Execute()) {
                Console.WriteLine("Execute failed");
                return;
            }

            _printResults(ex.results);
        }

        [Test]
        public void testMultiEmbeddedQuery() {
            const string test1 = "FROM=hlascience&WHERE=exposureTime,100/&SELECT=objid,ra,dec,exposureTime";
            
            TapPosArg pos = new TapPosArg("210.8,54.35|202.48,47.23");
            TapSizeArg size = new TapSizeArg(".24");

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            TapQueryArgs queryArgs = new TapQueryArgs(pos, size, qa);

            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, queryArgs, _generatorFactory);
            if (!ex.Execute( )) {
                Console.WriteLine("Execute failed");
                return;
            }

            _printResults(ex.results);
        }

        [Test]
        public void testOnePhotoPrimaryQuery() {
            const string test1 = "FROM=PhotoPrimary&SELECT=ra,dec,JMag,VMag&WHERE=Vmag,/20";

            TapPosArg pos = new TapPosArg("210.8,54.35");
            TapSizeArg size = new TapSizeArg(".24");
            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            TapQueryArgs queryArgs = new TapQueryArgs(pos, size, qa);

            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, queryArgs, _generatorFactory);

            if (!ex.Execute()) {
                Console.WriteLine("Execute failed");
                return;
            }

            _printResults(ex.results);
        }

        [Test]
        public void testMultiPhotoPrimaryQuery() {
            const string test1 = "FROM=PhotoPrimary&SELECT=ra,dec,JMag,VMag&WHERE=Vmag,/20";

            TapPosArg pos = new TapPosArg("210.8,54.35|202.48,47.23|188.86,14.50"); 
            TapSizeArg size = new TapSizeArg(".24");
            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            TapQueryArgs queryArgs = new TapQueryArgs(pos, size, qa);

            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, queryArgs, _generatorFactory);

            if (!ex.Execute()) {
                Console.WriteLine("Execute failed");
                return;
            }

            _printResults(ex.results);
        }

        private static void _printResults(DataSet results) {
            foreach (DataTable each in results.Tables) {
                Console.WriteLine("Id is: " + each.TableName);
                _printResult(each);
            }
        }

        private static void _printResult(DataTable r) {
            List<String> columnNames = new List<String>();
            int columnCount = r.Columns.Count;
            for (int i = 0; i < columnCount; i++) {
                columnNames.Add(r.Columns[i].ColumnName);
            }
            foreach (String each in columnNames) {
                Console.Write(String.Format("{0,-25}", each));
            }
            Console.WriteLine();
            foreach (DataRow each in r.Rows) {
                foreach (String columnName in columnNames) {
                    Console.Write(String.Format("{0,-25}", each[columnName]));
                }
                Console.Write("\n");
            }
            Console.WriteLine("DONE");
        }
    }
}
