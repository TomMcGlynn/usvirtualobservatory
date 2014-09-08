TheVoviewPackageName.prototype.makeFilter = function(_votableDOM,
        _filterCallback) {
    /**
     * Takes an XML VOTABLE, and creates an object for doing sorting, filtering
     * and paging.
     * 
     * @param {XML DOM Object} votableDOM The VOTABLE to be filtered.
     * @param {function} filterCallback Function to call when the result of the
     *            filtering is completed. The one argument to this function is
     *            an XML DOM object of the filtered VOTABLE.
     */
    function Filter(votableDOM, filterCallback) {
        var meFilter = this;
        var preProcDOM = null;
        var preProcessed = false;
        var resultCallback = filterCallback;

        var filterDOM = null;
        var filtered = false;
        var filterProc = new XSLTProcessor();

        var constraints = {};
        var types = {};

        var range = {
            start : 1,
            stop : -1
        };

        var sortColumns = null;

        var selectCriteria = null;

        var filteredTableDOM = null;

        function preProcessMod() {
            var xslTopNodes = preProcDOM.getElementsByTagName("stylesheet")[0];
        }

        function filterModify() {

            // Internal Functions

            function trim(str) {
                return str.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");
            }

            function rangeConstraint(index, constraint, negate, prefix) {
                var fields = constraint.split("\.\.", 2);
                if (fields[0].length === 0 || fields[1].length === 0) {
                    return null;
                }
                if (negate) {
                    var con = prefix + "TD[" + index + "] &lt;" + fields[0] + " or " + prefix + "TD[" + index + "] &gt;" + fields[1] + "";
                } else {
                    con = prefix + "TD[" + index + "] &gt;=" + fields[0] + " and " + prefix + "TD[" + index + "] &lt;=" + fields[1] + "";
                }
                return con;
            }

            function numConstraint(index, constraint, negate, prefix) {
                if (constraint.indexOf("..") > 0) {
                    return rangeConstraint(index, constraint, negate, prefix);
                }
                if (negate) {
                    if (constraint.substring(0, 2) === ">=") {
                        constraint = constraint.replace(">=", "&lt;");
                    } else if (constraint.substring(0, 2) === "<=") {
                        constraint = constraint.replace("<=", "&gt;");
                    } else if (constraint.substring(0, 1) === ">") {
                        constraint = constraint.replace(">", "&lt;=");
                    } else if (constraint.substring(0, 1) === "<") {
                        constraint = constraint.replace("<", "&gt;=");
                    } else if (constraint.substring(0, 1) !== "=") {
                        constraint = "!=" + constraint;
                    } else {
                        constraint = "!" + constraint;
                    }
                } else {
                    if (constraint.substring(0, 1) === ">") {
                        constraint = constraint.replace(">", "&gt;");
                    } else if (constraint.substring(0, 1) === "<") {
                        constraint = constraint.replace("<", "&lt;");
                    } else if (constraint.substring(0, 1) !== "=") {
                        constraint = "=" + constraint;
                    }
                }
                constraint = prefix + "TD[" + index + "]" + constraint;
                return constraint;
            }

            function wildCardConstraint(index, constraint, negate, prefix) {
                var fields = constraint.split("\*");
                var out = [];
                out.push("position() = " + index);

                var inner = "translate(normalize-space(),$lc,$uc)";
                if (fields[0]) {
                    inner = "(" + inner + ",'" + fields[0] + "')";
                    out.push("starts-with" + inner);
                    inner = "substring-after" + inner;
                }
                for ( var j = 1; j < fields.length - 1; j += 1) {
                    if (fields[j]) {
                        inner = "(" + inner + ",'" + fields[j] + "')";
                        out.push("contains" + inner);
                        inner = "substring-after" + inner;
                    }
                }
                if (fields[fields.length - 1]) {
                    inner = "(concat(" + inner + ",'a'),'" + fields[fields.length - 1] + "a')";
                    out.push("contains" + inner);
                }

                if (out.length === 1) {
                    // no constraints (this can happen with a value like "*" or
                    // "**")
                    return null;
                }
                if (negate) {
                    var p = out[0];
                    out.splice(0, 1);
                    out = p + " and not(" + out.join(" and ") + ")";
                } else {
                    out = out.join(" and ");
                }
                return prefix + "TD[" + out + "]";
            }

            function stdCharConstraint(index, constraint, negate, prefix) {
                constraint = trim(constraint);
                if (negate) {
                    return "translate(normalize-space(" + prefix + "TD[" + index + "]), $lc, $uc)!='" + constraint + "'";
                }
                return "translate(normalize-space(" + prefix + "TD[" + index + "]), $lc, $uc)='" + constraint + "'";
            }

            // Handle a constraint on a character column
            function charConstraint(index, constraint, negate, prefix) {
                constraint = constraint.toUpperCase();
                if (constraint.indexOf('*') >= 0) {
                    return wildCardConstraint(index, constraint, negate, prefix);
                }
                return stdCharConstraint(index, constraint, negate, prefix);
            }

            // Convert a single constraint into appropriate XSLT filter
            // elements.
            function makeXSLConstraint(index, constraint, isChar, prefix) {
                if (constraint.length === 0) {
                    return null;
                }
                if (constraint.substring(0, 1) === '!') {
                    var negate = true;
                    constraint = constraint.substring(1);
                } else {
                    negate = false;
                }
                if (constraint.substring(0, 1) === '=') {
                    constraint = constraint.substring(1);
                }
                if (constraint.length === 0) {
                    return null;
                }
                if (isChar) {
                    return charConstraint(index, constraint, negate, prefix);
                }
                return numConstraint(index, constraint, negate, prefix);
            }

            function makeOneConstraint(prefix, suffix) {
                var all = [];
                for ( var column in constraints) {
                    if (column.length > 0) {
                        var con = makeXSLConstraint(column,
                                constraints[column], types[column], prefix);
                        if (con !== null) {
                            all.push( [ con.length, con ]);
                        }
                    }
                }
                if (all.length) {
                    // sort to put shortest constraints (presumably fastest)
                    // first
                    var sortfunc = function(a, b) {
                        return a[0] - b[0];
                    };
                    all.sort(sortfunc);
                    // get rid of the lengths
                    for ( var j = 0; j < all.length; j += 1) {
                        all[j] = all[j][1];
                    }
                    var full = all.join(" and ");
                    return full;
                }
                return "";
            }

            // End Internal Functions

            var nsprefixes = [ "", "vo:", "v1:", "v2:", "v3:", "v4:" ];
            var nssuffixes = [ "", "0", "1", "2", "3", "4" ];

            var xslgen = [];
            // build separate constraint variables for each possible namespace
            // prefix
            // this is repetitive, but time is negligible here (and long in
            // XSLT)
            for ( var i = 0; i < nsprefixes.length; i += 1) {
                xslgen[i] = makeOneConstraint(nsprefixes[i], nssuffixes[i]);
            }

            // Replace the dummy values in the xslt stylesheet with the
            // generated filter expressions

            var xslVarNodes = filterDOM.documentElement
                    .getElementsByTagName("xsl:variable");
            for ( var node = 0; node < xslVarNodes.length; node = node + 1) {
                var varNode = xslVarNodes[node];
                var nodeName = varNode.getAttribute("name");
                var matchResults = nodeName.match(/filterRows(\d?)/);
                if (matchResults) {
                    var nsIndex = matchResults[1] - 1 + 2;
                    if (matchResults[1] === "") {
                        nsIndex = 0;
                    } else if (nsIndex <= 0 || nsIndex >= nsprefixes.length) {
                        alert("Filter error editing expressions in xslt.");
                        throw "Filter error editing expressions in xslt.";
                    }
                    var selectNode = varNode.getAttributeNode("select");
                    if (xslgen[nsIndex] === "") {
                        selectNode.nodeValue = "$allRows" + matchResults[1];
                    } else {
                        selectNode.nodeValue = "$allRows" + matchResults[1] + "[" + xslgen[nsIndex] + "]";
                    }
                }
            }
        }

        function ready() {
            var tempDOM;
            // alert("ready called filterDOM = "+filterDOM+", preProcDOM =
            // "+preProcDOM);
            if (filterDOM !== null && preProcDOM !== null) {
                // Do the actual filtering work and call back with the result.
                if (!preProcessed) {
                    preProcessed = true;
                    preProcessMod();
                    var preProcessor = new XSLTProcessor();
                    try {
                        preProcessor.importStylesheet(preProcDOM);
                        tempDOM = preProcessor.transformToDocument(votableDOM);
                        votableDOM = tempDOM;
                    } catch (e1) {
                        alert("Error preprocessing XML doc: " + e1.message);
                    }
                }

                if (!filtered) {
                    filtered = true;

                    /**
                     * Debug printout
                     * 
                     * var xmlstring = (new
                     * XMLSerializer()).serializeToString(filterDOM); xmlstring =
                     * xmlstring.replace(/</g,"&lt;"); xmlstring =
                     * xmlstring.replace(/>/g,"&gt;");
                     * document.getElementById("output").innerHTML = xmlstring;
                     */

                    filterModify();

                    /**
                     * Debug printout
                     * 
                     * xmlstring = (new
                     * XMLSerializer()).serializeToString(filterDOM); xmlstring =
                     * xmlstring.replace(/</g,"&lt;"); xmlstring =
                     * xmlstring.replace(/>/g,"&gt;");
                     * document.getElementById("output").innerHTML = xmlstring;
                     */

                    try {
                        filterProc.importStylesheet(filterDOM);
                        filterProc.clearParameters();
                        if (sortColumns !== null && sortColumns.length > 0) {
                            filterProc.setParameter(null, "sortColumn",
                                    sortColumns[0].column);
                            filterProc.setParameter(null, "sortOrder",
                                    sortColumns[0].direction);
                        }
                        if (range.stop >= 0) {
                            filterProc.setParameter(null, "pageStart",
                                    range.start);
                            filterProc
                                    .setParameter(null, "pageEnd", range.stop);
                        }
                        if (selectCriteria !== null) {
                            filterProc.setParameter(null, "selectAllCriteria",
                                    selectCriteria);
                        }
                        filteredTableDOM = filterProc
                                .transformToDocument(votableDOM);
                    } catch (e2) {
                        alert("Error processing votable DOM thru Filter: " + e2.message);
                    }
                }

                resultCallback(filteredTableDOM);
            }
        }

        /**
         * Produces a single page's worth of VOTABLE data. After filtering is
         * complete, the filterCallback function is called with the results.
         * 
         * @param {function} doFilterCallback Function to call when the result
         *            of the filtering is completed. The one argument to this
         *            function is an XML DOM object of the filtered VOTABLE. If
         *            omitted then use function specified in constructor.
         */
        this.doFilter = function(doFilterCallback) {
            if (doFilterCallback !== undefined) {
                resultCallback = doFilterCallback;
            }
            if (filterDOM === null) {
                var filterDOMCallback = function(data) {
                    filterDOM = data;
                    ready();
                };
                var filterGet = meFilter.makeGetXslt("@XSL_PATH@filter.xsl",
                        filterDOMCallback);
                filterGet.send();
            }
            if (preProcDOM === null) {
                var preprocDOMCallback = function(data) {
                    preProcDOM = data;
                    ready();
                };
                var preprocGet = meFilter.makeGetXslt(
                        "@XSL_PATH@preProcess.xsl", preprocDOMCallback);
                preprocGet.send();
            }
            ready();
        };

        /**
         * Set column value filters on the VOTABLE. Any filters all ready set on
         * columns not specified in the filterKeys are retained.
         * 
         * @param {columnFilterKey[]} filterKeys An array of type
         *            columnFilterKey.
         */
        this.setColumnFilters = function(filterKeys) {
            for ( var i = 0; i < filterKeys.length; i = i + 1) {
                var key = filterKeys[i];
                if (constraints[key.column] !== key.expression) {
                    constraints[key.column] = key.expression;
                    types[key.column] = key.isCharType;
                    filtered = false;
                }
            }
        };

        /**
         * Clear any column value filters currently set on the VOTABLE.
         */
        this.clearColumnFilters = function() {
            constraints = {};
            types = {};
            filtered = false;
        };

        /**
         * Set range of rows to be extracted from the VOTABLE.
         * 
         * @param {integer} firstRow The first row of the range.
         * @param {integer} lastRow The last row of the range.
         */
        this.setRowRange = function(firstRow, lastRow) {
            if (range.start !== firstRow || range.stop !== lastRow) {
                range.start = firstRow;
                range.stop = lastRow;
                filtered = false;
            }
        };

        /**
         * Set the columns to use for sorting the table, and the sorting
         * direction for each column.
         * 
         * @param {sortColumnKey[]} sortKeys An array of type sortColumnKey. The
         *            first key in the array has the highest precedence.
         */
        this.setSortColumns = function(sortKeys) {
            if (sortColumns !== null) {
                var numCols = sortColumns.length;
                for ( var ikey = 0; ikey < sortKeys.length; ikey = ikey + 1) {
                    if (ikey >= numCols || !sortColumns[ikey]
                            .equals(sortKeys[ikey])) {
                        filtered = false;
                    }
                }
            }
            sortColumns = sortKeys;
        };

        /**
         * Set the criteria which determines which rows will be selected when
         * the "select all" button is activated. **ONLY string argument is
         * currently implemented**
         * 
         * @param {string|function} selectCriteria If a string, use it to match
         *            against the contents of the VOTABLE row. The contents of
         *            the VOTABLE XML row will be searched and the row will be
         *            selected if it contains the input string. If a function,
         *            then a function which will be called for each row in the
         *            XML VOTABLE, with an XML DOM object of the row as its only
         *            argument. The function should return a boolean indicating
         *            whether the row should be selected or not.
         */
        this.setSelectRows = function(criteria) {
            if (criteria !== selectCriteria) {
                selectCriteria = criteria;
                filtered = false;
            }
        };
    }
    /**
     * Prototype inheritance of voview object methods.
     */
    Filter.prototype = this;
    return new Filter(_votableDOM, _filterCallback);
};
