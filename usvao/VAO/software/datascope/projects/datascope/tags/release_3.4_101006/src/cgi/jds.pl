#!/usr1/local/bin/perl
require  "./Common.pm";

#
# Start a response to a datascope request.
#

my $limit = 3;
my @results = `ps auxww|grep datascope.Run|wc`;
my $line = $results[0];
my ($cnt) = split(" ", $line);
if (defined($cnt)) {
    $cnt -= 2;
}

if ($cnt > $limit) {
    print <<EOT;
Content-type: text/html

<head>
<Title> DataScope overload </Title>
</head>
<body>
<H2> DataScope Overload </H2>
There are $cnt DataScope sessions currently active.  The limit
is $limit.  Please try again later.
</body>
EOT
    exit;
}
run("net.ivoa.datascope.Response");
