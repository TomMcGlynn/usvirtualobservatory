#
#
#
#
#
package Mail::VOMailer;


use Exporter;
@ISA = qw (Exporter);

use strict;
use warnings;

sub new
{
    my ($class) =shift;
    my $hash = {};
    bless $hash, $class;
    if ($_[2] eq 'crash')
    {
	$hash->init(@_);
    }
    elsif ($_[2] eq 'pass')
    {
	$hash->init1(@_);
    }
    return $hash;
}
sub init1
{
    my $self = shift;
    $self->{servicename} = $_[0];
    my $service          = $_[1];
    my $mailtype         = $_[2];
    my $email            = $_[3];
    $self->{data}        = $service;
    
    foreach my $n ( keys %$service)
    {
	next if (($n eq 'lastval')||($n eq 'overall'));
	my $time = $service->{$n}->{time};
	$self->{time} = format_time($time);
	$self->{zone} = getzone();
    }

    $self->{subject} = "Restored: $self->{servicename}";
    $self->{body}    = $self->generate_success_message();
    my @addresses    = (split /\,/, $email);
    $self->{to}      = shift @addresses;
    $self->{cc}      = join(",", @addresses);
}
sub init
{
    my $self = shift;
    $self->{servicename} = $_[0];
    $self->{service}     = $_[1];
    my $mailtype         = $_[2];
    my $email            = $_[3];  
    $self->{subject}     = "Alert: $self->{servicename}";
    $self->{body}        = $self->generate_crash_message();
    my @addresses        = (split /\,/, $email);
    print "EE: $email\n";
    $self->{to}          = shift @addresses;
    $self->{cc}          = join(",", @addresses);
}
sub generate_crash_message
{   
    my ($self)= shift;
    my $service = $self->{service};
    my $zone = getzone();
    my $body = "A liveness test for the service $self->{servicename} has failed\n\n";
    foreach my $testname (keys %$service)
    {
	next if (($testname eq 'overall') ||($testname eq 'lastval'));
	print "TYPE: $testname\n";
       
	if (($service->{$testname}->{status} eq 'fail') or ($service->{$testname}->{status} eq 'abort'))
	{
	    my $time    = $service->{$testname}->{time};
	    my $timenew = format_time($time);	 
	    $body      .=  "Test run at: heasarc.gsfc.nasa.gov\n $time $zone\n\n";
	    $body      .=  "Subtest type run: $testname\n";	 		
	    $body      .=  "URL tested is: $service->{$testname}->{url}\n"; 	    
	    $body      .= "Error Message:  $service->{$testname}->{errormessage}\n";
	    if ($testname eq 'contains data')
	    {
		$body .= "String to be matched: $service->{$testname}->{params}\n";
	    }
	    $body .= "\n\n\n";
	}
    }
    $body .= << "    EOF";
    You are registered as a responsible party for this service.
    Please check this service.  If the service is down and is restored,
    you should receive a restoration message at the next check
    (typically within one hour).
    EOF
    	
    $body  =~ s/^[^\S\n]+//gm;
    $body  .= "\n";    
    return $body;
}
sub generate_success_message
{
    my ($self)= shift;
    my $body = "A liveness test for the service $self->{servicename} has passed\n\n";
    $body .=  "Test run at: heasarc.gsfc.nasa.gov\n $self->{time} $self->{zone}\n\n";   
    $body .= "\n\nOur latest tests indicate that this service has been restored\n";	
    $body  =~ s/^[^\S\n]+//gm;
    $body  .= "\n";    
    return $body;
}
sub format_time
{
    my ($time) = @_;
    my %monthnum = qw( 01 January 02 February  03 March 04 April 05 May 06 June 07 July 08 
		       August 09 September 10 October 11 November  12 December );
    my @parts = (split / /,$time);  
    my ($year,$month,$day) = (split /\//,$parts[0]);  
    
    $month = $monthnum{$month};    
    return "$month, $day, $year,$parts[1]";
}
sub getzone
{    
    my $num =  (localtime(time))[8];
    return "EDT" if ($num == '1');
    return "EST" if ($num != '1');
}
sub send_mail
{
    my  $self = shift;
    my $from_address = "Michael.E.Preciado\@nasa.gov";
    
  
    #my $from_address = "operations\@usvao.org";
    #my $to_address   = "operations\@usvao.org";

    
    my $mailer = Mail::Mailer->new("sendmail");
    $mailer->open({From    => $from_address,
                   To      => $self->{to},
                   Subject => $self->{subject},
		   Cc      => $self->{cc},
               })|| die "can't open mail\n";
    
    
    print $mailer $self->{body}; 
    $mailer->close();

}
1;
