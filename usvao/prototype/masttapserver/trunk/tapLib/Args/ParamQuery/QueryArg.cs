using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace tapLib.Args.ParamQuery {
    public class QueryArg {
        private static readonly char[] SELECT_FIELD_SEPARATOR = new[] {','};
        private static readonly char[] FROM_NAME_SEPARATOR = new[] {'.'};

        private readonly String _paramQuery;
        // for now
        private String _from;
        private String _select;
        private String _where;
        private Boolean _isValid = true;
        private readonly List<String> _selectFields = new List<String>();
        private String _databaseName;
        private String _schemaName;
        private String _tableName;
        private int _maxrec = 0;

        private readonly List<String> _problem = new List<String>();
        private readonly ConstraintGroupList _constraintGroups = new ConstraintGroupList();
        internal DiagArg _diagArg = DiagArg.OFF;

        // Properties
        public String from { get { return _from; } }
        public String select { get { return _select; } }
        public String where { get { return _where; } }
        public Boolean isValid { get { return _isValid; } }
        public List<String> problems { get { return _problem; } }
        internal void _AddProblem(String problem) {
            _problem.Add(problem);
            _isValid = false;
        }
        public List<String> selectFields { get { return _selectFields; } }
        public String tableName { get { return _tableName; } }
        public String databaseName { get { return _databaseName; } }
        public String schemaName { get { return _schemaName; } }
        public ConstraintGroupList constraintGroupList { get { return _constraintGroups; } }
        public DiagArg diag { get { return _diagArg; } }
        public int maxrec { get { return _maxrec; } }

        /// <summary>
        /// This class handles a new param query string and creates args, etc
        /// </summary>
        /// <param name="paramQuery"></param>
        /// <param name="diagArg"></param>
        public QueryArg(String paramQuery, DiagArg diagArg) {
            // This is a bit of a hack, but I'm adding a final & to make all the parts of the
            // query look the same for the reg exps below
            _paramQuery = paramQuery+"&";
            _diagArg = diagArg;
            _parse();
        }

        public QueryArg(String paramQuery) : this(paramQuery, DiagArg.OFF) { }

        public QueryArg(System.Collections.Specialized.NameValueCollection input)
        {
            _paramQuery = string.Empty;
            for (int i = 0; i < input.Count; ++i)
            {
                if (input[i] != null && input[i] != string.Empty)
                    _paramQuery += input.AllKeys[i] + '=' + input[i] + '&';
            }
            _diagArg = DiagArg.OFF;
            _parse();
        }

        //tdower this is temporary = extract from adql somehow?)
        public QueryArg(String table, bool isADQL)
        {
            _tableName = table;
        }

        private void _parse() {
            _findFrom();
            _findSelect();
            _findWhere();
            _findMaxRec();
            findConstraints();
            findSelectFields();
        }

        private void _checkForMultipleMatches(ICollection m) {
            if (m.Count == 1) return;
            _isValid = false;
            _AddProblem("A ParamQuery is allowed to have only one FROM, SELECT, or WHERE parameter.");
        }

        private void _findFromParts() {
            if (String.IsNullOrEmpty(_from)) return;

            // Break up parts based on 
            String[] fromParts = _from.Split(FROM_NAME_SEPARATOR, StringSplitOptions.RemoveEmptyEntries);
            switch (fromParts.Length) {
                    // Only table name is given
                case 1:
                    _databaseName = String.Empty;
                    _schemaName = String.Empty;
                    _tableName = fromParts[0];
                    break;
                case 2:
                    _databaseName = String.Empty;
                    _schemaName = fromParts[0];
                    _tableName = fromParts[1];
                    break;
                case 3:
                    _databaseName = fromParts[0];
                    _schemaName = fromParts[1];
                    _tableName = fromParts[2];
                    break;
                default:
                    // Shouldn't happen, must be 1 to get here
                    _databaseName = String.Empty;
                    _schemaName = String.Empty;
                    _tableName = fromParts[0];
                    break;
            }
        }

        private void _findFrom() {
            // RegEx ?i is ignore case
            // \s* allows white space between FROM and =
            // ?'from' creates a group "from"
            // .*?(?=&) matches everything until the next & and assigns it to group 'from'
            MatchCollection m = Regex.Matches(_paramQuery, @"(?i)FROM\s*=(?'from'.*?(?=&))");
            if (m.Count > 0) {
                _from = m[0].Groups["from"].Value;
                // Check for multiple matches
                _checkForMultipleMatches(m);
                _findFromParts();
            }
            else {
                _isValid = false;
                _AddProblem("ParamQuery must have a FROM parameter.");
            }
        }

        private void _findMaxRec(){
            MatchCollection m = Regex.Matches(_paramQuery, @"(?i)MAXREC\s*=(?'maxrec'.*?(?=&))");
            if (m.Count > 0) {
                string max = m[0].Groups["maxrec"].Value; //only pay attention to the first maxrec
                try{
                    int imax = Convert.ToInt32(max);
                    if (imax > 0)
                        _maxrec = imax;
                    else
                        _problem.Add("MAXREC argument not a valid positive number");
                }
                catch (Exception ) {
                    _problem.Add( "MAXREC argument not a valid positive number");
                }
            }

        }

        private void _findSelect() {
            MatchCollection m = Regex.Matches(_paramQuery, @"(?i)SELECT\s*=(?'select'.*?(?=&))");
            if (m.Count > 0) {
                _select = m[0].Groups["select"].Value;
                // Check for multiple matches
                _checkForMultipleMatches(m);
            }
            else {
                // NO SELECT -- set to $STD
                _select = "$STD";
            }
        }

        private void _findWhere() {
            MatchCollection m = Regex.Matches(_paramQuery, @"(?i)WHERE\s*=(?'where'.*?(?=&))");
            if (m.Count > 0) {
                _where = m[0].Groups["where"].Value;
                // Check for multiple matches
               _checkForMultipleMatches(m);
            }
            else {
                // NO WHERE 
                _where = String.Empty;
            }
        }

        // Analyze the where clause to break it into individual constraintGroupsFor indexed by fieldname
        public void findConstraints() {
            // Don't do anything if there are no constraintGroupsFor
            if (where == String.Empty) return;

            // Check to see if it has been run yet --> if there are any constraintGroupsFor, return
            if (constraintGroups().Count() > 0) return;

            WhereClauseParser wp = new WhereClauseParser(where, constraintGroupList, diag);
            try {
                wp.parse();
            } catch (Exception ex) {
                _isValid = false;
                _AddProblem(ex.ToString());
            }
        }

        public void findSelectFields() {
            if (String.IsNullOrEmpty(select)) return;

            String[] fields = select.Split(SELECT_FIELD_SEPARATOR, StringSplitOptions.RemoveEmptyEntries);
            //
            foreach (String selectField in fields) {
                if (diag.isOn()) Console.WriteLine("selectField: " + selectField);
                _selectFields.Add(selectField.ToLower());
            }
        }

        public int selectFieldCount() {
            return _selectFields.Count();
        }

        public int numberOfUniqueFieldsWithConstraints() {
            return constraintFieldNames().Count();
        }

        // This method returns a list of all constraintGroupsFor for all parameters
        // ordered by fieldName
        public List<FieldConstraintGroup> constraintGroups() {
            return constraintGroupList.constraintGroups();
        }

        public List<FieldConstraintGroup> constraintGroupsFor(String fieldName) {
            return constraintGroupList.constraintGroupsFor(fieldName);
        }

        /// <summary>
        /// Returns the total number of fields that have constraintGroupsFor
        /// </summary>
        /// <returns>the count of fields with constraintGroupsFor</returns>
        internal int numberOfConstraintGroups() {
            return constraintGroupList.numberOfConstraintGroups();
        }

        // Return the number of constraintGroupsFor for a fieldName
        public int numberOfConstraintGroupsFor(String fieldName) {
            return constraintGroupList.numberOfConstraintGroupsFor(fieldName);
        }

        // Return the constrained fieldNames
        public List<String> constraintFieldNames() {
            return constraintGroupList.constraintFieldNames();
        }

        // Return the total number of constraints in all groups
        public int totalNumberOfConstraints() {
            return constraintGroupList.totalNumberOfConstraints();
        }

        // Convenience method to dump problems
        public void dumpProblems() {
            foreach (String s in _problem) {
                Console.WriteLine("> " + s);
            }
        }
    }
}
