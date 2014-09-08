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

function globalizeAppConfig() {
    initializationPath = AppConfig.initializationPath;
    pagesizeOverride = AppConfig.pagesizeOverride;
    loadLocal = AppConfig.loadLocal;
    isMast = AppConfig.isMast;
    useAV = AppConfig.useAV;
    avPlacement = AppConfig.avPlacement;
    useDesktop = AppConfig.useDesktop;
    isDevelopment = AppConfig.isDevelopment;
    isLocal = AppConfig.isLocal;
    mashupURLOverride = AppConfig.mashupURLOverride;
    origExtPath = AppConfig.origExtPath;
    localExtPath = AppConfig.localExtPath;
    fullSearchOnly = AppConfig.fullSearchOnly;
    extPath = AppConfig.extPath;
    cssBasePath = AppConfig.cssBasePath;
    AppConfig = AppConfig       // Chrome inexplicably needs this for it to be global

}

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
    if (queryString.match('(\\?|&)MAST') !== null) {
        AppConfig.isMast = true;
    }

    AppConfig.useAV = false;
    if (queryString.match('(\\?|&)useAV') !== null) {
        AppConfig.useAV = true;
    }

    AppConfig.avPlacement = getUrlParam('avPlacement', queryString, 'east').toLowerCase();
    if (AppConfig.avPlacement == 'w') AppConfig.avPlacement = 'west';
    if (AppConfig.avPlacement == 'n') AppConfig.avPlacement = 'north';
    if (AppConfig.avPlacement == 's') AppConfig.avPlacement = 'south';
    if ((AppConfig.avPlacement != 'south') && (AppConfig.avPlacement != 'west') && (AppConfig.avPlacement != 'north')) AppConfig.avPlacement = 'east';

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

    AppConfig.extVersion = getUrlParam('extVersion', queryString);

    if (AppConfig.extVersion == '4.0.7') {
        AppConfig.origExtPath = "http://vao.stsci.edu/portal/extjs/ext-4.0.7-gpl/";
        AppConfig.localExtPath = "http://127.0.0.1:8080/ext-4.0.7-gpl/";
    } else if (AppConfig.extVersion == '4.1') {
        //localExtPath = "http://127.0.0.1:8080/ext-4.1-pr1/";
        AppConfig.origExtPath = "http://vao.stsci.edu/portal/extjs/ext-4.1.0-beta-2/";
        //AppConfig.origExtPath = "http://vao.stsci.edu/portal/extjs/ext-4.1-pr1/";
    }
    else {
        AppConfig.origExtPath = "http://vao.stsci.edu/portal/extjs/4.0.2/";
        AppConfig.localExtPath = "http://127.0.0.1:8080/4.0.2/";
        AppConfig.extVersion = '4.0.2';
    }

    if (AppConfig.loadLocal) {
        AppConfig.extPath = AppConfig.localExtPath;
    } else {
        AppConfig.extPath = AppConfig.origExtPath;
    }

    AppConfig.fullSearchOnly = !(AppConfig.isDevelopment || AppConfig.isMast);

    var filestr = window.location.href;
    AppConfig.cssBasePath = AppConfig.extPath + 'resources/';
    //var cssBasePath = '';

    if (AppConfig.isMast || (!filestr.match("DemoStyled") && !filestr.match("Portal"))) {
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'resources/css/ext-all.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'examples/shared/example.css" />');
    } else {
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.cssBasePath + 'css-vao/ext-all_vao4.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.cssBasePath + 'css-vao/xtheme-vao4.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.cssBasePath + 'css-vao/xtheme-vao4_ie6.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.cssBasePath + 'css-vao/layout.css" />');
    }

    document.write('<script type="text/javascript" src="' + AppConfig.extPath + 'ext-all' + ((AppConfig.isDevelopment) ? '-dev' : '') + '.js"></script>');
    document.write('<script type="text/javascript" src="' + AppConfig.initializationPath + 'Shared/InitializeLocal.js"></script>');

    globalizeAppConfig();
})();
