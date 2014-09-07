var editrow = null;
var preftypes = null;
var oldprefs = null;
var formel = null;

/**
 * set up form management.  This is called upon loading of the page.
 */
function formload() {
    editrow = document.getElementById("editrow");
    formel = document.getElementById("prefsform");
    preftypes = getPrefsOrder();
}

/**
 * edit the preferences in the row containing the given element.
 * This is the function that responds to the "Edit" button in the form,
 * and the element is intended to be the Edit button/link itself.
 */
function editPrefs(el) {
    // start by turning off the feedback message
    var fb = document.getElementById("feedback");
    fb.style.display = "none";

    var row = findAncestorRow(el);
    var pname = row.id;
    editPrefsFor(pname, row);
    return false;
}

/**
 * replace the preference display row with a form for editing the preferences
 * @param portal   the name of the portal
 * @param row      the TR element that contains the portals preferences; if 
 *                   null, it will be found.
 */
function editPrefsFor(portal, row) {
    if (row == null) 
        // find desired row
        row = document.getElementById(portal);
    if (row == null) 
        throw new Error("Preferences row not found for " + portal);

    // extract current settings
    var prefs = extractCurrentPrefs(row);

    // move the edit row here 
    moveEditRowHere(row);

    // load the current settings into the form
    var desc = "";
    var td = row.firstChild;
    while (td != null && td.nodeName != "TD") td = td.nextSibling;
    if (td != null) desc = td.title;
    loadPrefs(prefs, portal, desc);

    // show the form and hide the display 
    row.style.display = "none";
    editrow.style.display = "table-row";
}

/**
 * find the TR element that contains the given element
 */
function findAncestorRow(el) {
    var el = findAncestorEl(el,"TR");
    if (el == null)
        throw new Error("findAncestorRow(): element outside of row(?)");
    return el;
}

/**
 * find the named element that contains the given element
 */
function findAncestorEl(el,pname) {
    el = el.parentNode;
    while (el.nodeName != pname) {
        el = el.parentNode;
        if (el == null) break;
    }
    return el;
}

/**
 * determine the order of the preferences in the display row.
 */
function getPrefsOrder() {
    var table = document.getElementById("display");
    var prefels = findTagsByClass(table, "pref-type");
    var out = new Array();
    var i=0;
    var p=-1;
    for(i=0; i < prefels.length; i++) {
        p = prefels[i].id.lastIndexOf("_");
        if (p < 0) {
            log("getPrefsOrder(): unexpected pref-type id: " + prefels[i].id);
            continue;
        }
        out[i] = prefels[i].id.substring(p+1);
    }
    return out;
}

/**
 * extract the current preferences from the given portal display row.
 * Return the preferences as an object of properties.
 */
function extractCurrentPrefs(row) {
    var preftds = findTagsByClass(row,"prefs-val");
    if (preftds == null || preftds.length == 0)
        throw new Error("extractCurrentPrefs(): Can't find pref values in row.");
    if (preftds.length != preftypes.length)
        log("extractCurrentPrefs(): found " + preftds.length + " values for " +
            preftypes.length + " properties.");

    var i=0;
    var out = Object();
    for(i=0; i < preftypes.length; i++) 
        out[preftypes[i]] = (preftds[i].innerHTML == "Yes");

    preftds = findTagsByClass(row,"prefs-confirm");
    if (preftds == null || preftds.length == 0)
      throw new Error("extractCurrentPrefs(): Can't find confirm value in row.");
    out.confirm = (preftds[0].innerHTML == "Yes");
    return out;
}

function findTagsByFunc(elem, testfunc, tagname, level) {
    var out = Array();
    var desc = null;
    var children = elem.childNodes;
    var i;
    for (i=0; i < children.length; i++) {
        if (children[i].nodeType != 1) continue;
        if (level == null || level > 0) {
            var nxt = null;
            if (level != null) nxt = level - 1;
            desc = findTagsByFunc(children[i], testfunc, tagname, nxt);
        }

        if (testfunc(children[i]) && 
            (tagname == null || children[i].nodeName == tagname))
            out.push(children[i])
        if (desc != null && desc.length > 0)
            out = out.concat(desc);
    }

    if (out.length == 0) return null;
    return out;
}

