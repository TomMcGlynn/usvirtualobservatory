Ext.define('Mvp.gui.custom.Caom', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvp.gui.AlbumView',
        'Mvp.custom.Caom',
        'Ext.tab.Panel',
        'Mvp.custom.Caom',
        'Mvpc.view.CaomDetailsContainer',
        'Mvp.custom.Generic'
    ],
    // Private methods

    // Override
    createGridPanel: function (config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            title: 'Table View',
            overrideCreateGrid: { fn: this.createGrid, scope: this },
            gridConfig: {
            },
            controller: this.controller,
            region: 'center',     // center region is required, no width/height specified
            collapsible: false
        });

        return grid;
    },

    // Provided to the GridView to tell it how to create the grid.
    createGrid: function (updateObject) {

        // Add custom renderers.
        var columnInfo = Ext.clone(updateObject.columnInfo);
        var columns = columnInfo.columns;
        for (c in columns) {
            var col = columns[c];
            if (col.ExtendedProperties && col.ExtendedProperties.isDate) col.renderer = Mvp.custom.Generic.dateRenderer;
            
            var index = col.dataIndex;
            if (index == 'wavelength_region') {
                col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
            }
        }

        // Add extra columns
        var previewColumn = { text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);

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

    createCenterPanel: function (config) {
        var gridPanel = this.createGridPanel(config);
        this.grid = gridPanel;  // needed by superclass for refreshing the grid on filtering

        var albumPanel = Ext.create('Mvp.gui.AlbumView', {
            title: 'Album View',
            controller: this.controller,
            imagePanelTemplate: Mvp.custom.Caom.dataviewTemplate()
        });

        var centerPanel = Ext.create('Ext.tab.Panel', {
            closable: false,
            layout: 'fit',
            cls: 'x-btn-text-icon',
            items: [gridPanel, albumPanel]
        });

        centerPanel.region = 'center';
        return centerPanel;
    },

    createDetailsPanel: function (view, record, htmlElement, index, e) {
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('target_name');
        var detailsContainer = Ext.create('Mvpc.view.CaomDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }

});