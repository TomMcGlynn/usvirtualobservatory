Ext.define('Mvp.gui.RefreshToolbar', {
    extend: 'Ext.toolbar.Toolbar',
    //mixins: 'Ext.util.Observable',

    requires: [
        'Ext.toolbar.TextItem',
        'Ext.button.Button'
    ],

    statics: {
        
    },
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    initComponent: function() {

        this.refreshLabel = Ext.create('Ext.toolbar.TextItem', { text: this.refreshText });

        this.refreshButton = Ext.create('Ext.button.Button', {
            text: 'Refresh Table',
            tooltip: 'Refresh Table to Include All New Data.',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            handler: this.reloadPressed
        });
        
        Ext.applyIf(this, {
            hidden: true,
            items: [this.refreshLabel, this.refreshButton]
        });

        this.callParent(arguments);
    },
    
    constructor: function(config) {
        Ext.apply(this, config);
        this.callParent(config);
        
        this.addEvents('refresh');
    },
    
    // Public methods.
    setRefreshText: function(text) {
        this.refreshLabel.setText(text);
    },
    
    // Private methods.
    reloadPressed: function() {
        Ext.callback(this.controller.reload, this.controller);
    }
    
});
    

    