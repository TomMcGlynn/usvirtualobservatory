
Ext.define('Mvp.grid.MvpGrid', {
    extend: 'Ext.grid.Panel',
    
    requires: ['Mvp.data.BufferedStore', 'Mvp.grid.MvpPagingScroller'],
    
    statics: {
        createGrid: function(extjsDataSet, width, height, title) {
        
            // create the Grid
            var grid = Ext.create('Mvp.grid.MvpGrid', {
                fields: extjsDataSet.Fields,
                data: extjsDataSet.Rows,
                columns: extjsDataSet.Columns,
                width: width,
                height: height,
                title: title,
                numberRows: true
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
        var fields = config.fields;
        delete config.fields;

        var store = Ext.create('Mvp.data.BufferedStore', {
            fields: fields,
            data: data,
            pageSize: 50
        });
    
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
            store: store,
            disableSelection: true,
            invalidateScrollerOnRefresh: false,
            plugins: [ Ext.create('DemoApp.FilterRow') ],
            verticalScroller: {
                xtype: 'mvppagingscroller',
                activePrefetch: false
            }
            });
        
        // Default config (can be overriden by caller)
        Ext.applyIf(config, {
            width: 800,
            height: 350,
            closable: true,
            loadMask: true,
            viewConfig: {
                trackOver: false
            }
            });
        

        this.callParent([config]);

    }

});

    
