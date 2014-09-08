Ext.require(['Ext.form.Panel', 'Ext.form.field.File']);

Ext.onReady(function () {
    var fileUploadPanel = Ext.create('Ext.form.Panel', {
        renderTo: 'uploadDiv',
        fileUpload: true,
        width: 500,
        title: 'File Upload',
        height: 120,
        border: 0,
        items: [{
            xtype: 'filefield',
            id: 'form-file',
            size: 50,
            emptyText: 'Select a file',
            fieldLabel: 'File',
            name: 'file-path',
            buttonText: '...'
        }],
        buttons: [{
            text: 'Upload',
            handler: function () {
                var f = fileUploadPanel.getForm();
                f.submit({
                    url: '../../Mashup.asmx/upload',
                    waitMsg: 'Uploading your file...',
                    success: function (f, action) {
                        //alert("OK:" + action.result.message);
                        alert("returned success");
                    },
                    failure: function (f, action) {
                        //alert("Error:" + action.result.message);
                        alert("returned failure");
                    }
                })
            }
        }]
    });
}); 