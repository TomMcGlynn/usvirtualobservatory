package org.usvo.openid.ui;

import org.usvo.openid.orm.YesNoBoth;
import org.usvo.openid.util.Compare;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

public class PortalFilter extends TemplatePage {
    private YesNoBoth active, approved;

    private static final String PARAM_ACTIVE = "showActive", PARAM_APPROVED = "showApproved";
    private static final String TAG_ACTIVE = "checkedShowActive", TAG_APPROVED = "checkedShowApproved",
        SUFFIX_ALL = "All", SUFFIX_TRUE = "True", SUFFIX_FALSE = "False";

    public PortalFilter(HttpServletRequest request) throws IOException {
        super(TemplateTags.SNIPPET_PORTAL_FILTER);
        active = parse(request, PARAM_ACTIVE);
        approved = parse(request, PARAM_APPROVED);
    }

    private YesNoBoth parse(HttpServletRequest request, String param) {
        String val = request.getParameter(param);
        String sessionKey = getClass().getSimpleName() + "." + param;
        if (Compare.isBlank(val)) {
            Object oVal = request.getSession().getAttribute(sessionKey);
            if (oVal != null)
                val = oVal.toString();
        }
        YesNoBoth result = YesNoBoth.parse(val, YesNoBoth.BOTH);
        request.getSession().setAttribute(sessionKey, result.toString());
        return result;
    }

    /** Show active portals?
     *  @return null: show all - TRUE: only active - FALSE: only inactive */
    public YesNoBoth getActive() { return active; }

    /** Show approved portals?
     *  @return null: show all - TRUE: only approved - FALSE: only unapproved */
    public YesNoBoth getApproved() { return approved; }

    public String toString() {
        Map<String, String> map = new HashMap<String, String>();
        setBoolean(map, TAG_ACTIVE, active);
        setBoolean(map, TAG_APPROVED, approved);
        return substitute(map);
    }

    private void setBoolean(Map<String, String> map, String tag, YesNoBoth val) {
        map.put(tag + SUFFIX_TRUE, YesNoBoth.YES == val ? "checked" : "");
        map.put(tag + SUFFIX_FALSE, YesNoBoth.NO == val ? "checked" : "");
        map.put(tag + SUFFIX_ALL, YesNoBoth.BOTH == val ? "checked" : "");
    }
}
