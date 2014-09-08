
Ext.define('Mvp.custom.NedImages', {
    statics: {

        nedPreviewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var ned = Mvp.custom.NedImages;
            
            var height = 120;
            var width = 120;
            var title = record.get('sia_url') || '';

            // Construct the html to render.
            var columns = store.columnInfo.columns;
            var html = ned.computePreviewHtml(record, title, 'thumb', height, width, columns);

            return html;
        },
        
        computePreviewHtml: function(record, title, imageSize, displayHeight, displayWidth, columns) {
            var util = Mvp.util.Util;
            var ned = Mvp.custom.NedImages;

            var previewUrl = ned.computePreviewUrl(record, imageSize, columns);
            if (!previewUrl) {
                previewUrl = '../Shared/img/nopreview.png';
                title = 'Preview Not Available';
            }

            // Construct the html to render.
            var html = util.createImageHtml(previewUrl, title, displayHeight, displayWidth);
            return html;
        },
        
        computePreviewUrl: function(record, imageSize, columns) {
            var urlColumn = Mvp.util.TableUtils.getColumnNameByUCD(columns, /VOX\:Image_AccessReference/);
            var url = record.get(urlColumn);
            var previewUrl = url.replace(/fits\.gz$/, 'gif');

            return previewUrl;
        }
    }
})