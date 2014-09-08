Ext.define('Mvp.app.DownloadBasket', {
    singleton: true,

    downloadBasket: {},     // a dictionary of comma-separated strings
    lastDownloadUrl: '',    // a copy of the last bundle/etc. sent to the client

    add: function (collection, records, field) {    // adds items to the named collection doing Ext.data.Store.get(field)
        if (!Mvp.app.DownloadBasket.downloadBasket[collection]) Mvp.app.DownloadBasket.downloadBasket[collection] = '';
        for (var i = 0; i < records.length; i++) {
            var id = records[i].get(field);
            if (Mvp.app.DownloadBasket.downloadBasket[collection].indexOf(',' + id) == -1) Mvp.app.DownloadBasket.downloadBasket[collection] += ',' + id;
        }
    },

    remove: function (collection, records, field) { // removes items from the named collection doing Ext.data.Store.get(field)
        if (!Mvp.app.DownloadBasket.downloadBasket[collection]) Mvp.app.DownloadBasket.downloadBasket[collection] = '';
        for (var i = 0; i < records.length; i++) {
            var id = records[i].get(field);
            if (Mvp.app.DownloadBasket.downloadBasket[collection].indexOf(',' + id) != -1) {
                Mvp.app.DownloadBasket.downloadBasket[collection] = Mvp.app.DownloadBasket.downloadBasket[collection].replace(',' + id, '');
            }
        }
    },

    getMaxCount: function (collection) {    // returns the count of the tokens in the given collection
        if (!Mvp.app.DownloadBasket.downloadBasket[collection]) Mvp.app.DownloadBasket.downloadBasket[collection] = '';
        if (Mvp.app.DownloadBasket.downloadBasket[collection].length == 0) return 0;
        return Mvp.app.DownloadBasket.downloadBasket[collection].split(',').length - 1; // reduce count by 1 because of the lazy leading comma in a populated list
    },

    hasFiles: function () {     // checks whether there are any IDs/URLs/etc. in any collection in the basket
        for (var i in Mvp.app.DownloadBasket.downloadBasket) {
            if (Mvp.app.DownloadBasket.getMaxCount(i)) return true;
        }
        return false;
    }
});