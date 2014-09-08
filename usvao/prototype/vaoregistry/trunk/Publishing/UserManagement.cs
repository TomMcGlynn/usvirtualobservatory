using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;

namespace Publishing
{
    public class UserManagement
    {
        private static string sAdminConnect = registry.Properties.Settings.Default.SqlAdminConnection;
        private static string sConnect = registry.Properties.Settings.Default.SqlConnection;
        private static string sUserDecrypt = Properties.Settings.Default.userDecrypt;

        private static string strGetAuths = "select distinct identifier from Resource, Authority where Authority.rkey = Resource.pkey and" +
                                     " Authority.pkey in (select authkey from UserAuthorities where ukey = $1)";

        internal static bool UserExists(string username)
        {
            try
            {
                SqlConnection conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter("select * from users where username = '" + username + "'", conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                return (ncount > 0);
            }
            catch
            {
                //true for non-collision, to be on the safe side? 
                return true;
            }
        }


        internal string[] CheckLoginCredentials(string username, string password, ref long ukey)
        {
            System.Collections.ArrayList errors = new System.Collections.ArrayList();

            try
            {
                string strSelect = "OPEN SYMMETRIC KEY PASS_Key_01 DECRYPTION BY CERTIFICATE PublishingPasswords WITH PASSWORD = '" + sUserDecrypt + "'; " +
                    " select pkey from users where username = '" + username + "' and CONVERT(nvarchar, DecryptByKey(bpassword)) = '" + password + '\'';
                //string strSelect = "select pkey from users where username = '" + username + "' and password = '" + password + '\'';

                SqlConnection conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter(strSelect, conn);              
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                if (ncount > 0)
                {
                    ukey = (long)ds.Tables[0].Rows[0][0];
                }
                else
                {
                    errors.Add("Incorrect username or password.");
                    ukey = -1;
                }
            }
            catch( Exception ex)
            {
                errors.Add("Error logging in: " + ex.Message);
            }


            return (string[])errors.ToArray(typeof(string));
        }

        internal string[] GetUserAuths(long ukey, ref string[] userAuths)
        {
            try
            {
                SqlConnection conn = new SqlConnection(sConnect);
                conn.Open();
                userAuths = GetAuthorityList(ukey, conn);
                conn.Close();
            }
            catch (Exception ex)
            {
                return (new string[1] { ex.Message } );
            }
            return new string[] { };
        }

        internal string[] RegisterNewUser(string username, string password, string authorityID, string email, string name, ref long ukey)
        {
            System.Collections.ArrayList errors = new System.Collections.ArrayList();

            SqlConnection conn = new SqlConnection(sAdminConnect);
            conn.Open();

            long authkey = GetAuthorityKey(authorityID, conn);
            if (authkey == 0)
                errors.Add("Error finding authority information about your associated institution.");
            else
            {
                //add user to list, and add to additional authority table
                string sql = string.Empty;
                try
                {
                    //too much effort atm to alter table to allow nulls on the old unencrypted PASS column. add it as an empty string.
                    string sqlKeyManagementInsert = "OPEN SYMMETRIC KEY PASS_Key_01 DECRYPTION BY CERTIFICATE PublishingPasswords WITH PASSWORD = '" + sUserDecrypt + "'; " +
                                                    " declare @pwd nvarchar(16) = '" + password + "'; " +
                                                    " insert into users(name, username, password, bpassword, email, authkey) values ('" +
                                    name + "', '" + username + "', '', EncryptByKey(Key_GUID('PASS_Key_01'), @pwd), '" + email + "', " + authkey + ")";
                    //sql = "insert into users(name, username, password, email, authkey) values ('" +
                    //                 name + "', '" + username + "', '" + password + "', '" + email + "', " + authkey + ")";

                    SqlTransaction transaction;
                    transaction = conn.BeginTransaction("NewUserTransaction");
                    SqlCommand command = new SqlCommand(sqlKeyManagementInsert);
                    //SqlCommand command = new SqlCommand(sql, conn);
                    command.Connection = conn;
                    command.Transaction = transaction;

                    int rows = command.ExecuteNonQuery();
                    transaction.Commit();
                    if(rows > 0 )
                    {
                        errors.AddRange(CheckLoginCredentials(username, password, ref ukey));
                        string sqlAuth = "insert into userAuthorities(ukey, authkey) values (" + ukey + ", " + authkey + ")";
                        SqlCommand commandAuth = new SqlCommand(sqlAuth, conn);
                        rows = commandAuth.ExecuteNonQuery();
                    }
                }
                catch (Exception ex)
                {
                    //transaction.Rollback();
                    errors.Add("Error registering new user: " + ex.Message);
                }
            }
            return (string[])errors.ToArray(typeof(string));
        }

        public static long GetAuthorityKey(string id, SqlConnection conn)
        {
            string select = "SELECT Authority.pkey FROM Authority, Resource WHERE( Authority.rkey = Resource.pkey and Resource.identifier LIKE '" + id + "')";
            SqlDataAdapter sqlDA = new SqlDataAdapter(select, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            if (ds.Tables[0].Rows.Count > 0)
            {
                DataRow auth = ds.Tables[0].Rows[0];
                return (long)auth[0];
            }
            return 0;
        }

        public static string[] GetAuthorityList(long ukey, SqlConnection conn)
        {
     
            SqlDataAdapter sqlDA = new SqlDataAdapter(strGetAuths.Replace("$1", ukey.ToString()), conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            string[] results = new string[ds.Tables[0].Rows.Count];

            for (int i = 0; i < results.Length; ++i )
            {
                results[i] = (string)ds.Tables[0].Rows[i][0];
            }
            return results;
        }
    }
}