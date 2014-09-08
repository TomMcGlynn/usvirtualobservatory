Ext.define('Mvp.search.SearchParams', {
    requires: [
			   'Mvp.search.receivers.SantaXmlReceiver',
			   'Mvp.search.receivers.ExtjsReceiver',
			   'Mvp.gui.GridView',
			   'Mvp.gui.FacetedGridView',
			   'Mvp.gui.custom.DataScope',
			   'Mvp.gui.custom.Caom',
               'Mvp.gui.custom.Siap',
               'Mvp.gui.custom.Stpr'
			   ],
    statics: {
        searchParamArray: null,

        /**
        * Note:  It's important that each uid is unique.
        */
        initSearches: function () {
            var c = Mvp.search.SearchParams;
            if (!c.searchIndex) {
                var allSearches = [
					{
					    uid: 'SANTA', text: 'Name Resolver (SANTA)', hint: 'Enter object name or RA and Dec',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Name Resolver: ',
					    service: 'Mast.Name.Lookup',
					    result: { format: 'xml', type: 'santa' }
					}, {
					    uid: 'CAOM', text: 'All MAST Observations (CAOM)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'CAOM: ',
					    service: 'Caom.Cone.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'DataScope', text: 'All Virtual Observatory Collections (DataScope)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Hesarc.Datascope', serviceParamFn: c.dataScopeParams,
					    result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
					}, {
					    uid: 'DataScopeVao', text: 'All Virtual Observatory Collections (DataScopeVao)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionThreeBox',
					    titlePrefix: '',
					    service: 'Vo.Hesarc.Datascope', serviceParamFn: c.dataScopeParams,
					    result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
					}, {
					    uid: 'Cone', text: 'Virtual Observatory Cone Search', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Cone', serviceParamFn: c.dataScopeDrilldownParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'Siap', text: 'Load SIAP VO Table', hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'siapTable' }
					}, {
					    uid: 'VOTable', text: 'Load VO Table', hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'STPR', text: 'Load STPR Table', //hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'ST Press Releases',
					    service: 'Mast.Stpr.Votable', serviceParamFn: null,
					    result: { format: 'extjs', type: 'stprTable' }
					}, {
					    uid: 'HSTPR', text: 'HST Press Releases (HubbleSite.org)', //hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'HST Press Releases',
					    service: 'Hst.PressRelease.Votable', serviceParamFn: null,
					    result: { format: 'extjs', type: 'hstprTable' }
					}, {
					    uid: 'STPRDrilldown', text: 'Load STPR Table', //hint: 'Enter URL for VO Table',
					    resolve: false, //inputType: 'searchButton',
					    titlePrefix: '',
					    service: 'Mast.KnownStpr.Votable', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'stprTable' }
					}, {
					    uid: 'SedDiscovery', text: 'NED SED Info Discovery', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Ned.SedInfoDiscovery.Votable', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'genericImageTable' }
					}, {
					    uid: 'SedAvailability', text: 'NED SED Info Availability', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Ned.SedInfoAvailability.Votable', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'genericImageTable' }
					}, {
					    uid: 'SedRetrieval', text: 'NED SED Data Retrieval', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Ned.SedDataRetrieval.Votable', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'genericImageTable' }
					}, {
					    uid: 'GalexObjects', text: 'Search Galex Objects', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Mast.Galex.Catalog', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'GalexTiles', text: 'Search Galex Tiles', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Mast.Galex.Tile', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'GalexSdss', text: 'Galex/SDSS', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Mast.Galex.Sdss.Catalog', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'HLA', text: 'Search HLA', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'HLA: ',
					    service: 'Hla.Hubble.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'genericTable' }
					},{
					    uid: 'ADS', text: 'ADS Search by Author (ADSAuthor)', hint: "Enter author's name",
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'ADS: ',
					    service: 'Ads.Author.Votable', serviceParamFn: c.adsParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'HLSP', text: 'HLSP Search',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'HLSP: ',
					    service: 'Mast.Hlsp.Project', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'hlspTable' }
					}, {
					    uid: 'SID', text: 'Sid Archive by Instrument (SID_I)', hint: 'Enter instrument name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SID: ',
					    service: 'Mast.Sid.Votable', serviceParamFn: c.sidParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'STP', text: 'Staff Papers (STP)', hint: 'Enter last name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'STP: ',
					    service: 'Mast.PaperTrack.Staff', serviceParamFn: c.stpParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 1000000 }
					}, {
					    uid: 'RP', text: 'Ranked Authors (RP)', hint: 'Enter anything',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'RP: ',
					    service: 'Mast.Hlsp.RankedAuthors',// serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 1000000 }
					}, {
					    uid: 'CSV', text: 'Load CSV Table', hint: 'Enter URL for CSV Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Csv.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}

				];

                // Searches will be accessed via their uid, so create this index.
                c.searchIndex = {};
                for (var i = 0; i < allSearches.length; i++) {
                    c.searchIndex[allSearches[i].uid] = allSearches[i];
                }
            }
        },

        getReceiverType: function (resultDesc) {
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
            genericTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'generic' }
                },
                storePageSize: 50
            },
            genericImageTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'image' }
                },
                storePageSize: 50
            },
            siapTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Siap',
                    config: { contentType: 'image' }
                },
                storePageSize: 50
            },
            stprTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Stpr',
                    config: { contentType: 'image' }
                },
                columnsconfigid: 'Mast.Stpr.Votable',
                storePageSize: 50
            },
            hstprTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Hstpr',
                    config: { contentType: 'image' }
                },
                storePageSize: 50,
                facetValueExclude: [{
                    column: 'UnnamedField', exclude: ['HTTP Request', 'Web Page']
                }]
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
            hlspTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 50,
                columnsconfigid: 'Mast.Hlsp.Votable'
            },
            datascope: {
                defaultView: {
                    type: 'Mvp.gui.custom.DataScope',
                    config: {
                        contentType: 'mixed',
						facetValueExclude: [{
							column: 'categories', exclude: ['HTTP Request', 'Web Page']
						}]
                    }
                },
                storePageSize: 50
            },
            catalog: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'catalog' }
                },
                storePageSize: 50
            }
        },

        getSearch: function (searchUid) {
            var c = Mvp.search.SearchParams;
            c.initSearches();

            var search = c.searchIndex[searchUid];
            if (search) {
                search = Ext.clone(search);  // Return a copy to protect the original definitions here.
            }
            return search;
        },

        createStore: function (searchParamArray) {

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

        coneParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            return params;
        },

        dataScopeParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            params.skipcache = 'NO';  // Change to 'YES' to force the DS server to not use cache.
            return params;
        },

        voTableParams: function (searchInput) {
            var params = {
                url: searchInput.inputText,
                input: searchInput.inputText
            };
            return params;
        },

        sidParams: function (searchInput) {
            var params = {
                inst: searchInput.inputText
            };
            return params;
        },

        dataScopeDrilldownParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            Ext.apply(params, {
                url: searchInput.url,
                input: searchInput.input
            });
            return params;
        },

        targetnameParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            params.targetname = searchInput.inputText;
            return params;
        },

        adsParams: function (searchInput) {
            var params = {
                input: searchInput.inputText,
                author: searchInput.inputText
            };
            return params;
        },

        stpParams: function (searchInput) {
            var params = {
                input: searchInput.inputText,
                lastname: searchInput.inputText
            };
            return params;
        }
    }

});