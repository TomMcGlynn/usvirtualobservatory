
Ext.define('Mvp.search.SearchContext', {
    extend: 'Mvp.context.Context',
    requires: [
        'Mvp.util.MashupQuery'
    ],

    statics: {
 
        createMashupRequestObject: function (searchParams, searchInput, coneSearchParams) {
            // Use the searchInput as the mashup params unless the search has a function
            // to compute the mashup params from the search input.
            var params = {
                input: searchInput.inputText
            };
    
            if (searchParams.serviceParamFn) {
                params = searchParams.serviceParamFn(searchInput, coneSearchParams);
            }
            
            var data = null;
            if (searchParams.serviceDataFn) {
                data = searchParams.serviceDataFn(searchInput, coneSearchParams);
            }
    
            var request = {
                service: searchParams.service,
                format: searchParams.result.format,
                pagesize: searchParams.result.pagesize ? searchParams.result.pagesize : 100,  // For overriding the default pagesize for chunking the results to the client.
                timeout: searchParams.timeout ? searchParams.timeout : 10,  //  For overriding how long the timeout the server will use before responding.
                data: data,
                params: params,
                clearcache: AppConfig.skipCache == 'YES'
            };
    
            // Allow the search params to specify a different columns config.
            // The server will also accept a full columns config object, but that's
            // not handled here.
            var resultType = Mvp.search.SearchParams.resultTypes[searchParams.result.type];
            if (resultType.columnsconfigid) {
                request.columnsconfigid = resultType.columnsconfigid;
            }
            
            return request;
        },
    
        createReceiver: function (searchParams) {
            var receiverType = Mvp.search.SearchParams.getReceiverType(searchParams.result);
            var receiver = Ext.create(receiverType, {
                searchParams: searchParams
            });
            
            return receiver;
        }        
        
    },

    /**
    * @cfg {Object} searchInput
    * An object containing all the user-input data.  This could be a simple string.
    */

    /**
    * @cfg {Object} searchParams
    * An object containing all the properties for configuring this search.
    */


    constructor: function (config) {
        this.callParent(arguments);
        this.addEvents('resolvercomplete');
        this.addEvents('newsearch');

        Ext.apply(this, config);

        // Apply default for title and description.
        var si = this.searchInput;
        si.title = (this.searchParams.titlePrefix || '') + (si.title ? si.title : si.inputText || "");
        si.description = (si.description ? si.description : si.inputText || "");

        // Accept a default location object, in the form of a coneSearchParams object.
        this.position = this.searchParams.position;

        this.cancelled = false;
        this.firstUpdate = true;
    },

    // Public methods
    start: function () {

        // The !this.searchParams.request clause skips normal processing is we already have a request.
        if (this.searchParams.resolve && !this.searchParams.request) {
            // The first query needs to be to the name resolver.
            var santaParams = Mvp.search.SearchParams.getSearch('SANTA');
            if (!this.searchInput.inputText.match(/r\s*=/i) && (AppConfig.mobile || jQuery.browser.mobile)) this.searchInput.inputText += ' r=.01';   // restrict default size for mobile browsers
            this.santaQuery = this.createMashupQuery(santaParams, this.searchInput, null, this.onResolverComplete);
            this.santaQuery.run();
        } else if (!this.searchParams.internal) {
            // Just create the Mashup query for this search.
            this.query = this.createMashupQuery(this.searchParams, this.searchInput, null, this.update);
            this.query.run();
        } else {
            // It was an internal request, so we don't create any mashup query.
        }


    },

    // Private methods

    // coneSearchParams is only used on the query following an automatically resolved query.
    createMashupQuery: function (searchParams, searchInput, coneSearchParams, updateCallback) {
        // If there's already a request attached to the searchParams (as with a followup to a summary result)
        // use that instead of creating a new one.
        var request = searchParams.request || Mvp.search.SearchContext.createMashupRequestObject(searchParams, searchInput, coneSearchParams);

        var receiver = Mvp.search.SearchContext.createReceiver(searchParams);
        receiver.addListener('storeupdated', updateCallback, this);

        // TBD:  This is just to get around the scope issues for now.
        // I think the callbacks should be consolidated, with error statuses
        // passed for those cases.
        var mashupQueryOptions = {
            request: request,
            onResponse: receiver.onResponse,
            onError: receiver.onError,
            onFailure: receiver.onFailure,
            scope: receiver,
            ajaxParams: searchParams.ajaxParams
        };
        var query = Ext.create('Mvp.util.MashupQuery', mashupQueryOptions);

        return query;
    },
    
    /**
    * Generic callback for handling resolver completion.
    */
    onResolverComplete: function (updateObject) {
        this.fireEvent('resolvercomplete', updateObject, this);
        var resolverStore = updateObject.store;
        var coneSearchParams = Mvp.util.NameResolverModel.getConeSearchParams(resolverStore.getAt(0));
        this.position = coneSearchParams;
        this.moveTo();

        // Create the Mashup query for this search.  For searches like this that
        // needed name resolution, the searchInput needs to be the cone search params
        // with an "input" property that is the original search input text.
        if (!this.cancelled && resolverStore.getCount()) {
            this.query = this.createMashupQuery(this.searchParams, this.searchInput, coneSearchParams,
                                                this.update);
            this.query.run();
        }

    },

    // Controller methods.

    moveTo: function() {
        if (this.position) {
            this.fireEvent('APP.context.position.changed', {
                type: 'APP.context.position.changed',
                context: this,
                position: Ext.clone(this.position)
            });
        }        
    },

    activate: function () {
        this.callParent(arguments);
        this.moveTo();
    },

    cancel: function () {
        this.callParent(arguments);
        this.cancelled = true;
        if (this.santaQuery) this.santaQuery.cancel();
        if (this.query) this.query.cancel();
    },

    update: function (updateObject) {
        if (!this.closing) {
            this.lastUpdateObject = updateObject;
            this.store = updateObject.store;
            if (this.searchParams.forceUpdate || (updateObject.complete && !updateObject.datascope) || this.firstUpdate) {
                if (!this.store) {
                    // The receiver didn't create the store so we'll do it here.
                    this.store = updateObject.createStore();
                    return;  // The totalcountchanged event firing from the store will trigger another update.
                }
            }
            this.firstUpdate = false;

            this.fireEvent('storeupdated', updateObject, this);
        }
    },

    reload: function () {
        this.store = this.lastUpdateObject.createStore();
        // The totalcountchanged event firing from the store will trigger another update.
        //this.lastUpdateObject.store = store;
        //this.update(this.lastUpdateObject);
    },

    getLastUpdateObject: function () {
        return this.lastUpdateObject;
    },

    invokeSearch: function (searchInput, searchParams) {
        this.fireEvent('newsearch', searchInput, searchParams);
    },

    getTitle: function () {
        return this.searchInput.title;
    },

    getDescription: function () {
        return this.searchInput.description;
    },

    getStore: function () {
        return this.store;
    },
    
    getPosition: function() {
        return this.position;
    }

});