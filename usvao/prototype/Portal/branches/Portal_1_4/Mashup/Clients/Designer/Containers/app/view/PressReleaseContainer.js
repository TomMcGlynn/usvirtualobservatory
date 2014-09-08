Ext.define('Mvpc.view.PressReleaseContainer', {
    requires: ['Mvp.util.Util', 'Mvp.custom.Caom', 'Mvp.custom.Hst'],
    extend: 'Mvpc.view.ui.CaomDetailsContainer',

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    constructor: function (config) {
        this.callParent(arguments);    //set up basic Container class variables

        var record = config.record;
        var collection = record.get('UnnamedField-20'),
            target = record.get('UnnamedField'),
            releaseId = record.get('UnnamedField-16'),
            authors = record.get('UnnamedField-17'),
            url = record.get('UnnamedField-14'),
            downloadPage = record.get('UnnamedField-18'),
            downloadPageLink = Mvp.util.Util.createLinkIf(downloadPage, 'Downloads Page'),
            releaseLink = Mvp.util.Util.createLinkIf(record.get('UnnamedField-19'), 'Press Release Page');

        /* Unlike CAOM images, the press release images aren't always square. Use a JS Image object to determine
        the image's height, then scale the height to 256 and the width accordingly. It won't always fit lengthwise
        in its allocated space in the container, but that's fine - height is what actually matters since the height
        of imageContainer is fixed.*/
        var img = new Image();
        img.src = Mvp.custom.Hst.pressPreviewUrl(url, 'medium');
        var height = img.height,
            ratio = 256 / height;
        var imageLink = Mvp.util.Util.createImageLink(url, img.src, 'Full Size TIFF Image - May Be Extremely Large', img.width * ratio, 256),
            targetHTML = '<center><h1>' + target + '</h1></center>',
            summaryPanel = this.getComponent('summaryPanel'),
            detailsPanel = this.getComponent('detailsPanel'),
            hyperlink2 = null, url2;
        var titleContainer = summaryPanel.getComponent('titleContainer'),
            imageContainer = summaryPanel.getComponent('imageContainer'),
            summaryContainer = summaryPanel.getComponent('summaryContainer');
        var targetLabel = {
            xtype: 'label',
            itemId: 'targetLabel',
            html: targetHTML
        },
            imageLabel = {
                xtype: 'label',
                itemId: 'imageLabel',
                html: '<center>' + imageLink + '</center>',
                width: (imageLink.match(/<img/)) ? null : 300
            },
            collectionLabel = {
                xtype: 'label',
                itemId: 'collectionLabel',
                text: 'Collection: ' + collection
            },
            obsIdLabel = {
                xtype: 'label',
                itemId: 'releaseLabel',
                html: 'Release ID: ' + releaseId + '<br /> &nbsp;<br />'
            },
            authorsLabel = {
                xtype: 'label',
                itemId: 'authorsLabel',
                text: 'Authors: ' + authors
            },
        /*
        adsSearch = {
        xtype: 'button',
        itemId: 'adsButton',
        text: 'Search for Publications',
        tooltip: 'Search for Publications authored by this proposal PI&nbsp;',
        scope: this.portal,
        shadow: true,
        style: 'border: 1px solid #000000;', // The 'border' config has no effect, overriding the toolbar button style is a pain
        handler: function (button, event) {
        Ext.log('ADS Search invoked for PI: ' + proposal_pi);
        this.searchAdsAuthor(proposal_pi);
        }

        },*/
            urlLabel = {
                xtype: 'label',
                itemId: 'urlLabel',
                html: downloadPageLink
            },
            moreLabel = {
                xtype: 'label',
                itemId: 'moreLabel',
                html: releaseLink
            };

        titleContainer.add(targetLabel);
        imageContainer.add(imageLabel);
        var items = [collectionLabel, obsIdLabel];
        if (authors.trim() && authors.toLowerCase() != 'unknown') items.push(authorsLabel);
        items.push(urlLabel, moreLabel);
        summaryContainer.add(items);
        detailsPanel.add(Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, controller: config.controller }));
    }
});