using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;
using System.Net;
using System.Xml;
using System.Xml.Linq;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.IO;
using System.Text;
using System.Diagnostics;
using System.Configuration;
using System.Threading;

using log4net;

using Utilities;

namespace Mashup.Adaptors
{	
    [Serializable]
    public class Download : ISyncAdaptor
    {
		//
		// Logger Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		const int BLOCK_SIZE = 100*1024;		// 100K chunks.
		
		public String url {get; set;}
		public String file {get; set;}
		public String filename {get; set;}
		public String attachment {get; set;}
		
        public Download()
        {
			url = "";
			file = "";
			filename = "";
			attachment = "";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest muRequest, HttpResponse httpResponse)
        {	

			IDictionary<string, object> paramss = muRequest.paramss;

			if (paramss != null && paramss.Count > 0)
			{
				if (paramss.ContainsKey("url")) url = paramss["url"].ToString();
				if (paramss.ContainsKey("file")) file = paramss["file"].ToString();
				if (paramss.ContainsKey("filename")) filename = paramss["filename"].ToString();
				if (paramss.ContainsKey("attachment")) attachment = paramss["attachment"].ToString();;
				
				if (url != null && url.Trim().Length > 0)
				{
					downloadUrl(url, filename, attachment, httpResponse);

				}
				else if (file != null && file.Trim().Length > 0)
				{
					downloadFile(file, filename, attachment, httpResponse);
				}
				else
				{
					throw new Exception("Download Adaptor must have either 'url' or 'file' param specified.");
				}
			}
		}
		
		#region downloadFile
		public void downloadFile(string file, string filename, string attachment, HttpResponse httpResponse) 
		{
			System.IO.Stream fs = null;

			try
			{
				// Decode attachment arg
				bool bAttachment = Convert.ToBoolean(attachment);

				// Verify that the file exists on disk
				string filepath = System.Web.HttpContext.Current.Server.MapPath(file);
				if (!File.Exists(filepath))
				{
					throw new Exception("File does not Exist: " + file);
				}
				
				// Set mime types based on file extension  
				string type = "application/octet-stream";
				string ext = Path.GetExtension(filepath);
				
				if ( ext != null )
				{
					switch( ext.ToLower() )
					{
						case ".xml":
						case ".vot":
							type = "text/xml";
						break;
						
						case ".htm":
						case ".html":
							type = "text/HTML";
						break;
						
						case ".txt":
						case ".json":
						case ".jsn":
							type = "text/plain";
						break;
						
						case ".csv":
							type = "text/csv";
						break;
						
						case ".doc":
						case ".rtf":
							type = "Application/msword";
						break;
					}
				}
				
				//
				// Open and Write the file back the client in buffered chunks
				//
				byte[] buffer = new Byte[10000];		// Buffer to read 10K bytes in chunk:
				int length;								// Length of the file:
				long dataToRead;						// Total bytes to read:
			
				// Open the file.
				fs = new System.IO.FileStream(filepath, 
											  System.IO.FileMode.Open, 
										      System.IO.FileAccess.Read,System.IO.FileShare.Read);
		
				// Total bytes to read:
				dataToRead = fs.Length;
		
				httpResponse.ContentType = type;
				if (bAttachment) {
					httpResponse.AppendHeader("Content-Disposition", "attachment; filename=" + "\"" + filename + "\"");
				}
		
				// Read the bytes while client is connected
		  		while (dataToRead > 0 && httpResponse.IsClientConnected)
				{
					// Read the data in buffer.
					length = fs.Read(buffer, 0, 10000);
	
					// Write the data to the current output stream.
					httpResponse.OutputStream.Write(buffer, 0, length);
	
					// Flush the data to the HTML output.
					httpResponse.Flush();
	
					dataToRead -= length;
				}
				
				log.Info(tid + "<=== " + "[DOWNLOAD_FILE] complete. file: " + file + " length: " + fs.Length);
			}
			catch (Exception ex)
			{
				// Close the open file stream (if necessary)
				if (fs != null) 
				{
					fs.Close();
				}				
				// Throw Exception up for Consolidated Mashup Error Processing
				throw ex;
			}
		}
		#endregion
		
		#region downloadUrl
		public void downloadUrl(string url, string filename, string attachment, HttpResponse httpResponse) 
		{
			// Extract filename from url
			if (filename.Trim().Length == 0) filename = Path.GetFileName(url);
			
			// Decode attachment arg
			bool bAttachment = Convert.ToBoolean(attachment);
			
			// Set mime types based on url extension  
			string type = "application/octet-stream";	
			
			//
			// Open the URL and Write the contents back the client in buffered chunks
			//
			System.Net.WebResponse webResponse = Utilities.Web.getWebResponse(url);
			
			// Ensure that the output content type will match the content type we're reading.
			if (webResponse.ContentType != null && webResponse.ContentType.Trim().Length > 0)
			{
				httpResponse.ContentType = webResponse.ContentType;
			}
			else
			{
				httpResponse.ContentType = type;
			}
			
			if (bAttachment) {
				httpResponse.AppendHeader("Content-Disposition", "attachment; filename=" + "\"" + filename + "\"");
			}
			
			// Relay the content one block at a time back to the client.
			int totalLength = 0;
			BinaryReader reader = new BinaryReader(webResponse.GetResponseStream(), System.Text.Encoding.ASCII);
			byte[] block = new byte[BLOCK_SIZE];
			int bytesRead = 0;
			while ((bytesRead = reader.Read(block, 0, BLOCK_SIZE)) > 0) 
			{
				httpResponse.OutputStream.Write(block, 0, bytesRead);	
				totalLength += bytesRead;
				httpResponse.Flush();
			}
			
			log.Info(tid + "<=== " + "[DOWNLOAD_URL] Complete. url: " + url + " length: " + totalLength);
		}
		#endregion	
    }
}
