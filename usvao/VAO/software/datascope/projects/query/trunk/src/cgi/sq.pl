#!/usr1/local/bin/perl
#
# Render a datascope query page.
#
require  "./Common.pm";

$ENV{VOCLI_HOME} = '@VOCLIENT@';
$ENV{PATH} = '@VOCLIENT@:' . $ENV{PATH};

my $maxtime = 1500;

$SIG{ALRM} = sub { 
	print "Content-type: text/xml\n\n";
	print "Command has exceeded the maximum time of $maxtime seconds.\n";
	my $vocpid = `ps -C voclientd --no-heading | awk '{print \$1}'`;
	my @subprocesses;
	push @subprocesses, `pstree -np $vocpid` if ( $vocpid );
	push @subprocesses, `pstree -np $$`;
	my @pids;
	foreach (@subprocesses){
		while( m/((\w+)\((\d+)\))/g ){
			push @pids,$3 if (( $2 !~ /pstree/ ) && ( $3 != $$ ));
		}
	}
	foreach my $pid (reverse(@pids)){
		if ( $pid ) {
			print "Killing child $pid\n";
			if (kill 'ABRT',$pid) {
				print "Succeeded in killing $pid.\n";
			} else {
				print "Couldn't kill $pid, so manual cleanup may be necessary\n";
			}
		}
	}
	exit 1;	#	die in a subroutine does not cause an exit
};

run("net.ivoa.query.VOCli", $maxtime);
