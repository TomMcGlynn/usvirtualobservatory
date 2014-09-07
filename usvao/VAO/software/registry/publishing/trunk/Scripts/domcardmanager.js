
var formPages = [];
var currentPageIndex = null;
var errorsLoading = [];

Ext.define('tagDesc', {
    extend: 'Ext.data.Model',
    fields: [
            { type: 'string', name: 'tag' },
            { type: 'string', name: 'description' }
        ]
});


var resourceTypes = [
        //real type: 'vs:CatalogService'
        { 'tag': 'cs:ConeSearch', 'description': 'Cone Search Catalog Service: A standard service for searching a catalog ' },
        { 'tag': 'sia:SimpleImageAccess', 'description': 'Simple Image Access Catalog Service: A standard service for searching a catalog ' },
        { 'tag': 'ssa:SimpleSpectralAccess', 'description': 'Simple Spectral Access Catalog Service: A standard service for searching a catalog ' },

        { 'tag': 'vs:DataService', 'description': 'Data Service: A web page or other non-standard data service. (SLAP and TAP services are not supported using form view at this time. Please use the "Upload XML Resource" feature.)' },
        //{ 'tag': 'vr:Organisation', 'description': 'Organisation: An organisation to be referenced by other resources' },
        //{ 'tag': 'vg:Authority', 'description': 'Naming Authority: An archive itself which will publish other resources' },
        { 'tag': 'vr:Resource', 'description': 'Generic Resource: This resource does not fit into any specified category' }
 ];

var orgResourceTypes = [{ 'tag':  'vr:Organisation', 'description': 'Organisation: An organisation to be referenced by other resources' }];

var storeResourceTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: resourceTypes });

var trueFalseTypes = [
        { 'tag': 'true', 'description': 'true' },
        { 'tag': 'false', 'description': 'false' }
 ];
var storeTrueFalse = Ext.create('Ext.data.Store', { model: 'tagDesc', data: trueFalseTypes });

var roleTypes = [
        { 'tag': '', 'description': ' ' },
        { 'tag': 'creation', 'description': 'creation: date refers to when the resource was created.' },
        { 'tag': 'update', 'description': 'update: date refers to when the resource was updated.' },
        { 'tag': 'representative', 'description': 'representative: date is a rough representation of the time coverage of the resource (default).' }
 ];
var storeRoleTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: roleTypes });

var queryTypes = [
        { 'tag': 'GET', 'description': 'GET' },
        { 'tag': 'POST', 'description': 'POST' },
        { 'tag': 'GET AND POST', 'description': 'GET and POST' }
 ];
var storeQueryTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: queryTypes });

var imageServiceTypes = [
        { 'tag': 'Cutout', 'description': 'Cutout' },
        { 'tag': 'Mosaic', 'description': 'Mosaic' },
        { 'tag': 'Atlas', 'description': 'Atlas' },
        { 'tag': 'Pointed', 'description': 'Pointed' }
 ];
var storeImageServiceTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: imageServiceTypes });

//Allowed values are "archival", "cutout", "filtered","mosaic", "projection", "spectralExtraction","catalogExtraction"

var creationTypes = [
        { 'tag': 'archival', 'description': 'Archival' },
        { 'tag': 'cutout', 'description': 'Cutout' },
        { 'tag': 'filtered', 'description': 'Filtered' },
        { 'tag': 'mosaic', 'description': 'Mosaic' },
        { 'tag': 'projection', 'description': 'Projection' },
        { 'tag': 'spectralExtraction', 'description': 'Spectral Extraction' },
        { 'tag': 'catalogExtraction', 'description': 'Catalog Extraction' },
 ];
var storeCreationTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: creationTypes });

var complianceLevelTypes = [
        { 'tag': 'query', 'description': 'query: SSA support, but does not return compliant format' },
        { 'tag': 'minimal', 'description': 'minimal: SSA support, returns in a compliant format' },
        { 'tag': 'full', 'description': 'full: SSA support of all MUST or SHOULD parts of the specification' }
 ];
