using System;
using NUnit.Framework;
using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Db.ParamQuery;
using tapLib.Stsci;

namespace TAPService.Test.Stsci
{
    [TestFixture]
    public class StsciGeneratorTests {
        class MockTapSchemaGenerator : ISqlGenerator {
            public bool generateSQL(TapQueryArgs queryArg) {
                return true;
            }

            public string tableName { get { return "TAPSchema.Tables"; } }

            public string ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime) {
                return "ToSQL+oneQuery";
            }

            public string ToSQL() {
                return "ToSQL";
            }
        }

        private SqlGeneratorFactory _factory;
        [SetUp]
        public void Setup() {
            Console.WriteLine("setup");
            _factory = new SqlGeneratorFactory();
            //_factory.publish("HlaScience", typeof(HlaScienceGenerator).FullName);
           // _factory.publish("PhotoPrimary", typeof(PhotoPrimaryGenerator).FullName);
            _factory.publish("$TAPSCHEMA.tables", typeof(MockTapSchemaGenerator).FullName);
        }

        [Test]
        public void testHlaScienceCreation() {
            ISqlGenerator gen = _factory.create("hlascience");
            const String test1 = "FROM=hlascience&WHERE=pi,*wi*&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);

            TapQueryArgs args = new TapQueryArgs(new TapPosArg("180.0,22.0"),
                                                 TapSizeArg.DEFAULT,
                                                 qa,
                                                 TapRegionArg.DEFAULT, TapMTimeArg.DEFAULT);

            Assert.IsTrue(gen.generateSQL(args));

            Console.WriteLine(gen.ToSQL());

            Console.WriteLine(gen.ToSQL(args.pos.posList[0], args.size, args.region, args.mtime));
        }

        [Test]
        public void testPhotoPrimaryCreation()
        {
            ISqlGenerator gen = _factory.create("photoprimary");
            const String test1 = "FROM=PhotoPrimary&WHERE=JMag,/4&SELECT=ra, dec, JMag, VMag";

            QueryArg qa = new QueryArg(test1);

            TapQueryArgs args = new TapQueryArgs(new TapPosArg("180.0,22.0"),
                                                 TapSizeArg.DEFAULT,
                                                 qa,
                                                 TapRegionArg.DEFAULT, TapMTimeArg.DEFAULT);

            Assert.IsTrue(gen.generateSQL(args));

            Console.WriteLine(gen.ToSQL());

            Console.WriteLine(gen.ToSQL(args.pos.posList[0], args.size, args.region, args.mtime));
        }

        [Test]
        public void testSchemaIdea() {
            ISqlGenerator gen = _factory.create("$TAPSCHEMA.tables");
            const String test1 = "FROM=$TAPSCHEMA.tables";

            QueryArg qa = new QueryArg(test1);

            TapQueryArgs args = new TapQueryArgs(qa);

            Assert.IsTrue(gen.generateSQL(args));

            Console.WriteLine(gen.ToSQL());

            Console.WriteLine(gen.ToSQL(args.pos.posList[0], args.size, args.region, args.mtime));
        }

    }
}