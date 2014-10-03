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
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * REST Web Service: availability resource
 * @author deoyani nandrekar-heinis
 */

@Path("availability")
public class AvailabilityResource {
    private @Context ServletContext context;
    private static Logger logger = Logger.getLogger(AvailabilityResource.class);

    /**
     * Retrieves representation of an instance of edu.jhu.pha.resources.vosi.AvailabilityResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        //System.out.println("!!!!!!!!!!!");
        boolean a  = this.getAvailabilitydatabase();
        ResourceHelper rHelper = new ResourceHelper();
        return rHelper.getVOSIXML("", "availability", a) ;
    }
    /**
     * Checks database availability
     * @return boolean
     */
    public boolean getAvailabilitydatabase(){
     java.sql.Connection dbConnection = null;      
     
      try{
            dbConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");            
            Configuration conf = (Configuration)context.getAttribute("configuration");            
            boolean jb = dbConnection.createStatement().execute(conf.getString("jobs.testquery"));            
            dbConnection.close();            
            dbConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");            
            boolean db= dbConnection.createStatement().execute(conf.getString("data.testquery"));            
            if(!jb || !db) return false;
            return true;
        }catch(SQLException sexp){
            System.out.println("Exception :"+sexp.getMessage());
            return false;
        }catch(Exception exp){
            System.out.println("Exception :"+exp.getMessage());
            return false;
        }finally{
          try{
              if(dbConnection != null)dbConnection.close();
          }catch(Exception e){
              
          }
        }        
    }
}