var storeComplianceLevel = Ext.create('Ext.data.Store', { model: 'tagDesc', data: complianceLevelTypes });

var dataSourceTypes = [
        { 'tag': 'survey', 'description': 'survey' },
        { 'tag': 'pointed', 'description': 'pointed' },
        { 'tag': 'custom', 'description': 'custom' },
        { 'tag': 'theory', 'description': 'theory' },
        { 'tag': 'artificial', 'description': 'artificial' }
 ];
var storeDataSource = Ext.create('Ext.data.Store', { model: 'tagDesc', data: dataSourceTypes });


var urlTypes = [
        {'tag': 'base', 'description': 'base: A base URL, that is, one requiring an extra portion to be appended before being invoked.'}, 
        {'tag': 'full', 'description': 'full: A full URL, that is, one that can be invoked directly without alteration. This usually returns a single document or file.'},
        { 'tag': 'dir', 'description': 'dir: URL points to a directory that will return a listing of files.' }
 ];

var storeUrlTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: urlTypes });

var interfaceTypes = [
        { 'tag': 'vs:ParamHTTP', 'description': 'Standard Param HTTP: interface uses standard parameters for the service.' },
        { 'tag': 'vr:WebBrowser', 'description': 'Non-standard interface for web browser.' },
        { 'tag': 'vr:WebService', 'description': 'Non-standard interface for web services.' }
 ];

var storeInterfaceTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: interfaceTypes });


var contentTypes = [
        { 'tag': '', 'description': ' ' },
        { 'tag': 'Other',	 'description': 'Other: resource that does not fall into any of the category names currently defined.' },
        { 'tag': 'Archive',	 'description': 'Archive: Collection of pointed observations' },
        { 'tag': 'Bibliography',	 'description': 'Bibliography: Collection of bibliographic reference, abstracts, and publications' },
        { 'tag': 'Catalog',	 'description': 'Catalog: Collection of derived data, primarily in tabular form' },
        { 'tag': 'Journal',	 'description': 'Journal: Collection of scholarly publications under common editorial policy' },
        { 'tag': 'Library',	 'description': 'Library: Collection of published materials (journals, books, etc.)' },
        { 'tag': 'Simulation',	 'description': 'Simulation: Theoretical simulation or model' },
        { 'tag': 'Survey',	 'description': 'Survey: Collection of observations covering substantial and contiguous areas of the sky' },
        { 'tag': 'Transformation',	'description': 'Transformation: A service that transforms data' },
        { 'tag': 'Education',	 'description': 'Education: Collection of materials appropriate for educational use, such as teaching resources, curricula, etc.' },
        { 'tag': 'Outreach',	'description': 'Outreach: Collection of materials appropriate for public outreach, such as press releases and photo galleries' },
        { 'tag': 'EPOResource',	 'description': 'EPO Resource: Collection of materials that may be suitable for EPO products but which are not in final product form, as in Type Outreach or Type Education. EPOResource would apply, e.g., to archives with easily accessed preview images or to surveys with easy-to-use images.' },
        { 'tag': 'Animation',	 'description': 'Animation: Animation clips of astronomical phenomena' },
        { 'tag': 'Artwork',	 'description': 'Artwork: Artist renderings of astronomical phenomena or objects' },
        { 'tag': 'Background',	 'description': 'Background: Background information on astronomical phenomena or objects' },
        { 'tag': 'BasicData',	 'description': 'Basic Data: Compilations of basic astronomical facts about objects, such as approximate distance or membership in constellation' },
        { 'tag': 'Historical',	 'description': 'Historical: Historical information about astronomical objects' },
        { 'tag': 'Photographic',	 'description': 'Photographic: Publication-quality photographs of astronomical objects' },
        { 'tag': 'Press',	 'description': 'Press: Press releases about astronomical objects' },
        { 'tag': 'Organisation',	'description': 'An organization that is a publisher or curator of other resources' },
        { 'tag': 'Project',	 'description': 'Project: A project that is a publisher or curator of other resources' },
        { 'tag': 'Registry',	 'description': 'Registry: a query service for which the response is a structured description of resources' },
    ];

var storeContentTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: contentTypes });

var contentLevels = [
        { 'tag': '', 'description': ' ' },
        { 'tag': 'General',	 'description':	 'General: Resource provides information appropriate for all users.'	},
        { 'tag': 'Elementary Education',	 'description':'Elementary Education: Resource provides information appropriate for use in elementary education (e.g. approximate ages 6-11).'},
        { 'tag': 'Middle School Education',	 'description': 'Middle School Education: Resource provides information appropriate for use in elementary education (e.g. approximate ages 14-18).'},
        { 'tag': 'Secondary Education',	 'description': 'Secondary Education: Resource provides information appropriate for use in middle school education (e.g. approximate ages 11-14).'},
        { 'tag': 'Community College',	 'description': 'Community College: Resource provides information appropriate for use in community/junior college or early university education.'},
        { 'tag': 'University',	'description': 'University: Resource provides information appropriate for use in university education.'},
        { 'tag': 'Research',	 'description': 'Research: Resource provides information appropriate for supporting scientific research.'},
        { 'tag': 'Amateur',	 'description':'Amateur: Resource provides information of interest to amateur astronomers.'},
        { 'tag': 'Informal Education',	 'description': 'Informal Education: Resource provides information appropriate for education at museums, planetariums, and other centers of informal learning.'},
    ];

var storeContentLevels = Ext.create('Ext.data.Store', { model: 'tagDesc', data: contentLevels });

var relationshipTypes = [
        { 'tag': '', 'description': '(no value)' },
        { 'tag': 'mirror-of', 'description': 'mirror-of: The current resource mirrors another resource.' },
        { 'tag': 'service-for', 'description': 'service-for: The current resource is a service that provides access to a data collection.' },
        { 'tag': 'served-by', 'description': 'served-by: The current resource can be accessed via the identified service.' },
        { 'tag': 'derived-from', 'description': 'derived-from: The current resource is derived from another resource.' },
        { 'tag': 'related-to', 'description': 'related-to: The current resource is related to another resource in an unspecified way.' }
    ];

var storeRelationshipTypes = Ext.create('Ext.data.Store', { model: 'tagDesc', data: relationshipTypes });

var wavebands = [
        { 'tag': '', 'description': ' ' },
        { 'tag': 'Radio', 'description': 'Radio: any wavelength > 10mm (or frequency < 30 GHz)' },
        { 'tag': 'Millimeter', 'description': 'Millimeter: 0.1mm <= wavelength < 10mm; 3000GHZ >= frequency >= 30GHz' },
        { 'tag': 'Infrared', 'description': 'Infrared: 1 micron <= wavelength <= 100 microns' },
        { 'tag': 'Optical', 'description': 'Optical: 0.3 microns <= wavelength <= 1 micron; 300 nm <= wavelength <= 1000 nm' },
        { 'tag': 'UV', 'description': 'UV: 0.1 micron <= wavelength <= 0.3 microns; 100 nm <= wavelength <= 300 nm; 1000 Angstroms <= wavelength <= 3000 Angstroms' },
        { 'tag': 'EUV', 'description': 'EUV: 100 Angstroms <= wavelength <= 1000 Angstroms; 12 eV <= energy <= 120 eV' },
        { 'tag': 'X-ray', 'description': 'X-ray: 0.1 Angstroms <= wavelength <= 100 Angstroms; 0.12 keV <= energy <= 120 keV' },
        { 'tag': 'Gamma-ray', 'description': 'Gamma-ray: energy >= 120 keV' }
    ];

var storeWavebands = Ext.create('Ext.data.Store', { model: 'tagDesc', data: wavebands });

