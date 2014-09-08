Ext.define('Mvp.data.BufferedStore', {
    extend: 'Ext.data.Store',
    
    requires: 'Mvp.data.StoreProxy',

    constructor: function (config) {
        config = config || {}; // ensure config is defined
        // The data is required for the backing store, but not the buffered store,
        // so save the value and remove it from the config.
        var data = config.data;
        delete config.data;
        this.fsStore = config.fsStore;
        delete config.fsStore;

        // The backingStore 
        var backingStore = Ext.create('Ext.data.ArrayStore', {
            fields: config.fields,
            data: data
        });
        
        // The proxy to the backing store
        var storeProxy = Ext.create('Mvp.data.StoreProxy', {
            backingStore: backingStore,
            useSimpleAccessors: true
        })

        // Required config for the buffered store:
        config.buffered = true;
        config.purgePageCount = 0;
        config.proxy = storeProxy;
        config.remoteFilter = true;
        config.remoteGroup = true;
        config.remoteSort = true;

        this.callParent([config]);

        this.load();
        
        if (data.length > 0) {
            this.guaranteeRange(0, this.pageSize);
        }
        
        //this.cacheRecords(backingStore.data.items);
    },
    
    // Returns the Ext.util.MixedCollection that directly backs the paged data.
    getCache: function() {
        return this.prefetchData;
    },
    
    /**
     * Guarantee a specific range, this will load the store with a range (that
     * must be the pageSize or smaller) and take care of any loading that may
     * be necessary.
     *
     * This is mostly a copy of the overridden method from Ext.data.Store.
     * It is here because the case where two pages must be read is buggy.
     * The guarantee callback should *not* be called after only the first
     * page has been read.
     *
     * I also uncommented the :blocking true lines because
     * being wholly on the client, there's no need for an async read, but I
     * don't think that has any effect because my proxy ignores it.
     */
    guaranteeRange: function(start, end, cb, scope) {
        //<debug>
        if (start && end) {
            if (end - start > this.pageSize) {
                Ext.Error.raise({
                    start: start,
                    end: end,
                    pageSize: this.pageSize,
                    msg: "Requested a bigger range than the specified pageSize"
                });
            }
        }
        //</debug>
        
        end = (end > this.totalCount) ? this.totalCount - 1 : end;
        
        var me = this,
            i = start,
            prefetchData = me.prefetchData,
            range = [],
            startLoaded = !!prefetchData.getByKey(start),
            endLoaded = !!prefetchData.getByKey(end),
            startPage = me.getPageFromRecordIndex(start),
            endPage = me.getPageFromRecordIndex(end);
            
        me.cb = cb;
        me.scope = scope;

        me.requestStart = start;
        me.requestEnd = end;
        // neither beginning or end are loaded
        if (!startLoaded || !endLoaded) {
            // same page, lets load it
            if (startPage === endPage) {
                me.mask();
                me.prefetchPage(startPage, {
                    blocking: true,
                    callback: me.onWaitForGuarantee,
                    scope: me
                });
            // need to load two pages
            } else {
                me.mask();
                me.prefetchPage(startPage, {
                    blocking: true,
                    // override callback: me.onWaitForGuarantee,
                    scope: me
                });
                me.prefetchPage(endPage, {
                    blocking: true,
                    callback: me.onWaitForGuarantee,
                    scope: me
                });
            }
        // Request was already satisfied via the prefetch
        } else {
            me.onGuaranteedRange();
        }
    },
    
    /**
     * Prefetches a page of data.
     * @param {Number} page The page to prefetch
     * @param {Object} options Optional config object, passed into the Ext.data.Operation object before loading.
     * See {@link #load}
     * @param
     *
     * This is overridden for the same reason as guaranteeRange().  The onWaitForGuarantee callback is being set up
     * even when only the first of two needed pages is loaded.
     */
    prefetchPage: function(page, options) {
        var me = this,
            pageSize = me.pageSize,
            start = (page - 1) * me.pageSize,
            end = start + pageSize;
        
        // Currently not requesting this page and range isn't already satisified 
        if (Ext.Array.indexOf(me.pagesRequested, page) === -1 && !me.rangeSatisfied(start, end)) {
            options = options || {};
            me.pagesRequested.push(page);
            Ext.applyIf(options, {
                page : page,
                start: start,
                limit: pageSize,
                // override callback: me.onWaitForGuarantee,
                scope: me
            });
            
            me.prefetch(options);
        }
        
    },
    
    /**
     * @private
     * Called internally when a Proxy has completed a load request.
     *
     * Overriding this to ensure that the prefetchData is filled completely on every load.
     */
    onProxyLoad: function(operation) {
        var me = this,
            resultSet = operation.getResultSet(),
            records = operation.getRecords(),
            allProcessedRecords = operation.allProcessedRecords,  //  All the filtered and sorted records, not just one page.
            successful = operation.wasSuccessful();

        if (resultSet) {
            me.totalCount = resultSet.total;
        }

        if (successful) {
            me.loadRecords(records, operation);
            if (allProcessedRecords) {
                me.prefetchData.clear();
                me.cacheRecords(allProcessedRecords);
                if (me.fsStore) {
//                    me.fsStore.loadRecords(allProcessedRecords);
                }
            }
        }

        me.loading = false;
        me.fireEvent('load', me, records, successful);

        //TODO: deprecate this event, it should always have been 'load' instead. 'load' is now documented, 'read' is not.
        //People are definitely using this so can't deprecate safely until 2.x
        me.fireEvent('read', me, records, operation.wasSuccessful());

        //this is a callback that would have been passed to the 'read' function and is optional
        Ext.callback(operation.callback, operation.scope || me, [records, operation, successful]);
    },
    
    /**
     * Filters the loaded set of records by a given set of filters.
     * @param {Mixed} filters The set of filters to apply to the data. These are stored internally on the store,
     * but the filtering itself is done on the Store's {@link Ext.util.MixedCollection MixedCollection}. See
     * MixedCollection's {@link Ext.util.MixedCollection#filter filter} method for filter syntax. Alternatively,
     * pass in a property string
     * @param {String} value Optional value to filter by (only if using a property string as the first argument)
     */
    filter: function(filters, value) {
        if (Ext.isString(filters)) {
            filters = {
                property: filters,
                value: value
            };
        }

        var me = this,
            decoded = me.decodeFilters(filters),
            i = 0,
            length = decoded.length;
        
        // This clear is important, as otherwise, old filters are not being cleared.  This may get
        // more complex as we integrate with the individual column filters and global text search.
        me.filters.clear();
        for (; i < length; i++) {
            me.filters.replace(decoded[i]);
        }

        //the load function will pick up the new filters and request the filtered data from the proxy
        // This assumes that we are doing "remote" loading, which we are pretending to do.
        me.load();
    }




})