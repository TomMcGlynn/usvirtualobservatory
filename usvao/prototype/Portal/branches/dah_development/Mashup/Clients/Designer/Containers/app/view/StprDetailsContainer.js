Ext.define('Mvpc.view.StprDetailsContainer', {
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
        var subjects = record.get('subjects'),
            title = record.get('title'),
            releaseId = record.get('resourceid'),
            credit = record.get('credit'),
            url = record.get('resourceurl'),
            downloadPage = record.get('referenceurl'),
            description = record.get('descriptions'),
            relatedResources = record.get('relatedresources');

        /* Unlike CAOM images, the press release images might not be square. Use a JS Image object to determine
        the image's height, then scale the height to 256 and the width accordingly. It won't always fit lengthwise
        in its allocated space in the container, but that's fine - height is what actually matters since the height
        of imageContainer is fixed.*/
        var img = new Image();
        img.src = Mvp.custom.Hst.stprPreviewUrl(url, 'medium');
        var height = img.height,
            ratio = 256 / height,
            imageLink = Mvp.util.Util.createImageLink(url, img.src, 'Full Size Image - May Be Extremely Large', img.width * ratio, 256),
            titleHtml = '<center><h1>' + title + '</h1></center>',
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

        downloadButton.hide();
        detailsToolbar.getComponent('separator1').hide();
        addToBasketButton.hide();
        detailsToolbar.getComponent('separator2').hide();
        interactiveDisplayButton.on('click', Mvpc.view.StprDetailsContainer.openLink, this, { url: downloadPage });
        interactiveDisplayButton.setTooltip('Go to Downloads Page');
        moreButton.on('click', Mvpc.view.StprDetailsContainer.openLink, this, { url: relatedResources });
        searchVoButton.on('click', this.voSearch, this, { ra: record.get('ra_j2000'), dec: record.get('dec_j2000')});

        if (description.length > 300) summaryContainer.setHeight(300);
        var titleLabel = {
                xtype: 'label',
                itemId: 'targetLabel',
                html: titleHtml,
                width: 350
            },
            imageLabel = {
                xtype: 'label',
                itemId: 'imageLabel',
                html: '<center>' + imageLink + '</center>',
                width: (imageLink.match(/<img/)) ? null : 300
            },
            descriptionLabel = {
                xtype: 'label',
                itemId: 'descriptionLabel',
                html: '<br />' + description + '<br />&nbsp; <br />',
                width: 350
            },
            creditLabel = {
                xtype: 'label',
                itemId: 'creditLabel',
                width: 300,
                html: 'Credit: ' + credit
            };

        titleContainer.add(titleLabel);
        imageContainer.add(imageLabel);
        summaryContainer.add([creditLabel, descriptionLabel]);
        detailsPanel.add(Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, controller: config.controller }));
    },

    voSearch: function (button, evt, config) {
        Mvp.custom.FullSearch.voSearch(config.ra, config.dec, this.controller);
    }
});