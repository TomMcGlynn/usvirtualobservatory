//because the schema is so complicated and we may need to create subforms, I'm starting off doing this by hand.
//This is lots of unnecessary typing, I think, and what of it can be eliminated should.
//In many cases, we're going to have to add 'if the DOM is missing this tag entirely', as well.

function GetDate(now) {
    var stringnow = now.getUTCFullYear() + '-';
    var time = now.getUTCMonth() + 1;
    if (time < 10)
        stringnow = stringnow + '0';
    stringnow = stringnow + time + '-';
    time = now.getUTCDate();
    if (time < 10)
        stringnow = stringnow + '0';
    stringnow = stringnow + time + 'T';
    time = now.getUTCHours();
    if (time < 10)
        stringnow = stringnow + '0';
    stringnow = stringnow + time + ':';
    time = now.getUTCMinutes();
    if (time < 10)
        stringnow = stringnow + '0';
    stringnow = stringnow + time + ':00Z';

    return stringnow;
}


function fillFormData(formPanel, resourceDOM, newResource, isCopy) {
    this.formPanel = formPanel;

    //set created/updated time to now.   
    var now = GetDate(new Date());
    resourceDOM.firstChild.attributes["updated"].textContent = now;
    if (newResource) {
        resourceDOM.firstChild.attributes["created"].textContent = now;
    }

    var genfieldset = formPanel.items.get('generalFieldSet');

    var elValue = resourceDOM.getElementsByTagName('title')[0].textContent;
    if (elValue != null && elValue != '') {
        genfieldset.items.get('title').setValue(elValue);
        if (isCopy) {
            document.getElementById('resourceTitle').innerHTML = 'Cloning Existing Resource ' + elValue + ':';
        }
        else if (!newResource) {
            document.getElementById('resourceTitle').innerHTML = 'Editing Resource ' + elValue + ':';
        }
    }

    elValue = resourceDOM.getElementsByTagName('shortName')[0].textContent;
    if (elValue != null && elValue != '')
        genfieldset.items.get('shortName').setValue(elValue);

    //if (isCopy)
    //    resourceDOM.getElementsByTagName('identifier')[0].textContent = "";
    if (!newResource && !isCopy) {
        elValue = resourceDOM.getElementsByTagName('identifier')[0].textContent;
        if (elValue != null && elValue != '') {
            genfieldset.items.get('identifier').setValue(elValue);
            genfieldset.items.get('identifier').disable();
        }
    }

    var content = resourceDOM.getElementsByTagName('content')[0];
    elValue = content.getElementsByTagName('description')[0].textContent;
    if (elValue != null && elValue != '')
        genfieldset.items.get('content/description').setValue(elValue);

    //curation tab
    var curationfieldset = formPanel.items.get('curationFieldSet');
    var curation = resourceDOM.getElementsByTagName('curation')[0];
    var elAttribute = '';

    if (curation.getElementsByTagName('publisher').length == 0) {
        createEmptyElement(curation, 'publisher', '') //in curation, tag 'publisher', after nothing (first element)
    }
    else {
        elValue = curation.getElementsByTagName('publisher')[0].textContent;
        var foundAttribute = false;
        //get attribute id: set dropdown if it exists
        if (curation.getElementsByTagName('publisher')[0].attributes["ivo-id"] != undefined) {
            elAttribute = curation.getElementsByTagName('publisher')[0].attributes["ivo-id"].textContent;
            if (elAttribute.length > 0) {
                curationfieldset.query('textfield[name=publisherInfo]')[0].setValue(elAttribute);
                foundAttribute = true;
            }
        }
        if (!foundAttribute && elValue.length > 0) { //we have text but no optional ID in an existing record.
            curationfieldset.items.get('altPublisherInfo').show();
            curationfieldset.items.get('altPublisherInfo').setValue(elValue);
         }
    }

    if (curation.getElementsByTagName('version').length == 0)
        createEmptyElement(curation, 'version', 'publisher');
    else
        curationfieldset.items.get('version').setValue(curation.getElementsByTagName('version')[0].textContent);

    populateOrImportBlank(Ext.getCmp('creatorFieldSet'), curation, fillCreator, 'creator', 'version');
    populateOrImportBlank(Ext.getCmp('contactFieldSet'), curation, fillContact, 'contact', 'creator');
    populateOrImportBlank(Ext.getCmp('contributorFieldSet'), curation, fillContributor, 'contributor', 'contact');
    populateOrImportBlank(Ext.getCmp('dateFieldSet'), curation, fillDate, 'date', 'contributor');

    //content tab
    var contentfieldset = formPanel.items.get('contentFieldSet');
    if (content.getElementsByTagName('referenceURL').length == 0)
        createEmptyElement(content, 'referenceURL', 'description');
    else
        contentfieldset.items.get('referenceURL').setValue(content.getElementsByTagName('referenceURL')[0].textContent);

    populateOrImportBlank(Ext.getCmp('subjectFieldSet'), content, fillSubject, 'subject', '');
    populateOrImportBlank(Ext.getCmp('typeFieldSet'), content, fillContentType, 'type', 'referenceURL');
    populateOrImportBlank(Ext.getCmp('contentLevelFieldSet'), content, fillContentLevel, 'contentLevel', 'type');
    populateOrImportBlank(Ext.getCmp('relationshipFieldSet'), content, fillRelationship, 'relationship', 'contentLevel');

    var capabilities = resourceDOM.getElementsByTagName('capability');
    var group = Ext.getCmp('coneSearchGroupFieldSet');
    var specifiedCapabilityIndex = 0;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] != undefined &&
            capabilities[i].attributes["standardID"].textContent.toUpperCase().indexOf('CONESEARCH') > -1) {
            fillConeSearch(group, group.query('fieldset[name=coneSearchFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            if (!newResource) document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' Cone Search';
        }
    }
    group = Ext.getCmp('simpleImageAccessGroupFieldSet');
    specifiedCapabilityIndex = 0;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] != undefined &&
            capabilities[i].attributes["standardID"].textContent.toUpperCase().indexOf('SIA') > -1) {
            fillSIA(group, group.query('fieldset[name=simpleImageAccessFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            if (!newResource) document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' Simple Image Access';
        }
    }
    group = Ext.getCmp('simpleSpectralAccessGroupFieldSet');
    specifiedCapabilityIndex = 0;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] != undefined &&
            capabilities[i].attributes["standardID"].textContent.toUpperCase().indexOf('SSA') > -1) {
            fillSSA(group, group.query('fieldset[name=simpleSpectralAccessFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            if (!newResource) document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' Simple Spectral Access';
        }
    }
    group = Ext.getCmp('tableAccessProtocolGroupFieldSet');
    specifiedCapabilityIndex = 0;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] != undefined &&
            capabilities[i].attributes["standardID"].textContent.toUpperCase().indexOf('TAP') > -1) {
            fillTAP(group, group.query('fieldset[name=tableAccessProtocolFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            if (!newResource) document.getElementById('resourceTitle').innerHTML = document.getElementById('resourceTitle').innerHTML + ' Table Access Protocol';
        }
    }

    var table = resourceDOM.getElementsByTagName('table')[0];
    if (table != undefined) {
        var container = Ext.getCmp('tableFieldSet'); 
        fillTable(table, container);
    }

    //nonstandard capabilities from here on out
    group = Ext.getCmp('ParamHTTPGroupFieldSet');
    specifiedCapabilityIndex = 0;
    var foundCap = false;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] == undefined &&
            capabilities[i].getElementsByTagName('interface').length > 0 && capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"] != undefined &&
            capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"].textContent.toUpperCase().indexOf('PARAMHTTP') > -1) {
            fillParamHTTP(group, group.query('fieldset[name=ParamHTTPFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            foundCap = true;
        }
    }
    if (foundCap == false)
        createEmptyCapability('', 'vs:ParamHTTP');

    group = Ext.getCmp('WebBrowserGroupFieldSet');
    specifiedCapabilityIndex = 0;
    foundCap = false;
    for (var i = 0; i < capabilities.length; ++i) {
        if (capabilities[i].attributes["standardID"] == undefined &&
            capabilities[i].getElementsByTagName('interface').length > 0 && capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"] != undefined &&
            capabilities[i].getElementsByTagName('interface')[0].attributes["xsi:type"].textContent.toUpperCase().indexOf('WEBBROWSER') > -1) {
            fillWebBrowser(group, group.query('fieldset[name=WebBrowserFieldSet]')[specifiedCapabilityIndex++], capabilities[i]);
            foundCap = true;
        }
    }
    if (foundCap == false)
        createEmptyCapability('', 'vr:WebBrowser');

    var coverage = resourceDOM.getElementsByTagName('coverage')[0];
    if (coverage != undefined) {
        populateOrImportBlank(Ext.getCmp('wavebandFieldSet'), coverage, fillWaveband, 'waveband', '');
    }
    else {
        createEmptyElement(resourceDOM.firstChild, 'coverage', 'capability');
    }
}

