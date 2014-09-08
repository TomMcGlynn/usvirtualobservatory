Ext.define('Mvp.data.StoreProxy', {
    extend: 'Ext.data.proxy.Server',

    constructor: function(config) {
        var me = this;
        
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
            groupers = operation.groupers;
            
        if (action === 'read') {
            // We need to set up the results and operation so that the callback can deal with the results.
            var backingData = backingStore.data;
            if (filters && (filters.length > 0)) {
                Ext.log('Applying ' + filters.length + ' filters.');
                backingData = backingData.filter(filters);
            }
            if (sorters && (sorters.length > 0)) {
                Ext.log('Applying ' + sorters.length + ' sorters.');
                backingData.sort(sorters);
            }
            var totalRecords = backingData.getCount();
            
            // Get the page of records out of the filtered, sorted backing data.
            Ext.log('Paging info: page(' + page + '), limit(' + limit + '), totalRecords(' + totalRecords + ')');
            var records = {};
            var start = limit * (page - 1);
            if (start < totalRecords) {
                var end = start + limit - 1;
                Ext.log('Paging info: start(' + start + '), end(' + end + ')');
                if (end >= totalRecords) {
                    end = totalRecords - 1;
                    Ext.log('trimmed end to ' + end);
                }
                records = backingData.getRange(start, end);
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
                //response: response, // We don't have a response object for this case.
                resultSet: resultSet,
                allProcessedRecords: backingData.items
            });
            operation.setCompleted();
            operation.setSuccessful();

            if (typeof callback == 'function') {
                callback.call(scope || me, operation);
            }
            
        } else {
            Ext.Error.raise("The doRequest function '" + action + "'has not been implemented on Mvp.data.StoreProxy.");         
        }
        
        
     }
    

})