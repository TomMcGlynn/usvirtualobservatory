#
#
# Use this to send mail
# on services that should be
# deleted from your db.
#
#
#

package Util::MailHandler;

use Exporter();
@ISA = qw(Exporter);
@EXPORT = qw (send_mail);

use strict;
no strict "refs";

sub send_mail
{
    my ($data)  = @_;
    my $handler =  $data->{type};
    my $subname = "$handler" . "_handler";
    &$subname($data);
}
sub parse_registry_resources_handler
{
    my ($data) = @_;
    my $errors = $data->{body};

    my  $mailer = Mail::Mailer->new("sendmail");
    $mailer->open({From    => $data->{sender},
                   To      => $data->{recipient},
                   Subject => "Errors:Registry XML",
               })|| die "can't open mail\n";
    
    foreach my $n (keys %$errors)
    {
	print $mailer "$n\n";  
    }
    $mailer->close;
}
sub update_db_handler
{
    my ($data)       = @_;
    my $list         = $data->{list};
    my $updates      = $data->{updatedservices};
    my $newservices  = $data->{newservices};
    my $todeprecate  = $data->{todeprecate};
    my $subject      = "Registry ids no longer in registry";

    my $mailer = Mail::Mailer->new("sendmail");
    $mailer->open({From    => "Michael.E.Preciado\@nasa.gov",
                   To      => "Michael.E.Preciado\@nasa.gov",
                   Subject => $subject,
               })|| die "can't open mail\n";
   
    print $mailer "**** The following identifiers should be deleted ****\n\n";
    foreach my  $mem (@$list)
    {           
            print $mailer "$mem\n";           
    }
    print $mailer "\n\n\nThe following identifiers are new\n\n";
    foreach my $new (@$newservices)
    {
	my ($ivoid, $service) = @$new;
	print $mailer "$ivoid:\n";
    }
    print $mailer "\n\n\n**** Automatically updated the following services (serviceID,ID)****\n\n";
    foreach my $mem (@$updates)
    {
	my ($id,$url,$serviceId) = @$mem;
	print $mailer "$id\n";
    }
    print $mailer "\n\n\n**** STScI deprecated services (Synched with local db) ****\n\n";
    foreach my $mem (@$todeprecate)
    { 
       print $mailer "$mem\n";
    }
    $mailer->close;

}
sub get_datatypes_handler
{



}
1;
