using System;
using System.Collections;
using System.Data;
using System.Data.SqlClient;
using tapLib.Args;


namespace tapLib.Db
{
    partial class ConnectionTester
    {
        public ConnectionTester()
        {
        }

        public bool Test(string strConn)
        {
            bool connected = false;
            try
            {
                SqlConnection conn = new SqlConnection(strConn);
                conn.Open();
                conn.Close();
                connected = true;
            }
            catch (Exception) { connected = false; }

            return connected;
        }
    }
}
