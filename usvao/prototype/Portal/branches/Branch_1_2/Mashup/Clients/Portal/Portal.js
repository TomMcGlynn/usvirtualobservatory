Ext.require('Mvp.util.SearchBox');
Ext.require('Mvp.util.NameResolverModel');
Ext.require('Mvp.util.Exporter');
Ext.require('Mvp.util.Uploader');
Ext.require('Mvp.grid.MvpGrid');
Ext.require('Mvp.grid.FacetedGridHelper');
Ext.require('Mvp.filters.FacetFilterPanel');
Ext.require('Mvp.util.MashupQuery');
Ext.require('Mvp.util.Util');
Ext.require('Mvp.custom.Caom');
Ext.require('Mvp.custom.FullSearch');
Ext.require('Mvp.custom.Hst');
Ext.require('Mvp.custom.Generic');
Ext.require('DemoApp.Layout');
Ext.require('DemoApp.Viewport');
Ext.require('DemoApp.Version');

/*
Ext.require('DemoApp.DetailsPanelDS');
Ext.require('DemoApp.DetailsPanelInv');
Ext.require('DemoApp.DetailsPanelInv2');
Ext.require('DemoApp.DetailsPanelGeneric');
Ext.require('DemoApp.DetailsPanelHLSP');
*/
Ext.require('Mvpc.view.GenericDetailsContainer');
Ext.require('Mvpc.view.CatalogDetailsContainer');
Ext.require('Mvpc.view.CaomDetailsContainer');
Ext.require('Mvpc.view.SiaDetailsContainer');
Ext.require('Mvpc.view.NedSedDetails');
Ext.require('Mvpd.view.VaoPositionSearchPanel');
Ext.require('Mvp.util.Constants');
Ext.require('Mvpc.view.AstroViewContainer');
Ext.require('Mvpc.view.PressReleaseContainer');
Ext.require('Mvpc.view.StprDetailsContainer');
Ext.require('Mvpc.view.TopBarContainer');
Ext.require('Mvp.util.Searches');

function onAstroViewEvent(msg)
{
	Ext.log("onAstroViewEvent:" + msg);
};


