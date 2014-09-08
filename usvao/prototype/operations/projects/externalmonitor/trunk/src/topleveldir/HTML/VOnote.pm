#
# Author: Michael Preciado
#
# Description: contains routines for adding
#              and parsing notes to the monitoring
#              page
#

package HTML::VOnote;
#use CGIVO::VOMonutil;
use Tie::IxHash;
@ISA = qw(Exporter);
@EXPORT = qw (display_user_notes see_if_active_note determine_note_color  parse_service_notes fix_html
	      organize_notes_current_expired gen_help_text_current_notes output_xml gen_help_text gen_show_table_header);


####################################
# sub display user notes
####################################
sub display_user_notes
{
    my ($notes) = @_;
    my $machine; 
    $machine = $ENV{'SERVER_NAME'};
   
   
    my $notesurl = "http://$machine/vo/notification/shownote.pl";
    print "<table class = 'tac' align = center width = '680' border-collapse: collapse id = notes>\n";
    
    my $note_size  = keys (%$notes);
    if ($note_size > 0)
    {
        print "<tr><td class = \"label\" = center>User Notes</td>";
        print "<td bgcolor = #A49D9D  align = center>Effective Date (GMT)</td>";      
        print "<td bgcolor = #A49D9D align = center>Expiration Date (GMT)</td>";
        print "<td bgcolor = #A49D9D  align = center>Priority</td></tr>";
    }
    my $count = 1;
    my ($priorities,$noneffectives);
    ($effectives,$noneffectives)   = organize_notes_current_expired($notes);

    foreach my $n (keys %$effectives)
    {         
	my @array = (split /\|/,$n);
	my $class = "\"titleredb\"";
        display(\@array,$class);
        $count++;
    }
      
    my $countb = 1;
    foreach my $entry (keys %$noneffectives)
    {	
	show_noneff_or_prior($entry, $count,$countb);      
	$count++;
	$countb++;
    }
    print "</table>";
  
    my $nonact = $countb-1; 
    show_notes_stats($effectives, $noneffectives, $nonact, $notesurl);
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
##########################################
# show notes stats
##########################################
sub show_notes_stats
{
    my ($effectives, $noneffectives, $nonact, $notesurl) = @_;
    my $lim =  4;  
    my $numeff = keys %$effectives;
    my $numnon  = keys %$noneffectives;
   
    my $total = $numeff+$numnon; 
    print "<table class = 'tac' align = center width = 680 border-collapse: collapse>\n";   
    print "<tr><td title =  \"There are $numeff Active Note(s),$numnon Pending Note(s), and $total Total Note(s)\" align = right>\n";
    print "<a href = '$notesurl' style = \"color:blue\">View notes\n";
    print "</a>\n";
    print "</td>";
    print "<td align = center ><a href = './doc/howtoaddnote.html'><u>how to add a note</u></td></tr>\n";  
    print "</table><br>\n";
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
    my $priority = pop @$array;
    #need to change priority numbers to name string
    if ( $priority eq '3'){$priority = "Medium";}
    if  ($priority eq '4'){$priority = "Low";}
    if ($priority eq '1'){$priority = "Highest";}
    if  ($priority eq '2'){$priority = "High";}
    my $host    = pop @array;
    my $epocheff = shift @$array;
    my $epochexp = shift @$array;
    my $note = decode(@$array[0]);
    my $eff = decode(@$array[1]);
    my $exp  = decode(@$array[2]);
    #print "Q: $note<br>";
    write_line($note,$eff, $exp, $priority,$class);
}
########################################
# write line
########################################
sub write_line
{
    my ($note,$eff, $exp, $priority,$class) = @_;
    print "<tr class = $class><td align = left ><pre>$note</pre></td>\n";
    print "<td>$eff</td>\n";
    print "<td>$exp</td>\n";
    print "<td>$priority</td></tr>\n";
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
	my @array     = (split /\|/, $n);
	my $effepoch  =  shift(@array); 
	my $expepoch  = shift(@array);
	my $prior      = pop @array;
	my $host    = pop @array;
	my $newentry  = join("|", @array);

	my $status    = see_if_active_note($effepoch, $expepoch); 
	if ($status eq "active")
	{	   
	    $newentry = "$effepoch|$expepoch|$newentry|$host|$prior";	    
	    $$effectives{$newentry} = "active";
	    next;
	}
	elsif($status eq "pending")
	{	  
	    $newentry =  "$effepoch|$expepoch|$newentry|$host|$prior";
	    $$noneffectives{$newentry}  = "pending";
	    next;   	    
	}
    }

    $effectives    = order_byprior($effectives);   
    $noneffectives = order_byprior($noneffectives);
    return $effectives,$noneffectives;
}

