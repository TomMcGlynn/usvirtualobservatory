Ext.define('Mast.Version', {
    statics: {
        version: '1',
        majorRev: '3',
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
                Mast.Version.version +
                '.' + Mast.Version.majorRev +
                ((Mast.Version.minorRev == '') ? '' : '.') + Mast.Version.minorRev +
                Mast.Version.status +
                ((Mast.Version.svnRevision != 'UNKNOWN') ? (' (' + Mast.Version.svnRevision + ')') : '');
                
            return versionString;
        }
    }
})