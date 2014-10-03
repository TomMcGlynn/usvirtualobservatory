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

import edu.jhu.pha.helpers.ResourceHelper;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.apache.log4j.Logger;

/**
 * Returns VOSI capabilities of service
 * @author deoyani nandrekar-heinis
 */

@Path("capabilities")
public class CapabilitiesResource  {
    //@Context    private UriInfo context;
    private static Logger logger = Logger.getLogger(CapabilitiesResource.class); 
    private @Context ServletContext context;
    private @Context HttpServletRequest req;
    /** Creates a new instance of CapabilitiesResource */
    public CapabilitiesResource() {
    }

    /**
     * Retrieves representation of an instance of edu.jhu.pha.resources.vosi.CapabilitiesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    //@Produces("application/octet-stream")
    public String getXml() {   
        ResourceHelper rHelper = new ResourceHelper();        
        String t = req.getRequestURL().toString();
        //System.out.println("Last Index :"+t.lastIndexOf("/"));
        //System.out.println("::"+t.substring(0, t.lastIndexOf("/")+1));
        String path = t.substring(0, t.lastIndexOf("/")+1);
        return rHelper.getVOSIXML(path, "capabilities", false) ;      
    }
    
 
}

