
var textCloudMaxShown = 60;

Ext.define('Mvpc.view.TextCloudContainer', {
    extend: 'Mvpc.view.ui.TextCloudContainer',
    requires: [],
    statics: {
        instances: {}
    },

    constructor: function (config) {
        this.callParent(arguments);

        // Set up communication with context manager.
        if (config && config.app) {
            this.app = config.app;
            var em = this.app.getEventManager();
            em.addListener('APP.context.records.selected', this.onRecordsSelected, this);
            em.addListener('APP.context.records.filtered', this.onRecordsFiltered, this);
            em.addListener('APP.context.added', this.onContextAdded, this);
            em.addListener('APP.context.removed', this.onContextRemoved, this);

            em.addListener('APP.complete', this.onRecordsComplete, this);
            this.addListener('complete', this.onRecordsComplete, this);
        }
        if (config && config.dataColumnName) {
            this.dataColumnName = config.dataColumnName;
        }

        Mvpc.view.TextCloudContainer.instances[this.id] = this;
        TextCloudContainer = this;
        this.TextCloudContainer = "Mvpc.view.TextCloudContainer.instances['" + this.id + "']";
    },

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    setDataColumnName: function (columnName) {
        this.dataColumnName = columnName;
    },

    getDataColumn: function (store, name) {
        for (var index = 0; index < store.columnInfo.allColumns.length; index++) {
            if (store.columnInfo.allColumns[index].dataIndex == name) {
                return store.columnInfo.allColumns[index];
            }
        }
        return null;
    },

    onRecordsComplete: function (event) {
        var store = event.updateObject.store;
        if (store) {
            var column = this.getDataColumn(store, this.dataColumnName);
            if (column) {
                var textList = this.generateTextList(store, column);
                if (textList && textList.length > 0)
                    TextCloudContainer.drawCloud(textList);
            }
        }
    },

    generateTextList: function (store, column) {
        var textList = [];
        if (column) {
            //tdower: is this always here if the column is? If not, generate it?
            var facetHistogram = column.ExtendedProperties.histObj.hist;
            var numShown = Math.min(textCloudMaxShown, facetHistogram.length);
            //sorted by most frequent first, so more popular terms will be shown even if the least popular don't fit.
            for (var index = 0; index < numShown; index++) {
                var adjustedWeight = Math.max(1, Math.log(facetHistogram[index].count));
                var item = { text: facetHistogram[index].key, weight: adjustedWeight };
                textList.push(item);
            }
        }
        return textList;
    },

    onRecordsSelected: function (event) {
    },

    onRecordsFiltered: function (event) {
    },

    onContextAdded: function (event) {
        //TextCloudContainer.drawCloud(event.context.searchInput.inputText);
    },

    onContextRemoved: function (event) {
    }

});