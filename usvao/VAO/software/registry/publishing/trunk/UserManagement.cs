using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;

namespace Publishing
{
    public class UserManagement
    {
        private static string sAdminConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];
        private static string sConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlConnection"];
        private static string sUserDecrypt = (string)System.Configuration.ConfigurationManager.AppSettings["userDecrypt"];

        private static string strGetAuths = "select distinct authorityID from UserAuthorities where ukey = $1";
        private static string strCheckAuthForOrg = "select ivoid from resource where ivoid like '$1%' and rstat = 1 and res_type = 'Organisation'";

        internal static long GetUserKey(string username)
        {
            try
            {
                SqlConnection conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter("select pkey from users where username = '" + username + "'", conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                return (long)ds.Tables[0].Rows[0][0];
            }
            catch
            {
                return 0;
            }
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
                return (new string[] {} );
            }
            return new string[] { };
        }

        internal bool CheckAuthForOrg(string auth)
        {
            bool foundOrg = false;
            try
            {
                SqlConnection conn = new SqlConnection(sConnect);
                conn.Open();
                SqlDataAdapter sqlDA = new SqlDataAdapter(strCheckAuthForOrg.Replace("$1", auth), conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                string[] results = new string[ds.Tables[0].Rows.Count];
                if (results.Length > 0)
                    foundOrg = true;
            }
            catch (Exception ex)
            {
            }
            return foundOrg;
        }

        internal string[] RegisterNewUser(string username, string email, string name, ref long ukey)
        {
            System.Collections.ArrayList errors = new System.Collections.ArrayList();

            SqlConnection conn = new SqlConnection(sAdminConnect);
            conn.Open();

            {
                //add user to list, and add to additional authority table
                string sql = string.Empty;
                try
                {
                    string sqlKeyManagementInsert = "insert into users(name, username, email) values ('" +
                                                      name + "', '" + username + "','" + email + "')";

                    SqlTransaction transaction;
                    transaction = conn.BeginTransaction("NewUserTransaction");
                    SqlCommand command = new SqlCommand(sqlKeyManagementInsert);
                    //SqlCommand command = new SqlCommand(sql, conn);
                    command.Connection = conn;
                    command.Transaction = transaction;

                    int rows = command.ExecuteNonQuery();
                    transaction.Commit();
                    //local login?

                    ukey = GetUserKey(username);
                }
                catch (Exception ex)
                {
                    //transaction.Rollback();
                    errors.Add("Error registering new user: " + ex.Message);
                }
            }
            return (string[])errors.ToArray(typeof(string));
        }

        public static string[] AssociateDefaultUserAuthority(string authorityID, long ukey)
        {
            System.Collections.ArrayList errors = new System.Collections.ArrayList();
            try
            {
                if (ukey > 0 && authorityID.Length > 0)
                {
                    SqlConnection conn = new SqlConnection(sAdminConnect);
                    conn.Open();

                    string sqlAuth = "insert into userAuthorities(ukey, authorityID) values (" + ukey + ",'" + authorityID + "')";
                    SqlCommand commandAuth = new SqlCommand(sqlAuth, conn);
                    int rows = commandAuth.ExecuteNonQuery();
                    if (rows == 0)
                        errors.Add("Error creating new user authority information");
                    else
                    {
                        sqlAuth = "update users set defaultAuthorityID = '" + authorityID +  "' where pkey = " + ukey;
                        commandAuth = new SqlCommand(sqlAuth, conn);
                        rows = commandAuth.ExecuteNonQuery();
                        if (rows == 0)
                            errors.Add("Error updating user authority information");
                    }

                    conn.Close();
                }
            }
            catch (Exception ex)
            {
                //transaction.Rollback();
                errors.Add("Error associating user with authority record: " + ex.Message);
            }
            return (string[])errors.ToArray(typeof(string));
        }

        //internal string[] ResetUserPassword(string username, string temppassword, string password, ref long ukey)
        //{
        //    System.Collections.ArrayList errors = new System.Collections.ArrayList(); 
        //    if ( ukey > 0)
        //    {
        //        SqlConnection conn = new SqlConnection(sAdminConnect);
        //        conn.Open();
        //        {
        //            string sql = string.Empty;
        //            try
        //            {
        //                string sqlKeyManagementInsert = "OPEN SYMMETRIC KEY PASS_Key_01 DECRYPTION BY CERTIFICATE PublishingPasswords WITH PASSWORD = '" + sUserDecrypt + "'; " +
        //                                                " declare @pwd nvarchar(128) = '" + password + "'; " +
        //                                                " update users set bpassword = EncryptByKey(Key_GUID('PASS_Key_01'), @pwd) where username = '" + username + "' and pkey = " + ukey + ";";

        //                SqlTransaction transaction;
        //                transaction = conn.BeginTransaction("NewUserTransaction");
        //                SqlCommand command = new SqlCommand(sqlKeyManagementInsert);
        //                command.Connection = conn;
        //                command.Transaction = transaction;

        //                int rows = command.ExecuteNonQuery();
        //                transaction.Commit();

        //            }
        //            catch (Exception ex)
        //            {
        //                //transaction.Rollback();
        //                errors.Add("Error updating user password: " + ex.Message);
        //            }
        //        }
        //    }

        //    return (string[])errors.ToArray(typeof(string));
        //}

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