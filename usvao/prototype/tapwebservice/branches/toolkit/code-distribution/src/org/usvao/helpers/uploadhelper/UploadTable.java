
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
package org.usvao.helpers.uploadhelper;

import cds.savot.model.FieldSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.TDSet;
import cds.savot.model.TRSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import org.apache.log4j.Logger;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.helpers.AuthenticationHelper;
import org.usvao.servlets.LoadProperties;
import org.usvao.servlets.MetadataDictionary;

/**
 * This takes care of uploading data from URL to database.
 * @author deoyani nandrekar-heinis
 */
public class UploadTable implements Runnable{
    private String jobid = null;    
    private String tablename = null;
    private String username = "testuser";
    private Logger logger = Logger.getLogger(UploadTable.class);
    private InsertData insertData;
    //private UWSJobDescription jDesc ;
    private boolean uploadsuccess = false;   
    private String tableurl;
    private String uploadid;
    private String usertablename ;    
    private String regdatatype = "long|double|short|float|int";    
    
    
    @Override
    public void run() {        
        this.uploadData();
        synchronized(UploadTable.class) {
            UploadTable.class.notify();
        }
    }
    /**
     * Parameterized constructor
     * @param jobid
     * @param username
     * @param tablename
     * @param tableurl
     * @param uploadid 
     */
    public UploadTable(String jobid,String username, String tablename, String tableurl, String uploadid){
        this.jobid = jobid;
        this.tablename = tablename;
        this.usertablename = tablename;
        this.tableurl = tableurl;
        this.username = username;
        this.uploadid = uploadid;
        //this.jDesc = new UWSJobDescription();
        
    }
        
    /**
     * Upload data from given url
     */
    private void uploadData(){        
        insertData = new InsertData();
        this.loadTableFromURL();                              
    }
    
    /**
     * 
     * @throws MalformedURLException
     * @throws IOException 
     */
    private void loadTableFromURL(){
        if(tableurl.contains("vos:/")){
            pullFromVospace();
        }else{
            
            URL u = null;        
            try {
                u = new URL(tableurl);
            } catch (MalformedURLException ex) {
                logger.error("MalformedURLException in loadTableFromURL:"+ex.getMessage());
                this.updateErrorJobsTable(ex.getMessage());
            }
            if(tableurl.endsWith(".xml") || tableurl.endsWith(".vot")){
             if(u != null)this.readVotable(u, null);                
            }else{
                System.out.println("tableurl:"+tableurl+":jobid:"+jobid);
                if(this.jobid.contains(StaticDescriptors.vospace)){
                    System.out.println("Check here in upload from vospace");
                    this.readVotable(u, null);
                }else{
                System.out.println("Other format not implemented");
                this.uploadsuccess = false;
                 this.updateUserTable();
                }
                //code for reading text csv files
            }
        }
    }

    /**
     * 
     */
    private void pullFromVospace(){
        java.io.InputStream isd=null;
        try{
         
         
         AuthenticationHelper oauthhelper = new AuthenticationHelper();
         oauthhelper.getStoredValues(this.username);
         
         List<Map.Entry> params = new ArrayList<Map.Entry>();
         boolean add = params.add(new OAuth.Parameter("oauth_token",oauthhelper.getAccessToken()));
         System.out.println("Direc::"+LoadProperties.propMain.getProperty("vospace.dataurl"));
         OAuthAccessor accessor = this.getOAuthAccessor();            
         accessor.requestToken = oauthhelper.getAccessToken();
         accessor.tokenSecret = oauthhelper.getTokenSecret(); 
            
         OAuthClient client = new OAuthClient(new HttpClient4());
         
         String vospaceurl="";
         if(tableurl.contains("vos://edu.jhu!vospace"))
          vospaceurl = tableurl.replace("vos://edu.jhu!vospace", LoadProperties.propMain.getProperty("vospace.dataurl")) ;
         else if(tableurl.contains("vos://edu.jhu\\!vospace"))
          vospaceurl = tableurl.replace("vos://edu.jhu\\!vospace", LoadProperties.propMain.getProperty("vospace.dataurl")) ;
         System.out.println("vospaceurl:"+vospaceurl);
         vospaceurl += "?view=data";         
         OAuthMessage requestMessage = client.invoke(accessor,"GET",vospaceurl , params);
        //System.out.println("After client Invoke ::"+requestMessage.readBodyAsString());
         isd  = requestMessage.getBodyAsStream();         
         readVotable(null, isd);
         
        } catch (OAuthException ex) {
            logger.error("OAuthException while getting client:"+ex.getMessage());
            this.updateErrorJobsTable(ex.getMessage());
        } catch (URISyntaxException ex) {
            logger.error("URISyntaxException while getting data uri:"+ex.getMessage());
            this.updateErrorJobsTable(ex.getMessage());
        }catch(IOException ex){
            logger.error("IOException while getting properties:"+ex.getMessage());
            this.updateErrorJobsTable(ex.getMessage());
        }finally{
            try{isd.close();}catch(IOException ie){}
        }
    }
    
