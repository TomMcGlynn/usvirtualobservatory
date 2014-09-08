#!/usr1/local/bin/perl5 -w
#
#
# Title: get_datatypes.pl
#
# Author: Michael Preciado
# 
# Description:  retrieve Cone, SIA, SSA, Registry, etc
#               records from JHU registry
#

{
   
    use strict;  
    use File::Basename qw (dirname); 
    use vars qw(%param);
    my $homedir;
    use lib '/www/htdocs/vo/validation/perl';
   
    my $valdirtop  = "/www/htdocs/vo/validation/perl";
    

    BEGIN
    {
	$homedir = dirname($0);	
    }

    use LWP::UserAgent;
    use Mail::Mailer;
    use File::Copy;
    use XML::SimpleObject::LibXML;       
    use Getopt::Long;   
    use Util::FixDump;
    use Util::LoadTypes;
        
 
    #unless (-e "$valdirtop/../log")
    #{
	#open (STDOUT,">$valdirtop/../log") || die "cannot open output log";
    #}
    #open (STDOUT,">>$valdirtop/../log") || die  "cannot open output log";
    #open (STDERR, ">>$valdirtop/../log") || die "cannot open output log";
   
    print `date`;
  
    #parse command line options
    parse_opts(); 
    my ($strings,$path);
    $path  = "$homedir/data/downloadtypes";
    $path  = "$homedir/data/downloadtypes_deprecated" if ($param{deprecated});
    #load types (siap,cone,etc.)
    $strings = load_types($path);

    #overide hash for when users enter type on command line
    my $overide = {};
    

    my  $output;
    if ($param{'type'})
    {	
        #done when type entered on the command line.  	
	if (exists ($strings->{$param{type}}))
	{    	   
	    print $strings->{$param{type}}->{type}, "\n";	    
	    $overide->{$param{type}} =  $strings->{$param{type}};
	}
	$strings = $overide;
       
    }

    eval 
    {
   
	foreach my $type (keys %$strings)
	{
	    #download data by type and store as big xml files.
	    download_data($strings,$homedir,$type);
	}
    };
    if ($@)
    {
	print "ERRORS: $@";
    }   
    close (STDOUT)|| die "cannot close output log";
    close (STDERR)|| die "cannot close output log";
    
}
###########################
sub download_data
{
    my ($strings,$homedir,$type) = @_;
    
    my $ua  = LWP::UserAgent->new;
    print "Retrieving resources from registry...\n";
    print  $strings->{$type}->{query}, "\n";
      
    my $res  = $ua->get($strings->{$type}->{query});
    my $cont = $res->content;
    if  ($cont)	    
    {
	unlink "$homedir/dump/$strings->{$type}->{out}";    	
	open (OUT,">$homedir/dump/$strings->{$type}->{out}") || die "cannot open $param{type}";
	print OUT $cont;
	close OUT || die "cannot close ";
	chmod 0664,"$homedir/dump/$strings->{$type}->{out}";		
	fix_file("$homedir/dump",$strings->{$type}->{out});	    
    }
    else
    {
	print "Content could not be retrieved\n";
    }
    
}
#######################################
# parse command line options 
#######################################
sub parse_opts
{
    %param = ('verbose' => 0,
		);
    
    GetOptions (\%param,
		"version",
		"type=s",
                "deprecated",
		"all",
		) or exit(1);

    return if $param{version};
    my $err;
    while (my ($par,$value) = each %param)
    {
	next if defined $value;
        warn ("parameter `$par` not set\n");
        $err = 0;
    }
    exit(1) if $err;
}
