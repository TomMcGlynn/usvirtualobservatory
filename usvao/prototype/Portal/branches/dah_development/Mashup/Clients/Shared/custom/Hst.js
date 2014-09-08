
Ext.define('Mvp.custom.Hst', {
    statics: {
        pressPreviewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            metaData.css = 'icon-align';    // class to vertically align the cell
            // Find a url for the preview image.
            var thumbUrl = null;
            var hst = Mvp.custom.Hst;

            var tifUrl = record.get('UnnamedField-14');
            var optionsUrl = record.get('UnnamedField-18');

            // Construct the html to render.
            var html = 'No Preview';
            if (tifUrl && optionsUrl) {
                thumbUrl = hst.pressPreviewUrl(tifUrl, 'thumb');
                html = Mvp.util.Util.createImageLink(optionsUrl, thumbUrl, 'Click for image download options', 120, 120, record);
                //html = '<img src="' + thumbUrl + '" alt="' + thumbUrl + '" height="120" width="120" />';
            }

            return html;
        },

        stprPreviewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            metaData.css = 'icon-align';    // class to vertically align the cell
            // Find a url for the preview image.
            var thumbUrl = null;
            var hst = Mvp.custom.Hst;

            var url = record.get('resourceurl');
            var optionsUrl = record.get('referenceurl');

            // Construct the html to render.
            var html = 'No Preview';
            if (url && optionsUrl) {
                thumbUrl = hst.stprPreviewUrl(url, 'thumb'),
                title = record.get('title');
                //html = Mvp.util.Util.createImageLink(optionsUrl, thumbUrl, 'Click for image download options', 120, 120);
                html = '<img src="' + thumbUrl + '" alt="' + title + '" title="' + title + '" height="120" width="120" />';
            }

            return html;
        },

        font15Renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            metaData.css = 'icon-align';    // class to vertically align the cell
            var html = '<p style="white-space: normal;font-size: 15px">' + value + '</p>';
            return html;
        },

        font13Renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = '<p style="white-space: normal;font-size: 13px">' + value + '</p>';
            return html;
        },

        /////////////////////////////////////////////////////////////
        // Functions for getting preview URLs.  Valid sizes are:
        //  full - (3840x3840 for GALEX)
        //  large - (1920x1920 for GALEX)
        //  medium - (960x960 for GALEX)
        //  small - (480x480 for GALEX)
        //  thumb - (120x120 for GALEX)

        pressSizeMap: {
            full: 'full_jpg.jpg',
            large: 'large_web.jpg',
            medium: 'web.jpg',
            small: 'small_web.jpg',
            thumb: 'small_web.jpg'
        },

        pressPreviewUrl: function (tifUrl, size) {
            var sizeArg = Mvp.custom.Hst.pressSizeMap[size];
            var url = null;
            if (tifUrl) {
                url = tifUrl.replace('full_tif.tif', sizeArg);
            }

            return url;
        },

        stprPreviewUrl: function (tifUrl, size) {
            var url = null;
            if (tifUrl) {
                url = tifUrl.replace('tif.tif', 'jpg.jpg').replace('-full-', '-' + size + '-');
            }

            return url;
        },

        dataviewTemplate: function () {
            var tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap">',
                        '<div class="thumb">',
                        '{[this.dvRenderer(400, 400, values["UnnamedField-14"], values["UnnamedField-18"])]}',
                            '<div style="width: 400px; height: 90px; text-align: center">',
                            '<font size="+1">{UnnamedField}</font><br>',  // Description
                            '<font size="+0">{[Mvp.util.Util.createLink(values["UnnamedField-19"], "The full press release")]}</font><br>',
                            '<font size="+0">{[Mvp.util.Util.createLink(values["UnnamedField-18"], "Image download options")]}</font><br>',
                            '</div>',
                        '</div>',
                    '</div>',
                '</tpl>',
                {
                    templateLink: function (collection, jpegURL, obs_id, target_name, instrument) {

                        var html = Mvp.util.Util.createLink(linkUrl, 'More...');

                        return html;

                    },

                    dvRenderer: function (height, width, tifUrl, optionsUrl) {
                        // Find a url for the preview image.
                        var thumbUrl = null;
                        var medUrl = null;
                        var fullUrl = null;
                        var hst = Mvp.custom.Hst;

                        // Construct the html to render.
                        var html = 'No Preview';
                        if (tifUrl) {
                            thumbUrl = hst.pressPreviewUrl(tifUrl, 'medium');
                            html = Mvp.util.Util.createImageLink(optionsUrl, thumbUrl, 'Click for image download options', height, width);
                        }

                        return html;
                    }

                }
            );

            return tpl;
        },

        stprDataviewTemplate: function () {
            var tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap">',
                        '<div class="thumb">',
                        '{[this.dvRenderer(400, 400, values["resourceurl"], values["referenceurl"])]}',
                            '<div style="width: 400px; height: 90px; text-align: center">',
                            '<font size="+1">{title}</font><br>',  // Description
                            '<font size="+0">{[Mvp.util.Util.createLink(values["referenceurl"], "Image download options")]}</font><br>',
                            '</div>',
                        '</div>',
                    '</div>',
                '</tpl>',
                {
                    templateLink: function (collection, jpegURL, obs_id, target_name, instrument) {

                        var html = Mvp.util.Util.createLink(linkUrl, 'More...');

                        return html;

                    },

                    dvRenderer: function (height, width, tifUrl, optionsUrl) {
                        // Find a url for the preview image.
                        var thumbUrl = null;
                        var medUrl = null;
                        var fullUrl = null;
                        var hst = Mvp.custom.Hst;

                        // Construct the html to render.
                        var html = 'No Preview';
                        if (tifUrl) {
                            thumbUrl = hst.stprPreviewUrl(tifUrl, 'medium');
                            html = Mvp.util.Util.createImageLink(optionsUrl, thumbUrl, 'Click for image download options', height, width);
                        }

                        return html;
                    }

                }
            );

            return tpl;
        }
    }
})