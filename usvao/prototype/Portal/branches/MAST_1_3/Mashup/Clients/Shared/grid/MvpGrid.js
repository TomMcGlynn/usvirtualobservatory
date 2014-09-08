
Ext.define('Mvp.grid.MvpGrid', {
    extend: 'Ext.grid.Panel',

    requires: [
               'Mvp.util.TableUtils', 'Mvp.util.Coords', 'Mvp.util.Wcs', 'Ext.ux.RowExpander',
               'Mvp.data.Columns', 'Mvp.grid.BufferedCheckboxModel'],

    constructor: function (config) {
        config = config || {}; // ensure config is defined

        this.origFields = config.columnInfo.fields;
        this.origAllColumns = config.columnInfo.allColumns;
        this.hiddenColumns = config.columnInfo.hiddenColumns;
        this.context = config.context;
        Ext.apply(this, config.columnInfo.specialColumns);

        // Move these from columnInfo to the config for the parent grid.
        config.columns = config.columnInfo.columns;
        config.autoFacetRules = config.columnInfo.autoFacetRules;

        delete config.columnInfo;

        // Create the selection model.
        var createCheckboxModel = false;
        for (var c in this.origAllColumns) {
            if (this.origAllColumns[c].dataIndex == '_selected_') {
                createCheckboxModel = true;
                break;
            }
        }

        if (createCheckboxModel) {
            this.selModel = Ext.create('Mvp.grid.BufferedCheckboxModel', { context: this.context });
        }

        // Required config for the parent grid
        if (config.numberRows) {
            delete config.numberRows;
            config.columns = [{
                xtype: 'rownumberer',
                width: 40,
                //locked: true,
                sortable: false
            }].concat(config.columns);
        }
        //Ext.apply(config, {
        //    //invalidateScrollerOnRefresh: false
        //    verticalScroller: {
        //        xtype: 'paginggridscroller',
        //        variableRowHeight: true
        //    }
        //});


        // Default config (can be overriden by caller)
        Ext.applyIf(config, {
            preventHeader: true,
            loadMask: true,
            viewConfig: {
                trackOver: false
            }
        });

        this.callParent([config]);

        this.selectedRecords = [];
        if (createCheckboxModel) this.store.on('refresh', this.getSelectionModel().restoreSelection, this.getSelectionModel());
        this.on('select', this.select, this, { select: true });
        this.on('deselect', this.select, this, { select: false });
    },

    select: function (selectionModel, record, index, eOpts) {
        record.set('_selected_', eOpts.select);

        // If we're using the BufferedCheckboxSelection model, it can use this to differentiate
        // selectionchanged events.  When this is set, selections really changed.  When it's not set,
        // it's likely due to the view switching pages.
        this.selModel.markSelectionChanged = true;
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

    getExportTable: function (filtercolumns) {
        var table = {};
        table.name = "Export Table";

        // For now, use the original field and column specifications.
        table.Fields = this.origFields;
        table.Columns = Ext.clone(this.origAllColumns);

        // Remove the histgrams from the column specs.  They are not useful for the export case.
        for (var c in table.Columns) {
            var col = table.Columns[c];
            if (col.ExtendedProperties) {
                delete col.ExtendedProperties.histObj;
                if (col.ExtendedProperties['cc.hidden']) {
                    col.ExtendedProperties['cc.remove'] = true;
                }
            }
        }

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
        var rowCollection = store.getFilteredRecords();
        var count = rowCollection.length;


        for (var i = 0; i < count; i++) {
            var record = rowCollection[i];
            var rowData = [];
            for (var f in table.Fields) {
                var fieldName = table.Fields[f].name;
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
    
    destroy: function() {
        this.callParent(arguments);
        
        // Make sure we remove listeners (at least global ones) for this and any
        // objects this thing created.
        Ext.log('Destroying a grid');
    }

});

    