//todo: populate or fill.
function fillTable(table, container) {
    container.query('textfield[name=name]')[0].setValue(table.getElementsByTagName('name')[0].textContent);
    container.query('textfield[name=description]')[0].setValue(table.getElementsByTagName('description')[0].textContent);
    //var columns = table.getElementsByTagName('column');
    var numcols = table.getElementsByTagName('column').length;
    var columnContainer = container.query('fieldset[name=columnFieldSet]')[0];
    for (var i = 0; i < numcols; ++i) {
        populateSection(columnContainer, table.getElementsByTagName('column')[i], fillColumn);
    }
}

//populates the EXT form with data from the XML if existent in an imported (edited/copied) resource. copies in relevant section from the empty new-resource example XML if not.
function populateOrImportBlank(EXTContainer, XMLContainer, fillFunction, tag, tagAfter) {
    if (XMLContainer.getElementsByTagName(tag).length == 0)
        createEmptyElement(XMLContainer, tag, tagAfter);
    else
        populateSection(EXTContainer, XMLContainer.getElementsByTagName(tag).length, fillFunction);
}

//assumes subcontainer is a field set.
function populateSection(container, count, fillFunction) {
    subcontainer = container.query('fieldset')[0];
    for (i = 0; i < count; ++i) {
        if (i > 0)  //no need to clone the first record, it's in the form as default. 
            subcontainer = cloneFormContainer(container);
        if (subcontainer != undefined)
            fillFunction(subcontainer, i);
    }
}


