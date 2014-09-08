Ext.define('Mvp.context.Manager', {
    extend: 'Ext.util.Observable',
    
    statics: {
        
    },
    
    constructor: function(config) {
        this.contexts = {};
        this.addEvents('storeupdated', 'contextupdated', 'contextadded', 'contextremoved');
    },
    
    // Public methods
    addContext: function(context) {
        var uid = context.uid;
        
        this.contexts[uid] = {context: context};
        
        // Add Listeners
        context.on('storeupdated', this.storeUpdated, this);
        context.on('contextupdated', this.storeUpdated, this);
        
        this.fireEvent('contextadded', this, uid);
        
        return uid;
    },
    
    removeContext: function(uid) {
        var contextInfo = this.contexts[uid];
        if (contextInfo) {
            var context = contextInfo.context;
            
            // Remove Listeners
            context.removeListener('storeupdated', this.storeUpdated, this);
            context.removeListener('contextupdated', this.storeUpdated, this);
            
            this.contexts[uid] = null;
            this.fireEvent('contextremoved', this, uid);
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
    
    // Let's try to not need the manager to manage all the stores if we can avoid it.
    // We'll see how this develops as there are more related views.
    
    //getStore: function(uid) {
    //    var contextInfo = this.contexts[uid];
    //    if (!contextInfo) {
    //        Ext.log({
    //            msg: 'Attempt to get nonexistent context in getStore().',
    //            dump: uid,
    //            level: 'warn',
    //            stack: true
    //        })
    //    }
    //    var store = null;
    //    var updateObject = contextInfo.updateObject;
    //    if (updateObject) {
    //        store = updateObject.store;
    //        if (!store && Ext.isFunction(updateObject.createStore)) {
    //            store = updateObject.createStore();
    //        }
    //    }
    //    
    //    return store;
    //},
    
    // Private methods
    storeUpdated: function(updateObject, context) {
        var contextInfo = this.contexts[context.uid];
        contextInfo.updateObject = updateObject;
        this.fireEvent('storeupdated', updateObject, context, this);
    },
    
    contextUpdated: function() {
        this.fireEvent('contextupdated', updateObject, context, this);
    }
})