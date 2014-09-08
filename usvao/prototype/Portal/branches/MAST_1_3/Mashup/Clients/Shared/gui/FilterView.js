Ext.define('Mvp.gui.FilterView', {
    extend: 'Ext.panel.Panel',
    //mixins: 'Ext.util.Observable',
    requires: [
        'Mvpc.view.FilterContainer'
    ],

    constructor: function (config) {
        this.callParent(arguments);
        this.addEvents('filterApplied');
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);

        this.exclude = config.exclude;

        // list of facets that can be charted, populated by FilterContainer
        this.chartableFacets = { decimalFacets: [], integerFacets: [], categoryFacets: [] };
        this.origStoreCount = -1;
    },

    // Public methods

    updateView: function (updateObject) {
        this.lastUpdateObject = updateObject;

        // Just refresh this thing if we got a *new* store.
        if (updateObject.store) {
            var prevStoreCount = updateObject.store.getOrigTotalCount();
            if ((updateObject.store !== this.lastStore) || (this.origStoreCount != prevStoreCount)) {
                if (this.filterPanel) {
                    // Remove the existing panel.
                    this.filterPanel.removeListener('filterApplied', this.onFilterApplied, this);
                    this.remove(this.filterPanel);
                }
                // Create and add the new panel.
                this.filterPanel = this.createFilterPanel(updateObject);
                this.filterPanel.addListener('filterApplied', this.onFilterApplied, this);
                this.add(this.filterPanel);
            }
            this.lastStore = updateObject.store;
            this.origStoreCount = updateObject.store.getOrigTotalCount();
        }
    },

    getChartableFacets: function () {
        return this.chartableFacets;
    },

    // Private methods
    createFilterPanel: function (updateObject) {
        var cache = updateObject.store.getFilteredRecords();
        var keys = cache[0] ? cache[0].fields.keys : [];
        var config = {
            store: updateObject.store,
            chartableFacets: this.chartableFacets,
            controller: this.controller
        };
        var filterPanel = Ext.create('Mvpc.view.FilterContainer', config);
        return filterPanel;
    },

    onFilterApplied: function (filters, store) {
        this.fireEvent('filterApplied', filters, store);
    }

});