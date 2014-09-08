using System;
using tapLib.Args;
using tapLib.Db.ParamQuery;

namespace tapLib.Stsci {
    public class PhotoPrimaryGenerator : AbstractSqlQueryGenerator {
        /// <summary>
        /// {0} SELECT plus the list of query columns needed (from default generator)
        /// {1} ra in degrees
        /// {2} dec in degrees
        /// {3} query level - assumes all of HLA tables
        /// {4} radius of search in minutes
        /// {5} is the additional where business from the query
        /// </summary>
        private const string POSITION_SQL_TEMPLATE =
            "SELECT {0} " +
            "FROM PhotoPrimary AS t0 INNER JOIN dbo.fGetNearbyObjEq({1}, {2}, {3}) AS t1 ON t0.objID=t1.objID ";

        private const string WITH_WHERE_TEMPLATE =
             "WHERE {0} order by distance";

        const string WITHOUT_WHERE_TEMPLATE =
             " order by distance";

        private String _selectClause;
        private String _whereClause;

        public override String tableName { get { return "PhotoPrimary"; } }

        public override bool generateSQL(TapQueryArgs args)
        {
            if (args == null) return false;

            if (!base.generateSQL(args)) return false;

            _selectClause = generateSelectArg(args.query);

            _whereClause = generateWhereArg(args.query);

            return true;
        }

        public override string ToSQL(TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime)
        {
            return String.Format(POSITION_SQL_TEMPLATE,
                                 _selectClause,
                                 pos.ra,
                                 pos.dec,
                                 size.getRadiusInArcMin()) +
                                 (_whereClause == String.Empty ? WITHOUT_WHERE_TEMPLATE : String.Format(WITH_WHERE_TEMPLATE, _whereClause));
        }

        // Other ToSQL is base class version
    }
}
