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
        nPopups: 0
    },

    /**
    * @cfg {Number} pagesize
    * The number of items to have on one page in the grid display.
    */

    constructor: function (config) {
        this.contentType = config.contentType;
        this.controller = config.controller;
        this.extraItems = config.extraItems;

        // Customize the config.
        config.layout = config.layout || 'fit';
        config.dockedItems = this.createDockedItems();
        if (config.controller.searchParams.windowed) {
            config.header = false
        }

        this.callParent([config]);
        this.addEvents({
            start: true,
            complete: true
        });

        this.controller.on('storeupdated', this.updateView, this);
        this.controller.on('resolvercomplete', this.resolverComplete, this);
        this.initIconAndTooltip();
        this.loadedRowCount = 0;
    },

    // Public methods

    setStarted: function () {
        if (this.tab) {
            this.tab.setIcon("../Shared/img/loading1.gif");
            this.tab.setTooltip("Loading");
        }
        this.fireEvent('start', this);
    },

    setComplete: function (updateObject) {
        if (this.tab && !this.controller.searchParams.windowed) {
            this.tab.setIcon(this.icon);
            this.tab.setTooltip(this.tooltip || "");
        }
        this.fireEvent('complete', this);
        this.fireEvent('APP.complete', { type: 'APP.complete', updateObject: updateObject } ); //tdower: talk to Tom. Which events should be APP.?
    },

    updateView: function (updateObject) {
        this.checkRefreshPanel(updateObject);
        if (updateObject.complete) {
            this.setComplete(updateObject);
        }
    },

    resolverComplete: function (updateObject) {
        var resolverStore = updateObject.store;
        if (!resolverStore.getCount()) this.destroy();
        else {
            this.description = Mvp.util.NameResolverModel.getResolverSummaryString(resolverStore.getAt(0));
            this.titleDock.setText(this.description);
            this.initIconAndTooltip();
        }
    },

    // Private methods

    fireUpdate: function (updateObject) {
        this.firingUpdate = true;
        Ext.callback(this.controller.update, this.controller, [updateObject]);
        this.firingUpdate = false;
    },

    initIconAndTooltip: function () {
        this.icon = Mvp.util.Constants.GENERIC_ICON['small'];
        this.tooltip = undefined;
        if (this.contentType) {
            if (this.contentType === 'image') {
                this.icon = Mvp.util.Constants.IMAGE_ICON['small'];
                this.tooltip = Mvp.util.Constants.IMAGE_TOOLTIP + this.description;
            } else if (this.contentType === 'catalog') {
                this.icon = Mvp.util.Constants.CATALOG_ICON['small'];
                this.tooltip = Mvp.util.Constants.CATALOG_TOOLTIP + this.description;
            } else if (this.contentType === 'mixed') {
                this.icon = Mvp.util.Constants.MIXED_COLLECTION_ICON['small'];
                this.tooltip = Mvp.util.Constants.MIXED_COLLECTION_TOOLTIP + this.description;
            } else if (this.contentType === 'spectra') {
                this.icon = Mvp.util.Constants.SPECTRA_ICON['small'];
                this.tooltip = Mvp.util.Constants.SPECTRA_TOOLTIP + this.description;
            } else if (this.contentType === 'observation') {
                this.icon = Mvp.util.Constants.OBSERVATION_ICON['small'];
                this.tooltip = Mvp.util.Constants.OBSERVATION_TOOLTIP + this.description;
            }
        }
    },

    createDockedItems: function () {
        this.statusDock = Ext.create('Mvp.gui.StatusItems', {
            controller: this.controller
        });
        this.statusDock.addListener('cancel', this.cancel, this);

        var statusItems = this.statusDock.getItems();

        this.titleDock = Ext.create('Mvp.gui.TitleItems', {
            controller: this.controller
        });
        var titleItems = this.titleDock.getItems();

        var topDocked = statusItems.concat(['->']);
        if (titleItems) {
            topDocked = topDocked.concat(titleItems);
        }
        if (this.extraItems) {
            topDocked = topDocked.concat(this.extraItems);
        }

        var topBar = {
            xtype: 'toolbar',
            height: 36,
            items: topDocked
        };

        var allDocked = [topBar];
        return allDocked;
    },

    showRefreshBar: function (show, refreshText) {
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
            this.refreshToolbar.show();
        } else {
            if (this.refreshToolbar) {
                this.refreshToolbar.hide();
            }
        }
    },

    checkRefreshPanel: function (updateObject) {
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
        this.showRefreshBar(show, refreshText);
        //if (show) {
        //    if (this.refreshToolbar) {
        //        this.refreshToolbar.setRefreshText(refreshText);
        //    } else {
        //        this.refreshToolbar = Ext.create('Mvp.gui.RefreshToolbar', {
        //            refreshText: refreshText,
        //            controller: this.controller
        //        });
        //        this.addDocked(this.refreshToolbar);
        //    }
        //} else {
        //    if (this.refreshToolbar) {
        //        this.removeDocked(this.refreshToolbar, true);
        //        this.refreshToolbar = null;
        //    }
        //}
    },

    cancel: function () {
        if (!this.lastUpdateObject) this.close();   // if there hasn't been a response just close the window
    },
    
    getStatusLabel: function () {
        return this.statusDock.getItems()[0];
    }
});