<html>
<head>
<title><?=$_SERVER[REQUEST_URI]?></title>
<style type="text/css">
     .block { background:#fff;border:2px solid #eee;padding:5px }
</style>
</head>
<body style="background:#ddd;margin:0px;padding:4px">

<div class="block" style="float:right;text-align:right">
  <a href="pc_logout_clearlogin"><b>Logout</b></a><br>
  <a href="/protected/debug.php"><small><?=$_SERVER[HTTP_HOST]?></small><b>/protected/debug.php</b></a><br>
  <a href="/debug/"><small><?=$_SERVER[HTTP_HOST]?></small><b>/debug/</b></a>
</div>

<div class="block" style="float:right;text-align:right">
  <a href="/protected/dump"><small><?=$_SERVER[HTTP_HOST]?></small><b>/protected/dump</b></a><br>
  <a href="/protected/welcome"><small><?=$_SERVER[HTTP_HOST]?></small><b>/protected/welcome</b></a>
</div>

<h1><?php echo "$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]" ?></h1>

<div class="block" style="float:left">
<h2>Server</h2>
<table>
<?php
foreach ($_SERVER as $key=>$value) {
    echo "<tr><td>$key</td><td>$value</td></tr>";
}
?>
</table>
</div>

<div class="block" style="float:right">
<h2>Environment</h2>
<table>
<?php
foreach ($_ENV as $key=>$value) {
    echo "<tr><td>$key</td><td>$value</td></tr>";
}
?>
</table>
</div>

</body>
</html>