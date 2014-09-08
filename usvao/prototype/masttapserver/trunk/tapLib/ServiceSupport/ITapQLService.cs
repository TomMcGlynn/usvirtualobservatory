using System;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Xml.Linq;

namespace tapLib.ServiceSupport
{
    [ServiceContract]
    interface ITapQLService {

        [OperationContract]
        [WebGet(ResponseFormat = WebMessageFormat.Xml, BodyStyle = WebMessageBodyStyle.Bare)]
        XElement paramquery(String REQUEST, String VERSION, String DIAG);
    }
}