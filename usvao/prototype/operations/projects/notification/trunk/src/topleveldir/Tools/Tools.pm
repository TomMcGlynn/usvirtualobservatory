#
#
#
#
#
package Tools::Tools;



use Exporter;
@ISA= qw (Exporter);
@EXPORT = qw(login_status_out check_login_status get_data_from_cookie decrypt_id encrypt_id
          delete_cookie get_user_id logout login_status_in login_status_incomplete
          gen_encodedfile gen_encryptedfile cleanup encode_session encrypt_session encode_encrypt set_cookie);

use Time::Local;
use CGI::Cookie;
use Table::Message;
use Table::ErrorMessage;
use Table::Layout;
use File::Copy;
use strict;


sub check_login_status
{
     my ($sessionid) = @_;
   #print "LL: $sessionid";
     my $out = `$::vaologin/bin/vaoopenid --config $::vaologin/conf/vaologin-py.cfg -q --session-id $sessionid status 2> /dev/null`;
     my ($string,$status) = ($out =~ m/(\w+)\=(\w+)/);
   #print "C: status : $status"; 
    return $status;
}
sub login_status_out
{
    my $c = shift;
    my $url = `$::vaologin/bin/vaoopenid -s $c->{sessionid}  request $::displaynote 2> /dev/null`;
    $url =~ s/\n/ /g;
    chmod 0777,  "$::tmp/$c->{sessionid}";
    encode_encrypt($c);
    cleanup($c->{sessionid},"1");
    set_cookie($c,$url);
    exit();
}
sub encode_encrypt
{
   my ($c)  = shift; 
   encrypt_session($c);
   my $sessionid = $c->{sessionid};
   chmod 0777,  "$::tmp/${sessionid}.encrypted";
   $c->{'encoded'} = encode_session($c);
}
sub encode_session
{
     my $c   =  shift;
     my $sessionid = $c->{sessionid};
     `$::notificationdir/uuencode -m   $::tmp/${sessionid}.encrypted $::tmp/${sessionid}.encrypted > $::tmp/${sessionid}.encoded`;
     my $encoded = do { local $/; local @ARGV = "$::tmp/${sessionid}.encoded"; <>}; 
     return $encoded;
}
sub gen_encodedfile
{
    my $c  = shift;
    my $sessionid = $c->{'sessionid'};
    open (File,">$::tmp/$sessionid.encoded");
    print File "$c->{encoded}";
    close File;
    chmod 0777,"$::tmp/${sessionid}.encoded";
}
sub gen_encryptedfile
{
    my ($sessionid) = @_;
    my $encrypted = `$::notificationdir/uudecode $::tmp/$sessionid.encoded -o $::tmp/$sessionid.encrypted`;
    chmod 0777,"$::tmp/${sessionid}.encrypted";
}
sub encrypt_session
{
    my $c  = shift;
    my $sessionid = $c->{sessionid};
   my $value = `$::notificationdir/gpg --batch   --no-random-seed-file  --no-tty --symmetric --passphrase $sessionid --homedir=$::gpghome -o $::tmp/$sessionid.encrypted   $::tmp/$sessionid  2>/dev/null`;
}
sub cleanup
{
    my ($sessionid,$true) = @_;
    unlink  "$::tmp/$sessionid.encrypted";
    unlink  "$::tmp/$sessionid.encoded";
    if ($true){ 
        unlink "$::tmp/$sessionid";
    }
}
sub decrypt_id
{
      my $sessionid = shift;
      `$::notificationdir/gpg  --passphrase $sessionid   --homedir=$::gpghome  --decrypt $::tmp/${sessionid}.encrypted > $::tmp/$sessionid 2> /dev/null`;
      chmod 0777, "$::tmp/$sessionid";
}
sub delete_cookie
{
    my ($c,$url) = @_;
    $url = "$::displaynote" if (! $url);
   my $cookie = $c->{cgi}->cookie(-name => 'username',
                        -value => '',
                        -expires=>'-1d',
                        -path=>"$::path",);
    #print $c->{cgi}->header(-cookie =>$cookie,-location => $url);
    print $c->{cgi}->header(-cookie =>$cookie,-location =>$url);
}
sub set_cookie
{
   my ($con,$url)  = @_;
    my ($c,$encoded);
     $encoded = $con->{'encoded'};
     $c = $con->{cgi}->cookie(-name => 'username',
	 		-value => "$con->{sessionid}:$con->{encoded}",
		        -expires=>'+30m',
		        -path=>"$::path",);
    print $con->{cgi}->header(-cookie =>$c,-location =>$url);
    #exit;
}
sub login_status_incomplete
{
     my ($c,$url) = @_;
     my $sessionid = $c->{sessionid};
     my $exitcode = system("$::vaologin/bin/vaoopenid -s $sessionid  process -l 1  '$url'");   
     my $value = $? >> 8;
     #copy ("$::tmp/$sessionid", "$::tmp/$sessionid.foo");
     if ($value == '0')
     {
       cleanup($sessionid); 
       encode_encrypt($c);
       set_cookie($c,$url);
       exit();
     }
     cleanup($sessionid);
     delete_cookie($c);
}
sub get_data_from_cookie
{
    my ($value,$id, $encoded);
    my %cookies = CGI::Cookie->fetch();
   foreach my $n (keys %cookies){   
    if ($n eq 'username')
    {
      $value  = $cookies{'username'}->value;
       my @a = (split /\:/, $value,2); 
        $id = $a[0];
       $encoded = $a[1];
        }
     }
     return $id,$encoded;
}
sub logout
{
   my $c = shift;
   cleanup($c->{sessionid},'1');
   my $url = `$::vaologin/bin/vaoopenid --config $::vaologin/conf/vaologin-py.cfg --session-id $c->{sessionid}  logout $::root`;
   $url =~ s/\n/ /g;
   delete_cookie($c,$url);
   #exit;
}
sub get_user_id
{
    my ($sessionid) = @_;
    my $response = `$::vaologin/bin/vaoopenid --config $::vaologin/conf/vaologin-py.cfg  --session-id $sessionid  status`;
    if  ($response =~ m/openid=(.*)\/(.*?)\s+(.*)/) {
        return $2;
    }
}
1;
