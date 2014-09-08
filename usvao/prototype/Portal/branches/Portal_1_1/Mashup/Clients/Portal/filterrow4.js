Ext.define('DemoApp.FilterRow', {
    extend: 'Ext.util.Observable',

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
            if (!col.xfilterField) {
                if (col.nofilter || col.isCheckerHd != undefined) {
                    col.xfilter = {};
                } else if (!col.xfilter) {
                    col.xfilter = {};
                    col.xfilter.xtype = 'textfield';
                }
                col.xfilter = Ext.apply({
                    id: filterDivId,
                    hidden: col.hidden,
                    xtype: 'component',
                    cls: "xfilter-row",
                    width: col.width - 2,
                    enableKeyEvents: true,
                    style: {
                        margin: '1px 1px 1px 1px'
                    },
                    hideLabel: true
                }, col.xfilter);

                col.xfilterField = Ext.ComponentManager.create(col.xfilter);

            } else {
                if (col.hidden != col.xfilterField.hidden) {
                    col.xfilterField.setVisible(!col.hidden);
                }
            }

            if (col.xfilterField.xtype == 'combo') {
                col.xfilterField.on("select", this.onSelect, this);
            } else if (col.xfilterField.xtype == 'datefield') {
                col.xfilterField.on("change", this.onChange, this);
            }

            col.xfilterField.on("keydown", this.onKeyDown, this);

            searchItems.push(col.xfilterField);
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

        this.applyFilterRowXtype();
    },

    // Removes filter fields from grid header and recreates
    // template. The latter is needed in case columns have been
    // reordered.
    resetFilterRow: function () {
        var dockedFilter = document.getElementById(this.grid.id + 'docked-filter');

        if (dockedFilter) {
            this.eachColumn(function (col) {
                var filterDivId = this.getFilterDivId(col.id);
                if (col.hidden != col.xfilterField.hidden) {
                    col.xfilterField.setVisible(!col.hidden);
                }
            });

        }

        //clear the filter boxes
        this.eachColumn(function (col) {
            if (col.xfilterField.xtype != 'component') {
                if (col.xfilterField.getValue() != '') {
                    col.xfilterField.setValue('');
                }
            }
        });

    },

    onChange: function () {
        var values = {};
        this.eachColumn(function (col) {
            if (col.xfilterField.xtype != 'component') {
                values[col.dataIndex] = col.xfilterField.getValue();
            }
        });
        this.processFiltering(values);

    },

    onKeyDown: function (field, e) {
        if (e.getKey() == e.ENTER) {

            var values = {};
            this.eachColumn(function (col) {
                if (col.xfilterField.xtype != 'component') {
                    values[col.xfilterField.xtype + ":" + col.dataIndex] = col.xfilterField.getValue();
                };
            });

            this.processFiltering(values);

            //get Mvp.filters.FacetFilterPanel selected box values.
            //var thisgridDom = Ext.getDom(this.grid.id);
            //console.log('get f ' + thisgridDom.parentNode.parentNode.parentNode.parentNode.parentNode.id);
            var filterFacetPnl = Ext.getCmp('tlWestContainer');
            var fFacets = filterFacetPnl.items;
            for (var i = 0; i < fFacets.length; i++) {
                var thisFacet = fFacets.getAt(i);
                var thisFGrid = Ext.getCmp(thisFacet.id); //  Mvp.filters.FacetFilterPanel.
                var thisFGSelected = thisFGrid.getSelectionModel().getSelection();
                thisFGrid.selectionListener(thisFGrid.getSelectionModel(), thisFGSelected);

                /*
                for (var row = 0; row < thisFGSelected.length; row++) {
                    var key = thisFGSelected[row].get('key');
                    console.log('fpnl ' + key);
                }  */
            }
        }
    },

    processFiltering: function (values) {

        this.grid.store.remoteFilter = false;
        //this.grid.store.clearFilter(true);
        var fBoxCount = 0;
        for (var i in values) {
            if (values[i] != '' && values[i] != null && values[i] != '::')
            { fBoxCount++; }
        }

        Ext.log("[filterrow4.l59]: " + fBoxCount + ' box');

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
                                var regexp = new RegExp(Ext.String.format('{0}', Ext.String.escape(values[i])), 'g,i');
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
                Ext.log("line227:" + newstring);
                return eval(newstring);
            }, this);
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

    //apply filterField.xtype 
    //get first record to identify whether it is numeric or not
    // this can be done in the web service which will be much easier and less 
    // this is just a temp quick fix for identifying which field is numeric so 
    // corresponding filter can pick it up 
    // *note: needs to be improved for empty and null fields 
    applyFilterRowXtype: function () {
        var records = this.grid.store.getRange();
        this.eachColumn(function (col) {
            if (col.dataIndex != '') {
                if (Ext.isNumeric(records[0].data[col.dataIndex])) {
                    col.xfilterField.xtype = 'numbercolumn';
                }
            }

        });
    },


    // Resizes filter field according to the width of column
    resizeFilterField: function (headerCt, column, newColumnWidth) {
        var editor;
        if (!column.xfilterField) {
            //This is because of the reconfigure
            this.resetFilterRow();
            editor = this.grid.headerCt.items.findBy(function (item) { return item.dataIndex == column.dataIndex; }).xfilterField;
        } else {
            editor = column.xfilterField;
        }

        if (editor) {
            editor.setWidth(newColumnWidth - 2);
        }
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
            if (col.xfilterField) {
                func.call(this, col, i);
            }
        });

    },

    // Iterates over each column in column config array
    eachColumn: function (func) {
        Ext.each(this.grid.columns, func, this);
    }
});