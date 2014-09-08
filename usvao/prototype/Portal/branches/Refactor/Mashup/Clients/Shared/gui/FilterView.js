Ext.define('Mvp.gui.FilterView', {
    extend: 'Ext.panel.Panel',
    //mixins: 'Ext.util.Observable',
    requires: [
        'Mvpc.view.FilterContainer'
    ],

    constructor: function(config) {
        this.callParent(arguments);
        this.addEvents('filterApplied');
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        
        this.exclude = config.exclude;
        
        // list of facets that can be charted, populated by FilterContainer
        this.chartableFacets = { decimalFacets: [], integerFacets: [], categoryFacets: [] };        
    },
    
    // Public methods
     
    updateView: function(updateObject) {
        this.lastUpdateObject = updateObject;

        // Just refresh this thing if we got a *new* store.
        if (updateObject.store) {
            if (updateObject.store !== this.lastStore) {
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
        }
    },
    
    getChartableFacets: function() {
        return this.chartableFacets;
    },
    
    // Private methods
    createFilterPanel: function (updateObject) {
        var cache = updateObject.store.getCache();
        var keys = cache.items[0] ? cache.items[0].fields.keys : [];
        var config = {
            store: updateObject.store,
            columns: keys,
            chartableFacets: this.chartableFacets,
            niceColumnNames: updateObject.columnInfo.niceColumnNames,
            exclude: this.exclude,
            gridColumns: updateObject.columnInfo.columns,
            autoFacetRules: updateObject.columnInfo.autoFacetRules,
            ignoreValues: updateObject.columnInfo.ignoreValues
        };
        var filterPanel = Ext.create('Mvpc.view.FilterContainer', config);
        return filterPanel;
    },

    onFilterApplied: function(filters, store) {
        this.fireEvent('filterApplied', filters, store);
    }
    
});