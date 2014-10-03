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
package edu.jhu.pha.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.log4j.Logger;


/**
 * Initialises the settings object in application context from the settingsFileName parameter of the servlet
 * 
 */
public class LoadProperties extends HttpServlet {


	private Logger logger = Logger.getLogger(LoadProperties.class);
        public static Properties propMain = new Properties();

	@Override
	public void init() throws ServletException {
            ConfigurationFactory factory = new ConfigurationFactory(getServletConfig().getInitParameter("settingsFileName"));
            try{
                   Configuration config = factory.getConfiguration();                   
                   getServletContext().setAttribute("configuration", config);                    
                   InputStreamReader is = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("/tapwebservice.properties"));
                   propMain.load(is);                            
                   is.close();
                   logger.debug("Configuration is loaded.");
                    
            }catch(IOException e){
                    logger.error("Error loading properties inloadproperties",e);
                    throw new ServletException(e);
            }catch (ConfigurationException e){
                    logger.error("Error loading configuration",e);
                    throw new ServletException(e);
            }            
	}
}
