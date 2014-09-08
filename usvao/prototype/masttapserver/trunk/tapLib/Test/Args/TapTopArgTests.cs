using System;
using NUnit.Framework;
using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapTopArgTests {
        [Test]
        public void testCreation() {
            TapTopArg arg = new TapTopArg("20");
            Assert.IsNotNull(arg);
        }

        [Test]
        public void testGoodValue() {
            var test1 = "20";
            var test1Int = 20;
            var arg = new TapTopArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(test1Int, arg.top);
        }

        [Test]
        public void testDefault() {
            var test1 = "";
            int expectedResult = Convert.ToInt32(TapTopArg.DEFAULT);
            var arg = new TapTopArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.AreEqual(expectedResult, arg.top);            
        }

        [Test]
        public void testBadValue() {
            var test1 = "yikes";
            var arg = new TapTopArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsFalse(arg.isValid);
        }

    }
}
