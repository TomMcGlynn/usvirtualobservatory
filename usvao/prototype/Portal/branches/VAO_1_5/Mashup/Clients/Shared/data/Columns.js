
Ext.define('Mvp.data.Columns', {
    requires: ['Mvp.util.Util', 'Mvp.util.Coords', 'Mvp.custom.Generic'],
    statics: {

        /**
        * Identify columns that have special utility.  For now this is RA, Dec and footprint info.
        *
        * Note that this function has side-effects, marking new properties on the columns
        * themselves.
        */
        identifyColumns: function (allColumns) {
            var raCol, decCol;
            var bestRaQuality = 9999, bestDecQuality = 9999;
            var raUnit, decUnit;
            var specialColumns = {};

            var TableUtils = Mvp.util.TableUtils;
            var Coords = Mvp.util.Coords;
            var Util = Mvp.util.Util;
            
            // Look for STC-S footprint columns.  Currently there isn't much agreement on how these should be identified,
            // so we'll look for a variety of things that have been used.
            var stcsTmp = null, stcsTmp2 = null;
            if ((stcsTmp = TableUtils.getColumnByName(allColumns, 's_region')) != null) {
                // CAOM (STScI ObsTAP) results.  Since this is the official ObsTAP column name, others may use this as well.
                // If they use it the same way it won't cause a problem.
                specialColumns.stcsFpColName = stcsTmp.text;
            } else if ((stcsTmp = TableUtils.getColumnByName(allColumns, 'regionSTCS')) != null) {
                // HLA SIA results
                specialColumns.stcsFpColName = stcsTmp.text;
            } else if ((stcsTmp = TableUtils.getColumnNameByUCD(allColumns, 'phys.area;obs.field')) === 'stcs') {
                // Combination reported by Thomas B. Unknown origin.
                specialColumns.stcsFpColName = stcsTmp;
            } else if ((stcsTmp = TableUtils.getColumnNameByXtype(allColumns, 'adql:REGION')) === 'position_bounds') {
                // Seems to come from CADC resources, such as CADC, CADC_HST, CADC_JCMT
                specialColumns.stcsFpColName = stcsTmp;
            } else if ((stcsTmp = TableUtils.getColumnNameByUtype(allColumns, 'Char.SpatialAxis.Coverage.Support.Area')) != null) {
                // Official Utype for STCS footprint in ObsTAP s_region column.  Still don't know what the UCD is supposed to be.
                specialColumns.stcsFpColName = stcsTmp;
            } else if ((stcsTmp = TableUtils.getColumnNameByUtype(allColumns, 'stc:ObservationLocation.AstroCoordArea.Region')) != null) {
                // Recommended by Thomas B.  Aladin supports it, and Spitzer may use it.
                specialColumns.stcsFpColName = stcsTmp;
            }
                
            // Look for SIA extent columns.
            var naxisCol = TableUtils.getColumnNameByUCD(allColumns, /VOX\:Image_Naxis/);
            var scaleCol = TableUtils.getColumnNameByUCD(allColumns, /VOX\:Image_Scale/);
            if (naxisCol && scaleCol) {
                specialColumns.siaExtentCols = { naxisCol: naxisCol, scaleCol: scaleCol };
            }
            
            // Look for WCS fields (uses naxisCol from above).
            var cdMatrixCol = TableUtils.getColumnNameByUCD(allColumns, /VOX\:WCS_CDMatrix/);
            var coordRefValueCol = TableUtils.getColumnNameByUCD(allColumns, /VOX\:WCS_CoordRefValue/);
            var coordRefPixelCol = TableUtils.getColumnNameByUCD(allColumns, /VOX\:WCS_CoordRefPixel/);
            if (naxisCol && cdMatrixCol && coordRefValueCol && coordRefPixelCol) {
                specialColumns.wcsCols = {
                    naxisCol: naxisCol,
                    cdMatrixCol: cdMatrixCol,
                    coordRefValueCol: coordRefValueCol,
                    coordRefPixelCol: coordRefPixelCol
                }
            }
            
            var magInsertPoint = 0,
                fluxInsertPoint = 0,
                errorInsertPoint = 0,
                reorderColumns = [];
            for (var c = 0; c < allColumns.length; c++) {
                var col = allColumns[c];
                var ep = col.ExtendedProperties;
                var cc = Util.extractByPrefix(ep, 'cc');
                if (col.datatype == 'date') {
                    col.getDisplayValue = Mvp.custom.Generic.dateRenderer;  // used by filter container, minmaxslider
                    col.readDisplayValue = Mvp.custom.Generic.dateReader;   // "
                    col.renderer = Mvp.custom.Generic.dateRenderer;         // used by the grid
                }
                if (ep.isMjd || cc.isMjd) {
                    col.getDisplayValue = Mvp.custom.Generic.mjdRenderer;
                    col.readDisplayValue = Mvp.custom.Generic.mjdReader;
                    col.renderer = Mvp.custom.Generic.mjdRenderer;
                }
                if (cc.unixtimeSeconds) {
                    col.getDisplayValue = Mvp.custom.Generic.unixSecondsRenderer;
                    col.readDisplayValue = Mvp.custom.Generic.unixSecondsReader;
                    col.renderer = Mvp.custom.Generic.unixSecondsRenderer;
                }

                // Pull the VO Table attributes out of the extended properties.
                var vot = {};
                if (ep) {
                    vot = Util.extractByPrefix(col.ExtendedProperties, 'vot');
                }
                var ucd = vot.ucd || "";
                var unit = vot.unit || "";
                var reordered = true;
                if (ucd) {
                    var mag = Util.findMagnitudeUcds(ucd),
                        flux = Util.findFluxUcds(ucd),
                        err = Util.findErrorUcds(ucd);
                    if (mag !== null) {
                        reorderColumns.splice(magInsertPoint++, 0, col);
                        fluxInsertPoint++;
                        errorInsertPoint++;
                    } else if (flux !== null) {
                        reorderColumns.splice(fluxInsertPoint++, 0, col);
                        errorInsertPoint++;
                    } else if (err !== null) {
                        reorderColumns.splice(errorInsertPoint++, 0, col);
                    } else {
                        reordered = false;
                    }
                }
                if ((!ucd || !reordered) && cc && !cc.add && !cc.remove) {
                    reorderColumns.push(col);
                }

                // Look for RA.
                var raQuality = TableUtils.raQuality(col.text, ucd);
                if (raQuality < bestRaQuality) {
                    raCol = col;
                    raUnit = unit;
                    bestRaQuality = raQuality;
                }

                // Look for Dec.
                var decQuality = TableUtils.decQuality(col.text, ucd);
                if (decQuality < bestDecQuality) {
                    decCol = col;
                    decUnit = unit;
                    bestDecQuality = decQuality;
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
                    raCol.renderer = Coords.raRenderer;
                }
                if (!decCol.renderer) {
                    decCol.renderer = Coords.decRenderer;
                }

                raCol.getDisplayValue = function (degreesValue) { return Coords.posDisplayValue(degreesValue, 15) };
                decCol.getDisplayValue = function (degreesValue) { return Coords.posDisplayValue(degreesValue) };

                raCol.readDisplayValue = function (sexagesimalValue) { return Coords.parseCoordinate(sexagesimalValue, 15) };
                decCol.readDisplayValue = function (sexagesimalValue) { return Coords.parseCoordinate(sexagesimalValue) };
            }

            return { specialColumns: specialColumns, reorderColumns: reorderColumns };

        },
        
        /**
         * Find if a table has any preview templates, then return them in an object with the sizes
         * keying the values.
         */
        getPreviewTemplates: function(table) {
            var pt = {};
            var votProps = table.ExtendedProperties.vot;
            if (votProps && votProps.LINKs) {
                for (var i = 0; i < votProps.LINKs.length; i++) {
                    link = votProps.LINKs[i];
                    var contentRole = link['content-role'];
                    var href = link['href'];
                    if ((contentRole === 'preview-thumb' ||
                        contentRole === 'preview-small' ||
                        contentRole === 'preview-medium' ||
                        contentRole === 'preview-large' ||
                        contentRole === 'preview-full') &&
                        href
                        ) {
                        
                        // This regular expression matches the ${col} part(s) of the URL with an inner capturing
                        // group of just the "col" part.  The "g" means that it will go for multiple matches.
                        // Since we want both the whole match and the inner capture, we need to loop on regexp.exec()
                        // instead of just doing one match.  One match with a regexp with "g" will return an array
                        // containing only the full matches.
                        var regex = new RegExp("\$\{([^\}]+)\}", "g");
                        
                        var match = null;
                        var replacements = [];
                        var valid = true;
                        while ((match = regex.exec(href)) && valid) {
                            // match[0] is the whole "${col}" and match[1] is just "col".
                            
                            // Maybe we should validate here that match[1] is actually a column!
                            // Use table.Fields[].name
                            var found = false;
                            for (f in table.Fields) {
                                if (table.Fields[f] === match[1]) {
                                    found = true;
                                    break;
                                }
                            }
                            
                            if (found) {
                                replacements.push(match);
                            } else {
                                valid = false;
                            }
                        }
                        
                        pt[contentRole] = {
                            href: href,
                            replacements: replacements
                        };
                    }
                }
            }            
            
            return pt;
        }

    }

});