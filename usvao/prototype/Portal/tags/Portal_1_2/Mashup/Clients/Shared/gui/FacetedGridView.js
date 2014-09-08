Ext.define('Mvp.gui.FacetedGridView', {
    extend: 'Mvp.gui.ResultViewPanel',
    requires: [
        'Mvp.gui.FilterView'
    ],
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.controller = config.controller;
        this.setupPanels(config);
               
        // These are the components we'll pass to the parent constructor.
        var items = [
            this.westPanel,
            this.centerPanel
        ];
    
       // Apply non-overridable config items.  Make this thing a border panel.      
        Ext.apply(config, {
            margin: 0,
            layout: 'border',
            items: items
        });
        
        this.callParent(arguments);
        
        // Necessary for telling the parent where to add the grid.
        this.gridContainer = this.centerPanel;
    },
    
    // Public methods
    
    // Private Methods
    setupPanels: function(config) {
        // Initialize the component panels.
        this.northPanel = null;
        this.southPanel = null;
        this.eastPanel = null;
        this.westPanel = this.filterPanel = this.createFilterPanel(config);
        this.filterPanel.on('filterApplied', this.filterApplied, this);

        this.centerPanel = this.createCenterPanel(config);

    },
    
    filterApplied: function(filters, store) {
        // Force the scroller to refresh.
        if (this.grid) {
            this.grid.refreshScrollers();
        }
        
        // Force any other parts of the view to refresh as needed.
        this.fireEvent('storeupdated', this.lastUpdateObject);
    },
    
    createFilterPanel: function(config) {
        var filterPanel = Ext.create('Mvp.gui.FilterView', {
            facetValueExclude: config.facetValueExclude,
            controller: config.controller,
            title: 'Filters',
            region:'west',
            layout: 'fit',
            floatable: false,
            //width: 270,
            collapsible: true,   // make collapsible
            autoScroll: false,
            resizable: true
            //margins: '0 10 0 0'
        });

        filterPanel.on('filterApplied', this.filterApplied, this);
        filterPanel.on('resize', function () {
            var size = filterPanel.getSize();
            filterPanel.suspendEvents();
            filterPanel.setSize(size.width + 10, size.height);
            filterPanel.setSize(size.width, size.height);
            filterPanel.resumeEvents();
        }, this);
        filterPanel.setWidth(300); // Ext bug workaround?
        return filterPanel;
    },
    
    createCenterPanel: function(config) {
        this.grid = this.createGridPanel(config);
        var centerPanel = this.grid;
        centerPanel.region = 'center';
        return centerPanel;
    },
    
    createGridPanel: function(config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            gridConfig: {
            },
            controller: this.controller,
           // width: 600,
            collapsible: false
        });
        
        return grid;
    }
    
});