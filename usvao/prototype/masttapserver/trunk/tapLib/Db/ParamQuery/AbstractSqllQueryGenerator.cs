using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using tapLib.Args;
using tapLib.Args.ParamQuery;

namespace tapLib.Db.ParamQuery
{
    /// <summary>
    /// This class provides a default conversion FROM, SELECT, WHERE part of the query -- 
    /// It returns something that can be used to generate an SQL query that doesn't use
    /// any position information.
    /// The class implements the ISqlGenerator interface.
    /// The class is abstract meaning that true generators (implementers of ISqlGenerator)
    /// can inherit from this to get the non-position SQL conversion and override methods
    /// to create the position-dependent SQL.  Various methods such as generateFromArg,
    /// generateSelectArg, and generateWhereArg are the right candidates for doing this.
    /// </summary>
    public abstract class AbstractSqlQueryGenerator : ISqlGenerator
    {
        protected const char WHERE_WILD_CARD_CHAR = '*';
        protected const char WHERE_NUMERIC_RANGE_CHAR = '/';

        protected const char SQLSERVER_LIKE_CHAR = '%';

        private StringBuilder _sqlResult;

        // OPTIONS -- Eventually make an option somehow
        public enum OPTION
        {
            USE_IN_EQUALITY_OPTION,
            USE_BETWEEN_RANGE_OPTION
        } ;

        // options
        private Boolean _useInEqualityOption = true;
        private Boolean _useBetweenRangeOption = true;

        internal Boolean USE_IN_EQUALITY_OPTION
        {
            get { return _useInEqualityOption; }
        }

        internal Boolean USE_BETWEEN_RANGE_OPTION
        {
            get { return _useBetweenRangeOption; }
        }

        // Set options
        public void setOption(OPTION option, Boolean value)
        {
            switch (option)
            {
                case OPTION.USE_IN_EQUALITY_OPTION:
                    _useInEqualityOption = value;
                    break;
                case OPTION.USE_BETWEEN_RANGE_OPTION:
                    _useBetweenRangeOption = value;
                    break;
                default:
                    break;
            }
        }

        public virtual String generateSelectArg(QueryArg qa)
        {
            int numberSelectFields = qa.selectFieldCount();

            // If no select was provided, return $STD, which is resolved in validator
            if (numberSelectFields == 0)
            {
                return "$STD";
            }

            string dbName = String.Empty;
            string internalTableName = String.Empty;
            string internalColumnName = String.Empty;
            if (qa.from != String.Empty)
            {
                Config.TapConfiguration.Instance._getTableNameByAlias(qa.from, ref internalTableName);
                dbName = Config.TapConfiguration.Instance.DatabaseForTable(qa.from);
            }

            // There are some fields
            List<String> selectFields = qa.selectFields;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numberSelectFields - 1; i++)
            {
                internalColumnName = string.Empty;
                if (internalTableName != string.Empty)
                {
                    if (Config.TapConfiguration.Instance._getColumnNameByAlias(dbName, internalTableName, selectFields[i], ref internalColumnName))
                    {
                        sb.Append(internalColumnName);
                        sb.Append(" AS '");
                        sb.Append(selectFields[i] + '\'');
                    }
                    else
                        sb.Append(selectFields[i]);
                }
                else
                {
                    sb.Append(selectFields[i]);
                }
                sb.Append(',');
            }

            internalColumnName = string.Empty;
            if (internalTableName != string.Empty)
            {
                if (Config.TapConfiguration.Instance._getColumnNameByAlias(dbName, internalTableName, selectFields[numberSelectFields - 1], ref internalColumnName))
                {
                    sb.Append(internalColumnName);
                    sb.Append(" AS '");
                    sb.Append(selectFields[numberSelectFields - 1] + '\'');
                }
                else
                    sb.Append(selectFields[numberSelectFields - 1]);
            }
            else
            {
                sb.Append(selectFields[numberSelectFields - 1]);
            }
            return sb.ToString();
        }


        public virtual String generateSelect(QueryArg qa) {
            string sel = "SELECT ";

            if (qa.maxrec > 0) {
                sel += "TOP " + qa.maxrec + " ";
            }            
            sel += generateSelectArg(qa);
            return sel;
        }

