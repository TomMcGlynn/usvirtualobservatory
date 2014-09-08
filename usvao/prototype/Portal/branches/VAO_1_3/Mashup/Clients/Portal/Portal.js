/**
 * Documentation for VAO Portal
 */

// This class needs to have the requires statements outside the Ext.define() because
// this class is loaded by the top-level html, and the internal requires statement
// seems to cause lazy initialization of the class.
Ext.require('Vao.view.VaoTopBar');
Ext.require('Vao.Version');
Ext.require('Mvp.util.BasePortal');
Ext.require('Mvp.gui.IFrame');

Ext.define('Vao.Portal', {
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
            sp.getSearch('CSV'),
            sp.getSearch('DataScopeVao'),
            sp.getSearch('DataScope'),
            sp.getSearch('SANTA'),
            sp.getSearch('SedAvailability'),
            sp.getSearch('SedRetrieval'),
			sp.getSearch('VOTable')
        ];
        var searchPanel = Ext.create('Vao.view.VaoTopBar', {
            searchParams: searchParams,
            defaultSearch: 'DataScopeVao',
            versionString: Vao.Version.versionString()
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
		//var src = AppConfig.startPage || 'data/Start Page Down Time.html';
		var src = AppConfig.startPage || 'http://www.usvao.org/documents/DataDiscoveryTool/StartPage/ddt_start.html';
		
		var iFrame = Ext.create('Mvp.gui.IFrame', {
			src: src
		});
		var startPanel = Ext.create('Ext.panel.Panel', {
			title:  'Start Page',
			layout: 'fit',
			items: [iFrame],
			closable: true
		});
		this.resultPanel.add(startPanel);
    },

    contextAdded: function () {
        if (!window.properHeight) {
            window.properHeight = true;
            window.onresize = undefined;
            this.searchPanel.setHeight(90);
        }
    },

    contextRemoved: function () {
    }

});