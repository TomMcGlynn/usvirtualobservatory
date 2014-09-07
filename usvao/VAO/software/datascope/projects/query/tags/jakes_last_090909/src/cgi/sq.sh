#!/usr1/local/bin/perl -w
use strict;
$ENV{PATH}       = "@VOCLIENT@:/usr1/local/java/bin:$ENV{PATH}";
$ENV{VOCLI_HOME} = "@VOCLIENT@";
my @result;
my $command = "/usr1/local/java/bin/java -cp query.jar:skyview.jar:stilts.jar net.ivoa.query.VOCli";
#my $command = "sleep 1000";
my $maxtime = 1500;
$| = 1;
$SIG{ALRM} = sub { 
	print "Content-type: text/xml\n\n";
	print "$command has exceeded the maximum time of $maxtime seconds.\n";
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

eval {
	alarm($maxtime);
	#  Start the command
	@result = `$command`;
	#  Clear the alarm.  (You don't get here if the timeout alarm was triggered above.)    
	alarm(0);
};

print @result;
