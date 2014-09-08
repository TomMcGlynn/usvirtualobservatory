Ext.define('Mvp.data.BufferedStore', {
    extend: 'Ext.data.Store',

    constructor: function (config) {
        config = config || {}; // ensure config is defined
        // The data is required for the backing store, but not the buffered store,
        // so save the value and remove it from the config.
        var data = config.data;
        delete config.data;

        // Required config for the buffered store:
        config.buffered = true;
        config.purgePageCount = 0;
        config.proxy = {
            type: 'memory'
        };

        this.callParent([config]);

        // The backingStore is just so that I can use the ArrayReader to parse our data into Model records.  I'm sure we
        // can shortcut this.
        var backingStore = Ext.create('Ext.data.ArrayStore', {
            fields: config.fields,
            data: data
        });

        this.cacheRecords(backingStore.data.items);

        //this.guaranteeRange(0, 49);    
    },

    /**
     * This method needs to be overriden because Store.filter only filters the data that's in the store, not the
     * full data that's in the prefetch 
     *
     * Filters the loaded set of records by a given set of filters.
     * @param {Mixed} filters The set of filters to apply to the data. These are stored internally on the store,
     * but the filtering itself is done on the Store's {@link Ext.util.MixedCollection MixedCollection}. See
     * MixedCollection's {@link Ext.util.MixedCollection#filter filter} method for filter syntax. Alternatively,
     * pass in a property string
     * @param {String} value Optional value to filter by (only if using a property string as the first argument)
     */
    filter: function (filters, value) {
        if (Ext.isString(filters)) {
            filters = {
                property: filters,
                value: value
            };
        }

        var me = this,
            decoded = me.decodeFilters(filters),
            i = 0,
            doLocalSort = me.sortOnFilter && !me.remoteSort,
            length = decoded.length;

        for (; i < length; i++) {
            me.filters.replace(decoded[i]);
        }

        if (me.remoteFilter) {
            //the load function will pick up the new filters and request the filtered data from the proxy
            me.load();
        } else {
            /**
             * A pristine (unfiltered) collection of the records in this store. This is used to reinstate
             * records when a filter is removed or changed
             * @property snapshot
             * @type Ext.util.MixedCollection
             */
            if (me.filters.getCount()) {
                //me.snapshot = me.snapshot || me.data.clone();
                //me.data = me.data.filter(me.filters.items);
                me.snapshot = me.prefetchData.clone();
                var filteredData = me.prefetchData.filter(me.filters.items);
                me.prefetchData.clear();
                me.cacheRecords(filteredData.items);

                if (doLocalSort) {
                    me.sort();
                }
                // fire datachanged event if it hasn't already been fired by doSort
                if (!doLocalSort || me.sorters.length < 1) {
                    me.fireEvent('datachanged', me);
                }
            }
        }
    },

    cacheRecords: function (records, operation) {
        var me = this,
            i = 0,
            length = records.length,
            start = operation ? operation.start : 0;
        if (!Ext.isDefined(me.totalCount) || (me.totalCount !== length)) {
            me.totalCount = records.length;
            me.fireEvent('totalcountchange', me.totalCount);
        }
        for (; i < length; i++) {
            records[i].index = start + i;
        }
        me.prefetchData.addAll(records);
        if (me.purgePageCount) {
            me.purgeRecords();
        }
    }
})