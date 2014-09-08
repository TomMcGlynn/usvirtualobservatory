using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Data;
using VOTLib;

namespace VOTLib
{
    public class VoTableDocument : XmlDocument
    {
        static public string DEFAULT_DESCRIPTION = "VoTable created by the Vao Portal - vao.stsci.edu";

        private XmlElement eVoTable = null;
        private XmlElement eResource = null;
        private XmlElement eTable = null;
        private XmlElement eData = null;
        private XmlElement eTableData = null;

        // Create an Empty VoTable - Good for returning Errors
        public VoTableDocument(string description)
        {
            addVoTable(description);
        }
		
        // Create a VoTable with queried Data
        public VoTableDocument(DataSet ds, string voTableDescription, string voProtocol, string voProtocolVersion)
        {
            addVoTable(voTableDescription);
            addResource();
            addQueryStatus();
			//addMetaData();
            addServiceProtocol(voProtocol, voProtocolVersion);
            addTable(ds.Tables[0]);
        }
		
		public VoTableDocument(DataTable dt, string voTableDescription, string voProtocol, string voProtocolVersion)
        {
            addVoTable(voTableDescription);
            addResource();
            addQueryStatus();
			//addMetaData();
            addServiceProtocol(voProtocol, voProtocolVersion);
            addTable(dt);
        }

        private void addVoTable(string description)
        {
            if (eVoTable == null)
            {
                eVoTable = this.CreateElement("VOTABLE");
                base.AppendChild(eVoTable);
                addAttribute(eVoTable, "Version", "1.1");
                addDescription(description);
            }
        }

        private void addDescription(string description)
        {
            // <DESCRIPTION>VoTable Description</DESCRIPTION>
            addElement(eVoTable, "DESCRIPTION", description);
        }

        public void addResource()
        {
            if (eResource == null)
            {
                // <RESOURCE type="results">
                if (eVoTable == null) addVoTable("");
                eResource = addElement(eVoTable, "RESOURCE");
                addAttribute(eResource, "type", "results");
            }
        }

        public void addQueryStatus()
        {
            addQueryStatus("OK", null);
        }

        public void addQueryStatus(string valu, string data)
        {
            // <INFO name="QUERY_STATUS" value="OK"/>
            if (eResource == null) addResource();
            XmlElement e = addElement(eResource, "INFO", data);
            addAttribute(e, "name", "QUERY_STATUS");
            addAttribute(e, "value", valu);
        }

        public void addServiceProtocol( string protocol, string version)
        {
            // <INFO name="SERVICE_PROTOCOL" value="1.02">SSAP</INFO>
            if (eResource == null) addResource();
            XmlElement e = addElement(eResource, "INFO", protocol);
            addAttribute(e, "name", "SERVICE_PROTOCOL");
            addAttribute(e, "value", version);
        }

        public void addTable(DataTable dt)
        {
            if (eTable == null)
            {
                // <TABLE name="SSAP">
                if (eResource == null) addResource();
                eTable = addElement(eResource, "TABLE");
                addAttribute(eTable, "name", dt.TableName);

                // <DESCRIPTION>MAST SSAP Search: 14 row(s) returned!</DESCRIPTION>
                addElement(eTable, "DESCRIPTION", "VoTable with " + dt.Rows.Count + " rows");

                addTableFields(dt);
                addTableData(dt);
            }
        }

        private void addTableFields(DataTable dt)
        {
            foreach (DataColumn col in dt.Columns)
            {
                // Create new VO Table Field
                //
                // <FIELD name="version" datatype="char" ucd="meta.version;meta.file" utype="ssa:dataid.version" arraysize="*">
                //    <DESCRIPTION>Version of VO file
                //    </DESCRIPTION>
                //  </FIELD>
                XmlElement e = this.CreateElement("FIELD");
				
				//
				// Add the Required FIELD name 
				//
				addAttribute(e, "name", col.ColumnName);
				
				//
				// Add the Required FIELD datatype 
				//
				string datatype;
				if (VOTType.VotTypeMapping.TryGetValue(col.DataType, out datatype))
				{
					addAttribute(e, "datatype", datatype);
				}
				
				//
				// Add any remaining VoTable Properties for this column that are stored as ExtendedProperties vith prefix "vot."
				//
				if (col.ExtendedProperties != null)
				{
					PropertyCollection properties = col.ExtendedProperties;	
					foreach (string key in properties.Keys)
					{
						if (key.StartsWith("vot."))
						{
							string votparam = (key.Substring(4));
							string votvalue = properties[key] as string;
	
							if (votparam.ToUpper().Equals("DESCRIPTION"))
							{
								addElement(e, "DESCRIPTION", votvalue);
							}
							else
							{
								addAttribute(e, votparam, votvalue);
							}
						}
					}
				}

                eTable.AppendChild(e);
            }
        }

