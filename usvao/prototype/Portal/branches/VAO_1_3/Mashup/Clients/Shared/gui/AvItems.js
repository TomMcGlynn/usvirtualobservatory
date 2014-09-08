Ext.define('Mvp.gui.AvItems', {
    extend: 'Ext.util.Observable',

    requires: [
        'Ext.form.Label',
        'Mvpc.view.ColorPickerContainer'
    ],

    statics: {

    },

    /**
    * @cfg {Number} pagesize
    * The number of items to have on one page in the grid display.
    */

    constructor: function (config) {
        this.callParent(arguments);
        this.addEvents('avtoggled', 'colorChanged', 'avmodechanged');
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);


        // Create the components that can be added to a dock.

        this.selectionLabel = Ext.create('Ext.form.Label', {
            text: 'Display',
            margins: '4 0 0 5'
        });

        var selectionModes = Ext.create('Ext.data.Store', {
            fields: ['abbr', 'name'],
            data: [
                { "abbr": "NONE", "name": "None" },
                { "abbr": "ALL", "name": "All" },
                { "abbr": "SELECTED", "name": "Selected" }
            ]
        });

        this.selectionPicker = Ext.create('Ext.form.ComboBox', {
            //fieldLabel: 'AstroView Display',
            store: selectionModes,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'abbr',
            width: 75,
            margins: '0 3 0 3',
            autoSelect: true,
            allowBlank: false,
            editable: false
        });

        this.selectionPicker.setValue('NONE');
        this.selectionPicker.on('change', this.selectionModeChanged, this);

        this.colorLabel = Ext.create('Ext.form.Label', {
            text: 'Color',
            margins: '4 1 0 3'
        });
        this.colorPicker = Ext.create('Mvpc.view.ColorPickerContainer');
        this.colorPicker.addListener('colorChanged', this.colorChanged, this);

        var items = [this.selectionLabel, this.selectionPicker, this.colorLabel, this.colorPicker];

        this.label = Ext.create('Ext.form.Label', {
            text: 'AstroView Controls',
            margins: '2 3 0 3',
            hidden: true
        });
        this.fieldSet = Ext.create('Ext.form.FieldSet', {
            items: items,
            height: 25,
            hidden: true,
            layout: 'hbox',
            width: 200,
            padding: 0,
            shadow: 'frame'
        });
    },

    // Public methods.

    updateView: function (updateObject) {
        var store = updateObject.store;
        if (store) {
            if (store.hasPositions() || store.hasFootprints()) {
                this.show();
            }
        }

    },

    getItems: function () {
        return [this.label, this.fieldSet];
    },

    show: function () {
        var items = this.getItems();
        for (var i in items) if (items[i].show) items[i].show();
    },

    hide: function () {
        var items = this.getItems();
        for (var i in items) if (items[i].hide) items[i].hide();
    },

    getColor: function () {
        var color = this.colorPicker.getColor();
        return color;
    },

    // Private methods.

    astroViewToggled: function (checkbox, newValue, oldValue) {
        Ext.log('AV toggled from ' + oldValue + ' to ' + newValue);
        this.fireEvent('avtoggled', newValue, oldValue);
    },

    colorChanged: function (picker, selColor) {
        this.fireEvent('colorChanged', selColor);
    },

    getSelectionMode: function () {
        return this.selectionPicker.getValue();
    },

    setSelectionMode: function (value) {
        this.selectionPicker.setValue(value);
    },
    
    selectionModeChanged: function(el, to, from) {
        this.fireEvent('avmodechanged', to, from);
    }

});
    

    