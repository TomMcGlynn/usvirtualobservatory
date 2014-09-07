// Scripts for cookies
// From http://www.quirksmode.org/js/cookies.html with some
// modifications by rlw (to add the path optional parameter)
// 2007 July 26

function createCookie(name,value,days,path) {
	// create a new cookie
	// days is number of days until expiration
	//   if days is omitted cookie expires when browser session ends
	//   if days is negative cookie is deleted immediately (see eraseCookie)
	// path defaults to "/" (meaning cookie applies to entire website)
	var expires = "";
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		expires = "; expires="+date.toGMTString();
	}
	if (path==undefined) path="/";
	document.cookie = name+"="+value+expires+"; path="+path;
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}
