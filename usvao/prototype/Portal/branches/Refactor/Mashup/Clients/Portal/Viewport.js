Ext.require('DemoApp.Layout');

Ext.define('DemoApp.Viewport', {
//    extend: 'Ext.panel.Panel',
    extend: 'Ext.container.Viewport',
    
    statics: {},
    
    constructor: function(config) {
        var me = this;
        
        // Apply mandatory config items.       
        Ext.apply(config, {
            margin: 0,
            layout: 'border',
            items: [
//		{
//                id: 'vpNorthContainer',
//                region: 'north',     // position for region
//                xtype: 'panel',
//                layout: 'fit',
//                border: false,
//                height: 76
//                //margins: '0 0 0 0'
//            },
//	    {
//                id: 'vpSouthContainer',
//                region: 'south',     // position for region
//                xtype: 'panel',
//                autoScroll: true,
//                height: 225,
//                split: true         // enable resizing
//            },
            //{
            //    id: 'tlEastContainer',
            //    title: 'East Region',
            //    region:'east',
            //    xtype: 'panel',
            //    width: 100,
            //    collapsible: true,   // make collapsible
            //    layout: 'fit'
            //},
            //{
            //    id: 'tlWestContainer',
            //    title: 'Filters',
            //    region:'west',
            //    xtype: 'panel',
            //    width: 300,
            //    split: true,
            //    collapsible: true,   // make collapsible
            //    autoScroll: true,
            //    margins: '0 10 0 0'
            //},
	    
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
        //this.southPanel = Ext.getCmp('vpSouthContainer');
        this.eastPanel = Ext.getCmp('vpEastContainer');
        this.westPanel = Ext.getCmp('vpWestContainer');
        this.centerPanel = Ext.getCmp('vpCenterContainer');
        
        //this.centerPanel.add(mainPanel);
        

    }
});
