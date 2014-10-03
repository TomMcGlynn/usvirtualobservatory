package org.usvao.descriptors.uwsdesc;

/**
 * Descriptors for the upload table option
 * @author deoyani nandrekar-heinis
 */
public class UploadDescriptors {
    
    private String uploadtable;
    private String uploadstatus;
    
    
    
   public UploadDescriptors(){
        
   }
   
   public UploadDescriptors(String uplaodtable, String uploadstatus){
        this.uploadtable = uplaodtable;
        this.uploadstatus = uploadstatus;
   }
    
   public String getUploadTable(){
       return uploadtable;
   }
  
   public String getUploadStatus(){
       return uploadstatus;
   }
   
   public void setUploadTable(String value){
       uploadtable = value;
   }
  
   public void setUploadStatus(String value){
       uploadstatus = value;
   }  
   
}
