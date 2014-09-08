Ext.define('Mvp.util.Util', {
    statics: {
        createLink: function(link, text) {
            var linkText = (text) ? text : link;
            var htmlLink = '<a target="_blank" href="' + link + '">' + linkText + '</a>';
            return htmlLink;
        },
        
        createLinkIf: function(link, text) {
            var retVal = link;
            if (Mvp.util.Util.isUrl(link)) {
                retVal = Mvp.util.Util.createLink(link, text);
            }
            return retVal;
        },
        
        createImageLink: function(link, imageSrc, title, width, height) {
            var linkTitle = (title) ? title : link;
            var html = '<a href="' + link + '" target="_blank" title="' + linkTitle + '"><img src="' + imageSrc + '" width="' + width + '" height="' + height + '" alt="' + linkTitle + '" /></a>';
            return html;
        },

        isUrl: function(url) {
            isUrl = false;
            if (Ext.isString(url)) {
                isUrl = url.match('^https?:\/\/');
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
        extractByPrefix: function(object, prefix) {
            var extracted = {};
            Ext.Object.each(object, function(key, value, myself) {
                var re = prefix + '\.';
                if (key.match(re)) {
                    var shortName = key.replace(re, '');
                    extracted[shortName] = value;
                }
            });

            return extracted;
        }
    }
})