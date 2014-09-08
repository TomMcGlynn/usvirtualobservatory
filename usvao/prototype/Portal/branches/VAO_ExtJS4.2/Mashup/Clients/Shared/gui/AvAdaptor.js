// No longer used.  Should be deleted soon.

Ext.define('Mvp.gui.AvAdaptor', {
    requires: [
    ],


    constructor: function (config) {
        this.controller = config.controller;
        this.controller.on('storeupdated', this.updateView, this);
        delete config.controller;

        this.enabled = false;
        this.color = "0xff0000";    // default to red.
    },

    // Public methods

    updateView: function (updateObject) {
        var store = updateObject.store;
        if (store) {
            if (store !== this.lastStore) {
                // We only need to refresh anything if this is a different store than last time.
                this.updateAstroView(store);
            }
            this.lastStore = store;
        }
    },

    updateAstroView: function (inStore) {
        var store = inStore || this.lastStore;
        if (store && (store.hasPositions() || store.hasFootprints())) {
            this.deleteAll();
            if (this.enabled) {
                // Get an array of footprint or position objects (one for each row in the filtered store).
                var rows = [],
                    type;
                if (store.hasFootprints()) {
                    rows = store.getFootprints();
                    type = 'footprint';
                } else if (store.hasPositions()) {
                    rows = store.getPositions();
                    type = 'catalog';
                }
                if (rows.length > 0) {
                    var layer = {
                        "type": type,
                        "attribs": {
                            "color": this.color
                        },
                        "rows": rows
                    };
                    if (!window.AstroView) return;
                    // Send the footprint layer to AstroView.
                    Ext.log({ msg: 'Creating AV layer' });
                    this.astroViewLayerId = AstroView.createLayer(layer);
                }
            }
        }
    },

    setEnabled: function (enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            this.updateAstroView(this.lastStore);
        }
    },

    setColor: function (color) {
        color = '0x' + color;
        if (this.color != color) {
            this.color = color;
            this.updateAstroView(this.lastStore);
        }
    },

    moveTo: function (coneSearchParams) {
        if (!window.AstroView) return;
        var location = {
            ra: coneSearchParams.ra,
            dec: coneSearchParams.dec
        };
        AstroView.moveTo(location);
    },

    onBeforeClose: function (panel, eOpts) {
        this.enabled = false;
        this.deleteAll();
    },

    // Private methods

    deleteAll: function () {
        if (this.astroViewLayerId) {
            AstroView.deleteLayer(this.astroViewLayerId);
            this.astroViewLayerId = null;
        }
    }

});