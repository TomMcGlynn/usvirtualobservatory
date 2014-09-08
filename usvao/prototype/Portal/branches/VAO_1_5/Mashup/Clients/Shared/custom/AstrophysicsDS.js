Ext.define('Mvp.custom.AstrophysicsDS', {
    statics: {
        adsCreatorRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var text = value.replace(/##/g, ' ');
            var comma = false,
                i = value.length - 1;
            for (; i > 0; i--) {
                if ((text.charAt(i) == ' ') && comma) {
                    var front = text.substring(0, i),
                        end = text.substring(i + 1)
                    text = front.concat('; ').concat(end);
                    comma = false;
                }
                if (text.charAt(i) == ',') comma = true;
            }
            return text;
        }
    }
});