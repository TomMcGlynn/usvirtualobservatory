Ext.define('Mvp.custom.Generic', {
    statics: {
        spaceColumnRenderer: function (value) {
            var html = value;
            html = value.trim().replace(/\s+/g, ', ');
            return html;
        },

        semicolonRenderer: function (value) {
            var html = value;
            html = value.trim().replace(';', ', ');
            return html;
        },

        titleRenderer: function (strSentence) {
            return strSentence.replace(/(\s|^|\.)([a-z]|[A-Z])/g, convertToUpper);

            function convertToUpper() {
                return arguments[0].toUpperCase();
            }
        },

        urlRenderer: function (url) {
            var html = '<a href="' + url.trim() + '">' + url.trim() + '</a>';
            return html;
        },

        dateRenderer: function (value) {
            if (!value) return '';
            var date = new Date(value);
            return Ext.Date.format(date, 'Y-m-d');
        },

        dateReader: function (value) {
            if (!value) return 0;
            var date = new Date(value);
            return (date.valueOf());
        },

        mjdRenderer: function (value) {
            if (!value) return '';
            var temp = value * 86400000 - 3506716800000;
            var date = new Date(temp);
            return Ext.Date.format(date, 'Y-m-d');
        },

        mjdReader: function (value) {
            if (!value) return 0;
            var date = new Date(value);
            var milli = date.valueOf();
            var temp = (milli + 3506716800000) / 86400000;
            return (temp);
        },

        unixSecondsRenderer: function (value) {
            return Mvp.custom.Generic.dateRenderer(value * 1000);
        },

        unixSecondsReader: function (value) {
            return Mvp.custom.Generic.dateReader(value) / 1000;
        },

        filesizeConverter: function (value, toUnits, fromUnits) {
            var units = {
                'bytes': 0,
                'B': 0,
                'KB': -1,
                'MB': -2,
                'GB': -3,
                'TB': -4,
            };
            var factor = fromUnits ? (units[toUnits] - units[fromUnits]) : units[toUnits];
            return value * Math.pow(1024, factor);
        },

        fixAnchorTags: function (value, metaData, record, rowIndex, colIndex, store, view) {
            if (typeof value !== 'string') return value;
            var s = value.replace('<a href=', '<a target="_blank" href=');
            return s;
        },
        
        gridWhitespace: function (value, metaData, record, rowIndex, colIndex, store, view) {
            metaData.style = 'white-space: normal;'
        }
    }

})