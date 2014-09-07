package org.usvao.sso.client;

import java.util.*;
import java.io.*;
import java.net.URL;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.openid.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.cert.X509Certificate;

public class VAOOpenIDAuthenticationSuccessHandler
              extends SavedRequestAwareAuthenticationSuccessHandler {

    private String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }


    public void onAuthenticationSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication)
                             throws javax.servlet.ServletException,
                                    java.io.IOException {
        super.onAuthenticationSuccess(request, response, authentication);
        String credential = VAOLogin.getCredentialURL();
        if (credential != null && credential.startsWith("https://")) {

            URL credentialURL = new URL(credential);
/*
            String cred = convertStreamToString(credentialURL.openStream());
*/
            String cred = "";
            String eol = System.getProperty("line.separator");
            BufferedReader in = new BufferedReader(
                         new InputStreamReader(credentialURL.openStream()));

            PrintStream out = new PrintStream(new ByteArrayOutputStream());

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                cred = cred + inputLine + eol;
            }
            in.close();
            out.close();

            List<OpenIDAttribute> attributes = VAOLogin.getAttributes();

            List<String> values = new ArrayList<String>();
            values.add(cred);
            OpenIDAttribute attrib = new OpenIDAttribute("RAWCREDENTIAL", "RAWCREDENTIAL", values);
            attributes.add(attrib);

            try {
                X509Certificate cert = X509Certificate.getInstance(
                                new ByteArrayInputStream(cred.getBytes()));
                System.out.println("SUBJECT: " + cert.getSubjectDN());
            } catch (javax.security.cert.CertificateException e) {
                // TODO: handle this approp.
            }
         }

    }
}
