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
package edu.jhu.pha.resources.vosi;

import edu.jhu.pha.exceptions.TapException;
import edu.jhu.pha.helpers.ResourceHelper;
import edu.jhu.pha.helpers.TablesDataCollector;
import edu.jhu.pha.writers.TablesWriter;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
//import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * Returs the tables supported by service.
 * @author deoyani nandrekar-heinis
 */

@Path("tables")
public class TablesResource {
//    @Context private UriInfo context;
//    private static Logger logger = Logger.getLogger(TablesResource.class); 

    /** Creates a new instance of TablesResource */
    public TablesResource() {
    }

    /**
     * Retrieves representation of an instance of edu.jhu.pha.resources.vosi.TablesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        try{
            TablesDataCollector tc = new TablesDataCollector();
            TablesWriter twriter = new TablesWriter(tc.getTapSchemaDescription());
            Document doc = twriter.getDocument();
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            StringWriter sw = new StringWriter();
            out.output(doc,sw);
            return sw.toString();
        }catch(Exception exp){
            
            throw new TapException(ResourceHelper.getVotableError("Exception in tableresources:"+exp.getMessage()));
            
            
        }
    }

    /**
     * PUT method for updating or creating an instance of TablesResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
}
