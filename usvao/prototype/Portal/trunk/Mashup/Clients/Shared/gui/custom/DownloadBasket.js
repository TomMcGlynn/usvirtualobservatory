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
            icon: Mvp.util.Constants.RETRY_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Retry the last download bundle',
            scope: this,
            shadow: true,
            disabled: !Mvp.app.DownloadBasket.lastDownloadUrl,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 10',
            handler: this.retryDownload
        });
        this.removeButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.TRASH_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Selection Required to Remove Items',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.removeClicked,
            disabled: true
        });
        this.downloadButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.SAVEAS_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Selection Required to Download Items',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.downloadPressed,
            disabled: true
        });
        this.dadsButton = Ext.create('Ext.button.Button', {
            icon: Mvp.util.Constants.OPEN_BROWSER_ICON[Mvp.util.Constants.ICON_SIZE],
            scale: Mvp.util.Constants.ICON_SIZE,
            tooltip: 'Selection Required to Initiate Dads Request',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;',
            margin: '0 1 0 1',
            handler: this.dadsPressed,
            disabled: true
        });
        extraItems.push(this.downloadInfoContainer, this.retryButton, this.removeButton, this.dadsButton, this.downloadButton);

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

        var idCol = records[0].get('obsID') ? 'obsID' : 'obsid';
        Mvp.app.DownloadBasket.remove('CAOM', records, idCol);    // remove the IDs from the basket
        store.remove(records);                          // adjust the stores
        store.origTotalCount -= nFiles;                 // correct store counts
        store.backingStore.remove(records);
        store.backingStore.origTotalCount -= nFiles;
        store.load();                                   // present the adjusted store
        if (!store.data.length) store.clearFilter();    // if the forward store is empty, clear filters so the user can see what's left
        store.addHistograms();                          // adjust all the histogram counts

        records = store.getSelectedRecords().concat(store.getUnselectedRecords());
        Mvp.app.DownloadBasket.add('CAOM', records, idCol);    // add back the obs IDs that are still in the store

        this.controller.update(this.controller.lastUpdateObject);   // adjusts the status items and sync the check-all box
        this.grid.grid.getSelectionModel().syncHeaderCheckbox();
        this.selectionChanged(undefined, store.getSelectedRecords());  // recalculates headers and toggles buttons
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

    dadsPressed: function() {
        var store = this.controller.store,
            records = store.getSelectedRecords(),
            len = records.length,
            dadsIds = {},
            i = len,
            hasDads;
        while (i--) {   // find all the dadsd URLs and pull their IDs
            var record = records[i],
                uri = record.get('dataURI');
            if (uri) {
                var match = uri.match('stdads_mark=');
                var pos = match && match.index;
                if (pos !== null) {
                    var id = uri.substr(pos + 12);
                    dadsIds[id] = true;
                    hasDads = true;
                }
            }
        }

        if (!hasDads) {  // require a DADS selection (this alert shouldn't ever happen anyways)
            Ext.Msg.alert({ msg: 'None of the selected records are DADS requests...', title: 'Error' });
            var task = new Ext.util.DelayedTask(function () { Ext.Msg.close() });
            task.delay(2000);
            return;
        }

        var params = '',
            dadsRecords = [];
        for (var i in dadsIds) {
            var append = 'stdads_mark=' + i;
            params += '&' + append;
            var bs = store.backingStore;
            bs.each(function (record) { // find all the records unselected in the grid for DADS requests whose IDs had been selected so we can remove them
                var uri = record.get('dataURI');
                if (uri.match(append) !== null) dadsRecords.push(record);
            });
        }
        if (params) {
            var html = 'http://archive.stsci.edu/cgi-bin/stdads_retrieval_options?mission=hst&RESULTS_TYPE=science' + params;
            window.open(html, '_blank');
            this.removeFiles(dadsRecords);
        }
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
        var i = selected.length,
            downloadEnabled = false,
            dadsEnabled = false;
        while(i--) {
            (selected[i].get('dataURI').match('stdads_mark=') !== null) ? dadsEnabled = true : downloadEnabled = true;
            if (downloadEnabled && dadsEnabled) break;
        }

        if (selected.length == 0) {
            this.removeButton.disable();
            this.removeButton.setTooltip('Selection Required to Remove Items');
        } else {
            this.removeButton.enable();
            this.removeButton.setTooltip('Remove Selected Items');
        }
        if (downloadEnabled) {
            this.downloadButton.enable();
            this.downloadButton.setTooltip('Download Selected Items');
        } else {
            this.downloadButton.disable();
            this.downloadButton.setTooltip('Selection Required to Download Items');
        }
        if (dadsEnabled) {
            this.dadsButton.enable();
            this.dadsButton.setTooltip('Initiate Dads Request');
        } else {
            this.dadsButton.disable();
            this.dadsButton.setTooltip('Selection Required to Initiate Dads Request');
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