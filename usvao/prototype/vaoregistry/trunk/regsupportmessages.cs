
namespace managereg
{
    using System.Xml.Serialization;

    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(TypeName = "User", Namespace = "http://www.stsci.edu/registrysupport")]
    public class User
    {
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("name")]
        public string name;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("email")]
        public string email;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("username")]
        public string username;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("password")]
        public string password;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("confirmpassword")]
        public string confirmpassword;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("authority")]
        public string associatedauthority;
    }

    [System.Xml.Serialization.XmlTypeAttribute(TypeName = "ResourceList", Namespace = "http://www.stsci.edu/registrysupport")]
    public class ResourceList
    {
        [System.Xml.Serialization.XmlElementAttribute()]
        public RegistryResponse Response;

        /// <remarks/>
        [System.Xml.Serialization.XmlArrayItemAttribute(IsNullable = false)]
        public ResourceInfo[] Records;

    }

    [System.Xml.Serialization.XmlTypeAttribute(TypeName = "ManagingOrgList", Namespace = "http://www.stsci.edu/registrysupport")]
    public class ManagingOrgList
    {
        [System.Xml.Serialization.XmlElementAttribute()]
        public RegistryResponse Response;

        /// <remarks/>
        [System.Xml.Serialization.XmlArrayItemAttribute(IsNullable = false)]
        public string[] Records;
    }

    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(TypeName = "ResourceInfo", Namespace = "http://www.stsci.edu/registrysupport")]
    public class ResourceInfo
    {
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("title")]
        public string title;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("ivoId")]
        public string ivoId;

        public ResourceInfo(string Title, string Id)
        {
            title = Title; ivoId = Id;
        }


        public ResourceInfo()
        {
            title = ivoId = string.Empty;
        }
    }

    /// <remarks/>
    [System.Xml.Serialization.XmlTypeAttribute(TypeName = "RegistryResponse", Namespace = "http://www.stsci.edu/registrysupport")]
    public class RegistryResponse
    {
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("message")]
        public string message;

        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("returncode")]
        public int returncode;

        public RegistryResponse() { returncode = -1; message = string.Empty; }

        public RegistryResponse(int Code, string Message)
        {
            message = Message;
            returncode = Code;
        }
    }
}
