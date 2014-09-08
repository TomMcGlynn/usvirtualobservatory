Ext.define('Mvp.custom.GalexTiles', {
    statics: {
        urlGenerator: function (value, record) {
            var num = record.get('tilenum'),
                nSpectra = record.get('nSpectra');
            var url = 'http://galex.stsci.edu/GR6/?page=downloadlist&tilenum=' + num + '&type=coadd' + (nSpectra > 0 ? 's' : 'i');
            var html = '<a target="_blank" href="' + url + '">' + value + '</a>';
            return { html: html, url: url };
        },

        urlRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var retVal = Mvp.custom.GalexTiles.urlGenerator(value, record);
            return retVal.html;
        },

        previewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            // Find a url for the preview image.
            var linkUrl = 'http://galex.stsci.edu/data/' + record.get('fullResColorJpeg'),
                smallUrl = linkUrl.replace('.jpg', '_small.jpg'),
                title = record.get('tilename');
            // Construct the html to render.
            imgSrc = (smallUrl) ? '<img src="' + smallUrl + '" alt="' + title + '" title="' + title + '" height="120" width="120" />' :
                             '<img src="../Shared/img/nopreview.png" title="No Preview" alt="No Preview" height="120" width="120" />';
            var html = '<a target="_blank" href="' + linkUrl + '">' + imgSrc + '</a>';

            return html;
        }
    }
});