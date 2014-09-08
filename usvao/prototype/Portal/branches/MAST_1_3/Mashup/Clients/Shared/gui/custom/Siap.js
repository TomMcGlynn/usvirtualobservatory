Ext.define('Mvp.gui.custom.Siap', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.SiaDetailsContainer'
    ],
    // Private methods

    // Override
    createGridPanel: function (config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            overrideCreateGrid: { fn: this.createGrid, scope: this },
            overrideCreateDetailsPanel: { fn: this.createDetailsPanel, scope: this },
            gridConfig: {
            },
            controller: this.controller,
            region: 'center',     // center region is required, no width/height specified
            // width: 600,
            collapsible: false
        });

        return grid;
    },

    createGrid: function (updateObject) {
        // Add custom renderers.
        var columnInfo = Ext.clone(updateObject.columnInfo);
        var columns = columnInfo.columns;
        /*
        for (c in columns) {
        var col = columns[c];
        if (col.dataIndex == 'waveband') {
        col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
        }
        }
        */

        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo,
            context: this.controller
        });
        grid.on('itemclick', this.createDetailsPanel, this);

        return grid;
    },

    createDetailsPanel: function (view, record, htmlElement, index, e) {
        var title = 'Details: ' + this.controller.getTitle();
        title += record.get('Survey') ? ' - ' + record.get('Survey') : '';
        var detailsContainer = Ext.create('Mvpc.view.SiaDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }

});