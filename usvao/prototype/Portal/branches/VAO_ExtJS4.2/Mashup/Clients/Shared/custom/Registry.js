Ext.define('Mvp.custom.Registry', {
    requires: ['Mvp.util.Exporter'],

    statics: {
        loadResourceXml: function (record) {
            //From Mvpc.view.RegistryDetailsContainer
            //url temporarily stored here.
            //note because of an IIS config bug with downloads and the new getRecord page, continue using the nvo...vor10 url for now
            var id = record.get('identifier'),
                title = record.get('title');
            var url = 'http://nvo.stsci.edu/vor10/getRecord.aspx?format=xml&id=' + id;
            Mvp.util.Exporter.downloadUrl(url, title + '.xml', true);
        }
    }
});