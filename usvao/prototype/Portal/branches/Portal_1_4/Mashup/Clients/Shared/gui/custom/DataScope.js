Ext.define('Mvp.gui.custom.DataScope', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.CatalogDetailsContainer',
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
            if (index == 'waveband') {
                col.renderer = Mvp.custom.FullSearch.hashColumnRenderer;
            }
            else if (index == 'capabilityClass') {
                col.renderer = Mvp.custom.FullSearch.categoryIconRenderer;
            }
            
            // Commented code showing how to override the default column tooltip
            //
            //var colinfoProps = col.ExtendedProperties;
            //if(colinfoProps){
            //    col.tip = "<table>";
            //    if(colinfoProps["vot.datatype"]){
            //        col.tip += '<tr><td>Data type:</td><td>' + colinfoProps["vot.datatype"] + '</td></tr>';
            //    }
            //    col.tip += "</table>";
            //}            
        }

        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: columnInfo,
            context: this.controller
        });
        grid.on('itemclick', this.createDetailsPanel, this);
        
        return grid;
    },

    createDetailsPanel: function (view, record, htmlElement, index, e) {
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('title');
        var detailsContainer = Ext.create('Mvpc.view.CatalogDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var type = record.get('capabilityClass');
        var icon;
        if (type == 'ConeSearch') {
            icon = Mvp.util.Constants.GENERIC_ICON;
        } else if (type == 'SimpleImageAccess') {
            icon = Mvp.util.Constants.IMAGE_ICON;
        } else icon = Mvp.util.Constants.SPECTRA_ICON;

        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            icon: icon,
            content: detailsContainer
        });
    }

});