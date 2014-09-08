
var baseDirectoryURL = "http://vaodev.stsci.edu/directory/";
var loginURL = "login.html?debug";


checkLoginInfo = function () {
    Ext.Ajax.request({
        url: 'login.aspx?action=isloggedin',
        method: 'GET',
        success: function (result, request) {
            var json = Ext.decode(result.responseText);
            if (json && json.success == true) {
                //Ext.getCmp('buttonLogin').hide();
                Ext.getCmp('labelUserName').setValue('logged in as user ' + json.details);
            }
            else {
                //Ext.getCmp('buttonLogout').hide();    
            }
        },
        failure: function (result, request) {
            Ext.Msg.alert('Could not determine login information. ', result.responseText, function (btn, text) {
                if (btn == 'ok')
                    window.location = loginURL;
            });
        }
    })
};


Ext.define('PublishingWizard.HelpWizard', {
    extend: 'Ext.panel.Panel',

    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.HelpWizard', options);
            wizard.run(options);
        },
    },

    
    run: function (options) {
        var me = this;
        app = me;

        // Create the main panel with a border layout.
	    me.mainPanel =  Ext.create('PublishingWizard.HelpWizard', {
	        renderTo: options.mainDiv
	        //border: false
	        });
    },

    constructor: function (config) {
        var me = this;

        // Apply mandatory config items.       
        Ext.apply(config, {
            border: 0,
            layout: 'fit',
            name: 'centerViewport',
            id: 'centerViewport',
            defaults: {
                autoScroll: 'true',
                autoHeight: 'true',
                layout: 'fit'
            },
            items: [
            {
                xtype: 'form',
                id: 'formPanel',
                border: 0,
                margin: 0,
                autoscroll: true,
                layout: 'fit',
                waitMsgTarget: true,
                url: './TestPublishResource.aspx',
                method: 'GET',
                fieldDefaults: {
                    labelWidth: 100,
                    width: 400,
                    msgTarget: 'side'
                },

                // configure how to read the XML data
                reader: Ext.create('Ext.data.reader.Xml', {
                    model: 'PublishingWizard.Resource',
                    record: 'ri:Resource',
                    successProperty: '@success'
                }),

                listeners: {
                    exception: function (proxy, response, operation) {
                    }
                },

                items: [{
                    xtype: 'panel',
                    id: 'titlePanel',
                    autoScroll: true,
                    border: 0,
                    margin: 0,
                    layout: 'hbox',
                    defaults: { border: 0, height: 50 },
                    items: [{ width: 20, autoEl: { tag: 'div'} },
                            { width: 100, autoEl: { tag: 'img', src: 'scripts/data/images/VAO_logo_100.png', alt: 'VAO Logo'} },
                            { width: 80, autoEl: { tag: 'div'} },
                            { autoEl: { tag: 'h1', html: 'VAO Registry Publishing'} },
                            { width: 100, autoEl: { tag: 'div'} },
                            {
                                xtype: 'panel',
                                id: 'loginPanel',
                                border: 0,
                                margin: 0,
                                layout: 'vbox',
                                items: [
                                {
                                    xtype: 'panel',
                                    id: 'loginButtonsPanel',
                                    border: 0,
                                    margin: 0,
                                    width: 400,
                                    layout: 'hbox',
                                    items: [
                                        {
                                            width: 200,
                                            xtype: 'button',
                                            text: 'login as new user',
                                            id: 'buttonLogin',
                                            handler: function () {
                                                window.location = loginURL;
                                            }
                                        },
                                        {
                                            xtype: 'button',
                                            text: 'logout',
                                            id: 'buttonLogout',
                                            handler: function () {
                                                Ext.Ajax.request({
                                                    url: 'login.aspx?action=logout',
                                                    method: 'GET',
                                                    success: function (result, request) {
                                                        Ext.Msg.alert('Success', 'Logged out successfully', function (btn, text) {
                                                            if (btn == 'ok') {
                                                                window.location = loginURL;
                                                            }
                                                        });
                                                    },
                                                    failure: function (result, request) {
                                                        Ext.Msg.alert('Logout Failed', result.responseText);
                                                    }
                                                })
                                            }
                                        }] //hbox
                                },
                                  {
                                      xtype: 'panel',
                                      layout: 'hbox',
                                      border: 0,
                                      margin: 0,
                                      width: 250,
                                      height: 20,
                                      items: [
                                      {
                                          xtype: 'displayfield',
                                          fieldLabel: '',
                                          id: 'labelUserName',
                                          value: '',
                                          width: 200,
                                          height: 20
                                      },
                                    { width: 75, height: 20, border: 0, margin: 0, autoEl: { tag: 'a', html: 'Help', href: 'help.html', target: "_blank"} }
                                    ] //hbox 2
                                  }] //vbox
                            }] //titlepanel
                }] //centerpanel items
        }]
    }); //viewport

    // Apply defaults for config.       
    Ext.applyIf(config, {
        width: 1100,
        autoScroll: true
    });

    this.callParent([config]);

    this.centerPanel = Ext.getCmp('centerViewport');
    checkLoginInfo();
}
});
