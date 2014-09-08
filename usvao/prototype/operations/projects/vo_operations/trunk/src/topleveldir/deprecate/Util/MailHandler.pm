#
#
# Use this to send mail
# on services that should be
# deleted from your db.
#
#
#

package Util::MailHandler;

use Text::Wrap;
$Text::Wrap::columns = 72;

use Exporter();
@ISA = qw(Exporter);

use strict;


sub new
{
    my ($class)  = shift;
    my $objref = {};
    bless $objref, $class;
    $objref->init(@_);
    return $objref;
    
}
sub get_date
{
    my $t = time();
    my $adjust = 60*60*24*14;
    $t = $t+$adjust;
    my ($year, $month,$day)= (localtime($t))[5,4,3];
    $year += 1900;
    $month += 1;
    return "$year/$month/$day";

}
sub init
{
    my $self = shift;
    $self->{email} = $_[0];
    $self->{array} =  $_[1];
    $self->{mailtype} = $_[2];
    
    my $array = $self->{array};   
    my $size = scalar(@$array);
    $self->{new_date}  = get_date();
    
    if ($self->{mailtype})
    {
	$self->create_standard_message($size, $array);
    }
    else
    {
	$self->create_special_message($size,$array);
    }
}
sub create_special_message
{
    my $self = shift;
    my ($size,$array) = @_;
    my $a;
    $a = <<"        EOF";
	The VAO has been monitoring and validating all VO registered Cone
        Search and SIA services.  To ensure that users of  the VAO registry
        (available at http://nvo.stsci.edu/vor10/index.aspx) can filter out
        obsolete and non-working services we are planning to set the registry's 
        internal compliance flags to note services that are not responding to
        our validation queries.

        These services will still be registered but users and other services
        that query the registry  can request only services where the flag
        indicates that the service is responsive. We anticipate that VAO
        services will typically apply this filter.
        EOF

      
      if ($size > '1')
      { 
        $a .= "\nWe have noted that the following data services for which you are the
        responsible party with IVO identifiers\n";

        $a =~ s/^[^\S\n]+//gm;
        $a .= "\n";
           foreach my $id (@$array)
           {
	      $a .= "$id\n";
           }
           $a .= "\n";
            my $b = "\ndo not seem to respond to our validation queries.  Unless we hear\n
           from you we are anticipating marking these services as non-responsive\n
           soon after $self->{new_date}. Please get in touch with us if you feel that\n
           flagging these services is inappropriate or premature or if you need\n
           further information.   Services that are deprecated will be reviewed\n
           monthly and we will automatically restore them if and when we find them\n
           responding to our validation queries.\n";
       
           $b =~ s/\s+(.*)\n/$1\n/g;
           $b .= "\nRegards,\nMichael Preciado\nmichael.e.preciado\@nasa.gov\nVirtual Astronomical Observatory\n";
           $a .= $b;
           $self->{body} = $a;
      }
      else
      {
	$a .= <<"        EOF"; 
        We have noted that the following data service for which you are the
        responsible party with IVO identifier
        EOF

           $a =~ s/^[^\S\n]+//gm;
           $a .= "\n";
           foreach my $id (@$array)
           {
	      $a .= "$id\n";
           }
           $a .= "\n";
            my $b = "\ndoes not seem to respond to our validation queries.  Unless we hear\n
           from you we are anticipating marking this service as non-responsive\n
           soon after $self->{new_date}.  Please get in touch with us if you feel that\n
           flagging this service is inappropriate or premature or if you need\n
           further information.   Services that are deprecated will be reviewed\n
           monthly and we will automatically restore them if and when we find them\n
           responding to our validation queries.\n";
       
           $b =~ s/\s+(.*)\n/$1\n/g;
           $b .= "\nRegards,\nMichael Preciado\nmichael.e.preciado\@nasa.gov\nVirtual Astronomical Observatory\n";
           $a .= $b;
           $self->{body} = $a;
      }


}
sub create_standard_message
{
    my $self = shift;
    my ($size,$array)  = @_;
    my $a = << "    EOF";
    The VAO has been monitoring and validating all VO registered Cone
    Search and SIA services.  To ensure that users of  the VAO registry
    (available at http://nvo.stsci.edu/vor10/index.aspx) can filter out
    obsolete and non-working services we are planning to set the registry's 
    internal compliance flags to note services that are not responding to
    our validation queries.

    These services will still be registered but users and other services
    that query the registry  can request only services where the flag
    indicates that the service is responsive. We anticipate that VAO
    services will typically apply this filter.
    EOF
    
    $a .= <<"        EOF"; 
        
    We have noted that the following data service for which you are the
    responsible party with IVO identifier
        EOF

     $a =~ s/^[^\S\n]+//gm;
     $a .= "\n";
     foreach my $id (@$array)
     {
	$a .= "$id\n";
     }
     $a .= "\n";
     my $b = "\ndoes not pass the default validation queries. Attached you will find
     a list of centers and the validation errors associated with each.
     Please find your center in this list and note that we have provided
     both the error types and the identifiers we tested.The errors for each
     identifier can be determined by crossreferencing the column number
     containing the 'X' with the number of the error message. For your
     convenience, we have also included a chart that suggests a possible
     solution for each error type.
     
     Unless we hear from you we are anticipating marking this service as
     non-responsive soon after $self->{new_date}.  Please get in touch with us if you
     feel that flagging this service is inappropriate or premature or if you
     need further information.   Services that are deprecated will be 
     reviewed monthly and we will automatically restore them if and when
     we find them responding to our validation queries.\n";
       
     $b =~ s/^[^\S\n]+//gm;
     $b .= "\nRegards,\nMichael Preciado\nmichael.e.preciado\@nasa.gov\nVirtual Astronomical Observatory\n";
     $a .= $b;
   
     $self->{body} = $a;
   
}
sub send_mail
{
    
    my $self= shift;
    my $from = "Michael.E.Preciado\@nasa.gov";
    my $to =  "Michael.E.Preciado\@nasa.gov";
    my $path = "/software/jira/software2/projects/vo_operations/trunk/src/topleveldir/deprecate/";
    my $ent = MIME::Entity->build(From    => $from,
			      To      => $to,
			      Subject => "VAO Registry to mark Non-Compliant Services: $self->{email}", 
			      Data    => $self->{body});
    $ent->attach(Path  => "$path/validation_errors_map.xls",
		 Type  => "application/msexcel",
		 );
    $ent->attach(Path  => "$path/list.0824.xls",
		 Type  => "application/msexcel",
		 );
    
    my  $sender = Mail::Send->new;
    foreach ($ent->head->tags)
    {       # give the sender our headers
	$sender->set($_, map {chomp $_; $_} $ent->head->get($_));
    }
    my $mailer = $sender->open('sendmail');
    $ent->print_body($mailer);
    $mailer->close;





     
    #my  $mailer = Mail::Mailer->new("sendmail");
    #$mailer->open({From    => "Michael.E.Preciado\@nasa.gov",
	#	   To      => "Michael.E.Preciado\@nasa.gov",
		#   Subject => "VO Service(s) marked for deletion     $self->{email}",
	       #})|| die "can't open mail\n";
     
    #print $mailer $self->{body};
    
    #print $self->{body};
   # $mailer->close;
    
}
1;
