using System;

namespace tapLib.Args {
    /// <summary>
    /// Class that contains immutable values for ra/dec contained in a
    /// POS TAP argument.
    /// </summary>
    public class TapPos {
        // Need to know that a POS actually is to be used, i.e. has a position that
        // is something to use in a search -- assume that a position has been set
        public readonly static TapPos EmptyPos = new TapPos(null);

        // Internal class to provide an enum for STC reference frames
        public class ReferenceFrame {
            public enum STCReferenceFrame {
                FK4,
                FK5,
                ECLIPTIC,
                ICRS,
                GALACTIC_I,
                GALACTIC,
                GALACTIC_II,
                SUPER_GALACTIC,
                AZ_EL,
                BODY,
                GEO_C,
                GEO_D,
                MAG,
                GSE,
                GSM,
                SM,
                HGC,
                HGS,
                HEEQ,
                BAD_FRAME
            }

            private readonly STCReferenceFrame _frame;
            private readonly String _text;

            // Properties
            public STCReferenceFrame frame { get { return _frame; } }
            public String text { get { return _text; } }

            private ReferenceFrame(STCReferenceFrame frame, String text) {
                _frame = frame;
                _text = text;
            }

            public static ReferenceFrame FK4 = new ReferenceFrame(STCReferenceFrame.FK4, "FK4");
            public static ReferenceFrame FK5 = new ReferenceFrame(STCReferenceFrame.FK5, "FK5");
            public static ReferenceFrame ECLIPTIC = new ReferenceFrame(STCReferenceFrame.ECLIPTIC, "ECLIPTIC");
            public static ReferenceFrame ICRS = new ReferenceFrame(STCReferenceFrame.ICRS, "ICRS");
            public static ReferenceFrame GALACTIC_I = new ReferenceFrame(STCReferenceFrame.GALACTIC_I, "GALACTIC_I");
            public static ReferenceFrame GALACTIC = new ReferenceFrame(STCReferenceFrame.GALACTIC, "GALACTIC");
            // Galactic II is handled in the "get" method to return GALACTIC
            public static ReferenceFrame SUPER_GALACTIC = new ReferenceFrame(STCReferenceFrame.SUPER_GALACTIC, "SUPER_GALACTIC");
            public static ReferenceFrame AZ_EL = new ReferenceFrame(STCReferenceFrame.AZ_EL, "AZ_EL");
            public static ReferenceFrame BODY = new ReferenceFrame(STCReferenceFrame.BODY, "BODY");
            public static ReferenceFrame GEO_C = new ReferenceFrame(STCReferenceFrame.GEO_C, "GEO_C");
            public static ReferenceFrame GEO_D = new ReferenceFrame(STCReferenceFrame.GEO_D, "GEO_D");
            public static ReferenceFrame MAG = new ReferenceFrame(STCReferenceFrame.MAG, "MAG");
            public static ReferenceFrame GSE = new ReferenceFrame(STCReferenceFrame.GSE, "GSE");
            public static ReferenceFrame GSM = new ReferenceFrame(STCReferenceFrame.GSM, "GSM");
            public static ReferenceFrame SM = new ReferenceFrame(STCReferenceFrame.SM, "SM");
            public static ReferenceFrame HGC = new ReferenceFrame(STCReferenceFrame.HGC, "HGC");
            public static ReferenceFrame HGS = new ReferenceFrame(STCReferenceFrame.HGS, "HGS");
            public static ReferenceFrame HEEQ = new ReferenceFrame(STCReferenceFrame.HEEQ, "HEEQ");
            public static ReferenceFrame BAD_FRAME = new ReferenceFrame(STCReferenceFrame.BAD_FRAME, "Bad Reference Frame");
            public static ReferenceFrame DEFAULT = ICRS;

            private static readonly ReferenceFrame[] _referenceFrames = new [] {
                                                                   FK4,
                                                                   FK5,
                                                                   ECLIPTIC,
                                                                   ICRS,
                                                                   GALACTIC_I,
                                                                   GALACTIC,
                                                                   SUPER_GALACTIC,
                                                                   AZ_EL,
                                                                   BODY,
                                                                   GEO_C,
                                                                   GEO_D,
                                                                   MAG,
                                                                   GSE,
                                                                   GSM,
                                                                   SM,
                                                                   HGC,
                                                                   HGS,
                                                                   HEEQ,
                                                                   BAD_FRAME
                                                               };

            public static ReferenceFrame getReferenceFrame(String stringFrame) {
                foreach (ReferenceFrame rf in _referenceFrames) {
                    if (stringFrame == rf.text) return rf;
                }
                return BAD_FRAME;
            }

            public override string ToString() {
                return _text;
            }

