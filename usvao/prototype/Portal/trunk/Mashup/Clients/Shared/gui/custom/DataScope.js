Ext.define('Mvp.gui.custom.DataScope', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.DatascopeDetailsContainer',
        'Mvp.util.Constants',
        'Mvp.gui.DetailsWindow',
        'Mvp.util.Constants'
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
        for (var c in columns) {
            var col = columns[c];
            var index = col.dataIndex;
            if (index == 'waveband') {
                col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
            }
            else if (index == 'capabilityClass') {
                col.tdCls = 'action-align-middle';
                col.renderer = Mvp.custom.FullSearch.categoryIconRenderer;
            }
            
            // Commented code showing how to override the default column tooltip
            //
            //var colinfoProps = col.ExtendedProperties;
            //if(colinfoProps){
            //    col.tip = "<table>";
            //    if(colinfoProps["vot.datatype"]){
            //        col.tip += '<tr><td>Data type:</td><td>' + colinfoProps["vot.datatype"] + '</td></tr>';
            //    }
            //    col.tip += "</table>";
            //}            
        }

        // Add action column
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_MEDIUM,
            align: 'center',
            tdCls: 'action-align-middle',
            renderer: Mvp.custom.Generic.gridWhitespace,
            items: [{
                icon: Mvp.util.Constants.GOTO_DATA_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Continue to Data',
                iconCls: 'icon-align',
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    Mvp.custom.FullSearch.loadRecords(record, this.controller)
                },
                scope: this
                
            }, {
                icon: Mvp.util.Constants.ABOUT_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Show Details',
                iconCls: 'icon-align',
                handler: function (grid, rowIndex, colIndex, item, e, record) {
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
            context: this.controller,
            listeners: {
                itemmousedown: function (obj, record, item, index, e, eOpts) {
                    this.selModel.suspendEvents();
                    this.selModel.select(record, false, true);
                    this.selModel.resumeEvents();
                }
            },
            scope: this
        });
        
        return grid;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('title'),
            detailsContainer = Ext.create('Mvpc.view.DatascopeDetailsContainer', {
                record: record,
                controller: this.controller
            }),
            type = record.get('capabilityClass'),
            icon;

        if (type == 'ConeSearch') {
            icon = Mvp.util.Constants.CATALOG_ICON['small'];
        } else if (type == 'SimpleImageAccess') {
            icon = Mvp.util.Constants.IMAGE_ICON['small'];
        } else if (type == 'SimpleSpectralAccess') {
            icon = Mvp.util.Constants.SPECTRA_ICON['small'];
        }

        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            icon: icon,
            content: detailsContainer
        });
    }

});