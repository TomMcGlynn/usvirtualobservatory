Ext.define('Mvp.search.Summary', {
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
		
		
        mastCaom: [
            {
				serviceId: 'CAOMDB', shortName: 'All MAST', title: 'All MAST Observations',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'A collection of MAST observation described according to a set of summary properties (RA, Dec, waveband, start/end time, etc.)',
				//extraParams: {exclude_hla: 'T', radius: 0.3},
				extraParams: {exclude_hla: 'T'},
				invokeBaseUrl: "http://masttest.stsci.edu/portal/Mashup/Mashup.asmx/invoke"
			}
        ],
		
		mastCaomHLA: [
            {
				serviceId: 'CAOMDB', shortName: 'All MAST', title: 'All MAST Observations',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'A collection of MAST observation described according to a set of summary properties (RA, Dec, waveband, start/end time, etc.)',
				invokeBaseUrl: "http://masttest.stsci.edu/portal/Mashup/Mashup.asmx/invoke"
			}
        ],
        
        mastMissions: [  
            {
				serviceId: 'HLA', shortName: 'HLA', title: 'Hubble Legacy Archive',
				publisher: 'MAST',
				capabilityClass: 'SimpleImageAccess',
				description: 'The Hubble Legacy Archive (HLA) is designed to optimize science from the Hubble Space Telescope by providing online, enhanced Hubble products and advanced browsing capabilities. The HLA is a joint project of the Space Telescope Science Institute (STScI), the Space Telescope European Coordinating Facility (ST-ECF), and the Canadian Astronomy Data Centre (CADC).',
				invokeBaseUrl: null
			},
            {
				serviceId: 'GalexTiles', shortName: 'GALEX', title: 'Galaxy Evolution Explorer',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Microchannel plate detector images between 1,350 - 2,800 Å, 1.2 sq. degree field of view and 5"/pixel resolution, 2003 - ?',
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'FUSE', title: 'Far-UV Spectroscopic Explorer',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 905 - 1,187 Å range, resolution ~30,000, ~3,000 targets observed, 1999 - 2007',
				extraInput: {mission: 'FUSE'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'Kepler', title: 'Kepler Data',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Light curves, 4,300 - 8,900 Å, 42 CCD detectors, 105 square degree FOV, ~3.08 arcsec/pixel, ~170,000 targets with average 32 pixels/target, most data points represent 30 minute average of 1 minute integrations, saturation may occur for targets brighter than 11.5 V, 2009 - ?',
				extraInput: {mission: 'KEPLER'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'IUE', title: 'International Ultraviolet Explorer',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 1,200 - 3,350 Å range, resolutions ~ 300 and 15,000, V_max ~ 15 and 10 respectively, ~ 10,000 individual targets, 1978 - 1996',
				extraInput: {mission: 'IUE'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'BEFS', title: 'Berkeley Extreme and Far-UV Spectrometer',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 400 - 900 (few) & 900 - 1200 Å range, resolution ~ 3,000, V_max ~ 15, ~175 targets observed, 2 missions, (9/93 & 11/96)',
				extraInput: {mission: 'BEFS'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'HUT', title: 'Hopkins Ultraviolet Telescope',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 912 - 1,850 Å range, resolution ~ 500, V_max ~ 16, ~ 300 targets, 2 shuttle missions (12/90 & 3/95)',
				extraInput: {mission: 'HUT'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'TUES', title: 'Tubingen Echelle Spectrograph',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 900 - 1,400 Å range, resolution ~ 10,000, V_max ~ 13, 65 targets observed, 12/96',
				extraInput: {mission: 'TUES'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'UIT', title: 'Ultraviolet Imaging Telescope',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Digitized film images between 1,200-3,300 Å, 40 arcmin field of view and 3" resolution, 259 targets total, 2 shuttle missions (12/90 & 3/95)',
				extraInput: {mission: 'UIT'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'WUPPE', title: 'Wisconsin Ultraviolet Photo-Polarimeter Experiment',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectropolarimetric data in the 1,400 - 3,200 Å range, ~ 200 targets, 2 shuttle missions (12/90 & 3/95)',
				extraInput: {mission: 'WUPPE'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'XMM-OM', title: 'X-ray Multi-Mirror (XMM) Telescope',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'MIC detector (microchannel plate/CCD) images between 1,700 - 5,500 Å, 24 arcmin field of view and 1"/pixel resolution, B-mag range ~24 to 8.6, 1999 - ?',
				extraInput: {mission: 'XMM-OM'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'EUVE', title: 'Extreme Ultraviolet Explorer',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'Spectra in the 70 - 760 Å range, resolution ~ 300, V_max ~ 15, ~ 400 targets, mostly Galactic, 1992 - 2001',
				extraInput: {mission: 'EUVE'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'HLSP', title: 'High Level Science Products',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'High-Level Science Products (HLSP) are community contributed, fully processed (reduced, co-added, cosmic-ray cleaned etc.) images and spectra that are ready for scientific analysis. HLSP also include files such as object catalogs, spectral atlases, and README files describing a given set of data.',
				extraInput: {mission: 'HLSP'},
				invokeBaseUrl: null
			},
            {
				serviceId: 'MAST_MISSION', shortName: 'SWIFT-UVOT', title: 'Swift UV/Optical Telescope (170-650 nm)',
				publisher: 'MAST',
				capabilityClass: 'Observation',
				description: 'High-Level Science Products (HLSP) are community contributed, fully processed (reduced, co-added, cosmic-ray cleaned etc.) images and spectra that are ready for scientific analysis. HLSP also include files such as object catalogs, spectral atlases, and README files describing a given set of data.',
				extraInput: {mission: 'SWIFTUVOT'},
				invokeBaseUrl: null
			}

        ],
        
        mastCatalogs: [
			{
				serviceId: 'GalexSdss', shortName: 'GALEX/SDSS', title: 'Galex and SDSS Objects',
				publisher: 'MAST',
				capabilityClass: 'ConeSearch',
				description: 'Microchannel plate detector images between 1,350 - 2,800 Å, 1.2 sq. degree field of view and 5"/pixel resolution, 2003 - ?',
				invokeBaseUrl: null
			},
			{
				serviceId: 'GalexObjects', shortName: 'GALEX Objects', title: 'The GALEX Source Catalog',
				publisher: 'MAST',
				capabilityClass: 'ConeSearch',
				description: 'Microchannel plate detector images between 1,350 - 2,800 Å, 1.2 sq. degree field of view and 5"/pixel resolution, 2003 - ?',
				invokeBaseUrl: null
			},
			{
				serviceId: 'Cone', shortName: 'GSC23', title: 'Guide Star Catalog 2.3',
				publisher: 'MAST',
				capabilityClass: 'ConeSearch',
				description: 'The Guide Star Catalog II (GSC-II) is an all-sky optical catalog based on 1" resolution scans of the photographic Sky Survey plates, at two epochs and three bandpasses, from the Palomar and UK Schmidt telescopes. This all-sky catalog will ultimately contains positions, proper motions, classifications, and magnitudes in multiple bandpasses for almost a billion objects down to approximately Jpg=21, Fpg=20. The GSC-II is currently used for HST Bright Object Protection and HST pointing. Looking ahead, the GSC-II will form the basis of the Guide Star Catalog for JWST. This was constructed in collaboration with ground-based observatories for use with the GEMINI, VLT and GALILEO telescopes.',
				extraInput: {url: 'http://gsss.stsci.edu/webservices/vo/ConeSearch.aspx?CAT=GSC23&'},
				invokeBaseUrl: null
			}
        ],
		
        selectVoResources: [

			{
				serviceId: 'Siap', shortName: 'Spitzer Level 2', title: 'Spitzer Level 2 / post Basic Calibrated Data',
				publisher: 'NASA/IPAC Infrared Science Archive',
				capabilityClass: 'SimpleImageAccess',
				description: 'Level 2 or post Basic Calibrated Data (PBCD) from Spitzer Space Telescope.' +
				'This products come from combining the individual data frames or BCDs ' +
				'[such as mosaics of individual pointings].',
				extraInput: {url: 'http://sha.ipac.caltech.edu/applications/Spitzer/VO/VOServices?SERVICE=SIAP&DATASET=ivo%3A%2F%2Firsa.ipac%2Fspitzer.level2&'},
				invokeBaseUrl: null
			},
			{
				serviceId: 'Siap', shortName: 'Chandra', title: 'Chandra X-Ray Observatory Data Archive',
				publisher: 'Chandra X-ray Observatory',
				capabilityClass: 'SimpleImageAccess',
				description: 'The Chandra X-ray Observatory is the U.S. follow-on to the Einstein Observatory.' +
				' Chandra was formerly known as AXAF, the Advanced X-ray Astrophysics Facility, but renamed by' +
				' NASA in December, 1998. Originally three instruments and a high-resolution mirror carried in' +
				' one spacecraft, the project was reworked in 1992 and 1993. The Chandra spacecraft carries a high' +
				' resolution mirror, two imaging detectors, and two sets of transmission gratings. Important Chandra' +
				' features are: an order of magnitude improvement in spatial resolution, good sensitivity from 0.1 to' +
				' 10 keV, and the capability for high spectral resolution observations over most of this range.',
				extraInput: {url: 'http://cda.harvard.edu/siap/queryImages?'},
				invokeBaseUrl: null
			},
			{
				serviceId: 'Siap', shortName: '2MASS ASKY AT', title: '2MASS All-Sky Atlas Image Service',
				publisher: 'NASA/IPAC Infrared Science Archive',
				capabilityClass: 'SimpleImageAccess',
				description: 'This service provides access to and information about the 2MASS All- Sky Atlas Images. Atlas Images delivered by this service are in FITS format and contain full WCS information in their headers. Additionally, the image headers contain photometric zero point information. 2MASS Atlas Images are suitable for quantitative photometric measurements. This particular record describes access to the images in this collection through the VO-standard SIA service interface. It can be used automatically by compliant tools and applications.',
				extraInput: {url: 'http://irsa.ipac.caltech.edu/cgi-bin/2MASS/IM/nph-im_sia?type=at&ds=asky&'},
				invokeBaseUrl: null
			},
			{
				serviceId: 'Cone', shortName: '2MASS-PSC', title: '2MASS All-Sky Point Source Catalog',
				publisher: 'NASA/IPAC Infrared Science Archive',
				capabilityClass: 'ConeSearch',
				description: '2MASS has uniformly scanned the entire sky in three near-infrared bands to detect and characterize point sources brighter than about 1 mJy in each band, with signal-to-noise ratio (SNR) greater than 1. This particular record describes access to the position-based catalog data in this collection through the VO-standard ConeSearch service interface. It can be used automatically by compliant tools and applications.',
				extraInput: {url: 'http://irsa.ipac.caltech.edu/cgi-bin/Oasis/CatSearch/nph-catsearch?CAT=fp_psc&'},
				invokeBaseUrl: null
			}
        ],
        
		generateSummaryData: function(searchInput, coneSearchParams, resources) {
            var data = [];
			
			// The Summary adaptor expects a list of queries that it should do.
			// For each query, we include the mashup request object, and some metadata like title and service id.
            
            for (var i=0; i<resources.length; i++) {
                var row = Mvp.search.Summary.generateSummaryDataRow(resources[i], searchInput, coneSearchParams);
                if (row) {
					row.recordNumber = i;
                    data.push(row);
                }
            }

            return data;			

        },
        
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
		
		invokeMashupQuery: function(context, serviceId, invokeBaseUrl, requestJson) {
			var searchParams = Ext.clone(Mvp.search.SearchParams.getSearch(serviceId));
			var request = Ext.decode(requestJson);
			var mission = request.params.mission;
			var searchInput = {
			    inputText: request.params.input,

			    //  TSD:  This is the worst hack yet.  Refactor me please!!
			    mission: mission
			};
			if (!searchParams.titlePrefix) searchParams.titlePrefix = mission ? mission + ': ': '';
			
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
		
		invokeSummaryQuery: function(context, inputText, resourceList) {
			var searchInput = {
				inputText: inputText,
				resourceList: resourceList
			}
			var searchParams = Mvp.search.SearchParams.getSearch('SUMMARY');
			
			// Invoke the search.
			context.invokeSearch(searchInput, searchParams);
		}
    }
    
});