//tdower: note we have to make sure these don't fill from the top-level DOM in the case of complex nested forms for services.
//Also there may be bugs on the form side with not using fieldsets, or nested levels of them in the same case. this will require debugging.
function fillCreator(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('creator');
    index = getIndex( elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=name]')[0].setValue(elements[index].getElementsByTagName('name')[0].textContent);
        if (elements[index].getElementsByTagName('logo').length > 0)
            subcontainer.query('textfield[name=logo]')[0].setValue(elements[index].getElementsByTagName('logo')[0].textContent);
    }//tdower test logo[0]/logo[index]
}

function fillContact(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('contact');
    index = getIndex( elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=name]')[0].setValue(elements[index].getElementsByTagName('name')[0].textContent);
        if (elements[index].getElementsByTagName('address').length > 0)
            subcontainer.query('textfield[name=address]')[0].setValue(elements[index].getElementsByTagName('address')[0].textContent);
        if (elements[index].getElementsByTagName('email').length > 0)
            subcontainer.query('textfield[name=email]')[0].setValue(elements[index].getElementsByTagName('email')[0].textContent);
        if (elements[index].getElementsByTagName('telephone').length > 0)
            subcontainer.query('textfield[name=telephone]')[0].setValue(elements[index].getElementsByTagName('telephone')[0].textContent);
    }
}

function fillContributor(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('contributor');
    index = getIndex( elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=name]')[0].setValue(elements[index].textContent);
    }
}

function fillDate(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('curation')[0].getElementsByTagName('date');
    index = getIndex(elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=date]')[0].setValue(elements[index].textContent);
        if (elements[index].attributes["role"] != null)
            subcontainer.query('textfield[name=role]')[0].setValue(elements[index].attributes["role"].textContent);
    }
}

function fillSubject(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('content')[0].getElementsByTagName('subject');
    index = getIndex( elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=subject]')[0].setValue(elements[index].textContent);
    }
}

function fillContentType(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('content')[0].getElementsByTagName('type');
    index = getIndex( elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=contentType]')[0].setValue(elements[index].textContent);
    }
}

function fillContentLevel(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('content')[0].getElementsByTagName('contentLevel');
    index = getIndex(elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=contentLevel]')[0].setValue(elements[index].textContent);
    }
}

function fillRelationship(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('content')[0].getElementsByTagName('relationship');
    index = getIndex(elements, index);
    if (index >= 0 && index < elements.length) {
        if (elements[index].getElementsByTagName('relatedResource').length > 0 && elements[index].getElementsByTagName('relatedResource')[0].attributes["ivo-id"] != undefined)
            subcontainer.query('textfield[name=relatedResource]')[0].setValue(elements[index].getElementsByTagName('relatedResource')[0].attributes["ivo-id"].textContent);
        if (elements[index].getElementsByTagName('relationshipType').length > 0)
            subcontainer.query('textfield[name=relationshipType]')[0].setValue(elements[index].getElementsByTagName('relationshipType')[0].textContent);
     }
}

function fillWaveband(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('coverage')[0].getElementsByTagName('waveband');
    index = getIndex(elements, index);
    if (index >= 0 && index < elements.length) {
        subcontainer.query('textfield[name=waveband]')[0].setValue(elements[index].textContent);
    }
}

function getCapability(type, isFirst, mustBeStandard) {
    var currentCapability = null;
    var capabilities = resourceDOM.getElementsByTagName('capability');
    for (var i = 0; i < capabilities.length; ++i) {
        if (mustBeStandard == true) {
            if (capabilities[i].attributes["standardID"] != undefined &&
            capabilities[i].attributes['xsi:type'].textContent.toUpperCase().indexOf(type.toUpperCase()) > -1) {
                currentCapability = capabilities[i];
                if (isFirst != null && isFirst == true)
                    return currentCapability;
            }
        }
        else {
            if (capabilities[i].attributes["standardID"] == undefined) {
                var interfaces = capabilities[i].getElementsByTagName('interface');
                for (var j = 0; j < interfaces.length; ++j) {
                    if (interfaces[j].attributes["xsi:type"] != undefined && interfaces[j].attributes["xsi:type"].textContent.indexOf(type) > -1) {
                        currentCapability = capabilities[i];
                        if (isFirst != null && isFirst == true)
                            return currentCapability;
                    }
                }
            }
        }
    }
    return currentCapability;
}

