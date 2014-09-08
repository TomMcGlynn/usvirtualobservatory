Ext.define('Mvp.search.receivers.SantaXmlReceiver', {
    extend: 'Ext.util.Observable',
    requires: [
        'Mvp.util.NameResolverModel'
    ],

    constructor: function (config) {
        this.callParent(arguments);

        this.addEvents('storeupdated');
        Ext.apply(this, config);
    },

    onResponse: function (xml, requestOptions, queryScope, complete) {
        var resolverStore = Mvp.util.NameResolverModel.createResolverStore(xml);
        var columnInfo = this.createColumnInfo(resolverStore);
        if (!resolverStore.count()) alert('Could not resolve ' + queryScope.request.params.input + ' to a position');

        var updateObject = {
            complete: true,
            updated: true,
            store: resolverStore,
            rowCount: resolverStore.data.length,
            columnInfo: columnInfo
        };
        this.fireEvent('storeupdated', updateObject);
    },

    onError: function () {

    },

    onFailure: function () {

    },

    createColumnInfo: function (resolverStore) {
        var columns = [
            { text: 'Canonical Name', dataIndex: 'canonicalName' },
            { text: 'RA', dataIndex: 'ra' },
            { text: 'Declination', dataIndex: 'dec' },
            { text: 'Object Type', dataIndex: 'objectType' },
            { text: 'Radius', dataIndex: 'radius' },
            { text: 'Resolver', dataIndex: 'resolver' },
            { text: 'Search Radius', dataIndex: 'searchRadius' },
            { text: 'Search String', dataIndex: 'searchString' }
        ];
        var hiddenColumns = [
            { text: 'cacheDate', dataIndex: 'cacheDate' },
            { text: 'cached', dataIndex: 'cached' },
        ];
        var allColumns = columns.concat(hiddenColumns);

        var columnInfo = {
            fields: resolverStore.fields,
            columns: columns,
            hiddenColumns: hiddenColumns,
            allColumns: allColumns
        };

        return columnInfo;
    }

})