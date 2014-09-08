Ext.define('Mvp.gui.custom.SidFiles', {
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
            //icon: '../Shared/img/add.png',
            text: 'Lookup Associated Files',
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
        this.jobIds = {};
        this.controller.store.backingStore.each(function (record) {
            if (record.get('_selected_')) {
                this.jobIds[record.get('Z_JOBNUM')] = true;
            }
        }, this);

        var jobString = '',
            first = true,
            count = 0;
        for (var i in this.jobIds) {
            if (!first) jobString += ',';
            jobString += i;
            first = false;
            count++;
        }

        var title = count.toString() + ' Job' + (count ? 's': ''),
            args = {
                inputText: jobString,
                title: title
            },
        searchParams = Mvp.search.SearchParams.getSearch('SIDGETALLFILES');
        this.controller.invokeSearch(args, searchParams);
        //window.downloadBasketWindow.open();
    }
});