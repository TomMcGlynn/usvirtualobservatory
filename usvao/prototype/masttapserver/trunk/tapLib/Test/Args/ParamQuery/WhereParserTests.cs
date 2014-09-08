using System;
using System.Collections.Generic;
using NUnit.Framework;
using tapLib.Args.ParamQuery;

namespace tapLib.Test.Args.ParamQuery {
    [TestFixture]
    public class WhereParserTests {
        [Test]
        public void TestTokens() {
            const string test1 = "xname,bob;imag,\"4\";kim,22/35;tt,'x;,!';th,22.3";
            WhereClauseParser p = new WhereClauseParser(test1);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.AreEqual("xname", p.GetIdentifier());
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Comma, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.IsTrue(p.TokenIdentifierIs("bob"));
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.SemiColon, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.IsTrue(p.TokenIdentifierIs("imag"));
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Comma, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.StringLiteral, p.tokenId);
            Assert.AreEqual("\"4\"", p.token.text);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.SemiColon, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.IsTrue(p.TokenIdentifierIs("kim"));
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Comma, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.IntegerLiteral, p.tokenId);
            Assert.AreEqual("22", p.token.text);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Slash, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.IntegerLiteral, p.tokenId);
            Assert.AreEqual("35", p.token.text);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.SemiColon, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.IsTrue(p.TokenIdentifierIs("tt"));
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Comma, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.StringLiteral, p.tokenId);
            Assert.AreEqual("'x;,!'", p.token.text);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.SemiColon, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Identifier, p.tokenId);
            Assert.IsTrue(p.TokenIdentifierIs("th"));
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.Comma, p.tokenId);
            p.NextToken();
            Assert.AreEqual(WhereClauseParser.TokenId.RealLiteral, p.tokenId);
            Assert.AreEqual("22.3", p.token.text);
        }

        [Test]
        public void testAllSeveral() {
            const string test1 = "xname,bob;imag,\"4\";kim,22/35;tt,'x;,!';th,22.3";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try {
                p.parse();
            }
            catch (Exception ex) {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
        }

        [Test]
        public void testParseNull() {
            const string test1 = "xname,null";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try {
                p.parse();
            }
            catch (Exception ex) {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
            ConstraintGroupList gl = p.constraints;
            Assert.AreEqual(1, gl.numberOfConstraintGroups());
            Assert.AreEqual(1, gl.numberOfConstraintGroupsFor("xname"));
            Assert.AreEqual(1, gl.totalNumberOfConstraints());
        }

        [Test]
        public void testSimpleTexts()
        {
            const string test1 = "bob,*long*,bob,b*b,'kim','kd;-,'";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try
            {
                p.parse();
            }
            catch (Exception ex)
            {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
            ConstraintGroupList gl = p.constraints;
            Assert.AreEqual(1, gl.numberOfConstraintGroups());
            Assert.AreEqual(1, gl.numberOfConstraintGroupsFor("bob"));
            Assert.AreEqual(5, gl.numberOfConstraintsFor("bob"));
            Assert.AreEqual(5, gl.totalNumberOfConstraints());

            // Look at bob list
            List<FieldConstraintGroup> blist = gl.constraintGroupsFor("bob");
            Assert.IsNotNull(blist);
            Assert.AreEqual(1, blist.Count);
            Assert.IsFalse(blist[0].isNegated);
            List<String> bcs = blist[0].constraints;
            Assert.AreEqual(5, bcs.Count);
            Assert.AreEqual("*long*", bcs[0]);
            Assert.AreEqual("bob", bcs[1]);
            Assert.AreEqual("b*b", bcs[2]);
            Assert.AreEqual("kim", bcs[3]);
            Assert.AreEqual("kd;-,", bcs[4]); // WHEW! two days work to get to this
        }


        [Test]
        public void testNotTexts() {
            const string test1 = "bob,!*long*,bob";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try {
                p.parse();
            }
            catch (Exception ex) {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
            ConstraintGroupList gl = p.constraints;
            Assert.AreEqual(1, gl.numberOfConstraintGroups());
            Assert.AreEqual(1, gl.numberOfConstraintGroupsFor("bob"));
            Assert.AreEqual(2, gl.numberOfConstraintsFor("bob"));
            Assert.AreEqual(2, gl.totalNumberOfConstraints());

            // Look at bob list
            List<FieldConstraintGroup> blist = gl.constraintGroupsFor("bob");
            Assert.IsNotNull(blist);
            Assert.AreEqual(1, blist.Count);
            Assert.IsTrue(blist[0].isNegated);
            Assert.AreEqual(1, blist.Count);
            List<String> bcs = blist[0].constraints;
            Assert.AreEqual(2, bcs.Count);
            Assert.AreEqual("*long*", bcs[0]);
            Assert.AreEqual("bob", bcs[1]);
        }

        [Test]
        public void testSimpleNumerics() {
            const string test1 = "bob,22,23/,/24,25.5/26.6;sam,22/;ralph,/22;kim,22.2/35.4";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try {
                p.parse();
            }
            catch (Exception ex) {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
            ConstraintGroupList gl = p.constraints;
            Assert.AreEqual(4, gl.numberOfConstraintGroups());
            Assert.AreEqual(1, gl.numberOfConstraintGroupsFor("bob"));
            Assert.AreEqual(7, gl.totalNumberOfConstraints());
            Assert.AreEqual(4, gl.numberOfConstraintsFor("bob"));
            Assert.AreEqual(1, gl.numberOfConstraintsFor("sam"));
            Assert.AreEqual(1, gl.numberOfConstraintsFor("ralph"));
            Assert.AreEqual(1, gl.numberOfConstraintsFor("kim"));
            
            // Look at bob list
            List<FieldConstraintGroup> blist = gl.constraintGroupsFor("bob");
            Assert.IsNotNull(blist);
            Assert.AreEqual(1, blist.Count);
            List<String> bcs = blist[0].constraints;
            Assert.AreEqual(4, bcs.Count);
            Assert.AreEqual("22", bcs[0]);
            Assert.AreEqual("23/", bcs[1]);
            Assert.AreEqual("/24", bcs[2]);
            Assert.AreEqual("25.5/26.6", bcs[3]);

            // Look at sam list
            List<FieldConstraintGroup> slist = gl.constraintGroupsFor("sam");
            Assert.IsNotNull(slist);
            Assert.AreEqual(1, slist.Count);
            List<String> scs = slist[0].constraints;
            Assert.AreEqual(1, scs.Count);
            Assert.AreEqual("22/", scs[0]);

            // Look at ralph list
            List<FieldConstraintGroup> rlist = gl.constraintGroupsFor("ralph");
            Assert.IsNotNull(rlist);
            Assert.AreEqual(1, rlist.Count);
            List<String> rcs = rlist[0].constraints;
            Assert.AreEqual(1, rcs.Count);
            Assert.AreEqual("/22", rcs[0]);

            // Look at ralph list
            List<FieldConstraintGroup> klist = gl.constraintGroupsFor("kim");
            Assert.IsNotNull(klist);
            Assert.AreEqual(1, klist.Count);
            List<String> kcs = klist[0].constraints;
            Assert.AreEqual(1, kcs.Count);
            Assert.AreEqual("22.2/35.4", kcs[0]);
        }

        [Test]
        public void testMultipleOfOneField() {
            const string test1 = "bob,22,23/;bob,50/100";
            WhereClauseParser p = new WhereClauseParser(test1);
            bool exceptionThrown = false;
            try {
                p.parse();
            }
            catch (Exception ex) {
                exceptionThrown = true;
                Console.WriteLine(ex);
            }
            Assert.IsFalse(exceptionThrown);
            ConstraintGroupList gl = p.constraints;
            Assert.AreEqual(2, gl.numberOfConstraintGroups());
            Assert.AreEqual(2, gl.numberOfConstraintGroupsFor("bob"));
            Assert.AreEqual(3, gl.totalNumberOfConstraints());
            Assert.AreEqual(3, gl.numberOfConstraintsFor("bob"));
        }
    }
}
