/**
 * Helper function for instantiating a Renderer object.
 * 
 * @param {Filter} rendererParams.filterObject Filter object to be used for
 *            generating filter tables from the original VOTABLE.
 * 
 * @returns {Renderer} Newly created Renderer object.
 * 
 */
voview.prototype.makeRenderer = function(rendererParams) {
    /**
     * @class Object used for rendering the HTML display for VOView. Also
     *        contains the callback methods that are embedded as javascript in
     *        the HTML display.
     * @param {Filter} filterObject Filter object to be used for generating
     *            filtered tables from the original VOTABLE.
     * 
     */
    function Renderer(_filterObject, objectName, widgetIDprefix) {
        var meRenderer = this;
        var filterObject = _filterObject;
        var renderDOM = null;
        var renderProcessor = new XSLTProcessor();
        var resultCallback = null;
        var filteredDOM = null;
        var fieldOrder;
        var fieldUCD;
        var fieldNames;
        var fieldColumn;
        var maxColumns = null;
        var columnOrder = null;
        var filterText = "";
        var titleText = null;
        var columnFormats = [];
        var colFormatTypes = [];
        var selectedRows = {};
        var rowSelection = false;

        // Default row range
        filterObject.setRowRange({ firstRow: 1, lastRow: 10 });

        /*
         * Function which takes the filtered VOTABLE and generates the HTML for
         * the page to be displayed.
         * 
         * @param {XML Dom object} _filteredDOM The filtered VOTABLE.
         */
        function renderTable(_filteredDOM) {
            filteredDOM = _filteredDOM;
            meRenderer.setTitle();

            var fields = meRenderer.selectNodes(filteredDOM, "//*[local-name()='FIELD']");
            fieldNames = [];
            fieldUCD = [];
            fieldOrder = {};
            for ( var i = 0; i < fields.length; i++) {
                fieldNames[i] = fields[i].getAttribute("name");
                fieldOrder[fields[i].getAttribute("name")] = i + 1;
                if (fields[i].getAttribute("ucd")) {
                    fieldUCD[i + 1] = fields[i].getAttribute("ucd");
                }
            }

            if (columnOrder !== null && typeof (columnOrder[0]) !== "number") {
                for ( var j = 0; j < columnOrder.length; j++) {
                    columnOrder[j] = fieldOrder[columnOrder[j]];
                }
            }
            
            if (columnOrder === null) {
                columnOrder = [];
                for ( var k = 0; k < fields.length; k++) {
                    columnOrder[k] = k + 1;
                }
            }
            
            fieldColumn = [];
            for ( var j = 0; j < columnOrder.length; j++) {
                fieldColumn[columnOrder[j]] = j;
            }
                

            var displayFragment = null;
            if (renderDOM !== null) {
                /**
                 * Debug printout
                 */
                var xmlstring;
                var debugElement = document.getElementById("debug_output");
                if( debugElement ){
                    xmlstring = (new XMLSerializer()).serializeToString(renderDOM);
                    xmlstring = xmlstring.replace(/</g,"&lt;"); 
                    xmlstring = xmlstring.replace(/>/g,"&gt;\n");
                    xmlstring = debugElement.innerHTML + 
                        "\nRenderer DOM:\n\n" + xmlstring;
                    debugElement.innerHTML = xmlstring;
                }
                
                if (renderProcessor.reset) {
                    renderProcessor.reset();
                }
                try {
                    renderProcessor.importStylesheet(renderDOM);

                    renderProcessor.setParameter(null, "pageCallback", objectName + ".renderObject.newPage");
                    renderProcessor.setParameter(null, "setPageLength", objectName + ".renderObject.setPageLength");
                    renderProcessor.setParameter(null, "setMaxColumnsCallback",
                            objectName + ".renderObject.setMaxColumns");
                    renderProcessor.setParameter(null, "sortCallback", objectName + ".renderObject.sortTable");
                    renderProcessor.setParameter(null, "sortCallback", objectName + ".renderObject.sortTable");

                    renderProcessor.setParameter(null, "filterCallback", objectName + ".renderObject.filterByColumn");
                    renderProcessor.setParameter(null, "filterResetCallback", objectName + ".renderObject.filterReset");

                    renderProcessor.setParameter(null, "clickClearCallback", objectName + ".renderObject.clickClear");
                    renderProcessor.setParameter(null, "clickResetCallback", objectName + ".renderObject.clickRecall");

                    renderProcessor.setParameter(null, "widgetIDprefix", widgetIDprefix);
                    renderProcessor.setParameter(null, "filterText", filterText);

                    renderProcessor.setParameter(null, "titleText", titleText);

                    if (maxColumns !== null) {
                        renderProcessor.setParameter(null, "maxColumns", maxColumns);
                    }
                    if (columnOrder !== null) {
                        renderProcessor.setParameter(null, "columnOrder", columnOrder.join(","));
                    }
                    renderProcessor.setParameter(null, "resetColumnOrderCallback",
                            objectName + ".renderObject.resetColumnOrder");

                    displayFragment = renderProcessor.transformToDocument(filteredDOM);
                    
                    /**
                     * Debug printout
                     */
                    if( debugElement ){
                        xmlstring = (new XMLSerializer()).serializeToString(displayFragment);
                        xmlstring = xmlstring.replace(/</g,"&lt;"); 
                        xmlstring = xmlstring.replace(/>/g,"&gt;\n");
                        xmlstring = debugElement.innerHTML + 
                            "\nDisplay fragment DOM::\n\n" + xmlstring;
                        debugElement.innerHTML = xmlstring;
                    }

                } catch (e1) {
                    alert("VOView: Error rendering XML doc: " + e1.message);
                }
            }            

            resultCallback(displayFragment);
        }

        /**
         * Callback function for tableDnD, which reads the column order from the
         * column table and sets the order in the renderer object.
         * 
         * @param {HTML Dom object} table The column table.
         * @param {HTML Dom object} row The row of the column table that was
         *            moved.
         */
        function setColumnOrder(table, row) {
            // determine the column order from the table
            var rows = table.tBodies[0].rows;
            var maxcolumns = rows.length - 1;
            var order = [];
            for ( var i = 0; i < rows.length; i++) {
                var classname = rows[i].className || "";
                if (classname.indexOf("separator") >= 0) {
                    maxcolumns = i;
                } else {
                    // ID for row is 'fieldrow_<number>'
                    var f = rows[i].id.split('_');
                    order.push(parseInt(f[f.length - 1], 10));
                }
            }
            if(maxcolumns === 0){
                maxcolumns = 1;
            }
            
            meRenderer.setDisplayedColumns(order);
            meRenderer.setDisplayedColumns(maxcolumns);
            meRenderer.render();
        }

        function applyUserFormats(tableDiv) {
            var tables = tableDiv.getElementsByTagName("table");
            var rows = tables[0].rows;

            if (colFormatTypes.length > 0) {

                for ( var ifield = 0; ifield < fieldNames.length; ifield++) {
                    var format = null;
                    for ( var iformat = 0; iformat < colFormatTypes.length; iformat++) {
                        var apply = false;
                        var formatType = colFormatTypes[iformat];
                        switch (typeof formatType) {
                        case "number":
                            apply = formatType === ifield;
                            break;
                        case "string":
                            apply = formatType === fieldNames[ifield];
                            break;
                        // Safari thinks that a regex has a type of 'function'
                        case "object":
                        case "function":
                            apply = formatType.test(fieldNames[ifield]);
                            break;
                        }
                        if (apply) {
                            format = columnFormats[iformat];
                            break;
                        }
                    }

                    if (format !== null) {
                        for ( var irow = 0; irow < rows.length; irow++) {
                            if (rows[irow].parentNode.tagName === "TBODY") {
                                var icolumn = fieldColumn[ifield+1];
                                var cells = rows[irow].cells;
                                switch (typeof format) {
                                case "string":
                                    cells[icolumn].innerHTML = format.replace(/@@/g, cells[icolumn].innerHTML);
                                    break;
                                case "function":
                                    format(cells[icolumn]);
                                    break;
                                }
                             }
                        }
                    }
                }
            }
            
            if(rowSelection){
                for ( var jrow = 0; jrow < rows.length; jrow++) {
                    if (rows[jrow].parentNode.tagName === "TBODY") {
                        addRowSelection(rows[jrow].cells[0]);
                    }
                }
            }
        }

        /**
         * Callback function which takes the XML document containing the
         * rendered HTML for the display, and places it in the web page.
         * Different parts of the display can be placed at different locations
         * in the web page based on the IDs of various divs specified in the web
         * page.
         * 
         * @param {XML Dom object} xmlDoc XML document containing the rendered
         *            HTML.
         */
        this.displayHTML = function(xmlDoc) {
            var docElements;
            var widgetName;
            var fragElement;

            var element = meRenderer.selectSingleNode(xmlDoc, "//form[@name='widgets']/input[@name='widget_names']");
            var subwidgets = element.getAttribute("value").split(",");
            for ( var i = 0; i < subwidgets.length; i++) {
                // First time thru, need to delete the "all" widget, because it
                // contains the other widgets
                if (i === 0) {
                    var allElement = null;
                    allElement = document.getElementById(widgetIDprefix);
                    if (allElement) {
                        allElement.innerHTML = "";
                    }
                }

                if (subwidgets[i] === "all") {
                    widgetName = widgetIDprefix;
                } else {
                    widgetName = widgetIDprefix + "." + subwidgets[i];
                }

                docElements = meRenderer.getElementsByClass(widgetName);
                if (docElements.length > 0) {
                    fragElement = meRenderer.selectSingleNode(xmlDoc, "//*[@id=\"" + widgetName + "\"]")
                            .cloneNode(true);
                    for ( var j = 0; j < docElements.length; j++) {
                        docElements[j].innerHTML = (new XMLSerializer()).serializeToString(fragElement);
                    }

                    var divs = document.getElementsByTagName("div");
                    var tableDivs = [];
                    var buttonDivs = [];
                    var idiv;
                    for (idiv = 0; idiv < divs.length; idiv++) {
                        if (divs[idiv].id === widgetIDprefix + ".table") {
                            tableDivs.push(divs[idiv]);
                        }
                        if (divs[idiv].id === widgetIDprefix + ".filterButtons") {
                            buttonDivs.push(divs[idiv]);
                        }
                    }

                    for (idiv = 0; idiv < tableDivs.length; idiv++) {
                        // Apply user formatting to table cells
                        applyUserFormats(tableDivs[idiv]);
                        var table = tableDivs[idiv].getElementsByTagName("table")[0];

                        // Make table columns draggable.
                        dragtable.makeDraggable(table);
                    }

                    // Add buttons for setting and clearing row selections
                    if (rowSelection) {
                        for (idiv = 0; idiv < buttonDivs.length; idiv++) {
                            buttonDivs[idiv].innerHTML = buttonDivs[idiv].innerHTML + "<span class=\"bbox\" onclick=\"return " + objectName + ".renderObject.selectAllRows();\">Select All Rows</span>" + "<span class=\"bbox\" onclick=\"return " + objectName + ".renderObject.clearRowSelection();\">Unselect All Rows</span>" +
                            "<span class=\"bbox\" onclick=\"return alert( " + objectName + ".filterObject.getRowValues(2));\">Show Row 2 Values</span>";
                            // "<span class=\"bbox\" onclick=\"return alert(" +
                            // objectName +
                            // ".renderObject.getSelectedRows());\">Show
                            // Selected Rows</span>";
                        }
                    }
                }
            }

            var ftable = document.getElementById('voview_column_fields');
            if (ftable) {
                var tablednd = new TableDnD.TableDnD();
                tablednd.onDrop = setColumnOrder;
                tablednd.init(ftable);
            }
        };

        /**
         * Webpage callback function which instructs VOView to display a new
         * page of the VOTABLE.
         * 
         * @param {integer} pageNumber The page of the table to display.
         */
        this.newPage = function(pageNumber) {
            var startElement = meRenderer.selectSingleNode(filteredDOM, "//*[@ID='VOV:PageStart']");
            var pageStart = Number(startElement.getAttribute("value"));
            var endElement = meRenderer.selectSingleNode(filteredDOM, "//*[@ID='VOV:PageEnd']");
            var pageEnd = Number(endElement.getAttribute("value"));

            var newStart = (pageEnd - pageStart + 1) * (pageNumber - 1) + 1;
            var newEnd = (pageEnd - pageStart + 1) * (pageNumber);
            filterObject.setRowRange({ firstRow: newStart, lastRow: newEnd });

            meRenderer.render();
        };

        /**
         * Set the maximum number of columns shown in the VOView display. The
         * display is then rendered with the new setting.
         * 
         * @param {integer} columns Maximum number of columns shown in the
         *            display.
         */
        this.setMaxColumns = function(_maxColumns) {
            maxColumns = _maxColumns;
            meRenderer.render();
        };

        /**
         * Set the order and number of columns to display.
         * 
         * @param {integer[]|string[]|integer} columnInfo If an array, contains
         *            a list of columns in the order in which they are to
         *            appear. If an integer array, the numbers correspond to the
         *            original order of the columns in the VOTABLE. If a string
         *            array, the strings must correspond to column names. If a
         *            scalar integer, then this value sets the maximum number of
         *            columns to initially display in the HTML table.
         */
        this.setDisplayedColumns = function(columnInfo) {
            if (typeof (columnInfo) === "number") {
                maxColumns = columnInfo;
            } else if (columnInfo.length) {
                columnOrder = columnInfo;
            } else {
                alert("VOView: columnInfo must be an integer or array");
            }
        };

        /**
         * Reset the column order and maximum number of columns displayed to
         * their default settings, and then render the table.
         */
        this.resetColumnOrder = function() {
            columnOrder = null;
            maxColumns = null;
            meRenderer.render();
        };

        /**
         * Set the number of rows shown in a single page in the VOView display,
         * and then render the table with the new settings.
         * 
         * @param {integer} pageLength The number of rows in a single page in
         *            the VOView display.
         */
        this.setPageLength = function(pageLength) {
            var startElement = meRenderer.selectSingleNode(filteredDOM, "//*[@ID='VOV:PageStart']");
            var pageStart = Number(startElement.getAttribute("value"));
            var length = Number(pageLength);

            var currentPage = Math.ceil(pageStart / pageLength);

            var newStart = length * (currentPage - 1) + 1;
            var newEnd = length * currentPage;
            filterObject.setRowRange({ firstRow: newStart, lastRow: newEnd });

            meRenderer.render();
        };

        /**
         * Set the column to use for sorting the VOTABLE, based on information
         * in the supplied column header element. If this column is already the
         * column being used for sorting, then toggle the sorting direction. The
         * table is then re-rendered.
         * 
         * @param {HTML DOM element} headElement HTML Dom element of the header
         *            cell of the column to be used for sorting.
         */
        this.sortTable = function(headElement) {
            var direction;
            var keys = [];
            if (headElement.className.indexOf("ascending") !== -1) {
                direction = "descending";
            } else {
                direction = "ascending";
            }

            var key = meRenderer.makeSortColumnKey({ column: headElement.innerHTML, direction: direction });
            keys.push(key);

            filterObject.setSortColumns({ sortKeys: keys });
            meRenderer.render();
        };

        /**
         * Takes the form embedded in the VO table display that contains the
         * column filtering expressions, And transforms them into
         * columnFilterKey objects which are then passed to the Filter object
         * for filtering of the table. Coordinate values in sexigesimal format
         * are first converted into decimal format.
         * 
         * @param {HTML DOM form} form The HTML form element containing the
         *            column filter inputs.
         */
        this.filterByColumn = function(form) {
            var keys = [];
            var el;
            var decimal;
            var components;

            filterText = "";

            for ( var j = 0; j < form.elements.length; j++) {
                el = form.elements[j];
                if (el.tagName === "INPUT" && el.type === "text" && !el.className.match("defaultComment")) {
                    var constraint = el.value;
                    if (constraint) {
                        // field number is in trailing digits
                        var i = parseInt(el.name.replace(/.*[^0-9]/, ""), 10);

                        var isCharType = form.elements[el.name + "_type"].value;
                        isCharType.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");
                        if (isCharType.toLowerCase() === "false") {
                            isCharType = false;
                        } else {
                            isCharType = true;
                        }

                        filterText = filterText + "|" + i + ":" + constraint;

                        if (!isCharType && fieldUCD[i] && fieldUCD[i].match(/pos.*eq.*main/i) !== null) {
                            var sexigesimal = constraint.match(/\d+:\d+:\d*\.?[\d ]+/g);
                            if (sexigesimal !== null) {
                                for ( var k = 0; k < sexigesimal.length; k++) {
                                    components = sexigesimal[k].split(":");
                                    decimal = Number(components[0]) + Number(components[1]) / 60 + Number(components[2]) / 3600;
                                    if (fieldUCD[i].match(/ra[\._]/i) !== null) {
                                        decimal = decimal * 360 / 24;
                                    }
                                    constraint = constraint.replace(sexigesimal[k], decimal);
                                }
                            }
                        }

                        var key = meRenderer.makeColumnFilterKey({ column: i, expression: constraint,
                            isCharType: isCharType });
                        keys.push(key);
                    }
                }
            }

            if (filterText !== "") {
                filterText = filterText + "|";
            }

            filterObject.clearColumnFilters();
            filterObject.setColumnFilters({ filterKeys: keys });
            meRenderer.render();
        };

        /**
         * Clear all column filters, so that the entire VOTABLE is available.
         */
        this.filterReset = function() {
            filterObject.clearColumnFilters();
            filterText = "";
            meRenderer.render();
        };

        /**
         * Initiate sequence of events for rendering a table.
         * 
         * @param {function} renderParams.renderCallback Function to be called
         *            after the table is rendered. The sole argument is in the
         *            XML Dom object containing the HTML for the rendered table.
         *            This callback function is generally used to insert the
         *            HTML for the table into the desired webpage.
         */
        this.render = function(renderParams) {
            if (renderParams !== undefined && renderParams.renderCallback !== undefined) {
                resultCallback = renderParams.renderCallback;
            }

            if (renderDOM === null) {
                if (voview.renderer_xsl) {
                    var renderParser = new DOMParser();
                    renderDOM = renderParser.parseFromString(voview.renderer_xsl, "text/xml");
                    filterObject.doFilter({ filterCallback: renderTable });
                } else {
                    var renderDOMCallback = function(data) {
                        renderDOM = data;
                        filterObject.doFilter({ filterCallback: renderTable });
                    };

                    var renderGet = meRenderer.makeGetXml({ fileUrl: "@XSL_PATH@renderer.xsl",
                        dataCallBack: renderDOMCallback });
                    renderGet.send();
                }
            } else {
                filterObject.doFilter({ filterCallback: renderTable });
            }

        };

        /**
         * Function used by input elements as the "onclick" method for managing
         * default comments which disappear when the user clicks in the input
         * field.
         * 
         * @param {HTML input element} thisfield The input element.
         * 
         * @param {string} defaulttext The default text for this input element.
         */
        this.clickClear = function(thisfield, defaulttext) {
            if (thisfield.value === defaulttext) {
                thisfield.value = "";
                var className = thisfield.className;
                thisfield.className = className.replace(/ *defaultComment */g, " ");
            }
        };

        /**
         * Function used by input elements as the "onblur" method for managing
         * default comments which disappear when the user clicks in the input
         * field.
         * 
         * @param {HTML input element} thisfield The input element.
         * 
         * @param {string} defaulttext The default text for this input element.
         */
        this.clickRecall = function(thisfield, defaulttext) {
            if (thisfield.value === "") {
                thisfield.value = defaulttext;
                thisfield.className = thisfield.className + " defaultComment";
            }
        };

        /**
         * Set the title of the VOView table display. If an argument is
         * supplied, this is used as the title. Otherwise, the method attempts
         * to extract the DESCRIPTION field from the VOTABLE to use as the
         * title. If this is not available, it uses the table URL as the title.
         * If no URL is available the title is left blank.
         * 
         * @param {string} title The title to be used for the table.
         */
        this.setTitle = function(title) {
            if (title !== undefined) {
                titleText = title;
                return;
            }

            if (filteredDOM !== null) {
                var nodes = meRenderer.selectNodes(filteredDOM, "//*[local-name()='TABLE']/DESCRIPTION/text()");
                if (nodes.length > 0) {
                    titleText = nodes[0].nodeValue;
                    return;
                }
            }

            if (meRenderer.votableUrl !== null) {
                // Remove the proxy stuff if it was inserted
                var url;
                var proxyExpression = /proxy\.pl\?(.*)/;

                if (proxyExpression.test(meRenderer.votableUrl)) {
                    url = RegExp.$1;
                    url = decodeURIComponent(url);
                } else {
                    url = meRenderer.votableUrl;
                }

                titleText = url.match(/[^\/]*$/)[0];
                return;
            }

            titleText = "";
        };

        /**
         * Add additional formatting to a column. The function can be called
         * when formatting the column, which can be used for formatting other
         * parts of the road as well, e.g. an empty column at the beginning of
         * the table if it exists.
         * 
         * @param column {String|integer|Regex} Column to be formatted. This can
         *            be specified either as: 1) an integer (the column number
         *            in the original order); 2) a string matching a substring
         *            of the column name; 3) a regular expression matching the
         *            column name.
         * 
         * @param format {String|function} The formatting information for the
         *            column. This can be either: 1) as string, in which case it
         *            will replace the current value of the column. If the
         *            string contains "@@", it will be replaced by the current
         *            column value; 2) a function, in which case the function
         *            will be called with the DOM element of the table cell as
         *            its only argument.
         */
        this.columnFormat = function(column, format) {
            colFormatTypes.push(column);
            columnFormats.push(format);
        };

        /**
         * Mark a row as selected.
         * 
         * @param {HTML Dom Element} Dom element pointing to the row (i.e. the
         *            TR element) to be selected.
         */
        this.selectRow = function(rowElem) {
            var inputElems = rowElem.getElementsByTagName("input");
            // only allow row selection in rows with a checkbox
            if (inputElems.length === 0) {
                return;
            }

            var rowNum = rowElem.id;
            rowNum = rowNum.replace("vov_", "");

            var className = rowElem.className;
            if (className.match(/selectedimage/) !== null) {
                rowElem.className = className.replace(/ *selectedimage */, "");
                inputElems[0].checked = false;
                selectedRows[rowNum] = 0;
            } else {
                rowElem.className = rowElem.className + " selectedimage";
                inputElems[0].checked = true;
                selectedRows[rowNum] = 1;
            }

        };

        /**
         * Function for adding a row selection checkbox to a table cell. Used as
         * the formatting function for call to columnFormat() when enabling row
         * selection.
         * 
         * @param {HTML Dom Element} Dom element pointing to the cell to be
         *            reformatted.
         */
        function addRowSelection(cellElem) {
            var rowElem = cellElem.parentNode;
            var rowNum = rowElem.id;
            rowNum = rowNum.replace("vov_", "");

            var oldContent = cellElem.innerHTML;
            cellElem.innerHTML = "<input id=\"" + rowElem.id + "\" type=\"checkbox\" name=\"" + rowElem.id + "\" value=\"" + rowElem.id + "\">" + oldContent;

            rowElem.setAttribute("onclick", objectName + ".renderObject.selectRow(this)");

            if (selectedRows[rowNum] && selectedRows[rowNum] === 1) {
                rowElem.className = rowElem.className + " selectedimage";
                var inputElems = rowElem.getElementsByTagName("input");
                inputElems[0].checked = true;
            }
        }

        /**
         * Enable row selection for this table. Adds in the additional needed
         * column formatting, functionality for tracking row selection, and
         * buttons for selecting or clearing all table rows.
         */
        this.enableRowSelection = function() {
            rowSelection = true;
        };

        /**
         * Select 'all' rows of a table. Which rows are selected is based on the
         * value set in the setSelectRows method of the filter class.
         */
        this.selectAllRows = function() {
            var allRows = meRenderer.selectSingleNode(filteredDOM, "//*[@ID='VOV:SelectAllRows']");
            var rowList = allRows.getAttribute("value").split(",");
            for ( var i = 0; i < rowList.length; i++) {
                selectedRows[rowList[i]] = 1;
            }
            meRenderer.render();
        };

        this.clearRowSelection = function() {
            selectedRows = [];
            meRenderer.render();
        };

        /**
         * Return a list of the currently selected rows.
         * 
         * @returns {integer[]} Array of currently selected rows. The row
         *          numbers correspond to the order of the rows in the original
         *          VOTABLE. These also correspond to the numbers in the row
         *          element IDs, which have the format vov_{number}.
         */
        this.getSelectedRows = function() {
            var rows = [];
            for ( var i in selectedRows) {
                rows.push(i);
            }

            return rows;
        };

        dragtable.moveColumn = function(table, sIdx, fIdx) {
            var movingColumn = columnOrder.splice(sIdx, 1);
            columnOrder.splice(fIdx, 0, movingColumn[0]);
            meRenderer.render();
        };

        /**
         * Return a list of the names of the table columns.
         * 
         * @returns {String[]} Array of the names of the table columns. The
         *          array is in the order of the fields in the original VOTABLE.
         */
        this.getColumnNames = function() {
            return fieldNames;
        };
    }
    /**
     * Prototype inheritance of voview object methods.
     */
    Renderer.prototype = this;
    return new Renderer(rendererParams.filterObject, rendererParams.objectName, rendererParams.widgetIDprefix);
};
