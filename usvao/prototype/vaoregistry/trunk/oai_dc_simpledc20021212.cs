﻿//------------------------------------------------------------------------------
// <autogenerated>
//     This code was generated by a tool.
//     Runtime Version: 1.1.4322.573
//
//     Changes to this file may cause incorrect behavior and will be lost if 
//     the code is regenerated.
// </autogenerated>
//------------------------------------------------------------------------------

// 
// This source code was auto-generated by xsd, Version=1.1.4322.573.
// 
namespace oai_dc {
    using System.Xml.Serialization;
    
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.openarchives.org/OAI/2.0/oai_dc/")]
    [System.Xml.Serialization.XmlRootAttribute("dc", Namespace="http://www.openarchives.org/OAI/2.0/oai_dc/", IsNullable=false)]
    public class oai_dcType {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("subject", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("rights", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("source", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("relation", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("creator", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("type", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("contributor", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("date", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("title", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("description", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("language", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("identifier", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("coverage", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("publisher", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlElementAttribute("format", typeof(elementType), Namespace="http://purl.org/dc/elements/1.1/")]
        [System.Xml.Serialization.XmlChoiceIdentifierAttribute("ItemsElementName")]
        public elementType[] Items;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("ItemsElementName")]
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public ItemsChoiceType[] ItemsElementName;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://purl.org/dc/elements/1.1/")]
    [System.Xml.Serialization.XmlRootAttribute("title", Namespace="http://purl.org/dc/elements/1.1/", IsNullable=false)]
    public class elementType {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Qualified, Namespace="http://www.w3.org/XML/1998/namespace")]
        public string lang;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.openarchives.org/OAI/2.0/oai_dc/", IncludeInSchema=false)]
    public enum ItemsChoiceType {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:subject")]
        subject,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:rights")]
        rights,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:source")]
        source,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:relation")]
        relation,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:creator")]
        creator,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:type")]
        type,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:contributor")]
        contributor,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:date")]
        date,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:title")]
        title,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:description")]
        description,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:language")]
        language,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:identifier")]
        identifier,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:coverage")]
        coverage,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:publisher")]
        publisher,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("http://purl.org/dc/elements/1.1/:format")]
        format,
    }
}