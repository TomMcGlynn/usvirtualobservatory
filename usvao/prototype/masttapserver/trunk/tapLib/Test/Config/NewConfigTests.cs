using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml;
using System.Xml.Serialization;
using NUnit.Framework;
using tapLib.Config;

namespace tapLib.Test.Config
{
    [TestFixture]
    public class NewConfigTests
    {
        private String TEST_FILE_LOCATION = "";
        // This would need to be changed if directories move
        private const string REL_LOCATION = @"\..\..\Test\Config\TestNewConfig.xml";

        [SetUp]
        public void testSetup()
        {
            // Set up the path to the test file
            String dir = Directory.GetCurrentDirectory();
            TEST_FILE_LOCATION = Path.GetFullPath(dir + REL_LOCATION);
            TapConfiguration.setConfigFilePath(TEST_FILE_LOCATION);
        }

        [Test]
        public void testSingletonish()
        {
            TapConfiguration instance1 = TapConfiguration.Instance;
            TapConfiguration instance2 = TapConfiguration.Instance;

            Assert.AreSame(instance1, instance2);
        }

        [Test]
        public void testLocalFileBasedCreation()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
        }

        [Test]
        public void testNumberDatabases()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
            Assert.AreEqual(2, instance.NumberDatabases());
        }

        [Test]
        public void testDatabaseSupported()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
            const String test1 = "Database1";
            Assert.IsTrue(instance.isSupportedDatabase(test1));
            const String test2 = "Database2";
            Assert.IsTrue(instance.isSupportedDatabase(test2));
            const String test3 = "Database3";
            Assert.IsFalse(instance.isSupportedDatabase(test3));
        }

        [Test]
        public void testTableSupported()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
            const string dbName = "Database1";
            Assert.IsTrue(instance.isSupportedTable(dbName, "Database1Table1"));
            Assert.IsTrue(instance.isSupportedTable(dbName, "Database1Table2"));
            Assert.IsFalse(instance.isSupportedTable(dbName, "Database1Table3"));
        }

        [Test]
        public void testDatabaseOf() {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
            const string dbName = "Database1";
            const String tableName = "Database1Table1";
            Assert.AreEqual(dbName, instance.DatabaseForTable(tableName));
            
            Assert.AreEqual("Database2", instance.DatabaseForTable("Database2Table1"));

            Assert.IsNull(instance.DatabaseForTable("blah"));
        }

        [Test]
        public void testConnectionString()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);
            Assert.AreEqual("Database1Connection", instance.ConnectionString("Database1"));
            Assert.AreEqual("Database2Connection", instance.ConnectionString("Database2"));
            Assert.AreEqual(null, instance.ConnectionString("Database3"));
        }

        [Test]
        public void testStdColumns()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);

            List<String> std = instance.StdColumns("Database1", "Database1Table1");
            Assert.IsNotNull(std);
            Assert.AreEqual(2, std.Count());
            Assert.AreEqual("d1t1c1", std[0]);
            Assert.AreEqual("d1t1c2", std[1]);

            List<String> std2 = instance.StdColumns("Database2", "Database2Table1");
            Assert.IsNotNull(std2);
            Assert.AreEqual(3, std2.Count());
            Assert.AreEqual("d2t1c1", std2[0]);
            Assert.AreEqual("d2t1c2", std2[1]);
            Assert.AreEqual("d2t1c3", std2[2]);
        }

        [Test]
        public void testAllColumns()
        {
            TapConfiguration instance = TapConfiguration.Instance;
            Assert.IsNotNull(instance);

            List<String> all = instance.AllColumns("Database1", "Database1Table1");
            Assert.IsNotNull(all);
            Assert.AreEqual(5, all.Count());
            Assert.AreEqual("d1t1c1", all[0]);
            Assert.AreEqual("d1t1c2", all[1]);
            Assert.AreEqual("d1t1c3", all[2]);
            Assert.AreEqual("d1t1c4", all[3]);
            Assert.AreEqual("d1t1c5", all[4]);

            List<String> all2 = instance.AllColumns("Database2", "Database2Table1");
            Assert.IsNotNull(all2);
            Assert.AreEqual(4, all2.Count());
            Assert.AreEqual("d2t1c1", all2[0]);
            Assert.AreEqual("d2t1c2", all2[1]);
            Assert.AreEqual("d2t1c3", all2[2]);
            Assert.AreEqual("d2t1c4", all2[3]);
        }

        /**[Test] **/

        public void TestNewConfigSerialization()
        {
            bool isException = false;
            try
            {
                String filePath =
                    Path.GetFullPath(Directory.GetCurrentDirectory() + @"\..\..\Test\Config\TestNewConfig.xml");
                XmlTextReader xmlReader = new XmlTextReader(filePath);
                XmlSerializer xs = new XmlSerializer(typeof (NewConfig));
                NewConfig config = (NewConfig) xs.Deserialize(xmlReader);
                Assert.IsNotNull(config);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
                isException = true;
            }
            Assert.IsFalse(isException);
        }
    }
}