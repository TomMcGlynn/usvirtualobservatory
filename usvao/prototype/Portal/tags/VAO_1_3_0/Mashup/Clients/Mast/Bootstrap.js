// This class needs to have the requires statements outside the Ext.define() because
// this class is loaded by the top-level html, and the internal requires statement
// seems to cause lazy initialization of the class.
Ext.require('Mast.Portal');
Ext.require('Mvp.util.ExtBugWorkarounds');
Ext.define('Mast.Bootstrap', {

    statics: {

        createAndRun: function (config) {
            var lib = Mvp.util.ExtBugWorkarounds;
			
			lib.filterBug_4_1_0();
			lib.noGridScroller();

            var portal = Ext.create('Mast.Portal', config);
            portal.run();
        }

    }
});