Ext.require([
    'Ext.data.*',
    'Ext.grid.*'
]);

Ext.onReady(function(){
    Ext.define('resolvedCoordinate',{
        extend: 'Ext.data.Model',
        fields: [
            // set up the fields mapping into the xml doc
            // The first needs mapping, the others are very basic
            {name: 'cacheDate',  mapping: 'cacheDate', type: 'string'},
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

        //Ext.define('resolvedCoordinate',{
        //    extend: 'Ext.data.Model',
        //    fields: [
        //    {name: 'cacheDate',  mapping: 'cacheDate', type: 'string'},
        //    {name: 'cached',   type: 'boolean'},
        //    {name: 'canonicalName', type: 'string'},
        //    {name: 'dec', type: 'float'},
        //    {name: 'objectType', type: 'string'},
        //    {name: 'ra', type: 'float'},
        //    {name: 'radius', type: 'string'},
        //    {name: 'resolver', type: 'string'},
        //    {name: 'searchRadius', type: 'string'},
        //    {name: 'searchString', type: 'string'}
        //    ]
        //});

     //create the Data Store
    var store = Ext.create('Ext.data.Store', {
        model: 'resolvedCoordinate',
        autoLoad: true,
        proxy: {
            // load using HTTP
            type: 'ajax',
            url: '../../Clients/VOT/query.xml',
            // the return will be XML, so lets set up a reader
            reader: {
                type: 'xml',
                // records will have an "Item" tag
                record: 'resolvedCoordinate',
                //idProperty: 'ASIN',
                //totalRecords: '@total'
            }
        }
    });

        //var store = new Ext.data.Store({
        //  //  data: text,
        //    autoload: true,
        //    model: 'resolvedCoordinate',
        //    proxy: {
        //        type: 'ajax',
        //        url: '../../Clients/VOT/query.xml',
        //       reader: {
        //            type: 'xml',
        //            //root: 'resolvedItems',
        //            record: 'resolvedCoordinate'
        //        }
        //    }
        //});
        //
        
    // create the grid
    var grid = Ext.create('Ext.grid.Panel', {
        store: store,
        columns: [
            {text: 'cacheDate', dataIndex: 'cacheDate'},
            {text: 'cached', dataIndex: 'cached'},
            {text: 'canonicalName', dataIndex: 'canonicalName'},
            {text: 'dec', dataIndex: 'dec'},
            {text: 'objectType', dataIndex: 'objectType'},
            {text: 'ra', dataIndex: 'ra'},
            {text: 'radius', dataIndex: 'radius'},
            {text: 'resolver', dataIndex: 'resolver'},
            {text: 'searchRadius', dataIndex: 'searchRadius'},
            {text: 'searchString', dataIndex: 'searchString'}
         ],
            stateful: true,
            stateId: 'stateGrid',
        renderTo:'example-grid',
        width: 540,
        height: 200
    });
    
        //    var grid = Ext.create('Ext.grid.Panel', {
        //    store: store,
        //    columns: [
        //    {text: 'cacheDate', dataIndex: 'cacheDate'},
        //    {text: 'cached', dataIndex: 'cached'},
        //    {text: 'canonicalName', dataIndex: 'canonicalName'},
        //    {text: 'dec', dataIndex: 'dec'},
        //    {text: 'objectType', dataIndex: 'objectType'},
        //    {text: 'ra', dataIndex: 'ra'},
        //    {text: 'radius', dataIndex: 'radius'},
        //    {text: 'resolver', dataIndex: 'resolver'},
        //    {text: 'searchRadius', dataIndex: 'searchRadius'},
        //    {text: 'searchString', dataIndex: 'searchString'}
        //    ],
        //    stateful: true,
        //    stateId: 'stateGrid',
        //    height: 350,
        //    width: 800,
        //    renderTo:'example-grid',
        //    title: 'Resolved positions'
        //});
        

});
