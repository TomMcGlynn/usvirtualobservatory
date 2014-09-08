/**
 * Documentation for PortalBorderContainer
 */

Ext.define('Mvp.gui.PortalBorderContainer', {
    extend: 'Ext.container.Container',
    requires: {},
    statics: {},

    constructor: function (config) {
        // Initialize the component panels.
        this.northPanel = Ext.create('Ext.panel.Panel', {
            region: 'north',
            layout: 'fit',
            resizable: true,
            border: 'false'
        });
        this.southPanel = null;
        this.eastPanel = null;
        this.westPanel = null;
        //Ext.create('Ext.panel.Panel', {
        //    title: 'Filters',
        //    region:'west',
        //    layout: 'fit',
        //    floatable: false,
        //    width: 270,
        //    collapsible: true,   // make collapsible
        //    autoScroll: true,
        //    margins: '0 10 0 0'
        //});

        // The center panel is actually another border panel with a center tabbed panel that will contain our main result panel,
        // an AstroView panel whose position is configurable (north, south, east or west) within this center panel.
        this.avPanel = Ext.create('Ext.panel.Panel', {
            region: avPlacement,  // avPlacement is a global set up from url params by Shared/InitializeJavaScript.js
            collapsible: true,
            title: 'AstroView',
            collapsed: false,
            floatable: false,
            hidden: !useAV,
            closable: false,
            animCollapse: false,
            height: 300,
            width: 300,
            resizable: true,
            layout: 'fit'
        });
        this.resultPanel = Ext.create('Ext.tab.Panel', {
            region: 'center',
            collapsible: false,
            closable: false
        });
        var items = [this.resultPanel];
        if (AppConfig.useAV) items.push(this.avPanel);
        this.centerPanel = Ext.create('Ext.panel.Panel', {
            region: 'center',     // center region is required, no width/height specified
            layout: 'border',
            items: items
        });

        // These are the components we'll pass to the parent constructor.
        var items = [
            this.northPanel,
        //this.westPanel,
            this.centerPanel
            ];

        // Apply non-overridable config items.       
        Ext.apply(config, {
            margin: 0,
            layout: 'border',
            items: items
        });

        // Apply defaults for overridable config items.       
        Ext.applyIf(config, {
            //width: 1100,
            //height: 790
        });

        // Call the parent constructor to finish initializing this element.
        this.callParent([config]);

    },

    // Public methods

    getNorth: function () { return this.northPanel },
    getWest: function () { return this.westPanel },
    getCenter: function () { return this.centerPanel },
    getAvPanel: function () { return this.avPanel },
    getResultPanel: function () { return this.resultPanel },

    addResultPanel: function (panel, searchContext) {
        this.centerPanel.add(panel);

        // TBD - add more association between searchContext and panel.
    }

});
