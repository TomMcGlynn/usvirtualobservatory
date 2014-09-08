/**
 * @class VOView object for filtering and displaying VOTABLEs. This object acts
 *        as the namespace for voview, as well as the base object which other
 *        objects inherit methods from.
 * 
 * @param {string} vovParams.widgetID Prefix for the HTML class attributes where
 *            the various sub-widgets of the HTML table display will be placed.
 *            For example, when displaying the main table containing the data,
 *            the HTML element whose class contains the value {prefix}.table
 *            would be searched for, and the table placed at this location in
 *            the HTML document. For placement of the default layout, only the
 *            widgetID (with no suffix) is specified in the class attribute.
 * 
 * @param {Object} vovParams.input Object containing parameter specifying how
 *            VOView is to obtain the input VOTABLE. Of the input parameters
 *            described below, only one need be specified.
 * 
 * @param {HTML DOM form} vovParams.input.form HTML form element containing the
 *            URL for obtaining a VOTABLE. The URL must be contained in an input
 *            element with the name "query_string".
 * 
 * @param {string} vovParams.input.url String containing the URL for obtaining
 *            the input VOTABLE.
 * 
 * @returns {voview Object}
 */

function voview(vovParams) {
    var meVoview = this;
    var inputParams = vovParams.input;
    this.votableUrl = null;
    var proxy = "@PROXY_URL@";

    // Make a unique identifying object name
    var date = new Date();
    var uniqueName = "vobj" + date.getTime().toString(16);
    eval(uniqueName + " = meVoview");

    this.filterObject = meVoview.makeFilter({});
    this.renderObject = meVoview.makeRenderer({ filterObject: this.filterObject, objectName: uniqueName,
        widgetIDprefix: vovParams.widgetID });

    /**
     * Function to call when a new input table has been downloaded. Kicks off
     * the filtering and rendering process.
     * 
     * @param {XML Dom object} table The table that has been downloaded.
     */
    var gotVotable = function(table) {
        meVoview.filterObject.setInputTable({ tableDOM: table });
        meVoview.renderObject.render({ renderCallback: meVoview.renderObject.displayHTML });
    };

    /**
     * Starts off the process of displaying a VOTABLE. Gets the input
     * information for the table, and initiates the process of acquiring the
     * table object.
     */
    this.start = function() {
        var urlForm;
        var key, keys;

        /**
         * function unloadWarning () { return "Don't do it!"; }
         * window.onbeforeunload = unloadWarning;
         */

        if (inputParams.form) {
            urlForm = document.getElementsByName(inputParams.form)[0];
            meVoview.votableUrl = urlForm.query_string.value;

            if (urlForm.sort_column) {
                keys = [];
                key = meVoview.makeSortColumnKey({ column: urlForm.sort_column.value,
                    direction: urlForm.sort_order.value });
                keys.push(key);
                meVoview.filterObject.setSortColumns({ sortKeys: keys });
            }
        } else {
            if (!inputParams.url) {
                alert("VOView: No input information specified.");
            }
            meVoview.votableUrl = inputParams.url;
        }

        if (meVoview.votableUrl) {
            if (proxy !== "" && meVoview.votableUrl.match(/^\w+:/)) {
                // Need to use a proxy for downloading the table
                meVoview.votableUrl = proxy + "?" + encodeURIComponent(meVoview.votableUrl);
            }
            var tableGet = meVoview.makeGetXml({ fileUrl: meVoview.votableUrl, dataCallBack: gotVotable });
            tableGet.send();
        } else {
            alert("VOView: No input data available.");
        }
        return false;
    };
}

voview.prototype.makeSortColumnKey = function(sortColParams) {
    /**
     * An object for specifying the data needed for filtering the table by the
     * values in a column.
     * 
     * @param {string|integer} sortColParams.column The column to use for
     *            sorting the table. If a string, specifies the name of the
     *            column. If an integer, the number corresponds to the column in
     *            the original order of the columns in the VOTABLE.
     * 
     * @param {string} sortColParams.direction Sort direction. Either
     *            "ascending" or "descending".
     */
    function SortColumnKey(_column, _direction) {
        var meSortColumnKey = this;
        meSortColumnKey.column = _column;
        meSortColumnKey.direction = _direction;

        this.equals = function(otherKey) {
            return otherKey.column === meSortColumnKey.column && otherKey.direction === meSortColumnKey.direction;
        };
    }
    return new SortColumnKey(sortColParams.column, sortColParams.direction);
};

