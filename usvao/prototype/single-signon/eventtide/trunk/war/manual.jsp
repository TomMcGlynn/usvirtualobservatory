<%@ page import="org.nvo.eventtide.server.RecordStepServlet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <title>Manually Create Log Entry</title>
      <style type="text/css">
          th { text-align:left; background:#ddd; font-weight:bold; padding:0.3em }
          td { padding:1px 0.2em 1px 0.2em; margin:0 }
      </style>
  </head>
  <body>
  <h1>Manually Create a Log Entry</h1>
  <form action="log">
      <input type="hidden" name="<%=RecordStepServlet.GIVE_FEEDBACK%>" />
      <table cellspacing="0" cellpadding="0" border="0">
          <tr><th colspan="4">
              Required elements
          </th></tr>
          <tr><td>&nbsp;</td>
              <label>
                  <td>Action</td>
                  <td><input type="text" name="action"></td>
              </label>
              <td>A complex action that a user may take. For example <em>register, sign in</em></td>
          </tr>
          <tr><td></td>
              <label>
                  <td>Phase</td>
                  <td><input type="text" name="phase"></td>
              </label>
              <td>A phase in this action. For example <em>predict, initiate, fail, succeed</em></td>
          </tr>
          <tr><th colspan="4">
              Additional details &#8212; used to group log entries in sequences.<br>
              These are examples only; any arbitrary key-value pair may be supplied.
          </th></tr>
          <tr><td></td>
              <label>
                  <td>Portal</td>
                  <td><input type="text" name="portal"></td>
              </label>
              <td>The portal this action is connected with</td>
          </tr>
          <tr><td></td>
              <label>
                  <td>User ID</td>
                  <td><input type="text" name="user_id"></td>
              </label>
              <td>An opaque ID identifying the individual who triggered this sequence</td>
          </tr>
          <tr><td></td>
              <label>
                  <td>Serial</td>
                  <td><input type="text" name="serial"></td>
              </label>
              <td>An opaque ID associated with this sequence</td>
          </tr>
          <tr><td></td>
              <label>
                  <td>Email</td>
                  <td><input type="text" name="email"></td>
              </label>
              <td>An email address (presumably belonging to a user)</td>
          </tr>
          <tr><th></th>
              <th colspan="2" style="text-align:right">
                  <input type="submit" value="Create"><input type="reset" value="Clear">
              </th>
              <th></th>
          </tr>
      </table>
  </form>
  </body>
</html>