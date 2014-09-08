/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.vospace;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;

import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.oauth.MySQLOAuthProvider;

/** A base class for VOSpace servlets that need to do error handling & redirection. */
public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = -3479515785847664821L;

	/** What page should we send the user to in case of an error?  For many, this will be "/authorize.jsp".
     *  If blank (null or ""), a very simple error page will be created. */
    public abstract String getErrorPage();
    
    private static final Logger logger = Logger.getLogger(BaseServlet.class);

    /** Show an error to the user. Does not log it, though -- assumes it is already logged, if appropriate. */
    public void handleError(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException, ServletException {
    	logger.debug("Handle Error");

        if (!isBlank(error))
            request.setAttribute("ERROR", error);
        if (!isBlank(getErrorPage()))
            request.getRequestDispatcher(getErrorPage()).forward(request, response);
        // Fall back to displaying the error in its own page -- kind of primitive, with no recourse
        // for the user.  Maybe forward to a full dedicated error page?
        else {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println("<html><title>Error - " + error + "</title>");
            response.getWriter().println("<body><h1>Error</h1>");
            response.getWriter().println("<p>" + error + "</p>");
            response.getWriter().println("</body></html>");
        }
    }

    /** Fetch the current OAuth access token from the database. */
    public OAuthAccessor getAccessor(HttpServletRequest request)
            throws IOException, OAuthProblemException
    {
        OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
        return MySQLOAuthProvider.getAccessor(requestMessage.getToken());
    }

    public static boolean isBlank(String s) { return s == null || s.trim().length() == 0; }
}
