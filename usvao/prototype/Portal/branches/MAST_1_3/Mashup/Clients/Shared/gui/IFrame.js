Ext.define('Mvp.gui.IFrame', {
    extend: 'Ext.ux.IFrame',

    initComponent: function () {
        this.callParent(arguments);
    },

    constructor: function (config) {
        this.callParent(arguments);
    },
    
    onLoad: function() {
        this.callParent(arguments);
        
        // This is a good place to do any customized initialization.
        
    }
})