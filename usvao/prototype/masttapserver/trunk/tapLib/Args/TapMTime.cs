using System;

namespace tapLib.Args {
    public class TapMTimeArg {
        public class MTime {
            public static readonly MTime Empty = new MTime("MTime default");
            public static readonly MTime DEFAULT = Empty;

            private readonly string _mtimeString;

            public MTime(String mtimeString) {
                _mtimeString = mtimeString;
            }

            public override String ToString() {
                return _mtimeString;
            }
        }

        private const bool _isValid = true;
        private readonly bool _isEmpty = true;
        private readonly String _problem = String.Empty;
        private readonly MTime _mtime;

        public static readonly TapMTimeArg DEFAULT = new TapMTimeArg(MTime.DEFAULT);
        public static readonly TapMTimeArg Empty = new TapMTimeArg((String)null);

        // Property
        public Boolean isValid { get { return _isValid; } }
        public Boolean isEmpty { get { return _isEmpty; } }
        public String problem { get { return _problem; } }
        public MTime mtime { get { return _mtime; } }

        public TapMTimeArg(String mtimeString) {
            // Missing args are created with null value
            if (mtimeString == null) {
                _mtime = MTime.DEFAULT;
                _isEmpty = true;
            }
            // Nothing to do at this time
            // Fake it
            _mtime = new MTime(mtimeString);
        }

        private TapMTimeArg(MTime mtime) {
            _mtime = mtime;
        }

        /**
        private static String _checkInputString(String value) {
            // Check for embedded " and remove
            return value.Replace("\"", "");
        }  
        **/      
    }
}