var storeAuthorityInfo = Ext.create('Ext.data.Store', {
    autoLoad: true,
    fields: ['title', 'identifier'],
    proxy: {
        type: 'ajax',
        url: 'GetResourceInfo.aspx?action=myauthoritylist',
        reader: {
            type: 'json',
            root: 'AuthorityInfo'
        },
        listeners: {
            exception: function (proxy, response, operation) {
                errorsLoading.push('Error loading authority information from registry: ' + response.statusText);
            }
        }

    },
    storeId: 'storeAuthorityInfo',
    root: 'AuthorityInfo'
});

var storePublisherInfo = Ext.create('Ext.data.Store', {
    autoLoad: true,
    fields: ['title', 'identifier'],
    proxy: {
        type: 'ajax',
        url: 'GetResourceInfo.aspx?action=publisherlist',
        reader: {
            type: 'json',
            root: 'PublisherInfo'
        },
        listeners: {
            exception: function (proxy, response, operation) {
                errorsLoading.push('Error loading publisher information from registry: ' + response.statusText);
                Ext.Msg.alert('Error', 'Error loading publisher information from registry: ' + response.statusText);
            }
        }

    },
    storeId: 'storePublisherInfo',
    root: 'PublisherInfo'
});

//'my resources' as per managementlayout
var storeResourceInfo = Ext.create('Ext.data.Store', {
    autoLoad: true,
    fields: ['title', 'shortName', 'identifier', 'status', 'updated', 'type'],
    proxy: {
        type: 'ajax',
        url: 'GetResourceInfo.aspx?action=myList',
        reader: {
            type: 'json',
            root: 'ResourceInfo'
        },
        failure: function (result, request) {
            Ext.Msg.alert('Failed', result.responseText);
            myResources = null;
        },
        listeners: {
            exception: function (proxy, response, operation) {
                Ext.Msg.alert('Error', 'Error loading resource information from registry: ' + response.statusText);
            }
        }

    },
    storeId: 'storeResourceInfo',
    root: 'ResourceInfo'
});

