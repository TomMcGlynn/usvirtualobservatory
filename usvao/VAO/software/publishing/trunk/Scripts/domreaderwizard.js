Ext.require('PublishingWizard.Layout');

var getResourceURL = 'GetResourceInfo.aspx?action=getResource&identifier=';
var resourceManagementURL = "resourcemanagement.html?debug";
var newResource = true;
var resourceDOM = null;
var emptyResourceDOM = null;
var app = null;
var appOptions = null;

var emptyResourceTries = 0;
var myResourceTries = 0;
var retriesMAX = 10;
var retryInterval = 500;

Ext.define('PublishingWizard.DomReaderWizard', {
    statics: {
        createAndRun: function (options) {
            var id = getArg("identifier");
            if( id != "")
                options["identifier"] = id;
            var copy = getArg("copy");
            if( copy == "")
                options["copy"] = false;
            else
                options["copy"] = true;
            var pending = getArg("pending");
            if( pending != "" )
                options["pending"] = pending;

            var wizard = Ext.create('PublishingWizard.DomReaderWizard', options);
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
        appOptions = options;
        Ext.QuickTips.init();
 
        showWaitMsg();
        getAuthInfo();

        app.getEmptyResourceXML(); //we'll need this to fill in anything missing from the resource we're cloning/editing.
        //app.getResourceXML(); //fills in resourceDOM object on success, clears on failure

        // Create the main panel with a border layout.
	    me.mainPanel =  Ext.create('PublishingWizard.Layout', {
	        renderTo: options.mainDiv
	    });
   },

   getEmptyResourceXML: function() {
        if( emptyResourceDOM == null ) {
            Ext.Ajax.request({
                url : './emptyVOResource.xml' , 
                method: 'GET',
                success: function ( result, request )
                { 
                    if( result.responseXML != null )
                        emptyResourceDOM = result.responseXML;
                    else if ( emptyResourceTries++ < retriesMAX ) {
                        setTimeout("throw new Error('getEmptyResourceRetry empty xml')",0);
                        setTimeout('app.getEmptyResourceXML();', retryInterval );
                    }
                },
                failure: function (result, request )
                {
                    if ( emptyResourceTries++ < retriesMAX ) {
                            setTimeout("throw new Error('getEmptyResourceRetry in a failure')",0);
                            setTimeout('app.getEmptyResourceXML();', retryInterval );
                    }
                }
            });
        }
        if( emptyResourceDOM == null && emptyResourceTries >= retriesMAX )
        {
            Ext.Msg.alert('Error', 'Timeout loading empty sample resource', function (btn, text) {
                   if (btn == 'ok') {
                       window.location = resourceManagementURL;
                   }
            })
        }
    },

    getResourceXML: function() {
         var identifier = appOptions.identifier;
         var pending = appOptions.pending; 

         var url = './emptyVOResource.xml';
         if( identifier != 'undefined' && identifier != '' && identifier != undefined ) {
              url = getResourceURL + identifier;
              if( pending!= null && pending != '' && pending != undefined )
                url = url + "&pending=true";
              newResource = false;
         }

          Ext.Ajax.request({
          url : url , 
          method: 'GET',
          success: function ( result, request )
          { 
              
              if( resourceDOM == null && result.responseXML != null )
                  resourceDOM = result.responseXML;
              else if ( myResourceTries++ < retriesMAX ) {
                  setTimeout("throw new Error('getResourceRetry empty xml')",0);
                  setTimeout('app.getResourceXML();', retryInterval );
              }
              if( resourceDOM == null )
              {
                   Ext.Msg.alert('Failed', 'Failed to load resource ' + identifier + ' belonging to current user. Login may have timed out.', function (btn, text) {
                         if (btn == 'ok') {
                             window.location = resourceManagementURL;
                         }
                   })
              }
              else {
                  if( resourceDOM != null && emptyResourceDOM != null ) {
                      if (!appOptions.copy) {
                        setTimeout('setupForms(app, resourceDOM, newResource, appOptions.copy, appOptions.pending);', 200);
                      }
                      if( newResource || appOptions.copy ) {
                          if( firstOrgRecord) 
                                storeResourceTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: orgResourceTypes });
                          showResourceTypeWindow(resourceDOM);
                      }
                  }
              }
          },
          failure: function (result, request )
          {
              if( resourceDOM == null ) {
                  if ( myResourceTries++ < retriesMAX ) {
                      setTimeout("throw new Error('getResourceRetry in a failure')",0);
                      setTimeout('app.getResourceXML();', retryInterval );
                  }
                  else {
                        Ext.Msg.alert('Failed', 'Timeout loading resource ' + identifier + ' belonging to current user. Login may have timed out.', function (btn, text) {
                             if (btn == 'ok') {
                                 window.location = resourceManagementURL;
                             }
                       })
                  }
              }
              else {
                   Ext.Msg.alert('Failed', 'Failed to load resource ' + identifier + ' belonging to current user. Login may have timed out.', function (btn, text) {
                         if (btn == 'ok') {
                             window.location = resourceManagementURL;
                         }
                   })
               }
          }
      });
    }
})

function getArg( name )
{
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}