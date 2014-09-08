Ext.define('Mvp.search.SearchParams', {
	requires: [
			   'Mvp.search.receivers.SantaXmlReceiver',
			   'Mvp.search.receivers.ExtjsReceiver',
			   'Mvp.gui.GridView',
			   'Mvp.gui.FacetedGridView',
			   'Mvp.gui.custom.DataScope',
			   'Mvp.gui.custom.Caom'
			   ],
    statics: {
		searchParamArray: null,
		
		/**
		 * Note:  It's important that each uid is unique.
		 */
		initSearches: function() {
			var c = Mvp.search.SearchParams;
			if (!c.searchIndex) {
				var allSearches = [
					{
						uid: 'SANTA', text: 'Name Resolver (SANTA)', hint: 'Enter object name or RA and Dec',
						resolve: false, inputType:  'positionOneBox',
						titlePrefix: 'Name Resolver: ',
						service: 'Mast.Name.Lookup', serviceParamFn: c.santaParams,
						result: {format: 'xml', type: 'santa'}
					}, {
						uid: 'CAOM', text: 'All MAST Observations (CAOM)', hint: 'Enter object name or RA and Dec',
						resolve: true, inputType:  'positionOneBox',
						titlePrefix: 'CAOM: ',
						service: 'Caom.Cone.Votable', serviceParamFn: null,
						result: {format: 'extjs', type: 'caom'}
					}, {
						uid: 'DataScope', text: 'All Virtual Observatory Collections (DataScope)', hint: 'Enter object name or RA and Dec',
						resolve: true, inputType:  'positionOneBox',
						titlePrefix: '',
						service: 'Vo.Hesarc.Datascope', serviceParamFn: c.dataScopeParams,
						result: {format: 'extjs', type: 'datascope', pagesize: 1000000}
					}
				];
				
				// Searches will be accessed via their uid, so create this index.
				c.searchIndex = {};
				for (var i=0; i<allSearches.length; i++) {
					c.searchIndex[allSearches[i].uid] = allSearches[i];
				}
			}
		},
		
		getReceiverType: function(resultDesc) {
			var receiverType = 'Mvp.search.receivers.ExtjsReceiver';
			if (resultDesc.format == 'xml') {
				receiverType = 'Mvp.search.receivers.SantaXmlReceiver';
			}
			return receiverType;
		},
		
		resultTypes: {
			santa: {
				defaultView: {
					type: 'Mvp.gui.GridView',
					config: {
						contentType: 'catalog'
					}
				}
			},
			caom: {
				defaultView: {
					type: 'Mvp.gui.custom.Caom',
					config: {
						contentType: 'image'
					}
				},
				storePageSize: 50,
				columnsconfigid: 'Mast.Caom.Cone'
			},
			datascope: {
				defaultView: {
					type: 'Mvp.gui.custom.DataScope',
					config: {
						contentType: 'mixed'
					}
				},
				storePageSize: 50,
				facetValueExclude: [{
					column: 'categories', exclude: ['HTTP Request', 'Web Page']
				}]
			}
		},
		
		getSearch: function(searchUid) {
			var c = Mvp.search.SearchParams;
			c.initSearches();
			
			var search = c.searchIndex[searchUid];
			if (search) {
				search = Ext.clone(search);  // Return a copy to protect the original definitions here.
			}
			return search;
		},
		
		createStore: function(searchParamArray) {
			
			// Now create the store.
			var searchParamStore = Ext.create('Ext.data.Store', {
				fields: ['uid', 'text', 'hint', 'resolve', 'inputType', 'resultType'],
				data: searchParamArray
			});
				
			return searchParamStore;
		},
		
		/**
		* @param {string} searchInput The search input will be a string unless
		* it is a resolved query.  Then it will be an object containing RA, Dec, radius and input.
		* */
	   santaParams: function(searchInput) {
		   var params = {};
		   params.input = searchInput;
		   return params;
	   },
	   
	   dataScopeParams: function(searchInput) {
			searchInput.skipcache = 'YES';  // Change to 'YES' to force the DS server to not use cache.
			return searchInput;
	   }

    }
	
});