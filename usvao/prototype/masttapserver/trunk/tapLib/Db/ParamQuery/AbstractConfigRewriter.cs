using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using tapLib.Args;
using tapLib.Config;

namespace tapLib.Db.ParamQuery {
    public abstract class AbstractConfigRewriter : AbstractSqlQueryGenerator {
        private TapConfiguration _config;

        protected AbstractConfigRewriter(TapConfiguration config) {
            if (config == null) throw new NullReferenceException("Config can not be null in AbstractConfigRewriter");
            _config = config;
        }

        // This could go in here
/**
        public void interpretVariables(StringBuilder query, TapQueryArgs args, List<String> errors) {
            String q = query.ToString();
            StringBuilder sb = new StringBuilder();

            //If there are no variables, there is nothing to do.
            if (!q.Contains('$')) {
                return;
            }

            //todo - in case of multiple tables, make sure we are handling proper placement
            string[] froms = args.query.from.Split(new[] { ',' });

            TableServiceConfiguration config = new TableServiceConfiguration();
            if (q.Contains("$STD")) {
                ArrayList columns = config.GetStdColumns(froms[0]);
                for (int i = 0; i < columns.Count; ++i) {
                    sb.Append(((TAPServiceConfigurationDatabaseTableColumn)columns[i]).name);
                    if (i < columns.Count - 1) {
                        sb.Append(", ");
                    }
                }
                query.Replace("$STD", sb.ToString());

                if (froms.Length > 1) {
                    errors.Add("variables with multiple tables not yet implemented");
                }
            }
            if (q.Contains("$ALL")) {
                ArrayList columns = config.GetTapSchemaColumns(froms[0]);
                for (int i = 0; i < columns.Count; ++i) {
                    sb.Append(((TAPServiceConfigurationDatabaseTableColumn)columns[i]).name);
                    if (i < columns.Count - 1) {
                        sb.Append(", ");
                    }
                }
                query.Replace("$ALL", sb.ToString());

                if (froms.Length > 1) {
                    errors.Add("variables with multiple tables not yet implemented");
                }
            }
            //todo -- vospace and upload. At least handle as "unsupported" message.

            return;
        }
        **/
    }
}
