Ext.define('Mvp.gui.IFrame', {
    extend: 'Ext.panel.Panel',

    initComponent: function () {
        this.callParent(arguments);
    },

    constructor: function (config) {
        var input = config.controller.searchInput.inputText;
        var splits = input.split(' ');
        var src = splits[0];
        this.newWindow = (splits.length > 1) && (splits[1] === 'window');
        var iFrame = Ext.create('Ext.ux.IFrame', {
            src: src,
            width: 600
        });
        Ext.apply(config, {
            title: config.controller.searchInput.title || src,
            layout: 'fit',
            width: 600,
            items: [iFrame]
        });
        
        if (this.newWindow) {
            Ext.apply(config, {
                preventHeader: true,
                closable: false
            });
        }
        this.callParent(arguments);
    },
    
    onLoad: function() {
        this.callParent(arguments);
        
        // This is a good place to do any customized initialization.
        
    }
})