function getInterface(capability, isFirst, mustBeStandard) {
    var currentInterface = null;
    var interfaces = capability.getElementsByTagName('interface');
    for (var i = 0; i < interfaces.length; ++i) {
        if ( (mustBeStandard && (interfaces[i].attributes["role"] != undefined && interfaces[i].attributes["role"].textContent.toUpperCase().indexOf('STD') > -1)) ||
             (!mustBeStandard && (interfaces[i].attributes["role"] == undefined || interfaces[i].attributes["role"].textContent.toUpperCase().indexOf('STD') == -1 )) ) {
            currentInterface = interfaces[i];
            if (isFirst != null && isFirst == true)
                return currentInterface;
        }
    }
    return currentInterface;
}

function fillColumn(subcontainer, index) {
    var elements = resourceDOM.getElementsByTagName('table')[0].getElementsByTagName('column');
    index = getIndex(elements, index);
    if (index >= 0 && index < elements.length) {
        if( elements[index].getElementsByTagName('name').length > 0 )
            subcontainer.query('textfield[name=name]')[0].setValue(elements[index].getElementsByTagName('name')[0].textContent);
        if (elements[index].getElementsByTagName('description').length > 0)
            subcontainer.query('textfield[name=description]')[0].setValue(elements[index].getElementsByTagName('description')[0].textContent);
        if (elements[index].getElementsByTagName('unit').length > 0)
            subcontainer.query('textfield[name=unit]')[0].setValue(elements[index].getElementsByTagName('unit')[0].textContent);
        if (elements[index].getElementsByTagName('ucd').length > 0)
            subcontainer.query('textfield[name=ucd]')[0].setValue(elements[index].getElementsByTagName('ucd')[0].textContent);
        if (elements[index].getElementsByTagName('utype').length > 0)
            subcontainer.query('textfield[name=utype]')[0].setValue(elements[index].getElementsByTagName('utype')[0].textContent);
        if (elements[index].getElementsByTagName('dataType').length > 0)
            subcontainer.query('textfield[name=dataType]')[0].setValue(elements[index].getElementsByTagName('dataType')[0].textContent);
    }
}

function fillConeSearch(groupcontainer, container, capability) {
    if (capability != null) {

        container = fillCapabilityInterface(groupcontainer, container, capability, 'ConeSearch');

        elements = capability.getElementsByTagName('maxSR');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxSR]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxSR', 'interface');

        var elements = capability.getElementsByTagName('maxRecords');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxRecords]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxRecords', 'maxSR');

        elements = capability.getElementsByTagName('verbosity');
        if( elements != null && elements.length > 0 )
            groupcontainer.query('textfield[name=verbosity]')[0].setValue(elements[0].textContent);

        elements = capability.getElementsByTagName('testQuery');
        if (elements != null && elements.length > 0) {
            groupcontainer.query('textfield[name=ra]')[0].setValue(elements[0].getElementsByTagName('ra')[0].textContent);
            groupcontainer.query('textfield[name=dec]')[0].setValue(elements[0].getElementsByTagName('dec')[0].textContent);
            groupcontainer.query('textfield[name=sr]')[0].setValue(elements[0].getElementsByTagName('sr')[0].textContent);
        }
        else
            createEmptyElement(capability, 'testQuery', 'verbosity', 'coneSearch');
    }
}

function fillSIA(groupcontainer, container, capability) {
    if (capability != null) {

        container = fillCapabilityInterface(groupcontainer, container, capability, 'SimpleImageAccess');
            
        //other, non-interfaces data here.
       elements = capability.getElementsByTagName('imageServiceType');
       if (elements != null && elements.length > 0)
           groupcontainer.query('textfield[name=imageServiceType]')[0].setValue(elements[0].textContent);

       var elements = capability.getElementsByTagName('maxRecords');
       if (elements != null && elements.length > 0)
           groupcontainer.query('textfield[name=maxRecords]')[0].setValue(elements[0].textContent);
       else
           createEmptyElement(capability, 'maxRecords', 'imageServiceType');
       elements = capability.getElementsByTagName('maxFileSize');
       if (elements != null && elements.length > 0)
           groupcontainer.query('textfield[name=maxFileSize]')[0].setValue(elements[0].textContent);
       else
           createEmptyElement(capability, 'maxFileSize', 'maxRecords');

       elements = capability.getElementsByTagName('maxImageExtent');
       if (elements != null && elements.length > 0) {
           groupcontainer.query('textfield[name=maxImageExtent/long]')[0].setValue(elements[0].getElementsByTagName('long')[0].textContent);
           groupcontainer.query('textfield[name=maxImageExtent/lat]')[0].setValue(elements[0].getElementsByTagName('lat')[0].textContent);
       }
       else
           createEmptyElement(capability, 'maxImageExtent', 'maxFileSize', 'SIA');

       elements = capability.getElementsByTagName('maxImageSize');
       if (elements != null && elements.length > 0) {
           groupcontainer.query('textfield[name=maxImageSize/long]')[0].setValue(elements[0].getElementsByTagName('long')[0].textContent);
           groupcontainer.query('textfield[name=maxImageSize/lat]')[0].setValue(elements[0].getElementsByTagName('lat')[0].textContent);
       }
       else
           createEmptyElement(capability, 'maxImageSize', 'maxImageExtent', 'SIA');

       //testQuery
       elements = capability.getElementsByTagName('testQuery');
       if (elements != null && elements.length > 0) {
           var subelements = elements[0].getElementsByTagName('pos');
           if (subelements.length > 0) {
               if (subelements[0].getElementsByTagName('long')[0] != undefined)
                   groupcontainer.query('textfield[name=pos/long]')[0].setValue(subelements[0].getElementsByTagName('long')[0].textContent);
               else
                   createEmptyElement(subelements[0], 'long', '', 'SIA');
               if (subelements[0].getElementsByTagName('lat')[0] != undefined)
                   groupcontainer.query('textfield[name=pos/lat]')[0].setValue(subelements[0].getElementsByTagName('lat')[0].textContent);
               else
                   createEmptyElement(subelements[0], 'lat', 'long', 'SIA');
           }
           else {
               createEmptyElement(elements[0], 'pos', '', 'SIA');
           }

           subelements = elements[0].getElementsByTagName('size');
           if (subelements.length > 0) {
               if (subelements[0].getElementsByTagName('long')[0] != undefined)
                   groupcontainer.query('textfield[name=size/long]')[0].setValue(subelements[0].getElementsByTagName('long')[0].textContent);
               else
                   createEmptyElement(subelements[0], 'long', '', 'SIA');
               if (subelements[0].getElementsByTagName('lat')[0] != undefined)
                   groupcontainer.query('textfield[name=size/lat]')[0].setValue(subelements[0].getElementsByTagName('lat')[0].textContent);
               else
                   createEmptyElement(subelements[0], 'lat', 'long', 'SIA');
           }
           else {
               createEmptyElement(elements[0], 'size', 'pos', 'SIA');
           }
       }
       else
           createEmptyElement(capability, 'testQuery', 'maxImageSize', 'SIA');
    }
}

