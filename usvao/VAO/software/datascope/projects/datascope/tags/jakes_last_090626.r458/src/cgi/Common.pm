use CGI::Carp qw(fatalsToBrowser);

my $JLIB="@ABS_PATH@";
#my $ALIB="@ABS_PATH@/vo/java/axis";
#my $ALIB="/www/htdocs/vo/java/axis";
#my $cmd = "/usr1/local/bin/java -Xmx500M -Dhttp.proxyHost=jumpgate.gsfc.nasa.gov -Dhttp.proxyPort=81 ";
my $cmd = "/usr1/local/java/bin/java -Xmx500M  net.ivoa.datascope.Run ";
#my $cmd = "/www/htdocs/vo/java/jre/bin/java -Xmx500M  net.ivoa.datascope.Run ";
#    "/www/htdocs/vo/java/datascope.jar".
#    ":/www/htdocs/vo/java/skyview.jar".
#    ":/www/htdocs/vo/java/tar.jar".
#$ENV{CLASSPATH} = 
#    "@ABS_PATH@/vo/java/datascope.jar".
#    ":@ABS_PATH@/vo/java/skyview.jar".
#    ":@ABS_PATH@/vo/java/tar.jar".
#    ":$ALIB/axis-ant.jar".
#    ":$ALIB/axis-schema.jar".
#    ":$ALIB/axis.jar".
#    ":$ALIB/commons-discovery-0.2.jar".
#    ":$ALIB/commons-logging-1.0.4.jar".
#    ":$ALIB/jaxrpc.jar".
#    ":$ALIB/log4j-1.2.8.jar".
#    ":$ALIB/saaj.jar".
#    ":$ALIB/wsdl4j-1.5.1.jar".
#    ":$ALIB/activation.jar".
#    ":$ALIB/mail.jar";

$ENV{CLASSPATH} = 
    "$JLIB/datascope.jar".
    ":$JLIB/skyview.jar".
    ":$JLIB/tar.jar".
    ":$JLIB/axis-ant.jar".
    ":$JLIB/axis-schema.jar".
    ":$JLIB/axis.jar".
    ":$JLIB/commons-discovery-0.2.jar".
    ":$JLIB/commons-logging-1.0.4.jar".
    ":$JLIB/jaxrpc.jar".
    ":$JLIB/log4j-1.2.8.jar".
    ":$JLIB/saaj.jar".
    ":$JLIB/wsdl4j-1.5.1.jar".
    ":$JLIB/activation.jar".
    ":$JLIB/commons-lang-2.1.jar".
    ":$JLIB/mail.jar";

sub appErr {
    my ($flag) = @_;
    my $cstr = $ENV{CLASSPATH};
    $cstr =~ s/\:/<br>/g;
    my $host = `hostname`;
    
    my $reason = "Exception in program";
    my $vis= 'hidden';
    if (!$flag) {
	
       print<<EOT;
Content-type: text/html

<html>
<head><title> DataScope error </title><head>
<body> <h2> DataScope Error</h2>
EOT
        $reason = "No output from program";	  
	$vis    = 'visible';
    }
    
    print <<EOT;
<div id=jobinfo style='visibility:$vis'>
<table border>
<tr><th align=right>Status</th><td> $reason </td></tr>
<tr><th align=right>Command</th><td> $cmd </td></tr>
<tr><th align=right>CLASSPATH</th><td> $cstr </td></tr>
<tr><th align=right>Method</th><td> $ENV{REQUEST_METHOD}</td></tr>
<tr><th align=right>QUERY_STRING</th><td> $ENV{QUERY_STRING}</td></tr>
<tr><th align=right>Host machine</th><td> $host </td></tr>
</table>
</div>
</body>
</html>
EOT
    exit()
}

sub run {
    $|  = 1;
    $cmd .= join(" ", @_);
    open(IN, "$cmd |") || appErr($!);
    my $count = 0;
    my $error = 0;
    while (<IN>) {
        $count += 1;
	if (/<title>DataScope Exception/i) {
	    $error = 1;
	}
        print $_;
    }
    if ($count <= 0  || $error) {
        appErr($error);
    }
}

1;
