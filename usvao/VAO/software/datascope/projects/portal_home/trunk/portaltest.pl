#!/usr/bin/perl -w

use strict;
my @children;
my $dev  = "http://heasarcdev.gsfc.nasa.gov/vo/squery/sq.sh";
my $test = "http://heasarcdev.gsfc.nasa.gov/jake/vo/squery/sq.sh";
my $pro1 = "http://heasarc1.gsfc.nasa.gov/vo/squery/sq.sh";
my $pro2 = "http://heasarc2.gsfc.nasa.gov/vo/squery/sq.sh";
my $ivo  = "ivo://nasa.heasarc/abell";
#my $vim  = "http://envoy5.cacr.caltech.edu:8080";
my $vim  = "http://envoy5.cacr.caltech.edu:8888";
my $inv  = "http://irsa.ipac.caltech.edu/cgi-bin/VOInventory/nph-voInventory";
my $xml  = "http://heasarc.gsfc.nasa.gov/jake/rassfsc-41.xml";

mkdir "output" unless ( -d "output" );

for(1..100) {
	sleep 3;
	my $pid = fork();
	if ($pid) {					# parent
		push(@children, $pid);
	} elsif ($pid == 0) {	# child
		print "$$ processing.\n";
		wget ("$dev?IVOID=$ivo&POSITION=3c273&RADIUS=10&units=degree", "sq.dev.1pos.$$" );
		wget ("$pro1?IVOID=$ivo&POSITION=3c273&RADIUS=10&units=degree", "sq.prodh1.1pos.$$" );
		wget ("$pro2?IVOID=$ivo&POSITION=3c273&RADIUS=10&units=degree", "sq.prodh2.1pos.$$" );
		wget ("$dev?IVOID=$ivo&POSITION=3c273;M101&RADIUS=10&units=degree","sq.dev.2pos.$$");
		wget ("$vim/?sourcesURL=$xml&toolName=sourcesURL", "vim.$$");
		wget ("$inv/?sourcesURL=$xml&radius=10&units=arcmin&findResources=1","inv.$$");
		wget ("$test?IVOID=$ivo&POSITION=3c273;M101&RADIUS=10&units=degree","sq.test.2pos.$$");
		exit(0);					#	children exit here.
	} else {						#	fork failed
		die "couldn’t fork: $!\n";
	}
}
#	only the parent makes it this far 
#	(assuming you put the exit(0) there)
foreach (@children) {
	print "Waiting for $_\n";
	waitpid($_, 0);
}

print "All done.\n";

exit;

sub wget {
	my ($url,$output)= @_;
	print "$url\n";
	system("wget -q '$url' -O output/$output");
}

sub curl {
	my ($url,$output)= @_;
	print "$url\n";
	system("curl --silent '$url' > output/$output");
}
