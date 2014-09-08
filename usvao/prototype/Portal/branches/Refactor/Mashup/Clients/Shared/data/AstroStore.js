Ext.define('Mvp.data.AstroStore', {
    extend: 'Mvp.data.BufferedStore',
    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.columnInfo = config.columnInfo;
    },
    
    // Accessors for special column info.
    getStcsFpColName: function() {
        var stcsFpColName = null;
        if (this.columnInfo && this.columnInfo.specialColumns) {
            stcsFpColName = this.columnInfo.specialColumns.stcsFpColName;
        }
        return stcsFpColName;
    },

    getRaColName: function() {
        var raColName = null;
        if (this.columnInfo && this.columnInfo.specialColumns) {
            raColName = this.columnInfo.specialColumns.raColName;
        }
        return raColName;
    },

    getDecColName: function() {
        var decColName = null;
        if (this.columnInfo && this.columnInfo.specialColumns) {
            decColName = this.columnInfo.specialColumns.decColName;
        }
        return decColName;
    },

    // Other public methods
    getFootprints: function () {
        var columnName = this.getStcsFpColName();
        var footprints = [];

        var store = this;
        var rowCollection = store.getCache();
        var count = rowCollection.getCount();

        if (columnName) {
            // Use the current grid contents, with all filters applied.
            for (var i = 0; i < count; i++) {
                var record = rowCollection.getAt(i);

                var fp = this.getFootprintForValue(record.get('s_region'));
                if (fp) {
                    footprints.push(fp);
                }
            }
        } else if (this.wcsCols) {
            // Use the WCS info to compute footprints.
            for (var i = 0; i < count; i++) {
                var record = rowCollection.getAt(i);

                var fp = this.getWcsFootprintForRecord(record);
                if (fp) {
                    footprints.push(fp);
                }
            }
        }
        return footprints;
    },

    getPositions: function (column) {
        var positions = [];

        if (this.hasPositions()) {
            // Use the current grid contents, with all filters applied.
            var store = this;
            var rowCollection = store.getCache();
            var count = rowCollection.getCount();
            for (var i = 0; i < count; i++) {
                var record = rowCollection.getAt(i);

                var ra = record.get(this.getRaColName());
                var dec = record.get(this.getDecColName());
                var positionObject = this.getPositionForValues(ra, this.raUnit, dec, this.decUnit);
                if (positionObject) {
                    positions.push(positionObject);
                }
            }
        }
        return positions;
    },

    hasFootprints: function () {
        var hasFootprints = this.getStcsFpColName();

        return hasFootprints;
    },

    hasPositions: function () {
        var hasPositions = (this.getRaColName() && this.getDecColName());

        return hasPositions;
    },
    
    // Private methods

    getFootprintForValue: function (val) {
        var fpObject = null;

        if (Ext.isString(val)) {
            // First some hacks to get the CAOM string into format (POLYGON ICRS  ([0-9\.]+ [ ]+)+)+
            // (or something like that)
            var fpString = val.replace(/\(/g, '');
            fpString = fpString.replace(/\)\)/g, 'POLYGON ICRS');

            if (fpString.length > 0) {
                fpObject = {
                    "footprint": fpString
                };
            }
        }
        return fpObject;
    },

    getWcsFootprintForRecord: function (record) {
        var wcs = this.getWcsForRecord(record);
        if (wcs) {

        }
    },

    getWcsForRecord: function (record) {
        var wcs = null;
        if (record.wcsObject) {
            wcs = record.wcsObject;
        } else if (this.wcsCols) {
            wcs = Ext.create('Mvp.util.Wcs', config);
        }
        return wcs;
    },

    getPositionForValues: function (ra, raUnit, dec, decUnit) {
        // NOTE:  We may need to use this.raUnit and this.decUnit to convert these values to decimal!

        var positionObject = {
            "ra": ra,
            "dec": dec
        };

        return positionObject;
    }

})