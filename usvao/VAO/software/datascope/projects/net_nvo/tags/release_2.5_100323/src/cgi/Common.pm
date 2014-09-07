use CGI::Carp qw(fatalsToBrowser);

my $ALIB = '@JAR_RUN@';

# my $cmd = "/usr1/local/bin/java -Xmx500M -Dhttp.proxyHost=jumpgate.gsfc.nasa.gov -Dhttp.proxyPort=81 ";
# my $cmd = "/www/htdocs/vo/java/jre/bin/java -Xmx500M  -Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y net.ivoa.datascope.Run ";
my $cmd = "/www/htdocs/vo/java/jre/bin/java -Xmx500M  net.ivoa.util.Run ";
$ENV{CLASSPATH} =
     "$ALIB/" . '@JAR_NAME@'
  . ":$ALIB/net_nvo.jar"
  . ":$ALIB/skyview.jar"
  . ":$ALIB/query.jar"
  . ":$ALIB/tar.jar"
  . ":$ALIB/axis.jar"
  . ":$ALIB/jaxrpc.jar"
  . ":$ALIB/saaj.jar"
  . ":$ALIB/wsdl4j-1.5.1.jar"
  . ":$ALIB/commons-lang-2.4.jar"
  . ":$ALIB/stilts.jar";
  
  my $maxtime = 0;

sub appErr {
	my ($flag) = @_;
	my $cstr = $ENV{CLASSPATH};
	$cstr =~ s/\:/<br>/g;
	my $host = `hostname`;

	my $reason = "Exception in program";
	my $vis    = 'hidden';
	if ( !$flag ) {

		print <<EOT;
Content-type: text/html

<html>
<head><title> Java runtime error </title><head>
<body> <h2> Java Runtime Error</h2>
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
	exit();
}

sub run {
	$| = 1;
	my $count = 0;
	my $error = 0;
	$cmd .= shift;
	$maxtime = shift if $_[0];
	#
	# Alarm action should be set by each individual application
	#
	eval{
		alarm($maxtime);
		open( IN, "$cmd |" ) || appErr($!);
		while (<IN>) {
			$count += 1;
			if (/<title>Java Exception/i) {
				$error = 1;
			}
			print $_;
		}
		alarm(0);
	};
	if ( $count <= 0 || $error ) {
		appErr($error);
	}
}

1;
