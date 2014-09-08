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
        'Mvp.util.Constants',
        'Mvp.app.DownloadBasket'
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

        var columnInfo = Ext.clone(updateObject.columnInfo);
        var columns = columnInfo.columns;
        // Add custom renderers.
        for (c in columns) {
            var col = columns[c];
            var index = col.dataIndex;
            if (index == 'wavelength_region') {
                col.renderer = Mvp.custom.Generic.semicolonRenderer;
            } else if (index == 't_obs_release') {
                col.renderer = Mvp.custom.Caom.releaseDateRenderer;
            }
            col.tdCls = 'x-change-cell'
        }
        // Add extra columns
        var previewColumn = { text: 'Preview', dataIndex: 'jpegURL', renderer: Mvp.custom.Caom.caomPreviewRenderer, width: 134, tdCls: 'x-change-cell' };
        columns.splice(0, 0, previewColumn);
        // Add action column for most generic grid, but have to ensure only create it once
        var controller = this.controller;
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_SMALL,
            align: 'center',
            tdCls: 'action-align-middle x-change-cell',
            renderer: Mvp.custom.Generic.gridWhitespace,
            items: [ {
                icon: Mvp.util.Constants.SAVEAS_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Download Associated Data Now',
                iconCls: 'icon-align',
                handler: function (view, rowIndex, colIndex, item, e, record) {
                    var service = this.controller.searchParams.uid.match('TEST') !== null ? 'Mast.Caomtest.Products' : 'Mast.Caom.Products';
                    var request = {
                        service: service,
                        params: {
                            obsid: record.get('obsID') || record.get('obsid'),
                            ajaxParams: { method: 'POST' }
                        }
                    };
                    this.query = Ext.create('Mvp.util.MashupQuery', {
                        request: request,
                        ajaxParams: { method: 'POST' },
                        onResponse: this.passthrough,
                        onError: this.onError,
                        onFailure: this.onFailure,
                        scope: this
                    });
                    this.query.run(true);
                },
                scope: this,
                getClass: function (value, metadata, record, rowIndex, colIndex, store) {
                    var project = record.get('project'),
                        value = record.get('t_obs_release'),
                        mjdTime = Mvp.custom.Generic.mjdRenderer(value),
                        timeAry = mjdTime.split('-'),
                        time = new Date(timeAry[0], timeAry[1], timeAry[2]);
                    if (project == 'DADS') {
                        return 'x-hide-display';
                    }
                    return 'icon-align';
                }
            }, {
                icon: Mvp.util.Constants.ADD_TO_BASKET_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Add Associated Files to Download Basket',
                iconCls: 'icon-align',
                handler: function (view, rowIndex, colIndex, item, e, record) {
                    var idCol = record.get('obsID') ? 'obsID' : 'obsid';
                    Mvp.app.DownloadBasket.add('CAOM', [record], idCol);
                    var basket = Mvp.app.DownloadBasket.getBasket('CAOM');
                    var args = {
                        obsid: basket,
                        inputText: basket
                    };
                    var search = this.controller.searchParams.uid.match('TEST') !== null ? 'CAOMDownloadTest' : 'CAOMDownload';
                    var searchParams = Mvp.search.SearchParams.getSearch(search);
                    this.controller.invokeSearch(args, searchParams);
                    this.fireEvent('APP.context.DownloadBasket.changed', {  // alert TopBarContainer
                        type: 'APP.context.DownloadBasket.changed',
                        context: this.controller
                    });
                },
                scope: this
            }, {
                icon: Mvp.util.Constants.ABOUT_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
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
        this.grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo,
            context: this.controller,
            viewConfig: {
                getRowClass: function (record, index) {
                    var value = record.get('t_obs_release'),
                        mjdTime = Mvp.custom.Generic.mjdRenderer(value),
                        timeAry = mjdTime.split('-'),
                        time = new Date(timeAry[0], timeAry[1], timeAry[2]);
                    if ((time - new Date) > 0) {
                        return 'proprietary-highlight'
                    }
                }
            }
        });

        var buttonObj = {
            addButton: this.gridContainer.activeTab.addButton
        };
        this.grid.addListener('selectionchange', this.selectionChange, this, buttonObj);
        this.app = window.app;
        var em = this.app.getEventManager();
        em.addListener('APP.context.records.selected', this.globalSelectionChange, this, buttonObj);
        em.addListener('APP.context.records.filtered', this.globalSelectionChange, this, buttonObj);
        return this.grid;
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
            controller: this.controller,
            parentScope: this		// allows the details panel to reference code in this class
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    },

    passthrough: function (response) {  // performs the CAOM product lookup search and immediately passes it to Distribution
        var table = response.data.Tables[0],
            rows = table.Rows,
            fields = table.Fields,
            i = fields.length,
            uriCol = -1,
            productGroupCol = -1,
            idCol = -1,
            missionCol = -1,
            filenameCol = -1;

        while (i--) {   // find the important columns
            var name = fields[i].name;
            if (name == 'dataURI') {
                uriCol = i;
            } else if (name == 'productGroupDescription') {
                productGroupCol = i;
            } else if ((name == 'obsID') || (name == 'obsid')) {
                idCol = i;
            } else if (name == 'obs_collection') {
                missionCol = i;
            } else if (name == 'productFilename') {
                filenameCol = i;
            }
        }
        if (uriCol < 0) this.onError();

        var mrp = false;
        if (productGroupCol >= 0) {     // figure out whether to download MRP or all
            for (var i = 0; i < rows.length; i++) {
                if (rows[i][productGroupCol] == 'Minimum Recommended Products') {
                    mrp = true;
                    break;
                }
            }
        }

        var urlList = '',
            pathList = '',
            first = true;
        for (var i = 0; i < rows.length; i++) { // build the file list to request from Distribution
            var add = true;
            if (mrp && (rows[i][productGroupCol] != 'Minimum Recommended Products')) add = false;
            var url = rows[i][uriCol].replace('&amp;', '&'),
                obsId = rows[i][idCol],
                mission = rows[i][missionCol],
                match = urlList.indexOf(url);  // don't have duplicate urls
            if (add && (match == -1)) {
                urlList += (first ? '': ',') + url;
                pathList += (first ? '' : ',') + mission + '/' + obsId || 'no_id' + '/' + (filenameCol != -1 ? rows[i][filenameCol] : ''); // this matches the hierarchy created when using the basket
                first = false;
            }
        }

        var request = {
            service: 'Mast.Distribution.Request',
            params: {
                urlList: urlList,
                pathList: pathList,
                filename: 'Observation_' + rows[0][idCol] + '.tar'
            }
        };
        this.query = Ext.create('Mvp.util.MashupQuery', {
            request: request,
            ajaxParams: { method: 'POST' },
            onResponse: this.onPassthroughResponse,
            onError: this.onError,
            onFailure: this.onError,
            scope: this
        });
        this.query.run(true);
        Ext.Msg.progress('Assembling Download', 'Assembling download... you can close this without interrupting the bundling process.', 'Initializing...');
    },

    onPassthroughResponse: function (responseObject, requestOptions, queryScope, complete, updated) {
        Ext.log('DownloadWindowContainer.onResponse: complete = ' + complete + ", updated = " + updated);
        if (complete) {
            // Get the URL and start the download.
            this.complete = complete;
            var data = responseObject.data,
                store = this.controller.store;
            if (data && data.url) {
                Ext.log('Opening as an attachment: ' + data.url);
                Ext.log("download() url: " + data.url);
                Ext.core.DomHelper.append(document.body, {
                    tag: 'iframe',
                    frameBorder: 0,
                    width: 0,
                    height: 0,
                    css: 'display:none;visibility:hidden;height:1px;',
                    src: data.url
                });
            }
            Ext.Msg.close();
        }
        else {  // keep trying
            var p = (responseObject.data && responseObject.data.progress) ? Math.floor(responseObject.data.progress) : 0;
            Ext.Msg.updateProgress(p, 'Working...');
        }
    },

    onError: function () {
        Ext.Msg.updateProgress(0, 'Error encountered, please try again later');
        var task = new Ext.util.DelayedTask(function () { Ext.Msg.close(); });
        task.delay(2000);
    },

    selectionChange: function (obj, selected, eOpts) {  // disables buttons when there's no selection
        if (selected.length == 0) {
            eOpts.addButton.disable();
        } else {
            eOpts.addButton.enable();
        }
    },

    globalSelectionChange: function (event, eOpts) {  // disables buttons when there's no selection
        if (!this.controller.store.hasSelection()) {
            eOpts.addButton.disable();
        } else {
            eOpts.addButton.enable();
        }
    }
});