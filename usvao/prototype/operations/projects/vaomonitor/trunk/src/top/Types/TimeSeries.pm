#
#
#
#
#
package Types::TimeSeries;
@ISA = ("Types::SimpleURLResponse");

use LWP::UserAgent;
use HTTP::Request;
use HTML::TreeBuilder;
use Time::Local;
#use strict;

#use warnings;

sub new 
{

    my ($class) = shift;
    my $hash = {};

    bless $hash, $class;
    $hash->initialize(@_);
    return $hash;

}
sub getsimpleresponse
{
    my ($self) = shift; 
    my $ur     = $self->{url};
    my $ua     = LWP::UserAgent->new();
    my $url    = 'http://vao-web.ipac.caltech.edu/cgi-bin/VAOTimeSeries/nph-vaoTS?location=NGC+3016&radius=10';
    
    #page1
    my $response  = $ua->get($url);
    my $data      = $response->content;
    
    my $tree      = HTML::TreeBuilder->new_from_content($data);
    my $table     = $tree->look_down('_tag','table','border','1','cellpadding', '2');
    my @rows      = $table->look_down('_tag','tr');    
    my @inner     = $rows[1]->look_down('_tag','td');
    my @a         = $inner[4]->look_down('_tag','a');
    my %hash      = $a[0]->all_attr();    
    my $urlmatch  = $hash{href};
    
    #page2
    my $url1       = "http://vao-web.ipac.caltech.edu/$urlmatch";
    my $response2  = $ua->get($url1);
    my $data1      = $response2->content;
    
    my $tree1      = HTML::TreeBuilder->new_from_content($data1);
    my $table1     = $tree1->look_down('_tag','table');
    my @rows1      = $tree1->look_down('_tag','tr');
    my @tds        = $rows1[3]->look_down('_tag','td');
    my @a1         = $tds[1]->look_down('_tag','a');
    my %hash1      = $a1[0]->all_attr();
    my $url2       = $hash1{href};
    
    #page3...xml response
    my $response3 = $ua->get($url2);
    return $response3->content;
}
sub initialize
{
    my $self = shift;
    $self->{url} = $_[0]; 
    $self->{testname} = $_[1];
    $self->{params} = $_[2];
    $self->{desc} = 'time series test';
}
sub showresponse
{
   my $self = shift;	
   my $res = $self->getsimpleresponse();
   print $res;
}
sub test
{
   my $self = shift;
   my $withxml = shift;
   $self->{withxml}  = '1' if ($withxml);
   my $res  = $self->getsimpleresponse();
   my @contents = ($res);
   
   #set defaults
   $self->setMessage("The TimeSeries test has failed. The response does not contain the string to be matched");
   $self->setStatus("fail");
   $self->setId("2.1");
   
   my @grepoutputs = grep (/<TABLEDATA>/,@contents);
   if (@grepoutputs)
   {
       $self->setMessage("The TimeSeries test has passed.");
       $self->setStatus("pass");       
   }
   $self->build_table();
}
1;




