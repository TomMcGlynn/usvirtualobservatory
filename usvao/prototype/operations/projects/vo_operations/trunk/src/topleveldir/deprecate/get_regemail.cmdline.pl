#!/usr/contrib/linux/bin/perl -w
#
#
# Description: get email for 
#              a unique identifier from the
#              registry 
#
#
# 
#
# Usage: get_regemail.cmdline.pl -l {filename}  -m [used for a particular mail body]
#
#
# Example: send message for queries that do not respond at all 
#          ./get_regemail.cmdline.pl -l foo  
#  
#          send message for queries that do not pass the validators
#          ./get_regemail.cmdline.pl -l foo -m
#



{
    use strict;   
    #use lib "/www/htdocs/vo/validation/perl";
    use lib  "/heasarc/src/misc/perl/lib";
    
    use Getopt::Long;
    use LWP::UserAgent;
    
    use Service::ParserWrap;   
    use XML::SimpleObject::LibXML;
    use Text::Wrap;
    use Service::Service;
    use Util::MailHandler;
    
    use MIME::Entity;
    use  Mail::Send;
    
    use Service::ParentXML;
    use Switch;
    use vars qw(%param);
    parse_opts();
   

    my $ua  = LWP::UserAgent->new();
		       
    #handle pars
    my ($id,$type,$list,$array);

    if ((!$param{id}) and  (!$param{type}))
    {
	
	if (!$param{list})
	{	    
	    print "Missing list\n";
	    exit;
	}
	
	$list = $param{list};
	print "LL: $list\n";
	$array = read_list();
    }
    elsif (!$param{list})
    {
	if ((!$param{id}) or (!$param{type}))
	{
	    print "Missing pars\n";
	    exit;
	}
	push @$array,"$param{type}|$param{id}";
    }
   
    
    #read unique registry queries from external file
    #my $queries =load_data(); 
    my $ids_mail = {};
    

    
    foreach my $entry (@$array)
    {      
    
	my ($id,$url) = (split /\|/,$entry);
	#$type = $param{type};
	my $query = 'http://nvo.stsci.edu/vor10/RegistryAdmin.asmx/DSQueryRegistry?predicate=[@status]=1';
	#my $query = $queries->{$type};
	
	
	$query    = "$query and identifier = \'$id\'";
	print "\n\nQQ: $query\n";

	
	#get response
	my $response = $ua->get($query);
	my $cont = $response->content;
	#print "$cont";
	
	my $services =  process_registry_xml($cont,$id);
	foreach my $service (@$services)
	{
	    my $id  =  $service->get_id;
	    my $email = $service->get_email();
	    if (exists ($ids_mail->{$email})) 
	    {
		my $array = $ids_mail->{$email};
		push @$array,$id;
		$ids_mail->{$email} = $array;
	    }
	    else
	    {
		
		$ids_mail->{$email} = [$id]; 
	    }
	    
	    print $service->get_id, ",   ", $service->get_email(), "\n";
	    
	}
    }
    print "\n\n\n";
    foreach my $email (keys %$ids_mail)
    {
        print "THERE";
	print "       @{$ids_mail->{$email}}\n\n\n";
	my $mail = new Util::MailHandler($email, $ids_mail->{$email},$param{mailtype});
	print $mail->{body};
	$mail->send_mail();
    }
    
    
    
}
sub read_list
{
    my $array = [];
    open (File, "$param{list}");
    while(<File>)
    {
	my $line = $_;
	chomp $line;
	my @a = (split /\|/,$line);
	my $s = "$a[0]|$a[1]"; 
	push @$array, $s;
    }
    return $array;
}

