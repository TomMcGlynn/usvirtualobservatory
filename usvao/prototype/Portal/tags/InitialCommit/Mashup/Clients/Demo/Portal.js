Ext.require('Mvp.util.SearchBox');
Ext.require('Mvp.util.NameResolverModel');
Ext.require('Mvp.grid.MvpGrid');
Ext.require('Mvp.filters.FacetFilterPanel');
Ext.require('Mvp.util.MashupQuery');

Ext.define('DemoApp.Portal', {
    statics: {
        createAndRun: function(options) {
            var portal = Ext.create('DemoApp.Portal', options);
            portal.run();
        }
    },
    
    centerWidth: 780,
    centerHeight: 440,
    southWidth: 1090,
    southHeight: 190,
    skipCache: false,
    
    constructor: function(config) {
        var me = this;
        
        Ext.apply(me, config);
    },
    
    run: function() {
        var me = this;
        
        // Create the main panel with a border layout.
        me.mainPanel = Ext.create('DemoApp.Layout', {
            renderTo: me.mainDiv
        });
        me.north = me.mainPanel.northPanel;
        me.south = me.mainPanel.southPanel;
        me.east = me.mainPanel.eastPanel;
        me.west = me.mainPanel.westPanel;
        me.center = me.mainPanel.centerPanel;
        
        me.setupNorthPanel();
        
    },
    
    setupNorthPanel: function() {
        var me = this;
        
        me.sbPanel = Ext.create('Ext.panel.Panel', {
            layout: {                        
                type: 'vbox',
                align: 'center'
            },
            width: 600,
            height: 200,
            border: 0
        });
        me.sbPanel.add(new Ext.form.field.Display({value: '<h1>Portal Component Demo</h1>'}));
        
        me.services = Ext.create('Ext.data.Store', {
            fields: ['fn', 'text'],
            data: [
                {fn: 'getDataSetFile', text: 'Load static data set'},
                {fn: 'searchDataScope', text: 'Search DataScope'},
                {fn: 'searchInventory', text: 'Search VO Inventory'},
                {fn: 'searchCaom', text: 'Search MAST CAOM'},
                {fn: 'searchRegistry', text: 'Search VAO Registry (enter keywords)'},
                {fn: 'searchVoTable', text: 'Load VO Table (enter URL)'}
            ]
        });
        
        
        me.actionPanel = Ext.create('Ext.panel.Panel', {
            layout: {                        
                type: 'hbox'
            },
            width: 600,
            height: 25,
            border: 0
        });

        me.actionChooser = Ext.create('Ext.form.ComboBox', {
            fieldLabel: 'Choose Action',
            store: me.services,
            queryMode: 'local',
            displayField: 'text',
            valueField: 'fn'
        });
        me.actionChooser.setValue('searchDataScope');
        me.actionPanel.add(me.actionChooser);
        
        
        me.actionPanel.add(new Ext.form.field.Display({value: '<pre>                </pre>'}));
        
        me.avButton = Ext.create('Ext.Button', {
            text: 'Start AstroView',       
            handler: me.addAstroView,
            scope: me
        });
        me.actionPanel.add(me.avButton);
        
        me.sbPanel.add(me.actionPanel);
        
        me.searchBox = Ext.create('Mvp.util.SearchBox', {
            width: 800
        });
        me.sbPanel.add(me.searchBox);

        me.north.add(me.sbPanel);       
        
        me.searchBox.on('searchInitiated', me.doSearch, me);
    },
     
    doSearch: function(text) {
        var me = this;
        var action = me.actionChooser.getValue();
        var dispatchFunction = me[action];
        if (action == 'searchDataScope' || action == 'searchInventory'
            || action == 'searchCaom' || action == 'astroViewSearch') {
            me.resolveName(text, dispatchFunction);
        } else {
            // Just dispatch on the search text.
            //dispatchFunction(text);
            Ext.callback(dispatchFunction, me, [text]);
        }
     
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // AstroView


    addAstroView: function() {
        var me = this;
        if (me.astroView) {
            me.center.remove(me.astroView);
        }
        me.astroView = me.makeFlashPanel();
        me.center.add(me.astroView);
        me.center.setActiveTab(me.astroView);
    },
    
    makeFlashPanel: function() {
        var me = this;
        
        me.swf = Ext.create('Ext.flash.Component', {
            id: 'AstroView',
	    url: '../AstroView/AstroView.swf'
        });
	var flashPanel = Ext.create('Ext.panel.Panel', {
	    title: "AstroView",
	    layout: 'fit',
	    //width: this.centerWidth,
	    //height: this.centerHeight,
	    //x: 20,
	    //y: 20,
	    resizable: true,
	    items: me.swf
	});
        
        me.swf.on('success', me.flashSuccess, me);
	return flashPanel;
    },
    
    flashSuccess: function() {
        this.swfId = this.swf.swfId;
        if (this.coneSearchParams) {
            Ext.defer(this.publish, 1000, this, [this.coneSearchParams]);
           // this.publish(this.coneSearchParams);
        }
    },
    
    publish: function(coneSearchParams) {
        if (this.swfId) {
            var json = Ext.encode(coneSearchParams);
            Publish('AstroView', json);
        }
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Name Resolver

    resolveName: function(text, dispatchFunction) {
        var me = this;
        var request = {
            service: 'Mast.Name.Lookup',
            format: 'xml',
            params: {
                input: text
            }
        };
        var query = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseName,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me,
            ajaxParams: {dispatchFunction: dispatchFunction}
        });
        query.run();
    },
    
    onResponseName: function(xml, requestOptions, queryScope, complete) {
        var me = this;
        if (me.resultGrid) me.sbPanel.remove(me.resultGrid);
        var resolverStore = Mvp.util.NameResolverModel.createResolverStore(xml);
        me.resultGrid = Mvp.util.NameResolverModel.createResolverGrid(resolverStore);
        me.sbPanel.add(me.resultGrid);
        
        // If we can get a position out of the results, search the CAOM.
        var coneSearchParams = Mvp.util.NameResolverModel.getConeSearchParams(resolverStore.getAt(0));
        if (coneSearchParams) {
            // Save the cone search parameters for follow-up searches.
            me.coneSearchParams = Ext.clone(coneSearchParams);
            
            // Search the cone.
            Ext.callback(requestOptions.dispatchFunction, me, [coneSearchParams]);
            me.publish(coneSearchParams);
        } else {
            alert("Could not resolve <" + text + "> to a position");
        }
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // CAOM

    searchCaom: function(coneSearchParams) {
        var me = this;
        
        // Cancel any outstanding CAOM request.
        if (me.queryCAOM) {
            var cancelledQuery = me.queryCAOM;
            me.queryCAOM = null;
            cancelledQuery.cancel();
        }
        
        var request = {service: 'Caom.Cone.Votable'};
        request.params = coneSearchParams;
        request.params.clearcache = this.skipCache;
        me.queryCAOM = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseCAOM,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me
        });
        me.queryCAOM.run();
    },
    
    onResponseCAOM: function(responseObject, requestOptions, queryScope, complete, updated) {
        if (this.queryCAOM === queryScope) {
            var firstResponse = !queryScope.myHasResponded;
            queryScope.myHasResponded = true;
            
            console.log('onResponseCAOM: firstResponse = ' + firstResponse + ", complete = " + complete + ", updated = " + updated);
            if (updated) {
                this.updateCAOMDisplay();
            }
        } else {
            console.log('onResponseCAOM: response detected on wrong query object.');
        }
    },
    
    updateCAOMDisplay: function() {
        
        var table = this.queryCAOM.markTable();
        if (this.caomGrid) {
            this.center.remove(this.caomGrid);
        }
        this.caomGrid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight, 'MAST CAOM Search Results');
        this.west.removeAll();
        this.center.add(this.caomGrid);
        this.center.setActiveTab(this.caomGrid);
        this.caomGrid.on('itemdblclick', this.caomClicked, this);

    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // VO Table

    searchVoTable: function(url, gridTitle, inSouth) {
        var me = this;
        
        // Cancel any outstanding VOT request.
        if (me.queryVOT) {
            var cancelledQuery = me.queryVOT;
            me.queryVOT = null;
            cancelledQuery.cancel();
        }
        
        var request = {service: 'Vo.Generic.Table'};
        var title = gridTitle || url;
        request.params = {};
        request.params.url = url;
        request.params.clearcache = this.skipCache;
        me.queryVOT = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseVOT,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me,
            ajaxParams: {gridTitle: title, inSouth: inSouth}
        });
        me.queryVOT.run();
    },
    
    onResponseVOT: function(responseObject, requestOptions, queryScope, complete, updated) {
        if (this.queryVOT === queryScope) {
            var firstResponse = !queryScope.myHasResponded;
            queryScope.myHasResponded = true;
            
            console.log('onResponseVOT: firstResponse = ' + firstResponse + ", complete = " + complete + ", updated = " + updated);
            if (updated) {
                this.updateVOTDisplay(requestOptions.gridTitle, requestOptions.inSouth);
            }
        } else {
            console.log('onResponseVOT: response detected on wrong query object.');
        }
    },
    
    updateVOTDisplay: function(gridTitle, inSouth) {
        
        var table = this.queryVOT.markTable();
        if (inSouth) {
            this.votSouthGrid = Mvp.grid.MvpGrid.createGrid(table, this.southWidth, this.southHeight, gridTitle);
            this.south.add(this.votSouthGrid);
            this.south.setActiveTab(this.votSouthGrid);
        } else {
            if (this.votCenterGrid) {
                this.center.remove(this.votCenterGrid);
            }
            this.votCenterGrid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight, gridTitle);
            this.center.add(this.votCenterGrid);
            this.center.setActiveTab(this.votCenterGrid);
        }
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Inventory

    searchInventory: function(coneSearchParams) {
        var me = this;
        
        // Cancel any outstanding VOI request.
        if (me.queryVOI) {
            var cancelledQuery = me.queryVOI;
            me.queryVOI = null;
            cancelledQuery.cancel();
        }
        
        var request = {service: 'Vo.Inventory.Cone'};
        request.params = coneSearchParams;
        request.params.clearcache = this.skipCache;
        me.queryVOI = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseVOI,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me
        });
        me.queryVOI.run();
    },
    
    onResponseVOI: function(responseObject, requestOptions, queryScope, complete, updated) {
        if (this.queryVOI === queryScope) {
            var firstResponse = !queryScope.myHasResponded;
            queryScope.myHasResponded = true;
            
            console.log('onResponseVOI: firstResponse = ' + firstResponse + ", complete = " + complete + ", updated = " + updated);
            if (updated) {
                this.updateVOIDisplay();
            }
        } else {
            console.log('onResponseVOI: response detected on wrong query object.');
        }
    },
    
    updateVOIDisplay: function() {
        
        var table = this.queryVOI.markTable();
        if (this.invGrid) {
            this.center.remove(this.invGrid);
        }
        this.invGrid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight, 'VO Inventory Search Results');
        this.west.removeAll();
        this.center.add(this.invGrid);
        this.center.setActiveTab(this.invGrid);
        this.invGrid.on('itemdblclick', this.inventoryClicked, this);

    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Static file load

    getDataSetFile: function(file) {
        var me = this;
        Ext.Ajax.request({ 

            useDefaultXhrHeader: 'false',
            method: 'GET',
            url: file,
            success: function ( result, request ) {
                var dataset = Ext.decode(result.responseText);
                me.dataSetFileGrid = Mvp.grid.MvpGrid.createGrid(dataset.Tables[0], 770, 500, file);                

                if (me.facetPanel) me.west.remove(me.facetPanel);
                me.facetPanel = Mvp.filters.FacetFilterPanel.createFacetFilterPanel(me.dataSetFileGrid,
                                                                                    me.dataSetFileGrid.getStore(),
                                                                                    'shortName',
                                                                                    'shortName Filters');
                me.west.add(me.facetPanel);

                me.center.add(me.dataSetFileGrid);
                me.dataSetFileGrid.on('itemdblclick', me.inventoryClicked, me);
            },
            failure: function(result, request) {
                alert("failure: " + result.responseText);
                console.log("failure: " + result.responseText);
            }
        });
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // DataScope

    searchDataScope: function(coneSearchParams) {
        var me = this;
        
        // Cancel any outstanding DS request.
        if (me.queryDS) {
            var cancelledQuery = me.queryDS;
            me.queryDS = null;
            cancelledQuery.cancel();
        }
        
        var request = {service: 'Vo.Hesarc.Datascope'};
        request.params = coneSearchParams;
        request.params.clearcache = this.skipCache;
        request.params.skipcache = (this.skipCache) ? 'YES' : 'NO';
        me.queryDS = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseDS,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me
        });
        me.queryDS.run();
    },
    
    onResponseDS: function(responseObject, requestOptions, queryScope, complete, updated) {
        if (this.queryDS === queryScope) {
            var firstResponse = !queryScope.myHasResponded;
            queryScope.myHasResponded = true;
            
            console.log('onResponseDS: firstResponse = ' + firstResponse + ", complete = " + complete + ", updated = " + updated);
            if (updated) {
                this.updateDSDisplay();
            }
        } else {
            console.log('onResponseDS: response detected on wrong query object.');
        }
    },
    
    updateDSDisplay: function() {
        
        var table = this.queryDS.markTable();
        if (this.dsGrid) {
            this.center.remove(this.dsGrid);
        }
        this.dsGrid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight, 'DataScope Search Results');
        
        this.west.removeAll();
        
        this.facetPanel = Mvp.filters.FacetFilterPanel.createFacetFilterPanel(this.dsGrid,
                                                                            this.dsGrid.getStore(),
                                                                            'categories',
                                                                            'categories Filters', '#');
        this.west.add(this.facetPanel);
        
        this.facetPanel = Mvp.filters.FacetFilterPanel.createFacetFilterPanel(this.dsGrid,
                                                                            this.dsGrid.getStore(),
                                                                            'waveband',
                                                                            'waveband Filters', '#');
        this.west.add(this.facetPanel);
        
        this.facetPanel = Mvp.filters.FacetFilterPanel.createFacetFilterPanel(this.dsGrid,
                                                                            this.dsGrid.getStore(),
                                                                            'publisher',
                                                                            'publisher Filters');
        this.west.add(this.facetPanel);

        this.center.add(this.dsGrid);
        this.center.setActiveTab(this.dsGrid);
        this.dsGrid.on('itemdblclick', this.dsClicked, this);                    
        
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone Search
    
    searchGenericCone: function(coneSearchParams, url, gridTitle) {
        var me = this;
        
        // Cancel any outstanding VOT request.
        if (me.queryGenCone) {
            var cancelledQuery = me.queryGenCone;
            me.queryGenCone = null;
            cancelledQuery.cancel();
        }
        
        var request = {service: 'Vo.Generic.Cone'};
        var title = gridTitle || url;
        request.params = coneSearchParams;
        request.params.url = url;
        request.params.clearcache = this.skipCache;
        me.queryGenCone = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            onResponse: me.onResponseGenCone,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me,
            ajaxParams: {gridTitle: title}
        });
        me.queryGenCone.run();
    },
    
    onResponseGenCone: function(responseObject, requestOptions, queryScope, complete, updated) {
        if (this.queryGenCone === queryScope) {
            var firstResponse = !queryScope.myHasResponded;
            queryScope.myHasResponded = true;
            
            console.log('onResponseGenCone: firstResponse = ' + firstResponse + ", complete = " + complete + ", updated = " + updated);
            if (updated) {
                this.updateGenConeDisplay(requestOptions.gridTitle);
            }
        } else {
            console.log('onResponseGenCone: response detected on wrong query object.');
        }
    },
    
    updateGenConeDisplay: function(gridTitle) {
        
        var table = this.queryGenCone.markTable();
            this.coneGrid = Mvp.grid.MvpGrid.createGrid(table, this.southWidth, this.southHeight, gridTitle);
            this.south.add(this.coneGrid);
            this.south.setActiveTab(this.coneGrid);
    },

    searchGenericCone2: function(coneSearchParams, url, gridTitle) {
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
                me.removeGrid('coneGrid');
                var dataset = Ext.decode(result.responseText);
                me.coneGrid = Mvp.grid.MvpGrid.createGrid(dataset.Tables[0], 770, 350, gridTitle);
                me.add(me.coneGrid);
            },
            failure: function(result, request) {
                alert("failure: " + result.responseText);
                console.log("failure: " + result.responseText);
            }
        });
    },
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Drill Down   
        
    caomClicked: function( view, record, htmlElement, index, e) {
        console.log('CAOM entry clicked');
    },

    inventoryClicked: function( view, record, htmlElement, index, e) {
        var me = this;
        var accessURL = record.get('serviceURL');
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
    },

    dsClicked: function( view, record, htmlElement, index, e) {
        var me = this;
        var accessURL = record.get('tableURL');
        var title = record.get('title');
        if (accessURL) {
            me.searchVoTable(accessURL, title, true);
        } else {
            alert("VO Table unavailable for " + title);
        }
    },

    onError: function(responseObject, requestOptions, queryScope) {
        console.log('onError: ');
    },
    
    onFailure: function(response, requestOptions, queryScope) {
        console.log('onFailure: status = ' + response.status + ', error text: ' + response.responseText);
    }
    



})