function setupForms(app, resourceDOM, newResource, isCopy, pending) {
     
    var formPanel = Ext.getCmp('formPanel');
    formPages.push(Ext.getCmp('generalFieldSet'));
    formPages.push(Ext.getCmp('curationFieldSet'));
    formPages.push(Ext.getCmp('contentFieldSet'));

    var needCoverage = false;
    Ext.getCmp('coneSearchGroupFieldSet').hide();
    Ext.getCmp('simpleImageAccessGroupFieldSet').hide();
    Ext.getCmp('simpleSpectralAccessGroupFieldSet').hide();
    Ext.getCmp('tableAccessProtocolGroupFieldSet').hide();
    Ext.getCmp('coverageFieldSet').hide();
    if (getCapability('ConeSearch', true, true) != null) {
        addFormPage('standardPage', '');
        Ext.getCmp('coneSearchGroupFieldSet').show();
        needCoverage = true;
    }
    if (getCapability('SimpleImageAccess', true, true) != null) {
        addFormPage('standardPage', '');
        Ext.getCmp('simpleImageAccessGroupFieldSet').show();
        needCoverage = true;
    }
    if (getCapability('SimpleSpectralAccess', true, true) != null) {
        addFormPage('standardPage', '');
        Ext.getCmp('simpleSpectralAccessGroupFieldSet').show();
        needCoverage = true;
    }
    if (getCapability('TableAccess', true, true) != null) {
        addFormPage('standardPage', '');
        Ext.getCmp('tableAccessProtocolGroupFieldSet').show();
    }

    if( needCoverage || getCapability('ParamHTTP', true, false) != null || getCapability('WebBrowser', true, false) != null ) {
        formPages.push(Ext.getCmp('nonStandardPageFieldSet'));
    }

    if (needCoverage) {
        Ext.getCmp('coverageFieldSet').show();
    }

    if( firstOrgRecord) {
        resourceDOM.firstChild.attributes["xsi:type"].textContent = "vg:Authority";
        Ext.getCmp('identifierAuthority').emptyText= 'Identifier Authority. This cannot be changed later.';
        Ext.getCmp('identifierAuthority').helpText = 'Identifier Authority (short form, prefix to all identifiers for this organisation). Cannot be changed later.';
        Ext.getCmp('identifierSuffix').emptyText= 'Identifier Suffix for organisation (short form). Cannot be changed later.';
    }
    else if ( storeAuthorityInfo.data.length < 2 ){
        Ext.getCmp('identifierAuthority').helpText= 'Identifier Authority. If a user is only associated with one organisation, that value is required.';
        Ext.getCmp('identifierAuthority').disable();  
        Ext.getCmp('identifierAuthority').setValue(storeAuthorityInfo.data.items[0].raw['identifier']);
    }

    if(!firstOrgRecord && resourceDOM.firstChild.attributes["xsi:type"].textContent != "vg:Authority") {
        Ext.getCmp('generalFieldSet').remove(Ext.getCmp('managingOrg'));
        Ext.getCmp('generalFieldSet').doLayout();
    }

    Ext.getCmp('curationFieldSet').hide();
    Ext.getCmp('contentFieldSet').hide();

    Ext.getCmp('standardPageFieldSet').hide();
    Ext.getCmp('nonStandardPageFieldSet').hide();

    Ext.getCmp('buttonSubmitResource').hide();
    Ext.getCmp('buttonSubmitDraftResource').hide();

    currentPageIndex = 0;

    fillFormData(formPanel, resourceDOM, newResource, isCopy, pending);

    if (firstOrgRecord == true)
        Ext.getCmp('buttonSubmitDraftResource').disable();


    if (errorsLoading.length == 0) {
        Ext.getCmp('errorPanel').removeAll();
        Ext.getCmp('buttonSubmitResource').enable();

        if((!firstOrgRecord) && newResource || isCopy || (pending != '' && pending != undefined && pending != 'undefined'))
            Ext.getCmp('buttonSubmitDraftResource').enable();

        app.mainPanel.centerPanel.add(formPanel);
        Ext.getCmp('buttonNext').enable();
    }
    document.getElementById('pages').innerHTML = formPages.length;
    hideWaitMsg();

    if(Ext.getCmp('resourceTypeDropDown') != undefined) {
        Ext.getCmp('resourceTypeDropDown').enable();
        Ext.getCmp('resourceTypeDropDown').focus(false, 100);
    }
    else
        Ext.getCmp('title').focus(false, 100);
};

var resourceWin;
var clonedResourceType = null;
function showResourceTypeWindow(resourceDOM) {
    var title = 'Choose a resource type. This will determine the required information for your resource.';
    if( appOptions.copy ) {
        clonedResourceType = getResourceType(resourceDOM);
        title = 'Choose a type for your cloned resource. Current type is ' + clonedResourceType +'.';
    }
      
    resourceWin = new Ext.Window({
        id: 'resourceTypeWindow',
        title: title,
        width: 520,
        modal: true,
        closable: false,
        items: [
            {
                xtype: 'form',
                id: 'resourceTypeFormPanel',
                fieldDefaults: {
                    labelWidth: 100,
                    msgTarget: 'side'
                },
                items: [
                {
                    xtype: 'combo',
                    displayField: 'description',
                    queryMode: 'local',
                    valueField: 'tag',
                    hiddenName: 'tag',
                    fieldLabel: 'Resource Type',
                    width: 500,
                    name: 'resourceTypeDropDown',
                    id: 'resourceTypeDropDown',
                    //autoselect: true,
                    store: storeResourceTypes,
                    closable: false,
                    editable: false,
                    disabled: !appOptions.copy,
                    listeners: { 'change': function (field, newVal, oldVal) { Ext.getCmp('done').enable(); } },
                    onFocus: function() {  this.getPicker().focus(); }      
                }],
                buttons: [
                {
                    text: 'Continue',
                    id: 'done',
                    disabled: true,
                    handler: function () {
                        if( Ext.getCmp('resourceTypeDropDown').value != null ) {
                            resourceWin.hide();
                            setResourceType(appOptions.copy);
                        }
                    }
                }
            ]
            }
        ]
    });

    resourceWin.show();
}

