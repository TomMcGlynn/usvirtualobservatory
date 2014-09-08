
/*
 * Do NOT hand edit this file.
 */

MainPanelUi = Ext.extend(Ext.Container, {
    width: 1000,
    height: 500,
    //layout: 'fit',
    //width: 400,
    //height: 250,
    initComponent: function() {
        this.items = [
            {
                xtype: 'searchbox',
                width: 800
            }
        ];
        MainPanelUi.superclass.initComponent.call(this);
    }
});

