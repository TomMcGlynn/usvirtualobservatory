namespace net.ivoa
{
	using System.Xml.Serialization;

	/// <remarks/>
	[System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.ivoa.net/xml/SkyNode/v0.1")]
	[System.Xml.Serialization.XmlRootAttribute("SkyNode", Namespace="http://www.ivoa.net/xml/SkyNode/v0.1", IsNullable=false)]
	public class SkyNodeType : CapabilityType 
	{
        
		/// <remarks/>
		public string Compliance;
		public double  Latitude;		// geographic location for skynode 
		public double  Longitude;
		public long MaxRecords;
		public string PrimaryTable;
		public string PrimaryKey;
        
	}

}

    