function nextPage() {
    if (currentPageIndex + 1 < formPages.length) {
        formPages[currentPageIndex++].hide();
        formPages[currentPageIndex].show();

        if (hiddenSets != undefined) {
            for (var i = 0; i < hiddenSets.length; ++i) {
                Ext.getCmp(hiddenSets[i]).hide();
            }
        }
        document.getElementById('pagenum').innerHTML = currentPageIndex + 1;

        if (currentPageIndex + 1 == formPages.length) {
            Ext.getCmp('buttonNext').disable();
        }
        if( currentPageIndex != 0 )
        {
            //Ext.getCmp('buttonPrev').show();
            Ext.getCmp('buttonPrev').enable();
        }
    }
    if (currentPageIndex + 1 == formPages.length) {
        Ext.getCmp('buttonNext').disable();
        //Ext.getCmp('buttonNext').hide();
        Ext.getCmp('buttonSubmitResource').show();
        Ext.getCmp('buttonSubmitDraftResource').show();

        //show errors from all panels when submit is greyed out:
        if( formPanel.getForm().isValid() == false ) {
            var errors = '';
            errors = getFieldSetErrors(formPanel, errors);           
        
            if( errors.length > 0 ) 
                Ext.Msg.alert('Errors or missing information in form fields:', errors);
         }
    }
};

function getFieldSetErrors(fieldset, errors) {
     if( fieldset.items == undefined ) 
        return errors;

     var fields = fieldset.items.items, numfields = fields.length;
     for( var i = 0; i < numfields; i++ ) {
        if( fields[i].el.id.contains("FieldSet") || (fields[i].name != undefined && fields[i].name.contains("SubSet")) ) {
            errors = getFieldSetErrors(fields[i], errors);
        }
        else {
            if( !fields[i].disabled && fields[i].getErrors != undefined && fields[i].getErrors().length > 0 ) {
                var prefix = fields[i].name + ': ';
                var parentName = '';
                if( fieldset.name != undefined && fieldset.name.contains("SubSet")) 
                    parentName = fieldset.name.substring(0, fieldset.name.indexOf("SubSet"));
                else if (fieldset.name != undefined && fieldset.name.contains("GroupFieldSet")) 
                    parentName = fieldset.name.substring(0, fieldset.name.indexOf("GroupFieldSet"));

                if(parentName.length > 0)
                    prefix = parentName + '/' + prefix;

                errors = errors + prefix + fields[i].getErrors() + '  <br\>';
            }
        }  
     }  
     return errors;
};

function prevPage() {
    if (currentPageIndex > 0) {
        formPages[currentPageIndex--].hide();
        formPages[currentPageIndex].show();

        if (hiddenSets != undefined) {
            for (var i = 0; i < hiddenSets.length; ++i) {
                Ext.getCmp(hiddenSets[i]).hide();
            }
        }
        document.getElementById('pagenum').innerHTML = currentPageIndex + 1;

        if (currentPageIndex == 0) {         
            Ext.getCmp('buttonPrev').disable();
        }
        Ext.getCmp('buttonNext').enable();
    }
    if (currentPageIndex + 1 < formPages.length) {
        //Ext.getCmp('buttonNext').show();
        Ext.getCmp('buttonSubmitResource').hide();
        Ext.getCmp('buttonSubmitDraftResource').hide();
    }
};

