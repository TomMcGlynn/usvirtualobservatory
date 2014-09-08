using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using NUnit.Framework;
using tapLib.Args;

namespace tapLib.Test.Args {
    [TestFixture]
    public class TapPosArgTests {
        [Test]
        public void testCreation() {
            const string test1 = "@tablename";
            TapPosArg arg = new TapPosArg(test1, new MockProviderFactory( ));
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
        }

        [Test]
        public void testTableIdentify() {
            const string test1 = "tablename";
            TapPosArg arg = new TapPosArg("@" + test1, new MockProviderFactory( ));
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(TapPosArg.TapPosType.TABLE_MULTI_POS, arg.posType);
            Assert.AreEqual(test1, arg.tableName);
        }

        [Test]
        public void testUploadIdentify() {
            const string test1 = "uploadFile";
            TapPosArg arg = new TapPosArg("@$UPLOAD." + test1, new MockProviderFactory());
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(TapPosArg.TapPosType.UPLOAD_MULTI_POS, arg.posType);
            Assert.AreEqual(test1, arg.tableName);
        }

        [Test]
        public void testVospaceIdentify() {
            const string test1 = "vospaceFile";
            TapPosArg arg = new TapPosArg("@$VOSPACE." + test1, new MockProviderFactory());
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(TapPosArg.TapPosType.VOSPACE_MULTI_POS, arg.posType);
            Assert.AreEqual(test1, arg.tableName);
        }

        [Test]
        public void testOnePos() {
            const string test1 = "180.0,22.0";
            TapPosArg arg = new TapPosArg(test1);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(TapPosArg.TapPosType.ONE_POS_ARG, arg.posType);
            Assert.AreEqual(String.Empty, arg.tableName);
            Assert.AreEqual(1, arg.posCount);
        }

        class MockMultiProvider : ITapPosProvider {
            private readonly List<String> _fakePositions = new List<string> {"120.2,19.5", "35.0,-20.5"};
            private String _problem = String.Empty;
            private Boolean _isValid = true;

            public IEnumerator<string> GetEnumerator() {
                return _fakePositions.GetEnumerator();
            }

            IEnumerator IEnumerable.GetEnumerator() {
                return GetEnumerator();
            }

            public bool isValid { get { return _isValid; } }
            public string problem { get { return _problem; } }

            public bool provide(TapPosArg arg) {
                if (arg.posType == TapPosArg.TapPosType.ONE_POS_ARG) {
                    _problem = "UNSUPPORTED";
                    _isValid = false;
                }
                // Nothing needed since iterator is already available.
                return isValid;
            }
        }

        class MockProviderFactory : ITapPosProviderFactory {
            public ITapPosProvider create(TapPosArg arg) {
                if (arg == null) throw new NoNullAllowedException("TapPosArg");
                return new MockMultiProvider();                
            }
        }

        [Test]
        public void testMultiPosProvider() {
            const string test1 = "MyTable";
            TapPosArg arg = new TapPosArg("@$VOSPACE." + test1, new MockProviderFactory());
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsFalse(arg.isEmpty);
            Assert.AreEqual(TapPosArg.TapPosType.VOSPACE_MULTI_POS, arg.posType);
            Assert.AreEqual(test1, arg.tableName);
            Assert.AreEqual(2, arg.posCount);
        }

        [Test]
        public void testNullCreation() {
            TapPosArg arg = new TapPosArg(null);
            Assert.IsNotNull(arg);
            Assert.IsTrue(arg.isValid);
            Assert.IsTrue(arg.isEmpty);
            Assert.AreEqual(1, arg.posCount);
            Assert.AreEqual(TapPosArg.TapPosType.EMPTY_POS, arg.posType);
        }

        [Test]
        // Just verify that the pieces separated by | getinto the pos list correctly with the right type
        public void testInlineMultiPos() {
            const string testpos1 = "180.0,22.0|22.3,-40.0;GALACTIC";

            var pos1 = new TapPosArg(testpos1);
            Assert.AreEqual(TapPosArg.TapPosType.INLINE_MULTI_POS, pos1.posType);

            Assert.AreEqual(2, pos1.posCount);
            Assert.AreEqual("180.0,22.0", pos1.posList[0].pos);
            Assert.AreEqual("22.3,-40.0;GALACTIC", pos1.posList[1].pos);
        }

    }
}
