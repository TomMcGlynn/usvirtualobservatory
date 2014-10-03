function ensureSelection() {
    checked = checkedResourceButton();
    if (checked == null) {
        alert("Please first select a resource by clicking \n" +
              "one of the buttons in the resource list above.");
        return false;
    }
}


function confirmDelete() {
    checked = checkedResourceButton();
    if (checked == null) {
        alert("Please select a resource to delete by clicking \n" +
              "one of the buttons in the resource list above.");
        return false;
    }

    changes = "delete the following resource";
    note = "Note that the previously published metadata for this resourse\n" +
        "will not be lost.\n\n";
    if (checked.value.indexOf(".uc_edit") >= 0) {
        changes = "delete uncommitted edits to the following resource";
    }
    else if (checked.value.indexOf(".uc_add") >=  0) {
        changes = "delete the following as-yet unpublished resource";
        note = "All metadata for this resource will be lost.\n\n";
    }
    else if (checked.value.indexOf(".uc_undel") >= 0) {
        changes = "cancel the re-publishing of the following resource";
        note = "When completed, this will resource will remain marked " +
            "as deleted.\n\n";
    }
    else if (checked.value.indexOf(".uc_del") >= 0) {
        alert("This resource is already set to be deleted; please select \n" + 
              "another resource.");
        return false;
    }
    msg = "You have selected to " + changes + ": \n\n" + 
        "   Title:   "   + checked.getAttribute("rname") + 
        "\n   ID:       " + checked.getAttribute("rid") +
        "\n   Type:   "  + checked.getAttribute("rtype") + "\n\n" + note +
        "Continue?"
    return confirm(msg)
}

function checkedResourceButton() {
    var r = document.forms[0].resource;
    for(i=0; i < r.length; i++) {
        if (r[i].checked) return r[i];
    }
    return null;
}
