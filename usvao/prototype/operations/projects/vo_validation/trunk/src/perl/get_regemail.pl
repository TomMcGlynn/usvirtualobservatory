#!/usr/bin/perl -wT
#
#
# Description: get email for 
#              a unique identifier from the
#              registry 
#
#
#
#

{
    use strict;
    
    use lib '/www/server/vo/validation';
    use lib "/www/htdocs/vo/validation/perl";
    

    use CGI;
    use LWP::UserAgent;
    use HTML::ErrorMessage;
    use XML::SimpleObject::LibXML;
    use HTML::Layout;
    use Service::Service;
    use Service::ParentXML;
    use data::startup;
    use Switch;
    #use CGI;
    my $cgi = CGI->new();
    my $ua  = LWP::UserAgent->new();
    #handle pars
    my $id   = detaint("id",$cgi->param("id"));
    my $type = detaint("type",$cgi->param("type"));

    #print "Content-type: text/xml\n\n";
    #print "Content-type: text/plain\n\n";
    print "Content-type: text/html\n\n";
    
    my @linknames = ('Validation','Monitor','NVO Home', 'NVO Feedback');

    gen_header_layout("Validation Email",\@linknames);

    #read unique registry queries from external file
    my $queries ={}; 
    open (File, "/www/htdocs/vo/validation/perl/data/downloadtypes") 
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
    
   
    my $query = $queries->{$type};
    
    
    $query    = "$query and identifier = \'$id\'";
    
    #get response
    my $response = $ua->get($query);
    my $cont = $response->content;
    #print "$cont";
   
    my $services =  process_registry_xml($cont,$type);
    foreach my $service (@$services)
    {
	print $service->get_id, ",   ", $service->get_email();
	
    }
    gen_footer_bas;
}
sub process_registry_xml
{    
    my ($xml,$type) = @_;  
    
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
        #my $parserwrap = new Service::ParserWrap($xmlnew);
        #my $valid     = $parserwrap->getstatus();       
        #my $nodeid     = $parserwrap->getid();
       
	
	$xmlnew      =~ s/&lt;/\</g;
	$xmlnew      =~ s/&gt;/>/g;
	$xmlnew      =~  s/(\w+):Resource/Resource/g;    
	$xmlnew      =~ s/vor:Resource/Resource/g;
	
	
	my $string = "empty";
        my $service =  get_service_from_resource($xmlnew, $type, $string); 
	if (ref($service) eq "ARRAY")
	{
	    push @$array, @$service[0];
	    push @$array, @$service[1];
	}
	else
	{
	    push @$array, $service;
	}
	
	
	
	$count++;
    }
    
    return $array;
}

sub get_service_from_resource
{
    my ($xml, $type,$string) = @_;
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
        
    
    
    my $child           = getchildobject($xmlobj,$type, $string);
   
    if (($child->getbase) and ($child->getbase  =~ /(.*?)\\(.*)/))
    {
        #this handles registry services with the same id but
        #multiple service urls. We need to create service objects
        #with a different service urls. 
        
        my @urls = (split /\\/, $child->getbase());
        my @xsi = (split /\\/, $child->getxsitype());
        my @service;
                
        for (my $i = 0; $i<scalar(@urls);$i++)
        {
         
            my $s = new_parsemail Service::Service::($$child{shortname},$child->setbase($urls[$i]),$child->{id},
                                                 $$child{input},$child->setxsi($xsi[$i]),$$child{email});            
          
            push @service, $s;
        }
        return \@service;               
    }
    else
    {           
	
        my $shortname      =  $child->getshortname();   
        my $service = new_parsemail Service::Service::($$child{shortname},$$child{base},$$child{id},
                                              $$child{input},$$child{xsitype},$$child{email});
        return $service; 
    }
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

sub detaint
{
    my ($parname, $value) = @_;
    my $status;

        
    switch($parname)
    {
        case "id" 
        { 
            if  ($value =~  /(ivo:\/\/.*[^ \<\>\;])/){$value = $1;}               	
	    else {$status = 1;} 
	}
	case "type"
	{ 
	    if ($value =~ m/(cone|siap|ssa|registry)/)
	    {$value = $1;}
	    else {$status = 1;}
	}
    }
    if ($status)
    { 
	#my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        #$error->display();
        #exit();
    }
    return $value;

}

