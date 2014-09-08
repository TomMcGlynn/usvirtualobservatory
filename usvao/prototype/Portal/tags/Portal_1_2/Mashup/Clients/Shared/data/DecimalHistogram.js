Ext.define('Mvp.data.DecimalHistogram', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'bucket', type: 'double' },
        { name: 'ratio', type: 'double' },
    ]
});