using System;
using net.ivoa;
using net.ivoa.VOTable;
using System.Data;
using System.Collections;
using System.Data.SqlClient;


	/// <summary>
	/// Summary description for Class1.
	/// </summary>
	public class VOTableFn
	{
		public static void WriteVOTable (DataSet myData, DataSet myUCD) : System.Xml.
        {
            string  description="Test";

            int rowcountDATA = myData.Tables[0].Rows.Count;
            int colcountDATA = myData.Tables[0].Columns.Count;
            int rowcountUCD  = myUCD.Tables[0].Rows.Count;
            int colcountUCD  = myUCD.Tables[0].Columns.Count;


            Response.Write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            Response.Write("<!DOCTYPE VOTABLE SYSTEM \"http://us-vo.org/xml/VOTable.dtd\">\n");
            Response.Write("<VOTABLE>\n");
            Response.Write("<DESCRIPTION >\n");
            Response.Write(description);
            Response.Write("</DESCRIPTION>\n");
            Response.Write(@"<DEFINITIONS><COOSYS ID=""J2000"" system=""eq_FK5"" equinox=""J2000"" /></DEFINITIONS>");
            Response.Write("<RESOURCE>\n");
            Response.Write("<TABLE>\n");

            foreach (DataRow ucdrow in myUCD.Tables[0].Rows)
            {
                for (int y = 0; y < aFields.Length; y++)
                {
                    if (ucdrow[0].Equals(aFields[y]))
                    {
                        Response.Write("<FIELD ID=\"" + ucdrow[0] + "\" datatype =\"" +
                            ucdrow[1] + "\" ucd=\"" + ucdrow[2] + "\"/>\n");
                    }
                }
            }

            Response.Write("<DATA>\n");
            Response.Write("<TABLEDATA>\n");

            try
            {
                if (ncount == 0)
                {
                    //Response.Write("<TR> No rows returned </TR>");
                }
                else
                {	//int kk=0;
                    foreach (DataRow pRow in ds.Tables[0].Rows)
                    {
                        Response.Write("<TR>");
                        for (int Index = 0; Index < ds.Tables[0].Columns.Count; Index++)
                        {
                            Response.Write("<TD>" + pRow[Index].ToString() + "</TD>");
                        }
                        Response.Write("</TR>\n");
                    }
                }
            }
            catch (NullReferenceException e)
            {
                Response.Write("<Diagnostic> " + e.Message + "</Diagnostic>");
            }
            Response.Write("</TABLEDATA>\n");
            Response.Write("</DATA>\n");
            Response.Write("</TABLE>\n");
            Response.Write("<INFO name=\"QUERY_STATUS\" value=\"OK\"> </INFO>\n");
            Response.Write("</RESOURCE>\n");
            Response.Write("</VOTABLE>\n");

		}
	}
