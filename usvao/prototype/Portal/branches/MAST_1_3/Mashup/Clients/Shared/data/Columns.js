
Ext.require('Mvp.util.Util');
Ext.require('Mvp.util.Coords');
Ext.require('Mvp.custom.Generic');

Ext.define('Mvp.data.Columns', {
    statics: {

        /**
        * Identify columns that have special utility.  For now this is RA, Dec and footprint info.
        *
        * Note that this function has side-effects, marking new properties on the columns
        * themselves.
        */
        identifyColumns: function (allColumns) {
            var raCol, decCol, stcsFpCol, wcsCols = {}, siaExtentCols = null;
            var bestRaQuality = 9999, bestDecQuality = 9999;
            var raUnit, decUnit;
            var specialColumns = {};

            var u = Mvp.util.TableUtils;
                
            // Look for SIA extent columns.
            var naxisCol = Mvp.util.TableUtils.getColumnbyUCD(allColumns, /VOX\:Image_Naxis/);
            var scaleCol = Mvp.util.TableUtils.getColumnbyUCD(allColumns, /VOX\:Image_Scale/);
            if (naxisCol && scaleCol) {
                siaExtentCols = {naxisCol: naxisCol, scaleCol: scaleCol};
            }

            for (c in allColumns) {
                var col = allColumns[c];
                var ep = col.ExtendedProperties;
                if (col.datatype == 'date') {
                    col.getDisplayValue = Mvp.custom.Generic.dateRenderer;
                    col.readDisplayValue = Mvp.custom.Generic.dateReader;
                }

                // Pull the VO Table attributes out of the extended properties.
                var vot = {};
                if (ep) {
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
                } else if (col.text.match('regionSTCS')) {
                    stcsFpCol = col;
                }

                // Look for WCS stuff.
                if (col.text.match('ctype1')) {
                    wcsCols.ctype1 = col.dataIndex;
                } else if (col.text.match('ctype2')) {
                    wcsCols.ctype2 = col.dataIndex;
                } else if (col.text.match('cunit1')) {
                    wcsCols.cunit1 = col.dataIndex;
                } else if (col.text.match('cunit2')) {
                    wcsCols.cunit2 = col.dataIndex;
                } else if (col.text.match('crpix1')) {
                    wcsCols.crpix1 = col.dataIndex;
                } else if (col.text.match('crpix2')) {
                    wcsCols.crpix2 = col.dataIndex;
                } else if (col.text.match('crval1')) {
                    wcsCols.crval1 = col.dataIndex;
                } else if (col.text.match('crval2')) {
                    wcsCols.crval2 = col.dataIndex;
                } else if (col.text.match('cd1_1')) {
                    wcsCols.cd1_1 = col.dataIndex;
                } else if (col.text.match('cd1_2')) {
                    wcsCols.cd1_2 = col.dataIndex;
                } else if (col.text.match('cd2_1')) {
                    wcsCols.cd2_1 = col.dataIndex;
                } else if (col.text.match('cd2_2')) {
                    wcsCols.cd2_2 = col.dataIndex;
                }


            }

            // If we found both an RA and Dec, save those on the grid.
            // (Make more modular later, because it probably does belong on the grid.)
            if (raCol && decCol) {
                specialColumns.raColName = raCol.dataIndex;
                specialColumns.raUnit = raUnit;
                specialColumns.decColName = decCol.dataIndex;
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
                specialColumns.stcsFpColName = stcsFpCol.dataIndex;
            }
            
            // If we found SIA extent columns, save them.
            if (siaExtentCols) {
                specialColumns.siaExtentCols = siaExtentCols;
            }

            // If we have enough WCS params to compute the corners, then use them.
            // Note we really need the image size bounds too.  I forget where they come from.
            if (wcsCols.crpix1 &&
                wcsCols.crpix2 &&
                wcsCols.crval1 &&
                wcsCols.crval2 &&
                wcsCols.cd1_1 &&
                wcsCols.cd1_2 &&
                wcsCols.cd2_1 &&
                wcsCols.cd2_2) {
                specialColumns.wcsCols = wcsCols;
            }

            return specialColumns;

        }

    }

});