    /**
     * 
     * @return 
     */
    private OAuthAccessor getOAuthAccessor(){

        String consumerKey = LoadProperties.propMain.getProperty("oauth.consumerKey");
        String consumerSecret = LoadProperties.propMain.getProperty("oauth.consumerSecret");
        
        String callbackUrl = LoadProperties.propMain.getProperty("oauth.callbackUrl");
        String reqUrl      = LoadProperties.propMain.getProperty("oauth.requestUrl");
        String authzUrl    = LoadProperties.propMain.getProperty("oauth.authorizationUrl");
        String accessUrl   = LoadProperties.propMain.getProperty("oauth.accessUrl");

        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        consumer.setProperty("consumer_name", "tapservice");               
        return new OAuthAccessor(consumer);
    }
    
   /**
     * Reads votable using SAVOt parser. Calls cretae table once metadata is read.
     * Inserts data row by row in to database tables.
     * @param url URL of VOtable
     * @throws SQLException 
     */
    private void readVotable(URL url, InputStream isd){
       SavotPullParser sb = null;
       if(url != null)
         sb = new SavotPullParser(url, SavotPullEngine.SEQUENTIAL,"UTF-8");
       else if (isd != null)
          sb = new SavotPullParser(isd, SavotPullEngine.SEQUENTIAL,"UTF-8");        
       this.savotPull(sb);
   }
    
    /**
     * 
     * @param sb
     * @throws SQLException 
     */
  private void savotPull(SavotPullParser sb){   
    
        TRSet tr = null;
        FieldSet fdSet = null;    
        SavotResource currentResource = null;
        // get the next resource of the VOTable file
        if(sb != null)
            currentResource = sb.getNextResource();
        // while a resource is available
        while (currentResource != null) {
            System.out.println("-----------------------------------------------------");   
        // for each table of this resource
        for (int i = 0; i < currentResource.getTableCount(); i++) {
            fdSet = currentResource.getFieldSet(i);
            if(fdSet != null){
            
            for (int j = 0; j < fdSet.getItemCount(); j++) {
            
                SavotField fd = (SavotField) fdSet.getItemAt(j);
                String deoyanitest = this.dbDataType(fd.getDataType().toLowerCase(),fd.getArraySize().toLowerCase(), null);
                System.out.println("deoyanitest:"+deoyanitest);
                insertData.getColumnNames().add(fd.getName());  
                insertData.getColumnDatatypes().add(deoyanitest);
            }
        }
        
        boolean issuccess = this.createTableIndatabase(creteValues());
        if(issuccess){
            
           try{ 
           insertData.createConnection();
           tr = currentResource.getTRSet(i);
              
           if (tr != null) {
              // for each row of the table
              for (int j = 0; j < tr.getItemCount(); j++) {
                  
                insertData.createStatement(this.tablename);
                // get all the data of the row
                TDSet theTDs = tr.getTDSet(j);
                String currentLine = new String();
                // for each data of the row            
                for (int k = 0; k < theTDs.getItemCount(); k++) {
                    currentLine = currentLine + theTDs.getContent(k);              
                    insertData.setValues(k+1, theTDs.getContent(k));                           
                }
              insertData.executeStatement();            
            }
          }        
          insertData.closeConnection();
          this.uploadsuccess = true;
          }catch(SQLException ex){
                this.uploadsuccess = false;
                logger.error("SQLException in savotPull: "+ex.getMessage());
                this.updateErrorJobsTable(ex.getMessage());
          }catch(Exception ex){
                this.uploadsuccess = false;
                logger.error("Exception in savotPull: "+ex.getMessage());
                this.updateErrorJobsTable(ex.getMessage());
          }    
           
       }else{
            this.uploadsuccess = false;
       }         
        
        this.updateUserTable();
        
    }
    // get the next resource
    currentResource = sb.getNextResource();
   }
    
//   }catch(SQLException ex){
//        this.uploadsuccess = false;
//        logger.error("SQLException in savotPull: "+ex.getMessage());
//        this.updateErrorJobsTable(ex.getMessage());
//   }catch(Exception ex){
//        this.uploadsuccess = false;
//        logger.error("Exception in savotPull: "+ex.getMessage());
//        this.updateErrorJobsTable(ex.getMessage());
//   }    
 }
  