        public virtual String generateFromArg(QueryArg qa)
        {
            string internalTableName = String.Empty;
            if (qa.from != String.Empty)
            {
                if (Config.TapConfiguration.Instance._getTableNameByAlias(qa.from, ref internalTableName))
                    return internalTableName;
            }
            return qa.tableName;
        }

        public virtual String generateFrom(QueryArg arg)
        {
            return "FROM " + generateFromArg(arg);
        }

        public virtual String generateWhereArg(QueryArg qa) {
            StringBuilder sb = new StringBuilder();

            int constraintFieldNameCount = qa.constraintFieldNames().Count();
            if (constraintFieldNameCount == 0)
                return sb.ToString();

            //aliasing possible at the table and column levels.
            string dbName = String.Empty;
            string internalTableName = qa.from;
            if (qa.from != String.Empty)
            {
                Config.TapConfiguration.Instance._getTableNameByAlias(qa.from, ref internalTableName);
                dbName = Config.TapConfiguration.Instance.DatabaseForTable(qa.from);
            }
            foreach (FieldConstraintGroup field in qa.constraintGroups())
            {
                String useColumnName = field.fieldName;
                if (Config.TapConfiguration.Instance._getColumnNameByAlias(dbName, internalTableName, field.fieldName, ref useColumnName))
                    field.fieldName = useColumnName;
            }

            int fieldNameCounter = 0;
            foreach (String fieldName in qa.constraintFieldNames()) {
                StringBuilder whereClause = _convertConstraintsForFieldName( qa.constraintGroupsFor(fieldName));

                sb.Append(whereClause);
                if (constraintFieldNameCount > 1 && fieldNameCounter < constraintFieldNameCount-1) {
                    sb.Append(" AND ");
                    fieldNameCounter++;
                }
            }
            return sb.ToString();
        }

        public virtual String generateWhere(QueryArg arg) {
            String whereArg = generateWhereArg(arg);
            return whereArg.Length > 0 ? "WHERE " + whereArg : String.Empty;
        }

