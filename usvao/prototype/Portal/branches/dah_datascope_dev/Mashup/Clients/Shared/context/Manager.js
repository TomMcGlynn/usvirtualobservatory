Ext.define('Mvp.context.Manager', {
    extend: 'Ext.util.Observable',
    
    statics: {
        
    },
    
    constructor: function(config) {
        this.callParent(arguments);
        this.contexts = {};
        this.addEvents('storeupdated', 'contextupdated', 'contextadded', 'contextremoved', 'avmsg');
    },
    
    // Public methods
    addContext: function(context) {
        var uid = context.uid;
        
        this.contexts[uid] = {context: context};
        
        // Add Listeners
        context.on('storeupdated', this.storeUpdated, this);
        context.on('contextupdated', this.storeUpdated, this);
        
        this.fireEvent('contextadded', this, uid);
        
        // Astroview testing.  Send a ping, expect a response...
        this.sendAvMsg({
            type: 'ping', // 'pingresponse', 'select', 'getinfo', 'info'
            data: {
                name: uid,
                attribs: {},
                rows: [],
                ids: [],
                debug: 'ping msg: search context added to web client, id: ' + uid
            }
        });
        
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

    // Private methods
    storeUpdated: function(updateObject, context) {
        var contextInfo = this.contexts[context.uid];
        contextInfo.updateObject = updateObject;
        this.fireEvent('storeupdated', updateObject, context, this);
    },
    
    contextUpdated: function() {
        this.fireEvent('contextupdated', updateObject, context, this);
    },
    
    // AstroView messaging.
    
    // Send a message to AstroView
    // msg looks like:
    //    var msg = {
    //        type: 'PING', // 'PING_RESPONSE', 'SELECT', 'GET_INFO', 'GET_INFO_RESPONSE'
    //        data: {
    //            name: 'contextId',
    //            attribs: {},
    //            rows: [],
    //            ids: [],
    //            debug: 'whatever I want'
    //        }
    //    };
    sendAvMsg: function(msg) {
        this.fireEvent('avmsg', msg);
    },
    
    // Receive a message from AstroView
    onAvMsg: function(msg) {
        Ext.log('AV mgs received, type = ' + msg.type + ', name = ' + msg.name);
        
        switch (msg.type) {
            case 'PING':
                // Respond
                msg.type = 'PING_RESPONSE';
                Ext.defer(this.sendAvMsg, 10, this, [msg]);
                break;
            case 'PING_RESPONSE':
                break;
            default:
                // Nothing for now.
        }
    }
})