
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
package edu.jhu.pha.helpers;

import edu.jhu.pha.descriptors.StaticMessages;
import edu.jhu.pha.descriptors.QueryDescription;
import edu.jhu.pha.descriptors.EnumDescriptors.ValidatorError;
import edu.jhu.pha.exceptions.BadRequestException;
import edu.jhu.pha.servlets.LoadProperties;
import org.apache.log4j.Logger;

/**
 * Validates the request to TAP resource.
 * @author deoyani nandrekar-heinis
 */
public class RequestValidator {
    
    public int invalidIn = 0;   
    public String errorMessage ="";
     private static Logger logger = Logger.getLogger(RequestValidator.class);

    public RequestValidator()
    {
        
    }
    
    /**
     * validating the query submitted by user
     * @param qdesc QueryDescription
     * 
     */
    //String queryLang = "|SQL|ADQL|ADQL-2.0|PQL|";
    public void validateRequest(QueryDescription qdesc){
        
       
        ValidatorError vError= ValidatorError.NO_PROBLEM;        
        if(qdesc.getRequest() == null || qdesc.getQuery() == null)
        {
            vError = ValidatorError.NULL_ERROR; 
        }
               
        else if(!qdesc.getRequest().equalsIgnoreCase("doQuery"))
        {
            if(!qdesc.getRequest().equalsIgnoreCase("getCapabilities"))
                 if(!qdesc.getRequest().equalsIgnoreCase("getAvailability"))
                    vError = ValidatorError.INVALID_REQUEST; 
        }
        else if(qdesc.getLang() == null){
                vError = ValidatorError.INVALID_LANG; 
        }       
        else if(!LoadProperties.propMain.getProperty("query.lang").contains("|"+qdesc.getLang().toUpperCase()+"|")){
                vError = ValidatorError.INVALID_LANG; 
        }
        else if( qdesc.getLang() != null && qdesc.getVersion() != null && !qdesc.getVersion().equals("") && !qdesc.getVersion().equals("1.0")){
            vError = ValidatorError.INVALID_VERSION; 
        }
        else if(qdesc.getFormat() != null){ 
               if( (!qdesc.getFormat().equals("") && !qdesc.getFormat().toUpperCase().equals(StaticMessages.outputVotable))
                       && (!qdesc.getFormat().equals("") && !qdesc.getFormat().toUpperCase().equals(StaticMessages.outputCSV))){
                   vError = ValidatorError.INVALID_FORMAT; 
               }            
        }
        
        if(vError != ValidatorError.NO_PROBLEM)
            throw new BadRequestException(ResourceHelper.getVotableError(getErrorMessage(vError)));        
        
        if((qdesc.getFormat() ==  null) || (qdesc.getFormat().equals("")))
              qdesc.setFormat(StaticMessages.outputVotable);
    }
  

    /**
     * Message to be returned
     * @param vError Error enum param
     * @return  String Error message
     */
    private String getErrorMessage(ValidatorError vError)
    {
        switch (vError) {

            case NO_REQUEST:       this.errorMessage ="There is no 'REQUEST' parameter value !!";
                                   break;
	    case INVALID_REQUEST:  this.errorMessage ="Please check/enter the 'REQUEST' parameter properly !!";
				   break;
	    case INVALID_LANG:     this.errorMessage ="Please enter the correct 'LANG' parameter !!";
				   break;
	    case INVALID_VERSION:  this.errorMessage ="Please enter the correct 'VERSION' number !!";
                                   break;
            case INVALID_FORMAT:   this.errorMessage ="Please enter the correct 'FORMAT' for output. (VOTABLE/CSV) !!";
                                   break;
            case INVALID_MAXREC:   this.errorMessage ="Please enter the correct 'FORMAT' for output. (VOTABLE/CSV) !!";
                                   break;
            case NO_QUERY:         this.errorMessage ="There is no query entered !!";
                                   break;
            case NULL_ERROR:       this.errorMessage ="Parameters 'REQUEST'/'QUERY' missing !! Please enter proper parameters!! ";
                                   break;
            case NO_PROBLEM: 	   this.errorMessage ="";
                                   break;                                   
	    default:	           this.errorMessage ="Default";
                                   break;
        }
        return this.errorMessage;
    }

}
