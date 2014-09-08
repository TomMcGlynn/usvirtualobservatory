using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Collections.Generic;
using JsonFx.Json;
using log4net;

namespace Mashup
{
    public class Histogram
    {
		// For logging...
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }

		private Dictionary<string, Object> dict;

        private class HistogramResult
        {
            public string key;
            public int count;
        }

        public Histogram(DataTable dt)
        {
            dict = new Dictionary<string, Object>();
            foreach (DataColumn column in dt.Columns)
            {
                if (dt.Rows.Count == 1) continue;
                string facetRule = "default";
                string rule = "cc.autoFacetRule";
                Type t = column.DataType;
                //string unit = (column.ExtendedProperties.ContainsKey("vot.unit") ? column.ExtendedProperties["vot.unit"].ToString() : null);
                string ucd =  (column.ExtendedProperties.ContainsKey("vot.ucd") ? column.ExtendedProperties["vot.ucd"].ToString() : null);
                bool isMjd = (ucd == "VOX:Image_MJDateObs");
                if (isMjd && !column.ExtendedProperties.ContainsKey("isMjd"))
                {
                    column.ExtendedProperties.Add("isMjd", true);
                }
                /*
                double dayVal = 0.0;
                bool realVal = ((t.Name == "Double") || (t.Name == "Single"));
                bool mjdUnit = ((unit == "d") || (unit == "mjd") || (unit == "y"));
                if (realVal && mjdUnit)
                {
                    dayVal = (double)dt.Rows[0][column];
                }
                bool isMjd = (realVal && mjdUnit && (dayVal > 2500.0));
                if (isMjd && !column.ExtendedProperties.ContainsKey("isMjd"))
                {
                    column.ExtendedProperties.Add("isMjd", true);
                }
                */
                bool isDate = (t.Name == "DateTime");
                if (isDate && !column.ExtendedProperties.ContainsKey("isDate"))
                {
                    column.ExtendedProperties.Add("isDate", true);
                }
                if (column.ExtendedProperties.ContainsKey(rule))
                {
                    facetRule = column.ExtendedProperties[rule].ToString();
                    if (!isDate && (facetRule == "never")) continue;
                }
                bool remove = false;
                if (column.ExtendedProperties.ContainsKey("cc.remove"))
                {
                    var ccr = column.ExtendedProperties["cc.remove"];
                    if (ccr != null) remove = (bool) ccr;
                }
                if (remove) continue;
                bool treatNumeric = false;
                if (column.ExtendedProperties.ContainsKey("cc.treatNumeric"))
                {
                    treatNumeric = (bool) column.ExtendedProperties["cc.treatNumeric"];
                }
                string type = column.DataType.Name;

				// For now, we don't treat int32's or int64's as numbers by default.  They get treated
				// as numbers if the treatNumeric columnsConfig is set to true.  
				// Ideally we would probably want to always treat int64's as numbers, but that causes
				// issues on the JavaScript size, since it can't accurately represent longs greater than 
				// about 10^16.
				if ((type == "Double") || (type == "Single") || isDate || treatNumeric || isMjd)
                {
                    Dictionary<string, Object> d = DecimalHistogram(dt, column, 100, isDate);
                    if (d != null) dict[column.ColumnName] = d;
                }
                else
                {
                    string separator = (column.ExtendedProperties.ContainsKey("cc.separator")) ? (string)column.ExtendedProperties["cc.separator"] : "";
                    string separatorType = (column.ExtendedProperties.ContainsKey("cc.separatorType")) ? (string)column.ExtendedProperties["cc.separatorType"] : "";
                    Dictionary<Object, int> d = DiscreteHistogram(dt, column, separator, separatorType);
                    /*if (d.Count < 51) */dict[column.ColumnName] = DictionaryToHistogram(d, column);
                }
            }

			// Ensure all Field modifications made in DecimalHistogram() are permanent.
			dt.AcceptChanges();
        }

        public Dictionary<string, Object> getHistogram()
        {
            return dict;
        }

        public static Dictionary<string, Object> DictionaryToHistogram(Dictionary<Object, int> d, DataColumn column)
        {
            ArrayList a = new ArrayList();
            Dictionary<string, Object> dictionary = new Dictionary<string, object>();
            
            foreach (KeyValuePair<Object, int> pair in d)
            {
                HistogramResult hr = new HistogramResult();
                hr.key = pair.Key.ToString();
                hr.count = pair.Value;
                a.Add(hr);
            }
            dictionary.Add("hist", a);
            dictionary.Add("type", "discrete");
            return dictionary;
        }

        private static DateTime EpochDate = new DateTime(1970, 1, 1);
        private static long EpochTicks = EpochDate.Ticks;

        public static Dictionary<Object, int> DiscreteHistogram(DataTable dt, DataColumn column, string separator, string separatorType)
        {
            Dictionary<Object, int> dict = new Dictionary<Object, int>();
            foreach (DataRow row in dt.Rows)
            {
                var value = row[column];
                if (separator != "")
                {
                    string v = "";
                    try
                    {
                        v = (string)value;
                    }
                    catch (Exception)    // skip failed casts
                    {
                        continue;
                    }
                    string[] a = v.Split(separator.ToCharArray());
                    foreach (string key in a)
                    {
                        if (key == "") continue;
                        if (dict.ContainsKey(key))
                        {
                            dict[key]++;
                        }
                        else
                        {
                            dict[key] = 1;
                        }
                    }
                }
                else
                {
                    if (dict.ContainsKey(value))
                    {
                        dict[value]++;
                    }
                    else
                    {
                        dict[value] = 1;
                    }
                }
            }
            return dict;
        }

