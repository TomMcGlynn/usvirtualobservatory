
Ext.define('Mvp.custom.FullSearch', {
    statics: {
        // Some Full Search result columns have '#' separators.  If we display that column, we want to remove those.
        hashColumnRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var html = value;
            if (value.length > 2) {
                html = value.substr(1,value.length-2).replace(/#/g,',');
            }
            //var valueArray = value.split('#');
            //var html = '';
            //var i;
            //for (i=1; i<valueArray.length-1; i++) {
            //    html += (valueArray[i] + ((i < valueArray.length -2) ? ',' : '')) ;
            //}
            return html;
        }
    }
        
})