sub load_data
{
    my $queries= {};
    open (File, "./downloadtypes") 
	|| die "cannot open downloadtypes";

    my @array = <File>;
    foreach my $n (@array)
    {
	my @a = (split "\\|", $n);
	my $type = shift @a;
	$a[2] =~ s/\"//g;
	
	$queries->{$type} = $a[2];	
    }
    close File|| 
	die "cannot close downloadtypes";
    return $queries;
}
sub process_registry_xml
{    
    my ($xml,$id) = @_;  
    
    $xml =~ s/diffgr\:diffgram(.*)\>/Replace\>/;
    $xml =~ s/<\/diffgr\:diffgram\>/\<\/Replace\>/;
    $xml =~ s/\<NewDataSet(.*)\>/\<NewDataSet\>/;
    $xml =~ s/<Table(.*)>/\<Table\>/;
    
    #print $xml;
    my $xmlobject                 = new XML::SimpleObject::LibXML( XML => $xml);
    #print "$xml";
    #print "snsn";
    my $datasetobj                = $xmlobject->child(DataSet)->child(Replace)->child(NewDataSet);
    my @tables                    = $datasetobj->children("Table");
    
     
    
    my $count = 0;
    my $array = ();
    
    
    foreach my $n (@tables)
    {    
      
       
        my $xmlinner     = $n->child("xml");     
        my $xmlnew  = $xmlinner->output_xml(indent => 1,original_encoding => 1);  
        
        
        #test if xml is well formed.
        
        
	$xmlnew      =~ s/&lt;/\</g;
	$xmlnew      =~ s/&gt;/>/g;
	$xmlnew      =~  s/(\w+):Resource/Resource/g;    
	$xmlnew      =~ s/vor:Resource/Resource/g;
	$xmlnew      =~ s/\<xml\>\<\?xml(.*?)\?\>/<xml>/g;
	
	my $parserwrap = new Service::ParserWrap($xmlnew);
	my $valid     = $parserwrap->getstatus();       
        my $nodeid     = $parserwrap->getid();

	
	if ($valid)
	{
	    
	    my $string = "empty";
	    my $service =  get_service_from_resource($xmlnew,$string); 
	    if (ref($service) eq "ARRAY")
	    {
		push @$array, @$service[0];
		push @$array, @$service[1];
	    }
	    else
	    {
		push @$array, $service;
	    }
	}
	else
	{
 
	    print "This id has invalid xml: $id\n\n";
	    print "$xmlnew\n";
	}
	
	
	$count++;
    }
    
    return $array;
}

sub get_service_from_resource
{
    my ($xml,$string) = @_;
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
    #print $xmlobj->output_xml(indent =>1);
        
    
    
    my $child           = getchildobject($xmlobj, $string);
   
    if (($child->getbase) and ($child->getbase  =~ /(.*?)\\(.*)/))
    {
        #this handles registry services with the same id but
        #multiple service urls. We need to create service objects
        #with a different service urls. 
        
        my @urls = (split /\\/, $child->getbase());
        #my @xsi = (split /\\/, $child->getxsitype());
	
        my @service;
                
        for (my $i = 0; $i<scalar(@urls);$i++)
        {
         
            my $s = new_parsemail Service::Service::($$child{shortname},$child->setbase($urls[$i]),$child->{id},
                                                 $$child{input},"cone",$$child{email});            
          
            push @service, $s;
        }
        return \@service;               
    }
    else
    {           
	
        my $shortname      =  $child->getshortname();   
        my $service = new_parsemail Service::Service::($$child{shortname},$$child{base},$$child{id},
                                              $$child{input},"cone",$$child{email});
        return $service; 
    }
}



sub getchildobject
{
    my ($xmlobj,$string) = @_; 
    my @res     = $xmlobj->child("xml")->child("Resource")->children();
    #my $upper   = ucfirst($type);
    my $name    = "XML";
    my $parser = "GenericClass";
    my $child   = Service::ParentXML::init(\@res,$xmlobj, $parser,$string);    
    return $child;
}
sub parse_opts
{
    
    %param = ('type', '',
              'id', '',
	      );
   
 
    GetOptions(\%param,
	      "mailtype",
	      "id=s",
	       "list=s",
	       ) or exit(1);
    
   my $err =0;

   
   foreach my $n (keys %param)
   {  
       
   }
    
    
}


