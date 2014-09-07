//some of these are going to be simple enough to do generically without element-index-finding
//notably, the same ones we handle directly in import.


function setResourceType(isClone) {

    var selection = Ext.getCmp('resourceTypeDropDown').value;
    var val = selection;
    if (selection == 'cs:ConeSearch' || selection == 'sia:SimpleImageAccess' || selection == 'ssa:SimpleSpectralAccess')
        val = 'vs:CatalogService';

    resourceDOM.firstChild.attributes["xsi:type"].textContent = val;

    if (firstOrgRecord == true)
        Ext.getCmp('buttonSubmitDraftResource').disable();

    if (val == 'vs:CatalogService') {
        Ext.getCmp('buttonSubmitDraftResource').disable();
        setResourceSubtype(isClone, selection);
    }
    else {
        removeFormPage('standardPage', '');
        if (isClone) {
            DeleteCapability('cs:ConeSearch');
            DeleteCapability('sia:SimpleImageAccess');
            DeleteCapability('ssa:SimpleSpectralAccess');
            DeleteCapability('tr:TableAccess');
            DeleteElement(resourceDOM.firstChild, 'coverage');
        }

        if (val.indexOf('Service') == -1) {
            removeFormPage('nonStandardPage', '');
            DeleteCapability('', 'ParamHTTP');
            DeleteCapability('', 'WebBrowser');

            if (val == 'vg:Authority') {
                document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' (Authority Resource)';
            }
            else if (val == 'vr:Organisation') {
                document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' (Organisation)';
            }
            else {
                document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' (Generic Resource)';
            }
        }
        else {
            document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' (Non-Standard Service)';
            if (isClone) {
                if (getCapability('ParamHTTP', true, false) == null) createEmptyCapability('', 'vs:ParamHTTP');
                if (getCapability('WebBrowser', true, false) == null) createEmptyCapability('', 'vr:WebBrowser');
            }
        }
        if (isClone) {
            formPages = [];
            setupForms(app, resourceDOM, newResource, isClone, false, null);
        }

        Ext.getCmp('title').focus(false, 100);
    }
    document.getElementById('pages').innerHTML = formPages.length;
}

function setResourceSubtype(isClone, val) {
    var titleTextLink = '';

    if (isClone) {
        if (getCapability('ParamHTTP', true, false) == null) createEmptyCapability('', 'vs:ParamHTTP');
        if (getCapability('WebBrowser', true, false) == null) createEmptyCapability('', 'vr:WebBrowser');
        titleTextLink = ' to';

        if( resourceDOM.firstChild.getElementsByTagName('coverage').length > 0 )
            DeleteElement(resourceDOM.firstChild.getElementsByTagName('coverage')[0], 'stc:STCResourceProfile');
    }
    
    if (val == 'cs:ConeSearch') {
        hideFieldSet('simpleImageAccessGroupFieldSet');
        hideFieldSet('simpleSpectralAccessGroupFieldSet');
        hideFieldSet('tableAccessProtocolGroupFieldSet');

        //mandatory elements for this type: first accessURL, etc
        Ext.getCmp('coneSearchFieldSet').query('fieldset[name=coneSearchSubSet]')[0].query('textfield[name=accessURL]')[0].allowBlank = false;
        Ext.getCmp('coneSearchGroupFieldSet').query('textfield[name=verbosity]')[0].allowBlank = false;

        document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + titleTextLink + ' Cone Search';
        if (isClone) {
            if (getCapability('cs:ConeSearch', true, true) == null) createEmptyCapability('cs:ConeSearch');
            DeleteCapability('sia:SimpleImageAccess');
            DeleteCapability('ssa:SimpleSpectralAccess');
            DeleteCapability('tr:TableAccess');
        }
    }
    else if (val == 'sia:SimpleImageAccess') {
        hideFieldSet('coneSearchGroupFieldSet');
        hideFieldSet('simpleSpectralAccessGroupFieldSet');
        hideFieldSet('tableAccessProtocolGroupFieldSet');

        //mandatory elements for this type: first accessURL, etc
        Ext.getCmp('simpleImageAccessFieldSet').query('fieldset[name=simpleImageAccessSubSet]')[0].query('textfield[name=accessURL]')[0].allowBlank = false;
        Ext.getCmp('simpleImageAccessGroupFieldSet').items.get('imageServiceType').allowBlank = false;

        document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + titleTextLink + ' Simple Image Access';
        if (isClone) {
            if (getCapability('sia:SimpleImageAccess', true, true) == null) createEmptyCapability('sia:SimpleImageAccess');
            DeleteCapability('cs:ConeSearch');
            DeleteCapability('ssa:SimpleSpectralAccess');
            DeleteCapability('tr:TableAccess');
        }
    }
    else if (val == 'ssa:SimpleSpectralAccess') {
        hideFieldSet('simpleImageAccessGroupFieldSet');
        hideFieldSet('coneSearchGroupFieldSet');
        hideFieldSet('tableAccessProtocolGroupFieldSet');

        //mandatory elements for this type: first accessURL, etc
        Ext.getCmp('simpleSpectralAccessFieldSet').query('fieldset[name=simpleSpectralAccessSubSet]')[0].query('textfield[name=accessURL]')[0].allowBlank = false;
        Ext.getCmp('simpleSpectralAccessGroupFieldSet').query('textfield[name=creationType]')[0].allowBlank = false;
        Ext.getCmp('simpleSpectralAccessGroupFieldSet').query('textfield[name=dataSource]')[0].allowBlank = false;

        document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + titleTextLink + ' Simple Spectral Access';
        if (isClone) {
            if (getCapability('ssa:SimpleSpectralAccess', true, true) == null) createEmptyCapability('ssa:SimpleSpectralAccess');
            DeleteCapability('sia:SimpleImageAccess');
            DeleteCapability('cs:ConeSearch');
            DeleteCapability('tr:TableAccess');
        }
    }
    else if (val == 'tr:TableAccess') {
        hideFieldSet('simpleImageAccessGroupFieldSet');
        hideFieldSet('simpleSpectralAccessGroupFieldSet');
        hideFieldSet('coneSearchGroupFieldSet');

        document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + titleTextLink + ' Table Access Protocol';
        if (isClone) {
            if (getCapability('tr:TableAccess', true, true) == null) createEmptyCapability('tr:TableAccess');
            DeleteCapability('sia:SimpleImageAccess');
            DeleteCapability('ssa:SimpleSpectralAccess');
            DeleteCapability('cs:ConeSearch');
        }
    }

    if (isClone) {
        formPages = [];
        setupForms(app, resourceDOM, newResource, isClone, false, null);
    }
    Ext.getCmp('title').focus(false, 100);
}

