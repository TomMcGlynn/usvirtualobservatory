Ext.define('Mvp.app.EventManager', {
    extend: 'Ext.util.Observable',
    
    requires: 'Ext.Component',
    
    statics: {
        supportedEvents: [
            'APP.context.added',
            'APP.context.removed',
            'APP.context.records.selected',
            'APP.context.records.filtered',
            'APP.context.records.lead.selected',
            'APP.context.color.changed',
            'APP.context.position.changed'
        ]
    },
    
    constructor: function(config) {
        this.callParent(arguments);
        
        // Add the events we'll handle.
        var cl = Ext.util.Observable;
        for (var e in cl.supportedEvents) {
            this.addEvents(cl.supportedEvents[e]);
        }
        
        if (config.intercept) {
            this.interceptAllEvents();
        }
    },
    
    interceptAllEvents: function() {
        var observer = this;
        
        this.overrideFireEventOnObservable(Ext.util.Observable, this);
        this.overrideFireEventOnObservable(Ext.Component, this);

        //this.setLogging(true);
    },
    
    overrideFireEventOnObservable: function(classToOverride, observer) {
        Ext.override(classToOverride, {
            fireEvent: function(eventName) {
                this.callParent(arguments);
                
                var source = this;
                
                // Log all events(?)
                observer.logEvent(source, arguments);
                
                // Intercept and refire our events.
                if (eventName.match(/^APP\./)) {
                    // For APP events, assume that the first arg is the msg type,
                    // and the second arg is the msg object.  We will poke the
                    // msg source into the msg object as the 'sender' property.
                    var msg = arguments[1];
                    msg['sender'] = source;
                    observer.fireInterceptedEvent(source, arguments);
                }
            }
        });       
    },
    
    fireInterceptedEvent: function(source, arguments) {
        this.fireEventOriginal.apply(this, arguments);
    },
    
    fireEventOriginal: function(eventName) {
        var name = eventName.toLowerCase(),
            events = this.events,
            event = events && events[name],
            bubbles = event && event.bubble;

        return this.continueFireEvent(name, Ext.Array.slice(arguments, 1), bubbles);
    },
    
    logEvent: Ext.emptyFn,
    
    emptyFn: Ext.emptyFn,
    
    realLog: function(source, arguments) {
        Ext.log('Event fired: ' + arguments[0]);
    },
    
    setLogging: function(enable) {
        if (enable) {
            this.logEvent = this.realLog;
        } else {
            this.logEvent = this.emptyFn;
        }
    },
    
    // override:
    // addListener( String/Object eventName, [Function fn], [Object scope], [Object options] )
    addMultiListener: function(eventName, fn, scope, options) {
        
        // Find events that match the eventName.
        var foundMatch = false;
        var re = new RegExp(eventName);
        var cl = Mvp.app.EventManager;
        for (var i in cl.supportedEvents) {
            var e = cl.supportedEvents[i];
            if (e.match(re)) {
                this.addListener(e, fn, scope, options);
                foundMatch = true;
            }
        }
        
        if (!foundMatch) {
            Ext.log({
                msg: 'No event found matching /' + eventName + '/',
                level: 'warn'
            });
        }
    }
    
})