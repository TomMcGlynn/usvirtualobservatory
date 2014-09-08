Ext.define('Mvp.gui.GridView', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mvp.grid.MvpGrid',
        'Mvpc.view.GenericDetailsContainer',
        'Mvp.gui.DetailsWindow',
        'Mvpd.view.ExportToWindowCombos',
        'Ext.util.Cookies'
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

        this.basketButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/basket.png',
            tooltip: 'Display download basket',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.basketPressed
        });

        this.addButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/add.png',
            tooltip: 'Add to download basket',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.addPressed
        });

        this.exportButton = Ext.create('Ext.button.Button', {
            text: 'Export Table As...',
            tooltip: 'Export the data in the table to a local file',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
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

    getBasketButton: function () {
        return this.basketButton;
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
            context: this.controller
        };
        if (this.gridConfig) {
            Ext.apply(config, this.gridConfig);
        }
        var grid = Ext.create('Mvp.grid.MvpGrid', config);
        grid.on('itemclick', this.createDetailsPanel, this);

        return grid;
    },

    createDetailsPanel: function (view, record, htmlElement, index, e) {
        var title = 'Details: ' + this.controller.getTitle();
        var detailsContainer = Ext.create('Mvpc.view.GenericDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        }
        );
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

    basketPressed: function () {
        var cookie = Ext.util.Cookies.get('MvpDownloadBasket');
        if (!cookie || cookie.match('CAOM') == -1) return;
        var args = {
            obsid: cookie.substr(cookie.indexOf(' ') + 1),
            title: cookie.substr(0, cookie.indexOf(' ')),
            inputText: cookie.substr(cookie.indexOf(' ') + 1)
        };
        var searchParams = Mvp.search.SearchParams.getSearch('Download');
        this.controller.invokeSearch(args, searchParams);
    },

    addPressed: function () {
        var cookie = Ext.util.Cookies.get('MvpDownloadBasket');
        var collection = this.controller.searchParams.uid;  //CAOM, CAOMDB etc., will need later
        if (collection == 'CAOMDB') collection = 'CAOM';
        var records = this.lastStore.getSelectedRecords();
        var len = records.length;
        if (!len) return;
        if (!cookie) {
            var basket = collection + ' ' + records[0].get('obsID');
            for (var r = 1; r < len; r++) {
                basket += ', ' + records[r].get('obsID');
            }
            Ext.util.Cookies.set('MvpDownloadBasket', basket);
        } else {
            var decodedCookie = cookie;
            var startIndex = decodedCookie.search(collection);
            var header = (startIndex == -1) ? collection + ' ' : ' ';
            var basket = header;
            var first = true;
            for (var r = 0; r < len; r++) {
                var id = records[r].get('obsID');
                var s = ' ' + id;
                if (decodedCookie.indexOf(s) == -1) {
                    basket += (first) ? id : ', ' + id;
                    first = false;
                }
            }
            var start = decodedCookie.substr(0, startIndex + collection.length),
                end = decodedCookie.substr(collection.length + 1);
            if (basket.trim()) basket += ', ';
            var result = (startIndex == -1) ? decodedCookie + ' ' + basket : start + basket + end;
            Ext.util.Cookies.set('MvpDownloadBasket', result);
        }
        cookie = Ext.util.Cookies.get('MvpDownloadBasket');
        this.basketPressed();
    }

});