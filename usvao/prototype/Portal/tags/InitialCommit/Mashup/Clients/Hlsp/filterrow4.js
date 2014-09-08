Ext.define('Ext.ux.grid.FilterRow', {
    extend: 'Ext.util.Observable',
    nkeypress: 0,
    init: function (grid) {
        this.grid = grid;
        this.applyTemplate();

        grid.on("staterestore", this.resetFilterRow, this);

        // when column width programmatically changed
        //q.f. append event to header with function
        grid.headerCt.on("columnresize", this.resizeFilterField, this);

        grid.headerCt.on("columnmove", this.resetFilterRow, this);
        grid.headerCt.on("columnshow", this.resetFilterRow, this);
        grid.headerCt.on("columnhide", this.resetFilterRow, this);

        grid.horizontalScroller.on('bodyscroll', this.scrollFilterField, this);
    },

    applyTemplate: function () {

        var searchItems = [];
        this.eachColumn(function (col) {
            var filterDivId = this.getFilterDivId(col.id);

            //check wether the filter field is set, if not, set it with Ext.apply
            if (!col.filterField) {
                if (col.nofilter) {
                    //q.f. if(col.nofilter || col.cls == 'x-column-header-checkbox ')
                    //q.f. an exception so that the filter won't appear if you create a checkcolumn
                    col.filterrow = {};
                } else if (!col.filterrow) {
                    col.filterrow = {};
                    col.filterrow.xtype = 'textfield';
                }
                //console.log(col);
                col.filterrow = Ext.apply({
                    id: filterDivId,
                    hidden: col.hidden,
                    xtype: 'component',
                    cls: "small-editor filter-row-icon",
                    width: col.width - 2,
                    enableKeyEvents: true,
                    style: {
                        margin: '1px 1px 1px 1px'
                    }
                }, col.filterrow);

                col.filterField = Ext.ComponentManager.create(col.filterrow);

            } else {
                if (col.hidden != col.filterField.hidden) {
                    col.filterField.setVisible(!col.hidden);
                }
            }

            if (col.filterField.xtype == 'combo' || col.filterField.xtype == 'datefield') {
                col.filterField.on("change", this.onChange, this);
            } else {
                col.filterField.on("keypress", this.onKeyPress, this);
            }

            searchItems.push(col.filterField);
        });

        //if number of filter box is more than 0 put them in the grid header
        if (searchItems.length > 0) {
            this.grid.addDocked(this.dockedFilter = Ext.create('Ext.container.Container', {
                id: this.grid.id + 'docked-filter',
                weight: 100,
                dock: 'top',
                border: false,
                baseCls: Ext.baseCSSPrefix + 'grid-header-ct',
                items: searchItems,
                layout: {
                    type: 'hbox'
                }
            }));
        }
    },

    // Removes filter fields from grid header and recreates
    // template. The latter is needed in case columns have been
    // reordered.
    resetFilterRow: function () {
        this.grid.removeDocked(this.grid.id + 'docked-filter', true);
        delete this.dockedFilter;

        //This is because of the reconfigure
        if (document.getElementById(this.grid.id + 'docked-filter')) {
            var dockedFilter = document.getElementById(this.grid.id + 'docked-filter');
            dockedFilter.parentElement.removeChild(dockedFilter)
        }

        //clear the filter boxes
        this.eachColumn(function (col) {
            if (col.filterField.xtype != 'component') {
                if (col.filterField.getValue() != '') {
                    col.filterField.setValue('');
                }
                //console.log("96:detect box value" + col.filterField.getValue());
            }
        });

        this.applyTemplate();
    },

    onChange: function () {
        var values = {};
        this.eachColumn(function (col) {
            if (col.filterField.xtype != 'component') {
                values[col.dataIndex] = col.filterField.getValue();
            }
        });
        this.processFiltering(values);

    },

    onKeyPress: function (field, e) {
        if (e.getKey() == e.ENTER) {

            var values = {};
            this.eachColumn(function (col) {
                if (col.filterField.xtype != 'component') {
                    values[col.dataIndex] = col.filterField.getValue();
                };
            });
            this.processFiltering(values);
        }
    },

    mynumfilter: function (op, colname, val) {
        //console.log("line126:" + val);
        //console.log("line127: " + store.getCount());
        this.grid.store.filterBy(function (record, id) {
            //store.filterBy(function (record, id) {
            switch (op) {
                case 'eq':
                    return record.get(colname) == val;
                    break;
                case 'gt':
                    return record.get(colname) > val;
                    break;
                case 'lt':
                    return record.get(colname) < val;
                    break;
                case 'gteq':
                    return record.get(colname) >= val;
                    break;
                case 'lteq':
                    return record.get(colname) <= val;
                    break;
                case 'range':
                    if (val[0] > val[1]) {
                        return record.get(colname) >= val[1] && record.get(colname) <= val[0];
                    } else if (val[0] < val[1]) {
                        return record.get(colname) >= val[0] && record.get(colname) <= val[1];
                    } else {
                        return record.get(colname) == val[0];
                    }
            }
        }, this);
        //console.log("line165: " + store.getCount());
    },

    ///
    processFiltering: function (values) {

        this.grid.store.remoteFilter = false;
        this.grid.store.clearFilter(true);
        var fBoxCount = 0;
        var filteredStore = this.grid.store;
        for (var i in values) {
            if (values[i] != '' && values[i] != null) {

                //process hp_filecount number filter
                if (i == 'hp_filecount') {
                    var numstring = Ext.String.escape(values[i]);
                    var casenum = {};
                    switch (numstring.substring(0, 1)) {
                        case '=':
                            if (Ext.isNumeric(numstring.substring(1))) {
                                this.mynumfilter('eq', i, numstring.substring(1));
                            } else {
                                casenum['err'] = 'number only.';
                            };
                            break;
                        case '>':
                            if (Ext.isNumeric(numstring.substring(1))) {
                                this.mynumfilter('gt', i, numstring.substring(1));
                            } else if (Ext.isNumeric(numstring.substring(2)) && numstring.substring(0, 2) == '>=') {
                                this.mynumfilter('gteq', i, numstring.substring(2));
                            } else {
                                casenum['err'] = 'number only.';
                            }
                            break;
                        case '<':
                            if (Ext.isNumeric(numstring.substring(1))) {
                                this.mynumfilter('lt', i, numstring.substring(1));
                            } else if (Ext.isNumeric(numstring.substring(2)) && numstring.substring(0, 2) == '<=') {
                                this.mynumfilter('lteq', i, numstring.substring(2));
                            } else {
                                casenum['err'] = 'number only.';
                            }
                            break;
                        default:
                            if (Ext.isNumeric(numstring)) {
                                this.mynumfilter('eq', i, numstring);
                            } else {
                                var nrang = numstring.split('..', 2);
                                if (nrang && Ext.isNumeric(nrang[0]) && Ext.isNumeric(nrang[1])) {
                                    this.mynumfilter('range', i, nrang);
                                } else {
                                    casenum['err'] = 'number only.';
                                }
                            }
                    };

                } else {
//                    console.log("line221:b " + filteredStore.getCount());
//                    filteredStore.filterBy(function (record, id) {
//                        return record.get('hp_filecount') < 8;
//                    });
                    //console.log("line221:a " + newdataset.getCount());
                    //filteredStore.removeAll();
                    //filteredStore.add(newdataset);
                    var regexp = new RegExp(Ext.String.format('{0}', Ext.String.escape(values[i])), 'i');
                    //this.grid.store.filter(i, regexp);
                    filteredStore.filter(i, regexp);

//                    console.log("line231:b " + filteredStore.getCount());
//                    filteredStore.filterBy(function (record, id) {
//                        return record.get('hp_filecount') < 80;
//                    });
//                    console.log("line231:a " + filteredStore.getCount());

                }

                fBoxCount++;

            }
            //console.log("144:" + fBoxCount);
        }

        //if no filter parameters supplied
        if (fBoxCount < 1) {
            this.grid.store.clearFilter(true);
            this.grid.store.load();
            //console.log("count " + this.grid.store.getCount());
        }
    },

    // Resizes filter field according to the width of column
    resizeFilterField: function (headerCt, column, newColumnWidth) {
        var editor;
        if (!column.filterField) {
            //This is because of the reconfigure
            this.resetFilterRow();
            editor = this.grid.headerCt.items.findBy(function (item) { return item.dataIndex == column.dataIndex; }).filterField;
        } else {
            editor = column.filterField;
        }
        editor.setWidth(newColumnWidth - 2);
    },

    scrollFilterField: function (e, target) {
        var width = this.grid.headerCt.el.dom.firstChild.style.width;
        this.dockedFilter.el.dom.firstChild.style.width = width;
        this.dockedFilter.el.dom.scrollLeft = target.scrollLeft;
    },

    // Returns HTML ID of element containing filter div
    getFilterDivId: function (columnId) {
        return this.grid.id + '-filter-' + columnId;
    },

    // Iterates over each column that has filter
    eachFilterColumn: function (func) {
        this.eachColumn(function (col, i) {
            if (col.filterField) {
                func.call(this, col, i);
            }
        });

    },

    // Iterates over each column in column config array
    eachColumn: function (func) {
        Ext.each(this.grid.columns, func, this);
    }
});