function fillSSA(groupcontainer, container, capability) {
    if (capability != null) {
        container = fillCapabilityInterface(groupcontainer, container, capability, 'SimpleSpectralAccess');

        //other, non-interfaces data here.


        //todo: multiples of these. but at least one of each are required
        elements = capability.getElementsByTagName('creationType');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=creationType]')[0].setValue(elements[0].textContent);
        elements = capability.getElementsByTagName('dataSource');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=dataSource]')[0].setValue(elements[0].textContent);

        elements = capability.getElementsByTagName('complianceLevel');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=complianceLevel]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'complianceLevel', 'dataSource', 'SSA');
        elements = capability.getElementsByTagName('maxSearchRadius');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxSearchRadius]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxSearchRadius', 'complianceLevel', 'SSA');
        var elements = capability.getElementsByTagName('maxRecords');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxRecords]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxRecords', 'maxSearchRadius', 'SSA');

        elements = capability.getElementsByTagName('defaultMaxRecords');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=defaultMaxRecords]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'defaultMaxRecords', 'maxRecords', 'SSA');

        elements = capability.getElementsByTagName('maxAperture');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxAperture]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxAperture', 'defaultMaxRecords', 'SSA');
        elements = capability.getElementsByTagName('maxFileSize');
        if (elements != null && elements.length > 0)
            groupcontainer.query('textfield[name=maxFileSize]')[0].setValue(elements[0].textContent);
        else
            createEmptyElement(capability, 'maxFileSize', 'maxAperture', 'SSA');


        //testQuery
        elements = capability.getElementsByTagName('testQuery');
        if (elements != null && elements.length > 0) {
            var subelements = elements[0].getElementsByTagName('pos');
            if (subelements.length > 0) {
                if (subelements[0].getElementsByTagName('long')[0] != undefined)
                    groupcontainer.query('textfield[name=long]')[0].setValue(subelements[0].getElementsByTagName('long')[0].textContent);
                else
                    createEmptyElement(subelements[0], 'long', '', 'SSA');
                if (subelements[0].getElementsByTagName('lat')[0] != undefined)
                    groupcontainer.query('textfield[name=lat]')[0].setValue(subelements[0].getElementsByTagName('lat')[0].textContent);
                else
                    createEmptyElement(subelements[0], 'lat', 'long', 'SSA');
            }
            else {
                createEmptyElement(elements[0], 'pos', '', 'SSA');
            }
            if (elements[0].getElementsByTagName('size')[0] != undefined)
                groupcontainer.query('textfield[name=size]')[0].setValue(elements[0].getElementsByTagName('size')[0].textContent);
            else
                createEmptyElement(elements[0], 'size', 'pos', 'SSA');

            if (elements[0].getElementsByTagName('queryDataCmd')[0] != undefined)
                groupcontainer.query('textfield[name=queryDataCmd]')[0].setValue(elements[0].getElementsByTagName('queryDataCmd')[0].textContent);
            else
                createEmptyElement(elements[0], 'queryDataCmd', 'size', 'SSA');
        }
        else
            createEmptyElement(capability, 'testQuery', 'maxFileSize', 'SSA');
    }
}

