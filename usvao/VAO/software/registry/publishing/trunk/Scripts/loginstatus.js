
var baseDirectoryURL = "http://vaotest.stsci.edu/directory/";
var loginURL = "VAOlogin.aspx";

isLoginPage = function() {
    if( document.title.toUpperCase().indexOf("LOGIN") > -1 )
        return true;
    return false;
}

isNeedLoggedInPage = function () {
    if( document.title.toUpperCase().indexOf("RESOURCE") > -1 )
        return true;
    return false;
}

forceLogout = function() {
    Ext.getCmp('buttonLogout').hide();
    if( isNeedLoggedInPage ) {
        Ext.Msg.alert('Error', 'Not logged in', function (btn, text) {
            if (btn == 'ok') {
                window.location = loginURL;
            }
        });
    }
}

checkLoginInfo = function () {
    Ext.Ajax.request({
        url: 'login.aspx?action=isloggedin',
        method: 'GET',
        success: function (result, request) {
            var json = Ext.decode(result.responseText);
            if (json && json.success == true) {
                Ext.getCmp('labelUserName').setValue('logged in as ' + json.details);
                if( isLoginPage() ) {
                    Ext.getCmp('buttonContinue').show();
                }
            }
            else  if( isNeedLoggedInPage() ) {
                forceLogout();
            }
            else {
                Ext.getCmp('buttonLogout').hide();
            }
        },
        failure: function (result, request) {
            if( isNeedLoggedInPage() ) 
                forceLogout();
        }
    })
};

logout = function () {
    Ext.Ajax.request({
       url: 'login.aspx?action=logout',
       method: 'GET',
       success: function (result, request) {
           var test = result.responseText;           
           Ext.getCmp('labelUserName').setValue('not logged in to registry publishing');
           Ext.MessageBox.show({
               title: 'Logout Successful',
               msg: 'You have been logged out of the registry publishing system. Your broader VAO login remains active.',
               buttons: Ext.MessageBox.OK,
               closable: false,
               fn: function (btn, text) { window.location = loginURL; }
           });
       },
       failure: function (result, request) {
           Ext.Msg.alert('Failed', result.responseText);
       }
   })
};


Ext.define('PublishingWizard.LoginStatusLayout', {
    extend: 'Ext.panel.Panel',

    statics: {},

    constructor: function (config) {
        var me = this;

        // Apply mandatory config items.       
        Ext.apply(config, {
            autoScroll: false,
            border: 0,
            layout: 'hbox',
            bodyStyle: 'background:transparent;',
            autoScroll: 'false',
            height: 50,
            style: {  marginLeft: 'auto', marginRight: 'auto', marginTop: 'auto', marginBottom: 'auto' },
            bodyStyle: 'padding: 5px 10px 5px 10px; background: transparent;',
            defaults: { border: 0, margin: 0, bodyStyle: 'background:transparent;'},
            items: [                      
                {
                     xtype: 'displayfield',
                     fieldLabel: '',
                     id: 'labelUserName',
                     value: 'not logged in to registry publishing',
                     bodyStyle: 'padding: 5px; background: transparent;',
                     width: 200
                  },
                  { 
                      xtype: 'button',
                      text: 'Continue to Resource Management',
                      hidden: true,
                      id: 'buttonContinue',
                      handler: function () {
                          window.location = resourceManagementURL;
                      }
                  }
                 ,{
                    xtype: 'button',
                    text: 'logout',
                    id: 'buttonLogout',
                    handler: function () {
                              window.location = 'login.aspx?action=logout';
//                            Ext.Ajax.request({
//                                url: 'login.aspx?action=logout',
//                                //url: 'https://sso.usvao.org/openid/logout?returnURL=' + window.location.href.substring(0, window.location.href.lastIndexOf('/')) + '/VAOLogin.aspx',
//                                method: 'GET',
//                                success: function (result, request) {
//                                    Ext.getCmp('labelUserName').setValue('not logged in to registry publishing');
//                                    Ext.MessageBox.show({
//                                        title: 'Logout Successful',
//                                        msg: 'You have been logged out of the registry publishing system. Your broader VAO login remains active.',
//                                        buttons: Ext.MessageBox.OK,
//                                        closable: false,
//                                        fn: function (btn, text) { window.location = loginURL; }
//                                    });
//                                },
//                                failure: function (result, request) {
//                                    //Ext.Msg.alert('Logout Failed', result.responseText);
//                                }
//                            })
                        }
                    }
                    , {width: 20, autoEl: { tag: 'div' } }
                    ,{ width: 75, height: 30, autoEl: { tag: 'a', html: 'Help', href: 'help.html', target: "_blank"} }
            ] //hbox items
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
Ext.define('PublishingWizard.LoginStatusWizard', {
    statics: {
        createAndRun: function (options) {
            var wizard = Ext.create('PublishingWizard.LoginStatusWizard', options);
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
	    me.mainPanel =  Ext.create('PublishingWizard.LoginStatusLayout', {
	        renderTo: options.mainDiv
	        //border: false
	        });

        checkLoginInfo();
    }
});
