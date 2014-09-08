﻿//------------------------------------------------------------------------------
// <autogenerated>
//     This code was generated by a tool.
//     Runtime Version: 1.0.3705.288
//
//     Changes to this file may cause incorrect behavior and will be lost if 
//     the code is regenerated.
// </autogenerated>
//------------------------------------------------------------------------------

// 
// This source code was auto-generated by xsd, Version=1.0.3705.288.
// 
namespace net.ivoa.vr0_10 {
    using System.Xml.Serialization;
    
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(CSCapRestriction))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearchCapability))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SIACapRestriction))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SIACapability))]
    public class Capability {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
        public string standardID;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
        public string standardURL;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/ConeSearch/v0.3")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearchCapability))]
    public abstract class CSCapRestriction : Capability {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/ConeSearch/v0.3")]
    public class ConeSearchCapability : CSCapRestriction {
        
        /// <remarks/>
        public System.Single maxSR;
        
        /// <remarks/>
        public int maxRecords;
        
        /// <remarks/>
        public bool verbosity;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SIACapability))]
    public abstract class SIACapRestriction : Capability {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    public class SIACapability : SIACapRestriction {
        
        /// <remarks/>
        public ImageServiceType imageServiceType;
        
        /// <remarks/>
        public SkySize maxQueryRegionSize;
        
        /// <remarks/>
        public SkySize maxImageExtent;
        
        /// <remarks/>
        public ImageSize maxImageSize;
        
        /// <remarks/>
        public int maxFileSize;
        
        /// <remarks/>
        public int maxRecords;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    public enum ImageServiceType {
        
        /// <remarks/>
        Cutout,
        
        /// <remarks/>
        Mosaic,
        
        /// <remarks/>
        Atlas,
        
        /// <remarks/>
        Pointed,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    public class SkySize {
        
        /// <remarks/>
        public System.Single @long;
        
        /// <remarks/>
        public System.Single lat;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    public class ImageSize {
        
        /// <remarks/>
        public int @long;
        
        /// <remarks/>
        public int lat;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(WebBrowser))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ParamHTTP))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(GLUService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(WebService))]
    public abstract class Interface {
        
        /// <remarks/>
        public AccessURL accessURL;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class AccessURL {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        public AccessURLUse use;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool useSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute(DataType="anyURI")]
        public string Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public enum AccessURLUse {
        
        /// <remarks/>
        full,
        
        /// <remarks/>
        @base,
        
        /// <remarks/>
        dir,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class WebBrowser : Interface {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class ParamHTTP : Interface {
        
        /// <remarks/>
        public string resultType;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("param")]
        public Param[] param;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        public HTTPQueryType qtype;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool qtypeSpecified;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Param {
        
        /// <remarks/>
        public string name;
        
        /// <remarks/>
        public string description;
        
        /// <remarks/>
        public DataType dataType;
        
        /// <remarks/>
        public string unit;
        
        /// <remarks/>
        public string ucd;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class DataType {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="token")]
        [System.ComponentModel.DefaultValueAttribute("1")]
        public string arraysize = "1";
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public ScalarDataType Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public enum ScalarDataType {
        
        /// <remarks/>
        boolean,
        
        /// <remarks/>
        bit,
        
        /// <remarks/>
        unsignedByte,
        
        /// <remarks/>
        @short,
        
        /// <remarks/>
        @int,
        
        /// <remarks/>
        @long,
        
        /// <remarks/>
        @char,
        
        /// <remarks/>
        unicodeChar,
        
        /// <remarks/>
        @float,
        
        /// <remarks/>
        @double,
        
        /// <remarks/>
        floatComplex,
        
        /// <remarks/>
        doubleComplex,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public enum HTTPQueryType {
        
        /// <remarks/>
        GET,
        
        /// <remarks/>
        POST,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class GLUService : Interface {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class WebService : Interface {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Temporal {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(DataType="date")]
        public System.DateTime startTime;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool startTimeSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(DataType="date")]
        public System.DateTime endTime;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool endTimeSpecified;
        
        /// <remarks/>
        public System.Single resolution;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool resolutionSpecified;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class WavelengthRange {
        
        /// <remarks/>
        public System.Single min;
        
        /// <remarks/>
        public System.Single max;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Spectral {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("waveband")]
        public Waveband[] waveband;
        
        /// <remarks/>
        public WavelengthRange range;
        
        /// <remarks/>
        public System.Single resolution;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool resolutionSpecified;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public enum Waveband {
        
        /// <remarks/>
        Radio,
        
        /// <remarks/>
        Millimeter,
        
        /// <remarks/>
        Infrared,
        
        /// <remarks/>
        Optical,
        
        /// <remarks/>
        UV,
        
        /// <remarks/>
        EUV,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("X-ray")]
        Xray,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Gamma-ray")]
        Gammaray,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public abstract class Region {
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Spatial {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("region")]
        public Region[] region;
        
        /// <remarks/>
        public System.Single resolution;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool resolutionSpecified;
        
        /// <remarks/>
        public System.Single regionOfRegard;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool regionOfRegardSpecified;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Coverage {
        
        /// <remarks/>
        public Spatial spatial;
        
        /// <remarks/>
        public Spectral spectral;
        
        /// <remarks/>
        public Temporal temporal;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Relationship {
        
        /// <remarks/>
        public RelationshipType relationshipType;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("relatedResource")]
        public ResourceName[] relatedResource;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public enum RelationshipType {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("mirror-of")]
        mirrorof,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("service-for")]
        servicefor,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("derived-from")]
        derivedfrom,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("related-to")]
        relatedto,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class ResourceName {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute("ivo-id", Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="anyURI")]
        public string ivoid;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Source {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        public string format;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Content {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("subject")]
        public string[] subject;
        
        /// <remarks/>
        public string description;
        
        /// <remarks/>
        public Source source;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
        public string referenceURL;
        
        /// <remarks/>
        public Type type;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool typeSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("contentLevel")]
        public ContentLevel[] contentLevel;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("relationship")]
        public Relationship[] relationship;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public enum Type {
        
        /// <remarks/>
        Other,
        
        /// <remarks/>
        Archive,
        
        /// <remarks/>
        Bibliography,
        
        /// <remarks/>
        Catalog,
        
        /// <remarks/>
        Journal,
        
        /// <remarks/>
        Library,
        
        /// <remarks/>
        Simulation,
        
        /// <remarks/>
        Survey,
        
        /// <remarks/>
        Transformation,
        
        /// <remarks/>
        Education,
        
        /// <remarks/>
        Outreach,
        
        /// <remarks/>
        EPOResource,
        
        /// <remarks/>
        Animation,
        
        /// <remarks/>
        Artwork,
        
        /// <remarks/>
        Background,
        
        /// <remarks/>
        BasicData,
        
        /// <remarks/>
        Historical,
        
        /// <remarks/>
        Photographic,
        
        /// <remarks/>
        Press,
        
        /// <remarks/>
        Organisation,
        
        /// <remarks/>
        Project,
        
        /// <remarks/>
        Registry,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public enum ContentLevel {
        
        /// <remarks/>
        General,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Elementary Education")]
        ElementaryEducation,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Middle School Education")]
        MiddleSchoolEducation,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Secondary Education")]
        SecondaryEducation,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Community College")]
        CommunityCollege,
        
        /// <remarks/>
        University,
        
        /// <remarks/>
        Research,
        
        /// <remarks/>
        Amateur,
        
        /// <remarks/>
        [System.Xml.Serialization.XmlEnumAttribute("Informal Education")]
        InformalEducation,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Contact {
        
        /// <remarks/>
        public ResourceName name;
        
        /// <remarks/>
        public string address;
        
        /// <remarks/>
        public string email;
        
        /// <remarks/>
        public string telephone;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Date {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        [System.ComponentModel.DefaultValueAttribute("representative")]
        public string role = "representative";
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute(DataType="date")]
        public System.DateTime Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Creator {
        
        /// <remarks/>
        public ResourceName name;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
        public string logo;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Curation {
        
        /// <remarks/>
        public ResourceName publisher;
        
        /// <remarks/>
        public Creator creator;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("contributor")]
        public ResourceName[] contributor;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("date")]
        public Date[] date;
        
        /// <remarks/>
        public string version;
        
        /// <remarks/>
        public Contact contact;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute("Resource0_10",Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(Service))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SkyService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(TabularSkyService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearch))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SimpleImageAccess))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(Registry))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(Organisation))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(DataCollection))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(Authority))]
    public class Resource {
        
        /// <remarks/>
        public string title;
        
        /// <remarks/>
        public string shortName;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute(DataType="anyURI")]
        public string identifier;
        
        /// <remarks/>
        public Curation curation;
        
        /// <remarks/>
        public Content content;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="date")]
        public System.DateTime created;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool createdSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified, DataType="date")]
        public System.DateTime updated;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool updatedSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        [System.ComponentModel.DefaultValueAttribute(ResourceStatus.active)]
        public ResourceStatus status = ResourceStatus.active;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public enum ResourceStatus {
        
        /// <remarks/>
        active,
        
        /// <remarks/>
        inactive,
        
        /// <remarks/>
        deleted,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SkyService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(TabularSkyService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearch))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SimpleImageAccess))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(Registry))]
    public class Service : Resource {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("interface")]
        public Interface[] @interface;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(TabularSkyService))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearch))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SimpleImageAccess))]
    public class SkyService : Service {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("facility")]
        public ResourceName[] facility;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("instrument")]
        public ResourceName[] instrument;
        
        /// <remarks/>
        public Coverage coverage;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(ConeSearch))]
    [System.Xml.Serialization.XmlIncludeAttribute(typeof(SimpleImageAccess))]
    public class TabularSkyService : SkyService {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("table")]
        public Table[] table;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Table {
        
        /// <remarks/>
        public string name;
        
        /// <remarks/>
        public string description;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("column")]
        public Param[] column;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        public string role;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/ConeSearch/v0.3")]
    public class ConeSearch : TabularSkyService {
        
        /// <remarks/>
        public ConeSearchCapability capability;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SIA/v0.7")]
    public class SimpleImageAccess : TabularSkyService {
        
        /// <remarks/>
        public SIACapability capability;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VORegistry/v0.3")]
    public class Registry : Service {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("managedAuthority")]
        public string[] managedAuthority;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VOResource/v0.10")]
    public class Organisation : Resource {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("facility")]
        public ResourceName[] facility;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("instrument")]
        public ResourceName[] instrument;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class DataCollection : Resource {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("facility")]
        public ResourceName[] facility;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("instrument")]
        public ResourceName[] instrument;
        
        /// <remarks/>
        public Coverage coverage;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("format")]
        public Format[] format;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("rights")]
        public Rights[] rights;
        
        /// <remarks/>
        public AccessURL accessURL;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public class Format {
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute(Form=System.Xml.Schema.XmlSchemaForm.Unqualified)]
        [System.ComponentModel.DefaultValueAttribute(false)]
        public bool isMIMEType = false;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value;
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VODataService/v0.5")]
    public enum Rights {
        
        /// <remarks/>
        @public,
        
        /// <remarks/>
        secure,
        
        /// <remarks/>
        proprietary,
    }
    
    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/VORegistry/v0.3")]
    public class Authority : Resource {
        
        /// <remarks/>
        public ResourceName managingOrg;
    }
}