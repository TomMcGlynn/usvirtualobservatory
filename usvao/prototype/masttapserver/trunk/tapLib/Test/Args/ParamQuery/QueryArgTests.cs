using System;
using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;
using tapLib.Args.ParamQuery;

namespace tapLib.Test.Args.ParamQuery {
    [TestFixture]
    public class QueryArTests {

        [Test]
        public void isItWorking() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;";
            const String selectpart = "ra,dec,flux";

            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);
            Assert.AreEqual(frompart, h.from);
            Assert.AreEqual(wherepart, h.where);
            Assert.AreEqual(selectpart, h.select);            
        }

        [Test]
        public void testUpperLowerCase() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/";
            const String selectpart = "ra,dec,flux";

            const String test1 = "ForMat=votable&FroM=" + frompart + "&wherE=" + wherepart + "&SeLecT=" + selectpart + "&pos=180.0,0.0&SizE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);
            Assert.AreEqual(frompart, h.from);
            Assert.AreEqual(wherepart, h.where);
            Assert.AreEqual(selectpart, h.select);
        }

        [Test]
        public void testFinalNoAmpersand() {
            const String testBase = "FROM=telephone&WHERE=";
            const String where1 = "pi,*wit*;";
            QueryArg qa = new QueryArg(testBase + where1);
            Assert.AreEqual(where1, qa.where);
            Assert.IsTrue(qa.isValid);
        }

        [Test]
        public void testMultiples() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/";
            const String selectpart = "ra,dec,flux";

            // Check for multiple FROM
            const String test1 = "FROM=telephone&FROM=" + frompart + "&WHERE=" + wherepart + "&SeLecT=" + selectpart + "&pos=180.0,0.0&SizE=0.2";
            QueryArg h = new QueryArg(test1);
            Assert.IsFalse(h.isValid);
            // Check for multiple WHERE
            const String test2 = "WHERE=this is a where&FROM=" + frompart + "&WHERE=" + wherepart + "&SeLecT=" + selectpart + "&pos=180.0,0.0&SizE=0.2";
            QueryArg h2 = new QueryArg(test2);
            Assert.IsFalse(h2.isValid);
            // Check for multiple SELECT
            const String test3 = "SELECT=this is a dup select&FROM=" + frompart + "&WHERE=" + wherepart + "&SeLecT=" + selectpart + "&pos=180.0,0.0&SizE=0.2";
            QueryArg h3 = new QueryArg(test3);
            Assert.IsFalse(h3.isValid);
        }

        [Test]
        public void testConstraintsFromTopLevel() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;pi,*ill*;expTime,22.2/30,100/;pi,kim";
            const String selectpart = "ra,dec,flux";

            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(3, h.numberOfUniqueFieldsWithConstraints());
            Assert.AreEqual(1, h.numberOfConstraintGroupsFor("j_snr"));
            Assert.AreEqual(2, h.numberOfConstraintGroupsFor("pi"));
            Assert.AreEqual(1, h.numberOfConstraintGroupsFor("expTime"));

            Assert.AreEqual(4, h.numberOfConstraintGroups());
            Assert.AreEqual(5, h.totalNumberOfConstraints());
        }

        [Test]
        public void testSelectFields() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;pi,*ill*;expTime,22.2/30,100/";
            const String selectpart = "ra,dec,flux";

            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(3, h.selectFieldCount());
            List<String> selectFields = h.selectFields;
            Assert.AreEqual("ra", selectFields[0]);
            Assert.AreEqual("dec", selectFields[1]);
            Assert.AreEqual("flux", selectFields[2]);
        }

        [Test]
        public void testTableName() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;pi,*ill*;expTime,22.2/30,100/";
            const String selectpart = "ra,dec,flux";

            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(frompart, h.tableName);
        }

        [Test]
        public void testTypes() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;pi,*ill*;expTime,22.2/30,100/";
            const String selectpart = "ra,dec,flux";
            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(3, h.numberOfConstraintGroups());
            IEnumerable<FieldConstraintGroup> constraints = h.constraintGroups();
            foreach (FieldConstraintGroup fc in constraints) {
                Console.WriteLine(fc);
            }
        }

        [Test]
        public void testConstraintFieldNames() {
            const String frompart = "fp_psc";
            const String wherepart = "j_snr,2.5/;pi,*ill*;expTime,22.2/30,100/";
            const String selectpart = "ra,dec,flux";
            const String test1 = "FORMAT=votable&FROM=" + frompart + "&WHERE=" + wherepart + "&SELECT=" + selectpart + "&POS=180.0,0.0&SIZE=0.2";

            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(3, h.constraintFieldNames().Count());
            List<String> constraintFields = h.constraintFieldNames();
            Assert.AreEqual("j_snr", constraintFields[0]);
            Assert.AreEqual("pi", constraintFields[1]);
            Assert.AreEqual("expTime", constraintFields[2]);
        }

        [Test]
        public void testFromParts() {
            const String test1 = "FROM=fp_psc&WHERE=j_snr,2.5/&SELECT=ra";
            QueryArg h = new QueryArg(test1);
            Assert.IsTrue(h.isValid);

            Assert.AreEqual(String.Empty, h.databaseName);
            Assert.AreEqual(String.Empty, h.schemaName);
            Assert.AreEqual("fp_psc", h.tableName);
            Assert.AreEqual("fp_psc", h.from);

            const String test2 = "FROM=HLAFootprint.Hlascience&WHERE=j_snr,2.5/&SELECT=ra";
            QueryArg h2 = new QueryArg(test2);
            Assert.IsTrue(h2.isValid);

            Assert.AreEqual("HLAFootprint", h2.databaseName);
            Assert.AreEqual(String.Empty, h2.schemaName);
            Assert.AreEqual("Hlascience", h2.tableName);
            Assert.AreEqual("HLAFootprint.Hlascience", h2.from);

            const String test3 = "FROM=HLAFootprint.MyFavoriteSchema.Hlascience&WHERE=j_snr,2.5/&SELECT=ra";
            QueryArg h3 = new QueryArg(test3);
            Assert.IsTrue(h3.isValid);

            Assert.AreEqual("HLAFootprint", h3.databaseName);
            Assert.AreEqual("MyFavoriteSchema", h3.schemaName);
            Assert.AreEqual("Hlascience", h3.tableName);
            Assert.AreEqual("HLAFootprint.MyFavoriteSchema.Hlascience", h3.from);
        }
    }
}
