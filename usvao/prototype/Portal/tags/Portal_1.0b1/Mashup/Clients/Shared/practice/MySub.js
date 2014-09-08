
Ext.define('Mvp.practice.MySub', {
    extend: 'Mvp.practice.MyBase',
    
    // Constructor
    constructor: function(config) {
        //config = config || {};  // ensure config is defined
        //var me = this;
                
        // Call the superclass' constructor.
        Ext.log('before parent call');
        this.callParent(arguments);
        Ext.log('after parent call');
    }
    
    // Methods
    //testMethod: function() {
    //    me = this;
    //    Ext.log('sub prop1: ' + me.prop1);
    //    Ext.log('sub prop2: ' + me.prop2);
    //    Ext.log('sub prop3: ' + me.prop3);
    //}
    

    
})