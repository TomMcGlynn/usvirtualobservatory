
Ext.define('Mast.Bootstrap', {
	requires: [
		'Mvp.util.ExtBugWorkarounds',
		'Ext.tip.QuickTipManager',
		'Mast.Portal'
	],

	constructor: function(config) {
		var bootstrapTime = new Date().getTime();
		if (console && Ext.isFunction(console.log)) {
			console.log('Bootstrapping portal, time = ' + (bootstrapTime - window.STSCI_startTime));
		}
		
		var lib = Mvp.util.ExtBugWorkarounds;
			
		lib.deletePageMapError();
		lib.selModelError();
		lib.abstractViewError();
		lib.allowHeaderCheckbox();
		//lib.refreshError();
		//lib.filterBug_4_1_0();
		//lib.noGridScroller();

		var portal = Ext.create('Mast.Portal', config);

		// Initialize quick tips.
		Ext.tip.QuickTipManager.init();
		delete Ext.tip.Tip.prototype.minWidth;  // this fixes a problem in Ext 4.2 where some calculated value doesn't get populated correctly

		portal.run();
	}

	},
	function() {
	    // This is a callback that will be called when the Ext.define is complete.
	}
);
