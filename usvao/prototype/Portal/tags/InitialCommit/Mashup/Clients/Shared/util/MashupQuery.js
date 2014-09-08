
Ext.define('Mvp.util.MashupQuery', {
    statics: {
        
    },
    
    tries: 0,
    
    cancelled: false,
    
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
        
        // Request defaults
        if (this.request) {
            Ext.applyIf(this.request, {
                timeout: 10,
                format: 'extjs'
            });
        }
        
        this.ajaxParams = this.ajaxParams || {};
        Ext.applyIf(this.ajaxParams, {
            method: 'GET',
            url: '../../Mashup.asmx/invoke'
        });
        
    },
    
    run: function() {
        if (this.request) {
            var requestOptions = { 
                params: {request : Ext.encode(this.request)},
                paramsObject: this.request,
                callback: this.callback,
                scope: this,
                userScope: this.scope || this,
                numTries: ++this.tries
            };
            Ext.apply(requestOptions, this.ajaxParams);
            
            Ext.Ajax.request(requestOptions);
        } else {
            config.log("No request parameters specified.");
        }
        
    },
    
    callback: function(requestOptions, success, response) {
        var format = requestOptions.paramsObject.format;
        var outsideScope = requestOptions.userScope;
        
        if (!this.cancelled) {
            if (success) {
                if (format == 'extjs') {
                    var responseObject = Ext.decode(response.responseText);
                    this.responseStatus = responseObject.status;
                    console.log('extjs status = ' + this.responseStatus);
                    
                    var complete = (this.responseStatus == 'COMPLETE');
                    var executing = (this.responseStatus == 'EXECUTING');
                    var args = [responseObject, requestOptions, this, complete];
                    if (complete || executing) {
                        var updated = this.checkForUpdates(responseObject);
                        args.push(updated);
                        if (this.onResponse) {
                            Ext.callback(this.onResponse, outsideScope, args);
                            if (!this.cancelled && executing) {
                                Ext.apply(requestOptions, {numTries: ++this.tries});
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
                    console.log('xml query COMPLETE');
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
    },
    
    checkForUpdates: function(responseObject) {
        var dataset = responseObject.data;
        var updated = false;
        if (dataset.Tables) {
            this.pendingTable = dataset.Tables[0];
            var pendingRowsCnt = this.pendingTable.Rows.length;
            if (this.currentTable) {
                var currentRowCnt = this.currentTable.Rows.length;
                if (pendingRowsCnt > currentRowCnt) {
                    updated = true;
                }
            } else {
                // This is the first real data we've received from this query.
                updated = true;
            }
        }
        return updated;
    },
    
    markTable: function() {
        this.currentTable = this.pendingTable;
        return this.currentTable;
    }
    
});

