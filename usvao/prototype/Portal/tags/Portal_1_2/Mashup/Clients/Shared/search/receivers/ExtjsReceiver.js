
Ext.require('Mvp.search.SearchParams');
Ext.require('Mvp.data.Columns');

Ext.define('Mvp.search.receivers.ExtjsReceiver', {
    extend: 'Ext.util.Observable',
    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.addEvents('storeupdated');
        Ext.apply(this, config);
    },
    
    onResponse: function(extjsDataSet, requestOptions, queryScope, complete, updated, pagingExpectedRowCount, respObj) {
        
        // Need to extract the this.query* stuff (except cancel?) from FacetedGridHelper, so that
        // we can build an info object to pass on callback.
        // We need to give the callback target a way to get the latest store and mark
        // the current table, but not build the store here on every update.
        
        
        if (respObj.updated) {
            var table = respObj.table;
            
            var percent = this.getDsPercent(table);
            var datascope = true;  // This should really be done better.
            if (percent === null) {
                // We're paging, so percent is not the DataScope percent.
                percent = respObj.rowCount / respObj.pageInfo.rowsFiltered;  // actually from 0 to 1
                datascope = false;
            }
            
            if (!this.columnInfo) {
                this.columnInfo = this.getColumnInfo(table);
            }
            
            var dataInfo = this.getDataInfo(table);
            
            var me = this;
            var updateObject = {
                pageInfo: respObj.pageInfo,
                complete: respObj.complete,
                cancelled: respObj.cancelled,
                percent: percent,
                datascope: datascope,
                rowCount: respObj.rowCount,
                dataInfo: dataInfo,
                columnInfo: this.columnInfo,
                store: null,
                createStore: function() {
                    var store = me.createStore(dataInfo); //Ext.callback(me.createStore, me, [dataInfo]);
                    return store;
                }
            };
            this.fireEvent('storeupdated', updateObject);
            
        }
    },
    
    onError: function() {
        
    },
        
    onFailure: function() {
        
    },
    
    getDsPercent: function(table) {
        var votProps = table.ExtendedProperties.vot;
        var percent = null;
        if (votProps) {
            for (var i = 0; i < votProps.PARAMs.length; i++) {
                param = votProps.PARAMs[i];
                if (param.name && param.name == "percentComplete") {
                    percent = param.value;
                }
            }
        }
        return percent;
    },
    
    getColumnInfo: function(table) {
        
        var allColumns = table.Columns;
        var fields = table.Fields;
        var columns = [];
        var hiddenColumns = [];
        var niceColumnNames = [];
        var autoFacetRules = [];
        var ignoreValues = [];
        
        // Add visible columns from data table.
        for (c in allColumns) {
            var col = allColumns[c];
            var include = true;

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
        
        var specialColumns = Mvp.data.Columns.identifyColumns(allColumns);

        var columnInfo = {
            fields: fields,
            columns: columns,
            hiddenColumns: hiddenColumns,
            allColumns: allColumns,
            specialColumns: specialColumns,
            autoFacetRules: autoFacetRules,
            niceColumnNames: niceColumnNames,
            ignoreValues: ignoreValues
        };
        
        return columnInfo;
    },
    
    getDataInfo: function(table) {
        var allColumns = table.Columns;
        var fields = table.Fields;
        var rows = table.Rows;
        var sorters = [];

        // Find all sorters.
        for (c in allColumns) {
            var col = allColumns[c];

            if (col.ExtendedProperties) {
                // Pull the column config attributes out of the extended properties.
                var cc = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'cc');

                // Sorter?
                if (cc.sort) {
                    sorters.push({ property: col.dataIndex, direction: cc.sort });
                }
            }
        }

        var dataInfo = {
            fields: fields,
            initialData: rows,
            sorters: sorters
        };
        
        // TBD - put this in a better place
        var sp = Mvp.search.SearchParams;
        var resultType = sp.resultTypes[this.searchParams.result.type];
        var pageSize = resultType.storePageSize;
        if (pageSize) {
            dataInfo.pageSize = pageSize;
        }
        
        return dataInfo;
    },
    
    createStore: function(dataInfo) {
        // TBD - refactor this once we figure out the right way to do proxies and buffered stores, etc.
        // In particular, the pagesize is problematic, because that is really specific to the
        // grid, whereas the proxy is what everyone can use.  Maybe we should just supply the proxy here.
        // But all that depends on how filtering, paging and views like grids and charts
        // work with the store/proxy scheme.
        // So for now, just mimic the old behavior, and kludge in the pagesize override (from getDataInfo())
        
        var store = Ext.create('Mvp.data.BufferedStore', dataInfo);
        
        return store;
    }
})