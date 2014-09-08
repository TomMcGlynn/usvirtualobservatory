
Ext.define('Mast.Bootstrap', {

	requires: [
		'Mvp.util.ExtBugWorkarounds',
		'Ext.tip.QuickTipManager',
		'Mast.App'
	],

	constructor: function(config) {
		var bootstrapTime = new Date().getTime();
		if (console && Ext.isFunction(console.log)) {
			console.log('Bootstrapping portal, time = ' + (bootstrapTime - window.STSCI_startTime));
		}
		
		var lib = Mvp.util.ExtBugWorkarounds;
			
		lib.filterBug_4_1_0();
		lib.noGridScroller();
		lib.enableJsonNaN();

		// Initialize quick tips.
		Ext.tip.QuickTipManager.init();

        var myDesktopApp = new Mast.App();
	}

	},
	function() {
		// This is a callback that will be called when the Ext.define is complete.
	}
);