
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.HtmlControls;
using System.Text;
using System.IO;
using System.Net;
using System.Xml;
using System.Xml.Serialization;

namespace TAPService.VOSI
{

    public partial class tables : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            VOSIRequestHandler handler = new VOSIRequestHandler();
            TableSet response = (TableSet)handler.HandleRequest(typeof(tables));

            Response.Clear();
            Response.ClearHeaders();
            Response.ClearContent();
            Response.ContentType = "text/xml";

            try
            {
                XmlSerializer serializer = new XmlSerializer(response.GetType());
                XmlTextWriter xw = new XmlTextWriter(Response.OutputStream, System.Text.Encoding.UTF8);
                serializer.Serialize(xw, response);
                xw.Close();
            }
            catch (Exception ex)
            {
                Response.ContentType = "text/plain";
                Response.Output.WriteLine("Error Handling Request: " + ex.Message);
            }

            Response.End();
        }

        public static TableSet ConvertDataSetsToTableSet(DataSet tables, DataSet columns)
        {
            TableSet responseTable = new TableSet();
            responseTable.schema = new TableSchema[1];

            int nTables = tables.Tables.Count;
            responseTable.schema[0] = new TableSchema();
            responseTable.schema[0].table = new Table[nTables];

            for (int i = 0; i < nTables; ++i)
            {
                DataTable dt = tables.Tables[i];
                Table thistable = new Table();
                thistable.name = (String)dt.Rows[i][0];
                thistable.description = (String)dt.Rows[i][1];

                DataTable dc = columns.Tables[i];

                int nColumns = dc.Rows.Count;
                thistable.column = new TableParam[nColumns];
                for (int j = 0; j < nColumns; ++j)
                {
                    TableParam thiscol = new TableParam();
                    thiscol.name = (String)dc.Rows[j][0];
                    thiscol.description = (String)dc.Rows[j][1];
                    if (((String)dc.Rows[j][5]).Length > 0)
                        thiscol.unit = (String)dc.Rows[j][5];

                    //thiscol.utype = "colutype";
                    //thiscol.ucd = "colucd";

                    thistable.column[j] = thiscol;
                }

                responseTable.schema[0].table[i] = thistable;
            }

            return responseTable;
        }
    }
}
