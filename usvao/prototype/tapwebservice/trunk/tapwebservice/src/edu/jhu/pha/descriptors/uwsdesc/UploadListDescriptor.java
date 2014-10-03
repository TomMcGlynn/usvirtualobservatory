package edu.jhu.pha.descriptors.uwsdesc;

import java.util.List;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class UploadListDescriptor {
    
    private List<UploadDescriptors> uploadst;
    private String listAccessError ="";

    public UploadListDescriptor(){

    }
    public void setUpsList(List<UploadDescriptors> value){

        this.uploadst = value;
    }
    public List<UploadDescriptors> getUpsList(){

        return this.uploadst;        
    }    
    public void setListAccessError(String value){
        this.listAccessError = value;
    }    
    public String getListAccessError(){
        return this.listAccessError;
    }
}
