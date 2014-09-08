using System;
using System.Collections.Generic;

namespace tapLib.Args {
    public class TapPosArg {
        public enum TapPosType {
            ONE_POS_ARG,
            INLINE_MULTI_POS,
            UPLOAD_MULTI_POS,
            VOSPACE_MULTI_POS,
            TABLE_MULTI_POS,
            EMPTY_POS
        }

        // Default is M100 position with no support for provders
        public static readonly TapPosArg DEFAULT = new TapPosArg("185.73,15.83", null);

        // Character for noticing POS is referencing a list
        // If present, this identifies a pos list location - SPEC 3.3.6
        private const char MULTI_POS_MARKER = '@';
        private const char TABLENAME_DELIMITER = '.';
        private const String VOSPACE_LOCATION = "$VOSPACE";
        private const String UPLOAD_LOCATION = "$UPLOAD";
        private const String INLINE_MULTI_POS_DELIMITER = "|";

        private readonly String _pos;
        private readonly ITapPosProviderFactory _providerFactory;
        private readonly List<TapPos> _tapPosList = new List<TapPos>();

        private TapPosType _posType;
        private String _tableName = String.Empty;
        private Boolean _isValid = true;
        private String _problem = String.Empty;

        // Properties
        public Boolean isValid { get { return _isValid; } }
        public Boolean isEmpty { get { return _posType == TapPosType.EMPTY_POS; } }
        public String problem { get { return _problem; } }
        public String tableName { get { return _tableName; } }
        public TapPosType posType { get { return _posType; } }
        public int posCount { get { return _tapPosList.Count; }}
        public List<TapPos> posList { get { return _tapPosList; } }

        private const string POS_FORM_MULTI_ERROR =
            "A multi-POS table must be of the form @tablename where tablename is $VOSPACE.name or $UPLOAD.name.";

        public static readonly TapPosArg Empty = new TapPosArg(null, null);

        public TapPosArg(String posString) : this(posString, null) {}

        // Not all queries need to have a POS so this EMPTY is here to test that a query has a POS
        // If the pos is not empty then it is a POS - this is initialized in static constructor
        // to have the EMPTY_POS type
        /// <summary>
        /// A TapPosArg can be a list of positions.
        /// TapPosArg is a container class for a list of TapPos instances.
        /// It's not necessary for a query to use a POS so if the class is created with a null or empty
        /// string, the TapPosArg is made equal to the Empty instance.  Classes may need to check this
        /// </summary>
        /// <param name="posString"></param>
        /// <param name="providerFactory"></param>
        public TapPosArg(String posString, ITapPosProviderFactory providerFactory) {
            if (posString == null || posString.Equals(String.Empty)) {
                _posType = TapPosType.EMPTY_POS;
                // Here I'm adding an empty position so that higher routines can always
                // execute a list of positions -- in this case there is one empty pos
                // The TapPos.isEmpty tests for equivalence with TapPos.EmptyPos
                _tapPosList.Add(TapPos.EmptyPos);
                return;
            }
            _providerFactory = providerFactory;

            _pos = _checkInputString(posString);
            _parsePOS(_pos);
        }

        private static String _checkInputString(String pos) {
            // Check for embedded "
            String result = pos.Replace("\"", "");
            return result;
        }

        private void _parsePOS(String posString) {
            _posType = _getPosType(posString);

            // Check to see if the POS is more than a simple POS string
            if (posType == TapPosType.ONE_POS_ARG) {
                TapPos onePos = new TapPos(posString);
                _tapPosList.Add(onePos);
                return;
            }

            if (posType == TapPosType.INLINE_MULTI_POS)
            {
                String[] positions = posString.Split(new [] {INLINE_MULTI_POS_DELIMITER}, StringSplitOptions.RemoveEmptyEntries);
                foreach (String each in positions)
                {
                    _tapPosList.Add(new TapPos(each));
                }
                return;
            }

            // If we are here then it's a multi pos query
            _setTableName(posString.Substring(1));

            _handleTableOfPositions();
        }

        private void _postUnsupportedProblem() {
            _isValid = false;
            switch (_posType) {
                case TapPosType.VOSPACE_MULTI_POS:
                    _problem = "VOSpace position table access is not supported on this service.";
                    break;
                case TapPosType.UPLOAD_MULTI_POS:
                    _problem = "Uploaded position tables are not supported on this service.";
                    break;
                case TapPosType.TABLE_MULTI_POS:
                    _problem = "Database position tables are not supported on this service.";
                    break;
            }
        }

        private void _handleTableOfPositions() {
            if (_providerFactory == null) {
                _postUnsupportedProblem();
                return;
            }

            // Use the factory to create a new instance of a provider
            ITapPosProvider p = _providerFactory.create(this);
            p.provide(this);
            if (!p.isValid) {
                _isValid = false;
                _problem = p.problem;
                return;
            }

            foreach (String posString in p) {
                TapPos pos = new TapPos(posString);
                _tapPosList.Add(pos);
            }
        }

        private static TapPosType _getPosType(String posString) {
            // Check to see if the POS is more than a simple POS string
            if (posString[0] != MULTI_POS_MARKER) {
                // Check for embedded separator
                if (posString.Contains(INLINE_MULTI_POS_DELIMITER)) return TapPosType.INLINE_MULTI_POS;
                // Else it must be a one position arg
                return TapPosType.ONE_POS_ARG;
            }
            // Skip over the multi marker
            posString = posString.Substring(1);

            // Check for match of $XXX
            if (posString.StartsWith(VOSPACE_LOCATION)) {
                return TapPosType.VOSPACE_MULTI_POS;
            }
            if (posString.StartsWith(UPLOAD_LOCATION)) {
                return TapPosType.UPLOAD_MULTI_POS;
            }
            return TapPosType.TABLE_MULTI_POS;
        }


        private void _setTableName(String multiPosString) {
            // Now get the table name
            if (posType == TapPosType.TABLE_MULTI_POS) {
                _tableName = multiPosString;
                return;
            }

            // It's VOSPACE or UPLOAD so split table name out
            int tableNameStart = multiPosString.IndexOf(TABLENAME_DELIMITER);
            if (tableNameStart == -1) {
                // No table name
                _isValid = false;
                _problem = POS_FORM_MULTI_ERROR;
                return;
            }
            // Skip over the delimiter
            _tableName = multiPosString.Substring(tableNameStart+1);
        }

        public String typeToString() {
            return _posType.ToString();
        }
    }
}