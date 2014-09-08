Ext.define('Mvpc.view.SummaryDetailsContainer', {
    requires: ['Mvpc.view.GenericDetailsContainer', 'Mvp.util.SearchBox', 'Mvp.util.Exporter'],
    extend: 'Mvpc.view.ui.DatascopeDetailsContainer',

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    constructor: function (config) {
        this.callParent(arguments);    //set up basic Container class variables
        Ext.apply(this, config);

        this.summaryPanel = this.getComponent('summaryPanel');
        this.detailsToolbar = this.summaryPanel.getComponent('detailsToolbar');
        this.loadButton = this.detailsToolbar.getComponent('loadButton');
        this.downloadButton = this.detailsToolbar.getComponent('downloadButton');
        this.searchToolbar = this.summaryPanel.getComponent('searchToolbar');
        this.summaryContainer = this.summaryPanel.getComponent('summaryContainer');
        this.detailsPanel = this.getComponent('detailsPanel');

        this.detailsToolbar.getComponent('MySeparator').hide();
        this.downloadButton.hide();
        this.searchToolbar.hide();

        var description = this.record.get('Description'),
            title = this.record.get('Title'),
            count = this.record.get('Records Found'),
            publisher = this.record.get('Publisher'),
            baseUrl = this.record.get('invokeBaseUrl'),
            requestJson = Ext.decode(this.record.get('requestJson'));

        var c = Ext.create('Mvpc.view.GenericDetailsContainer', {
            controller: this.controller,
            record: this.record,
            fieldOverrides: { invokeBaseUrl: { autolink: false } }
        });
        this.detailsPanel.add(c);

        var titleLabel = Ext.create('Ext.form.Label', {
            html: '<br /><center><h1>' + title + '</h1></center><p />'
        })

        var fs = Ext.create('Ext.form.FieldSet', {
            title: 'Resource Information',
            items: [{
                xtype: 'label',
                html: description
            }, {
                //fieldLabel: 'Publisher',
                //value: publisher
                xtype: 'label',
                html: '<p /><br />Publisher: &nbsp; &nbsp; ' + publisher
            }, {
                //fieldLabel: 'Record Count',
                //value: count
                xtype: 'label',
                html: '<p />Record Count: &nbsp; &nbsp; ' + count
            }]
        });

        this.summaryContainer.add([
            titleLabel,
            fs
        ]);

        /*
        searchField = Ext.create('Mvp.util.SearchBox', {
            itemId: 'searchField',
            fieldLabel: 'Search This Resource',
            labelWidth: 125,
            emptyText: 'Enter object name or RA and Dec',
            width: 360
        });
        if (this.refineSearchAction !== '') {
            searchbar.add([searchField]);
        }
        searchField.on('searchInitiated', this.doSearch, this);
        */

        this.loadButton.addListener('click', this.loadRecords, this);
    },

    loadRecords: function () {
        var r = this.record;
        Mvp.search.Summary.invokeMashupQuery(this.controller, r.get('serviceId'), r.get('invokeBaseUrl'), r.get('requestJson'));
    },

    doSearch: function () {
        var url = this.record.get('accessURL'),
            searchText = this.query('#searchField')[0].value;
        var title = this.record.get('shortName') + ": " + searchText;
        var description = this.record.get('title') + ' searched at ' + searchText;

        if (url) {
            url = url.replace(/amp;/gi, '');

            if (!url.match(/(\?&)$/)) {
                if (url.match(/\?/)) {
                    url = url + '&';
                } else {
                    url = url + '?';
                }
            }
        }

        var args = {
            url: url, inputText: searchText,
            title: title, description: description
        };

        var searchParams = Mvp.search.SearchParams.getSearch(this.refineSearchAction);
        searchParams.downloadEnabled = true;
        Mvp.gui.DetailsWindow.closeDetailsWindow();
        this.controller.invokeSearch(args, searchParams);
    }
});