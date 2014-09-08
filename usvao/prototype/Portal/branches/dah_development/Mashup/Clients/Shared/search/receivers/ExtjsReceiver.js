
// For some reason, this one needs to be here in an external require.
// Probably because of its static contents.


Ext.define('Mvp.search.receivers.ExtjsReceiver', {
    extend: 'Ext.util.Observable',
    requires: [
        'Mvp.data.Columns',
        'Mvp.data.AstroStore',
        'Mvp.util.UcdTrans',
    ],

    constructor: function (config) {
        this.callParent(arguments);

        this.addEvents('storeupdated');
        Ext.apply(this, config);
    },

    onResponse: function (extjsDataSet, requestOptions, queryScope, complete, updated, pagingExpectedRowCount, respObj) {

        // Need to extract the this.query* stuff (except cancel?) from FacetedGridHelper, so that
        // we can build an info object to pass on callback.
        // We need to give the callback target a way to get the latest store and mark
        // the current table, but not build the store here on every update.


        if (respObj.updated) {
            var table = respObj.table;

            var percent = respObj.percent;
            var datascope = true;  // This should really be done better.
            if (percent === undefined || percent === null) {
                // We're paging, so percent is not the DataScope percent.
                percent = respObj.rowCount / respObj.pageInfo.rowsFiltered;  // actually from 0 to 1
                datascope = false;
            }

            if (datascope || !this.columnInfo) {    // datascope responses have to update the server-generated histograms
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
                store: null
            };
            updateObject.createStore = function () {
                var store = me.createStore(dataInfo, me.columnInfo, function (store, records, successful, operation, eOpts) {
                    updateObject.store = this;
                    me.fireEvent('storeupdated', updateObject);
                    store.removeListener('prefetch', eOpts.pfcb);
                });
                return store;
            };
            this.fireEvent('storeupdated', updateObject);

        }
    },

    onError: function () {

    },

    onFailure: function () {

    },

    getDsPercent: function (table) {
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

    getColumnInfo: function (table) {

        var allColumns = table.Columns;
        var fields = table.Fields;
        var columns = [];
        var hiddenColumns = [];
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
                
                
                // Construct a tooltip from the column meta data
                var vot = Mvp.util.Util.extractByPrefix(col.ExtendedProperties, 'vot');
                var tipCount = 0;
                var tip = "<table>";
                for(key in vot){
                    tipCount++;
                    if(key != "description" && key != "arraysize"){
                        if(key == "ucd"){
                            tip += '<tr><td valign=top>' + key + ':</td><td>' + Mvp.util.UcdTrans.translate(vot[key]) + '</td></tr>';
                        }else if(key == "datatype"){
                            var attValue = vot[key];
                            if( vot["arraysize"] ){
                                attValue += "[" + vot["arraysize"] + "]";
                            }
                            tip += '<tr><td>' + key + ':</td><td>' + attValue + '</td></tr>';
                        }else{
                            tip += '<tr><td>' + key + ':</td><td>' + vot[key] + '</td></tr>';
                        }
                    }
                }
                tip += "</table>";                                    
                if(vot.description){
                    tip = vot.description + "<p>" + tip;
                }
                
                if(tipCount > 0){
                    col.tip = tip;
                }
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

        var colObj = Mvp.data.Columns.identifyColumns(allColumns),
            reorderColumns = colObj.reorderColumns,
            specialColumns = colObj.specialColumns;
        var raCol = specialColumns.raColName,
            decCol = specialColumns.decColName;
        var fields = table.Fields;
        for (var i = 0; i < fields.length; i++) {
            var f = fields[i], val;
            if ((f.name == raCol) && (f.type == 'string')) {
                f.type = 'float';
                for (var j in table.Rows) {
                    val = table.Rows[j][i];
                    table.Rows[j][i] = Number(Mvp.util.Coords.parseCoordinate(val, 15));
                }
            }
            if ((f.name == decCol) && (f.type == 'string')) {
                f.type = 'float';
                for (var j in table.Rows) {
                    val = table.Rows[j][i];
                    table.Rows[j][i] = Number(Mvp.util.Coords.parseCoordinate(val));
                }
            }
        }
        
        var previewTemplates = Mvp.data.Columns.getPreviewTemplates(table);
        var columnInfo = {
            fields: fields,
            //columns: reorderColumns,    // VAOPD 579 - uncomment this to have the data columns reordered with magnitudes first, then fluxes, then error columns
            columns: columns,
            hiddenColumns: hiddenColumns,
            allColumns: allColumns,
            specialColumns: specialColumns,
            previewTemplates: previewTemplates
        };

        table.Columns = allColumns;

        return columnInfo;
    },

    getDataInfo: function (table) {
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
        dataInfo.resultType = resultType;

        return dataInfo;
    },

    createStore: function (dataInfo, columnInfo, prefetchCallback) {
        // TBD - refactor this once we figure out the right way to do proxies and buffered stores, etc.
        // In particular, the pagesize is problematic, because that is really specific to the
        // grid, whereas the proxy is what everyone can use.  Maybe we should just supply the proxy here.
        // But all that depends on how filtering, paging and views like grids and charts
        // work with the store/proxy scheme.
        // So for now, just mimic the old behavior, and kludge in the pagesize override (from getDataInfo())

        // Check that clone() is not a deep copy, because that would be slow for large data sets.
        var config = Ext.clone(dataInfo);
        Ext.apply(config, { columnInfo: columnInfo }); //,
        //listeners: {prefetch: {fn: prefetchCallback, options: {single: true}}}});  // Doesn't work.  See store.on() below.
        var store = Ext.create('Mvp.data.AstroStore', config);

        // Add a one-time listener to prefetch to ensure that a storeupdated event is fired on the first data load.
        // The {single: true} option on adding a listener doesn't seem to work, so the listener will remove itself using
        // the pfcb value we pass in here.
        store.on('prefetch', prefetchCallback, store, { pfcb: prefetchCallback });

        return store;
    }
});