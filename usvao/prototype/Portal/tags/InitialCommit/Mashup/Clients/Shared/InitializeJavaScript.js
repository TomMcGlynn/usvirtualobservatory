/**
 * Load the library located at the same path with this file
 *
 * Will automatically load ext-all-debug.js if any of these conditions is true:
 * - Current hostname is localhost
 * - Current hostname is an IP v4 address
 * - Current protocol is "file:"
 *
 * Will load ext-all.js (minified) otherwise
 */
(function() {

    var scripts = document.getElementsByTagName('script'),
        localhostTests = [
            /^localhost$/,
            /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\d{1,5})?\b/ // IP v4
        ],
        host = window.location.hostname,
        isDevelopment = null,
        queryString = window.location.search,
        test, path, i, ln, scriptSrc, match;

    extPath = "http://galextest.stsci.edu/test/extjs/4.0.1/";
    
    for (i = 0, ln = scripts.length; i < ln; i++) {
        scriptSrc = scripts[i].src;
    
        match = scriptSrc.match(/InitializeJavaScript\.js$/);
    
        if (match) {
            initializationPath = scriptSrc.substring(0, scriptSrc.length - match[0].length);
            break;
        }
    }

    if (queryString.match('(\\?|&)debug') !== null) {
        isDevelopment = true;
    }
    else if (queryString.match('(\\?|&)nodebug') !== null) {
        isDevelopment = false;
    }

    if (isDevelopment === null) {
        for (i = 0, ln = localhostTests.length; i < ln; i++) {
            test = localhostTests[i];

            if (host.search(test) !== -1) {
                isDevelopment = true;
                break;
            }
        }
    }

    if (isDevelopment === null && window.location.protocol === 'file:') {
        isDevelopment = true;
    }

    //
    // Dynamically create these 3 tags:
    //
    // <link rel="stylesheet" type="text/css" href="http://dev.sencha.com/deploy/ext-4.0.0/resources/css/ext-all.css" />
    // <link rel="stylesheet" type="text/css" href="http://dev.sencha.com/deploy/ext-4.0.0/examples/shared/example.css" />
    // <script type="text/javascript" src="http://dev.sencha.com/deploy/ext-4.0.0/bootstrap.js"></script>
    //
 	// Use our [path] instead of [http://dev.sencha.com/deploy/ext-4.0.0/]
    //
    
    document.write('<link rel="stylesheet" type="text/css" href="' + extPath + 'resources/css/ext-all.css" />');
    document.write('<link rel="stylesheet" type="text/css" href="' + extPath + 'examples/shared/example.css" />');
    document.write('<script type="text/javascript" src="' + extPath + 'ext-all' + ((isDevelopment) ? '-debug' : '') + '.js"></script>');
    document.write('<script type="text/javascript" src="' + initializationPath + 'InitializeLocal.js"></script>');
    
})();
