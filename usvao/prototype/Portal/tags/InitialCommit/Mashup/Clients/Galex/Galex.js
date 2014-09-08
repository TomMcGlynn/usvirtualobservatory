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
    //Ext.Ajax.useDefaultXhrHeader = false;
    Ext.Ajax.request({ 
        method: 'GET',
        params :{ra: "9.0", dec: "-43.0", radius: ".05", format: "extjs" },
        url: '../../Mashup.asmx/VoGalexCone',
        success: function ( result, request ) {
            console.log("getProjects(): success: ");
            var dataset = Ext.decode(result.responseText);
            createGrid(dataset.Tables[0]);
        },
        failure: function(result, request) {
            console.log("getProjects(): failure: ");
        	alert("failure: " + result);
        },
    });
}

function createGrid(table)
{    
    console.log("createGrid: table = \n" + Ext.encode(table));
    
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
