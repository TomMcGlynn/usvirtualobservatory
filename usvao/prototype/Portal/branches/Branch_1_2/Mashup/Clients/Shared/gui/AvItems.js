Ext.define('Mvp.gui.AvItems', {
    extend: 'Ext.util.Observable',
    
    requires: [
        'Mvpc.view.LayerChooserContainer',
        'Mvpc.view.ColorPickerContainer'
    ],

    statics: {
        
    },
    
    /**
     * @cfg {Number} pagesize
     * The number of items to have on one page in the grid display.
     */

    constructor: function(config) {
        this.callParent(arguments);
        this.addEvents('avtoggled');

        
        // Create the components that can be added to a dock.
        this.layerChooser = Ext.create('Mvpc.view.LayerChooserContainer');
        this.layerChooser.addListener('change', this.astroViewToggled, this);
        this.layerChooser.hide();
        this.colorPicker = Ext.create('Mvpc.view.ColorPickerContainer');
        this.colorPicker.addListener('colorChanged', this.colorChanged, this);
        this.colorPicker.hide();

    },
    
    // Public methods.
    
    updateView: function(updateObject) {
        
        var statusText = '';
        if (updateObject.complete) {
            this.layerChooser.show();
            this.colorPicker.show();
        } 
    },
    
    getItems: function() {
        var items = [
            this.layerChooser,
            this.colorPicker
        ];
        return items;
    },
    
    // Private methods.

    astroViewToggled: function (checkbox, newValue, oldValue) {
        Ext.log('AV toggled from ' + oldValue + ' to ' + newValue);
        this.fireEvent('avtoggled', newValue, oldValue);
    },
    
    colorChanged: function(picker, selColor) {
        this.fireEvent('colorChanged', picker, selColor);
    }

    
});
    

    