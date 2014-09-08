#
#
#
#

package Table::NoteDumper;


use strict;
use lib '..';
sub new
{
    my ($class) = shift;
    my $hash = {};
    bless $hash,$class;
    $hash->{notes} = shift;
    $hash->{container} = shift;
    $hash->{size} = keys %{$hash->{notes}};
    $hash->{title} = 'Notices that affect VAO Services';
    return $hash;
}
sub new2
{
    my ($class) = shift;
    my $hash= {};
    bless $hash,$class;
    $hash->{notes} = shift;
    $hash->{container} = shift;
    $hash->{override} = shift;
    $hash->{size} = keys %{$hash->{notes}};
    $hash->{title} =  'Notices that affect services outside of the VAO';
    return $hash;
}
sub print_notes
{
    my ($self) = shift;
    my $notes  = $self->{notes};
    my $c      = $self->{container};
    my $sub    =  ($c->{showdeletes}) ? "format_line_deleted" : "format_line_active";
    my $subref = \&$sub;

    my @ids = sort { $notes->{$a}->{notestatus} cmp $notes->{$b}->{notestatus} 
                     || $notes->{$a}->{epochA}   <=> $notes->{$b}->{epochA} } keys %$notes;
    my $count = 2;
    foreach my $id (@ids)
    {   
        my $t;
        if ($count % 2){
             $c->{css}  = 'noteA';
             $c->{cssnote} = 'noticeA';
        }
        else { 
            $c->{css}  = 'noteB';
            $c->{cssnote} = 'notice'; 
        }
        my $h = $notes->{$id};
        my $line = &$subref($c,$h,$id);
        print $line;
        $count++;
    }
    print "</table>" if (! $self->{override}); 
}
sub format_line_active
{
    my ($c,$h,$id) = @_; 
    my $d = "";
    if ( $c->{login} and $c->{uid}){
	$d  = "<td>&nbsp;</td>";
	if  ($h->{identity} eq  $c->{uid}){
	    my $url = "./deletenote.pl?delete=yes&id=$id";
	    $d  =   "<td><a href = '$url'>delete</a></td>";
	}
    }
    my $line = "<tr class = $c->{css}><td>$h->{decodedeff}</td><td>$h->{decodedexp}</td>"
            ."<td>$h->{notestatus}</td><td>$h->{hostname}</td><td>$h->{priority}</td><td>$h->{identity}</td>" 
            . "$d</tr>\n<tr class = $c->{css}><td></td><td colspan = 10><table class = tac>"
	    . "<tr><td class = $c->{cssnote}><pre wrap = hard><p> $h->{note}</p></pre></td></tr>";
 
   if ($h->{affectedservices} ne '&nbsp;'){
	$line .= "<tr class = $c->{css}><td class = greenlnextra>$h->{affectedservices}</td></tr>";
    }
    if ($h->{otheraffected} ne '&nbsp;'){
	$line .= "<tr class = $c->{css}><td class = greenlnextra>$h->{otheraffected}</td></tr>";
    }
    $line .= "</table></td></tr>";
}        
sub format_line_deleted
{
    my ($c,$h,$id) = @_; 
    my $rd    = $h->{$id}->{reason_deleted};
    $rd       = 'expired' if (! $rd);
    my $line =  "\n<tr class = $c->{css}><td>$h->{decodedeff}</td><td>$h->{decodedexp}</td>"
              ."<td>$h->{notestatus}</td><td>$h->{hostname}</td><td>$h->{priority}</td><td>$h->{identity}</td>"   
              ."<td class = greenln>$rd</td></tr>"
	      ." <tr class = $c->{css}><td></td>"
	      . "<td colspan = 6><table class = tac>"
	      . "<tr><td class = $c->{cssnote}><pre wrap = hard><p>$h->{note}</p></pre></td></tr>";
    if ($h->{affectedservices} ne '&nbsp;'){
	$line .= "<tr class = $c->{css}><td colspan = 5 class = greenlnextra>$h->{affectedservices}</td></tr>";
    }
    if ($h->{otheraffected} ne '&nbsp;'){
	$line .= "<tr class = $c->{css}><td colspan = 5  class = greenlnextra>$h->{otheraffected}</td></tr>";
    }
    $line .= "</table></td></tr>";       
    return $line;
}
sub end_table
{
   my ($self) = shift;
   print "</table><br>";

}
sub gen_table_header
{ 
    my  ($self,$showdel,$container) = @_;
    my $isdt    = (localtime)[8];
    if ($isdt == "0"){
        #isdt == 0 (not daylight time so need to show EST in output)
        $isdt = "EST";
    }
    else {$isdt = "EDT";}
    
    my $zone = $container->{'zone'}; 
    if  (! $zone) {$zone = "none";}
   
 
    if ($self->{size} > 0)
    {
	print "<br>" if  $self->{override};
        print  << "    EOF";
    	<br><br><DIV id = 'zonechosen' value = "$zone"></DIV>
        <table class = tac  width = 800 border = 0 align =center id = note_table >
          <tr class = tblue><td colspan = 10>$self->{title}</td></tr>          
          <tr class = tann><td width = 160><a title = 'The start time of the notice'>Effective Date</a></td>
	    <td  width = 160><a title = 'The Date this notice expires'>Expiration Date</a></td>
	    <td><a title = ''>Status</a></td>
            <td><a title = 'The hostname impacted by this downtime'>Hostname</a></td>
            <td><a title = 'The priority of this notice'>Priority</a></td>
            <td><a title = 'The user who entered the notice'>Identity</a></td>
    EOF
   
    my $col  = get_extracol($container);
    print $col; 
    
     print "<tr class = tann><td/><td class = noticebox>Notice</td><td/><td/><td/><td/>";
     print "<td/>" if ($col); 
     print "<tr class = tann><td/><td>VAO Services Affected</td><td/><td/><td/><td/>";
     print "<td/>" if ($col); 
     print "<tr class = tann><td/><td>Other Services Affected</td><td/><td/><td/><td/>"; 
     print "<td/>" if ($col);
     print "</tr>";  

   
    }
}
sub get_extracol
{
    my ($container) = @_;
    my $col;
    if ($container->{login} and ! $container->{showdeletes})
    {
	$col = "<td>Action</td>";
    }
    if ($container->{showdeletes}) 
    {
	$col = "<td>Reason for Deletion</td>";
    }
    if ($container->{user})
    {
	$col = "<td>\n"
	    ."<a title = \"Delete links are visible for notes originating from the user's host address\">Action</a></td>";
    }
    return $col;
}
1;
