Ext.define('Mvp.util.Coords', {
    statics: {

        // Validates and parses a string containing a RA/Dec value (including
        // colon-separated or space-separated sexagesimal format).
        // Returns a float if value, undefined if not valid.
        // Set hfactor to 15 for RA field (hh:mm:ss). Assumes that a single float
        // value is always degrees (so it does not use hfactor in that case.)


        parseCoordinate: function(strValue, hfactor) {
            if (!hfactor) hfactor = 1.0;
            if (validateNumeric(strValue)) {
                return parseFloat(strValue);
            }
            var flist = strValue.split(':');
            if (flist.length < 2) {
                flist = strValue.split(' ');
                if (flist.length < 2) {
                    return undefined;
                }
            }
            // require 2 or 3 non-null fields for sexagesimal format
            var nonnull = [];
            for (var i = 0; i < flist.length; i++) {
                var v = trim(flist[i]);
                if (v) {
                    nonnull.push(v);
                }
            }
            flist = nonnull;
            if (flist.length > 3 || flist.length < 2) return undefined;
            i = flist.length - 1;
            if (validateNumeric(flist[i])) {
                var value = parseFloat(flist[i]);
            } else {
                return undefined;
            }
            var sign = 1;
            if (flist[0].charAt(0) == '-') sign = -1;
            flist[0] *= sign;
            for (i = i - 1; i >= 0; i--) {
                if (validateInteger(flist[i])) {
                    value = parseInt(flist[i], 10) + value / 60.0;
                } else {
                    return undefined;
                }
            }
            return sign * value * hfactor;
        },

        // Returns a sexagesimal string for the input RA or Dec value.
        // For RA, specify a scale of 15.
        sexagesimal: function(value, scale, ndigits) {
            if (scale == undefined) scale = 1;
            if (ndigits == undefined) {
                if (scale == 1) {
                    ndigits = 2;
                } else {
                    ndigits = 3;
                }
            }
            value = value / scale;
            if (value < 0) {
                value = Math.abs(value);
                var sign = "-";
            } else if (scale == 1) {
                sign = "+";
            } else {
                sign = "";
            }
            var dd = Math.floor(value);
            value = (value - dd) * 60;
            var mm = Math.floor(value);
            var ss = (value - mm) * 60;
            ss = ss.toFixed(ndigits);
            var v = parseFloat(ss);
            if (v >= 60) {
                v = v - 60;
                ss = v.toFixed(ndigits);
                mm = mm + 1;
                if (mm == 60) {
                    mm = 0;
                    dd = dd + 1;
                }
            }
            if (ss.length == ndigits + 2) {
                ss = "0" + ss;
            }
            mm = mm.toString();
            if (mm.length == 1) mm = "0" + mm;
            dd = dd.toString();
            if (dd.length == 1) dd = "0" + dd;
            return sign + dd + ":" + mm + ":" + ss;
        },
    
        raRenderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
            var coords = Mvp.util.Coords;
            var displayValue = coords.posDisplayValue(value, 15);
            var html = displayValue;
            
            return html;
        },
        
        decRenderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
            var coords = Mvp.util.Coords;
            var displayValue = coords.posDisplayValue(value);
            var html = displayValue;
            
            return html;
        },
        
        DEGREES: 1,
        SEXAGESIMAL: 2,
        
        positionDisplayStyle: 2, //Mvp.util.Coords.SEXAGESIMAL,
        
        posDisplayValue: function(degreesValue, scale, ndigits) {
            var coords = Mvp.util.Coords;
            var displayValue = degreesValue;
            if (coords.positionDisplayStyle == coords.SEXAGESIMAL) {
                displayValue = coords.sexagesimal(degreesValue, scale, ndigits);
            }
            return displayValue;
        }
    }
});


