package JobManager;

#

use CGI;
use Switch;
use Fcntl qw(:flock);
use Common;

use strict;

sub new {
    my $this = shift;
    my $class = ref($this) || $this;

	my ($jobID, $jobCommand, %parameters) = @_;
	my $self = {};
	$self->{query} = new CGI;
	$self->{params} = \%parameters;
	$self->{jobID} = $jobID;
	$self->{jobCommand} = $jobCommand;
	
	$self->{CacheDirRoot} = '@RUNTIME@@DSCache@';
	
	$self->{xmlDocAttributes} = ["xsi:schemaLocation=\"http://www.ivoa.net/xml/UWS/v1.0/UWS.xsd\"", 
	                             "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"",
	                             "xmlns:uws=\"http://www.ivoa.net/xml/UWS/v1.0\"",
	                             "xmlns:xlink=\"http://www.w3.org/1999/xlink\"",
	                             "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""];
	                             
	$self->{resultFiles} = ["results.xml", "hits.xml", "status.xml", "numberOfHits.txt"];
	
	$self->{base} = $self->{query}->url(-base=>1) . '@DSCache@';
	
	$self->{waitInterval} = 1;
	
	$self->{javaCommand} = 
		"/www/htdocs/vo/java/jre/bin/java -Xmx500M net.ivoa.datascope.Response";
#	$self->{javaCommand} = 
#		"/www/htdocs/vo/java/jre/bin/java -Xmx500M -Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y net.ivoa.datascope.Response";

	bless $self, $class;
	return $self;
}

sub process {
	my $self = shift;
	
	if( $self->{jobID} ){
		###################################
		# Do something with a specific job
		###################################
		if( $self->{jobCommand} ){
			switch( $self->{jobCommand} ){
				
				case 'error' {
					print $self->{query}->header(
							-type   => "text/plain",
							-status => "200 OK");
					print $self->getJobError();
					return;
				}
				
				case 'results' {
					my @uws = $self->getJobResults();
					$self->outputUWS(@uws);
					return;
				}
				
				case 'parameters' {
					my @uws = $self->getJobParameters();
					$self->outputUWS(@uws);
					return;					
				} 
				
				case 'phase' {
					if($self->{params}->{PHASE} && $self->{params}->{PHASE} eq 'RUN'){
						$self->startJob();
						return $self->{query}->url(-full=>1) . '/' . $self->{jobID};
					}else{
						print $self->{query}->header(
								-type   => "text/plain",
								-status => "200 OK");
						print $self->getJobPhase();
						return;
					}
				}
			}

		}else{
			my @uws = $self->getJobSummary();
			$self->outputUWS(@uws);
			return;
		}
	}else{
		###################################
		# Do something with the job queue
		###################################
		if( defined( $self->{params}->{RA} ) && defined( $self->{params}->{DEC} ) && defined( $self->{params}->{RADIUS} ) ){
			print STDERR "Datascope JobManager: ra, dec: ", $self->{params}->{RA}, " ", $self->{params}->{DEC}; 
			$self->initializeJob();
			if( $self->{params}->{PHASE} && $self->{params}->{PHASE} eq 'RUN'){
				$self->startJob();
			}
			return $self->{query}->url(-full=>1) . '/' . $self->{jobID};
		}
		my @uws = $self->getJobsSummary();
		$self->outputUWS(@uws);
		return;		
	}
}

sub initializeJob(){
	my $self = shift;
	my $jobQueueDir = $self->{CacheDirRoot} . "/job_queue";
	
	unless ( open(LOCKFILE, $jobQueueDir . "/.creationLock") &&
	         flock(LOCKFILE, LOCK_EX) ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error locking job creation file: $!";
		print STDERR "Datascope JobManager: Error locking job creation file: $! .\n";
		exit 0;	         	
	} 
	
	#######################
	# Get a unique job id
	#######################
	unless( open(JOBQUEUE, "ls $jobQueueDir |") ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error accessing job queue directory";
		print STDERR "Datascope JobManager: Error accessing job queue directory $jobQueueDir .\n";
		exit 0;
	} 
	
	my @all_dirs = <JOBQUEUE>;
	chomp @all_dirs;
	if($?){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error reading job queue directory: $!";
		print STDERR "Datascope JobManager: Error reading job queue directory $jobQueueDir: $! .\n";
		flock(LOCKFILE, LOCK_UN);
		exit 0;
	} 
	
	my @dirs = sort {$b <=> $a} grep (/^\d{3,}$/, @all_dirs);
	close JOBQUEUE;
	
	if($dirs[0]){
		$self->{jobID} = $dirs[0] + 1;
	}else{
		$self->{jobID} = 100;
	}
	
	unless( mkdir $jobQueueDir . "/" . $self->{jobID} ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error creating job directory $!", join(' ', @all_dirs);
		print STDERR "Datascope JobManager: Error creating job directory ", $self->{jobID}, ": $! .\n";
		flock(LOCKFILE, LOCK_UN);
		exit 0;
	}
	
	$self->writeJobParameters();
	$self->setJobPhase("PENDING");
	
	unless ( flock(LOCKFILE, LOCK_UN)  && close(LOCKFILE) ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error UNlocking job creation file";
		print STDERR "Datascope JobManager: Error UNlocking job creation file.\n";
		exit 0;	         	
	} 
	
}

sub startJob(){
	my $self = shift;
	my @output;
	
	if( $self->getJobPhase() ne "PENDING" ){
		return;
	}
	
	$self->setJobPhase("EXECUTING");
	
	my $jobdir = $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID};
	
	eval{
		open( OUTPUT, "$self->{javaCommand} $jobdir  2>&1 & |" ) || die($!);
		@output = <OUTPUT>;
		close OUTPUT;
	};	
	
	if($@){
		$self->setJobError("Error running DataScope\n", $@, "\n", @output);
		return;
	}
	
	if( @output ){
		$self->setJobError("DataScope failed to run properly\n", @output);
		return;
	}
	
	my $cacheDirFile = $jobdir . "/cachedir.txt";
	
	my $checkCount = 0;
	while( ! -f $cacheDirFile ){
		if( $checkCount++ > 10 ){
			$self->setJobError("Timeout waiting for datascope to start.");
			return;
		}
		sleep $self->{waitInterval};
	}
}

