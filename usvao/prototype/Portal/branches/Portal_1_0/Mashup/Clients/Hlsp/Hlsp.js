Ext.require([
    'Ext.grid.*',
    'Ext.data.*',
    'Ext.util.*',
    'Ext.state.*'
]);

Ext.onReady(function() {
    Ext.QuickTips.init();
    
    // setup the state provider, all state information will be saved to a cookie
    Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    
    getProjects();
    
});

function getProjects()
{ 
    var request = {};
    request.service = 'Mast.Hlsp.Project';
    request.format = 'extjs';

    Ext.Ajax.request({ 
        method: 'GET',
        params : {request : Ext.encode(request)},
        url: 'http://vaodev.stsci.edu/portal/Mashup/Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("getProjects: success: " + result);
            var result = Ext.decode(response.responseText);
            if (result != null && result.status == "COMPLETE" && result.data != null && result.data.Tables.length > 0)
            {
            	createProjectsGrid(result.data.Tables[0]);
            }
            else
            {
                Ext.log ("Hslp Data Retreival Failed:" + result.msg);
            	alert ("Hslp Data Retreival Failed:" + result.msg);
            }
        },
        failure: function( response, request) {
        	alert("getProjects: failure: " + response.status + " : " + response.statusText);
            Ext.log("getProjects: failure: " + response.status + " : " + response.statusText);
        },
    });
}

function createProjectsGrid(table)
{    
    // create the data store   
    var store = Ext.create('Ext.data.ArrayStore', {
        fields: table.Fields,
        data: table.Rows
    });

    // create the Projects Grid
    var grid = Ext.create('Ext.grid.Panel', {
        store: store,
        stateful: true,
        stateId: 'sidHlspGridTable',
        columns: table.Columns,
        height: 350,
        width: 1200,
        title: 'HLSP Projects',
        renderTo: 'divProjectsGrid',
        viewConfig: {
            stripeRows: true
        },
        dockedItems: [
        {
	        xtype: 'toolbar',
			dock: 'top',
			items: [
			{
                xtype: 'button',
                dock: 'top',
                text: 'Export',
                handler: function() {
                    var rows = [];
					store.each(function(row){
					  rows.push(row.data);
					});
					var jsonData = Ext.encode({rows: rows});
					
                    // data:[<MIME-type>][;charset=<encoding>][;base64],<data>
                    window.open("data:" + "," + jsonData);
                    
					//window.open("http://www.google.com");
				}
			}]
        }]
    });   
    
    // update panel body on selection change
    grid.getSelectionModel().on('selectionchange', function(sm, selectedRecord) {
        if (selectedRecord.length) {
            getProducts(selectedRecord[0].data.hp_id);
        }
    }); 
};

function getProducts(pid)
{     
    var request = {};
    request.service = 'Mast.Hlsp.Products';
    request.format = 'extjs';
    request.params = {};
    request.params.id = pid;
    
    Ext.log("request: \n" + Ext.encode(request));
   
    Ext.Ajax.request({ 
        method: 'GET',
        params :{request: Ext.encode(request)},
        url: 'http://vaodev.stsci.edu/portal/Mashup/Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("getProducts: success: " + response);
            var result = Ext.decode(response.responseText);
            if (result != null && result.data != null && result.status != "ERROR")
            {
            	createProductsGrid(result.data.Tables[0], pid);
            }
            else
            {
                Ext.log ("Hlsp Projects Data Retrieval Failed:" + result.msg);
            	alert ("Hlsp Projects Data Retrieval Failed:" + result.msg);
            }
        },
        failure: function(response, request) {
            Ext.log("getProducts: failure: " + response);
        	alert("getProducts: failure: " + response);
        },
    });
}

function createProductsGrid(table, pid)
{    
    // create the data store  
    if (typeof(pstore) == 'undefined') 
    { 
        Ext.log ("createProductsGrid: creating new pstore...");
        
	    pstore = Ext.create('Ext.data.ArrayStore', {
	        fields: table.Fields,
	        data: table.Rows
	    });

   		// create the Projects Grid
	    pgrid = Ext.create('Ext.grid.Panel', {
	        store: pstore,
	        stateful: true,
	        stateId: 'sidProductsGrid',
	        columns: table.Columns,
	        height: 350,
	        width: 1200,
	        title: "HLSP Products (" + pid + ")",
	        renderTo: 'divProductsGrid',
	        viewConfig: {
	            stripeRows: true
	        }
	    }); 
    }
    else
    {
    	Ext.log ("createProductsGrid: NOT creating new pstore.");
    	pgrid.setTitle("HLSP Products (" + pid + ")");
    	pstore.loadData(table.Rows);
    }
};