Ext.define('DemoApp.Portal', {
    statics: {
        // URLS
        voInventorySubsetBaseURL: 'https://osiris.ipac.caltech.edu/cgi-bin/VAOLink/nph-VAOlink?action=subset',

        createAndRun: function (options) {
            DemoApp.Portal.extBugWorkaround();
            var portal = Ext.create('DemoApp.Portal', options);
			
			// Hack to make the portal scope available for callbacks.
			// Remove during refactoring.
			DemoApp.Portal.portalScope = portal;
			
            portal.run();
        },

        extBugWorkaround: function () {
            // Workaround for bug in ext that prevents us from closing a tab that contains a sub-tab that has
            // never been visualized:  EXTJSIV-3294, see:
            // http://www.sencha.com/forum/showthread.php?136528-4.0.2-Store.bindStore-assumes-me.loadMask-has-bindStore-function/page2
            //
            //put at the root of your script, but inside .onReady 
            //I also happened to change 'me' to 'this' , in my override , if you compare 
            //to original source, but its not required at all 
            Ext.override(Ext.view.AbstractView, {

                bindStore: function (store, initial) {
                    //var me = this;
                    if (!initial && this.store) {
                        if (store !== this.store && this.store.autoDestroy) {
                            this.store.destroy();
                        } else {
                            this.mun(this.store, {
                                scope: this,
                                datachanged: this.onDataChanged,
                                add: this.onAdd,
                                remove: this.onRemove,
                                update: this.onUpdate,
                                clear: this.refresh
                            });
                        }
                        if (!store) {
                            if (this.loadMask && typeof this.loadMask.bindStore == 'function') {
                                this.loadMask.bindStore(null);
                            }
                            this.store = null;
                        }
                    }
                    if (store) {
                        store = Ext.data.StoreManager.lookup(store);
                        this.mon(store, {
                            scope: this,
                            datachanged: this.onDataChanged,
                            add: this.onAdd,
                            remove: this.onRemove,
                            update: this.onUpdate,
                            clear: this.refresh
                        });
                        if (this.loadMask && typeof this.loadMask.bindStore == 'function') {
                            this.loadMask.bindStore(store);
                        }
                    }
                    this.store = store;
                    this.getSelectionModel().bind(store);
                    if (store && (!initial || store.getCount())) {
                        this.refresh(true);
                    }
                }
            });
        },

        launchNewPage: function launchDataDiscTool(url) {
            window.open(url, '_blank');
        },

        injectSearchText: function (el) {
            // amazingly, this has to be left at the Portal level to behave the way we want it
            // the TopBarContainer constructor cannot refer to itself in HTML after it has executed
            var me = DemoApp.Portal;  // A static reference to this class.
            var value = el.innerHTML;
            if (value) {
                Ext.log('Injecting <' + value + '> into search box.');
                me.searchBox.setValue(value);
            } else {
                Ext.log('Unable to find value to inject into search box.');
            }
        }
    },
    
    // Used only by FacetedGridHelper
    centerWidth: 780,
    centerHeight: 440,
    //southWidth: 1090,
    //southHeight: 190,

    skipCache: false,

    constructor: function (config) {
        var me = this;

        Ext.apply(me, config);
        this.detailsWindow = Ext.create('Ext.window.Window', {
            layout: 'fit',
            width: 400,
            minWidth: 400,
            height: 600,
            minHeight: 450,
            closeAction: 'hide',
            constrainHeader: true
        });
        this.detailsWindow.addListener('resize', this.resizeFix, this.detailsWindow);
    },

    run: function () {
        var me = this;

        // Create the main panel with a border layout.
        me.mainPanel = Ext.create('DemoApp.Layout', {
            //renderTo: Ext.getBody()
            region: 'center'     // center region is required, no width/height specified
            //border: false
        });
        // Create the container Viewport.
        me.viewport = Ext.create('DemoApp.Viewport', {
            renderTo: Ext.getBody(),
            mainPanel: me.mainPanel
        });

        me.north = me.mainPanel.northPanel;
        //me.south = me.mainPanel.southPanel;
        me.east = me.mainPanel.eastPanel;
        me.west = me.mainPanel.westPanel;
        me.center = me.mainPanel.centerPanel;
        this.avPanel = this.center.getComponent('centerAvPanel');

        // Aliases for FacetedGridHelper
        //me.gridContainer = me.center;
        this.gridContainer = this.center.getComponent('centerGridPanel');
        //me.center.on('tabchange', this.centerTabChange, this);
        this.gridContainer.on('tabchange', this.centerTabChange, this);
        me.facetContainer = me.west;

        me.facetContainer.addListener('resize', this.resizeFix, me.facetContainer);

        DemoApp.Portal.searchPanel = Ext.create('Mvpc.view.TopBarContainer', {
            scope: this,
            serviceList: Mvp.util.Searches.defineServiceList()
        });

        me.north.add(DemoApp.Portal.searchPanel);

        if (useAV) {
            this.ensureAstroViewPanel();
            if (!useDesktop) {
                this.avPanel.add(this.astroview);
            } else {
                var a = globalDesk.createWindow({
                    id: "astroview-win",
                    title: "AstroView",
                    width: 600,
                    height: 400,
                    iconCls: "icon-grid",
                    animCollapse: false,
                    constrainHeader: true,
                    layout: "fit",
                    items: [this.astroview]
                });
                a.show();

            }
        }
    },

    resizeFix: function () {
        var size = this.getSize();
        this.suspendEvents();
        this.setSize(size.width + 5, size.height - 5);
        this.setSize(size.width + 5, size.height);
        this.resumeEvents();
    },

    centerTabChange: function (tabPanel, newCard, oldCard) {
        if (Ext.isFunction(newCard.tabSelected)) {
            newCard.tabSelected();
        }
    },

    publish: function (subject, msg) {
        var json = Ext.encode(msg);
        Publish(subject, json);
    },

    ensureAstroViewPanel: function () {
        if (!this.astroview) {
            this.astroview = Ext.create('Mvpc.view.AstroViewContainer', {rendertype:'flash'});
        }
        return this.astroview;
    },

    moveTo: function (coneSearchParams) {

        if (useAV) {
            AstroView.moveTo(coneSearchParams);
        }
    },

    test: function () {
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
            Ext.callback(requestOptions.dispatchFunction, this, args);

            this.moveTo(coneSearchParams);

        } else {
            alert("Could not resolve <" + queryScope.request.params.input + "> to a position");
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
        var mashupQueryOptions = {
            request: request,
            onResponse: me.onResponseName,
            onError: me.onError,
            onFailure: me.onFailure,
            scope: me,
            ajaxParams: {
                dispatchFunction: dispatchFunction,
                extraArgs: extraArgs
            }
        };
        if (isLocal) {
            //Ext.apply(mashupQueryOptions, { overrideURL: mashupDevelopmentURLOverride });
        }
        var query = Ext.create('Mvp.util.MashupQuery', mashupQueryOptions);
        query.run();
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // NED SED services
    //
    // 
    Ned_SedInfoDiscovery_Votable: function (coneSearchParams, searchText) {

        var title = 'SEDs near: ' + searchText;
        var request = {
            service: 'Ned.SedInfoDiscovery.Votable',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            //extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            app: this,
            onClick: this.nedSedRecordSelected,
            onDblClick: null,
            //createImagePanel: true,
            tooltip: title,
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    Ned_SedInfoAvailability_Votable: function (targetname) {

        var title = 'SEDs for: ' + targetname;
        var request = {
            service: 'Ned.SedInfoAvailability.Votable',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.targetname = targetname;
        request.params.input = targetname;

        var options = {
            title: '&nbsp' + title,
            searchText: targetname,
            request: request,
            //extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            app: this,
            onClick: this.nedSedRecordSelected,
            onDblClick: null,
            //createImagePanel: true,
            tooltip: title,
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    Ned_SedDataRetrieval_Votable: function (targetname) {

        var title = 'SEDs for: ' + targetname;
        var request = {
            service: 'Ned.SedDataRetrieval.Votable',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.targetname = targetname;
        request.params.input = targetname;

        var options = {
            title: '&nbsp' + title,
            searchText: targetname,
            request: request,
            //extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            //createImagePanel: true,
            tooltip: title,
            icon: Mvp.util.Constants.CATALOG_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },


    //////////////////////////////////////////////////////////////////////////////////////
    // CAOM VO Table
    searchCaomVoTable: function (coneSearchParams, searchText) {

        var title = 'CAOM VOT: ' + searchText;
        var request = {
            service: 'Caom.Cone.Votable',
            clearcache: this.skipCache,
            columnsconfigid: 'Mast.Caom.Cone'
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            app: this,
            onClick: this.caomDbRecordSelected,
            onDblClick: null,
            createImagePanel: true,
            imagePanelTemplate: Mvp.custom.Caom.dataviewTemplate(),
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // HST Press Releases
    searchPressRelease: function () {

        var title = 'HST Press Releases (HubbleSite.org)';
        var request = {
            service: 'Hst.PressRelease.Votable',
            clearcache: this.skipCache
        };

        var exclude = [{ column: 'UnnamedField', exclude: ['HTTP Request', 'Web Page']}];

        var options = {
            title: title,
            searchText: '',
            request: request,
            extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Hst.pressPreviewRenderer, width: 134}],
            app: this,
            onClick: this.hstPressRecordSelected,
            onDblClick: null,
            createImagePanel: true,
            imagePanelTemplate: Mvp.custom.Hst.dataviewTemplate(),
            renderers: [{ dataIndex: 'UnnamedField', renderer: Mvp.custom.Hst.font15Renderer },
						{ dataIndex: 'UnnamedField-20', renderer: Mvp.custom.Hst.font13Renderer },
						{ dataIndex: 'UnnamedField-22', renderer: Mvp.custom.Hst.font13Renderer },
						{ dataIndex: 'UnnamedField-24', renderer: Mvp.custom.Hst.font13Renderer }
						],
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // HST Press Releases (STPR service)
    searchStpr: function () {

        var title = 'HST Press Releases (STPR)';
        var request = {
            service: 'Mast.Stpr.Votable',
            clearcache: this.skipCache
        };

        var options = {
            title: title,
            searchText: '',
            request: request,
            extraColumns: [{ text: 'Preview', dataIndex: 'resourceurl', renderer: Mvp.custom.Hst.stprPreviewRenderer, width: 134}],
            app: this,
            onClick: this.stprSelected,
            onDblClick: null,
            createImagePanel: true,
            imagePanelTemplate: Mvp.custom.Hst.stprDataviewTemplate(),
            renderers: [{
                dataIndex: 'title', renderer: Mvp.custom.Hst.font15Renderer
            }, {
                dataIndex: 'descriptions', renderer: Mvp.custom.Hst.font13Renderer
            }],
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Galex objects
    searchGalexObjects: function (coneSearchParams, searchText) {

        var title = 'Galex Objects: ' + searchText;
        var request = {
            service: 'Mast.Galex.Catalog',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: '&nbsp' + title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.CATALOG_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Galex/SDSS objects
    searchGalexSDSSObjects: function (coneSearchParams, searchText) {

        var title = 'Galex/SDSS Objects: ' + searchText;
        var request = {
            service: 'Mast.Galex.Sdss.Catalog',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: '&nbsp' + title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.CATALOG_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Galex tiles
    searchGalexTiles: function (coneSearchParams, searchText) {

        var title = 'Galex Tiles: ' + searchText;
        var request = {
            service: 'Mast.Galex.Tile',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // HLA VO Table
    searchHlaVoTable: function (coneSearchParams, searchText) {

        var title = 'HLA: ' + searchText;
        var request = {
            service: 'Hla.Hubble.Votable',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // ADS Code Search
    searchAdsCone: function (coneSearchParams, searchText) {

        var title = 'ADS: ' + searchText;
        var request = {
            service: 'Ads.Cone.Votable',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // CAOM DB
    searchCaomDb: function (coneSearchParams, searchText) {

        var title = /*'CAOM DB: ' + */searchText;
        var request = {
            service: 'Mast.Caom.Cone',
            clearcache: this.skipCache
        };

        request.params = coneSearchParams;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            app: this,
            onClick: this.caomDbRecordSelected,
            onDblClick: null,
            createImagePanel: true,
            imagePanelTemplate: Mvp.custom.Caom.dataviewTemplate(),
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
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

        if (coneSearchParams.id) {
            // This is a drill down request for one resource, so the onClick callback should be the one for generic table records,
            // and the request service should be the drill down one.
            onClick = this.genericRecordSelected;
            request.service = 'Vo.Inventory2.DrillDown';
        }

        var options = {
            title: title,
            searchText: searchText,
            coneSearchParams: coneSearchParams,
            request: request,
            app: this,
            onClick: onClick,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // HLSP Drill Down
    getHlspProducts: function (hp_id) {

        var title = 'HLSP: ' + hp_id;
        var request = {
            service: 'Mast.Hlsp.Products',
            clearcache: this.skipCache,
            columnsconfigid: 'Mast.Hlsp.Votable'
        };
        request.params = {};
        request.params.id = hp_id;

        var options = {
            title: title,
            // searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Table
    searchVoTable: function (url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Table',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle,
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // MixedCollection
    fullSearchVoTable: function (url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Table',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: '&nbsp' + title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle,
            icon: Mvp.util.Constants.CATALOG_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // CSV File
    loadCsvFile: function (url, gridTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Csv.Generic.Table',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // ADS Author Search
    searchAdsAuthor: function (author) {

        var title = 'ADS: ' + author;
        var request = {
            service: 'Ads.Author.Votable',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.author = author;
        request.params.input = author;

        var options = {
            title: '&nbsp' + title,
            searchText: author,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // VO Table export
    voTableExport: function (url, filename, filetype) {

        var title = filename || url;
        var request = {
            service: 'Vo.Generic.Table',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            filename: filename || 'ExportedVoTable.csv',
            filetype: filetype || 'csv',
            request: request,
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.util.Exporter.activate(options);
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

        var options = {
            title: title,
            searchText: searchText,
            coneSearchParams: coneSearchParams,
            request: request,
            app: this,
            onClick: this.invRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // HLSP
    searchHlsp: function (searchText) {

        var title = 'HLSP';
        var onClick = this.hlspRecordSelected;
        var request = {
            service: 'Mast.Hlsp.Project',
            clearcache: this.skipCache,
            columnsconfigid: 'Mast.Hlsp.Votable'
        };
        request.params = {};

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: onClick,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            renderers: [{ dataIndex: 'hp_wavelength', renderer: Mvp.custom.Generic.spaceColumnRenderer },
                        { dataIndex: 'hp_prodtype', renderer: Mvp.custom.Generic.titleRenderer },
                        { dataIndex: 'hp_objtype', renderer: Mvp.custom.Generic.titleRenderer}],
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Staff Papers
    searchStaffPapers: function (searchText) {

        var title = 'Staff Papers';
        var onClick = this.genericRecordSelected;
        var request = {
            service: 'Mast.PaperTrack.Staff',
            clearcache: this.skipCache
           // columnsconfigid: 'Mast.Hlsp.Votable'
        };
        request.params = {
			lastname: searchText
		};
        request.pagesize = 10000000;  // Ensure that the results are not chunked from the server.

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: onClick,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
            //renderers: [{ dataIndex: 'hp_wavelength', renderer: Mvp.custom.Generic.spaceColumnRenderer },
            //            { dataIndex: 'hp_prodtype', renderer: Mvp.custom.Generic.titleRenderer },
            //            { dataIndex: 'hp_objtype', renderer: Mvp.custom.Generic.titleRenderer}],
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Ranked Authors
    searchRankedAuthors: function (searchText) {

        var title = 'Ranked Authors';
        var onClick = this.genericRecordSelected;
        var request = {
            service: 'Mast.Hlsp.RankedAuthors',
            clearcache: this.skipCache
           // columnsconfigid: 'Mast.Hlsp.Votable'
        };
        request.params = {};

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: onClick,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
            //renderers: [{ dataIndex: 'hp_wavelength', renderer: Mvp.custom.Generic.spaceColumnRenderer },
            //            { dataIndex: 'hp_prodtype', renderer: Mvp.custom.Generic.titleRenderer },
            //            { dataIndex: 'hp_objtype', renderer: Mvp.custom.Generic.titleRenderer}],
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // DataScope
    searchDataScope: function (coneSearchParams, searchText) {

        var title = searchText;
        var request = {
            service: 'Vo.Hesarc.Datascope',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.skipcache = (this.skipCache) ? 'YES' : 'NO';
        request.params.input = searchText;
        request.pagesize = 10000000;  // Ensure that the Full Search results are not chunked from the server.

        var exclude = [{ column: 'categories', exclude: ['HTTP Request', 'Web Page']}];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.dsRecordSelected,
            onDblClick: null,
            tooltip: DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            exclude: exclude,
            renderers: [{ dataIndex: 'waveband', renderer: Mvp.custom.FullSearch.hashColumnRenderer}],
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
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

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.dsRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Search SID archive by instrument
	searchSidByInst: function (searchText) {

        var title = 'SID: ' + searchText;
        var request = {
            service: 'Mast.Sid.Votable',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.inst = searchText;
        request.params.input = searchText;

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Static file load
    getDataSetFile: function (file) {

        var title = file;
        var url = file;

        var options = {
            title: title,
            searchText: file,
            request: url,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Whole registry
    loadWholeRegistry: function (searchText) {

        var title = 'Whole Registry ';
        var url = 'data/wholeregistry-trimmed-extjs.json';

        var options = {
            title: title,
            searchText: url,
            request: url,
            app: this,
            onClick: this.dsRecordSelected,
            onDblClick: null,
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone Search
    searchGenericCone: function (coneSearchParams, url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Cone',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle || coneSearchParams.input || DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.GENERIC_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone SIAP
    searchGenericSiap: function (coneSearchParams, url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Siap',
            clearcache: this.skipCache
        };
        request.params = coneSearchParams;
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle || coneSearchParams.input || DemoApp.Portal.searchPanel.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Generic Cone SIAP
    loadGenericSiap: function (url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Vo.Generic.Table',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: url,
            request: request,
            app: this,
            onClick: this.siaRecordSelected,
            onDblClick: null,
            tooltip: longTitle,
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // STPR table load
    loadStpr: function (url, gridTitle, longTitle) {

        var title = gridTitle || url;
        var request = {
            service: 'Mast.KnownStpr.Votable',
            columnsconfigid: 'Mast.Stpr.Votable',
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.url = url;
        request.params.input = title;

        var options = {
            title: title,
            searchText: gridTitle,
            request: request,
            extraColumns: [{ text: 'Preview', dataIndex: 'resourceurl', renderer: Mvp.custom.Hst.stprPreviewRenderer, width: 134}],
            app: this,
            onClick: this.stprSelected,
            onDblClick: null,
            createImagePanel: true,
            imagePanelTemplate: Mvp.custom.Hst.stprDataviewTemplate(),
            renderers: [{
                dataIndex: 'title', renderer: Mvp.custom.Hst.font15Renderer
            }, {
                dataIndex: 'descriptions', renderer: Mvp.custom.Hst.font13Renderer
            }],
            tooltip: longTitle,
            icon: Mvp.util.Constants.IMAGE_ICON
        };
        Mvp.grid.FacetedGridHelper.activate(options);
    },

    //////////////////////////////////////////////////////////////////////////////////////
    // Drill Down   
    caomClicked: function (view, record, htmlElement, index, e, config) {
        Ext.log('CAOM entry clicked');
    },

    registryClicked: function (view, record, htmlElement, index, e, config) {
        Ext.log('Registry entry clicked');
    },

    inventoryClicked: function (view, record, htmlElement, index, e, config) {
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

    dsClicked: function (view, record, htmlElement, index, e, config) {
        var me = this;
        var accessURL = record.get('tableURL');
        var title = record.get('title');
        if (accessURL) {
            me.searchVoTable(accessURL, title);
        } else {
            alert("VO Table unavailable for " + title);
        }
    },

    dsRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = null;
        var identifier = record.get('identifier');
        var p = Ext.create('Mvpc.view.CatalogDetailsContainer', { record: record, searchScope: this, grid: config.grid, searchText: searchText });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    siaRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = Ext.create('Mvpc.view.SiaDetailsContainer', { record: record, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    hlspRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    invRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var coneSearchParams = (view.panel) ? view.panel.coneSearchParams : null;

        var p = Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    inv2RecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var coneSearchParams = (view.panel) ? view.panel.coneSearchParams : null;

        var p = Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    genericRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();

    },

    caomDbRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var target = record.get('target_name'),
            obsId = record.get('obs_id');

        var p = Ext.create('Mvpc.view.CaomDetailsContainer', { record: record, portal: this, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();

        var fp = record.get('s_region');
        fp = fp.replace('ICRS', 'J2000');
        fp = fp.replace('(', '');
        this.flashContextHack = { footprint: fp };
    },

    hstPressRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;

        var p = Ext.create('Mvpc.view.PressReleaseContainer', { record: record, portal: this, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();

        /*
        var fp = record.get('s_region');
        fp = fp.replace('ICRS', 'J2000');
        fp = fp.replace('(', '');
        this.flashContextHack = { footprint: fp };
        */
    },

    stprSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;

        var p = Ext.create('Mvpc.view.StprDetailsContainer', { record: record, portal: this, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();

        /*
        var fp = record.get('s_region');
        fp = fp.replace('ICRS', 'J2000');
        fp = fp.replace('(', '');
        this.flashContextHack = { footprint: fp };
        */
    },

    nedSedRecordSelected: function (view, record, htmlElement, index, e, config) {
        var searchText = (view.panel) ? view.panel.searchText : null;
        var p = Ext.create('Mvpc.view.NedSedDetails', { record: record, searchScope: this, grid: config.grid });
        this.detailsWindow.removeAll();
        this.detailsWindow.add(p);
        this.detailsWindow.setTitle('Details: ' + searchText);
        this.detailsWindow.show();
    },

    onError: function (responseObject, requestOptions, queryScope) {
        Ext.log('onError: ');
    },

    onFailure: function (response, requestOptions, queryScope) {
        Ext.log('onFailure: status = ' + response.status + ', error text: ' + response.responseText);
    }

});
