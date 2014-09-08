Ext.define('Mvp.gui.custom.GalexTiles', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: ['Mvp.custom.GalexTiles',
        'Mvpc.view.GenericDetailsContainer',
        'Mvp.util.Constants'
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
        var accessColumn, status;

        // Add extra columns
        var previewColumn = { text: 'Preview', dataIndex: 'fullResColorJpeg', renderer: Mvp.custom.GalexTiles.previewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_MEDIUM,
            align: 'center',
            tdCls: 'action-align-middle',
            renderer: Mvp.custom.Generic.gridWhitespace,
            items: [{
                icon: '../Shared/img/exp_24x24_up.png',
                scale: 'medium',
                tooltip: 'Go to Download Page',
                iconCls: 'icon-align',
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    var url = Mvp.custom.GalexTiles.urlGenerator(record.get('tilename'), record).url;
                    window.open(url, '_blank');
                },
                scope: this

            }, {
                icon: '../Shared/img/about_24x24.png',
                scale: 'medium',
                tooltip: 'Show Details',
                iconCls: 'icon-align',
                handler: function (view, rowIndex, colIndex, item, e, record) {
                    this.createDetailsPanel(record);
                },
                scope: this
            }]
        };
        columns.splice(0, 0, actionColumn);

        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo,
            context: this.controller
        });
        return grid;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + record.get('tilename');
        var detailsContainer = Ext.create('Mvpc.view.GenericDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }
});





