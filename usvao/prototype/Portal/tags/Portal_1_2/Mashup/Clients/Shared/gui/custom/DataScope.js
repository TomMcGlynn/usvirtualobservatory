Ext.define('Mvp.gui.custom.DataScope', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid'
    ],
    // Private methods
    
    // Override
    createGridPanel: function(config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            overrideCreateGrid: {fn: this.createGrid, scope: this},
            gridConfig: {
            },
            controller: this.controller,
            region: 'center',     // center region is required, no width/height specified
           // width: 600,
            collapsible: false
        });
        
        return grid;
    },
    
    createGrid: function(updateObject) {
        // Add custom renderers.
        var columnInfo = Ext.clone(updateObject.columnInfo);
        var columns = columnInfo.columns;
        for (c in columns) {
            var col = columns[c];
            if (col.dataIndex == 'waveband') {
                col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
            }
        }
        
        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo
        });
        
        return grid;
    }
    
});