function DeleteCapability(standardType, interfaceName) {
    var sourceElement = null;
    var container = resourceDOM.firstChild;
    var caps = container.getElementsByTagName('capability');
    for (var index = 0; index < caps.length; ++index) {
        if (standardType.length == 0 && caps[index].attributes['standardID'] == undefined && caps[index].getElementsByTagName('interface')[0].attributes['xsi:type'].textContent.toUpperCase().indexOf(interfaceName.toUpperCase()) > -1) {
            sourceElement = caps[index];
            break;
        }
        else if (standardType.length > 0 && caps[index].attributes['xsi:type'] != undefined && caps[index].attributes['xsi:type'].textContent == standardType) {
            sourceElement = caps[index];
            break;
        }
    }
    if (sourceElement != null) {
        container.removeChild(sourceElement);
    }
}

//only works for one. add non-delete-as-traverse multiple version as needed
function DeleteElement(parent, name) {
    var els = parent.getElementsByTagName(name);
    if (els.length > 0) 
        parent.removeChild(els[0]);
}

function setDOMUniqueTag(newVal, oldVal, resourceDOM, tag) {
    if (oldVal != newVal && resourceDOM != null)
        resourceDOM.getElementsByTagName(tag)[0].textContent = newVal;
}

function setIdentifierAuthority(newVal, oldVal, resourceDOM) {
    if (oldVal != newVal && resourceDOM != null && newVal.length > 0) {

        if (newVal[0].data == undefined) { //editable: raw string
            identifierAuthority = newVal;
        }
        else
            identifierAuthority = newVal[0].data.identifier;

        if (!identifierAuthority.indexOf('ivo://') == 0) identifierAuthority = 'ivo://' + identifierAuthority;
        if( firstOrgRecord == true || oldVal != undefined)
            resourceDOM.getElementsByTagName('identifier')[0].textContent =  identifierAuthority + identifierSuffix;
    }
}

function setIdentifierSuffix(newVal, oldVal, resourceDOM) {
    if (oldVal != newVal && resourceDOM != null && newVal.length > 0) {
        identifierSuffix = newVal;
        if (!identifierSuffix.indexOf('/') == 0) identifierSuffix = '/' + identifierSuffix;
        if( firstOrgRecord == true || oldVal != undefined )
            resourceDOM.getElementsByTagName('identifier')[0].textContent = identifierAuthority + identifierSuffix;
    }
}

