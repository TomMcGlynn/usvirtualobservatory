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
        Ext.getCmp('recordNum').setText(this.grid.store.getCount() + ' record(s) found');
    },

    onChange: function () {
        var values = {};
        this.eachColumn(function (col) {
            if (col.filterField.xtype != 'component') {
                //values[col.dataIndex] = col.filterField.getValue();
                if (!col.xtype) {
                    values["specialString:" + col.dataIndex] = col.filterField.getValue();
                } else {
                    values[col.xtype + ":" + col.dataIndex] = col.filterField.getValue();
                }
            }
        });
        this.processFiltering(values);

    },

    onKeyPress: function (field, e) {
        if (e.getKey() == e.ENTER) {
            // for filter facets.
            var gridName = this.grid.id;
            var FacetItems = {};
            FacetItems['hp_prodtype'] = 'Product Type';
            FacetItems['hp_wavelength'] = 'Wave Length';
            var values = {};

            for (var filterCol in FacetItems) {
                if (FacetItems[filterCol] != '' && FacetItems[filterCol] != null) {
                    var i = 0
                    var CheckboxValues = '::';
                    //process the filter check boxes on the left
                    while (Ext.getCmp(filterCol + '_ckbx' + i)) {
                        var myfb = Ext.getCmp(filterCol + '_ckbx' + i);
                        if (myfb.checked) {
                            //gather all checked box value
                            CheckboxValues += myfb.inputValue + '::';
                        }
                        i++;
                    }
                    values["FilterPanel:" + filterCol] = CheckboxValues;
                }
            };
            /////

            this.eachColumn(function (col) {
                if (col.filterField.xtype != 'component') {
                    //values[col.dataIndex] = col.filterField.getValue();
                    if (!col.xtype) {
                        if (col.dataIndex == 'hp_prodtype') {
                            values["FilterPanel:hp_prodtype"] += (values["FilterPanel:hp_prodtype"]) ? col.filterField.getValue() : '';
                        } else if (col.dataIndex == 'hp_wavelength') {
                            values["FilterPanel:hp_wavelength"] += (values["FilterPanel:hp_wavelength"]) ? col.filterField.getValue() : '';
                        } else {
                            values["specialString:" + col.dataIndex] = col.filterField.getValue();
                        }
                    } else {
                        values[col.xtype + ":" + col.dataIndex] = col.filterField.getValue();
                    }

                };
            });

            this.processFiltering(values);
        }
    },

    ///
    //op: (!)negate / (no !) no_negate
    strNumFilter: function (op, numString, colTitle) {
        var CString = 'record.get("' + colTitle + '")';
        switch (numString.substr(0, 1)) {
            case '=':
                CString += ((op == 'nneg') ? " == " : " != ") + numString.substr(1);
                break;
            case '>':
                if (numString.substr(1, 1) != '=') {
                    CString += ((op == 'nneg') ? " > " : " <= ") + numString.substr(1);
                } else if (numString.substr(0, 2) == '>=') {
                    CString += ((op == 'nneg') ? " >= " : " < ") + numString.substr(2);
                }
                break;
            case '<':
                if (numString.substr(1, 1) != '=') {
                    CString += ((op == 'nneg') ? " < " : " >= ") + numString.substr(1);
                } else if (numString.substr(0, 2) == '<=') {
                    CString += ((op == 'nneg') ? " <= " : " > ") + numString.substr(2);
                }
                break;
            default:
                if (Ext.isNumeric(numString)) {
                    CString += ((op == 'nneg') ? " == " : " != ") + numString;
                } else {
                    //process range	
                    var nrange = numString.split('..', 2);
                    if (nrange && Ext.isNumeric(nrange[0]) && Ext.isNumeric(nrange[1])) {
                        if ((nrange[0] - nrange[1]) > 0) {
                            CString += ((op == 'nneg') ? " > " + nrange[1] + " && " : " <= " + nrange[1] + " || ") + 'record.get("' + colTitle + '")' + ((op == 'nneg') ? " < " : " >= ") + nrange[0];
                        } else if ((nrange[1] - nrange[0]) > 0) {
                            CString += ((op == 'nneg') ? " < " + nrange[1] + " && " : " >= " + nrange[1] + " || ") + 'record.get("' + colTitle + '")' + ((op == 'nneg') ? " > " : " <= ") + nrange[0];
                        } else {
                            CString += ((op == 'nneg') ? " == " : " != ") + nrange[0];
                        }
                    } else {
                        //process as regular string
                        CString += ((op == 'nneg') ? " == " : " != ") + numString;
                    }
                }
        };
        return "(" + CString + ")";
    },

    //op: (!)negate / (no !) no_negate
    strDateFilter: function (op, dateString, colTitle) {
        var CString = 'record.get("' + colTitle + '")';
        switch (dateString.substr(0, 1)) {
            case '=':
                var dString = new Date(dateString.substr(1));
                CString += ((op == 'nneg') ? " == " : " != ") + dString.getTime();
                break;
            case '>':
                if (dateString.substr(1, 1) != '=') {
                    var dString = new Date(dateString.substr(1));
                    CString += ((op == 'nneg') ? " > " : " <= ") + dString.getTime();
                } else if (dateString.substr(0, 2) == '>=') {
                    var dString = new Date(dateString.substr(2));
                    CString += ((op == 'nneg') ? " >= " : " < ") + dString.getTime();
                }
                break;
            case '<':
                if (dateString.substr(1, 1) != '=') {
                    var dString = new Date(dateString.substr(1));
                    CString += ((op == 'nneg') ? " < " : " >= ") + dString.getTime();
                } else if (dateString.substr(0, 2) == '<=') {
                    var dString = new Date(dateString.substr(2));
                    CString += ((op == 'nneg') ? " <= " : " > ") + dString.getTime();
                }
                break;
            default:
                if (Ext.isDate(dateString)) {
                    var dString = new Date(dateString);
                    CString += ((op == 'nneg') ? " == " : " != ") + dString.getTime();
                } else {
                    //process date range -- to do	
                    var drange = dateString.split('..', 2);
                    if (drange && Ext.isDate(drange[0]) && Ext.isDate(drange[1])) {
                        if ((drange[0] - drange[1]) > 0) {
                            CString += ((op == 'nneg') ? " > " + drange[1] + " && " : " <= " + drange[1] + " || ") + 'record.get("' + colTitle + '")' + ((op == 'nneg') ? " < " : " >= ") + drange[0];
                        } else if ((nrange[1] - nrange[0]) > 0) {
                            CString += ((op == 'nneg') ? " < " + drange[1] + " && " : " >= " + drange[1] + " || ") + 'record.get("' + colTitle + '")' + ((op == 'nneg') ? " > " : " <= ") + drange[0];
                        } else {
                            CString += ((op == 'nneg') ? " == " : " != ") + drange[0];
                        }
                    } else {
                        //process as regular string
                        CString += ((op == 'nneg') ? " == " : " != ") + dateString;
                    }
                }
        };
        return "(" + CString + ")";
    },
    ///
    processFiltering: function (values) {
        this.grid.store.remoteFilter = false;
        //this.grid.store.clearFilter(true);
        var fBoxCount = 0;
        for (var i in values) {
            if (values[i] != '' && values[i] != null && values[i] != '::')
            { fBoxCount++; }
        }

        console.log("[filterrow4.line247]: " + fBoxCount + 'box');

        if (fBoxCount < 1) {
            //if no filter parameters supplied
            this.grid.store.clearFilter(true);
            this.grid.store.load();
        } else {
            this.grid.store.filterBy(function (record, id) {

                //prepare the filtering criteria.                
                var CriteriaString = [];
                var j = 0;
                for (var i in values) {
                    if (values[i] != '' && values[i] != null) {
                        //identify filter type: number, date, string
                        var thisFilterType = i.split(':', 2);
                        switch (thisFilterType[0]) {
                            case 'numbercolumn':
                                //process number 
                                var numString = Ext.String.escape(values[i]);
                                if (numString.substr(0, 1) == '!') {
                                    CriteriaString[j] = this.strNumFilter('neg', numString.substr(1), thisFilterType[1]);
                                } else {
                                    CriteriaString[j] = this.strNumFilter('nneg', numString, thisFilterType[1]);
                                }
                                break;

                            case 'datecolumn':
                                //process date
                                var dateString = values[i];
                                if (dateString.substr(0, 1) == '!') {
                                    CriteriaString[j] = this.strDateFilter('neg', dateString.substr(1), thisFilterType[1]);
                                } else {
                                    CriteriaString[j] = this.strDateFilter('nneg', dateString, thisFilterType[1]);
                                }
                                break;

                            case 'FilterPanel':
                                //process input from filter panel
                                var FPcheckbxVal = values[i].split('::');

                                var queryStr = '';
                                if (FPcheckbxVal.length - 2 > 0) {
                                    for (var i = 1; i < FPcheckbxVal.length - 1; i++) {
                                        queryStr += 'record.get("' + thisFilterType[1] + '").match("^' + FPcheckbxVal[i] + '") ||';
                                    }
                                    CriteriaString[j] = '(' + queryStr.substr(0, queryStr.length - 2) + ')';
                                } else {
                                    CriteriaString[j] = '';
                                }
                                break;

                            default:
                                //process string
                                var regexp = new RegExp(Ext.String.format('{0}', Ext.String.escape(values[i])), 'i');
                                CriteriaString[j] = 'record.get("' + thisFilterType[1] + '").match(' + regexp + ')';
                        }
                        j++;
                    }
                };
                var newstring = '';
                for (var j = 0; j < CriteriaString.length; j++) {
                    if (CriteriaString[j] != '') {
                        newstring += CriteriaString[j] + " && ";
                    }
                }
                newstring = newstring.substr(0, newstring.length - 4);
                console.log("line339:" + newstring);
                return eval(newstring);
            }, this);
        }
        Ext.getCmp('recordNum').setText(this.grid.store.getCount() + ' record(s) found');
    },

    // Resizes filter field according to the width of column
    resizeFilterField: function (headerCt, column, newColumnWidth) {
        var editor;
        if (!column.filterField) {
            //This is because of the reconfigure  
            console.log('h2');
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