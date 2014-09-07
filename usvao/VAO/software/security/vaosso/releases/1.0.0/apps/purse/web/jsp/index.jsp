<% response.sendRedirect("register.jsp" + (request.getQueryString() == null ? "" : "?" + request.getQueryString())); %>
