Ext.define('PublishingWizard.CardManager', {
    statics: {
        /**
        *
        * The options argument should be a JavaScript object with the following properties:
        *    title(string): The title to give the resulting grid panel
        *    
        *    app(object): The calling application object.  That application is expected to have the following:
        *        onError(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has an error
        *        onFailure(function(responseObject, requestOptions, queryScope)): A callback that will be called if the query has a failure
        *        tabContainer(tabpanel): A tab panel to which the new grid will be added
        *
        */
        activate: function (options) {
            var load = Ext.create('PublishingWizard.CardManager', {
                title: options.title,
                app: options.app
            });
            load.load();
        }
    },

    constructor: function (config) {
        Ext.apply(this, config);
    },

    load: function () {
        this.firstData = true;
        var c = this.app.tabContainer;


        this.genPanel = Ext.create('Ext.panel.Panel', {
            title: "General Information",
            closable: false,
            layout: 'fit',
            id: 'genPanel',
            width: this.centerWidth,
            height: this.centerHeight
        });

        c.add(this.genPanel);
        c.setActiveTab(this.genPanel);
        this.myTab = this.genPanel.tab;
        this.myTab.setIcon("loading1.gif");

        this.curationPanel = Ext.create('Ext.panel.Panel', {
            title: "Curation Information",
            closable: false,
            layout: 'fit',
            id: 'curationPanel',
            width: this.centerWidth,
            height: this.centerHeight
        });

        c.add(this.curationPanel);
        this.myTab = this.curationPanel.tab;
        this.myTab.setIcon("loading1.gif");

        this.app.tabContainer.setActiveTab(this.genPanel);

        var options = {
            title: "xmlForm",
            file: 'EmptyVOResource.xml',
            app: this
        };
        PublishingWizard.XmlForm.activate(options);
    },


    onResponse: function (responseObject, requestOptions, queryScope, complete, updated) {
        Ext.log('CardManager.onResponse: firstData = ' + this.firstData + ", complete = " + complete + ", updated = " + updated);
        if (updated || complete) {
            this.queryScope = queryScope;
            this.complete = complete;
            if (this.firstData) {
                this.genPanel.removeDocked(this.initialDocked[0]);
                this.updateDisplay();
                this.firstData = false;
            } else {
                this.updateStatusText();
            }

            if (this.complete) {
                this.myTab.setIcon("");
            }
        }
    },

    onError: function (responseObject, requestOptions, queryScope, complete) {
        Ext.log('CardManager.onError() called');
        this.myTab.setIcon("");
        var errMsg = new Ext.form.field.Display({
            value: 'The server encountered an error loading this data.'
        })
        this.genPanel.add(errMsg);

        this.updateStatusText('<b>Error Loading Data</b>');
    },

    onFailure: function (responseObject, requestOptions, queryScope) {
        Ext.log('CardManager.onFailure() called');
        this.myTab.setIcon("");
        var errMsg = new Ext.form.field.Display({
            value: 'The server failed to respond.'
        })
        this.genPanel.add(errMsg);

        this.updateStatusText();
    },

    updateDisplay: function () {
        var me = this;

        this.app.tabContainer.setActiveTab(this.genPanel);
        this.genPanel.add(this.grid);

        // This is a crumby way to get allow us to redraw the facets for this grid when this grid's tab has been selected.
        this.app.tabContainer.tabSelected = function (args) {
            var test = args;
        };

    },


    updateStatusText: function (overrideText) {
        if (this.statusLabel) {
            this.statusLabel.setText(overrideText);
        }
    }

});