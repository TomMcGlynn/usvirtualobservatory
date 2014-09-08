
Ext.onReady(function(){

    // shorthand alias
    var fm = Ext.form;
    
    //
    // The column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store (created below)
    //
    var cm = new Ext.grid.ColumnModel([{
           header: "Short Name",
           dataIndex: 'abbr',
           width: 100
        }, {
           header: "Long Name",
           dataIndex: 'state',
           width: 200
        }]);

    // by default columns are sortable
    cm.defaultSortable = true;

    // create the Data Store
    var ds = new Ext.data.Store({
		proxy: new Ext.ux.data.PagingMemoryProxy(Ext.exampledata.states),
		reader: new Ext.data.ArrayReader({}, [
			{name: 'abbr'},
			{name: 'state'}
		]),
        remoteSort: true
    });

    // create the editor grid
    var grid = new Ext.grid.GridPanel('grid', {
        ds: ds,
        cm: cm,
        selModel: new Ext.grid.RowSelectionModel(),
        enableColLock:false
    });

    var layout = Ext.BorderLayout.create({
        center: {
            margins:{left:3,top:3,right:3,bottom:3},
            panels: [grid]
        }
    }, 'divMain');

    //
    // render grid and footer 
    //
    grid.render();

    var gridFoot = grid.getView().getFooterPanel(true);

    // add a paging toolbar to the grid's footer
    var paging = new Ext.PagingToolbar(gridFoot, ds, {
        pageSize: 6,
        displayInfo: false
    });

    // trigger the data store load
    ds.load({params:{start:0, limit:6}});
});