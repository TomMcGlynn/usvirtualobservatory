Ext.define('Mvpc.view.HlaDetailsContainer', {
    requires: ['Mvp.util.Util', 'Mvp.custom.Hla'],
    extend: 'Mvpc.view.SiaDetailsContainer',

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    constructor: function (config) {
        this.callParent(arguments);    //set up basic Container class variables

        var record = config.record;
        var target = record.get('Target'),
            source = record.get('Source'),
            propId = record.get('PropID'),
            proposal_pi = record.get('PI_Name').trim(),
            detector = record.get('Detector'),
            aperture = record.get('Aperture'),
            specel = record.get('Spectral_Elt');
            fullJpg = Mvp.custom.Hla.computePreviewUrl(record, 'full');
        var imageLink = Mvp.custom.Hla.computePreviewHtml(record, null, 'small', 256, 256);
        var targetHTML = '<center><h1>' + target + '</h1></center>',
            summaryPanel = this.getComponent('summaryPanel'),
            detailsPanel = this.getComponent('detailsPanel');
        var titleContainer = summaryPanel.getComponent('titleContainer'),
            imageContainer = summaryPanel.getComponent('imageContainer'),
            summaryContainer = summaryPanel.getComponent('summaryContainer');
            
        if (source === 'HLSP') {
            source = 'High-Level Science Product';
        } else if (source === 'HLA') {
            source = 'Hubble Legacy Archive';
        } else if (source === 'CADC') {
            source = 'Canadian Astronomy Data Centre';
        } else if (source === 'DADS') {
            source = 'Hubble Legacy Archive';
        }
        var targetLabel = {
            xtype: 'label',
            itemId: 'targetLabel',
            html: targetHTML
        },
            imageLabel = {
                xtype: 'label',
                itemId: 'imageLabel',
                html: '<center>' + imageLink + '</center>',
                width: (imageLink.match(/<img/)) ? null : 300,
                margins: '10 10 10 10'
            },
            hlaDetails = {
                xtype: 'fieldset',
                itemId: 'hlaDetails',
                style: '{font-size: \'16px\'}',
                title: 'Hubble Legacy Archive Details',
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'detectorLabel',
                        fieldLabel: 'Detector/Aperture',
                        value: detector + ((aperture && (aperture.trim() !== '')) ? ' / ' + aperture : ''),
                        labelWidth: 150,
                        anchor: '100%'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'specelLabel',
                        fieldLabel: 'Spectral Element(s)',
                        value: specel,
                        labelWidth: 150,
                        anchor: '100%'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'targetLabel',
                        fieldLabel: 'Target Name',
                        value: target,
                        labelWidth: 150,
                        anchor: '100%'
                    },                    {
                        xtype: 'displayfield',
                        itemId: 'sourceLabel',
                        fieldLabel: 'Product Source',
                        value: source,
                        labelWidth: 150,
                        anchor: '100%'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'jpegLabel',
                        fieldLabel: 'Preview Image',
                        value: Mvp.util.Util.createLink(fullJpg, 'Full Resolution JPEG'),
                        labelWidth: 150,
                        anchor: '100%'
                    }
                ]
            };
        
        var fovDetails = summaryContainer.getComponent('fieldOfView');
        fovDetails.hide();
        summaryContainer.insert(2, hlaDetails);
        summaryContainer.insert(0, imageLabel);
    }
});