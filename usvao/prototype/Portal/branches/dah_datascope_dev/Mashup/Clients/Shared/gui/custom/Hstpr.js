Ext.define('Mvp.gui.custom.Hstpr', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.PressReleaseContainer'
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
        var previewColumn = { text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Hst.pressPreviewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);
        for (c in columns) {
            var col = columns[c];
            if (col.dataIndex == 'title') {
                col.renderer = Mvp.custom.Hst.font15Renderer
            } else if (col.dataIndex == 'descriptions') {
                col.renderer = Mvp.custom.Hst.font13Renderer;
            }
        }
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: 80,
            items: [{
                icon: '../Shared/img/more.png',
                tooltip: 'Show Details',
                getClass: function (obj, metadata, record, rowIndex, colIndex, store) {
                    metadata.css = 'action-align-middle'    //PortalCustomStyles.css
                },
                style: 'margin-left:auto; margin-right:auto;',
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
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('UnnamedField');
        var detailsContainer = Ext.create('Mvpc.view.PressReleaseContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }

});