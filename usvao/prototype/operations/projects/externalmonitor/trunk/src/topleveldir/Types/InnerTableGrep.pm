#
#
#
#
#
package Types::InnerTableGrep;

@ISA = ("Types::SimpleURLResponse");
use strict;


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
    $self->{desc} = 'string matching test';
    $self->setId("6.1");
}
sub showresponse
{
    my $self = shift;
    $self->SUPER::showresponse;
}
sub test
{
    my ($self)       = shift;
    my $withxml      = shift;   
    my $params       = $self->{params};
    my $testname     = $self->{testname};
    my @array        = (split /;/,$params);
    my $matchstring  = shift @array;  
    my $res          = $self->SUPER::getsimpleresponse();
    $self->{res}     = $res;
    $self->{withxml} = '1' if ($withxml);
    
     #set defaults
     $self->setMessage("The $testname test has failed. String in VOTable response was not matched");
     $self->setStatus('fail');
		
    my $content = $res->content;

    if ($res->is_success)
    {
	my $xmlobj = new XML::SimpleObject::LibXML(XML=>$content);
	
	my $pos =0;
      LINE: foreach my $name ($xmlobj->child("TABLE")->children("FIELD"))
      {
	  my %atts = $name->attributes;
	  foreach my $inner (keys %atts)
	  {		
	      if (($inner eq 'utype') and ($atts{$inner} =~ /ssa(.?)Access(.?)Reference(.?)/i))
	      {
		  last LINE;
	      }	      
	  }
	  $pos++;
      }
       
	#extract url
	my ($url,@tds);
	my $table  =  $xmlobj->child("TABLEDATA");
        if ($table->child("TR"))
        {
          @tds  = $table->child("TR")->children("TD");
          $url        =  $tds[$pos]->value if ($tds[$pos]);
	
          if (! $url)
          {
            #if no url, get a best guess url       
            foreach  my $td (@tds)
            {
               if ($td->value)
	       {
                  $url  = $td->value if ($td->value=~ /^http:(.*)/i);
               }
            }
          }
	}
	if ($url)
	{	    
	    my $simplegrepobj = new Types::SimpleGrep($url,$testname,$params);
	    $simplegrepobj->test($withxml);
            $self->{message}  = $simplegrepobj->getMessage();
            $self->{status}   = $simplegrepobj->getStatus(); 
	}
    }
}
1;
