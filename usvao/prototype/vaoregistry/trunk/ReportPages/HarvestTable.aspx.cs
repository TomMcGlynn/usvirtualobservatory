using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web.Security;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Data.SqlClient;

namespace ReportPages
{
    public partial class HarvestTable : System.Web.UI.Page
    {
        protected string _wsUrl = registry.Properties.Settings.Default.baseURL;
        string connect = registry.Properties.Settings.Default.SqlConnection;

        protected void Page_Load(object sender, EventArgs e)
        {
            Page.Title = "NVO - Harvester Administrative Information";
            GetTotals();
            GetHarvesterLogTable(HarvesterTable);
        }

        protected void GetTotals()
        {
            TotalResources.Text = "Total Active Resources in Registry: ";
            TotalCount.Text = "Total Active Local Resources: ";
            HarvestedCount.Text = "Total Active Harvested Resources: ";

            SqlConnection conn = new SqlConnection(connect);
            try
            {
                conn = new SqlConnection(connect);
                conn.Open();

                string sGetEntry = "select count(*) from resource where (harvestedFrom is null or " +
                                   "harvestedFrom = '' or harvestedFrom like 'STScI%') and [@status] = 1";
                SqlDataAdapter sqlDA = new SqlDataAdapter(sGetEntry, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);
                if (ds.Tables[0].Rows.Count > 0)
                {
                    DataRow row = ds.Tables[0].Rows[0];
                    TotalCount.Text += row[0].ToString();
                }
                sGetEntry = "select count(*) from resource where (harvestedFrom is not null and " +
                                   "harvestedFrom <> '' and (not harvestedFrom like 'STScI%') ) and [@status] = 1";
                sqlDA = new SqlDataAdapter(sGetEntry, conn);
                ds = new DataSet();
                sqlDA.Fill(ds);
                if (ds.Tables[0].Rows.Count > 0)
                {
                    DataRow row = ds.Tables[0].Rows[0];
                    HarvestedCount.Text += row[0].ToString();
                }
                sGetEntry = "select count(*) from resource where [@status] = 1";
                sqlDA = new SqlDataAdapter(sGetEntry, conn);
                ds = new DataSet();
                sqlDA.Fill(ds);
                if (ds.Tables[0].Rows.Count > 0)
                {
                    DataRow row = ds.Tables[0].Rows[0];
                    TotalResources.Text = "<b>" + TotalResources.Text + row[0].ToString() + "</b>";
                }
            }
            finally
            {
                conn.Close();
            }
        }

        protected void GetHarvesterLogTable(Table HarvestTable)
        {
            SqlConnection conn = new SqlConnection(connect);

            string[] harvestURLs = null;
            int[] currentHarvest = null;
            int[] countArray = null;

            try
            {
                conn = new SqlConnection(connect);
                conn.Open();

                GetHarvesterInfo(conn, ref harvestURLs, ref currentHarvest);

                DateTime date = DateTime.MinValue;
                string message = String.Empty;
                int status = -1;


                TableRow trow = new TableRow();
                trow.BackColor = System.Drawing.Color.LightBlue;
                TableCell tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Registry OAI Interface</center></b>"));
                trow.Cells.Add(tcell);
                tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Active Resources</center></b>"));
                trow.Cells.Add(tcell);
                tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Automatically Harvested</center></b>"));
                trow.Cells.Add(tcell);
                tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Last Harvested (UTC)</center></b>"));
                trow.Cells.Add(tcell);
                tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Result of Last Harvest</center></b>"));
                trow.Cells.Add(tcell);
                tcell = new TableCell();
                tcell.Controls.Add(new LiteralControl("<b><center>Message</center></b>"));
                trow.Cells.Add(tcell);

                HarvestTable.Rows.Add(trow);
                GetResourcesCount(conn, harvestURLs, ref countArray);

                for (int i = 0; i < harvestURLs.Length; ++i)
                {
                    string url = harvestURLs[i];
                    GetLastLogEntry(url, conn, ref date, ref message, ref status);

                    trow = new TableRow();
                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    tcell.Controls.Add(new LiteralControl(url));
                    trow.Cells.Add(tcell);
                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    tcell.HorizontalAlign = HorizontalAlign.Center;
                    tcell.Controls.Add(new LiteralControl(countArray[i].ToString()));
                    trow.Cells.Add(tcell);


                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    tcell.HorizontalAlign = HorizontalAlign.Center;
                    if (currentHarvest[i] == 1)
                    {
                        tcell.Controls.Add(new LiteralControl("Yes"));
                    }
                    else
                    {
                        tcell.Controls.Add(new LiteralControl("No"));
                        trow.BackColor = System.Drawing.Color.LightGray;
                    }
                    trow.Cells.Add(tcell);

                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    if (date > DateTime.MinValue)
                        tcell.Controls.Add(new LiteralControl(date.ToUniversalTime().ToString()));
                    else
                        tcell.Controls.Add(new LiteralControl("Never"));
                    trow.Cells.Add(tcell);

                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    if (status == 0)
                    {
                        tcell.Controls.Add(new LiteralControl("Success"));
                        tcell.ForeColor = System.Drawing.Color.Green;
                    }
                    else if (status == 1)
                    {
                        tcell.Controls.Add(new LiteralControl("Failure"));
                        tcell.ForeColor = System.Drawing.Color.Red;
                    }
                    else if (status == 2)
                    {
                        tcell.Controls.Add(new LiteralControl("Partial Failure"));
                        tcell.ForeColor = System.Drawing.Color.DarkGoldenrod;
                    }
                    else if (status == -1)
                    {
                        if (date == DateTime.MinValue)
                        {
                            tcell.Controls.Add(new LiteralControl(""));
                        }
                        else if (date.AddHours(2) < DateTime.Now)
                        {
                            tcell.Controls.Add(new LiteralControl("Did Not Complete"));
                            tcell.ForeColor = System.Drawing.Color.Red;
                        }
                        else
                            tcell.Controls.Add(new LiteralControl("Waiting..."));
                    }
                    trow.Cells.Add(tcell);

                    //todo - fix the logging so this isn't so shaky.
                    string truncMessage = String.Empty;
                    if (status != -1)
                    {
                        truncMessage = "From " + message.Replace("Wrote DB inserts.", "").Replace(url, "");
                        int inHarvest = truncMessage.IndexOf("Harvesting") + 11;
                        int inGot = truncMessage.IndexOf("Got");
                        if (inHarvest > 0 && inGot > 0)
                            truncMessage = truncMessage.Remove(inHarvest, inGot - inHarvest);
                        while (truncMessage.Contains("resumption"))
                        {
                            int startVerb = truncMessage.IndexOf("?verb");
                            int endToken = truncMessage.IndexOf("Got", truncMessage.IndexOf("resumption")) - 1;
                            if (endToken > 0)
                                truncMessage = truncMessage.Remove(startVerb, endToken - startVerb);
                            else //in case we have logged an error.
                                truncMessage = truncMessage.Remove(startVerb, truncMessage.IndexOf("Harvester", startVerb) - startVerb);
                        }
                        if (truncMessage.Contains("recs") && truncMessage.Contains("Loaded"))
                        {
                            int recs = truncMessage.IndexOf("recs") + 4;
                            int loaded = truncMessage.IndexOf("Loaded");
                            truncMessage = truncMessage.Remove(recs, loaded - recs).Insert(recs, ". ");
                        }
                    }
                    tcell = new TableCell();
                    tcell.VerticalAlign = VerticalAlign.Top;
                    tcell.Controls.Add(new LiteralControl(truncMessage));
                    trow.Cells.Add(tcell);

                    HarvestTable.Rows.Add(trow);
                }
            }
            finally
            {
                conn.Close();
            }
        }

