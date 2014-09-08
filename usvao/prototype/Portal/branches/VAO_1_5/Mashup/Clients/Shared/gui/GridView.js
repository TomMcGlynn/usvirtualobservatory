Ext.define('Mvp.gui.GridView', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mvp.grid.MvpGrid',
        'Mvpc.view.GenericDetailsContainer',
        'Mvp.gui.DetailsWindow',
        'Mvpd.view.ExportToWindowCombos',
        'Ext.grid.RowNumberer',
        'Mvpd.view.VaoPositionSearchPanel',
        'Mvp.util.Util',
        'Mvp.util.Constants',
        'Mvp.app.DownloadBasket'
    ],

    /**
    * @cfg {Number} pagesize
    * The number of items to have on one page in the grid display.
    */

    constructor: function (config) {
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        delete config.controller;

        // Save any configs for the grid.  These will not be used if overrideCreateGrid
        // is specified.
        this.gridConfig = config.gridConfig;
        delete config.gridConfig;

        // Allow the creator to override the createGrid function for customization.
        if (config.overrideCreateGrid && Ext.isFunction(config.overrideCreateGrid.fn)) {
            this.overrideCreateGrid = config.overrideCreateGrid;
            delete config.overrideCreateGrid;

            var overrideFn = this.overrideCreateGrid.fn;
            var scope = this.overrideCreateGrid.scope;
            this.createGrid = function (updateObject) {
                var grid = overrideFn.call(scope, updateObject);
                return grid;
            }
        } else {
            this.createGrid = this.baseCreateGrid;
        }

        this.addButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.ADD_TO_BASKET_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Add to Download Basket',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.addPressed,
            disabled: true
        });

        this.exportButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.EXPORT_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Export the Data in the Table to a Local File',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.exportPressed
        });

        config.layout = 'fit';
        this.callParent(arguments);
    },

    // Public methods

    updateView: function (updateObject) {
        // If there's a store, create the Grid
        if (updateObject.store) {
            if (updateObject.store !== this.lastStore) {
                // We only need to refresh the grid if this is a different store than last time.

                // Remove the old grid, if any.
                if (this.grid) {
                    this.remove(this.grid, true);  // The true makes sure the grid is destroyed
                }

                this.grid = this.createGrid(updateObject);
                this.add(this.grid);
            }
            this.lastStore = updateObject.store;
        }
    },

    getExportButton: function () {
        return this.exportButton;
    },

    getAddButton: function () {
        return this.addButton;
    },

    getExportTable: function (filtercolumns) {
        var table = null;
        if (this.grid) {
            table = this.grid.getExportTable(filtercolumns);
        }
        return table;
    },

    // Private methods

    baseCreateGrid: function (updateObject) {
        var config = {
            store: updateObject.store,
            numberRows: true,
            columnInfo: updateObject.columnInfo,
            context: this.controller,
            listeners: {
                itemmousedown: function (obj, record, item, index, e, eOpts) {
                    if (obj.selModel.$className.match(/checkbox/i) !== null) return;
                    this.selModel.suspendEvents();
                    this.selModel.select(record, false, true);
                    this.selModel.resumeEvents();
                }
            },
            scope: this
        };
        var columnInfo = updateObject.columnInfo, //Ext.clone(updateObject.columnInfo);
            columns = columnInfo.columns;
        for (var c in columns) {
            var col = columns[c];
            if (col.dataIndex == 'html_file') col.renderer = Mvp.custom.Generic.fixAnchorTags
        }
        if (columns[0].xtype != 'actioncolumn') {
            // Add action column for most generic grid, but have to ensure only create it once
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
                    icon: Mvp.util.Constants.ABOUT_ICON[Mvp.util.Constants.ICON_SIZE],
                    scale: Mvp.util.Constants.ICON_SIZE,
                    tooltip: 'Show Details',
                    iconCls: 'icon-align',
                    handler: function (grid, rowIndex, colIndex, item, e, record) {
                        this.createDetailsPanel(undefined, record);
                    },
                    scope: this
                }]
            };
            columns.splice(0, 0, actionColumn);
        }
        if (this.gridConfig) {
            Ext.apply(config, this.gridConfig);
        }
        var grid = Ext.create('Mvp.grid.MvpGrid', config);

        return grid;
    },

    createDetailsPanel: function (view, record, htmlElement, index, e) {
        var title = 'Details: ' + this.controller.getTitle(),
            detailsContainer = Ext.create('Mvpc.view.GenericDetailsContainer', {
            record: record,
            controller: this.controller
        }),
            w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    },

    exportPressed: function () {
        // Replace title characters that could be problematic in a filename, then remove duplicate underscores.
        var title = this.controller.getTitle();

        // Specify the file type and name.
        var filetype = 'csv';
        var filename = Mvp.util.Util.filenameCreator(title, filetype);

        // Present the user dialog so they can select a file type and modify the file name.
        var exportWindow = Ext.create('Mvpd.view.ExportToWindowCombos', {
            grid: this
        });
        exportWindow.setFilename(filename);
        exportWindow.setFiletype(filetype);
        exportWindow.show();

    },

    addPressed: function () {
        var records = this.lastStore.getSelectedRecords();
        var len = records.length;
        if (!len) return;

        var collection = this.controller.searchParams.uid;  //CAOM, CAOMDB etc., will need later
        if (collection == 'CAOMDB') collection = 'CAOM';
        var changed,
            obsid,
            field = (collection == 'CAOM') ? 'obsID' : 'Z_JOBNUM';

        Mvp.app.DownloadBasket.add(collection, records, field);

        var basket = Mvp.app.DownloadBasket.downloadBasket;
        var caom = basket ? basket['CAOM'] : undefined,
            dads = basket ? basket['SIDBYJOB'] : undefined;
        if ((!caom && !dads) || !changed) {
            this.fireEvent('APP.download.view', {
                type: 'APP.download.view'
            })
            //return;
        }
        if (caom) {
            var args = {
                obsid: caom || '',
                inputText: caom || ''
            };
            var searchParams = Mvp.search.SearchParams.getSearch('CAOMDownload');
            this.controller.invokeSearch(args, searchParams);
        }
        this.fireEvent('APP.context.DownloadBasket.changed', {  // alert TopBarContainer
            type: 'APP.context.DownloadBasket.changed',
            context: this.controller
        });
    }
});