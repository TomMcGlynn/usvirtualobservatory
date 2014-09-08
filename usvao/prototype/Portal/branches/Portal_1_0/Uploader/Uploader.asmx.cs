using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Configuration;
using System.IO;

namespace Uploader
{
    /// <summary>
    /// Summary description for Service1
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class Uploader : System.Web.Services.WebService
    {
        [WebMethod]
        public void invoke()
        {
            // Response type is void because we write back a custom result
			// First off, we have to deal with a POST and extract any uploaded files:
			processUploadeRequest(Context);
        }
		
		protected void processUploadeRequest(HttpContext context)
	    {
	        // Verify the contents contain a file
	        if (context.Request.Files.Count == 0)
	        {
	            context.Response.Write("No file(s) uploaded.");
	            return;
	        }
	
	        foreach (string fileKey in context.Request.Files)
	        {
	            // Save the file out to disk
	            HttpPostedFile file = context.Request.Files[fileKey];
	            UploaderResponse response = saveHttpFile(file);
	            context.Response.Write(response.msg);
	        }
	    } 
	
	    private UploaderResponse saveHttpFile(HttpPostedFile httpFile)
	    {
	        UploaderResponse ulResponse = getUploaderResponse(httpFile.FileName);
	
	        try
	        {
	            httpFile.SaveAs(ulResponse.filename);
	            ulResponse.msg = "Upload Completed.  Filename = " + ulResponse.filename;
	        }
	        catch (Exception ex)
	        {
	            ulResponse.msg = "Upload Failed: " + ex.Message;
	        }
			
			return ulResponse;
	    }
		
		private class UploaderResponse
		{
			public string msg = "";
			public string filename = "";
			public string url = "";
			
			public UploaderResponse (string filename, string url) 
			{
				this.filename = filename;
				this.url = url;
			}
		}
		
		//
		// Need a static lock here
		//
		static object UploaderLock = new object();
		
		private UploaderResponse getUploaderResponse(string fileNameIn)
		{
	        // Remove '+' character which causes headache(s) for the SQL
	        fileNameIn = fileNameIn.Replace('+', '-');
	
	        string internalTempDir = ConfigurationManager.AppSettings["internalTempDir"];
	        string externalTempDir = ConfigurationManager.AppSettings["externalTempDir"];
	
	        //
	        // Ensure that only one thread at a time is determining a unique filename
	        //
	        string fileName = Path.GetFileName(fileNameIn);
	        string fullFileName = internalTempDir + fileName;
	        string url = ""; // initialized below
	
	        lock (UploaderLock)
	        {
	            int i = 0;
	            while (File.Exists(fullFileName))
	            {
	                fullFileName = internalTempDir + Path.GetFileNameWithoutExtension(fileName) + "_" + (i++) + Path.GetExtension(fileName);
	            }
	
	            // set the new URL
	            url = externalTempDir + Path.GetFileName(fullFileName);
	
	        }  // lock()
	
	        UploaderResponse ulResponse = new UploaderResponse(fullFileName, url);
			return ulResponse;
	    }
    }
}