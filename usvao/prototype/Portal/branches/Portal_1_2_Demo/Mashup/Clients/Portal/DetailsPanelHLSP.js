Ext.require(Mvp.util.Util);

Ext.define('DemoApp.DetailsPanelHLSP', {
    extend: 'Ext.form.Panel',
    
    statics: {
        create: function (record, searchText, app) {

            var panel = Ext.create('DemoApp.DetailsPanelHLSP', {
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
        
	var leftLabelWidth = 200;
	var leftTitleWidth = 350;
	var rightLabelWidth = 100;
	//var flexLeft = 2;
	var flexRight = 3;
	
	var record = config.record;
        var searchText = config.searchText;
	
	//// HLSP specific ////////////////
	        var dataType = record.get('hp_prodtype');


        var wavebandString = me.formatKeywords(record.get('hp_wavelength'));

        var subjectString = me.formatKeywords(record.get('hp_title'));
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Left panel for summarizing and giving access to DataScope results for this resource.

        var searchSummary = null;
            // There is a hits record, so this must be a DataScope result, so we should display the search summary.
            var searchSummaryItems = [];
    
            // Data type
            searchSummaryItems.push({
                fieldLabel: 'Data Type',
                labelWidth: leftLabelWidth,
                value: dataType
            });
            
            // Load buttons
            var loadButtons = [];
            loadButtons.push({
                xtype: 'button',
                text: 'Load Records into New Table',
                handler: function () {
                    var hp_id = record.get('hp_id');
                    if (hp_id ) {
                        me.app.getHlspProducts(hp_id);
                    } else {
                        alert("Data table unavailable for " + title);
                    }
                }
            });
            
            // Just a separator.  Probably a better way to do this.
            loadButtons.push({
                xtype: 'component',
                width: 10
            });
            
            
            var loadSet = {
                xtype: 'container',
                layout: 'hbox',
                defaults: {
                    hideEmptyLabel: true
                },
                items: loadButtons
            };
            searchSummaryItems.push(loadSet);
            
            searchSummary = {
                xtype: 'fieldset',
                flex: 1,
                title: 'Search Summary',
                defaultType: 'displayfield',
                layout: 'anchor',
                defaults: {
                    anchor: '100%',
                    hideEmptyLabel: false
                },
                items: searchSummaryItems
            };


        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Combine left panels
        
        var leftColumnItems = [{
                xtype: 'displayfield',
                width: leftTitleWidth,
               // html: '<h2>' + record.get('title') + '</h2>'
               //value: '<div id="resourceTitle">' + record.get('title') + '<br>&nbsp;</div>'
               value: '<div id="resourceTitle">' + record.get('hp_title') + '</div>'
            }];
        
        if (searchSummary) {
            leftColumnItems.push(searchSummary);
        }

               
        var leftColumn = {
            xtype: 'container',
            //flex: flexLeft,
            items: leftColumnItems
        };
	
	
	
	///////////////////////////////////////////////
	
	
	
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

	var leftItems = leftColumnItems.concat(displayItems.slice(0, halfway));
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
    },
    
    formatKeywords: function (keyString) {
        var result = "";
        if (keyString) {
            var splits = keyString.split('#');
            var prefix = "";
            for (i in splits) {
                if (splits[i] != '') {
                    result += (prefix + splits[i]);
                    prefix = ", ";
                }
            }
        }
        return result;
    }


    
});