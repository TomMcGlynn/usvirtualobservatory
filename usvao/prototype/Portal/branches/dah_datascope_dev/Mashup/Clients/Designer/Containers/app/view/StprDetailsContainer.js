Ext.define('Mvpc.view.StprDetailsContainer', {
    requires: ['Mvp.util.Util', 'Mvp.custom.Caom', 'Mvp.custom.Hst'],
    extend: 'Mvpc.view.ui.CaomDetailsContainer',

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
            downloadPageLink = Mvp.util.Util.createLinkIf(downloadPage, 'Downloads Page'),
            description = record.get('descriptions'),
            relatedResources = record.get('relatedresources'),
            relatedResourcesLink = Mvp.util.Util.createLinkIf(relatedResources, 'Related Resources');

        /* Unlike CAOM images, the press release images might not be square. Use a JS Image object to determine
        the image's height, then scale the height to 256 and the width accordingly. It won't always fit lengthwise
        in its allocated space in the container, but that's fine - height is what actually matters since the height
        of imageContainer is fixed.*/
        var img = new Image();
        img.src = Mvp.custom.Hst.stprPreviewUrl(url, 'medium');
        var height = img.height,
            ratio = 256 / height;
        var imageLink = Mvp.util.Util.createImageLink(url, img.src, 'Full Size Image - May Be Extremely Large', img.width * ratio, 256),
            titleHtml = '<center><h1>' + title + '</h1></center>',
            summaryPanel = this.getComponent('summaryPanel'),
            detailsPanel = this.getComponent('detailsPanel');
        var titleContainer = summaryPanel.getComponent('titleContainer'),
            imageContainer = summaryPanel.getComponent('imageContainer'),
            summaryContainer = summaryPanel.getComponent('summaryContainer');
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
            subjectsLabel = {
                xtype: 'label',
                itemId: 'subjectsLabel',
                width: 300,
                html: 'Subjects: ' + subjects
            },
            descriptionLabel = {
                xtype: 'label',
                itemId: 'descriptionLabel',
                html: '<br />' + description + '<br />&nbsp; <br />',
                width: 350
            },
            obsIdLabel = {
                xtype: 'label',
                itemId: 'releaseLabel',
                width: 300,
                html: 'Release ID: ' + releaseId
            },
            creditLabel = {
                xtype: 'label',
                itemId: 'creditLabel',
                width: 300,
                html: 'Credit: ' + credit
            },
            urlLabel = {
                xtype: 'label',
                itemId: 'urlLabel',
                width: 300,
                html: '<br />' + downloadPageLink
            },
            relatedLabel = {
                xtype: 'label',
                itemId: 'relatedLabel',
                width: 300,
                html: relatedResourcesLink
            };

        titleContainer.add(titleLabel);
        imageContainer.add(imageLabel);
        var items = [/*subjectsLabel, obsIdLabel,*/creditLabel, descriptionLabel, urlLabel, relatedLabel];
        summaryContainer.add(items);
        detailsPanel.add(Ext.create('Mvpc.view.GenericDetailsContainer', { record: record, controller: config.controller }));
    }
});