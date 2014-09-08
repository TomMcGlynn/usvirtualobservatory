using System;
using System.Collections.Generic;
using System.Text;

namespace tapLib.Args {
    public class FormatInfo {
        public enum Format {
            IMAGE_FITS,
            IMAGE_PNG,
            IMAGE_JPEG,
            TEXT_HTML,
            ALL,
            GRAPHIC,
            METADATA,
            GRAPHIC_ALL,
            GRAPHIC_SPECIFIED
        }
        private readonly Format _format;
        private readonly String _text;
        private FormatInfo(Format format, String text) {
            _format = format;
            _text = text;
        }
        // Properties
        public Format format { get { return _format; } }
        public String text { get { return _text; } }

        static public List<FormatInfo> FORMATS;

        static public FormatInfo IMAGE_FITS;
        static public FormatInfo IMAGE_PNG;
        static public FormatInfo IMAGE_JPEG;
        static public FormatInfo TEXT_HTML;
        static public FormatInfo ALL;
        static public FormatInfo GRAPHIC;
        static public FormatInfo METADATA;
        
        /// Static constructor should run before anything else
        static FormatInfo() {
            FORMATS = new List<FormatInfo>();
            IMAGE_FITS = new FormatInfo(Format.IMAGE_FITS, "image/fits");
            FORMATS.Add(IMAGE_FITS);        
            IMAGE_PNG = new FormatInfo(Format.IMAGE_PNG, "image/png");
            FORMATS.Add(IMAGE_PNG);
            IMAGE_JPEG = new FormatInfo(Format.IMAGE_JPEG, "image/jpeg");
            FORMATS.Add(IMAGE_JPEG);
            TEXT_HTML = new FormatInfo(Format.TEXT_HTML, "text/html");
            FORMATS.Add(TEXT_HTML);
            ALL = new FormatInfo(Format.ALL, "ALL");
            FORMATS.Add(ALL);
            GRAPHIC = new FormatInfo(Format.GRAPHIC, "GRAPHIC");
            FORMATS.Add(GRAPHIC);
            METADATA = new FormatInfo(Format.METADATA, "METADATA");
            FORMATS.Add(METADATA);
        }

        public override string ToString() {
            return _text;
        }

        /// <summary>
        /// Method to test whether or not an SIA Mime type is valid
        /// </summary>
        /// <param name="stringFormat">proposed MIME value</param>
        /// <returns>the cooresponding FormatInfo or null if not valid</returns>
        static public FormatInfo getSiaFormat(String stringFormat) {
            FormatInfo result = null;
            foreach(FormatInfo f in FORMATS) {
                if (stringFormat.Equals(f.text)) result = f;
            }
            return result;
        }
    }

    /// <summary>
    /// This class contains and parses the information with an SIA FORMAT argument.
    /// Once the class is constructed, the isValid method can be used to determine
    /// if the argument was parsed coorectly.
    /// </summary>
    public class TapFormatArg {
        private readonly List<FormatInfo> _formats = new List<FormatInfo>();
        private Boolean _isValid = true;
        private String _problem = String.Empty;
        private readonly String _format = String.Empty;

        private const string FORMAT_FORM_ERROR = "FORMAT must contain valid SIA MIME types or keywords.";
        public static TapFormatArg DEFAULT;

        // properties
        // Note this is cloned to ensure that TapFormatArg is immutable
        public FormatInfo[] FormatInfos {
            get {
                return _formats.ToArray();                
            }
        }

        public Boolean isValid() { return _isValid; }
        public String problem { get { return _problem; } }
        public String format { get { return _format; } }

        private static String _checkInputString(String size) {
            // Check for embedded " and remove
            String result = size.Replace("\"", "");
            return result;
        }

        private void _parseFormats(String imageformat) {
            String[] parts = imageformat.Split(new[] { ',' });
            if (parts.Length == 0) {
                _isValid = false;
                _problem = FORMAT_FORM_ERROR;
                return;
            }
            // Now look at each part and compare the the list of valid formats
            foreach (String part in parts) {
                FormatInfo f = FormatInfo.getSiaFormat(part);
                if (f != null) {
                    _formats.Add(f);
                }                
            }

            // Spec isn't really clear on this but if there are parts and 
            // they aren't in the format list, then we go on and return an
            // empty list.  I'm tempted to treat "," as an error since clearly
            // the user made a mistake
            if (parts.Length > 1 && _formats.Count == 0) {
                _isValid = false;
                _problem = FORMAT_FORM_ERROR;
                return;
            }

            // Spec says if no formats are present, format should be all
            if (parts.Length == 1 && _formats.Count == 0) {
                _formats.Add(FormatInfo.ALL);
                return;
            }

            // Do some optimizations 
            // If there are several formats and one is ALL, remove others
            if (_formats.Count > 1 && _formats.Contains(FormatInfo.ALL)) {
                _formats.Clear();
                _formats.Add(FormatInfo.ALL);
            }
        }

        // Override ToString to print a list in brackets of all types
        public override string ToString() {
            var result = new StringBuilder("[");
            var count = 0;
            foreach (FormatInfo f in _formats) {
                result.Append(f.ToString());
                if (++count < _formats.Count) result.Append(",");
            }
            result.Append("]");
            return result.ToString();
        }

        // Private constructor used only to initialize the default value
        private TapFormatArg() { }

        public TapFormatArg(String imageformat) {
            if (imageformat == null) {
                // By the crazy spec, if FORMAT isn't present, then ALL should be assumed
                // Empty is handled in parse (i.e. FORMAT=""
                imageformat = FormatInfo.ALL.text;
            }
            _format = _checkInputString(imageformat);
            _parseFormats(imageformat);
        }

        // Static constructor needed to initialize DEFAULT value
        static TapFormatArg() {
            DEFAULT = new TapFormatArg();
            DEFAULT._formats.Add(FormatInfo.IMAGE_FITS);
        }
    }
}