var curation = undefined;
function setCurationTag(resourceDOM) {
    if (curation == undefined) curation = resourceDOM.getElementsByTagName('curation')[0];
}

//from drop-down
function setDOMPublisher(newVal, oldVal, resourceDOM, dropDownValue) {
    setCurationTag(resourceDOM);
    if (curation != undefined && newVal != oldVal) {
        if (newVal.indexOf("ivo://") != 0) { //alt value.
            curation.getElementsByTagName('publisher')[0].setAttribute('ivo-id', '');
            curation.getElementsByTagName('publisher')[0].textContent = newVal;
        }
        else if (dropDownValue != null && dropDownValue.length > 0) {
                curation.getElementsByTagName('publisher')[0].setAttribute('ivo-id', newVal);
                curation.getElementsByTagName('publisher')[0].textContent = dropDownValue;
        }
    }
    Ext.getCmp('publisherInfo').allowBlank = false;
    Ext.getCmp('altPublisherInfo').allowBlank = true;
}

function setDOMAltPublisher(newVal, oldVal, resourceDOM) {
    setCurationTag(resourceDOM);
    if (curation != undefined && newVal != oldVal) {
        curation.getElementsByTagName('publisher')[0].setAttribute('ivo-id', '');
        curation.getElementsByTagName('publisher')[0].textContent = newVal;
    }
    Ext.getCmp('altPublisherInfo').allowBlank = false;
    Ext.getCmp('publisherInfo').allowBlank = true;
    //Ext.getCmp('enterAltPublisher').text = 'Select Existing Publisher from Drop Down';
}

function setDOMVersion(newVal, oldVal, resourceDOM) {
    setCurationTag(resourceDOM);
    if (curation != undefined && newVal != oldVal)
        curation.getElementsByTagName('version')[0].textContent = newVal;
}

var content = undefined;
function setContentTag() {
    if (content == undefined)
        content = resourceDOM.getElementsByTagName('content')[0];
}

function setDOMDescription(newVal, oldVal, resourceDOM) {
    setContentTag(resourceDOM);
     if( content != undefined && newVal != oldVal)
        content.getElementsByTagName('description')[0].textContent = newVal;
 }

 //ex: 'which curation/creator is associated with this form creator/name field'
 function getContainerLevelFormIndex(subcontainerfield) {
     var subcontainer = subcontainerfield.up('fieldset');

     //up not working from generated fields
     //var container = subcontainer.up('fieldset');
     var containerName = subcontainer.name.substring(0, subcontainer.name.indexOf('SubSet')) + 'FieldSet';
     var container = Ext.getCmp(containerName);

     var allsubcontainers = container.query('fieldset[name=' + subcontainer.name + ']');
     for (var i = 0; i < allsubcontainers.length; ++i) {
         if (allsubcontainers[i] == subcontainer)
             return i;
     }
     return -1;
 }

function setDOMCreator(formField, newVal, oldVal, resourceDOM) {
     var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('creator');
     var index = getContainerLevelFormIndex(formField);
     if (index > -1 )
        elements[index].getElementsByTagName(formField.name)[0].textContent = newVal;
 }

 function setDOMContact(formField, newVal, oldVal, resourceDOM) {
     var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('contact');
     var index = getContainerLevelFormIndex(formField);
     if (index > -1)
         elements[index].getElementsByTagName(formField.name)[0].textContent = newVal;
 }

 function setDOMDate(formField, newVal, oldVal, resourceDOM) {
     var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('date');
     var index = getContainerLevelFormIndex(formField);
     if (index > -1) {
         if (formField.name == 'role') {
             elements[index].attributes[formField.name].textContent = newVal;
         }
         else {
             elements[index].textContent = newVal;
         }
     }
 }

 function setDOMRelationship(formField, newVal, oldVal, resourceDOM) {
     var elements = resourceDOM.getElementsByTagName('content')[0].getElementsByTagName('relationship');
     var index = getContainerLevelFormIndex(formField);
     if (index > -1) {
         if (formField.name == 'relatedResource') {
             elements[index].getElementsByTagName(formField.name)[0].attributes["ivo-id"].textContent = newVal;
             elements[index].getElementsByTagName(formField.name)[0].textContent = formField.rawValue;
         }
         else {
             elements[index].getElementsByTagName(formField.name)[0].textContent = newVal;
         }
     }
 }