#############################################
# order by eff
#############################################
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
##########################################
# sort bypriority
##########################################
sub byprior
{    
    my @entryA = (split /\|/, $a); 
    my @entryB = (split /\|/, $b);
    my $priorA = pop @entryA;
    my $priorB = pop @entryB;
    my $epochA = shift @entryA;
    my $epochB =  shift @entryB;
    {$priorA <=> $priorB  ||  $epochA <=> $epochB };
}
############################################
# see if note is active
############################################
sub see_if_active_note
{   
    my ($effepoch, $expepoch) = @_;
    my $current_epoch = time();
    my ($sec, $min, $hour,$mday,$mon,$year,$wday,$yday, $isd) = gmtime($current_epoch);
    
    if (($current_epoch > $effepoch) and ($current_epoch  < $expepoch))
    {
	return "active";
    }
    else 
    {
	#print "not an......AAAA<br>";
	return "pending";
    }
}
##############################################
# process service notes
##############################################
sub parse_service_notes
{
    my ($entries) = @_;
    my %hash;
    tie %hash, "Tie::IxHash";
    foreach my $entry (@$entries)
    {
        my $note       = $entry->child("Note")->value;     
        $note          = fix_html($note);
	$note          = decode($note);
        my $eff        = $entry->child("EffDate")->value;
        $eff           = encode($eff);
        my $exp        = $entry->child("ExpDate")->value;
        $exp           = encode($exp);
        my $priority   = $entry->child("Priority")->value;
        $priority      = encode($priority);
        my $epoch      = $entry->child("EpochEffDate")->value;
        my $epochb     = $entry->child("EpochExpDate")->value;
	my $host       = $entry->child("Host")->value;
	my $reason     = $entry->child("ReasonDeleted")->value  if ($entry->child("ReasonDeleted"));
        my $concat     = "$epoch|$epochb|$note|$eff|$exp|$host|$priority";
	$concat        = "$epoch|$epochb|$note|$eff|$exp|$host|$priority|$reason" if ($reason);
        $hash{$concat} = 1;
    }
    return \%hash;
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
##########################################
# help text for current notes
###########################################
sub gen_help_text_current_notes
{
    #print "<table class = tac width = 400 align = center>\n";
    #print "<tr><td colspan = 2><font style = \"color: green\"> *Use the \"Add a Note\" link to add a note to the monitoring page</td></tr>\n"; 
    #print "</table>\n";
}
########################################
# output xml
########################################
sub output_xml
{
    my ($hash) = @_;
    print "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    print "<ServiceNotes>\n";
     
    foreach  my $entry (keys %$hash )
    {           
        my ($epoch,$epochb, $host, $dates,$text, $priority,$reason) = (split /\|/,$entry);    
        my ($eff_date, $exp_date) = (split /\:/,$dates);
	#adjust effective and expiration dates for zones.
	#$eff_date = adjust_for_zone($epoch);
	#$exp_date = adjust_for_zone($epochb);
        my $nice_effec_date    = nice_date($eff_date);
        my $nice_exp_date      = nice_date($exp_date);
        $text                  =~ s/^text%3D//;
        $text                  =~ s/\&amp;/\&/g;
        print "<Entry>\n";
        print "  <Note>$text</Note>\n";
        print "  <Host>$host</Host>\n";
        print "  <EffDate>$nice_effec_date</EffDate>\n";
        print "  <ExpDate>$nice_exp_date</ExpDate>\n";
        print "  <EpochEffDate>$epoch</EpochEffDate>\n";
        print "  <EpochExpDate>$epochb</EpochExpDate>\n";
        print "  <Priority>$priority</Priority>\n";
        print "  <ReasonDeleted>$reason</ReasonDeleted>\n" if ($reason);
        print "</Entry>\n";     
    }
    print "</ServiceNotes>\n";
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
        print "<td width = 180>\n";
        print "<a href = '/vo/notification/shownote.pl?showdeletes=yes' style = \"color: white\">\n";
        print "Show Expired Notices</td></tr>\n";
    }
    print "<tr/><br>\n";
    gen_help_text_current_notes() if  (! $showdel);
    print "</table>\n";
    #</span>\n";
    print "<br>";   
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
###########################################
# show table headings
###########################################
sub gen_show_table_header
{
    my ($showdel,$container) = @_;
    my $width;
    if ($showdel) {$width = 850;}
    else {$width = 800;}

    my $isdt    = (localtime)[8];
    if ($isdt == "0")
    {
        #isdt == 0 (not daylight time so need to show EST in output)
        $isdt = "EST";
    }
    else {$isdt = "EDT";}
    
    my $zone = $container->{'zone'}; 
    if  (! $zone) {$zone = "none";}
    print "<DIV id = 'zonechosen' value = \"$zone\"></DIV>\n";
    print "<table class = tac cellpadding  = 6 width = $width  border = 3 align =center id = \"note\_table\">";
    print "<tr><td bgcolor = tan  align = center><a title = \"The start time of the notice\">Effective Date</a></td>";
    print "<td bgcolor = tan  align = center><a title = \"The Date this notice expires\">Expiration Date</a></td>";
    print "<td  bgcolor = tan  align = center><a title = \"The notice added by the user\">Notice</a></td>";
    print "<td bgcolor = tan align = center><a title = \"The priority of the notice: Highest,High,Medium,Low\">Priority</a></td>";
    print "<td bgcolor = tan align = center><a title = \"The status of this notice: active, pending\">Status</a></td>";
    if ($showdel) 
    {
        print "<td align = center  bgcolor = tan>Reason for Deletion</td></tr>";
    }
    else
    {
        print "<td align = center  bgcolor = tan>\n";
	print "<a title = \"Delete links are visible for notes originating from the user's host address\">Action</a></td></tr>\n";
    }
}
return 1;
