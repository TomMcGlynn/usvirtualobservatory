
/*
 * EsoSsapServlet.java
 * $ID*
 */

/**
 *
 * @author M.sierra
 */

package eso;


import dalserver.*;
//import java.io.IOException;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;


/**
 * HTTP servlet implementing a proxy SSAP service for the ESO spectrum
 * services.  
 */
public class EsoSsapServlet extends SsapServlet {

    /**
     * Get a new instance of the EsoSsapService proxy service.
     * 
     * 
     * @param params	Service parameter set.
     */
    public SsapService newSsapService(SsapParamSet params) {
        return ((SsapService) new EsoSsapService(params));
    }
    
}    
    // Get the address to construct base url in EsoSsapService
//    public static String dataAddr;
//    public void doGet(HttpServletRequest request, HttpServletResponse response)
//        throws ServletException, IOException {
//        dataAddr = request.getScheme() + "://" + request.getServerName();
//        super.doGet(request, response);
//     }    
//}


