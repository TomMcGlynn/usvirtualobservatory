/**
 * Documentation for MAST.Portal
 */

Ext.define('Mast.Portal', {
    requires: ['Mast.view.MastTopBar', 'Ext.util.Cookies', 'Mvp.util.Version', 'Ext.ux.IFrame', 'Ext.layout.container.Border', 'Ext.container.Viewport', 'Mvp.search.SearchParams', 'Mvp.search.Summary'],
    extend: 'Mvp.util.BasePortal',

    constructor: function (config) {
        this.callParent(arguments);
    },

    // Public methods

    // Private methods
    run: function () {
        this.mainPanel = Ext.create('Mvp.gui.PortalBorderContainer', {
            region: 'center',
            avPanel: this.avPanel,
            cloudPanel: this.cloudPanel
        });

        var sp = Mvp.search.SearchParams;
        var searchParams = [
			sp.getSearch('SUMMARY'),
			sp.getSearch('HYBRID_SUMMARY'),
            sp.getSearch('REGKEYWORD'),
            sp.getSearch('CAOM'),
            sp.getSearch('DataScope')
        ];
        var devSearchParams = [
            sp.getSearch('PA'),
            sp.getSearch('AS'),
            sp.getSearch('HLA'),
            sp.getSearch('MAST_MISSION'),
            sp.getSearch('IUE'),
            sp.getSearch('HSTSC'),
            sp.getSearch('GSC23'),
            sp.getSearch('CAOMDB'),
            sp.getSearch('CAOMDBDEV'),
            sp.getSearch('CAOMBYOBS'),
            sp.getSearch('WhatIs'),
            sp.getSearch('ADS'),
            sp.getSearch('CSV'),
            sp.getSearch('CAOMDownload'),
            sp.getSearch('GalexObjects'),
            sp.getSearch('GalexSdss'),
            sp.getSearch('GalexTiles'),
            sp.getSearch('GalexPhotonListNuv'),
            sp.getSearch('GalexPhotonListFuv'),
            sp.getSearch('HLSP'),
            sp.getSearch('STPR'),
            sp.getSearch('HSTPR'),
            sp.getSearch('RP'),
            sp.getSearch('SANTA'),
        //sp.getSearch('SedDiscovery'), // service is broken as far as I can tell
            sp.getSearch('SedAvailability'),
            sp.getSearch('SedRetrieval'),
            sp.getSearch('SIDBYINST'),
            sp.getSearch('SIDBYJOB'),
            //sp.getSearch('SIDGETPACKAGE'),
            //sp.getSearch('SIDGETALLFILES'),
			sp.getSearch('VOTable'),
			sp.getSearch('iFrame'),
            sp.getSearch('LIT'),
            sp.getSearch('STP'),
            sp.getSearch('DSREG'),
            sp.getSearch('WHOLEREG')
        ];
        if (AppConfig.isDebug || AppConfig.allChoices) searchParams = searchParams.concat(devSearchParams);
        var searchPanel = Ext.create('Mast.view.MastTopBar', {
            searchParams: searchParams,
            defaultSearch: 'SUMMARY',
            versionString: Mvp.util.Version.versionString()
        });
        searchPanel.addListener('newsearch', this.searchCallback, this);

        // Create the container Viewport.
        this.viewport = Ext.create('Ext.container.Viewport', {
            renderTo: this.mainDiv,
            margin: 0,
            layout: 'border',
            items: this.mainPanel
        });

        this.mainPanel.getNorth().add(searchPanel);
        this.searchPanel = searchPanel;


        this.resultPanel = this.mainPanel.getResultPanel();
        this.resultPanel.on('tabchange', this.onTabChange, this);
        // Some trickery with the Astroview panel to make the page look "clean"
        var em = this.app.getEventManager();
        em.addListener('APP.context.added', this.contextAdded, this);
        em.addListener('APP.context.removed', this.contextRemoved, this);
        em.addListener('APP.AstroView.Search.Request', this.searchWhatIs, this);	

        // Try adding a new tab with startup info.
        var src = AppConfig.startPage || 'data/html/Start Page.html';

        var iFrame = Ext.create('Ext.ux.IFrame', {
            src: src,
            width: 600
        });
        var startPanel = Ext.create('Ext.panel.Panel', {
            title: 'Start Page',
            layout: 'fit',
            width: 600,
            items: [iFrame],
            closable: true
        });
        this.resultPanel.add(startPanel);
    },

    contextAdded: function () {
    },

    contextRemoved: function () {

    },
	
	searchWhatIs: function(event) {
		var sp = Mvp.search.SearchParams.getSearch('WhatIs');
        sp.coords = event.coords;
        var searchInput = {
            inputText: event.coords,
            title: 'What Is',
            description: 'XXX'
        };
        //this.searchCallback(searchInput, sp);
	}

});
