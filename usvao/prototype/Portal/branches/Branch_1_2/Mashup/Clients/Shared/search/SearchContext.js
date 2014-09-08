
Ext.define('Mvp.search.SearchContext', {
    extend: 'Mvp.context.Context',
    
    statics: {},
    
    /**
     * @cfg {Object} searchInput
     * An object containing all the user-input data.  This could be a simple string.
     */
    
    /**
     * @cfg {Object} searchParams
     * An object containing all the properties for configuring this search.
     */
    
    
    constructor: function(config) {
        this.callParent(arguments);
        this.addEvents('resolvercomplete');
        
        Ext.apply(this, config);
        
        this.cancelled = false;
        this.firstUpdate = true;
    },
    
    // Public methods
    start: function() {
        
        if (this.searchParams.resolve) {
            // The first query needs to be to the name resolver.
            var santaParams = Mvp.search.SearchParams.getSearch('SANTA');
            this.santaQuery = this.createMashupQuery(santaParams, this.searchInput, this.onResolverComplete);
            this.santaQuery.run();
        } else {
            // Just create the Mashup query for this search.
            this.query = this.createMashupQuery(this.searchParams, this.searchInput, this.update);
            this.query.run();
        }
        

    },
    
    // Private methods
    
    createMashupQuery: function(searchParams, searchInput, updateCallback) {
        var sp = searchParams;
        var a = arguments;
        
        // Use the searchInput as the mashup params unless the search has a function
        // to compute the mashup params from the search input.
        var params = searchInput;
        if (sp.serviceParamFn) {
            params = sp.serviceParamFn(searchInput);
        }
        
        var request = {
            service: sp.service,
            format: sp.result.format,
            pagesize: sp.result.pagesize,  // For overriding the default pagesize for chunking the results to the client.
            params: params
        };
        
        // Allow the search params to specify a different columns config.
        // The server will also accept a full columns config object, but that's
        // not handled here.
        var resultType = Mvp.search.SearchParams.resultTypes[sp.result.type];
        if (resultType.columnsconfigid) {
            request.columnsconfigid = resultType.columnsconfigid;
        }
        
        var receiverType = Mvp.search.SearchParams.getReceiverType(searchParams.result);
        var receiver = Ext.create(receiverType, {
            searchParams: sp
        });
        receiver.addListener('storeupdated', updateCallback, this);
        
        // TBD:  This is just to get around the scope issues for now.
        // I think the callbacks should be consolidated, with error statuses
        // passed for those cases.
        var mashupQueryOptions = {
            request: request,
            onResponse: receiver.onResponse,
            onError: receiver.onError,
            onFailure: receiver.onFailure,
            scope: receiver
        };
        var query = Ext.create('Mvp.util.MashupQuery', mashupQueryOptions);
        
        return query;
    },
    
    /**
     * Generic callback for handling resolver completion.
     */
    onResolverComplete: function(updateObject) {
        this.fireEvent('resolvercomplete', updateObject, this);
        var resolverStore = updateObject.store;
        var coneSearchParams = Mvp.util.NameResolverModel.getConeSearchParams(resolverStore.getAt(0));
        coneSearchParams.input = this.searchInput;
        
        // Create the Mashup query for this search.  For searches like this that
        // needed name resolution, the searchInput needs to be the cone search params
        // with an "input" property that is the original search input text.
        if (!this.cancelled) {
            this.query = this.createMashupQuery(this.searchParams, coneSearchParams, this.update);
            this.query.run();
        }
        
    },
    
    // Controller methods.
    
    cancel: function() {
        this.cancelled = true;
        if (this.santaQuery) this.santaQuery.cancel();
        if (this.query) this.query.cancel();
    },
    
    update: function(updateObject) {
        this.lastUpdateObject = updateObject;
        var store = updateObject.store;
        if ((updateObject.complete && !updateObject.datascope) || this.firstUpdate) {
            if (!store) {
                // The receiver didn't create the store so we'll do it here.
                store = updateObject.createStore();
                updateObject.store = store;
            }
        }
        this.firstUpdate = false;
        
        this.fireEvent('storeupdated', updateObject, this);
    },
    
    reload: function() {
        var store = this.lastUpdateObject.createStore();
        this.lastUpdateObject.store = store;
        this.update(this.lastUpdateObject);
    }
    
});