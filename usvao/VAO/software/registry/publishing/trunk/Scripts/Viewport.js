Ext.require('PublishingWizard.Layout');

Ext.define('PublishingWizard.Viewport', {
    //    extend: 'Ext.panel.Panel',
    extend: 'Ext.container.Viewport',

    statics: {},

    constructor: function (config) {
        var me = this;

        // Apply mandatory config items.       
        Ext.apply(config, {
            margin: 0,
            layout: 'fit',
            items: [

	    config.mainPanel    // In the center.
            //	   {
            //                id: 'vpCenterContainer',
            //                //title: 'Initial Search Results',
            //                region: 'center',     // center region is required, no width/height specified
            //                //border: false,
            //                xtype: 'tabpanel'
            //            }
	    ]
        });

        var mainPanel = config.mainPanel;
        delete config.mainPanel;

        this.callParent([config]);

        // Get the components from this main viewport border layout.
        this.northPanel = Ext.getCmp('vpNorthContainer');
        this.eastPanel = Ext.getCmp('vpEastContainer');
        this.westPanel = Ext.getCmp('vpWestContainer');
        this.centerPanel = Ext.getCmp('vpCenterContainer');
        this.southPanel = Ext.getCmp('vpSouthContainer');
    }
});
