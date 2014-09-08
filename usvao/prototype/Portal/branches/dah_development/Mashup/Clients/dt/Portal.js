/**
 * Documentation for MAST.Portal
 */

// This class needs to have the requires statements outside the Ext.define() because
// this class is loaded by the top-level html, and the internal requires statement
// seems to cause lazy initialization of the class.


Ext.define('Mast.Portal', {
    requires: ['Mast.view.MastTopBar', 'Ext.util.Cookies', 'Mvp.util.Version', 'Ext.ux.IFrame', 'Ext.layout.container.Border', 'Ext.container.Viewport', 'Mvp.search.SearchParams'],
    extend: 'Mvp.util.BasePortal',

    constructor: function(config) {
		this.callParent(arguments);
		this.viewCount = 0;
		
    },
    
    // Public methods
    
    // Private methods
    run: function() {
		
        var sp = Mvp.search.SearchParams;
        this.searchParams = [
			sp.getSearch('SUMMARY'),
            sp.getSearch('REGKEYWORD'),
            sp.getSearch('CAOMDB'),
            sp.getSearch('DataScope'),
            sp.getSearch('ADS'),
            sp.getSearch('MAST_MISSION'),
            sp.getSearch('IUE'),
            sp.getSearch('HSTSC'),
            //sp.getSearch('GSC23'),
            sp.getSearch('CAOM'),
            sp.getSearch('CSV'),
            sp.getSearch('GalexObjects'),
            sp.getSearch('GalexSdss'),
            sp.getSearch('GalexTiles'),
            sp.getSearch('GalexPhotonListNuv'),
            sp.getSearch('GalexPhotonListFuv'),
            sp.getSearch('HLA'),
            sp.getSearch('HLSP'),
            sp.getSearch('STPR'),
            sp.getSearch('HSTPR'),
            sp.getSearch('RP'),
            sp.getSearch('SANTA'),
            //sp.getSearch('SedDiscovery'), // service is broken as far as I can tell
            sp.getSearch('SedAvailability'),
            sp.getSearch('SedRetrieval'),
            sp.getSearch('SIDBYINST'),
            sp.getSearch('SIDBYJOB'),
            //sp.getSearch('SIDGETPACKAGE'),
            //sp.getSearch('SIDGETALLFILES'),
            sp.getSearch('STP'),
            sp.getSearch('LIT'),
			sp.getSearch('VOTable')
        ];
		
    },
	
	getSearchParams: function() {
		return this.searchParams;
	},
	
	searchCallback: function(searchInput, searchParams) {
		// searchInput *must* contain inputText.
		// It may optionally contain title and description, which will default to
		// inputText if not present.
        Ext.log('searchParams = ' + Ext.encode(searchParams));
        
        // TBD:  Create result view(s) (e.g., GridResultView, FilterResultView, AlbumView..)
        // These may need to know there destination containers or otherwise how
        // to add their contents to something.
        var searchContext = Ext.create('Mvp.search.SearchContext', {
            searchInput: searchInput,
            searchParams: searchParams,
			useAv: AppConfig.useAV
        });        
		searchContext.addListener('newsearch', this.searchCallback, this);
        
        var sp = Mvp.search.SearchParams;
        var resultType = sp.resultTypes[searchParams.result.type];
        var viewClass = resultType.defaultView.type;
        var viewConfig = Ext.clone(resultType.defaultView.config);
        var title = searchContext.getTitle();
		var description = searchContext.getDescription();
        Ext.apply(viewConfig, {
            title: title,
            description: description,
            controller: searchContext,
			preventHeader: true,
            closable: false
        });
        var view = Ext.create(viewClass, viewConfig);
		
		// Add view to desktop!
		this.addViewToDesktop(view, title, searchContext);
		
        view.on('beforeclose', this.onBeforeClose, this, { context: searchContext });
        if (Ext.isFunction(view.setStarted)) {
            view.setStarted();
        }
		
        
        if (searchParams.resolve) {
            // TBD:  Update GUI to show name resolver in progress if necessary.
            // This could just be handled by the above views.
        }
        
        this.app.addContext(searchContext);
        
        searchContext.start();

    },
	
	addViewToDesktop: function(view, title, searchContext) {
        var desktop = this.desktopApp.getDesktop();
		
		var el = desktop.el;
		var w = el.getWidth();
		var h = el.getHeight();
		var x = 100 + ((this.viewCount * 10) % 100);
		var width = w / 2 - 50;
		if (width < 650) width = 650;
		var y = 140 + ((this.viewCount * 20) % 100);
		height = h - 250;
		//Ext.log('x = ' + x + ', y = ' + y);
		//Ext.log('width = ' + width + ', height = ' + height);
		
		win = desktop.createWindow({
			id: 'result' + this.viewCount++,
			title:title,
			x: x,
			y: y,
			width: width,
			height: height,
			//iconCls: 'icon-grid',
			icon: view.icon,
			animCollapse:false,
			constrainHeader:true,
			layout: 'fit',
			items: [
				view
			]
		});
 		win.show();
		win.on('activate', this.onActivate, this, {context: searchContext});
		view.on('start', this.onStart, this, {window: win});
		view.on('complete', this.onComplete, this, {window: win});
	},
	
	onActivate: function(window, options) {
		Ext.log('onActivate()');
		if (options.context) {
			this.app.setActiveContext(options.context);
		}
	},
	
    onStart: function (view, options) {
		options.window.setIcon("../Shared/img/loading1.gif");
    },

    onComplete: function (view, options) {
        options.window.setIcon(view.icon);
    }
    

});