var waitimg = undefined;
var waitstatic = "/images/clearpixel.gif";
var waitanim = "/images/PleaseWait.gif";

function makeRequest(url, parameters, doneCallback) {
    // general-purpose XMLHttpRequest
    var http_request = new XMLHttpRequest();
    var errmsg = alert;
    if (!waitimg) {
        var wait = document.getElementById("wait");
        if (wait) {
            waitimg = wait.getElementsByTagName("img");
            if (waitimg) waitimg = waitimg[0];
        }
    }
    http_request.onreadystatechange = function () {
        if (http_request.readyState == 4) {
            if (waitimg) {
                waitcount -= 1;
                if (waitcount <= 0) waitimg.src = waitstatic;
            }
            if (http_request.status == 200) {
                // do final update
                var xmldata = http_request.responseXML;
                if (xmldata) {
                    doneCallback(xmldata);
                } else {
                    errmsg("Response from '" + url + parameters + "' is not XML?");
                }
            } else {
                errmsg("Error " + http_request.status + ": " + http_request.statusText);
            }
        }
    };
    try {
        if (waitimg) {
            if (waitcount == 0) waitimg.src = waitanim;
            waitcount += 1;
        }
        http_request.open('GET', url + parameters, true);
        http_request.send(null);
    } catch (exceptionId) {
        if (waitimg) {
            waitcount -= 1;
            if (waitcount <= 0) waitimg.src = waitstatic;
        }
        errmsg("Error on open: " + exceptionId);
    }
}
