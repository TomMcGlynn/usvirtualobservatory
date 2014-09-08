Ext.define('Mvp.context.Context', {
    extend: 'Ext.util.Observable',
    
    statics: {
        uidCounter: 0
    },
    
    constructor: function(config) {
        this.callParent(arguments);
        
        this.addEvents('storeupdated', 'contextupdated');
        var cl = Mvp.context.Context;
        this.uid = cl.uidCounter++;
    }
});