#
#
#
#
#
# Description: tests the Importer service (IRSA). Used
#              to test the service in real time by a user
#
package Types::VOImporter;
@ISA = ("Types::SimpleURLResponse");
use URI::URL;
use XML::SimpleObject::LibXML;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Response;
use HTML::Form;
use HTML::TreeBuilder;
use CGI;    
use Socket;
use Tie::IxHash;

sub new
{

    my ($class) = shift;
    my $hash  = {};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my ($self) = shift;
    $self->SUPER::initialize(@_); 
    $self->{desc} = 'is up'; 
}
sub showresponse
{
    my $self = shift;
    my $res = $self->getsimpleresponse();
    print $res;
}
sub getsimpleresponse
{
    my $self = shift;
    my $url = $self->{url};  
	
    my ($status,$content);
    
    #get start time
    my $start_time        = time();
   
    my $ua                = LWP::UserAgent->new;
    my $response          = $ua->get($url);
    $content              = $response->content;
    
   
    my @nvo_importer      = HTML::Form->parse($response);
    my $form              = shift @nvo_importer;

    #specify file for upload
    my $file_to_upload = 'nvo_upload';
    foreach my $n ($form->inputs)
    {
	if ($n->type eq "file")	{
	    $n->value("/www/htdocs/vo/vaomonitor/data/$file_to_upload");
	}
    }
    
    my $req = $form->click;
    my $res = $ua->request($req);
    my $resnewcont = $res->content;   
    return unless $resnewcont;
    
    my $tree = HTML::TreeBuilder->new_from_content($resnewcont);
    my @urls = $tree->look_down('_tag','a', 'href', qr/${file_to_upload}\/votbl.xml/); 
    my $a    = shift(@urls);
    return unless ref($a);
    my $urlext = $a->attr_get_i('href');
    return unless $urlext;
    
    #build final url and call it to get the VOTable response.
    my $final_url = $urlext;     
    my $final_res = $ua->get($final_url);
    my $c = $final_res->content;
    
    my @data = $final_res->content;
    my @grepout = grep (/<TABLEDATA>/,@data);
    my $status = "fail";	
    if (@grepout)	
    {
	$status = "pass";
    }
    
    return $c;

}
sub test
{
    my $self         = shift;   
    my $withxml      = shift;
    $self->{withxml} = $withxml if ($withxml);
    my $url          = $self->{url};      
    my $res          = $self->getsimpleresponse(); 
  
     
    my $testname = $self->{testname};    
    my @a = ($res);
    my @b = grep(/<TABLEDATA>/,@a);

    $self->setMessage("The '$testname' test has failed. Response does not contain the string to be matched");
    $self->setStatus("fail");
    $self->setId("6.1");


    if  (@b)
    {
	$self->setMessage("The $testname test has passed. Response contains the string to be matched");
	$self->setStatus("pass");       
    }
    $self->build_table();
}
