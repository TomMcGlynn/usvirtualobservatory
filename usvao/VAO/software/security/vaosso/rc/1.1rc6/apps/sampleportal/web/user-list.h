  <p>Stored users:</p>
  <blockquote>
      <table cellpadding="5" border="0" cellspacing="0">
          <tr style="background:#aae">
              <th>Name</th><th>Email</th><th>Institution</th><th>Phone</th><th>Color</th>
              <th>ID</th><th>Created</th><th>Registered</th>
          </tr>
          <% Comma rowColor = new Comma("ddd", "fff", true); %>
          <% for (UserInfo u : users.values()) { %>
          <tr style="background:#<%=rowColor%>">
              <td><%=u.getName()%></td>
              <td><%=u.getEmail()%></td>
              <td><%=u.getInst()%></td>
              <td><%=u.getPhone()%></td>
              <td><%=u.getColor()%></td>
              <td><%=u.getUniqueId()%></td>
              <td><%=u.getCreated()%></td>
              <td>
                <% if (u.isRegistered()) { %>
                    <%= u.getRegistered() + "<br>NVO Username: <b>" + u.getNvoUsername() + "</b>" %>
                <% } else { %>
                    <%="<a href=" + u.getRegistrationUrl(getServletConfig(), request, true) + ">register</a>"%><br>
                    <%="<a href=" + u.getRegistrationUrl(getServletConfig(), request, false) + ">no returnUrl</a>"%>
                <% } %>
              </td>
          </tr>
          <% } %>
      </table>
  </blockquote>
