/**
 * VOView object for filtering and displaying VOTABLEs.  This object acts as the namespace
 * for voview, as well as the base object which other objects inherit methods from. 
 *
 * @returns {voview}
 */

function voview(vovParams){}

voview.prototype.makeSortColumnKey = function(sortColParams){
	/**
	 * An object for specifying the data needed for filtering the table by the
	 * values in a column.
	 * 
	 * @param {string|integer} sortColParams.column The column to use for sorting the table. If 
	 * a string, specifies the name of the column. If an integer, the number 
	 * corresponds to the column in the original order of the columns in the VOTABLE.
	 * 
	 * @param {string} sortColParams.direction Sort direction. Either "ascending" or "descending".
	 */
	function SortColumnKey(_column, _direction) {
		var meSortColumnKey = this;
		meSortColumnKey.column = _column;
		meSortColumnKey.direction = _direction;
		this.equals = function(otherKey){
			return otherKey.column === meSortColumnKey.column && 
				otherKey.direction === meSortColumnKey.direction;
		};		
	}
	return new SortColumnKey(sortColParams.column, sortColParams.direction);
};

voview.prototype.makeColumnFilterKey = function(filteKeyParams) {
    /**
     * An object for specifying the data needed for filtering the table by the
     * values in a column.
     * 
     * @param filteKeyParams.column {string|integer} The column to use for filtering the table. If a 
     * string, specifies the name of the column. If an integer, the number corresponds 
     * to the column in the original order of the columns in the VOTABLE.
     * 
     * @param filteKeyParams.expression {string} The filtering expression to be applied to the column.
     * 
     * @param filteKeyParams.isCharType {boolean} Indicates if the column is a Character type, rather than 
     * numerically valued.
     */
    function columnFilterKey(_column, _expression, _isCharType) {
        var meColumnFilterKey = this;
        meColumnFilterKey.column = _column;
        meColumnFilterKey.expression = _expression;
        meColumnFilterKey.isCharType = _isCharType;        
        this.equals = function(otherKey){
            return otherKey.column === meSortColumnKey.column && 
                otherKey.expression === meSortColumnKey.expression &&
                otherKey.isCharType === meSortColumnKey.isCharType;
        };      
    }
    return new columnFilterKey(filteKeyParams.column, filteKeyParams.expression, filteKeyParams.isCharType);
};

voview.prototype.makeGetXslt = function(getXsltParams){
	function GetXslt(fileUrl, dataCallBack) {
		var meGetXslt = this;
		// var vov_xmlhttp = API.createXmlHttpRequest();
		var vov_xmlhttp = new XMLHttpRequest();
		// vov_xmlhttp.overrideMimeType('text/xml');
		
		vov_xmlhttp.onreadystatechange = function() {
			// alert("Ready xml "+fileUrl+" state "+vov_xmlhttp.readyState);
			if( vov_xmlhttp.readyState === 4 ){
				try {
					var httpStatus = vov_xmlhttp.status;
				} catch(e) {
					// This apparently happens when the request was aborted,
					// so simply return
					alert("Problem getting httpStatus");
					return;
				}
				if( httpStatus === 200 ){
					var responseData = vov_xmlhttp.responseXML;
					if (responseData !== null && responseData.documentElement !== null && 
							responseData.documentElement.nodeName !== 'parsererror') {
						dataCallBack(responseData);
					} else {
						alert("Response from '" + fileUrl + "' is not XML? " + responseData);
						throw "Response from '" + fileUrl + "' is not XML? " + responseData;
					}
				}else{
					alert("VOView error getting xslt file " +fileUrl+ ". Status: " + vov_xmlhttp.status);
					throw "VOView error getting xslt file " +fileUrl+ ". Status: " + vov_xmlhttp.status;
				}
			}
		};	
	
		try {
			vov_xmlhttp.open("GET", fileUrl, true);	
			vov_xmlhttp.setRequestHeader('Accept', 'text/xml');			
		} catch (e) {
			alert("Error opening '" + fileUrl + "':" + e.message);
			throw "Error opening '" + fileUrl + "':" + e.message;
		}
		this.send = function() {
			vov_xmlhttp.send();			
		};
	}
	return new GetXslt(getXsltParams.fileUrl, getXsltParams.dataCallBack);
};


