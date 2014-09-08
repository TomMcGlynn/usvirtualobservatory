using System;
using net.ivoa.VOTable;
using System.Data;
using System.Collections;
using System.Data.SqlClient;
	/// <summary>
	/// Summary description for VOTableUtil.
	/// </summary>
	public abstract class VOTableUtil {
		private static Hashtable FDT_table = null;
	
		public static DataSet VOTable2DataSet(VOTABLE vot){
			DataSet rst = new DataSet();
			Hashtable dstype = getdataTypeTableReverse();
			for (int k=0; k<vot.RESOURCE[0].TABLE.Length; k++) {
				DataTable dt = new DataTable();
				for (int j=0; j<vot.RESOURCE[0].TABLE[k].Items.Length; j++) {	
					DataColumn dc = new DataColumn();
					dc.ColumnName = ((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).name;
                    
					dc.DataType = dstype[((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).datatype] as Type;
					dt.Columns.Add(dc);
				}
				TABLEDATA data = (TABLEDATA)vot.RESOURCE[0].TABLE[k].DATA.Item;
				for (int i=0; i<data.TR.Length; i++) {//rows
					DataRow row = dt.NewRow();
					for (int j=0; j<(data.TR[i].TD.Length); j++){//cols
						Type t = dt.Columns[j].DataType;
						row[j] = data.TR[i].TD[j].Text[0];
					}
					dt.Rows.Add(row);
				}
				rst.Tables.Add(dt);
			}
			return rst;
		}


        public static VOTABLE CreateErrorVOTable(string errortext)
        {
            VOTABLE vot = new VOTABLE();

            vot.version = VOTABLEVersion.Item11;
            vot.RESOURCE = new RESOURCE[1];
            vot.RESOURCE[0] = new RESOURCE();
            vot.RESOURCE[0].type = RESOURCEType.results;
            vot.RESOURCE[0].TABLE = new TABLE[1];
            vot.RESOURCE[0].TABLE[0] = new TABLE();

            vot.INFO = new INFO[1];
            vot.INFO[0] = new INFO();
            vot.INFO[0].name = "QUERY_STATUS";
            vot.INFO[0].value = "ERROR";
            vot.INFO[0].Text = new string[1];
            vot.INFO[0].Text[0] = errortext;

            return vot;
        }

        public static bool IsErrorVOTable(VOTABLE vot)
        {
            if (vot.INFO != null && vot.INFO.Length > 0 && vot.INFO[0].value == "ERROR")
                return true;
            return false;
        }

		public static VOTABLE DataSet2VOTable(DataSet ds) {

		VOTABLE vot = new VOTABLE();

			vot.version = VOTABLEVersion.Item11;
			vot.RESOURCE = new RESOURCE[1];
			vot.RESOURCE[0] = new RESOURCE();
			vot.RESOURCE[0].type = RESOURCEType.results;

			int ntbl = ds.Tables.Count;
			vot.RESOURCE[0].TABLE = new TABLE[ntbl];
			vot.INFO = new INFO[ntbl];


			// get name and type of columns
			Hashtable votype = getdataTypeTable();
			for (int k=0; k<ntbl; k++) 
			{
				DataTable dt = ds.Tables[k];
				vot.INFO[k] = new INFO();
				vot.INFO[k].name = "QUERY_STATUS";
                vot.INFO[k].value = "OK";

				vot.RESOURCE[0].TABLE[k] = new TABLE();

				vot.RESOURCE[0].TABLE[k].Items = new object[dt.Columns.Count];
				for (int j=0; j<dt.Columns.Count; j++) {	
					vot.RESOURCE[0].TABLE[k].Items[j] = new FIELD();
					((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).name = dt.Columns[j].ColumnName;
					try 
					{
						((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).datatype = (dataType) votype[dt.Columns[j].DataType];
						if (((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).datatype  == dataType.@char) 
						{
							((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).arraysize="*";
						}
					} 
					catch (Exception e) 
					{
						Console.Out.WriteLine(e+":"+e.StackTrace);
					}
				}
				// load data
				TABLEDATA data = new TABLEDATA();
				data.TR= new TR[dt.Rows.Count];
				for (int i=0; i<data.TR.Length; i++) 
				{
					data.TR[i]=new TR();
					data.TR[i].TD = new TD[dt.Columns.Count];
					for (int j=0; j<data.TR[i].TD.Length; j++) 
					{
						data.TR[i].TD[j] = new TD();
						data.TR[i].TD[j].Text=new string[1];
						data.TR[i].TD[j].Text[0] = Convert.ToString(ds.Tables[k].Rows[i][j]);
					}
				}
				vot.RESOURCE[0].TABLE[k].DATA = new DATA();
				vot.RESOURCE[0].TABLE[k].DATA.Item = data;		
			}
			return vot;
		}

        public  static VOTABLE DataSet2VOTableUCD(DataSet ds, DataSet ucd) 
        {

            VOTABLE vot = new VOTABLE();

            vot.version = VOTABLEVersion.Item11;
            vot.RESOURCE = new RESOURCE[1];
            vot.RESOURCE[0] = new RESOURCE();
            vot.RESOURCE[0].type = RESOURCEType.results;

            int ntbl = ds.Tables.Count;
            vot.RESOURCE[0].TABLE = new TABLE[ntbl];
            vot.INFO = new INFO[ntbl];


            // get name and type of columns
            Hashtable votype = getdataTypeTable();
            for (int k=0; k<ntbl; k++) 
            {
                DataTable dt = ds.Tables[k];
                vot.INFO[k] = new INFO();
                vot.INFO[k].name = "rowcount, table "+k;
                vot.INFO[k].value= ""+dt.Rows.Count;

                vot.RESOURCE[0].TABLE[k] = new TABLE();

                vot.RESOURCE[0].TABLE[k].Items = new object[dt.Columns.Count];
                for (int j=0; j<dt.Columns.Count; j++) 
                {	
                    vot.RESOURCE[0].TABLE[k].Items[j] = new FIELD();
                    ((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).name = dt.Columns[j].ColumnName;
                    try 
                    {
                        ((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).datatype = (dataType) votype[dt.Columns[j].DataType];
                        if (((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).datatype  == dataType.@char) 
                        {
                            ((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).arraysize="*";
                           }
                        foreach (DataRow ucdrow in ucd.Tables[0].Rows)
                        {
                            if (ucdrow[0].Equals(dt.Columns[j].ColumnName))
                            {
                                ((FIELD)vot.RESOURCE[0].TABLE[k].Items[j]).ucd=ucdrow[2].ToString();
                                }
                            }

                        } 
                    catch (Exception e) 
                    {
                        Console.Out.WriteLine(e+":"+e.StackTrace);
                    }
                }
                // load data
                TABLEDATA data = new TABLEDATA();
                data.TR= new TR[dt.Rows.Count];
                for (int i=0; i<data.TR.Length; i++) 
                {
                    data.TR[i]=new TR();
                    data.TR[i].TD = new TD[dt.Columns.Count];
                    for (int j=0; j<data.TR[i].TD.Length; j++) 
                    {
                        data.TR[i].TD[j] = new TD();
                        data.TR[i].TD[j].Text=new string[1];
                        data.TR[i].TD[j].Text[0] = Convert.ToString(ds.Tables[k].Rows[i][j]);
                    }
                }
                vot.RESOURCE[0].TABLE[k].DATA = new DATA();
                vot.RESOURCE[0].TABLE[k].DATA.Item = data;		
            }
            return vot;
        }


		 public static Hashtable getdataTypeTable() 
         {
			if ( FDT_table == null) { 
				FDT_table = new Hashtable();
				FDT_table.Add(typeof(System.Byte),	  dataType.unsignedByte);
				FDT_table.Add(typeof(System.Int16),	  dataType.@short);
				FDT_table.Add(typeof(System.Int32),	  dataType.@int);
				FDT_table.Add(typeof(System.Int64),	  dataType.@long);
				FDT_table.Add(typeof(System.UInt64),  dataType.@long);
				FDT_table.Add(typeof(System.Char),    dataType.@char);
				FDT_table.Add(typeof(System.Single),  dataType.@float);
				FDT_table.Add(typeof(System.Double),  dataType.@double);
				FDT_table.Add(typeof(System.String),  dataType.@char);
				FDT_table.Add(typeof(System.DateTime),dataType.@char);
				
				}
			return FDT_table;
		}
		public static Hashtable getdataTypeTableReverse(){
			Hashtable forward = getdataTypeTable();
			Hashtable backwards = new Hashtable();
			// string is special
			backwards.Add(dataType.@char,typeof(System.String));
			foreach(object o in forward.Keys)
				if(!backwards.ContainsKey(forward[o]))
					backwards.Add(forward[o],o);
			return backwards;
		}

		//uses transaction/coinnection from command
		public static void loadTable (DataTable dt, SqlCommand command) {
			SqlCommand com = new SqlCommand();
			SqlCommand c2 = new SqlCommand();
			
			if(command.Transaction != null){
				com.Transaction = command.Transaction;
				c2.Transaction = command.Transaction;
			}
			com.Connection = command.Connection;
			c2.Connection = command.Connection;
			string qry = "CREATE TABLE "+dt.TableName+" (";
			for(int x=0;x<dt.Columns.Count;x++){
				qry+="["+dt.Columns[x].ColumnName+"] "+GetSqlDBType(dt.Columns[x].DataType).ToString();
				if(x<(dt.Columns.Count-1))
					qry+=",";
			}
			qry+=")";
			com.CommandText = qry;

			
			//try{
				com.ExecuteNonQuery();
			
			//}catch(Exception e){}
			
			qry = "";
			for(int y=0;y<dt.Rows.Count;y++){
				qry += "insert into "+dt.TableName+" values(";
				for(int x=0;x<dt.Columns.Count;x++){
					if (dt.Columns[x].DataType == typeof(string)
					|| dt.Columns[x].DataType == typeof(char[]) || 
						dt.Columns[x].DataType == typeof(DateTime))
					{
						qry+="'"+dt.Rows[y][x].ToString()+"'";
					}
					else 
					{
						if (dt.Rows[y][x] == null || dt.Rows[y][x]== DBNull.Value) 
						{
							qry+="null";
						} 
						else 
						{
							qry+=dt.Rows[y][x].ToString();
						}
					}

					if(x<(dt.Columns.Count-1))
						qry+=",";
				}
				qry+=") ";
				c2.CommandText = qry;
			//throw new Exception(qry);
				c2.ExecuteNonQuery();
				qry = "";
			}
		}


		public static void makeXYZ(DataTable dt) 
		{
			bool isRaDec = false;
			string ra = "ra", dec = "dec";
			foreach(DataColumn dc in dt.Columns)//just look for ra
				if(dc.ColumnName.ToLower().CompareTo(ra) == 0)
					isRaDec = true;
			if(isRaDec){
				string x = "x";
				string y = "y";
				string z = "z";
				double d2r = Math.PI / 180;
				dt.Columns.Add(x,typeof(double));
				dt.Columns.Add(y,typeof(double));
				dt.Columns.Add(z,typeof(double));
				foreach(DataRow dr in dt.Rows)
				{
					
					double r = double.Parse(dr[ra].ToString());
					double d = double.Parse(dr[dec].ToString());
					dr[x] = Math.Cos(d*d2r)*Math.Cos(r*d2r);
					dr[y] = Math.Cos(d*d2r)*Math.Sin(r*d2r);
					dr[z] = Math.Sin(d*d2r);
				}
			}
		}

		public static void loadTableAsXYZ(DataTable dt, SqlCommand command)
		{
			makeXYZ(dt);
			loadTable(dt,command);
		}

		public string type2sql(string dsName) {
			string res = @"";

			switch(dsName) {
				
				default: {
					res = @"UnHandled -> " + dsName ;
					break;
				}

			};

			return res;

		}
		public static SqlDbType GetSqlDBType(Type t){
			switch(t.Name.ToString()){
				case "Int16":
					return SqlDbType.Int;
				case "Int64":
					return SqlDbType.BigInt;
				case "UInt64":
					return SqlDbType.BigInt;
				case "Boolean":
					return SqlDbType.Bit;
				case "Int32":
					return SqlDbType.Int;
				case "Decimal":
					return SqlDbType.Decimal;
				case "Double":
					return SqlDbType.Float;
				case "String":
					return SqlDbType.NText;
				case "Single":
					return SqlDbType.Real;
				case "Byte":
					return SqlDbType.Char;
				case "DateTime":
					return SqlDbType.DateTime;
			}
			throw new Exception("couldn't find type "+t);
		}

	}
