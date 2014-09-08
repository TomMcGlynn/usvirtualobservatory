Ext.define('Mvp.data.AstroStore', {
    requires: ['Mvp.util.Wcs'],
    extend: 'Mvp.data.BufferedStore',
    
    constructor: function(config) {
        this.callParent(arguments);
        
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
    
    getSiaExtentCols: function() {
        var siaExtentCols = null;
        if (this.columnInfo && this.columnInfo.specialColumns) {
            siaExtentCols = this.columnInfo.specialColumns.siaExtentCols;
        }
        return siaExtentCols;
    },
    
    getWcsCols: function() {
        var wcsCols = null;
        if (this.columnInfo && this.columnInfo.specialColumns) {
            wcsCols = this.columnInfo.specialColumns.wcsCols;
        }
        return wcsCols;
    },

    // Other public methods
    // which is one of 'ALL' (default), 'SELECTED' or 'UNSELECTED'
    getFootprints: function (which) {
        var columnName = this.getStcsFpColName();
        var siaExtentCols = this.getSiaExtentCols();
        
        var footprints = [];

        var store = this;
        var rowCollection = null;
        which = which || 'ALL'; // Default to getting all records
        if (which === 'SELECTED') {
            rowCollection = store.getSelectedRecords();
        } else if (which === 'UNSELECTED') {
            rowCollection = store.getUnselectedRecords();
        } else {  // ALL
            rowCollection = store.getFilteredRecords();
        }
        var count = rowCollection.length;

        if (columnName) {
            // Use the current grid contents, with all filters applied.
            for (var i = 0; i < count; i++) {
                var record = rowCollection[i];
        
                var fp = this.getFootprintForValue(record.get(columnName));
                if (fp) {
                    var fullId = record.internalId; 
                    fp._id_ = fullId;
                    footprints.push(fp);
                }
            }
        } else if (this.getWcsCols()) {
            // Use the WCS info to compute footprints.
            for (var i = 0; i < count; i++) {
                var record = rowCollection[i];
        
                var fp = this.getWcsFootprintForRecord(record);
                if (fp) {
                    var fullId = record.internalId; 
                    fp._id_ = fullId;
                    footprints.push(fp);
                }
            }
        } else if (siaExtentCols && this.hasPositions()) {
            for (i = 0; i < count; i++) {
                record = rowCollection[i];
        
                fp = this.getSiaExtentFpForRecord(record);
                if (fp) {
                    var fullId = record.internalId; 
                    fp._id_ = fullId;
                    footprints.push(fp);
                }
            }
        }

        return footprints;
    },

    // which is one of 'ALL' (default), 'SELECTED' or 'UNSELECTED'
    getPositions: function (which) {
        var positions = [];

        if (this.hasPositions()) {
            // Use the current grid contents, with all filters applied.
            var store = this;
            var rowCollection = null;
            which = which || 'ALL'; // Default to getting all records
            if (which === 'SELECTED') {
                rowCollection = store.getSelectedRecords();
            } else if (which === 'UNSELECTED') {
                rowCollection = store.getUnselectedRecords();
            } else {  // ALL
                rowCollection = store.getFilteredRecords();
            }
            var count = rowCollection.length;
            for (var i = 0; i < count; i++) {
                var record = rowCollection[i];

                var ra = record.get(this.getRaColName());
                var dec = record.get(this.getDecColName());
                var positionObject = this.getPositionForValues(ra, this.raUnit, dec, this.decUnit);
                if (positionObject) {
                    //var fullId = record[record.idProperty]; //record.getId();
                    var fullId = record.internalId; 
                    positionObject._id_ = fullId;
                    positions.push(positionObject);
                }
            }
        }
        return positions;
    },

    hasFootprints: function () {
        var hasFootprints = this.getStcsFpColName() || (this.getSiaExtentCols() && this.hasPositions());

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

    getSiaExtentFpForRecord: function (record) {
        var siaFpObject = record._siaFpObject;
        if (siaFpObject === undefined) {
            siaFpObject = null;  // null means we tried.  undefined means we haven't tried yet.
        
            var siaExtentCols = this.getSiaExtentCols();
            
            if (siaExtentCols && this.hasPositions()) {
    
                // Though the VOTable standard seems to indicate that array elements should be
                // separated by spaces, there are some implementations that use commas.
                
                var axesValue = record.get(siaExtentCols.naxisCol);
                axesValue = axesValue.replace(/[ ,]+/g, ' ');
                axesValue = axesValue.replace(/^ */, '');  // 2MASS from IPAC seemed to have a leading space
                var axes = axesValue.split(' ');
    
                var scaleValue = new String( record.get(siaExtentCols.scaleCol) );
                scaleValue = scaleValue.replace(/[\s,]+/g, ' ');
                scaleValue = scaleValue.replace(/^\s*/, '');
                scaleValue = scaleValue.replace(/\s*$/, '');
                
                var scale = scaleValue.split(' ');
                
                if( isNaN(scale[0]) || scale[0]==0 ){
                    return null;
                }
                
                if( scale[1] == null ){
                    scale[1] = scale[0];
                }else{
                    if( isNaN(scale[1]) || scale[1]==0 ){
                        return null;
                    }
                }
                
                var xExt = Math.abs(scale[0]*axes[0]);  // X extent in degrees
                var yExt = Math.abs(scale[1]*axes[1]);  // Y extent in degrees
                
                var ra = new Number(record.get(this.getRaColName()));
                var dec = new Number(record.get(this.getDecColName()));
                var pos = this.getPositionForValues(ra, this.raUnit, dec, this.decUnit);
                if (pos) {
                    var decMax = this.wrapDec(pos.dec + yExt/2);
                    var cosDecMax = Math.cos((decMax * Math.PI) / 180.0);
                    var raDiffMax = xExt / (2 * cosDecMax);
                    
                    
                    var decMin = this.wrapDec(pos.dec - yExt/2);
                    var cosDecMin = Math.cos((decMin * Math.PI) / 180.0);
                    var raDiffMin = xExt / (2 * cosDecMin);
                    
                    // Sanity check, because we won't do well near the poles, so don't bother.
                    if ((raDiffMax < 30) && (raDiffMin < 30)) {
                        var fpString = 'POLYGON ICRS ';
                        fpString += this.wrapRa(ra - raDiffMax) + ' ' + decMax + ' ';  //  Upper left
                        fpString += this.wrapRa(ra + raDiffMax) + ' ' + decMax + ' ';  //  Upper right
                        fpString += this.wrapRa(ra + raDiffMin) + ' ' + decMin + ' ';  //  Lower right
                        fpString += this.wrapRa(ra - raDiffMin) + ' ' + decMin;  //  Lower left
                        
                        siaFpObject = {
                            "footprint": fpString
                        };
                    }
                    
                }
            }
            
            // Cache whatever we got, even if null, to indicate we tried.
            record._siaFpObject = siaFpObject;
        }
        return siaFpObject;
    },
    
    wrapDec: function(dec) {
        var fixed = dec;
        if (dec > 90) {
            fixed = 90 - (dec - 90);
        } else if (dec < -90) {
            fixed = -90 - (dec + 90);
        }
        return fixed;
    },
    
    wrapRa: function(ra) {
        var fixed = ra;
        if (ra > 360) {
            fixed = ra - 360;
        } else if (ra < 0) {
            fixed = ra + 360;
        }
        return fixed;
    },

    getWcsFootprintForRecord: function (record) {
        var wcsFpObject = record._wcsFpObject;
        if (wcsFpObject === undefined) {
            wcsFpObject = null;  // null means we tried.  undefined means we haven't tried yet.
            
            var wcs = record._wcs;
            if (wcs === undefined) {
                wcs = null;  // null means we tried.  undefined means we haven't tried yet.
                var wcsCols = this.getWcsCols();
                if (wcsCols) {
                    wcs = Ext.create('Mvp.util.Wcs', {
                        naxis: record.get(wcsCols.naxisCol),
                        cdMatrix: record.get(wcsCols.cdMatrixCol),
                        coordRefValue: record.get(wcsCols.coordRefValueCol),
                        coordRefPixel: record.get(wcsCols.coordRefPixelCol)
                    });
                    
                    if (wcs.valid) {
                        var fpString = 'POLYGON ICRS ';
                        fpString += wcs.ul[0] + ' ' + wcs.ul[1] + ' ';
                        fpString += wcs.ur[0] + ' ' + wcs.ur[1] + ' ';
                        fpString += wcs.lr[0] + ' ' + wcs.lr[1] + ' ';
                        fpString += wcs.ll[0] + ' ' + wcs.ll[1];
                        wcsFpObject = {
                            "footprint": fpString
                        };
                    }
                    
                }
                record._wcs = wcs;
            }
            // Cache whatever we got, even if null, to indicate we tried.
            record._wcsFpObject = wcsFpObject;
        }
        return wcsFpObject;
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