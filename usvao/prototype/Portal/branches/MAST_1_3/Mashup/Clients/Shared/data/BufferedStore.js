Ext.define('Mvp.data.BufferedStore', {
    extend: 'Ext.data.Store',

    requires: ['Mvp.data.StoreProxy',
               'Mvp.util.Util'
               ],

    constructor: function (config) {
        config = config || {}; // ensure config is defined

        if (config.initialData) {
            // The refactored code uses this instead of data.
            var data = config.initialData;
        }
        this.columnInfo = config.columnInfo;
        for (var f in config.fields) {
            var field = config.fields[f];
            if (field.type == 'date') {
                config.fields[f].type = 'int';
                var cc = Mvp.util.Util.extractByPrefix(config.columnInfo.allColumns[f].ExtendedProperties, 'cc');
                cc['isDate'] = true;
            }
        }

        if (data) {
            this.origTotalCount = data.length;
        } else {
            Ext.log('data undefined');
        }

        // The backingStore 
        this.backingStore = Ext.create('Ext.data.ArrayStore', {
            fields: config.fields,
            data: data
        });

        // The proxy to the backing store
        var storeProxy = Ext.create('Mvp.data.StoreProxy', {
            backingStore: this.backingStore,
            useSimpleAccessors: true
        })

        // Required config for the buffered store:
        config.buffered = true;
        config.purgePageCount = 0;
        config.leadingBufferZone = 50000;
        config.proxy = storeProxy;
        config.remoteFilter = true;
        config.remoteGroup = true;
        config.remoteSort = true;
        config.autoLoad = true;

        this.callParent([config]);
    },

    // Returns an array of all the data with filters and sorting applied.
    // Won't work with server-side data.
    getFilteredRecords: function () {
        var records = this.backingStore.getRange();
        return records;
    },

    // Returns a mixed collection of all the data with *no* filters and sorting applied.
    // Won't work with server-side data.
    getUnfilteredRecords: function () {
        var bs = this.backingStore;
        var data = bs.snapshot || bs.data;
        Ext.log('Unfiltered length = ' + data.length);
        return data;
    },

    getSelectedRecords: function () {
        var bs = this.backingStore;
        var selected = [];
        bs.each(function (record) {
            if (record.get('_selected_')) selected.push(record);
        }, this);
        return selected;
    },

    getUnselectedRecords: function () {
        var bs = this.backingStore;
        var unselected = [];
        bs.each(function (record) {
            var sel = record.get('_selected_');
            if ((sel == false) || (sel === undefined)) unselected.push(record);
        }, this);
        return unselected;
    },

    hasSelection: function () {
        // Return true iff at least one of the filtered records has _selected_ true.
        var hasSelection = false;
        this.backingStore.each(function (record) {
            hasSelection = record.get('_selected_');
            return !hasSelection;  // returning false breaks the 'each' iteration
        }, this);
        return hasSelection;
    },
        
    allSelected: function() {
        // Return true iff ALL of the filtered records has _selected_ true.
        var hasSelection = false;
        this.backingStore.each(function (record) {
            hasSelection = record.get('_selected_');
            return hasSelection;  // returning false breaks the 'each' iteration
        }, this);
        return hasSelection;
    },

    setSelected: function (id, selected) {
        var changed = false;
        var record = this.getBsById(id);
        if (record) {
            var oldValue = record.get('_selected_');
            record.set('_selected_', selected);
            changed = (oldValue !== selected);
        }
        return changed;
    },

    getBsById: function (id) {
        return (this.backingStore.snapshot || this.backingStore.data).findBy(function (record) {
            return record.internalId === id;
        });
    },

    setSelectedRange: function (ids, selected) {
        var changed = false;
        for (var i = 0; i < ids.length; i++) {
            changed = this.setSelected(ids[i], selected) || changed;
        }
        return changed;
    },

    getOrigTotalCount: function () {
        // TSD - Can we delegate this to the backing store?
        return this.origTotalCount;
    },

    // Returns an array of all the column information objects.
    getAllColumns: function () {
        var allColumns = null;
        if (this.columnInfo) {
            allColumns = this.columnInfo.allColumns;
        }
        return allColumns;
    },

    getMinMaxValues: function (fieldName, ignoreValue) {
        var min = Infinity;
        var max = -Infinity;
        var rowCollection = this.getFilteredRecords();
        var count = rowCollection.length;
        for (var i = 0; i < count; i++) {
            var record = rowCollection[i];
            var val = record.get(fieldName);
            if (val != ignoreValue) {
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }
        return { min: min, max: max };
    },

    // Private methods

    addHistograms: function () {
        var allColumns = this.getAllColumns();
        if (allColumns) {
            for (c in allColumns) {
                var col = allColumns[c],
                    histObj = {};
                if ((col.datatype == 'double') || (col.datatype == 'float')) {
                    // Compute a histogram of numeric bins.
                    histObj.type = 'numeric';
                    var ignoreValue = col.ignoreValue || NaN;
                    var minmax = this.getMinMaxValues(col.dataIndex, ignoreValue);
                    if (minmax.max == minmax.min) continue;     // doesn't make sense to have a numeric facet if there is no range
                    Ext.apply(histObj, minmax);
                    histObj.hist = Mvp.util.Util.decimalHistogram(this.getFilteredRecords(), col.dataIndex, minmax.min, minmax.max, 100, ignoreValue);

                } else {
                    // Compute a histogram of the discrete values
                    histObj.type = 'discrete';
                    histObj.hist = Mvp.util.Util.histogramToArray(Mvp.util.Util.histogram(this.getFilteredRecords(), col.dataIndex));
                }
                col.ExtendedProperties.histObj = histObj;
            }
        }
    }


})