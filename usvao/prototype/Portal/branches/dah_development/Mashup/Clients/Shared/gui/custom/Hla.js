Ext.define('Mvp.gui.custom.Hla', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvp.gui.AlbumView',
        'Mvp.custom.Caom',
        'Ext.tab.Panel',
        'Mvp.custom.Caom',
        'Mvpc.view.HlaDetailsContainer',
        'Mvp.custom.Generic',
        'Mvp.custom.Hla',
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
        for (c in columns) {
            var col = columns[c];
            var index = col.dataIndex;
        }

        // Add extra columns
        var previewColumn = { text: 'Preview', dataIndex: 'URL', renderer: Mvp.custom.Hla.hlaPreviewRenderer, width: 134 };
        columns.splice(0, 0, previewColumn);
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_SMALL,
            align: 'center',
            tdCls: 'action-align-middle',
            renderer: Mvp.custom.Generic.gridWhitespace,
            items: [{
                icon: '../Shared/img/savas_24x24.png',
                scale: 'medium',
                tooltip: 'Download File',
                iconCls: 'icon-align',
                handler: function (view, rowIndex, colIndex, item, e, record) {
                    Mvp.custom.Hla.downloadRecords(record, this.controller);
                },
                scope: this
            }, {
                icon: '../Shared/img/about_24x24.png',
                scale: 'medium',
                tooltip: 'Show Details',
                iconCls: 'icon-align',
                handler: function (view, rowIndex, colIndex, item, e, record) {
                    this.createDetailsPanel(record, this.controller);
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
            imagePanelTemplate: Mvp.custom.Caom.dataviewTemplate()
        });

        var centerPanel = Ext.create('Ext.tab.Panel', {
            closable: false,
            layout: 'fit',
            cls: 'x-btn-text-icon',
            items: gridPanel //[gridPanel, albumPanel]
        });

        centerPanel.region = 'center';
        return centerPanel;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('Target');
        var detailsContainer = Ext.create('Mvpc.view.HlaDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }

});