<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="XmlResource.aspx.cs" Inherits="Publishing.XmlResource" ValidateRequest="false" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>VAO Publishing: Create or Edit a Resource in Raw XML Format</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <h2><asp:Label ID="LabelCreateEdit" runat="server" Text=""></asp:Label></h2>
        <br />
        <asp:TextBox ID="TextBoxResource" runat="server" Height="400px" 
            ontextchanged="TextBoxResource_TextChanged" TextMode="MultiLine" 
            Width="85%"></asp:TextBox>
        <br />
        <asp:Button ID="ButtonSubmit" runat="server" Text="Submit" 
            onclick="ButtonSubmit_Click" />
        <asp:Button ID="ButtonCancel" runat="server" Text="Cancel" 
            onclick="ButtonCancel_Click" />
    
        <br />
        <br />
        <asp:Label ID="LabelErrorMessage" runat="server"></asp:Label>
    
    </div>
    </form>
</body>
</html>
