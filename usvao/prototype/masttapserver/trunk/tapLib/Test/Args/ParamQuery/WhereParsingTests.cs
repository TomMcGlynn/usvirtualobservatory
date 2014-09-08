using System;
using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;

using tapLib.Args;
using tapLib.Args.ParamQuery;

namespace tapLib.Test.Args.ParamQuery {
    [TestFixture]
    public class WhereParsingTests {

        [Test]
        public void testPublicConstraintAnalysis() {
            const String testBase = "FROM=telephone&WHERE=";
            const String field1 = "pi";
            const String where1 = field1 + ",*wit*;";
            QueryArg qa = new QueryArg(testBase + where1);
            Assert.AreEqual(1, qa.numberOfConstraintGroups());
            Assert.AreEqual(1, qa.numberOfConstraintGroupsFor(field1));

            // Ensure constraintGroupsFor doesn't run twice
            qa.findConstraints();
            Assert.AreEqual(1, qa.numberOfConstraintGroups());
            Assert.AreEqual(1, qa.numberOfConstraintGroupsFor(field1));

            const String where2 = "pi,*wit*;expTime,22;value,22.2/40.3,50/100";
            QueryArg qa2 = new QueryArg(testBase + where2);
            Assert.AreEqual(3, qa2.numberOfUniqueFieldsWithConstraints());
            Assert.AreEqual(3, qa2.numberOfConstraintGroups());
            Assert.AreEqual(1, qa2.numberOfConstraintGroupsFor("pi"));
            Assert.AreEqual(1, qa2.numberOfConstraintGroupsFor("expTime"));
            Assert.AreEqual(1, qa2.numberOfConstraintGroupsFor("value"));
            
            // Add more constraints for similar fieldnames
            const String where3 = "pi,*wit*;expTime,22;value,22.2/40.3,50/100;pi,silva;expTime,100;value,1500/";
            QueryArg qa3 = new QueryArg(testBase + where3);
            Assert.AreEqual(3, qa3.numberOfUniqueFieldsWithConstraints());
            Assert.AreEqual(6, qa3.numberOfConstraintGroups());
            Assert.AreEqual(2, qa3.numberOfConstraintGroupsFor("pi"));
            Assert.AreEqual(2, qa3.numberOfConstraintGroupsFor("expTime"));
            Assert.AreEqual(2, qa3.numberOfConstraintGroupsFor("value"));
            Assert.AreEqual(7, qa3.totalNumberOfConstraints());

            // Test an improperly written constraint
            const String where4 = "kim=test";
            QueryArg qa4 = new QueryArg(testBase + where4);
            qa4.dumpProblems();
            Assert.IsFalse(qa4.isValid);
            Assert.AreEqual(0, qa4.numberOfUniqueFieldsWithConstraints());
        }

        [Test]
        // Does it still work with lots of whitespace?
        public void testSpaces() {
            const String testBase = "FROM=telephone&WHERE = ";
            const String field1 = "pi ";
            const String where1 = field1 + ", *wit* ;";
            QueryArg qa = new QueryArg(testBase + where1);

            Assert.AreEqual(1, qa.constraintGroups().Count());
            Assert.AreEqual(1, qa.numberOfConstraintGroupsFor(field1));
        }

        [Test]
        public void testNegatedValues() {
            const String testBase = "FROM=telephone&WHERE = ";
            const String field1 = "pi";
            const String where1 = field1 + ",!*wit*";
            QueryArg qa = new QueryArg(testBase + where1);

            Assert.AreEqual(1, qa.numberOfConstraintGroups());
            Assert.AreEqual(1, qa.numberOfConstraintGroupsFor(field1));
            Assert.IsTrue(qa.constraintGroupsFor(field1).First().isNegated);
            Assert.AreEqual("*wit*", qa.constraintGroupsFor(field1).First().constraints[0]);

            const String where2 = field1 + ",pux*";
            QueryArg qa2 = new QueryArg(testBase + where2);

            Assert.AreEqual(1, qa2.constraintGroups().Count());
            Assert.AreEqual(1, qa2.numberOfConstraintGroupsFor(field1));
            Assert.IsFalse(qa2.constraintGroupsFor(field1).First().isNegated);
        }
    }
}
