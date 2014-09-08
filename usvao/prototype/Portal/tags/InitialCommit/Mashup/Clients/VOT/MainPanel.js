
MainPanel = Ext.extend(MainPanelUi, {
    initComponent: function() {
        MainPanel.superclass.initComponent.call(this);
        
        this.on('searchInitiated', this.searchInitiated);
        },
        
    searchInitiated1 : function(text){
        if (this.grid) {
            this.remove(this.grid);
        }
        this.grid = new Ext.form.field.Display({
            fieldLabel:  'Search text',
            value: text
        });
        this.add(this.grid);
    },
    
    searchInitiated: function(text) {
        var me = this;
        var request = {};
        request.service = 'Vo.Generic.Table';
        request.format = 'extjs';
        request.params = {};
        request.params.url = text;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            params : {request : Ext.encode(request)},
            url: '../../Mashup.asmx/invoke',
            success: function ( result, request ) {
                if (me.grid) {
                    me.remove(me.grid);
                }
                var dataset = Ext.decode(result.responseText);
                me.grid = me.createTableGrid(dataset.Tables[0]);
                me.add(me.grid);
            },
            failure: function(result, request) {
                alert("failure: " + result);
                console.log("failure: " + result);
            }
        });
    },
    
    createTableGrid: function(table) {    
        // create the data store   
        var itemsPerPage = 25;   // set the number of items you want per page

        var store = Ext.create('Ext.data.ArrayStore', {
            fields: table.Fields,
            data: table.Rows,
            autoLoad: false,
            pageSize: itemsPerPage
        });
    
        //var store = Ext.create('Ext.data.Store', {
        //    fields: table.Fields,
        //    data: table.Rows,
        //    autoLoad: false,
        //    pageSize: itemsPerPage,
        //    proxy: {
        //        type: 'memory',
        //        reader: {
        //            type: 'json'
        //        }
        //    }
        //});
        //
        // specify segment of data you want to load using params
        //store.load({
        //    params:{
        //        start:0,    
        //        limit: itemsPerPage
        //    }
        //});

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping', {
            groupHeaderTpl: 'Group: {name} ({rows.length}  Item{[values.rows.length > 1 ? "s" : ""]})', //print the number of items in the group
            startCollapsed: true // start all groups collapsed
        });
        
        // create the Grid
        var grid = Ext.create('Ext.grid.Panel', {
            store: store,
            stateful: true,
            stateId: 'stateGrid',
            //features: [groupingFeature],
            columns: table.Columns,
            height: 350,
            width: 800,
            title: 'Galex Grid Table',
            //dockedItems: [{
            //    xtype: 'pagingtoolbar',
            //    store: store,   // same store GridPanel is using
            //    dock: 'bottom',
            //    displayInfo: true
            //}],
          //  renderTo: 'grid-example',
            viewConfig: {
                stripeRows: true
            }
        });
        
        return grid;
    }

});
  


