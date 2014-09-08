var resourceManagementURL = "resourcemanagement.html?debug";


//authority records for associating new users with existing publishing structure.
var storeAuthorityInfo = Ext.create('Ext.data.Store', {
    autoLoad: false,
    fields: ['title', 'identifier'],
    proxy: {
        type: 'ajax',
        url: 'GetResourceInfo.aspx?action=authoritylist',
        reader: {
            type: 'json',
            root: 'AuthorityInfo'
        },
        listeners: {
            exception: function (proxy, response, operation) {
                errorsLoading.push('Error loading authority information from registry: ' + response.statusText);
            }
        }

    },
    storeId: 'AuthorityInfo',
    root: 'AuthorityInfo'
});

var isExistingUser = true;
toggleRegistration = function () {
    var confirm = Ext.getCmp('confirmLoginPassword');
    var auth = Ext.getCmp('authorityInfo');
    var email = Ext.getCmp('email');
    var name = Ext.getCmp('name');
    if (isExistingUser) {
        confirm.hide();
        confirm.allowBlank = true;
        auth.hide();
        auth.allowBlank = true;
        email.hide();
        email.allowBlank = true;
        name.hide();
        name.allowBlank = true;
        Ext.getCmp('newUser').setText("Register New User");
        Ext.getCmp('login').setText("Login");
    }
    else {
        confirm.show();
        confirm.allowBlank = false;
        auth.show();
        auth.allowBlank = false;
        email.show();
        email.allowBlank = false;
        name.show();
        name.allowBlank = false;
        Ext.getCmp('newUser').setText("Login as Existing User");
        Ext.getCmp('login').setText("Register New User and Login");
    }
    isExistingUser = !isExistingUser;
}


loginHandler = function () {
    Ext.getCmp('loginForm').getForm().submit({
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

Ext.define('PublishingWizard.LoginLayout', {
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
                layout: 'fit'
            },
            items: [{
                                xtype: 'form',
                                labelWidth: 80,
                                margin: 50,
                                width: 525,
                                url: 'login.aspx',
                                frame: true,
                                title: 'Please Login to the US VAO Registry Publishing System',
                                defaultType: 'textfield',
                                id: 'loginForm',
                                monitorValid: true,
                                fieldDefaults: { width: 495 },
                                bodyStyle: 'padding: 5px 10px 5px 10px; background: transparent;',
                                // Specific attributes for the text fields for username / password. 
                                // The "name" attribute defines the name of variables sent to the server.
                                items: [{
                                    fieldLabel: 'Username',
                                    name: 'loginUsername',
                                    id: 'username',
                                    allowBlank: false
                                }, {
                                    fieldLabel: 'Password',
                                    name: 'loginPassword',
                                    inputType: 'password',
                                    allowBlank: false
                                },
                                {
                                    fieldLabel: 'Confirm Password',
                                    name: 'confirmLoginPassword',
                                    id: 'confirmLoginPassword',
                                    inputType: 'password'
                                },
                                {
                                    fieldLabel: 'Name',
                                    name: 'name',
                                    id: 'name'
                                },
                                {
                                    fieldLabel: 'Email Address',
                                    name: 'email',
                                    vtype: 'email',
                                    id: 'email'
                                },
                                {
                                    xtype: 'combo',
                                    id: 'authorityInfo',
                                    store: storeAuthorityInfo,
                                    displayField: 'title',
                                    valueField: 'identifier',
                                    hiddenName: 'identifier',
                                    queryMode: 'remote', //change this to 'local' to preload / 'remote' to not?
                                    fieldLabel: 'Associated Institution',
                                    name: 'authorityInfo',
                                    autoselect: true,
                                    data: 'all',
                                    typeAhead: true,
                                    listeners: { 'change': function (field, newVal, oldVal) { } }
                                }],
                                buttons: [
                                    {
                                        text: 'Register New User',
                                        id: 'newUser',
                                        handler: function () {
                                            toggleRegistration();
                                        }
                                    },
                                    {
                                        text: 'Login',
                                        id: 'login',
                                        formBind: true,
                                        handler: loginHandler
                                    }]
                                ,listeners: {
                                    afterRender: function(thisForm, options){
                                            this.keyNav = Ext.create('Ext.util.KeyNav', this.el, {                    
                                                enter: loginHandler,
                                                scope: this
                                            });
                                        }
                                    }
            }]
        }); //viewport

        // Apply defaults for config.       
        Ext.applyIf(config, {
            width: 1100,
            autoScroll: true
        });

        this.callParent([config]);

        this.centerPanel = Ext.getCmp('centerViewport');
        //this.centerPanel.add(login);
    }
});

var app = null;
Ext.define('PublishingWizard.LoginWizard', {
    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.LoginWizard', options);
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
	    me.mainPanel =  Ext.create('PublishingWizard.LoginLayout', {
	        renderTo: options.mainDiv
	        //border: false
	        });

        toggleRegistration();
        Ext.getCmp('username').focus(false, 100);
    }
});

