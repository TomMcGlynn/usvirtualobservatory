Ext.define('Mvp.gui.custom.Keywords', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.KeywordDetailsContainer',
        'Mvp.util.Constants'
    ],
    // Private methods

    // Override
    createGridPanel: function (config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            overrideCreateGrid: { fn: this.createGrid, scope: this },
            overrideCreateDetailsPanel: { fn: this.createDetailsPanel, scope: this },
            gridConfig: {
        },
        controller: this.controller,
        region: 'center',     // center region is required, no width/height specified
        // width: 600,
        collapsible: false
    });

    return grid;
},

createGrid: function (updateObject) {
    // Add custom renderers.
    var columnInfo = Ext.clone(updateObject.columnInfo);
    var columns = columnInfo.columns;
    for (c in columns) {
        var col = columns[c];
        var index = col.dataIndex;
        if (index == 'keywordList') {
            col.renderer = Mvp.custom.FullSearch.pipeColumnRenderer;
        }
    }
    var actionColumn = {
        xtype: 'actioncolumn',
        menuDisabled: true,
        sortable: false,
        text: 'Actions',
        width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_SMALL,
        align: 'center',
        tdCls: 'action-align-middle',
        renderer: Mvp.custom.Generic.gridWhitespace,
        items: [{
            icon: '../Shared/img/about_24x24.png',
            scale: 'medium',
            tooltip: 'Show Details',
            iconCls: 'icon-align',
            handler: function (view, rowIndex, colIndex, item, e, record) {
                this.createDetailsPanel(record);
            },
            scope: this
        }]
    };
    columns.splice(0, 0, actionColumn);

    // Create the grid.
    var grid = Ext.create('Mvp.grid.MvpGrid', {
        store: updateObject.store,
        numberRows: true,
        columnInfo: columnInfo,
        context: this.controller,
        listeners: {
            'itemmouseenter': function (obj, record, item, index, e, eOpts) {
                this.selModel.suspendEvents()
            },
            'itemmouseleave': function (obj, record, item, index, e, eOpts) {
                this.selModel.resumeEvents()
            }
        },
        scope: this
    });

    return grid;
},

createDetailsPanel: function (record) {
    var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('title');
    var detailsContainer = Ext.create('Mvpc.view.KeywordDetailsContainer', {
        record: record,
        controller: this.controller
    });
    var w = Mvp.gui.DetailsWindow.showDetailsWindow({
        title: title,
        content: detailsContainer
    });
}

});