//generic function with element/form array findin
function setDOMArrayValue(formField, newVal, oldVal, resourceDOMUniqueSection, tag) {
    if (newVal != oldVal) {
        if (tag == undefined) tag = formField.name; //default, same as form field.
        var numElements = resourceDOMUniqueSection.getElementsByTagName(tag).length;
        var index = getContainerLevelFormIndex(formField);
        if (index > -1 && numElements > 0)
            resourceDOMUniqueSection.getElementsByTagName(tag)[index].textContent = newVal;
    }
}

function setTableComponentValue(formField, oldVal, newVal, tag) {
    if (newVal != oldVal) {
        if (tag == undefined) tag = formField.name; //default, same as form field.
        var isInCol = false;

        var targetContainer = formField.up('fieldset');
        var itemname = targetContainer.name;
        if (itemname.indexOf('SubSet') > -1) {
            itemname = itemname.substring(0, itemname.indexOf('SubSet'));
            isInCol = true;
        }
        else 
            itemname = itemname.substring(0, itemname.indexOf('FieldSet'));

        var targetTable = Ext.getCmp('tableFieldSet');
        var DOMTable = resourceDOM.getElementsByTagName('table')[0];

        if (isInCol) {
            var columns = targetTable.query('fieldset[name=' + itemname + 'SubSet]');
            for (var colIndex = 0; colIndex < columns.length; ++colIndex) {
                if( columns[colIndex] == targetContainer) {
                    DOMTable.getElementsByTagName('column')[colIndex].getElementsByTagName(tag)[0].textContent = newVal;
                    break;
                }
            }        
        }
        else {
            DOMTable.getElementsByTagName(tag)[0].textContent = newVal;
        }
    }
}

function setCapabilityComponentValue(formField, oldVal, newVal, tag) {
    if (newVal != oldVal) {
        if (tag == undefined) tag = formField.name; //default, same as form field.
        var isInInterface = false;

        var targetContainer = formField.up('fieldset');
        var itemname = targetContainer.name;
        if (itemname.indexOf('SubSet') > -1) {
            itemname = itemname.substring(0, itemname.indexOf('SubSet'));
            isInInterface = true;
        }
        else 
            itemname = itemname.substring(0, itemname.indexOf('GroupFieldSet'));

        var targetCapability = Ext.getCmp(itemname + 'FieldSet');
        var DOMCapability = null;
        var capabilities = resourceDOM.getElementsByTagName('capability');
        for (var i = 0; i < capabilities.length; ++i) {
            if (capabilities[i].attributes["xsi:type"] != undefined &&
                capabilities[i].attributes["xsi:type"].textContent.toUpperCase().indexOf(itemname.toUpperCase()) > -1) {
                DOMCapability = capabilities[i];
                break;
            } //for non-standard capabilities, go by interface type
            else if( capabilities[i].attributes["xsi:type"] == undefined && 
                     capabilities[i].getElementsByTagName('interface').length > 0 && capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"] != undefined &&
                     capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"].textContent.toUpperCase().indexOf(itemname.toUpperCase()) > -1) {
                DOMCapability = capabilities[i];
                break;
            }
        }

        if (isInInterface) {
            var interfaceIndex;
            for (interfaceIndex = 0; interfaceIndex < targetContainer.items.items.length; ++interfaceIndex) {
                if (targetCapability.items.items[interfaceIndex] == targetContainer)
                    break;
            }

            if (tag == "use") {
                DOMCapability.getElementsByTagName('interface')[interfaceIndex].getElementsByTagName('accessURL')[0].attributes["use"].textContent = newVal;
            }
            else if (tag == "version") {
                DOMCapability.getElementsByTagName('interface')[interfaceIndex].attributes["version"].textContent = newVal;
            }
            else if (tag == "xsitype") {
                DOMCapability.getElementsByTagName('interface')[interfaceIndex].attributes["xsi:type"].textContent = newVal;
            }
            else if (DOMCapability.getElementsByTagName('interface')[interfaceIndex].getElementsByTagName(tag)[0].textContent != newVal) {
                DOMCapability.getElementsByTagName('interface')[interfaceIndex].getElementsByTagName(tag)[0].textContent = newVal;
            }
        }
        else {
            if (tag.indexOf('/') > -1) {
                var subtag = tag.substring(tag.indexOf('/') + 1);
                tag = tag.substring(0, tag.indexOf('/'));
                DOMCapability.getElementsByTagName(tag)[0].getElementsByTagName(subtag)[0].textContent = newVal;
            }
            else
                DOMCapability.getElementsByTagName(tag)[0].textContent = newVal;
        }
    }
}
