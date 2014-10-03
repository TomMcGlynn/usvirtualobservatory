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
package org.usvao.descriptors;

/**
 * This class is for keeping all query related information, 
 * easy to transfer between classes
 * @author deoyani nandrekar-heinis
 */
public class QueryDescription  {

    private String request;
    private String query;
    private String lang;
    private String version;
    private String format;
    private int    maxrec = 0;
    private int    serviceMax =0;
    private String jobid="";
    private String destruction;    
    private String csvseparator = ",";    
    private String adqlQuery;    
    private String resultsDir ="";
    private int    duration =0;
    private String uploadparam ="";    
    
    public QueryDescription(){
        
    }

//    public QueryDescription(QueryDescription qDesc){
//        this.request = qDesc.getRequest();
//        this.lang    = qDesc.getLang();
//        this.maxrec  = qDesc.getMaxrec();
//        this.query   = qDesc.getQuery();
//        this.version = qDesc.getVersion();
//        this.format  = qDesc.getFormat();
//        this.destruction = qDesc.getDestruction();
//    }

    public QueryDescription(String request,String query,String lang,
                            String version,String format, int maxrec, 
                            String jobid,String destruction,String adql,
                            int serviceMax){
        this.request = request;
        this.lang    = lang;      
        this.query   = query;
        this.version = version;
        this.format  = format;
        this.maxrec = maxrec;
        this.jobid = jobid;
        this.destruction = destruction;
        this.adqlQuery = adql;
        this.serviceMax = serviceMax;
    }

    public String getRequest(){
        return request;
    }
    public String getQuery(){
        return query;
    }
    public String getLang(){
        return lang;
    }
    public String getVersion(){
        return version;
    }
    public String getFormat(){
        return format;
    }
    public int getMaxrec(){
        return maxrec;
    }        
    public String getJobId(){
        return this.jobid;
    }
    public String getDestruction(){
        return this.destruction;
    }
    public String getSeparator(){
        return this.csvseparator;
    }
    public String getAdqlQuery(){
        return this.adqlQuery;
    }    
    public String getResultsDir(){
        return this.resultsDir;
    }
    public int getDuration(){
        return this.duration;
    }
    public int getServiceMax(){
        return this.serviceMax;
    }
    public String getUploadparam(){
        return this.uploadparam;
    }
    
    public void setJobId(String jobid){
        this.jobid = jobid;
    }
    public void setRequest(String request){
        this.request = request;
    }
    public void setLang(String value){
        this.lang = value;
    }
    public void setQuery(String value){
        this.query = value;
    }
    public void setVersion(String value){
        this.version = value;
    }
    public void setFormat(String value){
        this.format = value;
    }
    
    public void setMaxrec(int value){
        this.maxrec = value;
    }
    public void setDestruction(String value){
        this.destruction = value;
    }
    public void setSeparator(String sep){
        this.csvseparator = sep;
    } 
    public void setADQLquery(String value){
        this.adqlQuery = value;
    }
    public boolean isAllNull(){
         return (request == null && lang == null && query == null && version == null && format == null );             
    }
    public void setResultsDir(String val){
        this.resultsDir = val;
    }
    public void setDuration(int val){
        this.duration = val;
    }
    public void setServiceMax(int val){
        this.serviceMax = val;
    }
    public void setUploadparam(String val){
        this.uploadparam = val;
    }
}
