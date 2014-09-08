
Ext.define('Mvp.custom.Hla', {
    statics: {
        hlaPreviewRendererOld: function (value, metaData, record, rowIndex, colIndex, store, view) {
            // Find a url for the preview image.
            var imageUrl = null;
            var linkUrl = null;
            var hla = Mvp.custom.Hla;
            var link = record.get('URL');
            linkUrl = hla.hlaPreviewUrl(link);
            imageUrl = hla.hlaPreviewUrl(link, 'thumb');

            // Construct the html to render.
            var html = ((record.get('Format') == 'image/jpeg') && imageUrl && imageUrl.match('fitscut.cgi')) ? '<img src="' + imageUrl + '" alt="' + imageUrl + '" height="120" width="120" />' :
                                    '<img src="../Shared/img/nopreview.png" alt="No Preview" height="120" width="120" />';

            return html;
        },

        hlaPreviewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var hla = Mvp.custom.Hla;
            
            var height = 120;
            var width = 120;
            var title = null;
            var previewUrl = hla.computePreviewUrl(record, 'thumb');
            if (!previewUrl) {
                previewUrl = '../Shared/img/nopreview.png';
                title = 'Preview Not Available';
            }

            // Construct the html to render.
            var html = hla.computePreviewHtml(record, title, 'thumb', height, width);

            return html;
        },

        // Returns an array containing a link to a small version of the image, and an
        // html snippet that makes an image of that small image at the given height and width that
        // is also a hyperlink to the full resolution of the image.
        simplePreviewGenerator: function (record, height, width) {
            // Find a url for the preview image.
            var imageUrl = null;
            var linkUrl = null;
            var hla = Mvp.custom.Hla;
            var link = record.get('URL');
            linkUrl = hla.hlaPreviewUrl(link);
            imageUrl = hla.hlaPreviewUrl(link, 'thumb');
            if (!height || height == 0) height = 120;
            if (!width || width == 0) width = 120;

            // Construct the html to render.
            var html = ((record.get('Format') == 'image/jpeg') && imageUrl && imageUrl.match('fitscut.cgi')) ? '<img src="' + imageUrl + '" alt="' + imageUrl + '" height="' + height + '" width="' + width + '" />' :
                                    '<img src="../Shared/img/nopreview.png" alt="No Preview" height="' + height + '" width="' + width + '" />';

            return [linkUrl, html];

        },
        
        computePreviewHtml: function(record, title, imageSize, displayHeight, displayWidth) {
            var util = Mvp.util.Util;
            var hla = Mvp.custom.Hla;

            var previewUrl = hla.computePreviewUrl(record, imageSize);
            if (!previewUrl) {
                previewUrl = '../Shared/img/nopreview.png';
                title = 'Preview Not Available';
            }

            // Construct the html to render.
            var html = util.createImageHtml(previewUrl, title, displayHeight, displayWidth);
            return html;
        },
        
        computePreviewUrl: function(record, imageSize) {
            var previewUrl = null;
            if (imageSize === undefined) imageSize = 'full';
            var dataUrl = record.get('URL');
            if (dataUrl.match('fitscut.cgi')) {
                // The data access is already a fitscut.  We just need to modify
                // that to make sure that it's a jpeg.
                if (dataUrl.match(/format=[a-z]+/)) {
                    previewUrl = dataUrl.replace(/format=[a-z]+/, 'format=jpeg');
                } else {
                    previewUrl = dataUrl + '&format=jpeg';
                }
            
            } else {
                // We need to create a fitscut from the record's dataset name.
                // http://hla.stsci.edu/cgi-bin/fitscut.cgi?red=[DATASET]&size=ALL&format=jpeg&asinh=1&autoscale=99.50&qext=4
                var datasetName = record.get('Dataset');
                if (datasetName) {
                    previewUrl = 'http://hla.stsci.edu/cgi-bin/fitscut.cgi?red=' +
                        datasetName +
                        '&size=ALL&format=jpeg&asinh=1&autoscale=99.50&qext=4';
                }
            }
            
            // If we got a value for the URL, add the outputSize parameter.
            if (previewUrl) {
                previewUrl += Mvp.custom.Hla.hlaSizeMap[imageSize];
            } 
            return previewUrl;
        },

        /////////////////////////////////////////////////////////////
        // Functions for getting preview URLs.  Valid sizes are:
        //  full - (3840x3840 for GALEX)
        //  large - (1920x1920 for GALEX)
        //  medium - (960x960 for GALEX)
        //  small - (480x480 for GALEX)
        //  thumb - (120x120 for GALEX)

        hlaSizeMap: {
            full: '',
            large: '&output_size=1024&qext=4',
            medium: '&output_size=512',  // 512 and 256 are precomputed on the server
            small: '&output_size=256',
            thumb: '&output_size=256'
        },

        hlaPreviewUrl: function (url, size) {
            if (size == undefined) return url;
            return url + Mvp.custom.Hla.hlaSizeMap[size];
        },

        dataviewTemplate: function () {
            var tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap">',
                        '<div class="thumb">',
                        '{[this.dvRenderer(256, 256, values.URL)]}',
                            '<div>',
                            '<font size="+2">{target_name}</font><br>',
                            '<font size="+1">{instrument}</font><br>',
                            '<font size="+0">{obs_id}</font><br>',
                            '<font size="+0">{[this.displayLink(values.URL)]}</font><br>',
                            '</div>',
                        '</div>',
                    '</div>',
                '</tpl>',
                {

                    displayLink: function (jpegURL) {

                        // Find a url for displaying image.
                        var linkUrl = null;
                        var hla = Mvp.custom.Hla;
                        linkUrl = hla.hlaPreviewUrl(jpegURL, 'large');
                        var html = linkUrl ? Mvp.util.Util.createLink(linkUrl, 'Interactive Display') : "";

                        return html;

                    },

                    dvRenderer: function (height, width, jpegURL) {

                        // Find a url for the preview image.
                        var imageUrl = null;
                        var linkUrl = null;
                        var hla = Mvp.custom.Hla;
                        linkUrl = hla.hlaPreviewUrl(jpegURL);
                        imageUrl = hla.hlaPreviewUrl(jpegURL, 'small');

                        // Construct the html to render.
                        if (!height || height == 0) height = 120;
                        if (!width || width == 0) width = 120;
                        var html = (imageUrl) ? '<img src="' + imageUrl + '" alt="' + imageUrl + '" height="' + height + '" width="' + width + '" />' :
                                    '<img src="../Shared/img/nopreview.png" alt="No Preview" height="' + height + '" width="' + width + '" />';

                        return html;
                    }
                }
            );

            return tpl;
        }
    }
})