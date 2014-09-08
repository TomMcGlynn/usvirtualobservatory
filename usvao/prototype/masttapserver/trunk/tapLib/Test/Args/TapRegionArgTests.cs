using System;
using NUnit.Framework;
using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapRegionArgTests {
        [Test]
        public void testCreation() {
            TapRegionArg arg = new TapRegionArg("Circle ICRS 20 3 2");
            Assert.IsNotNull(arg);
        }

        [Test]
        public void testGoodValue() {
            var test1 = "Circle ICRS 20 3 2";
            var arg = new TapRegionArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(test1, arg.regionString);
        }

        [Test]
        public void testDefault() {
            var test1 = "";
            var arg = new TapRegionArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsTrue(arg.isEmpty);
            Assert.AreEqual(String.Empty, arg.regionString);
        }

        [Test]
        // Currently a bad value is not supported so this reminds me
        public void testBadValue() {
            var test1 = "yikes";
            var arg = new TapRegionArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
        }

    }
}
