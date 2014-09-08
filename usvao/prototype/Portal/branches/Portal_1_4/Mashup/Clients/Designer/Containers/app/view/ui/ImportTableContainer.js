/*
 * File: app/view/ui/ImportTableContainer.js
 * Date: Fri May 25 2012 15:45:03 GMT-0400 (Eastern Daylight Time)
 *
 * This file was generated by Ext Designer version 1.2.2.
 * http://www.sencha.com/products/designer/
 *
 * This file will be auto-generated each and everytime you export.
 *
 * Do NOT hand edit this file.
 */

Ext.define('Mvpc.view.ui.ImportTableContainer', {
    extend: 'Ext.window.Window',

    height: 130,
    id: '',
    itemId: 'importForm',
    width: 450,
    layout: {
        type: 'fit'
    },
    closeAction: 'hide',
    title: 'Import Table',

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'panel',
                    itemId: 'importPanel',
                    bodyPadding: 10,
                    flex: 1,
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'formatPulldown',
                            fieldLabel: 'Format',
                            labelWidth: 50,
                            allowBlank: false,
                            editable: false,
                            displayField: 'displayField',
                            forceSelection: true,
                            queryMode: 'local',
                            store: 'FormatStore',
                            valueField: 'valueField'
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    }
});