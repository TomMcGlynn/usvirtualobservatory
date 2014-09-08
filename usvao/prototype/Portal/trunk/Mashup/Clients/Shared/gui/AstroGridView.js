Ext.define('Mvp.gui.AstroGridView', {
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
            
            var crossMatchButton = this.grid.getCrossMatchButton();
            extraItems.push(crossMatchButton);
        }
        extraItems.push(exportButton);

        return extraItems;
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