function fillTAP(groupcontainer, container, capability) {
    /*if (capability != null) {
        container = fillCapabilityInterface(groupcontainer, container, capability, 'TableAccess');

        //other, non-interfaces data here.
    }*/
}

function fillParamHTTP(groupcontainer, container, capability) {
    if (capability != null) {
        container = fillCapabilityInterface(groupcontainer, container, capability);
    }
}

function fillWebBrowser(groupcontainer, container, capability) {
    if (capability != null) {
        container = fillCapabilityInterface(groupcontainer, container, capability);
    }
}

function fillCapabilityInterface(groupcontainer, container, capability, captype) {
    if (capability != null) {
        if (container == null) {
            container = getCapability(captype, true)
        }

        var subcontainers = container.query('fieldset'); //subsets which are interfaces
        var interfaces = capability.getElementsByTagName('interface');
        for (var i = 0; i < interfaces.length; ++i) {
            if (i > subcontainers.length - 1) { //clone interface subcontainers as necessary
                var newcontainer = subcontainers[0].cloneConfig();
                container.items.insert(i, newcontainer);
                fillInterface(newcontainer, interfaces[i]);
                container.doLayout();
            }
            else
                fillInterface(subcontainers[i], interfaces[i]);

            if (i != 0) {
                var deleteButtons = newcontainer.query('button[name=delete]');
                if (deleteButtons != undefined && deleteButtons.length > 0)
                    deleteButtons[0].enable();
            }
        }
    }
    return container;
}

//tdower todo: a better way? fillInterface from subcontainer is so far the only place we've had to hard code this
function isStandardTag(tag) {
    if (tag.toUpperCase().indexOf('CONESEARCH') > -1) return true;
    if (tag.toUpperCase().indexOf('SIMPLEIMAGE') > -1) return true;
    if (tag.toUpperCase().indexOf('SIMPLESPECTRAL') > -1) return true;
    if (tag.toUpperCase().indexOf('TABLEACCESS') > -1) return true;
    return false;
}

function fillInterface(subcontainer, interface) {
    if (interface == null) {
        var tag = subcontainer.name.substring(0, subcontainer.name.indexOf('SubSet'));
        interface = getInterface(getCapability(tag, true, isStandardTag(tag)), true, isStandardTag(tag)); 
    }

    if (interface != null) {
        //only required field for all interfaces/capabilities.
        subcontainer.query('textfield[name=accessURL]')[0].setValue(interface.getElementsByTagName('accessURL')[0].textContent);

        if (subcontainer.query('textfield[name=accessURLUse]') != undefined && subcontainer.query('textfield[name=accessURLUse]').length > 0 &&
        interface.getElementsByTagName('accessURL')[0].attributes['use'] != undefined && interface.getElementsByTagName('accessURL')[0].attributes['use'].textContent.length > 0)
            subcontainer.query('textfield[name=accessURLUse]')[0].setValue(interface.getElementsByTagName('accessURL')[0].attributes['use'].textContent);
        if (subcontainer.query('textfield[name=version]') != undefined && subcontainer.query('textfield[name=version]').length > 0 &&
        interface.attributes['version'] != undefined && interface.attributes['version'].textContent.length > 0)
            subcontainer.query('textfield[name=version]')[0].setValue(interface.attributes['version'].textContent);
        if (subcontainer.query('textfield[name=xsitype]') != undefined && subcontainer.query('textfield[name=xsitype]').length > 0 && interface.attributes['xsi:type'] != undefined)
            subcontainer.query('textfield[name=xsitype]')[0].setValue(interface.attributes['xsi:type'].textContent);
        if (subcontainer.query('textfield[name=resultType]') != undefined && subcontainer.query('textfield[name=resultType]').length > 0 && interface.getElementsByTagName('resultType').length > 0)
            subcontainer.query('textfield[name=resultType]')[0].setValue(interface.getElementsByTagName('resultType')[0].textContent);
    }
}


function getIndex(elements, index) {
    if (index == undefined) 
        return elements.length - 1;
    else 
        return index;
}


function cloneFormContainer(container) {
    var newcontainer = undefined;
    if (container != undefined) {
        var sampleSubContainer = container.query('fieldset')[0];
        if (sampleSubContainer != undefined) {
            newcontainer = sampleSubContainer.cloneConfig();
            var len = sampleSubContainer.items.items.length;
            for (var i = 0; i < len; ++i ) {
                if (sampleSubContainer.items.items[i].allowBlank == false) {
                    newcontainer.items.items[i].allowBlank = false;
                }
            }

            container.items.add(newcontainer);
            var deleteButtons = newcontainer.query('button[name=delete]');
            if (deleteButtons != undefined && deleteButtons.length > 0)
                deleteButtons[0].enable();

            var addButtons = container.query('button[name=add]');
            if (addButtons != undefined && addButtons.length > 0) { //move add button to bottom of items list
                container.items.remove(addButtons[0]);
            }

            container.items.add(addButtons[0]);
            container.doLayout();
        }
    }
    return newcontainer;
}

