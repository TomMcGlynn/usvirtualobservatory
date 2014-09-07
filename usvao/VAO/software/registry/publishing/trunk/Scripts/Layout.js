
var resourceManagementURL = "resourcemanagement.html?debug";

var firstOrgRecord = false;
var gotAuthInfo = false;
var defaultAuth = null;
var identifierAuthority = '';
var identifierSuffix = '';
getAuthInfo = function () {
    if( !gotAuthInfo) {
        Ext.Ajax.request({
            url: 'login.aspx?action=getauthinfo',
            method: 'GET',
            success: function (result, request) {
                var json = Ext.decode(result.responseText);
                if (json && json.success == true) {
                    defaultAuth = json.defaultauth;
                    if(defaultAuth == undefined)
                        firstOrgRecord = true;
                    app.getResourceXML(); //fills in resourceDOM object on success, clears on failure
                    gotAuthInfo = true;
                }
            },
            failure: function (result, request) {
                gotAuthInfo = true;
                firstOrgRecord = true;
                Ext.Msg.alert('Error', 'Timeout loading authorization information', function (btn, text) {
                       if (btn == 'ok') {
                           window.location = resourceManagementURL;
                       }
                })
            }
        })
    }
};

testURL = function (button) {
    var textfieldname = button.name.substring(0, button.name.indexOf('button'));
    var textfield = Ext.getCmp(textfieldname);
    if( textfield != null && textfield != undefined) {
        var urlvalue = textfield.value;

        Ext.Ajax.request({
        url: urlvalue,
        method: 'GET',
        success: function (result, request) {
            Ext.Msg.Alert('Success', 'Test URL responded successfully.');
        },
        failure: function (result, request) {
            Ext.Msg.alert('Error', 'Error loading test URL ' + request.url + ': ' + result.responseText);
        }
    })
    }
};

// custom Vtype for vtype:'PositiveInteger'
Ext.apply(Ext.form.field.VTypes, {
    PositiveInteger:  function(v) {
        return /^\d+$/.test(v);
    },
    PositiveIntegerText: 'This field must be a positive integer, no commas or periods as delimiters'
});

// custom Vtype for vtype:'DecimalNumber'
Ext.apply(Ext.form.field.VTypes, {
    DecimalNumber:  function(v) {
        return /^-?\d+\.?\d*$/.test(v);
    },
    DecimalNumberText: 'This field must be a decimal number with an optional period for the decimal mark, no commas as a thousands separator'
});

// custom Vtype for vtype:'UTCDateTime'
Ext.apply(Ext.form.field.VTypes, {
    UTCDateTime:  function(v) {
        return /^\d{4}-\d\d-\d\d(T\d\d:\d\d:\d\d(\.\d+)?)?$/.test(v);
    },
    UTCDateTimeText: 'This field must be a date or datetime in UTC format. YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS or YYYY-MM-DDTHH:MM:SS.ms '
});

