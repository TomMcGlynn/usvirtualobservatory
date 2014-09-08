Ext.define('Mvp.gui.custom.Summary', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: ['Mvp.custom.Summary',
               'Mvp.search.Summary',
               'Mvpc.view.SummaryDetailsContainer',
               'Mvp.custom.FullSearch',
               'Mvp.custom.Hst',
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
        for (c in columns) {
            var col = columns[c];
            var index = col.dataIndex;
            col.tdCls = 'action-align-middle';  // for summaries, vertically center all the columns
            
            if (index == 'access') {
                col.renderer = Mvp.custom.Summary.accessRenderer;
            } 
            else if (index == 'capabilityClass') {
                col.renderer = Mvp.custom.FullSearch.categoryIconRenderer;
            } 
            else if (index == 'Publisher') {
                col.renderer = Mvp.custom.FullSearch.publisherIconRenderer;
            }
            else if (index == 'Status') {
                col.renderer = Mvp.custom.Summary.statusRenderer;
            } else if (index == 'invokeBaseUrl') {
                col.xtype = 'actioncolumn';
                col.menuDisabled = true;
                col.width = Mvp.util.Constants.ACTION_COLUMN_WIDTH_MEDIUM;
                col.align = 'center';
                col.tdCls = 'action-align-middle';
                col.renderer = Mvp.custom.Generic.gridWhitespace;
                col.items = [{
                    icon: '../Shared/img/exp_24x24_up.png',
                    scale: 'medium',
                    tooltip: 'Load Resource',
                    iconCls: 'icon-align',
                    handler: function (grid, rowIndex, colIndex, item, e, record) {
                        var store = grid.store;
                        var record = store.getAt(rowIndex);
                        var count = record.get('Records Found');
                        if (count == 0) {
                            Ext.Msg.alert('Warning', 'No records available');
                            var task = new Ext.util.DelayedTask(function () { Ext.Msg.close() });
                            task.delay(1500);
                        } else {
                            var serviceId = record.get('serviceId');
                            var invokeBaseUrl = record.get('invokeBaseUrl');
                            var requestJson = record.get('requestJson');
                            var context = grid.panel.context;
                            Mvp.search.Summary.invokeMashupQuery(context, serviceId, invokeBaseUrl, requestJson);
                        }
                    },
                    scope: this
                }, {
                    icon: '../Shared/img/about_24x24.png',
                    scale: 'medium',
                    tooltip: 'Show Details',
                    iconCls: 'icon-align',
                    handler: function (grid, rowIndex, colIndex, item, e, record) {
                        var store = grid.store;
                        var record = store.getAt(rowIndex);
                        this.createDetailsPanel(record);
                    },
                    scope: this
                }]
            } else if ((index == 'Records Found')|| (index == 'Short Name') ||(index == 'Title')) {
                // Default text size to 13.
                col.renderer = Mvp.custom.Hst.font13Renderer;
            }
        }

        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo,
            context: this.controller,
            listeners: {
                'itemmouseenter': function (obj, record, item, index, e, eOpts) {
                    this.selModel.suspendEvents()
                },
                'itemmouseleave': function (obj, record, item, index, e, eOpts) {
                    this.selModel.resumeEvents()
                }
            },
            scope: this
        });

        return grid;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + record.get('Title');
        var detailsContainer = Ext.create('Mvpc.view.SummaryDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }
 
 });





