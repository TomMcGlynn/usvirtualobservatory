Ext.define('DemoApp.Version', {
    statics: {
        version: '1',
        majorRev: '2',
        minorRev: '',  // Optional.  Only used for updates to a major release.
        status: '',   // Optional.  Should be 'b' for beta, and 'a' for alpha releases.
        svnRevision: 'UNKNOWN',
        
        /**
         * Generates a version string of the form:
         *    version.majorRev[.minorRev][status] (svnRevision)
         *
         * For example:
         *    1.0  (1234)
         *    1.1b  (2000)
         *    1.1.3  (6578)
        */
        versionString: function() {
            var versionString =
                DemoApp.Version.version +
                '.' + DemoApp.Version.majorRev +
                ((DemoApp.Version.minorRev == '') ? '' : '.') + DemoApp.Version.minorRev +
                DemoApp.Version.status +
                ((DemoApp.Version.svnRevision != 'UNKNOWN') ? (' (' + DemoApp.Version.svnRevision + ')') : '');
                
            return versionString;
        }
    }
})