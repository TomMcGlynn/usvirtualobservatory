#
#
#
#
#
package Tools::VOMailer;
use Mail::Mailer;

use Exporter;
@ISA = qw (Exporter);

use strict;


sub new
{
    my ($class) =shift;
    my $hash = {};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
    
}
sub init 
{
    my $self = shift;
    $self->{con} = $_[0];
    

    
    #$self->{currenttime}  = "$self->{con}->{ymdhA} 00";
    #print "none: $self->{currenttime}<br>";
}


sub send_mail
{
    my  $self = shift;
    my $from_address = "Michael.E.Preciado\@nasa.gov";
    my $to_address   = "Michael.E.Preciado\@nasa.gov";
    #my $from_address = "operations\@usvao.org";
    #my $to_address   = "operations\@usvao.org";
    my $priority = $self->{con}->{'prioritystring'};
    #my @array = ("Highest","High", "Medium", "Low");
    #$priority = $array[$priority-1];

    my @anew   = @{$self->{con}->{affectedservices}};    
    my $s      = (join "\n   ",@anew);
    my @bnew   = @{$self->{con}->{otheraffected}};
    my $ss     = (join "\n   ",@bnew);
    my $subject      = "VAO notice posted  $self->{con}->{currenttime} (GMT)";
    my $mailer = Mail::Mailer->new("sendmail");
    $mailer->open({From    => $from_address,
		   To      => $to_address,
                  Subject => $subject,     
               })|| die "can't open mail\n";
    
   print $mailer "The following notice has been posted:\n\n\n"
                  ."$self->{con}->{text}\n\n\n"
                  ."Effective  date: $self->{con}->{niceeff}(GMT)\n"
                  ."Expiration date: $self->{con}->{niceexp} (GMT)\n\n"
                  ."Hostname: $self->{con}->{host}\n\n"    
		  ."Affected VAO Science Services:\n   $s\n"
		  ."Other Services Affected:\n   $ss\n\n" 
		  ."Priority:  $self->{con}->{prioritystring}\n\n\n"
                  ."You may view all VAO notices at:\n\n"
                  ."http://heasarc.gsfc.nasa.gov/vo/vaomonitor\n\n"
                  ."If you wish to delete your notice for any reason, you may do so at:\n\n"
		  ."http://heasarc.gsfc.nasa.gov/vo/notification/\n\n";
    $mailer->close();

}
1;
