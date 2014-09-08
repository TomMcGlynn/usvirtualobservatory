Ext.define('Mvp.util.Exporter', {
    statics: {


        // TODO: remove the options here that are not relevant.
        // I think the required args are:
        //      filename
        //      filetype
        // with optional arguments of:
        //      data  - used when uploading data to export
        //      request - used when another request supplies the source of the download data
        activate: function(options) {
            var exporter = Ext.create('Mvp.util.Exporter', options);
            exporter.start();
        },
        
        download: function(url) {
	        Ext.log("download() url: " + url);
		        Ext.core.DomHelper.append(document.body, {
		                    tag: 'iframe',
		                    frameBorder: 0,
		                    width: 0,
		                    height: 0,
		                    css: 'display:none;visibility:hidden;height:1px;',
		                    src: url
		            });
		    },
		    
		downloadUrl: function(url, filename, attachment) {
			Ext.log("downloadUrl() url: " + url);
			
			// Init Optional Arguments
		    if (!filename)
		    {
		    	if (url.lastIndexOf('/') < url.lastIndexOf('.'))
		    	{
		    		filename = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.') + 4);
		    	}
		    	else
		    	{
		    		filename = "download.txt";
		    	}
		    }
		    if (!attachment) attachment= "true";
		    
		    // Create Mashup Download Request Object
			var request = {};
			request.service = "Mashup.Url.Download";
			request.params = {};
			request.params.url = url;
			request.params.filename = filename;
			request.params.attachment = attachment;
			
			// Create encoded Url from the Mashup Download Request, then invoke it
			var json = Ext.encode(request);
			var Url='../../Mashup.asmx/invoke?request=' + encodeURIComponent(json);      
		    Mvp.util.Exporter.download(Url);
		    }
    },
    
    constructor: function(config) {
        this.attachment = true;
        Ext.apply(this, config);
        if(this.responseFunction){
            this.onResponse = this.responseFunction;
        }
    },
    
    start: function() {
        
        this.exportRequest = {};
        var ajaxParams = null;
        if (this.request) {
            // This export will get it's data from the given query request, so include those request parameters in export request.
            Ext.apply(this.exportRequest, this.request);
        } else {
            // Build the request object for doing an export from our uploaded data.
            ajaxParams = {method: 'POST'};  // Uploading data requires a POST instead of the default 'GET'.
            this.exportRequest.service = 'Mashup.Table.Exporter';
            this.exportRequest.data = this.data;
        }
        
        this.exportRequest.filename = this.filename;
        this.exportRequest.filetype = this.filetype;
        this.exportRequest.filtercolumns = this.filtercolumns;
        this.exportRequest.attachment = this.attachment;
        
        // the export results are only one page of data, so make sure the pagesize is big enough to get everything.
	this.exportRequest.pagesize = 10000000;  
        
        this.query = Ext.create('Mvp.util.MashupQuery', {
            request: this.exportRequest,
            ajaxParams: ajaxParams,
            onResponse: this.onResponse,
            onError: this.onError,
            onFailure: this.onFailure,
            scope: this
        });
        this.query.run(false);
    },
    
    onResponse: function(responseObject, requestOptions, queryScope, complete, updated) {
        Ext.log('Exporter.onResponse: complete = ' + complete + ", updated = " + updated);
        if (complete) {
            // Get the URL and start the download.
            this.complete = complete;
            var data = responseObject.data;
            if (data) {
                if (this.attachment) {
                    Ext.log('Opening as an attachment: ' + data.url);
                    Mvp.util.Exporter.download(data.url);
                } else {
                    Ext.log('Opening in a new Window: ' + data.url);
                    window.open(data.url);
                }
            }
        } else if (updated) {
            // Um, nothing to do, I guess
        } else {
            // Also nothing to do.  Just wait for a complete response.
        }
    },
    
    onError: function(responseObject, requestOptions, queryScope, complete) {
        Ext.log('Exporter.onError() called');
    },

    onFailure: function(responseObject, requestOptions, queryScope) {
        Ext.log('Exporter.onFailure() called');
    }
});
    
