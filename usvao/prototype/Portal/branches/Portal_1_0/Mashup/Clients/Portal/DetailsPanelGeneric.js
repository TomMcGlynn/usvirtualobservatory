Ext.require(Mvp.util.Util);

Ext.define('DemoApp.DetailsPanelGeneric', {
    extend: 'Ext.form.Panel',
    
    statics: {
        create: function (record, searchText, app) {

            var panel = Ext.create('DemoApp.DetailsPanelGeneric', {
                record: record,
                searchText: searchText,
                app: app
            });
    
            return panel;
        }
    },
    
    constructor: function(config) {
        
        var me = this;
        me.record = config.record;
        me.searchText = config.searchText;
        me.app = config.app;
        
        var record = config.record;
        var searchText = config.searchText;
	
	var fields = record.fields;
	var keys = (fields) ? fields.keys : [];
	var length = keys.length;
	
	var textVals = [];
	var hyperVals = [];
	
	var displayItems = [];
	for(var k in keys) {
	    var fieldName = keys[k];
	    var value = record.get(fieldName);
	    
	    if (Mvp.util.Util.isUrl(value)) {
		me.addItem(hyperVals, fieldName, value);
	    } else {
		me.addItem(textVals, fieldName, value);
	    }
	}
	
	var hLength = hyperVals.length;
	var tLength = textVals.length;
	var halfway = (length - 1) / 2;
	if (hLength > (length / 2)) {
	    // More than half the items are hypertext.  We'll start them in the first column.
	    displayItems = hyperVals.concat(textVals);
	} else {
	    // All the hypertext will fit in the second column, so put it there.
	    displayItems = Ext.Array.insert(textVals, halfway + 1, hyperVals);
	}

	var leftItems = displayItems.slice(0, halfway);
	var rightItems = displayItems.slice(halfway+1);
	
	leftItems.push({value: ' '});	
	leftItems.push({value: ' '});	
	rightItems.push({value: ' '});	
	rightItems.push({value: ' '});
	
	var leftColumn = {
	    xtype: 'fieldset',
	    flex: 1,
	    title: 'Record Details',
	    defaultType: 'displayfield',
	    layout: 'anchor',
	    defaults: {
		anchor: '100%',
		hideEmptyLabel: false
	    },
	    items: leftItems
	};
        
	var rightColumn = {
	    xtype: 'fieldset',
	    flex: 1,
	    title: "Record Details Cont'd",
	    defaultType: 'displayfield',
	    layout: 'anchor',
	    defaults: {
		anchor: '100%',
		hideEmptyLabel: false
	    },
	    items: rightItems
	};
        
       /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Finish building this (the field panel containing all those displays).
        
        var parentConfig = {
            border: false,
            layout: {
                type: 'hbox',
                align: 'stretchmax',
                padding: 5
            },
            fieldDefaults: {
                labelWidth: 100
            },
            items: [
            leftColumn
            ,{
                xtype: 'component',
                width: 10
            },
            rightColumn
	    ]
        };  
        this.callParent([parentConfig]);
	
    },
    
    addItem: function(items, fieldName, value) {
	var label = fieldName;
	var displayVal = Mvp.util.Util.createLinkIf(value);
	var newItem = {
	    fieldLabel: label,
	    value: displayVal
	};
	items.push(newItem);
    }
    
});