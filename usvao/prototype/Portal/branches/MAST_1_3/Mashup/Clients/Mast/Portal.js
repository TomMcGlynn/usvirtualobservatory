/**
 * Documentation for MAST.Portal
 */

// This class needs to have the requires statements outside the Ext.define() because
// this class is loaded by the top-level html, and the internal requires statement
// seems to cause lazy initialization of the class.
Ext.require('Mast.view.MastTopBar');
Ext.require('Ext.util.Cookies');
Ext.require('Mast.Version');
Ext.require('Mvp.gui.IFrame');

Ext.define('Mast.Portal', {
    extend: 'Mvp.util.BasePortal',

    constructor: function (config) {
        this.callParent(arguments);
    },

    // Public methods

    // Private methods
    run: function () {
        this.mainPanel = Ext.create('Mvp.gui.PortalBorderContainer', {
            region: 'center',
            avPanel: this.avPanel
        });

        var sp = Mvp.search.SearchParams;
        var searchParams = [
            sp.getSearch('CAOMDB'),
            sp.getSearch('DataScope')
        ];
        var devSearchParams = [
            sp.getSearch('CAOM'),
            sp.getSearch('ADS'),
            sp.getSearch('CSV'),
            sp.getSearch('CAOMDownload'),
            sp.getSearch('GalexObjects'),
            sp.getSearch('GalexSdss'),
            sp.getSearch('GalexTiles'),
            sp.getSearch('HLA'),
            sp.getSearch('HLSP'),
            sp.getSearch('STPR'),
            sp.getSearch('HSTPR'),
            sp.getSearch('RP'),
            sp.getSearch('SANTA'),
        //sp.getSearch('SedDiscovery'), // service is broken as far as I can tell
            sp.getSearch('SedAvailability'),
            sp.getSearch('SedRetrieval'),
            sp.getSearch('SID'),
            sp.getSearch('STP'),
            sp.getSearch('STPR'),
			sp.getSearch('VOTable')
        ];
        if (AppConfig.isDevelopment) searchParams = searchParams.concat(devSearchParams);
        var searchPanel = Ext.create('Mast.view.MastTopBar', {
            searchParams: searchParams,
            defaultSearch: 'CAOMDB',
            versionString: Mast.Version.versionString()
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

        // Try adding a new tab with startup info.
        var src = AppConfig.startPage || 'data/html/Start Page.html';

        var iFrame = Ext.create('Mvp.gui.IFrame', {
            src: src,
            width: 600
        });
        var startPanel = Ext.create('Ext.panel.Panel', {
            title: 'Start Page',
            layout: 'fit',
            padding: 20,
            width: 600,
            items: [iFrame],
            closable: true
        });
        this.resultPanel.add(startPanel);
    },

    contextAdded: function () {
    },

    contextRemoved: function () {

    }
});