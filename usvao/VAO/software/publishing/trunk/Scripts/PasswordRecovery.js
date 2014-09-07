var resourceManagementURL = "resourcemanagement.html?debug";


ResetHandler = function () {
    Ext.getCmp('recoveryForm').getForm().submit({
        method: 'POST',
        waitTitle: 'Connecting',
        waitMsg: 'Sending data...',

        success: function () {
            //Ext.Msg.alert('Status', 'Login Successful!', function (btn, text) {
                //if (btn == 'ok') {
                    window.location = resourceManagementURL;
                //}
            //});
        },

        failure: function (form, action) {
            if (action.failureType == 'server') {
                obj = Ext.decode(action.response.responseText);
                Ext.Msg.alert('Login Failed!', obj.errors.reason);
            } else {
                Ext.Msg.alert('Warning!', 'Missing login information or authentication server is unreachable.');
            }
        }
    });
}

Ext.define('PublishingWizard.RecoveryLayout', {
    extend: 'Ext.panel.Panel',

    statics: {},

    constructor: function (config) {
        var me = this;

        // Apply mandatory config items.       
        Ext.apply(config, {
            border: 0,
            layout: 'fit',
            name: 'centerViewport',
            id: 'centerViewport',
            bodyStyle: 'background: transparent;',
            defaults: {
                autoScroll: 'true',
                autoHeight: 'true',
            },
            items: [{
                                xtype: 'form',
                                labelWidth: 80,
                                margin: 50,
                                width: 500,
                                url: 'login.aspx?action=reset',
                                frame: true,
                                title: 'Please Login to the US VAO Registry Publishing System',
                                defaultType: 'textfield',
                                id: 'recoveryForm',
                                monitorValid: true,
                                bodyStyle: 'padding: 5px 10px 5px 10px; background: transparent;',
                                // Specific attributes for the text fields for username / password. 
                                // The "name" attribute defines the name of variables sent to the server.
                                items: [{
                                    fieldLabel: 'Username',
                                    name: 'loginUsername',
                                    id: 'username',
                                    width: 400,
                                    allowBlank: false
                                }, 
                                {
                                    fieldLabel: 'Old/Temporary Password',
                                    name: 'oldPassword',
                                    inputType: 'password',
                                    width: 400,
                                    allowBlank: false
                                },{
                                    fieldLabel: 'New Password',
                                    name: 'loginPassword',
                                    inputType: 'password',
                                    width: 400,
                                    allowBlank: false
                                },
                                {
                                    fieldLabel: 'Confirm New Password',
                                    name: 'confirmLoginPassword',
                                    id: 'confirmLoginPassword',
                                    width: 400,
                                    inputType: 'password'
                                },
                                {
                                    name: 'action',
                                    value: 'reset',
                                    hidden: true
                                }
                                ],
                                buttons: [
                                {
                                        text: 'Login',
                                        id: 'login',
                                        formBind: true,
                                        handler: ResetHandler
                                    }]
                                ,listeners: {
                                    afterRender: function(thisForm, options){
                                            this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {                    
                                                enter: ResetHandler,
                                                scope: this
                                            });
                                        }
                                    }
            }]
        }); //viewport

        // Apply defaults for config.       
        Ext.applyIf(config, {
            autoScroll: true,
        });

        this.callParent([config]);

        this.centerPanel = Ext.getCmp('centerViewport');
        //this.centerPanel.add(login);
    }
});

var app = null;
Ext.define('PublishingWizard.ResetWizard', {
    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.ResetWizard', options);
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
	    me.mainPanel =  Ext.create('PublishingWizard.RecoveryLayout', {
	        renderTo: options.mainDiv
	        //border: false
	        });
        Ext.getCmp('username').focus(false, 100);
    }
});

