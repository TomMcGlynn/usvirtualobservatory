package org.usvo.openid.serve;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.JdbcServerAssociationStore;
import org.openid4java.server.ServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.usvo.openid.Conf;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/** Top-level management of OpenID login process. One instance per Thread / ServletContext.
 *  Based on org.openid4java.server.SampleServer.  Most work is delegated to {@link IdRequest}, which
 *  is one instance per HttpServletRequest. */
public class IdServer {
    private static final Log log = LogFactory.getLog(IdServer.class);

    private ServerManager manager = new ServerManager();

    /** Either create or retrieve the IdServer for this servlet instance.
     *  @param context this servlet's context, used to look up configuration */
    public static IdServer getInstance(ServletContext context) throws IOException {
        IdServer result = (IdServer) context.getAttribute("id server");
        if (result == null) {
            result = new IdServer();
            context.setAttribute("id server", result);
        }
        return result;
    }

    /** Construct a new IdServer. */
    private IdServer() throws IOException { init(); }

    /** Handle a login or confirmation request. */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws OpenIDException, IOException
    {
        createIdRequest(request, response).handleOpenidRequest();
    }

    /** Bypass OpenID machinery to service an authentication request.  If successful, set up a session cookie.
     *  Check for either an existing valid session cookie or form-based authentication originating with LoginUI. */
    public AuthnAttempt authenticate(HttpServletRequest request, HttpServletResponse response)
            throws OpenIDException
    {
        return createIdRequest(request, response).authenticate();
    }

    private IdRequest createIdRequest(HttpServletRequest request, HttpServletResponse response)
            throws OpenIDException
    {
        return new IdRequest(manager, request, response);
    }

    /** Set up this server. */
    private void init() throws IOException {
        manager.setOPEndpointUrl(Conf.get().getBaseUrl() + "/provider");

        // TODO: enforce RP realm discovery
        // for a working demo, don't enforce RP realm discovery, since it is not deployed
        // manager.getRealmVerifier().setEnforceRpId(false);

        ServerAssociationStore sharedAssociations, privateAssociations;
        // assocations from stateless RPs don't need to be shared anyway
        // VSY: Actually they should be shared as well with the mirror
        Map<String, String> conf = Conf.get().read();
        if ("true".equalsIgnoreCase(conf.get(Conf.KEY_DB_ASSOCIATIONS))) {
            // share stateful associations via a database table (which can be synchronized with mirror server if necessary)
            MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
            ds.setUser(conf.get(Conf.KEY_DB_USERNAME));
            ds.setPassword(conf.get(Conf.KEY_DB_PW));
            ds.setUrl(conf.get(Conf.KEY_DB_URL));
            JdbcServerAssociationStore sharedJdbc = new JdbcServerAssociationStore
                    (Conf.TABLE_OPENID_ASSOCIATION);
            sharedJdbc.setDataSource(ds);
            sharedAssociations = sharedJdbc;
            // VSY: use same JDBC connection for privateAssociations
            // since they need to be shared with the mirror as well.
            privateAssociations = sharedJdbc;
        } else {
            // keep shared associations in memory
            sharedAssociations = new InMemoryServerAssociationStore();
            privateAssociations = new InMemoryServerAssociationStore();
        }

        manager.setSharedAssociations(sharedAssociations);
        manager.setPrivateAssociations(privateAssociations);
    }
}
