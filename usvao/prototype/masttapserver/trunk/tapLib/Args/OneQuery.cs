namespace tapLib.Args {
    public class OneQuery {
        private readonly long _id;
        private readonly TapMTimeArg _mtime;
        private readonly TapPos _pos;
        private readonly TapRegionArg _region;
        private readonly TapSizeArg _size;

        // properties
        public long id { get { return _id; } }
        public TapPos pos { get { return _pos; } }
        public TapSizeArg size { get { return _size; } }
        public TapRegionArg region { get { return _region; } }
        public TapMTimeArg mtime { get { return _mtime; } }

        public OneQuery(long id, TapPos pos, TapSizeArg size, TapRegionArg region, TapMTimeArg mtime) {
            _id = id;
            _pos = pos;
            _size = size;
            _region = region;
            _mtime = mtime;
        }

        // Convenience constructors
        public OneQuery() : this(0) {}

        public OneQuery(long id) {
            _id = id;
            _pos = TapPosArg.DEFAULT.posList[0];
            _size = TapSizeArg.DEFAULT;
            _region = TapRegionArg.DEFAULT;
            _mtime = TapMTimeArg.DEFAULT;
        }
    }
}