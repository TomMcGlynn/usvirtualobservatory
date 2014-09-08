using System;

namespace tapLib.Args {
    public class TapOutputArg {

        private readonly String _outputString;
        private const bool _isValid = true;
        private readonly String _problem = String.Empty;

        // Property
        public Boolean isValid { get { return _isValid; } }
        public String problem { get { return _problem; } }
        public String output { get { return _outputString; } }
        // These two properties allow a check to determine if a query is sync or async
        public Boolean isSync { get { return _outputString.Equals(String.Empty); } }
        public Boolean isASync { get { return !isSync; } }

        public TapOutputArg(String outputString) {
            // Missing args are created with null value
            if (outputString == null) {
                _outputString = String.Empty;
                return;
            }
            _outputString = _checkInputString(outputString);
            // Nothing to do at this time
        }

        private static String _checkInputString(String value) {
            // Check for embedded " and remove
            return value.Replace("\"", "");
        }        
    }
}
