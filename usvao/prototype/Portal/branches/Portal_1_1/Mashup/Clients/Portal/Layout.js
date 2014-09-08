
Ext.define('DemoApp.Layout', {
 //   extend: 'Ext.panel.Panel',
    extend: 'Ext.container.Container',
    
    statics: {},
    
    constructor: function(config) {
        var me = this;
        
        // Get variables from config
        me.sourceGrid = config.sourceGrid;
        delete config.sourceGrid;
	
	var items = [{
                id: 'tlNorthContainer',
                //title: 'North Region',
                region: 'north',     // position for region
                xtype: 'panel',
                layout: 'fit',
                border: false,
                height: 90
                //margins: '0 0 0 0'
            },
            //{
            //    id: 'tlEastContainer',
            //    title: 'East Region',
            //    region:'east',
            //    xtype: 'panel',
            //    width: 100,
            //    collapsible: true,   // make collapsible
            //    layout: 'fit'
            //},
            {
                id: 'tlWestContainer',
                title: 'Filters',
                region:'west',
                xtype: 'panel',
                layout: 'fit',
                floatable: false,
                //border: false,
                width: 270,
                split: true,
                collapsible: true,   // make collapsible
                autoScroll: true,
                margins: '0 10 0 0'
            },{
                id: 'tlCenterContainer',
                //title: 'Initial Search Results',
                region: 'center',     // center region is required, no width/height specified
                //border: false,
                xtype: 'tabpanel'
            }];
	
	if (useAV) {
	    items.push({
                id: 'tlSouthContainer',
                //title: 'Data',
                region: 'south',     // position for region
                xtype: 'panel',
               // layout: 'fit',
                //border: false,
                autoScroll: true,
                height: 500,
                split: true         // enable resizing
            });
	    
	}
        
        // Apply mandatory config items.       
        Ext.apply(config, {
	    //border: false,
            margin: 0,
            //title: 'Border Layout',
            layout: 'border',
            items: items
        });
        
        // Apply defaults for config.       
        Ext.applyIf(config, {
            width: 1100,
            height: 790
        });
        
        this.callParent([config]);
        
        // This panel with a border layout has been initialized, so we should be able to get the regional containers.
        this.northPanel = Ext.getCmp('tlNorthContainer');
        if (useAV) this.southPanel = Ext.getCmp('tlSouthContainer');
        this.eastPanel = Ext.getCmp('tlEastContainer');
        this.westPanel = Ext.getCmp('tlWestContainer');
        this.centerPanel = Ext.getCmp('tlCenterContainer');
        

    }
});
