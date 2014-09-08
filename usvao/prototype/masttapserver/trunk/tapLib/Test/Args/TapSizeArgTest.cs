using NUnit.Framework;
using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapSizeArgTest {
        [Test]
        /// Just test to see if constructor is non null
        public void testCreation() {
            var arg = new TapSizeArg("0,0");
            Assert.IsNotNull(arg);
        }

        [Test]
        /// Test to handle null and empty SIZE arg
        public void testConstructorArg() {
            var arg = new TapSizeArg(null);
            Assert.IsTrue(arg.isValid);
            Assert.IsTrue(arg.isEmpty);

            arg = new TapSizeArg("");
            Assert.IsTrue(arg.isValid);
            Assert.IsTrue(arg.isEmpty);
        }

        [Test]
        // Test to handle ","
        public void testCommaOnly() {
            var arg = new TapSizeArg(",");
            Assert.IsFalse(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
        }

        [Test]
        public void testOneGoodArg() {
            var arg = new TapSizeArg("22.3");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
        }

        [Test]
        public void testNegativeDiameter() {
            var arg = new TapSizeArg("-18");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(18.0d, arg.diameter);
        }

        [Test]
        /// Test some good arg
        public void testGoodarg() {
            var arg = new TapSizeArg("0");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(0.0d, arg.diameter);
            Assert.AreEqual(0.0d, arg.radius);

            arg = new TapSizeArg("32.0");
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(32.0d, arg.diameter);
        }

        [Test]
        // Test the radius in degress
        public void testRadiusInDegrees() {
            var arg = new TapSizeArg("5.0");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            // 2.5 since 1/2 of 5
            Assert.AreEqual(2.5, arg.radius);
        }


        [Test]
        // Test the radius in arc me
        public void testRadiusInArcMin() {
            var arg = new TapSizeArg("5.0");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            // 300 since radius is in arg min
            Assert.AreEqual(150.0, arg.getRadiusInArcMin());
        }

        [Test]
        // Test the testing constructor
        public void testTestConstructor() {
            var arg = new TapSizeArg(3.0);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(3.0, arg.diameter);
        }

        [Test]
        public void testToString() {
            var arg = new TapSizeArg("3.0");
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual("[3]", arg.ToString());
            arg = new TapSizeArg("123.456");
            Assert.AreEqual("[123.456]", arg.ToString());
        }

        [Test]
        public void testRemoveExtraQuotes() {
            var arg = new TapSizeArg("\"10.1\"");
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual("10.1", arg.size);
            Assert.AreEqual(10.1, arg.diameter);
        }
    }
}