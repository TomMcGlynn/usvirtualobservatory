# a new script used to build different types of table
# objects. 
#
#
#
package HTML::DisplayTable;
use HTML::Parent;
use Util::TableTools;
use HTML::Util;
use  Tie::IxHash;
use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw(load_types);
use DBI;


use warnings;

sub new
{
    my ($classname) = shift;
    my $objref = {};
    bless $objref,$classname; 
    $objref->initialize(@_); 
    return $objref;
}
sub newreader
{
    my ($classname) = shift;
    my $objref = {};
    bless $objref,$classname; 
    $objref->initializereader(@_);
    return $objref;
}
sub initializereader
{
    my $self = shift;
    $self->{titleobj}   = $_[0];
    $self->{_sqlquey} = $_[1];
    $self->{_cnames}  = $_[2];
    $self->{_con}     = $_[3];
        
    
    $self->{_response} = getfiledata();
   # my $g = 
    
}
sub initialize
{
    my $self = shift;
    $self->{titleobj} = $_[0];
    $self->{_sqlquey} = $_[1];
    $self->{_cnames}  = $_[2];
    $self->{_con}     = $_[3];
    $self->{_cnotes}  = $_[4];
   
    my $colfunc       = $self->{_con}->{'colfunc'};
    my $ignoretypes   = $self->{_con}->{ignore};
    my $size =        keys %$ignoretypes;
    $self->{top}      = &$colfunc($self);
    my $q             = "SQL::Queries::";
    $q               .= $_[1];
    my $response = &$q($self->{_con}); 
    #post process array of arrays to exclude types
    #that we do not want to display.
    postprocess_table($response,$ignoretypes) if ($size > 0);
    $self->{_response} = $response;       
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
sub getcnames
{
    my $self = shift;
    return  $self->{_cnames};
}
sub displayTable
{
    my $self        = shift;
    my $table       = $self->{_response};
    my $cnames      = $self->{_cnames}; 
    my $topline     = $self->{top};
    
    my $title       = $self->{titleobj}->get_title();
    my $valcode     = $self->{_con}->{'valcode'};
    my $show        = $self->{_con}->{'show'};
    my $func        = $self->{_con}->{'func'};  
    my $summarycode = $self->{_con}->{summarycode};    
    my $sid         = $self->{_con}->{'sid'};
    my $runid       = $self->{_con}->{'runid'};
    my $index       = $self->{_con}->{'index'};
    my $switch      = $self->{_con}->{'switch'};
    my $ob          = $self->{_con}->{'orderby'};
    my $names       = load_types();
    $self->{_con}->{'loadtypes'} = $names;
    my $size        = scalar(@$cnames);
    
    #add all notes to page;
   
    add_notes($title); 

    #add table title
    print "\n<table class = \'tac\' align = \'center\' border = 1  id = statustable>";
    print "<tr class = \'titleblue\'><td colspan = $size>$title</td></tr>";   

  
    #change values when using getcenter.pl code
    if (($self->{_con}->{summarycode}) and (! $self->{_con}->{error}) and ($show ne 'details') and ($show ne 'oldtests'))
    {	
       
	$self->{_con}->{valcode}  = $self->{_con}->{summarycode};
	$valcode = $summarycode;
	if ($0 =~ /(.*?)vresults.pl(.*)/)
	{	    
	    $self->{_con}->{valcode} = $::valcode;
	    $valcode =  $::valcode;
	}
	else
	{
	    $valcode = $summarycode;
	}	   
    }
  
    #write columns names    
    write_colnames($topline,'greenln');
    

    #write column notes
    my $newline = readnotes($self);
    write_colnames($newline, 'greenln');
    
    my $have_deleted;
   
    
    if (scalar(@$table == '0'))
    {
	
      	my $class = 'greenln';
        my ($line,$deleted) = &$func($r,$self);
	write_line($line,$class);
    }
    else
    { 
	
	
	#print "I supp";
	my $counter = 0;
	my $c = 0;
	my $g = 0;
	#print scalar(@$table), "<br>";
	foreach my $r(@$table)
	{
	    my $class = 'greenln';	    
	    $g++;
	    my  ($line,$deleted,$deprecated) = &$func($r,$self); 
	    
	    if ($deleted)
	    {
		
		#$self->setdelete();
		$class              = 'deleted';	       
		if ($counter == '0')
		{
		    print "<tr bgcolor = 'gray'>";
		    print "<td align = center colspan = 11>Deleted identifiers</td></tr>";
		    $counter++;
		}
	    }
	    if ($deprecated)
	    {
		
		$class = 'deleted';
		if ($c  == '0')
		{
		    print "<tr bgcolor = 'gray'>";
		    print "<td align = center colspan = 11>Deprecated identifiers</td></tr>";
		    $c++;
		}
	    }
	   
	    write_line($line,$class);
	    
	}
    }
    print "</table>";
    append_addons($title,$self); 
    print "<br>";
    append_comments($title);
    print "<br><br>";      
}
sub append_comments
{
    my ($title) = @_;
    
    if (($title =~ /Current(.*)/)  || ($title =~ /Last(.*)/))
    {
	print "<table class = 'tac' align = center>";
	
	print "<tr class = greenln><td align = left><a name = 'notes'>*Shortname - links to short description of services</td></tr>";
	print "<tr class = greenln><td align = left>*Ivoid- Links to full description of resource in the STScI registry. </td></tr>";
	print "<tr class = greenln><td align = left>*Status - links to the latest validation  result for the resource</td></tr>";
	print "<tr class = greenln><td align = left>*serviceURL- this is a link to the base url for a service. This is only meant to be copied. </td></tr>";
	print "<tr class = greenln><td align = left>*runtest - run a real-time validation test on the service;the directory link runs the VAOResource validator </td></tr>";
	print "</table>";
	print "<br>";
    }
	
}
sub readnotes
{
    my ($self) = @_;
    my $cnames = $self->{_cnames};
    my $cnotes = $self->{_cnotes};
    my $line = [];
    my $show  = $self->{_con}->{show};
    if ((($show ne "details") and ( $show ne "oldtests")) || ($self->{_con}->{querystring}) || 
	($self->{_con}->{error}))
    {
	push @$line, "<td>links to details</td>";
    }
    my $sid = shift @$cnames;
    foreach my $n (@$cnames)
    {
       
	if (! exists ($cnotes->{$n}))
	{
	  
	    push @$line, "<td></td>",
	}
	else
	{
	    push @$line, "<td><a href = '#notes' style = 'color: green'>$cnotes->{$n}</td>";	    
	}
    }
    return $line;

}
sub append_addons
{
    my ($title,$self) = @_;
  
    my $switch = $self->{_con}->{switch};
    my $runid = $self->{_con}->{runid};
    my $sid = $self->{_con}->{sid};
    my $valcode = $self->{_con}->{valcode};
    my $show = $self->{_con}->{show};


     if ($have_deleted)
    {
	#print "<br>";
	#print "<table class = tac align = center><tr class = greenln>";
	#print "</tr></table>\n";
    }
    
    add_oldtestbox($runid, $sid, $valcode) if (($show eq 'details') and ($switch ne "yes"));  
}
sub add_notes
{
    my ($title) = @_;
    if (($title =~ /Current(.*)/) || ($title =~ /Last(.*)/))
    {
     
	print "<table class = 'tac' align = center>";
       
	print "<tr class = greenln><td align = left>*Click on \"links to details\" for more information </td></tr>";
	print "</table>";
	print "<br>";
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
sub fixcolname_latesttest
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
  
    my $newindex;
    $newindex = 'asc' if ($index eq 'desc');
    $newindex = 'desc' if ($index eq 'asc');

    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {	
	my $icon;
	$icon = '&uarr;' if ($newindex eq  'asc');
	$icon = '&darr;' if  ($newindex ne 'asc');
	$icon = ''  if (($$cnames[$i] ne $ob) || ($$cnames[$i] eq 'runtest')); 
 	$$cnames[$i] = "<td><a href = \'$valcode?show=oldtests&sid=$sid&orderby="
	               . "$$cnames[$i]&index=$newindex\'>$$cnames[$i] $icon</a></td>";	
    }
 
    return $cnames;
}
sub add_oldtestbox
{
    my ($runid, $sid, $valcode) = @_;
    print "<br><br>";
    print "<table class = \'tac\' align = center  border = '1'>";
    print "<tr class = greenln><td><a href =  \'$valcode?" .
	"show=oldtests&sid=$sid&runid=$runid\'>View All Tests</td></tr>";
    print "</table>"; 
}
sub fixcolname_services
{    
 
    my ($self)   = @_;
    my $cnam = $self->{_cnames};
   
    my $valcode  = $self->{_con}->{'valcode'};
   
    my $show     = $self->{_con}->{'show'};
    my $type  = 'show';
   
    if ($self->{_con}->{'querystring'})
    {
	$type = 'querystring';
	$show = $self->{_con}->{'querystring'};
    }
       
    my $index    = $self->{_con}->{'index'};
    my $sid      = $self->{_con}->{'sid'};
    my $ob       = $self->{_con}->{orderby};
    my $error    = '';
    my $summarycode = $self->{_con}->{summarycode};
 
    if ($self->{_con}->{error})
    {
	$error       = "&error=" .  $self->{_con}->{error};
	$valcode = $summarycode;
    }
    my $newindex;   
    $newindex = 'asc' if ($index eq 'desc');
    $newindex = 'desc' if ($index eq 'asc');

  
    my $array =[];
    for (my $i = 0; $i<scalar(@$cnam);$i++)
    {	
	my $icon;
	my $entry = $$cnam[$i];
	$icon = '&uarr;' if ($newindex eq  'asc');
	$icon = '&darr;' if  ($newindex ne 'asc');
	$icon = ''  if (($entry ne $ob) || ($entry eq 'runtest'));     
	$entry = 'type' if ($entry eq 'runtest');
	
	$$array[$i] = "<td><a href = \'$valcode?"
	    . "$type=$show&orderby=$entry&index=$newindex$error\'>"
	    . "$$cnam[$i]  $icon</a></td>";	
    }
    return $array;
}
sub determine_color
{
    my ($status) = @_;
    my $color;
    if (($status) and (($status eq "fail") || ($status eq "skip")))
    {
	$color = "<font style = \"color: red\">";
    }
    return $color;
}
sub allids_html
{    
    my ($r,$self) = @_;  

    
    if (! $r){return $r, '';}
    
    
    my $valcode     = $self->{_con}->{'valcode'};
    my $show        = $self->{_con}->{'show'};
    
    my $names       = $self->{_con}->{loadtypes};
   
    #"<br>";
    my $curator     = $self->{_con}->{'curator'};   
    my $deprecated  = pop @$r;
    my $deleted     = pop @$r;
    my $runid       = pop @$r;

    my $radius      = $$r[9];
    my $testdec     = $$r[8];
    my $testra      = $$r[7];    
    my $sid         = $$r[0];
    my $identifier  = $$r[6];   
    
  my $type        = $$r[5];
   
    my $shortname   = $$r[1];
    my $time        = $$r[3];
    my $url         = $$r[4];
    
    #print "@$r<br>";
    #print "$sid, 
    #$runid,
    #print "$identifier<br>";
    #,$type,$radius,$deleted<br>";
    my $status      = 'not validated'; 
    $status         = $$r[2] if $$r[2];
    my $color       = determine_color($status);
    $color          = '' if (!$ color);
    
    if (exists ($$names{$type}))
    {
	$type = $$names{$type};
    }
    else  { print "Type not there: $runid, $identifier<br>";
    }
    my $runurl = 'undefined';
    
    my $id = encode($identifier);
   
    if (! ($curator) or ($curator  eq 'off'))
    {

       #---uncomment these lines when you only want to run a real time test and not store anything---#
       #only provide a real time test where the results do *not* get stored
       #my $service      =  HTML::Parent::init($r,$type);
       #$runurl          =  $service->getvalurl;
       #my $voresource_testurl = $service->getvoresourceurl;    
       #$runurl          = "<a href = $voresource_testurl>directory, </a>";
       #$url  =~ s/\&/\|/g;
       #$url = encode($url);
       $runurl           =  "<a href = ./run_test.pl?id=$id&type=$type>$type</a>"; 
       $runurl = "<a href = ./tap.pl?url=$url>$type</a>" if ($type eq 'TAP');
     }	
    else 
    {
       #----uncomment the line below if you wish to store results -----#
       ##alllow users to store test results
       #$runurl =  "<a href = ./run_test_and_store.pl?id=$id>$type</a>";
    }
  

    $runurl             = '' if ($runurl eq  'undefined');
    #redefine array elements with html and create a new array
    my $array = [];
   
    $shortname           = "<td align  = \'left\'><a href = '$::vometadata?id=$identifier'"
	                . "target = \"_blank\">$shortname</a></td>";
    if (!$runid)
    {
	$status       = "<td>$color $status</td>";
	$time         = "<td>null</td>";
    }
    else
    {	
        $status       = "<td><a href = \'$valcode?show=details&sid=$sid&runid="
	                 . "$runid&switch=no\'>$color $status</td>";
        $time         = "<td>$time</td>"; 
    }        
    $url              = "<td><a href  =  \'$url\'>url</td>";
    $runurl           = "<td><a href = \'$runurl\'>$runurl</td>";       
    $type           = "<td align = \'left\'>$type</td>";
    $identifier  = "<td align = \'left\'>"
	. "<a href =  http://vao.stsci.edu/directory/getRecord.aspx?id=$identifier>$identifier</a></td>";
    $testra       = "<td>$testra</td>";
    my $img =  "<td><img src = '/vo/valtest/doc/votablebar.jpeg' width = '2' height  = 100></td>";
    $testdec           = "<td>$testdec</td>";
    $radius          = "<td>$radius</td>";
    $sid = "<td>$sid</td>";

    #print "$sid,$shortname, $identifier, $status, $time,$type,$testra,$testdec,$radius";
    $array = [$sid, $shortname, $identifier, $status, $time,$url,$runurl,$type, $testra,$testdec,$radius];
    return ($array,$deleted,$deprecated);    
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
	$r = encode_array($r);
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


sub latesttests_html
{
    my ($r, $self) = @_;
    my $valcode = $self->{_con}->{valcode};   
    my $types   = $self->{_con}->{loadtypes};    
    my $deleted;
    my $runid;
    
    my $sid;
    $r = encode($r);
    #a href = \'$valcode?show=details&sid=$sid&runid=$runid&switch=yes\'>
    $$r[0]  = "<td>$$r[0]</td>";
    $$r[1]  = "<td>$$r[1]</td>";
    $$r[2]  = "<td>$$r[2]</td>";
    $$r[3]  = "<td align = left>$$r[3]</td>";
    $$r[4]  = "<td align = left>$$r[4]</td>";
    $$r[5]  = "<td align = left>$$r[5]</td>";
    $$r[6]  = "<td align  = left>$$r[6]</td>";
    
    return $r,$deleted;
}
sub oldtests_html
{
    my ($r, $self) = @_;
    my $valcode = $self->{_con}->{valcode};   
    my $types   = $self->{_con}->{loadtypes};    
    my $deleted;
    $r = encode_array($r);
    my $runid    = $$r[1];
    my $sid      = $$r[0];

    if (exists ($types->{$$r[3]}))
    {
	$$r[3] = $types->{$$r[3]};
    }
    $$r[0]  = "<td>$$r[0]</td>";
    $$r[1]  = "<td>$$r[1]</td>";
    $$r[2]  = "<td><a href = \'$valcode?show=details&sid=$sid&runid=$runid&switch=yes\'>$$r[2]</td>";
    $$r[3]  = "<td align = left>$$r[3]</td>";
    $$r[4]  = "<td align = left>$$r[4]</td>";
    return $r,$deleted;
}
sub special
{
    my ($r, $self) = @_;
    my $summarycode = $self->{_con}->{summarycode};
    my ($deleted,$override);
    my $show = $self->{_con}->{show};
    if (! $r)
    {
       	
	$$r[0] = "";
	$$r[1] = "null";
	$$r[2] = "null";

    }

    my $e = $$r[0];   
    $r  = encode_array($r);  
    $$r[0] = "<td><a href = \'$summarycode?show=$show&error=$e \'>$$r[0]</td>";
    
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
sub load_types
{
    my %names;
    
    open ("Types", "$::loadtypes")	
	|| die "cannot load types";
   
    while(<Types>)
    {
	chomp $_;
	next if ($_ =~ /^(\#|\s)(.*)/);
       
	my ($type, $shortname)=  (split /\,/,$_);
	$names{$type} = $shortname;
    }
    close Types 
	|| die  "cannot close types";
    return \%names;
}
1;
