#
#
#
#
#
package Table::LoginBox;
use strict;

sub new
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub init
{ 
  my $self = shift;
  my $pars    = shift;
  $self->{pars} = $pars;
  my $sub  = "get_box_"; 
  if (! $pars->{showdeletes} and !$pars->{login}){
  $sub .= "login";
  }
  if ($pars->{login} and ! $pars->{showdeletes}){
  $sub .= "logout";
  }
  if ($pars->{showdeletes}){
     $sub .= "return";
  } 
  my $subref = \&$sub;
  $self->$subref();
}
sub get_box_logout
{
   my $self = shift;
   $self->{box} = << "    EOF";
     <div style='position:absolute; width: 356px; top: 100px; height:60px; left: 160px; padding: 0px; border:1px solid #00cccc;
     background-color: #FFFFCC; font-size: small; margin:10px; text-align:center;'>
      <img src='http://vaoportaltest.ncsa.illinois.edu/images/vao-si.ico' align='left' style='margin-right: 5px;'>
    <font style = 'color:green'><p style  = 'text-align:left;'> Logged In  &nbsp;&nbsp;&nbsp; User: &nbsp;$self->{pars}->{uid}<br></p>
    <input type = 'button' value = 'Logout' onclick = \"window.location.href='$::root/logout.pl?';">
    <input type = 'button' value = 'Add a Notice' onclick = "window.location.href='$::root/composenote.pl?';">
     <input type = 'button' value = 'Show Expired Notices' onclick ="window.location.href='$::root/displaynote.pl?showdeletes=yes';">
    </div><br><br><br>
    EOF
}
sub get_box_login
{
     my $self = shift;
     $self->{box}  =  << "    EOF";
    <div style="position:absolute; width: 360px; top: 100px; height: 60px;left: 160px; padding: 0px; border:1px solid #00cccc; 
    background-color: #FFFFCC; font-size:  small; margin:10px; text-align:center;">
    <img src="http://vaoportaltest.ncsa.illinois.edu/images/vao-si.ico" align="left" style="margin-right: 5px;"/>
         <font style = "color:green"><p style = 'text-align:left;'>Add/Delete notes &nbsp;&nbsp;Status: Logged Out</p>
        <div align = left>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
         <input type = 'button' value = 'Login' onclick = "window.location.href='$::root/login.pl?';">
         <input type = 'button' value = 'Show Expired Notices' onclick ="window.location.href='$::root/displaynote.pl?showdeletes=yes';"></div>
    </div><br><br><br>
    EOF
}
sub get_box_return
{
  my $self = shift;
  my $pars = $self->{pars};
  my $status;
   my $user = "";
   $status = 'Out' if (! $pars->{login});
   $status = 'In' if ($pars->{login});
   $user = "User: &nbsp; $self->{pars}->{uid}" if ($pars->{login});
  $self->{box}  =  << "    EOF";
             <div style='position:absolute; width: 230px; top: 100px; height:60px; left: 160px; padding: 0px; border:1px solid #00cccc; 
               background-color: #FFFFCC; font-size: small; margin-left:10px; text-align:center;'>
               <img src='http://vaoportaltest.ncsa.illinois.edu/images/vao-si.ico' align='left' style='margin-right: 5px;'>
             <font style = 'color:green'><p style = 'text-align:left;'>Logged $status  &nbsp;&nbsp;&nbsp; $user<br></p>
             <input type = 'button' value = 'Return to Notices home' onclick = "window.location.href='$::root/displaynote.pl?';">
             </div><br><br><br>
    EOF
}
sub printbox
{
   my $self = shift;
   print $self->{box};

}
1;
