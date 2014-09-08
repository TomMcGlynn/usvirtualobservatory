#
#
#
package Tools::Form;

use Exporter;
@ISA = qw (Exporter);
@EXPORT = (generate_form);
use Table::Layout;
use strict;

sub generate_form
{
    my ($c)  = shift;
    my $id = $c->{uid};
    
    if (! $id){ 
	$id = get_user_id($c->{sessionid});     
    }    
    my @linknames         = ('VO Service Notices','VAO Home','VAO Feedback');
    my $voheader             = new Table::VOheader("Add note",\@linknames);
    print "Content-type: text/html\n\n";
    print << "    EOF";
    <HTML>
	<HEAD><TITLE>VO</TITLE>
	<LINK REL="StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css' TYPE="text/css">
	<LINK REL="StyleSheet" HREF='/vo/vaomonitor/css/styles.css' TYPE="text/css">
	<LINK REL="StyleSheet" HREF='/vo/vaomonitor/css/tr.css' TYPE="text/css">
	<LINK REL = "StyleSheet" HREF  = '/vo/vaomonitor/css/datepickercontrol.css' TYPE = "text/css" media = "all">    
	<script language='JavaScript' type='text/javascript' src="/vo/vaomonitor/js/vodate.js"></script>
	<script language='JavaScript' type='text/javascript' src="/vo/vaomonitor/js/julian.js"></script>
	<script language='JavaScript' type='text/javascript' src="/vo/vaomonitor/js/datepickercontrol.js"></script>
	<script language='JavaScript' type='text/javascript' src="./js/tools.js"></script>
	<input type="hidden" id="DPC_TODAY_TEXT" value="today">
	<input type="hidden" id="DPC_BUTTON_TITLE" value="Open calendar...">
	<input type="hidden" id="DPC_MONTH_NAMES" value="['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October','November', 'December']">
        <input type="hidden" id="DPC_DAY_NAMES" value="['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']">
	<script type = "text/javascript">	
	function displaynewdate(){       
	    var x = document.getElementById("datedivA");
	    var y = document.getElementById("datedivB");
	    var isvisible = y.style.visibility;
	    if (isvisible == 'hidden'){
		y.style.visibility = "visible";
		datehandler("none");
	    }
	    else {
		y.style.visibility = "hidden";
		q.style.visibility = "hidden";
	    }       
        }
        </script>
        </HEAD>
	<BODY onload = "load_dates()">
    EOF
	$voheader->printheader("VO");
        print << "    EOF";
        <h3> Service notices</h3>
        <form  action= /vo/notification/addnote.pl method = GET  name='formfoo' id = "formA" onSubmit = "return evalform()">
	<font size = 2> Message:</font><textarea rows = "3" name = text cols = 40></textarea><br><br>
	<font size = 2>Affected VAO Science Services:</font>
	<select multiple = 'multiple' name = "multiple">
	  <option value="none">(None)</option>
	  <option value="Data Discovery">Data Discovery</option>
	  <option value="Cross Comparison Service">Cross Comparison Service</option>
	  <option value="Time Series">Time Series</option>
	  <option value="IRIS SED">IRIS SED</option>
	  <option value ="IRIS">IRIS</option>   
	  <option value = "VAO Website">VAO Website</option>
	  <option value = "VAO Directory and Registry">VAO Directory and Registry</option>
	</select><br><br>
	<font size = 2>Other VAO Services Affected:&nbsp;&nbsp;&nbsp;&nbsp;</font>
	<select multiple = 'multiple' name = "multipleother">
	  <option value="none">(None)</option>
	  <option value="JIRA">JIRA</option>
	  <option value="Trac">Trac</option>
	  <option value="Twiki">Twiki</option>
	  <option value ="SVN">SVN</option>               
	  <option value = "Image Validator">Validators</option>
	  <option value = "SSO Replication Monitor">Security Service</option>
	  <option value = "Sky Alert">Sky Alert</option>
	  <option value = "VAO Jenkins">VAO Jenkins</option>
	  <option value = "VO Web Log">VO Web Log</option>       
	</select><br><br>	
	</font><font size = 2>Affected Host Machines:<input type = "text" cols = "30" name = host><br><br>
	<font size = 2 style = "color: green">Effective Date (must be within the next 30 days; Format: YYYY-MM-DD):</font>
	<br>       
	<input type  = 'hidden'  name = "identity" value  = $id >            
	<DIV id = "datedivA" >
        <input type = "hidden" id = "zoneadjust" name = "zoneadjust" >
        <input type="text" id="DPC_edit1_YYYY-MM-DD" name="caldate" size="12" datepicker_format = "YYYY-MM-DD" >
      Time:
    EOF
        print "<select name = 'hourA' id = 'hourA'>";
	print_options("hours"); 
	print "</select>";
        print << "    EOF";    
        Zone:
	<select name = "zone"  id = "zone">      
	  <option value = "EDT">EDT
	  <option value = "CDT">CDT
	  <option value = "MDT">MDT
	  <option value = "PDT">PDT
	  <option value = "EST">EST
	  <option value = "CST">CST
	  <option value = "MST">MST
	  <option value = "PST">PST              
	</select>
        <select name = "yearA" id = "yearA"  style = "visibility: hidden">
           <option value = "">     
        </select>            
	</select>            
	<select name = "monthA" id  = "monthA" style = "visibility: hidden">
          <option value = "jan">Jan
	  <option value = "feb" >Feb
	  <option value = "mar" >Mar
	  <option value = "apr" >Apr
	  <option value = "may" >May 
	  <option value = "jun"> Jun 
	  <option value = "jul" >Jul
	  <option value = "aug" >Aug 
	  <option value = "sep" > Sep
	  <option value = "oct" > Oct
	  <option value = "nov" > Nov
	  <option value = "dec" > Dec
	</select>      
        <select  name = "dayA" id = "dayA" style = "visibility: hidden" >
    EOF
	print_options("days");
        print << "    EOF";  
	</select>
        <br><br>
	<font size = 2 style = "color: green">Priority (Highest,high,medium,low) </font>
	<select name =  "priority" id = "priority" >
	  <option value = "1">Highest
	  <option value = "2">High
	  <option value = "3">Medium
	  <option value = "4">Low
	</select>    
     </DIV><br> 
    <input id = "cbox" type = checkbox  onclick = "displaynewdate()">
     <font size = 2>Change Expiration Date</font><fontsize = 2 style  = "color: green"> (Defaults to two days after Effective Date)</font><br>
        
     <DIV id  = "datedivB" name = "foo" style = "visibility: hidden">
     <font size = 2 style = "color: green">Expiration Date:<br></font> 
     <input type="text" id="DPC_editnew_YYYY-MM-DD" name="newcaldate" size="12" datepicker_format = "YYYY-MM-DD" >        
        Time: 
        <select name = "hourB" id = "hourB">
    EOF
	print_options("hours"); 
        print  << "    EOF";
        </select>   	 
        <select name = "yearB" id = "yearB" style  = "visibility: hidden">
          <option  name  = test  value = 'your got it'>
        </select>
        <select name = "monthB" id = "monthB" style = "visibility: hidden" >
	  <option value = "jan">Jan
	  <option value = "feb" >Feb 
	  <option value = "mar" >Mar 
	  <option value = "apr" >Apr 
	  <option value = "may" >May 
	  <option value = "jun"> Jun 
	  <option value = "jul" >Jul 
	  <option value = "aug" >Aug 
	  <option value = "sep" > Sep
	  <option value = "oct" > Oct 
	  <option value = "nov" > Nov 
	  <option value = "dec" > Dec 
         </select>        
         <select  name = "dayB" id = "dayB" style = "visibility: hidden">
    EOF
    print_options("days");
    print << "    EOF";
	 </select> 
	 &nbsp;&nbsp;            
     </DIV>
     <br>
     <input type = submit value = "Submit" ><input type = reset>
    </form> 
    EOF
    gen_footer_bas(); 
}
sub print_options
{
    my ($option) = @_;
    if ($option eq 'hours'){
	for (my $x = 0; $x<= 23;$x++){
	    my $y = sprintf("%02d",$x); 
	    print "\t<option value = $x>$y:00\n";
	}
    }
    elsif ($option eq 'days'){
        for (my $x= 1; $x<=31;$x++){
	    print "\t<option value = $x>$x\n";
	}
    }
}
1;
