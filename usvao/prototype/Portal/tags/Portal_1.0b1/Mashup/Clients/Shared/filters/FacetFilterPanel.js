Ext.require('Mvp.filters.FilterTest');

Ext.define('Mvp.filters.FacetFilterPanel', {
    
    extend: 'Ext.grid.Panel',
    
    statics: {
        createFacetFilterPanel: function(sourceGrid, sourceStore, columnName, title, separator, include) {
            var panel = Ext.create('Mvp.filters.FacetFilterPanel', {
                sourceGrid: sourceGrid,
                sourceStore: sourceStore,
                columnName: columnName,
                title: title,
                separator: separator,
                include: include
            });
            
            return panel;
        }
    },
    
    constructor: function(config) {
        var me = this;
        
        // Get variables from config
        me.sourceGrid = config.sourceGrid;
        delete config.sourceGrid;
        me.sourceStore = config.sourceStore;
        delete config.sourceStore;
        me.columnName = config.columnName;
        delete config.columnName;
        me.separator = config.separator;
        delete config.separator;
        me.include = config.include;
        delete config.include;
        
        // Gather histogram data for the column into a new store.
        var histogram = Mvp.filters.FilterTest.histogram(me.sourceStore.getCache(), me.columnName, me.separator);
        var prunedHistogram = histogram;
        
        // If any values were explicitly included, remove values that were not explicitly included.  Otherwise include all values.
        if (Ext.isArray(me.include)) {
            prunedHistogram = {};
            Ext.each(me.include, function(key) {
                 if (histogram[key]) {
                    prunedHistogram[key] = histogram[key];
                }               
            });
        }
        
        me.histArray = Mvp.filters.FilterTest.histogramToArray(prunedHistogram);
        me.histStore = Mvp.filters.FilterTest.histogramArrayToStore(me.histArray);
        
        // Create the selection model.
        var sm = Ext.create('Ext.selection.CheckboxModel', {
        });
        
        // Apply mandatory config for the grid.       
        Ext.apply(config, {
            store: me.histStore,
            selModel: sm,
            columns: [
                {text: "Value", width: 170, dataIndex: 'key', text: '', menuDisabled: true},
                {text: "Count", width: 60, dataIndex: 'count', menuDisabled: true}
            ]
        });
        
        // Try to make height based on number of entries.  This needs improvement.  Look at the paging scroller for hints.
        var height = me.histStore.getCount() * 25 + 70;
        if (height > 500) height = 500;
        
        // Apply defaults for config.       
        Ext.applyIf(config, {
            columnLines: false,
            width: 285,
            height: height,
            frame: true,
            collapsible: true,
            iconCls: 'icon-grid'
        });
        
        this.callParent([config]);
        
        // The grid is initialized.  Do other initialization.
        sm.addListener('selectionchange', me.selectionListener, me);
    },
    
    selectionListener: function(selModel, selected) {
        var me = this;
        
        Ext.log(selected.length + ' rows selected:');
        
        // Get the filter keys from the selections.
        me.filterKeys = new Array();
        for (row in selected) {
            var key = selected[row].get('key');
            if (me.separator) {
                // Match on the value surrounded by the separator.
                key = me.separator + key + me.separator;
            } else {
                // Match on the entire value.
                key = '^' + key + '$';
            }
            me.filterKeys.push(key);
        }
        
        var filter = null;
        // We only do a filter for this if some, but not all values are selected.
        if ((selected.length > 0) && (selected.length != me.histArray.length)) {
            filter = {
                filterFn: function(item) {
                    var me = this;
                    var match = false;
                    
                    for (k in me.filterKeys) {
                        var filterValue = me.filterKeys[k];
                        var itemValue = item.get(me.columnName);
                        if (itemValue.match(filterValue)) {
                            match = true;
                            break;
                        }
                    }
                    return match;
                },
                scope: me
            };
        }
        
        var filters = [];
        if (me.sourceGrid) {
            // Ensure the global filter list exists.
            if (!me.sourceGrid.facetFilters) {
                me.sourceGrid.facetFilters = {};
            }
            
            // Add or remove our filter from the global list.
            if (filter) {
                me.sourceGrid.facetFilters[me.columnName] = filter;
            } else {
                delete me.sourceGrid.facetFilters[me.columnName];
            }
            
            // Build the array of filters to apply from the global list.
            for (f in me.sourceGrid.facetFilters) {
                filters.push(me.sourceGrid.facetFilters[f]);
            }
        } else if (filter) {
            filters = [filter];
        }

        // Do the filtering.
        if (filters.length > 0) {
            me.sourceStore.filter(filters);
        } else {
            me.sourceStore.clearFilter();
        }
        
        me.verifyScroller();
        me.sourceGrid.determineScrollbars();
        me.sourceGrid.invalidateScroller();

        var filteredRecordCnt = me.sourceGrid.getStore().getTotalCount();
        me.sourceGrid.updateStatusText(filteredRecordCnt);
    },

    verifyScroller: function() {
        var g = this.sourceGrid;
        if (g) {
            var s = g.verticalScroller;
          //  Ext.log('VScroller: ' + s.scrollEl.id + ", " + s.managedListeners.length);
        }
    }
});
