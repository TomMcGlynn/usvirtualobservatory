Ext.define('Mvp.util.NameResolverModel',{
    extend: 'Ext.data.Model',
     requires: ['Mvp.util.Coords', 'Ext.Number'],
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
                if ((radius == null) || (radius < 0)) {
                    radius = 0.2;  // Default radius to 0.2 degrees.
                }
                if ((ra !== null) && (dec !== null)) {
                    coneSearchParams = {ra: ra, dec: dec, radius: radius, verb: 3};
                }
            }
            return coneSearchParams;
        },

        getResolverSummaryString: function(resolverRecord) {
            var resolverSummaryString = null;
            if (resolverRecord) {
                var name = resolverRecord.get('canonicalName');
                var ra = resolverRecord.get('ra');
                var dec = resolverRecord.get('dec');
                var radius = resolverRecord.get('searchRadius');
                if (!radius || (radius <= 0)) {
                    radius = 0.2;  // Default radius to 0.2 degrees.
                }
                if (ra && dec) {
                    coneSearchParams = {ra: ra, dec: dec, radius: radius, verb: 3};
                }
                
                // Create summary string.
                var raDecRadStr = 'RA: <b>' + ra + '&#176;</b>, Dec: <b>' + dec + '&#176;</b>';
                var c = Mvp.util.Coords;
                if (c.positionDisplayStyle == c.SEXAGESIMAL) {
                    raDecRadStr = 'RA: <b>' + c.posDisplayValue(ra, 15) +
                    '</b>, Dec: <b>' + c.posDisplayValue(dec) + '</b>';
                }
                
                var radStr = ', radius: ' + radius + '&#176;';
                var radiusNum = Ext.Number.from(radius, 9999);
                if (radiusNum != 9999) {
                    radStr = ', radius: ' + Ext.Number.toFixed(radiusNum, 5) + '&#176;';
                }
                if (name && name != '') {
                    resolverSummaryString = '<b>' + name + '</b> (' + raDecRadStr + ')' + radStr;
                } else {
                    resolverSummaryString = raDecRadStr + radStr;
                }
            }

            return resolverSummaryString;
        },

        getResolverSummaryPanel: function(resolverRecord) {
            var resolverSummaryPanel = null;
            var resolverSummaryString = Mvp.util.NameResolverModel.getResolverSummaryString(resolverRecord);
            if (resolverSummaryString) {
                resolverSummaryPanel = new Ext.form.field.Display({
                    //value: '<pre>' + resolverSummaryString + '</pre>'
                    value: resolverSummaryString
                });

            }
            return resolverSummaryPanel;
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
