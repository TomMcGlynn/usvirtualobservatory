using System;
using System.IO;
using System.Text;
using System.Data;
using System.Collections;
using System.Collections.Generic;

namespace VOTLib
{
	public class VOTType
	{
		// Not meant to be instantiated.  
		private VOTType ()
		{
		}
		
		// Types
		public static readonly Type DS_STRING = Type.GetType ("System.String");
		public static readonly Type DS_BOOLEAN = Type.GetType ("System.Boolean");
		public static readonly Type DS_INT16 = Type.GetType ("System.Int16");
		public static readonly Type DS_INT32 = Type.GetType ("System.Int32");
		public static readonly Type DS_INT64 = Type.GetType ("System.Int64");
		public static readonly Type DS_SINGLE = Type.GetType ("System.Single");
		public static readonly Type DS_DOUBLE = Type.GetType ("System.Double");
		
		private static Dictionary<string, Type> systemTypeMapping = null;
		public static Dictionary<string, Type> SystemTypeMapping
		{
			get { 
				lock(typeof(VOTType))
				{
					if (systemTypeMapping == null)
					{
						createMappings();	
					}
				}
				return systemTypeMapping;
			} 
		}
		
		private static Dictionary<Type, string> votTypeMapping = null;
		public static Dictionary<Type, string> VotTypeMapping
		{
			get { 
				lock(typeof(VOTType))
				{
					if (votTypeMapping == null)
					{
						createMappings();
					}
				}
				return votTypeMapping; 
			}
		}
		
		private static void createMappings()
		{
			systemTypeMapping = new Dictionary<string, System.Type> ();
			votTypeMapping = new Dictionary<System.Type, string> ();
			
			systemTypeMapping.Add ("char", DS_STRING);			votTypeMapping.Add (DS_STRING, "char");
			systemTypeMapping.Add ("boolean", DS_BOOLEAN);		votTypeMapping.Add (DS_BOOLEAN, "boolean");
			systemTypeMapping.Add ("short", DS_INT16);			votTypeMapping.Add (DS_INT16, "short");
			systemTypeMapping.Add ("int", DS_INT32);			votTypeMapping.Add (DS_INT32,"int");

			systemTypeMapping.Add ("long", DS_INT64);			votTypeMapping.Add (DS_INT64, "long");
			systemTypeMapping.Add ("float", DS_SINGLE);			votTypeMapping.Add (DS_SINGLE,"float");
			systemTypeMapping.Add ("double", DS_DOUBLE);		votTypeMapping.Add (DS_DOUBLE, "double");
			
			// NOTE: These two are not in the Vo Table spec, but are seen in the Galex cone search.
			systemTypeMapping.Add ("int16", DS_INT16);	
			systemTypeMapping.Add ("int32", DS_INT32);
		}
	}
}