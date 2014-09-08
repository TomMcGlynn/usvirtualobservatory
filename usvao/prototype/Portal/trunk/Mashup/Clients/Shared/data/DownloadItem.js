Ext.define('Mvp.data.DownloadItem', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'obsID', type: 'string' },
        { name: 'obsid', type: 'string' },
        { name: 'obs_collection', type: 'string' },
        { name: 'dataproduct_type', type: 'string' },
        { name: 'type', type: 'string' },
        { name: 'description', type: 'string' },
        { name: 'productGroupDescription', type: 'string' },
        { name: 'productSubGroupDescription', type: 'string' },
        { name: 'documentation', type: 'string'},
        { name: 'dataURI', type: 'string' }
    ]
});