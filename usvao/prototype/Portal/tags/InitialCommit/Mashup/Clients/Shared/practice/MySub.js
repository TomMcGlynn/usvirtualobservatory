
Ext.define('Mvp.practice.MySub', {
    extend: 'Mvp.practice.MyBase',
    
    // Constructor
    constructor: function(config) {
        //config = config || {};  // ensure config is defined
        //var me = this;
                
        // Call the superclass' constructor.
        console.log('before parent call');
        this.callParent(arguments);
        console.log('after parent call');
    }
    
    // Methods
    //testMethod: function() {
    //    me = this;
    //    console.log('sub prop1: ' + me.prop1);
    //    console.log('sub prop2: ' + me.prop2);
    //    console.log('sub prop3: ' + me.prop3);
    //}
    

    
})