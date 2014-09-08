
Ext.define('Mvp.practice.MyBase', {
    // Statics
    statics: {
        testStatic: function(msg) {
            console.log('testStatic: ' + msg);
        }
    },

    // Config options (only reliably specified on object creation)
    prop1: 'a',
    prop2: 'b',
    
    // Other properties
    
    // Constructor
    constructor: function(config) {
        console.log('in base constructor');
        config = config || {};  // ensure config is defined
        var me = this;
        
        // Put all the configs into this object.
        Ext.apply(me, config);
    },
    
    // Methods
    testMethod: function() {
        me = this;
        console.log('prop1: ' + me.prop1);
        console.log('prop2: ' + me.prop2);
        console.log('prop3: ' + me.prop3);
    }
    
})