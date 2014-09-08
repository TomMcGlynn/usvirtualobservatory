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

    var getUrlParam = function (param, url, default_) {
        if (default_==null) default_=""; 
        param = param.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regex = new RegExp("[\\?&]"+param+"=([^&#]*)");
        var qs = regex.exec(url);
        if(qs == null)
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
            initializationPath = scriptSrc.substring(0, scriptSrc.length - match[0].length);

            // Trim the trailing '/' off the initialization path because it causes a '//' in the class loading,
            // which fails on Windows.
            //initializationPath = initializationPath.substring(0, initializationPath.length - 1);

            break;
        }
    }

    pagesizeOverride = getUrlParam('pagesize', queryString, null);
    
    loadLocal = false;
    if (queryString.match('(\\?|&)loadLocal') !== null) {
        loadLocal = true;
    }

    isMast = false;
    if (queryString.match('(\\?|&)MAST') !== null) {
        isMast = true;
    }

    useAV = false;
    if (queryString.match('(\\?|&)useAV') !== null) {
        useAV = true;
    }

    useDesktop = false;
    if (window.location.href.match('desktop.html') !== null) {
        useDesktop = true;
    }

    isDevelopment = null;
    if (queryString.match('(\\?|&)debug') !== null) {
        isDevelopment = true;
    }
    else if (queryString.match('(\\?|&)nodebug') !== null) {
        isDevelopment = false;
    }

    isLocal = false;
    if (isDevelopment === null) {
        for (i = 0, ln = localhostTests.length; i < ln; i++) {
            test = localhostTests[i];

            if (host.search(test) !== -1) {
                isLocal = true;
                isDevelopment = true;
                break;
            }
        }
    }

    if (isDevelopment === null && window.location.protocol === 'file:') {
        isDevelopment = true;
    }
    //isDevelopment = false;

    // Enable the use of a remote server during client development.
 //   mashupDevelopmentURLOverride = 'http://vaodev.stsci.edu/portal/Mashup/Mashup.asmx/invoke';
    mashupURLOverride = undefined;
    if (window.location.protocol === 'file:') {
        //The html was loaded from a local file, so we will assume the developer wants to use a remote server.
        mashupURLOverride = mashupDevelopmentURLOverride;
    }

//    origExtPath = "http://vao.stsci.edu/portal/extjs/ext-4.0.7-gpl/";
    origExtPath = "http://vao.stsci.edu/portal/extjs/4.0.2/";
    localExtPath = "http://127.0.0.1:8080/ext-4.0.2/";
//    localExtPath = "http://127.0.0.1:8080/ext-4.0.7-gpl/";
//    localExtPath = "http://127.0.0.1:8080/ext-4.1-pr1/";

    if (loadLocal) {
        extPath = localExtPath;
    } else {
        extPath = origExtPath;
    }

    fullSearchOnly = !(isDevelopment || isMast);

    var filestr = window.location.href;
    var cssBasePath = extPath + 'resources/';
    //var cssBasePath = '';

    if (isMast || (!filestr.match("DemoStyled") && !filestr.match("Portal"))) {
        document.write('<link rel="stylesheet" type="text/css" href="' + extPath + 'resources/css/ext-all.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + extPath + 'examples/shared/example.css" />');
    } else {
        document.write('<link rel="stylesheet" type="text/css" href="' + cssBasePath + 'css-vao/ext-all_vao4.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + cssBasePath + 'css-vao/xtheme-vao4.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + cssBasePath + 'css-vao/xtheme-vao4_ie6.css" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + cssBasePath + 'css-vao/layout.css" />');
    }

    document.write('<script type="text/javascript" src="' + extPath + 'ext-all' + ((isDevelopment) ? '-dev' : '') + '.js"></script>');
    document.write('<script type="text/javascript" src="' + initializationPath + 'Shared/InitializeLocal.js"></script>');

})();
