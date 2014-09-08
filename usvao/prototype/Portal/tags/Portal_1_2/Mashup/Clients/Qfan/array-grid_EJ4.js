Ext.Loader.setConfig({
    enabled: true
});
//Ext.Loader.setPath('Ext.ux', '../examples/ux');

Ext.require([
	'Ext.selection.CellModel',
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.util.*',
    'Ext.state.*',
    'Ext.state.*',
    'Ext.form.*',
    //'Ext.ux.grid.FiltersFeature',
    //'Ext.ux.CheckColumn'
]);

Ext.onReady(function() {
    Ext.QuickTips.init();
    
    // setup the state provider, all state information will be saved to a cookie
    Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    
    getProjects();

    /**
     * Stylesheet Switcher
     */
    Ext.get('styleswitcher_select').on('change', function(e, select) {
        var name = select[select.selectedIndex].value,
            currentPath = window.location.pathname,
            isCurrent = currentPath.match(name);
        
        if (!isCurrent) {
            window.location = name;
        }
    });
    
});

function getProjects()
{     
    //Ext.Ajax.useDefaultXhrHeader = false;
    Ext.Ajax.request({ 
        useDefaultXhrHeader: 'false',
        method: 'GET',
        params :{format: "extjs" },
        //url: '../../Mashup.asmx/MastHlspProject',
		url: 'mashup_hlsp_numberNdate.txt',
        success: function ( result, request ) {
            var dataset = Ext.decode(result.responseText);
            createTableGrid(dataset.Tables[0]);
        },
        failure: function(result, request) {
        	alert("failure: " + result);
            Ext.log("failure: " + result);
        },
    });
}

function createTableGrid(table)
{    
    /**
     * Custom function used for column renderer
     * @param {Object} val
   
    function change(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '</span>';
        }
        return val;
    }      */

    /**
     * Custom function used for column renderer
     * @param {Object} val
   
    function pctChange(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '%</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '%</span>';
        }
        return val;
    }
	 
	function formatDate(value){
        return value ? Ext.Date.dateFormat(value, 'M d, Y') : '';
    } */      
	
    // create the data store   
    var store = Ext.create('Ext.data.ArrayStore', {
        fields: table.Fields,
        data: table.Rows
    });

	
  /*
  	 var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
        clicksToEdit: 1
    });*/

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
                text	: 'BibCode',
                sortable: true,
                width	: 150,
                dataIndex: 'hp_bibcode',
                id		: 'hp_bibcode'
			},
            {
                text	:'Date',
				sortable: true,
                width	: 100,
                dataIndex: 'hp_date',
                id		: 'hp_date',
                renderer: Ext.util.Format.dateRenderer('m/d/Y'),
				filterrow:{
					xtype:'datefield',
					format:'m/d/Y'
				}
			},
            {
                text	:'NumberF',
				sortable: true,
                width	: 60,
                dataIndex: 'hp_filecount',
                id		: 'hp_filecount'
			},
            {
                text     : 'hp_prodtype',
                sortable : true,
                width    : 100,
                dataIndex: 'hp_prodtype',
				id       : 'hp_prodtype',
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
				}

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
                    '<tpl if="hp_search_flag == &quot;Y&quot;"><a href=http://archive.stsci.edu/hlsp/search.php?hd_id={hp_id}&amp;action=Search><img src=../Shared/css/images/search_button_5c.gif alt=searchbutton border=0></a></tpl>'
                ],
                id: 'hp_search_flag',
				nofilter: {}
            }           
        ],
		
        height: 350,
        width: 1200,
        title: 'HLSP Grid Table',
		//selModel: {
            //selType: 'cellmodel'
        //},
        renderTo: 'grid-example',
		bbar: [
			{
				text: "Clear Filter",
				handler: function() {
					store.clearFilter(true);
					store.load();
					grid.fireEvent("staterestore");					
				}
			}  
		],
		plugins: [ Ext.create('Ext.ux.grid.FilterRow') ],
        viewConfig: {
            stripeRows: true,
        }
    });    
};
