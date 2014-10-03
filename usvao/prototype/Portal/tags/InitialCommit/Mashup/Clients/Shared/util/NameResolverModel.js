Ext.define('Mvp.util.NameResolverModel',{
    extend: 'Ext.data.Model',
    
    statics: {
        createResolverStore: function(xmlDocument) {
            var store = Ext.create('Ext.data.Store', {
                model: 'Mvp.util.NameResolverModel',
                autoLoad: true,
                data: xmlDocument,
                proxy: {
                    type: 'memory',
                    reader: {
                        type: 'xml',
                        record: 'resolvedCoordinate'
                    }
                }
            });    
            return store;    
        },
        
        // This method doesn't really belong here as it will be typical
        // to want to customize the grid, but it serves as an example
        // of how one might use a resolver store.
        createResolverGrid: function(store) {
        
            // create the Grid
            var grid = Ext.create('Ext.grid.Panel', {
                store: store,
                columns: [
                    //{text: 'cacheDate', dataIndex: 'cacheDate'},
                    //{text: 'cached', dataIndex: 'cached'},
                    {text: 'canonicalName', dataIndex: 'canonicalName'},
                    {text: 'ra', dataIndex: 'ra'},
                    {text: 'dec', dataIndex: 'dec'},
                    {text: 'objectType', dataIndex: 'objectType'},
                    {text: 'radius', dataIndex: 'radius'},
                    {text: 'resolver', dataIndex: 'resolver'},
                    {text: 'searchRadius', dataIndex: 'searchRadius'},
                    {text: 'searchString', dataIndex: 'searchString'}
                ],
                stateful: true,
                stateId: 'resolverGridState',
                border: 0,
                height: 50,
                width: 800
                //title: 'Resolved positions'
            });
            return grid;
        },
        
        // This method doesn't really belong here as it will be typical
        // to want to customize the grid, but it serves as an example
        // of how one might use a resolver store.
        getConeSearchParams: function(resolverRecord) {
            var coneSearchParams;
            if (resolverRecord) {
                var ra = resolverRecord.get('ra');
                var dec = resolverRecord.get('dec');
                var radius = resolverRecord.get('searchRadius');
                if (!radius || (radius <= 0)) {
                    radius = 0.2;  // Default radius to 0.2 degrees.
                }
                if (ra && dec) {
                    coneSearchParams = {ra: ra, dec: dec, radius: radius, verb: 3};
                }
            }
            return coneSearchParams;
        }

    },
    
    fields: [
        {name: 'cacheDate', type: 'string'},
        {name: 'cached',   type: 'boolean'},
        {name: 'canonicalName', type: 'string'},
        {name: 'dec', type: 'float'},
        {name: 'objectType', type: 'string'},
        {name: 'ra', type: 'float'},
        {name: 'radius', type: 'string'},
        {name: 'resolver', type: 'string'},
        {name: 'searchRadius', type: 'string'},
        {name: 'searchString', type: 'string'}
     ]
});               