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
    
    // Check for mobile.
    AppConfig.mobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

    // Define a function for retrieving parameters from a URL.  The 3rd argument is
    // the default value to use if the parameter is not found in the URL.
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

    // Initialize local variables.
    var scripts = document.getElementsByTagName('script'),  // A list of all the script tags in the html file.
        localhostTests = [
            /^localhost$/,
            /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:\d{1,5})?\b/ // IP v4
        ],
        host = window.location.hostname,
        queryString = window.location.search,
        test, path, i, ln, scriptSrc, match;

    // Initialize AppConfig.initializationPath to be parent directory of all the JS modules.  Currently
    // this is the .../Clients directory.  Do this by finding the path to InitializeJavaScript.js, and
    // extracting the first part of it.
    for (i = 0, ln = scripts.length; i < ln; i++) {
        scriptSrc = scripts[i].src;
        match = scriptSrc.match(/Shared[\/\\]InitializeJavaScript\.js$/);
        if (match) {
            AppConfig.initializationPath = scriptSrc.substring(0, scriptSrc.length - match[0].length);
            break;
        }
    }

    /////////////////////////
    // Development-only parameters...
    
    // pagesizeOverride overrides the default download page size
    AppConfig.pagesizeOverride = getUrlParam('pagesize', queryString, null);

    /////////////////////////
    
    // isMast is only used by InitializeLocal.js to determine which code namespace to use.
    AppConfig.isMast = false;
    if ((window.location.pathname.match('Clients/Mast') !== null) ||
        (window.location.pathname.match('Clients/dt') !== null)) {
        AppConfig.isMast = true;
    }

    // useAV tells the app whether or not to use AstroView.
    AppConfig.useAV = true;
    if (queryString.match('(\\?|&)noAV') !== null) {
        AppConfig.useAV = false;
    }

    // isBuild is only used when bundling the JavaScript for deployment.  It tells this file
    // not to load any bundles, the loader can figure out all the dependencies.
    AppConfig.isBuild = false;
    if (queryString.match('(\\?|&)isBuild') !== null) {
        AppConfig.isBuild = true;
    }

    AppConfig.useCloud = false;
    if (queryString.match('(\\?|&)useCloud') !== null) {
        AppConfig.useCloud = true;
    }

    // avPlacement tells the app where to put the AstroView display.  Legal values are:
    // north, south, east, west and tab.
    AppConfig.avPlacement = getUrlParam('avPlacement', queryString, (AppConfig.mobile ? 'tab' : 'east')).toLowerCase();
    if ((AppConfig.avPlacement != 'south') && (AppConfig.avPlacement != 'west') && (AppConfig.avPlacement != 'north') && (AppConfig.avPlacement != 'tab')) AppConfig.avPlacement = 'east';

    // avRenderType tells the app how to render AstroView.  Legal values are:
    // flash, canvas and webgl.
    AppConfig.avRenderType = getUrlParam('avRenderType', queryString, 'canvas').toLowerCase();
    
    // avSurveyType tells the app which survey to use.  Legal values are:
    // DSS, GALEX, SDSS (others found in AstroView/Surveys.js).
    AppConfig.avSurveyType = getUrlParam('avSurveyType', queryString, 'DSS');

    // startPage tells the app the URL for content to put in the first tab.
    AppConfig.startPage = getUrlParam('startPage', queryString, null);

    // isLocal tells us whether we're running off the local computer, either through
    // a local web server or via the file: protocol.
    AppConfig.isLocal = false;
    for (i = 0, ln = localhostTests.length; i < ln; i++) {
        test = localhostTests[i];

        if (host.search(test) !== -1) {
            AppConfig.isLocal = true;
            break;
        }
    }
    if (!AppConfig.isLocal && (window.location.protocol === 'file:')) {
        AppConfig.isLocal = true;
    }

    // allChoices tells us whether to offer all search choices.  isDebug also
    // offers this, but has other affects like debug info in AstroView.
    AppConfig.allChoices = false;
    if (queryString.match(/(\\?|&)allChoices/i)) {
        AppConfig.allChoices = true;
    }

    // isDevelopment tells us whether to run in development mode.  Development
    // mode implies debug mode, and may affect how we load the JavaScript.
    AppConfig.isDevelopment = false;
    if (AppConfig.isLocal || (queryString.match('(\\?|&)[dD]evelopment'))) {
        AppConfig.isDevelopment = true;
    }
    if (queryString.match('(\\?|&)no[dD]evelopment') !== null) {
        AppConfig.isDevelopment = false;
    }

    // isDebug tells the app whether to run in debug mode.
    AppConfig.isDebug = AppConfig.isDevelopment;
    if (queryString.match('(\\?|&)[dD]ebug') !== null) {
        AppConfig.isDebug = true;
    }
    if (queryString.match('(\\?|&)no[dD]ebug') !== null) {
        AppConfig.isDebug = false;
    }
    AppConfig.skipCache = 'NO';
    if (queryString.match('(\\?|&)skip[cC]ache') !== null) {
        AppConfig.skipCache = 'YES';
    }

    // Make sure the trim() function exists for strings.
    if (typeof (String.prototype.trim) === "undefined") {
        String.prototype.trim = function () {
            return String(this).replace(/^\s+|\s+$/g, '');
        };
    }

    // Enable the use of a remote server during client development.  (This was not working last time I checked. -TSD)
    AppConfig.mashupURLOverride = undefined;
    if (window.location.protocol === 'file:') {
        //The html was loaded from a local file, so we will assume the developer wants to use a remote server.
        AppConfig.mashupURLOverride = 'http://vaodev.stsci.edu/portal/Mashup/Mashup.asmx/invoke';
    }

    // extPath determines from what directory we will load ExtJS.
    var extDirName = "/extjs/extjs-4.1.0/";  // Can control which version of ExtJS to use
    AppConfig.defaultExtPath = "http://vaotest.stsci.edu" + extDirName;
    
    if (AppConfig.isBuild) 
    {
        // We are building the application: Load extjs using the Local File System, NOT a web server.
        // In order to get relative paths into the jsb file during build time, the
        // Ext JS files must exist at a relative path.
        AppConfig.extPath = "../../../../" + extDirName;
    } 
    else if (!AppConfig.isLocal)  // (NOTE: isLocal indicates local development machine)
    {
        // We are running on Dev, Test or Ops web site: so load extjs from the root of our Current web server
        AppConfig.extPath = extDirName;
    }
    else
    {
        // We are running on our local development machine: load extjs from the remote Test web server
        AppConfig.extPath = AppConfig.defaultExtPath;
    }
    
    // Decide which Ext and App code to load.  These versions are possible:
    // extLoad:
    // 1) Just Ext main class
    //      a. ext.js (minified)
    //      b. ext-debug.js
    //      c. ext-debug-w-comments.js
    //      d. ext-dev.js  (has Ext.log)
    // 2) All Ext classes
    //      a. ext-all.js  (minified)
    //      b. ext-all-debug.js
    //      c. ext-all-debug-w-comments.js
    //      d. ext-all-dev.js  (has Ext.log)
    //
    // appLoad:
    // 1) Automatically by the Loader
    // 2) Bundled  (bundle includes necessary Ext classes)
    //      a. app-all.js  (minified)
    //      b. all-classes.js
    //
    // We can run in the following modes:
    //    Build, development, debug, normal
    
    // Defaults
    if (AppConfig.isBuild) {
        // Be careful changing this one since you could break the build process.
        AppConfig.extLoad = '1a';
        AppConfig.appLoad = '1';
    } else if (AppConfig.isDevelopment) {
        AppConfig.extLoad = '2d';
        AppConfig.appLoad = '1';
    } else if (AppConfig.isDebug) {
        AppConfig.extLoad = '2d';
        AppConfig.appLoad = '1';
    } else {
        AppConfig.extLoad = '2a';
        AppConfig.appLoad = '1';
    }
    
    // Overrides for extLoad and appLoad.  The possiblities are expressed in the
    // outline above.  The legal values for extLoad are 1a, 1b, 1c, 1d, 2a, 2b, 2c and 2d.
    // The values for appLoad are 1, 2a and 2b.
    AppConfig.extLoad = getUrlParam('extLoad', queryString, AppConfig.extLoad);
    AppConfig.appLoad = getUrlParam('appLoad', queryString, AppConfig.appLoad);
    
    // Point to the right files.
    switch (AppConfig.extLoad) {
        case '1a': AppConfig.extLoadFile = AppConfig.extPath + 'ext.js'; break;
        case '1b': AppConfig.extLoadFile = AppConfig.extPath + 'ext-debug.js'; break;
        case '1c': AppConfig.extLoadFile = AppConfig.extPath + 'ext-debug-w-comments.js'; break;
        case '1d': AppConfig.extLoadFile = AppConfig.extPath + 'ext-dev.js'; break;
        case '2a': AppConfig.extLoadFile = AppConfig.extPath + 'ext-all.js'; break;
        case '2b': AppConfig.extLoadFile = AppConfig.extPath + 'ext-all-debug.js'; break;
        case '2c': AppConfig.extLoadFile = AppConfig.extPath + 'ext-all-debug-w-comments.js'; break;
        case '2d': AppConfig.extLoadFile = AppConfig.extPath + 'ext-all-dev.js'; break;
        default: AppConfig.extLoadFile = AppConfig.extPath + 'ext-all-debug.js';
    }
    switch (AppConfig.appLoad) {
        case '1': AppConfig.appLoadFile = null; break;
        case '2a': AppConfig.appLoadFile = 'app-all.js'; break;
        case '2b': AppConfig.appLoadFile = 'all-classes.js'; break;
        default: AppConfig.appLoadFile = null;
    }
    

    // Include the document elements that make the css stuff load.
    document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'resources/css/ext-all' + ((AppConfig.isDebug) ? '-debug' : '') + '.css" />');
    document.write('<link rel="stylesheet" type="text/css" href="' + AppConfig.extPath + 'examples/shared/example.css" />');

    // Include the document elements that make the JavaScript load.
    document.write('<script type="text/javascript" src="' + AppConfig.extLoadFile + '"></script>');
    
    document.write('<script type="text/javascript" src="' + AppConfig.initializationPath + 'Shared/InitializeLocal.js"></script>');
    document.write('<script type="text/javascript" src="Bootstrap.js"></script>');

    //document.write('<script type="text/javascript" src="../Shared/detectMobile.js"></script>');
    // If we're generating app-all and all-classes for a build, then we shouldn't load them.
    if (AppConfig.appLoadFile) {
        document.write('<script type="text/javascript" src="' + AppConfig.appLoadFile + '"></script>');
        document.write('<script type="text/javascript" src="../Shared/charts/jquery.charts.min.js"></script>');     // jquery.min + jqplot.min
        document.write('<script type="text/javascript" src="../Shared/charts/jqplot.plugins.min.js"></script>');    // jqplot plugins concatenated
        if (AppConfig.useCloud) {   // load minified cloud software
            document.write('<script type="text/javascript" src="../Shared/gui/textcloud/textcloud.min.js"></script>');
        }
    } else {
        // Files needed for charting loaded individually
        var path = '../Shared/charts';
        document.write('<script class="include" type="text/javascript" src="' + path + '/jquery' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/jquery.jqplot' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.cursor' + (AppConfig.isDebug ? '' : '.min.stsci') + '.js"></script>'); // since STSCI changed this source we have to manually minify the source
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.highlighter' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.barRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.dateAxisRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.logAxisRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.canvasTextRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.canvasAxisLabelRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');
        document.write('<script class="include" type="text/javascript" src="' + path + '/plugins/jqplot.canvasAxisTickRenderer' + (AppConfig.isDebug ? '' : '.min') + '.js"></script>');

        if (AppConfig.useCloud) {   // load full cloud software
            document.write('<script type="text/javascript" src="../Shared/gui/textcloud/canvasport-v0.1.js"></script>');
            document.write('<script type="text/javascript" src="../Shared/gui/textcloud/nebulos.js"></script>');
        }
    }
    AppConfig = AppConfig       // Chrome inexplicably needs this for it to be global
})();