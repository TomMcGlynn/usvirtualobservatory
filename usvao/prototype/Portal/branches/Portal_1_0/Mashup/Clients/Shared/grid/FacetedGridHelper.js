Ext.define('Mvp.grid.FacetedGridHelper', {
    statics: {
        /**
         * This is a helper method for starting a search whose results will be displayed in a grid along with
         * faceted filter panels.
         *
         * The options argument should be a JavaScript object with the following properties:
         *    title(string): The title to give the resulting grid panel
         *    
         *    searchText(string): The string the user entered to initiate the search
         *    
         *    request(object): The mashup request object that will be sent to the mashup server
         *    
         *    app(object): The calling application object.  That application is expected to have the following:
         *        onError(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has an error
         *        onFailure(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has a failure
         *        gridContainer(tabpanel): A tab panel to which the new grid will be added
         *        facetContainer(container): A container (probably a panel) to which the facet panels will be added
         *
         *    onClick(function(view, record, htmlElement, index, e)): A callback that will be called when a record in the resulting grid is clicked.
         *
         *    onDblClick(function(view, record, htmlElement, index, e)): A callback that will be called when a record in the resulting grid is double-clicked.
         *    
         *        
         *
         */
        activate: function(options) {
            var search = Ext.create('Mvp.grid.FacetedGridHelper', {
                title: options.title,
                searchText: options.searchText,
                coneSearchParams: options.coneSearchParams,  // Only used so far for VO Inventory queries
                request: options.request,
                app: options.app,
                facetConfigs: options.facetConfigs,
                itemclick: options.onClick,
                itemdblclick: options.onDblClick,
                tooltip: options.tooltip  // The resolved name, where applicable. 
                });
            search.search();
        }
    },
    
    constructor: function(config) {
        Ext.apply(this, config);
    },
    
    search: function() {
        this.firstData = true;
        var c = this.app.gridContainer;
        
        this.initialDocked = this.createDockedItems();
        
        this.myPanel = Ext.create('Ext.panel.Panel', {
            title: this.title,
            closable: true,
            layout: 'fit',
            dockedItems: this.initialDocked,
            width: this.centerWidth,
            height: this.centerHeight});

        c.add(this.myPanel);
        c.setActiveTab(this.myPanel);
        this.myTab = this.myPanel.tab;
        this.myTab.setIcon("loading1.gif");
        if (this.tooltip) {
            this.myTab.setTooltip(this.tooltip);
        }
        this.myTab.on('beforeclose', this.beforeClose, this);
        
        this.query = Ext.create('Mvp.util.MashupQuery', {
            request: this.request,
            onResponse: this.onResponse,
            onError: this.onError,
            onFailure: this.onFailure,
            scope: this
        });
        this.query.run();

        // Used to create empty grid so toolbar with name resolver info is displayed
        this.app.gridContainer.setActiveTab(this.myPanel);
        
        //var dockedItems = this.createDockedItems();
        //var emptyJSDataSet = { "Columns":[{}], "Fields":[{}], "Rows":[[]] };
        //this.grid = Mvp.grid.MvpGrid.createGrid(emptyJSDataSet, this.centerWidth, this.centerHeight, this.title, dockedItems);
        //this.app.gridContainer.setActiveTab(this.myPanel);
        //this.myPanel.add(this.grid);

    },
    
    beforeClose: function(tab) {
        // The tab is being closed.  Cancel any unfinished query.
        if (this.query) {
            this.query.cancel();
        }
        
        // and remove our facet panels if we're the active tab.
        if (this.app.facetContainer.myActiveTab === this.myPanel) {
            this.app.facetContainer.removeAll(true);
        }
    },
    
    onResponse: function(responseObject, requestOptions, queryScope, complete, updated) {
        Ext.log('FacetedGridHelper.onResponse: firstData = ' + this.firstData + ", complete = " + complete + ", updated = " + updated);
        if (updated || complete) {
            this.queryScope = queryScope;
            this.complete = complete;
            if (this.firstData) {
                this.myPanel.removeDocked(this.initialDocked[0]);
                this.updateDisplay();
                this.firstData = false;
            } else {
            	this.updateStatusText();
            }

            if( this.complete ){
                this.myTab.setIcon("");
            }
        }
    },
    
    onError: function(responseObject, requestOptions, queryScope, complete) {
        Ext.log('FacetedGridHelper.onError() called');
        this.myTab.setIcon("");
        var errMsg = new Ext.form.field.Display({
            value: 'The server encountered an error loading this data.'
        })
        this.myPanel.add(errMsg);

        this.updateStatusText('<b>Error Loading Table</b>');
    },

    onFailure: function(responseObject, requestOptions, queryScope) {
        Ext.log('FacetedGridHelper.onFailure() called');
        this.myTab.setIcon("");
        var errMsg = new Ext.form.field.Display({
            value: 'The server failed to respond.'
        })
        this.myPanel.add(errMsg);

        this.updateStatusText();
    },

    updateRowCounts: function() {
        if (this.queryScope && this.queryScope.currentTable && this.queryScope.pendingTable) {
            this.currentRowCnt = this.queryScope.currentTable.Rows.length;
            this.pendingRowCnt = this.queryScope.pendingTable.Rows.length;
        } else {
            this.currentRowCnt = 0;
            this.pendingRowCnt = 0;
        }
        this.additionalRowCnt = this.pendingRowCnt - this.currentRowCnt;
    },
    
    updateDisplay: function() {
        var me = this;
        var table = this.query.markTable();
        this.updateRowCounts();
        
        if (this.grid) {
            this.grid.destroy();
        }
        
        var dockedItems = this.createDockedItems();
        this.grid = Mvp.grid.MvpGrid.createGrid(table, this.centerWidth, this.centerHeight,
                                                this.title, dockedItems);
        
        this.facets = [];
        for (f in this.facetConfigs) {
            var fConfig = this.facetConfigs[f];
            this.facets.push(Mvp.filters.FacetFilterPanel.createFacetFilterPanel(this.grid,
                                                                            this.grid.getStore(),
                                                                            fConfig.column,
                                                                            fConfig.column,
                                                                            fConfig.separator,
                                                                            fConfig.include));
        }

        // Add query-specific stuff to the grid.
        //this.app.gridContainer.add(this.grid);
        this.app.gridContainer.setActiveTab(this.myPanel);
        this.myPanel.add(this.grid);
        if (this.itemclick) {
            this.grid.on('itemclick', this.itemclick, this.app);
        }
        if (this.itemdblclick) {
            this.grid.on('itemdblclick', this.itemdblclick, this.app);
        }
        // This is a crumby way to make the original search string available in the callbacks.
        this.grid.searchText = this.searchText;
        this.grid.coneSearchParams = this.coneSearchParams;
        
        this.refreshFacets();
        
        // This is a crumby way to get allow us to redraw the facets for this grid when this grid's tab has been selected.
        this.myPanel.tabSelected = function(args) {
            me.refreshFacets();
        };
        
        // Sometimes when multiple queries are run at the same time, the resulting grid does not get drawn.
        // This is an attempt to work around that issue.  Hopefully it won't cause a noticable flash or other
        // performance issue in the cases where it's not necessary.
        //this.grid.forceComponentLayout();
    
        
    },
    
    createDockedItems: function() {
        this.statusLabel = Ext.create('Ext.toolbar.TextItem', {
            text: ''
        });
        this.updateStatusText();

        this.reloadButton = Ext.create('Ext.button.Button', {
            text: 'Reload Table',
            tooltip: 'Reload table to include all new data.',
            hidden: true,
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',  // The 'border' config has no effect, overriding the toolbar button style is a pain
            handler: this.reloadPressed
        });

        this.resolveLabel = Ext.create('Ext.toolbar.TextItem', {
            text: this.tooltip
        });

        var dockedItems = [
        {
            xtype: 'toolbar',
            height: 24,
            items: [this.statusLabel, this.reloadButton, '->', this.resolveLabel]  // '->' used to right justify next element
        }];

        return dockedItems;
    },

    updateStatusText: function(overrideText) {
        if (this.statusLabel) {
            this.updateRowCounts();
            var statusText = overrideText || this.computeStatusText();
            Ext.log('Updating status text: ' + statusText);
            this.statusLabel.setText(statusText);
            if (this.additionalRowCnt) {
                this.reloadButton.show();
            }
        }
    },

    computeStatusText: function() {
    	var currentText = '';
    	// statusLabel text can only be retrieved if the grid object has been initialized
        if (this.grid && this.grid.getDockedItems()[0] && ! this.firstData) {
            currentText = this.grid.getDockedItems()[0].getComponent(0).getEl().dom.innerHTML;
        }

        var text = '';

        // If status has been altered for filtering, preserve that info
        // FRAGILE!  Format must be kept in sync with MvpGrid.updateStatusText()
        var re = /^Displaying <b>\d+<\/b> of /;
        if (re.test(currentText)) {
            text = currentText.match(re)[0];
        }

        text += '<b>' + this.currentRowCnt + ' Total Rows</b>';
        if (!this.complete) {
            text += '...<i>search continuing</i>';
        }
        if (this.additionalRowCnt) {
            text += '...' + this.additionalRowCnt + ' new rows available';
        }
        return text;
    },
    
    reloadPressed: function() {
        this.myPanel.remove(this.grid);
        this.updateDisplay();
    },
    
    refreshFacets: function() {
        // Mark this as the active tab.
        this.app.facetContainer.myActiveTab = this.myPanel;
        
        var removedComps = this.app.facetContainer.removeAll(false);  // The false makes sure we don't destroy the facet panels that we're removing.
        for (r in removedComps) {
            var facetPanel = removedComps[r];
            Ext.log('removed facet' + facetPanel.title);
            
            // Work around apparent bug where if we don't destroy the facetPanel, it stays in the container display.
            facetPanel.hide();
        }
        
        for (f in this.facets) {
            var facetPanel = this.facets[f];
            Ext.log('adding facet ' + facetPanel.title);
            this.app.facetContainer.add(facetPanel);
            facetPanel.show();
        }
    },
    
});
    
