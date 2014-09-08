Ext.define('Mvp.data.DecimalHistogram', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'key', type: 'double' },
        { name: 'count', type: 'int' },
    ]
});