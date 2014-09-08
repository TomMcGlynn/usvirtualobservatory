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

    constructor: function (config) {
        this.controller = config.controller;
        this.controller.position = this.controller.searchParams.position;
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

    updateView: function (updateObject) {
        this.callParent(arguments);
        this.lastUpdateObject = updateObject;
        if (updateObject.complete && this.avDock && !this.complete) {
            if (updateObject.rowCount < 10000) {
                this.complete = true;
                this.avDock.selectionPicker.setValue('ALL');
            }
        }
    },

    // Private Methods
    setupPanels: function (config) {
        // Initialize the component panels.
        this.northPanel = null;
        this.southPanel = null;
        this.eastPanel = null;
        this.westPanel = this.filterPanel = this.createFilterPanel(config);

        this.centerPanel = this.createCenterPanel(config);

    },

    createExtraItems: function (config) {
        var me = this;

        // AstroView buttons
        if (this.controller.useAv) {
            var avDock = Ext.create('Mvp.gui.AvItems', {
                controller: this.controller
            });
            var avItems = avDock.getItems();
            avDock.on('avmodechanged', function (newValue, oldValue) {
                this.controller.setAvMode(newValue);
            });
            avDock.on('colorChanged', function (newColor) {
                this.controller.setColor(newColor);
            });
            this.controller.setColor(avDock.getColor());
            this.avDock = avDock;
        }

        var extraItems = [];

        // Export button.
        var exportButton = this.grid.getExportButton();

        if (avItems) {
            extraItems = extraItems.concat(avItems);
        }
        if (this.controller.searchParams.downloadEnabled) {
            var addButton = this.grid.getAddButton();
            var basketButton = this.grid.getBasketButton();
            extraItems.push(addButton, basketButton);
        }
        extraItems.push(exportButton);

        return extraItems;
    },

    filterApplied: function (filters, store) {
        // Force any other parts of the view to refresh as needed.
        this.controller.update(this.lastUpdateObject);
        this.fireEvent('APP.context.records.filtered', {
            type: 'APP.context.records.filtered',
            context: this.controller
        })
    },

    createFilterPanel: function (config) {
        var filterPanel = Ext.create('Mvp.gui.FilterView', {
            exclude: config.facetValueExclude,
            controller: config.controller,
            title: 'Filters',
            region: 'west',
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

    createCenterPanel: function (config) {
        this.grid = this.createGridPanel(config);
        var centerPanel = this.grid;
        centerPanel.region = 'center';
        return centerPanel;
    },

    createGridPanel: function (config) {
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