function deleteContainerAndXml(deletebutton) {
    var subcontainer = deletebutton.up('fieldset');
    if (subcontainer != undefined) {

        //which index (for DOM element) is this?
        var fieldsetlist = this.formPanel.query('fieldset[name=' + subcontainer.name + ']');
        var index = -1;
        for (i = 0; i < fieldsetlist.length && index < 0; ++i) {
        var deletebuttons = fieldsetlist[i].query('button[name=delete]');
            if (deletebuttons[0] == deletebutton)
                index = i;
        }

        if (index >= 0) {
            var tag = subcontainer.name.substring(0, subcontainer.name.indexOf('SubSet'));
            var taggedElements = resourceDOM.getElementsByTagName(tag);
            if (taggedElements.length > index) {
                taggedElements[index].parentNode.removeChild(taggedElements[index]);

                var container = Ext.getCmp(subcontainer.getEl().up('fieldset').id);
                container.remove(subcontainer);
                container.doLayout();
            }
        }
    }
}

function deleteSubContainerAndXml(deletebutton, tagname) {
    var subcontainer = deletebutton.up('fieldset');
    if (subcontainer != undefined) {

        var fieldsetlist = this.formPanel.query('fieldset[name=' + subcontainer.name + ']');
        var index = -1;
        for (i = 0; i < fieldsetlist.length && index < 0; ++i) {
            var deletebuttons = fieldsetlist[i].query('button[name=delete]');
            if (deletebuttons[0] == deletebutton)
                index = i;
        }

        if (index >= 0) {
            var taggedElements = resourceDOM.getElementsByTagName(tagname);
            if (taggedElements.length > index) {
                taggedElements[index].parentNode.removeChild(taggedElements[index]);

                var container = Ext.getCmp(subcontainer.getEl().up('fieldset').id);
                container.remove(subcontainer);
                container.doLayout();
            }
        }
    }
}

function deleteInterfaceAndXml(deletebutton) {
    var subcontainer = deletebutton.up('fieldset');
    if (subcontainer != undefined) {

        var fieldsetlist = this.formPanel.query('fieldset[name=' + subcontainer.name + ']');
        var index = -1; //index within the relevant capability.
        for (i = 0; i < fieldsetlist.length && index < 0; ++i) {
            var deleteButtons = fieldsetlist[i].query('button[name=delete]');
            if (deleteButtons[0] == deletebutton)
                index = i;
        }

        var tag = subcontainer.name.substring(0, subcontainer.name.indexOf('SubSet'));
        var capability = getCapability(tag, true, isStandardTag(tag));
        if (index >= 0) {
            var taggedElements = capability.getElementsByTagName('interface');
            if (taggedElements.length > index) {
                taggedElements[index].parentNode.removeChild(taggedElements[index]);

                var container = Ext.getCmp(subcontainer.getEl().up('fieldset').id);
                container.remove(subcontainer);
                container.doLayout();
            }
        }
    }
}

function addContainerAndXml(addbutton) {
    var newContainer = undefined;
     var maincontainer = addbutton.up('fieldset');
    if (maincontainer != undefined) {
        var tag = maincontainer.name.substring(0, maincontainer.name.indexOf('FieldSet'));
        var taggedElements = resourceDOM.getElementsByTagName(tag);
        newContainer = cloneFormContainer(maincontainer);

        var clonedNode = taggedElements[0].cloneNode(true);
        taggedElements[0].parentNode.insertBefore(clonedNode, taggedElements[taggedElements.length - 1].nextSibling);
    }
    return newContainer;
}

function addInterfaceAndXml(addbutton) {
    var maincontainer = addbutton.up('fieldset');
    if (maincontainer != undefined) {
        var capabilityType = maincontainer.name.substring(0, maincontainer.name.indexOf('FieldSet'));
        var newContainer = cloneFormContainer(maincontainer);

        var currentCapability = null;
        var standardCapability = false;
        var capabilities = resourceDOM.getElementsByTagName('capability');
        for (var i = 0; i < capabilities.length; ++i) {
            if (capabilities[i].attributes["standardID"] != undefined &&
                capabilities[i].attributes['xsi:type'].textContent.toUpperCase().indexOf(capabilityType.toUpperCase()) > -1) {
                currentCapability = capabilities[i];
                standardCapability = true;
                break;
            }
        }
        if (currentCapability == null) {
            for (var i = 0; i < capabilities.length; ++i) {
                if (capabilities[i].attributes["standardID"] == undefined &&
                    capabilities[i].getElementsByTagName('interface').length > 0 &&
                    capabilities[i].getElementsByTagName('interface')[0].attributes['xsi:type'].textContent.toUpperCase().indexOf(capabilityType.toUpperCase()) > -1) {
                    currentCapability = capabilities[i];
                    break;
                }
            }
        }

        if (currentCapability != null) {
            var oldinterface = getInterface(currentCapability, true, standardCapability);
            var clonedNode = oldinterface.cloneNode(true);
            oldinterface.parentNode.insertBefore(clonedNode, oldinterface.nextSibling);
            return newContainer;
        }
    }
    return null;
}

