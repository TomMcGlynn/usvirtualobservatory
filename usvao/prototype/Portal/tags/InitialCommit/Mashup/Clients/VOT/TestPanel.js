
TestPanel = Ext.extend(MainPanelUi, {
    initComponent: function() {
        MainPanel.superclass.initComponent.call(this);
        
        this.defineResolverModel();
        this.on('searchInitiated', this.resolveName);
        },

    defineResolverModel: function() {
        Ext.define('resolvedCoordinate',{
            extend: 'Ext.data.Model',
            fields: [
                {name: 'cacheDate', type: 'string'},
                {name: 'cached',   type: 'boolean'},
                {name: 'canonicalName', type: 'string'},
                {name: 'dec', type: 'float'},
                {name: 'objectType', type: 'string'},
                {name: 'ra', type: 'float'},
                {name: 'radius', type: 'string'},
                {name: 'resolver', type: 'string'},
                {name: 'searchRadius', type: 'string'},
                {name: 'searchString', type: 'string'}
             ]
        });               
    },
    
    createResolverStore: function(xmlDocument) {
        var store = Ext.create('Ext.data.Store', {
            model: 'resolvedCoordinate',
            autoLoad: true,
            data: xmlDocument,
            proxy: {
                type: 'memory',
                reader: {
                    type: 'xml',
                    record: 'resolvedCoordinate'
                }
            }
        });    
        return store;    
    },
    
    resolveName: function(text) {
        var me = this;
        var request = {};
        request.service = 'Mast.Name.Lookup';
        request.format = 'xml';
        request.params = {};
        request.params.input = text;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            params : {request : Ext.encode(request)},
            url: '../../Mashup.asmx/invoke',
            success: function ( result, request ) {
                if (me.resultGrid) {
                    me.remove(me.resultGrid);
                }
                var resolverStore = me.createResolverStore(result.responseXML);
                me.resultGrid = me.createResolverGrid(resolverStore);
                me.add(me.resultGrid);
                
                // If we can get a position out of the results, search the CAOM.
                var coneSearchParams = me.getConeSearchParams(resolverStore.getAt(0));
                if (coneSearchParams) {
                    // Save the cone search parameters for follow-up searches.
                    me.coneSearchParams = Ext.clone(coneSearchParams);
                    
                    // Search the cone.
                    //me.searchCaom(coneSearchParams);
                    me.searchInventory(coneSearchParams);
                } else {
                    alert("Could not resolve <" + text + "> to a position");
                }
            },
            failure: function(result, request) {
                alert("failure: " + result);
                console.log("failure: " + result);
            }
        });
    },
    
    createResolverGrid: function(store) {
        
        // create the Grid
        var grid = Ext.create('Ext.grid.Panel', {
            store: store,
            columns: [
                //{text: 'cacheDate', dataIndex: 'cacheDate'},
                //{text: 'cached', dataIndex: 'cached'},
                {text: 'canonicalName', dataIndex: 'canonicalName'},
                {text: 'ra', dataIndex: 'ra'},
                {text: 'dec', dataIndex: 'dec'},
                {text: 'objectType', dataIndex: 'objectType'},
                {text: 'radius', dataIndex: 'radius'},
                {text: 'resolver', dataIndex: 'resolver'},
                {text: 'searchRadius', dataIndex: 'searchRadius'},
                {text: 'searchString', dataIndex: 'searchString'}
            ],
            stateful: true,
            stateId: 'resolverGridState',
            height: 100,
            width: 800,
            title: 'Resolved positions'
        });
        return grid;
    },
    
    getConeSearchParams: function(resolverRecord) {
        var coneSearchParams;
        if (resolverRecord) {
            var ra = resolverRecord.get('ra');
            var dec = resolverRecord.get('dec');
            var radius = resolverRecord.get('searchRadius');
            if (!radius || (radius <= 0)) {
                radius = 0.2;  // Default radius to 0.2 degrees.
            }
            if (ra && dec) {
                coneSearchParams = {ra: ra, dec: dec, radius: radius, verb: 3};
            }
        }
        return coneSearchParams;
    },

    searchCaom: function(coneSearchParams) {
        var me = this;
        var request = {};
        //request.service = 'Mast.Caom.Cone';
        request.service = 'Caom.Cone.Votable';
        //request.service = 'Hla.Cone.Votable';
        request.format = 'extjs';
        request.params = coneSearchParams;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            params : {request : Ext.encode(request)},
            url: '../../Mashup.asmx/invoke',
            success: function ( result, request ) {
                if (me.caomGrid) {
                    me.remove(me.caomGrid);
                }
                var dataset = Ext.decode(result.responseText);
                me.caomGrid = me.createGenericGrid(dataset.Tables[0], 800, 350, 'MAST CAOM Search Results');
                me.add(me.caomGrid);
            },
            failure: function(result, request) {
                alert("failure: " + result.responseText);
                console.log("failure: " + result.responseText);
            }
        });
    },
    
    searchInventory: function(coneSearchParams) {
        var me = this;
        var request = {};
        request.service = 'Vo.Inventory.Cone';
        request.format = 'extjs';
        request.params = coneSearchParams;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            params : {request : Ext.encode(request)},
            url: '../../Mashup.asmx/invoke',
            success: function ( result, request ) {
                if (me.invGrid) {
                    me.remove(me.invGrid);
                }
                var dataset = Ext.decode(result.responseText);
                me.invGrid = me.createGenericGrid(dataset.Tables[0], 800, 350, 'VO Inventory Search Results');
                me.add(me.invGrid);
                me.invGrid.on('itemclick', me.inventoryClicked, me);
            },
            failure: function(result, request) {
                alert("failure: " + result.responseText);
                console.log("failure: " + result.responseText);
            }
        });
    },
    
    searchGenericCone: function(coneSearchParams, url, gridTitle) {
        var me = this;
        var request = {};
        request.service = 'Vo.Generic.Cone';
        request.format = 'extjs';
        request.params = coneSearchParams;
        request.params.url = url;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            params : {request : Ext.encode(request)},
            url: '../../Mashup.asmx/invoke',
            success: function ( result, request ) {
                if (me.coneGrid) {
                    me.remove(me.coneGrid);
                }
                var dataset = Ext.decode(result.responseText);
                me.coneGrid = me.createGenericGrid(dataset.Tables[0], 800, 350, gridTitle);
                me.add(me.coneGrid);
            },
            failure: function(result, request) {
                alert("failure: " + result.responseText);
                console.log("failure: " + result.responseText);
            }
        });
    },
    
    createGenericGrid: function(extjsDataSet, width, height, title) {
        // create the data store   
        var store = Ext.create('Ext.data.ArrayStore', {
            fields: extjsDataSet.Fields,
            data: extjsDataSet.Rows,
            autoLoad: false
        });
    
        var groupingFeature = Ext.create('Ext.grid.feature.Grouping', {
            groupHeaderTpl: 'Group: {name} ({rows.length}  Item{[values.rows.length > 1 ? "s" : ""]})', //print the number of items in the group
            startCollapsed: true // start all groups collapsed
        });
        
        // create the Grid
        var grid = Ext.create('Ext.grid.Panel', {
            store: store,
            stateful: true,
            stateId: title,
            features: [groupingFeature],
            columns: extjsDataSet.Columns,
            width: width,
            height: height,
            title: title,
            viewConfig: {
                stripeRows: true
            }
        });
        
        return grid;        
    },
    
    inventoryClicked: function( view, record, htmlElement, index, e) {
        var me = this;
        var accessURL = record.get('accessURL');
        var title = record.get('title');
        if (accessURL) {
            if (me.coneSearchParams) {
                me.searchGenericCone(me.coneSearchParams, accessURL, title);
            } else {
                alert("Can't determine cone search parameters for searching " + title);
            }
        } else {
            alert("Cone search unavailable for " + title);
        }
    }

});
  


