using System;
namespace VOTLib
{
	public static class Tags
	{
		public const string VOTABLE = "VOTABLE";
		public const string DESCRIPTION = "DESCRIPTION";
		public const string DEFINITIONS = "DEFINITIONS";
		public const string COOSYS = "COOSYS";
		public const string INFO = "INFO";
		public const string PARAM = "PARAM";
		public const string PARAMref = "PARAMref";
		public const string GROUP = "GROUP";
		public const string RESOURCE = "RESOURCE";
		public const string LINK = "LINK";
		public const string FIELD = "FIELD";
		public const string FIELDref = "FIELDref";
		public const string TABLE = "TABLE";
		public const string DATA = "DATA";
		public const string TABLEDATA = "TABLEDATA";
		public const string BINARY = "BINARY";
		public const string FITS = "FITS";
		public const string STREAM = "STREAM";
		public const string TR = "TR";
		public const string TD = "TD";
		public const string VALUES = "VALUES";
		public const string MIN = "MIN";
		public const string MAX = "MAX";
		public const string OPTION = "OPTION";

		// These are not tags, but instead a keys we can use for 
		// stashing element metadata in data structures.
		public const string TAG_ATTR = "__tag__";
		public const string ID_ATTR = "__eltId__";
		public const string PARENT_ID_ATTR = "__parentId__";
		public const string CONTENT_ATTR = "content";
		public const string LITERAL_CONTENT_ATTR = "literalContent";
		public const string VOT_METADATA = "votMetadata";

	}
}

