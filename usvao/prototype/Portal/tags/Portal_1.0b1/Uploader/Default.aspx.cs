using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Net;
using System.IO;

public partial class _Default : System.Web.UI.Page
{
	/*
    //
    // NOTE: This class is a Test Driver to invoke the 2 standalone uploader Handlers:
    //
    // FileUploader.ashx: Simply Upload a file to disk
    // TargetUploader.ashx: Upload file to disk and validate it's contents as a target file.
    //
    // The Upload Buttons on this .aspx page send their HTTP POST to either one of the above handlers.
    //
    // FileUploader.ashx and TargetUploader.ashx are general purpose Upload Handlers that can
    // work with a standard File Upload POST request from any client.  Both save the uploaded
    // file to disk and return the URL for retrieving it.  The TargetUploader also validates the
    // contents of the file to ensure it is a 3 column list of targets.
    //
    // This allows for debugging of these Handlers within VS.
    //
    protected void Page_Load(object sender, EventArgs e)
    {
    }
 
    protected void btnUpload_Click(object sender, EventArgs e)
    {
        //
        // This code dmonstrates uploading a file using the MS asp:FileUpload control.
        //
    }

    public string uploadFile(string fileName, string folderName)
    {
        if (fileName == "")
        {
            return "Invalid filename supplied";
        }

        if (fileUploader.PostedFile.ContentLength == 0)
        {
            return "Invalid file content";
        }

        fileName = System.IO.Path.GetFileName(fileName);

        if (folderName == "")
        {
            return "Path not found";
        }

        try
        {
            if (fileUploader.PostedFile.ContentLength <= 2048000)
            {
                string sFilename = folderName + "\\" + fileName;
                fileUploader.PostedFile.SaveAs(sFilename); 
                return "File uploaded successfully";
            }
            else
            {
                return "Unable to upload,file exceeds maximum limit";
            }

        }

        catch (UnauthorizedAccessException ex)
        {
            return ex.Message + "Permission to upload file denied";
        }
    }
    */
}
