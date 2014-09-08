Ext.define('Mvp.gui.StatusItems', {
    extend: 'Ext.util.Observable',
    
    requires: [
        'Ext.toolbar.TextItem',
        'Ext.ProgressBar',
        'Ext.button.Button'
    ],

    statics: {
        
    },
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.callParent(arguments);
        this.addEvents('cancel');
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        
        // Create the components that can be added to a dock.
        this.statusLabel = Ext.create('Ext.toolbar.TextItem', { text: '<b>0 Total Rows</b>' });

        this.statusBar = Ext.create('Ext.ProgressBar', {
            value: 0.01,
            width: 200,
            hidden: false,
            style: { 'border-style': 'solid' }
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

    },
    
    // Public methods.
       
    updateView: function(updateObject) {
        var statusText = this.computeStatusText(updateObject);
        this.statusLabel.setText(statusText);
        var progressText = '';
        
        if (updateObject.complete) {
            // Mark the load complete.
            this.cancelButton.hide();
            if (updateObject.cancelled) {
                progressText = '<i>Load cancelled</i>';
                this.statusBar.updateProgress(updateObject.percent, progressText);
                this.statusBar.show();
            } else {
                this.statusBar.hide();
            }
        } else {
            // Mark the load as in progress.
            
            if (updateObject.datascope) {
                if (updateObject.percent) {
                    progressText += '<i> ' + Ext.Number.toFixed(updateObject.percent * 100, 1) + '% of resources searched</i>';
                }          
            } else {
                // Non-datascope updates are being chunked from the server, so show that progress.
                var availableCount = this.availableCount(updateObject);
                var expectedCount = this.expectedCount(updateObject);
                var progressText = '<b>' + availableCount + ' / ' +
                    expectedCount + ' Rows Loaded</b>';
            }
            this.statusBar.updateProgress(updateObject.percent, progressText);
            this.statusBar.show();
            this.cancelButton.show();
        }
        
        // Save the store for future updates that may not come with one.      
        if (updateObject.store) {
            this.lastStore = updateObject.store;
        }

    },
    
    computeStatusText: function(updateObject) {
        var statusText = '';
        var loadedCount = this.loadedCount(updateObject);
        var filteredCount = this.filteredCount(updateObject);
        if ((loadedCount === filteredCount) || (filteredCount === undefined)) {
            statusText = '<b>' + loadedCount + ' Total Rows</b>';
        } else {
            statusText = 'Displaying <b>' + filteredCount + '</b> of ' + loadedCount + ' Total Rows</b>';
        }
        return statusText;
    },
    
    loadedCount: function(updateObject) {
        var loadedCount = 0;
        if (updateObject.store) {
            loadedCount = updateObject.store.getOrigTotalCount();
        } else if (this.lastStore) {
            loadedCount = this.lastStore.getOrigTotalCount();
        }
        return loadedCount;
    },
    
    filteredCount: function(updateObject) {
        var filteredCount = 0;
        if (updateObject.store) {
            filteredCount = updateObject.store.getTotalCount();
        } else if (this.lastStore) {
            filteredCount = this.lastStore.getTotalCount();
        }
        return filteredCount;
    },
    
    availableCount: function(updateObject) {
        var availableCount = updateObject.rowCount;
        return availableCount;
    },
    
    expectedCount: function(updateObject) {
        var expectedCount = updateObject.pageInfo.rowsFiltered;
        return expectedCount;
    },
    
    getItems: function() {
        var items = [
            this.statusLabel,
            this.statusBar,
            this.cancelButton
        ];
        return items;
    },
    
    // Private methods.
    cancelPressed: function() {
        Ext.callback(this.controller.cancel, this.controller);
    }
    
});
    

    