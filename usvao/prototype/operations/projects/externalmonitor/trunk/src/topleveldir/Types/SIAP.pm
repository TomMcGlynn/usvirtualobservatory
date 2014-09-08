#
#
#
package Types::SIAP;

@ISA = ("Types::SimpleURLResponse");
use Util::ByteDumper;
use XML::Simple;
use warnings;

sub new 
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->initialize(@_);
    return $hash;
}
sub initialize
{
    my $self = shift;
    $self->SUPER::initialize(@_);
    $self->setId("3.1");
    $self->{desc} = 'image download test';
}
sub test
{
    my ($self)      = shift;
    my $withxml     = shift;
    my $params      = $self->{params};
    my $testname    = $self->{testname};    
    my $res = $self->getsimpleresponse();   
    $self->{res} = $res;
    $self->{withxml} = '1' if ($withxml);
    my ($contents,$match,@cdatarows,$accessr,$accessf); 
    if (($res->is_success) and ($res->header('content-type') =~ /text\/xml/))
    { 
        my $parser   = XML::LibXML->new();
        my $dom      = $parser->parse_string($res->content);
	my $xmlobj   = new XML::SimpleObject::LibXML($dom);
	my $pos =0;
      LINE: foreach my $name ($xmlobj->child("TABLE")->children("FIELD"))
        {
	  my %atts = $name->attributes;
	  foreach my $inner (keys %atts){
	      if ($inner eq 'ucd')  {  
		  if ($atts{$inner} =~ /VOX\:Image\_AccessReference/i){
		      $accessr = $pos;
		  }
		  elsif ($atts{$inner} =~ /VOX\:Image\_Format/i){
                     $accessf = $pos;
                 }
	      }
	  }
	  $pos++;	    
        }
        my @tds;
        my $table = $xmlobj->child("TABLEDATA");
        if ($table->child("TR")->children("TD"))
        { 
	 @tds      =  $table->child("TR")->children("TD");
	 my $tdmatch  =  $tds[$accessr];
         $match       =  $tds[$accessr]->value if ($accessr);
         my $format   =  $tds[$accessf]->value if ($accessf);  
         #see if there is cdata
	 @cdatarows = $tdmatch->children;
	 #process cdata entry
	 $match  =   $cdatarows[0]->output_xml(indent=>1, original_encoding=>1) if (@cdatarows);
	}
	
	if (! $match)
	{ 
            #if no url, get a best guess url       
            foreach  my $td (@tds){
                $match = $td->value if ($td->value =~ /^\s*http(.*)/i);
            }
            if (! $match){
                $self->setMessage("URL not found in table. Possibly utype missing in &lt;FIELD&gt; element");
                $self->setStatus("fail");
            }
	}
	if ($match)
	{ 
	    my 	$type = "";
	    if (($format) &&  ( $format =~ /votable/i)){ 
		my $test = new Types::SimpleGrep($match,"VOTable in VOTable","<TR>");
		$test->test();
		$contents =  $test->getStatus();    
	    }  
	    else { 
		my $bd    =  new Util::ByteDumper($match);	
		$contents =  $bd->get_response();
		$type = $bd->getRes()->header("Content-Type");
	    }	
	    $self->check_contents($type,$contents,$testname);
         }
      }
      else
      {    #if res code is not 200 or response is not xml!
           $self->setMessage("Response could be html instead of xml or the res code status might not be 200");
           $self->setStatus("fail");
      }
}
1;
