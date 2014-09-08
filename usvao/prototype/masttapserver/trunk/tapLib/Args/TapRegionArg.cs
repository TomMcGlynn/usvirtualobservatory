using System;

namespace tapLib.Args {
    public class TapRegionArg {
        /// <summary>
        /// This silly class is a placeholder for what I figure is going
        /// to require support for Regions and checking of validity of 
        /// Region strings or conversion to STC other forms.
        /// Right now all it does it hold a String that is assumed to be valid STC/X.
        /// </summary>
        public class Region {
            public static readonly Region Empty = new Region(String.Empty);
            public static readonly Region DEFAULT = Empty;
            private readonly String _stcxString;

            // Properties
            public String stcx { get { return _stcxString; } }

            public Region(String stcxString) {
                _stcxString = stcxString;
            }

            public override string ToString() {
                return _stcxString;
            }
        }

        private readonly String _regionString;
        private readonly Region _region;
        private const bool _isValid = true;
        private readonly bool _isEmpty;
        private readonly String _problem = String.Empty;

        public static readonly TapRegionArg DEFAULT = new TapRegionArg(Region.Empty);
        public static readonly TapRegionArg Empty = new TapRegionArg((String)null);

        // Property
        public Boolean isValid { get { return _isValid; } }
        public Boolean isEmpty { get {return _isEmpty; } }
        public String problem { get { return _problem; } }
        public String regionString { get { return _regionString; } }
        public Region region { get { return _region; } }

        public TapRegionArg(String regionString) {
            if (String.IsNullOrEmpty(regionString)) {
                _region = Region.Empty;
                _regionString = String.Empty;
                _isEmpty = true;
                return;
            }
            _regionString =_checkInputString(regionString);
            // Nothing to do at this time
            _region = new Region(_regionString);
        }

        private TapRegionArg(Region region) {
            _region = region;
        }

        private static String _checkInputString(String value) {
            // Check for embedded " and remove
            return value.Replace("\"", "");
        }
        
    }
}
