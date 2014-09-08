using System;
using System.Collections.Generic;
using System.Text;

using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Collections;

using net.ivoa.VOTable;


namespace tapLib.Config
{
    public class VOTableConfig
    {
        private VOTABLE catalogConfig;

        public VOTableConfig()
        {
            catalogConfig = new VOTABLE();
        }

        public bool LoadConfigFile(string filename) {
            bool success = false;
            try
            {
                //XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
                //ns.Add("", "http://www.ivoa.net/xml/VOTable/v1.1");

                XmlTextReader xmlReader = new XmlTextReader(filename);
                XmlSerializer xs = new XmlSerializer(typeof(VOTABLE), "http://www.ivoa.net/xml/VOTable/v1.1");
                catalogConfig = (VOTABLE)xs.Deserialize(xmlReader);
                xmlReader.Close();

                success = true;
            }
            catch (Exception ex)
            {
                Console.WriteLine("Load of config file: " + filename);
                Console.WriteLine("Failed with exception: " + ex);
                throw new FileLoadException("Config file load failed: " + ex);
            }

            return success;
        }
    }
}
