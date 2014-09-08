function TheVoviewPackageName(vovParams){ 
	var meVoview = this;
	var _sortCol = null;
	var _rangeParams = null;
	
    this.setSortColumn = function(sortColParams){
		_sortCol = meVoview.makeSortColumnKey(sortColParams.column, sortColParams.direction);
	};
	
    function dataCallBack(votableDOM){ 
    	var filterObj = meVoview.makeFilter(votableDOM, vovParams.filterCallBack);
    	if (_sortCol !== null) {
    		var keys = [];
    		keys.push(_sortCol);
    		filterObj.setSortColumns(keys);
		} 
    	if (_rangeParams.firstRow !== null) {
			filterObj.setRowRange(_rangeParams.firstRow, _rangeParams.lastRow);
		}
        filterObj.doFilter(); 
    } 
        
    this.start = function(startParams){ 
    	// Need to use a proxy for downloading the table
    	var proxy = "@PROXY_URL@";
    	var VoUrl;
    	if (proxy === "" || !startParams.votableURL.match(/^\w+:/) ) {
            VoUrl = startParams.votableURL;
		}else{
            VoUrl = "@PROXY_URL@" + "?" + encodeURIComponent(startParams.votableURL);
		}
    	var tableGet = meVoview.makeGetXslt(VoUrl, dataCallBack);
    	tableGet.send();
    };
     
	this.setRowRange = function(rangeParams) {
		_rangeParams = rangeParams;
	};
}

TheVoviewPackageName.prototype.makeSortColumnKey = function(_column, _direction){
	/**
	 * An object for specifying the data needed for filtering the table by the
	 * values in a column.
	 * 
	 * @param {string|integer} __column The column to use for sorting the table. If 
	 * a string, specifies the name of the column. If an integer, the number 
	 * corresponds to the column in the original order of the columns in the VOTABLE.
	 * 
	 * @param {string} __direction  Sort direction. Either "ascending" or "descending".
	 */
	function SortColumnKey(__column, __direction) {
		var meSortColumnKey = this;
		meSortColumnKey.column = __column;
		meSortColumnKey.direction = __direction;
		this.equals = function(otherKey){
			return otherKey.column === meSortColumnKey.column && 
				otherKey.direction === meSortColumnKey.direction;
		};		
	}
	return new SortColumnKey(_column, _direction);
};

TheVoviewPackageName.prototype.makeGetXslt = function(_fileUrl, _dataCallBack){
	function GetXslt(fileUrl, dataCallBack) {
		var meGetXslt = this;
//		var vov_xmlhttp = API.createXmlHttpRequest();
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
	return new GetXslt(_fileUrl, _dataCallBack);
};


