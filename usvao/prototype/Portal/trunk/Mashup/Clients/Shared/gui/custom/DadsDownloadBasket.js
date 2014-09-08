Ext.define('Mvp.gui.custom.DadsDownloadBasket', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.gui.FacetedGridView'
    ],

    constructor: function (config) {
        // These are extra items that will be put in the top bar.
        var extraItems = this.createExtraItems(config);

        // Apply non-overridable config items.  Make this thing a border panel.      
        Ext.apply(config, {
            extraItems: extraItems,
            closable: false
        });

        this.callParent(arguments);

        // Necessary for telling the parent where to add the grid.
        this.gridContainer = this.centerPanel;
    },

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

    createGrid: function (updateObject) {
        // Create the grid.
        var grid = Ext.create('Mvp.grid.MvpGrid', {
            store: updateObject.store,
            numberRows: true,
            columnInfo: updateObject.columnInfo,
            context: this.controller
        });

        grid.addListener('selectionchange', this.selectionChanged, this);
        this.app = window.app;
        var em = this.app.getEventManager();
        //em.addListener('APP.context.records.filtered', this.filtersChanged, this);
        return grid;
    },

    createExtraItems: function (config) {
        this.removeButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.TRASH_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Selection Required to Remove Items',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.removeClicked,
            disabled: true,
            hidden: true        // has to be hidden until Z_JOBNUM is in the data
        });
        this.openButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.OPEN_BROWSER_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            style: 'border: 1px solid #000000;',
            scope: this,
            handler: this.download,
            disabled: true
        });

        return [this.removeButton, this.openButton];
    },

    download: function (config) {
        var params = '';
        this.controller.store.backingStore.each(function (record) {
            if (record.get('_selected_')) {
                var ds = record.get('data_set_name'),
                    fn = record.get('file_name');
                if (ds && fn) params += '&stdads_pmark=' + ds + '/' + fn;
            }
        }, this);

        var url = 'http://archive.stsci.edu/cgi-bin/jwst/sid/dataset_lookup?mission=sidarchive';
        window.open(url + params, '_blank');
        this.fireEvent('APP.context.DownloadBasket.changed', {  // alert TopBarContainer
            type: 'APP.context.DownloadBasket.changed',
            context: this.controller
        });
        //window.downloadBasketWindow.close();
    },

    removeClicked: function(obj, event, eOpts) {
        this.remove();
    },

    remove: function (recordList) {    // remove files from the store and IDs from the download basket
        // recordList can be used when calling back from another place, e.g. after finishing a download
        var store = this.controller.store;
        var records = recordList || store.getSelectedRecords();
        var nFiles = records.length;
        if (!nFiles) return;
        var idCol = 'Z_JOBNUM';

        Mvp.app.DownloadBasket.remove('SIDBYJOB', records, idCol);    // remove the IDs from the basket
        store.remove(records);                          // adjust the stores
        store.origTotalCount -= nFiles;                 // correct store counts
        store.backingStore.remove(records);
        store.backingStore.origTotalCount -= nFiles;
        store.load();                                   // present the adjusted store
        if (!store.data.length) store.clearFilter();    // if the forward store is empty, clear filters so the user can see what's left
        store.addHistograms();                          // adjust all the histogram counts

        records = store.getSelectedRecords().concat(store.getUnselectedRecords());
        Mvp.app.DownloadBasket.add('SIDBYJOB', records, idCol);    // add back the obs IDs that are still in the store

        this.controller.update(this.controller.lastUpdateObject);   // adjusts the status items and sync the check-all box
        this.grid.grid.getSelectionModel().syncHeaderCheckbox();
        this.selectionChanged(undefined, records);  // recalculates headers and toggles buttons
        this.fireEvent('APP.context.DownloadBasket.changed', {  // alert TopBarContainer
            type: 'APP.context.DownloadBasket.changed',
            context: this.controller
        });
    },

    selectionChanged: function (obj, selected, eOpts) {
        var i = selected.length;
        if (i) {
            this.openButton.enable();
            this.removeButton.enable();
        } else {
            this.openButton.disable();
            this.removeButton.disable();
        }
    }
});