#!/usr/contrib/linux/bin/perl -w   
#
#
# author: michael preciado
# 
# name: get_registry_deprecated.pl
# 
# description: get_list of VO services from registry
#              that have been deprecated (val level 1)
# usage: ./get_registry_deprecated.pl 
#
# 
#
{
    use lib '/software/jira/software2/projects/vo_operations/trunk/src/topleveldir/deprecate';
    use lib './'; 
    use strict;
    
    use DBI;
    use XML::SimpleObject::LibXML;
    
    use Connect::MySQLValidationDB;
    use SQL::Queries;
    use Getopt::Long;
    use Service::Service;
    use Service::ParentXML;    
    use LWP::UserAgent;
    use File::Basename qw(dirname);

    use vars qw(%param);    
    parse_opts();
   
    #connect to db
    my $dbh  = vodb_connect();

    if ($param{type})
    {
	my $res  = getdeprecatedlist_registry($param{type});
	process($res,$param{type});
	exit();
    }

    #get cone val level 1
    my $cone  = getdeprecatedlist_registry("Cone");
    process($cone,"Cone");

    #get siap val level 1
    my $siap = getdeprecatedlist_registry("Siap");
    process($siap,"Siap");


}
sub process
{
    my ($response,$type) = @_;
    
    my $xmlobject                 = new XML::SimpleObject::LibXML( XML => $response);
    my $datasetobj                = $xmlobject->child(NewDataSet);
    my @tables                    = $datasetobj->children("Table");
    open (LIST,">./$type.list");

    foreach my $n(@tables)
    {
	 
        my $xmlinner  = $n->child("xml");     
        my $xmlnew    = $xmlinner->output_xml(indent => 1,original_encoding => 1);
        $xmlnew       =~  s/ri:Resource(.*?)>/Resource$1>/s;	
	$xmlnew       =~ s/&lt;/\</g;
	$xmlnew       =~ s/&gt;/>/g;
	$xmlnew       =~ s/(\w+):Resource/Resource/g;    
	$xmlnew       =~ s/vor:Resource/Resource/g;
	$xmlnew       =~ s/<\?xml(.*?)>//sg;
	
	if ($type eq 'Siap')
	{	    
	    $string =  "sia:SimpleImageAccess|ivo://ivoa.net/std/SIA";
	}
	elsif ($type  eq 'Cone') 
	{	    
	    $string = "cs:ConeSearch|ivo://ivoa.net/std/ConeSearch";
	}	
	my $service =  get_service_from_resource($xmlnew,$type, $string); 
	print LIST  $service->get_id(),"|", $service->get_baseurl(), "\n";
    }
    close LIST;
}
sub get_service_from_resource
{
    my ($xml,$type, $string) = @_;
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
    
    
    my $count = 0;
    my $array = ();
    
        
    my $child           = getchildobject($xmlobj, $type, $string);
    
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
         
            my $s = new_alternate Service::Service::($$child{shortname},$child->setbase($urls[$i]),
				       $child->{id},$$child{input},$$child{xsitype},$$child{email},
                                       $$child{vallevel});            
          
            push @service, $s;
        }
        return \@service;               
    }
    else
    {           
        my $shortname      =  $child->getshortname();   
        my $service = new_alternate Service::Service::($$child{shortname},$$child{base},$$child{id},
					    $$child{input},$$child{xsitype},$$child{email},
                                            $$child{vallevel});
        return $service; 
    }

}
sub getchildobject
{
    my ($xmlobj,$type, $string) = @_;
    my @res     = $xmlobj->child("xml")->child("Resource")->children();
    my $upper   = ucfirst($type);
    my $name    = "XML";
    my $parser = $upper.$name;
    my $child   = Service::ParentXML::init(\@res,$xmlobj, $parser,$string);    
    return $child;
}
sub getdeprecatedlist_registry
{
    my ($type) = @_;
    my $ua = LWP::UserAgent->new();
    my $res;
    #my $res = $ua->get('http://nvo.stsci.edu/vor10/registryadmin.asmx/DSQueryRegistry?predicate=[@status]=1%20and%20validationLevel%20=%201');
    #my $res =  $ua->get('http://nvo.stsci.edu/vor10/RegistryAdmin.asmx/DSQueryRegistry?predicate=[@status]=1%20and%20identifier=%27ivo://nrao.archive/vlba/vcs%27');

    if ($type eq 'Cone')
    {
	$res = $ua->get('http://nvo.stsci.edu/vor10/RegistryAdmin.asmx/DSQueryRegistry?predicate=%20[@status]=1and%20validationLevel%20=%201%20and%20xsi_type%20=%20%27vs:CatalogService%27%20and%20contains%28*,%27STANDARDID*ivo://ivoa.net/std/ConeSearch%27%29');
    }
    elsif ($type eq 'Siap')
    {
	$res = $ua->get('http://nvo.stsci.edu/vor10/RegistryAdmin.asmx/DSQueryRegistry?predicate=[@status]=1%20and%20validationLevel%20=%201%20and%20xsi_type%20=%20%27vs:CatalogService%27%20and%20contains%28*,%27STANDARDID*ivo://ivoa.net/std/SIA%27%29');
    }
    else
    {
	print "type not recognized\n";
	exit(1);
    }
    my $page = $res->content;
    $page =~ s/<\/diffgr:diffgram>(\s+)//;
    $page =~ s/<\/DataSet>(.*)//;
    $page =~ s/<DataSet(.*)<NewDataSet xmlns="">(\s+)/<NewDataSet>/sg;
    $page =~ s/(<\/NewDataSet>)(.*)/$1/g;
    $page =~ s/<Table(.*?)>/<Table>/g;    
    return $page;    
}
################################################
# subroutine for parsing command line options
################################################
sub parse_opts
{ 
    %param = (
              verbose => 0
              );
    
    GetOptions (\%param, 
                "version",
                "type=s",
                ) or exit(1);
    
    return if $param{help} or $param{version};
    
    my $err =0;
    
    exit(1) if $err;
}




