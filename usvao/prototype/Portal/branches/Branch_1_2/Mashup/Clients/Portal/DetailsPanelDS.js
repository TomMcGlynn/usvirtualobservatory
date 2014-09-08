
Ext.define('DemoApp.DetailsPanelDS', {
    extend: 'Ext.form.Panel',
    
    statics: {
        create: function (record, searchText, app) {

            var panel = Ext.create('DemoApp.DetailsPanelDS', {
                record: record,
                searchText: searchText,
                app: app
            });
    
            return panel;
        }
    },
    
    constructor: function(config) {
        
	var leftLabelWidth = 200;
	var leftTitleWidth = 350;
	var rightLabelWidth = 100;
	//var flexLeft = 2;
	var flexRight = 3;
        
        var me = this;
        me.record = config.record;
        me.searchText = config.searchText;
        me.app = config.app;
        
        var record = config.record;
        var searchText = config.searchText;
        
        var dataType = 'Unknown';
	var downloadLinkText = 'Download Data';
        var searchLabel = null;
        me.searchAction = null;
        var categoryString = record.get('categories');
        if (categoryString.match('Images')) {
            dataType = 'Images';
            me.searchAction = 'searchGenericSiap';
            searchLabel = 'Image Search';
	    downloadLinkText = 'Download Image Metadata';
        } else if (categoryString.match('Catalog')) {
            dataType = 'Catalog';
            me.searchAction = 'searchGenericCone';
            searchCallback = me.doConeSearch;
            searchLabel = 'Catalog Search';
	    downloadLinkText = 'Download Catalog Records';
        }

        var wavebandString = me.formatKeywords(record.get('waveband'));

        var subjectString = me.formatKeywords(record.get('subject'));
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Left panel for summarizing and giving access to DataScope results for this resource.

        var searchSummary = null;
        var hits = record.get('hits');
        if (hits) {
            // There is a hits record, so this must be a DataScope result, so we should display the search summary.
            var searchSummaryItems = [];
    
            // Description
            searchSummaryItems.push({
                fieldLabel: 'Records Found When Searching ' + searchText,
                labelWidth: leftLabelWidth,
                value: record.get('hits')
            });
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
                    var accessURL = record.get('tableURL');
                    var title = record.get('shortName') + ": " + searchText;
                    if (accessURL) {
                        me.app.searchVoTable(accessURL, title);
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
            
            
            var accessURL = record.get('tableURL');
            var hyperLink = Mvp.util.Util.createLink(accessURL, downloadLinkText);
            loadButtons.push({
                xtype: 'displayfield',
                fieldLabel: 'or',
                labelWidth: 20,
                value: hyperLink
            });
            
            //loadButtons.push({
            //    xtype: 'button',
            //    text: 'Download Records to Disk',
            //    handler: function () {
            //        var accessURL = record.get('tableURL');
            //        var title = 'Download ' + record.get('shortName') + ": " + searchText;
            //        if (accessURL) {
            //            window.open(accessURL, title);
            //        } else {
            //            alert("Data table unavailable for " + title);
            //        }
            //    }
            //});
            
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
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Lower left panel for performing a new search on this resource
        
        var newSearch = null;
        
        if (me.searchAction) {
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
                width: leftTitleWidth,
               // html: '<h2>' + record.get('title') + '</h2>'
               //value: '<div id="resourceTitle">' + record.get('title') + '<br>&nbsp;</div>'
               value: '<div id="resourceTitle">' + record.get('title') + '</div>'
            }];
        
        if (searchSummary) {
            leftColumnItems.push(searchSummary);
        }
        
        if (newSearch) {
            leftColumnItems.push(newSearch);
        }
               
        var leftColumn = {
            xtype: 'container',
            //flex: flexLeft,
            items: leftColumnItems
        };

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////  Right panel for general resource info
        
        var resourceInfoItems = [
            {
                value: record.get('description')
            }, {
                fieldLabel: 'Publisher',
                value: record.get('publisher') 
            },{
                fieldLabel: 'Website',
                value: Mvp.util.Util.createLink(record.get('referenceURL') )
            }, {
                fieldLabel: 'Wavebands',
                value: wavebandString
            }, {
                fieldLabel: 'Subjects',
                value: subjectString
            }, {
                value: '&nbsp;'
            }, {
                value: '&nbsp;'
            }, {
                value: '&nbsp;'
            }
            ]
              
        var resourceInfo = {
            xtype: 'fieldset',
            flex: flexRight,
            title: 'Resource Information',
            defaultType: 'displayfield',
            layout: 'anchor',
            defaults: {
                anchor: '100%',
                hideEmptyLabel: true
            },
            items: resourceInfoItems
        };


        //            // Don't include?
        //            // descriptionUpdated
        //            // typicalRegionSize
        //            // version
        //            // capabilityValidationLevel (might still be nice to use this)
        //            // interface* and supportedInputParams.  DS handles that for us unless we want to do our own query
        //            // (as we will for the registry)
        //            // maxRecords (don't know what this is for)
        //            // publisherIdentifier (don't know why anyone would need this)
        //            // fitsImages and nonFitsImages (what does this tell us?)
        //            // tableUrl
        //            // accessUrl
        //            // serviceStatus
   
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
                labelWidth: rightLabelWidth
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
    }

});