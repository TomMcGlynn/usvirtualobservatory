
Ext.define('Mvp.gui.SaveAsPanel', {
    extend: 'Ext.window.MessageBox',
    
    constructor: function() {
        this.callParent();  // Though there may be no constructor there...
        
        this.setupComponents();
    },
    
    setupComponents: function () {
        // Create the file name box
        
        // Create the file type radio buttons with callback(s)
        
        // Create the "Show Hidden Columns" checkbox
        
        // Create the containing panel
        
        
        
        var formPanel = Ext.create('Ext.form.Panel', {
            frame: true,
            title: 'Form Fields',
            width: 340,
            bodyPadding: 5,
    
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 90,
                anchor: '100%'
            },
    
            items: [{
                xtype: 'textfield',
                name: 'filename',
                fieldLabel: 'File Name',
                value: 'Text field value'
            }, {
                xtype: 'displayfield',
                name: 'displayfield1',
                fieldLabel: 'Display field',
                value: 'Display field <span style="color:green;">value</span>'
            }, {
                xtype: 'checkboxfield',
                name: 'includeHidden',
                fieldLabel: 'Include Hidden Columns',
                boxLabel: ''
            }, {
                xtype: 'radiofield',
                name: 'radio1',
                value: 'csv',
                fieldLabel: 'Comma-Separated Values',
                boxLabel: 'radio 1'
            }, {
                xtype: 'radiofield',
                name: 'radio1',
                value: 'votable',
                fieldLabel: '',
                labelSeparator: '',
                hideEmptyLabel: false,
                boxLabel: 'VO Table'

            }]
        });

        this.add(formPanel);
        
        
    },
    
    // The callback supplied here needs to say:
    //      - Whether Save or Cancel (or window close) was pressed
    //      - The file name
    //      - The file type
    //      - Whether to include hidden columns
    showPanel: function(filename, filetype, showHidden) {
        this.show('Save XXX As');
        
        //  Set the OK button text to "Save"
    }
    
}, function() {
    /**
     * @class Ext.MessageBox
     * @alternateClassName Ext.Msg
     * Singleton instance of {@link Ext.window.MessageBox}.
     */
    Mvp.SaveAsPanel = new this();
});