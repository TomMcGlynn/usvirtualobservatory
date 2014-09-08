Ext.define('Mvp.custom.MastMission', {
    statics: {
        urlGenerator: function (value, mission, record) {
            if (mission.match(/GALEX/i) !== null) {
                html = value;
                url = value;
            } else if (mission.match(/KEPLER/i) !== null) {
                url = 'http://archive.stsci.edu/kepler/preview.php?dsn=' + value + '&type=' + record.get('Target Type') + value;
                html = '<a target="_blank" href="' + url + '">' + value + '</a>';

            } else if (mission.match(/SWIFT/i) !== null) {
                var filter = record.get('Filter');
                var filterCode;
                if (filter == 'UGRISM') filterCode = 'ugu';
                else if (filter == 'U') filterCode = 'uuu';
                else if (filter == 'B') filterCode = 'ubb';
                else if (filter == 'V') filterCode = 'uvv';
                else if (filter == 'UVM2') filterCode = 'um2';
                else if (filter == 'UVW1') filterCode = 'uw1';
                else if (filter == 'UVW2') filterCode = 'uw2';
                url = 'http://archive.stsci.edu/cgi-bin/hla/display?image=sw' + value + filterCode;
                html = '<a target="_blank" href="' + url + '">' + value + '</a>';
            }
            else {
                url = 'http://archive.stsci.edu/cgi-bin/mastpreview?mission=' + mission + '&dataid=' + (mission == 'BEFS' ? value.toUpperCase() : value);
                html = '<a target="_blank" href="' + url + '">' + value + '</a>';
            }
            return { url: url, html: html };
        },

        urlRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var mission = (this.context && this.context.searchInput.mission) || (this.controller && this.controller.searchInput.mission);
            return Mvp.custom.MastMission.urlGenerator(value, mission, record);
        }
    }
});