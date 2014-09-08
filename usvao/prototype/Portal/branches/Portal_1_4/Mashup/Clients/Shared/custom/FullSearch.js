
Ext.define('Mvp.custom.FullSearch', {
    requires: ['Mvp.util.Constants'],
    statics: {
        // Some Full Search result columns have '#' separators.  If we display that column, we want to remove those.
        hashColumnRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = value;
            if (value.length > 2) {
                html = value.substr(1, value.length - 2).replace(/#/g, ',');
            }
            //var valueArray = value.split('#');
            //var html = '';
            //var i;
            //for (i=1; i<valueArray.length-1; i++) {
            //    html += (valueArray[i] + ((i < valueArray.length -2) ? ',' : '')) ;
            //}
            return html;
        },

        // Some Full Search result columns have '|' separators.  If we display that column, we want to remove those.
        pipeColumnRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = value;
            if (value.length > 1) {
                html = value.replace(/\|/g, ', ');
            }
            return html;
        },

        categoryIconRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html;
            switch (value) {
                case 'ConeSearch': html = '<center><img title="A catalog of objects" alt="A catalog of objects" src="' + Mvp.util.Constants.GENERIC_ICON + '" /></center>'; break;
                case 'SimpleImageAccess': html = '<center><img title= "A collection of images" alt= "A collection of images" src="' + Mvp.util.Constants.IMAGE_ICON + '" /></center>'; break;
                case 'SimpleSpectralAccess': html = '<center><img title="A collection of spectra" alt="A collection of spectra" src="' + Mvp.util.Constants.SPECTRA_ICON + '" /></center>'; break;
                default: html = '';
            }
            return html;
        }
    }
        
})