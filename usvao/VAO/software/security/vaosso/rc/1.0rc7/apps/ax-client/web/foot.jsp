<% if (!errors.isEmpty()) { %>
    <h2>Errors:</h2>
    <% for (String context : errors.keySet()) { %>
        <p><strong><%=context%></strong></p>
        <% if (errors.get(context) != null) { %>
<pre style="margin-left:2em">
<%=trace(errors.get(context))%>
</pre>
        <% } %>
    <% } %>
<% } %>
</body>
</html>
<%!
    String trace(Throwable t) {
        StringWriter s = new StringWriter();
        PrintWriter writer = new PrintWriter(s);
        t.printStackTrace(writer);
        return s.toString();
    }
%>
