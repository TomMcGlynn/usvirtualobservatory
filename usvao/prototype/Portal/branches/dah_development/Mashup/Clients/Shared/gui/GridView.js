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
            icon: '../Shared/img/add_basket.png',
            scale: 'medium',
            tooltip: 'Add to Download Basket',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.addPressed,
            disabled: true
        });

        this.exportButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/exp_24x24.png',
            scale: 'medium',
            tooltip: 'Export the Data in the Table to a Local File',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.exportPressed
        });

        this.batchSearchButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/srchvo_24x24.png',
            scale: 'medium',
            tooltip: 'Positional Search All Selected Services',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.batchSearchPressed,
            disabled: true
        });

        this.searchThreeBox = Ext.create('Mvpd.view.VaoPositionSearchPanel', {});
        this.searchThreeBox.on('searchInitiated', this.batchInvokeSearch, this);
        this.batchSearchWindow = Ext.create('Ext.window.Window', {
            title: 'Search All Selected Resources',
            layout: 'fit',
            plain: true,
            closable: true,
            closeAction: 'hide',    
            items: this.searchThreeBox
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

    getBatchSearchButton: function () {
        return this.batchSearchButton;
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
                'itemmouseenter': function (obj, record, item, index, e, eOpts) {
                    this.selModel.suspendEvents()
                },
                'itemmouseleave': function (obj, record, item, index, e, eOpts) {
                    this.selModel.resumeEvents()
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
                    icon: '../Shared/img/about_24x24.png',
                    scale: 'medium',
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
    },

    batchSearchPressed: function () {
        this.MAX_BATCH = 500;
        var records = this.lastStore.getSelectedRecords();
        var len = records.length;
        if (!len) return;

        var collection = this.controller.searchParams.uid;
        if (collection == 'REGKEYWORD') {
            var records = this.lastStore.getSelectedRecords();
            var len = records.length;
            if (!len) {
                Ext.Msg.show({ msg: 'No resources selected for positional search.', title: 'Warning' })
            }

            var nonServiceResources = '';
            var validServiceRecords = false;
            for (var i = 0; i < len; i++) {
                var data = records[i].data;
                var serviceTypeTag = undefined;
                if (data.capabilityClass == 'SimpleImageAccess' ||
                    data.capabilityClass == 'ConeSearch' ||
                    data.capabilityClass == 'SimpleSpectralAccess') {
                    validServiceRecords = true;
                }
                else {
                    nonServiceResources = nonServiceResources + data.shortName + ' ';
                }
            }

//            if (nonServiceResources.length > 0) {
//                Ext.Msg.show({ msg: 'At least one of the selected resources cannot be positionally searched and will be skipped: ' + nonServiceResources, title: 'Warning' });
//            }
//            if (len > this.MAX_BATCH) {
//                Ext.Msg.show({ msg: 'Too many resources selected. Positional searches will only be made for the first ' + this.MAX_BATCH + ' resources.', title: 'Warning' });
//            }

            if (validServiceRecords) {
                this.batchSearchWindow.show();
            }
            else {
                Ext.Msg.show({ msg: 'No selected resources can be positionally searched.', title: 'Warning' });
            }
        }
    },

    //some duplicate processing, but avoids keeping a store or processing more detailed searchability info twice.
    batchInvokeSearch: function (textValues) {
        this.batchSearchWindow.hide();

        var resourceList = [];
        var recordIndex = 0;
        var records = this.lastStore.getSelectedRecords();
        var len = records.length;

        while (resourceList.length < this.MAX_BATCH && recordIndex < len) {
            var data = records[recordIndex].data;
            var serviceTypeTag = undefined;
            if (data.capabilityClass == 'SimpleImageAccess') {
                serviceTypeTag = 'Siap';
            }
            else if (data.capabilityClass == 'ConeSearch') {
                serviceTypeTag = 'Cone';
            }
            else if (data.capabilityClass == 'SimpleSpectralAccess') {
                serviceTypeTag = 'Ssap';
            }

            if (serviceTypeTag) {
                var fixedUrl = Mvp.util.Util.fixAccessUrl(data.accessURL);
                
                var publisher = data.publisher;
                if (publisher.match(/^Space Telescope Science/)) {
                    publisher = 'MAST';  // Use MAST as the publisher for all MAST resources.
                }
                var record = {
                    serviceId: serviceTypeTag,
                    shortName: data.shortName,
                    publisher: publisher,
                    capabilityClass: data.capabilityClass,
                    title: data.title,
                    description: data.description,
                    invokeBaseUrl: null,
                    extraInput: { url: fixedUrl }
                };
                resourceList.push(record);
            }
            recordIndex++;
        }

        if (resourceList.length > 0) {
            Mvp.search.Summary.invokeSummaryQuery(this.controller, textValues[0], resourceList);
        }
    }
});