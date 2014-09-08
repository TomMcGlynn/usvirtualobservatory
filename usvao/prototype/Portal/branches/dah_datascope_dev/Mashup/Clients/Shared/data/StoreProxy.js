Ext.define('Mvp.data.StoreProxy', {
    extend: 'Ext.data.proxy.Server',

    constructor: function(config) {
        var me = this;
        
        this.oldSorters = null;
        this.oldFilters = null;
        
        config = config || {};
        this.addEvents(
            /**
             * @event exception
             * Fires when the server returns an exception
             * @param {Ext.data.proxy.Proxy} this
             * @param {Object} response The response from the AJAX request
             * @param {Ext.data.Operation} operation The operation that triggered request
             */
            'exception'
        );
        me.callParent([config]);
        
        me.backingStore = config.backingStore;
        
    },
    
     /**
     * In ServerProxy subclasses, the {@link #create}, {@link #read}, {@link #update} and {@link #destroy} methods all pass
     * through to doRequest. Each ServerProxy subclass must implement the doRequest method - see {@link Ext.data.proxy.JsonP}
     * and {@link Ext.data.proxy.Ajax} for examples. This method carries the same signature as each of the methods that delegate to it.
     * @param {Ext.data.Operation} operation The Ext.data.Operation object
     * @param {Function} callback The callback function to call when the Operation has completed
     * @param {Object} scope The scope in which to execute the callback
     */
    doRequest: function(operation, callback, scope) {
        var me = this,
            store = scope,
            backingStore = me.backingStore,
            action = operation.action,
            page = operation.page,
            limit = operation.limit,
            filters = operation.filters,
            sorters = operation.sorters,
            filtersChanged = !me.filtersEqual(filters, this.oldFilters),
            sortersChanged = !me.sortersEqual(sorters, this.oldSorters),
            groupers = operation.groupers;
            
        if (action === 'read') {
            // We need to set up the results and operation so that the callback can deal with the results.
            if (filtersChanged) {
                Ext.log('Applying ' + filters.length + ' filters.');
                me.filter(filters);
            }
            if (filtersChanged || sortersChanged) {
                Ext.log('Applying ' + sorters.length + ' sorters.');
                me.sort(sorters);
            }
            var totalRecords = backingStore.getCount();
            
            // Get the page of records out of the filtered, sorted backing data.
            Ext.log('Paging info: page(' + page + '), limit(' + limit + '), totalRecords(' + totalRecords + ')');
            var records = {};
            var start = limit * (page - 1);
            if (start < totalRecords) {
                var end = start + limit - 1;
                Ext.log('Paging info: start(' + start + '), end(' + end + ')');
                if (end >= totalRecords) {
                    end = totalRecords - 1;
                    //Ext.log('trimmed end to ' + end);
                }
                records = backingStore.getRange(start, end);
            } else {
                Ext.log('Warning:  start(' + start + ') < totalRecords(' + totalRecords + ')');
            }
            
            // Create a ResultSet from the records.
            var resultSet = Ext.create('Ext.data.ResultSet', {
                count: records.length,
                loaded: true,
                records: records,
                success: true,
                total: totalRecords
            });
            
            Ext.apply(operation, {
                resultSet: resultSet
            });
            operation.setCompleted();
            operation.setSuccessful();

            if (typeof callback == 'function') {
                callback.call(scope || me, operation);
            }
            
        } else {
            Ext.Error.raise("The doRequest function '" + action + "'has not been implemented on Mvp.data.StoreProxy.");         
        }
     },
     
     filtersEqual: function(filters) {
        return false;
     },
     
     sortersEqual: function(s1, s2) {
        var equal = true;
        if (s1 === null) {
            equal = (s2 === null);
        } else if (s2 === null) {
            equal = false;
        } else if (s2 !== null) {
            if (s2.length === s1.length) {
                var i=0;
                for (i=0; i<s2.length && equal; i++) {
                    equal = this.sorterEqual(s1[i], s2[i]);
                }
            } else {
                equal = false;
            }
        }
        return equal;
     },
     
     sorterEqual: function(s1, s2) {
        var equal = true;
        if (s1 === null) {
            equal = (s2 === null);
        } else if (s2 === null) {
            equal = false;
        } else if (s2 !== null) {
            d1 = s1.direction;
            d2 = s2.direction;
            p1 = s1.property;
            p2 = s2.property;
            equal = (d1 === d2) && (p1 === p2);
        }
        return equal;
     },
     
     filter: function(filters) {
        if (filters && (filters.length > 0)) {
            // Since the backingStore uses local filtering, these new filters
            // are applied to the possibly previously filtered results, so we
            // need to clear the filters completely before we refilter.
            this.backingStore.clearFilter(true);
            this.backingStore.filter(filters);
        } else {
            this.backingStore.clearFilter();
        }
        this.oldFilters = filters;
     },
     
     sort: function(sorters) {
        if (sorters && (sorters.length > 0)) {
            this.backingStore.sort(sorters);
        }
        this.oldSorters = sorters;
     }
    

})