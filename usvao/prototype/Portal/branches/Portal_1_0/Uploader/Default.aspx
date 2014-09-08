<%@ Page Language="C#" AutoEventWireup="true" CodeFile="Default.aspx.cs" Inherits="_Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Untitled Page</title>
<script language="javascript" type="text/javascript">
<!--

function TABLE1_onclick() {

}

// -->
</script>
</head>
<body id="divResultTable">
    <form id="form1" runat="server">
    <div>
        &nbsp;&nbsp;
        <table style="width: 476px" id="TABLE1" language="javascript" onclick="return TABLE1_onclick()">
            <tr>
                <td style="width: 313px">
        <asp:FileUpload ID="fileUploader" runat="server" Width="400px" /></td>
                <td style="width: 83px">
        <asp:Button ID="btnFileUploadGalex" runat="server" OnClick="btnUpload_Click" Text="File Upload Galex" Height="25px" Font-Bold="True" PostBackUrl="http://galex.stsci.edu/gxWS/Uploader/FileUploader.ashx" Width="170px" /></td>
                <td style="width: 313px">
                    <asp:Button ID="btnFileUploadLocal" runat="server" OnClick="btnUpload_Click" Text="File Upload Local" Height="25px" PostBackUrl="http://localhost:1280/Uploader/FileUploader.ashx" Width="170px" /></td>
            </tr>
            <tr>
                <td style="width: 313px">
        <asp:Label ID="lblMessage" runat="server" Width="400px"></asp:Label></td>
                <td style="width: 83px"><asp:Button ID="btnTargetUploadGalex" runat="server" OnClick="btnUpload_Click" Text="Target Upload Galex" Height="25px" Font-Bold="True" PostBackUrl="http://galex.stsci.edu/gxWS/Uploader/TargetFileUploader.ashx" Width="170px" /></td>
                <td style="width: 313px">
                    <asp:Button ID="btnTargetUploadLocal" runat="server" OnClick="btnUpload_Click" Text="Target Upload Local" Height="25px" PostBackUrl="http://localhost:1280/Uploader/TargetFileUploader.ashx" Width="170px" /></td>
            </tr>
            <tr>
                <td colspan="2" style="width: 313px; height: 24px">
                    result xml:</td>
                <td colspan="1" style="width: 313px; height: 24px">
                </td>
            </tr>
            <tr>
                <td colspan="2" style="width: 313px; height: 24px">
                    <div style="width: 478px; height: 148px" id="divResult" runat="server">
                        <br />
                    </div>
                </td>
                <td colspan="1" style="width: 313px; height: 24px">
                </td>
            </tr>
        </table>
    </div>
    </form>
</body>
</html>