function createEmptyElement(container, tagName, tagAfter, parentCapabilityName) {
    if (container != undefined) {
        if (emptyResourceDOM == null) {
            Ext.Msg.alert('Failed', 'Failed to load sample resource for adding empty elements.', function (btn, text) {
                if (btn == 'ok') {
                    //window.location = resourceManagementURL;
                }
            })
        }

        var sourceElements = emptyResourceDOM.getElementsByTagName(tagName);
        var sourceElement = undefined;
        if (parentCapabilityName != undefined && parentCapabilityName.length > 0) {
            for (var i = 0; i < sourceElements.length; ++i) {

                //since testQueries can force this into being variably nested
                var parentNode = sourceElements[i].parentNode;
                while (parentNode != emptyResourceDOM.firstChild) {
                    if (parentNode.attributes['standardID'] != undefined &&
                        parentNode.attributes['standardID'].textContent.toUpperCase().indexOf(parentCapabilityName.toUpperCase()) > -1) {
                        sourceElement = sourceElements[i];
                        break;
                    }
                    else
                        parentNode = parentNode.parentNode;
                }
                if (sourceElement != undefined)
                    break;
            }
        }
        else
            sourceElement = sourceElements[0];

        var foundAfter = false;
        if (sourceElement != undefined) {
            var numEls = 0;
            if (tagAfter.length > 0) {
                var numEls = container.getElementsByTagName(tagAfter).length;
                if (numEls > 0) {
                    foundAfter = true;
                    container.insertBefore(sourceElement, container.getElementsByTagName(tagAfter)[numEls - 1].nextSibling);
                }
            }
            if (!foundAfter) {
                container.insertBefore(sourceElement, container.firstChild);
            }
        }
    }
}

function createEmptyCapability(standardType, interfaceName) {
    var sourceElement;
    var container = resourceDOM.firstChild;
    var emptyCaps = emptyResourceDOM.getElementsByTagName('capability');
    if (standardType.length == 0) {
        for (var index = 0; index < emptyCaps.length; ++index) {
            if (emptyCaps[index].attributes['standardID'] == undefined && emptyCaps[index].getElementsByTagName('interface')[0].attributes['xsi:type'].textContent.toUpperCase().indexOf(interfaceName.toUpperCase()) > -1) {
                sourceElement = emptyCaps[index];
                break;
            }
        }

        if (container.getElementsByTagName('coverage').length > 0)
            container.insertBefore(sourceElement, container.getElementsByTagName('coverage')[0]);
        else if (container.getElementsByTagName('table').length > 0 )
            container.insertBefore(sourceElement, container.getElementsByTagName('table')[0]);
        else
            container.insertBefore(sourceElement, container.lastChild.nextSibling);
    }
    else {
        //todo: fill required fields? 
        for (var index = 0; index < emptyCaps.length; ++index) {
            if (emptyCaps[index].attributes['xsi:type'] != undefined && emptyCaps[index].attributes['xsi:type'].textContent == standardType) {
                sourceElement = emptyCaps[index];
                break;
            }
        }
        if (container.getElementsByTagName('capability').length > 0)
            container.insertBefore(sourceElement, container.getElementsByTagName('capability')[0]);
        else if (container.getElementsByTagName('coverage').length > 0)
            container.insertBefore(sourceElement, container.getElementsByTagName('coverage')[0]);
        else if (container.getElementsByTagName('table').length > 0)
            container.insertBefore(sourceElement, container.getElementsByTagName('table')[0]);
        else
            container.insertBefore(sourceElement, container.lastChild.nextSibling);
    }
}

function getResourceType(resourceDOM) {
    var val = resourceDOM.firstChild.attributes["xsi:type"].textContent;

    if (val == 'vs:CatalogService') {
        //which kind?
        return getResourceSubType(val);
    }
    else {
        if (val.indexOf('Service') == -1) {
            return 'Generic Resource';
        }
        else
            return 'Non-Standard Service)';
    }
}

function getResourceSubType(mainType) {
    if (mainType == 'vs:CatalogService') {
        if (getCapability('ConeSearch', true, true) != null)
            return 'Cone Search';
        else if (getCapability('SimpleImageAccess', true, true) != null)
            return 'Simple Image Access';
        else if (getCapability('SimpleSpectralAccess', true, true) != null)
            return 'Simple Spectral Access';
        else if (getCapability('TableAccess', true, true) != null)
            return 'Table Access Protocol';
    }
    return '';
}

function addFormPage(title, subtitle) {
    if (formPages.indexOf(Ext.getCmp(title + subtitle + 'FieldSet')) == -1)
        formPages.push(Ext.getCmp( title + subtitle + 'FieldSet'));
}

function removeFormPage(title, subtitle) {
    if (formPages.indexOf(Ext.getCmp(title + subtitle + 'FieldSet')) > -1)
        formPages.splice(formPages.indexOf(Ext.getCmp(title + subtitle + 'FieldSet')), 1);
}

var hiddenSets = [];
function hideFieldSet(title) {
    hiddenSets.push(title);
}
