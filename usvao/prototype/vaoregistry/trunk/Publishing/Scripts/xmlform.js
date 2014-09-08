Ext.require([
    'Ext.form.*',
    'Ext.data.*'
]);


Ext.define('PublishingWizard.Resource', {
    extend: 'Ext.data.Model',
    fields: [
            { name: 'title' },
            { name: 'shortName' },
            { name: 'content/description' },
            { name: 'publisher/name' }
        ]
});


Ext.define('PublishingWizard.XmlForm', {
    statics: {
        /**
        *
        * The options argument should be a JavaScript object with the following properties:
        *    title(string): The title to give the resulting grid panel
        *    
        *    app(object): The calling application object.  That application is expected to have the following:
        *        onError(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has an error
        *        onFailure(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has a failure
        *        tabContainer(tabpanel): A tab panel to which the new grid will be added
        *
        */
        activate: function (options) {
            var form = Ext.create('PublishingWizard.XmlForm', {
                title: options.title,
                app: options.app
            });
            form.load(options);
        }
    },

    constructor: function (config) {
        Ext.apply(this, config);
    },

    load: function (options) {
        this.file = options.file;
        var formPanel = Ext.create('Ext.form.Panel', {
            id: options.title,
            //layout: 'fit',
            bodyPadding: 5,
            frame: true,
            waitMsgTarget: true,
            monitorValid: true,
            method: 'GET',
            //plugins: [new Ext.ux.OOSubmit()];

            fieldDefaults: {
                labelAlign: 'right',
                labelWidth: 85,
                msgTarget: 'side'
            },

            // configure how to read the XML data
            reader: Ext.create('Ext.data.reader.Xml', {
                model: 'PublishingWizard.Resource',
                record: 'ri:Resource',
                successProperty: '@success'
            }),

            items: [{
                xtype: 'fieldset',
                title: 'General Information',
                defaultType: 'textfield',
                autoheight: true,
                defaults: {
                    width: 400
                },
                items: [{
                    id: 'title',
                    fieldLabel: '* Title',
                    emptyText: 'Resource Title for Searching',
                    name: 'title',
                    allowBlank: false
                },
                {
                    id: 'shortName',
                    fieldLabel: '* shortName',
                    emptyText: 'Shortened Resource Title for Searching',
                    name: 'shortName',
                    allowBlank: false
                },
                {
                    id: 'content/description',
                    fieldLabel: '* Description',
                    emptyText: '',
                    name: 'content/description',
                    xtype: 'textareafield',
                    width: 500,
                    height: 200,
                    allowBlank: false
                }
            ]
            }],

            buttons: [{
                text: 'Submit',
                disabled: true,
                formBind: true,
                id: 'submitResource',
                handler: function () {
                    this.up('form').getForm().submit({
                        url: 'xml-form-errors.xml',
                        submitEmptyText: false,
                        waitMsg: 'Saving Data...'
                    });
                }
            }]
        });

        formPanel.on({
            actioncomplete: function (form, action) {
                if (action.type == 'load') {
                    formPanel.enable();
                    Ext.getCmp('submitResource').enable();
                    form.method = 'POST';
                }
            }
        });

        this.app.genPanel.add(formPanel);
        formPanel.getForm().load({
            url: this.file,
            waitMsg: 'Loading...'
        });

    }

});