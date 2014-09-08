Ext.define('Mvp.app.App', {
    extend: 'Ext.util.Observable',
    requires: ['Mvp.app.EventManager'],
    
    statics: {
        
    },
    
    constructor: function(config) {
        
        this.contexts = {};
        this.eventMgr = Ext.create('Mvp.app.EventManager', {
            intercept: true
        });
        this.addEvents('APP.context.added', 'APP.context.removed');
        
    },
    
    // Public methods
    
    getEventManager: function() {
        return this.eventMgr;
    },
    
    addContext: function(context) {
        var uid = context.uid;
        
        this.contexts[uid] = {context: context};
        
        // Add Listeners
        context.on('storeupdated', this.storeUpdated, this);
        
        this.fireEvent('APP.context.added', {
            type: 'APP.context.added',
            context: context,
            uid: uid
        });

        return uid;
    },
    
    removeContext: function(uidOrContext) {
        var uid = null;
        
        if (Ext.isString(uidOrContext)) {
            uid = uidOrContext;
        } else {
            uid = uidOrContext.uid;
        }
        var contextInfo = this.contexts[uid];
        if (contextInfo) {
            var context = contextInfo.context;
            
            // Remove Listeners
            context.removeListener('storeupdated', this.storeUpdated, this);
            
            this.contexts[uid] = null;
            this.fireEvent('APP.context.removed', {
                type: 'APP.context.removed',
                context: context,
                uid: uid
            });
        } else {
            Ext.log({
                msg: 'Attempt to remove nonexistent context.',
                dump: uid,
                level: 'warn',
                stack: true
            })
        }
    },
    
    getContext: function(uid) {
        var contextInfo = this.contexts[uid];
        if (!contextInfo) {
            Ext.log({
                msg: 'Attempt to get nonexistent context.',
                dump: uid,
                level: 'warn',
                stack: true
            })
        }
        return contextInfo.context;
    },
    
    setActiveContext: function(context) {
        var oldActive = this.activeContext;
        if (!oldActive || (oldActive.uid !== context.uid)) {
            if (oldActive) {
                oldActive.deactivate();
            }
            this.activeContext = context;
            this.activeContext.activate();
        }
    },
    
    getActiveContext: function() {
        return this.activeContext;
    },

    // Private methods
    storeUpdated: function(updateObject, context) {
        // Don't know that we need to do anything for this.
        //var contextInfo = this.contexts[context.uid];
        //contextInfo.updateObject = updateObject;
        //this.fireEvent('storeupdated', updateObject, context, this);
    }
    
})