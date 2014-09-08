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
    
    getConeSearch();
});

function getConeSearch()
{     
    //Ext.Ajax.useDefaultXhrHeader = false;
    Ext.Ajax.request({ 
        method: 'GET',
        params :{ra: "9.0", dec: "-43.0", radius: ".05", format: "extjs", timeout: "10", clearcache: "false" },
        url: '../../Mashup.asmx/VoGalexCone',
        success: function ( response, request ) {
            Ext.log("getProjects(): success: " + response.responseText);
            var result = Ext.decode(response.responseText);

            if (result.status == "COMPLETE" && result.data != null && result.data.Tables.length>0)
            {
            	createGrid(result.data.Tables[0]);
            }
            else
            {
            	alert("Data Retreival Failed: " + result.msg);
            }
        },
        failure: function(response, request) {
            Ext.log("getProjects(): failure: ");
        	alert("failure: response: " + response);
        },
    });
}

function createGrid(table)
{    
    Ext.log("createGrid: called!");
    
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
        }
    });    
};
