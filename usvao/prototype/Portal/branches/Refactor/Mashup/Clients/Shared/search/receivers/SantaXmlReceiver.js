Ext.define('Mvp.search.receivers.SantaXmlReceiver', {
    extend: 'Ext.util.Observable',
    requires: [
        'Mvp.util.NameResolverModel'
    ],
    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.addEvents('storeupdated');
        Ext.apply(this, config);
    },
    
    onResponse: function(xml, requestOptions, queryScope, complete) {
        var resolverStore = Mvp.util.NameResolverModel.createResolverStore(xml);
        var columnInfo = this.createColumnInfo(resolverStore);
        
        var updateObject = {
            complete: true,
            updated: true,
            store: resolverStore,
            rowCount: resolverStore.data.length,
            columnInfo: columnInfo
            };
        this.fireEvent('storeupdated', updateObject);
    },
    
    onError: function() {
        
    },
        
    onFailure: function() {
        
    },
    
    createColumnInfo: function (resolverStore) {
        var columns = [
            {text: 'canonicalName', dataIndex: 'canonicalName'},
            {text: 'ra', dataIndex: 'ra'},
            {text: 'dec', dataIndex: 'dec'},
            {text: 'objectType', dataIndex: 'objectType'},
            {text: 'radius', dataIndex: 'radius'},
            {text: 'resolver', dataIndex: 'resolver'},
            {text: 'searchRadius', dataIndex: 'searchRadius'},
            {text: 'searchString', dataIndex: 'searchString'}
        ];
        var hiddenColumns = [
            {text: 'cacheDate', dataIndex: 'cacheDate'},
            {text: 'cached', dataIndex: 'cached'},
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