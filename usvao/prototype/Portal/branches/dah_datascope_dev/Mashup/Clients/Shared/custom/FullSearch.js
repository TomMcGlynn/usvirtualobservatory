
Ext.define('Mvp.custom.FullSearch', {
    requires: ['Mvp.util.Constants'],
    statics: {
        loadRecords: function (record, controller) {
            var tableURL = record.get('tableURL');
            var description = record.get('title'),
                inputText = controller.searchInput.inputText,
                shortTitle = inputText + ': ' + record.get('shortName');
            var capability = record.get('capabilityClass');
            var identifier = record.get('identifier');
            var publisher = record.get('publisherIdentifier');

            if (tableURL) {
                var sp = Mvp.search.SearchParams.getSearch('VOTable');
                if (identifier.match('^ivo://mast.stsci/stpr')) {
                    sp.result.type = 'stprTable';
                } else if (identifier.match('^ivo://archive.stsci.edu/siap/hla')) {
                    sp.result.type = 'hla';
                } else if (capability && capability == 'SimpleImageAccess') {
                    if (publisher === 'ivo://ned.ipac/NED') {
                        sp.result.type = 'ned';
                    } else {
                        sp.result.type = 'siapTable';
                    }
                } else if (capability && capability == 'SimpleSpectralAccess') {
                    sp.result.type = 'ssapTable';
                }
                sp.position = controller.position;  // Pass along the sky location for the new context.  (allows astroview to move there on tab selection)
                var searchInput = {
                    inputText: tableURL,
                    title: shortTitle,
                    description: description
                };
                controller.invokeSearch(searchInput, sp);
                Mvp.gui.DetailsWindow.closeDetailsWindow();
            } else {
                alert("Data table unavailable for " + title);
            }
        },
        // Some Full Search result columns have '#' separators.  If we display that column, we want to remove those.
        hashColumnRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = value;
            if (value.length > 2) {
                html = value.substr(1, value.length - 2).replace(/#/g, ',');
            }
            //var valueArray = value.split('#');
            //var html = '';
            //var i;
            //for (i=1; i<valueArray.length-1; i++) {
            //    html += (valueArray[i] + ((i < valueArray.length -2) ? ',' : '')) ;
            //}
            return html;
        },

        // Some Full Search result columns have '|' separators.  If we display that column, we want to remove those.
        pipeColumnRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = value;
            if (value.length > 1) {
                html = value.replace(/\|/g, ', ');
            }
            return html;
        },

        categoryIconRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html;
            metaData.css = 'icon-align';
            switch (value) {
                case 'ConeSearch': html = '<center><img title="A catalog of objects" alt="A catalog of objects" src="' + Mvp.util.Constants.GENERIC_ICON + '" /></center>'; break;
                case 'SimpleImageAccess': html = '<center><img title= "A collection of images" alt= "A collection of images" src="' + Mvp.util.Constants.IMAGE_ICON + '" /></center>'; break;
                case 'SimpleSpectralAccess': html = '<center><img title="A collection of spectra" alt="A collection of spectra" src="' + Mvp.util.Constants.SPECTRA_ICON + '" /></center>'; break;
                case 'Observation': html = '<center><img title="A collection of observations" alt="A collection of observations" src="' + Mvp.util.Constants.OBSERVATION_ICON + '" /></center>'; break;
                default: html = '';
            }
            return html;
        },

        publisherIconRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html;
            switch (value) {
                case 'MAST': html = '<img title="Mikulski Archive for Space Telescopes" alt="Mikulski Archive for Space Telescopes" src="' + Mvp.util.Constants.MAST_16_ICON + '" />'; break;
                //default: html = '<center>' + value + '</center>';
                default: html = '<p style="white-space: normal;font-size: 13px">' + value + '</p>'; //'<center>' + value + '</center>';
                //default: html = value; //'<center>' + value + '</center>';
            }
            return html;
        }
    }
        
})