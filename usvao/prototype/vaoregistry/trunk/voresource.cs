﻿//------------------------------------------------------------------------------
// <autogenerated>
//     This code was generated by a tool.
//     Runtime Version: 1.0.3705.0
//
//     Changes to this file may cause incorrect behavior and will be lost if 
//     the code is regenerated.
// </autogenerated>
//------------------------------------------------------------------------------

// 
// This source code was auto-generated by xsd, Version=1.0.3705.0.
// 
using System.Xml.Serialization;


/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute("VODescription", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class VODescription {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute("Resource")]
    public genericResource[] Resource;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute("Resource", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class genericResource {
    
    /// <remarks/>
    public Title Title;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string Identifier;
    
    /// <remarks/>
    public Curation Curation;
    
    /// <remarks/>
    public Content Content;
    
    /// <remarks/>
    public Coverage Coverage;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string @ref;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string managedBy;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Title {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
    public string shortName;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTextAttribute()]
    public string Value;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute("Temporal", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class dateType {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute("Begin")]
    public System.Decimal[] Begin;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute("End")]
    public System.Decimal[] End;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Spectral {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlArrayItemAttribute("item", IsNullable=false)]
    public string[] SpecDesc;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlArrayItemAttribute("item", IsNullable=false)]
    public string[] Bandpass;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Coverage {
    
    /// <remarks/>
    public string Spatial;
    
    /// <remarks/>
    public Spectral Spectral;
    
    /// <remarks/>
    public dateType Temporal;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Content {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="date")]
    public System.DateTime Date;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlIgnoreAttribute()]
    public bool DateSpecified;
    
    /// <remarks/>
    public string Version;
    
    /// <remarks/>
    public string Description;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlArrayItemAttribute("item", IsNullable=false)]
    public string[] Subject;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlArrayItemAttribute("item", IsNullable=false)]
    public string[] ContentLevel;
    
    /// <remarks/>
    public string Facility;
    
    /// <remarks/>
    public string Instrument;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlArrayItemAttribute("item", IsNullable=false)]
    public string[] Format;
    
    /// <remarks/>
    public string Rights;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Contact {
    
    /// <remarks/>
    public string Name;
    
    /// <remarks/>
    public string Email;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Creator {
    
    /// <remarks/>
    public string CreatorName;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string Logo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Curation {
    
    /// <remarks/>
    public string Publisher;
    
    /// <remarks/>
    public Creator Creator;
    
    /// <remarks/>
    public string Contributor;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string ReferenceURL;
    
    /// <remarks/>
    public Contact Contact;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class Service : genericResource {
    
    /// <remarks/>
    public genericCapability Capability;
    
    /// <remarks/>
    public genericInterface Interface;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlIncludeAttribute(typeof(standardCapability))]
[System.Xml.Serialization.XmlRootAttribute("Capability", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class genericCapability {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string StandardURL;
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute("StdCapability", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class standardCapability : genericCapability {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string StandardID;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute("Interface", Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class genericInterface {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
    public string defaultTo;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class WebBrowser : genericInterface {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string InterfaceURL;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class WebService : genericInterface {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string InterfaceURL;
}

/// <remarks/>
[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource")]
[System.Xml.Serialization.XmlRootAttribute(Namespace="http://rai.ncsa.uiuc.edu/~rplante/VO/schemas/VOResource", IsNullable=false)]
public class GLUService : genericInterface {
    
    /// <remarks/>
    [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
    public string InterfaceURL;
}