package org.usvo.openid.serve;

public class OpenIdQuery {

    public enum AssocType {
        HMAC_SHA1("HMAC_SHA1"),
        HMAC_SHA256("HMAC-SHA256");

        AssocType(String label) { _name = label; }
        private String _name = null;
        @Override public String toString() { return _name; }
        public static String ParamName = "assoc_type";
        public static String toParam(AssocType type) { 
            return makeParam(ParamName, type.toString());
        }
    }
    public enum SessionType {
        NO_ENCRYPT("no-encryption"),
        DH_SHA1("DH-SHA1"),
        DH_SHA256("DH-SHA256");

        SessionType(String label) { _name = label; }
        private String _name = null;
        @Override public String toString() { return _name; }
        public static String ParamName = "session_type";
        public static String toParam(SessionType type) { 
            return makeParam(ParamName, type.toString());
        }
    }
    public enum Mode {
        ASSOC("associate"),
        CHECKID_IMMEDIATE("checkid_immediate"),
        CHECKID_SETUP("checkid_setup"),
        CHECK_AUTHENTICATION("check_authentication");

        Mode(String label) { _name = label; }
        private String _name = null;
        @Override public String toString() { return _name; }
        public static String ParamName = "mode";
        public static String toParam(Mode mode) { 
            return makeParam(ParamName, mode.toString());
        }
    }

    public final static String PARAM_PREFIX = "openid.";
    public final static String PARAM_AX_PREFIX = "openid.ax.";
    public final static String NS = "http://specs.openid.net/auth/2.0";
    public final static String AX_NS = "http://openid.net/srv/ax/1.0";
    public final static String NS_PARAM = PARAM_PREFIX + "ns=" + NS;
    public final static String AX_NS_PARAM = PARAM_PREFIX + "ns.ax=" + AX_NS;
    public final static String IDENTIFIER_SELECT = 
        "http://specs.openid.net/auth/2.0/identifier_select";
    public final static String FETCH_MODE = makeAxParam("mode","fetch_request");

    public static String associate() {return associate(AssocType.HMAC_SHA256);}
    public static String associate(AssocType type) {
        StringBuilder sb = new StringBuilder(NS_PARAM);
        sb.append('&').append(Mode.toParam(Mode.ASSOC));
        sb.append('&').append(AssocType.toParam(type));
        sb.append('&').append(SessionType.toParam(SessionType.NO_ENCRYPT));
        return sb.toString();
    }

    public static String checkid_immediate(String returnurl) { 
        return checkid_immediate(returnurl, null); 
    }
    public static String checkid_immediate(String returnurl, String username) {
        return checkid(returnurl, username, true); 
    }

    public static String checkid_setup(String returnurl) { 
        return checkid_setup(returnurl, null); 
    }
    public static String checkid_setup(String returnurl, String username) {
        return checkid(returnurl, username, false);
    }
    static String checkid(String returnurl, String username, boolean immediate){
        String oid = (username == null) ? IDENTIFIER_SELECT 
            : "https://sso.usvao.org/openid/id/"+username;
             
        StringBuilder sb = new StringBuilder(NS_PARAM);
        sb.append('&').append(Mode.toParam((immediate) ? Mode.CHECKID_IMMEDIATE
                                                       : Mode.CHECKID_SETUP));
        sb.append('&').append(makeParam("identity", oid));
        sb.append('&').append(makeParam("claimed_id", oid));
        sb.append('&').append(makeParam("return_to", returnurl));
        return sb.toString();
    }

    public static String appendAxFetch(String qstring, String[] atts) {
        StringBuilder sb = new StringBuilder(qstring);
        sb.append('&').append(AX_NS_PARAM)
          .append('&').append(FETCH_MODE);
        for(int i=0; i < atts.length; i++) {
            sb.append('&').append(PARAM_AX_PREFIX).append("type.").append(atts[0])
              .append("=").append("http://sso.usvao.org/schema/")
              .append(atts[0]);
        }
        sb.append('&').append(PARAM_AX_PREFIX).append("required").append("=");
        for(int i=0; i < atts.length; i++) {
            if (i != 0) sb.append(',');
            sb.append(atts[0]);
        }

        return sb.toString();
    }

    static String makeParam(String name, String val) {
        return PARAM_PREFIX + name + "=" + val;
    }

    static String makeAxParam(String name, String val) {
        return PARAM_AX_PREFIX + name + "=" + val;
    }

    public static void main(String[] args) {
        System.out.println("Associate: " + OpenIdQuery.associate());
        System.out.println("Immediate: " + OpenIdQuery.checkid_immediate("rplante"));
    }
}