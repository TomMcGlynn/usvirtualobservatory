Ext.require('Mvp.gui.SaveAsPanel');
Ext.require('Mvpd.view.ExportToWindowCombos');
Ext.require('Mvpc.view.FilterContainer');
Ext.require('Mvpc.view.LayerChooserContainer');
Ext.require('Mvpc.view.ColorPickerContainer');
Ext.require('Mvpc.view.ChartContainer');
Ext.require('Mvp.util.Util');
Ext.require('Mvp.util.Constants');

Ext.define('Mvp.grid.FacetedGridHelper', {
    statics: {
        /**
        * This is a helper method for starting a search whose results will be displayed in a grid along with
        * faceted filter panels.
        * 
        * The options argument should be a JavaScript object with the following properties:
        *    title(string): The title to give the resulting grid panel
        * 
        * searchText(string): The string the user entered to initiate the search
        * 
        * request(object): The mashup request object that will be sent to the mashup server
        * 
        *    app(object): The calling application object.  That application is expected to have the following:
        *        onError(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has an error
        *        onFailure(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has a failure
        *        gridContainer(tabpanel): A tab panel to which the new grid will be added
        *        facetContainer(container): A container (probably a panel) to which the facet panels will be added
        * 
        * onClick(function(view, record, htmlElement, index, e)): A callback that will be called when a record in the resulting grid is clicked.
        * 
        * onDblClick(function(view, record, htmlElement, index, e)): A callback that will be called when a record in the resulting grid is double-clicked.
        * 
        * 
        * 
        */
        activate: function (options) {
            var search = Ext.create('Mvp.grid.FacetedGridHelper', {
                title: options.title,
                searchText: options.searchText,
                coneSearchParams: options.coneSearchParams, // Only used so far for VO Inventory queries
                request: options.request,
                app: options.app,
                extraColumns: options.extraColumns,
                itemclick: options.onClick,
                itemdblclick: options.onDblClick,
                createImagePanel: options.createImagePanel,
                imagePanelTemplate: options.imagePanelTemplate,
                tooltip: options.tooltip, // The resolved name, where applicable.
                exclude: options.exclude,
                renderers: options.renderers,
                icon: options.icon
            });
            search.search();
        }
    },

    constructor: function (config) {
        Ext.apply(this, config);
        this.chartWindow = Ext.create('Ext.window.Window', {
            height: 600,
            width: 800,
            closeAction: 'hide',
            title: 'Table Data Scatter Plot',
            constrainHeader: true,
            layout: 'fit'
        });
        this.exclude = config.exclude;
        this.chartableFacets = { decimalFacets: [], integerFacets: [], categoryFacets: [] };        // list of facets that can be charted, populated by FilterContainer
        this.niceColumnNames = [];      // list of facets that have nice names as defined in ColumnsConfig, populated by MvpGrid
        this.ignoreValues = [];
        this.autoFacetRules = [];
        this.icon = config.icon;
    },

    search: function () {
        this.firstData = true;
        var c = this.app.gridContainer;

        this.initialDocked = this.createDockedItems();


        if (this.createImagePanel) {
            this.pagesize = 20;
            this.tablePanel = Ext.create('Ext.panel.Panel', {
                title: 'Table View',
                closable: false,
                layout: 'fit',
                dockedItems: this.initialDocked
            });


            this.imagePanel = Ext.create('Ext.panel.Panel', {
                title: 'Album View',
                closable: false,
                layout: 'fit'
            });

            if (useAV && false) {
                this.avPanel = Ext.create('Ext.panel.Panel', {
                    title: 'AstroView',
                    closable: false,
                    layout: 'fit'
                });


                this.flashPanel = this.app.makeFlashPanel(this);
                this.avPanel.add(this.flashPanel);


                this.containerPanel = Ext.create('Ext.tab.Panel', {
                    title: this.title,
                    closable: true,
                    layout: 'fit',
                    items: [this.tablePanel, this.imagePanel, this.avPanel]
                });


                this.containerPanel.on('tabchange', function (args) {
                    Ext.log('tab selected');
                    this.app.flashSuccess();
                },
                    this);

            } else {
                this.containerPanel = Ext.create('Ext.tab.Panel', {
                    title: this.title,
                    closable: true,
                    layout: 'fit',
                    cls: 'x-btn-text-icon',
                    items: [this.tablePanel, this.imagePanel]
                });

            }
            this.containerPanel.setActiveTab(this.detailsPanel);
            //this.containerPanel.on('beforeclose', function(panel) {
            //    Ext.log('Close rejected!');
            //    if (this.imagePanel) {
            //        this.imagePanel.close();
            //        this.imagePanel = null;
            //    }
            //    return false;
            //}, this);

        } else {
            this.pagesize = 50;
            this.containerPanel = Ext.create('Ext.panel.Panel', {
                title: this.title,
                closable: true,
                layout: 'fit',
                dockedItems: this.initialDocked
            });
            this.tablePanel = this.containerPanel;
        }

        if (!useDesktop) {
            c.add(this.containerPanel);
            c.setActiveTab(this.containerPanel);
            this.myTab = this.containerPanel.tab;
            this.myTab.setIcon("../Shared/img/loading1.gif");
            if (this.icon != Mvp.util.Constants.GENERIC_ICON) {
                var tooltip = '';
                if (this.icon == Mvp.util.Constants.MIXED_COLLECTION_ICON) tooltip = Mvp.util.Constants.MIXED_COLLECTION_TOOLTIP;
                if (this.icon == Mvp.util.Constants.IMAGE_ICON) tooltip = Mvp.util.Constants.IMAGE_TOOLTIP;
                if (this.icon == Mvp.util.Constants.CATALOG_ICON) tooltip = Mvp.util.Constants.CATALOG_TOOLTIP;
                if (this.tooltip) tooltip += this.tooltip;
                this.myTab.setTooltip(tooltip);
            }
            this.myTab.on('beforeclose', this.beforeClose, this);
        } else {
            var resultsWindow = globalDesk.createWindow({
                //  id: "result-win",
                title: this.title,
                width: 600,
                height: 400,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: [this.containerPanel]
            });
            resultsWindow.show();
            this.app.facetContainer = Ext.create('Ext.panel.Panel', {
                width: 50,
                height: 50
            });

            var filterWindow = globalDesk.createWindow({
                //     id: "result-win",
                title: "Filters for " + this.title,
                width: 270,
                height: 400,
                iconCls: "icon-grid",
                animCollapse: false,
                constrainHeader: true,
                layout: "fit",
                items: [this.app.facetContainer]
            });
            this.app.facetContainer.show();
        }

        this.query = Ext.create('Mvp.util.MashupQuery', {
            request: this.request,
            onResponse: this.onResponse,
            onError: this.onError,
            onFailure: this.onFailure,
            scope: this
        });
        this.query.run();

        // Used to create empty grid so toolbar with name resolver info is displayed
        if (!useDesktop) {
            this.app.gridContainer.setActiveTab(this.containerPanel);
        }
    },

    beforeClose: function (tab) {
        // The tab is being closed. Cancel any unfinished query.
        if (this.query) {
            this.query.cancel();
        }

        // and remove our facet panels if we're the active tab.
        if (this.app.facetContainer.myActiveTab === this.containerPanel) {
            this.app.facetContainer.removeAll(true);
        }
    },

    onResponse: function (responseObject, requestOptions, queryScope, complete, updated, pagingExpectedRowCount) {
        Ext.log('FacetedGridHelper.onResponse: firstData = ' + this.firstData + ", complete = " + complete + ", updated = " + updated);
        if (updated || complete) {
            this.queryScope = queryScope;
            this.complete = complete;
            this.pagingExpectedRowCount = pagingExpectedRowCount;
            if (pagingExpectedRowCount) {
                this.wasPaged = true;
            }

            var votProps = queryScope.pendingTable.ExtendedProperties.vot;
            this.percentComplete = 0;
            if (votProps) {
                for (var i = 0; i < votProps.PARAMs.length; i++) {
                    param = votProps.PARAMs[i];
                    if (param.name && param.name == "percentComplete") {
                        this.percentComplete = param.value;
                    }
                }
            }
            Ext.log('FacetedGridHelper.onResponse: Percent Complete: ' + this.percentComplete);

            if (this.firstData) {
                var docked = this.tablePanel.getDockedItems();
                for (d in docked) {
                    var item = docked[d];
                    this.tablePanel.removeDocked(item);
                }
                this.updateDisplay();
                this.firstData = false;
            } else {
                if (pagingExpectedRowCount) {
                    // this.updateDisplay();
                }
                this.updateStatus();
            }

            if (this.complete && !this.pagingExpectedRowCount) {
                if (!useDesktop) {
                    this.restoreIcon();
                }
                // For a paged scenario, ensure that the final display is updated.
                if (this.wasPaged) {
                    this.updateDisplay();
                }
                this.cancelButton.hide();
            }
        }
    },

    restoreIcon: function () {
        (this.icon) ? this.myTab.setIcon(this.icon) : this.myTab.setIcon(Mvp.util.Constants.GENERIC_ICON);
    },

    onError: function (responseObject, requestOptions, queryScope, complete) {
        Ext.log('FacetedGridHelper.onError() called');
        if (!useDesktop) {
            this.myTab.setIcon("");
        }
        var errMsg = new Ext.form.field.Display({ value: 'The server encountered an error loading this data.' });
        this.tablePanel.add(errMsg);

        this.updateStatus('<b>Error Loading Table</b>');
    },

    onFailure: function (responseObject, requestOptions, queryScope) {
        Ext.log('FacetedGridHelper.onFailure() called');
        this.myTab.setIcon("");
        var errMsg = new Ext.form.field.Display({ value: 'The server failed to respond.' });
        this.tablePanel.add(errMsg);

        this.updateStatus();
    },

    updateRowCounts: function () {
        if (this.queryScope && this.queryScope.currentTable && this.queryScope.pendingTable) {
            this.currentRowCnt = this.queryScope.currentTable.Rows.length;
            this.pendingRowCnt = this.queryScope.pendingTable.Rows.length;
        } else {
            this.currentRowCnt = 0;
            this.pendingRowCnt = 0;
        }
        this.additionalRowCnt = this.pendingRowCnt - this.currentRowCnt;
    },

    updateDisplay: function () {
        var me = this;
        var table = this.query.markTable();
        this.updateRowCounts();

        if (this.grid) {
            this.grid.destroy();
        }

        var dockedItems = this.createDockedItems();
        this.grid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight, this.title, dockedItems, this.extraColumns, this.pagesize, this.niceColumnNames, this.renderers, this.autoFacetRules, this.ignoreValues);
        //this.autoFacetRules = this.grid.autoFacetRules;

        if (this.currentRowCnt > 0) {
            // Create the facet panel.  It will be made visible in refreshFacets.
            this.setupFacetPanel();
        }

        // Add query-specific stuff to the grid.
        // this.app.gridContainer.add(this.grid);
        if (!useDesktop) {
            this.app.gridContainer.setActiveTab(this.containerPanel);
        }
        this.tablePanel.add(this.grid);

        //////////////////////////////////////////////////////////////////////////
        // Dataview experiment
        if (this.createImagePanel) {
            // Note this needs some cleanup if it's supposed to work with multiple calls to updateDisplay (like with DataScope).

            var store = this.grid.getStore();
            var tpl = this.imagePanelTemplate; //Mvp.custom.Caom.dataviewTemplate();

            var pagingbar = new Ext.toolbar.Paging({
                style: 'border:1px solid #99BBE8;',
                store: store,
                pageSize: 5,
                displayInfo: true
            });

            var datav = new Ext.view.View({
                //autoScroll: true,
                store: store, tpl: tpl,
                /*autoHeight: true, height: 400, */multiSelect: true,
                //height: this.centerHeight,
                overItemCls: 'x-view-over', itemSelector: 'div.thumb-wrap',
                emptyText: 'No images to display',
                style: 'border:1px solid #99BBE8; border-top-width: 0;'
            });

            this.panelMain = new Ext.Panel({
                itemId: 'images-view',
                frame: false,
                autoHeight: true,
                autoScroll: true,
                layout: 'auto',
                items: [pagingbar, datav]
            });
            this.imagePanel.add(this.panelMain);
        }
        //////////////////////////////////////////////////////////////////////////

        if (this.itemclick) {
            this.grid.on('itemclick', this.itemclick, this.app, { grid: this.grid });
        }
        if (this.itemdblclick) {
            this.grid.on('itemdblclick', this.itemdblclick, this.app, { grid: this.grid });
        }
        // This is a crumby way to make the original search string available in the callbacks.
        this.grid.searchText = this.searchText;
        this.grid.coneSearchParams = this.coneSearchParams;

        this.refreshFacets();

        // This is a crumby way to get allow us to redraw the facets for this grid when this grid's tab has been selected.
        this.containerPanel.tabSelected = function (args) {
            me.refreshFacets();
        };

        // Sometimes when multiple queries are run at the same time, the resulting grid does not get drawn.
        // This is an attempt to work around that issue. Hopefully it won't cause a noticable flash or other
        // performance issue in the cases where it's not necessary.
        // this.grid.forceComponentLayout();

    },

    setupFacetPanel: function () {
        if (this.facetPanel) this.facetPanel.removeAll();
        var keys = this.grid.store.getCache().items[0].fields.keys;
        var config = { store: this.grid.store, columns: keys, chartableFacets: this.chartableFacets, niceColumnNames: this.niceColumnNames, exclude: this.exclude, gridColumns: this.grid.columns, autoFacetRules: this.autoFacetRules, ignoreValues: this.ignoreValues };
        this.facetPanel = Ext.create('Mvpc.view.FilterContainer', config);
        if (this.chartableFacets.decimalFacets.length + this.chartableFacets.categoryFacets.length < 2) this.chartButton.disable();
        this.facetPanel.addListener('filterApplied', this.onFilterApplied, this);
    },

    createDockedItems: function () {
        this.statusLabel = Ext.create('Ext.toolbar.TextItem', { text: '' });

        this.statusBar = Ext.create('Ext.ProgressBar', {
            value: 0.5,
            width: 200,
            hidden: true,
            style: { 'border-style': 'solid' }
        });

        this.updateStatus();

        this.reloadButton = Ext.create('Ext.button.Button', {
            text: 'Refresh Table',
            tooltip: 'Reload table to include all new data.',
            hidden: true,
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            handler: this.reloadPressed
        });

        this.cancelButton = Ext.create('Ext.button.Button', {
            text: 'Cancel',
            tooltip: 'Cancel loading data for this table<br>This will recreate the filters panel',
            hidden: false,
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            handler: this.cancelPressed
        });

        this.resolveLabel = Ext.create('Ext.toolbar.TextItem', { text: this.tooltip });

        this.exportButton = Ext.create('Ext.button.Button', {
            text: 'Export Table As...',
            tooltip: 'Export the data in the table to a local file.',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.exportPressed
        });

        this.layerChooser = Ext.create('Mvpc.view.LayerChooserContainer');
        this.layerChooser.addListener('change', this.astroViewToggled, this);
        this.colorPicker = Ext.create('Mvpc.view.ColorPickerContainer');
        this.colorPicker.addListener('colorChanged', this.colorChanged, this);
        this.chartButton = Ext.create('Ext.button.Button', {
            text: 'Charts...',
            handler: this.displayChart,
            scope: this,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            shadow: true
        });
        var dockedItems;
        if (isDevelopment) {
            dockedItems = [{
                xtype: 'toolbar',
                height: 28,
                items: [
                        this.statusLabel, this.statusBar, this.reloadButton, this.cancelButton, '->', this.layerChooser, this.colorPicker, this.chartButton, this.exportButton, this.resolveLabel
                ] // '->' used to right justify next element
            }];
        }
        else if (useAV) {
            dockedItems = [{
                xtype: 'toolbar',
                height: 28,
                items: [
                        this.statusLabel, this.statusBar, this.reloadButton, this.cancelButton, '->', this.layerChooser, , this.colorPicker, this.exportButton, this.resolveLabel
                ] // '->' used to right justify next element
            }];
        }
        else {
            dockedItems = [{
                xtype: 'toolbar',
                height: 28,
                items: [
                        this.statusLabel, this.statusBar, this.reloadButton, this.cancelButton, '->', this.exportButton, this.resolveLabel
                ] // '->' used to right justify next element
            }];
        }

        return dockedItems;
    },

    astroViewToggled: function (checkox, newValue, oldValue) {
        Ext.log('AV toggled from ' + oldValue + ' to ' + newValue);
        this.updateAstroView();
    },

    colorChanged: function (picker, selColor) {
        // When AV has a layer update, we can do that.  For now, we'll just redraw the whole layer.
        this.updateAstroView();
    },

    displayChart: function () {
        var chartContainer = Ext.create('Mvpc.view.ChartContainer', { store: /*this.grid.getFsStore()*/this.grid.getStore(), facets: this.chartableFacets, niceColumnNames: this.niceColumnNames });
        this.chartWindow.setTitle('Charts - ' + this.grid.title);
        this.chartWindow.removeAll();
        this.chartWindow.add(chartContainer);
        this.chartWindow.show();
    },

    updateStatus: function (overrideText) {
        if (this.statusLabel) {
            this.updateRowCounts();
            var statusText = overrideText || this.computeStatusText();
            Ext.log('Updating status text: ' + statusText);
            this.statusLabel.setText(statusText);
            if (this.additionalRowCnt && !this.pagingExpectedRowCount) {
                this.reloadButton.show();
            }
        }
        if (this.statusBar) {
            var barText = '';
            if (this.cancelled) {
                barText += '<i>Load cancelled</i>';
                this.statusBar.show();
            }
            if (this.additionalRowCnt) {
                barText += this.additionalRowCnt + ' new rows received:';
                this.statusBar.show();
            }
            if (!this.complete) {
                if (this.percentComplete) {
                    barText += '<i> ' + Ext.Number.toFixed(this.percentComplete * 100, 1) + '%</i>';
                }
                this.statusBar.show();
            }
            this.statusBar.updateProgress(this.percentComplete, barText);
        }
    },

    computeStatusText: function () {
        var currentText = '';
        // statusLabel text can only be retrieved if the grid object has been initialized
        if (this.grid && this.grid.getDockedItems()[0] && !this.firstData) {
            currentText = this.grid.getDockedItems()[0].getComponent(0).getEl().dom.innerHTML;
        }

        var text = '';

        // If status has been altered for filtering, preserve that info
        // FRAGILE! Format must be kept in sync with MvpGrid.updateStatusText()
        var re = /^Displaying <b>\d+<\/b> of /;
        if (re.test(currentText)) {
            text = currentText.match(re)[0];
        }

        if (this.pagingExpectedRowCount) {
            text += '<b>' + this.currentRowCnt + ' / ' + this.pagingExpectedRowCount + ' Rows Loaded</b>';
        } else {
            text += '<b>' + this.currentRowCnt + ' Total Rows</b>';
        }
        return text;
    },

    reloadPressed: function () {
        this.tablePanel.remove(this.grid);
        this.updateDisplay();
    },

    cancelPressed: function () {
        this.query.cancelled = true;
        this.cancelled = true;
        this.pagingExpectedRowCount = null;

        // Update the display only if we've actually received any data.
        if (!this.firstData) {
            this.updateDisplay();
        }
        this.cancelButton.hide();

        this.complete = true;
        this.updateStatus();
        if (!useDesktop) {
            this.restoreIcon();
        }
    },

    exportPressed: function () {
        // Replace title characters that could be problematic in a filename, then remove duplicate underscores.

        // Specify the file type and name.
        var filetype = 'csv';
        var filename = Mvp.util.Util.filenameCreator(this.title, filetype);



        // Present the user dialog so they can select a file type and modify the file name.
        var exportWindow = Ext.create('Mvpd.view.ExportToWindowCombos', {
            grid: this.grid,
            exportHandler: this.exportHandler
        });
        exportWindow.setFilename(filename);
        exportWindow.setFiletype(filetype);
        exportWindow.show();

    },

    exportHandler: function (args) {
        var me = this;

        // Get the data that needs to be uploaded to the server for export.
        var filtercolumns = me.getFiltercolumns();
        var table = this.grid.getExportTable(filtercolumns);

        var options = {
            filename: me.getFilename(),
            filetype: me.getFiletype(),
            filtercolumns: filtercolumns,
            attachment: me.getAttachment(),
            data: table
        };

        Ext.log('filename = ' + options.filename);
        Ext.log('filetype = ' + options.filetype.filetype);
        Ext.log('filtercolumns = ' + options.filtercolumns);
        Ext.log('attachment = ' + options.attachment);

        Mvp.util.Exporter.activate(options);

        me.closeWindow();
    },

    refreshFacets: function () {
        // Mark this as the active tab.
        this.app.facetContainer.myActiveTab = this.containerPanel;

        var removedComps = this.app.facetContainer.removeAll(false); // The false makes sure we don't destroy the facet panels that we're removing.
        for (r in removedComps) {
            var p = removedComps[r];
            //Ext.log('removed facet' + p.title);

            // Work around apparent bug where if we don't destroy the facet panel, it stays in the container display.
            p.hide();
        }

        if (this.facetPanel) {
            //Ext.log('adding facet ' + this.facetPanel.title);
            this.app.facetContainer.add(this.facetPanel);
            this.facetPanel.show();
        }
    },

    onFilterApplied: function (filters, store) {
        // The grid has just been filtered.  Update anything necessary in the display.

        // Force the scroller to refresh.
        if (this.grid) {
            this.grid.determineScrollbars();
            this.grid.invalidateScroller();
        }


        // Update the toolbar to give proper counts ("Displaying x of y").
        // (Kind of terrible that FacetedGridHelper and MvpGrid have an updateStatusText that need to know
        // about each other.)
        var filteredRecordCnt = store.getTotalCount();
        this.grid.updateStatusText(filteredRecordCnt);
        if (this.chartWindow.isVisible()) this.displayChart();
        this.updateAstroView();

    },

    updateAstroView: function () {
        if (useAV && (this.grid.hasFootprints() || this.grid.hasPositions())) {
            if (this.astroViewLayerId) {
                AstroView.deleteLayer(this.astroViewLayerId);
                this.astroViewLayerId = null;
            }

            var layerOn = this.layerChooser.getValue();
            if (layerOn) {
                var color = this.colorPicker.getColor();
                if (color) {
                    color = '0x' + color;
                } else {
                    color = "0xff0000";    // default to red.
                }
                // Get an array of footprint or position objects (one for each row in the filtered store).
                var rows = [],
                    type;
                if (this.grid.hasFootprints()) {
                    rows = this.grid.getFootprints();
                    type = 'footprint';
                } else if (this.grid.hasPositions()) {
                    rows = this.grid.getPositions();
                    type = 'catalog';
                }
                if (rows.length > 0) {
                    var layer = {
                        "type": type,
                        "attribs": {
                            "color": color
                        },
                        "rows": rows
                    };

                    // Send the footprint layer to AstroView.
                    this.astroViewLayerId = AstroView.createLayer(layer);
                }
            }
        }
    }

});
