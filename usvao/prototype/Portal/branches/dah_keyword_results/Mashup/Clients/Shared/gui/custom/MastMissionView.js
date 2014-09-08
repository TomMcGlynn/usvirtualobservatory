Ext.define('Mvp.gui.custom.MastMissionView', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.grid.MvpGrid',
        'Mvp.gui.GridView',
        'Mvp.custom.MastMission',
        'Mvpc.view.GenericDetailsContainer',
        'Mvp.util.Constants'
    ],

    createGridPanel: function (config) {
        var grid = Ext.create('Mvp.gui.GridView', {
            title: 'Table View',
            overrideCreateGrid: { fn: this.createGrid, scope: this },
            gridConfig: {
            },
            controller: this.controller,
            region: 'center',     // center region is required, no width/height specified
            collapsible: false
        });

        return grid;
    },

    // Provided to the GridView to tell it how to create the grid.
    createGrid: function (updateObject) {

        // Add custom renderers.
        var columnInfo = Ext.clone(updateObject.columnInfo),
            columns = columnInfo.columns,
            mission = this.controller.searchInput.mission;
        this.idIndex = Mvp.util.TableUtils.getColumnbyUCD(columns, /ID_MAIN/);

        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_MEDIUM,
            items: [{
                icon: '../Shared/img/exp_24x24_up.png',
                scale: 'medium',
                tooltip: (mission == 'HLSP') ? 'Go to Project Page': 'Go to Download Page',
                getClass: function (obj, metadata, record, rowIndex, colIndex, store) {
                    metadata.css = 'action-align-middle'    //PortalCustomStyles.css
                },
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    var url = (mission == 'HLSP') ? record.get('Webpage'): Mvp.custom.MastMission.urlGenerator(record.get(this.idIndex), mission, record).url;
                    window.open(url, '_blank');
                },
                scope: this

            }, {
                icon: '../Shared/img/about_24x24.png',
                scale: 'medium',
                tooltip: 'Show Details',
                getClass: function (obj, metadata, record, rowIndex, colIndex, store) {
                    metadata.css = 'action-align-middle'    //PortalCustomStyles.css
                },
                style: 'margin-left:auto; margin-right:auto;',
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
            context: this.controller
        });

        return grid;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + record.get(this.idIndex);
        var detailsContainer = Ext.create('Mvpc.view.GenericDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            content: detailsContainer
        });
    }
});