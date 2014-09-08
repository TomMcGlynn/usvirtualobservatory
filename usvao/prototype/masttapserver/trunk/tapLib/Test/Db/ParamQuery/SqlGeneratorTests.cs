using System;
using NUnit.Framework;
using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Db.ParamQuery;

namespace tapLib.Test.Db.ParamQuery {
    class MockSqlGenerator : ISqlGenerator {
        public bool generateSQL(TapQueryArgs queryArg) {
            return true;
        }

        public string tableName { get { return "MockTest"; } }

        public string ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime) {
            return "ToSQL+oneQuery";
        }

        public string ToSQL() {
            return "ToSQL";
        }
    }

    // This test class doesn't implement the right interface
    class MockSqlGenerator3 {
        public bool generateSQL(QueryArg queryArg) {
            return true;
        }
    }

    [TestFixture]
    public class SqlGeneratorTests {
        [Test]
        // This test shows basic working publish/create functionality
        public void basicFactorTests()
        {
            const string tableName = "test1";

            ISqlGeneratorFactory f = new SqlGeneratorFactory();
            f.publish(tableName, typeof (MockSqlGenerator).FullName);

            ISqlGenerator test = f.create(tableName);
            Assert.IsNotNull(test);
            OneQuery q = new OneQuery();
            Assert.AreEqual("ToSQL+oneQuery", test.ToSQL(q.pos, q.size, q.region, q.mtime));
            Assert.AreEqual("ToSQL", test.ToSQL());
        }

        [Test]
        public void badLookupTest() {
            ISqlGeneratorFactory f = new SqlGeneratorFactory();
            f.publish("tableName", typeof(MockSqlGenerator).FullName);

            ISqlGenerator test = f.create("tableName2");
            Assert.IsNull(test);
        }

        [Test]
        public void publishNoInterfaceTest() {
            ISqlGeneratorFactory f = new SqlGeneratorFactory();
            bool exThrown = false;
            try {
                f.publish("tableName", typeof(MockSqlGenerator3).FullName);
            }
            catch (Exception ex) {
                exThrown = true;
                Assert.IsTrue(typeof(ArgumentException) == ex.GetType());
            }
            Assert.IsTrue(exThrown);
        }

        [Test]
        public void publishNonExistingClassTest() {
            ISqlGeneratorFactory f = new SqlGeneratorFactory();
            bool exThrown = false;
            try {
                f.publish("tableName", "SamIYam");
            }
            catch (Exception ex) {
                exThrown = true;
                Assert.IsTrue(typeof(TypeLoadException) == ex.GetType());
            }
            Assert.IsTrue(exThrown);
        }

        [Test]
        // This test looks for lower case working in mapping
        public void publishLowerCaseTests() {
            const string tableNameUpper = "TEST1";
            const string tableNameLower = "test1";

            ISqlGeneratorFactory f = new SqlGeneratorFactory();
            // Publish as upper and look for lower and upper
            f.publish(tableNameUpper, typeof(MockSqlGenerator).FullName);

            ISqlGenerator test = f.create(tableNameUpper);
            Assert.IsNotNull(test);
            OneQuery q = new OneQuery();
            Assert.AreEqual("ToSQL+oneQuery", test.ToSQL(q.pos, q.size, q.region, q.mtime));
            Assert.AreEqual("ToSQL", test.ToSQL());

            test = f.create(tableNameLower);
            Assert.IsNotNull(test);
            Assert.AreEqual("ToSQL+oneQuery", test.ToSQL(q.pos, q.size, q.region, q.mtime));
            Assert.AreEqual("ToSQL", test.ToSQL());
        }
    }
}
