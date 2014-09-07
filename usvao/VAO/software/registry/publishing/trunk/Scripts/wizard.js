Ext.require('PublishingWizard.Layout');
Ext.require('PublishingWizard.Viewport');
Ext.require('PublishingWizard.CardManager');

Ext.define('PublishingWizard.Wizard', {
    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.Wizard', options);
            wizard.run();
        },
    },

    titlePanelWidth: 700,
    fieldGapWidth: 10,

    constructor: function (config) {
        var me = this;
        Ext.apply(me, config);
    },

    run: function () {
        var me = this;

        // Create the main panel with a border layout.
	    me.mainPanel =  Ext.create('PublishingWizard.Layout', {
	    //renderTo: Ext.getBody()
	    region: 'center'     // center region is required, no width/height specified
	    //border: false
	});

        me.north = me.mainPanel.northPanel;
        me.setupNorthPanel();

        me.south = me.mainPanel.southPanel;
        me.east = me.mainPanel.eastPanel;
        me.west = me.mainPanel.westPanel;

	    // Create the container Viewport.
        me.viewport = Ext.create('PublishingWizard.Viewport', {
	    renderTo: Ext.getBody(),
	    mainPanel: me.mainPanel
        });
	
        me.center = me.mainPanel.centerPanel;
        me.center.on('tabchange', this.centerTabChange, this);
        me.tabContainer = me.center; 
        me.setupCenterTab();

    },

    setupCenterTab: function (titleText)
    {
	    var options = {
	        title: titleText,
	        app: this
	    };
        PublishingWizard.CardManager.activate(options);
    },

    centerTabChange: function (tabPanel, newCard, oldCard) {
        if (Ext.isFunction(newCard.tabSelected)) {
            newCard.tabSelected();
        }
    },	


   setupNorthPanel: function () {
        var me = this;
	    var sep1 = Ext.create('Ext.Component', {width: 20});
	    var logo = Ext.create('Ext.Component', {width: 100, autoEl: {tag: 'img', src: 'scripts/data/images/VAO_logo_100.png', alt:'VAO Logo'}});
	    var sep2 = Ext.create('Ext.Component', {width: 50});
	    me.top = Ext.create('Ext.panel.Panel', {
                layout: {
                    type: 'hbox',
		    align: 'middle'
                },
                border: 0
            });


            me.titlePanel = Ext.create('Ext.panel.Panel', {
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                width: me.titlePanelWidth,
                height: 72,  // Height set by Layout.js is 76
                border: 0
            });
	   
            me.top.add(sep1);
	        me.top.add(logo);
	        me.top.add(sep2);
	        me.top.add(me.titlePanel);

            me.titlePanel.add(new Ext.form.field.Display({
        	    height: 8,
                value: '&nbsp;'
            }));
           me.titlePanel.add(new Ext.Component ({width: me.titlePanelWidth, autoEl: {tag: 'h1', html: 'VAO Registry Publishing'}}));

           me.north.add(me.top);
    },


})