Ext.define('Mvp.gui.FacetedGridView', {
    extend: 'Mvp.gui.ResultViewPanel',
    requires: [
        'Mvp.gui.FilterView',
        'Mvp.gui.AvItems',
        'Mvp.gui.GridView'
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
        
        // These are extra items that will be put in the top bar.
        var extraItems = this.createExtraItems(config);
    
       // Apply non-overridable config items.  Make this thing a border panel.      
        Ext.apply(config, {
            margin: 0,
            layout: 'border',
            items: items,
            extraItems: extraItems
        });
        
        this.callParent(arguments);
        
        // Necessary for telling the parent where to add the grid.
        this.gridContainer = this.centerPanel;
    },
    
    // Public methods
    
    updateView: function(updateObject) {
        this.callParent(arguments);
        this.lastUpdateObject = updateObject;
    },
    
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
    
    createExtraItems: function(config) {
        var me = this;
        
        // AstroView buttons
        if (this.controller.useAv) {
            var avDock = Ext.create('Mvp.gui.AvItems', {
                controller: this.controller
            });
            var avItems = avDock.getItems();
            avDock.on('avtoggled', function(newValue, oldValue) {
                this.controller.setAvEnabled(newValue);
            });
            avDock.on('colorChanged', function(newColor) {
                this.controller.setAvColor(newColor);
            });
            this.controller.setAvColor(avDock.getColor());
        }
        
        // Export button.
        var exportButton = this.grid.getExportButton();

        var extraItems = [];
        if (avItems) {
            extraItems = extraItems.concat(avItems);
        }
        extraItems.push(exportButton);
        
        return extraItems;
    },
    
    filterApplied: function(filters, store) {
        // Force the scroller to refresh.
        if (this.grid) {
            this.grid.refreshScrollers();
        }
        
        // Force any other parts of the view to refresh as needed.
        this.controller.update(this.lastUpdateObject);
    },
    
    createFilterPanel: function(config) {
        var filterPanel = Ext.create('Mvp.gui.FilterView', {
            exclude: config.facetValueExclude,
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