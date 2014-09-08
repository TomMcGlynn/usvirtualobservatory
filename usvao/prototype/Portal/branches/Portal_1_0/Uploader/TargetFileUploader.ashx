<%@ WebHandler Language="C#" Class="TargetFileUploader" %>

using System;
using System.IO;
using System.Web;
using System.Web.Configuration;
using System.Text.RegularExpressions;
using System.Configuration;

public class TargetFileUploader : FileUploader 
{
    protected override gxResultFile verifyFile(gxResultFile resultIn)
    {
        int nID = 0, nRA = 1, nDEC = 2;
        char cSeparator = '?';

        StreamReader sr = null;
        StreamWriter sw = null;

        gxResultFile resultCsv = gxResultFile.getCsvResultFile(resultIn.Filename);

        try
        {
            sr = new StreamReader(resultIn.Filename);  // Open existing target file for reading
            sw = new StreamWriter(resultCsv.Filename); // Open new Csv file for writing

            int nMaxUploadTargets = 250;
            try
            {
                string sMaxTargets = ConfigurationSettings.AppSettings["maxUploadTargets"];
                nMaxUploadTargets = int.Parse(sMaxTargets);
            }
            catch (Exception ex)
            {
                // Ignore this minor problem and keep going.
                edu.stsci.galex.gxMail.gxSendException(ref ex, "FileUploader: Unable to load Web.Config setting:maxUploadTargets.");
            }
            
            string sLine = "";
            int nLine = 0;
            int nHeader = 0;
            while ((sLine = sr.ReadLine()) != null)
            {
                nLine++;

                if (nLine > nMaxUploadTargets + nHeader)
                {
                    throw new Exception("Maximum number of upload targets [" + nMaxUploadTargets + "] exceded");
                }
                else if (nLine == 1)
                {
                    cSeparator = getSeparatorChar(sLine);
                    int id, ra, dec;
                    if (getColumnIDs(sLine, cSeparator, out id, out ra, out dec))
                    {
                        nID = id;
                        nRA = ra;
                        nDEC = dec;
                        nHeader = 1;
                        continue;
                    }
                }
                     
                string[] sLineArray = sLine.Trim().Split(cSeparator);
                if (sLineArray.Length != 3)
                {
                    throw new Exception("Line #" + nLine + ": Does not contain 3 columns.");
                }

                string sID = sLineArray[nID].Trim();
                if (sID.Length == 0)
                {
                    throw new Exception("Line #" + nLine + ": [ID] value is missing.");
                }

                string sRA = sLineArray[nRA].Trim();
                if (sRA.Length == 0)
                {
                    throw new Exception("Line #" + nLine + ": [RA] value is missing.");
                }
                else
                {
                    try
                    {
                        float.Parse(sRA);
                    }
                    catch (Exception)
                    {
                        throw new Exception("Line #" + nLine + ": [RA] value is invalid.  Expecting float value.");
                    }
                }

                string sDEC = sLineArray[nDEC].Trim();
                if (sDEC.Length == 0)
                {
                    throw new Exception("Line #" + nLine + ": [DEC] value is missing.");
                }
                else
                {
                    try
                    {
                        float.Parse(sDEC);
                    }
                    catch (Exception)
                    {
                        throw new Exception("Line #" + nLine + ": [DEC] value is invalid.  Expecting float value.");
                    }
                } 
                
                // Write out the correctly formatted record for database ingest later
                sw.WriteLine(sID + "," + sRA + "," + sDEC);   
            }
            
            // Close both files
            sr.Close();
            sw.Close();

            resultCsv.OK = true;
            resultCsv.Message = "Target File Verified";                   
            return resultCsv;
        }
        catch (Exception ex)
        {
            // Close both files
            if (sr != null) sr.Close();
            if (sw != null) sw.Close();
            
            resultCsv.OK = false;
            resultCsv.Message = "Target File Invalid: " + ex.Message;
            return resultCsv;
        }
    }

    protected char getSeparatorChar(string sLine)
    {
        char cSeparator = '?';
        if (sLine.IndexOf(",") >= 0)
        {
            cSeparator = ',';
        }
        else if (sLine.IndexOf("|") >= 0)
        {
            cSeparator = '|';
        }
        else if (sLine.IndexOf("\t") >= 0)
        {
            cSeparator = '\t';
        }
        else if (sLine.IndexOf(" ") >= 0)
        {
            cSeparator = ' ';
        }

        if (cSeparator == '?')
        {
            throw new Exception("Line #1: Column Separator character not found.");
        }

        return cSeparator;
    }

    protected Boolean getColumnIDs(string sLine, char cSeparatorIn, out int nID, out int nRA, out int nDEC)
    {
        nID = nRA = nDEC = -1;
        
        Regex reNumber = new Regex(".*[0-9]+.*");
        if (reNumber.IsMatch(sLine))
        { 
            return false; // String contains numbers, it must not be a true header defintion line.
        }

        bool bFoundRA = false, bFoundDEC = false, bFoundID = false;

        string[] sLineArray = sLine.Trim().Split(cSeparatorIn);

        for (int i = 0; i < sLineArray.Length; i++)
        {
            string sCol = sLineArray[i].ToUpper().Trim();
            switch (sCol)
            {
                case "ID":
                    nID = i;
                    bFoundID = true;
                    break;
                case "RA":
                    nRA = i;
                    bFoundRA = true;
                    break;
                case "DEC":
                    nDEC = i;
                    bFoundDEC = true;
                    break;      
            }
        }
        
        return (bFoundRA && bFoundDEC && bFoundID);
    }
}