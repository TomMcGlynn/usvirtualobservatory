#
# Author: Michael Preciado
#
# Description: contains routines for adding
#              and parsing notes to the monitoring
#              page
#

package HTML::VOnote;
use HTML::VOMonutil;
use Tie::IxHash;
@ISA = qw(Exporter);
@EXPORT = qw (display_user_notes see_if_active_note determine_note_color  parse_service_notes fix_html
	      organize_notes_current_expired gen_help_text_current_notes output_xml gen_help_text gen_show_table_header
	      decode encode load_notes_array  load_notes nice_date  nice_priority);


####################################
# sub display user notes
####################################
sub display_user_notes
{
    my ($notes,$override) = @_;
    
    my $note_size  = keys (%$notes);
    my $title   = "Notices that affect VAO services";
    $title      = "Notices that affect services outside of the VAO" if ($override);
    my $colname = 'Affected VAO Science Services';
    my $colname1 = "Other Services Affected";
    $colname = '&nbsp;&nbsp;&nbsp;' if ($override);
    $colname1 = "&nbsp;&nbsp;&nbsp;" if ($override);
    
    if ($note_size > 0)
    {
	print "<table class = 'tac' width = 800 align = center border-collapse: collapse id = notes>"
	     ."<tr class = tblue><td colspan = 9>$title</td></tr>"
             ."<tr><td class = \"label\" = center width  = 100>Server Notes</td>"
             ."<td bgcolor = #A49D9D  align = center>Effective Date (GMT)</td>"      
             ."<td bgcolor = #A49D9D align = center>Expiration Date (GMT)</td>"
             ."<td bgcolor = #A49D9D  align = center>Affected Host Machine</td>"
	     ."<td bgcolor = #A49D9D align = center>$colname</td>"
	     ."<td bgcolor = #A49D9D align = center>$colname1</td>"
	     ."<td bgcolor = #A49D9D  align = center>Priority</td></tr>";
    }    

    my @ids = sort { $notes->{$a}->{notestatus} cmp $notes->{$b}->{notestatus} 
                  || $notes->{$a}->{epochA}   <=> $notes->{$b}->{epochA} } keys %$notes;
    foreach my $id (@ids)
    {  
        print "<tr class = $notes->{$id}->{css_class}>"
             ."<td><pre>$notes->{$id}->{note}</pre></td>"
	     ."<td>$notes->{$id}->{decodedeff}</td>"
             ."<td>$notes->{$id}->{decodedexp}</td>"         
             ."<td>$notes->{$id}->{hostname}</td>"
             ."<td>$notes->{$id}->{affectedservices}</td>\n"
             ."<td>$notes->{$id}->{otheraffected}</td>"
             ."<td>$notes->{$id}->{prioritystring}</td>\n";
      
    }
    
    print "</table><br>";    
}
#########################################
# determine note color
#########################################
sub determine_note_color
{
    my ($string) = @_;
    return "titleredc" if ($string eq  "Failing");
    return "titleblueb" if ($string eq "OK");
}
sub load_notes
{
    my ($array) = @_;
    my $hash = {};
    foreach my $n (@$array)
    {   
        $hash->{$n} = 1;
    }
    return $hash;   
}
sub show_notes_stats
{
    my ($effectives, $noneffectives, $nonact, $notesurl) = @_;
    my $lim =  4;  
    my $numeff = keys %$effectives;
    my $numnon  = keys %$noneffectives;
   
    my $total = $numeff+$numnon; 
    print "<table class = 'tac' align = center border-collapse: collapse>\n" 
         ."<tr><td title =  \"There are $numeff Active Note(s),$numnon Pending Note(s), and $total Total Note(s)\" align = right>\n"
         ."<a href = '$notesurl' style = \"color:blue\">View notes</a></td>"
         ."<td align = center ><a href = './doc/howtoaddnote.html'><u>how to add a note</u></td></tr>\n"
         ."</table><br>\n";
}
########################################
# display noneffective and priorities
########################################
sub show_noneff_or_prior
{
    my ($entry, $count,$countb) = @_;
    my $num = $countb % 2;
    my $class;
    if ($num == "1")
    {
	$class = "\"noteA\"";
    }
    else {$class = "\"noteB\"";}
    my @array  = (split /\|/, $entry);
    display(\@array,$class);
}
########################################
# display
########################################
sub display 
{
    my ($array, $class) = @_;  
    
    my $otherservices = pop @array;
    my $affectedservices = pop @$array;
    my $hostname = pop @$array;
    my $priority = pop @$array;
    #need to change priority numbers to string
    if ( $priority == '3'){$priority = "Medium";}
    if ($priority == '4'){$priority = "Low";}
    if ($priority == '1'){$priority = "Highest";}
    if ($priority == '2'){$priority = "High";}
   
    
    my $epocheff = shift @$array;
    my $epochexp = shift @$array;
    my $note = decode(@$array[0]);
    my $eff = decode(@$array[1]);
    my $exp  = decode(@$array[2]);
    #print "Q: $note<br>";
    write_line($note,$eff, $exp, $priority,$hostname,$affectedservices,$otherservices,$class);
}
########################################
# write line
########################################
sub write_line
{
    my ($note,$eff, $exp, $priority,$hostname,
	$affectedservices,$otherservices,$class) = @_;
   
    print "<tr class = $class><td align = left>"
	 ."<pre>$note</pre></td>\n"
         ."<td>$eff</td>\n"
         ."<td>$exp</td>\n"
	 ."<td>$hostname</td>"
         ."<td>$affectedservices</td>"
	 ."<td>$otherservices</td>"
         ."<td>$priority</td></tr>\n";
}
########################################
# organize notes
########################################
sub organize_notes_current_expired
{
    my ($notes,$rhost) = @_;   
    my $effectives    = {};
    my $noneffectives = {};
    #notes need to be separated into groups
    #active dates (current date > eff date, current date < exp date)
    #non-active dates 
 
    foreach my $n (keys %$notes)
    {   
	my @array            = (split /\|/, $n);
	my $otherservices    = $array[9];
	my $affectedservices = $array[8];		
	my $hostname         = $array[7]; 	
	my $effepoch         =  shift(@array); 
	my $expepoch         = shift(@array);
	
	my $prior     = $array[4];
	my $host      = $array[3];
	my $newentry  = join("|", @array);

	my $status    = see_if_active_note($effepoch, $expepoch); 
	if ($status eq "active")
	{	   
	    $newentry = "$effepoch|$expepoch|$newentry";
	    $$effectives{$newentry} = "active";
	    next;
	}
	elsif($status eq "pending")
	{	  
	    $newentry =  "$effepoch|$expepoch|$newentry";
	    $$noneffectives{$newentry}  = "pending";
	    next;   	    
	}
    }

    $effectives    = order_byprior($effectives);   
    $noneffectives = order_byprior($noneffectives);
    return $effectives,$noneffectives;
}
sub order_byprior
{
    my ($eff) = @_;
    tie my %hash, Tie::IxHash; 
    
    foreach my $entry (sort byprior keys  %$eff)
    {  	
	$hash{$entry} = $$eff{$entry};
    }
    return \%hash;
}
sub bypriority
{   
    #print "Q: $notes $a<br>";
    #foreach my $p (keys %$notes)
    #{

	#print "LLL: $p";
    #}
    #my $hash = $notes->{$a};
    #print "II: $hash<br>";
    #my $hashb = $notes->{$b};
    #my $hash2 = $notes->{$b};
    #print "GG: $notes->{$a}->{notestatus}";
    #my $priorityA = $hash->{notestatus};
    #print "SSS: $priorityA";
    #my $priorityB = $hashb->{notestatus};
    
    return  $notes->{$a}->{priority}<=> $notes->{$b}->{priority};
    
}
############################################
# see if note is active
############################################
sub see_if_active_note
{   
    my ($effepoch, $expepoch) = @_;
    my $current_epoch = time();
    my ($sec, $min, $hour,$mday,$mon,$year,$wday,$yday, $isd) = gmtime($current_epoch);
    my $value = "pending";
    $value    = "active" if (($current_epoch > $effepoch) and ($current_epoch  < $expepoch));
    return $value;    
}
sub add_note_status
{
    my ($hash) = @_;
    my $current_epoch = time();
    my ($sec, $min, $hour,$mday,$mon,$year,$wday,$yday, $isd) = gmtime($current_epoch);
    $hash->{notestatus}   = "pending";
    $hash->{css_class}    = "noteA";
    #print "CC: $current_epoch, $hash->{epochA}<br>";
    if (($current_epoch > $hash->{epochA}) and ($current_epoch  < $hash->{epochB}))
    {
	$hash->{notestatus}  = "active";
	$hash->{css_class}   = "noteB";
    }  
}
##############################################
# process service notes
##############################################
sub parse_service_notes
{
    my ($entries,$allowedhosts) = @_;
    
    my $bighash = {};
    my $bighash1 = {};
    foreach my $entry (@$entries)
    {
        my $note       = $entry->child("Note")->value;     
        $note          = fix_html($note);
	$note          = decode($note);
	my $id         = $entry->child("TableId")->value;
        my $eff        = $entry->child("EffDate")->value;
        $eff           = encode($eff);
        my $exp        = $entry->child("ExpDate")->value;
        $exp           = encode($exp);
        my $priority   = $entry->child("Priority")->value;
        #$priority      = encode($priority);
        my $epochA      = $entry->child("EpochEffDate")->value;
        my $epochB     = $entry->child("EpochExpDate")->value;
	my $host       = $entry->child("Host")->value;
	my $hostname   = $entry->child("HostName")->value;
	my @affectedservices = $entry->child("AffectedServices")->children();
	my @otheraffected    = $entry->child("OtherAffectedServices")->children();
	my (@a,@b,@c);
#	if ((@affectedservices == '1') and (@otheraffected == '1'))
	#{
	#if (($affectedservices[0]->value eq "null|none") and ($otheraffected[0]->value eq "null|none"))
	#{
	    
	    
	    #push @c,'null';
	    #print "this is great, br: $v1, $v2<br>";
	#}
	#else
	#{
	    
	    foreach my $n (@affectedservices)
	    {
		my $val = $n->value;
		
		push @a, $val;	
	    }
	    foreach my $n (@otheraffected)
	    {
		my $val = $n->value;
	       
		push @b,$val;
	    }
	
	my $affectedservices  = join (";",@a);	
	my $otheraffected     = join (";",@b);
	my $reason;
	if ($entry->child("ReasonDeleted"))
	{
	    $reason            = $entry->child("ReasonDeleted")->value;  
	}
	my $deff               = decode($eff);   
	my $dexp               = decode($exp);
	#$priority	       = nice_priority($priority);
	my $hash = { 'epochA'            => $epochA,
		     'epochB'            => $epochB,
		     'id'                => $id,
		     'note'              => $note,
		     'eff'               => $eff,
		     'decodedeff'        => $deff,
		     'decodedexp'        => $dexp,
		     'exp'               => $exp,
		     'host'              => $host,
		     'hostsimple'        => get_hostsuffixes($host),           
		     'priority',         => $priority,
		     'hostname'          => $hostname,
		     'reason_deleted'    => undef,
		     'affectedservices'  => $affectedservices,
		     'otheraffected'     => $otheraffected,
		 };
	$hash->{reason_deleted}  = $reason  if ($reason);      
	add_note_status($hash);
        if ((($hash->{affectedservices} eq "null") || ($hash->{affectedservices} eq 'none')) &&
	     (($hash->{otheraffected} eq "null") || ($hash->{otheraffected} eq 'none')))
	{
	    $bighash1->{$id} = $hash;
	}
	else  
	{
	    
	    $bighash->{$id} = $hash;
	}
    }
    return $bighash,$bighash1;
}
########################################
# strip unwanted html from text input 
########################################
sub fix_html
{
    my ($text) = @_; 
    $text =~ s/</&lt;/g;
    $text =~ s/>/&gt;/g;
    return $text;
}
sub load_notes_array
{
    my ($file) = @_;
    my @array;
    open (Hostnames,"$file") || die "cannot open hosts_file";
    while(<Hostnames>)
    {
        my $line = $_;
        chomp $line;
        push @array, $line;
    }
    close Hostnames  || die "cannot close $file";
    return \@array;
}
sub output_xml
{
    my ($hash) = @_;
    print "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    print "<ServiceNotes>\n";
     
    foreach  my $entry (keys %$hash )
    {
        my ($epoch,$epochb, $id,$host, $text,$dates,$priority,$hostname,$affectedservices,
	      $otherservices,$reason,$deleted) = (split /\|/,$entry);
	my @af = (split /\;/,$affectedservices);
	my @other = (split /\;/,$otherservices);
	if ($hostname eq '') {$hostname = "  ";} 
        my @a = (split /\:/,$dates, 4);
	my $eff_date = $a[0];
	my $exp_date = $a[2];
        my $nice_effec_date    = $eff_date . ":00";
        my $nice_exp_date      = $exp_date . ":00";
	$priority              = nice_priority($priority); 
        $text                  =~ s/^text%3D//;
        $text                  =~ s/\&amp;/\&/g;
        print "<Entry>\n"
             ."  <Note>$text</Note>\n"
	     ."  <TableId>$id</TableId>"
             ."  <Host>$host</Host>\n"
             ."  <EffDate>$nice_effec_date</EffDate>\n"
             ."  <ExpDate>$nice_exp_date</ExpDate>\n"
             ."  <EpochEffDate>$epoch</EpochEffDate>\n"
             ."  <EpochExpDate>$epochb</EpochExpDate>\n"
             ."  <Priority>$priority</Priority>\n"
    	     ."  <HostName>$hostname</HostName>\n"
	     ."  <AffectedServices>\n";
        foreach my $service (@af)
	{				
            print "        <Service>$service</Service>\n";
	}
	print "  </AffectedServices>\n"
	     ."  <OtherAffectedServices>\n";
	foreach my $service (@other)
	{
	   print "<Service>$service</Service>\n";
        }
	print "  </OtherAffectedServices>\n";
        print "  <ReasonDeleted>$reason</ReasonDeleted>\n" if ($reason);
        print "</Entry>\n";     
    }
    print "</ServiceNotes>\n";
}
sub nice_priority
{
	my ($priority) = @_;
	my @array = ("Highest","High","Medium","Low");
	$priority = $array[$priority-1];
	return $priority;

}
################################################################
# adjust times displayed for individual users (handle time zone)
################################################################
sub adjust_for_zone
{
    my ($epochtime) = @_;
    my ($seconds, $minutes, $hour,$day, $month, $year, $wday,$yday, $isdt) = localtime($epochtime);
    my @months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
    my @weekDays = qw(Sun Mon Tue Wed Thu Fri Sat Sun); 
    $month = $month+1;
    $year = $year+1900;
    $month = $months[$month];
    $month = lc($month);
    my $date = "$year-$month-$day-$hour";
    return $date;

}
###########################################
# gen pre current notes data
###########################################
sub gen_help_text
{
    my ($showdel) = @_;    
    
    
    #print "<span style = \"position: relative; left: 290px;\">\n";
    print "<table class = tac width = 380 align = center ><tr class = \"titleblue\"><td width = 180>\n";
    print "<a href = '/vo/notification/addnote.html' style = \"color: white\">Add A Notice</td>\n";

    if( ! $showdel) 
    {
        print "<td width = 180>\n"
              ."<a href = '/vo/notification/shownote.pl?showdeletes=yes' style = \"color: white\">\n"
              ."Show Expired Notices</td></tr>\n";
    }
    print "<tr/><br>\n";
   
    print "</table>\n";
    #</span>\n";
    print "<br>";   
}
###########################################
# show table headings
###########################################
sub gen_show_table_header
{
    my ($showdel,$container) = @_;
    my $width;
    if ($showdel) {$width = 850;}
    else {$width = 850;}

    my $isdt    = (localtime)[8];
    if ($isdt == "0")
    {
        #isdt == 0 (not daylight time so need to show EST in output)
        $isdt = "EST";
    }
    else {$isdt = "EDT";}
    
    my $zone = $container->{'zone'}; 
    if  (! $zone) {$zone = "none";}
    print "<DIV id = 'zonechosen' value = \"$zone\"></DIV>\n"
          ."<table class = tac cellpadding  = 1  width = $width border = 3 align =center id = \"note\_table\">\n"
          ."<tr><td bgcolor = tan  align = center><a title = \"The start time of the notice\">Effective Date</a></td>"
          ."<td bgcolor = tan  align = center><a title = \"The Date this notice expires\">Expiration Date</a></td>"
          ."<td  bgcolor = tan  align = center><a title = \"The notice added by the user\">Notice</a></td>"
          ."<td bgcolor = tan align = center><a title = \"The priority of the notice: Highest,High,Medium,Low\">Priority</a></td>"
          ."<td bgcolor = tan align = center><a title = \"The hosts affected\">Affected Host Machines</a></td>"
	  ."<td bgcolor = tan align = center><a title = \"Services affected\">Affected VAO Science Services</a></td>"
          ."<td bgcolor = tan align = center><a title = \"Other services affected\">Other Services Affected</a></td>"
          ."<td bgcolor = tan align = center><a title = \"The status of this notice: active, pending\">Status</a></td>";
    
    if ($showdel) 
    {
        print "<td align = center  bgcolor = tan>Reason for Deletion</td></tr>";
    }
    else
    {
        print "<td align = center  bgcolor = tan>\n"
	     ."<a title = \"Delete links are visible for notes originating from the user's host address\">Action</a></td></tr>\n";
    }
}
#################################################
sub decode  
{
     my($string) = shift(@_);
     $string =~ tr/\+/ /;
     $string =~ s/%([0-9a-fA-F][0-9a-fA-F])/chr(hex($1))/ge;
     return($string);
}
#################################################
# encode text
#################################################
sub encode
{    
     my($string) = shift(@_);  
     $string =~ s/([^ 0-9A-Za-z])/sprintf("%%%02X",ord($1))/ge;
     $string =~ tr/ /+/;
     return($string);
}
return 1;