    /**
     * 
     * @param voDatatype
     * @param voArraysize
     * @param voXtype
     * @return 
     */
    private String dbDataType(String voDatatype , String voArraysize, String voXtype){
        
        String datatypeStore = null;  
        String trykey = null;
        //System.out.println("vodatatype:"+voDatatype+"::voArraySize"+voArraysize+":: check is empty:"+voArraysize.isEmpty());
        if(voDatatype != null){

            if(regdatatype.contains(voDatatype))
                trykey = voDatatype;
            else{
                if(voArraysize != null){
                     if(!voArraysize.isEmpty()){
                        System.out.println("check here:"+voDatatype);
                        if(voArraysize.contains("*")) trykey= voDatatype+"|n*";                      
                        else if (!voArraysize.equals("")) trykey = voDatatype+"|n";                      
                     }else
                         trykey = voDatatype+"|-";
                     
                }else 
                trykey = voDatatype+"|-";
            } 
           
           System.out.println("trykey ="+trykey);
           datatypeStore = (String)MetadataDictionary.datatypeDictionary.get(trykey);
           
           //System.out.print("insertData.getDatatypeNumbers() ="+MetadataDictionary.datatypeJavaSQL.get(trykey));           
           insertData.getDatatypeNumbers().add(MetadataDictionary.datatypeJavaSQL.get(trykey));
           if(datatypeStore != null){           
               if(datatypeStore.contains("(n)")){
                   if(voArraysize != null){
                       if(!voArraysize.contains("*") && !voArraysize.equals("") && !voArraysize.isEmpty())
                        datatypeStore = datatypeStore.replace("(n)", "("+voArraysize+")");
                       else
                         datatypeStore = datatypeStore.replace("(n)", "(MAX)");
                   }                  
               }    
           }else
               datatypeStore = voDatatype+"(MAX)";
       }        
        
       return datatypeStore;      
    }
    //private String columnWidth ="binary|char|datetime2|decimal|nchar|numeric|nvarchar|time|varbinary|varchar";
    /**
     * Using metadata create datatypes and column names for creation of tables in database
     * @return String format to complete create table syntax
     */
    private String creteValues(){
        String createtableValues ="";
        for(int i=0; i< insertData.getColumnDatatypes().size() ; i++ ){
            createtableValues += insertData.getColumnNames().get(i)+" "+insertData.getColumnDatatypes().get(i) ;            
            if(i != insertData.getColumnDatatypes().size()-1 ) createtableValues += ",";
        } 
        return createtableValues;
    }
     
    
    /**
     * Create table in the database according to metadata available
     * @return boolean on completion
     */
    private boolean createTableIndatabase(String createValues){
        //tablename = "TAP_UPLOAD."+username+"_"+this.uploadid;
        this.tablename = StaticDescriptors.uploadSchema+"." + this.usertablename;
        java.sql.Connection conn = null;
        java.sql.Statement stmt  = null;
        boolean createsuccess = false;
        try{
         conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolUpload");
         stmt = conn.createStatement();
         String createString = "create table "+tablename+"("+createValues+")";
         System.out.println("CreateString:"+createString);
         stmt.execute(createString);        
         createsuccess = true;
        }catch(SQLException sexp){            
            logger.error("SQL Excpetion creating uploaded table :"+sexp.getMessage());            
            createsuccess = false;
            throw new SQLException(sexp.getMessage());
        }catch(Exception exp){
            logger.error("Excpetion creating uploaded table :"+exp.getMessage());
            createsuccess = false;
            throw new Exception(exp.getMessage());
        }finally{
            try{conn.close();}catch(Exception e){}
            try{stmt.close();}catch(Exception e){}
            return createsuccess;
        }        
    }    
    

    
    /**
     * This function updates tapuserdata
     * 
     */
    private void updateUserTable(){

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt  = null;
        try{
            
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            String updateString = " update "+StaticDescriptors.tapSchema+"."+"tapusersdata"
                    + "  set uploadsuccess=?,tabledbname=? where tableuploadid = ? ";
            pstmt = conn.prepareStatement(updateString);        
            if(this.uploadsuccess)
                pstmt.setString(1, "COMPLETED");
            else
                pstmt.setString(1, "ABORTED");
            pstmt.setString(2, this.tablename);
            pstmt.setString(3, this.uploadid);           
            pstmt.executeUpdate();
        }catch(SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            //throw new TapException("Exception in updateUserTable:"+sexp.getMessage());
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            //throw new TapException("Exception in updateUsertable:"+exp.getMessage());
        }finally{
            try{conn.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }        
    }
    
    /**
     * 
     * @param error 
     */
    private void updateErrorJobsTable(String error){

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt  = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        String errorString ="";
        try{            
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            
            String selectString = " select error from "+StaticDescriptors.tapSchema+"."+"tapjobstable where jobid='"+jobid+"'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectString);
            while(rs.next()){
                if(rs.getString("error")!= null ){
                    errorString = rs.getString("error");
                }
            }
            
            errorString += error;
            String updateString = " update  "+StaticDescriptors.tapSchema+"."+"tapjobstable set error=? where jobid = ?";
            pstmt = conn.prepareStatement(updateString);                    
            pstmt.setString(1, errorString);            
            pstmt.setString(2, this.jobid);
            pstmt.executeUpdate();
            
        }catch(SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            //throw new TapException("Exception in updateUserTable:"+sexp.getMessage());
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            //throw new TapException("Exception in updateUsertable:"+exp.getMessage());
        }finally{
            try{conn.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }        
    }
    
    /**
     * 
     * @param pattern
     * @param value
     * @return 
     */
    private static boolean match(Pattern pattern, String value) {
	return (pattern != null && value != null && pattern.matcher(value).matches());
    }
    /**
     * 
     * @param p
     * @return 
     */
    private static Pattern pattern(String p) {
	if (p == null) {  return null;	}
	return Pattern.compile(p);
    }
    
//    public UploadTable(){
//        jDesc = new UWSJobDescription();
//    }
//    
//    public UploadTable(String userid, boolean async, boolean tableaccess){
//        this.async = async;
//        username = userid;
//    }
//    
//    public UploadTable(String userid){
//        username   = userid;            
//        jDesc = new UWSJobDescription();
//    }
    
//    public UploadTable(String upload, String jobid, boolean fromasync){
//        this.jDesc = new UWSJobDescription();
//        this.jobid = jobid;
//        this.upload = upload;        
//    }
//    //upload data from async thread    
//    public void uploadData(){        
//            
//     System.out.println("here");            
//     try{
//     String[] uploadUser ;
//     if(upload.contains("#")){          
//         //System.out.println("contains #:"+upload.split("#").length);            
//        uploadUser = upload.split("#");
//        //System.out.println("length:"+uploadUser.length);
//        //System.out.println("UploadUser:"+uploadUser[0]);
//     }else{
//          uploadUser = new String[1];
//          uploadUser[0]=upload; 
//     }
//             //System.out.println("Uploaduser:"+uploadUser[0]);   
//             
//                for(int i=0 ;i<uploadUser.length;i++ ){
//                    
//                   if(uploadUser[i].contains("@username=")){
//                       
//                     String[] uploadparams = new String[2];
//                     uploadparams = uploadUser[i].split("@username=");
//                     this.username = uploadparams[1];
//                     //System.out.println("Uploaduser name:"+username);   
//                     //System.out.println("Uploaduser name:"+uploadparams[0]);   
//                     
//                     String[] touploadTables = new String[uploadparams[0].split(";").length];
//                     touploadTables = uploadparams[0].split(";");     
//                     for(int j=0 ;j<touploadTables.length;j++ ){
//                                              
//                       String[] uploadValues = new String[2];
//                       uploadValues = touploadTables[j].split(",");
//                       tablename = uploadValues[0];       
//                       //System.out.println("Uploaduser tablename:"+tablename);   
//                       String tablelink = uploadValues[1];
//                       //System.out.println("Uploaduser tablelink:"+tablelink);                         
//                       insertData = new InsertData(); 
//                       if(tablelink.contains("://"))    
//                       this.loadTableFromURL(tablelink);  
//                       
//                       //upload table inline
//                       //inline table here                       
//                       //On successful table creation update information is user tables
//                       if(this.uploadsuccess)
//                       this.updateUserTable();
//                     }
//                     
//                     
//                   }else{
//                       System.out.println("Error here: no username");
//                       //error no authenticated user found
//                   }
//                    
//                }     
//     }catch(MalformedURLException mexp){
//         
//     }catch(IOException ie){
//         
//     }catch(Exception ex){
//         
//     }
//    }
    
    
    
     /**
     * Get URL of the table to be uploaded
     * @param tableUrl URL of the table
     * @throws MalformedURLException 
     * @throws IOException 
     */
//    private void loadTableFromURL(String tableUrl) throws MalformedURLException, IOException{
//        try{
//        URL u = new URL(tableUrl);
//        
//        if(tableUrl.endsWith(".xml") || tableUrl.endsWith(".vot"))     {
//        System.out.println("Upload table");    
//        this.readVotable(u);
//        }
//        else{
//            //code for reading text csv files
//        }
//        }catch(Exception exp){
//            System.out.println("exception :"+exp.getMessage());
//        }
//            
// //       URLConnection uc = u.openConnection();
// //       String contentType = uc.getContentType();
// //       int contentLength = uc.getContentLength();
//
// //       InputStream raw = uc.getInputStream();
// //       InputStream in = new BufferedInputStream(raw);
//        
////        java.io.BufferedReader r  = new java.io.BufferedReader(new InputStreamReader(in));
//        
////        java.io.Reader r = new InputStreamReader(in);
////        int c;
////        
////        while ((c = r.read()) != -1) {
////                System.out.print((char) c);
////        }
//        /**
//         * Here is working URL data
//         */
////        byte[] data = new byte[contentLength];
////        int bytesRead = 0;
////        int offset = 0;
////        
////        while (offset < contentLength) {
////            bytesRead = in.read(data, offset, data.length - offset);
////            if (bytesRead == -1)    break;
////            offset += bytesRead;            
////        }
////        in.close();
////        if (offset != contentLength) {
////            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
////        }
////
////        String filename = tableName;//u.getFile().substring(tableUrl.lastIndexOf('/') + 1);
////        FileOutputStream out = new FileOutputStream(new File("H:\\testdir\\testupload\\"+filename));
////        out.write(data);
////        out.flush();
////        out.close();
//    }
    
   
 
    
//    /**
//     * Create table in the database according to metadata available
//     * @return boolean on completion
//     */
//    private boolean createTableIndatabase(String createValues){
//        tablename = "TAP_UPLOAD."+username+"_"+this.tablename;
//        int count = 1;
//        String temptablename = tablename;
//        while(true){
//          boolean temp = checkTableNameAvailable(tablename);
//          if(temp) break;          
//          else tablename = temptablename+"_"+count;
//          count++;
//        }
//        java.sql.Connection conn = null;
//        java.sql.Statement stmt  = null;
//        try{
//         conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolUpload");
//         stmt = conn.createStatement();
//         String createString = "create table "+tablename+"("+createValues+")";
//         System.out.println("CreateString:"+createString);
//         stmt.execute(createString);        
//         return true;
//        }catch(SQLException sexp){            
//            logger.error("SQL Excpetion creating uploaded table :"+sexp.getMessage());
//            return false;
//        }catch(Exception exp){
//            logger.error("Excpetion creating uploaded table :"+exp.getMessage());
//            return false;
//        }finally{
//            try{conn.close();}catch(Exception e){}
//            try{stmt.close();}catch(Exception e){}
//        }        
//    }    
    
    
  

    /**
     * Check whether uploaded table name available to create new table
     * @param tablename name specified
     * @return boolean
     */
//    private boolean checkTableNameAvailable(String tablename) {
//        
//        java.sql.Connection conn = null;
//        java.sql.Statement stmt  = null;
//        try{
//         conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
//         stmt = conn.createStatement();
//         String sString = "select * from tapusersdata where tablename ='"+ tablename+"'";
//         //System.out.println("sString:"+sString);
//         ResultSet rs = stmt.executeQuery(sString);        
//         while(rs.next()){ 
//             //System.out.println("sString:");
//             return false;             
//         }
//         return true;
//        }catch(SQLException sexp){
//            throw new TapException("Exception:"+sexp.getMessage());
//        }catch(Exception exp){
//            throw new TapException("Exception:"+exp.getMessage());
//        }finally{
//            try{conn.close();}catch(Exception e){}
//            try{stmt.close();}catch(Exception e){}
//        }        
//    }   
//    
    
    
    
    
    
//    /***
//     * loading table from file on machine
//     * @param input 
//     */
//     public void loadTableFromdesk(InputStream input){
//        try{
//            System.out.println("Checking file:"+input.available());
//            //File f=new File("H:\\testdir\\outFile");            
//            OutputStream out=new FileOutputStream("H:\\testdir\\outFile");  
//            byte buf[]=new byte[1024];
//            int len;
//            while((len=input.read(buf))>0){
//                  out.write(buf,0,len);
//                  
//            }      
//            out.close();
//            input.close();
//            System.out.println("\nFile is created....................");
//            
//        }catch (IOException e){        
//        }
//    }
     
     
     
     
     
      //** Following functions are for uploading data directly
    
//    public void uploadData(String upload, String jobid)
//            throws MalformedURLException, IOException{
//        
//        this.jobid = jobid;
//        
//        if(isPending()){       
//            
//            if(jDesc.getUploadedtables() != null)
//            uploadedTables = jDesc.getUploadedtables();
//            //System.out.println("here is what it is");            
//            if(upload.contains("param") && upload.contains("PARAM")){
//                System.out.println("Param:");
//            }else{
//                   String[] touploadTables = new String[upload.split(";").length];
//                   touploadTables = upload.split(";");     
//                   for(int i=0 ;i<touploadTables.length;i++ ){
//                                              
//                       String[] uploadValues = new String[2];
//                       uploadValues = touploadTables[i].split(",");
//                       tablename = uploadValues[0];       
//                       String tablelink = uploadValues[1];
//                       //if("://".matches(tablelink))
//                       if(tablelink.contains("://"))    
//                       this.loadTableFromURL(tablelink);  
//                       //else upload table inline
//                       //inline table here
//                       
//                       //On successful table creation update information is user tables
//                       if(this.uploadsuccess)
//                       this.updateUserTable();
//                   }
//            }        
//        }else{
//            throw new TapException("Job is not in PENDING state, you can not upload data");
//        }        
//    }
    
//    public boolean isPending(){
//        
//        UWSResources uws = new UWSResources();        
//        jDesc = uws.getJobData(uwsJobElements.uploadedtables, jobid);    
//         System.out.println(":"+jDesc.getPhase()+":"+jDesc.getUploaadParams());
//        if(jDesc.getPhase().equalsIgnoreCase("PENDING"))
//            return true;        
//        return false;
//    }
     
    
//   /**
//    * To get proper syntax for uploading data
//    * @param columnData
//    * @param columnType
//    * @return 
//    */     
//    private String getDbInputData(String columnData, String columnType){
//    
//        if(!columnType.equalsIgnoreCase("float") && !columnType.equalsIgnoreCase("int")&&
//           !columnType.equalsIgnoreCase("real") && !columnType.equalsIgnoreCase("bigint") &&
//           !columnType.equalsIgnoreCase("tinyint") && !columnType.equalsIgnoreCase("smalltint")){
//                return "'"+columnData+"'";
//        }else
//                return columnData;
//    }
    
   // public static enum db {username,tablename}

 
  
    
//    private boolean insertIntoDatabase(String columnData, int count){
//        try{
//            String insertQ = "insert into "+tablename+"("+columnData +"), values(";
//            for(int i=0;i<count;i++){
//                insertQ += "?";
//                if(i<count-1)
//                insertQ +=",";    
//            }
//            insertQ += ")";
//            pstmtupload = connupload.prepareStatement(insertQ);
//            return true;
//        }catch(Exception exp){
//            return false;
//        }
//    }

        
//    private void pullFromVospace(){
//        try{
//         InputStreamReader is = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("/tapwebservice.properties"));
//         Properties propMain = new Properties();
//         propMain.load(is);
//         
//          System.out.println("Direc::"+propMain.getProperty("vospace.dataurl"));
//         //if(match(pattern("edu.jhu!vospace"),tableurl)){        
//         //String vospaceurl = tableurl.replace("vos://edu.pha!vospace", propMain.getProperty("vospace.dataurl")) ;
//            pulldataVospace(propMain);
//         //}else
//         //   throw new TapException("Could not resolve vospace URL");
//        }catch(IOException ex){
//                System.out.println("IOException while getting properties");
//                throw new TapException("Could not resolve vospace URL");
//        }
//        
//    }
    
    
    //         
//                     if (isd != null) {
//            java.io.StringWriter writer = new java.io.StringWriter();
// 
//            char[] buffer = new char[1024];
//            try {
//                java.io.Reader reader = new java.io.BufferedReader(
//                        new java.io.InputStreamReader(isd, "UTF-8"));
//                int n;
//                while ((n = reader.read(buffer)) != -1) {
//                    writer.write(buffer, 0, n);
//                }
//            } finally {
//                isd.close();
//            }
//            
//            System.out.println("data** ::"+writer.toString());
//            }
}
