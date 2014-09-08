Ext.require('Mvp.data.Histogram');

Ext.define('Mvp.filters.FilterTest', {
    // Statics
    statics: {
        factory1: function () {
            var f = Ext.create('Ext.util.Filter', {
                property: 'shortName',
                value: 'Chan',
                anyMatch: true
            });
            return f;
        },

        // This is supposed to generate a histogram of the contents of a column of data
        // in the specified mixed collection.
        histogram: function (mixedCollection, property, separator) {
            var items = mixedCollection.items;
            var hist = {};
            hist._numEntries = 0;
            if (items && Ext.isArray(items)) {
                var i = items.length;
                while (i--) {
                    var record = items[i];
                    var value = record.get(property);
                    if (value) {
                        var stringVal = value.toString();
                        var keys = [stringVal];
                        if (separator) {
                            keys = stringVal.split(separator);
                        }
                        var k = keys.length;
                        while (k--) {
                            var key = keys[k];
                            if (key != '') {
                                var histEntry = hist[key];
                                if (!histEntry) {
                                    hist._numEntries++;
                                    hist[key] = { key: key, count: 1 };
                                } else {
                                    histEntry.count++;
                                }
                            }
                        }
                    }
                }
            }
            return hist;
        },

        histogramToArray: function (histogram) {
            var histArray = new Array(histogram._numEntries);
            var i = 0;
            for (property in histogram) {
                if (property.charAt(0) !== '_') {
                    histArray[i++] = histogram[property];
                }
            }
            return histArray;
        },

        histogramArrayToStore: function (histArray) {
            var store = Ext.create('Ext.data.Store', {
                model: 'Mvp.data.Histogram'
            });
            store.add(histArray);
            return store;
        },

        createBarChart: function (histStore) {
            var chart = Ext.create('Ext.chart.Chart', {
                width: 500,
                height: 1000,
                animate: true,
                store: histStore,
                axes: [{
                    type: 'Numeric',
                    position: 'bottom',
                    fields: ['count'],
                    label: {
                        renderer: Ext.util.Format.numberRenderer('0,0')
                    },
                    title: 'Counts',
                    grid: true,
                    minimum: 0
                }, {
                    type: 'Category',
                    position: 'left',
                    fields: ['key'],
                    title: 'Column Values'
                }],
                series: [{
                    type: 'bar',
                    axis: 'bottom',
                    highlight: true,
                    tips: {
                        trackMouse: true,
                        width: 140,
                        height: 28,
                        renderer: function (storeItem, item) {
                            this.setTitle(storeItem.get('key') + ': ' + storeItem.get('count') + ' times');
                        }
                    },
                    label: {
                        display: 'insideEnd',
                        field: 'count',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'horizontal',
                        color: '#333',
                        'text-anchor': 'middle'
                    },
                    xField: 'key',
                    yField: ['count']
                }]
            });
            return chart;
        },

        createSelectionGrid: function (histStore, title) {
            var sm = Ext.create('Ext.selection.CheckboxModel');
            var grid = Ext.create('Ext.grid.Panel', {
                store: histStore,
                selModel: sm,
                columns: [
                    { text: "Value", width: 200, dataIndex: 'key' },
                    { text: "Count", dataIndex: 'count' }
                ],
                columnLines: true,
                width: 300,
                height: 500,
                frame: true,
                title: title,
                iconCls: 'icon-grid'
            });

            return grid;
        }
    },

    // Config options (only reliably specified on object creation)

    // Other properties

    // Constructor
    constructor: function (config) {
        config = config || {};  // ensure config is defined
        var me = this;

        // Put all the configs into this object.
        Ext.apply(me, config);
    },

    // Methods
    testMethod: function (msg) {
        alert('testMethod: ' + msg);
    }

});





