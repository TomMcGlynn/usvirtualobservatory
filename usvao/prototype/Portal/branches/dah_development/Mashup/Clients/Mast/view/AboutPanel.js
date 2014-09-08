
Ext.define('Mast.view.AboutPanel', {
    extend: 'Ext.container.Container',
    requires: [
        'Ext.window.Window'
    ],
    statics: {
        show: function (versionString) {
            var aboutPanel = Ext.create('Mast.view.AboutPanel', {
                versionString: versionString
            });
            var aboutWindow = Ext.create('Ext.window.Window', {
                layout: 'fit',
                width: 300,
                height: 250,
                modal: true,
                constrainHeader: true,
                items: [aboutPanel]
            });
            aboutWindow.show();
        }

    },
    
    constructor: function(config) {
        var title = 'MAST Portal';
        
        var html = '<div style="text-align: center; margin: 0px;">' +
            '<h3 style="color: black;font-size: 20px;">' + title + '</h3>' +
            (config.versionString ? ('Version ' + config.versionString + '</div>') : '') + /*
            '<p /><br />This tool is currently in development. ' +
            'It is generally stable, but all components are to be considered as works in progress.' +*/
            '<p /><br />Please contact the <a href="mailto:archive@stsci.edu?subject=Portal Feedback">MAST Portal Team</a>' +
            ' with any feedback, comments or suggestions. Please note: the above link may cause you to leave the current page if your browser' +
            'does not have a default email client setup! In this case, please send email to archive[at]stsci.edu with the subject "Portal Feedback".';
        config.html = html;
        config.margin = '10 10 10 10';
        this.callParent(arguments);
    }
})