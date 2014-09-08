Ext.require('Mvp.util.MashupQuery');

Ext.define('Mvp.util.Searches', {
    statics: {
        defineServiceList: function () {
            var serviceData = [];

            if (isMast) {
                serviceData = serviceData.concat([{
                    fn: 'searchCaomVoTable', hint: 'Enter object name or RA and Dec',
                    text: 'All MAST Observations (CAOM)', resolve: true
                }, {
					fn: 'searchCaomDb', hint: 'Enter object name or RA and Dec',
					text: 'Search MAST CAOM via DB', resolve: true
				}, {
                    fn: 'searchStpr', hint: 'Enter object name or RA and Dec',
                    text: 'Hubble Press Releases (STPR)', resolve: false,
                    searchAll: true
                }, {
                    fn: 'searchPressRelease', hint: 'Enter object name or RA and Dec',
                    text: 'Hubble Public Images (Hubblesite)', resolve: false,
                    searchAll: true
                }, {
                    fn: 'searchDataScope', hint: 'Enter object name or RA and Dec',
                    text: 'All Virtual Observatory Collections (DataScope)', resolve: true
                }, {
                    fn: 'searchAdsAuthor', hint: "Enter Author's Name",
                    text: 'ADS Search by Author (ADSAuthor)', resolve: true
                }, {
                    fn: 'searchGalexSDSSObjects', hint: 'Enter object name or RA and Dec',
                    text: 'Galex/SDSS Objects', resolve: true
                }, {
                    fn: 'searchGalexObjects', hint: 'Enter object name or RA and Dec',
                    text: 'Galex Catalog Objects (GR6)', resolve: true
                }, {
                    fn: 'searchGalexTiles', hint: 'Enter object name or RA and Dec',
                    text: 'Galex Image Tiles (GR6)', resolve: true
                }, {
                    fn: 'searchHlaVoTable', hint: 'Enter object name or RA and Dec',
                    text: 'Hubble Legacy Archive (HLA)', resolve: true
                }, {
                    fn: 'searchSidByInst', hint: 'Enter instrument name',
                    text: 'Sid Archive by Instrument (SID_I)', resolve: false
                }
	            ]);

                if (isDevelopment) {
                    serviceData = serviceData.concat([{
                        fn: 'searchHlsp', hint: 'Enter anything',
                        text: 'MAST High-Level Science Products (HLSP)', resolve: false,
                        searchAll: true
                    }, {
                        fn: 'searchStaffPapers', hint: 'Enter last name',
                        text: 'Staff Papers (STP)', resolve: false,
                        searchAll: false
                    }, {
                        fn: 'searchRankedAuthors', hint: 'Enter anything',
                        text: 'Ranked Authors (RP)', resolve: false,
                        searchAll: true
                    }, {
                        fn: 'searchInventory2', hint: 'Enter object name or RA and Dec',
                        text: 'VO Inventory (fast for selected collections)', resolve: true
                    }, {
                        fn: 'searchVoTable', hint: 'Enter the URL of a VO Table',
                        text: 'Load VO Table', resolve: false
                    }, {
                        fn: 'loadCsvFile', hint: 'Enter the URL of a CSV file',
                        text: 'Load CSV File', resolve: false
                    }]);
                }
            } else {
                serviceData = serviceData.concat([{
                    fn: 'searchInventory2', hint: 'Enter object name or RA and Dec',
                    text: 'Quick Search (fast for selected collections)', resolve: true
                }, {
                    fn: 'searchDataScope', hint: 'Enter object name or RA and Dec',
                    text: 'Full Search (all known catalog and image collections)', resolve: true
                }
	            ]);

                if (isDevelopment) {
                    serviceData = serviceData.concat([{
                        fn: 'searchAdsAuthor', hint: "Enter Author's Name",
                        text: 'Search ADS for publications by author', resolve: false
                    }, {
                        fn: 'Ned_SedInfoDiscovery_Votable', hint: "Enter object name or RA and Dec",
                        text: 'Search NED for SEDs near a position', resolve: true
                    }, {
                        fn: 'Ned_SedInfoAvailability_Votable', hint: "Enter object name",
                        text: 'Search NED for an SED for an object', resolve: false
                    }, {
                        fn: 'Ned_SedDataRetrieval_Votable', hint: "Enter NED Object Name",
                        text: 'Load NED SED for object', resolve: false
                    }, {
                        fn: 'searchInventory', hint: 'Enter object name or RA and Dec',
                        text: 'Search VO Inventory Classic', resolve: true
                    }, {
                        fn: 'searchCaomVoTable', hint: 'Enter object name or RA and Dec',
                        text: 'Search MAST CAOM via VOTable', resolve: true
                    }, {
                        fn: 'searchHlaVoTable', hint: 'Enter object name or RA and Dec',
                        text: 'Search HLA', resolve: true
                    }, {
                        fn: 'searchHlsp', hint: 'Enter anything',
                        text: 'Load MAST HLSP Projects', resolve: false,
                        searchAll: true
                    }, {
                        fn: 'searchGalexObjects', hint: 'Enter object name or RA and Dec',
                        text: 'Search Galex Objects', resolve: true
                    }, {
                        fn: 'searchGalexSDSSObjects', hint: 'Enter object name or RA and Dec',
                        text: 'Search Galex/SDSS Objects', resolve: true
                    }, {
                        fn: 'searchGalexTiles', hint: 'Enter object name or RA and Dec',
                        text: 'Search Galex Tiles', resolve: true
                    }, {
                        fn: 'searchCaomDb', hint: 'Enter object name or RA and Dec',
                        text: 'Search MAST CAOM via DB', resolve: true
                    }, {
                        fn: 'searchRegistry', hint: 'Enter keyword(s)',
                        text: 'Search VAO Registry', resolve: false
                    }, {
                        fn: 'loadWholeRegistry', hint: 'Enter anything',
                        text: 'Load Whole Registry', resolve: false
                    }, {
                        fn: 'getDataSetFile', hint: 'Enter filename (path relative to this html file)',
                        text: 'Get File', resolve: false
                    }, {
                        fn: 'searchVoTable', hint: 'Enter the URL of a VO Table',
                        text: 'Load VO Table', resolve: false
                    }, {
                        fn: 'loadCsvFile', hint: 'Enter the URL of a CSV file',
                        text: 'Load CSV File', resolve: false
                    }, {
                        fn: 'voTableExport', hint: 'Enter the URL of a VO Table',
                        text: 'Export VO Table', resolve: false
                    }]);
                }

            }
            return serviceData;

        }
    }
});