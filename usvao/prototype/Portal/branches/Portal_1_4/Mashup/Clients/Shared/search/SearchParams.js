Ext.define('Mvp.search.SearchParams', {
    requires: [
			   'Mvp.search.receivers.SantaXmlReceiver',
			   'Mvp.search.receivers.ExtjsReceiver',
			   'Mvp.gui.GridView',
			   'Mvp.gui.FacetedGridView',
			   'Mvp.gui.AstroGridView',
			   'Mvp.gui.custom.DataScope',
			   'Mvp.gui.custom.Caom',
               'Mvp.gui.custom.Siap',
               'Mvp.gui.custom.Ssap',
               'Mvp.gui.custom.Stpr',
               'Mvp.gui.custom.Hstpr',
               'Mvp.gui.custom.DownloadBasket',
               'Mvp.gui.custom.Hla',
			   'Mvp.util.Constants',
			   'Mvp.gui.IFrame',
               'Mvp.gui.custom.Keywords'
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
					    uid: 'SANTA', text: 'Name Resolver' + (AppConfig.isDebug ? ' (SANTA)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Name Resolver: ',
					    service: 'Mast.Name.Lookup',
					    result: { format: 'xml', type: 'santa' }
					}, {
					    uid: 'CAOM', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOM)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'CAOM: ', downloadEnabled: true,
					    service: 'Caom.Cone.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMDB', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOMDB)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'CAOMDB: ', downloadEnabled: true,
					    service: 'Mast.Caom.Cone', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'DataScope', text: 'All Virtual Observatory Collections' + (AppConfig.isDebug ? ' (Datascope)' : ''),
					    hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '', downloadEnabled: false,
					    service: 'Vo.Hesarc.Datascope', serviceParamFn: c.dataScopeParams,
					    result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
					}, {
					    uid: 'DataScopeVao', text: 'All Virtual Observatory Collections' + (AppConfig.isDebug ? ' (DatascopeVao)' : ''),
					    hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionThreeBox',
					    titlePrefix: '', downloadEnabled: false,
					    service: 'Vo.Hesarc.Datascope', serviceParamFn: c.dataScopeParams,
					    result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
					}, {
					    uid: 'Cone', text: 'Virtual Observatory Cone Search', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Cone', serviceParamFn: c.dataScopeDrilldownParams,
					    result: { format: 'extjs', type: 'genericTable' }
					},  {
					    uid: 'WhatIs', text: 'What is the object at this position?', hint: 'Enter ra dec [radius [radius units]]',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '', whatIsWindow: true,
					    service: 'SimbadScript.Cone.VoTable', serviceParamFn: c.whatIsParams2,
					    result: { format: 'extjs', type: 'whatIs' }
					}, {
					    uid: 'Siap', text: 'Virtual Observatory SIAP Search', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Siap', serviceParamFn: c.dataScopeDrilldownParams,
					    result: { format: 'extjs', type: 'siapTable' }
					}, {
					    uid: 'Ssap', text: 'Virtual Observatory SSAP Search', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Ssap', serviceParamFn: c.dataScopeDrilldownParams,
					    result: { format: 'extjs', type: 'ssapTable' }
					}, {
					    uid: 'VOTable', text: 'Load VO Table', hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Vo.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'iFrame', text: 'Display URL', hint: 'Enter URL [window]',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
						internal: true,
					    result: {type: 'iFrame' }
					}, {
					    uid: 'STPR', text: 'Space Telescope Press Releases' + (AppConfig.isDebug ? ' (STPR)' : ''), //hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'ST Press Releases',
					    service: 'Mast.Stpr.Votable', serviceParamFn: null,
					    result: { format: 'extjs', type: 'stprTable' }
					}, {
					    uid: 'HSTPR', text: 'HST Press Releases' + (AppConfig.isDebug ? ' (Hubblesite)' : ''), //hint: 'Enter URL for VO Table',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'HST Press Releases',
					    service: 'Hst.PressRelease.Votable', serviceParamFn: null,
					    result: { format: 'extjs', type: 'hstprTable' }
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
					    titlePrefix: 'Galex Objects: ',
					    service: 'Mast.Galex.Catalog', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'GalexTiles', text: 'Search Galex Tiles', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'Galex Tiles: ',
					    service: 'Mast.Galex.Tile', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'GalexSdss', text: 'Galex/SDSS', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'Galex/SDSS: ',
					    service: 'Mast.Galex.Sdss.Catalog', serviceParamFn: c.targetnameParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'GalexPhotonListNuv', text: 'Search Galex Photon List (NUV)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'Galex Photon List (NUV): ',
					    service: 'Galex.Photon.List.Nuv', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'GalexPhotonListFuv', text: 'Search Galex Photon List (FUV)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'Galex Photon List (FUV): ',
					    service: 'Galex.Photon.List.Fuv', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'HLAOLD', text: 'Search HLA(Old)', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'HLA: ',
					    service: 'Hla.Hubble.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'hla' }
					}, {
					    uid: 'HLA', text: 'Search HLA', hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'HLA: ',
					    service: 'Hla.SIA.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'hla' }
					}, {
					    uid: 'ADS', text: 'ADS Search by Author' + (AppConfig.isDebug ? ' (ADSAuthor)' : ''), hint: "Enter author's name",
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'ADS: ',
					    service: 'Ads.Author.Votable', serviceParamFn: c.adsParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'HLSP', text: 'High-Level Science Products' + (AppConfig.isDebug ? ' (HLSP)' : ''),
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'HLSP: ',
					    service: 'Mast.Hlsp.Project', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'hlspTable' }
					}, {
					    uid: 'SID', text: 'Sid Archive by Instrument' + (AppConfig.isDebug ? ' (SID_I)' : ''), hint: 'Enter instrument name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SID: ',
					    service: 'Mast.Sid.Votable', serviceParamFn: c.sidParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'STP', text: 'Staff Papers' + (AppConfig.isDebug ? ' (STP)' : ''), hint: 'Enter last name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Staff Papers: ',
					    service: 'Mast.PaperTrack.Staff', serviceParamFn: c.stpParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 1000000 }
					}, {
					    uid: 'RP', text: 'Ranked Authors' + (AppConfig.isDebug ? ' (RP)' : ''), hint: 'Enter anything',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Ranked Authors: ',
					    service: 'Mast.Hlsp.RankedAuthors',
					    result: { format: 'extjs', type: 'genericTable', pagesize: 1000000 }
					}, {
					    uid: 'LIT', text: 'Literature by Keyword' + (AppConfig.isDevelopment ? ' (LIT)' : ''), hint: 'Enter keyword',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'LIT: ',
					    service: 'Mast.PaperTrack.Keyword', serviceParamFn: c.litParams,
					    result: { format: 'extjs', type: 'literatureTable', pagesize: 1000000 }
					}, 
                    {
					    uid: 'CSV', text: 'Load CSV Table', hint: 'Enter URL for CSV Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Csv.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'CAOMDownload', text: 'CAOMDownload', hint: 'Enter Obs IDs',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Download Basket: ', windowed: true,
					    service: 'Mast.Caom.Products', serviceParamFn: c.downloadParams,
					    result: { format: 'extjs', type: 'basket', pagesize: 1000000 }
					}, {
					    uid: 'Distribution', text: 'Distribution', hint: '',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Distribution Request: ', windowed: true,
					    service: 'Mast.Distribution.Request', serviceParamFn: c.distributionParams,
					    result: { format: 'extjs', type: 'basket', pagesize: 1000000 }
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
			whatIs: {
                defaultView: {
                    type: 'Mvp.gui.AstroGridView',
                    config: {
                        contentType: 'catalog'
                    }
                },
                storePageSize: 500000
            },
            genericTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'generic' }
                },
                storePageSize: 500000
            },
            genericImageTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'image' }
                },
                storePageSize: 500000
            },
            siapTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Siap',
                    config: { contentType: 'image' }
                },
                storePageSize: 500000
            },
            ssapTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Ssap',
                    config: { contentType: 'spectra' }
                },
                storePageSize: 500000
            },
            stprTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Stpr',
                    config: { contentType: 'image' }
                },
                columnsconfigid: 'Mast.Stpr.Votable',
                storePageSize: 500000
            },
            hstprTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Hstpr',
                    config: { contentType: 'image' }
                },
                storePageSize: 500000,
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
                storePageSize: 500000,
                columnsconfigid: 'Mast.Caom.Cone'
            },
            hlspTable: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 500000,
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
                storePageSize: 500000,
                columnsconfigid: 'Vo.Hesarc.Datascope'
            },
            catalog: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'catalog' }
                },
                storePageSize: 500000
            },
            hla: {
                defaultView: {
                    type: 'Mvp.gui.custom.Hla',
                    config: { contentType: 'image' }
                },
                storePageSize: 500000,
                columnsconfigid: "Hla.Hubble.Votable"
            },
            basket: {
                defaultView: {
                    type: 'Mvp.gui.custom.DownloadBasket',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 500000
            },
			iFrame: {
				defaultView: {
					type: 'Mvp.gui.IFrame',
                    config: { contentType: 'generic' }
				}
            },
            literatureTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Keywords',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 500000
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
        },

        litParams: function (searchInput) {
            var params = {
                input: searchInput.inputText,
                key: searchInput.inputText
            };
            return params;
        },

        downloadParams: function (searchInput) {
            var params = {
                obsid: searchInput.inputText,
                ajaxParams: { method: 'POST' }
            };
            return params;
        },

        distributionParams: function (searchInput) {
            var params = {
                filelist: searchInput.inputText,
                filename: 'Download',
                ajaxParams: { method: 'POST' }
            };
            return params;
        },
		
		whatIsParams: function(searchInput, whatIsParams) {
			var params = {input: searchInput.inputText};
			if (whatIsParams) {
				Ext.apply(params, whatIsParams);
			} else {
				var args = searchInput.inputText.split(' ');
				params.coords = args[0] + ' ' + args[1];
				params.coords = params.coords.replace(/\+/g, '%2B');
				params.coords = params.coords.replace(/ /g, '+');
				params.coords = params.coords.replace(/:/g, '+');
				if (args.length > 2) {
					params.radius = args[2];
				}
				if (args.length > 3) {
					params.radiusUnits = args[3];
				}
			}
			return params;
		},
		
		whatIsParams2: function(searchInput, whatIsParams) {
			var params = {input: searchInput.inputText};
			if (whatIsParams) {
				Ext.apply(params, whatIsParams);
			} else {
				var args = searchInput.inputText.split(' ');
				params.coords = args[0] + ' ' + args[1];
				params.coords = params.coords.replace(/\+/g, '%2B');
				params.coords = params.coords.replace(/ /g, '+');
				if (args.length > 2) {
					params.radius = args[2];
				}
			}
			return params;
		}
    }

});