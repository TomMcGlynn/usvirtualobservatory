#!/usr1/local/bin/perl5 -w 
#
#
#  author: michael preciado
#  name : updatedb.pl 
#
#  description: updates VO db with new service URLs
#               from registry.
#
#
#
{
    use strict; 
    use lib "/heasarc/src/misc/perl/lib";
    use lib '/www/server/vo/validation';
    use data::startup;
    use lib "/www/htdocs/vo/validation/perl";
    use File::Basename qw (dirname);
    use DBI;
    use Mail::Mailer;  
    use Util::MailHandler;
    use Getopt::Long;
    use Connect::MySQLValidationDB;
    use Service::Service; 
    use SQL::Queries;
    
    #declare global vars
    use vars qw ($registryhash $reghashdep $dir %param);
    $registryhash = {}; 
    $reghashdep = {};
       
  
     BEGIN
     {	
	 $dir = dirname($0);
     }

    parse_opts();
    if ($param{help})
    {
	print_help();
	exit();
    }
    
    #get all service types
    my $array =  get_types_to_test("$dir/data/types");   
 
    #get local services
    my $dbh        = vodb_connect();
    my $DBServices = get_db_services($dbh);
      
    #get services from VO registry
    my $regservices = get_registry_services($array,$dir);   
    my $regdeprecated = get_deprecated();
	
    #get entries that need changing
    my ($updatedservices,$newservices,$localdeletions) = get_updates($DBServices,$dbh, $regservices);
    my $todeprecate  =  get_to_deprecate($DBServices,$regdeprecated); 
    
    #update db
    add_entry($newservices,$dbh);
    update_entry($updatedservices,$dbh);
    update_deprecated($todeprecate,$dbh);
    
    
    #send out summary and mail
    summarize_and_mail($localdeletions, $updatedservices,$newservices,$todeprecate);
         
    #disconnect 
    $dbh->disconnect 
	|| warn "Disconnection failed: $DBI::errstr\n";

    close (STDOUT)|| die "cannot close output log";
    close (STDERR)|| die "cannot close output log"; 
}
sub get_to_deprecate
{
    my ($DBservices,$regdeprecated) = @_;
    my @todep;   
    foreach my $n (@$regdeprecated)
    {
	my ($id, $type) = @$n;
        my $combined = "$id\\$type";
	if (exists $DBservices->{$combined})
	{
	   my  ($serviceId, $baseurl,$dep,$del)  = (split /\\/, $DBservices->{$combined});
	   if ($dep ne 'yes')
	   {
	      push @todep,"$id\\$type";
           }
        }
    }
    return \@todep;
}
sub get_deprecated
{
    opendir (DIR, "$::valdirperl/dump") || die "cannot open directory\n";
    my $array = [];
    while (defined(my $file = readdir(DIR)))
    {
	next if ($file =~ /^\.\.?/);
	if ($file =~ /(.*?)deprecated\_res\_for\_validation$/)
	{
	    open (FILE, "$::valdirperl/dump/$file");
	    while (<FILE>)
	    {
		my $line = $_;
		chomp $line;
		my @a = (split /\|/,$line);
		my $g = [$a[1],$a[8]];
	        $reghashdep->{"$a[1]\\$a[8]"} = 1; 
		push @$array, $g;
	    }
	    close FILE;
	}

    }
    closedir DIR;
    return $array;


}
sub get_registry_services
{
    my ($array,$dir) = @_;
    my @regservices;
    eval
    {			
	foreach  my $type (@$array)
	{
	    print "\nReading: $type data\n";
	    my $data = get_registry_services_bytype($type,$dir);
	    push @regservices, $data;	    	   
	}
    };
    print "ERROR in update db script: $@"  if ($@);
    
    return \@regservices;
    
}
sub summarize_and_mail
{
    my ($list, $updatedservices,$newservices,$todeprecate) = @_;
    print "\n####### Summary ########\n";

    if ((@$list) || (@$updatedservices) || (@$newservices) || (@$todeprecate))
    {
	
	print  "Services that need to be deleted\n";
	foreach my $n (@$list)
	{
	    print "$n\n";
	}
	print  "\nURLs have changed for the following service IDs/identifiers\n";
	foreach my $n (@$updatedservices)
	{
	    my ($ivoid, $service) = @$n;
	    print "$ivoid\n";
	 
	}
	print "\nServices that are new\n";
	foreach my $n (@$newservices)
	{
	    my ($ivoid,$service) = @$n;
	    print "$ivoid\n";
        }
	print "\nServices that have been deprecated\n";
	foreach my $n (@$todeprecate)
	{
	   my ($ivoid,$service) = $n;
	   print "$ivoid\n";
	}
	my $data = {};
	$data->{type} = 'update_db';
	$data->{list} = $list;
	$data->{updatedservices} = $updatedservices;
	$data->{newservices}= $newservices;
	$data->{todeprecate} = $todeprecate;
	send_mail($data);
    }
}
sub get_db_services
{
    my ($dbh) = @_;
    my $sth = $dbh->prepare("select ivoid, xsitype,serviceURL,serviceId,deprecated,deleted from Services");
    $sth->execute 
	or die "cannot run select statement\n";
 
    my $rowref    = $sth->fetchall_arrayref();  
    my $services  = load_services($rowref);
    return $services;
}
sub get_types_to_test
{
    my ($dir) = @_;
    my @array;
    open (File, "$dir")|| die "cannot open types";
    while(<File>)
    {
	my $line = $_;
	chomp $line;
	next if ($line =~ /^#(.*)/);
	my  ($type,$value) = (split /,/, $line);
	push @array, $type  if (-e "$::valdirperl/dump/$type\_res");
   }
    close File|| die "cannot close types";    
    return \@array;
}
sub load_services
{
    my ($rowref) = @_; 
    my $hash = {};

    my $size = scalar(@$rowref);
 
    foreach my $n (@$rowref)
    {
	my ($ivoid,$xsitype, $base,$serviceId,$deprecated,$deleted) = @$n;
	$ivoid      =  trim($ivoid);	
	$xsitype    = trim($xsitype);
	$base       = trim($base);
	$deprecated = 'no'  if (! defined $deprecated);
        $deleted = 'no' if (! defined $deleted);
	#$deprecated = trim($deprecated);

   	$hash->{"$ivoid\\$xsitype"}  = "$serviceId\\$base\\$deprecated\\$deleted";
        

    }
    return $hash;
}
sub get_updates
{
    my ($DBServices,$dbh,$regservices) = @_;
    my $count =0;
   
    print "Updating DB ...\n";
   
    my (@updates,@new,@localdeletions,@todep);
    #add new services to local db:fix service URLs that have changed 
    #loop over cone,siap, etc.
     
    foreach my $data (@$regservices)
    {    #will not contain deps or deletes
	#loop over each service within a type
	foreach my $service (@$data)
	{
	    my $id = $service->get_id();
	    
	    my $xsitype = $service->get_xsitype();
	    my $combined = "$id\\$xsitype";
	    
	    if (! exists ($DBServices->{$combined}))
	    {
		my $a = [$id,$service];
		push @new, $a;
	       
		$count++;
	    }
	    else
	    {
		#see if the serviceURL has changed for the service
		 
		my ($serviceId, $baseurl_localdb,$deprecated,$deleted) = (split /\\/,$DBServices->{$combined});
		my $b = $service->get_baseurl;
	       
		if ($b ne $baseurl_localdb)
		{   
		    my $c  = [$id,$b,$serviceId];
		    push @updates,$c;
		   
		}
	    }
	}
    }
 
    foreach my $combined (keys %$DBServices)
    {
        my @array  = (split /\\/, $DBServices->{$combined});
	next if ((exists $registryhash->{$combined}) || (exists $reghashdep->{$combined}));
        if ($array[3] ne 'yes')	
	{
	       print "This needs to be deleted from the local db: $combined, $array[3]\n" if ($param{debug});
	       push @localdeletions, $combined;
	}


    }
    return \@updates,\@new,\@localdeletions; 
}
sub get_registry_services_bytype
{
    my ($type,$dir) = @_;
 
    my @array;
    if (-e "$dir/dump/$type\_res\_for\_validation")
    {
	open (Type, "$dir/dump/$type\_res\_for\_validation") || die "cannot open type: $type\n";
	while(<Type>)
	{  
	    my $line = $_;
	    chomp $line;
	    my @d        = (split /\|/,$line);
	    my $service  =  new Service::Service(\@d);		   
	    push @array, $service; 
	    #store data in global variable here as well.
	    $registryhash->{"$d[1]\\$d[8]"} = 1;	    
	}	
	close Type || die "cannot close type: $type\n";
    }
    else
    {
	print "This type has not been downloaded from the registry or does not exist\n";
    }
    return \@array;
}
sub trim
{
    my ($string) = @_;
    $string =~ s/\s+$//g;
    $string =~ s/^\s+//g;
    return $string;
}
sub parse_opts
{ 
    %param = (
              verbose => 0
              );
    
    GetOptions (\%param, 
                "version", 
                "debug",
                "help") or exit(1);
    
    return if $param{help} or $param{version};
    
    my $err =0;
    while (my ($par,$val) = each (%param))
    {
        next if defined $val;
        warn ("parameter `$par` not set\n");
        $err++;
    }
    exit(1) if $err;
}
sub print_help
{
   
    print << " EOF";
A tool that updates the local validation db with the latest information from
the STScI registry. 

Options: 
  -h,            This help message
  --debug        Turn on debug mode
 EOF
}

