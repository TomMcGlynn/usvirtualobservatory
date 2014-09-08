Ext.define('Mvp.gui.ResultViewPanel', {
    extend: 'Ext.panel.Panel',
    //mixins: 'Ext.util.Observable',
    requires: [
        'Mvp.util.Constants',
        'Mvp.gui.StatusItems',
        'Mvp.gui.AvItems',
        'Mvp.gui.TitleItems',
        'Mvp.gui.RefreshToolbar'
    ],

    statics: {
        
    },
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.contentType = config.contentType;
        this.controller = config.controller;
        this.extraItems = config.extraItems;
        
        // Customize the config.
        config.layout = config.layout || 'fit';
        config.dockedItems = this.createDockedItems();
        
        this.callParent([config]);
        
        this.controller.on('storeupdated', this.updateView, this);
        this.controller.on('resolvercomplete', this.resolverComplete, this);
        this.initIconAndTooltip();
        this.loadedRowCount = 0;
    },
    
    // Public methods
    
    setStarted: function() {
        if (this.tab) {
            this.tab.setIcon("../Shared/img/loading1.gif");
            this.tab.setTooltip("Loading");
        }
    },
    
    setComplete: function() {
        if (this.tab) {
            this.tab.setIcon(this.icon);
            this.tab.setTooltip(this.tooltip);
        }
    },
    
    updateView: function(updateObject) {
        this.checkRefreshPanel(updateObject);            
        if (updateObject.complete) {
            this.setComplete();
        }
    },
    
    resolverComplete: function(updateObject) {
        var resolverStore = updateObject.store;
        this.description = Mvp.util.NameResolverModel.getResolverSummaryString(resolverStore.getAt(0));
        this.titleDock.setText(this.description);
        this.initIconAndTooltip();
    },
    
    // Private methods

    fireUpdate: function(updateObject) {
        this.firingUpdate = true;
        Ext.callback(this.controller.update, this.controller, [updateObject]);
        this.firingUpdate = false;
    },
    
    initIconAndTooltip: function() {
        this.icon = Mvp.util.Constants.GENERIC_ICON;
        this.tooltip = null;
        if (this.contentType) {
            if (this.contentType === 'image') {
                this.icon = Mvp.util.Constants.IMAGE_ICON;
                this.tooltip = Mvp.util.Constants.IMAGE_TOOLTIP + this.description;
            } else if (this.contentType === 'catalog') {
                this.icon = Mvp.util.Constants.CATALOG_ICON;
                this.tooltip = Mvp.util.Constants.CATALOG_TOOLTIP + this.description;
            } else if (this.contentType === 'mixed') {
                this.icon = Mvp.util.Constants.MIXED_COLLECTION_ICON;
                this.tooltip = Mvp.util.Constants.MIXED_COLLECTION_TOOLTIP + this.description;
            }
        }
    },
    
    createDockedItems: function() {
        this.statusDock = Ext.create('Mvp.gui.StatusItems', {
            controller: this.controller
        });
        var statusItems = this.statusDock.getItems();
        
        this.titleDock = Ext.create('Mvp.gui.TitleItems', {
            controller: this.controller
        });
        var titleItems = this.titleDock.getItems();
        
        var topDocked = statusItems.concat(['->']);
        if (this.extraItems) {
            topDocked = topDocked.concat(this.extraItems);
        }
        topDocked = topDocked.concat(titleItems);
    
        var topBar = {
            xtype: 'toolbar',
            height: 28,
            items: topDocked
        };
        
        var allDocked = [topBar];
        return allDocked;
    },
    
    showRefreshBar: function(show) {
        if (show) {
            this.addDocked(this.refreshToolbar);
        } else {
            this.removeDocked(this.refreshToolbar);
        }
    },
    
    checkRefreshPanel: function(updateObject) {
        var show = false;
        var refreshText = '';
        if (updateObject.datascope) {
            if (updateObject.store) {
                this.loadedRowCount = updateObject.store.getOrigTotalCount();
            }
            
            this.additionalRowCount = updateObject.rowCount - this.loadedRowCount;
            if (this.additionalRowCount) {
                refreshText = this.additionalRowCount + ' new rows received';
                show = true;
            } 
        }
        
        if (show) {
            if (this.refreshToolbar) {
                this.refreshToolbar.setRefreshText(refreshText);
            } else {
                this.refreshToolbar = Ext.create('Mvp.gui.RefreshToolbar', {
                    refreshText: refreshText,
                    controller: this.controller
                });
                this.addDocked(this.refreshToolbar);
            }
        } else {
            if (this.refreshToolbar) {
                this.removeDocked(this.refreshToolbar, true);
                this.refreshToolbar = null;
            }
        }
    }
    
    
});