function findTagsByClass(elem, clname, tagname, level) {
    return findTagsByFunc(elem,
                         (function(elem) { return (clname == elem.className); }),
                          tagname, level);
}

function findInputByName(elem, iname, level) {
    var out = findTagsByFunc(elem,
                            (function(elem) { return (iname == elem.name); }),
                            "INPUT", level);
    if (out == null || out.length == 0) return null;
    return out[0];
}

function log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}

/**
 * find the preceding TR element to the given one
 */
function findPreviousRow(trel) {
    trel = trel.previousSibling;
    while (trel != null && trel.nodeName != "TR") 
        trel = trel.previousSibling;
    return trel;
}

/**
 * move the preferences editing form to a new display row for editing 
 * its preferences.  
 */
function moveEditRowHere(row) {

    if (editrow.style.display != "none") {
        // check to see if there are changes we need to save
        var prefs = getPrefsFromForm();
        if (prefsDiffer(oldprefs, prefs)) {
            // ask user if changes should be saved
            var portalinp = document.getElementById("portal");
            var msg = "Some changes have been made to the preferences for the " +
                portalinp.value + " portal.  ";
            msg += "Should these changes be saved?\n\n";
            msg += "Click 'OK' to save, or 'Cancel' to disregard the updates";
            var save = confirm(msg);
            if (save) {
                // yes, save the changes
                formel.submit();
            }
        }

        var olddisplayrow = findPreviousRow(editrow);
        if (olddisplayrow != null) olddisplayrow.style.display = "table-row";
        editrow.style.display = "none";
    }

    var parent = editrow.parentNode;
    if (parent != null) parent.removeChild(editrow);

    parent = row.parentNode;
    if (parent == null)
        throw new Error("moveEditRowHere(): given row is orphaned.");
    parent.insertBefore(editrow, row.nextSibling);

    // We haven't yet turned on the editrow and turned off the display 
    // row.  We'll do that after the form has been loaded.
}

/**
 * return the preferences currently set in the form
 */
function getPrefsFromForm() {
    inps = editrow.getElementsByTagName("input");

    var out = Object();
    for(var i=0; i < inps.length; i++) {
        if (inps[i].name.match(/^share_/)) 
            out[inps[i].name.substring(6)] = inps[i].checked;
        else if (inps[i].name == "always_confirm")
            out.confirm = inps[i].checked;
    }

    return out;
}

/**
 * Load the given preferences into the edit form
 * @param prefs  the preference values
 * @param name   the name of the portal
 * @param desc   the description of the portal.
 */
function loadPrefs(prefs, name, desc) {
    oldprefs = prefs;
    var inp = null
    for (var pname in prefs) {
        if (pname == "confirm") 
            inp = findInputByName(editrow, "always_confirm");
        else 
            inp = findInputByName(editrow, "share_" + pname);
        inp.checked = prefs[pname];
    }

    // update the displayed name and description
    var dl = findTagsByClass(editrow, "portal-name");
    var el = dl[0].getElementsByTagName("DT");
    el[0].innerHTML = name;
    el = dl[0].getElementsByTagName("DD");
    el[0].innerHTML = desc;

    // update the portal input parameter
    el = findInputByName(editrow, "portal");
    if (el == null) {
        log("Warning: can't find hidden portal input");
        return;
    }
    el.value = name;
}

/**
 * reset the form to its initial values
 */
function cancelEdit() {
    var disp = findPreviousRow(editrow);
    if (disp != null) disp.style.display = "table-row";
    editrow.style.display = "none";
    return false;
}

/**
 * return true if the two sets of preferences differ in any way
 */
function prefsDiffer(p1, p2) {
    for(var pname in p1) {
        if (p1[pname] != p2[pname]) return true;
    }
    return false;
}
