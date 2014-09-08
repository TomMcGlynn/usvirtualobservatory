#
#
package Util::ByteDumper;

use Scalar::Util qw (tainted);
use LWP::UserAgent;
our $contents;
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
    my $url  = $_[0];
    if ($url =~  m/^\<\!\[CDATA\[(.*?)\]\]\>/)
    {
	$self->{url}  = $1;
	$self->download_bytes_cdata;
    }
    else
    {    
	$self->{url} = $_[0];
	$self->download_bytes; 
    }
}
sub download_bytes
{
	my $self = shift;
        my $ua  = LWP::UserAgent->new();
        my $image = $self->{url};
        my $res  = $ua->get($image,
			    ':read_size_hint' => 100,
			    ':content_cb' => \&callback,
			    );
	$self->{res} = $res;
}
sub get_response
{
    my $self =  shift; 
    return $contents;
}
sub getRes
{
    my $self = shift;
    return $self->{res};

}
sub download_bytes_cdata
{
    my $self = shift;
    my $ua = LWP::UserAgent->new();
    foreach $key (sort keys(%ENV))
    {
	my $t = tainted($ENV{$key});
	if ($t)
	{                   
	    #printf("%-10.10s: <br> PR $key $ENV{$key} is tainted ");print "<br><br>";
	    untaint($key);	    
	    my $res  = $ua->get($self->{url},
				':read_size_hint' => 100,
				':content_cb' => \&callback,
				);
	    $self->{res} = $res;
	}
        next if (! $contents);
	last if (length($contents) > 100);       
    }
}
sub callback
{
    my ($data, $response, $protocol) = @_;
    $contents  .= $data;
    die if length($contents) > 10;
}
sub untaint
{
        my $string  = shift;
        my $value = $ENV{$string};
        $value =~ /(.*)/;
        $ENV{$string} = $1;
}
1;

