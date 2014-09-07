Ext.require('PublishingWizard.ManagementLayout');

var app = null;

Ext.define('PublishingWizard.ResourceManagementWizard', {
    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.ResourceManagementWizard', options);
            wizard.run(options);
        },
    },

 
    constructor: function (config) {
        var me = this;
        Ext.apply(me, config);
    },

    run: function (options) {
        var me = this;
        app = me;

        // Create the main panel with a border layout.
	    me.mainPanel =  Ext.create('PublishingWizard.ManagementLayout', {
	        renderTo: options.mainDiv
	        //border: false
	        });
    }
});