##########################################################

sub getJobsSummary {
	my $self = shift;
	my $jobQueueDir = $self->{CacheDirRoot} . "/job_queue";
	
	#######################
	# Get a unique job id
	#######################
	unless( open(JOBQUEUE, "ls $jobQueueDir |") ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error accessing job queue directory";
		print STDERR "Datascope JobManager: Error accessing job queue directory $jobQueueDir .\n";
		exit 0;
	} 
	
	my @all_dirs = <JOBQUEUE>;
	chomp @all_dirs;
	if($?){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error reading job queue directory: $?";
		print STDERR "Datascope JobManager: Error reading job queue directory $jobQueueDir: $? .\n";
		exit 0;
	} 
	
	my @dirs = sort {$b <=> $a} grep (/^\d{3,}$/, @all_dirs);
	close JOBQUEUE;

	my @summary;
	push @summary, "<uws:jobs>";	
	
	foreach my $dir (@dirs) {
		$self->{jobID} = $dir;
		push @summary, $self->getJobSummary();
	}	

	push @summary, "</uws:jobs>";		
	return @summary;
}

sub getJobSummary {
	my $self = shift;
	
	my $phase = $self->getJobPhase();

	my @summary;
	push @summary, "<uws:job>";
	push @summary, "<uws:jobId>" . $self->{jobID} . "</uws:jobId>"; 
	push @summary, "<uws:phase>" . $phase . "</uws:phase>";
	push @summary, $self->getJobParameters();
	push @summary, $self->getJobResults();
	push @summary, $self->getJobTimes();
	
	if( $phase eq "ERROR" ){
		push @summary, "<uws:errorSummary type=\"transient\" hasDetail=\"true\">";
		push @summary, "<uws:message>";
		push @summary, $self->getJobError();
		push @summary, "</uws:message>", "</uws:errorSummary>";		
	}

	push @summary, "</uws:job>";
	return @summary;
}

