Ext.define('Mvp.util.Util', {
    requires: ['Mvp.data.Histogram', 'Mvp.data.DecimalHistogram'],
    statics: {
        createLink: function (link, text, onclick) {
            var linkText = (text) ? text : link;
            var htmlLink = '<a href="' + link + '" ' + 
            ((onclick) ? 'onclick="' + onclick + '" ' : 'target="_blank" ')
            + '>' + linkText + '</a>';
            return htmlLink;
        },

        createLinkIf: function (link, text) {
            var retVal = link;
            if (Mvp.util.Util.isUrl(link)) {
                retVal = Mvp.util.Util.createLink(link, text);
            }
            return retVal;
        },

        createImageLink: function (link, imageSrc, title, width, height, record) {
            var src = (!record || !record.imageError) ? imageSrc : '../Shared/img/nopreview.png';

            var linkTitle = (title) ? title : link;
            var html = '<a href="' + link + '" target="_blank" title="' + linkTitle + '">' +
                Mvp.util.Util.createImageHtml(src, linkTitle, width, height, record) + '</a>';
            return html;
        },

        createImageHtml: function (imageSrc, title, width, height, record) {
            var src = (!record || !record.imageError) ? imageSrc : '../Shared/img/nopreview.png';
            var img = new Image(width, height);
            img.onerror = function () {
                this.src = '../Shared/img/nopreview.png';
                if (record) record.imageError = true;   // hangs a flag on the record that the image is bad, if a record was passed in
            }
            img.src = src;


            var alt = (title) ? title : '';
            var html = '<img src="' + src +
            ((width) ? ('" width="' + width) : '') +
            ((height) ? ('" height="' + height) : '') +
            '" alt="' + alt + '" title="' + title + '" onerror="this.src=\'../Shared/img/nopreview.png\';" />';
            return html;
        },

        isUrl: function (url) {
            isUrl = false;
            if (Ext.isString(url)) {
                isUrl = url.match('^(http|ftp)s?:\/\/');
            }
            return isUrl;
        },

        isFtpUrl: function (url) {
            isUrl = false;
            if (Ext.isString(url)) {
                isUrl = url.match('^ftps?:\/\/');
            }
            return isUrl;
        },
        
        // The accessUrl value for registered VO services needs to be patched a little in some cases.
        // First, "&amp;" needs to be replaced with "&".  Then the URL needs to end with either a "?" or "&"
        // so that it's ready for the additional query parameters.
        fixAccessUrl: function(accessUrl) {
            var fixed = accessUrl;
            
            if (fixed) {
                fixed = fixed.replace(/amp;/gi, '');
    
                if (!fixed.match(/(\?&)$/)) {
                    if (fixed.match(/\?/)) {
                        fixed = fixed + '&';
                    } else {
                        fixed = fixed + '?';
                    }
                }
            }
            return fixed;
        },

        /**
        * This method creates a new object whose attributes are the subset of given object's attributes
        * that start with the given prefix followed by a period.  The new attributes have the prefix and
        * period stripped off.
        * Arguments:
        * object:  The object from which to extract the prefixed attributes
        * prefix:  A string containing the prefix to look for
         
        * 
        * So this line of code:
        * var cc = Mvp.util.Util.extractByPrefix({cc.a": "aval", "cc.b": "bval", "ccc": "cval", "vot.a": "votaval"}, 'cc');
        * would create an object called cc that contained:
        * {a: "aval", b: "bval}    (no cval because the ccc attribute has no period)
        */
        extractByPrefix: function (object, prefix) {
            var extracted = {};
            Ext.Object.each(object, function (key, value, myself) {
                var re = prefix + '\.';
                if (key.match(re)) {
                    var shortName = key.replace(re, '');
                    extracted[shortName] = value;
                }
            });

            return extracted;
        },

        numberValidator: function (val) {
            if (Ext.Number.from(val, -1) >= 0) {
                return true;
            } else {
                return 'Value must be a non-negative number.';
            }
        },

        filenameCreator: function (startingTitle, extension) {
            // drop all non-word characters, then remove all duplicate, trailing and leading underscores
            var title = startingTitle.trim().replace(/\W+/g, '_').replace(/_+/g, '_').replace(/(^_|_$|nbsp)/g, '');

            // Duplicate labels are common, so try and remove them
            var tokens = title.split('_');
            var testToken;
            for (var i = 0; i <= tokens.length; i++) {
                testToken = tokens[i];
                if (testToken) {
                    for (var j = i + 1; j <= tokens.length; j++) {
                        if (tokens[j] && testToken.toLowerCase() == tokens[j].toLowerCase()) {
                            tokens.splice(j, 1);
                        }
                    }
                }
            }
            title = tokens.join('_') + '.' + extension;
            return title;

        },

        decimalHistogram: function (store, property, min, max, nBuckets, ignoreValue) {
            var histogram = new Array(nBuckets);
            for (var i = 0; i < nBuckets; i++) histogram[i] = 0;
            var bucketSize = (max - min) / nBuckets;
            var items = store;
            var nItems = items.length;

            if (items && Ext.isArray(items)) {
                var i = items.length;
                while (i--) {
                    var record = items[i];
                    var value = record.get(property);
                    if ((value < min) || (value > max)) continue;
                    if (ignoreValue != value) {
                        var b = Math.floor((value - min) / bucketSize);
                        b = Math.min(b, nBuckets - 1);
                        histogram[b]++;

                    }
                }
            }
            var hist = [],
            maxCount = 0;
            for (var i = 0; i < nBuckets; i++) {
                var r = histogram[i];
                hist.push({ bucket: min + i * bucketSize, ratio: r, key: min + i * bucketSize, count: r, exclude: 0 });
                if (r > maxCount) maxCount = r;
            }
            return {
                histArray: hist,
                max: maxCount
            };
        },

        decimalHistogramToStore: function (histArray) {
            var s = Ext.create('Ext.data.JsonStore', { extend: 'Mvp.data.DecimalHistogram', fields: ['key', 'count'], data: histArray });
            return s;
        },

        potentialHistogram: function (potentialStore, property, min, max, nBuckets, ignoreValue, minBound, maxBound) {
            //  this function creates a decimal histogram, but has the additional parameters minBound and maxBound that define
            //  the range of values outside of which to mark values as excluded
            var histogram = new Array(nBuckets), exclusionHistogram = new Array(nBuckets);
            for (var i = 0; i < nBuckets; i++) {
                histogram[i] = 0;
                exclusionHistogram[i] = 0
            }
            var bucketSize = (max - min) / nBuckets;

            if (potentialStore && Ext.isArray(potentialStore)) {
                var i = potentialStore.length;
                while (i--) {
                    var record = potentialStore[i];
                    var value = record.get(property) || record[property];
                    if (ignoreValue != value) {
                        var b = Math.floor((value - min) / bucketSize);
                        if ((b < 0) || (b > 99)) continue;
                        (((value < minBound) && (value >= min)) || ((value > maxBound) && (value <= max))) ? exclusionHistogram[b]++ : histogram[b]++;
                    }
                }
            }
            var hist = [],
            maxCount = 0;
            for (var i = 0; i < nBuckets; i++) {
                var inc = histogram[i], ex = exclusionHistogram[i];
                hist.push({ bucket: min + i * bucketSize, ratio: inc, key: min + i * bucketSize, count: inc, excluded: ex });
                if (inc > maxCount) maxCount = inc;
                if (ex > maxCount) maxCount = ex;
            }
            return {
                histArray: hist,
                max: maxCount
            };
        },

        potentialHistogramToStore: function (histArray) {
            var s = Ext.create('Ext.data.JsonStore', { extend: 'Mvp.data.DecimalHistogram', fields: ['key', 'count', 'excluded'], data: histArray });
            return s;
        },

        // This is supposed to generate a histogram of the contents of a column of data
        // in the specified mixed collection.
        histogram: function (mixedCollection, property, separator) {
            var items = mixedCollection;
            var hist = {};
            hist._numEntries = 0;
            if (items && Ext.isArray(items)) {
                var i = items.length;
                while (i--) {
                    var record = items[i];
                    var value = record.get(property);
                    if ((value !== undefined) && (value !== null)) {
                        var stringVal = value.toString();
                        var keys = [stringVal];
                        if (separator) {
                            keys = stringVal.split(separator);
                        }
                        var k = keys.length;
                        while (k--) {
                            var key = keys[k];
                            //if (key != '') {
                            var histEntry = hist[key];
                            if (!histEntry) {
                                hist._numEntries++;
                                hist[key] = { key: key, count: 1 };
                            } else {
                                histEntry.count++;
                            }
                            //}
                        }
                    }
                }
            }
            return hist;
        },

        histogramToArray: function (histogram) {
            var histArray = new Array(histogram._numEntries);
            var i = 0;
            for (property in histogram) {
                if (property.charAt(0) !== '_') {
                    histArray[i++] = histogram[property];
                }
            }
            return histArray;
        },

        histogramArrayToStore: function (histArray) {
            var store = Ext.create('Ext.data.Store', {
                model: 'Mvp.data.Histogram'
            });
            store.add(histArray);
            return store;
        },

        logicalSort: function (a, b) {
            // sorts in ascending alphabetical/numeric order - standard array .sort() sorts lexicographically, which gets numbers wrong
            var na = Number(a), nb = Number(b)
            if ((na == a) && (nb == b)) return na - nb;
            var sa = a.toString().toLowerCase(),
                sb = b.toString().toLowerCase();
            return sa.localeCompare(sb);
        },

        adoptContext: function (sourceWin, destWin) {
            var shareNamespace = function (ns, sourceWin, destWin) {
                if (sourceWin[ns]) {
                    destWin[ns] = sourceWin[ns];
                }
            };
            shareNamespace('Ext', sourceWin, destWin);
            shareNamespace('Mvp', sourceWin, destWin);
            shareNamespace('Mvpc', sourceWin, destWin);
            shareNamespace('Mvpd', sourceWin, destWin);
            shareNamespace('Vao', sourceWin, destWin);
            shareNamespace('Mast', sourceWin, destWin);

            // Make the injection function easier to access.
            if (sourceWin.Vao) {
                Mvp.injectSearchText = Vao.view.VaoTopBar.injectSearchText;
            } else if (sourceWin.Mast) {
                Mvp.injectSearchText = Mast.view.MastTopBar.injectSearchText;
            }
        },

        parseCsvWithDashes: function (str) {    // parses a string of the form "1, 4-6, 9" and returns "1,4,5,6,9"
            var tokens = str.replace(' ', '').replace(/(^,|,$)/, '').split(',');
            var dict = {};
            var retVal = '';
            for (var i in tokens) {
                var token = tokens[i];
                if (token.trim() === '') continue;
                var range = token.split('-');
                var min = Number(range[0]),
                    len = range.length,
                    max = len > 1 ? Number(range[len - 1]) : undefined;
                if (max !== undefined && max < min) {
                    var t = min;
                    min = max;
                    max = t;
                }
                dict[min] = true;
                if (max) {
                    for (var j = min + 1; j <= max; j++) {
                        dict[j] = true;
                    }
                }
            }
            var first = true;
            for (var i in dict) {
                if (!first) retVal += ',';
                retVal += i;
                first = false;
            }
            return retVal;
        },
            
        // return true iff 'a 'is a JS array containing all numeric values (can be strings parsable as numbers), and
        // the length of 'a' is exactly 'size' if 'size' specified.
        // Also, string numerics are turned into Numbers.
        isValidNumericArray: function(a, size) {
            var valid = Ext.isArray(a);
            if (valid && Ext.isDefined(size)) {
                valid = (size === a.length);
            }
            if (valid) {
                for (var i=0; i<a.length && valid; i++) {
                    valid = Ext.isNumeric(a[i]);
                    if (valid) {
                        a[i] = new Number(a[i]);
                    }
                }
            }
            return valid;
        },



        escapeRegExp: function (str) {  // replaces all characters that are required to be escaped in a RegExp
            return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        },

        findCatalogUcds: function (str) {    // matches magnitude, flux and error UCDs
            return str.match(/(FIT_LF_MAG|FIT_LF_MAG_MAX|FIT_LF_MAG_MIN|MODEL_MAG|MODEL_MAG_CORR|MODEL_MAG_VALUE|PHOT_13C_52|PHOT_ABS-MAG|PHOT_ABS-MAG_BAND|PHOT_ABS-MAG_BOL|PHOT_BOL_MAG|PHOT_COUS_I|PHOT_COUS_R|PHOT_DDO_MAG|PHOT_DIFF_MAG|PHOT_GEN_V|PHOT_GUNN_G|PHOT_GUNN_I|PHOT_GUNN_R|PHOT_GUNN_V|PHOT_GUNN_Z|PHOT_HST_F1042M|PHOT_HST_F140W|PHOT_HST_F170W|PHOT_HST_F220W|PHOT_HST_F255W|PHOT_HST_F275W|PHOT_HST_F300W|PHOT_HST_F342W|PHOT_HST_F430W|PHOT_HST_F450W|PHOT_HST_F480LP|PHOT_HST_F547M|PHOT_HST_F555W|PHOT_HST_F569W|PHOT_HST_F606W|PHOT_HST_F622W|PHOT_HST_F675W|PHOT_HST_F702W|PHOT_HST_F725LP|PHOT_HST_F775W|PHOT_HST_F785LP|PHOT_HST_F791W|PHOT_HST_F814W|PHOT_HST_F850LP|PHOT_HST_V|PHOT_INT-MAG|PHOT_INT-MAG_B|PHOT_INT-MAG_H|PHOT_INT-MAG_I|PHOT_INT-MAG_J|PHOT_INT-MAG_K|PHOT_INT-MAG_MISC|PHOT_INT-MAG_R|PHOT_INT-MAG_U|PHOT_INT-MAG_V|PHOT_IR|PHOT_IR_2\.29|PHOT_IR_3\.4|PHOT_IR_4\.2|PHOT_IR_BGAMMA|PHOT_IR_L0|PHOT_IR_MAG|PHOT_IR_N:10\.4|PHOT_IR_N1:8\.38|PHOT_IR_N2:9\.69|PHOT_IR_N3:12\.89|PHOT_IR_Q:18\.06|PHOT_JHN_B|PHOT_JHN_H|PHOT_JHN_I|PHOT_JHN_J|PHOT_JHN_K|PHOT_JHN_K'|PHOT_JHN_L|PHOT_JHN_L\'|PHOT_JHN_M|PHOT_JHN_N|PHOT_JHN_R|PHOT_JHN_U|PHOT_JHN_V|PHOT_MAG|PHOT_MAG_5007|PHOT_MAG_B|PHOT_MAG_BLUE|PHOT_MAG_CENTR|PHOT_MAG_CORR|PHOT_MAG_DROP|PHOT_MAG_EYE|PHOT_MAG_H|PHOT_MAG_HI|PHOT_MAG_I|PHOT_MAG_IR|PHOT_MAG_J|PHOT_MAG_K|PHOT_MAG_LIMIT|PHOT_MAG_OFFST|PHOT_MAG_OPTICAL|PHOT_MAG_R|PHOT_MAG_RED|PHOT_MAG_U|PHOT_MAG_UNDEF|PHOT_MAG_UV|PHOT_MAG_V|PHOT_MAG_VISUAL|PHOT_MAG_Y|PHOT_PHG|PHOT_PHG_B|PHOT_PHG_BJ|PHOT_PHG_I|PHOT_PHG_MAG|PHOT_PHG_R|PHOT_PHG_U|PHOT_PHG_V|PHOT_RF_V|PHOT_SDSS_G|PHOT_SDSS_I|PHOT_SDSS_R|PHOT_SDSS_U|PHOT_SDSS_Z|PHOT_SPHOT_MAG|PHOT_STR_B|PHOT_STR_U|PHOT_STR_V|PHOT_STR_Y|PHOT_TOT-BRIGHT\/B-BRIGHT|PHOT_TYCHO_B|PHOT_TYCHO_V|PHOT_UV|PHOT_UV_1300|PHOT_UV_1400|PHOT_UV_1500|PHOT_UV_1600|PHOT_UV_1700|PHOT_UV_1800|PHOT_UV_1900|PHOT_UV_2000|PHOT_UV_2200|PHOT_UV_2300|PHOT_UV_2400|PHOT_UV_2500|PHOT_UV_2700|PHOT_UV_2945|PHOT_UV_3150|PHOT_UV_3300|PHOT_UV_4250|PHOT_UV_GENERAL|PHOT_UVBGRI_B|PHOT_UVBGRI_G|PHOT_UVBGRI_I|PHOT_UVBGRI_R|PHOT_UVBGRI_U|PHOT_UVBGRI_V|PHOT_VIL_V|PHOT_VIL_X|PHOT_WASH_C|PHOT_WASH_M|PHOT_WASH_T1|PHOT_WASH_T2|PHOT_WLRV_B|PHOT_WLRV_L|PHOT_WLRV_U|PHOT_WLRV_V|PHOT_WLRV_W|VAR_MAG-RANGE|INST_BEAM_TEMP|INST_SENSITIVITY|MODEL_FLUX|MODEL_LINE-FLUX|PHOT_BOL_FLUX|PHOT_FLUX|PHOT_FLUX_B|PHOT_FLUX_DENSITY|PHOT_FLUX_GAMMA|PHOT_FLUX_HALPHA|PHOT_FLUX_HBETA|PHOT_FLUX_I|PHOT_FLUX_IR|PHOT_FLUX_IR_100|PHOT_FLUX_IR_12|PHOT_FLUX_IR_15|PHOT_FLUX_IR_170|PHOT_FLUX_IR_25|PHOT_FLUX_IR_300|PHOT_FLUX_IR_6|PHOT_FLUX_IR_60|PHOT_FLUX_IR_9|PHOT_FLUX_IR_FAR|PHOT_FLUX_IR_H|PHOT_FLUX_IR_J|PHOT_FLUX_IR_K|PHOT_FLUX_IR_L|PHOT_FLUX_IR_MISC|PHOT_FLUX_NORM|PHOT_FLUX_OPTICAL|PHOT_FLUX_R|PHOT_FLUX_RADIO|PHOT_FLUX_RADIO_1\.4G|PHOT_FLUX_RADIO_1\.6G|PHOT_FLUX_RADIO_10\.7G|PHOT_FLUX_RADIO_110M|PHOT_FLUX_RADIO_125G|PHOT_FLUX_RADIO_150M|PHOT_FLUX_RADIO_15G|PHOT_FLUX_RADIO_175M|PHOT_FLUX_RADIO_180G|PHOT_FLUX_RADIO_1G|PHOT_FLUX_RADIO_2\.7G|PHOT_FLUX_RADIO_22G|PHOT_FLUX_RADIO_22M|PHOT_FLUX_RADIO_250G|PHOT_FLUX_RADIO_250M|PHOT_FLUX_RADIO_2G|PHOT_FLUX_RADIO_3\.9G|PHOT_FLUX_RADIO_31M|PHOT_FLUX_RADIO_325M|PHOT_FLUX_RADIO_350G|PHOT_FLUX_RADIO_365M|PHOT_FLUX_RADIO_36G|PHOT_FLUX_RADIO_400M|PHOT_FLUX_RADIO_43G|PHOT_FLUX_RADIO_43M|PHOT_FLUX_RADIO_500G|PHOT_FLUX_RADIO_500M|PHOT_FLUX_RADIO_5G|PHOT_FLUX_RADIO_600M|PHOT_FLUX_RADIO_61M|PHOT_FLUX_RADIO_63G|PHOT_FLUX_RADIO_7\.5G|PHOT_FLUX_RADIO_700G|PHOT_FLUX_RADIO_700M|PHOT_FLUX_RADIO_8\.4G|PHOT_FLUX_RADIO_850M|PHOT_FLUX_RADIO_87M|PHOT_FLUX_RADIO_90G|PHOT_FLUX_RADIO_MISC|PHOT_FLUX_RADIO_SIO-MASER|PHOT_FLUX_RATIO|PHOT_FLUX_REL|PHOT_FLUX_U|PHOT_FLUX_UNDEF|PHOT_FLUX_UV|PHOT_FLUX_V|PHOT_FLUX_X|PHOT_JHN_N|PHOT_SPHOT_FLUX|PHOT_SPHOT_INDEX|PHOT_WLRV_B|PHOT_WLRV_L|PHOT_WLRV_U|PHOT_WLRV_V|PHOT_WLRV_W|PHYS_MASS_FLUX|POL_FLUX|POL_FLUX_LCP|POL_FLUX_LINEAR|POL_FLUX_RCP|SPECT_FLUX|SPECT_FLUX_NORM|SPECT_FLUX_RATIO|SPECT_FLUX_UV|SPECT_FLUX_VALUE|ERROR|FIT_ERROR|INST_CALIB_ERROR|INST_ERROR|STAT_DISP)/i);
        },

        findMagnitudeUcds: function (str) {
            return str.match(/(FIT_LF_MAG|FIT_LF_MAG_MAX|FIT_LF_MAG_MIN|MODEL_MAG|MODEL_MAG_CORR|MODEL_MAG_VALUE|PHOT_13C_52|PHOT_ABS-MAG|PHOT_ABS-MAG_BAND|PHOT_ABS-MAG_BOL|PHOT_BOL_MAG|PHOT_COUS_I|PHOT_COUS_R|PHOT_DDO_MAG|PHOT_DIFF_MAG|PHOT_GEN_V|PHOT_GUNN_G|PHOT_GUNN_I|PHOT_GUNN_R|PHOT_GUNN_V|PHOT_GUNN_Z|PHOT_HST_F1042M|PHOT_HST_F140W|PHOT_HST_F170W|PHOT_HST_F220W|PHOT_HST_F255W|PHOT_HST_F275W|PHOT_HST_F300W|PHOT_HST_F342W|PHOT_HST_F430W|PHOT_HST_F450W|PHOT_HST_F480LP|PHOT_HST_F547M|PHOT_HST_F555W|PHOT_HST_F569W|PHOT_HST_F606W|PHOT_HST_F622W|PHOT_HST_F675W|PHOT_HST_F702W|PHOT_HST_F725LP|PHOT_HST_F775W|PHOT_HST_F785LP|PHOT_HST_F791W|PHOT_HST_F814W|PHOT_HST_F850LP|PHOT_HST_V|PHOT_INT-MAG|PHOT_INT-MAG_B|PHOT_INT-MAG_H|PHOT_INT-MAG_I|PHOT_INT-MAG_J|PHOT_INT-MAG_K|PHOT_INT-MAG_MISC|PHOT_INT-MAG_R|PHOT_INT-MAG_U|PHOT_INT-MAG_V|PHOT_IR|PHOT_IR_2\.29|PHOT_IR_3\.4|PHOT_IR_4\.2|PHOT_IR_BGAMMA|PHOT_IR_L0|PHOT_IR_MAG|PHOT_IR_N:10\.4|PHOT_IR_N1:8\.38|PHOT_IR_N2:9\.69|PHOT_IR_N3:12\.89|PHOT_IR_Q:18\.06|PHOT_JHN_B|PHOT_JHN_H|PHOT_JHN_I|PHOT_JHN_J|PHOT_JHN_K|PHOT_JHN_K'|PHOT_JHN_L|PHOT_JHN_L\'|PHOT_JHN_M|PHOT_JHN_N|PHOT_JHN_R|PHOT_JHN_U|PHOT_JHN_V|PHOT_MAG|PHOT_MAG_5007|PHOT_MAG_B|PHOT_MAG_BLUE|PHOT_MAG_CENTR|PHOT_MAG_CORR|PHOT_MAG_DROP|PHOT_MAG_EYE|PHOT_MAG_H|PHOT_MAG_HI|PHOT_MAG_I|PHOT_MAG_IR|PHOT_MAG_J|PHOT_MAG_K|PHOT_MAG_LIMIT|PHOT_MAG_OFFST|PHOT_MAG_OPTICAL|PHOT_MAG_R|PHOT_MAG_RED|PHOT_MAG_U|PHOT_MAG_UNDEF|PHOT_MAG_UV|PHOT_MAG_V|PHOT_MAG_VISUAL|PHOT_MAG_Y|PHOT_PHG|PHOT_PHG_B|PHOT_PHG_BJ|PHOT_PHG_I|PHOT_PHG_MAG|PHOT_PHG_R|PHOT_PHG_U|PHOT_PHG_V|PHOT_RF_V|PHOT_SDSS_G|PHOT_SDSS_I|PHOT_SDSS_R|PHOT_SDSS_U|PHOT_SDSS_Z|PHOT_SPHOT_MAG|PHOT_STR_B|PHOT_STR_U|PHOT_STR_V|PHOT_STR_Y|PHOT_TOT-BRIGHT\/B-BRIGHT|PHOT_TYCHO_B|PHOT_TYCHO_V|PHOT_UV|PHOT_UV_1300|PHOT_UV_1400|PHOT_UV_1500|PHOT_UV_1600|PHOT_UV_1700|PHOT_UV_1800|PHOT_UV_1900|PHOT_UV_2000|PHOT_UV_2200|PHOT_UV_2300|PHOT_UV_2400|PHOT_UV_2500|PHOT_UV_2700|PHOT_UV_2945|PHOT_UV_3150|PHOT_UV_3300|PHOT_UV_4250|PHOT_UV_GENERAL|PHOT_UVBGRI_B|PHOT_UVBGRI_G|PHOT_UVBGRI_I|PHOT_UVBGRI_R|PHOT_UVBGRI_U|PHOT_UVBGRI_V|PHOT_VIL_V|PHOT_VIL_X|PHOT_WASH_C|PHOT_WASH_M|PHOT_WASH_T1|PHOT_WASH_T2|PHOT_WLRV_B|PHOT_WLRV_L|PHOT_WLRV_U|PHOT_WLRV_V|PHOT_WLRV_W|VAR_MAG-RANGE)/i);
        },

        findFluxUcds: function(str) {
            return str.match(/(INST_BEAM_TEMP|INST_SENSITIVITY|MODEL_FLUX|MODEL_LINE-FLUX|PHOT_BOL_FLUX|PHOT_FLUX|PHOT_FLUX_B|PHOT_FLUX_DENSITY|PHOT_FLUX_GAMMA|PHOT_FLUX_HALPHA|PHOT_FLUX_HBETA|PHOT_FLUX_I|PHOT_FLUX_IR|PHOT_FLUX_IR_100|PHOT_FLUX_IR_12|PHOT_FLUX_IR_15|PHOT_FLUX_IR_170|PHOT_FLUX_IR_25|PHOT_FLUX_IR_300|PHOT_FLUX_IR_6|PHOT_FLUX_IR_60|PHOT_FLUX_IR_9|PHOT_FLUX_IR_FAR|PHOT_FLUX_IR_H|PHOT_FLUX_IR_J|PHOT_FLUX_IR_K|PHOT_FLUX_IR_L|PHOT_FLUX_IR_MISC|PHOT_FLUX_NORM|PHOT_FLUX_OPTICAL|PHOT_FLUX_R|PHOT_FLUX_RADIO|PHOT_FLUX_RADIO_1\.4G|PHOT_FLUX_RADIO_1\.6G|PHOT_FLUX_RADIO_10\.7G|PHOT_FLUX_RADIO_110M|PHOT_FLUX_RADIO_125G|PHOT_FLUX_RADIO_150M|PHOT_FLUX_RADIO_15G|PHOT_FLUX_RADIO_175M|PHOT_FLUX_RADIO_180G|PHOT_FLUX_RADIO_1G|PHOT_FLUX_RADIO_2\.7G|PHOT_FLUX_RADIO_22G|PHOT_FLUX_RADIO_22M|PHOT_FLUX_RADIO_250G|PHOT_FLUX_RADIO_250M|PHOT_FLUX_RADIO_2G|PHOT_FLUX_RADIO_3\.9G|PHOT_FLUX_RADIO_31M|PHOT_FLUX_RADIO_325M|PHOT_FLUX_RADIO_350G|PHOT_FLUX_RADIO_365M|PHOT_FLUX_RADIO_36G|PHOT_FLUX_RADIO_400M|PHOT_FLUX_RADIO_43G|PHOT_FLUX_RADIO_43M|PHOT_FLUX_RADIO_500G|PHOT_FLUX_RADIO_500M|PHOT_FLUX_RADIO_5G|PHOT_FLUX_RADIO_600M|PHOT_FLUX_RADIO_61M|PHOT_FLUX_RADIO_63G|PHOT_FLUX_RADIO_7\.5G|PHOT_FLUX_RADIO_700G|PHOT_FLUX_RADIO_700M|PHOT_FLUX_RADIO_8\.4G|PHOT_FLUX_RADIO_850M|PHOT_FLUX_RADIO_87M|PHOT_FLUX_RADIO_90G|PHOT_FLUX_RADIO_MISC|PHOT_FLUX_RADIO_SIO-MASER|PHOT_FLUX_RATIO|PHOT_FLUX_REL|PHOT_FLUX_U|PHOT_FLUX_UNDEF|PHOT_FLUX_UV|PHOT_FLUX_V|PHOT_FLUX_X|PHOT_JHN_N|PHOT_SPHOT_FLUX|PHOT_SPHOT_INDEX|PHOT_WLRV_B|PHOT_WLRV_L|PHOT_WLRV_U|PHOT_WLRV_V|PHOT_WLRV_W|PHYS_MASS_FLUX|POL_FLUX|POL_FLUX_LCP|POL_FLUX_LINEAR|POL_FLUX_RCP|SPECT_FLUX|SPECT_FLUX_NORM|SPECT_FLUX_RATIO|SPECT_FLUX_UV|SPECT_FLUX_VALUE)/i);
        },

        findErrorUcds: function (str) {
            return str.match(/(ERROR|FIT_ERROR|INST_CALIB_ERROR|INST_ERROR|STAT_DISP)/i);
        }
    }
});