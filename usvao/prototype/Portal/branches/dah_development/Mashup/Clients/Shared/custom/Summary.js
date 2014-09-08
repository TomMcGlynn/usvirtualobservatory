Ext.define('Mvp.custom.Summary', {
    statics: {
        accessRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            var val = Ext.JSON.decode(value),
                url = val.url,
                status = record.get('Status'),
                allowUrl = ((status == 'EXECUTING') || (status == 'COMPLETE'));
            return (allowUrl ? '<a href="' + url + '">': '') + url + (allowUrl ? '</a>': '');
        },

        statusRenderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
            metaData.css = 'icon-align-nocursor';
            if ((value == 'PENDING') || (value == 'EXECUTING')) {
                return '<center><img src="../Shared/img/loading1.gif" title="Loading" alt="Loading" /></center>';
            } else if (value == 'COMPLETE') {
                return '<center><img src="../Shared/img/checkmark.png" title="Complete" alt="Complete" /></center>';
            } else {
                return '<center><img src="../Shared/img/close_24x24.png" title="Error" alt="Error" /></center>';
            }
        }
    }
});