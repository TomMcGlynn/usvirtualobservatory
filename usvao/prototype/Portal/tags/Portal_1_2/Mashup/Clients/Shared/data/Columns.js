
Ext.require('Mvp.util.Util');
Ext.require('Mvp.util.Coords');

Ext.define('Mvp.data.Columns', {
    statics: {

        /**
        * Identify columns that have special utility.  For now this is RA, Dec and footprint info.
        *
        * Note that this function has side-effects, marking new properties on the columns
        * themselves.
        */
        identifyColumns: function (allColumns) {
            var raCol, decCol, stcsFpCol, wcsCols = {};
            var bestRaQuality = 9999, bestDecQuality = 9999;
            var raUnit, decUnit;
            var specialColumns = {};
    
            var u = Mvp.util.TableUtils;
    
            for (c in allColumns) {
                var col = allColumns[c];
    
                // Pull the VO Table attributes out of the extended properties.
                var vot = {};
                if (col.ExtendedProperties) {
                    vot = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'vot');
                }
                var ucd = vot.ucd || "";
                var unit = vot.unit || "";
    
                // Look for RA.
                var raQuality = u.raQuality(col.text, ucd);
                if (raQuality < bestRaQuality) {
                    raCol = col;
                    raUnit = unit;
                    bestRaQuality = raQuality;
                }
    
                // Look for Dec.
                var decQuality = u.decQuality(col.text, ucd);
                if (decQuality < bestDecQuality) {
                    decCol = col;
                    decUnit = unit;
                    bestDecQuality = decQuality;
                }
    
                // Look for stcsFpCol.  This is dumb for now
                if (col.text.match('s_region')) {
                    stcsFpCol = col;
                }
    
                // Look for WCS stuff.
                if (col.text.match('ctype1')) {
                    wcsCols.ctype1 = col;
                } else if (col.text.match('ctype2')) {
                    wcsCols.ctype2 = col;
                } else if (col.text.match('cunit1')) {
                    wcsCols.cunit1 = col;
                } else if (col.text.match('cunit2')) {
                    wcsCols.cunit2 = col;
                } else if (col.text.match('crpix1')) {
                    wcsCols.crpix1 = col;
                } else if (col.text.match('crpix2')) {
                    wcsCols.crpix2 = col;
                } else if (col.text.match('crval1')) {
                    wcsCols.crval1 = col;
                } else if (col.text.match('crval2')) {
                    wcsCols.crval2 = col;
                } else if (col.text.match('cd1_1')) {
                    wcsCols.cd1_1 = col;
                } else if (col.text.match('cd1_2')) {
                    wcsCols.cd1_2 = col;
                } else if (col.text.match('cd2_1')) {
                    wcsCols.cd2_1 = col;
                } else if (col.text.match('cd2_2')) {
                    wcsCols.cd2_2 = col;
                }
    
    
            }
    
            // If we found both an RA and Dec, save those on the grid.
            // (Make more modular later, because it probably does belong on the grid.)
            if (raCol && decCol) {
                specialColumns.raColName = raCol.text;
                specialColumns.raUnit = raUnit;
                specialColumns.decColName = decCol.text;
                specialColumns.decUnit = decUnit;
    
                // Attach special renderers to the RA and Dec columns, unless they already had special renderers
                // specified somewhere else.  These renderers are probably only used by the grid view.  For other views,
                // we will put in a different hook.
                if (!raCol.renderer) {
                    raCol.renderer = Mvp.util.Coords.raRenderer;
                }
                if (!decCol.renderer) {
                    decCol.renderer = Mvp.util.Coords.decRenderer;
                }
    
                raCol.getDisplayValue = function (degreesValue) { return Mvp.util.Coords.posDisplayValue(degreesValue, 15) };
                decCol.getDisplayValue = function (degreesValue) { return Mvp.util.Coords.posDisplayValue(degreesValue) };
    
                raCol.readDisplayValue = function (sexagesimalValue) { return Mvp.util.Coords.parseCoordinate(sexagesimalValue, 15) };
                decCol.readDisplayValue = function (sexagesimalValue) { return Mvp.util.Coords.parseCoordinate(sexagesimalValue) };
            }
    
            // If we found an STC-S footprint column, save it on the grid.
            if (stcsFpCol) {
                specialColumns.stcsFpColName = stcsFpCol;
            }
    
            // If we have enough WCS params to compute the corners, then use them.
            // Note we really need the image size bounds too.  I forget where they come from.
            if (Ext.isNumber(wcsCols.crpix1) &&
                Ext.isNumber(wcsCols.crpix2) &&
                Ext.isNumber(wcsCols.crval1) &&
                Ext.isNumber(wcsCols.crval2) &&
                Ext.isNumber(wcsCols.cd1_1) &&
                Ext.isNumber(wcsCols.cd1_2) &&
                Ext.isNumber(wcsCols.cd2_1) &&
                Ext.isNumber(wcsCols.cd2_2)) {
                specialColumns.wcsCols = wcsCols;
            }
            
            return specialColumns;
    
        }
    
    }

});