        private void addTableData(DataTable dt)
        {
            //<DATA>
            //   <TABLEDATA>
            //      <TR>
            //         <TD>150.523375</TD>
            //      </TR>
            //   </TABLEDATA>
            //</DATA>

            eData = this.addElement(eTable, "DATA");
            eTableData = this.addElement(eData, "TABLEDATA");
          
            foreach (DataRow row in dt.Rows)
            {
                XmlElement eTR = this.addElement(eTableData, "TR");
                foreach (DataColumn col in dt.Columns)
                { 
                    addElement(eTR, "TD", row[col].ToString());
                }
            }
        }
		
		//
		// Unused
		//
		private void addMetaData()
        {
            // Add PARAMs:
            if (eResource == null) addResource();

            XmlElement e = addElement(eResource, "PARAM");
            addAttribute(e, "name", "INPUT:POS");
            addAttribute(e, "value", ""); //paramsIn.Pos.ToString());
            addAttribute(e, "datatype", "char");
            addAttribute(e, "arraysize", "*");
            addElement(e, "DESCRIPTION", "Search Position in the form \"ra,dec\" where ra and dec are given in decimal degrees in the ICRS coordinate system. Note: A cone search is performed rather than a rectangular search.");

            e = addElement(eResource, "PARAM");
            addAttribute(e, "name", "INPUT:SIZE");
            addAttribute(e, "value", ""); //paramsIn.Size.ToString());
            addAttribute(e, "datatype", "double");
            addAttribute(e, "unit", "deg");
            addElement(e, "DESCRIPTION", "Search DIAMETER in decimal degrees."); 

            e = addElement(eResource, "PARAM");
            addAttribute(e, "name", "INPUT:BAND");
            addAttribute(e, "value", ""); //paramsIn.Band.ToString());
            addAttribute(e, "datatype", "char");
            addAttribute(e, "arraysize", "*");
            addElement(e, "DESCRIPTION", "A pair of slash-separated values specifying a range of wavelengths in meters (e.g., 1.3e-7/1.4e-7). Note: the optional \"source/observer\" parameters are currently ignored.");

            e = addElement(eResource, "PARAM");
            addAttribute(e, "name", "INPUT:TIME");
            addAttribute(e, "value", ""); //paramsIn.Time.ToString());
            addAttribute(e, "datatype", "char");
            addAttribute(e, "arraysize", "*");
            addElement(e, "DESCRIPTION", "A pair of slash-separated values specifying a range in time specified in ISO 8601 Format (e.g., 2003/2006-12-11).");

            e = addElement(eResource, "PARAM");
            addAttribute(e, "name", "INPUT:FORMAT");
            addAttribute(e, "value", ""); //paramsIn.Format.ToString());
            addAttribute(e, "datatype", "char");
            addAttribute(e, "arraysize", "*");
            addElement(e, "DESCRIPTION", "Requested format of spectra: \"ALL, FITS, METADATA\". Currently only FITS files are supported.");
        }

        //
        // Utility Routines
        //
        private XmlAttribute addAttribute(XmlElement eParent, string sName, string sValue)
        {
            XmlAttribute a = this.CreateAttribute(sName);
            a.Value = sValue;
            eParent.Attributes.Append(a);
            return a;
        }

        private XmlElement addElement(XmlElement eParent, string sName)
        {
            return addElement(eParent, sName, null);
        }

        private XmlElement addElement(XmlElement eParent, string sName, string sValue)
        {
            XmlElement e = this.CreateElement(sName);
            if (sValue != null)
            {
                XmlText t = this.CreateTextNode(sValue);
                e.AppendChild(t);
            }
            eParent.AppendChild(e);
            return e;
        }  
		
		
    }
}
