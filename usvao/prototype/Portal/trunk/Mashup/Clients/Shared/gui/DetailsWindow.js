Ext.define('Mvp.gui.DetailsWindow', {
    extend: 'Ext.window.Window',

    statics: {
        singleWindow: null,

        getDetailsWindow: function () {
            var c = Mvp.gui.DetailsWindow;
            if (c.singleWindow === null) {
                c.singleWindow = Ext.create('Mvp.gui.DetailsWindow', {
                    layout: 'fit',
                    width: 465,
                    minWidth: 400,
                    height: 600,
                    minHeight: 450,
                    closeAction: 'hide',
                    constrainHeader: true
                });
            }
            return c.singleWindow;
        },

        showDetailsWindow: function (config) {
            var c = Mvp.gui.DetailsWindow;
            var w = c.getDetailsWindow();
            w.removeAll();
            w.add(config.content);
            w.setTitle(config.title);
            w.setIcon(config.icon);
            w.show();
        },

        closeDetailsWindow: function () {
            var c = Mvp.gui.DetailsWindow;
            var w = c.getDetailsWindow();
            w.close();
        }
    },

    constructor: function (config) {
        this.callParent(arguments);
        this.addListener('resize', this.resizeFix, this);
    },

    resizeFix: function () {
        var size = this.getSize();
        this.suspendEvents();
        this.setSize(size.width + 10, size.height + 10);
        this.setSize(size.width, size.height);
        this.resumeEvents();
    }
})