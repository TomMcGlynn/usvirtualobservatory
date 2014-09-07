<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="HarvestTable.aspx.cs" Inherits="OperationsManagement.HarvestTable" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Harvesting Report Page</title>
</head>
<body>

<table>
<tr>
<td width="56"></td>
<td>
<br />

    <form id="form1" runat="server">
        <asp:Label ID="LocalInfo" runat="server"></asp:Label>
        <br /><br />
        <asp:Label ID="TotalCount" runat="server" Text=""></asp:Label>
        <br />
        <asp:Label ID="HarvestedCount" runat="server"></asp:Label>
        <br />
        <asp:Label ID="TotalResources" runat="server"></asp:Label>
        <br /><br />
        <asp:Table ID="HarvesterTable" cellpadding="2" GridLines="Both" runat="server" />

    </form>
    <br />
   </td>
   <td width="60"></td> 
   </tr>
   </table>

</body>
</html>
