using System;
using System.Collections.Generic;
using System.Text;
using System.Configuration;
using System.Collections;
using System.Data;
using System.Data.SqlClient;

namespace UserMigrationTool
{
    class Program
    {
        static void Main(string[] args)
        {
            string TargetDBWrite = (string)UserMigrationTool.Properties.Settings.Default["TargetDBWrite"];
            string SourceDBRead = (string)UserMigrationTool.Properties.Settings.Default["SourceDBRead"];
            //string userEncryption = (string)UserMigrationTool.Properties.Settings.Default["UserEncryption"];

            SqlConnection connWrite = new SqlConnection(TargetDBWrite);
            SqlConnection connReadOld = new SqlConnection(SourceDBRead);
            connReadOld.Open();
            connWrite.Open();


            #region read all the old data to transfer
            DataSet ds = new DataSet();
            try
            {
                //string strSelect = "OPEN SYMMETRIC KEY PASS_Key_01 DECRYPTION BY CERTIFICATE PublishingPasswords WITH PASSWORD = '" + userEncryption + "'; " +
                //    " select username, name, email, CONVERT(nvarchar, DECRYPTBYKEY(bpassword)) as pwd, defaultAuthorityID, pkey from Users order by username asc;";
                string strSelect = " select username, name, email, defaultAuthorityID, pkey from Users order by username asc; ";

                strSelect += @"SELECT Users.username, userAuthorities.authorityID 
                                FROM UserAuthorities, Users
                                WHERE UserAuthorities.ukey = Users.pkey 
                                order by username asc;";

                strSelect += @"select distinct identifier, username
                                    from Resource, users
                                    where [@status] = 1 and ukey is not null
                                    and Users.pkey = Resource.ukey";

                SqlDataAdapter sqlDA = new SqlDataAdapter(strSelect, connReadOld);
                sqlDA.Fill(ds);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }
            #endregion

           /* #region write user data to new db
            try
            {
                //string sqlKeyManagementInsert = "OPEN SYMMETRIC KEY PASS_Key_01 DECRYPTION BY CERTIFICATE PublishingPasswords WITH PASSWORD = '" + userEncryption + "'; ";
                string sqlInsert = string.Empty;

                foreach (DataRow dr in ds.Tables[0].Rows)
                {
                    string username = (string)dr[0];
                    string name = (string)dr[1];
                    string email = (string)dr[2];
                    //string pwd = (string)dr[3];
                    string defaultAuth = "NULL";
                    //if (dr[4] != null && dr[4] != DBNull.Value)
                    //    defaultAuth = (string)dr[4];
                    if (dr[3] != null && dr[3] != DBNull.Value)
                        defaultAuth = (string)dr[3];

                    //sqlKeyManagementInsert += " insert into users(name, username, bpassword, email, defaultAuthorityID) values ('" +
                    //              name + "', '" + username + "', EncryptByKey(Key_GUID('PASS_Key_01'), CONVERT(NVARCHAR(128), '" + pwd + "')), '" + email + "', '" + defaultAuth + "');";
                    sqlInsert += " insert into users(name, username, email, defaultAuthorityID) values ('" +
                                  name + "', '" + username + "', '" + email + "', '" + defaultAuth + "');";
 
                }

                SqlTransaction transaction;
                transaction = connWrite.BeginTransaction("NewUserTransaction");
                //SqlCommand command = new SqlCommand(sqlKeyManagementInsert);
                SqlCommand command = new SqlCommand(sqlInsert);
                command.Connection = connWrite;
                command.Transaction = transaction;

                int rows = command.ExecuteNonQuery();
                transaction.Commit();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }
            #endregion*/

            #region get new keys for authority in new db
            DataSet dsNewUserKeys = new DataSet();
            try
            {
                string strSelect = "select username, pkey from users order by username asc";

                SqlDataAdapter sqlDA = new SqlDataAdapter(strSelect, connWrite);
                sqlDA.Fill(dsNewUserKeys);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }
            #endregion
            

           /* #region write authority data to new db
            try
            {
                string sqlInsert = string.Empty;

                foreach (DataRow dr in ds.Tables[1].Rows)
                {
                    long ukey = 0;
                    string username = (string)dr[0];
                    string auth = "NULL";
                    if (dr[1] != null && dr[1] != DBNull.Value)
                    {
                        auth = (string)dr[1];
                        foreach (DataRow drNewUsers in dsNewUserKeys.Tables[0].Rows)
                        {
                            if ((string)drNewUsers[0] == username)
                            {
                                ukey = (long)drNewUsers[1];
                                break;
                            }
                        }

                        if (ukey != 0)
                        {
                            sqlInsert += " insert into userAuthorities(authorityID, ukey) values ('" + auth + "', " + ukey + "); ";
                        }
                    }
                }

                SqlTransaction transaction;
                transaction = connWrite.BeginTransaction("UserAuthoritiesTransaction");
                SqlCommand command = new SqlCommand(sqlInsert);
                command.Connection = connWrite;
                command.Transaction = transaction;

                int rows = command.ExecuteNonQuery();
                transaction.Commit();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }
            #endregion*/

            #region write resource user linking data to new db
            try
            {
                string sqlUpdate = string.Empty;

                foreach (DataRow drOwnedResources in ds.Tables[2].Rows)
                {
                    long ukey = 0;
                    string identifier = (string)drOwnedResources[0];
                    string username = (string)drOwnedResources[1];

                    foreach (DataRow drNewUsers in dsNewUserKeys.Tables[0].Rows)
                    {
                        if ((string)drNewUsers[0] == username)
                        {
                            ukey = (long)drNewUsers[1];
                            break;
                        }
                    }
                    if (ukey != 0)
                    {
                        sqlUpdate += " UPDATE resource SET UKEY = " + ukey + " WHERE ivoid = '" + identifier + "'; ";
                    }
                }

                //SqlTransaction transaction;
                //transaction = connWrite.BeginTransaction("UserResourceLinkingTransaction");
                //SqlCommand command = new SqlCommand(sqlUpdate);
                //command.Connection = connWrite;
                //command.Transaction = transaction;

                //int rows = command.ExecuteNonQuery();
                //transaction.Commit();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }
            #endregion
            

            connReadOld.Close();
            connWrite.Close();
        }
    }
}