function switchPublisherType() {
    var dropdown = Ext.getCmp('publisherInfo');
    var alttext = Ext.getCmp('altPublisherInfo');

    if (dropdown.hidden == false) {
        dropdown.allowBlank = true;
        dropdown.hide();
        alttext.show();
        alttext.allowBlank = false;
        Ext.getCmp('publisherTypeButton').setText('Select Existing Publisher');
     }
    else {
        alttext.show();
        alttext.allowBlank = false;
        dropdown.allowBlank = true;
        dropdown.hide();
        Ext.getCmp('publisherTypeButton').setText('Enter New Publisher as Text');
    }
    Ext.getCmp('curationFieldSet').doLayout();
}


function getXmlAsString(xmlDomObj) {
    return (typeof XMLSerializer !== "undefined") ?
      (new XMLSerializer()).serializeToString(xmlDomObj) :
      xmlDomObj.xml;
}

function buildDOMString() {
    var paramList = {};
    var testString = getXmlAsString(resourceDOM);
    paramList['DOM'] = testString;

    return paramList;
}

function submitDraftResource(isCopy, pending) {
    
    var formPanel = Ext.getCmp('formPanel');

    var url = formPanel.url + "?saveAsDraft=true";
    if( !isCopy && pending != undefined && pending != 'undefined')
        url = url + "&pending=true";

    formPanel.getForm().submit({
        clientValidation: true,
        url: url,
        submitEmptyText: false,
        method: 'POST',
        params: buildDOMString(),           //send the entire resourceDOM.
        success: function () {
            Ext.Msg.alert('Status', 'Draft Resource Publication Successful! Reopen draft in form editor to publish it.', function (btn, text) {
                if (btn == 'ok') {
                    var redirect = 'resourcemanagement.html?debug';
                    window.location = redirect;
                }
            });
        },
        failure: function (form, action) {
            if (action.failureType == 'server') {
                obj = Ext.decode(action.response.responseText);
                Ext.Msg.alert('Resource Creation Failed!', obj.errors.reason);
            } else if (action.failureType == Ext.form.action.Action.CLIENT_INVALID) {
                Ext.Msg.alert('Failure', 'Missing or invalid information from required field(s). All fields marked with an * are required. Some fields have maximum text lengths, or are required to be in valid URL or email address format.');
            }
            else {
                Ext.Msg.alert('Warning!', 'Publishing server is unreachable.');
            }
        }
    });
};

function submitResource(isCopy, pending) {
    
    var formPanel = Ext.getCmp('formPanel');
    var url = formPanel.url;
    if( !isCopy && pending != '' && pending != undefined && pending != 'undefined')
        url = url + "?pending=true";

    formPanel.getForm().submit({
        clientValidation: true,
        url: url,
        submitEmptyText: false,
        method: 'POST',
        params: buildDOMString(),           //send the entire resourceDOM.
        success: function () {
            Ext.Msg.alert('Status', 'Resource Publication Successful!', function (btn, text) {
                if (btn == 'ok') {
                    var redirect = 'resourcemanagement.html?debug';
                    window.location = redirect;
                }
            });
        },
        failure: function (form, action) {
            if (action.failureType == 'server') {
                obj = Ext.decode(action.response.responseText);
                Ext.Msg.alert('Resource Creation Failed!', obj.errors.reason);
            } else if (action.failureType == Ext.form.action.Action.CLIENT_INVALID) {
                Ext.Msg.alert('Failure', 'Missing or invalid information from required field(s). All fields marked with an * are required. Some fields have maximum text lengths, or are required to be in valid URL or email address format.');
            }
            else {
                Ext.Msg.alert('Warning!', 'Publishing server is unreachable.');
            }
        }
    });
};


function showWaitMsg() {
    Ext.MessageBox.show({
        msg: 'Loading resources, please wait...',
        //progressText: 'Saving...',
        //width: 300,
        //wait: true,
        //waitConfig: { interval: 200 },
        //icon: 'my-progress-class', 
    });
};

function hideWaitMsg() {
    Ext.MessageBox.hide();
};
