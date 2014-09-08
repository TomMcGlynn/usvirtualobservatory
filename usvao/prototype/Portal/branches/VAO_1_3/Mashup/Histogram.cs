using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Collections.Generic;
using JsonFx.Json;

namespace Mashup
{
    public class Histogram
    {
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
                if ((type == "Double") || (type == "Single") || isDate || treatNumeric)
                {
                    Dictionary<string, Object> d = DecimalHistogram(dt, column, 100, isDate);
                    if (d != null) dict[column.ColumnName] = d;
                }
                else
                {
                    string separator = (column.ExtendedProperties.ContainsKey("cc.separator")) ? (string)column.ExtendedProperties["cc.separator"] : "";
                    string separatorType = (column.ExtendedProperties.ContainsKey("cc.separatorType")) ? (string)column.ExtendedProperties["cc.separatorType"] : "";
                    Dictionary<Object, int> d = DiscreteHistogram(dt, column, separator, separatorType);
                    if (d.Count < 51) dict[column.ColumnName] = DictionaryToHistogram(d, column);
                }
            }
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

        private static long EpochTicks = new DateTime(1970, 1, 1).Ticks;

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
            double ignore = dmin;
            if (column.ExtendedProperties.ContainsKey("cc.ignoreValue")) ignore = System.Convert.ToDouble(column.ExtendedProperties["cc.ignoreValue"]);

            foreach (DataRow row in dt.Rows)
            {   // find the bounds of the histogram
                var obj = row[column];
                string type = obj.GetType().Name;
                if (type == "DBNull") continue;
                if (isDate) obj = (((DateTime)obj).ToUniversalTime().Ticks - EpochTicks) / 10000;
                Double val = (isDate || (type == "Single") || (type == "Int32")) ? System.Convert.ToDouble(obj): val = (Double) obj;
                if (val != ignore)
                {
                    dmin = Math.Min(dmin, val);
                    dmax = Math.Max(dmax, val);
                }
            }
            if (dmax == dmin) return null;

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
                Double val = (isDate || (type == "Single") || (type == "Int32")) ? System.Convert.ToDouble(obj) : val = (Double)obj;
                if (bucketSize > 0.0)
                {
                    bucketIndex = (int)((val - dmin) / bucketSize);
                    if (bucketIndex == totalBuckets) bucketIndex--;
                }
                ((HistogramResult) buckets[bucketIndex]).count++;
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
    }
}