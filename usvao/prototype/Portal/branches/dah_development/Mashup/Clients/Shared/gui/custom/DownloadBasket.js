Ext.define('Mvp.gui.custom.DownloadBasket', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.gui.FilterView',
        'Mvp.gui.GridView',
        'Mvpd.view.DownloadWindowContainer',
        'Ext.button.Button',
        'Mvp.app.DownloadBasket'
    ],

    /**
    * @cfg {Number} pagesize
    * The number of items to have on one page in the grid display.
    */

    // Public methods

    // Private Methods

    constructor: function (config) {
        config.title = false;
        config.header = false;
        this.callParent(arguments);
    },

    createExtraItems: function (config) {
        var me = this;

        var extraItems = [];

        // Export button.
        //var exportButton = this.grid.getExportButton();
        var width = 180;
        this.downloadSizeLabel = Ext.create('Ext.form.Label', {
            text: '0.00 MB Selected / ' + Mvp.custom.Generic.filesizeConverter(Mvp.util.Constants.MAX_BUNDLE_REQUEST, 'MB').toFixed(0) + ' MB Max',
            width: width - 5,
            style: 'text-align: right'
        });
        this.downloadInfoContainer = Ext.create('Ext.container.Container', {
            width: width,
            items: this.downloadSizeLabel,
            layout: {
                type: 'hbox',
                pack: 'end'
            }
        });
        this.retryButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/ref_24x24.png',
            scale: 'medium',
            tooltip: 'Retry the last download bundle',
            scope: this,
            shadow: true,
            disabled: !Mvp.app.DownloadBasket.lastDownloadUrl,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 10',
            handler: this.retryDownload
        });
        this.removeButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/trash.png',
            scale: 'medium',
            tooltip: 'Selection Required to Remove Items',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.removeClicked,
            disabled: true
        });
        this.downloadButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/savas_24x24.png',
            scale: 'medium',
            tooltip: 'Selection Required to Download Items',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.downloadPressed,
            disabled: true
        });
        extraItems.push(this.downloadInfoContainer, this.retryButton, this.removeButton, this.downloadButton);

        return extraItems;
    },

    removeClicked: function(btn, event, config) {
        this.removeFiles(undefined);
    },

    removeFiles: function (recordList) {    // remove files from the store and IDs from the download basket
        // recordList can be used when calling back from another place, e.g. after finishing a download
        var store = this.controller.store;
        var records = recordList || store.getSelectedRecords();
        var nFiles = records.length;
        if (!nFiles) return;

        Mvp.app.DownloadBasket.remove('CAOM', records, 'obsID');    // remove the IDs from the basket
        store.remove(records);                          // adjust the stores
        store.origTotalCount -= nFiles;                 // correct store counts
        store.backingStore.remove(records);
        store.backingStore.origTotalCount -= nFiles;
        store.load();                                   // present the adjusted store
        if (!store.data.length) store.clearFilter();    // if the forward store is empty, clear filters so the user can see what's left
        store.addHistograms();                          // adjust all the histogram counts

        records = store.getSelectedRecords().concat(store.getUnselectedRecords());
        Mvp.app.DownloadBasket.add('CAOM', records, 'obsID');    // add back the obs IDs that are still in the store

        this.controller.update(this.controller.lastUpdateObject);   // adjusts the status items and sync the check-all box
        this.grid.grid.getSelectionModel().syncHeaderCheckbox();
        this.selectionChanged(undefined, records);  // recalculates headers and toggles buttons
        this.fireEvent('APP.context.DownloadBasket.changed', {  // alert TopBarContainer
            type: 'APP.context.DownloadBasket.changed',
            context: this.controller
        });
    },

    downloadPressed: function () {  // initiate a download
        var title = this.controller.getTitle();

        // Specify the file type and name.
        var filename = "Portal_" + Ext.Date.format(new Date(), 'Y-m-d Hi').replace(' ', 'T');

        // Present the user dialog so they can select a file type and modify the file name.
        var downloadWindow = Ext.create('Mvpd.view.DownloadWindowContainer', { controller: this.controller });
        downloadWindow.addListener('downloadcomplete', this.downloadComplete, this);
        downloadWindow.setFilename(filename);
        downloadWindow.show();
    },
    
    downloadComplete: function (config) {   // callback after download
        this.removeFiles(config.records);
        Mvp.app.DownloadBasket.lastDownloadUrl = config.url;
        this.retryButton.enable();
    },

    retryDownload: function () {    // lets the user retry the last bundle the browser attempted
        var url = Mvp.app.DownloadBasket.lastDownloadUrl;
        if (!url) return;
        Ext.log('Opening as an attachment: ' + url);
        Ext.log("download() url: " + url);
        Ext.core.DomHelper.append(document.body, {
            tag: 'iframe',
            frameBorder: 0,
            width: 0,
            height: 0,
            css: 'display:none;visibility:hidden;height:1px;',
            src: url
        });
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
        // Add custom renderers.
        var columns = updateObject.columnInfo.columns;
        for (c in columns) {
            var col = columns[c];
            var index = col.dataIndex;
            if (index == 'size') {
                col.renderer = this.renderFilesize
            }
        }

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
        em.addListener('APP.context.records.filtered', this.filtersChanged, this);
        return grid;
    },

    renderFilesize: function (value, metaData, record, rowIndex, colIndex, store, view) {
        return value ? Mvp.custom.Generic.filesizeConverter(value, 'MB').toFixed(3) + ' MB': '';
    },

    filtersChanged: function (evt) {    // feign a grid selection event to feed the selectionChanged function
        if (this.controller != evt.context) return; // ignore filters from other contexts
        var selected = [];
        this.controller.store.backingStore.each(function (record) {
            if (record.get('_selected_')) selected.push(record);
        });
        this.selectionChanged(undefined, selected);
    },

    selectionChanged: function (obj, selected, eOpts) {  // enable/disable actions and update labels/tooltips based on selection
        if (selected.length == 0) {
            this.removeButton.disable();
            this.removeButton.setTooltip('Selection Required to Remove Items');
            this.downloadButton.disable();
            this.downloadButton.setTooltip('Selection Required to Download Items');
        } else {
            this.removeButton.enable();
            this.removeButton.setTooltip('Remove Selected Items');
            this.downloadButton.enable();
            this.downloadButton.setTooltip('Download Selected Items');
        }

        // update the file size label
        var sum = 0,
            unknown = false;
        this.controller.store.backingStore.each(function (record) {
            var selected = record.get('_selected_'),
                size = record.get('size');
            if (selected) {
                if (size == 0) unknown = true;
                sum += size;
            }
        });
        var unit = (sum > Mvp.util.Constants.GB) ? 'GB' : 'MB',
            size = Mvp.custom.Generic.filesizeConverter(sum, unit).toFixed(2),  // scale file size appropriately
            tooLarge = (sum > Mvp.util.Constants.MAX_BUNDLE_REQUEST);
        var text = size + (unknown ? '+' : '') + (tooLarge ? '!' : '') + ' ' + unit + ' Selected / ' +   // add a + when total filesize isn't known, ! when max is exceeded
            Mvp.custom.Generic.filesizeConverter(Mvp.util.Constants.MAX_BUNDLE_REQUEST, unit).toFixed(0) + ' ' + unit + ' Max';
        this.downloadSizeLabel.setText(text);
        this.controller.store.tooLarge = tooLarge;
        this.fireEvent('APP.context.DownloadBasket.selectionchanged', {  // alert TopBarContainer
            type: 'APP.context.DownloadBasket.selectionchanged',
            context: this.controller
        });
    }
});