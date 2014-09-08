using System;
using tapLib.Args.ParamQuery;

namespace tapLib.Args {
    public class TapQueryArgs {
        private readonly TapPosArg _pos;
        private readonly TapSizeArg _size;
        private readonly QueryArg _query;
        private readonly TapRegionArg _region;
        private readonly TapMTimeArg _mtime;

        private readonly string _adql;
        
        // Properties
        public QueryArg query { get { return _query; } }
        public TapPosArg pos { get { return _pos; } }
        public TapSizeArg size { get { return _size; } }
        public TapRegionArg region { get { return _region; } }
        public TapMTimeArg mtime { get { return _mtime; } }
        public string adql { get { return _adql; } }

        // Convenience methods
        public Boolean hasPos { get { return !_pos.isEmpty; } }

        public TapQueryArgs(TapPosArg posArg, TapSizeArg sizeArg, QueryArg queryArg, TapRegionArg regionArg, TapMTimeArg mtimeArg) {
            _pos = posArg;
            _size = sizeArg;
            _query = queryArg;
            _region = regionArg;
            _mtime = mtimeArg;

            _adql = null;
        }

        public TapQueryArgs(String adql, QueryArg queryArg)
        {
            _adql = adql;
            _query = queryArg;
        }

        // Convenience constructors
        public TapQueryArgs(QueryArg queryArg) {
            _pos = TapPosArg.Empty;
            _size = TapSizeArg.Empty;
            _query = queryArg;
            _region = TapRegionArg.Empty;
            _mtime = TapMTimeArg.Empty;

            _adql = null;
        }

        public TapQueryArgs(TapPosArg posArg, TapSizeArg sizeArg, QueryArg queryArg) {
            _pos = posArg;
            _size = sizeArg;
            _query = queryArg;
            _region = TapRegionArg.Empty;
            _mtime = TapMTimeArg.Empty;

            _adql = null;
        }
    }
}
