using System;
using NUnit.Framework;
using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapPosTests {

        [Test]
        /// Just test to see if constructor is non null
        public void testCreation() {
            var arg = new TapPos("0,0");
            Assert.IsNotNull(arg);
        }

        [Test]
        /// Test to handle null and empty POS arg
        public void testNullConstructorArg() {
            var arg = new TapPos(null);
            // Must handle null for the case when a query has no pos
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(String.Empty, arg.pos);

            // Same kind of thing
            arg = new TapPos(String.Empty);
            Assert.IsTrue(arg.isValid);
        }

        [Test]
        // Test the testing constructor
        public void testTestConstructor() {
            var arg = new TapPos(12.0, 32.0);
            Assert.IsTrue(arg.isValid);
            Assert.IsNotNull(arg);
            Assert.AreEqual(12.0, arg.ra);
            Assert.AreEqual(32.0, arg.dec);
        }

        [Test]
        // Test to handle ","
        public void testCommaOnly() {
            var arg = new TapPos(",");
            Assert.IsFalse(arg.isValid);
        }

        [Test]
        /// Test some good args
        public void testGoodarg() {
            var arg = new TapPos("0,0");
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(0.0d, arg.ra);
            Assert.AreEqual(0.0d, arg.dec);

            arg = new TapPos("180.0,-32.0");
            // For some reason Assert.isTrue() doesn't work
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(180.0d, arg.ra);
            Assert.AreEqual(-32.0d, arg.dec);
        }

        [Test]
        public void testNegativeRA() {
            var arg = new TapPos("-180,32");
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(180.0d, arg.ra);
        }

        [Test]
        public void testOneArgFailure() {
            var arg = new TapPos("180.0");
            Assert.IsFalse(arg.isValid);

            // The following will be parsed as an empty string and a value
            arg = new TapPos(",90.0");
            Assert.IsFalse(arg.isValid);
        }

        [Test]
        public void testNoConvertArg() {
            var arg = new TapPos("AB,CD");
            Assert.IsFalse(arg.isValid);
        }

        [Test]
        public void testToString() {
            var arg = new TapPos("180.0,0.0");
            String result = arg.ToString();
            Assert.AreEqual("180,0;ICRS", result);
            arg = new TapPos("180.012,-4.2");
            result = arg.ToString();
            Console.WriteLine("Result: " + result);
            Assert.AreEqual("180.012,-4.2;ICRS", result);
        }

        [Test]
        public void testRemoveExtraQuotes() {
            var arg = new TapPos("\"180.0,10.0\"");
            Assert.AreEqual("180.0,10.0", arg.pos);
            // Should check the actual ra/dec too
            Assert.AreEqual(180.0, arg.ra);
            Assert.AreEqual(10.0, arg.dec);
        }

        [Test]
        public void testCoordFrame() {
            var arg = new TapPos("180.0,22.0;GALACTIC");
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(180.0, arg.ra);
            Assert.AreEqual(22.0, arg.dec);
            Assert.AreEqual(TapPos.ReferenceFrame.GALACTIC, arg.frame);
        }

        [Test]
        public void testEquality() {
            const string testpos1 = "180.0,22.0";
            const string testpos2 = "22.3,-40.0";
            const string testpos3 = "180.0,22.0;GALACTIC";

            var pos1 = new TapPos(testpos1);
            var pos2 = new TapPos(testpos1);

            Assert.AreEqual(pos1, pos2);

            var pos3 = new TapPos(testpos2);
            Assert.AreNotEqual(pos1, pos3);

            var pos4 = new TapPos(testpos3);
            Assert.AreNotEqual(pos1, pos4);
            Assert.AreNotEqual(pos2, pos4);
            Assert.AreNotEqual(pos3, pos4);
            Assert.AreEqual(pos4, pos4);
        }

    }
}