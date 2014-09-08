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

    createExtraItems: function (config) {
        var button = Ext.create('Ext.button.Button', {
            text: 'Initiate Download',
            style: 'border: 1px solid #000000;'
        });
        button.addListener('click', this.download, this);

        return [button];
    },

    download: function (config) {
        this.params = '';
        this.controller.store.backingStore.each(function (record) {
            if (record.get('_selected_')) {
                var ds = record.get('data_set_name'),
                    fn = record.get('file_name')
                if (ds && fn) this.params += '&stdads_pmark=' + ds + '/' + fn;
            }
        }, this);

        var url = 'http://archive.stsci.edu/cgi-bin/jwst/sid/dataset_lookup?mission=sidarchive';
        
        var args = {
            inputText: url + this.params,
            title: 'DADS Retrieval (external page)'
        };
        var searchParams = Mvp.search.SearchParams.getSearch('iFrame');
        this.controller.invokeSearch(args, searchParams);
        window.downloadBasketWindow.close();
    }
});