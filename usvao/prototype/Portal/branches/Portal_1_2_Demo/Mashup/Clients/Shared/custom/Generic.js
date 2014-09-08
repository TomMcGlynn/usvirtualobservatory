Ext.define('Mvp.custom.Generic', {
    statics: {
        spaceColumnRenderer: function (value) {
            var html = value;
            html = value.trim().replace(/\s+/g, ', ');
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
        }
    }

})