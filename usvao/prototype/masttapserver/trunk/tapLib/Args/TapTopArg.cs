using System;

namespace tapLib.Args {
    public class TapTopArg {
        public static String DEFAULT = "0";

        private int _topValue;
        private readonly String _topString;
        private Boolean _isValid = true;
        private String _problem;

        // Property
        public int top { get { return _topValue; } }
        public Boolean isValid { get { return _isValid; } }
        public String problem { get { return _problem; } }

        public static readonly TapTopArg Empty = new TapTopArg(String.Empty);

        public TapTopArg(String topString) {
            topString = _checkInputString(topString);
            if (topString == String.Empty) topString = DEFAULT;

            _topString = topString;
            _parse();
        }

        private static String _checkInputString(String size) {
            // Check for embedded " and remove
            if (size == null)
                return String.Empty;
            String result = size.Replace("\"", "");
            return result;
        }

        private void _parse() {
            // Convert can throw System.FormatException or System.OverflowException
            double tempValue;
            if (!Double.TryParse(_topString, out tempValue)) {
                _isValid = false;
                _problem = "TOP is not a valid numeric value";
                return;
            }

            _topValue = (int) tempValue;

            // Clean up any problems
            if (_topValue < 0.0) _topValue = Math.Abs(_topValue); 
        }
        
    }
}
