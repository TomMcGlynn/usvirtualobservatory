// pack form parameters into a GET string

function getFormPars(formname) {
    if (typeof(formname) == "string") {
	var form = document.forms[formname];
    } else {
	form = formname;
    }
    var parlist = [];
    for (var i=0; i<form.elements.length; i++) {
	var el = form.elements[i];
	if (el.tagName == "INPUT") {
	    var value = encodeURIComponent(el.value);
	    if (el.type == "text" || el.type == "hidden") {
		parlist.push(el.name + "=" + value);
	    } else if (el.type == "checkbox") {
		if (el.checked) {
		    parlist.push(el.name + "=" + value);
		} else {
		    parlist.push(el.name + "=");
		}
	    } else if (el.type == "radio") {
		if (el.checked) {
		    parlist.push(el.name + "=" + value);
		}
	    }
	} else if (el.tagName == "SELECT") {
	    parlist.push(el.name + "=" + encodeURIComponent(el.options[el.selectedIndex].value));
	}
    }
    return parlist.join("&");
}

// extract form parameters from a GET string and set form values

function setFormPars(formname,getstr) {
    if (typeof(formname) == "string") {
	var form = document.forms[formname];
    } else {
	form = formname;
    }
    var parlist = getstr.split("&");
    for (var i=0; i<parlist.length; i++) {
	var f = parlist[i].split("=");
	if (f.length < 2) {
	    var name = parlist[i];
	    var value = "";
	} else {
	    // don't know if embedded '=' can happen, but might as well handle it
	    name = f.shift();
	    value = decodeURIComponent(f.join("="));
	}
	var el = form[name];
	if (el != undefined) {
	    if (el.tagName == "INPUT") {
	        // text or hidden element
	        el.value = value;
	    } else if (el.tagName == "SELECT") {
		for (var j=0; j < el.options.length; j++) {
		    var option = el.options[j];
		    if (option.value == value) {
			option.selected = true;
		    } else {
			option.selected = false;
		    }
		}
	    } else if (el.length > 0) {
	        // radio buttons
		for (j=0; j < el.length; j++) {
		    if (el[j].value == value) {
		        el[j].checked = true;
		    } else {
			el[j].checked = false;
		    }
		}
	    }
	}
    }
}

