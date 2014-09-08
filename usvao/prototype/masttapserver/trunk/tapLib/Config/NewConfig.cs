using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace tapLib.Config {
    
    /// <summary>
    ///  This class is the one that is deserialized to form the configuration
    /// </summary>
    public class Column {
        [XmlAttribute] public String name;
        [XmlAttribute] public String internalName;
        [XmlAttribute] public bool std;
        [XmlElement("Description")] public String description;
        [XmlElement("Unit", IsNullable=true)] public String unit;
        [XmlElement("Utype", IsNullable = true)] public String utype;
        [XmlElement("Ucd", IsNullable = true)] public String ucd;
        [XmlElement("Datatype", IsNullable = true)] public String datatype;
        [XmlElement("Arraysize", IsNullable = true)] public String arraysize;
        [XmlElement("Primary")] public bool primary;
        [XmlElement("Indexed")] public bool indexed;
    }

    public enum TableType {
        [XmlEnum("table")]
        TABLE,
        [XmlEnum("view")]
        VIEW,
        [XmlEnum("output")]
        OUTPUT
    }

    public class Table {
        [XmlAttribute] public String name;
        [XmlAttribute] public String internalName;
        [XmlAttribute("type")] public TableType tableType;
        [XmlElement("Description")]public String description;
        [XmlElement("Utype", IsNullable = true)] public String utype;
        [XmlArray("Columns")] public List<Column> columns;
    }

    public class Connection{
        [XmlAttribute] public String value;
    }

    public class Database {
        [XmlAttribute] public String schemaName;
        [XmlAttribute] public String database;
        [XmlAttribute] public String description;
        [XmlAttribute] public String utype;
        [XmlElement("Connection")] public Connection connection;
        [XmlArray("Tables")] public List<Table> tables = new List<Table>();
    }
    [XmlRoot("NewConfig")]
    public class NewConfig {
        [XmlArray("Databases")]public List<Database> databases = new List<Database>();
    }

}
