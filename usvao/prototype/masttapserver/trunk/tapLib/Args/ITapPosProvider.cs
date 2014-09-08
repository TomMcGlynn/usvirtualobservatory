using System;
using System.Collections.Generic;

namespace tapLib.Args {
    /// <summary>
    /// This interface describes a software component that should
    /// provide the services with a list of TapPos instances stored
    /// in a VOSpace, Upload, or other database table in a site
    /// specific way.
    /// Protocol:  Each time the service is called, a new instance
    /// of the provider is expected.  The provide method is called with
    /// a parsed POS argument.  Methods of TapPosArg can be used to 
    /// determine details of the source of TapPos instances.
    /// The provide method is called.
    /// If the provider is successful, the object is used to iterate 
    /// over the resultant set of Strings in Tap POS format (ITapPosProvider extends
    /// IEnumerable<String>.
    /// If there is a problem, isValid should be false and a problem
    /// string is available to return to the user.
    /// </summary>
    public interface ITapPosProvider : IEnumerable<String> {

        // Properties
        // Is the provide successful?
        Boolean isValid { get; }
        // If not successful, what is the error?
        String problem { get; }        

        // This method attempts to load TapPos objects from the 
        // location in the TapPosArg.
        Boolean provide(TapPosArg arg);        
    }

    public interface ITapPosProviderFactory {

        ITapPosProvider create(TapPosArg arg);

    }
}