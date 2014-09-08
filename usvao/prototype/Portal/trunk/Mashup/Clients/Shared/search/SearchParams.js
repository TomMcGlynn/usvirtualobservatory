Ext.define('Mvp.search.SearchParams', {
    requires: [
				'Ext.Date',
			   'Mvp.search.receivers.SantaXmlReceiver',
			   'Mvp.search.receivers.ExtjsReceiver', 'Mvp.search.Summary',
			   //'Mvp.search.Summary',
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
               'Mvp.gui.custom.DadsDownloadBasket',
               'Mvp.gui.custom.SidFiles',
               'Mvp.gui.custom.Hla',
               'Mvp.gui.custom.NedImages',
               'Mvp.gui.custom.Summary',
			   'Mvp.util.Constants',
			   'Mvp.gui.IFrame',
               'Mvp.gui.custom.Keywords',
               'Mvp.gui.custom.MastMissionView',
			   'Mvp.gui.custom.Registry',
               'Mvp.gui.custom.GalexTiles',
               'Mvp.gui.custom.Ads',
               'Mvp.util.Constants'
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
					    titlePrefix: 'MAST: ', downloadEnabled: true,
					    service: 'Caom.Cone.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMTEST', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOMTEST VOTable)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'MAST: ', downloadEnabled: true,
					    service: 'Caomtest.Cone.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMDB', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOMDB)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'MAST: ', downloadEnabled: true,
					    service: 'Mast.Caom.Cone', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMDBTEST', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOMDBTEST)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'MAST: ', downloadEnabled: true,
					    service: 'Masttest.Caom.Cone', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMDBDEV', text: 'All MAST Observations' + (AppConfig.isDebug ? ' (CAOMDBDEV)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'CAOMDBDEV: ', downloadEnabled: true,
					    service: 'Mastdev.Caom.Cone', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
					    uid: 'CAOMBYOBS', text: 'Search CAOM by ObsID' + (AppConfig.isDebug ? ' (CAOMBYOBS)' : ''), hint: 'Enter ObsID',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'CAOMBYOBS: ', downloadEnabled: true,
					    service: 'Mastdev.Caom.SearchByObsID', serviceParamFn: c.obsidParams,
					    result: { format: 'extjs', type: 'caom' }
					}, {
                        uid: 'DataScope', text: 'All Virtual Observatory Collections' + (AppConfig.isDebug ? ' (Datascope)' : ''),
                        hint: 'Enter object name or RA and Dec',
                        resolve: true, inputType: 'positionOneBox',
                        titlePrefix: 'VO: ', downloadEnabled: false,
                        service: 'Vo.Hesarc.DatascopeListable', serviceParamFn: c.dataScopeParams,
                        result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
                    }, {
                        uid: 'DataScopeListable', text: 'All Virtual Observatory Collections' + (AppConfig.isDebug ? ' (Datascope)' : ''),
                        hint: 'Enter object name or RA and Dec',
                        resolve: true, inputType: 'positionOneBox',
                        titlePrefix: 'VO: ', downloadEnabled: false, ajaxParams: { method: 'POST' },
                        service: 'Vo.Hesarc.DatascopeListable', serviceParamFn: c.dataScopeListableParams, serviceDataFn: c.dataScopeData,
                        result: { format: 'extjs', type: 'datascope', pagesize: 1000000 }
                    }, {
					    uid: 'DataScopeVao', text: 'All Virtual Observatory Collections' + (AppConfig.isDebug ? ' (DatascopeVao)' : ''),
					    hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionThreeBox',
					    titlePrefix: '', downloadEnabled: false,
					    service: 'Vo.Hesarc.DatascopeListable', serviceParamFn: c.dataScopeParams,
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
					    result: { format: 'extjs', type: 'galexTable' }
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
					    result: { format: 'extjs', type: 'adsTable' }
					}, {
					    uid: 'HLSP', text: 'High-Level Science Products' + (AppConfig.isDebug ? ' (HLSP)' : ''),
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'HLSP',
					    service: 'Mast.Hlsp.Project', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'hlspTable' }
					}, {
					    uid: 'SIDBYINST', text: 'SID Archive by Instrument' + (AppConfig.isDebug ? ' (SIDBYINST)' : ''), hint: 'Enter instrument name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SIDBYINST: ',
					    service: 'Mast.Sid.Votable', serviceParamFn: c.sidParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'SIDBYJOB', text: 'SID Archive by Job IDs' + (AppConfig.isDebug ? ' (SIDBYJOB)' : ''), hint: 'Enter job numbers (1, 3-5, 8)',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SID By Job: ', downloadEnabled: true,
					    service: 'Mast.Sid.Jobnum', serviceParamFn: c.sidIdParams,
					    result: { format: 'extjs', type: 'sidFiles' }
					}, {
					    uid: 'SIDGETPACKAGE', text: 'SID Package Files by Package ID' + (AppConfig.isDebug ? ' (SIDGETPACKAGE)' : ''), hint: 'Enter package ID',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SIDGETPACKAGE: ', downloadBasketWindow: true,
					    service: 'Mast.Sid.GetPackageFiles', serviceParamFn: c.sidGetPackageParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'SIDGETALLFILES', text: 'SID Packages and Files by Job IDs' + (AppConfig.isDebug ? ' (SIDGETALLFILES)' : ''), hint: 'Enter job numbers (1, 3-5, 8)',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'SID Files: ', downloadBasketWindow: true, downloadEnabled: true,
					    service: 'Mast.Sid.GetAllFilesByJobId', serviceParamFn: c.sidIdParams,
					    result: { format: 'extjs', type: 'dadsBasket', internal: false }
					}, {
					    uid: 'STP', text: 'Staff Papers' + (AppConfig.isDebug ? ' (STP)' : ''), hint: 'Enter last name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Staff Papers: ',
					    service: 'Mast.PaperTrack.Staff', serviceParamFn: c.stpParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 1000000 }
					}, {
					    uid: 'PA', text: 'Papers By Author' + (AppConfig.isDebug ? ' (PA)' : ''), hint: 'Enter last name',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: 'Papers by Author: ',
					    service: 'Mast.PaperTrack.ByAuthor', serviceParamFn: c.stpParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 100 }
					}, {
					    uid: 'AS', text: 'Author Summary' + (AppConfig.isDebug ? ' (AS)' : ''), 
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'Author Summary',
					    service: 'Mast.PaperTrack.AuthorSummary', serviceParamFn: c.stpParams,
					    result: { format: 'extjs', type: 'genericTable', pagesize: 100 }
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
					}, {
					    uid: 'CSV', text: 'Load CSV Table', hint: 'Enter URL for CSV Table',
					    resolve: false, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Csv.Generic.Table', serviceParamFn: c.voTableParams,
					    result: { format: 'extjs', type: 'genericTable' }
					}, {
					    uid: 'CAOMDownload', text: 'CAOMDownload', hint: 'Enter Obs IDs',
					    resolve: false, inputType: 'positionOneBox',
					    /*titlePrefix: 'Download Basket: ',*/ downloadBasketWindow: true,
					    service: 'Mast.Caom.Products', serviceParamFn: c.downloadParams,
					    result: { format: 'extjs', type: 'basket', pagesize: 1000000, internalDownload: true }
					}, {
					    uid: 'CAOMDownloadTest', text: 'CAOMDownload (Test DB)', hint: 'Enter Obs IDs',
					    resolve: false, inputType: 'positionOneBox',
					    /*titlePrefix: 'Download Basket: ',*/ downloadBasketWindow: true,
					    service: 'Mast.Caomtest.Products', serviceParamFn: c.downloadParams,
					    result: { format: 'extjs', type: 'basket', pagesize: 1000000, internalDownload: true }
					}, {
					    uid: 'Distribution', text: 'Distribution', hint: '',
					    resolve: false, inputType: 'positionOneBox',
					    /*titlePrefix: 'Distribution Request: ',*/ downloadBasketWindow: true,
					    service: 'Mast.Distribution.Request', serviceParamFn: c.distributionParams,
					    result: { format: 'extjs', type: 'basket', pagesize: 1000000, internalDownload: true }
					}, {
					    uid: 'DSREG', text: 'Load VO Registry (Standard Services)', hint: '',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'Standard Registry',
					    service: 'Vo.Registry.StandardServices', serviceParamFn: null,
					    result: { format: 'extjs', type: 'registry', pagesize: 5000 }
					}, {
					    uid: 'WHOLEREG', text: 'Load VO Registry (All)', hint: '',
					    resolve: false, inputType: 'searchButton',
					    titlePrefix: 'VO Registry',
					    service: 'Vo.Registry.Whole', serviceParamFn: null,
					    result: { format: 'extjs', type: 'registry', pagesize: 5000 }
					}, {
                        uid: 'REGKEYWORD', text: 'VO Registry by Keyword', hint: 'Enter Anything',
                        resolve: false, inputType: 'positionOneBox',
                        datascopeSearchEnabled: true,
                        titlePrefix: 'VO Registry: ',
                        service: 'Vo.Registry.VOTKeyword',  serviceParamFn: c.regKeywordParams,
                        result: { format: 'extjs', type: 'registryKeyword', pagesize: 500 }
                    }, {
					    uid: 'MAST_MISSION', text: 'Search MAST Mission' + (AppConfig.isDebug ? ' (MAST [IUE])' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: '',
					    service: 'Mast.Missions.Votable', serviceParamFn: c.mastMissionParams,
					    result: { format: 'extjs', type: 'mastMission' }
					}, {
                        uid: 'IUE', text: 'Search IUE Holdings' + (AppConfig.isDebug ? ' (MAST-IUE)' : ''), hint: 'Enter object name or RA and Dec',
                        resolve: true, inputType: 'positionOneBox',
                        titlePrefix: 'IUE: ',
                        service: 'Mast.Missions.Iue', serviceParamFn: c.iueParams,
                        result: { format: 'extjs', type: 'iueTable' }
                    }, {
					    uid: 'HSTSC', text: 'Search HST Source Catalog' + (AppConfig.isDebug ? ' (HSTSC)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'HSTSC: ',
					    service: 'Mast.Hsc.Votable', serviceParamFn: c.hscParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'GSC23', text: 'Search GSC2.3' + (AppConfig.isDebug ? ' (GSC23)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'GSC 2.3: ',
					    service: 'Mast.Gsc23.Votable', serviceParamFn: c.coneParams,
					    result: { format: 'extjs', type: 'catalog' }
					}, {
					    uid: 'SUMMARY', text: 'MAST Inventory' + (AppConfig.isDebug ? ' (SUMMARY)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'MAST Inventory: ',
					    service: 'Mast.Summary', serviceParamFn: c.summaryParams, timeout: 2, forceUpdate: true, ajaxParams: { method: 'POST' },  
						serviceDataFn: c.summaryData,
						result: { format: 'extjs', type: 'summary', pagesize: Mvp.util.Constants.MAX_SUMMARY_REQUESTS }
					}, {
					    uid: 'HYBRID_SUMMARY', text: 'MAST/VO Search' + (AppConfig.isDebug ? ' (HYBRID_SUMMARY)' : ''), hint: 'Enter object name or RA and Dec',
					    resolve: true, inputType: 'positionOneBox',
					    titlePrefix: 'MAST/VO Search: ',
					    service: 'Mast.Summary', serviceParamFn: c.hybridSummaryParams, timeout: 2, forceUpdate: true, ajaxParams: { method: 'POST' },  
						serviceDataFn: c.hybridSummaryData,
						result: { format: 'extjs', type: 'summary', pagesize: Mvp.util.Constants.MAX_SUMMARY_REQUESTS }
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
            iueTable: {
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
                        contentType: 'observation'
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
                storePageSize: 500000
            },
            registry: {
                defaultView: {
                    type: 'Mvp.gui.custom.Registry',
                    config: {
                        contentType: 'mixed'
                    }
                },
                storePageSize: 500000,
                columnsconfigid: 'Vo.Registry'
            },
            registryKeyword: {
                defaultView: {
                    type: 'Mvp.gui.custom.Registry',
                    config: {
                        contentType: 'mixed'
                    }
                },
                storePageSize: 30000,
                columnsconfigid: 'Vo.Registry'
            },
            catalog: {
                defaultView: {
                    type: 'Mvp.gui.FacetedGridView',
                    config: { contentType: 'catalog' }
                },
                storePageSize: 500000
            },
			mastMission: {
                defaultView: {
                    type: 'Mvp.gui.custom.MastMissionView',
                    config: { contentType: 'observation' }
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
            ned: {
                defaultView: {
                    type: 'Mvp.gui.custom.NedImages',
                    config: { contentType: 'image' }
                },
                storePageSize: 500000
            },
            basket: {
                defaultView: {
                    type: 'Mvp.gui.custom.DownloadBasket',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 500000,
                columnsconfigid: "Mast.Caom.Products"
            },
            dadsBasket: {
                defaultView: {
                    type: 'Mvp.gui.custom.DadsDownloadBasket',
                    config: { contentType: 'generic' }
                },
                storePageSize: 500000
            },
            sidFiles: {
                defaultView: {
                    type: 'Mvp.gui.custom.SidFiles',
                    config: { contentType: 'generic' }
                },
                storePageSize: 500000
            },
			iFrame: {
				defaultView: {
					type: 'Mvp.gui.IFrame',
                    config: { contentType: 'generic' }
				}
			},
			galexTable: {
			    defaultView: {
			        type: 'Mvp.gui.custom.GalexTiles',
			        config: { contentType: 'generic' }
			    },
			    storePageSize: 500000
			},
            literatureTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Keywords',
                    config: {
                        contentType: 'generic'
                    }
                },
                storePageSize: 500000
            },
            summary: {
                defaultView: {
                    type: 'Mvp.gui.custom.Summary',
                    config: {
                        contentType: 'mixed'
                    }
                },
                storePageSize: 500000
            },
            adsTable: {
                defaultView: {
                    type: 'Mvp.gui.custom.Ads',
                    config: { contentType: 'generic' }
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

		// Will search searchInput.mission if it exists, otherwise IUE
        mastMissionParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
			
			searchInput.mission = searchInput.mission || 'IUE';
            params.mission = searchInput.mission;
            return params;
        },

       iueParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            params.mission = 'IUE';
            return params;
        },
        
        hscParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            return params;
        },

        dataScopeParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            params.skipcache = AppConfig.skipCache;  // 'NO' by default; change to 'YES' to force the DS server to not use cache.

            return params;
        },

        dataScopeListableParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
            params.skipcache = AppConfig.skipCache;  // 'NO' by default; change to 'YES' to force the DS server to not use cache.
            // This timestamp will handle caching issues for this service.
            // This timestamp will be set as a parameter on the initial query,
            // thereby becoming part of the cache key.  On subsequent queries
            // for updates (every 2 seconds at this writing), we won't change the
            // timestamp, so we will get updates for this query.  New user queries will
            // get new timestamps, so they will get fresh cache for their results.
            params.timestamp = Ext.Date.now();

            return params;
        },

        dataScopeData: function (searchInput, coneSearchParams) {
            var data = searchInput.resourceList;
            return data;            

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

        sidIdParams: function (searchInput) {
            var params = {
                jobnum: Mvp.util.Util.parseCsvWithDashes(searchInput.inputText),
                ajaxParams: { method: 'POST' }
            };
            return params;
        },

        sidGetPackageParams: function (searchInput) {
            var params = {
                package_id: searchInput.inputText
            };
            return params;
        },

        dataScopeDrilldownParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            Ext.apply(params, {
                url: searchInput.url,
                input: searchInput.inputText
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

        regKeywordParams: function (searchInput) {
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

        obsidParams: function (searchInput) {
            var params = {
                input: searchInput.inputText,
                obsid: searchInput.inputText
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
		},
		
        summaryParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
			
			// This timestamp will handle caching issues for this service.
			// This timestamp will be set as a parameter on the initial query,
			// thereby becoming part of the cache key.  On subsequent queries
			// for updates (every 2 seconds at this writing), we won't change the
			// timestamp, so we will get updates for this query.  New user queries will
			// get new timestamps, so they will get fresh cache for their results.
			params.timestamp = Ext.Date.now();

            return params;			

        },
		
		// The default resource list can be overridden by putting the desired list
		// in searchInput.resourceList.
        summaryData: function (searchInput, coneSearchParams) {
			var defaultResources = Mvp.search.Summary.mastCaom.concat(Mvp.search.Summary.mastMissions,
																	  Mvp.search.Summary.mastCatalogs);
			var resources = searchInput.resourceList || defaultResources;
			var data = Mvp.search.Summary.generateSummaryData(searchInput, coneSearchParams, resources);

            return data;			

        },
		
        hybridSummaryParams: function (searchInput, coneSearchParams) {
            var params = coneSearchParams;
            params.input = searchInput.inputText;
			
			// This timestamp will handle caching issues for this service.
			// This timestamp will be set as a parameter on the initial query,
			// thereby becoming part of the cache key.  On subsequent queries
			// for updates (every 2 seconds at this writing), we won't change the
			// timestamp, so we will get updates for this query.  New user queries will
			// get new timestamps, so they will get fresh cache for their results.
			params.timestamp = Ext.Date.now();

            return params;			

        },
		
		// The default resource list can be overridden by putting the desired list
		// in searchInput.resourceList.
        hybridSummaryData: function (searchInput, coneSearchParams) {
			var defaultResources = Mvp.search.Summary.mastCaomHLA.concat(Mvp.search.Summary.mastCatalogs,
																	  Mvp.search.Summary.selectVoResources);
			var resources = searchInput.resourceList || defaultResources;
			var data = Mvp.search.Summary.generateSummaryData(searchInput, coneSearchParams, resources);

            return data;			

        }		
    }

});