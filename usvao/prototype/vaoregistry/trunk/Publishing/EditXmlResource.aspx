<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="EditXmlResource.aspx.cs"  Inherits="Publishing.EditXmlResource" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title></title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
    
        <asp:TextBox ID="ResourceTextBox" runat="server" Height="500px" Width="95%"></asp:TextBox>
        <br />
        <asp:Button ID="SubmitButton" runat="server" Text="Submit" />
        <asp:Button ID="CancelButton" runat="server" Text="Cancel" />

        <asp:ScriptManager ID="ScriptManager1" runat="server">      
        </asp:ScriptManager>
 
        <asp:UpdatePanel ID="UpdatePanel1" runat="server">
        </asp:UpdatePanel>
 
    </div>
    </form>
</body>
</html>