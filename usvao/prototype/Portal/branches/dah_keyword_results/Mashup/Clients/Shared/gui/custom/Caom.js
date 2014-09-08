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
        'Mvp.custom.Generic',
        'Mvp.util.Constants'
    ],
    // Private methods

    // Override
    createGridPanel: function (config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            title: 'Table View',
            overrideCreateGrid: { fn: this.createGrid, scope: this },
            overrideCreateDetailsPanel: { fn: this.createDetailsPanel, scope: this },
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
            var index = col.dataIndex;
            if (index == 'wavelength_region') {
                col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
            }
        }

        // Add extra columns
        var previewColumn = { text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);
            // Add action column for most generic grid, but have to ensure only create it once
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_SMALL,
            items: [{
                icon: '../Shared/img/about_24x24.png',
                scale: 'medium',
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

    createCenterPanel: function (config) {
        var gridPanel = this.createGridPanel(config);
        this.grid = gridPanel;  // needed by superclass for refreshing the grid on filtering

        var albumPanel = Ext.create('Mvp.gui.AlbumView', {
            title: 'Album View',
            controller: this.controller,
            hidden: true,  //  Disable Album view until it works.
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

    createDetailsPanel: function (record) {
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