            public override bool Equals(object obj) {
                if (ReferenceEquals(null, obj)) return false;
                if (ReferenceEquals(this, obj)) return true;
                if (obj.GetType() != typeof (ReferenceFrame)) return false;
                return Equals((ReferenceFrame) obj);
            }

            public bool Equals(ReferenceFrame obj) {
                if (ReferenceEquals(null, obj)) return false;
                if (ReferenceEquals(this, obj)) return true;
                return (obj._frame == _frame);
            }

            public override int GetHashCode() {
                unchecked {
                    return _frame.GetHashCode()*397;
                }
            }
        }

        private readonly String _pos = String.Empty;
        private Double _ra;
        private Double _dec;
        private ReferenceFrame _frame = ReferenceFrame.DEFAULT; // ICRS
        private Boolean _isValid = true;
        private String _problem = String.Empty;

        // properties
        public String pos { get { return _pos; } }
        public Double ra { get { return _ra; } }
        public Double dec { get { return _dec; } }
        public ReferenceFrame frame { get { return _frame; } }
        public Boolean isValid { get { return _isValid; } }
        public Boolean isEmpty { get { return this == EmptyPos; } }
        public String problem { get { return _problem; } }

        private const string POS_FORM_ERROR = "POS must be of the form \"ra,dec\" in degrees.";

        /// <summary>
        /// Class represents one POS string argument
        /// Note:  This class handles a POS of form ra,dec;frame.  The @option is
        /// handled by TapPosArg
        /// </summary>
        /// <param name="posString">String in TAP position format</param>
        public TapPos(String posString) {
            if (posString == null || posString.Equals(String.Empty)) {
                _pos = String.Empty;
                return;
            }
            _pos = _checkInputString(posString);
            // Note that this is using _pos now
            _parsePOS(_pos);
        }

        public TapPos(Double ra, Double dec) {
            String input = String.Format("{0},{1}", ra, dec);
            _parsePOS(input);
        }

        private static String _checkInputString(String pos) {
            // Check for embedded "
            String result = pos.Replace("\"", "");
            return result;
        }

        /// <summary>
        /// Internal method to test the validity of a POS string which is
        /// to be an ra,dec
        /// </summary>
        /// <param name="posString">the ra,dec string</param>
        ///</returns>
        private void _parsePOS(String posString) {
            // Check to see if the POS parses into 2 pieces
            // ; is to locate possible coordinate frame
            String[] parts = posString.Split(new[] { ',', ';'});
            if (parts.Length != 2 && parts.Length != 3) {
                _isValid = false;
               _problem = POS_FORM_ERROR;
                return;
            }

            // First check for the 3 item case
            if (parts.Length == 3) {
                ReferenceFrame rf = ReferenceFrame.getReferenceFrame(parts[2]);
                if (rf == ReferenceFrame.BAD_FRAME) {
                    _isValid = false;
                    _problem = String.Format("Given coordinate frame \"{0}\" is not a valid STC coordinate frame.",
                                             parts[2]);
                    return;
                }
                _frame = rf;
            }

            // Check to see if the parts are both non empty
            int notEmpty = 0;
            foreach (String s in parts) {
                if (!s.Equals(String.Empty)) notEmpty++;
            }
            if ((parts.Length ==2 && notEmpty != 2) || (parts.Length == 3 && notEmpty != 3)) {
                _isValid = false;
                _problem = POS_FORM_ERROR;
                return;
            }

            // Now check to see if the strings are digits
            // Convert can throw System.FormatException or System.OverflowException
            _ra = 0.0d;
            if (!Double.TryParse(parts[0], out _ra)) {
                _isValid = false;
                _problem = "ra part of POS is not a valid numeric value";
                return;
            }
            // Do some cleanup on funky ra arg values
            if (_ra < 0.0) _ra = Math.Abs(_ra);
            // Should I check for >< 360?

            // Covert dec - avoid catching exceptions
            _dec = 0.0d;
            if (!Double.TryParse(parts[1], out _dec)) {
                _isValid = false;
                _problem = "dec part of POS is not a valid numeric value.";
                return;
            }
        }


        /// <summary>
        /// Override ToString to print ra/dec
        /// </summary>
        /// <returns></returns>
        public override String ToString() {
            return String.Format("{0},{1};{2}", ra, dec, frame);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != typeof (TapPos)) return false;
            return Equals((TapPos) obj);
        }

        public bool Equals(TapPos obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj._ra == _ra && obj._dec == _dec && Equals(obj._frame, _frame);
        }

        public override int GetHashCode() {
            unchecked {
                int result = _ra.GetHashCode();
                result = (result*397) ^ _dec.GetHashCode();
                result = (result*397) ^ (_frame != null ? _frame.GetHashCode() : 0);
                return result;
            }
        }
    }
}