        public static Dictionary<string, Object> DecimalHistogram(DataTable dt, DataColumn column, int totalBuckets, bool isDate)
        {
			string coltype = column.DataType.Name;
			double dmin = double.MaxValue;
            double dmax = double.MinValue;
            var buckets = new ArrayList(100);
            double ignoreVal = dmin;
            if (column.ExtendedProperties.ContainsKey("cc.ignoreValue")) ignoreVal = System.Convert.ToDouble(column.ExtendedProperties["cc.ignoreValue"]);

            foreach (DataRow row in dt.Rows)
            {   // find the bounds of the histogram
                var obj = row[column];
                string type = obj.GetType().Name;
                if (type == "DBNull")
                {
                    if (!column.ExtendedProperties.ContainsKey("cc.ignoreValue"))
                    {
                        ignoreVal = isDate ? 0 : -9999.0;
                        column.ExtendedProperties["cc.ignoreValue"] = ignoreVal;
                    }
                    else
                    {
						// Turn off all validation and events for this row to improve performance
						row.BeginEdit();
                    }

                    if (isDate)
                    {
                        row[column] = EpochDate;
                    }
                    else
                    {
                        row[column] = ignoreVal;
                    }
                    continue;
                }
                if (isDate) obj = (((DateTime)obj).ToUniversalTime().Ticks - EpochTicks) / 10000;

				double val = convertObjToDouble(obj, ignoreVal);
                if (val != ignoreVal)
                {
                    dmin = Math.Min(dmin, val);
                    dmax = Math.Max(dmax, val);
                }
            }

            if ((dmax == double.MinValue) || (dmin == double.MaxValue) || (dmax == dmin)) return null;

            var bucketSize = (dmax - dmin) / totalBuckets;
            for (var i = 0; i < totalBuckets; i++)
            {    // initialize bucket array
                HistogramResult hr = new HistogramResult();
                hr.key = (dmin + i * bucketSize).ToString();
                hr.count = 0;
                buckets.Add(hr);
            }

            foreach (DataRow row in dt.Rows)
            {   // populate bucket counts
                int bucketIndex = 0;
                var obj = row[column];
                string type = obj.GetType().Name;
                if (type == "DBNull") continue;
                if (isDate) obj = (((DateTime)obj).ToUniversalTime().Ticks - EpochTicks) / 10000;
				double val = convertObjToDouble(obj, ignoreVal);
                if (val == ignoreVal) continue;
                if (bucketSize > 0.0)
                {
                    bucketIndex = (int)((val - dmin) / bucketSize);
                    if (bucketIndex == totalBuckets) bucketIndex--;
                }
                if (isDate && (bucketIndex >= 0))
                // handles the case where a null date created an ignoreValue of Jan 1 epoch, which falls outside the range of non-null dates
                {
                    ((HistogramResult)buckets[bucketIndex]).count++;
                }
            }

            int maxCount = 0;
            foreach (HistogramResult hr in buckets)
            {   // find the most populated bucket (histogram y-height)
                if (hr.count > maxCount) maxCount = hr.count;
            }

			// Down-convert min and max if needed, so that we don't have extra precision.
			Object min = dmin;
			Object max = dmax;
			if (coltype == "Single") {
				min = System.Convert.ToSingle(dmin);
				max = System.Convert.ToSingle (dmax);
			} else if (coltype == "Int32") {
				min = System.Convert.ToInt32(dmin);
				max = System.Convert.ToInt32 (dmax);
			} else if (coltype == "Int64") {
				min = System.Convert.ToInt64(dmin);
				max = System.Convert.ToInt64(dmax);
			}

            // build return object
            Dictionary<string, Object> dictionary = new Dictionary<string, object>();
            dictionary.Add("type", "numeric");
            dictionary.Add("min", min);
            dictionary.Add("max", max);
            Dictionary<string, Object> d = new Dictionary<string, object>();
            d.Add("hist", buckets);
            d.Add("max", maxCount);
            dictionary.Add("hist", d);
            return dictionary;
        }

		private static double convertObjToDouble(object obj, double ignoreVal) {
			// Try to convert the value to a double.  If it doesn't convert, set it to the ignore value.
			double val;
			try {
				val = Convert.ToDouble(obj);
			}                     
			catch (FormatException) {
				//log.Warn("The " + obj.GetType().Name + " value " + obj + " is not recognized as a valid Double value.");
				val = ignoreVal;
			}                     
			catch (InvalidCastException) {
				//log.Warn("Conversion of the " + obj.GetType().Name + " value " + obj + " to a Double is not supported.");
				val = ignoreVal;
			} 
			catch (OverflowException) {
				//log.Warn("The " + obj.GetType().Name + " value " + obj + " is outside the bounds of a valid Double value.");
				val = ignoreVal;
			}
			return val;
		}
    }
}