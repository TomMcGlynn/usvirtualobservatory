<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="validationmanagement.aspx.cs" Inherits="Publishing.ValidationManagement" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title></title>
</head>
<body>
    <form  id="validationform" runat="server">
    <div>
        <h2>Resource Validation Management</h2>
        <asp:Panel ID="PanelMain" runat="server" Height="400px" Width="85%">
            <asp:Label ID="LabelIdentifier" runat="server" ></asp:Label>&nbsp;
            <asp:HyperLink ID="HyperlinkViewResource" runat="server" Target="new">(View Details)</asp:HyperLink>
            <br />
            <br />
            <asp:Label ID="DropDownActive" runat="server">Status: </asp:Label>
            <asp:DropDownList ID="DropDownStatus" runat="server" 
                onselectedindexchanged="DropDownStatus_SelectedIndexChanged">
                <asp:ListItem>active</asp:ListItem>
                <asp:ListItem>inactive</asp:ListItem>
                <asp:ListItem>deleted</asp:ListItem>
            </asp:DropDownList>
            <br />
            <asp:Label ID="DropDown_validationLevel" runat="server">General Validation: </asp:Label>
        </asp:Panel>
        <br />


        <asp:Button ID="ButtonSubmit" runat="server" Text="Submit Changes" 
            onclick="ButtonSubmit_Click"  OnClientClick="return confirm('Are you sure?')"/>
        <asp:Button ID="ButtonCancel" runat="server" Text="Cancel" 
            onclick="ButtonCancel_Click" />
        <br />
        <br />
        <asp:Label ID="LabelErrorMessage" runat="server"></asp:Label>
    </div>
    </form>

</body>
</html>
