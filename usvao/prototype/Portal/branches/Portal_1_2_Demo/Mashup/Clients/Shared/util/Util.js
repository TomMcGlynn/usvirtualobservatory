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
            var html = '<a href="' + link + '" target="_blank" title="' + linkTitle + '"><img src="' + imageSrc +
            ((width) ? ('" width="' + width) : '') +
            ((height) ? ('" height="' + height) : '') +
            '" alt="' + linkTitle + '" /></a>';
            return html;
        },

        isUrl: function (url) {
            isUrl = false;
            if (Ext.isString(url)) {
                isUrl = url.match('^https?:\/\/');
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
            var title = startingTitle.replace(/\W+/g, '_').replace(/_+/g, '_').replace(/(^_|_$)/g, '');

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
        
        trimString: function(s) {
            var result = s.replace(/^\s+|\s+$/g, '');
            return result;
        }
    }
})