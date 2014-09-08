using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace tapLib.Args.ParamQuery {
    /// <summary>
    /// A FieldConstraintGroup is a fieldName and a list of constraintGroupsFor
    /// as presented in the query.  For instance in pi=*wi*, the fieldName
    /// is pi and the constraint is *wi*.
    /// </summary>
    public class FieldConstraintGroup : IEquatable<FieldConstraintGroup> {
        public enum ConstraintType { NUMERIC_CONSTRAINT, TEXT_CONSTRAINT, NULL_CONSTRAINT };
        private String _fieldName;
        private readonly List<String> _constraints;
        private readonly bool _negated;
        private readonly ConstraintType _type;

        public String fieldName { get { return _fieldName;}  set { _fieldName = value; } }
        public List<String> constraints { get { return _constraints; } }
        public bool isNegated { get { return _negated; } }
        public ConstraintType type { get { return _type; } }

        internal FieldConstraintGroup(String fieldName, bool isNegated, ConstraintType type, List<String> constraints) {
            _fieldName = fieldName;
            _constraints = constraints;
            _negated = isNegated;
            _type = type;
        }

        public override string ToString() {
            var sb = new StringBuilder();
            sb.Append("fieldName: ");
            sb.Append(fieldName);
            sb.Append('\n');
            sb.Append("isNegated: ");
            sb.Append(isNegated ? "Negated" : "Not Negated");
            sb.Append('\n');
            sb.Append("type: ");
            sb.Append(type == ConstraintType.NUMERIC_CONSTRAINT ? "numeric" : (type == ConstraintType.TEXT_CONSTRAINT ? "text" : "null"));
            sb.Append('\n');
            sb.Append("Constraints:\n");
            foreach (String s in constraints) {
                sb.Append(s);
                sb.Append('\n');
            }
            return sb.ToString();
        }

        public override bool Equals(object obj) {
            if (!(obj is FieldConstraintGroup)) return false;
            return Equals((FieldConstraintGroup)obj);
        }

        public bool Equals(FieldConstraintGroup obj) {
            if (fieldName != obj.fieldName) return false;
            for (int i = 0; i < constraints.Count(); i++) {
                if (constraints[i] != obj.constraints[i]) return false;
            }
            return true;
        }

        public override int GetHashCode() {
            return _fieldName.GetHashCode() + _constraints.GetHashCode();
        }

        public static bool operator ==(FieldConstraintGroup fc1, FieldConstraintGroup fc2) {
            return fc1.Equals(fc2);
        }

        public static bool operator !=(FieldConstraintGroup fc1, FieldConstraintGroup fc2) {
            return !fc1.Equals(fc2);
        }
    }

    public class ConstraintGroupList {
        private readonly List<FieldConstraintGroup> _constraintGroups = new List<FieldConstraintGroup>();

        public void Add(FieldConstraintGroup group) {
            _constraintGroups.Add(group);
        }

        // This method returns a list of all constraintGroupsFor for all parameters
        // ordered by fieldName
        public List<FieldConstraintGroup> constraintGroups() {
            return _constraintGroups.OrderBy(n => n.fieldName).ToList();
        }

        /// <summary>
        /// Returns the total number of fields that have constraintGroupsFor
        /// </summary>
        /// <returns>the count of fields with constraintGroupsFor</returns>
        internal int numberOfConstraintGroups() {
            return _constraintGroups.Count();
        }

        // Return the number of constraintGroupsFor for a fieldName
        public int numberOfConstraintGroupsFor(String fieldName) {
            return constraintGroupsFor(fieldName).Count();
        }

        // Return the total number of constraints in all groups
        public int totalNumberOfConstraints() {
            return _constraintGroups.Aggregate(0, (seed, n) => seed + n.constraints.Count());
        }

        // Return the number of constraints for a single group name
        public int numberOfConstraintsFor(String fieldName) {
            List<FieldConstraintGroup> groupsFor = constraintGroupsFor(fieldName);
            return groupsFor.Aggregate(0, (seed, n) => seed + n.constraints.Count);
        }

        // Return the constrained fieldNames
        public List<String> constraintFieldNames() {
            return _constraintGroups.Select(n => n.fieldName).Distinct().ToList();
        }

        /// <summary>
        /// Returns the list of constraintGroupsFor for a single field name.
        /// Note: A FieldConstraintGroup includes its field name.
        /// Note: if two constraint groups are in the query for the _same_ fieldName,
        ///       there will be two entries for that name, each with a constraint list.
        ///       This is to allow each to be "or'd" and the entire thing to be negated
        ///       as the spec requires. Argh.
        /// </summary>
        /// <param name="fieldName">Name of a field</param>
        /// <returns></returns>
        public List<FieldConstraintGroup> constraintGroupsFor(String fieldName) {
            // Ensure no spaces, makes tests work
            fieldName = fieldName.Trim();
            return _constraintGroups.Where(r => r.fieldName == fieldName).ToList();
        }
    }

}