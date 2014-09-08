 Ext.require(['*']);
 
    Ext.onReady(function() {
 
        Ext.QuickTips.init();
 
        // NOTE: This is an example showing simple state management. During development,
        // it is generally best to disable state management as dynamically-generated ids
        // can change across page loads, leading to unpredictable results.  The developer
        // should ensure that stable state ids are set for stateful components in real apps.
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
 
        var viewport = Ext.create('Ext.Viewport', {
            id: 'border-example',
            layout: 'border',
            items: [
            // create instance immediately
            Ext.create('Ext.Component', {
                region: 'north',
                height: 32, // give north and south regions a height
                autoEl: {
                    tag: 'div',
                    html:'<p>north - generally for menus, toolbars and/or advertisements</p>'
                }
            }), {
                // lazily created panel (xtype:'panel' is default)
                region: 'south',
                contentEl: 'south',
                split: true,
                height: 100,
                minSize: 100,
                maxSize: 200,
                collapsible: true,
                collapsed: true,
                title: 'South',
                margins: '0 0 0 0'
            }, {
                xtype: 'tabpanel',
                region: 'east',
                title: 'East Side',
                dockedItems: [{
                    dock: 'top',
                    xtype: 'toolbar',
                    items: [ '->', {
                       xtype: 'button',
                       text: 'test',
                       tooltip: 'Test Button'
                    }]
                }],
                animCollapse: true,
                collapsible: true,
                split: true,
                width: 225, // give east and west regions a width
                minSize: 175,
                maxSize: 400,
                margins: '0 5 0 0',
                activeTab: 1,
                tabPosition: 'bottom',
                items: [{
                    html: '<p>A TabPanel component can be a region.</p>',
                    title: 'A Tab',
                    autoScroll: true
                }, Ext.create('Ext.grid.PropertyGrid', {
                        title: 'Property Grid',
                        closable: true,
                        source: {
                            "(name)": "Properties Grid",
                            "grouping": false,
                            "autoFitColumns": true,
                            "productionQuality": false,
                            "created": Ext.Date.parse('10/15/2006', 'm/d/Y'),
                            "tested": false,
                            "version": 0.01,
                            "borderWidth": 1
                        }
                    })]
            }, {
                region: 'west',
                id: 'west-panel', // see Ext.getCmp() below
                title: 'Filters',
                split: true,
                width: 200,
                minWidth: 175,
                maxWidth: 400,
                collapsible: true,
                animCollapse: true,
                margins: '0 0 0 5',
                layout: 'accordion',
				html:'<p>filter summary <b>message here</b></p>',
				/*
				tbar:[{
					text: 'Select All',
					handler: function() {
						var checkbox3 = Ext.getCmp('checkbox3');
						checkbox3.setValue(true);
						}
					}, 
					'-',
					{
						text: 'Deselect All',
						handler: function() {
							var checkbox1 = Ext.getCmp('checkbox1');
						}
					},
					]
                items: [{
                    title: 'Filters applied',
                    html: '<p>Filters applied are: </p>',
					id: 'filterSum',
                    iconCls: 'settings'
                },  {
                    title: 'Information',
                    html: '<p>Some info in here.</p>',
                    iconCls: 'info'
                }
				]*/
            },
            // in this instance the TabPanel is not wrapped by another panel
            // since no title is needed, this Panel is added directly
            // as a Container
            Ext.create('Ext.tab.Panel', {
                region: 'center', // a center region is ALWAYS required for border layout
                deferredRender: false,
                activeTab: 0,     // first tab initially active
                items: [{
                    contentEl: 'center1',
                    title: 'Close Me',
                    closable: true,
                    autoScroll: true
                }, {
                    contentEl: 'center2',
                    title: 'Center Panel',
                    autoScroll: true
                }]
            })]
        });
        // get a reference to the HTML element with id "hideit" and add a click listener to it
        Ext.get("hideit").on('click', function(){
            // get a reference to the Panel that was created with id = 'west-panel'
            var w = Ext.getCmp('west-panel');
            // expand or collapse that Panel based on its collapsed property state
            w.collapsed ? w.expand() : w.collapse();
        });
		
		getProjects();		
			
		function getProjects()
		{     
			//Ext.Ajax.useDefaultXhrHeader = false;
			Ext.Ajax.request({ 
				useDefaultXhrHeader: 'false',
				method: 'GET',
				params :{format: "extjs" },
				//url: '../../Mashup.asmx/MastHlspProject',
				//url: 'mashup_hlsp.txt',
				url: 'mashup_hlsp_numberNdate.txt',
				success: function ( result, request ) {
					var dataset = Ext.decode(result.responseText);
					
					// create the data store   
					var store = Ext.create('Ext.data.ArrayStore', {
						fields: dataset.Tables[0].Fields,
						data: dataset.Tables[0].Rows
					});
					
					createTableGrid(store);
				},
				failure: function(result, request) {
					alert("failure: " + result);
					console.log("failure: " + result);
				},
			});
		};

		
		
//****************************************
// force filterrow to take the values from filter check boxes on the left
// filterColName : the specific column has filter checkbox 
// gridName: name of the current grid 
/*
function onFilterFacetChk(filterColName, gridName, tFields){
	var i=0
	var CheckboxValues = '::';
	//process the filter check boxes on the left
	while (Ext.getCmp(filterColName+'_ckbx'+i))
	{
		var myfb=Ext.getCmp(filterColName+'_ckbx'+i);
		myfb.on('change', function(){
			if(this.checked){	
				//gather all checked box value
				CheckboxValues += this.inputValue+'::';
			}else{
				//take out the unchecked box values
				if(CheckboxValues.indexOf(this.inputValue+'::')>0){
					CheckboxValues=CheckboxValues.replace('::'+this.inputValue+'::', '::');		
				}
			}
			//process filtering using filterrow filters.
			var borrowedFilterRow=Ext.getCmp(gridName).getPlugin();
			
			var values = {};
			borrowedFilterRow.eachColumn(function (col) {		
				if(col.id==filterColName){
					values["FilterPanel:" + col.dataIndex] = CheckboxValues;
					console.log(CheckboxValues);
				} 
			});	
			borrowedFilterRow.processFiltering(values);	
			var currentFd=Ext.getCmp(gridName).store;
			Ext.getCmp('recordNum').setText(currentFd.getCount()+ ' record(s) found');
			
			//a new store for rest filters
			var newstore = Ext.create('Ext.data.ArrayStore', {
				fields: tFields
			});
			newstore.loadRecords(currentFd.getRange(0,currentFd.getCount()),{addRecords: false});

			Ext.getCmp('west-panel').remove('hp_wavelength_filterpnl_form', true);
			createFilterFacet(newstore, 'hp_wavelength','Wave length');
			
		});
		i++;
	}	
}
*/	
	
//**************************************
//create filter check box form panel at left
// store - current store
// filterFtItems[] - eg.filterFtItems['hp_prodtype']=' Product Type'
function createFilterFacet(store, filterFtItems, gridName){
	var filterItems =[];
	for (var filterCol in filterFtItems) {		
		if (filterFtItems[filterCol] != '' && filterFtItems[filterCol] != null) { 			
			
			//customized for the hp_prodtype for better displaying on the left.
			/*
			switch(filterCol){
				case 'hp_prodtype':
				var uFilterValues = new Array("Image atlas", "Individual object", "Spectral atlas","Survey", "Time Series", "Composite", "Catalog","Model");
				break;
				case 'hp_wavelength':
				var uFilterValues = new Array("IR","Near IR", "Optical","Ultraviolet","X_Ray","None");				
				break;
				default:
				var uFilterValues=store.collect(filterCol,true, false);				
			}
			*/
			var uFilterValues=store.collect(filterCol,true, false);		
			var fCheckboxItems =[];
				
			for(var i=0; i<uFilterValues.length; i++){
				store.filter(filterCol, uFilterValues[i]);
				fCheckboxItems.push({
					boxLabel  : uFilterValues[i]+ '('+store.getCount()+')',
					name      : filterCol,
					inputValue: uFilterValues[i],
					thisGridName: gridName,
					FacetItems: filterFtItems,
					id        : filterCol +'_ckbx'+i,
					handler	  : onBoxChange
				});
				store.clearFilter(true);		
			}
			if(fCheckboxItems.length>0){
				filterItems.push({
					xtype      : 'fieldcontainer',
					fieldLabel : '<b>'+filterFtItems[filterCol]+'</b>',
					labelAlign : 'top',
					defaultType: 'checkboxfield',
					id:'group_'+filterCol,	
					items: fCheckboxItems  
				});					
			}
		}	
	}
	if(filterItems.length>0){
		var pf_prodtype= Ext.create('Ext.form.Panel', {
			bodyPadding: 4,
			flex:1,
			title      : 'Filters',
			collapsible: true,
			animCollapse: true,		
			autoScroll: true,
			iconCls: 'filtericon',
			items: filterItems,
			id: "filterpnl_form"
		});	
		var westpnl = Ext.getCmp('west-panel');
		westpnl.add(pf_prodtype);
	}
}
	
function onBoxChange(item){
	var gridName=item.thisGridName;
	var FacetItems=item.FacetItems;	
	var values = {};
	var filterSummaryMsg='';
	for(var filterCol in FacetItems){
		if(FacetItems[filterCol]!='' && FacetItems[filterCol]!=null){
			var i=0
			var CheckboxValues = '::';
			//process the filter check boxes on the left
			while (Ext.getCmp(filterCol+'_ckbx'+i))
			{			
				var myfb=Ext.getCmp(filterCol+'_ckbx'+i);
				if(myfb.checked){	
					//gather all checked box value
					CheckboxValues += myfb.inputValue+'::';
				}else{
					//take out the unchecked box values
					if(CheckboxValues.indexOf(myfb.inputValue+'::')>0){
						CheckboxValues=CheckboxValues.replace('::'+myfb.inputValue+'::', '::');		
					}
				}
				i++;			
			}
			values["FilterPanel:" + filterCol] = CheckboxValues;
		}		
	};
	var someChecked=false;
	for (var i in values) {
		if (values[i] != '' && values[i] != null && values[i]!='::') {
			someChecked=true;
			filterSummaryMsg += values[i].replace(/::/gi, "<BR>");
		}
	}
	var currentFd=Ext.getCmp(gridName).store;
	var borrowedFilterRow=Ext.getCmp(gridName).getPlugin();
	var filterTBox=0;
	//add function that gets the value from the filterbox.
	borrowedFilterRow.eachColumn(function (col) {
		if (col.filterField.xtype != 'component') {					
			if (!col.xtype) {                        
				if(col.dataIndex=='hp_prodtype'){
					values["FilterPanel:hp_prodtype"] += (values["FilterPanel:hp_prodtype"])?col.filterField.getValue():'';
				}else if(col.dataIndex == 'hp_wavelength'){
					values["FilterPanel:hp_wavelength"] += (values["FilterPanel:hp_wavelength"])?col.filterField.getValue():'';
				}else{
					values["specialString:" + col.dataIndex] = col.filterField.getValue();
				}
			} else {
				values[col.xtype + ":" + col.dataIndex] = col.filterField.getValue();
			}
			if(col.filterField.getValue()!=''){ 
				filterTBox++; 
				filterSummaryMsg += '<BR>'+col.filterField.getValue()+'<BR>';
		
			}
		};
	});
	
	var filterSummary=Ext.getDom('west-panel');	
	filterSummary.childNodes[1].firstChild.innerHTML='VALUES IN FILTERS ARE: <BR><b>'+filterSummaryMsg+'</b>';
	
	console.log('tbox '+filterTBox);
	if(someChecked){
		//process filtering using filterrow filters.		
		borrowedFilterRow.processFiltering(values);
				
		//a new store for rest filters
		var newstore = Ext.create('Ext.data.ArrayStore', {});
		newstore.loadRecords(currentFd.getRange(0,currentFd.getCount()),{addRecords: false});	
		updateFilterFacet(newstore, FacetItems, item.name, gridName,'notall');
		
	}else{
	
		if(filterTBox>0){
			//process filtering using filterrow filters.
			currentFd.clearFilter(true);
			//console.log('h '+currentFd.getCount());
			
			//a new store for rest filters
			var newstore = Ext.create('Ext.data.ArrayStore', {});
			newstore.loadRecords(currentFd.getRange(0,currentFd.getCount()),{addRecords: false})
			borrowedFilterRow.processFiltering(values);
			
			//update filter facet			
			updateFilterFacet(newstore, FacetItems, item.name, gridName, 'all');
		}else{
			//update recored number display		
			currentFd.clearFilter(true);
			Ext.getCmp('recordNum').setText(currentFd.getCount()+ ' record(s) found');	
						
			//a new store for rest filters
			var newstore = Ext.create('Ext.data.ArrayStore', {});
			newstore.loadRecords(currentFd.getRange(0,currentFd.getCount()),{addRecords: false});	
			//updateFilterFacet(newstore, FacetItems, item.name, gridName);
			Ext.getCmp(gridName).destroy();
			Ext.getCmp("filterpnl_form").destroy();
			createTableGrid(newstore);
		}
	}
}	

//to do: update based on the level of filters, we assume the prodtype is level1
// and wavelength is level2, and blah blah is level 3, so the update rule is 
// if action is on  level 1, then update level 2 and level3,
// if action is on level 2, then upate is on level 3,
// if action is level3, there is no upate on the filter facets.
// range is for partially update or update all. [all/notall]
function updateFilterFacet(store, filterColumns, filterColnm, gridName, range){
	var filterItems = Ext.getCmp('filterpnl_form').items
	var TotalLevels=filterItems.getCount(); 
	if(range=='all'){
		var fLevel=0;
	}else if(range=='notall'){
		var fLevel=switchLevelnName(filterColnm)+1;
	}
	var fLevel=switchLevelnName(filterColnm)+1;
	for (var i=fLevel; i<TotalLevels; i++){
		var CheckboxValues = '::';				
		var thisGroup=filterItems.getAt(i);
		
		Ext.getCmp('filterpnl_form').remove(thisGroup, true);
		
		var filterCol=switchLevelnName(i);
		var uFilterValues=store.collect(filterCol, true, false);
		var fCheckboxItems =[];
		
		
		for(var i=0; i<uFilterValues.length; i++){
			store.filter(filterCol, uFilterValues[i]);	
			fCheckboxItems.push({
				boxLabel  : uFilterValues[i]+ '('+store.getCount()+')',
				name      : filterCol,
				inputValue: uFilterValues[i],
				thisGridName: gridName,
				FacetItems: filterColumns,
				id        : filterCol +'_ckbx'+i,
				handler	  : onBoxChange
			});
			store.clearFilter(true);		
		}	
		
		if(fCheckboxItems.length>0){
			Ext.getCmp('filterpnl_form').add({
				xtype      : 'fieldcontainer',
				fieldLabel : thisGroup.fieldLabel,
				labelAlign : 'top',
				defaultType: 'checkboxfield',
				id:'group_'+filterCol,
				items: fCheckboxItems  
			});					
		}
		
	}
}

//to do: need a function to create filters and corresponding levels. base on supply
function switchLevelnName(colnameOrLvl){
	switch(colnameOrLvl){
		case 'hp_prodtype':
			return 0;
		break;
		case 'hp_wavelength':
			return 1;
		break;
		case 0:
			return 'hp_prodtype';
		break;
		case 1:
			return 'hp_wavelength';
		break;
		default:
		return false;
	}
}
	
function createTableGrid(store)
{  
	// create the Grid
    var grid = Ext.create('Ext.grid.Panel', {
		id: 'grid_fr1',
        store: store,
        stateful: true, 
        stateId: 'stateGrid',
        columns: [
            {
                text     : 'Title',
				xtype	 : 'templatecolumn',
                sortable : true,
                width    : 300,
                dataIndex: 'hp_title',
				tpl: [
                    '<a href=\"{hp_webpage}\">{hp_title}</a>'
                ],
                id: 'hp_title'				
				//field:{}
            },
            {
                text	:'Primary Investigator',
				sortable: true,
                width	: 100,
                dataIndex: 'hp_pi',
                id		: 'hp_pi',
				//field:{}
			},
            {
                text	:'Date',
				sortable: true,
                width	: 100,
                dataIndex: 'hp_date',
                id		: 'hp_date',
				
			},
            {
                text	:'NumberF',
				sortable: true,
                width	: 10,
                dataIndex: 'hp_filecount',
                id		: 'hp_filecount',
                xtype: 'numbercolumn',
                format:'0.00'
			},
			{
                text	: 'BibCode',
                sortable: true,
                width	: 150,
                dataIndex: 'hp_bibcode',
                id		: 'hp_bibcode'
			},
            {
                text     : 'hp_prodtype',
                sortable : true,
                width    : 100,
                dataIndex: 'hp_prodtype',
				id       : 'hp_prodtype',
				/*
				filterrow:{
					xtype:'combo',
					queryMode: 'local',
					displayField: 'name',
					valueField: 'fieldval',
					store:Ext.create('Ext.data.Store', {
						fields:['name', 'fieldval'],
						data:[{
							name:'survey', fieldval:'survey'
						},{
							name:'atlas', fieldval:'atlas'
						},{
							name:'individual', fieldval:'individual'
						}]
					})
				}*/

				//field:{}
            },
            {
                text     : 'hp_objtype',
                sortable : true,
                width    : 200,
                dataIndex: 'hp_objtype',
				id       : 'hp_objtype',
				//field:{}
            },
            {
                text     : 'hp_wavelength',
                width    : 100,
                sortable : true,
                dataIndex: 'hp_wavelength',
				id       : 'hp_wavelength'
				//field:{}
            },
            {
                text     : 'hp_search_flag',
                width    : 100,
                sortable : true,
                dataIndex: 'hp_search_flag',
				xtype	 : 'templatecolumn',
				tpl: [
                    '<tpl if="hp_search_flag == &quot;Y&quot;"><a href=http://archive.stsci.edu/hlsp/search.php?hd_id={hp_id}&amp;action=Search><img src=search_button_5c.gif alt=searchbutton border=0></a></tpl>'
                ],
                id: 'hp_search_flag',
				nofilter: {}
            }           
        ],
		
        height: 350,
        width: 1200,
        title: 'HLSP Grid Table',
        renderTo: 'grid-example',
		bbar: [
			{
				text: "Clear Filter",
				handler: function() {
					store.clearFilter(true);
					store.load();
					grid.fireEvent("staterestore");					
				}
			}, 
			'-',
			{
				xtype: 'tbtext',
				text: '',
				id:'recordNum'
			}
		],
		plugins: [ Ext.create('Ext.ux.grid.FilterRow') ],
        viewConfig: {
            stripeRows: true,
        }
    });
	
	Ext.getCmp('recordNum').setText(store.getCount() + ' record(s) found');
	
	// creating filter facets
	var filterFacetItems={};
	filterFacetItems['hp_prodtype']='Product Type';
	filterFacetItems['hp_wavelength']='Wave Length';
	createFilterFacet(store,filterFacetItems,'grid_fr1');
	
};
    });
	
	/* ********************
	// filter panel using store and grid.
	var fpstore = Ext.create('Ext.data.ArrayStore', {
        fields: [{name:'typename'}, {name:'rnumber'}],
        data: prodTPCount
    });
	console.log("line170:"+Ext.JSON.encode(prodTPCount));
	var fpgrid=Ext.create('Ext.grid.Panel', {
		id: 'fpgrid',
        store: fpstore,
        stateful: true, 
        stateId: 'statefpGrid',
		columns: [
            {
                text     : 'Type',
                sortable : true,
                flex: 4,
                dataIndex: 'typename',
                id: 'typename'	
            },
			{
                text     : '#',
                sortable : true,
                flex: 1,
                dataIndex: 'rnumber',
                id: 'rnumber'	
            }],
		height: 350,
        width: 1200,
        title: 'filter panel',
        renderTo: 'filter_panel_grid',
		viewConfig: {
            stripeRows: true,
		}
	});
	*/