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
    
    getHlaResults();
});

function getHlaResults()
{ 
    var request = {};
    request.service = 'Hla.Hubble.Votable';
    request.format = 'extjs';

    Ext.Ajax.request({ 
        method: 'GET',
        params : {request : Ext.encode(request)},
        url: '../../Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("getHlaResults: success:");
            var result = Ext.decode(response.responseText); 
            var table = getResultTable(result);
            if (table)
            {
            	createHlaGrid(table);
            }
            else
            {
                Ext.log ("getHlaResults: invalid result");
            	alert ("getHlaResults: invalid result");
            }
        },
        failure: function( response, request) {
        	alert("getHlaResults: failure: " + response.status + " : " + response.statusText);
            Ext.log("getHlaResults: failure: " + response.status + " : " + response.statusText);
        },
    });
}

function getResultTable(result)
{
    var rt = null;
	if (result != null && 
        result.status == "COMPLETE" && 
        result.data != null && 
        result.data.Tables != null &&
        result.data.Tables.length > 0)
    {
        rt = result.data.Tables[0];
    }
    return rt;
}

function createHlaGrid(table)
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
        stateId: 'sidHlaGridTable',
        columns: table.Columns,
        height: 350,
        width: 1200,
        title: 'HLA Results',
        renderTo: 'divHlaGrid',
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
                listeners: {
			        click: function() {
			            onExportGrid(grid);
			        }
			    }
			}]
        }]
    }); 
    
    /*
    // update panel body on selection change
    grid.getSelectionModel().on('selectionchange', function(sm, selectedRecord) {
        if (selectedRecord.length) {
            getProducts(selectedRecord[0].data.hp_id);
        }
    }); 
    */
    
    // Save the Table on the grid for onExportGrid()
    grid.Fields = table.Fields;  
    grid.Columns = table.Columns;  
    grid.Rows = table.Rows;  
    grid.Filename = "Projects.xls";
}

function onExportGrid(grid)
{
	Ext.log("onExportGrid: grid = " + grid);
	Ext.log("onExportGrid: grid.Fields.length = " + grid.Fields.length);
	Ext.log("onExportGrid: grid.Columns.length = " + grid.Columns.length);
	Ext.log("onExportGrid: grid.Rows.length = " + grid.Rows.length);
	Ext.log("onExportGrid: grid.Filename = " + grid.Filename);
		
    var table = {};
    table.name = "Export Table";
    table.Fields = grid.Fields;
    table.Columns = grid.Columns;
    table.Rows = grid.Rows;
        
    var request = {};
    request.service = 'Mashup.Table.Exporter';
    request.format = 'extjs';
    request.filename = grid.Filename; 
    request.filetype = 'xls'; 
    request.data = table;

    Ext.Ajax.request({ 
        method: 'POST',
        params : {request : Ext.encode(request)},
        url: '../../Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("onExportGrid: success:");
            var result = Ext.decode(response.responseText);
            if (result != null && result.status == "COMPLETE" && result.data != null && result.data.url != null)
            { 
                Ext.log("onExportGrid: result.data.url: " + result.data.url);
                download(result.data.url);
            }
            else
            {
                Ext.log ("onExportGrid: result is invalid:" + result.msg);
            	alert ("onExportGrid: result is invalid:" + result.msg);
            }
        },
        failure: function( response, request) {
        	alert("onExportGrid: failure: " + response.status + " : " + response.statusText);
            Ext.log("onExportGrid: failure: " + response.status + " : " + response.statusText);
        },
    });
}

function download(url)
{
    Ext.log("download: url: " + url);
    Ext.core.DomHelper.append(document.body, {
		tag: 'iframe',
		frameBorder: 0,
		width: 0,
		height: 0,
		css: 'display:none;visibility:hidden;height:1px;',
		src: url
	});
}

function downloadJavaScript(url)
{
    Ext.log("download: url: " + url);
    var iframe = document.createElement("iframe");
    iframe.src = url;
    iframe.style.display = "none";
    document.body.appendChild(iframe);
}
