Ext.define('Mvp.data.Histogram', {
    extend: 'Ext.data.Model',
    idProperty: 'key',
    fields: [
        {name: 'key',     type: 'string'},
        {name: 'count',      type: 'int'}
    ]
});