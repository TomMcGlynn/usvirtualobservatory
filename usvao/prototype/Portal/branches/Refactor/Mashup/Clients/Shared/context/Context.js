Ext.define('Mvp.context.Context', {
    extend: 'Ext.util.Observable',
    requires: [
        'Mvp.gui.AvAdaptor'
    ],
    
    statics: {
        uidCounter: 0
    },
    
    constructor: function(config) {
        this.callParent(arguments);
        this.useAv = config.useAv;
        
        this.addEvents('storeupdated', 'contextupdated');
        var cl = Mvp.context.Context;
        this.uid = cl.uidCounter++;
        
        if (this.useAv) {
            this.avContext = Ext.create('Mvp.gui.AvAdaptor', {
                controller: this
            });
        }
    },
    
    onBeforeClose: function(panel, eOpts) {
        this.closing = true;
        if (this.avContext) {
            Ext.callback(this.avContext.onBeforeClose, this.avContext, [panel, eOpts]); 
        }
        this.cancel();
    },
    
    cancel: function() {
        
    },
    
    activate: function() {
        
    },
    
    deactivate: function() {
        
    },
    
    setAvEnabled: function(enabled) {
        if (this.avContext) {
            this.avContext.setEnabled(enabled);
        }
    },
    
    setAvColor: function(color) {
        if (this.avContext) {
            this.avContext.setColor(color);
        }
    }
});