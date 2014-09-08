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
Ext.require('Mvpd.view.VaoPositionSearchPanel');
Ext.require('Mvp.util.Constants');

function onAstroViewEvent(msg)
{
	Ext.log("onAstroViewEvent:" + msg);
};


Ext.define('DemoApp.Portal', {
    statics: {
        // URLS
        voInventorySubsetBaseURL: 'https://osiris.ipac.caltech.edu/cgi-bin/VAOLink/nph-VAOlink?action=subset',
        tourHelpURL: 'http://www.usvao.org/documents/DataDiscoveryTool/UserGuide/index.html',
        choicesHelpURL: 'http://wiki.usvao.org/twiki/pub/Docs/DataDiscoveryToolTour/Data_Discovery_Tool_Tour.html#dropdownmenu',
        moreExamplesHelpURL: 'http://www.usvao.org/documents/DataDiscoveryTool/UserGuide/index.html#Search',
        homePage: isMast ? 'http://archive.stsci.edu/' : 'http://www.usvao.org/',
        logoImageLocation: isMast ? 'data/images/MAST_logo.png' : 'data/images/VAO_logo_100.png',
        logoWidth: isMast ? 173 : 100,
        logoHeight: isMast ? 71 : 50,

        createAndRun: function (options) {
            DemoApp.Portal.extBugWorkaround();
            var portal = Ext.create('DemoApp.Portal', options);
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

        injectSearchText: function (el) {
            var me = DemoApp.Portal;  // A static reference to this class.
            var value = el.innerHTML;
            if (value) {
                Ext.log('Injecting <' + value + '> into search box.');
                me.searchBox.setValue(value);
            } else {
                Ext.log('Unable to find value to inject into search box.');
            }
        },

        showAboutWindow: function () {
            var title = 'VAO Data Discovery Tool';
            if (isMast) {
                title = 'MAST Portal';
            }
            var versionString = DemoApp.Version.versionString();
            var aboutWindow = Ext.create('Ext.window.Window', {
                layout: 'fit',
                width: 300,
                height: 150,
                modal: true,
                html: '<div style="text-align: center;">' +
		    '<h3 style="color: black;font-size: 20px;padding-bottom: 8px;padding-top: 16px;">' + title +
		    '</h3>Version ' + versionString + '</div>',
                constrainHeader: true
            });
            aboutWindow.show();
        },

        launchNewPage: function launchDataDiscTool(url) {
            window.open(url, '_blank');
        }
    },

    // SearchBox panel and component dimensions
    sbPanelWidth: 700,
    fieldGapWidth: 10,
    actionChooserWidth: 410,
    exampleWidth: 350,

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

        me.facetContainer.addListener('resize', this.resizeFix, me.facetContainer);

        var searchPanel = me.createSearchPanel();
        //	var win = Ext.create('Ext.window.Window', {
        //            width: 450,
        //            minWidth: 435,
        //            height: 600,
        //            minHeight: 450,
        //            title: 'test',
        //            layout: 'fit',
        //            constrainHeader: true
        //        });
        //        win.add(searchPanel);
        //        win.show();

        me.north.add(searchPanel);

        if (useAV) {
            this.ensureAstroViewPanel();
            if (!useDesktop) {
                this.south.add(this.astroview);
            } else {
                var a = glabalDesk.createWindow({
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

    defineServiceList: function () {
        var serviceData = [];

        if (isMast) {
            serviceData = serviceData.concat([{
                fn: 'searchCaomVoTable', hint: 'Enter object name or RA and Dec',
                text: 'Search MAST CAOM via VOTable', resolve: true
            }, {
                fn: 'searchCaomDb', hint: 'Enter object name or RA and Dec',
                text: 'Search MAST CAOM', resolve: true
            }, {
                fn: 'searchHlsp', hint: 'Enter anything',
                text: 'Load MAST HLSP Projects', resolve: false
            }, {
                fn: 'searchGalexObjects', hint: 'Enter object name or RA and Dec',
                text: 'Search Galex Objects', resolve: true
            }, {
                fn: 'searchGalexTiles', hint: 'Enter object name or RA and Dec',
                text: 'Search Galex Tiles', resolve: true
            }, {
                fn: 'searchHlaVoTable', hint: 'Enter object name or RA and Dec',
                text: 'Search HLA', resolve: true
            }
	    ]);

            if (isDevelopment) {
                serviceData = serviceData.concat([{
                    fn: 'searchAdsAuthor', hint: "Enter Author's Name",
                    text: 'Search ADS for publications by author', resolve: true
                }, {
                    fn: 'searchDataScope', hint: 'Enter object name or RA and Dec',
                    text: 'Full VO Search (all known catalog and image collections)', resolve: true
                }, {
                    fn: 'searchInventory2', hint: 'Enter object name or RA and Dec',
                    text: 'VO Inventory (fast for selected collections)', resolve: true
                }, {
                    fn: 'searchVoTable', hint: 'Enter the URL of a VO Table',
                    text: 'Load VO Table', resolve: false
                }, {
                    fn: 'searchGalexSDSSObjects', hint: 'Enter object name or RA and Dec',
                    text: 'Search Galex/SDSS Objects', resolve: true
                }, {
                    fn: 'loadCsvFile', hint: 'Enter the URL of a CSV file',
                    text: 'Load CSV File', resolve: false
                }]);
            }
        } else {
            serviceData = serviceData.concat([{
                fn: 'searchInventory2', hint: 'Enter object name or RA and Dec',
                text: 'Quick Search (fast for selected collections)', resolve: true
            }, {
                fn: 'searchDataScope', hint: 'Enter object name or RA and Dec',
                text: 'Full Search (all known catalog and image collections)', resolve: true
            }
	    ]);

            if (isDevelopment) {
                serviceData = serviceData.concat([{
                    fn: 'searchAdsAuthor', hint: "Enter Author's Name",
                    text: 'Search ADS for publications by author', resolve: true
                }, {
                    fn: 'searchInventory', hint: 'Enter object name or RA and Dec',
                    text: 'Search VO Inventory Classic', resolve: true
                }, {
                    fn: 'searchCaomVoTable', hint: 'Enter object name or RA and Dec',
                    text: 'Search MAST CAOM via VOTable', resolve: true
                }, {
                    fn: 'searchHlaVoTable', hint: 'Enter object name or RA and Dec',
                    text: 'Search HLA', resolve: true
                }, {
                    fn: 'searchHlsp', hint: 'Enter anything',
                    text: 'Load MAST HLSP Projects', resolve: false
                }, {
                    fn: 'searchGalexObjects', hint: 'Enter object name or RA and Dec',
                    text: 'Search Galex Objects', resolve: true
                }, {
                    fn: 'searchGalexSDSSObjects', hint: 'Enter object name or RA and Dec',
                    text: 'Search Galex/SDSS Objects', resolve: true
                }, {
                    fn: 'searchGalexTiles', hint: 'Enter object name or RA and Dec',
                    text: 'Search Galex Tiles', resolve: true
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
                }, {
                    fn: 'loadCsvFile', hint: 'Enter the URL of a CSV file',
                    text: 'Load CSV File', resolve: false
                }, {
                    fn: 'voTableExport', hint: 'Enter the URL of a VO Table',
                    text: 'Export VO Table', resolve: false
                }]);
            }

        }
        return serviceData;

    },

    centerTabChange: function (tabPanel, newCard, oldCard) {
        if (Ext.isFunction(newCard.tabSelected)) {
            newCard.tabSelected();
        }
    },


    //////////////////////////////////////////////////////////////////////////////////////
    // AstroView
    makeAstroViewPanel: function () {

        this.swf = Ext.create('Ext.flash.Component', {
            id: 'AstroView',
            url: '../AstroView/AstroView.swf'
        });
        var panel = Ext.create('Ext.panel.Panel', {
            //   title: "AstroView",
            height: 500,
            layout: 'fit',
            resizable: true,
            items: this.swf
        });

        this.swf.on('success', this.onFlashSuccess, this);
        return panel;
    },

    onFlashSuccess: function () {
        this.swfId = this.swf.swfId;
    },

    publish: function (subject, msg) {
        var json = Ext.encode(msg);
        Publish(subject, json);
    },

    ensureAstroViewPanel: function () {
        if (!this.astroview) {
            this.astroview = this.makeAstroViewPanel();
        }
        return this.astroview;
    },

    moveTo: function (coneSearchParams) {

        if (useAV) {
            AstroView.moveTo(Ext.encode(coneSearchParams));
        }
    },


    //////////////////////////////////////////////////////////////////////////////////////


    createSearchPanel: function () {
        var me = this;

        var searchBoxWidth = me.sbPanelWidth - ( fullSearchOnly ? 0 : me.fieldGapWidth ) - me.actionChooserWidth;

        var sep1 = Ext.create('Ext.Component', { width: 20 });

        var logo = Ext.create('Ext.Component', {//width: 100,
            html: Mvp.util.Util.createImageLink(DemoApp.Portal.homePage, DemoApp.Portal.logoImageLocation, "Home",
								  DemoApp.Portal.logoWidth, DemoApp.Portal.logoHeight)
        });
        var sep2 = Ext.create('Ext.Component', { width: 50 });
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
            width: me.sbPanelWidth + 200,
            height: 86,  // Height set by Layout.js is 76
            border: 0
        });
        me.top.add(sep1);
        me.top.add(logo);
        if(!fullSearchOnly){
            me.top.add(sep2);
        }
        me.top.add(me.sbPanel);


        me.sbPanel.add(new Ext.form.field.Display({
            height: 8,
            value: '&nbsp;'
        }));

        var serviceData = me.defineServiceList();

        me.services = Ext.create('Ext.data.Store', {
            fields: ['fn', 'hint', 'text', 'resolve'],
            data: serviceData
        });

        me.actionPanel = Ext.create('Ext.panel.Panel', {
            layout: {
                type: 'hbox'
            },
            width: me.sbPanelWidth + 200,
            height: 25,
            border: 0
        });

        if (isMast) {
            me.searchBox = Ext.create('Mvp.util.SearchBox', {
                width: searchBoxWidth,
                height: 25
            });
        } else {
            me.searchBox = Ext.create('Mvpd.view.VaoPositionSearchPanel', {
                //width: searchBoxWidth + 100,
                //height: 30
            });
        }
        // Register this search box with the static part of the class so that it's easy to inject the search text
        // from the in-line help.
        DemoApp.Portal.searchBox = me.searchBox;


        if (!fullSearchOnly) {
            me.actionChooser = Ext.create('Ext.form.ComboBox', {
                id: 'tomdbg',
                store: me.services,
                queryMode: 'local',
                forceSelection: true,
                editable: false,
                displayField: 'text',
                width: me.actionChooserWidth,
                height: 25,
                valueField: 'fn'
            });

            me.actionChooser.on('change', me.actionChanged, me);
            if (isMast) {
                me.actionChooser.setValue('searchCaomVoTable');
            } else {
                me.actionChooser.setValue('searchDataScope');
            }
        } else {
            me.actionChooser = Ext.create('Ext.toolbar.TextItem', {
                width: me.actionChooserWidth,
               text: 'Search all catalog and image collections known to the VO:',
               style: {'font-size': '128%', 'text-align': 'right', 'vertical-align': 'middle'}
            });
        }

        me.actionPanel.add(me.actionChooser);

        if( !fullSearchOnly ){
            me.actionPanel.add(new Ext.form.field.Display({
                width: me.fieldGapWidth,
                value: '&nbsp;'
            }));
        }

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


        ////q.f. added an id for the empty panel. line 249
        //related line 475-480
        //me.examplesPanel.add(Ext.create('Ext.panel.Panel',{id: 'unResolveMsg'}), {


        //me.examplesPanel.add(Ext.create(Ext.panel.Panel), {
        //    width: me.actionChooserWidth + 10,
        //    border: 0
        //});

        me.examplesPanel.add(Ext.create('Ext.form.field.Display'), {
            width: me.actionChooserWidth +5,
            style: { 'text-align': 'center' },
            border: 0,
            //html: '<a target="_blank" href="' + DemoApp.Portal.choicesHelpURL + '">About Search Options</a>' + ' | ' +
            //html: Mvp.util.Util.createLink(DemoApp.Portal.choicesHelpURL, 'Search Options') + ' | ' +
            html: Mvp.util.Util.createLink(DemoApp.Portal.tourHelpURL, 'User Guide') + ' | ' +
                '<a href="javascript: void(0)" onclick="DemoApp.Portal.showAboutWindow()">About Discovery Tool...</a>'
        });

        if( !fullSearchOnly ){
            me.examplesPanel.add(new Ext.form.field.Display({
                width: me.fieldGapWidth,
                value: '&nbsp;'
            }));
        }

        var ex1 = Ext.create('Ext.form.field.Display', {
            width: me.exampleWidth,
            html: 'Examples: ' +
		'<a href="javascript: void(0)" onclick="DemoApp.Portal.injectSearchText(this)">M101</a>, ' +
		'<a href="javascript: void(0)" onclick="DemoApp.Portal.injectSearchText(this)">14 03 12.6 +54 20 56.7' + (isMast ? ' r=0.2d</a>, ' : '</a>, ') +
		Mvp.util.Util.createLink(DemoApp.Portal.moreExamplesHelpURL, 'more...')
        });
        me.examplesPanel.add(ex1);

        // create upload button.
        var uploadButton = Ext.create('Ext.Button', {
            text: 'Upload File...',
            tooltip: 'Upload a file from the local disk',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            handler: function (button, event) {
                Mvp.util.Uploader.showDialog(this);
            }
        });
        //me.top.add(uploadButton);

        //me.north.add(me.top);
        me.sbPanel.add(me.examplesPanel);
        if (isDevelopment) me.sbPanel.add(uploadButton);

        me.searchBox.on('searchInitiated', me.doSearch, me);

        return me.top;
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

    doSearch: function (text, actionOverride, extraArgs) {
        var me = this;

        var action = 'searchDataScope';
        if (!fullSearchOnly) {
            action = me.actionChooser.getValue();
        }
        Ext.log('Search invoked for: ' + text);

        if (Ext.isString(actionOverride)) {
            action = actionOverride;
        }

        var dispatchFunction = me[action];
        if (action == 'searchDataScope' ||
        	action == 'searchInventory' ||
        	action == 'searchInventory2' ||
        	action == 'searchCaomVoTable' ||
        	action == 'searchAdsCone' ||
        	action == 'searchHlaVoTable' ||
        	action == 'searchHlsp' ||
        	action == 'searchGalexObjects' ||
            action == 'searchGalexSDSSObjects' ||
        	action == 'searchGalexTiles' ||
            action == 'searchCaomDb' ||
            action == 'astroViewSearch' ||
            action == 'searchGenericCone' ||
            action == 'searchGenericSiap' ||
            action == 'loadGenericSiap') {
            me.resolveName(text, dispatchFunction, extraArgs);
        } else {
            // Just dispatch on the search text.
            Ext.callback(dispatchFunction, me, [text]);
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
            var currentHint = me.searchBox.getHint();
            var hintChanged = (currentHint !== hint);

            // Set the new empty text value.
            me.searchBox.setHint(hint);

            // If the search box was empty, or if there's a new hint, force it to display the new hint.
            if ((currentSearchText === '') || hintChanged) {
                me.searchBox.reset();

                // Hide the resolver summary if new action does not use it
                if (me.resolverSummaryPanel) {
                    if (resolve) {
                        me.resolverSummaryPanel.show();
                    } else {
                        me.resolverSummaryPanel.hide();
                        me.resolverSummaryPanel.setValue('');
                    }
                }
            }
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

            me.moveTo(coneSearchParams);

        } else {
            alert("Could not resolve <" + queryScope.request.params.input + "> to a position");
            //q.f Add message.  475-480  , related line 250
            //var msgPanel=Ext.getDom('unResolveMsg');
            //msgPanel.innerHTML='<p style="color:sienna;margin-right:5px">Could not resolve <"' + queryScope.request.params.input + '"> to a position</p>';
            //msgPanel.style.width="auto";
            //msgPanel.style.height="auto"; 
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
            extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.caomDbRecordSelected,
            onDblClick: null,
            createImagePanel: true,
            tooltip: this.resolverSummaryPanel.getValue(),
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

        var facetConfigs = [{
            column: 'survey'
        }, {
            column: 'tilename'
        }];

        var options = {
            title: '&nbsp' + title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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

        var facetConfigs = [{
            column: 'survey'
        }, {
            column: 'tilename'
        }];

        var options = {
            title: '&nbsp' + title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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

        var facetConfigs = [{
            column: 'survey'
        }, {
            column: 'tilename'
        }];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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

        var facetConfigs = [{
            column: 'Format'
        }, {
            column: 'Detector'
        }, {
            column: 'Aperture'
        }, {
            column: 'Spectral_Elt'
        }, {
            column: 'Target'
        }, {
            column: 'PI_Name'
        }];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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

        var facetConfigs = [];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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
            extraColumns: [{ text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134}],
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.caomDbRecordSelected,
            onDblClick: null,
            createImagePanel: true,
            tooltip: this.resolverSummaryPanel.getValue(),
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
            tooltip: this.resolverSummaryPanel.getValue(),
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
            clearcache: this.skipCache
        };
        request.params = {};
        request.params.id = hp_id;

        var facetConfigs = [{
            column: 'hd_instrument'
        }, {
            column: 'hd_format'
        }];

        var options = {
            title: title,
            // searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
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
        var facetConfigs = [];

        var options = {
            title: title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
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
        var facetConfigs = [];

        var options = {
            title: '&nbsp' + title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
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
        var facetConfigs = [];

        var options = {
            title: title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
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
        var facetConfigs = [];

        var options = {
            title: '&nbsp' + title,
            searchText: author,
            request: request,
            facetConfigs: facetConfigs,
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
        var facetConfigs = [];

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
            tooltip: this.resolverSummaryPanel.getValue(),
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
            clearcache: this.skipCache
        };
        request.params = {};

        var facetConfigs = [{
            column: 'hp_wavelength'
        }, {
            column: 'hp_prodtype'
        }, {
            column: 'hp_objtype'
        }];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: onClick,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
            icon: Mvp.util.Constants.IMAGE_ICON
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

        var exclude = [{ column: 'categories', exclude: ['HTTP Request', 'Web Page']}];

        var options = {
            title: title,
            searchText: searchText,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.dsRecordSelected,
            onDblClick: null,
            tooltip: this.resolverSummaryPanel.getValue(),
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
            icon: Mvp.util.Constants.MIXED_COLLECTION_ICON
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
        var facetConfigs = [];

        var options = {
            title: title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle || coneSearchParams.input || this.resolverSummaryPanel.getValue(),
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
        var facetConfigs = [];

        var options = {
            title: title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.genericRecordSelected,
            onDblClick: null,
            tooltip: longTitle || coneSearchParams.input || this.resolverSummaryPanel.getValue(),
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
        var facetConfigs = [];

        var options = {
            title: title,
            searchText: url,
            request: request,
            facetConfigs: facetConfigs,
            app: this,
            onClick: this.siaRecordSelected,
            onDblClick: null,
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

    onError: function (responseObject, requestOptions, queryScope) {
        Ext.log('onError: ');
    },

    onFailure: function (response, requestOptions, queryScope) {
        Ext.log('onFailure: status = ' + response.status + ', error text: ' + response.responseText);
    }

});