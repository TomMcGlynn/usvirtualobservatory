Ext.require(['*']);

Ext.onReady(function() {
    var cw;
    
    Ext.create('Ext.Viewport', {
        layout: {
            type: 'border',
            padding: 5
        },
        defaults: {
            split: true
        },
        items: [{
            region: 'center',
            layout: 'border',
            border: false,
            items: [{
                region: 'center',
                border: false,
                html: 'center',
                title: 'HLSP Projects'
            },{
                region: 'south',
                height: 300,
                split: true,
                collapsible: true,
                title: 'HLSP Products',
                html: 'Select HLSP Project above to view Products.'
            }]
        }]
    });
});