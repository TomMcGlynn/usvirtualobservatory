<%
    Map<Long, UserInfo> users = (Map<Long, UserInfo>) session.getServletContext().getAttribute("users");
    if (users == null) {
        users = new HashMap<Long, UserInfo>();
        session.getServletContext().setAttribute("users", users);
    }
%>