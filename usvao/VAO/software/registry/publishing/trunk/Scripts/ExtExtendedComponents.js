// custom Vtype for vtype:'PositiveInteger'
Ext.apply(Ext.form.field.VTypes, {
    PositiveInteger: function (v) {
        return /^\d+$/.test(v);
    },
    PositiveIntegerText: 'This field must be a positive integer, no commas or periods as delimiters'
});

// custom Vtype for vtype:'DecimalNumber'
Ext.apply(Ext.form.field.VTypes, {
    DecimalNumber: function (v) {
        return /^-?\d+\.?\d*$/.test(v);
    },
    DecimalNumberText: 'This field must be a decimal number with an optional period for the decimal mark, no commas as a thousands separator'
});

// custom Vtype for vtype:'UTCDateTime'
Ext.apply(Ext.form.field.VTypes, {
    UTCDateTime: function (v) {
        return /^\d{4}-\d\d-\d\d(T\d\d:\d\d:\d\d(\.\d+)?)?$/.test(v);
    },
    UTCDateTimeText: 'This field must be a date or datetime in UTC format. YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS or YYYY-MM-DDTHH:MM:SS.ms '
});

Ext.define('tagtest', {
    extend: 'Ext.data.Model',
    fields: [
            { type: 'string', name: 'datafield' }
        ]
});

var test = [
        { 'datafield': 'a'},
        { 'datafield': 'ab' },
        { 'datafield': 'abc' }
 ];

var storeAutosuggest = Ext.create('Ext.data.Store', { model: 'tagtest', data: test });

Ext.define('PublishingWizard.AutoSuggestBox', {
    extend: 'Ext.form.ComboBox',
    alias: 'widget.autosuggest',
    store: storeAutosuggest,
    displayField: 'datafield',
    valueField: 'datafield',
    typeAhead: true,
    triggerAction: 'all',
    selectOnFocus: true,
    hideTrigger: true
});