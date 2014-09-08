<%@ WebHandler Language="C#" Class="FileUploader" %>

using System;
using System.IO;
using System.Web;
using System.Web.Configuration;
using System.Configuration;

public class FileUploader : IHttpHandler
{
    public virtual void ProcessRequest(HttpContext _context)
    {
        // Verify the contents contain a file
        if (_context.Request.Files.Count == 0)
        {
            gxResultFile result = new gxResultFile(false, "No file uploaded.", "", "");
            _context.Response.Write(result.ToXmlString());
            return;
        }

        foreach (string fileKey in _context.Request.Files)
        {
            // Save the file out to disk
            HttpPostedFile file = _context.Request.Files[fileKey];
            gxResultFile result = saveHttpFile(file);
            if (result.OK)
            {
                //result = verifyFile(result);
            }
            _context.Response.Write(result.ToXmlString());
        }
    } 

    public gxResultFile saveHttpFile(HttpPostedFile httpFile)
    {
        gxResultFile result = gxResultFile.getUniqueResultFile(httpFile.FileName);

        try
        {
            httpFile.SaveAs(result.Filename);
            result.OK = true;
            result.Message = "Upload Completed.";
            return result;
        }
        catch (Exception ex)
        {
            result.OK = false;
            result.Message = "Upload Failed: " + ex.Message;
            return result;
        }
    }
    
    public bool IsReusable
    {
        get { return true; }
    }
}