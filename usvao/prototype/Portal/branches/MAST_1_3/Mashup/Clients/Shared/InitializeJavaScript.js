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

(function () {
    AppConfig = {};  // A catch all for the boot-time information.

    var getUrlParam = function (param, url, default_) {
        if (default_ == null) default_ = "";
        param = param.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + param + "=([^&#]*)");
        var qs = regex.exec(url);
        if (qs == null)
            return default_;
        else
            return qs[1];
    };

    var scripts = document.getElementsByTagName('script'),
        localhostTests = [
            /^localhost$/,
            /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\d{1,5})?\b/ // IP v4
        ],
        host = window.location.hostname,
        queryString = window.location.search,
        test, path, i, ln, scriptSrc, match;

    for (i = 0, ln = scripts.length; i < ln; i++) {
        scriptSrc = scripts[i].src;

        //match = scriptSrc.match(/InitializeJavaScript\.js$/);
        match = scriptSrc.match(/Shared[\/\\]InitializeJavaScript\.js$/);

        if (match) {
            AppConfig.initializationPath = scriptSrc.substring(0, scriptSrc.length - match[0].length);

            // Trim the trailing '/' off the initialization path because it causes a '//' in the class loading,
            // which fails on Windows.
            //initializationPath = initializationPath.substring(0, initializationPath.length - 1);

            break;
        }
        match = scriptSrc.match(/Shared[\/\\]IJS\.js$/);

        if (match) {
            AppConfig.initializationPath = scriptSrc.substring(0, scriptSrc.length - match[0].length);

            // Trim the trailing '/' off the initialization path because it causes a '//' in the class loading,
            // which fails on Windows.
            //initializationPath = initializationPath.substring(0, initializationPath.length - 1);

            break;
        }
    }

    AppConfig.pagesizeOverride = getUrlParam('pagesize', queryString, null);

    AppConfig.loadLocal = false;
    if (queryString.match('(\\?|&)loadLocal') !== null) {
        AppConfig.loadLocal = true;
    }

    AppConfig.isMast = false;
    if (window.location.pathname.match('Clients/Mast') !== null) {
        AppConfig.isMast = true;
    }

    AppConfig.useAV = false;
    if (queryString.match('(\\?|&)useAV') !== null) {
        AppConfig.useAV = true;
    }

    AppConfig.avPlacement = getUrlParam('avPlacement', queryString, 'east').toLowerCase();
    if (AppConfig.avPlacement == 'w') AppConfig.avPlacement = 'west';
    //if (AppConfig.avPlacement == 'n') AppConfig.avPlacement = 'north';
    //if (AppConfig.avPlacement == 's') AppConfig.avPlacement = 'south';
    if ((AppConfig.avPlacement != 'south') && (AppConfig.avPlacement != 'west') && (AppConfig.avPlacement != 'north')) AppConfig.avPlacement = 'east';

    AppConfig.avRenderType = getUrlParam('avRenderType', queryString, 'flash').toLowerCase();

    AppConfig.startPage = getUrlParam('startPage', queryString, null);

    AppConfig.useDesktop = false;
    if (window.location.href.match('desktop.html') !== null) {
        AppConfig.useDesktop = true;
    }

    AppConfig.isDevelopment = null;
    if (queryString.match('(\\?|&)debug') !== null) {
        AppConfig.isDevelopment = true;
    }
    else if (queryString.match('(\\?|&)nodebug') !== null) {
        AppConfig.isDevelopment = false;
    }

    AppConfig.isLocal = false;
    if (AppConfig.isDevelopment === null) {
        for (i = 0, ln = localhostTests.length; i < ln; i++) {
            test = localhostTests[i];

            if (host.search(test) !== -1) {
                AppConfig.isLocal = true;
                AppConfig.isDevelopment = true;
                break;
            }
        }
    }

    if (AppConfig.isDevelopment === null && window.location.protocol === 'file:') {
        AppConfig.isDevelopment = true;
    }

    // Make sure the trim() function exists for strings.
    if (typeof (String.prototype.trim) === "undefined") {
        String.prototype.trim = function () {
            return String(this).replace(/^\s+|\s+$/g, '');
        };
    }

    // Enable the use of a remote server during client development.
    AppConfig.mashupURLOverride = undefined;
    //AppConfig.mashupURLOverride = 'http://vaodev.stsci.edu/portal/Mashup/Mashup.asmx/invoke';
    if (window.location.protocol === 'file:') {
        //The html was loaded from a local file, so we will assume the developer wants to use a remote server.
        AppConfig.mashupURLOverride = 'http://mastdev.stsci.edu/portal/Mashup/Mashup.asmx/invoke';
    }

    localExtPath = "http://127.0.0.1:8080/extjs/";
    AppConfig.origExtPath = "http://vao.stsci.edu/portal/extjs/extjs-4.1.0/";

    if (AppConfig.loadLocal) {
        AppConfig.extPath = AppConfig.localExtPath;
    } else {
        AppConfig.extPath = AppConfig.origExtPath;
    }

    //AppConfig.fullSearchOnly = !(AppConfig.isDevelopment || AppConfig.isMast);
    AppConfig.fullSearchOnly = true;

    var filestr = window.location.href;
    AppConfig.cssBasePath = AppConfig.extPath + 'resources/';

    document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'resources/css/ext-all' + ((AppConfig.isDevelopment) ? '-debug': '') + '.css" />');
    document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'examples/shared/example.css" />');

    document.write('<script type="text/javascript" src="' + AppConfig.extPath + 'ext-all' + ((AppConfig.isDevelopment) ? '-dev' : '') + '.js"></script>');
    //document.write('<script type="text/javascript" src="' + AppConfig.extPath + 'ext-debug.js"></script>');
    //document.write('<script type="text/javascript" src="' + AppConfig.extPath + 'src/tip/QuickTipManager.js"></script>');
    document.write('<script type="text/javascript" src="' + AppConfig.initializationPath + 'Shared/InitializeLocal.js"></script>');

    AppConfig = AppConfig       // Chrome inexplicably needs this for it to be global
})();
