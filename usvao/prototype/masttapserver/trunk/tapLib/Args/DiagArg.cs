using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace tapLib.Args {

    public class DiagArg {
        public enum DiagLevel {
            ON,
            OFF
        };
        private readonly DiagLevel _level;
        private readonly String _description;

        // Note that the description is all caps -- the comparison method
        // below does a ToUpper on the input
        static public DiagArg ON = new DiagArg(DiagLevel.ON, "ON");
        static public DiagArg OFF = new DiagArg(DiagLevel.OFF, "OFF");

        static private DiagArg[] _diagLevels = new DiagArg[] { ON, OFF };
        static public DiagArg DEFAULT = DiagArg.OFF;

        private DiagArg(DiagLevel level, String description) {
            _level = level;
            _description = description;
        }

        public override string ToString() {
            return _description;
        }

        public Boolean isOn() {
            return _level == DiagLevel.ON;
        }       

        // Properties
        public DiagLevel level { get { return _level; } }
        public String description { get { return _description; } }        

        static public DiagArg getDiag(String slevel) {
            // If it's unknown, it's the default
            if (slevel == null || slevel == String.Empty) {
                slevel = DEFAULT.ToString();
            }
            foreach (DiagArg dl in _diagLevels) {
                if (slevel.ToUpper() == dl._description) return dl;
            }
            return OFF;
        }
    }
}