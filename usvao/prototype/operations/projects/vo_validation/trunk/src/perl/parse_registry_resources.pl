#!/usr1/local/bin/perl5 -w
#
#
# Author: Michael Preciado
# 
# Description: parses the registry siap, ssa,
#              and cone search xmls.
#             
#
#
#
{
    use strict;    
    use lib '/www/server/vo/validation/'; 
    use lib "/www/htdocs/vo/validation/perl";
   
    use data::startup;
    use Mail::Mailer;   
    use LWP::UserAgent;
    use Service::Service;
    use File::Basename qw(dirname);
    use XML::SimpleObject::LibXML;
    use Getopt::Long;
    use Service::ParentXML;
    use Util::MailHandler;
    use Service::ParserWrap;
    use XML::Parser;
   
    use vars qw (%param $mailrecipient  $mailsender $listofissues); 
    $listofissues = [];
   
      
    print `date`;
    print "Parsing registry resources....\n";
    
    #parse command line options
    parse_opts();

    
    #store all possible download types in a hash
    my $types_hash =  readtypes($::valdirtop);    
    
    if ($param{type}) 
    {  
       print "You need to enter a valid type...exiting\n" and exit if (! exists $types_hash->{$param{type}});
       if (exists $types_hash->{$param{type}})
       {
           my $value = $types_hash->{$param{type}};
           %$types_hash = ();
           $types_hash->{$param{type}} = $value;
       }  			  
    } 
    #process each data type
    foreach my $type (keys %$types_hash)    
    { 
        my ($output,$file);
	$file = "$type\_res";
	$file = "$type\_deprecated_res" if ($param{deprecated}); 
	my $readfile = "$::registrydump/$file";
       	$output   = "$::registrydump/$file" . "_for\_validation";
	#remove older processed copy and skip if does not exist
	unlink $output if (-e $output);
        print "The file: $readfile does not exist\n" and next if (! -e $readfile);	
	
        #validate xmls
        my ($validxmls, $invalidxmls)  = get_validated_xmls($readfile);
        push @$listofissues,$invalidxmls  if (@$invalidxmls);

	#get services
 	my $services =  get_services_from_resources($validxmls, $type, $types_hash->{$type}); 
	build($services,$output);

	#record urls with errors 
        record_urlerrors($services,$file);
    }
    
    #send mail
    my $data            = {};
    $data->{body}       = $listofissues;
    $data->{type}       =  'parse_registry_resources';
    $data->{sender}     = "Michael.E.Preciado\@nasa.gov";
    $data->{recipient}  = "Michael.E.Preciado\@nasa.gov";
    send_mail($data) if (@$listofissues);
}
sub record_urlerrors
{
    my ($services,$file) = @_;
    $file = "$file" ."_urlerrors";
    my $output = "$file" . "_urls";
    open my $filehandle,">", "$::registrydump/$output";
     
    foreach my $s (@$services)
    {
        if (($s->get_urlerror) ne "null")
        {            
            printf $filehandle ("%-50s", $s->get_id);        
            printf $filehandle  ("%-100s", $s->get_baseurl);
            print $filehandle "\n";                     
        }        
    } 
    close $filehandle;
}
sub readtypes
{
    my ($homedir) = @_;
    my %types_hash;
    print "$::valdirperl/data/types\n";
    open (Types, "$::valdirperl/data/types")
	|| die "cannot open types";

    while (<Types>)
    {
	chomp $_;
        my $line = $_;
	my ($type, $metadata) = (split /,/, $line);
        $types_hash{$type} = $metadata;    
    }
    close Types 
	||die "cannot close types";
    return \%types_hash;
}
sub build
{
    my ($services,$output) = @_;
    #print "O: $output\n";
    open (OUT,">$output") || die "cannot open $output\n";
    foreach my $service  (@$services)
    {     

	print OUT $service->get_shortname;
	print OUT "|", $service->get_id, "|",$service->get_baseurl;
	print OUT "|", $service->get_type;
	print OUT "|", $service->get_ra, "|", $service->get_dec;
	print OUT "|", $service->get_sr;
	print OUT "|", $service->get_role;
	print OUT "|", $service->get_xsitype, "|", "\n";


print "JRU: ", $service->get_xsitype, "\n";	
	#print "Ident:", $service->get_id, "\n";
	#$service->print_validateurl;
    }
    close OUT;
    chmod 0664, "$output";
}
sub get_services_from_resources
{
    my ($xmls, $type,$string) = @_;
    my $array = [];
    foreach my $xml( @$xmls)
    {
	my $service;
	my $hash =     
	{
	    'shortname' => 'null',
	    'id'        => 'null',
	    'base'      => 'null',
	    'input'     => 'null',
	    'ra'        => '0',
	    'dec'       => '0',
	    'sr'        => 'null',
	    'role'      => 'null',
	    'xsitype'   => 'null',
	};	
	
	#get data from individual resource	
	my $xmlobj  = new XML::SimpleObject::LibXML(XML => $xml);
	print $xmlobj->output_xml(indent =>1) if ($param{debug});
	
	my $child           = getchildobject($xmlobj,$type, $string);
	
	if (($child->getbase) and ($child->getbase  =~ /(.*?)\\(.*)/))
	{
	    #this handles registry services with the same id but
	    #multiple service urls. We need to create service objects
	    #with derent service urls. 
	    

            print "UUUU: $child->{base}\n";
            #$child->{base} = $url;
	  my $foo = $child->getbase;
            my @urls = (split /\\/, $child->getbase);
	    my @xsi = (split /\\/, $child->getxsitype);
	    my @id = (split /\\/, $child->getid);


            my @servicearray;
	    
	    for (my $i = 0; $i<scalar(@urls);$i++)
	    {
                if (scalar(@id) == '1') { $id[$i] = $id[0]; }
	        my @a = ($$child{shortname}, $child->setid($id[$i]), $child->setbase($urls[$i]),
					       $$child{input}, $$child{ra}, $$child{dec},
					       $$child{sr},$$child{role}, $child->setxsi($xsi[$i]), $child->get_errorinurl());
  	               
	        my $s = new Service::Service::(\@a);		
		push @servicearray, $s;
	    }
	    $service = \@servicearray;
            	
	}
	else
	{   	
	    my $shortname      =  $child->getshortname();	
	    my @a = ($$child{shortname}, $$child{id}, $$child{base},
						 $$child{input}, $$child{ra}, $$child{dec},
						 $$child{sr}, $$child{role}, $$child{xsitype}, $child->get_errorinurl());
	    $service =  new Service::Service(\@a); 
	}
	if (ref($service) eq "ARRAY")
	{
            foreach my $n (@$service)
	    {
	       push @$array, $n;
	    }
         }
	else
	{
	    push @$array, $service;
	}
	$count++;
        
    }
    print "\nTotal: $count\n";
    return $array;
}
sub getchildobject
{
    my ($xmlobj,$type,$string) = @_; 
    my @res     = $xmlobj->child("xml")->child("Resource")->children();
    my $upper   = ucfirst($type);
    my $name    = "XML";
    my $parser  = $upper.$name;  
    my $child   = Service::ParentXML::init(\@res,$xmlobj, $parser,$string);    
    return $child;
}
sub cleanup_data
{
    my ($hash) = @_;
    foreach my $n (keys %$hash)
    {
	if  (! (defined($$hash{$n})))
	{
	    $hash->{$n} = 'null';
	}
	$hash->{$n}  = trim($hash->{$n});
    }
    return $hash;
}
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
sub  get_validated_xmls
{    
    my ($filename) = @_;       
    my $xmlobject                 = new XML::SimpleObject::LibXML( file => "$filename");
    my $datasetobj                = $xmlobject->child();
    my @tables                    = $datasetobj->children("Table");
            
    
    my $count = 0;
    my $array = [];
    my $array1 = [];
    print "Extracting $param{count} from $filename resources" if ($param{count});
    
    foreach my $n (@tables)
    {	 
	if ($param{count})
	{	 
	    last if ($count == $param{count});
	} 
	my $xml     = $n->child("xml");     
	my $xmlnew  = $xml->output_xml(indent => 1,original_encoding => 1);  
		
	#test if xml is well formed.
	my $parserwrap = new Service::ParserWrap($xmlnew);
        my $valid      = $parserwrap->getstatus();       
	my $nodeid     = $parserwrap->getid();
        my $x          = $parserwrap->getxml();
	if ($valid)
	{  	  	   
	    push @$array, $x;
	}	
	else
	{
	    push @$array1,$x;
	}
	$count++;
	#{	   
	#    print STDOUT "\n\nXML not well formed for id : $nodeid\n";
	 #   $listofissues->{$nodeid} = 1;
	#}
    }
    print "\nTotal: $count\n";
    return $array,$array1; 
}
sub parse_opts
{ 
    %param = (
              verbose => 0
              );
    
    GetOptions (\%param, 
                "version",
		"type=s",
                "all",
                "deprecated",
		"count=s",
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
