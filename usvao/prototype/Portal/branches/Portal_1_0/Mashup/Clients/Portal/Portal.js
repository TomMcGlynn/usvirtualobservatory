Ext.require('Mvp.util.SearchBox');
Ext.require('Mvp.util.NameResolverModel');
Ext.require('Mvp.grid.MvpGrid');
Ext.require('Mvp.grid.FacetedGridHelper');
Ext.require('Mvp.filters.FacetFilterPanel');
Ext.require('Mvp.util.MashupQuery');
Ext.require('Mvp.util.Util');
Ext.require('DemoApp.Layout');
Ext.require('DemoApp.Viewport');
Ext.require('DemoApp.DetailsPanelDS');
Ext.require('DemoApp.DetailsPanelInv');
Ext.require('DemoApp.DetailsPanelInv2');
Ext.require('DemoApp.DetailsPanelGeneric');


Ext.define('DemoApp.Portal', {
    statics: {
	// URLS
	voInventorySubsetBaseURL: 'https://osiris.ipac.caltech.edu/cgi-bin/VAOLink/nph-VAOlink?action=subset',
	tourHelpURL: 'http://wiki.usvao.org/twiki/pub/Docs/DataDiscoveryToolTour/Data_Discovery_Tool_Tour.html',
	choicesHelpURL: 'http://wiki.usvao.org/twiki/pub/Docs/DataDiscoveryToolTour/Data_Discovery_Tool_Tour.html#dropdownmenu',
	moreExamplesHelpURL: 'http://wiki.usvao.org/twiki/pub/Docs/DataDiscoveryTool/Data_Discovery_Tool_Example_Query.html',
	usvaoHomePage: 'http://www.usvao.org/',
	

        createAndRun: function (options) {
            var portal = Ext.create('DemoApp.Portal', options);
            portal.run();
        },
	
	injectSearchText: function(el) {
	    var me = DemoApp.Portal;  // A static reference to this class.
	    var value = el.innerHTML;
	    if (value) {
		Ext.log('Injecting <' + value + '> into search box.');
		me.searchBox.setValue(value);
	    } else {
		Ext.log('Unable to find value to inject into search box.');
	    }
	},
    
	launchNewPage: function launchDataDiscTool(url) {
	    window.open(url,'_blank');
	}
    },

    // SearchBox panel and component dimensions
    sbPanelWidth: 700,
    fieldGapWidth: 10,
    actionChooserWidth: 325,
    exampleWidth: 350,

    // Used only by FacetedGridHelper
    centerWidth: 780,
    centerHeight: 440,
    southWidth: 1090,
    southHeight: 190,

    skipCache: false,

    constructor: function (config) {
        var me = this;

        Ext.apply(me, config);
    },

    run: function () {
        var me = this;

        // Create the main panel with a border layout.
	me.mainPanel =  Ext.create('DemoApp.Layout', {
	    //renderTo: Ext.getBody()
	    region: 'center'     // center region is required, no width/height specified
	    //border: false
	});
	
//    var detailsPanel = {
//        id: 'details-panel',
//        title: 'Details',
//        region: 'center',
//        bodyStyle: 'padding-bottom:15px;background:#eee;',
//        autoScroll: true,
//        html: '<p class="details-info">When you select a layout from the tree, additional details will display here.</p>'
//    };
//    
	// Create the container Viewport.
        me.viewport = Ext.create('DemoApp.Viewport', {
	    renderTo: Ext.getBody(),
	    mainPanel: me.mainPanel
        });
	
        me.north = me.mainPanel.northPanel;
        me.south = me.mainPanel.southPanel;
        me.east = me.mainPanel.eastPanel;
        me.west = me.mainPanel.westPanel;
        me.center = me.mainPanel.centerPanel;
        me.center.on('tabchange', this.centerTabChange, this);

        // Aliases for FacetedGridHelper
        me.gridContainer = me.center;
        me.facetContainer = me.west;

        me.setupNorthPanel();
    },

    centerTabChange: function (tabPanel, newCard, oldCard) {
        if (Ext.isFunction(newCard.tabSelected)) {
            newCard.tabSelected();
        }
    },

    setupNorthPanel: function () {
        var me = this;

        var searchBoxWidth = me.sbPanelWidth - me.fieldGapWidth - me.actionChooserWidth;
	
	var sep1 = Ext.create('Ext.Component', {width: 20});
	//var logo = Ext.create('Ext.button.Button', {
	//    width: 100,
	//    tooltip: 'Home',
	//    icon: 'data/images/VAO_logo_100.png',
	//    href: DemoApp.Portal.usvaoHomePage
	//    });
	var logo = Ext.create('Ext.Component', {width: 100,
			      html: Mvp.util.Util.createImageLink(DemoApp.Portal.usvaoHomePage, "data/images/VAO_logo_100.png", "Home", 100, 50)
			      //html: '<a href="' + DemoApp.Portal.usvaoHomePage + '" target="_blank" title="Home"><img src="data/images/VAO_logo_100.png" width="100" height="50" alt="vao logo" /></a>'
			      //autoEl: {tag: 'img',
			      //src: 'data/images/VAO_logo_100.png',
			      //href: 'http://www.usvao.org/',
			      //title: 'Home'
			      //}
			      });
	var sep2 = Ext.create('Ext.Component', {width: 50});
	me.top = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox',
		align: 'middle'
            },
            border: 0
        });

	

        me.sbPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'vbox',
                align: 'left'
            },
            width: me.sbPanelWidth,
            height: 72,  // Height set by Layout.js is 76
            border: 0
        });
	me.top.add(sep1);
	me.top.add(logo);
	me.top.add(sep2);
	me.top.add(me.sbPanel);
	

        me.sbPanel.add(new Ext.form.field.Display({
        	height: 8,
            value: '&nbsp;'
        }));

	    var serviceData = [{
                fn: 'searchInventory2', hint: 'Enter object name or RA and Dec',
                text: 'Quick Search (fast for selected collections)', resolve: true
            }, {
                fn: 'searchDataScope', hint: 'Enter object name or RA and Dec',
                text: 'Full Search (all known catalog and image collections)', resolve: true
	        }
	    ];

	    if (isDevelopment) {
	        serviceData = serviceData.concat([{
                fn: 'searchInventory', hint: 'Enter object name or RA and Dec',
                text: 'Search VO Inventory Classic', resolve: true
            }, {
                fn: 'searchCaomVoTable', hint: 'Enter object name or RA and Dec',
                text: 'Search MAST CAOM via VOTable', resolve: true
            }, {
                fn: 'searchCaomDb', hint: 'Enter object name or RA and Dec',
                text: 'Search MAST CAOM via DB', resolve: true
            }, {
                fn: 'searchRegistry', hint: 'Enter keyword(s)',
                text: 'Search VAO Registry', resolve: false
            }, {
                fn: 'loadWholeRegistry', hint: 'Enter anything',
                text: 'Load Whole Registry', resolve: false
            }, {
                fn: 'getDataSetFile', hint: 'Enter filename (path relative to this html file)',
                text: 'Get File', resolve: false
            }, {
                fn: 'searchVoTable', hint: 'Enter the URL of a VO Table',
                text: 'Load VO Table', resolve: false
            }]);
	    }

	    me.services = Ext.create('Ext.data.Store', {
            fields: ['fn', 'hint', 'text', 'resolve'],
            data: serviceData
        });

        me.actionPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox'
            },
            width: me.sbPanelWidth,
            height: 25,
            border: 0
        });

        me.searchBox = Ext.create('Mvp.util.SearchBox', {
            width: searchBoxWidth
        });
	// Register this search box with the static part of the class so that it's easy to inject the search text
	// from the in-line help.
	DemoApp.Portal.searchBox = me.searchBox;

        me.actionChooser = Ext.create('Ext.form.ComboBox', {
            store: me.services,
            queryMode: 'local',
            forceSelection: true,
            editable: false,
            displayField: 'text',
            width: me.actionChooserWidth,
            valueField: 'fn'
        });

        me.actionChooser.on('change', me.actionChanged, me);
        me.actionChooser.setValue('searchInventory2');
        me.actionPanel.add(me.actionChooser);

        me.actionPanel.add(new Ext.form.field.Display({
        	width: me.fieldGapWidth,
            value: '&nbsp;'
        }));

        me.avButton = Ext.create('Ext.Button', {
            text: 'Start AstroView',
            handler: me.addAstroView,
            scope: me
        });
        // disable astroview for now.  me.actionPanel.add(me.avButton);

        me.sbPanel.add(me.actionPanel);

        //me.sbPanel.add(me.searchBox);
        me.actionPanel.add(me.searchBox);
	
	    // Add the placeholder for the resolver summary text.
	    me.resolverSummaryPanel = new Ext.form.field.Display({
            fieldLabel: 'Object:',
            hidden: true,
            hideMode: 'visibility',
            labelAlign: 'right',
            labelSeparator: '&nbsp;',
            labelStyle: 'font-weight:bold;font-style:italic',
            labelWidth: me.actionChooserWidth,
            width: me.sbPanelWidth,
            value: ''
        });
        //me.sbPanel.add(me.resolverSummaryPanel);
	
	// Add search examples.
	me.examplesPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox'
            },
            width: me.sbPanelWidth,
            height: 20,
            border: 0
        });
	
	//me.examplesPanel.add(Ext.create(Ext.panel.Panel), {
	//    width: me.actionChooserWidth + 10,
	//    border: 0
	//});
	
	me.examplesPanel.add(Ext.create('Ext.form.field.Display'), {
	    width: me.actionChooserWidth + 10,
	    border: 0,
	    //html: '<a target="_blank" href="' + DemoApp.Portal.choicesHelpURL + '">About Search Options</a>' + ' | ' +
	    html: Mvp.util.Util.createLink(DemoApp.Portal.choicesHelpURL, 'About Search Options') + ' | ' +
		Mvp.util.Util.createLink(DemoApp.Portal.tourHelpURL, 'Take the Tour')
	});
	var ex1 = Ext.create('Ext.form.field.Display', {
	    width: me.exampleWidth,
	    html:'Examples: ' +
		'<a href="javascript: void(0)" onclick="DemoApp.Portal.injectSearchText(this)">M101</a>, ' + 
		'<a href="javascript: void(0)" onclick="DemoApp.Portal.injectSearchText(this)">14 03 12.6 +54 20 56.7 r=0.2d</a>, ' + 
		Mvp.util.Util.createLink(DemoApp.Portal.moreExamplesHelpURL, 'more...')
	});
	me.examplesPanel.add(ex1);
        me.sbPanel.add(me.examplesPanel);

	
        //me.north.add(me.sbPanel);
        me.north.add(me.top);

        me.searchBox.on('searchInitiated', me.doSearch, me);
        me.searchBox.on('searchReset', me.doSearchReset, me);
    },
	
    test: function() {
	var text = '{top: "topval", "vot.a": "avot", "vot.b": "bvot","cc.a": "aval", "cc.b": "bval", "cc.c": "cval", "ccd": "dval"}';
	var dobj = Ext.decode(text);
	
	var vot = Mvp.util.Util.extractByPrefix(dobj, 'vot');
	console.log('vot.a = ' + vot.a);
	console.log('vot.b = ' + vot.b);
	
	var cc = Mvp.util.Util.extractByPrefix(dobj, 'cc');
	console.log('cc.a = ' + cc.a);
	console.log('cc.b = ' + cc.b);
	console.log('cc.c = ' + cc.c);
	console.log('cc.d = ' + cc.d);
    },
	
    doSearch: function (text, actionOverride, extraArgs) {
        var me = this;
        var action = me.actionChooser.getValue();
        Ext.log('Search invoked for: ' + text);

        if (Ext.isString(actionOverride)) {
            action = actionOverride;
        }

        var dispatchFunction = me[action];
        if (action == 'searchDataScope' || action == 'searchInventory' || action == 'searchInventory2' || action == 'searchCaomVoTable' ||
            action == 'searchCaomDb' || action == 'astroViewSearch' || action == 'searchGenericCone' || action == 'searchGenericSiap') {
            me.resolveName(text, dispatchFunction, extraArgs);
        } else {
            // Just dispatch on the search text.
            Ext.callback(dispatchFunction, me, [text]);
        }
    },

    doSearchReset: function() {
    	var me = this;

        // Hide the resolver summary and its label, since there is now no search value 
        if (me.resolverSummaryPanel) {
            me.resolverSummaryPanel.hide();
            me.resolverSummaryPanel.labelEl.hide();
        }
    },

    actionChanged: function (field, newValue, oldValue, options) {
        var me = this;
        var action = me.actionChooser.getValue();
        var idx = me.services.findExact('fn', action);
        var record = idx !== -1 ? me.services.getAt(idx) : false;
	
        if (record) {
            var hint = record.get('hint');
            var resolve = record.get('resolve');

            var currentSearchText = me.searchBox.getValue();
            var currentHint = me.searchBox.emptyText;
            var hintChanged = (currentHint !== hint);

            // Set the new empty text value.
            me.searchBox.emptyText = hint;
	    
            // If the search box was empty, or if there's a new hint, force it to display the new hint.
            if ((currentSearchText === '') || hintChanged) {
                me.searchBox.reset();

                // Hide the 'X' button since there is no longer any text
                if (me.searchBox.triggerEl) {
                    me.searchBox.triggerEl.item(0).hide();
                }

                // Hide the resolver summary if new action does not use it
                if (me.resolverSummaryPanel) {
                    if (resolve) {
                        me.resolverSummaryPanel.show();
                    } else {
                        me.resolverSummaryPanel.hide();
                        me.resolverSummaryPanel.setValue('');
                    }

                    // Have to hide the label separately since we don't want it displayed when there is no search term
                    if (me.resolverSummaryPanel.labelEl && me.resolverSummaryPanel.getValue() === '') {
                        me.resolverSummaryPanel.labelEl.hide();
                    } else if (me.resolverSummaryPanel.labelEl) {
                    	me.resolverSummaryPanel.labelEl.show();
                    }
                }
            }
        }
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // AstroView
    addAstroView: function () {
        var me = this;
        if (me.astroView) {
            me.center.remove(me.astroView);
        }
        me.astroView = me.makeFlashPanel();
        me.center.add(me.astroView);
        me.center.setActiveTab(me.astroView);
    },

    makeFlashPanel: function () {
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

    flashSuccess: function () {
        this.swfId = this.swf.swfId;
        if (this.coneSearchParams) {
            Ext.defer(this.publish, 1000, this, [this.coneSearchParams]);
            // this.publish(this.coneSearchParams);
        }
    },

    publish: function (coneSearchParams) {
        if (this.swfId) {
            var json = Ext.encode(coneSearchParams);
            Publish('AstroView', json);
        }
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Name Resolver
    resolveName: function (text, dispatchFunction, extraArgs) {
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
            ajaxParams: {
                dispatchFunction: dispatchFunction,
		        extraArgs: extraArgs
            }
        });
        query.run();
    },

    onResponseName: function (xml, requestOptions, queryScope, complete) {
        var me = this;
        var resolverStore = Mvp.util.NameResolverModel.createResolverStore(xml);

        var extra = requestOptions.extraArgs;
        var summaryDisplay = me.resolverSummaryPanel;

        if (Ext.isArray(extra)) {
            // This is a crummy way to avoid this side effect (refilling the name resolver grid) when we're
            // doing the search from the details panel.  So find their display field in the extra args and use
            // it instead.
            summaryDisplay = extra.summaryDisplay;
            delete extra.summaryDisplay;
        }

        if (summaryDisplay) {
            var s = Mvp.util.NameResolverModel.getResolverSummaryString(resolverStore.getAt(0));
            //summaryDisplay.labelEl.show();
            summaryDisplay.setValue(s);
            //summaryDisplay.setVisible(true);
        }

        // If we can get a position out of the results, search the CAOM.
        var coneSearchParams = Mvp.util.NameResolverModel.getConeSearchParams(resolverStore.getAt(0));
        if (coneSearchParams) {
            // Save the cone search parameters for follow-up searches.
            me.coneSearchParams = Ext.clone(coneSearchParams);
	    
            // If arguments were given, use them.  Otherwise, use the default coneSearchParams and searchText.
            var args = [coneSearchParams];
            if (Ext.isArray(extra)) {
                args = args.concat(extra);
            } else {
                // Add the default argument, which is just the search text.
                args.push(queryScope.request.params.input);
            }

            // Search the cone.
            Ext.callback(requestOptions.dispatchFunction, me, args);
            me.publish(coneSearchParams);
        } else {
            alert("Could not resolve <" + queryScope.request.params.input + "> to a position");
        }
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // CAOM VO Table
    searchCaomVoTable: function (coneSearchParams, searchText) {

        var title = 'CAOM VOT: ' + searchText;
        var request = {
            service: 'Caom.Cone.Votable',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'obs_collection'
        }, {
            column: 'instrument'
        }, {
            column: 'target_name'
        }, {
            column: 'proposal_pi'
        }, {
            column: 'dataproduct_type'
        }];

	var options = {
	    title: title,
	    searchText: searchText,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // CAOM DB
    searchCaomDb: function (coneSearchParams, searchText) {

        var title = 'CAOM DB: ' + searchText;
        var request = {
            service: 'Mast.Caom.Cone',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'obs_collection'
        }, {
            column: 'instrument'
        }, {
            column: 'target_name'
        }, {
            column: 'proposal_pi'
        }, {
            column: 'dataproduct_type'
        }];

	var options = {
	    title: title,
	    searchText: searchText,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Table
    searchVoTable: function (url, gridTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Table',
	    clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
	request.params.input = title;
        var facetConfigs = [];

	var options = {
	    title: title,
	    searchText: url,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Inventory Classic
    searchInventory: function (coneSearchParams, searchText) {

        var title = 'VO Inventory: ' + searchText;
        var request = {
            service: 'Vo.Inventory.Cone',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'resourceType'
        }, {
            column: 'datatype'
        }, {
            column: 'archive'
        }, {
            column: 'set'
        }];

	var options = {
	    title: title,
	    searchText: searchText,
	    coneSearchParams: coneSearchParams,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.invRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
     },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Inventory - New Service
    searchInventory2: function (coneSearchParams, searchText, gridTitle) {

        var title = gridTitle || 'Quick Search: ' + searchText;
	var onClick = this.inv2RecordSelected;
        var request = {
            service: 'Vo.Inventory2.Cone',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'datatype'
        }, {
            column: 'archive'
        }, {
            column: 'set'
        }];

	if (coneSearchParams.id) {
	    // This is a drill down request for one resource, so the onClick callback should be the one for generic table records,
	    // and the request service should be the drill down one.
	    onClick = this.genericRecordSelected;
	    request.service = 'Vo.Inventory2.DrillDown';
	    facetConfigs = [];
	}
	
	var options = {
	    title: title,
	    searchText: searchText,
	    coneSearchParams: coneSearchParams,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: onClick,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
     },

    //////////////////////////////////////////////////////////////////////////////////////
    // DataScope
    searchDataScope: function (coneSearchParams, searchText) {

        var title = 'Full Search: ' + searchText;
        var request = {
            service: 'Vo.Hesarc.Datascope',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.skipcache = (this.skipCache) ? 'YES' : 'NO';
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'categories',
            separator: '#',
	    include: ['Catalog', 'Images']
        }, {
            column: 'waveband',
            separator: '#'
        }, {
            column: 'publisher'
        }];
	
	var options = {
	    title: title,
	    searchText: searchText,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.dsRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Search registry
    searchRegistry: function (searchText) {

        var title = 'VO Yellow Pages: ' + searchText;
        var request = {
            service: 'Vo.Registry.VOTKeyword',
	    clearcache: this.skipCache
        };
        request.params = {};
        request.params.searchtext = searchText;
	request.params.input = searchText;

        var facetConfigs = [{
            column: 'categories',
            separator: '#',
	    include: ['Catalog', 'Images']
        }, {
            column: 'waveband',
            separator: '#'
        }, {
            column: 'publisher'
        }];

	var options = {
	    title: title,
	    searchText: searchText,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.dsRecordSelected,
	    onDblClick: null
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Static file load
    getDataSetFile: function (file) {

        var title = file;
        var url = file;

        var facetConfigs = [];

	var options = {
	    title: title,
	    searchText: file,
	    request: url,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Whole registry
    loadWholeRegistry: function (searchText) {

        var title = 'Whole Registry ';
        var url = 'data/wholeregistry-trimmed-extjs.json';

        var facetConfigs = [{
            column: 'categories',
            separator: '#'
        }, {
            column: 'waveband',
            separator: '#'
        }, {
            column: 'publisher'
        }];

	var options = {
	    title: title,
	    searchText: url,
	    request: url,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.dsRecordSelected,
	    onDblClick: null
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone Search
    searchGenericCone: function (coneSearchParams, url, gridTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Cone',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.url = url;
	request.params.input = title;
        var facetConfigs = [];

	var options = {
	    title: title,
	    searchText: url,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone SIAP
    searchGenericSiap: function (coneSearchParams, url, gridTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Siap',
	    clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.url = url;
	request.params.input = title;
        var facetConfigs = [];

	var options = {
	    title: title,
	    searchText: url,
	    request: request,
	    facetConfigs: facetConfigs,
	    app: this,
	    onClick: this.genericRecordSelected,
	    onDblClick: null,
	    tooltip: this.resolverSummaryPanel.getValue()
	};
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Drill Down   
    caomClicked: function (view, record, htmlElement, index, e) {
        Ext.log('CAOM entry clicked');
    },

    registryClicked: function (view, record, htmlElement, index, e) {
        Ext.log('Registry entry clicked');
    },

    inventoryClicked: function (view, record, htmlElement, index, e) {
        var me = this;
        var accessURL = record.get('serviceURL');
        var title = record.get('description');
        if (accessURL) {
	    // TabularSkyService records don't seem to have a needed '&' at the end of their url.
	    if (!accessURL.match('.*&$')) {
		accessURL += '&';
	    }
	    
            if (me.coneSearchParams) {
                me.searchGenericCone(me.coneSearchParams, accessURL, title);
            } else {
                alert("Can't determine cone search parameters for searching " + title);
            }
        } else {
            alert("Cone search unavailable for " + title);
        }
    },

    dsClicked: function (view, record, htmlElement, index, e) {
        var me = this;
        var accessURL = record.get('tableURL');
        var title = record.get('title');
        if (accessURL) {
            me.searchVoTable(accessURL, title);
        } else {
            alert("VO Table unavailable for " + title);
        }
    },

    dsRecordSelected: function (view, record, htmlElement, index, e) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = DemoApp.DetailsPanelDS.create(record, searchText, this);
        this.south.removeAll();
        this.south.add(p);
    },

    invRecordSelected: function (view, record, htmlElement, index, e) {
        var searchText = (view.panel) ? view.panel.searchText : null;
	var coneSearchParams = (view.panel) ? view.panel.coneSearchParams : null;
        var p = DemoApp.DetailsPanelInv.create(record, searchText, coneSearchParams, this);
        this.south.removeAll();
        this.south.add(p);
    },

    inv2RecordSelected: function (view, record, htmlElement, index, e) {
        var searchText = (view.panel) ? view.panel.searchText : null;
	var coneSearchParams = (view.panel) ? view.panel.coneSearchParams : null;
        var p = DemoApp.DetailsPanelInv2.create(record, searchText, coneSearchParams, this);
        this.south.removeAll();
        this.south.add(p);
    },

    genericRecordSelected: function (view, record, htmlElement, index, e) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = DemoApp.DetailsPanelGeneric.create(record, searchText, this);
        this.south.removeAll();
        this.south.add(p);
    },

    onError: function (responseObject, requestOptions, queryScope) {
        Ext.log('onError: ');
    },

    onFailure: function (response, requestOptions, queryScope) {
        Ext.log('onFailure: status = ' + response.status + ', error text: ' + response.responseText);
    }

})