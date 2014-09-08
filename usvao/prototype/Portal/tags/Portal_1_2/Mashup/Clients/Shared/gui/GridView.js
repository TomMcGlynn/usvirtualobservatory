Ext.define('Mvp.gui.GridView', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mvp.grid.MvpGrid'
    ],

    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        delete config.controller;
        
        // Save any configs for the grid.  These will not be used if overrideCreateGrid
        // is specified.
        this.gridConfig = config.gridConfig;
        delete config.gridConfig;
        
        // Allow the creator to override the createGrid function for customization.
        if (config.overrideCreateGrid && Ext.isFunction(config.overrideCreateGrid.fn)) {
            this.overrideCreateGrid = config.overrideCreateGrid;
            delete config.overrideCreateGrid;
            
            var overrideFn = this.overrideCreateGrid.fn;
            var scope = this.overrideCreateGrid.scope;
            this.createGrid = function(updateObject) {
                var grid = overrideFn.call(scope, updateObject);
                return grid;
            }
        } else {
            this.createGrid = this.baseCreateGrid;
        }
        
        config.layout = 'fit';
        this.callParent(arguments);
    },
    
    // Public methods
     
    updateView: function(updateObject) {
        // If there's a store, create the Grid
        if (updateObject.store) {
            if (updateObject.store !== this.lastStore) {
                // We only need to refresh the grid if this is a different store than last time.
                
                // Remove the old grid, if any.
                if (this.grid) {
                    this.remove(this.grid);
                }

                this.grid = this.createGrid(updateObject);
                this.add(this.grid);
            }
            this.lastStore = updateObject.store;
        }
    },
    
    refreshScrollers: function() {
        // Force the scroller to refresh.
        if (this.grid) {
            this.grid.determineScrollbars();
            this.grid.invalidateScroller();
        }
        
        // Force any other parts of the view to refresh as needed.
        this.fireEvent('storeupdated', this.lastUpdateObject);
    },
    
    // Private methods
    
    baseCreateGrid: function(updateObject) {
        var config = {
            store: updateObject.store,
            numberRows: true,
            columnInfo: updateObject.columnInfo
        };
        if (this.gridConfig) {
            Ext.apply(config, this.gridConfig);
        }
        var grid = Ext.create('Mvp.grid.MvpGrid', config);
        
        return grid;
    }
    
    
});