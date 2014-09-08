Ext.define('Mvp.util.Version', {
    statics: {
        version: '1',
        majorRev: '5',
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
                Mvp.util.Version.version +
                '.' + Mvp.util.Version.majorRev +
                ((Mvp.util.Version.minorRev == '') ? '' : '.') + Mvp.util.Version.minorRev +
                Mvp.util.Version.status +
                ((Mvp.util.Version.svnRevision != 'UNKNOWN') ? (' (' + Mvp.util.Version.svnRevision + ')') : '');
                
            return versionString;
        }
    }
})