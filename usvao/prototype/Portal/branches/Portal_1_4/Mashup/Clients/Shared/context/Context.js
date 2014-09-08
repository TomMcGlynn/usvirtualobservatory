Ext.define('Mvp.context.Context', {
    extend: 'Ext.util.Observable',

    statics: {
        uidCounter: 0
    },

    constructor: function (config) {
        this.callParent(arguments);
        this.useAv = config.useAv;
        this.avMode = 'NONE';   // Default for what rows to display in astroview

        this.addEvents('storeupdated',
                       'contextupdated',
                       'APP.context.avmode.changed',
                       'APP.context.color.changed');
        var cl = Mvp.context.Context;
        this.uid = cl.uidCounter++;
    },

    cancel: function () {

    },

    activate: function () {

    },

    deactivate: function () {

    },

    // avMode can be one of 'NONE', 'ALL' or 'SELECTED'
    setAvMode: function (avMode) {
        if (avMode !== this.avMode) {
            this.avMode = avMode;
            this.fireEvent('APP.context.avmode.changed', {
                type: 'APP.context.avmode.changed',
                context: this,
                avMode: avMode
            });
        }
    },

    getAvMode: function () {
        return this.avMode;
    },

    setColor: function (color) {
        if (this.color !== color) {
            this.color = color;
            this.fireEvent('APP.context.color.changed', {
                type: 'APP.context.color.changed',
                context: this,
                color: color
            });
        }
    },

    getColor: function () {
        return this.color;
    },

    getUid: function () {
        return this.uid;
    }
});