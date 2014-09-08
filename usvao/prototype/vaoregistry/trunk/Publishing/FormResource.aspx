<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="FormResource.aspx.cs" Inherits="Publishing.FormResource" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
     <link rel="Stylesheet" href="StyleSheet1.css" type="text/css"/>
     <title>VAO Publishing: Create or Edit a Resource</title>
</head>
<body>
    <form id="form" runat="server">
    <div>
        <h2><asp:Label ID="LabelCreateEdit" runat="server">Resource Management: Create or Edit a Resource </asp:Label></h2>
        <asp:LinkButton class="selecttab" runat="server" OnClick="SelectTab" ID="Tab_PanelMain">General Information</asp:LinkButton>
        <asp:LinkButton class="selecttab" runat="server" OnClick="SelectTab" ID="Tab_PanelCuration">Curation</asp:LinkButton>
        <asp:Panel ID="PanelMain" runat="server" class="maintab">
            <table>
            <tr><td><asp:Label ID="LabelTitle" runat="server">Title: </asp:Label></td><td><asp:TextBox ID="TextBox_title" runat="server" 
                ontextchanged="TextBoxUniqueElement_TextChanged"></asp:TextBox></td></tr>           
            <tr><td><asp:Label ID="LabelIdentifier" runat="server">Identifier: </asp:Label></td><td><asp:TextBox ID="TextBox_identifier" runat="server" 
                ontextchanged="TextBoxUniqueElement_TextChanged"></asp:TextBox></td></tr>
            <tr><td><asp:Label ID="LabelShortName" runat="server">Short Name: </asp:Label></td><td><asp:TextBox ID="TextBox_shortName" runat="server" 
                ontextchanged="TextBoxUniqueElement_TextChanged"></asp:TextBox></td></tr>
            </table>
        </asp:Panel>
        <asp:Panel ID="PanelCuration" runat="server" class="maintab" Visible="false">
        </asp:Panel>
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
