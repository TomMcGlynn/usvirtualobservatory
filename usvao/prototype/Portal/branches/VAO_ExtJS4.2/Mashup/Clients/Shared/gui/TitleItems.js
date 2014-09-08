Ext.define('Mvp.gui.TitleItems', {
    extend: 'Ext.util.Observable',
    
    requires: [
        'Ext.toolbar.TextItem'
    ],

    statics: {
        
    },
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.callParent(arguments);
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        
        // Create the components that can be added to a dock.
        this.label = Ext.create('Ext.toolbar.TextItem', { text: this.tooltip });
        this.separator = Ext.create('Ext.toolbar.Separator', { hidden: true });
    },
    
    // Public methods.
    
    updateView: function(updateObject) {

    },
    
    getItems: function () {
        var items = [
            this.label,
            this.separator
        ];
        return items;
    },
    
    setText: function(text) {
        this.label.setText(text);
        if (text) this.separator.show();
    }
    
    // Private methods.

    
});
    

    