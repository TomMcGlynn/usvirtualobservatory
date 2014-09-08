# used to build different types of table
# objects. 
#
#
#
package HTML::DisplayTable;


use  Tie::IxHash;
use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw(load_types);
use DBI;
use Tie::IxHash;
use HTML::Top;
use HTML::BuildButton;
use HTML::Banner;

use warnings;

sub new
{
    my ($classname) = shift;
    my $objref = {};
    bless $objref,$classname; 
    $objref->initialize(@_);
    return $objref;
}
sub initialize
{
    my $self = shift;
    $self->{titleobj}   = $_[0];
    $self->{_cnames}  = $_[2];
    $self->{_con}     = $_[3];
    if (ref($_[1]) eq 'ARRAY')
    {       
	$self->{_response} = $_[1];
    }
    else
    {	
	$self->{_sqlquey} = $_[1];
	my $q             = "SQLVo::Queries::";
	$q               .= $_[1];
	my $response = &$q($self->{_con});
	$self->{_response} = $response;   
    } 
}
sub getContainer
{
    my $self = shift;
    return $self->{_con};
}
sub getTitle
{
    my $self  = shift;
    return $self->{_title};
}
sub getQuery
{
    my $self = shift;
    return $self->{_sqlquery};
}
sub getColumns  
{
    my $self =shift;
    return $self->{_cnames};
}
sub getResponse
{
    my $self =shift;
    return $self->{_response};    
}
sub displayTable
{
    my $self        = shift;
    my $table       = $self->{_response};
    
  
    #print "Content-type: text/html\n\n";  
    
    my $size        = scalar(@{$self->{_cnames}});
    my $title       = $self->{titleobj}->get_title();
    my $valcode     = $self->{_con}->{'valcode'};
    my $show        = $self->{_con}->{'show'};
    
    my $func        = $self->{_con}->{'func'};
  
    my $summarycode = $self->{_con}->{summarycode};
    my $colfunc     = $self->{_con}->{'colfunc'};
    
    #add all notes to page;
    
    add_notes($title); 

    
    
    my $have_deleted;
    if (exists $self->{reprocessed})
    {
	
	#add table title
	print "<table class = \'tac\' align = \'center\' border = 1>";
	print "<tr class = \'titleblue\'><td colspan = $size>$title</td></tr>";    
	#write columns names
	my  $line  = &$colfunc($self);
	write_colnames($line,'greenln');
	$self->display_reprocessed();
    }
    else
    { 
	$self->display();
    }
}
sub display_reprocessed
{
    my $self = shift;
    
    my $reprocessed = $self->{reprocessed};
    foreach my $n (sort keys %$reprocessed)
    {      
	my $inner = $reprocessed->{$n};
	my $status = $inner->{overall};
	my $font = determine_color($status);
	
	print "<tr class = greenlnextra><td>$n";
	print "<td>$inner->{type}</td>";
        print "<td><a href = '$::valcode?show=$n&orderby=time&type=oldtests&index=desc'>$font $inner->{overall}</td>";
	print "<td>$inner->{percent}</td>";
	print "<td>$inner->{time}</td>";
	print "<td><a href= \"$::valcode?show=uptime&sid=$inner->{serviceId}\">view</td>";
	print "</tr>";	
    }
    print "</table>";
    print "<br><br>";
}
sub display
{   
    my ($self)= shift;
    my $table = $self->{_response};
    
    my $size  = scalar(@$table);
    my $func  = $self->{_con}->{'func'};
    
    my $sizefields        = scalar(@{$self->{_cnames}});
    
    if ((scalar(@$table)  == '0') and ($self->{_con}->{type}))
    {		
	#type exists when using offset keyword in renderer		    
	require HTML::ErrorServiceNotFound;
	my $error = new HTML::ErrorServiceNotFound("Offset value out of range");
	$error->display();
	exit();
    }
    #print_h();
    #print_banner();
    #add table title
    print "<table class = \'tac\' align = \'center\' border = 1>";
    print "<tr class = \'titleblue\'><td colspan = $sizefields >", $self->{titleobj}->get_title(), "</td></tr>";   
 
    #result set size= 0 when clicking on any "pass" link in renderer
    if ((scalar(@$table)  == '0') and (!$self->{_con}->{type}))
    {
	my $class = 'greenln';
	my ($line,$deleted) = &$func($r,$self);
	write_line($line,$class);    
    }
    else
    {
	my $counter = 0;
	
	foreach my $r (@$table)
	{	  
	    my $class = 'greenln';	    
	    
	    my  ($line,$deleted) = &$func($r,$self);  
	    if ($deleted)
	    {
		$have_deleted = 1;
		$class              = 'deleted';	       
		if ($counter == '0')
		{
		    print "<tr bgcolor = 'gray'><td align = center colspan = 11>Row(s) highlighted in gray indicate deleted identifiers</td></tr>";
		    $counter++;
		}
	    } 
	    write_line($line,$class);	    
	}
    }
    print "</table>";
    append_addons($have_deleted,$title,$self) if ($size == '100');    
    print "<br><br>";      
}
sub append_addons
{
    my ($have_deleted,$title,$self) = @_;
   
    my $switch  = $self->{_con}->{switch};
    
    my $show    = $self->{_con}->{show};
   
     
    if ($have_deleted)
    {
	print "<br>";
	print "<table class = tac align = center><tr class = greenln>";
	print "</tr></table>\n";
    }  
    if ( $show ne 'details')
    {
	my $button = new HTML::BuildButton("oldtests",$self);
	$button->display();
    }
}
sub add_notes
{
    my ($title) = @_;
    if ($title =~ /(.*?)Services(.*)/)
    {
	print "<table class = 'tac' align = center>";
	print "<tr class = greenln><td>*Click on a shortname to see the resource metadata. ";
	print "*Click on the status to see the test details. *Click on a column name to order by that column</td></tr>";
	print "</table>";
    }
}
sub fixcolname_uniquetest
{
    my ($self)  = @_;
    
    my $cnames   = $self->{_cnames};
    my $valcode  = $self->{_con}->{'valcode'};
    my $show     = $self->{_con}->{'show'};
    my $index    = $self->{_con}->{'index'};
    my $sid      = $self->{_con}->{'sid'};
    my $ob       = $self->{_con}->{orderby};
    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {	
	$$cnames[$i] = "<td>$$cnames[$i]</a></td>";
    }
    return $cnames;
}
sub fixcolname_oldtest
{   
    my ($self)  = @_;
    my $cnames   = $self->{_cnames};
    my $valcode  = $self->{_con}->{'valcode'};
    my $show     = $self->{_con}->{'show'};
    my $index    = $self->{_con}->{'index'};
    my $sid      = $self->{_con}->{'sid'};
    my $ob       = $self->{_con}->{orderby};
    my $offset   = $self->{_con}->{offset};
   
    my $newindex;
    $newindex = 'asc' if ($index eq 'desc');
    $newindex = 'desc' if ($index eq 'asc');

    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {	
	my $icon;
	$icon = '&uarr;' if ($newindex eq  'asc');
	$icon = '&darr;' if  ($newindex ne 'asc');
	$icon = ''  if (($$cnames[$i] ne $ob) || ($$cnames[$i] eq 'runtest'));
 	$$cnames[$i] = "<td><a href = \'$valcode?show=$show&type=oldtests&orderby="
	               . "$$cnames[$i]&index=$newindex&offset=$offset\'>$$cnames[$i] $icon</a></td>";	
    }
    return $cnames;
} 
sub fixcolname_uptime
{    
    my ($self)   = @_;
    my $cnames    = $self->{_cnames};
    my $valcode  = $self->{_con}->{'valcode'};
    my $show     = $self->{_con}->{'show'};  
    my $sid      = $self->{_con}->{'sid'};
    my $error    = ''; 
 
    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {     
	$$cnames[$i] = "<td>$$cnames[$i]</td>"; 	
    } 
    return $cnames;
}
sub fixcolname_services
{     
    my ($self)   = @_;
    my $cnames    = $self->{_cnames};
    my $valcode  = $self->{_con}->{'valcode'};
    my $show     = $self->{_con}->{'show'};
    my $index    = $self->{_con}->{'index'};
    my $sid      = $self->{_con}->{'sid'};
    my $ob       = $self->{_con}->{orderby};
    
    my $error    = '';
    my $summarycode = $self->{_con}->{summarycode};
    $error       = "&error=" .  $self->{_con}->{error} if ($self->{_con}->{error});
      
    if ($index eq 'desc') { $index = 'asc';}
    elsif ($index eq 'asc'){$index = 'desc';}
  
    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {	      
        my $icon = '&uarr';
	$icon = '&darr;' if  ($index ne 'asc');
	$icon = ''  if ($$cnames[$i] ne $ob);
	$$cnames[$i] = "<td><a href = \'$valcode?"
	    . "show=$show&orderby=$$cnames[$i]&index=$index$error\'>"
            . "$$cnames[$i] $icon</a></td>";    
    }
    return $cnames;
}
sub determine_color
{
    my ($status) = @_;
    my $color;
    if ($status)
    {
	$color = "<font style = \"color: green\">";
	if ($status ne "pass")	    
	{	    
	    $color = "<font style = \"color: red\">";
	}	    
    }
    return $color;
}
sub allids_html
{    
    my ($r,$self) = @_;  
    my $valcode     = $self->{_con}->{'valcode'};
    my $show        = $self->{_con}->{'show'};
 
    my $runid       = pop @$r;
    my $time        = $$r[6];        
    my $name        = $$r[0];
    my $sid         = $$r[1];   
    my $testid      = $$r[2];
    my $testname    = $$r[3];
    my $type        = $$r[4]; 
    my $status      = $$r[5];
       
    my $color       = determine_color($status);
    $color          = '' if (!$ color);    
    $tag            = '';

    #redefine array elements with html
    $$r[0]           = "<td align =  left>$name</td>";
    $$r[1]           = "<td align  = \'center\'>$sid</td>";
    $$r[2]           = "<td>$testid</td>";    
    $$r[4]           = "<td>$type</td>";
    $$r[5]           = "<td><a href = \'$valcode?show=details&sid=$sid&runid="
	            . "$runid&switch=no\'>$color $status</td>";
    $$r[6]           = "<td>$time</td>";     
    $$r[3]           = "<td>$testname</td>";   
    $$r[7]           = "<td>$runid</td>";      
    return ($r,$deleted);    
}
sub uniquetest_html
{    
    my ($r,$self) = @_; 
    my ($deleted,@array);
    my $table = $self->{_response};
   
    if (scalar(@$table) == '0')
    {     
	#if here, service has passed validation
	#just show serviceId, and runid
	$$r[0] =  "<td>$self->{_con}->{sid}</td>";
	$$r[1] =  "<td> $self->{_con}->{runid}</td>";
	$$r[2] = "<td>pass</td>";
	$$r[3] = "<td>null</td>";
    }
    else
    {
	my $s;
	$$r[0]  = "<td>$$r[0]</td>";
	$$r[1]  = "<td>$$r[1]</td>";
	$$r[2]  = "<td>$$r[2]</td>";
	
	$s     =  $$r[3] if ($$r[3]);
	$s      = 'null' if (!$$r[3] || (( $$r[3]) and ($$r[3] eq '')));
	$s     =~ s/</&lt;/g;
	$s     =~ s/>/&gt;/g;
	$$r[3]  = "<td align = left>$s</td>";
    }
    return $r,$deleted;
}
sub uptime_html
{ 
    my  ($res, $self) = @_;
    my $valcode = $self->{_con}->{valcode};   
    my @a = @res;
    my $name = $self->{_con}->{name};
    my $sid =   $self->{_con}->{sid};
 
    my $days  = $$res[0];
    my $hours = $$res[1];
    my $min   = $$res[2]; 
  
    my $r  = [];
    $$r[0] = "<td>$name</td>";
   
    $$r[1] = "<td>$days</td>";
    if (($$res[0] ne 'never up') && ($$res[0] ne 'always up'))
    {     
	$$r[1]  = "<td>$days d $hours h $min m</td>";
    }
    
    return $r,$deleted;
}
sub oldtests_html
{
    my ($r, $self) = @_;
    my $valcode = $self->{_con}->{valcode};   
  
    my $deleted;
    my $runid    = $$r[1];
    my $sid      = $$r[0];

    $$r[0]  = "<td>$$r[0]</td>";
 
    $$r[2] = "<td>$$r[2]</td>";
    $$r[3]  = "<td><a href = \'$valcode?show=details&sid=$sid&runid=$runid&switch=yes\'>$$r[3]</td>";
    $$r[4]  = "<td align = left>$$r[4]</td>";
   
    return $r,$deleted;
}
sub special
{
    my ($r, $self) = @_;
    my $summarycode = $self->{_con}->{summarycode};
    my $deleted;
    my $show = $self->{_con}->{show};

    $$r[0] = "<td><a href = \'$summarycode?show=$show&error=$$r[0] \'>$$r[0]</td>";
   
    $$r[1] = "<td>$$r[1]</td>";
    $$r[2] = "<td>$$r[2]</td>";
    return $r,$deleted;
}
sub write_line
{
    my ($r,$class) = @_;  
    
    print "<tr class = $class>";
    foreach my $n (@$r)
    {	
	print $n;	
    }
    print "</tr>\n";
}
sub write_colnames
{
    my ($r,$class) = @_;
   
    print "<tr class = greenln >"; 
    foreach my $n (@$r)
    {
	print "$n";
    }
    print "</tr>\n";
}
sub reProcess
{
    my $self = shift;
    my $res  = $self->{_response};
    tie my %hash, "Tie::IxHash";
   
    foreach my $n (@$res)
    {	
	my @array = @$n;
	my $pass = 0;
	my $fail= 0;
	my $service = $array[0];

	if (exists ($hash{$service}))
	{	    
  	    my $status = $array[6];
	    my $innerhash = $hash{$service};
	    foreach my $g (keys %$innerhash)
	    {
		my $v = $innerhash->{$g};
		$v++ if ($g eq $status); 
		$innerhash->{$g} = $v;		
	    }
	    $hash{$service}   = $innerhash;
	}
	else
	{
	    my $innerhash = {'pass' => 0,
			     'fail' => 0,
			     'abort' =>0, 
			     'skip' =>0,
			 };

	    my $status = $array[6];
	    $innerhash->{$status}++;	    
	    $hash{$service} = $innerhash;	    
	}
    }
    $self->add_percentages(\%hash,$res);
}
sub add_percentages
{
    my $self = shift;
    my ($hash,$res) = @_;
    foreach my $n (keys %$hash)
    {   
	my $inner           = $hash->{$n};
        my $total           = $inner->{'pass'} + $inner->{'fail'} + $inner->{abort} + $inner->{'skip'};     
	$inner->{overall}   = 'pass';
	$inner->{overall}   = 'fail'  if (($inner->{fail} > 0 ) || ($inner->{abort} > 0));
	$inner->{overall}   = 'skip' if (($inner->{skip} >0) and ($inner->{skip} ==  $total));
	my $passing         =  ($inner->{'pass'} * 100)/$total;
	$inner->{'percent'} = $passing;
    }
    
    foreach my $n (@$res)
    {
	my $g         = @$n[0];
	my $serviceId = @$n[1];
	my $type      = @$n[4];
	my $time      = @$n[6];
      
	if (exists ($hash->{$g}))
	{	 
	    $hash->{$g}->{serviceId} = $serviceId;
	    $hash->{$g}->{type}      = $type;
	    $hash->{$g}->{time}      = $time;
	}  
    }   
    $self->{reprocessed} = $hash;
    my @cnames = ('name', 'type','status','% passing', 'time', 'uptime');
    $self->{_cnames} = \@cnames;
}
1;
