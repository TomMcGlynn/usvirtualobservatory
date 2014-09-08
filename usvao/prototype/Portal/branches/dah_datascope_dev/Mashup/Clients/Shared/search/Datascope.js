Ext.define('Mvp.search.Datascope', {
    requires: [
                'Mvp.search.SearchContext',
                // Really bad, but this causes mutual dependencies:  'Mvp.search.SearchParams'
                ],
    statics: {
        
		// To use the default mashup server (the same server the summary request goes to) for a request,
		// set invokeUrl to null.
		// Alternate mashup URLs are:
		// mast dev:  http://mastdev.stsci.edu/portal/Mashup/Mashup.asmx/invoke
		// mast test:  http://masttest.stsci.edu/portal/Mashup/Mashup.asmx/invoke
		// mast production:  http://mast.stsci.edu/portal/Mashup/Mashup.asmx/invoke
		
		
         
        generateSummaryDataRow: function(resourceDesc, searchInput, coneSearchParams) {
            var row = Ext.clone(resourceDesc);
			var params = Ext.clone(coneSearchParams);
			var input = Ext.clone(searchInput);
			
			// Add extra params to the params object.
			var extraParams = row.extraParams;
			delete row.extraParams;
			Ext.apply(params, extraParams);
			
			// Some search types have a specialized parameter function that require special
			// input set up.  Apply those special inputs here:
			var extraInput = row.extraInput;
			delete row.extraInput;
			Ext.apply(input, extraInput);
            
            var searchParams = Mvp.search.SearchParams.getSearch(resourceDesc.serviceId);
            row.request = Mvp.search.SearchContext.createMashupRequestObject(searchParams, input, params);
            
            return row;
        },
		
/*
        invokeMashupQuery: function(context, serviceId, invokeBaseUrl, requestJson) {
			var searchParams = Mvp.search.SearchParams.getSearch(serviceId);
			var request  = Ext.decode(requestJson);
			var searchInput = {
				inputText: request.params.input,
				
				//  TSD:  This is the worst hack yet.  Refactor me please!!
				mission: request.params.mission
			}
			
			// By placing request in the search params, we tell the new context not to generate a new request,
			// but to use this one.
			searchParams.request = request;
			
			// Ensure that the new context will have the same location as the old one.
			searchParams.position = context.position;
			
			// Use the proper base URL.
			if (!searchParams.ajaxParams) {
				searchParams.ajaxParams = {};
			}
			searchParams.ajaxParams.url = invokeBaseUrl;
			
			// Invoke the search.
			context.invokeSearch(searchInput, searchParams);
			
		},
*/
		
		invokeDatascopeQuery: function(context, inputText, resourceList) {
			var searchInput = {
				inputText: inputText,
				resourceList: resourceList
			};
			var searchParams = Mvp.search.SearchParams.getSearch('DataScopeListable');
			
			// Invoke the search.
			context.invokeSearch(searchInput, searchParams);
		}
    }
    
});