Ext.define('PublishingWizard.Layout', {
    extend: 'Ext.panel.Panel',
    //extend: 'Ext.Viewport',

    statics: {},

    constructor: function (config) {
        var me = this;

        // Apply mandatory config items.       
        Ext.apply(config, {
            border: 0,
            layout: 'fit',
            width: 800,
            name: 'centerViewport',
            id: 'centerViewport',
            defaults: {
                autoScroll: 'true',
                autoHeight: 'true',
                //layout: 'fit'
            },
            items: [
            {
                xtype: 'form',
                id: 'formPanel',
                waitMsgTarget: true,
                url: './IngestResource.aspx',
                method: 'GET',
                fieldDefaults: {
                    labelWidth: 150,
                    width: 750,
                    msgTarget: 'side'
                },

                // configure how to read the XML data
                reader: Ext.create('Ext.data.reader.Xml', {
                    model: 'PublishingWizard.Resource',
                    record: 'ri:Resource',
                    successProperty: '@success'
                }),

                listeners: {
                    exception: function (proxy, response, operation) {
                    }
                },

                items: [
                    {
                        xtype: 'fieldset',
                        id: 'generalFieldSet',
                        title: 'General Information',
                        defaultType: 'textfield',
                        margin: 10,
                        items: [{
                            id: 'title',
                            maxLength: 512,
                            helpText: 'Typically, a Title will be a name by which the resource is formally known. Title should be an unabbreviated form (e.g., Hubble ACS Archive Images) rather than an  acronym unless the acronym is so well known as to be part of standard usage.  Publishers are encouraged, but not required, to define unique Titles. Ex: "Hubble ACS Archive Images"',
                            fieldLabel: '* Title',
                            emptyText: 'Resource Title for Searching',
                            name: 'title',
                            allowBlank: false,
                            listeners: { 'change': function (field, newVal, oldVal) { setDOMUniqueTag(newVal, oldVal, resourceDOM, 'title'); } }
                        },
                        {
                            id: 'shortName',
                            maxLength: 16,
                            helpText: 'The ShortName will be used where brief annotations for the resource name ' +  
                                       'are desired, such as in GUIs that might refer to many resources in a compact display.' +  
                                       'ShortName strings are limited to a maximum of sixteen characters.  Care should be' + 
                                       'taken to define illuminating ShortNames indicating either where the resource comes' + 
                                       'from or what data collection it provides.  ShortNames are not required to be unique.' +  
                                       'A resource provider may use the same ShortName for several related resources' + 
                                       '(e.g., different services that access the same collection), or the same ShortName might' + 
                                       'be used by different providers for common/mirrored resources.  In the latter case, the' + 
                                       'ShortName defined by the original publisher of the resource should have preference. EX: "HST.HDF_SOUTH"',
                            fieldLabel: '* shortName',
                            emptyText: 'Shortened Resource Title for Searching',
                            name: 'shortName',
                            allowBlank: false,
                            listeners: { 'change': function (field, newVal, oldVal) { setDOMUniqueTag(newVal, oldVal, resourceDOM, 'shortName'); } }
                        },
                          {
                              xtype: 'combo',
                              fieldLabel: '* IVOA Identifier Authority Prefix',
                              name: 'identifierAuthority',
                              id: 'identifierAuthority',
                              queryMode: 'remote',
                              store: storeAuthorityInfo,
                              displayField: 'identifier',
                              valueField: 'identifier',
                              hiddenName: 'identifier',
                              closable: false,
                              allowBlank: false,
                              editable: true,
                              selectOnFocus: false,
                              listeners: { 'change': function (field, newVal, oldVal) { setIdentifierAuthority(newVal, oldVal, resourceDOM); } }
                        },
                        {
                            id: 'identifierSuffix',
                            name: 'identifierSuffix',
                            maxLength: 400, // ivo:// auth / suffix max 512
                            helpText: 'An unambiguous URI-style reference to the resource. Identifiers must begin with the identifier of an institution already ' +
                                       'itself described in the Registry. This subfield represents the resource itself.',
                            fieldLabel: '* IVOA Identifier Suffix',
                            emptyText: 'Unique Identifier. This cannot be changed later.',
                            allowBlank: false,
                            listeners: { 'change': function (field, newVal, oldVal) { setIdentifierSuffix(newVal, oldVal, resourceDOM); } }
                        },
                        {
                            id: 'content/description',
                            helpText: 'Description may include but is not limited to: an abstract, table of contents, ' +
                                        'reference to a graphical representation of content or a free-text account of the content. ' + 
                                        'Thorough text descriptions are particularly encouraged in order to make text-based ' +
                                        'searches against the registries maximally useful.  Description should emphasize what ' +
                                        'the resource is about, as other matters such as who created it, when it was created, and ' +
                                        'where it is located are described elsewhere in the resource metadata.',
                            fieldLabel: '* Description',
                            emptyText: '',
                            name: 'content/description',
                            xtype: 'textareafield',
                            height: 200,
                            allowBlank: false,
                            listeners: { 'change': function (field, newVal, oldVal) { setDOMDescription(newVal, oldVal, resourceDOM); } }
                        },
                        {
                            id: 'managingOrg',
                            helpText: 'The organisation that owns or manages this authority identifier.' +
                                      ' This is almost always the organisation listed as the publisher of this Authority.',
                            fieldLabel: '* Managing Organisation',
                            emptyText: 'Full name of the organisation that owns or manages this authority identifier.',
                            name: 'managingOrg',
                            allowBlank: false,
                            listeners: { 'change': function (field, newVal, oldVal) { setDOMUniqueTag(newVal, oldVal, resourceDOM, 'managingOrg'); } }
                        }] //general fieldset items
                    },
                {
                    xtype: 'fieldset',
                    id: 'curationFieldSet',
                    title: 'Curation Information',
                    defaultType: 'textfield',
                    defaultType: 'textfield',
                    defaults: {  },
                    margin: 10,
                    items: [{
                        xtype: 'combo',
                        id: 'publisherInfo',
                        helpText: 'Person or organization registering this service.  Users of the resource should include Publisher in subsequent credits and acknowledgments.',
                        store: storePublisherInfo,
                        displayField: 'title',
                        valueField: 'identifier',
                        hiddenName: 'identifier',
                        queryMode: 'remote', 
                        fieldLabel: '* Publisher',
                        name: 'publisherInfo',
                        selectOnFocus: true,
                        autoselect: true,
                        typeAhead: true,
                        allowBlank: false,
                        data: 'all',
                        listeners: { 'change': function (field, newVal, oldVal) { setDOMPublisher(newVal, oldVal, resourceDOM, field.rawValue); Ext.getCmp('altPublisherInfo').hide(); } }
                    },
                    { //only for editing old records that don't have a specified ID with the publisher.
                        id: 'altPublisherInfo',
                        helpText: 'Person or organizatio registering this service. Publisher information should be selected from the drop-down if listed; this associates the field with a resource in the registry. Examples of a Publisher include a person or an organisation.  Users of the resource should include Publisher in subsequent credits and acknowledgments.',
                        fieldLabel: '* Publisher (Text Only, no ID)',
                        emptyText: 'Publisher Name (no ID attached)',
                        name: 'altPublisherInfo',
                        hidden: true,
                        listeners: { 'change': function (field, newVal, oldVal) { setDOMAltPublisher(newVal, oldVal, resourceDOM); } }
                    },
                    {
                        name: 'publisherTypeButton',
                        id: 'publisherTypeButton',
                        xtype: 'button',
                        text: 'Enter New Publisher as Text',
                        width: 150,
                        handler: function () { switchPublisherType(); }
                    },
                    {
                        id: 'version',
                        fieldLabel: 'Version #',
                        emptyText: 'Freetext version number for resource itself',
                        helpText: ' A label associated with the creation or availability (i.e., most recent release or version) of the resource. This is not necessarily the same as the version number for any service interface.',
                        name: 'version',
                        listeners: { 'change': function (field, newVal, oldVal) { setDOMVersion(newVal, oldVal, resourceDOM); } }
                    },
                        {
                            xtype: 'fieldset',
                            id: 'creatorFieldSet',
                            name: 'creatorFieldSet',
                            title: 'Creators',
                            defaultType: 'textfield',
                            autoheight: true,
                            width: 750,
                            items: [{
                                xtype: 'fieldset',
                                name: 'creatorSubSet',
                                defaultType: 'textfield',
                                autoheight: true,
                                defaults: { width: 700, labelWidth: 125 },
                                items: [{
                                    name: 'name',
                                    helpText: 'Examples of a Creator include a person or an organisation.  Users of the ' +
                                                'resource should include Creator in subsequent credits and acknowledgments.   Creator ' +
                                                'is intended to refer to the organisation or individuals responsible for the intellectual ' +
                                                'content of the resource, and not the organisation or individuals who may have developed ' +
                                                'the service by which the content is made available.  Guidelines:  1) If the resource is a ' +
                                                'data collection or service accessing a collection, then Creator fields should list the scientists responsible for the original data collection. Typically, this would be list of authors ' +
                                                'associated with the defining published paper for the collection. At a minimum, the PI or ' +
                                                'lead author should be given. Full names should be given, not just surnames. 2) For a ' +
                                                'collection that is a compilation of many separately published collections (e.g., an archive), then the Creator should be set to "various".  3) If the resource is an organisation ' +
                                                'not associated with a specific collection, the most appropriate value is either empty or ' +
                                                'the name of the person responsible to assembling the organisation. Often, an empty ' +
                                                'value is most appropriate. 4) If the resource is a Registry that publishes records for a ' +
                                                'single organisation, the Creator may contain the person(s) responsible for collecting or ' +
                                                'creating the metadata held in its records. Otherwise, it can be an empty value.  5) If the ' +
                                                'resource is an Authority, it should contain the name of the person that reserved the ' +
                                                'authority ID it records',
                                    fieldLabel: 'Name',
                                    emptyText: 'Personal Name or Organization',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMCreator(field, newVal, oldVal, resourceDOM); } }
                                },
                                {
                                    name: 'logo',
                                    helpText: 'A URL pointing to a graphical logo, which may be used to help identify the information resource. This will be displayed in some generated web pages summarizing the resource, if provided. ' +                                 
                                        ' There is no hard size limit on the image as only the URL is kept in the resource, but keep in mind that it will most often be displayed at a thumbnail size by registry clients.',
                                    fieldLabel: 'Logo',
                                    emptyText: 'Optional URL of a logo for displaying with your resources',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMCreator(field, newVal, oldVal, resourceDOM); } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this creator',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new creator',
                                width: 150,
                                handler: function () { fillCreator(addContainerAndXml(this)); }
                            }]
                        },
                        {
                            xtype: 'fieldset',
                            id: 'contactFieldSet',
                            name: 'contactFieldSet',
                            title: 'Contacts',
                            defaultType: 'textfield',
                            autoHeight: true,
                            width: 750,
                            items: [{
                                xtype: 'fieldset',
                                name: 'contactSubSet',
                                defaultType: 'textfield',
                                autoheight: true,
                                defaults: { width: 700, labelWidth: 125 },
                                items: [{
                                    name: 'name',
                                    fieldLabel: 'Name',
                                    helpText: 'The name of the contact. A personal name, "John P. Jones", or a group, "Archive Support Team".',
                                    emptyText: 'Name of a contact for this resource',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMContact(field, newVal, oldVal, resourceDOM); } }
                                },
                                    {
                                        name: 'address',
                                        helpText: 'The mailing address of the contact.' +
                                                'All components of the mailing address are given in one string, e.g., ' +
                                                '3700 San Martin Drive, Baltimore, MD 21218  USA',
                                        fieldLabel: 'Mailing Address',
                                        emptyText: 'Optional physical addres of your contact for this resource',
                                        xtype: 'textfield',
                                        listeners: { 'change': function (field, newVal, oldVal) { setDOMContact(field, newVal, oldVal, resourceDOM); } }
                                    },
                                    {
                                        name: 'email',
                                        vtype: 'email',
                                        helpText: 'Email address of the contact.',
                                        fieldLabel: 'Email Address',
                                        emptyText: 'Email address of your contact for this resource',
                                        xtype: 'textfield',
                                        listeners: { 'change': function (field, newVal, oldVal) { setDOMContact(field, newVal, oldVal, resourceDOM); } }
                                    },
                                    {
                                        name: 'telephone',
                                        helpText: 'The telephone number of the contact. Complete international dialing codes should be given, e.g., 1-410-338-4547',
                                        fieldLabel: 'Telephone Number',
                                        emptyText: 'Optional telephone number of your contact for this resource',
                                        xtype: 'textfield',
                                        listeners: { 'change': function (field, newVal, oldVal) { setDOMContact(field, newVal, oldVal, resourceDOM); } }
                                    },
                                    {
                                        name: 'delete',
                                        xtype: 'button',
                                        text: 'Delete this contact',
                                        disabled: true,
                                        width: 150,
                                        handler: function () { deleteContainerAndXml(this); }
                                    }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new contact',
                                width: 150,
                                handler: function () { fillContact(addContainerAndXml(this)); }
                            }]
                        },
                        {
                            xtype: 'fieldset',
                            id: 'contributorFieldSet',
                            name: 'contributorFieldSet',
                            title: 'Contributors',
                            defaultType: 'textfield',
                            autoheight: true,
                            width: 750,
                            items: [{
                                xtype: 'fieldset',
                                id: 'contributorSubSet',
                                name: 'contributorSubSet',
                                defaultType: 'textfield',
                                autoheight: true,
                                defaults: { width: 700, labelWidth: 125 },
                                items: [{
                                    name: 'name',
                                    helpText: 'An entity responsible for making contributions to the content of the resource. ' +
                                            'Examples of a Contributor include a person or an organisation.  Users of the ' +
                                            'resource should include Contributor in subsequent credits and acknowledgments.  Like ' + 
                                            'Creator, Contributor is intended to refer to the organisation or individuals responsible for ' + 
                                            'the intellectual content of the resource, and not the organisation or individuals who may ' +
                                            'have developed the service by which the content is made available.  Also see the Guidelines under Creator',
                                    fieldLabel: 'Name',
                                    emptyText: 'Other person or organisation contributing to this resource',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMArrayValue(field, newVal, oldVal, resourceDOM.getElementsByTagName('curation')[0], 'contributor'); } }

                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this contributor',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new contributor',
                                width: 150,
                                handler: function () { fillContributor(addContainerAndXml(this)); }
                            }] //contributorfieldset items
                        },
                                                {
                            xtype: 'fieldset',
                            id: 'dateFieldSet',
                            name: 'dateFieldSet',
                            title: 'Dates of Interest',
                            defaultType: 'textfield',
                            autoheight: true,
                            width: 750,
                            items: [{
                                xtype: 'fieldset',
                                name: 'dateSubSet',
                                defaultType: 'textfield',
                                autoheight: true,
                                defaults: { width: 700, labelWidth: 125 },
                                items: [{
                                    name: 'date',
                                    helpText: 'Date associated with an event in the life cycle of the resource. This field must be a date or datetime in UTC format. YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS or YYYY-MM-DDTHH:MM:SS.ms . This may be for keeping track of creation, or last update, or a timespan for data provided by a service.',
                                    fieldLabel: 'Date',
                                    emptyText: 'Date of interest for this resource. YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS.ms',
                                    xtype: 'textfield',
                                    vtype: 'UTCDateTime',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMDate(field, newVal, oldVal, resourceDOM); } }
                                },
						        {
						            xtype: 'combo',
						            displayField: 'description',
						            queryMode: 'local',
						            valueField: 'tag',
						            hiddenName: 'tag',
						            fieldLabel: 'Role of this date',
						            name: 'role',
						            id: 'role',
						            autoselect: true,
                                    helpText: 'The purpose of the role attribute is to indicate what aspect of the resource the date describes. This allows several date elements to be provided, each with a different role.',
						            store: storeRoleTypes,
						            selectOnFocus: true,
						            typeAhead: true,
						            listeners: { 'change': function (field, newVal, oldVal) { setDOMDate(field, newVal, oldVal, resourceDOM); } }
						        },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this date',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new date',
                                width: 150,
                                handler: function () { fillDate(addContainerAndXml(this)); }
                            }]
                        }] //curationfieldsetitems
                },
                   {
                       xtype: 'fieldset',
                       id: 'contentFieldSet',
                       name: 'contentFieldSet',
                       title: 'Content Information',
                       defaultType: 'textfield',
                       margin: 10,
                       defaults: { },
                       items: [
                       {
//                        xtype: 'fieldcontainer',
//                        layout: 'hbox',
//                        id: 'referenceURLcontainer',
//                        items: [
//                            {
                                id: 'referenceURL',
                                helpText: 'A URL pointing to additional human-readable information about the resource.',
                                fieldLabel: '* Reference URL',
                                emptyText: 'URL for more information',
                                name: 'referenceURL',
                                vtype: 'url',
                                xtype: 'textfield',
                                allowBlank: false,
                                //width: 620,
                                //labelWidth: 120,
                                listeners: { 'change': function (field, newVal, oldVal) { setDOMUniqueTag(newVal, oldVal, resourceDOM, 'referenceURL'); } }
//                            }
//                            ,{width: 10, heigth: 0, border: 0, autoEl: { tag: 'div' } }
//                            ,{
//                               name: 'referenceURLbutton',
//                                xtype: 'button',
//                                text: 'Test URL',
//                                width: 100,
//                                handler: function () { testURL(this); }
//                            } 
//                            ]
                        },
                        {
                            xtype: 'fieldset',
                            id: 'typeFieldSet',
                            name: 'typeFieldSet',
                            title: 'Content Types',
                            width: 750,
                            autoheight: true,
                            items: [{
                                xtype: 'fieldset',
                                name: 'typeSubSet',
                                defaults: { width: 700, labelWidth: 150 },
                                items: [{
                                    xtype: 'combo',
                                    helpText: 'The nature or genre of the content of the resource. Type includes terms describing general categories, functions, genres, or aggregation levels for content.',
                                    displayField: 'description',
                                    queryMode: 'local',
                                    valueField: 'tag',
                                    hiddenName: 'tag',
                                    fieldLabel: 'Type',
                                    name: 'contentType',
                                    autoselect: true,
                                    store: storeContentTypes,
                                    selectOnFocus: true,
                                    typeAhead: true,
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMArrayValue(field, newVal, oldVal, resourceDOM.getElementsByTagName('content')[0], 'type'); } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this content type',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                            }, //contenttype subset
                            {
                            name: 'add',
                            xtype: 'button',
                            text: 'Add new content type',
                            width: 150,
                            handler: function () { fillContentType(addContainerAndXml(this)); }
                        }] //type fieldset items
                    },
                    {
                        xtype: 'fieldset',
                        id: 'contentLevelFieldSet',
                        name: 'contentLevelFieldSet',
                        title: 'Levels of Content',
                        autoheight: true,
                        width: 750,
                        items: [{
                            xtype: 'fieldset',
                            name: 'contentLevelSubSet',
                            defaults: { width: 700, labelWidth: 150 },
                            items: [{
                                xtype: 'combo',
                                helpText: ' A description of the content level, or intended audience. ' +
                                        'VO resources will be available to professional astronomers, amateur astronomers, educators, and the general public.  These different audiences need a way to ' +
                                        'find material appropriate for their needs.',
                                displayField: 'description',
                                queryMode: 'local',
                                valueField: 'tag',
                                hiddenName: 'tag',
                                fieldLabel: 'Level of Content',
                                name: 'contentLevel',
                                autoselect: true,
                                store: storeContentLevels,
                                selectOnFocus: true,
                                typeAhead: true,
                                listeners: { 'change': function (field, newVal, oldVal) { setDOMArrayValue(field, newVal, oldVal, resourceDOM.getElementsByTagName('content')[0]); } }
                            },
                            {
                                name: 'delete',
                                xtype: 'button',
                                text: 'Delete this content level',
                                disabled: true,
                                width: 150,
                                handler: function () { deleteContainerAndXml(this); }
                            }] //content level subset items
                        },
                            {
                                name: 'add',
                                xtype: 'button',
                                width: 150,
                                text: 'Add new content level',
                                handler: function () { fillContentLevel(addContainerAndXml(this)); }
                            }] //content level fieldset items
                    },
                    {
                        xtype: 'fieldset',
                        id: 'relationshipFieldSet',
                        name: 'relationshipFieldSet',
                        title: 'Relationships with Other Resources',
                        autoheight: true,
                        width: 750,
                        items: [{
                            xtype: 'fieldset',
                            name: 'relationshipSubSet',
                            defaults: { width: 700, labelWidth: 150 },
                            items: [
                                /*{
                                    name: 'relatedResource',
                                    helpText: 'A registered IVOA resource that this resource is related to.',
                                    fieldLabel: 'Related Resource',
                                    emptyText: 'identifier of related resource',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMRelationship(field, newVal, oldVal, resourceDOM.getElementsByTagName('content')[0]); } }
                                },*/
                                {
                                    xtype: 'combo',
                                    helpText: 'Resource related to this one in some way',
                                    store: storeResourceInfo,
                                    displayField: 'title',
                                    valueField: 'identifier',
                                    hiddenName: 'identifier',
                                    queryMode: 'remote', 
                                    fieldLabel: 'Related Resource',
                                    name: 'relatedResource',
                                    emptyText: 'Select one of your own resources from the drop-down',
                                    selectOnFocus: true,
                                    autoselect: true,
                                    typeAhead: true,
                                    data: 'all',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMRelationship(field, newVal, oldVal, resourceDOM);} }
                                },
                                {
                                    xtype: 'combo',
                                    helpText: ' A description of a relationship to another resource. ',
                                    displayField: 'description',
                                    queryMode: 'local',
                                    valueField: 'tag',
                                    hiddenName: 'tag',
                                    fieldLabel: 'Type of Relationship',
                                    name: 'relationshipType',
                                    autoselect: true,
                                    emptyText: 'Select type of relationship to that resource from the drop-down',
                                    store: storeRelationshipTypes,
                                    selectOnFocus: true,
                                    typeAhead: false,
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMRelationship(field, newVal, oldVal, resourceDOM); } }
                            },
                            {
                                name: 'delete',
                                xtype: 'button',
                                text: 'Delete this relationship',
                                disabled: true,
                                width: 150,
                                handler: function () { deleteContainerAndXml(this); }
                            }] //relationship subset items
                        },
                            {
                                name: 'add',
                                xtype: 'button',
                                width: 150,
                                text: 'Add new relationship',
                                handler: function () { fillRelationship(addContainerAndXml(this)); }
                            }] //relationship fieldset items
                    },
                    {
                            xtype: 'fieldset',
                            id: 'subjectFieldSet',
                            name: 'subjectFieldSet',
                            title: 'Subjects',
                            defaultType: 'textfield',
                            autoheight: true,
                            width: 750,
                            items: [{
                                xtype: 'fieldset',
                                name: 'subjectSubSet',
                                defaultType: 'textfield',
                                autoheight: true,
                                defaults: { width: 700, labelWidth: 125 },
                                items: [
                                {
                                    name: 'subject',
                                    helpText: 'A list of the topics, object types, or other descriptive keywords about the ' +
                                                'resource. Subject is intended to provide additional information about the nature of the ' +
                                                'information provided by the resource.  Is this a catalog of quasars?  Of planetary nebulae?  Is this a tool for computing ephemerides?  Terms for Subject should be drawn from ' +
                                                'the IAU Astronomy Thesaurus (http://msowww.anu.edu.au/library/thesaurus/), though in ' +
                                                'the absence of suitable terms (the IAU Thesaurus is not complete in all areas of astronomical research) the following alternate collections of astronomical research terms may ' +
                                                'be used: Vizier keywords (CDS):  http://vizier.u-strasbg.fr/doc/ADCkwds.htx ' +
                                                'Astronomy journal keywords:  http://www.aanda.org/index2.php?option=com_content&task=view&id=170&Itemid=184',
                                    fieldLabel: 'Subject',
                                    emptyText: 'Freetext subject of this resource',
                                    xtype: 'textfield',
                                    listeners: { 'change': function (field, newVal, oldVal) { setDOMArrayValue(field, newVal, oldVal, resourceDOM.getElementsByTagName('content')[0]); } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this subject',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                            },
                           {
                               name: 'add',
                               xtype: 'button',
                               text: 'Add new subject',
                               width: 150,
                               handler: function () { fillSubject(addContainerAndXml(this)); }
                           }] //subjectfieldset items
                        }] //content fieldset items.
                   },
                   { 
                        xtype: 'panel',
                        id: 'standardPageFieldSet',
                        name: 'standardPageFieldSet',
                        margin: 0,
                        border: false,
                        items: [
                   {
                       xtype: 'fieldset',
                       id: 'coneSearchGroupFieldSet',
                       name: 'coneSearchGroupFieldSet',
                       title: 'Cone Search Service',
                       width: 778, //calculated to match the margins of the non-invisibly-nested panels
                       margin: 10,
                       items: [
                        {
                            xtype: 'combo',
                            displayField: 'description',
                            queryMode: 'local',
                            valueField: 'tag',
                            hiddenName: 'tag',
                            fieldLabel: '* Verbosity',
                            helpText: 'True or false, depending on whether the service supports the VERB keyword in the request.',
                            name: 'verbosity',
                            value: 'false',
                            autoselect: true,
                            store: storeTrueFalse,
                            selectOnFocus: true,
                            typeAhead: true,
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal); } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxRecords',
                            vtype: 'PositiveInteger',
                            fieldLabel: 'Max Records',
                            helpText: 'The largest number of records (entries) that the service will return, as an integer.',
                            emptyText: 'The largest number of records that the service will return.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxSR',
                            vtype: 'DecimalNumber',
                            fieldLabel: 'Max Search Radius',
                            helpText: 'The largest search radius, given in decimal degrees, that will be accepted by the service without returning an error condition. A value of 180.0 or an empty field indicates that there is no restriction. ',
                            emptyText: 'The largest search radius that will be accepted by the service.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'fieldset',
                            name: 'coneSearchGroupFieldSetTestQuery',
                            defaultType: 'textfield',
                            title: 'Test Query',
                            defaults: { width: 700 },
                            autoheight: true,
                            items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'ra',
                                        fieldLabel: 'RA',
                                        vtype: 'DecimalNumber',
                                        helpText: 'For a test query wherein your service will return a non-empty response: a right-ascension in the ICRS coordinate system for the positon of the center of the cone to search, given in decimal degrees.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        emptyText: 'Right Ascension for a valid test query, in decimal degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'dec',
                                        helpText: 'For a test query wherein your service will return a non-empty response: a declination in the ICRS coordinate system for the positon of the center of the cone to search, given in decimal degrees.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        fieldLabel: 'DEC',
                                        vtype: 'DecimalNumber',
                                        emptyText: 'Declination for a valid test query, in decimal degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'sr',
                                        helpText: 'For a test query wherein your service will return a non-empty response: the radius of the cone to search, given in decimal degrees.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        fieldLabel: 'Search Radius',
                                        emptyText: 'Search Radius for a valid test query, in decimal degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    }
                               ]
                        },
                        {
                            xtype: 'fieldset',
                            id: 'coneSearchFieldSet',
                            name: 'coneSearchFieldSet',
                            title: 'Interfaces to This Cone Search Capability',
                            defaultType: 'textfield',
                            autoheight: true,
                            items: [{
                                xtype: 'fieldset',
                                name: 'coneSearchSubSet',
                                defaultType: 'textfield',
                                //title: 'Interface to This Cone Search Service',
                                autoheight: true,
                                defaults: { width: 700 },
                                items: [{
                                    name: 'accessURL',
                                    fieldLabel: '* Access URL',
                                    vtype: 'url',
                                    helpText: 'URL for access to the Cone Search interface. The service must respond to a HTTP GET request represented by a URL having two parts: this base URL and valid Cone Search arguments RA, DEC, and SR, and, optionally, VERB.',
                                    emptyText: 'URL for access to the Cone Search interface',
                                    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                },
                                {
                                    name: 'version',
                                    fieldLabel: 'Version',
                                    helpText: 'Version number of the Cone Search Protocol expected at this URL. Example (current) version is 1.03; 1.0 is often used to note general compliance.',
                                    emptyText: 'Version Number of the Cone Search Protocol Used',
                                    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this Interface',
                                    width: 150,
                                    disabled: true,
                                    handler: function () { deleteInterfaceAndXml(this); }
                                }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new Interface to this Cone Search Service',
                                handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                            }] //csfieldset items
                        }
                       ] //conesearchgroupfieldset
                   },
               {
                   xtype: 'fieldset',
                   id: 'simpleImageAccessGroupFieldSet',
                   name: 'simpleImageAccessGroupFieldSet',
                   title: 'Simple Image Access Service',
                   width: 778, //calculated to match the margins of the non-invisibly-nested panels
                   margin: 10,
                   items: [
                        {
                            xtype: 'textfield',
                            name: 'maxRecords',
                            vtype: 'PositiveInteger',
                            helpText: 'The largest number of records (entries) that the Image Query web method will return, as an integer.',
                            fieldLabel: 'Max Records',
                            emptyText: 'The largest number of records the service will return.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxFileSize',
                            fieldLabel: 'Max File Size',
                            vtype: 'PositiveInteger',
                            helpText: 'The size of the largest file the service will return, in integer bytes.',
                            emptyText: 'The size of the largest file the service will return, in integer bytes.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
						{
						    xtype: 'combo',
						    displayField: 'description',
						    queryMode: 'local',
						    valueField: 'tag',
						    hiddenName: 'tag',
						    fieldLabel: '* Type of Image Service',
                            labelWidth: 175,
						    name: 'imageServiceType',
						    id: 'imageServiceType',
						    autoselect: true,
                            helpText: 'The category of Image Service that this services falls into. Cutout: ' +
                                'This is a service which extracts or "cuts out" rectangular regions of some larger image, returning an image of the requested size to the client. Such images are usually drawn from a database or a collection of survey images that cover some large portion of the sky. To be considered a cutout service, the returned image should closely approximate (or at least not exceed) the size of the requested region; however, a cutout service will not normally resample (rescale or reproject) the pixel data. A cutout service may mosaic image segments to cover a large region but is still considered a cutout service if it does not resample the data. Image cutout services are fast and avoid image degredation due to resampling. ' +
                                'Mosaic: This service is similar to the image cutout service but adds the capability to compute an image of the size, scale, and projection specified by the client. Mosaic services include services which resample and reproject existing image data, as well as services which generate pixels from some more fundamental dataset, e.g., a high energy event list or a radio astronomy measurement set. Image mosaics can be expensive to generate for large regions but they make it easier for the client to overlay image data from different sources. Image mosaicing services which resample already pixelated data will degrade the data slightly, unlike the simpler cutout service which returns the data unchanged. ' +
                                'Atlas: This service is similar to the image cutout service but adds the capability to compute an image of the size, scale, and projection specified by the client. Mosaic services include services which resample and reproject existing image data, as well as services which generate pixels from some more fundamental dataset, e.g., a high energy event list or a radio astronomy measurement set. Image mosaics can be expensive to generate for large regions but they make it easier for the client to overlay image data from different sources. Image mosaicing services which resample already pixelated data will degrade the data slightly, unlike the simpler cutout service which returns the data unchanged. ' +
                                'Pointed: This category of service provides access to collections of images of many small, "pointed" regions of the sky. "Pointed" images normally focus on specific sources in the sky as opposed to being part of a sky survey. This type of service usually applies to instrumental archives from observatories with guest observer programs (e.g., the HST archive) and other general purpose image archives (e.g., the ADIL). If a service provides access to both survey and pointed images, then it should be considered a Pointed Image Archive for the purposes of this specification; if a differentiation between the types of data is desired the pointed and survey data collections should be registered as separate image services. ',
						    store: storeImageServiceTypes,
						    selectOnFocus: true,
						    typeAhead: true,
						    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal); } }
						},
                        {
                            xtype: 'fieldset',
                            name: 'simpleImageAccessGroupFieldSetmaxImageExtent',
                            defaultType: 'textfield',
                            title: 'Maximum Image Extent',
                            autoheight: true,
                            defaults: { width: 700 },
                            items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'maxImageExtent/long',
                                        fieldLabel: 'Longitude (RA)',
                                        vtype: 'DecimalNumber',
                                        helpText: 'The maximum image extent on the sky in decimal degrees, e.g., "1.0".',
                                        emptyText: 'The maximum image extent on the sky in decimal degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'maxImageExtent/lat',
                                        fieldLabel: 'Latitude (Dec)',
                                        vtype: 'DecimalNumber',
                                        helpText: 'The maximum image extent on the sky in decimal degrees, e.g., "1.0".',
                                        emptyText: 'The maximum image extent on the sky in decimal degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    }
                               ]
                        },
                        {
                            xtype: 'fieldset',
                            name: 'simpleImageAccessGroupFieldSetmaxImageSize',
                            defaultType: 'textfield',
                            title: 'Maximum Image Size',
                            autoheight: true,
                            defaults: { width: 700 },
                            items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'maxImageSize/long',
                                        vtype: 'PositiveInteger',
                                        fieldLabel: 'Longitude (RA)',
                                        helpText: 'The maximum image size across RA, in integer pixels, e.g., "8192"',
                                        emptyText: 'The maximum image size, in pixels',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'maxImageSize/lat',
                                        fieldLabel: 'Latitude (Dec)',
                                        vtype: 'PositiveInteger',
                                        helpText: 'The maximum image size across DEC, in integer pixels, e.g., "8192"',
                                        emptyText: 'The maximum image size, in pixels',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    }
                               ]
                        },
                               {
                                   xtype: 'fieldset',
                                   name: 'simpleImageAccessGroupFieldSetTestQuery',
                                   defaultType: 'textfield',
                                   title: 'Test Query',
                                   autoheight: true,
                                   defaults: { width: 700 },
                                   items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'pos/long',
                                        fieldLabel: 'Longitude (RA)',
                                        vtype: 'DecimalNumber',
                                        helpText: 'For a test query wherein your service will return a non-empty response: a right-ascension in the ICRS coordinate system for the position of the field center, given in decimal degrees.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        emptyText: 'Longitude for a valid test query',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'pos/lat',
                                        fieldLabel: 'Latitude (Dec)',
                                        vtype: 'DecimalNumber',
                                        helpText: 'For a test query wherein your service will return a non-empty response: a declination in the ICRS coordinate system for the position of the field center, given in decimal degrees.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        emptyText: 'Latitude for a valid test query',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'size/long',
                                        fieldLabel: 'Size in Longitude (RA)',
                                        vtype: 'DecimalNumber',
                                        emptyText: 'Size of test query, longitude',
                                        helpText: 'For a test query wherein your service will return a non-empty response: The coordinate angular size of the region"s RA given in decimal degrees. A special case is SIZE=0. For an atlas or pointed image archive this tests whether the given point is in any image.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'size/lat',
                                        fieldLabel: 'Size in Latitude (Dec)',
                                        vtype: 'DecimalNumber',
                                        emptyText: 'Size of test query, latitude',
                                        helpText: 'For a test query wherein your service will return a non-empty response: The coordinate angular size of the region"s declination given in decimal degrees. A special case is SIZE=0. For an atlas or pointed image archive this tests whether the given point is in any image.  If a positional/size test query is defined at all, all POS/SIZE values should be provided. ',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                               ]
                               },
                       {
                           xtype: 'fieldset',
                           id: 'simpleImageAccessFieldSet',
                           name: 'simpleImageAccessFieldSet',
                           title: 'Interfaces to This Simple Image Access Capability',
                           defaultType: 'textfield',
                           autoheight: true,
                           items: [{
                               xtype: 'fieldset',
                               name: 'simpleImageAccessSubSet',
                               defaultType: 'textfield',
                               //title: 'Interface to This Simple Image Access Service',
                               autoheight: true,
                               defaults: { width: 700 },
                               items: [{
                                   name: 'accessURL',
                                   fieldLabel: '* Access URL',
                                   vtype: 'url',
                                   helpText: 'URL for access to the SIA interface. The service must respond to a HTTP GET request represented by a URL having two parts: this base URL and valid SIA arguments.',
                                   emptyText: 'URL for Simple Image Access access to this resource',
                                   listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                               },
                                {
                                    name: 'version',
                                    fieldLabel: 'Version',
                                    emptyText: 'Version Number of the SIA Protocol Used',
                                    helpText: 'Version number of the Simple Image Access Protocol expected at this URL. Ex: "1.0".',
                                    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this Interface',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteInterfaceAndXml(this); }
                                }]
                           },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new Interface to this Simple Image Access Service',
                                handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                            }] //siafieldset items
                       }
                       ] //simpleImageAccessgroupfieldset
               },
               {
                   xtype: 'fieldset',
                   id: 'simpleSpectralAccessGroupFieldSet',
                   name: 'simpleSpectralAccessGroupFieldSet',
                   title: 'Simple Spectral Access Service',
                   width: 778, //calculated to match the margins of the non-invisibly-nested panels
                   margin: 10,
                   items: [
                   		{
                   		    xtype: 'combo',
                   		    displayField: 'description',
                   		    queryMode: 'local',
                   		    valueField: 'tag',
                   		    hiddenName: 'tag',
                   		    fieldLabel: '* Data Creation Type',
                   		    name: 'creationType',
                            helpText: 'The category that describes the process used by the service to produce the dataset. Typically this describes only the processing ' +
                                'performed by the data service, but it could describe some additional earlier processing as well, e.g., if data returned by the service is partially precomputed from the source data.',
                   		    autoselect: true,
                   		    store: storeCreationTypes,
                   		    selectOnFocus: true,
                   		    typeAhead: true,
                   		    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal); } }
                   		},
                        {
                            xtype: 'combo',
                            displayField: 'description',
                            queryMode: 'local',
                            valueField: 'tag',
                            hiddenName: 'tag',
                            fieldLabel: 'Compliance Level',
                            helpText: 'This indicates the level at which a service instance complies with the SSA standard.' +
                            'Query: The service supports all of the capabilities and features of the SSA protocol identified as "must" in the specification, except that it does not support returning data in at least one SSA-compliant format (only data in some native project format is returned). ' +
                            'Minimal: The service supports all of the capabilities and features of the SSA protocol identified as "must" in the specification. ' +
                            'Full: The service supports, at a minimum, all of the capabilities and features of the SSA protocol identified as "must" or "should" in the specification. (Highest level of compliance)',
                            name: 'complianceLevel',
                            autoselect: true,
                            store: storeComplianceLevel,
                            selectOnFocus: true,
                            typeAhead: true,
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal); } }
                        },
                        {
                            xtype: 'combo',
                            displayField: 'description',
                            queryMode: 'local',
                            valueField: 'tag',
                            hiddenName: 'tag',
                            fieldLabel: '* Data Source',
                            name: 'dataSource',
                            autoselect: true,
                            helpText: 'The defined categories that specify where the spectral data originally came from, i.e., the type of data collections to which the service provides access.',
                            store: storeDataSource,
                            selectOnFocus: true,
                            typeAhead: true,
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal); } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxRecords',
                            vtype: 'PositiveInteger',
                            fieldLabel: 'Max Records',
                            helpText: 'The hard limit on the largest number of records (entries) that the query operation will return in a single response, as an integer.',
                            emptyText: 'The largest number of records the service will return in a single response.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'defaultMaxRecords',
                             vtype: 'PositiveInteger',
                            fieldLabel: 'Default Max Records',
                            helpText: 'The largest number of records (entries) that the service will return when the MAXREC parameter not specified in the query input, as an integer. If not specified the default maximum number of records in a query response is undefined.',
                            emptyText: 'The largest number of records the service will return when MAXREC not set.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxAperture',
                            helpText: 'Only relevant for services with the data creation type "Spectral Extractoin" The largest aperture diameter, in degrees, that can be supported upon request via the APERTURE input parameter by a service that supports the special extraction creation method.  A value of 360 (the default) means there is no fixed limit.',
                            fieldLabel: 'Max Aperture',
                            emptyText: 'For Spectral Extraction services, the largest aperture diameter, in degrees, that can be supported via APERTURE parameter.',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxFileSize',
                            fieldLabel: 'Max File Size',
                            vtype: 'DecimalNumber',
                            emptyText: 'The size of the largest file the service will return, in kilobytes.',
                            helpText: 'The estimated maximum output dataset file size in kilobytes. (Some older services have this listed in bytes.)',
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'textfield',
                            name: 'maxSearchRadius',
                            fieldLabel: 'Max Search Radius',
                            vtype: 'DecimalNumber',
                            emptyText: 'The maximum search radius in decimal degrees',
                            helpText: 'The hard limit on the largest number of records that the query operation will return in a single response. ' +
                                     'If not specified there is no predefined hard limit on the number of records in a query response.',                    
                            listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                        },
                        {
                            xtype: 'fieldset',
                            name: 'simpleSpectralAccessGroupFieldSetTestQuery',
                            defaultType: 'textfield',
                            title: 'Test Query',
                            autoheight: true,
                            defaults: { width: 700},
                            items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'long',
                                        fieldLabel: 'Longitude (RA)',
                                        vtype: 'DecimalNumber',
                                        emptyText: 'Longitude for a valid test query',                                 
                                        helpText: 'For a test query wherein your service will return a non-empty response: The longitude (e.g. Right Ascension) of the center of the search position specified in decimal degrees.  If a positional/size test query is defined at all, both POS values should be provided. ',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'lat',
                                        fieldLabel: 'Latitude (Dec)',
                                        vtype: 'DecimalNumber',
                                        emptyText: 'Latitude for a valid test query',
                                        helpText: 'For a test query wherein your service will return a non-empty response: The latitude (e.g. declination) of the center of the search position specified in decimal degrees.  If a positional/size test query is defined at all, both POS values should be provided.  ',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'size',
                                        fieldLabel: 'Size',
                                        vtype: 'DecimalNumber',
                                        helpText: 'For a test query wherein your service will return a non-empty response: The diameter of the search specified in decimal degrees.  If a positional/size test query is defined, both POS values should be provided; SIZE is optional. ',
                                        emptyText: 'Size for valid test query, in degrees',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'queryDataCmd',
                                        fieldLabel: 'Query Data Command',
                                        helpText: 'Fully specified queryData test query formatted as an URL argument list in the syntax specified by the SSA standard.  The list must exclude the REQUEST argument which is assumed to be set to "queryData".' +
                                                  ' VERSION may be included if the test query applies to a specific version of the service protocol. ' +
                                                  'If queryDataCmd is used to form a query,  Positional and Size test query values are not used; if the test query requires POS and SIZE these should be included directly in queryDataCmd (hence non-positional test queries can be supported). ' +
                                                  'This value must be a string in the form of name=value pairs delimited with ampersands.  A query may then be formed by appending to the baseURL the request argument.',
                                        emptyText: 'Query Data Command arguments for a valid test query',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    }
                               ]
                        },
                       {
                           xtype: 'fieldset',
                           id: 'simpleSpectralAccessFieldSet',
                           name: 'simpleSpectralAccessFieldSet',
                           title: 'Interfaces to This Simple Spectral Access Capability',
                           defaultType: 'textfield',
                           autoheight: true,
                           items: [{
                               xtype: 'fieldset',
                               name: 'simpleSpectralAccessSubSet',
                               defaultType: 'textfield',
                               //title: 'Interface to This Simple Spectral Access Service',
                               defaults: { width: 700 },
                               autoheight: true,
                               items: [{
                                   name: 'accessURL',
                                   fieldLabel: '* Access URL',
                                   helpText: 'URL for access to the SSA interface. The service must respond to a HTTP GET request represented by a URL having two parts: this base URL and valid SSA arguments.',
                                   vtype: 'url',
                                   emptyText: 'URL for Simple Spectral Access access to this resource',
                                   listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                               },
                                {
                                    name: 'version',
                                    fieldLabel: 'Version',
                                    emptyText: 'Version Number of the SSA Protocol Used',
                                    helpText: 'Version number of the SSA Protocol this service uses. Ex: "0.4" or "1.0"',
                                    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this Interface',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteInterfaceAndXml(this); }
                                }]
                           },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new Interface to this Simple Spectral Access Service',
                                handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                            }] //ssafieldset items
                       }
                       ] //simpleSpectralAccessgroupfieldset
               },
                    {
                        xtype: 'fieldset',
                        id: 'tableAccessProtocolGroupFieldSet',
                        name: 'tableAccessProtocolGroupFieldSet',
                        title: 'Table Access Protocol Service',
                        width: 778, //calculated to match the margins of the non-invisibly-nested panels
                        margin: 10,
                        items: [
                        {
                            xtype: 'fieldset',
                            id: 'tableAccessProtocolFieldSet',
                            name: 'tableAccessProtocolFieldSet',
                            title: 'Interfaces to This TAP Capability',
                            defaultType: 'textfield',
                            autoheight: true,
                            items: [{
                                xtype: 'fieldset',
                                name: 'tableAccessProtocolSubSet',
                                defaultType: 'textfield',
                                //title: 'Interface to This Service',
                                autoheight: true,
                                defaults: { width: 700 },
                                items: [{
                                    name: 'accessURL',
                                    fieldLabel: '* Access URL',
                                    vtype: 'url',
                                    emptyText: 'URL for parameterized access to this resource',
                                    listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                },
							   {
							       xtype: 'combo',
							       displayField: 'description',
							       queryMode: 'local',
							       valueField: 'tag',
							       hiddenName: 'tag',
							       fieldLabel: 'Type of URL',
							       name: 'accessURLUse',
                                   helpText: 'Use of the access URL above. BASE indicates the service expects the above URL to have various arguments appended. FULL indicates that a request to that URL with no additional arguments (they may be included as written above) will return data. DIR indicates the URL points to a directory of files.',
							       autoselect: true,
							       store: storeUrlTypes,
							       selectOnFocus: true,
							       typeAhead: true,
							       listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal, 'use'); } }
							   },
                               {
                                   name: 'version',
                                   fieldLabel: 'Version',
                                   emptyText: 'Version Number of the TAP Protocol Used',
                                   listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                               },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this Interface',
                                    disabled: true,
                                    width: 150,
                                    handler: function () { deleteInterfaceAndXml(this); }
                                }]
                            },
                            {
                                name: 'add',
                                xtype: 'button',
                                text: 'Add new Interface to this TAP Standard Service',
                                handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                            }] //fieldset items
                        },
                        /*{
                        xtype: 'fieldset',
                        id: 'tableFieldSet',
                        name: 'tableFieldSet',
                        title: 'Set of Tables Served',
                        defaultType: 'textfield',
                        autoheight: true,
                        items: [{
                        name: 'tableSetName',
                        width: 500,
                        fieldLabel: 'Table Set Name',
                        emptyText: 'Schema name of this set of tables',
                        value: 'default',
                        listeners: { 'change': function (field, newVal, oldVal) { } }
                        },
                        {
                        fieldLabel: 'Description',
                        emptyText: 'Description of the table set and how the tables are logically related',
                        name: 'description',
                        xtype: 'textareafield',
                        height: 100,
                        listeners: { 'change': function (field, newVal, oldVal) { } }
                        },*/
                            {
                            xtype: 'fieldset',
                            name: 'tableFieldSet',
                            id: 'tableFieldSet',
                            defaultType: 'textfield',
                            title: 'Individual Table Served',
                            autoheight: true,
                            width: 600,
                            items: [{
                                name: 'name',
                                fieldLabel: 'Name',
                                emptyText: 'A fully qualified name for the table including all catalog or schema prefixes necessary to identify it in a query.',
                                listeners: { 'change': function (field, newVal, oldVal) { } }
                            },
                                        {
                                            fieldLabel: 'Description',
                                            emptyText: 'Description of the table.',
                                            name: 'description',
                                            xtype: 'textareafield',
                                            height: 75,
                                            listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                        },
                                        {
                                            xtype: 'fieldset',
                                            name: 'columnFieldSet',
                                            defaultType: 'textfield',
                                            title: 'Columns',
                                            autoheight: true,
                                            width: 575,
                                            items: [{
                                                xtype: 'fieldset',
                                                name: 'columnSubSet',
                                                defaultType: 'textfield',
                                                title: 'Individual column',
                                                autoheight: true,
                                                items: [
                                                {
                                                    name: 'name',
                                                    width: 500,
                                                    fieldLabel: 'Column Name',
                                                    emptyText: 'Name of the Column',
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    fieldLabel: 'Description',
                                                    emptyText: 'Description of the column.',
                                                    name: 'description',
                                                    xtype: 'textareafield',
                                                    width: 500,
                                                    height: 50,
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    name: 'unit',
                                                    width: 500,
                                                    fieldLabel: 'Unit',
                                                    emptyText: 'The unit associated with all values associated with this parameter or table column.',
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    name: 'ucd',
                                                    width: 500,
                                                    fieldLabel: 'UCD',
                                                    emptyText: 'The name of a unified content descriptor that describes the scientific content of the parameter.',
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    name: 'utype',
                                                    width: 500,
                                                    fieldLabel: 'UType',
                                                    emptyText: 'An identifier for a concept in a data model that the data in this schema as a whole represent.',
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    name: 'dataType',
                                                    width: 500,
                                                    fieldLabel: 'Data Type',
                                                    emptyText: 'Name of the data type for the current parameter (double, float, array, etc.)',
                                                    listeners: { 'change': function (field, newVal, oldVal) { setTableComponentValue(field, oldVal, newVal) } }
                                                },
                                                {
                                                    name: 'delete',
                                                    xtype: 'button',
                                                    text: 'Delete this column',
                                                    disabled: true,
                                                    handler: function () { deleteSubContainerAndXml(this, 'column'); }
                                                }]
                                            },
                                            {
                                                name: 'add',
                                                xtype: 'button',
                                                text: 'Add new column in this table',
                                                handler: function () { fillColumn(addContainerAndXml(this)); }
                                            }]
                                        }/*,
                                        {
                                            name: 'add',
                                            xtype: 'button',
                                            text: 'Add new Table served by this TAP Standard Service',
                                            handler: function () { }
                                        }*/] //fieldset items
                        }/*,
                               {
                                   name: 'delete',
                                   xtype: 'button',
                                   text: 'Delete this Table',
                                   disabled: true,
                                   handler: function () {  }
                               }*/
                        /*]
                        }*/
                       ] //tableAccessProtocolGroupFieldset 
                    },
                    { height: 20, border: 0, autoEl: { tag: 'div' } },
                      {
                        xtype: 'fieldset',
                        id: 'coverageFieldSet',
                        title: 'Service Data Coverage (All Optional)',
                        margin: 10,
                        width: 778, //calculated to match the margins of the non-invisibly-nested panels
                        items: [
                            {
                                xtype: 'fieldset',
                                id: 'wavebandFieldSet',
                                name: 'wavebandFieldSet',
                                title: 'Wavebands of data served by this resource',
                                autoheight: true,
                                items: [{
                                    xtype: 'fieldset',
                                    name: 'wavebandSubSet',
                                    autoheight: true,
                                    defaults: { width: 700, labelWidth: 125 },
                                    items: [{
                                        xtype: 'combo',
                                        helpText: 'A named spectral region of the electro-magnetic spectrum that the resource"s spectral coverage overlaps with. Optional, multiple occurrences allowed',
                                        displayField: 'description',
                                        queryMode: 'local',
                                        valueField: 'tag',
                                        hiddenName: 'tag',
                                        fieldLabel: 'Waveband',
                                        name: 'waveband',
                                        autoselect: true,
                                        store: storeWavebands,
                                        selectOnFocus: true,
                                        typeAhead: true,
                                        listeners: { 'change': function (field, newVal, oldVal) { setDOMArrayValue(field, newVal, oldVal, resourceDOM.getElementsByTagName('coverage')[0]); } }
                                    },
                                {
                                    name: 'delete',
                                    xtype: 'button',
                                    text: 'Delete this waveband',
                                    width: 150,
                                    disabled: true,
                                    handler: function () { deleteContainerAndXml(this); }
                                }]
                                },
                           {
                               name: 'add',
                               xtype: 'button',
                               text: 'Add new waveband',
                               handler: function () { fillWaveband(addContainerAndXml(this)); }
                           }] //waveband items
                            }] //coverage items
                    }] //standard panel
                    },
                    { 
                        xtype: 'panel',
                        id: 'nonStandardPageFieldSet',
                        name: 'nonStandardPageFieldSet',
                        margin: 0,
                        border: false,
                        items: [
                        {
                            xtype: 'fieldset',
                            id: 'ParamHTTPGroupFieldSet',
                            name: 'ParamHTTPGroupFieldSet',
                            title: 'Non-standard Service Endpoints Using HTTP Parameters',
                            margin: 10,
                            width: 778, //calculated to match the margins of the non-invisibly-nested panels
                            items: [
                            {
                                xtype: 'fieldset',
                                id: 'ParamHTTPFieldSet',
                                name: 'ParamHTTPFieldSet',
                                title: 'Individual Non-standard Interfaces to this HTTP Service',
                                defaultType: 'textfield',
                                autoheight: true,
                                items: [{
                                    xtype: 'fieldset',
                                    name: 'ParamHTTPSubSet',
                                    defaultType: 'textfield',
                                    autoheight: true,
                                    defaults: { width: 700 },
                                    items: [{
                                        name: 'accessURL',
                                        helpText: 'URL for access to the non-standard parameterized interface. The service must respond to a HTTP GET request represented by a URL having two parts: a base URL and arguments that denote specific behaviour by the service itself.',
                                        fieldLabel: 'Access URL',
                                        vtype: 'url',
                                        emptyText: 'URL for access to this service with GET parameters',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
							       {
							           xtype: 'combo',
							           displayField: 'description',
							           queryMode: 'local',
							           valueField: 'tag',
							           hiddenName: 'tag',
							           fieldLabel: 'Type of URL',
							           name: 'accessURLUse',
                                       helpText: 'Use of the access URL above. BASE is standard and indicates the service expects that URL with standard arguments appended.',
							           autoselect: true,
							           store: storeUrlTypes,
							           selectOnFocus: true,
							           typeAhead: true,
							           listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal, 'use'); } }
							       },
                                   {
                                       name: 'resultType',
                                       fieldLabel: 'result MIME type',
                                       emptyText: 'The MIME type of a document returned in the HTTP response',                                       
                                       helpText: 'The MIME type of a document returned in the HTTP response. Ex: "text/xml"',
                                       listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                   },
                                    {
                                        name: 'delete',
                                        xtype: 'button',
                                        text: 'Delete this Interface',
                                        disabled: true,
                                        width: 150,
                                        handler: function () { deleteInterfaceAndXml(this); }
                                    }]
                                },
                                {
                                    name: 'add',
                                    xtype: 'button',
                                    text: 'Add new Non-Standard Parameter-based HTTP Interface to this Service',
                                    handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                                }] //csfieldset items
                            }
                           ] //ParamHTTPgroupfieldset
                        },
                       { height: 20, border: 0, autoEl: { tag: 'div' } },
                       {
                           xtype: 'fieldset',
                           id: 'WebBrowserGroupFieldSet',
                           name: 'WebBrowserGroupFieldSet',
                           title: 'Web Browser-Based Interactive Access to this Service',
                           margin: 10,
                           items: [
                            {
                                xtype: 'fieldset',
                                id: 'WebBrowserFieldSet',
                                name: 'WebBrowserFieldSet',
                                title: 'Individual Web Pages for Browser-Based Access to this Service',
                                defaultType: 'textfield',
                                autoheight: true,
                                items: [{
                                    xtype: 'fieldset',
                                    name: 'WebBrowserSubSet',
                                    defaultType: 'textfield',
                                    autoheight: true,
                                    defaults: { width: 700 },
                                    items: [{
                                        name: 'accessURL',
                                        helpText: 'The accessURL represents the URL of the web form itself for accessing a service.',
                                        fieldLabel: 'Access URL',
                                        vtype: 'url',
                                        emptyText: 'URL for web browser access to this resource, i.e. a web form.',
                                        listeners: { 'change': function (field, newVal, oldVal) { setCapabilityComponentValue(field, oldVal, newVal) } }
                                    },
                                    {
                                        name: 'delete',
                                        xtype: 'button',
                                        text: 'Delete this Interface',
                                        disabled: true,
                                        width: 150,
                                        handler: function () { deleteInterfaceAndXml(this); }
                                    }]
                                },
                                {
                                    name: 'add',
                                    xtype: 'button',
                                    text: 'Add new Web Browser-Based Interface',
                                    handler: function () { fillInterface(addInterfaceAndXml(this), null); }
                                }] //csfieldset items
                            }
                           ] //WebBrowserGroupfieldset
                           }
                       ] //nonstandard page field set
                   },
                    {
                        id: 'infoPanel',
                        border: 0,
                        margin: 0,
                        layout: 'hbox',
                        width: 650,
                        defaults: { border: 0, height: 30 },
                        items: [
                            { width: 50, height: 2, autoEl: { tag: 'div'} },
                            { autoEl: { tag: 'h3', html: '* indicates field is required'}}]
                    },
                    {
                        id: 'errorPanel',
                        name: 'errorPanel',
                        border: 0,
                        margin: 0,
                        layout: 'hbox',
                        width: 650,
                        defaults: { border: 0, height: 30 },
                    }], //formpanel items

                buttons: [
                {
                    text: "CANCEL",
                    id: 'buttonCancel',
                    handler: function () { window.location = resourceManagementURL; }
                },
                {
                    text: 'PREV',
                    disabled: true,
                    autoHeight: true,
                    //formBind: true,
                    id: 'buttonPrev',
                    handler: function () { prevPage(); }
                },
                {
                    text: 'NEXT',
                    disabled: true,
                    autoHeight: true,
                    //formBind: true,
                    id: 'buttonNext',
                    handler: function () { nextPage(); }
                },
                {
                    text: 'SAVE AS DRAFT',
                    disabled: true,
                    autoHeight: true,
                    //formBind: true,
                    id: 'buttonSubmitDraftResource',
                    handler: function () { submitDraftResource(appOptions.copy, appOptions.pending); }
                },
                {
                    text: 'SUBMIT',
                    disabled: true,
                    autoHeight: true,
                    formBind: true,
                    id: 'buttonSubmitResource',
                    handler: function () { submitResource(appOptions.copy, appOptions.pending); }
                }] //formpanel buttons
            }] //centerpanel items
        }); //viewport

        // Apply defaults for config.       
        Ext.applyIf(config, {
            width: 1100,
            autoScroll: true
        });

        this.callParent([config]);

        this.centerPanel = Ext.getCmp('centerViewport');
        //checkLoginInfo();
    }
});
