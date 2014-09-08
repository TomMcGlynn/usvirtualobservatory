
Ext.define('Mvp.custom.Caom', {
    statics: {
        caomPreviewRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            // First figure out what type of record this is.
            type = record.get('obs_collection');

            // Find a url for the preview image.
            var imageUrl = null;
            var linkUrl = null;
            var caom = Mvp.custom.Caom;
            if (type === 'GALEX') {
                linkUrl = record.get('jpegURL');
                imageUrl = caom.galexPreviewUrl(linkUrl, 'thumb');
            } else if (type == 'HLA') {
                var obs_id = record.get('obs_id');
                linkUrl = caom.hlaPreviewUrl(obs_id);
                imageUrl = caom.hstPreviewUrl(obs_id, 'small');
            } else if (type == 'IUE') {
                var linkUrl = record.get('jpegURL');
                linkUrl = caom.iuePreviewUrl(linkUrl);
                imageUrl = caom.iuePreviewUrl(linkUrl, 'small');
            }

            // Construct the html to render.
            var html = 'No Preview';
            if (imageUrl) {
                //html = Mvp.util.Util.createImageLink(linkUrl, imageUrl, 'Click for full resolution image', 120, 120);
                html = '<img src="' + imageUrl + '" alt="' + imageUrl + '" height="120" width="120" />';
            }

            return html;
        },

        // Returns an array containing a link to a small version of the image, and an
        // html snippet that makes an image of that small image at the given height and width that
        // is also a hyperlink to the full resolution of the image.
        simplePreviewGenerator: function (record, height, width) {
            type = record.get('obs_collection');

            // Find a url for the preview image.
            var imageUrl = null;
            var linkUrl = null;
            var caom = Mvp.custom.Caom;
            if (type === 'GALEX') {
                linkUrl = record.get('jpegURL');
                imageUrl = caom.galexPreviewUrl(linkUrl, 'thumb');
            } else if (type == 'HLA') {
                var obs_id = record.get('obs_id');
                linkUrl = caom.hlaPreviewUrl(obs_id);
                imageUrl = caom.hstPreviewUrl(obs_id, 'small');
            } else if (type == 'IUE') {
                var linkUrl = record.get('jpegURL');
                linkUrl = caom.iuePreviewUrl(linkUrl);
                imageUrl = caom.iuePreviewUrl(linkUrl, 'small');
            }

            // Construct the html to render.
            var html = 'No Preview';
            if (!height || height == 0) height = 120;
            if (!width || width == 0) width = 120;
            if (imageUrl) {
                //html = Mvp.util.Util.createImageLink(linkUrl, imageUrl, 'Click for full resolution image', height, width);
                html = '<img src="' + imageUrl + '" alt="' + imageUrl + '" height="' + height + '" width="' + width + '" />';
            }

            return [linkUrl, html];

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

        hlaPreviewUrl: function (obs_id) {
            var url = null;
            url = 'http://hla.stsci.edu/cgi-bin/display?image=' + obs_id;
            //+ '&size=5650%205650&izoom=1.000000&detector=ACS%2FWFC&aperture=WFC&crpix=2825.000%202825.000&crval=210.875767%2054.357767&cdmatrix=-1.38889E-05%200%200%201.38889E-05&proj=TAN&refframe=ICRS&title=10918_01%20ACS%2FWFC%20F555W%20%28combined%29%20M101-F1';
            return url;
        },

        hstPreviewUrl: function (obs_id, size) {
            var url = null;
            var sizeArg = Mvp.custom.Caom.hlaSizeMap[size];

            // Compute size
            if (obs_id.match(/^hst/i)) {
                url = 'http://hla.stsci.edu/cgi-bin/fitscut.cgi?red=' + obs_id + '&size=ALL' + sizeArg;
            } else if (obs_id.match(/^hlsp/i)) {
                url = 'http://archive.stsci.edu/cgi-bin/hla/fitscut.cgi?red=' + obs_id + '&size=ALL' + sizeArg;
            }

            return url;
        },

        galexPreviewUrl: function (baseJpgUrl, size) {
            url = baseJpgUrl;
            if (url) {
                url = url.replace(/\.jpg$/i, '_' + size + '.jpg');
            }
            return url;
        },
        
        iuePreviewUrl: function (baseJpgUrl, size) {
            url = baseJpgUrl;
            return url;
        },
        
        dataviewTemplate: function() {
            var tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thumb-wrap">',
                        '<div class="thumb">',
                        '{[this.dvRenderer(256, 256, values.obs_collection, values.jpegURL, values.obs_id)]}',
                            '<div>',
                            '<font size="+2">{target_name}</font><br>',
                            '<font size="+1">{instrument}</font><br>',
                            '<font size="+0">{obs_id}</font><br>',
                            '<font size="+0">{[this.displayLink(values.obs_collection, values.jpegURL, values.obs_id)]}</font><br>',
                            '<font size="+0">{[this.moreInfoLink(values.obs_collection, values.jpegURL, values.obs_id, values.target_name, values.instrument)]}</font><br>',
                            '</div>',
                        '</div>',
                    '</div>',
                '</tpl>',
                {
                    moreInfoLink: function(collection, jpegURL, obs_id, target_name, instrument) {
                        type = collection;
            
                        // Find a url for displaying image.
                        var linkUrl = null;
                        var caom = Mvp.custom.Caom;
                        if (type === 'GALEX') {
                            linkUrl = jpegURL;
                        } else if (type == 'HLA') {
                            linkUrl = 'http://hlatest.stsci.edu//cgi-bin/moreinfo.cgi?html=1' +
                            '&dataset=' +
                            obs_id +
                            '&filename=' +
                            obs_id +
                            '&detector=' +
                            instrument +
                            '&target=' +
                            target_name;
                        }
                        var html = Mvp.util.Util.createLink(linkUrl, 'More...');
            
                        return html;
            
                    },
                
                    displayLink: function(collection, jpegURL, obs_id) {
                        type = collection;
            
                        // Find a url for displaying image.
                        var linkUrl = null;
                        var caom = Mvp.custom.Caom;
                        if (type === 'GALEX') {
                            linkUrl = jpegURL;
                        } else if (type == 'HLA') {
                            linkUrl = caom.hlaPreviewUrl(obs_id);
                        } else if (type === 'IUE') {
                            linkUrl = jpegURL;
                        }
                        var html = Mvp.util.Util.createLink(linkUrl, 'Interactive Display');
            
                        return html;
            
                    },
                
                    dvRenderer: function (height, width, collection, jpegURL, obs_id) {
                        type = collection;
            
                        // Find a url for the preview image.
                        var imageUrl = null;
                        var linkUrl = null;
                        var caom = Mvp.custom.Caom;
                        if (type === 'GALEX') {
                            linkUrl = jpegURL;
                            imageUrl = caom.galexPreviewUrl(linkUrl, 'small');
                        } else if (type == 'HLA') {
                            linkUrl = caom.hlaPreviewUrl(obs_id);
                            imageUrl = caom.hstPreviewUrl(obs_id, 'small');
                        } else if (type === 'IUE') {
                            linkUrl = jpegURL;
                            imageUrl = caom.iuePreviewUrl(linkUrl, 'small');
                        }
            
                        // Construct the html to render.
                        var html = 'No Preview';
                        if (!height || height == 0) height = 120;
                        if (!width || width == 0) width = 120;
                        if (imageUrl) {
                            html = Mvp.util.Util.createImageLink(linkUrl, imageUrl, 'Click for full resolution image', height, width);
                        }
            
                        return html;
            
                    }

                    
                }
            );
            
            return tpl;
        }
    }
})