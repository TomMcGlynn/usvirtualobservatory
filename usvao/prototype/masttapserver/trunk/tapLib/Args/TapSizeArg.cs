using System;

namespace tapLib.Args {
    /// <summary>
    /// Class that contains an immutable SIZE arg for the TAP service.
    /// Converstion and error handling are present in this class.
    /// Note: The TAP Size arg is an angular circular diameter in DEGREES!
    /// </summary>
    public class TapSizeArg {
        private readonly String _size = String.Empty;
        private Double _angularDiameter;
        private Boolean _isValid = true;
        private readonly Boolean _isEmpty;
        private String _problem = String.Empty;

        // properties
        public String size { get { return _size; } }
        public Double diameter { get { return _angularDiameter; } }
        public Double radius { get { return diameter/2.0d; } }
        public Boolean isValid { get { return _isValid; } }
        public Boolean isEmpty { get { return _isEmpty; } }
        public String problem { get { return _problem; } }

        private const string SIZE_FORM_ERROR = "SIZE must be of the form \"diameter\" in degrees.";

        // Default value is .2 degrees
        public static readonly TapSizeArg DEFAULT = new TapSizeArg("0.2");
        public static readonly TapSizeArg Empty = new TapSizeArg(null);

        public TapSizeArg(String size) {
            if (size == null || size.Equals(String.Empty)) {
                _isEmpty = true;
                _size = String.Empty;
                return;
            }
            _size = _checkInputString(size);
            // Note that parseSIZE is using _size now
            _parseSIZE(_size);
        }

        // A Test constructor to allow entering numbers rather than a String
        public TapSizeArg(Double diameter) {
            String input = String.Format("{0}", diameter);
            _parseSIZE(input);
        }

        private static String _checkInputString(String size) {
            // Check for embedded " and remove
            String result = size.Replace("\"", "");
            return result;
        }

        private void _parseSIZE(String sizeString) {
            // Check to see if the SIZE parses into 1 piece
            // right thing according to the spec
            String[] parts = sizeString.Split(new [] { ',' });
            if (parts.Length != 1) {
                _isValid = false;
                _problem = SIZE_FORM_ERROR;
                return;
            }
            // Now check to see if any of the parts are non-empty
            if (parts[0].Equals(String.Empty)) {
                _isValid = false;
                _problem = SIZE_FORM_ERROR;
                return;
            }

            // If there is only 1 string then it should be parsed as a diameter in degrees
            // Convert can throw System.FormatException or System.OverflowException
            if (!Double.TryParse(parts[0], out _angularDiameter)) {
                _isValid = false;
                _problem = "diameter in SIZE is not a valid numeric value";
                return;
            }

            // Clean up any problems
            if (_angularDiameter < 0.0) _angularDiameter = Math.Abs(_angularDiameter);
        }

        // Method to return a radius usable by the HLA search in arcmin
        public double getRadiusInArcMin() {
            // Radius is the distance to the corner of the width/height box
            return radius * 60.0d;
        }

        // Override the ToString method to print the width/height
        public override string  ToString() {
            return String.Format("[{0}]", diameter);
        }
    }
}