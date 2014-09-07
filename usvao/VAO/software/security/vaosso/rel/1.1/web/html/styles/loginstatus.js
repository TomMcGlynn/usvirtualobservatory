var statusdisplay = {
   showLogin: true
};

function makeStatusHTML(status) {
    var htmlt = "";
    if (status.state == "in") {
        htmlt += "Logged in as ";
        htmlt += status.username;
        htmlt += " &nbsp;&nbsp;Time left: ";
        htmlt += status.dispLeft;
        htmlt += ' &nbsp;&nbsp;<a href="/openid/logout">Logout</a> ';
    }
    else if (status.state == "expired") {
        htmlt += "Expired session as ";
        htmlt += status.username;
        if (statusdisplay.showLogin)
            htmlt += '&nbsp;&nbsp;<a href="/openid/">Login</a>';
    }
    else {
        htmlt += 'Logged out &nbsp;&nbsp;';
        if (statusdisplay.showLogin)
            htmlt += '<a href=\"/openid/\">Login</a>';
    }
    return htmlt;
}

function displayStatus(status) {
    jQuery("#loginStatus")[0].innerHTML = makeStatusHTML(status);
}
function vaoStatus() {
    jQuery.getJSON("/openid/loginStatus", displayStatus);
}

