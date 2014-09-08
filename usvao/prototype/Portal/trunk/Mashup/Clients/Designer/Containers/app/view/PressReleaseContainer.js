Ext.define('Mvpc.view.PressReleaseContainer', {
    requires: ['Mvp.util.Util', 'Mvp.custom.Caom', 'Mvp.custom.Hst'],
    extend: 'Mvpc.view.ui.CaomDetailsContainer',

    statics: {
        openLink: function (btn, evt, config) {
            window.open(config.url, '_blank');
        }
    },

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
            releaseLink = Mvp.util.Util.createLinkIf(record.get('UnnamedField-19'), 'Press Release Page');

        /* Unlike CAOM images, the press release images aren't always square. Use a JS Image object to determine
        the image's height, then scale the height to 256 and the width accordingly. It won't always fit lengthwise
        in its allocated space in the container, but that's fine - height is what actually matters since the height
        of imageContainer is fixed.*/
        var img = new Image();
        img.src = Mvp.custom.Hst.pressPreviewUrl(url, 'medium');
        var height = img.height,
            ratio = 256 / height,
            imageLink = Mvp.util.Util.createImageLink(url, img.src, 'Full Size TIFF Image - May Be Extremely Large', img.width * ratio, 256),
            targetHTML = '<center><h1>' + target + '</h1></center>',
            summaryPanel = this.getComponent('summaryPanel'),
            detailsPanel = this.getComponent('detailsPanel'),
            titleContainer = summaryPanel.getComponent('titleContainer'),
            imageContainer = summaryPanel.getComponent('imageContainer'),
            summaryContainer = summaryPanel.getComponent('summaryContainer'),
            detailsToolbar = summaryPanel.getComponent('detailsToolbar'),
            downloadButton = detailsToolbar.getComponent('downloadButton'),
            addToBasketButton = detailsToolbar.getComponent('addToBasketButton'),
            interactiveDisplayButton = detailsToolbar.getComponent('interactiveDisplayButton'),
            searchVoButton = detailsToolbar.getComponent('searchVoButton'),
            moreButton = detailsToolbar.getComponent('moreButton');

        addToBasketButton.hide();
        detailsToolbar.getComponent('separator2').hide();
        downloadButton.on('click', this.download, this, { url: url });
        downloadButton.setTooltip('Download Image');
        interactiveDisplayButton.on('click', Mvpc.view.PressReleaseContainer.openLink, this, { url: downloadPage });
        interactiveDisplayButton.setTooltip('Go to Downloads Page');
        moreButton.on('click', Mvpc.view.PressReleaseContainer.openLink, this, { url: record.get('UnnamedField-19') });
        searchVoButton.on('click', this.voSearch, this, { ra: record.get('UnnamedField-1'), dec: record.get('UnnamedField-2') });

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
            };

        titleContainer.add(targetLabel);
        imageContainer.add(imageLabel);
        var items = [collectionLabel, obsIdLabel];
        if (authors.trim() && authors.toLowerCase() != 'unknown') items.push(authorsLabel);
        summaryContainer.add(items);
        detailsPanel.add(Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, controller: config.controller }));
    },

    download: function(button, evt, config) {
        Ext.core.DomHelper.append(document.body, {
            tag: 'iframe',
            frameBorder: 0,
            width: 0,
            height: 0,
            css: 'display:none;visibility:hidden;height:1px;',
            src: config.url
        });
    },

    voSearch: function (button, evt, config) {
        Mvp.custom.FullSearch.voSearch(config.ra, config.dec, this.controller);
    }
});