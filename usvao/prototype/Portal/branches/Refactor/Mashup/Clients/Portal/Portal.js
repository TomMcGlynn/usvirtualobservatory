/**
 * Documentation for MAST.Portal
 */

// This class needs to have the requires statements outside the Ext.define() because
// this class is loaded by the top-level html, and the internal requires statement
// seems to cause lazy initialization of the class.
Ext.require('Mvp.gui.PortalBorderContainer');
Ext.require('Mvp.search.SearchParams');
Ext.require('Mvp.search.SearchContext');
Ext.require('Mvp.context.Manager');
Ext.require('Vao.view.VaoTopBar');
Ext.require('Vao.Version');

Ext.define('Vao.Portal', {

    statics: {
        createAndRun: function (config) {
            var portal = Ext.create('Vao.Portal', config);
            portal.run();
        }
        
    },
    
    constructor: function(config) {
        Ext.apply(this, config);
        
        this.mgr = Ext.create('Mvp.context.Manager', {});
        this.mgr.addListener('storeupdated', this.resultLogger, this);
    },
    
    // Public methods
    
    // Private methods
    run: function() {
        this.mainPanel = Ext.create('Mvp.gui.PortalBorderContainer', {
            region: 'center',
			useAv: AppConfig.useAV,
			avRenderType: AppConfig.avRenderType
        });
		
        var sp = Mvp.search.SearchParams;
        var searchParams = [
            sp.getSearch('CSV'),
            sp.getSearch('DataScopeVao'),
            sp.getSearch('DataScope'),
            sp.getSearch('SANTA'),
            sp.getSearch('SedAvailability'),
            sp.getSearch('SedRetrieval'),
			sp.getSearch('VOTable')
        ];
        var searchPanel = Ext.create('Vao.view.VaoTopBar', {
            searchParams: searchParams,
            defaultSearch: 'DataScopeVao',
            versionString: Vao.Version.versionString()
        });
		searchPanel.addListener('newsearch', this.searchCallback, this);
        
        // Create the container Viewport.
        this.viewport = Ext.create('Ext.container.Viewport', {
            renderTo: this.mainDiv,
            margin: 0,
            layout: 'border',
            items: this.mainPanel
        });

        this.mainPanel.getNorth().add(searchPanel);
		
		
        this.resultPanel = this.mainPanel.getResultPanel();
		this.resultPanel.on('tabchange', this.onTabChange, this);
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
            closable: true
        });
        var view = Ext.create(viewClass, viewConfig);
        this.resultPanel.add(view);
        this.resultPanel.setActiveTab(view);
		view.on('beforeclose', searchContext.onBeforeClose, searchContext);
        if (Ext.isFunction(view.setStarted)) {
            view.setStarted();
        }
		
        
        if (searchParams.resolve) {
            // TBD:  Update GUI to show name resolver in progress if necessary.
            // This could just be handled by the above views.
        }
        
        this.mgr.addContext(searchContext);
        
        searchContext.start();

    },
    
    resolveCallback: function(nameResolverStore) {
        // Now we know the resolver is complete.
        
        //  TBD: Update the GUI if necessary.
    },
    
    resultLogger: function(result) {
        Ext.log('result received');
    },
	
	onTabChange: function(tabPanel, newCard, oldCard, eOpts) {
		if (oldCard) {
			var oldContext = oldCard.controller;
			if (oldContext) oldContext.deactivate();
		}
		var newContext = newCard.controller;
		if (newContext) newContext.activate();
	}
    
});