sub getJobPhase {
	my $self = shift;

	unless( open(PHASEFILE, $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/phase.txt") ){
		$self->fatalJobError("Error reading phase file");
	}
	my $phase = <PHASEFILE>;
	close PHASEFILE;
	
	if( $phase eq "ERROR" ){
		return $phase;
	}
	
	my $cachedir = $self->{CacheDirRoot} . "/" . $self->getJobCacheDir();
	my $dscopeErrorFile = $cachedir . "/error.txt";
	my $dscopePhaseFile =  $cachedir . "/phase.txt";
		
	my $dscopePhase;
	if( -f $dscopePhaseFile ){
		unless( open(DPHASEFILE, $dscopePhaseFile) ){
			$self->fatalJobError("Error reading datascope job phase file");
		}	
		$dscopePhase = <DPHASEFILE>;
		close DPHASEFILE;
	}
	
	
	if( ($dscopePhase && $dscopePhase eq "ERROR") || -f $dscopeErrorFile ){		
		if( -f $dscopeErrorFile ){
			unless( open(DERRORFILE, $dscopeErrorFile) ){
				$self->fatalJobError("Error reading datascope job error file");
			}	
			my @dscopeError = <DERRORFILE>;
			close DERRORFILE;

			$self->setJobError(@dscopeError);
		}
		
		$self->setJobPhase("ERROR");
		return "ERROR";
	}
	
	if( $dscopePhase ){
		return $dscopePhase;
	}
	
	return $phase;
}

sub setJobPhase {
	my $self = shift;
	my $phase = shift;
	
	unless( open(PHASEFILE, ">", $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/phase.txt") ){
		##############################################################
		# Don't call fatalJobError, otherwise could have infinite loop
		##############################################################
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error writing phase file to", $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . " ", $!;
		print STDERR "Datascope JobManager: Error writing phase file to", 
				$self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . " ", $!, "\n";
		exit 0;		
	}
	
	print PHASEFILE $phase;
	close PHASEFILE;
}

sub setJobError {
	my $self = shift;
	my @data = @_;
	
	map s/&/&amp;/, @data;
	map s/>/&gt;/, @data;
	map s/</&lt;/, @data;
	map s/"/&quot;/, @data;
	map s/'/&apos;/, @data;
	
	$self->setJobPhase("ERROR");
	
	unless( open(ERRORFILE, ">", $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/error.txt") ){
		##############################################################
		# Don't call fatalJobError, otherwise could have infinite loop
		##############################################################
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "Error writing error file";
		print STDERR "Datascope JobManager: Error writing error file to job ",  $self->{jobID}, ".\n";
		exit 0;		
	}
	
	print ERRORFILE @data;
	close ERRORFILE;
}

sub getJobError {
	my $self = shift;

	my $errorFile = $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/error.txt";
	unless( -f $errorFile ){
		return ();
	}
	unless( open(ERRORFILE, $errorFile) ){
		$self->fatalJobError("Error reading error file");
	}
	my @contents = <ERRORFILE>;
	close ERRORFILE;
	
	chomp @contents;
	return @contents;
}

sub fatalJobError {
	my $self = shift;
	my @data = @_;
	
	print $self->{query}->header(
		-type   => "text/plain",
		-status => "403 Forbidden");
	print "Fatal Job Error, unable to continue:\n";
	print @data;
	
	print STDERR "Datascope JobManager: Fatal Job Error, unable to continue:\n", @data;
		
	$self->setJobError(@data);
	
	exit 0;
}

sub getJobParameters {
	my $self = shift;

	unless( open(PARAMFILE, $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/parameters.xml") ){
		$self->fatalJobError("Error reading parameter file");
	}
	my @contents = <PARAMFILE>;
	close PARAMFILE;
	
	chomp @contents;
	return @contents;
}

sub writeJobParameters {
	my $self = shift;
	
	
	unless( open(PARAMFILE, ">", $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/parameters.xml") ){
		$self->fatalJobError("Error writing parameter file " . $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/parameters.xml");
	}
	print PARAMFILE "<uws:parameters>\n";
	
	my $params = $self->{params};
	foreach my $param (keys %$params){
		print PARAMFILE "<uws:parameter id=\"" . $param . "\">" . 
			$params->{$param} . "</uws:parameter>\n";
	}
	
	print PARAMFILE "</uws:parameters>\n";
	close PARAMFILE;
}

sub getJobResults {
	my $self = shift;
	
	my $cachedir = $self->getJobCacheDir();
	unless( $cachedir ){
		return ();		
	}
		
	my $resultFiles = $self->{resultFiles};
	my @results;
	push @results, "<uws:results>";
	foreach my $resultsFile (@$resultFiles) {
		#
		# Find the latest version of the results file.  
		# Assumes the files have the form <prefix>_v{number}.<suffix>
		#
		$resultsFile =~ /^(.*)\.([^\.]*)$/;
		my ($prefix, $suffix) = ($1,$2);
		
		my @versions = glob $self->{CacheDirRoot} . $cachedir . "/" . $prefix . "*." . $suffix;
		if(@versions){
			@versions = sort { ($b =~ /_v(\d+)\./)[0] <=> ($a =~ /_v(\d+)\./)[0] } @versions; 
		
			if( $suffix eq 'txt' ){
				my $idName = $prefix;
				my $filePath = $versions[0];
				if( -f $filePath ){
					unless( open(TXTFILE, $filePath) ){
						$self->fatalJobError("Error reading text file $filePath");
					}
					my @contents = <TXTFILE>;
					close TXTFILE;
					chomp @contents;
				
					push @results, "<uws:result id=\"$idName\" >" . $contents[0] . "</uws:result>";
				}
			}else{
				$versions[0] =~ /([^\/]*)$/;
				my $fileName = $1;
				push @results, "<uws:result id=\"" . $resultsFile . "\" xlink:href=\"" .  $self->{base} . $cachedir . "/" . $fileName . "\" />";
			}
		}
	}
	push @results, "</uws:results>";
	
	return @results;
}

sub getJobCacheDir {
	my $self = shift;	
	
	my $cachedirFile = $self->{CacheDirRoot} . "/job_queue/" . $self->{jobID} . "/cachedir.txt";
	
	unless( -f $cachedirFile ){
		return "";
	}
	
	unless( open(CACHEDIR, $cachedirFile) ){
		$self->fatalJobError("Error reading cache directory file");		
	}
	my $cachedir = <CACHEDIR>;
	close CACHEDIR;
	chomp $cachedir;
	
	return $cachedir;
}

sub getJobTimes {
	my $self = shift;	
	
	my $cachedir = $self->getJobCacheDir();
	unless( $cachedir ){
		return ();
	}	
	$cachedir = $self->{CacheDirRoot} . "/" . $cachedir;
	
	unless( open(TIMESFILE, $cachedir . "/timeline.xml") ){
		$self->fatalJobError("Error reading datascope job time stamps file: $cachedir/timeline.xml");				
	}
	my @contents = <TIMESFILE>;
	close TIMESFILE;
	
	chomp @contents;
	return @contents;
}

sub outputUWS {
	my $self = shift;	
	
	######################################################################
	# Add the schema document attributes to the first element in the list
	######################################################################
	my @contents = @_;
	unless( $contents[0] =~ /^<.*>$/ ){
		print $self->{query}->header(
			-type   => "text/plain",
			-status => "403 Forbidden");
		print "UWS output not in proper format";
		print STDERR "Datascope JobManager: UWS output not in proper format.\n";
		exit 0;
	}
	
	my $attribs = $self->{xmlDocAttributes};
	my $replacement = ' ' . join(' ', @$attribs) . " >";
	$contents[0] =~ s/>$/$replacement/; 
	
	print $self->{query}->header(
		-type   => "text/xml",
		-status => "200 OK");
	print join("\n", @contents);
}

1;