Ext.define('Mvp.gui.custom.Registry', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.custom.FullSearch',
        'Mvp.gui.GridView',
        'Mvp.grid.MvpGrid',
        'Mvpc.view.RegistryDetailsContainer',
        'Mvp.util.Constants',
        'Mvp.custom.Registry',
        'Mvp.search.Datascope'
    ],

    // Private methods

    constructor: function (config) {
        this.datascopeSearchButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.SEARCH_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Datascope search of all selected services',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.datascopeSearchPressed,
            disabled: true
        });

        this.datascopeThreeBox = Ext.create('Mvpd.view.VaoPositionSearchPanel', {});
        //this.datascopeThreeBox.on('searchInitiated', this.datascopeInvokeSearch, this);
        this.datascopeThreeBox.on('searchInitiated', this.batchInvokeSearch, this);
        this.datascopeSearchWindow = Ext.create('Ext.window.Window', {
            title: 'Search All Selected Resources with Datascope',
            layout: 'fit',
            plain: true,
            closable: true,
            closeAction: 'hide',    
            items: this.datascopeThreeBox
        });
        
        this.callParent(arguments);
    },
        
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
                col.tdCls = 'action-align-middle';
                col.renderer = Mvp.custom.FullSearch.categoryIconRenderer;
            }
            else if (col.dataIndex == 'title') {
                col.renderer = Mvp.custom.Hst.font13Renderer;
            }
            else if (col.dataIndex == 'description') {
                col.renderer = Mvp.custom.Hst.font13Renderer;
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
        var actionColumn = {
            xtype: 'actioncolumn',
            menuDisabled: true,
            sortable: false,
            text: 'Actions',
            width: Mvp.util.Constants.ACTION_COLUMN_WIDTH_MEDIUM,
            align: 'center',
            tdCls: 'action-align-middle',
            renderer: Mvp.custom.Generic.gridWhitespace,
            items: [{
                icon: Mvp.util.Constants.SAVEAS_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Download Resource XML',
                iconCls: Mvp.util.Constants.ICON_CLS[Mvp.util.Constants.ICON_SIZE],
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    Mvp.custom.Registry.loadResourceXml(record);
                },
                scope: this

            }, {
                icon: Mvp.util.Constants.ABOUT_ICON[Mvp.util.Constants.ICON_SIZE],
                scale: Mvp.util.Constants.ICON_SIZE,
                tooltip: 'Show Details',
                iconCls: Mvp.util.Constants.ICON_CLS[Mvp.util.Constants.ICON_SIZE],
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
        
        grid.addListener('selectionchange', this.selectionChange, this);
        return grid;
    },

    createDetailsPanel: function (record) {
        var title = 'Details: ' + this.controller.getTitle() + ' - ' + record.get('title');
        var detailsContainer = Ext.create('Mvpc.view.RegistryDetailsContainer', {
            record: record,
            controller: this.controller
        });
        var type = record.get('capabilityClass');
        var icon;
        if (type == 'ConeSearch') {
            icon = Mvp.util.Constants.CATALOG_ICON['small'];
        } else if (type == 'SimpleImageAccess') {
            icon = Mvp.util.Constants.IMAGE_ICON['small'];
        } else if (type == 'SimpleSpectralAccess') {
            icon = Mvp.util.Constants.SPECTRA_ICON['small'];
        }
        var w = Mvp.gui.DetailsWindow.showDetailsWindow({
            title: title,
            icon: icon,
            content: detailsContainer
        });
    },
    
    datascopeSearchPressed: function () {
        var records = this.grid.lastStore.getSelectedRecords();
        var len = records.length;
        if (!len) return;

        var collection = this.controller.searchParams.uid;
        if (collection == 'REGKEYWORD') {
            var records = this.grid.lastStore.getSelectedRecords();
            var len = records.length;
            if (!len) {
                Ext.Msg.show({ msg: 'No resources selected for positional search.', title: 'Warning' });
            }

            var nonServiceResources = '';
            var validServiceRecords = false;
            for (var i = 0; i < len; i++) {
                var data = records[i].data;
                var serviceTypeTag = undefined;
                if (data.capabilityClass == 'SimpleImageAccess' ||
                    data.capabilityClass == 'ConeSearch' ||
                    data.capabilityClass == 'SimpleSpectralAccess') {
                    validServiceRecords = true;
                }
                else {
                    nonServiceResources = nonServiceResources + data.shortName + ' ';
                }
            }

//            if (nonServiceResources.length > 0) {
//                Ext.Msg.show({ msg: 'At least one of the selected resources cannot be positionally searched and will be skipped: ' + nonServiceResources, title: 'Warning' });
//            }

            if (validServiceRecords) {
                this.datascopeSearchWindow.show();
            }
            else {
                Ext.Msg.show({ msg: 'No selected resources can be positionally searched.', title: 'Warning' });
            }
        }
    },
    
    //some duplicate processing, but avoids keeping a store or processing more detailed searchability info twice.
    /*
    datascopeInvokeSearch: function (textValues) {
        this.datascopeSearchWindow.hide();

        var resourceList = [];
        var recordIndex = 0;
        var records = this.grid.lastStore.getSelectedRecords();
        var len = records.length;

        while (recordIndex < len) {
            var data = records[recordIndex].data;
            var serviceTypeTag = undefined;
            if (data.capabilityClass == 'SimpleImageAccess') {
                serviceTypeTag = 'Siap';
            }
            else if (data.capabilityClass == 'ConeSearch') {
                serviceTypeTag = 'Cone';
            }
            else if (data.capabilityClass == 'SimpleSpectralAccess') {
                serviceTypeTag = 'Ssap';
            }

            if (serviceTypeTag) {
                resourceList.push(data.identifier);
            }
            recordIndex++;
        }

        if (resourceList.length > 0) {
            Mvp.search.Datascope.invokeDatascopeQuery(this.controller, textValues[0], resourceList);
        }
    },
    */

    batchInvokeSearch: function (textValues) {
        this.datascopeSearchWindow.hide();

        var resourceList = [];
        var recordIndex = 0;
        var records = this.grid.lastStore.getSelectedRecords();
        var len = records.length;

        while ((resourceList.length < Mvp.util.Constants.MAX_SUMMARY_REQUESTS) && (recordIndex < len)) {
            var data = records[recordIndex].data;
            var serviceTypeTag = undefined;
            if (data.capabilityClass == 'SimpleImageAccess') {
                serviceTypeTag = 'Siap';
            }
            else if (data.capabilityClass == 'ConeSearch') {
                serviceTypeTag = 'Cone';
            }
            else if (data.capabilityClass == 'SimpleSpectralAccess') {
                serviceTypeTag = 'Ssap';
            }

            if (serviceTypeTag) {
                var record = {
                    serviceId: serviceTypeTag,
                    shortName: data.shortName,
                    publisher: data.publisher,
                    title: data.title,
                    description: data.description,
                    invokeBaseUrl: null,
                    capabilityClass: data.capabilityClass,
                    extraInput: { url: data.accessURL }
                };
                resourceList.push(record);
            }
            recordIndex++;
        }

        if (resourceList.length > 0) {
            Mvp.search.Summary.invokeSummaryQuery(this.controller, textValues[0], resourceList);
        }
    },

    selectionChange: function (obj, selected, eOpts) {  // disables buttons when there's no selection
        if (selected.length) {
            this.datascopeSearchButton.enable();
        } else {
            this.datascopeSearchButton.disable();
        }
    }

});