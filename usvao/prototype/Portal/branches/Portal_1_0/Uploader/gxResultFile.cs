using System;
using System.IO;
using System.Xml;
using System.Data;
using System.Configuration;
using System.Text.RegularExpressions;

/// <summary>
/// Summary description for gxResultFile
/// </summary>
public class gxResultFile
{
    private bool bOK;
    private string sMessage;
    private string sFile;
    private string sUrl;
    private string sUnc;
    private string sDirectory;
    private string sFilenameOnly;

    public gxResultFile(bool bOKIn, string sMessageIn, string sFileIn, string sUrlIn)
    {
        init(bOKIn, sMessageIn, sFileIn, sUrlIn, "");
    }

    public gxResultFile(bool bOKIn, string sMessageIn, string sFileIn, string sUrlIn, string sUncIn)
    {
        init(bOKIn, sMessageIn, sFileIn, sUrlIn, sUncIn);
    }

    private void init(bool bOKIn, string sMessageIn, string sFileIn, string sUrlIn, string sUncIn)
    {
        bOK = bOKIn;
        sMessage = sMessageIn;
        sFile = sFileIn;
        sUrl = sUrlIn;
        sUnc = sUncIn;
        sFilenameOnly = Path.GetFileName(sFileIn);
        sDirectory = Path.GetDirectoryName(sFileIn);
    }

    public string ToXmlString()
    {
        return "<result>" +
                  "<status>" + (bOK ? "OK" : "Error") + "</status>" +
                  "<message>" + sMessage + "</message>" +
                  "<file>" + sFile + "</file>" +
                  "<url>" + sUrl + "</url>" +
                  "<unc>" + sUnc + "</unc>" +
                "</result>";
    }

    public XmlDocument toXmlDocument()
    {
        XmlDocument xmlDoc = new XmlDocument();
        xmlDoc.LoadXml(ToXmlString());
        return xmlDoc;
    }

    public string Filename
    {
        get { return sFile; }
        set { sFile = value; }
    }

    public string Directory
    {
        get { return sDirectory; }
        set { sDirectory = value; }
    }

    public string FilenameOnly
    {
        get { return sFilenameOnly; }
        set { sFilenameOnly = value; }
    }

    public string Url
    {
        get { return sUrl; }
        set { sUrl = value; }
    }

    public string Unc
    {
        get { return sUnc; }
        set { sUnc = value; }
    }

    public bool OK
    {
        get { return bOK; }
        set { bOK = value; }
    }

    public string Message
    {
        get { return sMessage; }
        set { sMessage = value; }
    }

    //
    // Static Functions Used to generate Filenames Used throughout the Uploader Service
    //
    public static gxResultFile getUniqueResultFile(string sFileIn)
    {
        // Remove '+' character which causes headache for the SQL
        sFileIn = sFileIn.Replace('+', '-');

        string sInternalDir = ConfigurationManager.AppSettings["internalTempDir"];
        string sExternalDir = ConfigurationManager.AppSettings["externalTempDir"];

        //
        // Ensure that only one thread at a time is determining a unique filename
        //
        string sFileNameIn = Path.GetFileName(sFileIn);
        string sFullFileName = sInternalDir + sFileNameIn;
        string sUrl = ""; // initialized below

        lock (typeof(gxResultFile))
        {
            int i = 0;
            while (File.Exists(sFullFileName))
            {
                sFullFileName = sInternalDir + Path.GetFileNameWithoutExtension(sFileNameIn) + "_" + (i++) + Path.GetExtension(sFileNameIn);
            }

            //
            // store the new URL
            //
            sUrl = sExternalDir + Path.GetFileName(sFullFileName);

        }  // lock()

        return new gxResultFile(true, "", sFullFileName, sUrl, "");
    }

    public static gxResultFile getCsvResultFile(string sFileNameIn)
    {
        string sCsvFileName = Path.GetFileNameWithoutExtension(sFileNameIn) + ".csv";

        string sInternalUploadDir = ConfigurationManager.AppSettings["internalCsvDir"];
        string sExternalUploadDir = ConfigurationManager.AppSettings["externalCsvDir"];
        string sInternalUncDir = ConfigurationManager.AppSettings["internalUncDir"];

        string sHostName = System.Environment.MachineName;;

        string sFile = sInternalUploadDir + sCsvFileName;
        string sUrl = sExternalUploadDir + sCsvFileName;
        string sUnc = (sInternalUncDir + sCsvFileName).Replace("[HOSTNAME]", sHostName);

        return new gxResultFile(true, "", sFile, sUrl, sUnc);
    }
}
