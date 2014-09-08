
Ext.define('DemoApp.DetailsPanelInv', {
    extend: 'Ext.form.Panel',
    
    statics: {
        create: function (record, searchText, coneSearchParams, app) {

            var panel = Ext.create('DemoApp.DetailsPanelInv', {
                record: record,
                searchText: searchText,
		coneSearchParams: coneSearchParams,
                app: app
            });
    
            return panel;
        }
    },
    
    constructor: function(config) {
        
        var me = this;
        me.record = config.record;
        me.searchText = config.searchText;
	me.coneSearchParams = config.coneSearchParams;
        me.app = config.app;
        
        var record = config.record;
        var searchText = config.searchText;
        
        var dataType = 'Unknown';
        var searchLabel = null;
        me.searchAction = null;
        var serviceURL = me.getServiceURL();
	
        var typeString = record.get('datatype');
        if (typeString.match('images')) {
            dataType = 'Images';
            me.searchAction = 'searchGenericSiap';
            searchLabel = 'Image Search';
        } else if (typeString.match('Catalog')) {
            dataType = 'Catalog';
            me.searchAction = 'searchGenericCone';
            searchCallback = me.doConeSearch;
            searchLabel = 'Catalog Search';
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Left panel for summarizing and giving access to DataScope results for this resource.

        var searchSummary = null;
        var hits = record.get('count');
        if (hits) {
            // There is a hits record, so we can have a useful search summary.
            var searchSummaryItems = [];
    
            // Description
            searchSummaryItems.push({
                fieldLabel: 'Records Found When Searching ' + searchText,
                labelWidth: 200,
                value: hits
            });
            // Data type
            searchSummaryItems.push({
                fieldLabel: 'Data Type',
                labelWidth: 200,
                value: dataType
            });
	    
            // Load buttons to access the service URL for the same position as the original search.
	    if (serviceURL && coneSearchParams) {
		// Load buttons
		var loadButtons = [];
		
		loadButtons.push({
		    xtype: 'button',
		    text: 'Load Records into New Table',
		    handler: function () {
			var title = record.get('shortName') + ": " + searchText;
			me.app.searchGenericCone(me.coneSearchParams, serviceURL, title);
		    }
		});
		
		// Just a separator.  Probably a better way to do this.
		loadButtons.push({
		    xtype: 'component',
		    width: 10
		});
		
		var ra = 'ra=' + me.coneSearchParams.ra;
		var dec = 'dec=' + me.coneSearchParams.dec;
		var radius = 'sr=' + me.coneSearchParams.radius;
		var verb = 'verb=' + me.coneSearchParams.verb;
		var accessURL = serviceURL + '&' + ra + '&' + dec + '&' + radius + '&' + verb;
		
		var hyperLink = Mvp.util.Util.createLink(accessURL, 'Download Data');
		loadButtons.push({
		    xtype: 'displayfield',
		    fieldLabel: 'or',
		    labelWidth: 20,
		    value: hyperLink
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
            
	    }
            
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
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Lower left panel for performing a new search on this resource
        
        var newSearch = null;
        if (me.searchAction && serviceURL) {
            var newSearchItems = [];
            
            // Create the search box.
            me.searchBox = Ext.create('Mvp.util.SearchBox', {
                fieldLabel: searchLabel,
                emptyText: 'Enter object name or RA and Dec'
            });
            me.searchBox.on('searchInitiated', me.doSearch, me);
            newSearchItems.push(me.searchBox);
            
            // Create the placeholder for the resolver summary.
            me.resolverSummary = Ext.create('Ext.form.field.Display', {
            fieldLabel: ' ',
	    labelSeparator: '',
	    labelWidth: 100,
	    value: ' '
            });
            newSearchItems.push(me.resolverSummary);
            
            var newSearch = {
                xtype: 'fieldset',
                flex: 1,
                title: 'Perform Search on New Position',
                defaultType: 'displayfield',
                layout: 'anchor',
                defaults: {
                    anchor: '100%',
                    hideEmptyLabel: false
                },
                items: newSearchItems
            };
            
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Combine left panels
        
        var leftColumnItems = [{
                xtype: 'displayfield',
                width: 250,
               // html: '<h2>' + record.get('title') + '</h2>'
               //value: '<div id="resourceTitle">' + record.get('title') + '<br>&nbsp;</div>'
               value: '<div id="resourceTitle">' + record.get('description') + '</div>'
            }];
        
        if (searchSummary) {
            leftColumnItems.push(searchSummary);
        }
        
        if (newSearch) {
            leftColumnItems.push(newSearch);
        }
        
        var leftColumn = {
            xtype: 'container',
            flex: 1,
            items: leftColumnItems
        };

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Right panel for general resource info

        var resourceInfo = {
            xtype: 'fieldset',
            flex: 2,
            title: 'Resource Information',
            defaultType: 'displayfield',
            // each item will be a radio button
            layout: 'anchor',
            //columnWidth: .4,
            //margin: '20 20 20 20',
            defaults: {
                anchor: '100%',
                hideEmptyLabel: true
            },
            items: [ // Description
            {
                fieldLabel: 'Archive',
                value: record.get('archive') 
            },{
                fieldLabel: 'Set',
                value: record.get('set')
            }, {
                fieldLabel: 'Notes',
                value: record.get('notes')
            }, {
                fieldLabel: 'Total Records',
                value: record.get('nrec')
            }, {
                fieldLabel: 'IVOA Identifier',
                value: record.get('identifier')
            } ]
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
            leftColumn,
            {
                xtype: 'component',
                width: 10
            },
            resourceInfo]
        };  
        this.callParent([parentConfig]);
    },
    
    doSearch: function(text) {
        var me = this;
        var url = me.record.get('accessURL');
        // Some of these urls have their ampersands encoded (at least once).  Try to remove that.
        // This won't get rid of any other html encoding.
        if (url) {
            url = url.replace(/amp;/gi, '');
        }
        var title = me.record.get('shortName') + ': ' + text;
        
        // searchGenericCone expect the base url and the title of the resulting grid.
        var extraArgs = [url, title];
        extraArgs.summaryDisplay = me.resolverSummary;
        var args = [text, me.searchAction, extraArgs];
        
        // Call the main app's search routine.
        Ext.callback(me.app.doSearch, me.app, args);
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
    },
    
    getServiceURL: function () {
	var serviceURL = this.record.get('serviceURL');
        if (serviceURL) {
	    // TabularSkyService records don't seem to have a needed '&' at the end of their url.
	    if (!serviceURL.match('.*&$')) {
		serviceURL += '&';
	    }
	}
	return serviceURL;
    }

});