        public void GetHarvesterInfo(SqlConnection conn, ref string[] harvestURLs, ref int[] currentHarvest)
        {
            string sGetEntry = "select distinct serviceurl, harvest from harvester";

            SqlDataAdapter sqlDA = new SqlDataAdapter(sGetEntry, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            harvestURLs = new string[ds.Tables[0].Rows.Count];
            currentHarvest = new int[ds.Tables[0].Rows.Count];
            for (int i = 0; i < ds.Tables[0].Rows.Count; ++i)
            {
                DataRow row = ds.Tables[0].Rows[i];
                harvestURLs[i] = (string)row[0];
                if ((byte)row[1] > 0)
                    currentHarvest[i] = 1;
                else
                    currentHarvest[i] = 0;
            }
        }

        private void GetResourcesCount(SqlConnection conn, string[] urls, ref int[] count)
        {
            string sGetCount = "select harvestedFrom, count(*) from Resource " +
                               "where  [@status] = 1 and (harvestedFrom is not null and harvestedFrom <> '' and harvestedFrom not like 'STScI%') " +
                               "group by harvestedFrom";

            SqlDataAdapter sqlDA = new SqlDataAdapter(sGetCount, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            count = new int[urls.Length];
            for (int thiscount = 0; thiscount < count.Length; ++thiscount)
                count[thiscount] = 0;

            for (int i = 0; i < ds.Tables[0].Rows.Count; ++i)
            {
                DataRow data = ds.Tables[0].Rows[i];

                for (int j = 0; j < urls.Length; ++j)
                {
                    if (urls[j].CompareTo((string)data[0]) == 0)
                    {
                        count[j] += (int)data[1];
                        break;
                    }
                }
            }
        }

        private void GetLastLogEntry(string harvestURL, SqlConnection conn, ref DateTime date, ref string message, ref int status)
        {
            string sGetEntry = "select top 1 date, message, status " +
                               "from harvesterlog where serviceurl = '" +
                               harvestURL + "' and status is not null " +
                               "order by date desc";

            SqlDataAdapter sqlDA = new SqlDataAdapter(sGetEntry, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);
            if (ds.Tables[0].Rows.Count > 0)
            {
                DataRow data = ds.Tables[0].Rows[0];
                date = (DateTime)data[0];
                if (!(data[1] is System.DBNull))
                    message = (String)data[1];
                if (data[2] is System.DBNull)
                    status = -1;
                else
                    status = (int)data[2];
            }
            else
            {
                date = DateTime.MinValue;
                message = String.Empty;
                status = -1;
            }
        }
    }
}