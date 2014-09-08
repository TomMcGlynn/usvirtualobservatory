#
#
#
#
use LWP::UserAgent;
use HTTP::Request;
use HTML::TreeBuilder;
use Time::Local;


package  HTML::HarvesterPageScraper;
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
sub getregistry_data
{
    
    
    my $ua = LWP::UserAgent->new();
    my $url = 'http://nvo.stsci.edu/vor10/harvesttable.aspx';
    
    my $response = $ua->get($url);
    my $data = $response->content;
    my $hash = {};
    
    my $tree = HTML::TreeBuilder->new_from_content($data);
    my $table = $tree->look_down('_tag','table','id','HarvesterTable');
    my @rows = $table->look_down('_tag','tr');
    shift @rows;
    my @array;
    foreach my $row (@rows)
    {
        my $n  = ($row->look_down('_tag','td'))[0];
	my $name = $n->as_text;
	$name =~ s/\/\//\//;
	my @a =  (split /\//,$name);
	#print "JJ: $a[1]";
	my $newname =  convert_hostname($a[1]); 
        $newname = $a[1] if (! $newname); 	
	my $tstamp = ($row->look_down('_tag','td'))[3];
	my $time = $tstamp->as_text;
	$hash->{$newname} =  $time;
	
	
    }
    #my $epochtime_1 = convert_to_epoch($array[0]);
    #my $status = verify_timewindow($epochtime_1);   
    return $hash;
}
sub convert_hostname
{
    my ($name ) = @_;
    
    my %hash = (
		'nvo.caltech.edu:8080'             => 'Caltech',
	        'voparis-astrogrid.obspm.fr:8080'  => 'VOParis',
		'nvo.ncsa.uiuc.edu'                => 'NCSA',
		'publishing-registry.roe.ac.uk:80' => 'ROE',
		'astrogrid.ast.cam.ac.uk'          => 'Astrogrid',
		'jvo.nao.ac.jp'                    => 'JVO',
		'dc.zah.uni-heidelberg.de'         => 'DC', 
		'vo.astronet.ru'                   => 'Astronet',
		'rakaposhi.star.le.ac.uk:8080'     => 'Uk',
		'cdsweb.u-strasbg.fr'              => 'Strasburg',
		'registry.euro-vo.org'             => 'EURO VO',
		'heasarc.gsfc.nasa.gov'            => 'HEASARC',
		'msslkz.mssl.ucl.ac.uk'            => 'MSSL',
                'rofr.ivoa.net'                    => 'ROFR',
                'datanet.csiro.au:80'              => 'CSIRO', 		
                'www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca' => 'CADC', 

 	);


    my $newname = $hash{$name};
    return $newname;
}
sub initialize
{
    my $self = shift;
    
    #create a container with
    #information on each harvester.
    
    $self->{reg} =  getregistry_data();
    
    
}
1;
