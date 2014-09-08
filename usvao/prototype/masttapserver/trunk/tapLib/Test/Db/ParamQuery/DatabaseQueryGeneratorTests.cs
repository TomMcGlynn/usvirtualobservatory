using System;
using NUnit.Framework;
using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Db.ParamQuery;

namespace tapLib.Test.Db.ParamQuery
{
    [TestFixture]
    public class DatabaseQueryGeneratorTests {
        [Test]
        // This one is basic one text constraint
        public void oneTextWildCard() {
            const String test1 = "FROM=hlascience&WHERE=pi,*wi*&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (pi LIKE '%wi%')", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void notOneTextWildCard() {
            const string test1 = "FROM=hlascience&WHERE=pi,!*wi*&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (NOT (pi LIKE '%wi%'))", p.ToSQL());
        }

        [Test]
        // This one adds a second pi constraint that should be ord
        public void twoTextWildCards() {
            const String test1 = "FROM=hlascience&WHERE=pi,*wi*,k*m&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (pi LIKE '%wi%') OR (pi LIKE 'k%m')",
                            p.ToSQL());
        }

        [Test]
        // This one adds a second pi constraint that should be ord
        public void notTwoTextWildCards() {
            const String test1 = "FROM=hlascience&WHERE=pi,!*wi*,k*m&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (NOT ((pi LIKE '%wi%') OR (pi LIKE 'k%m')))", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void oneTextEquality() {
            const String test1 = "FROM=hlascience&WHERE=pi,kim&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (pi='kim')", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void notOneTextEquality() {
            const String test1 = "FROM=hlascience&WHERE=pi,!kim&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT (pi='kim'))", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void twoTextEqualities() {
            const String test1 = "FROM=hlascience&WHERE=pi,kim,sally&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (pi='kim') OR (pi='sally')", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void notTwoTextEqualities() {
            const String test1 = "FROM=hlascience&WHERE=pi,!kim,sally&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT ((pi='kim') OR (pi='sally')))", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void twoTextWildCardsInSeparateGroups() {
            const String test1 = "FROM=hlascience&WHERE=pi,*ob;pi,*nn*&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (pi LIKE '%ob') AND (pi LIKE '%nn%')", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void notTwoTextWildCardsInSeparateGroups() {
            const String test1 = "FROM=hlascience&WHERE=pi,!*ob;pi,!*nn*&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT (pi LIKE '%ob')) AND (NOT (pi LIKE '%nn%'))", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void oneNumericEquality() {
            const String test1 = "FROM=hlascience&WHERE=flux,23&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (flux=23)", p.ToSQL());
        }

        [Test]
        // This one is basic one text constraint
        public void notOneNumericEquality() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux!=23)", p.ToSQL());
        }

        [Test]
        // Multiple numerics in the where
        public void multipleNumericEqualityUsingEquals() {
            const String test1 = "FROM=hlascience&WHERE=flux,23,24,25&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_IN_EQUALITY_OPTION, false);

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (flux=23) OR (flux=24) OR (flux=25)", p.ToSQL());
        }

        [Test]
        // Multiple numerics in the where
        public void notMultipleNumericEqualityUsingEquals() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23,24,25&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_IN_EQUALITY_OPTION, false);

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (NOT ((flux=23) OR (flux=24) OR (flux=25)))", p.ToSQL());
        }

        [Test]
        // Multiple numerics in the where using "in" - DEFAULT
        public void multipleNumericEqualityUsingIn() {
            const String test1 = "FROM=hlascience&WHERE=flux,23,24,25&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (flux IN (23,24,25))", p.ToSQL());
        }

        [Test]
        // Multiple numerics in the where using "in" - DEFAULT
        public void notMultipleNumericEqualityUsingIn() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23,24,25&SELECT=objid,ra,dec,targetname";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT objid,ra,dec,targetname FROM hlascience WHERE (NOT (flux IN (23,24,25)))", p.ToSQL());
        }

        [Test]
        // One range
        public void singleNumericRangeLessThanGreaterThan() {
            const String test1 = "FROM=hlascience&WHERE=flux,23/200&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, false);

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE ((flux >= 23) AND (flux <= 200))", p.ToSQL());
        }

        [Test]
        // One range
        public void singleNumericRangeBetween() {
            const String test1 = "FROM=hlascience&WHERE=flux,23/200&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, true); // DEFAULT

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux BETWEEN 23 AND 200)", p.ToSQL());
        }

        [Test]
        // One range
        public void notSingleNumericRangeLessThanGreaterThan() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23/200&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, false); // DEFAULT

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE ((flux < 23) OR (flux > 200))", p.ToSQL());
        }

        [Test]
        // One range
        public void notSingleNumericRangeBetween() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23/200&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, true); // DEFAULT

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux NOT BETWEEN 23 AND 200)", p.ToSQL());
        }

        [Test]
        // Low half range
        public void singleNumericLowHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,23/&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux >= 23)", p.ToSQL());
        }

        [Test]
        // Low half range
        public void notSingleNumericLowHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,!23/&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux < 23)", p.ToSQL());
        }

        [Test]
        // High half range
        public void singleNumericHighHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,/23&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux <= 23)", p.ToSQL());
        }

        [Test]
        // High half range
        public void notSingleNumericHighHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,!/23&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux > 23)", p.ToSQL());
        }

        [Test]
        // High half range
        public void multipleNumericHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,/23,50/&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);
            ISqlGenerator p = new DefaultSqlQueryGenerator();

            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux <= 23) OR (flux >= 50)", p.ToSQL());
        }

        [Test]
        // High half range
        public void notMultipleNumericHalfRange() {
            const String test1 = "FROM=hlascience&WHERE=flux,!/23,50/&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT ((flux <= 23) OR (flux >= 50)))", p.ToSQL());
        }

        [Test]
        // Multiple ranges
        public void multipleNumericRangesLessThanGreaterThan() {
            const String test1 = "FROM=hlascience&WHERE=flux,/23,500/,50/55&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, false);
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux <= 23) OR (flux >= 500) OR ((flux >= 50) AND (flux <= 55))", p.ToSQL());
        }

        [Test]
        // Multiple ranges - BETWEEN version
        public void multipleNumericRangesBetween() {
            const String test1 = "FROM=hlascience&WHERE=flux,/23,500/,50/55&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, true);
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (flux <= 23) OR (flux >= 500) OR (flux BETWEEN 50 AND 55)", p.ToSQL());
        }

        [Test]
        // Not multiple numeric ranges
        public void notMultipleNumericRangesLessThanGreaterThan() {
            const String test1 = "FROM=hlascience&WHERE=flux,!/23,500/,50/55&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, false);
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT ((flux <= 23) OR (flux >= 500) OR ((flux >= 50) AND (flux <= 55))))", p.ToSQL());
        }

        [Test]
        // Not multiple numeric ranges
        public void notMultipleNumericRangesBetween() {
            const String test1 = "FROM=hlascience&WHERE=flux,!/23,500/,50/55&SELECT=ra,dec";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            DefaultSqlQueryGenerator p = new DefaultSqlQueryGenerator();
            p.setOption(AbstractSqlQueryGenerator.OPTION.USE_BETWEEN_RANGE_OPTION, true);
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT ra,dec FROM hlascience WHERE (NOT ((flux <= 23) OR (flux >= 500) OR (flux BETWEEN 50 AND 55)))", p.ToSQL());
        }

        [Test]
        // Contributed email test1
        public void bigEmailTest1() {
            const String test1 = "FROM=hlascience&WHERE=vmag,4.5/5.5;imag,4.5/;bmag,/5.5;flag,4,5,6;jmag,4.5/5.5,/3.0,9.0/;name,*Lon*;kmag,4.5/5.5;flux,null;last,1";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT $STD FROM hlascience WHERE (vmag BETWEEN 4.5 AND 5.5) AND (imag >= 4.5) AND (bmag <= 5.5) AND (flag IN (4,5,6)) AND (jmag BETWEEN 4.5 AND 5.5) OR (jmag <= 3.0) OR (jmag >= 9.0) AND (name LIKE '%Lon%') AND (kmag BETWEEN 4.5 AND 5.5) AND (flux IS NULL) AND (last=1)", p.ToSQL());
        }

        [Test]
        // Contributed email test2 - whew, a lot of changes to get this going with all optimizations
        public void bigEmailTest1Not() {
            const String test1 = "FROM=hlascience&WHERE=vmag,!4.5/5.5;imag,!4.5/;bmag,!/5.5;flag,!4,5,6;jmag,!4.5/5.5,/3.0,9.0/;name,!*Lon*;kmag,!4.5/5.5;flux,!null;last,!1";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            // Email is this, but I can't yet optimize multiple constraints to change not x and not y to not x or y
            //Assert.AreEqual("SELECT $STD FROM hlascience WHERE (NOT (vmag BETWEEN 4.5 AND 5.5)) AND (imag >= 4.5) AND (bmag <= 5.5) AND (NOT (flag IN (4,5,6))) AND (NOT (jmag BETWEEN 4.5 AND 5.5) OR (jmag <= 3.0) OR (jmag >= 9.0)) AND (NOT (name LIKE '%Lon%')) AND (kmag NOT BETWEEN 4.5 AND 5.5) AND (flux IS NOT NULL) AND (NOT (last=1))", p.ToSQL());
            Assert.AreEqual("SELECT $STD FROM hlascience WHERE (vmag NOT BETWEEN 4.5 AND 5.5) AND (imag < 4.5) AND (bmag > 5.5) AND (NOT (flag IN (4,5,6))) AND (NOT ((jmag BETWEEN 4.5 AND 5.5) OR (jmag <= 3.0) OR (jmag >= 9.0))) AND (NOT (name LIKE '%Lon%')) AND (kmag NOT BETWEEN 4.5 AND 5.5) AND (flux IS NOT NULL) AND (last!=1)", p.ToSQL());
        }

        [Test]
        // Basic multi arg to get going again
        public void testMultiFunkyWithQuotes() {
            const String test1 = @"FROM=hlascience&WHERE=name,""bob*"";imag,""4-,""";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT $STD FROM hlascience WHERE (name LIKE 'bob%') AND (imag='4-,')", p.ToSQL());
        }

        [Test]
        // Contributed test to notice embedded semicolon
        public void testEmailTest3EmbeddedTextSemicolon() {
            const String test1 = "FROM=hlascience&WHERE=name,'*Lon;*'";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT $STD FROM hlascience WHERE (name LIKE '%Lon;%')", p.ToSQL());
        }

        [Test]
        // Contributed test with more wacky args
        public void testEmailTest4WackyColumns() {
            const String test1 = "FROM=hlascience&WHERE=' vmag ' ,4.5/5.5;'imag;x',4.5/;'bmag:y',/5.5";

            QueryArg qa = new QueryArg(test1);
            Assert.IsTrue(qa.isValid);

            ISqlGenerator p = new DefaultSqlQueryGenerator();
            Boolean result = p.generateSQL(new TapQueryArgs(qa));
            Assert.IsTrue(result);
            Assert.AreEqual("SELECT $STD FROM hlascience WHERE (vmag BETWEEN 4.5 AND 5.5) AND (imag;x >= 4.5) AND (bmag:y <= 5.5)", p.ToSQL());
        }

        [Test]
        // Contributed test with error
        public void testEmailTest5RangeError() {
            const String test1 = "FROM=hlascience&WHERE=bmag,/";

            QueryArg qa = new QueryArg(test1);
            Assert.IsFalse(qa.isValid);
            Assert.IsTrue(_testExceptionText(50, WhereClauseParser.Res.ExpectedNumeric, qa.problems[0]));
            // Should make an exception
        }

        private static Boolean _testExceptionText(int size, String one, String two) {
            String s1 = one.Substring(0, size);
            String s2 = two.Substring(0, size);
            return s1 == s2;
        }

        [Test]
        // Contributed test with error
        public void testEmailTest6NegationError() {
            const String test1 = "FROM=hlascience&WHERE=bmag,!4.5/!5.5";

            QueryArg qa = new QueryArg(test1);
            Assert.IsFalse(qa.isValid);
            Assert.IsTrue(_testExceptionText(50, WhereClauseParser.Res.NoExclamationExpected, qa.problems[0]));
            // Should make an exception
        }

        [Test]
        // Contributed test with error
        public void testEmailTest7EmptyConstraint() {
            const String test1 = "FROM=hlascience&WHERE=bmag,;jmag,!4.5";

            QueryArg qa = new QueryArg(test1);
            Assert.IsFalse(qa.isValid);
            Assert.IsTrue(_testExceptionText(30, WhereClauseParser.Res.NoConstraint, qa.problems[0]));
            // Should make an exception
        }

        [Test]
        // Contributed test with wacky negates
        public void testEmailTest8WackyNegates() {
            const String test1 = "FROM=hlascience&WHERE='vmag',!4,!5,!6";

            QueryArg qa = new QueryArg(test1);
            Assert.IsFalse(qa.isValid);
            Assert.IsTrue(_testExceptionText(30, WhereClauseParser.Res.ExpectedNumeric, qa.problems[0]));
            // Should make an exception
        }
    }

}