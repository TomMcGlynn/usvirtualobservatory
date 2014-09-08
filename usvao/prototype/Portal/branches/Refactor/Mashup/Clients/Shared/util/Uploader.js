Ext.require('Mvpd.view.UploadOptionsForm');

Ext.define('Mvp.util.Uploader', {
    statics: {
        showDialog: function(portalScope) {
             var uploadOptionsForm = Ext.create('Mvpd.view.UploadOptionsForm', {
                portalScope: portalScope});
             uploadOptionsForm.show();
        },
        
        uploadFile: function (fileForm, successCallback, cbScope) {
                fileForm.submit({
                    url: '../../Mashup.asmx/upload',
                    waitMsg: 'Uploading your file...',
                    scope: cbScope,
                    success: successCallback,
                    failure: function (f, action) {
                        alert("returned failure");
                    }
            });
        }

    },
    
    constructor: function(config) {
        Ext.apply(this, config);

    }
    
    
 

});
    
