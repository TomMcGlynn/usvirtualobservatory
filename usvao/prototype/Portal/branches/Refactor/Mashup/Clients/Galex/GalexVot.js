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
    
    getGenericTable();
});

function getGenericTable()
{     
    var request = {};
    request.service = 'Vo.Generic.Table';
    request.format = 'extjs';
    request.params = {url: 'http://127.0.0.1:8080/Temp/Galex.vot'};
        
    Ext.Ajax.request({ 
        method: 'GET',
        params : {request : Ext.encode(request)},
        url: '../../Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("getGenericTable: success: ");
            var result = Ext.decode(response.responseText);
            var table = getResultTable(result);
            if (table != null)
            {
            	createGrid(table);
            }
            else
            {
            	alert("Data Retreival Failed: ");
            }
        },
        failure: function(response, request) {
            Ext.log("getGenericTable: failure: ");
        	alert("getGenericTable: failure: " + response);
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

function createGrid(table)
{    
    Ext.log("createGrid: table = " + table.name);
    
    // create the data store   
    var store = Ext.create('Ext.data.ArrayStore', {
        fields: table.Fields,
        data: table.Rows
    });

    // create the Grid
    var grid = Ext.create('Ext.grid.Panel', {
        store: store,
        stateful: true,
        stateId: 'stateGrid',
        columns: table.Columns,
        height: 350,
        width: 1200,
        title: 'Galex Grid Table',
        renderTo: 'divMain',
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
			            onExport(table);
			        }
			    }
			}]
        }]
    });    
}

function onExport(etable)
{
    Ext.log ("onExport: etable.name :" + etable.name);
    
    var request = {};
    request.service = 'Mashup.Table.Exporter';
    request.format = 'extjs';
    request.filename = 'GalexVot.vot';
    request.filetype = 'vot';
    request.clearcache = "true";
    request.data = etable;

    Ext.Ajax.request({ 
        method: 'POST',
        params : {request : Ext.encode(request)},
        url: '../../Mashup.asmx/invoke',
        success: function ( response, request ) {
            Ext.log("onExport: success:");
            var result = Ext.decode(response.responseText);
            if (result != null && result.status == "COMPLETE" && result.data != null && result.data.url != null)
            { 
                Ext.log("onExport: result.data.url: " + result.data.url);
            	download(result.data.url);
            }
            else
            {
                Ext.log ("onExport: result is invalid:" + result.msg);
            	alert ("onExport: result is invalid:" + result.msg);
            }
        },
        failure: function( response, request) {
        	alert("onExport: failure: " + response.status + " : " + response.statusText);
            Ext.log("onExport: failure: " + response.status + " : " + response.statusText);
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