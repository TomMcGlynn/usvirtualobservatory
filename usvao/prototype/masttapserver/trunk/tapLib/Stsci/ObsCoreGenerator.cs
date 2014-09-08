using System;
using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Db.ParamQuery;
using System.Collections.Generic;

namespace tapLib.Stsci
{
    public class ObsCoreGenerator : AbstractSqlQueryGenerator
    {
        /// <summary>
        /// {0} SELECT plus the list of query columns needed (from default generator)
        /// {1} ra in degrees
        /// {2} dec in degrees
        /// {3} query level - assumes all of HLA tables
        /// {4} radius of search in minutes
        /// {5} is the additional where business from the query
        /// </summary>

        private String _selectClause;
        private String _fromClause;
        private String _whereClause;

        // 0 = select columns
        // 1 = get internal table name from config
        // 2, 3, 4 = arguments to fgetnearbyobj (ra, dec, rad)
        private const string POSITION_SQL_TEMPLATE =
            "{0} " +
            "FROM {1} AS t0 INNER JOIN dbo.fGetNearbyObjEq('{1}', {2}, {3}, {4}) AS t1 ON t0.obs_id=t1.dataID ";
        
        private const string WITH_WHERE_TEMPLATE = "{0} order by distance";
        private const string WITHOUT_WHERE_TEMPLATE =" order by distance";


        public override string tableName { get { return "obscore"; } }

        public override string generateSelect(QueryArg qa)
        {
            string tablename = qa.tableName;
            //Config.TapConfiguration.Instance._getTableNameByAlias(qa.from, ref tablename);
            string database = Config.TapConfiguration.Instance.DatabaseForTable(qa.from);

            String baseResult = base.generateSelect(qa);
            if (!qa.selectFields.Contains("$std") && !qa.selectFields.Contains("$all"))
            {
                return baseResult;
            }
            else
            {
                List<string> list = null;
                string strReplace = string.Empty;
                if (qa.selectFields.Contains("$std"))
                {
                    list = Config.TapConfiguration.Instance.StdColumns(database, tablename);
                    strReplace = "$std";
                }
                else //$ALL
                {
                    list = Config.TapConfiguration.Instance.AllColumns(database, tablename);
                    strReplace = "$all";
                }

                string strList = string.Empty;
                for (int i = 0; i < list.Count; ++i)
                {
                    string useColName = list[i];
                    Config.TapConfiguration.Instance._getColumnNameByAlias(database, qa.tableName, list[i], ref useColName);
                    //strList += list[i];
                    strList += useColName + " AS '" + list[i] + '\'';
                    if (i < list.Count - 1)
                        strList += ", ";
                }

                return baseResult.Replace(strReplace, strList);
            }
        }

        public bool CheckTableValidity(TapQueryArgs args)
        {
            if (args.query.from.StartsWith("TAP_SCHEMA"))
            {
                //do something
                return true;
            }
            else
            {
                string database = Config.TapConfiguration.Instance.DatabaseForTable(args.query.from);

                //do we serve this table at all?
                if (!Config.TapConfiguration.Instance.isSupportedTable(database, args.query.from))
                    return false;

                //are "select" columns relevant?
                List<string> cols = Config.TapConfiguration.Instance.AllColumns(database, args.query.from);
                if (cols.Count == 0)
                {
                    args.query._AddProblem("No valid columns to select.");
                    return false;
                }

                for (int i = 0; i < args.query.selectFields.Count; ++i)
                {
                    string str = args.query.selectFields[i];
                    if (str != "$all" && str != "$pos" && str != "$std" &&!cols.Contains(str.ToLower().Trim()))
                    {
                        args.query._AddProblem("invalid select column: " + str + '.');
                        return false;
                    }
                }
            }
            return true;
        }

        public override string generateFrom(QueryArg arg)
        {
            //note no actual 'from', put in in q.
            //doing this for positional query formatting.
            return base.generateFromArg(arg); 
        }

        public override bool generateSQL(TapQueryArgs args)
        {
            if (args == null) return false;


            if (!CheckTableValidity(args)) return false;

            //if (!base.generateSQL(args)) return false;

            _selectClause = generateSelect(args.query);
            _fromClause =  generateFrom(args.query);
            _whereClause = base.generateWhere(args.query);

            return true;
        }

        // Other ToSQL is base class version
        public override string ToSQL()
        {
            String formatted = _selectClause + " FROM " + _fromClause + ' ' + _whereClause;
            return formatted;
        }

        public override string ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime)
        {


            // 0 = columns
            // 1 = get internal table name from config
            // 2, 3, 4 = arguments to fgetnearbyobj (ra, dec, rad)
            return String.Format(POSITION_SQL_TEMPLATE,
                                 _selectClause,
                                 _fromClause,
                                 pos.ra,
                                 pos.dec,
                                 size.getRadiusInArcMin()) +
                                 (_whereClause == String.Empty ? WITHOUT_WHERE_TEMPLATE : String.Format(WITH_WHERE_TEMPLATE, _whereClause));
        }
    }
}
