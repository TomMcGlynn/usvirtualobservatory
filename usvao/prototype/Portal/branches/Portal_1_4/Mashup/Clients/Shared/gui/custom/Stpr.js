Ext.define('Mvp.gui.custom.Stpr', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.StprDetailsContainer'
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
        var previewColumn = { text: 'Preview', dataIndex: 'resourceurl', renderer: Mvp.custom.Hst.stprPreviewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);
        for (c in columns) {
            var col = columns[c];
            if (col.dataIndex == 'title') {
                col.renderer = Mvp.custom.Hst.font15Renderer
            } else if (col.dataIndex == 'descriptions') {
                col.renderer = Mvp.custom.Hst.font13Renderer;
            }
        }

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
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('title');
        var detailsContainer = Ext.create('Mvpc.view.StprDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }

});