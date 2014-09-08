/**
 * MyProxyLogon.java
 * Author: Matthew Graham (Caltech)
 * Version: Original (0.1) - 7 August 2006
 */

import org.globus.myproxy.*;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import java.security.cert.X509Certificate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * This class retrieves a proxy certificate from the NVO MyProxy server at 
 * NCSA and saves to the specified local file.
 */

public class MyProxyLogon {

    public static void main(String[] args) {

	boolean getCert = true;
	boolean noPass = false;
	String SERVER = null;
	String USER = null;
	String PASSPHRASE = null;
	String PATH = null;
	String LOCATION = null;
	String CREDNAME = null;
	String LIFETIME = null;

	// Parse arguments
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-s")) {
	        SERVER = args[i + 1];
	    } else if (args[i].equals("-l")) {
		USER = args[i + 1];
	    } else if (args[i].equals("-o")) {
	        PATH = args[i + 1];
	    } else if (args[i].equals("-k")) {
		CREDNAME = args[i + 1];
	    } else if (args[i].equals("-t")) {
		LIFETIME = args[i + 1];
	    } else if (args[i].equals("-c")) {
		LIFETIME = args[i + 1];
		getCert = false;
	    } else if (args[i].equals("-n")) {
		noPass = true;
	    } else if (args[i].equals("-in")) {
	        LOCATION = args[i + 1];
	    }
	}

	try {
	    if (!noPass) {
                // Read passphrase
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                PASSPHRASE = br.readLine();
	    }
            // Set up MyProxy client
	    System.out.println("Server = " + SERVER);
            org.globus.myproxy.MyProxy mp = new org.globus.myproxy.MyProxy(SERVER, 7512);
	    if (getCert) {
                // Get proxy certificate
	        GetParams params = new GetParams(USER, PASSPHRASE);
	        if (CREDNAME != null) params.setCredentialName(CREDNAME);
	        if (LIFETIME != null) params.setLifetime(Integer.parseInt(LIFETIME));
		System.out.println("Params = " + params);
   	        GSSCredential proxyCert = mp.get(null, params);
	        // Write out proxy certificate
 	        FileOutputStream certout = new FileOutputStream(new File(PATH));
	        byte[] certData = ((ExtendedGSSCredential) proxyCert).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
	        certout.write(certData);
	    } else {
		// Read in proxy certificate
		File cert = new File(LOCATION);	
		FileInputStream certin = new FileInputStream(new File(LOCATION));
		byte[] certData = new byte[(int) cert.length()];
		certin.read(certData);
		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
		GSSCredential proxyCert = manager.createCredential(certData, ExtendedGSSCredential.IMPEXP_OPAQUE, GSSCredential.INDEFINITE_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
		mp.put(proxyCert, USER, null, Integer.parseInt(LIFETIME));
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }    
}
