Ext.require('Mvp.data.Histogram');
Ext.require('Mvp.data.DecimalHistogram');

Ext.define('Mvp.util.Util', {
    statics: {
        createLink: function (link, text) {
            var linkText = (text) ? text : link;
            var htmlLink = '<a target="_blank" href="' + link + '">' + linkText + '</a>';
            return htmlLink;
        },

        createLinkIf: function (link, text) {
            var retVal = link;
            if (Mvp.util.Util.isUrl(link)) {
                retVal = Mvp.util.Util.createLink(link, text);
            }
            return retVal;
        },

        createImageLink: function (link, imageSrc, title, width, height) {
            var linkTitle = (title) ? title : link;
            var html = '<a href="' + link + '" target="_blank" title="' + linkTitle + '">' +
                Mvp.util.Util.createImageHtml(imageSrc, linkTitle, width, height) + '</a>';
            return html;
        },

        createImageHtml: function (imageSrc, title, width, height) {
            var alt = (title) ? title : '';
            var html = '<img src="' + imageSrc +
            ((width) ? ('" width="' + width) : '') +
            ((height) ? ('" height="' + height) : '') +
            '" alt="' + alt + '" />';
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
                hist.push({ bucket: min + i * bucketSize, ratio: r });
                if (r > maxCount) maxCount = r;
            }
            return {
                histArray: hist,
                max: maxCount
            };
        },
        
        decimalHistogramToStore: function(histArray) {
            var s = Ext.create('Ext.data.JsonStore', { extend: 'Mvp.data.DecimalHistogram', fields: ['key', 'count'], data: histArray });
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
                            if (key != '') {
                                var histEntry = hist[key];
                                if (!histEntry) {
                                    hist._numEntries++;
                                    hist[key] = { key: key, count: 1 };
                                } else {
                                    histEntry.count++;
                                }
                            }
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
        
        adoptContext: function(sourceWin, destWin) {
            var shareNamespace = function(ns, sourceWin, destWin) {
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
        }
        

    }
});