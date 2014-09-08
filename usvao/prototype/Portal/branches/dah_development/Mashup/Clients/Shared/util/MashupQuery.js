
Ext.define('Mvp.util.MashupQuery', {
    statics: {
        
    },
    
    tries: 0,
    cancelled: false,
    
    // Paging variables
    
    // Default the pagesize to 100, but allow it to be overridden by the global
    // variable pagesizeOverride.  The override's value will be ignored if it's
    // not a number from 10 to 10,000,000.
    pagesize: Ext.Number.constrain(Ext.Number.from(AppConfig.pagesizeOverride, 100), 10, 10000000),


    /**
     * This config should contain:
     *
     *  Mashup parameters
     *  request:  An object for specifying the Mashup parameters which are:
     *      service (required):  The Mashup service to access
     *      params (required):  An object containing service-specific parameters
     *      timeout (optional, defaults to 10):  The amount of time the service will wait before responding
     *          (ignored for xml format)
     *      format (optional, defaults to 'extjs'):  The desired response format
     *
     *  Callbacks
     *  onComplete(responseObject or responseXml, requestOptions, queryScope)
     *  onExecuting(responseObject, requestOptions, queryScope)
     *  onDataUpdate(responseObject, requestOptions, queryScope)
     *  onError(responseObject, requestOptions, queryScope)
     *  onFailure(response, requestOptions, queryScope
     *  scope: scope for the callbacks
     *      where
     *          responseObject is the decoded responseText from an extjs format query.
     *          responseXml is the xml returned from an xml format query.
     *          requestOptions is an object containing all the options given to the Ajax request.
     *          queryScope is the MashupQuery object.
     *          response is the full reponse object returned from the Ajax query
     *  
     *  Ajax parameters
     *  ajaxParams: {
     *      method: defaults to 'GET',
     *      url: defaults to '../../Mashup.asmx/invoke'
     *      }
     */
    constructor: function(config) {
        Ext.apply(this, config);
        
        // If the mashup url was specified externally, use that, otherwise use our local default.
        var url = '../../Mashup.asmx/invoke';
        if (Ext.isDefined(AppConfig.mashupURLOverride)) {
            url = AppConfig.mashupURLOverride;
        } else if (this.overrideURL) {
            url = this.overrideURL;
        }
        
        // Request defaults
        if (this.request) {
            if (Ext.isString(this.request)) {
                // The request is just a url, not a config for a mashup request.
                url = this.request;
            } else {
                // Add the default timeout and format to the request config.
                Ext.applyIf(this.request, {
                    timeout: 10,
                    format: 'extjs',
                    page: 1,
                    pagesize: this.pagesize
                });
            }
        }
        
        this.ajaxParams = this.ajaxParams || this.request.params.ajaxParams || {};
        Ext.applyIf(this.ajaxParams, {
            method: 'GET',
            url: url
        });
        
    },

    run: function(async) {
        if (this.request) {
            var requestOptions = { 
                callback: this.callback,
                scope: this,
                userScope: this.scope || this,
                numTries: ++this.tries
            };
            // Only add the Mashup parameters if this.request is more than a simple url.
            if (!Ext.isString(this.request)) {
                    Ext.apply(requestOptions, {
                        params: {request : Ext.encode(this.request)},
                        paramsObject: this.request
                    });
            }
            Ext.apply(requestOptions, this.ajaxParams);
            
            if (Ext.isDefined(async)) {
                Ext.apply(requestOptions, {async: async});
            }
            Ext.log('Ext.Ajax.timeout = ' + Ext.Ajax.timeout);
            Ext.Ajax.timeout = 30000;
            Ext.log('Ext.Ajax.newTO  = ' + Ext.Ajax.timeout);
            Ext.log('MashupQuery initial request, page ' + requestOptions.paramsObject.page + ', pagesize = ' + requestOptions.paramsObject.pagesize);
            Ext.Ajax.request(requestOptions);
        } else {
            Ext.log("No request parameters specified.");
        }
        
    },
    
    callback: function(requestOptions, success, response) {
        // The "!format" is a hack to allow us to handle a response when the request object was a simple
        // url.  This means that loading a simple url now only supports extjs format.  That's OK for now...
        var format = (requestOptions.paramsObject) ? requestOptions.paramsObject.format : 'extjs';
        var outsideScope = requestOptions.userScope;
        
        if (!this.cancelled) {
            if (success) {

                if (format == 'extjs') {
                    var responseObject = Ext.decode(response.responseText);
                    this.responseStatus = responseObject.status;
                    Ext.log('extjs status = ' + this.responseStatus);
                    
                    var complete = (this.responseStatus == 'COMPLETE');
                    var executing = (this.responseStatus == 'EXECUTING');

                    var args = [responseObject, requestOptions, this, complete];
                    if (complete || executing) {

                        // Some responses (like for export request) won't have tables, so have reasonable defaults.
                        var updated = false;
                        var nextPage = 1;
                        if (responseObject.data.Tables) {
                            var table = responseObject.data.Tables[0];
                            this.updatePendingTable(table);
                            updated = this.checkForUpdates(table);
                            nextPage = this.computeNextPageNumber(table);
                        }
                        var pagingExpectedRowCount = null;
                        if (nextPage > 1) {
                            pagingExpectedRowCount = table.ExtendedProperties.Paging.rowsFiltered;
                        }
                        args.push(updated);
                        args.push(pagingExpectedRowCount);
                        if (this.onResponse) {
                            
                            var rowCount = 0;
                            var reallyComplete = complete;
                            var pageInfo = {
                                page: 0,
                                pagesize: this.request.pagesize,
                                rowsFiltered: 0
                            };
                            if (this.pendingTable) {
                                rowCount = this.pendingTable.Rows.length;

                                var pep = this.pendingTable.ExtendedProperties.Paging;
                                reallyComplete = complete && (rowCount === pep.rowsFiltered);
                                pageInfo = {
                                    page: pep.lastPage,
                                    pagesize: this.request.pagesize,
                                    rowsFiltered: pep.rowsFiltered
                                };
                            }
                            var respObj = {
                                pageInfo: pageInfo,
                                table: this.pendingTable,
                                rowCount: rowCount,
                                complete: reallyComplete,
                                percent: responseObject.percentComplete,
                                updated: updated
                            };
                            args.push(respObj);
                            
                            // For resending on cancel.
                            this.outsideScope = outsideScope;
                            this.lastRespObj = respObj;
                            this.lastArgs = args;
                            
                            // Call the requestor's callback.
                            Ext.callback(this.onResponse, outsideScope, args);
                            
                            // Start the next query if necessary.  This is for an ongoing (executing)
                            // UWS query, or when we need multiple pages.
                            if (executing || pagingExpectedRowCount) {
                                Ext.apply(this.request, {
                                    page: nextPage
                                });
                                Ext.apply(requestOptions, {
                                    params: {request : Ext.encode(this.request)},
                                    paramsObject: this.request,
                                    numTries: ++this.tries
                                });
                                
                                Ext.log('MashupQuery follow-up request, page ' + requestOptions.paramsObject.page + ', pagesize = ' + requestOptions.paramsObject.pagesize);
                                Ext.Ajax.request(requestOptions);
                            }
                        }
                    } else if (this.responseStatus == 'ERROR') {
                        if (this.onError) {
                            Ext.callback(this.onError, outsideScope, args);
                        }
                    }
                    
                } else if (format == 'xml' ) {
                    this.responseStatus = 'COMPLETE';
                    Ext.log('xml query COMPLETE');
                    var args = [response.responseXML, requestOptions, this, true];
                    if (this.onResponse) {
                        Ext.callback(this.onResponse, outsideScope, args);
                    }
                }
            } else {
                this.responseStatus = 'FAILED';
                var args = [response, requestOptions, this];
                if (this.onFailed) {
                    Ext.callback(this.onFailure, outsideScope, args);
                }
            }
        }
    },
    
    cancel: function() {
        this.cancelled = true;

        var lr = this.lastRespObj;               
        if (lr && !lr.complete && lr.table) {
            lr.complete = true;
            lr.cancelled = true;
            Ext.callback(this.onResponse, this.outsideScope, this.lastArgs);
        }
    },
    
    updatePendingTable: function(table) {
        var tep = table.ExtendedProperties.Paging;
        
        if (!this.pendingTable) {
            // This is the first response for this pendingTable.
            this.setPendingTable(table);
            
        } else {
            // We already had some pending data.
                    
            // Is this table an update of the current page, or a new page?
            if (tep.page > this.pendingTable.ExtendedProperties.Paging.lastPage) {
                // We have a new page of data, so append the new data.
                this.appendToPendingTable(table);
            } else {
                // We have an update to the existing page or we're starting over at page 1, so replace the pendingTable.
                this.setPendingTable(table);
            }
        }
    },
    
    setPendingTable: function(table) {
        this.pendingTable = table; //Ext.clone(table);
        var pep = this.pendingTable.ExtendedProperties.Paging;
        pep.firstPage = pep.page;
        pep.lastPage = pep.page;
    },
    
    appendToPendingTable: function(table) {
        var tep = table.ExtendedProperties.Paging;
        var pep = this.pendingTable.ExtendedProperties.Paging;
        pep.lastPage = tep.page;
        this.pendingTable.Rows = this.pendingTable.Rows.concat(table.Rows);
        pep.rows = this.pendingTable.Rows.length;
    },
    
    checkForUpdates: function(table) {
        var updated = false;

        var pendingRowCnt = this.pendingTable.Rows.length;
        if (this.currentTable) {
            var currentRowCnt = this.currentTable.Rows.length;
            if (pendingRowCnt > currentRowCnt) {
                updated = true;
            }
        } else {
            // This is the first real data we've received from this query.
            updated = true;
            this.currentRowCnt = 0;
        }
            
        return updated;
    },
    
    computeNextPageNumber: function(table) {
        var nextPage = 1;
        if (this.pendingTable) {
            var pep = this.pendingTable.ExtendedProperties.Paging;
            var tep = table.ExtendedProperties.Paging;
            if (tep.pagesFiltered > pep.lastPage) {
                nextPage = pep.lastPage + 1;
            }
        }
        return nextPage;
    },
    
    checkForUpdatesOld: function(responseObject) {
        var dataset = responseObject.data;
        var updated = false;
        if (dataset.Tables) {
            
            this.pendingTable = dataset.Tables[0];
            var pendingRowCnt = this.pendingTable.Rows.length;
            if (this.currentTable) {
                var currentRowCnt = this.currentTable.Rows.length;
                if (pendingRowCnt > currentRowCnt) {
                    updated = true;
                }
            } else {
                // This is the first real data we've received from this query.
                updated = true;
                this.currentRowCnt = 0;
            }
        }
        return updated;
    },
    
    markTable: function() {
        // No need to save the whole current table.  The only thing we need to save is the current row count.
        this.currentTable = this.pendingTable;  //Ext.clone(this.pendingTable);
        this.currentRowCnt = this.currentTable.Rows.length;
        return this.currentTable;
    }
    
});