        protected static StringBuilder _convertTextWildCardConstraint(String fieldName, String constraint)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append('(');
            sb.Append(fieldName);
            sb.Append(" LIKE '");
            sb.Append(constraint.Replace(WHERE_WILD_CARD_CHAR, SQLSERVER_LIKE_CHAR));
            sb.Append('\'');
            sb.Append(')');
            return sb;
        }

        protected static StringBuilder _convertTextEqualityConstraint(String fieldName, String constraint)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append('(');
            sb.Append(fieldName);
            sb.Append("=\'");
            sb.Append(constraint);
            sb.Append('\'');
            sb.Append(')');
            return sb;
        }

        protected static StringBuilder _convertOneTextConstraint(String fieldName, String constraint)
        {
            return constraint.Contains(WHERE_WILD_CARD_CHAR)
                       ? _convertTextWildCardConstraint(fieldName, constraint)
                       : _convertTextEqualityConstraint(fieldName, constraint);
        }

        protected static StringBuilder _convertTextConstraint(FieldConstraintGroup fcg)
        {
            StringBuilder sb = new StringBuilder();
            int numberConstraints = fcg.constraints.Count();
            String constraint;

            // If the expression is negated, and there are multiple or'ed constraints
            // Add another paren pair as is done in SQL Server
            if (fcg.isNegated)
            {
                sb.Append(numberConstraints > 1 ? "(NOT (" : "(NOT ");
            }

            for (int i = 0; i < numberConstraints - 1; i++)
            {
                constraint = fcg.constraints[i];
                sb.Append(_convertOneTextConstraint(fcg.fieldName, constraint));
                sb.Append(" OR ");
            }
            // The last one or one
            constraint = fcg.constraints[numberConstraints - 1];
            sb.Append(_convertOneTextConstraint(fcg.fieldName, constraint));

            if (fcg.isNegated)
            {
                sb.Append(numberConstraints > 1 ? "))" : ")");
            }
            return sb;
        }

        protected StringBuilder _convertNumericRangeConstraint(String fieldName, Boolean isNegated, String constraint)
        {
            StringBuilder sb = new StringBuilder();
            // Test for full range, open, etc.
            // First case, starts with / flux,/23
            if (constraint[0] == WHERE_NUMERIC_RANGE_CHAR)
            {
                sb.Append('(');
                sb.Append(fieldName);
                sb.Append(isNegated ? " > " : " <= ");
                sb.Append(constraint.Substring(1));
                sb.Append(')');
                return sb;
            }

            if (constraint[constraint.Length - 1] == WHERE_NUMERIC_RANGE_CHAR)
            {
                sb.Append('(');
                sb.Append(fieldName);
                sb.Append(isNegated ? " < " : " >= ");
                sb.Append(constraint.Substring(0, constraint.Length - 1));
                sb.Append(')');
                return sb;
            }

            // Full range
            int rangeCharIndex = constraint.IndexOf(WHERE_NUMERIC_RANGE_CHAR);
            if (rangeCharIndex == -1)
            {
                sb.Append("Error: found no / in range");
                return sb;
            }
            // Output different stuff depending on whether or not one wants to use BETWEEN or <= =>
            if (USE_BETWEEN_RANGE_OPTION) {
                sb.Append('(');
                sb.Append(fieldName);
                sb.Append(isNegated ? " NOT BETWEEN " : " BETWEEN ");
                sb.Append(constraint.Substring(0, rangeCharIndex));
                sb.Append(" AND ");
                sb.Append(constraint.Substring(rangeCharIndex + 1));
                sb.Append(')');
            }
            else
            {
                sb.Append('(');
                sb.Append('(');
                sb.Append(fieldName);
                sb.Append(isNegated ? " < " : " >= ");
                sb.Append(constraint.Substring(0, rangeCharIndex));
                sb.Append(')');
                sb.Append(isNegated ? " OR " : " AND ");
                sb.Append('(');
                sb.Append(fieldName);
                sb.Append(isNegated ? " > " : " <= ");
                sb.Append(constraint.Substring(rangeCharIndex + 1));
                sb.Append(')');
                sb.Append(')');
            }
            return sb;
        }

        protected StringBuilder _convertNumericRangeConstraint(String fieldName, String constraint) {
            return _convertNumericRangeConstraint(fieldName, false, constraint);
        }

        protected static StringBuilder _convertNumericEqualityConstraint(String fieldName, Boolean isNegated, String constraint)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append('(');
            sb.Append(fieldName);           
            sb.Append(isNegated ? "!=" : "=");
            sb.Append(constraint);
            sb.Append(')');
            return sb;
        }

        // This method is here to allow the case when there is a complex multiple piece constraint
        // and the NOT is handled at the beginning of the complex constraint so != is not appropriate
        protected static StringBuilder _convertNumericEqualityConstraint(String fieldName, String constraint) 
        {
            return _convertNumericEqualityConstraint(fieldName, false, constraint);
        }

        protected static StringBuilder _inNumericEqualityConstraint(FieldConstraintGroup fcg)
        {
            // Assumes all are an equality
            StringBuilder sb = new StringBuilder();
            int numberConstraints = fcg.constraints.Count();

            if (fcg.isNegated)
            {
                sb.Append("(NOT ");
            }
            sb.Append('(');
            sb.Append(fcg.fieldName);
            sb.Append(" IN (");
            for (int i = 0; i < numberConstraints - 1; i++)
            {
                sb.Append(fcg.constraints[i]);
                sb.Append(',');
            }
            sb.Append(fcg.constraints[numberConstraints - 1]);
            sb.Append("))");
            if (fcg.isNegated)
            {
                sb.Append(')');
            }
            return sb;
        }

        protected static Boolean _isNumericRange(String constraint) {
            return constraint.Contains(WHERE_NUMERIC_RANGE_CHAR);
        }

        protected StringBuilder _convertOneNumericConstraint(String fieldName, String constraint)
        {
            return _isNumericRange(constraint)
                       ?
                           _convertNumericRangeConstraint(fieldName, constraint)
                       :
                           _convertNumericEqualityConstraint(fieldName, constraint);
        }

        // If all the constraints are single values the query can be optimized
        // This tests for something like flux,23,24,25.2
        protected static Boolean _allNumericEquality(FieldConstraintGroup fcg)
        {
            for (int i = 0; i < fcg.constraints.Count(); i++)
            {
                if (fcg.constraints[i].Contains(WHERE_NUMERIC_RANGE_CHAR))
                {
                    return false;
                }
            }
            return true;
        }

        protected StringBuilder _convertNumericConstraint(FieldConstraintGroup fcg)
        {
            StringBuilder sb = new StringBuilder();
            int numberConstraints = fcg.constraints.Count();

            // Test for case of 1 to enable != = 
            if (numberConstraints == 1) {
                String oneConstraint = fcg.constraints[0];
                if (_isNumericRange(oneConstraint)) {
                    sb.Append(_convertNumericRangeConstraint(fcg.fieldName, fcg.isNegated, oneConstraint));
                    return sb;
                }
                return _convertNumericEqualityConstraint(fcg.fieldName, fcg.isNegated, fcg.constraints[0]);
            }

            // Special test to possibly use "in"
            if (numberConstraints > 1)
            {
                if (_allNumericEquality(fcg) && USE_IN_EQUALITY_OPTION)
                {
                    return _inNumericEqualityConstraint(fcg);
                }
            }

            // Now we are in a complex constraint with multiple constraint pieces
            // If the expression is negated, and there are multiple or'ed constraints
            // Add another paren pair as is done in SQL Server
            if (fcg.isNegated)
            {
                sb.Append(numberConstraints > 1 ? "(NOT (" : "(NOT ");
            }

            // Use an alternate approach using OR
            String constraint;
            for (int i = 0; i < numberConstraints - 1; i++)
            {
                constraint = fcg.constraints[i];
                sb.Append(_convertOneNumericConstraint(fcg.fieldName, constraint));
                sb.Append(" OR ");
            }

            // The last one or one
            constraint = fcg.constraints[numberConstraints - 1];
            sb.Append(_convertOneNumericConstraint(fcg.fieldName, constraint));

            if (fcg.isNegated)
            {
                sb.Append(numberConstraints > 1 ? "))" : ")");
            }

            return sb;
        }

        protected static StringBuilder _convertNullConstraint(FieldConstraintGroup fcg) {
            StringBuilder sb = new StringBuilder();
            sb.Append('(');
            sb.Append(fcg.fieldName);
            sb.Append(fcg.isNegated ? " IS NOT NULL" : " IS NULL");
            sb.Append(')');
            return sb;
        }

        protected StringBuilder _convertOneConstraint(FieldConstraintGroup fcg)
        {
            if (fcg.type == FieldConstraintGroup.ConstraintType.NULL_CONSTRAINT) {
                return _convertNullConstraint(fcg);
            }

            if (fcg.type == FieldConstraintGroup.ConstraintType.TEXT_CONSTRAINT)
            {
                return _convertTextConstraint(fcg);
            }
            return _convertNumericConstraint(fcg);
        }

        protected StringBuilder _convertConstraintsForFieldName(IList<FieldConstraintGroup> constraintGroups)
        {
            StringBuilder sb = new StringBuilder();
            int numberGroups = constraintGroups.Count();

            if (numberGroups == 0)
            {
                // ERROR
                return sb;
            }
            // At least one group
            if (numberGroups == 1)
            {
                sb.Append(_convertOneConstraint(constraintGroups[0]));
                return sb;
            }

            // More than one group
            for (int i = 0; i < numberGroups - 1; i++)
            {
                sb.Append(_convertOneConstraint(constraintGroups[i]));
                sb.Append(" AND ");
            }
            sb.Append(_convertOneConstraint(constraintGroups[numberGroups - 1]));

            return sb;
        }

        /// <summary>
        /// This method is abstract and should be implemented by subclasses
        /// </summary>
        public abstract String tableName { get; }

        public virtual bool generateSQL(TapQueryArgs queryArg)
        {
            _sqlResult = new StringBuilder();

            // Generate select
            String selectClause = generateSelect(queryArg.query);
            _sqlResult.Append(selectClause);
            // Append a final space to make things nice
            _sqlResult.Append(' ');

            String fromClause = generateFrom(queryArg.query);
            _sqlResult.Append(fromClause);

            String whereClause = generateWhere(queryArg.query);
            // Append a final space to make things nice
            if (whereClause != String.Empty) {
                _sqlResult.Append(' ');
                _sqlResult.Append(whereClause);
            }
            return true;
        }

        public virtual string ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime)
        {
            return ToSQL();
        }

        public virtual string ToSQL()
        {
            return _sqlResult.ToString();
        }

        public override string ToString()
        {
            return ToSQL();
        }
    }
}