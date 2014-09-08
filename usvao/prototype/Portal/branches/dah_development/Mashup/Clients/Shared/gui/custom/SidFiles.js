﻿Ext.define('Mvp.gui.custom.SidFiles', {
    extend: 'Mvp.gui.FacetedGridView',
    requires: [
        'Mvp.gui.FacetedGridView'
    ],

    constructor: function (config) {
        // These are extra items that will be put in the top bar.
        var extraItems = this.createExtraItems(config);

        // Apply non-overridable config items.  Make this thing a border panel.      
        Ext.apply(config, {
            extraItems: extraItems
        });

        this.callParent(arguments);

        // Necessary for telling the parent where to add the grid.
        this.gridContainer = this.centerPanel;
    },

    createExtraItems: function (config) {
        this.addButton = Ext.create('Ext.button.Button', {
            icon: '../Shared/img/exp_24x24_up.png',
            scale: 'medium',
            tooltip: 'Lookup all files associated with these job numbers',
            scope: this,
            shadow: true,
            style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
            margin: '0 1 0 1',
            handler: this.addPressed
        });
        this.addButton.addListener('click', this.download, this);

        return [this.addButton];
    },

    download: function (config) {
        var records = [];
        this.controller.store.backingStore.each(function (record) {
            if (record.get('_selected_')) records.push(record);
        }, this);
        Mvp.app.DownloadBasket.add('SIDBYJOB', records, 'Z_JOBNUM');

        var args = {
                inputText: Mvp.app.DownloadBasket.downloadBasket['SIDBYJOB'].slice(1),  // cut out the lazy leading comma
                title: 'SID Jobs'
            },
        searchParams = Mvp.search.SearchParams.getSearch('SIDGETALLFILES');
        this.controller.invokeSearch(args, searchParams);
        //window.downloadBasketWindow.open();
    }
});