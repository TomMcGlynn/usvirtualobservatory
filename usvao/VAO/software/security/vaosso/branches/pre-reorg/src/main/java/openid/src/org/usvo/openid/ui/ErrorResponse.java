package org.usvo.openid.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/** Report an error in a web page. */
public class ErrorResponse {
    public static void reportError(String message, int code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TemplateTags.TAG_FEEDBACK, message);
        response.setStatus(code);
        TemplatePage.display(request, response, TemplateTags.PAGE_ERROR, map);
    }
}
