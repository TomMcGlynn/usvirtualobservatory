
Ext.define('Mvp.grid.MvpGrid', {
    extend: 'Ext.grid.Panel',

    requires: ['Mvp.data.BufferedStore',
               'Mvp.util.TableUtils', 'Mvp.util.Coords', 'Mvp.util.Wcs', 'Ext.ux.RowExpander',
               'Ext.data.Store', 'Mvp.data.Columns'],

    statics: {
        createGrid: function (extjsDataSet, width, height, title, dockedItems, extraColumns, pagesize, niceColumnNames, renderers, autoFacetRules, ignoreValues) {

            // Apply widths from column ExtendedProperties.
            var allColumns = extjsDataSet.Columns;
            var fields = extjsDataSet.Fields;
            var columns = [];
            var sorters = [];
            var hiddenColumns = [];
            var first = true;

            // Add extra columns.
            if (extraColumns) {
                columns = columns.concat(extraColumns);
            }

            // Add visible columns from data table.
            for (c in allColumns) {
                var col = allColumns[c];
                var include = true;

                for (r in renderers) {
                    var rendererField = renderers[r].dataIndex;
                    if (rendererField == col.dataIndex) {
                        col.renderer = renderers[r].renderer;
                    }
                }

                if (col.ExtendedProperties) {
                    // Pull the column config attributes out of the extended properties.
                    var cc = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'cc');

                    // Width
                    if (cc.width) {
                        col.width = cc.width;
                    }

                    // Column Header Text
                    if (cc.text) {
                        col.text = cc.text;
                        niceColumnNames.push({ column: col.dataIndex, niceName: col.text });
                    }

                    var rule = (cc.autoFacetRule != null) ? cc.autoFacetRule : 'default';
                    autoFacetRules.push({ column: col.dataIndex, rule: rule });

                    if (cc.ignoreValue != undefined) ignoreValues.push({ column: col.dataIndex, value: cc.ignoreValue });

                    // Sorter?
                    if (cc.sort) {
                        sorters.push({ property: col.dataIndex, direction: cc.sort });
                    }

                    // Visible - If visible was specified and is false, we mark this column hidden, which
                    // means it won't be shown initially, but will be available for showing in the
                    // Columns menu item that edits column visibility.
                    if (Ext.isDefined(cc.visible) && !cc.visible) {
                        col.hidden = true;
                    }

                    // Hidden - If hidden was specified and is true, it means we never want to show that column to
                    // the user, so it shouldn't even be available for showing in the
                    // Columns menu item that edits column visibility.
                    if (Ext.isDefined(cc.hidden) && cc.hidden) {
                        include = false;
                    }

                    col.datatype = fields[c].type;
                } else {
                    // Put in a placeholder for ExtendedProperties to prevent the need for
                    // null checks later and to make it easier to add properties if appropriate.
                    col.ExtendedProperties = {};
                }

                if (include) {
                    columns.push(col);
                } else {
                    hiddenColumns.push(col);
                }
            }
            
            var data = extjsDataSet.Rows;
            
            var specialColumns = Mvp.data.Columns.identifyColumns(allColumns);

            var columnInfo = {
                fields: fields,
                columns: columns,
                hiddenColumns: hiddenColumns,
                allColumns: allColumns,
                specialColumns: specialColumns,
                autoFacetRules: autoFacetRules
            };
            
            var store = Ext.create('Mvp.data.BufferedStore', {
                fields: fields,
                data: data,
                sorters: sorters,
                pageSize: pagesize,
                buffered: true
            });
            
            // create the Grid
            var grid = Ext.create('Mvp.grid.MvpGrid', {
                store: store,
                title: title,
                dockedItems: dockedItems,
                numberRows: true,
                pagesize: pagesize,
                columnInfo: columnInfo
            });

            return grid;
        }
    },

    constructor: function (config) {
        config = config || {}; // ensure config is defined

        // The data and fields are required for the buffered store, but not the parent grid,
        // so save the value and remove it from the config.
        var data = config.data;
        delete config.data;
        
        this.origFields = config.columnInfo.fields;
        this.origAllColumns = config.columnInfo.allColumns;
        this.hiddenColumns = config.columnInfo.hiddenColumns;
        Ext.apply(this, config.columnInfo.specialColumns);
        
        // Move these from columnInfo to the config for the parent grid.
        config.columns = config.columnInfo.columns;
        config.autoFacetRules = config.columnInfo.autoFacetRules;
        
        delete config.columnInfo;
        
        var pagesize = config.pagesize || 50;
        delete config.pagesize;

        // This need serious refactoring.
        // We need three stores, all of which need to be able to be local or remote,
        // and are interdependent.
        // The 3 stores are: Page, FilteredSorted and Full (now Page == Buffered and Full == Backing)

        // Create the selection model.
        var sm = Ext.create('Ext.selection.RowModel');

        // Required config for the parent grid
        if (config.numberRows) {
            delete config.numberRows;
            config.columns = [{
                xtype: 'rownumberer',
                width: 40,
                sortable: false
            }].concat(config.columns);
        }
        Ext.apply(config, {
            invalidateScrollerOnRefresh: false,
            verticalScroller: {
                xtype: 'paginggridscroller',
                percentageFromEdge: .50
            }
        });


        // Default config (can be overriden by caller)
        Ext.applyIf(config, {
            preventHeader: true,
            loadMask: true,
            viewConfig: {
                trackOver: false
            }
        });

        this.callParent([config]);

        this.on('selectionchange', this.selectionChanged);
    },

    selectionChanged: function (selectionModel, selections, options) {
        var record = selections[0];
        if (record) {
            // Have the selection model remember which record is selected in case we scroll away from it.
            var selModel = this.getSelectionModel();
            selModel.selectedIndex = record.index;

            var title = record.get('title');
            Ext.log('selection changed with a record: ' + title);
        } else {
            Ext.log('selection changed with no record');
        }
    },

    // This is to work around a bug where sometimes the vertical scroller stops responding.  You can drag it up and
    // down, but the callback is never called.  It seems to happen after the scrollbar goes away because it's not
    // needed after a filter is applied, then comes back when the filter is taken away.
    //
    // This override tries to ensure that the callback is always registered when the scrollbar is shown.
    //
    // See the 16 Jun 2011, 2:38 PM comment in:
    // http://www.sencha.com/forum/archive/index.php/t-136263.html?s=cdae2bc4c55d9b9436b67d7a799addee
    //
    showVerticalScroller: function () {
        var me = this;

        me.setHeaderReserveOffset(true);
        if (me.verticalScroller && me.verticalScroller.ownerCt !== me) {
            //ensure scroll listener is attached
            if (me.verticalScroller.scrollEl) {
                me.verticalScroller.mun(me.verticalScroller.scrollEl, 'scroll', me.verticalScroller.onElScroll, me.verticalScroller);
                me.verticalScroller.mon(me.verticalScroller.scrollEl, 'scroll', me.verticalScroller.onElScroll, me.verticalScroller);
            }
            me.addDocked(me.verticalScroller);
            me.addCls(me.verticalScrollerPresentCls);
            me.fireEvent('scrollershow', me.verticalScroller, 'vertical');
        }
    },

    showHorizontalScroller: function () {
        var me = this;

        if (me.verticalScroller) {
            me.verticalScroller.setReservedSpace(Ext.getScrollbarSize().height - 1);
        }
        if (me.horizontalScroller && me.horizontalScroller.ownerCt !== me) {
            //ensure scroll listener is attached
            if (me.horizontalScroller.scrollEl) {
                me.horizontalScroller.mun(me.horizontalScroller.scrollEl, 'scroll', me.horizontalScroller.onElScroll, me.horizontalScroller);
                me.horizontalScroller.mon(me.horizontalScroller.scrollEl, 'scroll', me.horizontalScroller.onElScroll, me.horizontalScroller);
            }
            me.addDocked(me.horizontalScroller);
            me.addCls(me.horizontalScrollerPresentCls);
            me.fireEvent('scrollershow', me.horizontalScroller, 'horizontal');
        }
    },

    showHorizontalScroller: function () {
        var me = this;

        if (me.verticalScroller) {
            me.verticalScroller.setReservedSpace(Ext.getScrollbarSize().height - 1);
        }
        if (me.horizontalScroller && me.horizontalScroller.ownerCt !== me) {
            me.addDocked(me.horizontalScroller);
            me.addCls(me.horizontalScrollerPresentCls);
            me.fireEvent('scrollershow', me.horizontalScroller, 'horizontal');
        }
    },

    updateStatusText: function (filteredRecordCnt) {
        var statusText = this.getDockedItems()[0].getComponent(0).getEl().dom.innerHTML;

        // Need to strip existing filter msg prior to redisplay
        // FRAGILE!  Format must be kept in sync with FacetedGridHelper.computeStatusText()
        var re = /^Displaying <b>\d+<\/b> of /;
        if (re.test(statusText)) {
            statusText = statusText.replace(re, '');
        }

        // Determine original total record count from status string
        re = /\d+/;
        var matchAry = statusText.match(re);
        var originalRecordCnt = matchAry[0];

        // Only prepend text when the count is non-negative and does not include every record
        if (statusText && filteredRecordCnt >= 0 && filteredRecordCnt != originalRecordCnt) {
            statusText = 'Displaying <b>' + filteredRecordCnt + '</b> of ' + statusText;
        }

        Ext.log('Updating status text: ' + statusText);
        this.getDockedItems()[0].getComponent(0).setText(statusText);
    },

    getFsStore: function () {
        return this.fsStore
    },

    getExportTable: function (filtercolumns) {
        var table = {};
        table.name = "Export Table";

        // For now, use the original field and column specifications.
        table.Fields = this.origFields;
        table.Columns = Ext.clone(this.origAllColumns);

        // Loop through current columns so we can mark what's currently hidden for removal on the server side.
        // Note:  this.columns is all the columns the grid knows about, so it's limited to
        // the list of columns available in the grid's column customization.
        // table.Columns contains the complete list of columns, so there's one for
        // every field.  Those with ExtendedProperties['cc.hidden'] == true were never
        // put in the grid, so are omitted from this.columns.
        if (filtercolumns) {
            for (var c in table.Columns) {
                var col = table.Columns[c];
                if (col.ExtendedProperties) {
                    if (col.ExtendedProperties['cc.hidden']) {
                        col.ExtendedProperties['cc.remove'] = true;
                    }
                }
            }
            for (var c in this.columns) {
                var col = this.columns[c];

                // Find which export column has the same text.
                var exportColumn = this.findExportColumn(table.Columns, col.text);
                if (exportColumn) {
                    // In this case, col.hidden just refers to whether it is currently hidden in the grid view.
                    exportColumn.ExtendedProperties['cc.remove'] = col.hidden;
                }
            }
        }

        // Extract the data rows.  If we change from using the above defaults for fields/columns,
        // we have to make sure the data in the rows syncs up with field definitions.

        table.Rows = [];
        // Use the current grid contents, with all filters applied.
        var store = this.getStore();
        var rowCollection = store.getCache();
        var count = rowCollection.getCount();
        for (var i = 0; i < count; i++) {
            var record = rowCollection.getAt(i);
            var fields = record.fields;
            var keys = (fields) ? fields.keys : [];
            var rowData = [];
            for (var k in keys) {
                var fieldName = keys[k];
                var value = record.get(fieldName);
                rowData.push(value);
            }
            table.Rows.push(rowData);
        }

        return table;
    },

    findExportColumn: function (columns, text) {
        var result = null;
        var i = 0;
        for (i = 0; i < columns.length && !result; i++) {
            if (columns[i].text === text) {
                result = columns[i];
            }
        }
        return result;
    },

    getFootprints: function () {
        var columnName = this.stcsFpColName;
        var footprints = [];

        var store = this.getStore();
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

    getPositions: function (column) {
        var positions = [];

        if (this.hasPositions()) {
            // Use the current grid contents, with all filters applied.
            var store = this.getStore();
            var rowCollection = store.getCache();
            var count = rowCollection.getCount();
            for (var i = 0; i < count; i++) {
                var record = rowCollection.getAt(i);

                var ra = record.get(this.raColName);
                var dec = record.get(this.decColName);
                var positionObject = this.getPositionForValues(ra, this.raUnit, dec, this.decUnit);
                if (positionObject) {
                    positions.push(positionObject);
                }
            }
        }
        return positions;
    },

    getPositionForValues: function (ra, raUnit, dec, decUnit) {
        // NOTE:  We may need to use this.raUnit and this.decUnit to convert these values to decimal!

        var positionObject = {
            "ra": ra,
            "dec": dec
        };

        return positionObject;
    },

    hasFootprints: function () {
        var hasFootprints = this.stcsFpColName;

        return hasFootprints;
    },

    hasPositions: function () {
        var hasPositions = (this.raColName && this.decColName);

        return hasPositions;
    }

});

    
