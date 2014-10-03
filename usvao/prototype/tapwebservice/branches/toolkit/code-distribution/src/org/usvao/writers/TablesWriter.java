/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.usvao.writers;

/**
 * generates xml for /tables resource (TAP_Schema and VOSI)
 * @author deoyani nandrekar-heinis
 */

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;


import org.usvao.descriptors.tapschema.TapSchemaDescription;
import org.usvao.descriptors.tapschema.SchemaDescription;
import org.usvao.descriptors.tapschema.TableDescription;
import org.usvao.descriptors.tapschema.ColumnDescription;
import org.usvao.servlets.LoadProperties;

public class TablesWriter {

    private static Logger log = Logger.getLogger(TablesWriter.class);

    private static final String DEFAULT_SCHEMA = "default";

    private TapSchemaDescription tapSchema;

//    String vosi_uri = "xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\" ";
//    String xml_uri = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
//    String vod_uri = "xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\"";
//    String vod_schema = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VODataService/VODataService-v1.1.xsd\"";

//    private Namespace vosi = Namespace.getNamespace("vs", vosi_uri);
//    private Namespace xsi = Namespace.getNamespace("xsi", xml_uri);
//    private Namespace vod = Namespace.getNamespace("vod", vod_uri);
//    private Namespace vod_schemalocation  = Namespace.getNamespace("schemaLocation", vod_schema);
//    
     

    public TablesWriter(TapSchemaDescription tapSchema)
    {
        this.tapSchema = tapSchema;
    }

    /**
     * @return the TapSchema as a document to be rendered as XML
     */
    public Document getDocument()
    {

        Element eleTableset = toXmlElement(tapSchema);
        //eleTableset.addNamespaceDeclaration(xsi);
        //eleTableset.addNamespaceDeclaration(vod);

        Document document = new Document();
        document.addContent(eleTableset);
        return document;
    }

    /**
     * @param tapSchema
     * @return
     */
    private Element toXmlElement(TapSchemaDescription tapSchema)
    {
        Namespace vosi = Namespace.getNamespace("vosi", LoadProperties.propMain.getProperty("uri.vosi"));    
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));   
        Namespace vod = Namespace.getNamespace("vod",  LoadProperties.propMain.getProperty("uri.vod"));
        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.vodloc")); 
        
        Element eleTableset = new Element("tableset", vosi.getPrefix(),vosi.getURI());
        eleTableset.addNamespaceDeclaration(vosi);
        eleTableset.addNamespaceDeclaration(vod);
        eleTableset.addNamespaceDeclaration(xsi);
        eleTableset.addNamespaceDeclaration(schemaLocation);
       
        if (tapSchema.getSchemaDescs().isEmpty()) throw new IllegalArgumentException("Exception: Make sure Schema is not Empty");
        for (SchemaDescription sd : tapSchema.getSchemaDescs())
        {
            eleTableset.addContent(toXmlElement(sd));
        }
        return eleTableset;
    }

    /**
     * @param sd
     * @return
     */
    private Element toXmlElement(SchemaDescription sd)
    {
        Element eleSchema = new Element("schema");
        Element ele;
        ele = new Element("name");
        if (sd.getSchemaName() == null)
            ele.setText(DEFAULT_SCHEMA);
        else
            ele.setText(sd.getSchemaName());
        eleSchema.addContent(ele);
        if (sd.getTableDescs() != null) for (TableDescription td : sd.getTableDescs())
        {
            eleSchema.addContent(toXmlElement(td));
        }
        return eleSchema;
    }

    /**
     * @param td
     * @return
     */
    private Element toXmlElement(TableDescription td)
    {
        Element eleTable = new Element("table");
        eleTable.setAttribute("type", "output");

        Element ele;
        ele = new Element("name");
        ele.setText(td.getTableName());
        eleTable.addContent(ele);

        if (td.getColumnDescs() != null) for (ColumnDescription cd : td.getColumnDescs())
        {
            eleTable.addContent(toXmlElement(cd));
        }

        return eleTable;
    }

    /**
     * @param cd
     * @return
     */
    private Element toXmlElement(ColumnDescription cd)
    {
        Namespace vod = Namespace.getNamespace("vod",  LoadProperties.propMain.getProperty("uri.vod"));
        Element eleColumn = new Element("column");

        addChild(eleColumn, "name", cd.getColumnName());
        addChild(eleColumn, "description", cd.getDescription());
        addChild(eleColumn, "unit", cd.getUnit());
        addChild(eleColumn, "ucd", cd.getUcd());
        addChild(eleColumn, "utype", cd.getUtype());

        Element eleDt = addChild(eleColumn, "dataType", cd.getDatatype());
        if (eleDt != null)
        {
            Attribute attType = new Attribute("type", vod.getPrefix() + ":TAP", vod);
            eleDt.setAttribute(attType);

            if (cd.getSize() != null && cd.getSize() > 0) eleDt.setAttribute("size", cd.getSize().toString());
        }

        return eleColumn;
    }

    private Element addChild(Element eleParent, String chdName, String chdText)
    {
        Element ele = null;
        if (chdText != null && !chdText.equals(""))
        {
            ele = new Element(chdName);
            ele.setText(chdText);
            eleParent.addContent(ele);
        }
        return ele;
    }

    public TapSchemaDescription getTapSchema()
    {
        return tapSchema;
    }

    public void setTapSchema(TapSchemaDescription tapSchema)
    {
        this.tapSchema = tapSchema;
    }
}