voview.prototype.makeColumnFilterKey = function(filteKeyParams) {
    /**
     * An object for specifying the data needed for filtering the table by the
     * values in a column.
     * 
     * @param filteKeyParams.column {string|integer} The column to use for
     *            filtering the table. If a string, specifies the name of the
     *            column. If an integer, the number corresponds to the column in
     *            the original order of the columns in the VOTABLE.
     * 
     * @param filteKeyParams.expression {string} The filtering expression to be
     *            applied to the column.
     * 
     * @param filteKeyParams.isCharType {boolean} Indicates if the column is a
     *            Character type, rather than numerically valued.
     */
    function columnFilterKey(_column, _expression, _isCharType) {
        var meColumnFilterKey = this;
        meColumnFilterKey.column = _column;
        meColumnFilterKey.expression = _expression;
        meColumnFilterKey.isCharType = _isCharType;

        this.equals = function(otherKey) {
            return otherKey.column === meColumnFilterKey.column && otherKey.expression === meColumnFilterKey.expression && otherKey.isCharType === meColumnFilterKey.isCharType;
        };
    }
    return new columnFilterKey(filteKeyParams.column, filteKeyParams.expression, filteKeyParams.isCharType);
};

/**
 * Helper function for instantiating a GetXml object.
 * 
 * @param {string} getXsltParams.fileUrl URL of the XML file to be downloaded.
 * 
 * @param {function} getXsltParams.dataCallBack Function to call once the file
 *            is downloaded. The sole argument to the function is the XML Dom
 *            object for the downloaded file.
 */
voview.prototype.makeGetXml = function(getXsltParams) {
    /**
     * @class Object for using an http request to download an XML file.
     * @see voview#makeGetXml
     */
    function GetXml(fileUrl, dataCallBack) {
        var meGetXml = this;
        var httpStatus;
        // var vov_xmlhttp = API.createXmlHttpRequest();
        var vov_xmlhttp = new XMLHttpRequest();
        // vov_xmlhttp.overrideMimeType('text/xml');

        vov_xmlhttp.onreadystatechange = function() {
            // alert("Ready xml "+fileUrl+" state "+vov_xmlhttp.readyState);
            if (vov_xmlhttp.readyState === 4) {
                try {
                    httpStatus = vov_xmlhttp.status;
                } catch (e) {
                    // This apparently happens when the request was aborted,
                    // so simply return
                    alert("VOView: Problem getting httpStatus");
                    return;
                }
                if (httpStatus === 200) {
                    var responseData = vov_xmlhttp.responseXML;
                    if (responseData !== null && responseData.documentElement !== null && responseData.documentElement.nodeName !== 'parsererror') {
                        dataCallBack(responseData);
                    } else {
                        alert("VOView: Response from '" + fileUrl + "' is not XML? " + responseData);
                    }
                } else {
                    var errorMessage = "VOView: Error getting xslt file " + fileUrl + ". Status: " + vov_xmlhttp.status;
                    if (httpStatus === 404 && fileUrl.match(/proxy.pl\?/)) {
                        errorMessage += "\n\nProxy may not be properly installed.";
                    }
                    alert(errorMessage);
                }
            }
        };

        try {
            vov_xmlhttp.open("GET", fileUrl, true);
            vov_xmlhttp.setRequestHeader('Accept', 'text/xml');
        } catch (e) {
            alert("VOView: Error opening '" + fileUrl + "':" + e.message);
        }
        this.send = function() {
            vov_xmlhttp.send();
        };
    }
    return new GetXml(getXsltParams.fileUrl, getXsltParams.dataCallBack);
};

voview.prototype.selectSingleNode = function(inDoc, xpath) {
    var result;
    var e1, e2;

    // Mozilla version
    try {
        result = inDoc.evaluate(xpath, inDoc, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    } catch (e) {
        e1 = e;
    }

    if (e1) {
        // IE version
        try {
            result = inDoc.selectSingleNode(xpath);
        } catch (e) {
            e2 = e;
        }
    }

    if (e1 && e2) {
        alert("VOView: Unable to execute selectSingleNode. Mozilla: " + e1.message + " IE: " + e2.message);
    }
    return result;
};

voview.prototype.selectNodes = function(inDoc, xpath) {
    var results = [];
    var e1, e2;
    var nodes;

    // Mozilla version
    try {
        nodes = inDoc.evaluate(xpath, inDoc, null, XPathResult.ANY_TYPE, null);
    } catch (e) {
        e1 = e;
    }

    if (!e1) {
        var result = nodes.iterateNext();
        while (result) {
            results.push(result);
            result = nodes.iterateNext();
        }
        return results;
    }

    // IE version
    try {
        results = inDoc.selectNodes(xpath);
    } catch (e) {
        e2 = e;
    }

    if (e1 && e2) {
        alert("VOView: Unable to execute selectSingleNode. Mozilla: " + e1.message + " IE: " + e2.message);
    }
    return results;
};

voview.prototype.getElementsByClass = function(searchClass, node, tag) {
    var classElements = [];
    if (node === undefined) {
        node = document;
    }
    if (tag === undefined) {
        tag = '*';
    }

    var els = node.getElementsByTagName(tag);
    var elsLen = els.length;
    for ( var i = 0, j = 0; i < elsLen; i++) {
        var className = els[i].className;
        if( className ){
        var classes = className.split(" ");
            for( var k = 0; k < classes.length; k++){
                if( classes[k] === searchClass ){
                    classElements[j] = els[i];
                    j++;
                }
            }
        }
    }
    return classElements;
};
