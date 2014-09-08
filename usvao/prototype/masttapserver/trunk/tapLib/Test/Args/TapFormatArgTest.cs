using System;
using NUnit.Framework;

using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapFormatArgTest {
        [Test]
        // Some tests for SiaFormat
        public void testSiaFormat() {
            // Test that they are placed in the List
            Assert.IsTrue(FormatInfo.FORMATS.Count > 0);
        }

        [Test]
        /// Just test to see if constructor is non null
        public void testCreation() {
            var arg = new TapFormatArg("image/fits");
            Assert.IsNotNull(arg);
        }

        [Test]
        /// Test to handle null and empty FORMAT arg
        public void testConstructorArg() {
            var arg = new TapFormatArg(null);
            Assert.IsTrue(arg.isValid());

            // Check that it's all
            Assert.AreEqual(1, arg.FormatInfos.Length);
        }

        [Test]
        // Test the empty case which should result in ALL by spec
        public void testEmptyFormat() {
            var arg = new TapFormatArg("");
            Assert.IsTrue(arg.isValid());
            FormatInfo[] formats = arg.FormatInfos;
            Assert.AreEqual(1, formats.Length);
            Assert.AreSame(FormatInfo.ALL, formats[0]);
        }

        [Test]
        // Test to handle ","
        public void testCommaOnly() {
            var arg = new TapFormatArg(",");
            Assert.IsFalse(arg.isValid());
        }

        [Test]
        // Test some good args
        public void testGoodArgs() {
            var arg = new TapFormatArg("image/fits,image/png");
            Assert.IsTrue(arg.isValid());
            Assert.AreEqual(2, arg.FormatInfos.Length);
        }

        [Test]
        // Test to see if others are removed when ALL is present
        public void testAllOptimization() {
            var arg = new TapFormatArg("image/fits,ALL");
            Assert.IsTrue(arg.isValid());
            Assert.AreEqual(1, arg.FormatInfos.Length);
            Assert.AreSame(FormatInfo.ALL, arg.FormatInfos[0]);
        }

        [Test]
        // Test the default value
        public void testDefault() {
            Assert.AreEqual(TapFormatArg.DEFAULT.FormatInfos.Length, 1);
            Assert.IsTrue(TapFormatArg.DEFAULT.FormatInfos[0] == FormatInfo.IMAGE_FITS);
        }

        [Test]
        // Test tostring
        public void testToString() {
            // First one arg
            var arg = new TapFormatArg("image/fits");
            String result = arg.ToString();
            Assert.AreEqual("[image/fits]", result);
            // Now two
            arg = new TapFormatArg("image/fits,image/png");
            result = arg.ToString();
            Assert.AreEqual("[image/fits,image/png]", result);
            // Now three 
            arg = new TapFormatArg("image/fits,image/png,GRAPHIC");
            result = arg.ToString();
            Assert.AreEqual("[image/fits,image/png,GRAPHIC]", result);
        }

        [Test]
        public void testRemoveExtraQuotes() {
            var arg = new TapFormatArg("\"image/fits\"");
            Assert.AreEqual("image/fits", arg.format);
            Assert.AreEqual(1, arg.FormatInfos.Length);
        }
    }
}