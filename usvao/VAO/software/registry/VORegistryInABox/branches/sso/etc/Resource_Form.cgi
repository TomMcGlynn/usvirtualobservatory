#! /usr/bin/perl
#
#
#  Resource_Form.cgi
#
#  Author:        Ramon Williamson (ramonw@ncsa.uiuc.edu)
#                 http://www.ncsa.uiuc.edu/people/ramonw
#
#  Creation date: Thursday, March 28, 2003
# 
# 
#  Revision:      $Revision: 2.52 $
#  Checked in by: $Author: ramonw $
#  Last modified: $Date: 2005/08/01 20:32:29 $
# 
#  $Log: Resource_Form.cgi,v $
#
#  Revision 2.52a 2006/06/21 rplante
#  fixed Generic Service bugs:  detect failure to move file into directory;
#    add "Service" to default list of sets; add explicit support in form.
#  
#  Revision 2.52  2005/08/01 20:32:29  ramonw
#  Add namespace to xsi:type for Coverage; Fix bug in CoordRAnge
#
#  Revision 2.49  2004/11/29 16:17:02  ramonw
#  Small bugfix to fix problem with setting up new repository
#
#  Revision 2.48  2004/11/19 18:24:47  ramonw
#  Bugfix for CGIservice Param table
#
#  Revision 2.47  2004/11/18 21:48:37  ramonw
#  Add range check for integer values
#
#  Revision 2.46  2004/11/17 18:31:07  ramonw
#  Bug fix
#
#  Revision 2.45  2004/11/17 18:07:52  ramonw
#  Few more bug fixes
#
#  Revision 2.44  2004/11/16 20:53:14  ramonw
#  Bug fixes
#
#  Revision 2.43  2004/11/15 19:55:15  ramonw
#  Update to VOResource v0.10; add CGIService and WebService resource types;
#
#  Revision 2.42  2004/08/02 15:39:06  ramonw
#  Take out update message from login form
#
#  Revision 2.41  2004/08/02 15:14:53  ramonw
#  Small changes to fix problems with VOR output
#
#  Revision 2.40  2004/07/29 21:46:24  ramonw
#  Fixed some links to the form help
#
#  Revision 2.39  2004/07/29 21:07:22  ramonw
#  Updated form for VOResource v0.10
#  Removed save_authority and save_registry subroutines,
#  incorporate needed code into save_resource.
#
#  Revision 2.38  2004/01/15 17:49:29  ramonw
#  Typo prevents creation of registry directories
#
#  Revision 2.37  2004/01/02 16:12:32  ramonw
#  Modification of hint texts; added created, updated and status
#  attributes to resource tags of output
#
#  Revision 2.36  2003/12/29 22:23:17  ramonw
#  *** empty log message ***
#
#  Revision 2.35  2003/12/29 22:14:39  ramonw
#  Modified Publisher list to list ID's
#  Added resources remain in added status until committed
#  rather than becoming an edited resource if edited before committed.
#
#  Revision 2.34  2003/12/19 20:42:39  ramonw
#  Even more bugfixes
#
#  Revision 2.33  2003/12/15 21:28:15  ramonw
#  More bugfixes
#
#  Revision 2.32  2003/12/10 19:08:54  ramonw
#  Bugfixes
#
#  Revision 2.31  2003/12/09 19:49:06  ramonw
#  Converted ot VOResource 0.9
#
#  Revision 2.30  2003/10/31 22:19:58  ramonw
#  One more try to make it work on monet
#
#  Revision 2.29  2003/10/31 20:55:12  ramonw
#  Further bugfix for delete to handle array values correctly
#
#  Revision 2.28  2003/10/31 18:09:22  ramonw
#  Delete and undelete did not convert strings to escaped XML characters
#
#  Revision 2.27  2003/09/15 18:51:09  ramonw
#  Form modified to RSM 0.8 schema
#
#  Revision 2.26  2003/09/02 19:24:17  ramonw
#  Little debugging things
#
#  Revision 2.25  2003/09/02 17:02:45  ramonw
#  Modified for release - mostly minor changes
#  Revision 2.24  2003/06/27 15:44:09  ramonw
#  Use of "our" only supported in Perl > 5.60, so had to change code
#  to be compatible with older versions of Perl.
#
#  Revision 2.23  2003/06/18 21:22:13  ramonw
#  Undelete was allowed on any file!  Saved user from himself by
#  only undeleting deleted files.
#
#  Revision 2.22  2003/06/18 19:11:17  ramonw
#  Changes to repository now are not committed until the
#  "Commit Resources" button is hit.
#
#  Revision 2.21  2003/06/17 18:57:24  ramonw
#  Small typo fixed
#
#  Revision 2.20  2003/06/17 18:51:44  ramonw
#  Small bug causes Content tag to be printed to output
#  even if there is no Content information.
#
#  Revision 2.19  2003/06/17 18:17:11  ramonw
#
#  Revision 2.20  2003/06/17 18:51:44  ramonw
#  Small bug causes Content tag to be printed to output
#  even if there is no Content information.
#
#  Revision 2.19  2003/06/17 18:17:11  ramonw
#  Had to make chamges to output XML for namespace problems with registry.
#
#  Revision 2.18  2003/06/16 19:08:24  ramonw
#  deletedRecord not allowed in Static Repository, changed Identify flag accordingly
#
#  Revision 2.17  2003/06/16 18:52:38  ramonw
#  Changed status and datestamp position in output - outside of erviceService block
#
#  Revision 2.16  2003/06/13 21:11:53  ramonw
#  Added reserved tag block into output with datestamp and status
#  tags in it for use in monitoring deletion of resources.
#  Deleted resources now continue to be retained but with a deleted
#  status flag.
#  Modified static repository code to account for changes to resource files.
#
#  Revision 2.15  2003/06/11 21:27:01  ramonw
#  Added ability to login again to tryout account
#
#  Revision 2.14  2003/06/11 16:27:37  ramonw
#  Modified with checkbox to allow for trying form without
#  adding to the published repository;
#
#  Revision 2.13  2003/05/07 17:56:43  ramonw
#  Fixed ifaceURL get for BrowserBasedService and hardwire username in form
#
#  Revision 2.12  2003/05/07 17:02:41  ramonw
#  Changes made by Ray in layout and look
#
#  Revision 2.10  2003/05/05 16:31:28  ramonw
#  Slight typo in the namespace declaration of the output file caused invalid namespace check in repository.  Changed to correct form.
#
#  Revision 2.9  2003/04/29 17:13:51  ramonw
#  Now outputs DC and VOR outputs for ListRecords (Download XML)
#
#  Revision 2.8  2003/04/28 21:27:19  ramonw
#  Output XML files are now compliant to VODescription.vsd.
#
#  Revision 2.7  2003/04/24 20:39:12  ramonw
#  Typo fix for SIA parameter
#
#  Revision 2.6  2003/04/24 19:39:17  ramonw
#  Ooops; problem with arrays and XML from and to; fixed.
#
#  Revision 2.5  2003/04/24 18:51:36  ramonw
#  Added toXML and fromXML routines to convert to and from XML for special
#  characters.
#
#  Revision 2.4  2003/04/24 16:43:26  ramonw
#  Small bugfix in setup portion.
#
#  Revision 2.3  2003/04/24 16:19:07  ramonw
#  Add WebBrowser Service and SIA Service parameters to form.
#
#  Revision 2.2  2003/04/22 22:10:36  ramonw
#  Added checks for Title, Short Name, Identifier, Contact Information.
#  non-unique identifiers disallowed.
#
#  Revision 2.1  2003/04/18 16:10:45  ramonw
#  Added delete function.
#
#  Revision 2.0  2003/04/17 19:56:34  ramonw
#  Restructured for Set format laid out in the VT-XMLFiles package
#  for OAI-Verbs.
#
#  Revision 1.9  2003/04/16 20:49:59  ramonw
#  Fixed error in first switch to figure out starting form.
#  Fixed error with carriage returns in Subject and Format fields.
#
#  Revision 1.8  2003/04/16 19:59:12  ramonw
#  Modified to include usernames/passwords for adding multiple sites.
#  Modified form printouts where possible to "here" documents for ease
#  of updating forms.
#
#  Revision 1.7  2003/04/07 18:58:52  ramonw
#  Added header w/ RCS keys
#
#

use FindBin;
use lib "/appl/VO/registry/lib";


use strict;
use FileHandle;
use CGI qw( :netscape :html3 );
use CGI::Carp qw( fatalsToBrowser );
use Pure::X2D;
use Pure::EZXML;

my $form = new CGI;

my $workspace = "/appl/VO/registry/data/webwrk";
my $alt_workspace = "/appl/VO/registry/data/alt";
my $doc_dir = "voregistry";
my $new_registry = "false";

use vars qw($path $resource_file @list %attribute $fname $username);
use vars qw($defSet $authorityId $resourceKey $service $title $status);
use vars qw(@defSets @defImTypes $nspace @defFrames @waveRange);
use vars qw($skyservice $tabskyservice $hasCoverage $numParam);
use vars qw($password);
use vars qw(%setstolabels %labelstosets);
$path = "$workspace";
%attribute = ();
%setstolabels = (
    "Organisation"        => "Organization or Project",
    "DataCollection"      => "Data Collection",
    "Authority"           => "Naming Authority ID",
    "BrowserBasedService" => "Service accessible from a Web Page",
    "ConeSearch"          => "Simple Cone Search Service",
    "SIAService"          => "Simple Image Access Service",
    "SSAService"          => "Simple Spectral Access Service",
    "WebService"          => "SOAP-based Web Service", 
    "CGIService"          => "CGI/URL-based Web Service",
    "Service"             => "Generic Service",
    "Resource"            => "Generic Resource" 
);
@defSets =  keys(%setstolabels);
print STDERR "defSets =", join(" ", @defSets), "\n";
print STDERR "Organisation =>", $setstolabels{"Organisation"}, "\n";
%labelstosets = ();
foreach my $set (@defSets) {
    $labelstosets{$setstolabels{$set}} = $set;
}
@waveRange =  qw(GeV MeV KeV ev Angstroms microns mm meters Hz MHz GHz);
@defFrames =  qw(ICRS FK5 FK4 ECL GAL SGAL);
@defImTypes =  qw(Cutout Mosaic Atlas Pointed);
$nspace = "http://www.ivoa.net/xml/VOResource/v0.10";
$skyservice = 0; #Flag for CGI, WebServices to denote it is a SkyService
$tabskyservice = 0; #Flag for CGI, WebServices to denote it is a TabularSkyService
$numParam = 3; # Number of Service parameters to list
$SIG{PIPE} = 'handler';                                                      
$SIG{INT} = 'handler';

umask(02);

# determine which page we're on, and respond accordingly
SWITCH: {
    # If no parameters, either set up a new repository or go to login page
    # Count config files to determine how many sites there are.

	if ( !$form->param ) { 	
        my @list = count_config_files();
        my $num = scalar(@list);
        if ($num > 0) {
            login_form();
        } else {
            $new_registry = "true";
		    resource_form("setup"); 
        }
		last SWITCH;
    }
    # Verify Login info, List resources
	if ( $form->param('ftype') eq '   Login   ') { 	
	    if ( $form->param('tryout')) {
            $path = $alt_workspace;
        }
        my $verify = verify_login();
        if ($verify == 0) {
            read_config($username);
            @list = read_resources();
		    resource_list(\@list);
        } 
        if ($verify == 1) { 
            login_form("Invalid username...Try Again");
        }
        if ($verify == 2) { 
            login_form("Incorrect login...Try Again");
        }
		last SWITCH;
	}
    # Create New Site in existing repository
	if ( $form->param('ftype') eq 'Create new site') { 	
	    if ( $form->param('tryout')) {
            $path = $alt_workspace;
        }
		resource_form("new_site"); 
		last SWITCH;
	}
    # Add new Resource Form
	if ( $form->param('ftype') eq ' Add Resource') { 	
        $defSet = $form->param('defset');
	    $username = $form->param('uname');
	    $path = $form->param('path');
        read_config($username);
		resource_form("add");
		last SWITCH;
	}
    # Identifier Information needed
	if ( $form->param('ftype') eq '   Next   ') { 	
        save_resource("new","temp.xml","Organisation");
        save_resource("new","temp2.xml", "Authority");
        # If it is a new organisation within an existing registry,
        # We *don't* want another Registry entry, so skip it if
        # a Registry entry already exists.
        my $reg_file = find_registry_file();
        if ($reg_file eq "") { 
            save_resource("new","temp3.xml","Registry");
        }
		ident_form("");
		last SWITCH;
	}
    # Identifier Information to be saved, go to resource form
	if ( $form->param('ftype') eq 'Create Site') { 	
	    $username = $form->param('uname');	
	    $path = $form->param('path');	
	    my $passwd = $form->param('passwd');	
	    my $passwd2 = $form->param('passwd2');	
        my $vreturn = verify_site($username, $passwd, $passwd2);
        if ($vreturn == 0) {
            my $oldfile = $path."/temp.xml";
            my $oldfile2 = $path."/temp2.xml";
            my $oldfile3 = $path."/temp3.xml";
            my $newfile = $path."/Organisation/".$username."00001.xml.uc_add";
            my $newfile2 = $path."/Authority/".$username."00002.xml.uc_add";
            my $newfile3 = $path."/Registry/".$username."00003.xml.uc_add";
            foreach my $id (@defSets) {
                `mkdir "$path/$id"` unless -e "$path/$id";
                `chmod 0755 "$path/$id"`;
            }
            rename($oldfile, $newfile) 
                    || die "error writing $oldfile to $newfile\n";
            rename($oldfile2, $newfile2) 
                || die "error writing $oldfile2 to $newfile2\n";
            if (-e $oldfile3) {
                rename($oldfile3, $newfile3) 
                    || die "error writing $oldfile3 to $newfile3\n";
            }
		    save_config($username, $passwd);
            read_config($username);
            my @list = read_resources();
		    resource_list(\@list);
        }
        if ($vreturn == 1) {
            ident_form("Login name $username already exists.  Try Again!");
        }
        if ($vreturn == 2) {
            ident_form("Passwords do not match.  Try Again!");
        }
        if ($vreturn == 3) {
            ident_form("Password is null. Please provide a password!");
        }
		last SWITCH;
	}
    # Edit Resource Information
	if ( $form->param('ftype') eq ' Edit Resource') { 	
        $path = $form->param('path');
	    $username = $form->param('uname');	
        read_config($username);
		resource_form("edit");
		last SWITCH;
	}
    # Delete Resource Information
	if ( $form->param('ftype') eq 'Delete Resource') { 	
        $path = $form->param('path');
	    $username = $form->param('uname');	
	    my $delfile = $form->param('resource');	
        read_config($username);
		confirm_delete($delfile);
		last SWITCH;
    }
    # Delete Action
    if ( $form->param('ftype') eq 'Confirm Delete') { 	
        $path = $form->param('path');
	    my $delfile = $form->param('inputfname');	
        if ($delfile =~ /xml.uc_/) {
           unlink "$path/$delfile";
        } else {
           read_resource_file($delfile);
           save_resource("delete", $delfile, "");
        }
	    $username = $form->param('uname');	
        read_config($username);
        my  @list = read_resources();
		resource_list(\@list);
		last SWITCH;
	}
    # Undelete Action
    if ( $form->param('ftype') eq 'Undelete Resource') { 	
        $path = $form->param('path');
	    my $udfile = $form->param('resource');	
        if ($udfile =~ /uc_del/) {
           unlink "$path/$udfile";
        } else {
           read_resource_file($udfile);
           if ($status eq "deleted" || $status eq "uncommitted delete") {
              save_resource("undelete", $udfile, "");
           }
        }
	    $username = $form->param('uname');	
        read_config($username);
        my  @list = read_resources();
		resource_list(\@list);
		last SWITCH;
	}
    # Cancel Action (for Delete and Edit)
    if ( $form->param('ftype') eq 'Cancel') { 	
        $path = $form->param('path');
	    $username = $form->param('uname');	
        my  @list = read_resources();
		resource_list(\@list);
		last SWITCH;
	}
    # Add Resource Information
    if ($form->param('ftype') eq '   Add Resource  ') {
        my $message = check_fields("add");
        if ($message eq "Verified") {
            $numParam = $form->param('numParam');
	        $skyservice = $form->param('skyservice');
	        $tabskyservice = $form->param('tabskyservice');
            $path = $form->param('path');
	        $username = $form->param('uname');	
            read_config($username);
            my $filename = &find_filename;
	        $defSet = $form->param('defset');
            $filename = "$defSet/$filename";
            save_resource("add", $filename,"");
            my  @list = read_resources();
		    resource_list(\@list);
        } else {
            $numParam = $form->param('numParam');
	        $skyservice = $form->param('skyservice');
	        $tabskyservice = $form->param('tabskyservice');
	        $defSet = $form->param('defset');
	        $path = $form->param('path');
	        $username = $form->param('uname');	
            save_resource_variables();
            resource_form("add", $message);
        }
		last SWITCH;
    }    
    # Add SkyService Information
    if ($form->param('ftype') eq 'Update Coverage') {
	    $numParam = $form->param('numParam');
	    $skyservice = $form->param('skyservice');
	    $tabskyservice = $form->param('tabskyservice');
        $defSet = $form->param('defset');
	    $username = $form->param('uname');
	    $path = $form->param('path');
        save_resource_variables();
        resource_form("update");
		last SWITCH;
    }
    # Add Parameter slots Information
    if ($form->param('ftype') eq 'Add Param') {
	    $numParam = $form->param('numParam');
	    $numParam = $numParam + $form->param('addparam');
	    $skyservice = $form->param('skyservice');
	    $tabskyservice = $form->param('tabskyservice');
        $defSet = $form->param('defset');
	    $username = $form->param('uname');
	    $path = $form->param('path');
        save_resource_variables();
        resource_form("update");
		last SWITCH;
    }
    # Add Parameter slots Information on Edit
    if ($form->param('ftype') eq 'Add Param ') {
	    $numParam = $form->param('numParam');
	    $numParam = $numParam + $form->param('addparam');
	    $skyservice = $form->param('skyservice');
	    $tabskyservice = $form->param('tabskyservice');
        $defSet = $form->param('defset');
	    $username = $form->param('uname');
	    $authorityId = $form->param('authorityId');	
	    $resourceKey = $form->param('resourceKey');	
	    $resource_file = $form->param('inputfname');	
	    $path = $form->param('path');
        save_resource_variables();
        resource_form("editupdate", "params added");
		last SWITCH;
    }
    # Change Resource Information
    if ($form->param('ftype') eq ' Submit Changes') {
        my $message = check_fields("edit");
        if ($message eq "Verified") {
	       $defSet = $form->param('defset');
	       my $file = $form->param('inputfname');
           $path = $form->param('path');
	       $skyservice = $form->param('skyservice');
	       $tabskyservice = $form->param('tabskyservice');
	       $numParam = $form->param('numParam');
	       $username = $form->param('uname'); 	
           read_config($username);
           save_resource("edit", $file,"");
           my  @list = read_resources();
		   resource_list(\@list);
		   last SWITCH;
        } else {
	       $defSet = $form->param('defset');
	       $path = $form->param('path');
	       $authorityId = $form->param('authorityId');	
	       $resourceKey = $form->param('resourceKey');	
	       $resource_file = $form->param('inputfname');	
	       $skyservice = $form->param('skyservice');
	       $tabskyservice = $form->param('tabskyservice');
	       $numParam = $form->param('numParam');
	       $username = $form->param('uname');	
           save_resource_variables();
           resource_form("edit", $message);
        }
    }    
    # Publish Resources Action 
    if ( $form->param('ftype') eq 'Publish Resources') { 	
        $path = $form->param('path');
	    $username = $form->param('uname');	
        commit_resources();
        my  @list = read_resources();
		resource_list(\@list);
		last SWITCH;
	}
    # Create Static Repository 
	if ( $form->param('ftype') eq 'Download XML') { 	
        $path = $form->param('path');
	    $username = $form->param('uname');	
        xmlheader();  
        identify($username);
        listMetadataFormats($username);
        listRecords($username);
        xmlfooter();  
		last SWITCH;
	}
	die "CGI error in script.  Cannot continue.\n";	# unknown

}

###############################################################################
#
#   Subroutines
#
###############################################################################

###############################################################################
#
# resource_list: List of available resources in current repository
#
###############################################################################
sub resource_list {

    my $resource = shift;
    my $string;
    my $res_string;
    my $add_string;
    my $warn = "";
    my $publish = "";
    my $checked = 0;
    my $org = 'Your Organisation';
    my $oai_path = $ENV{'SCRIPT_NAME'};
    my @parts = split ('/', $oai_path);
    pop(@parts);
    pop(@parts);
    $oai_path = join('/', @parts);
    if ($oai_path eq "") {
        $oai_path = "/cgi-bin";
    }

    if ($path eq $alt_workspace) {  # Test Repository
	  $warn = "<font color=\"red\"><em>***Test Registry - Data Will Not Be Published***</em></font>";
    } else {  # Real Repository
    }

 foreach my $res (@$resource) {
        if (!$checked) {
           $string .= "<td valign='top'><input type=\"radio\" name=\"resource\" value=\"$res->[0]\" checked></td>\n";
           $checked = 1;
        } else {
           $string .= "<td valign='top'><input type=\"radio\" name=\"resource\" value=\"$res->[0]\" ></td>\n";
        }
	if ($res->[1] eq "published" || $res->[1] eq "deleted") {
        $string .= "<td valign='top'><font color=\"green\">$res->[1]</font>&nbsp;</td>\n";
    } else {
        $string .= "<td valign='top'><font color=\"red\">$res->[1]</font>&nbsp;</td>\n";
    }
	$string .= "<td valign='top'>$res->[2] ($res->[3])<br />\n";
	$string .= "   <font color='#6ba5d7'><strong><em>ivo://$res->[4]/$res->[5]</em></strong></font>\n</td>";
        if ($setstolabels{$res->[6]} eq '') {
            $string .= "<td valign='top'>$res->[6]</td>\n";
        }
        else {
            $string .= "<td valign='top'>$setstolabels{$res->[6]}</td>\n";
        }
	$string .= "</tr>\n";
    }
    foreach my $id (@defSets) {
	    $res_string .= "<option value=$id>$id";
    }
    $add_string .= "<select name=\"defset\">";
    $add_string .= "$res_string";
    $add_string .= "</select>";
	$org = &find_org("title");

    print
	$form->header,
	$form->start_html(-title=>'Resource List', -bgcolor=>'white',
                          -head=>CGI::Link({-rel=>'stylesheet', 
                                            -type=>'text/css',
                                            -href=>'/usvo_template.css'})),
	&formatBanner(), "\n", $form->h1("Resource List for $org"),
    $form->h2("$warn"), "\n";
    print <<EOT;
    
		
This page lets you register, view, and update your resources.  As a
way to minimize the amount you have to type in, you can add new
resources by <em>inheriting</em> the values from an existing one;
thus, you only update those items that are different.  <p>

Resources that are added, edited, or deleted will not be made available
to the VO via the harvesting interface until the "Publish Resources" 
button is hit.

EOT
    print $form->start_form;
	print <<EOT;
        <table border="0" width=100% cellpadding="1" cellspacing="8">
		<tr align=left>
		<th><font size=4></font></td>
		<th><font size=4>Status</font></td>
		<th><font size=4>Resource</font></td>
		<th><font size=4>Resource Type</font></td>
		<tr>
       $string
        </tr>
		</table>
		<p>
<input type=hidden name=uname value="$username">
<center>
<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="3" valign="top" width="25%" bgcolor="#f9ebc9" >
      To <strong>add</strong> a new Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above to inherit values from, 
    </td>
  </tr>
  <tr>
    <td align="left" bgcolor="#6bc5d7">
      2. Choose the type of new resource being added: &nbsp; 
      <select name="defset">
	<option value="Organisation">$setstolabels{"Organisation"}</option>
	<option value="DataCollection">$setstolabels{"DataCollection"}</option>
	<option value="Authority">$setstolabels{"Authority"}</option>
	<option value="BrowserBasedService">$setstolabels{"BrowserBasedService"}</option>
	<option value="CGIService">$setstolabels{"CGIService"}</option>
	<option value="ConeSearch">$setstolabels{"ConeSearch"}</option>
	<option value="SIAService">$setstolabels{"SIAService"}</option>
	<option value="SSAService">$setstolabels{"SSAService"}</option>
	<option value="WebService">$setstolabels{"WebService"}</option>
	<option value="Service">$setstolabels{"Service"}</option>
	<option value="Resource">$setstolabels{"Resource"}</option>
      </select>
    <br/>&nbsp;&nbsp;&nbsp;&nbsp;
    Click <a href="$ENV{'PATH_INFO'}/$doc_dir/resourcedesc.html" target="nvohelp">here</a> for a description of the resources to help you decide.
    </td>
  </tr>
  <tr>
    <td bgcolor="#f5b21d">
      3. Click: &nbsp; <input type=submit name=ftype value=" Add Resource">
    </td>
  </tr>
</table> <p>

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>edit</strong> (or view) a Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above, 
    </td>
  </tr>
  <tr>
    <td align="left" bgcolor="#f5b21d">
      2. Click: &nbsp; <input type=submit name=ftype value=" Edit Resource">
    </td>
  </tr>
</table> <p>

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>delete</strong> a Resource (or remove an uncommitted change)...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above, 
    </td>
  </tr>
 <tr>
    <td bgcolor="#f5b21d">
      2. Click: &nbsp; <input type=submit name=ftype value="Delete Resource"> 
      &nbsp; &nbsp; <em>Confirmation will be requested.</em>
    </td>
  </tr>
</table>
<p>

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>undelete</strong> a deleted Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the deleted resources above, 
    </td>
  </tr>
  <tr>
    <td bgcolor="#f5b21d">
      2. Click: &nbsp; <input type=submit name=ftype value="Undelete Resource"> 
    </td>
  </tr>
</table>
<p>
<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="1" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>commit</strong> all uncommitted Resources...
    </td>
    <td align="left" bgcolor="#f5b21d">
      Click: &nbsp; <input type=submit name=ftype value="Publish Resources"> 
    </td>
  </tr>
</table>
<p>
</center>

<table border="0" cellspacing="2" cellpadding="4" >
  <tr>
    <td >
      To <strong>download</strong> all resource descriptions in XML <br>
      (using the OAI Static Repository Format)...
    </td>
    <td align="left">
      <input type=hidden name=path value="$path">
      <input type=submit name=ftype value="Download XML">
    </td>
  </tr>
  <tr>
    <td>
      [Note: deleted records are not supported in this format <br>
        and are excluded from the output]
    </td>
  </tr>
</table>
<p>
EOT
    print $form->end_form, "\n", &formatFooter(),
		$form->end_html;

    if ($path eq $workspace) {
        print <<EOT;
        Click on <a href="$ENV{'PATH_INFO'}/$doc_dir/OAIinstructions.html"target="oaihelp">this</a> link for instructions on how to query your 
        registry and test it with the Open Archive
        Initiative Registry Explorer.<p>  
         <a href="$oai_path/oai.pl?verb=Identify">This</a> link is to the Harvesting Interface to your
Registry. <p>It is an example of how to use your Harvesting Interface and will
return the results from the "Identify" OAI verb.<p> 
EOT
    }
}

###############################################################################
#
# resource_form: Body of form to add/edit resource information
#
###############################################################################

sub resource_form {

    use vars qw($action $message );
    ($action, $message) = @_;
    use vars qw($submitButton $startString @userSets @repSets $manageBy $num_sets $title $sname $publisherId $pub_auth $pub_key $pub_title $pub_description $pub_refURL $creator $logo @subject $description $contributor $date $version $authorityId $resourceKey $refURL $source $servURL $contact_name $contact_email $contact_address $contact_telephone $region $spatial_res @waveband $temporal_start $temporal_end $temporal_res $rel_authorityId $rel_resourceKey $relationType $rel_title $relation_id $rel_auth $rel_key $log_authorityId $log_resourceKey $regionFrame $region_lat $region_long $region_rad $rangeFrame $range_lat_min $range_lat_max $range_long_min $range_long_max $region_regard $wave_min $wave_max $wave_min_units $wave_max_units $spec_res);
    use vars qw(@ctype @content $fac_auth $fac_key $facility $facilityId $ins_auth $ins_key $instrument $instrumentId @format @rights $ifaceURL $output $outputMime @iface_params $imServType $maxRegSize_long $maxRegSize_lat $maxImExt_long $maxImExt_lat $maxImSize_long $maxImSize_lat $maxFSize $verbosity $maxSR $maxRec $verbosity $datestamp $status $relId @relation @frames $create_date $update_date);
    use vars qw(@dataType @param_name @param_description @param_dataType @param_unit @param_ucd @param_req @param_arraysize);
 
    if ($message ne "") {
        $message .= "<br>Please make the necessary changes and resubmit<br>";
    }
    my $tempSet = $defSet;
    if ($action ne "setup" && $action ne "new_site" && $message eq "" && $action
ne "update") {
        read_resource_file();
    }
    if ($action eq "add" || $action eq "update") {
       $defSet = $tempSet;
    }
    my $sub = join("\n", @subject);
    my $format = join("\n", @format);
    
    use vars qw($wave_string $res_string $cont_string $imtype_string  $rt_string $ctype_string $rtype_string $region_frame_string $range_frame_string $wave_range_string $dataType_string);
    $wave_string = "";
    $wave_range_string = "";
    $res_string = "";
    $cont_string = "";
    $imtype_string = "";
    $ctype_string = "";
    $rtype_string = "";
    $rt_string = "";
    $region_frame_string = "";
    $range_frame_string = "";
    $dataType_string = "";
    use vars qw(%set %wave @atype %clevel %conType %rtType @imType %relType %frmType %data);
    %set = ();
    %wave = ();
    @atype = ();
    %clevel = ();
    %conType = ();
    %relType = ();
    %frmType = ();
    %rtType = ();
    @imType = ();
    %data= ();

# Build some strings for use by the form

    my @type = qw(Other Archive Bibliography Catalog Journal Library Simulation Survey Transformation Education Outreach EPOResource Animation Artwork Background BasicData Historical Photographic Press Organisation );
    my @dataType= qw(string boolean bit unsignedByte short int long char unicodeChar
float double floatComplex doubleComplex);
    my @srange = qw(Radio Millimeter Infrared Optical UV EUV X-ray Gamma-ray);
    my  @defContent = ("General","Elementary Education","Middle School Education","Secondary Education","Community College", "University","Research","Amateur", "Informal Education");                          
    my  @defRelation = ("mirror-of","service-for","derived-from","related-to");                          
    my @defRights = qw(public secure proprietary);
    foreach my $waveband (@waveband) {
			$wave{$waveband} = 
                "<option selected value=$waveband>$waveband";
    }
    foreach my $id (@srange) {
		if ($wave{$id} =~ /option/) {
			$wave_string .= $wave{$id};
		} else {
			$wave_string .= "<option value=$id>$id";
		}
    }
    foreach my $con (@content) {
			$clevel{$con} = 
                "<option selected value=$con>$con";
    }
    foreach my $id (@defContent) {
		if ($clevel{$id} =~ /option/) {
			$cont_string .= $clevel{$id};
		} else {
			$cont_string .= "<option value=$id>$id";
		}
    }

    foreach my $con (@ctype) {
			$conType{$con} = 
                "<option selected value=$con>$con";
    }

    foreach my $id (@type) {
		if ($conType{$id} =~ /option/) {
			$ctype_string .= $conType{$id};
		} else {
			$ctype_string .= "<option value=$id>$id";
		}
    }

    foreach my $rel (@relation) {
			$relType{$rel} = 
                "<option selected value=$rel>$rel";
    }

    foreach my $id (@defRelation) {
		if ($relType{$id} =~ /option/) {
			$rtype_string .= $relType{$id};
		} else {
			$rtype_string .= "<option value=$id>$id";
		}
    }

    foreach my $rt (@rights) {
			$rtType{$rt} = 
                "<option selected value=$rt>$rt";
    }

    foreach my $id (@defRights) {
		if ($rtType{$id} =~ /option/) {
			$rt_string .= $rtType{$id};
		} else {
			$rt_string .= "<option value=$id>$id";
		}
    }
    foreach my $im (@defImTypes) {
		if ($im eq $imServType) {
			$imtype_string .= "<option selected value=$im>$im";
		} else {
			$imtype_string .= "<option value=$im>$im";
		}
    }

    foreach my $set (@userSets) {
			$set{$set} = "<option selected value=$set>$set";
    }

    foreach my $fr (@defFrames) {
		if ($fr eq $rangeFrame) {
			$range_frame_string .= "<option selected value=$fr>$fr";
		} else {
			$range_frame_string .= "<option value=$fr>$fr";
		}
    }

    foreach my $fr (@defFrames) {
		if ($fr eq $regionFrame) {
			$region_frame_string .= "<option selected value=$fr>$fr";
		} else {
			$region_frame_string .= "<option value=$fr>$fr";
		}
    }
    foreach my $wr (@waveRange) {
		if ($wr eq "meters") {
			$wave_range_string .= "<option selected value=$wr>$wr";
		} else {
			$wave_range_string .= "<option value=$wr>$wr";
		}
    }


    

# Removed; May be reimplemented later. RLW 4/17/03.

#    foreach my $id (@repSets) {
#		if ($set{$id} =~ /option/) {
#			$set_string .= $set{$id};
#		} else {
#			$set_string .= "<option value=$id>$id";
#		}
#    }

# Add action header

    if ($action eq "add" || $action eq "update") {
        $submitButton = "   Add Resource  ";
        print 
	    $form->header,
	    $form->start_html(-title=>'New Resource Entry Form', 
			      -bgcolor=>'white',
                              -head=>CGI::Link({-rel=>'stylesheet', 
                                                -type=>'text/css',
                                                -href=>'/usvo_template.css'})),
	    "\n", &formatBanner(), $form->h1('New Resource Entry Form'), "\n";
	print <<EOT;
Use this page to <strong>add</strong> a new resource description.  The 
default values are those of one of your previously registered resources.  
Change only those values that are different; then click the 
<strong>Add Resource</strong> button at the bottom of the form.  Click 
the <strong>Reset</strong> button to return to the default values. Use your
Browser's <strong>Back</strong> button to cancel the add.<p> <!-- ' -->

EOT

# Edit action header

    } elsif ($action eq "edit" || $action eq "editupdate") {
        $submitButton = " Submit Changes";
	print
	    $form->header,
	    $form->start_html(-title=>'Edit Resource Form', 
			      -bgcolor=>'white',
                              -head=>CGI::Link({-rel=>'stylesheet', 
                                                -type=>'text/css',
                                                -href=>'/usvo_template.css'})),
	    "\n", &formatBanner(), $form->h1('Edit Resource Form'), "\n\n";
	print <<EOT;
This page enables you to edit a resource description that has already
been registered.  Change the inputs below as necessary and then hit the 
<strong>Submit Changes</strong> button.  Click the <strong>Reset</strong> 
button to return to the original values. Use your Browser's  <!-- ' -->
<strong>Back</strong> button to cancel the edit.<p>

EOT

# Setup action header

    } elsif ($action eq "setup") {
        # Create default set directories, if not already there
        foreach my $id (@defSets) {
            `mkdir "$path/$id"` unless -e "$path/$id";
            `chmod 0755 "$path/$id"` unless -e "$path/$id";
        }

        $submitButton = "   Next   ";
        print
		    $form->header,
		    $form->start_html(-title=>'Setup Repository', 
                                      -bgcolor=>'white',
                                      -head=>CGI::Link({-rel=>'stylesheet', 
                                                        -type=>'text/css',
                                            -href=>'/usvo_template.css'})),
		    "\n", &formatBanner(),
		    $form->h1('Setup Repository'),
		    "\n",
		    "This page will set up the configuration for the Respository.\n",
		    "<p>",
            " Fill in appropriate fields for this repository and then click on the \"Next\" Button to continue.",
		    "<p>",
            " The \"Reset\" button will reset all changed fields."
    } elsif ($action eq "new_site") {
        $submitButton = "   Next   ";
        print
            $form->header,
            $form->start_html(-title=>'Setup New Site', -bgcolor=>'white',
                              -head=>CGI::Link({-rel=>'stylesheet', 
                                                -type=>'text/css',
                                                -href=>'/usvo_template.css'})),
            "\n", &formatBanner(), 
            $form->h1('Register a New Publisher'),
            "\n";
        print <<EOT;

Use this form to describe your organisation, which will serve as the
publisher (or <em>Naming Authority</em>) for your resources.  When
this form is submitted, it will create two resource descriptions in your
workspace: an "Organisation" and an "Authority."  <p>

Fill in appropriate fields for this site and then click on the
"Next" Button to continue.  The "Reset" button will empty all the
fields.  <p>
EOT
    } # End of header definitions

    print <<EOT;

Not all inputs need to be filled in.  Any non-required information
that does not apply to your resource can be left blank.  The relative
importance of an input is given by the following labels:
<table border="0" cellspacing="8" cellpadding="4">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td>Required</td>
  </tr>
  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td>Recommended</td>
  </tr>
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td>Optional</td>
  </tr>
</table> <p>

To learn more about what an input item means, click on its highlighted 
name to access <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#nvohelp" target="nvohelp">help</a> on 
that item.  <p>
EOT

if ($action eq "setup" && $action eq "new_site") { 
    print <<EOT;
        <h2>Resource Type: Organisation</h2>
EOT
}
# Removed; May be reimplemented later. RLW 4/17/03.

#    if ($action ne "setup"  && $action ne "new_site") {
#        $startString =  
#        "<a href=\"$ENV{'PATH_INFO'}/formhelp.html#Resource\"\>Resource:</a\>   "; 
#        $startString .= $defSet;
#		$startString .= "<p>";
#        $startString .= "<p>";
# 
#        $startString .= "<a href=\"$ENV{'PATH_INFO'}/formhelp.html#Sets\"\>Sets:</a\>   "; 
#        $startString .= "<select name=\"set\" size=our $num_sets multiple>";
#        $startString .= "$set_string";
#        $startString .= "</select>";
#		$startString .= "<p>";
#        $startString .= "<p>";
#    } 


# Body of form 
    $message = '' if ($message =~ /params added/);
    print "<p><b><font color=\"#ff0000\">\n$message\n</font></b><p>\n"
	if ($message ne '');
    print $form->start_form;

#    print "$startString\n<p><p>\n";
	      print <<EOT;
    <table border="0" width="100%" cellspacing="8" cellpadding="4">
EOT

       if ($action eq "edit" || $action eq "editupdate") {
	      print <<EOT;
  <tr>
    <td colspan="3"><font size="5"<b>Resource Type: $defSet</b></font></td>
  </tr>

  <tr>
    <td colspan="3"><font size="5"<b>Identifer: ivo://$authorityId/$resourceKey</b></font></td>
  </tr>
EOT
       } else {
          if ($action eq "setup" || $action eq "new_site" 
                || $defSet eq "Authority") { 
             $resourceKey = "";
	         print <<EOT;

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AuthorityID" target="nvohelp">Authority ID:</a>     
    </td>
    <td>
      <input type="text" name=authorityId size=60  value="$authorityId">
	<br />
	<font color="green"><em>
        A globally unique namespace that you will control; 
        <br /> e.g. "adil.ncsa" or "ncsa.adil" or "ncsa"
        </em></font> 
    </td>
  </tr>
EOT
          } else {
	        my @list = read_resources();
	        my $options = '';
	        foreach my $res (@list) {
                # Only show Authorities for this list
                next if ($res->[6] ne "Authority");
	            $options .= "  <option value=\"$res->[4]\"";
	            $options .= " selected" if ($res->[4] eq "$authorityId");
	            $options .= ">$res->[4] ($res->[2])</option>\n";
	        }
            if ($options =~ /selected/) { 
	            $options .= '  <option value="">Select a registered Authority</option>';
            } else {
	            $options .= '  <option value="" selected>Select a registered Authority</option>';
            }
	        print <<EOT;
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AuthorityID" target="nvohelp">Authority ID:</a>     
    </td>
    <td>
<select name="authorityId">
$options
</select>
	<br />
	<font color="green"><em>
      A globally unique namespace controlled by a single naming authority
    <br />
	  Select the previously registered Authority.     
    </td>
  </tr>
EOT
        }
        if($defSet ne "Authority") {
           print <<EOT;
           <tr>
           <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
           <td valign="top" width="25%">
           <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ResourceKey"
               target="nvohelp">Resource Key:</a>     
           <td>
           <input type="text" name=resourceKey size=60  value="$resourceKey">
	       <br />
	       <font color="green"><em>
            A localized name for a resource that is unique within the namespace
            of the authority ID. 
           </em></font> 
           </td>
           </tr>
EOT
        }
       }
print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">General Information:<td>
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Title" target="nvohelp">Title:</a>   
    </td>
    <td>
      <input type="text" name=title size=60  value="$title"> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ShortName" target="nvohelp">Short Name:<a>
    </td>
EOT
    if ($action eq "new_site" || $action eq "setup") {
    print <<EOT;
    <td>
      <input type="text" name=sname size=11  value="$sname">
	<br />
	<font color="green"><em>
	A short name (11 characters or less) that can be used to identify this resource in a 
        compact display of many resources
        </em></font> 
    </td>
EOT
    } else {
        print<<EOT;
    <td>
      <input type="text" name=sname size=16  value="$sname">
	<br />
	<font color="green"><em>
	A short name (16 characters or less) that can be used to identify this resource in a 
        compact display of many resources
        </em></font> 
    </td>
EOT
    }
print<<EOT;
  </tr>

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Curation Information:<td>
  </tr>

EOT

##   if ($action ne "setup"  && $action ne "new_site") {
##	my @list = read_resources();
##	my $options = '';
##	foreach my $res (@list) {
##	    $options .= "  <option value=\"$res->[3]\"";
##	    $options .= " selected" if ($res->[3] eq "$manageBy");
##	    $options .= ">$res->[2] ($res->[3])</option>\n";
##	}
##	$options .= '  <option value="">[None]</option>';
##	print <<EOT;
##  <tr>
##    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
##    <td valign="top" width="25%">
##<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ManagedBy" target="nvohelp">Resource Managed By:</a>   
##    </td>
##    <td>
#/<select name="manageBy">
##$options
##</select>
##	<br />
##	<font color="green"><em>Select the previously registered
##        resource that manages this one.</em></font>
##    </td>
##  </tr>
##EOT
##    }

#  print <<EOT;
#  <tr>
#    <td colspan="2" bgcolor="#eeeeee">Publisher Resource Information:<td>
#  </tr>
#EOT
	my @list = read_resources();
    my @orgs_list = ();
	my $options = '';
	foreach my $res (@list) {
       push (@orgs_list, $res) if ($res->[0] =~ /Organisation/);
    }
    if (scalar(@orgs_list)) { 
	   foreach my $res (@orgs_list) {
           if ($res->[0] =~ /Organisation/) {
	       $options .= "  <option value=\"$res->[2] (ivo://$res->[4]/$res->[5])\"";
	       $options .= " selected" 
                if ($res->[4] eq $pub_auth && $res->[5] eq $pub_key);
	       $options .= ">$res->[2] (ivo://$res->[4]/$res->[5])</option>\n";
	   }
	   $options .= '  <option value="">[None]</option>';
       if ($options =~ /selected/) { 
	     $options .= '  <option value="">Select a registered Publisher</option>';
       } else {
	      $options .= '  <option value="" selected>Select a registered Publisher</option>';
       }
          }
	   print <<EOT;
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Publisher" 
          target="nvohelp">Publisher</a>   
    </td>
    <td>
        <select name="publisherId">
        $options
        </select>
	<br />
	<em><font color="green">Select the previously registered
        publisher, 
        <b>or fill in the</b></font> <b>Publisher's Title</b> <!-- ' -->
        <font color="green"><b>below:</b></font></em>  <br />
        <input type="text" name=pub_title size=60  value="$pub_title"> 
    </td>
EOT
    } else {
	print <<EOT;
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Publisher" 
          target="nvohelp">Publisher</a>   
    </td>
    <td>
        <input type="text" name=pub_title size=60  value="$pub_title"> 
	    <br/><em><font color="green"> An entity responsible for making the resource available
    </td>
  </tr>
EOT
    }
## Commented out for now; kept for late use 12/2/2003 RLW.
# <tr>
#    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
#    <td valign="top" width="25%">
#<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Description" target="nvohelp">Publisher Description:</a>     
#    </td>
#    <td>
#        <textarea name=pub_description cols=60 rows=6>$pub_description</textarea>
#    </td>
#  </tr>
#
#  <tr>
#    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
#    <td valign="top" width="25%">
#<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ReferenceURL" target="nvohelp">Publisher Reference URL:</a>     
#    </td>
#    <td>
#        <input type="text" name=pub_refURL size=60  value="$pub_refURL">
#	<br />
#	<font color="green"><em>
#	A URL to a human-readable document giving more information
#        about this resource.
#        </em></font> 
#    </td>
#  </tr>
##
  print <<EOT;
  <tr>
    <td colspan="2" bgcolor="#eeeeee">Creator Information:<td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#CreatorName" target="nvohelp">Creator Name:</a>     
    </td>
    <td>
        <input type="text" name=creator size=60  value="$creator">
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Date" target="nvohelp">Creation Date:</a>
    </td>
    <td>
        <input type="text" name=date size=20  value="$date">
	<br />
	<font color="green"><em>
        (Example: 1984, 1990-07, 2001-04-25)
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#CreatorLogo" target="nvohelp">URL to Creator Logo:</a>
    </td>
    <td>
        <input type="text" name=logo size=60  value="$logo">
    </td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">                  <td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Contributor" target="nvohelp">Contributor:</a>
    </td>
    <td>
        <input type="text" name=contributor size=60  value="$contributor">
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Version" target="nvohelp">Version:</a>
    </td>
    <td>
        <input type="text" name=version size=60  value="$version">
    </td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Contact Information:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ContactName" target="nvohelp">Contact Name:</a>     
    </td>
    <td>
        <input type="text" name=contact size=60  value="$contact_name">
    </td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ContactEmail" target="nvohelp">Contact Email:</a>     
    </td>
    <td>
        <input type="text" name=contactEmail size=60  value="$contact_email">
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ContactAddress" target="nvohelp">Contact Address:</a>     
    </td>
    <td>
        <textarea name=contactAddress cols=60 rows=3>$contact_address</textarea>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ContactTelephone" target="nvohelp">Contact Telephone:</a>     
    </td>
    <td>
        <input type="text" name=contactTelephone size=60  value="$contact_telephone">
    </td>
  </tr>

EOT

  print <<EOT;

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Content Information:<td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Subject" target="nvohelp">Subject:</a> 
        <br />
	<font color="green"><em>
        one subject per line.
        </em></font> 
    </td>
    <td>
        <textarea name=subject cols=60 rows=4>$sub</textarea>
    </td>
  </tr>

 <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Description" target="nvohelp">Description:</a>     
    </td>
    <td>
        <textarea name=description cols=60 rows=6>$description</textarea>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Source" target="nvohelp">Source:</a>
    </td>
    <td>
        <input type="text" name=source size=60  value="$source">
	<br />
	<font color="green"><em>
        an ADS bibcode for the article that this resource is derived from.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ReferenceURL" target="nvohelp">Reference URL:</a>     
    </td>
    <td>
        <input type="text" name=refURL size=60  value="$refURL">
	<br />
	<font color="green"><em>
	A URL to a human-readable document giving more information
        about this resource.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Type" target="nvohelp">Type:</a>     
        <br />
	<font color="green"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
      <select name="type" size=7 multiple>
      $ctype_string
      </select>
    </td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ContentLevel" target="nvohelp">Content Level:</a>     
        <br />
	<font color="green"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
      <select name="content" size=7 multiple>
      $cont_string
      </select>
    </td>
  </tr>
EOT
    if ($defSet ne "Resource" && $defSet ne "Authority" && 
                $defSet ne "Registry" ) {
	my @list = read_resources();
	my $options = '';
	foreach my $res (@list) {
	    $options .= "  <option value=\"$res->[2] (ivo://$res->[4]/$res->[5])\"";
	    $options .= " selected" 
                if ($res->[4] eq $fac_auth && $res->[5] eq $fac_key);
	    $options .= ">$res->[2] (ivo://$res->[4]/$res->[5])</option>\n";
	}
	   $options .= '  <option value="">[None]</option>';
    if ($options =~ /selected/) { 
	   $options .= '  <option value="">Select a registered Facility</option>';
    } else {
	   $options .= '  <option value="" selected>Select a registered Facility</option>';
    }
 	print <<EOT;

   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
     <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Facility" target="nvohelp">Facility:</a>
     </td>
     <td>
 <select name="facilityId">
 $options
 </select>
 	<br />
 	<font color="green"><em>Select the previously registered
         facility, 
        <b>or fill in the</b></font> <b>Facility's Name below</b> 
        <font color="green"><b>below:</b></font></em>  <br />
        <input type="text" name=facility size=60  value="$facility"> 

     </td>
   </tr>
 
EOT

	my @list = read_resources();
	my $options = '';
	foreach my $res (@list) {
	    $options .= "  <option value=\"$res->[2] (ivo://$res->[4]/$res->[5])\"";
	    $options .= " selected" 
                if ($res->[4] eq $ins_auth && $res->[5] eq $ins_key);
	    $options .= ">$res->[2] (ivo://$res->[4]/$res->[5])</option>\n";
	}
	   $options .= '  <option value="">[None]</option>';
    if ($options =~ /selected/) { 
	   $options .= '  <option value="">Select a registered Instrument</option>';
    } else {
	   $options .= '  <option value="" selected>Select a registered Instrument</option>';
    }
 	print <<EOT;

   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
     <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Instrument" target="nvohelp">Instrument:</a>
     </td>
     <td>
 <select name="instrumentId">
 $options
 </select>
 	<br />
 	<font color="green"><em>Select the previously registered
         instrument, 
        <b>or fill in the</b></font> <b>Instrument's Name</b> 
        <font color="green"><b>below:</b></font></em>  <br />
        <input type="text" name=instrument size=60  value="$instrument"> 

     </td>
   </tr>
 
EOT
}

## Related Resource information and Logical Identifier information
## commented out put kept in for future use - 12/2/2003 RLW
## Reinstate as Relationship - 7/22/2004 RLW

 print <<EOT;
 
   <tr>
     <td colspan="3" bgcolor="#eeeeee">Related Resource Information:<td>
   </tr>
 
   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
 <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Relationship" target="nvohelp">Relationship:</a>     
         <br />
 	<font color="green"><em>
         The relationship of this resource to another resource.
         </em></font> 
     </td>
     <td>
       <select name="relation" size=4>
       $rtype_string
       </select>
     </td>
   </tr>
 
EOT

	my @list = read_resources();
	my $options = '';
	foreach my $res (@list) {
	    $options .= "  <option value=\"$res->[2] (ivo://$res->[4]/$res->[5])\"";
	    $options .= " selected" 
                if ($res->[4] eq $rel_auth && $res->[5] eq $rel_key);
	    $options .= ">$res->[2] (ivo://$res->[4]/$res->[5])</option>\n";
	}
	   $options .= '  <option value="">[None]</option>';
    if ($options =~ /selected/) { 
	   $options .= '  <option value="">Select a registered Resource</option>';
    } else {
	   $options .= '  <option value="" selected>Select a registered Resource</option>';
    }
 	print <<EOT;
   <tr>

     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
 <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#RelatedResourceID" target="nvohelp">Related Resource:</a>     
     </td>
     <td>
 <select name="relatedResourceId">
 $options
 </select>
 	<br />
 	<font color="green"><em>Select the previously registered
         resource, 
        <b>or fill in the</b></font> <b>Resource's Name</b> <!-- ' -->
        <font color="green"><b>below:</b></font></em>  <br />
        <input type="text" name=rel_title size=60  value="$rel_title"> 

     </td>
   </tr>
 
EOT
    if ($defSet eq "DataCollection" ) {
print <<EOT;

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Resource Access Description:<td>
  </tr>


  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Formats" target="nvohelp">Formats:</a>
        <br />
	<font color="green"><em>
        one MIME format type per line; e.g. "image/fits"
        </em></font> 
    </td>
    <td>
        <textarea name=format cols=30 rows=4>$format</textarea>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Rights" target="nvohelp">Rights:</a> 
        <br />
	<font color="green"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
      <select name="rights" multiple> $rt_string
      </select>
    </td>
  </tr>
EOT
    }
    if ($defSet eq "DataCollection" || $defSet eq "BrowserBasedService"
            || $defSet eq "GLUService" || $defSet eq "SIAService"
            || $defSet eq "ConeSearch" || $defSet eq "SSAService" 
            || $skyservice || $tabskyservice) {

   my $checked_AllSky = "";
   my $checked_CircleRegion = "";
   my $checked_CoordRange = "";
   if ($region eq "vs:AllSky"){
      $checked_AllSky = "checked";
   } elsif ($region eq "vs:CircleRegion") {
      $checked_CircleRegion = "checked";
   } elsif ($region eq "vs:CoordRange") {
      $checked_CoordRange = "checked";
   }

    print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Data Coverage:<td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Spatial Information<td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#SpatialCoverage" 
          target="nvohelp">Spatial Coverage:</a>
    <td> 
	<font color="green"><em>
        Select one of the options for Spatial Coverage and fill in
        relevant fields. </em></font> 

        <dl>
           <dt> <input type=radio name=region 
                       value="AllSky" $checked_AllSky>All Sky
                <br />
	        <font color="green"><em>
                Coverage is the entire sky.
                </em></font> <p>

           <dt> <input type=radio name=region 
                       value="CircleRegion" $checked_CircleRegion>Circle Region
                <br />
	        <font color="green"><em>
                  A central position and angular radius.
                </em></font> 

           <dd> 
             <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#CoordinateFrame" 
                target="nvohelp">Coordinate Frame:</a>
             <select name="regionFrame">
             $region_frame_string
             </select>  <br />

             Longitude:  
             <input type="text" name=region_long size=15  value="$region_long">
             &nbsp; &nbsp; Latitude: 
             <input type="text" name=region_lat size=15 value="$region_lat">
             <br />
             
             Radius (degrees):
             <input type="text" name=region_rad size=15  value="$region_rad">
             <p>

           <dt> <input type=radio name=region 
                       value="CoordRange" $checked_CoordRange>Coordinate Range
                <br />
	        <font color="green"><em>
                  A range of longitude and latitude (in degrees). 
                </em></font>
                <br />  

           <dd>
             <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#CoordinateFrame" 
                target="nvohelp">Coordinate Frame:</a>
             <select name="rangeFrame">
             $range_frame_string
             </select> <br />

      Longitude:   Min:  
      <input type="text" name=range_long_min size=15  value="$range_long_min">
      &nbsp; &nbsp; Max: 
      <input type="text" name=range_long_max size=15  value="$range_long_max">
      <br />

      Latitude:   Min:  
      <input type="text" name=range_lat_min size=15  value="$range_lat_min">
      &nbsp; &nbsp; Max: 
      <input type="text" name=range_lat_max size=15  value="$range_lat_max">
        </dl>
    </td>
    </tr>

    <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#SpatialResolution" target="nvohelp">Spatial Resolution:</a></td>
    <td valign="top">
      <input type="text" name=spatial_res size=15  value="$spatial_res">
        &nbsp;
	<font color="green"><em>
        The spatial (angular) resolution that is typical of the observations
of interest, in decimal degrees.
        </em></font> 
    </td>
  </tr>
    <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#RegionofRegard" target="nvohelp">Region of Regard:</a></td>
    <td valign="top">
      <input type="text" name=region_regard size=15  value="$region_regard">
        &nbsp;
	<font color="green"><em>
        The intrinsic size scale, given in arcseconds, associated with data
items contained in a resource.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Spectral Information<td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td valign="top" width="25%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Waveband" 
          target="nvohelp">Waveband:</a>
        <br />
	<font color="green"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
        <select name="waveband" size=5 multiple>
        $wave_string
        </select>
    </td>
  </tr>
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td>
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#WavelengthRange" 
          target="nvohelp">Wavelength Range:</a>
    </td>
    <td>
        Min:  <input type="text" name=wave_min size=25  value="$wave_min">
        <select name="wave_min_units" size=1 >
        $wave_range_string
        </select>
        <br />
        Max: <input type="text" name=wave_max size=25  value="$wave_max">
        <select name="wave_max_units" size=1 >
        $wave_range_string
        </select>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td colspan="2">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#SpectralResolution" target="nvohelp">Spectral Resolution:</a>
      <input type="text" name=spec_res size=15  value="$spec_res">
    </td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#eeeeee">Time Information<td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Temporal" 
          target="nvohelp">Temporal Coverage:</a>
    </td>
    <td>
      Start:  
      <input type="text" name=temporal_start size=15  value="$temporal_start">
      &nbsp; &nbsp; End: 
      <input type="text" name=temporal_end size=15  value="$temporal_end">
        <br />
	<font color="green"><em>
        (Example: 1984, 1990-07, 2001-04-25)
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td colspan="2">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#TemporalResolution" target="nvohelp">Temporal Resolution (seconds):</a>
      <input type="text" name=temporal_res size=15  value="$temporal_res">
    </td>
  </tr>

EOT
}
    if ($defSet eq "CGIService" || $defSet eq "WebService" || 
        $defSet eq "Service") 
    {
        if (!$skyservice && !$tabskyservice && ($action eq "add" || $action eq "update" || $action eq "edit" || $action eq "editupdate")) {
        $numParam = scalar(@param_name) if ($action eq 'add' || $action eq
"update" || $action eq "edit" || $action eq "editupdate");
        $numParam = 3 if ($numParam eq 0);
        print <<EOT;
           <tr>
               <td colspan="3" bgcolor="#eeeeee">Data Coverage:</td>
           </tr>
           <tr>
           <td bgcolor="#eeeeee" valign="top"><font color="white">Opt.</font></td>
           <td><input type=\"checkbox\" name=\"skyservice\"></td><td>Check Here if this service has space, time or frequency coverage parameters.</td>
            </tr>
            <tr>
            <td/><td/><td/>Then press the Update Coverage Button to bring up Coverage
parameters.</td>
           </tr>
           <tr>
            <td/>
        <td><input type=hidden name=numParam value="$numParam"></td>
		<td><input type=submit name=ftype value="Update Coverage"></td>
        </tr>
EOT
        }
    }    
    if ( $defSet eq "SIAService" ) {
        print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Capabilities and Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#ImageServiceType" target="nvohelp">Image Service Type:</a>     
    </td>
    <td>
      <select name="imServType" size=1 >
      $imtype_string
      </select>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxRegionSize" target="nvohelp">Maximum Query Region Size:</a>
    </td>
    <td>
        Longitude:  
        <input type="text" name=maxRegSize_long size=15 value="$maxRegSize_long">
        &nbsp; &nbsp; Latitude:  
        <input type="text" name=maxRegSize_lat size=15 value="$maxRegSize_lat">
        <br />
	<font color="green"><em>
	Largest queryable region in decimal degrees; enter "360.0" and "180.0"
        if there is no limit.  
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxImExt" target="nvohelp">Maximum Image Extent:</a>
    </td>
    <td>
        Longitude: 
        <input type="text" name=maxImExt_long size=20 value="$maxImExt_long">
        &nbsp; &nbsp; Latitude:  
        <input type="text" name=maxImExt_lat size=20 value="$maxImExt_lat">
        <br />
	<font color="green"><em>
	Largest image that can be returned in decimal degrees; enter "360.0" 
        and "180.0" if there is no limit.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxImSize" target="nvohelp">Maximum Image Size:</a>     
    </td>
    <td>
        Longitude: 
        <input type="text" name=maxImSize_long size=20 value="$maxImSize_long">
        &nbsp; &nbsp; Latitude:  
        <input type="text" name=maxImSize_lat size=20 value="$maxImSize_lat">
        <br />
	<font color="green"><em>
	Size of largest image that can be returned in integer pixels
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxFSize" target="nvohelp">Maximum File Size:</a>     
    </td>
    <td>
        <input type="text" name=maxFSize size=20  value="$maxFSize">
        <br />
	<font color="green"><em>
	Filesize of largest image that can be returned in bytes
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxRec" target="nvohelp">Limit on Number of Returned Records:</a>
    </td>
    <td>
        <input type="text" name=maxRec size=20  value="$maxRec">
    </td>
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#BaseURL" target="nvohelp">Base URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The base URL to use for SIA image queries.
        </em></font> 
    </td>
  </tr>

EOT
    }
    if ($defSet eq "ConeSearch") {
        print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Capabilities and Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxSR" target="nvohelp">Maximum Search Radius:</a>
    </td>
    <td>
        <input type="text" name=maxSR size=20  value="$maxSR">
        <br />
	<font color="green"><em>
	Largest search radius, in degrees. 
        </em></font> 
    </td>
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#MaxRec" target="nvohelp">Maximum Number of Records to Return:</a>
    </td>
    <td>
        <input type="text" name=maxRec size=20  value="$maxRec">
    </td>
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Verbosity" target="nvohelp">Verbosity:</a>
EOT
    if ($verbosity eq "true") {
    print <<EOT;
    <td colspan="3"><input type=checkbox name=verbosity value="Verbosity"
checked>
    <br />
	<font color="green"><em>
          Click here if the service supports the VERB keyword
        </em></font> 
    </td>
  <tr>
EOT
    } else {
        print <<EOT;
    <td colspan="3"><input type=checkbox name=verbosity value="Verbosity">
    <br />
	<font color="green"><em>
          Click here if the service supports the VERB keyword
        </em></font> 
    </td>
  <tr>
EOT
}
    print <<EOT;
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#BaseURL" target="nvohelp">Base URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The base URL to use for ConeSearch queries.
        </em></font> 
    </td>
  </tr>
EOT
    }
    if ( $defSet eq "BrowserBasedService" ) {
    print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AccessURL" target="nvohelp">Access URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The URL of the document containing the Web-based form.
        </em></font> 
    </td>
  </tr>

EOT
    }        
    if ( $defSet eq "SSAService") {
    print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AccessURL" target="nvohelp">Access URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The URL to access the SSA Service.
        </em></font> 
    </td>
  </tr>
  <tr>
EOT
   }
    if ( $defSet eq "CGIService" || $defSet eq "GLUService" || 
         $defSet eq "WebService" || $defSet eq "Service") {
    print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AccessURL" target="nvohelp">Access URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The URL to access the Service.
        </em></font> 
    </td>
  </tr>
  <tr>
EOT
    if ($defSet eq "CGIService" || $defSet eq "WebService" || $defSet eq "Service") {
        if ($action eq "add" || $action eq "update" || $action eq "edit" ||
$action eq "editupdate") {
        my $tabcheck = "";

        
        my $checked_VOTable="";
        my $checked_CSVTable="";
        my $checked_HTMLTable="";
        my $checked_Other="";

        if ($outputMime eq "application/xml+votable" || $output eq "VOTable") {
            $outputMime = "";
            $checked_VOTable = "checked";
            $tabskyservice = "1";
        } elsif ($outputMime eq "application/csv" || $output eq "CSVTable") {
            $outputMime = "";
            $checked_CSVTable = "checked";
            $tabskyservice = "1";
        } elsif ($outputMime eq "text/html" || $output eq "HTMLTable") {
            $outputMime = "";
            $checked_HTMLTable = "checked";
            $tabskyservice = "1";
        } elsif ($outputMime ne ""){
            $checked_Other = "checked";
        } 

        if ($tabskyservice && $checked_Other) {
           $tabcheck = "<td colspan=2><input type=\"checkbox\" name=\"tabskyservice\" checked>Is this tabular output?</td>";
        } else {
           $tabcheck .= "<td align=\"center\" width=\"25%\" colspan=2><input type=\"checkbox\" name=\"tabskyservice\">Is this tabular output?</td>";
        }


        print <<EOT;
           <tr>
               <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
               <td valign="top">Output Type:</td>
           <td colspan=3> 
        <table noborder width=100% cellpadding=1 cellspacing=8>
            <tr>
           <td><input type=radio name=output 
                       value="VOTable" $checked_VOTable>VOTable 
           </td>
           <td> <input type=radio name=output 
                       value="CSVTable" $checked_CSVTable>CSV Table 
           </td>
           <td> <input type=radio name=output 
                       value="HTMLTable" $checked_HTMLTable>HTML Table 
           </td>
           </tr>
           <tr>
           <td> <input type=radio name=output 
                       value="Other" $checked_Other>Other:
           </td>
           </tr>
           <tr>
            <td valign="top" align="right" width="25%">
             <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#OutputMime" target="nvohelp">Output Mime Type:</a>
    </td>
    </td>
    <td colspan=2>
        <input type="text" name=outputMime size=60  value="$outputMime">
    </td>
           </tr>
            <tr>
                $tabcheck
            </tr>
           </table>
           </td>
           </tr>
EOT
        }
    }

    if ($defSet eq "CGIService") {
        print <<EOT;

      <td colspan="2" bgcolor="#eeeeee">Service Parameters:<td>
    </tr>
EOT
$numParam = scalar(@param_name) unless ($action eq "add" || $action eq
"update" || $action eq "editupdate");
        $numParam = 3 if ($numParam eq 0);
    for (my $i = 0; $i < $numParam; $i++) {
            $dataType_string = "";
            %data = ();
            if ($param_dataType[$i] eq "char" && $param_arraysize[$i] eq "*"){
                $param_dataType[$i] = "string";
            } 
			$data{$param_dataType[$i]} = 
                "<option selected value=$param_dataType[$i]>$param_dataType[$i]";
        foreach my $id (@dataType) {
	    	if ($data{$id} =~ /option/) {
	    		$dataType_string .= $data{$id};
	    	} else {
	    		$dataType_string .= "<option value=$id>$id";
	    	}
        }
        my $checkbox_stat = "";
        if (@param_req[$i] eq 'YES') {
            $checkbox_stat = "<input type=checkbox name=\"param_req$i\" checked>Required Parameter"
        } else {
            $checkbox_stat = "<input type=checkbox name=\"param_req$i\" >Required Parameter"
        }
    print <<EOT;
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="75%">
       <a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#Parameter" 
          target="nvohelp">Parameter:</a>
    </td>
     </tr>
    <tr>
    <td colspan=3> 
        <table cellpadding="0" border="0">
        <tr>
        <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
        <td>Name:  <input type="text" name="param_name$i" size=20 value=\"$param_name[$i]\"></td>
        <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
        <td>DataType:
        <select name="param_dataType$i" size=1 >
        $dataType_string
        </select>
        </td>
        </tr>
        <tr>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
        <td>
        Unit:<input type="text" name=\"param_unit$i\" size=10 value = \"$param_unit[$i]\">
        </td>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
        <td>
        <a href="http://vizier.u-strasbg.fr/UCD/old/ucds_from_kw/"
        target="nvohelp">UCD:</a><input type="text" name=\"param_ucd$i\" size=10
        value=\"$param_ucd[$i]\">
        </td>
        </tr>
        <tr>
       <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
       <td colspan=4>Description: <input type="text" name=\"param_desc$i\" size=60 value=\"$param_description[$i]\"><td>
        </tr>
        <tr>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
        <td>
        $checkbox_stat
        </td>
        </tr>
        </table>
    </td>
  </tr>
EOT
    }
    if ( $action eq "add" || $action eq "update") {
       print <<EOT;
  <tr></tr>
      <td/><td><input type=\"text\" name=\"addparam\" size=5></td><td>If you need to add more parameters, type in the number to add.
       <br />Then press the Add Param Button.</td>
      </tr>
      <tr>
       <td/>
   <td><input type=hidden name=numParam value="$numParam">
   <input type=hidden name=skyservice value="$skyservice">
   <input type=hidden name=tabskyservice value="$tabskyservice">
   <input type=submit name=ftype value="Add Param"></td>
   </tr>
EOT
    } elsif ( $action eq "edit" || $action eq "add" || $action eq "editupdate") {
       print <<EOT;
  <tr></tr>
      <td/><td><input type=\"text\" name=\"addparam\" size=5></td><td>If you need to add more parameters, type in the number to add.
       <br />Then press the Add Param Button.</td>
      </tr>
      <tr>
       <td/>
   <td><input type=hidden name=numParam value="$numParam">
   <input type=hidden name=skyservice value="$skyservice">
   <input type=hidden name=tabskyservice value="$tabskyservice">
   <input type=hidden name=inputfname value="$resource_file">
   <input type=submit name=ftype value="Add Param "></td>
   </tr>
EOT

    }
    }
    }
    if ( $action eq "setup" && $new_registry eq "true") {
    print <<EOT;
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Registry Interface:<td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="$ENV{'PATH_INFO'}/$doc_dir/formhelp.html#AccessURL" target="nvohelp">Access URL:</a>
    </td>
    <td>
        <input type="text" name=ifaceURL size=60  value="$ifaceURL">
        <br />
	<font color="green"><em>
	The URL to access this Registry.
        </em></font> 
    </td>
  </tr>

EOT
    }        
    print <<EOT;
</table>

        <input type=hidden name=inputfname value="$resource_file">
		<input type=hidden name=authorityId value="$authorityId">
		<input type=hidden name=resourceKey value="$resourceKey">
		<input type=hidden name=create_date value="$create_date">
		<input type=hidden name=numParam value="$numParam">
		<input type=hidden name=defset value="$defSet">
		<input type=hidden name=uname value="$username">
		<input type=hidden name=path value="$path">
		<input type=submit name=ftype value="$submitButton">
		<input type=reset value="Reset Form">
		<input type=submit name=ftype value="Cancel">
EOT
    print
		$form->end_form, &formatFooter(),
		$form->end_html;

}

###############################################################################
#
# login_form: Login to form
#
###############################################################################

sub login_form {

    my $message = shift;

    print
        $form->header,
        $form->start_html(-title=>'NVO Registration Portal', -bgcolor=>'white',
                          -head=>CGI::Link({-rel=>'stylesheet', 
                                            -type=>'text/css',
                                            -href=>'/usvo_template.css'})),
        "\n",
	&formatBanner(),
        $form->h1('Welcome to the NVO Registration Portal at NCSA'),
        "\n", $form->start_form;

#    print <<EOF;
#<font color="red"><em>
#Note: If you have previously (before Dec. 15, 2003) registered resources at this site, you should use
#the form at <a
#href="http://rai.ncsa.uiuc.edu/cgi-bin/Resource_Form_update.cgi">this</a> link in order to update your records.  Once you have updated
#your records, you can use the present page.  Click <a
#href="http://rai.ncsa.uiuc.edu/formupdate.html">here</a> 
#for more information about this change.
#</em></font>

    print <<EOF;
        
<table border="1" width="100%" cellpadding="4">
<tr>
  <td width="50%" valign="top">
<h3>Is this your first time registering?</h3>

If so, the first thing you must do is register your organization, providing 
us with some general information in our registration form.  You will then be
prompted to make up a login name and password that will allow you to return
to this site to register other related resources.  <p>

And don't worry about long the registration form looks like.  You will 
discover that registering additional resources is easier since you can 
<em>inherit</em> values from previously registered resources.  <p>

<input type=checkbox name="tryout">Click this checkbox to try out the form without publishing the data.<P>

To get started by registering your organization, click: <br />
<center>
<input type=submit name=ftype value="Create new site">
</center>
    </td>
    <td valign="top">
<h3>Do you already have a login?</h3>

EOF
    print "<b><font color='#ff0000'>$message</font></b><p>\n" 
	if ($message ne '');
    print <<EOF;   
Please provide username and password to access your registered resources. <p>
<a href="$ENV{'PATH_INFO'}/loginhelp.html#Username">Username:</a>   
    <input type=text name=uname size=20>
<p>
<a href="$ENV{'PATH_INFO'}/loginhelp.html#Password">Password:</a>
    <input type=password name=passwd size=20 maxlength=25>
<p>
<input type=checkbox name="tryout">Click this checkbox to log in to your non-publishing scratch area.<P>
<center>
<input type=submit name=ftype value="   Login   ">
<input type=reset value="Reset Form">
</center>
<p> 
<font color="green"><em>
If you would like to look at a sample repository, log in as "sample",
password "sample".
</em></font> 
<p>
    </td>
  </tr>
</table>
EOF
    print $form->end_form, &formatFooter(),
    $form->end_html;

}
###############################################################################
#
# confirm_delete: Confirm the deletion of a resource
#
###############################################################################

sub confirm_delete {

    my $delfile = shift;
    read_resource_file($delfile);
    $status = "uncommitted add" if $delfile =~/uc_add/;
    $status = "uncommitted edit" if $delfile =~/uc_edit/;
    $status = "uncommitted delete" if $delfile =~/uc_del/;
    $status = "uncommitted undelete" if $delfile =~/uc_undel/;

    print
        $form->header,
        $form->start_html(-title=>'Confirm Deletion', -bgcolor=>'white',
                          -head=>CGI::Link({-rel=>'stylesheet', 
                                            -type=>'text/css',
                                            -href=>'/usvo_template.css'})),
        "\n",
	&formatBanner(),
        $form->h1('Confirm Deletion'),
        "\n",
        "You have asked that the resource:",
        "\n",
        "<table noborder width=100% cellpadding=1 cellspacing=8>",
		"<tr align=left>",
		"<th><font size=4><u>Resource Name</u></font></td>",
		"<th><font size=4><u>Status</u></font></td>",
		"<th><font size=4><u>Resource Type</u></font></td>",
		"<tr>",
        "<td>$title</td>",
        "<td>$status</td>",
        "<td>$defSet</td>",
		"</table>",
		"<p>",

        $form->p,
        "be removed from the Repository.",
        "\n",
        $form->p,
        "Note that files with uncommitted status will revert to their\n",
        "previous committed status.\n",
        $form->p,
        "Uncommitted add files will be removed.\n",
        $form->p,
        "If this is correct, click the \"Confirm Delete\" button.\n",
"\n",
        $form->p,
        "Click the \"Cancel\" button to cancel the delete.\n",
        $form->start_form;
    print <<EOF;
    <p>
        <input type=hidden name=path value=\"$path\">
        <input type=hidden name=uname value=\"$username\">
        <input type=hidden name=inputfname value=\"$delfile\">
    <table noborder width=100% cellpadding=1 cellspacing=8>
        <tr align=left>
        <td colspan=2><input type=submit name=ftype value=\"Confirm Delete\">
        <input type=submit name=ftype value=\"Cancel\"></td>
        </tr>
    </table>
    <p>
EOF
    print $form->end_form, &formatFooter(),
    $form->end_html;

}
###############################################################################
#
# ident_form: Site-specfic parameter form
#
###############################################################################

sub ident_form {

    my $message = shift;
    print
        $form->header,
        $form->start_html(-title=>'Site Identification', -bgcolor=>'white',
                          -head=>CGI::Link({-rel=>'stylesheet', 
                                            -type=>'text/css',
                                            -href=>'/usvo_template.css'})),
	&formatBanner(),
        $form->h1('Site Identification Form');
    print <<EOT;
        <p>
        Please provide a username and password for accessing your site.
        <p>
        Click on "Create Site" to save the configuration and begin adding
resources to the repository
        <p>
        Click on "Reset" to reset the values on this page.
EOT

# If personal sets reimplemented, take this comment block and replace "here"
# docuemnt above.

#    print <<EOT;
#        <p>
#        This page creates site-specific parameters for your Repository,
#        <p>
#        which will be placed in the working directory.
#        <p>
#        Click on "Create Site" to save the configuration and begin adding
#resources to the repository
#        <p>
#        Click on "Reset" to reset the values on this page.
#EOT
    print
        $form->start_form;

    print <<EOT;
        <a href="$ENV{'PATH_INFO'}/confighelp.html#Username">
           Login name for Site:</a>
        <input type=\"text\" name=uname size=60 >
        <p>
        <p>
        <a href="$ENV{'PATH_INFO'}/confighelp.html#Password">
           Password for Login for Site:</a>
        <input type=password name=passwd size=20 maxlength=25>
        <p>
        <p>
        <a href="$ENV{'PATH_INFO'}/confighelp.html#Password">
           Verify Password for Site:</a>
        <input type=password name=passwd2 size=20 maxlength=25>
        <p>
        <p>
        <table noborder width=100% cellpadding=1 cellspacing=8>
        <tr align=center>
        <td bgcolor=bbbbbb colspan=2>
		<input type=hidden name=path value="$path">
        <input type=submit name=ftype value="Create Site">
        <input type=reset value=Reset Form></td>
        </tr>
        </table>
        <p>
        <p>
        <b><font color="#ff0000">$message</font></b>
EOT

# Removed; May be reimplemented later RLW 4/17/03.
#           List site-specific Data Sets (one per line):</a>
#        <textarea name=listSets cols=20 rows=6></textarea>
#        <p>
#        <p>
    print
        $form->end_form, &formatFooter(),
        $form->end_html;

}

###############################################################################
#
# verify_login  Check the username/password
#
###############################################################################

sub verify_login {

    $username = $form->param('uname');
    my $passwd = $form->param('passwd');

    my $infile = $username."config.xml";

    # Override the path if the username is sample
    if ($username eq "sample") {
        $path = $alt_workspace;
    }

    if (!-e "$path/$infile") {
        return 1;
    } else {
        read_config($username);
        if ($password ne $passwd) {
           return 2;
        }
    }
return 0;

}

###############################################################################
#
# verify_site: Verify validity of site-specific information
#
###############################################################################

sub verify_site {
    
    my ($username, $password, $password2) = @_;
    opendir (DIR, $path);
    my @dirnames = grep(/^$username+config.xml$/, readdir (DIR));
    close (DIR);
    
    my $num_config = scalar (@dirnames);
    if ($num_config > 0) {
        return 1;
    }
    if ($password ne $password2) {
           return 2;
    }
    if ($password eq "") {
        return 3;
    }
return 0;

}

###############################################################################
#
# read_config: Read the configuration file for the repository
#              Currently only the set information for the repository are kept
#              there.
#
###############################################################################

sub read_config {

    my $uname = shift;
    my $file = $uname."config.xml";
    my $in = new Pure::X2D ("$path/$file");
    @repSets = $in->param ('ListSets/set/setName', "");
    $password = $in->param ('Password', "");


} 

###############################################################################
#
# count_config_files: Test to see if there are config files in the work directory.
#
###############################################################################

sub count_config_files {

    opendir (DIR, $path);
    my @dirnames = grep(/config.xml/, readdir (DIR));
    close (DIR);
    
    return @dirnames;

}

###############################################################################
#
# read_resources: Read the resource name and type fronm all of the resources in
# the repository
#
###############################################################################

sub read_resources {

    my @database = ();
    my $status = "";
# Removed; May be implemeted later RLW 4/17/03.
#    # Add User Sets
#    foreach my $set (our @repSets) {
#        push (@sets, $set);
#    }

    foreach my $set (@defSets) {
        my $rdir = "$path/$set";
        opendir (DIR, $rdir);
        my @dirnames = grep {!/^\./} 
                       grep {/^$username\d+.xml$|^$username\d+.xml.uc/} 
                       readdir (DIR);

        close (DIR);

        @dirnames = sort { $a cmp $b } @dirnames;

 
        my $adir;
        foreach $adir (@dirnames) {
            my $file = "$set/$adir";
            next if -e "$path/$file.uc_edit";
            next if -e "$path/$file.uc_del";
            next if -e "$path/$file.uc_undel";
            my $in = new Pure::X2D ("$path/$file");
            my $rname = $in->param ("Resource/title", "");
            my $sname = $in->param ("Resource/shortName", "");
            my $auth = $in->param ("Resource/identifier/authorityID", "");
            my $key = $in->param ("Resource/identifier/resourceKey", "");
            my $description = $in->param ("Resource/content/description", "");
            my $refURL = $in->param ("Resource/content/referenceURL", "");

            if ($file =~/xml.uc_edit/){
                $status = "uncommitted edit";
            } elsif ($file =~/xml.uc_del/) {
                $status = "uncommitted delete";
            } elsif ($file =~/xml.uc_undel/) {
                $status = "uncommitted undelete";
            } elsif ($file =~/xml.uc_add/) {
                $status = "uncommitted add";
            } else {
                $status = $in->param ("reserved/status", "");
            }
            
            push (@database, [ $file, $status, $rname, $sname, $auth, 
                $key, $set, $description, $refURL] );
        }
    }

    return @database;

}

###############################################################################
#
# read_resource_file: Read parameters from a resource file
#
###############################################################################

sub read_resource_file {
    
    $resource_file = shift;
    if ($resource_file eq "") {
            $resource_file = $form->param('resource');
    }
    my $in = new Pure::X2D ("$path/$resource_file");
    ($service, my $trash) = split ("\/", $resource_file);
    $defSet = $service;
    if ($service eq "BrowserBasedService" || $service eq "GLUService") {
        $service = "SkyService";
    }
    if ($service eq "ConeSearch" || $service eq "SIAService" 
         || $service eq "SSAService") {
        $service = "TabularSkyService";
    }

#    our @userSets = $in->param ('Resource/ListSets/set/setName', "");
#    our $num_sets = scalar(@userSets);
# Get Service attributes
    getAttr($resource_file, "Resource");
    $create_date = fromXML(@attribute{"created"});
    $update_date = fromXML(@attribute{"updated"});

#set skyservice, tabskyservice flags where applicable
    if ($action ne "add") {
        if ($service eq "CGIService" || $service eq "WebService" || 
            $service eq "Service") 
        {
            if (@attribute{"type"} eq "vs:SkyService") {
                $skyservice = 1;
            } 
            if (@attribute{"type"} eq "vs:TabularSkyService") {
                $tabskyservice = 1;
            } 
        }
    }
# Get Title, Identifier and any Attributes of those
    $title = fromXML($in->param ("Resource/title", ""));
    $authorityId = fromXML($in->param ("Resource/identifier/authorityID", ""));
    $resourceKey = fromXML($in->param ("Resource/identifier/resourceKey", ""));
    $sname = fromXML($in->param ("Resource/shortName", ""));

#Curation parameters
    getAttr($resource_file, "publisher");
    $publisherId = fromXML(@attribute{"ivo-id"});
    ($pub_auth, $pub_key) = convertID($publisherId);
    $pub_title = fromXML($in->param ("Resource/curation/publisher", ""));
    $creator = fromXML($in->param ("Resource/curation/creator/name", ""));
    $logo = fromXML($in->param ("Resource/curation/creator/logo", ""));
    $contributor = fromXML($in->param ("Resource/curation/contributor", ""));
    $date = convertDate(fromXML($in->param ("Resource/curation/date", "")));
    $version = fromXML($in->param ("Resource/curation/version", ""));
    $contact_name = fromXML($in->param ("Resource/curation/contact/name", ""));
    $contact_address = fromXML($in->param ("Resource/curation/contact/address", ""));
    $contact_email = fromXML($in->param ("Resource/curation/contact/email", ""));
    $contact_telephone = fromXML($in->param ("Resource/curation/contact/telephone", ""));

#Content parameters

    my @sub = $in->param ("Resource/content/subject", "");
    @subject = ();
    foreach my $s (@sub) {
        my $string = fromXML($s);
        push (@subject, $string);
    }

    $description = fromXML($in->param ("Resource/content/description", ""));
    $source = fromXML($in->param ("Resource/content/source", ""));
    $refURL = fromXML($in->param ("Resource/content/referenceURL", ""));

    @ctype = ();
    my @cty= $in->param ("Resource/content/type", "");
    foreach my $c (@cty) {
        my $string = fromXML($c);
        push (@ctype, $string);
    }
    @content = ();
    my @con= $in->param ("Resource/content/contentLevel", "");
    foreach my $c (@con) {
        my $string = fromXML($c);
        push (@content, $string);
    }
    @relation = fromXML($in->param ("Resource/content/relationship/relationshipType", ""));
    getAttr($resource_file, "relatedResource");
    $relation_id = fromXML(@attribute{"ivo-id"});
    ($rel_auth, $rel_key) = convertID($relation_id);
    $rel_title = fromXML($in->param ("Resource/content/relationship/relatedResource", ""));

    $facility= fromXML($in->param ("Resource/facility", ""));
    getAttr($resource_file, "facility");
    $facilityId = fromXML(@attribute{"ivo-id"});
    ($fac_auth, $fac_key) = convertID($facilityId);
    $instrument= fromXML($in->param ("Resource/instrument", ""));
    $instrumentId = fromXML(@attribute{"ivo-id"});
    ($ins_auth, $ins_key) = convertID($facilityId);
    my @frmt= $in->param ("Resource/format", "");
    @format = ();
    foreach my $f (@frmt) {
        my $string = fromXML($f);
        push (@format, $string);
    }
    @rights = ();
    my @rites= $in->param ("Resource/rights", "");
    foreach my $r (@rites) {
        my $string = fromXML($r);
        push (@rights, $string);
    }

#Coverage parameters
    
    # Check to see which Spatial Coverage we might have, set $region
    # accordingly
    getAttr($resource_file, "region");
    $region = fromXML(@attribute{"type"});
    
    if ($region eq "vs:CircleRegion") {
       $regionFrame = 
           $in->param ("Resource/coverage/spatial/region/coordFrame", "");
       $regionFrame = "ICRS" if ($regionFrame eq '');
       $region_long = 
           $in->param ("Resource/coverage/spatial/region/center/long","");
       $region_lat = 
           $in->param ("Resource/coverage/spatial/region/center/lat","");
       $region_rad = 
           $in->param ("Resource/coverage/spatial/region/radius", "");
    } elsif ($region eq "vs:CoordRange") {
        $rangeFrame = 
            $in->param ("Resource/coverage/spatial/region/coordFrame", "");
        $rangeFrame = "ICRS" if ($rangeFrame eq '');
        $range_long_min = 
            $in->param ("Resource/coverage/spatial/region/long/min", "");
        $range_long_max = 
            $in->param ("Resource/coverage/spatial/region/long/max", "");
        $range_lat_min = 
            $in->param ("Resource/coverage/spatial/region/lat/min", "");
        $range_lat_max = 
            $in->param ("Resource/coverage/spatial/region/lat/max", "");
    }
    $spatial_res = 
        $in->param ("Resource/coverage/spatial/resolution", "");
    $region_regard = 
        $in->param ("Resource/coverage/spatial/regionOfRegard", "");

    @waveband= $in->param ("Resource/coverage/spectral/waveband", "");
    $wave_min = $in->param ("Resource/coverage/spectral/range/min", "");
    $wave_max = $in->param ("Resource/coverage/spectral/range/max", "");
    $spec_res = $in->param ("Resource/coverage/spectral/resolution", "");

    $temporal_start= convertDate(fromXML($in->param ("Resource/coverage/temporal/startTime", "")));
    $temporal_end= convertDate(fromXML($in->param ("Resource/coverage/temporal/endTime", "")));
    $temporal_res= fromXML($in->param ("Resource/coverage/temporal/resolution", ""));

#Service-specific
    if ($defSet eq "BrowserBasedService" || $defSet eq "GLUService"  
            || $defSet eq "Service" || $defSet eq "WebService" 
            || $defSet eq "SSAService") {
       $ifaceURL= fromXML($in->param ("Resource/interface/accessURL", ""));
       @iface_params= fromXML($in->param ("Resource/param", ""));
    }
    if ($defSet eq "CGIService") {
       $ifaceURL= fromXML($in->param ("Resource/interface/accessURL", ""));
       $outputMime = fromXML($in->param ("Resource/interface/resultType", ""));
       getAttr($resource_file, "dataType");
       @param_name = $in->param ("Resource/interface/param/name", "");
       @param_description = $in->param ("Resource/interface/param/description", "");
       @param_dataType = $in->param ("Resource/interface/param/dataType", "");
       @param_unit = $in->param ("Resource/interface/param/unit", "");
       @param_ucd = $in->param ("Resource/interface/param/ucd", "");
       @param_req = getAttrArray($resource_file, "param");
       @param_arraysize = getAttrArray($resource_file, "dataType");
    }
    if ($defSet eq "SIAService") {
       $ifaceURL= fromXML($in->param ("Resource/interface/accessURL", ""));
       $maxRec= fromXML($in->param ("Resource/capability/maxRecords", "")); 
       $imServType= 
          fromXML($in->param ("Resource/capability/imageServiceType", "")); 
       $maxRegSize_long = 
           fromXML($in->param ("Resource/capability/maxQueryRegionSize/long", "")); 
       $maxRegSize_lat = 
           fromXML($in->param ("Resource/capability/maxQueryRegionSize/lat", "")); 
       $maxImExt_long = 
           fromXML($in->param ("Resource/capability/maxImageExtent/long", "")); 
       $maxImExt_lat = 
           fromXML($in->param ("Resource/capability/maxImageExtent/lat", "")); 
       $maxImSize_long = 
           fromXML($in->param ("Resource/capability/maxImageSize/long", "")); 
       $maxImSize_lat = 
           fromXML($in->param ("Resource/capability/maxImageSize/lat", "")); 

       $maxFSize= fromXML($in->param ("Resource/capability/maxFileSize", "")); 
    }
    if ($defSet eq "ConeSearch") {
       $ifaceURL= fromXML($in->param ("Resource/interface/accessURL", ""));
       $maxRec= fromXML($in->param ("Resource/capability/maxRecords", "")); 
       $verbosity= fromXML($in->param ("Resource/capability/verbosity", "")); 
       $maxSR = fromXML($in->param ("Resource/capability/maxSR", "")); 
    }
# Reserved Tags
    $datestamp = fromXML($in->param("reserved/datestamp", ""));
    $status = fromXML($in->param("reserved/status", ""));
}


###############################################################################
#
# save_resource_variables: Save variables from form for possible reload of 
#  form.
#
###############################################################################

sub save_resource_variables {
    
    $manageBy = $form->param('manageBy');
    $title = $form->param('title');
    $sname = $form->param('sname');
    $authorityId = $form->param('authorityId');
    $resourceKey = $form->param('resourceKey');
    $publisherId = $form->param('publisherId'); 
    if ($publisherId ne "") {
       (my $pub_sname, $pub_auth, $pub_key) = &getInfo($publisherId);
    } 
    $pub_title = $form->param('pub_title'); 
    $creator = $form->param('creator');
    $logo = $form->param('logo');
    $contributor = $form->param('contributor');
    $description = $form->param('description');
    my $sub = $form->param('subject');
    @subject = split(/\n/, $sub);
    $date = $form->param('date');
    $version = $form->param('version');
    $refURL = $form->param('refURL');
    $servURL = $form->param('servURL');
    $source = $form->param('source');
    $contact_name = $form->param('contact');
    $contact_address = $form->param('contactAddress');
    $contact_email = $form->param('contactEmail');
    $contact_telephone = $form->param('contactTelephone');
    $region = $form->param('region');
    $regionFrame = $form->param('regionFrame');
    $region_lat = $form->param('region_lat');
    $region_long = $form->param('region_long');
    $region_rad = $form->param('region_rad');
    $rangeFrame = $form->param('rangeFrame');
    $range_lat_min = $form->param('range_lat_min');
    $range_lat_max = $form->param('range_lat_max');
    $range_long_min = $form->param('range_long_min');
    $range_long_max = $form->param('range_long_max');
    $spatial_res = $form->param('spatial_res');
    $region_regard = $form->param('region_regard');
    @waveband = $form->param('waveband');
    $wave_min = $form->param ('wave_min'); 
    $wave_min_units = $form->param ('wave_min_units'); 
    $wave_min = convert_wave($wave_min, $wave_min_units);
    $wave_max = $form->param ('wave_max'); 
    $wave_max_units = $form->param ('wave_max_units'); 
    $wave_max = convert_wave($wave_max, $wave_max_units);
    $spec_res = $form->param ('spec_res'); 
    $temporal_start = $form->param('temporal_start');
    $temporal_end = $form->param('temporal_end');
    $temporal_res = $form->param('temporal_res');
    @ctype = $form->param('type');
    @content = $form->param('content');
    $facility = $form->param('facility');
    $facilityId = $form->param('facilityId');
    if ($facilityId ne "") {
       (my $fac_sname, $fac_auth, $fac_key) = &getInfo($facilityId);
    } 
    $instrument = $form->param('instrument');
    $instrumentId = $form->param('instrumentId');
    if ($instrumentId ne "") {
       (my $ins_sname, $ins_auth, $ins_key) = &getInfo($instrumentId);
    } 
    $relationType = $form->param('relation');
    $relation_id = $form->param('relatedResourceId');
    if ($relation_id ne "") {
       (my $rel_sname, $rel_auth, $rel_key) = &getInfo($relation_id);
    } 
    $rel_title = $form->param('rel_title');
    my $format = $form->param('format');
    @format = split(/\n/, $format);
    @rights = $form->param('rights');
    $imServType = $form->param ('imServType'); 
    $maxRegSize_long= $form->param ('maxRegSize_long'); 
    $maxRegSize_lat= $form->param ('maxRegSize_lat'); 
    $maxImExt_long= $form->param ('maxImExt_long'); 
    $maxImExt_lat= $form->param ('maxImExt_lat'); 
    $maxImSize_long= $form->param ('maxImSize_long'); 
    $maxImSize_lat= $form->param ('maxImSize_lat'); 
    $maxFSize= $form->param ('maxFSize'); 
    $verbosity= $form->param ('verbosity'); 
    $maxSR = $form->param ('maxSR'); 
    $maxRec= $form->param ('maxRec'); 
    $ifaceURL = $form->param('ifaceURL');
    $output = $form->param('output');
    $outputMime = $form->param('outputMime');
    for (my $i = 0; $i < $numParam; $i++) {
        push (@param_name, $form->param ("param_name$i")); 
        push (@param_description, $form->param ("param_desc$i")); 
        push (@param_dataType, $form->param ("param_dataType$i")); 
        push (@param_unit, $form->param ("param_unit$i")); 
        push (@param_ucd, $form->param ("param_ucd$i")); 
        if ($form->param ("param_req$i")) {
            push (@param_req, 'YES');
        } else {
            push (@param_req, 'NO');
        }
    }
    

}

###############################################################################
#
# commit_resources: Published altered resource files
#
###############################################################################

sub commit_resources {

    foreach my $set (@defSets) {
        my $rdir = "$path/$set";
        opendir (DIR, $rdir);
        my @dirnames = grep(/^$username\d+.xml.uc/, readdir (DIR));
        close (DIR);

        @dirnames = sort { $a cmp $b } @dirnames;

 
        my $adir;
        foreach $adir (@dirnames) {
            my $infile = "$path/$set/$adir";
            my ($left, $right) = split(/\./, $adir);
            my $outfile= "$path/$set/$left.xml";
            update_date($infile, $outfile);
        }
    }

}

###############################################################################
#
# update_date: Move an input record file to an output record, updating the 
#   the updated date stamp in the process.
#
###############################################################################

sub update_date {
    my($infile, $outfile) = @_;
    my $basefile=$infile; $basefile =~ s!^.*/!!;
    my $exterrmsg = "Resource_Form.cgi: failed to update date on $basefile";

    my $tag = time;
    my $tempfile = $outfile; $tempfile =~ s/\.xml$/-$tag/;
    if ($tempfile =~ m!/!) {
        $tempfile =~ s!/([^/]*)$!temp-$1!;
    }
    else {
        $tempfile = "temp-$tempfile";
    }

    if (! open(IN, "$infile")) {
        print STDERR "Resource_Form.cgi: failed to open $infile for ",
                     "date update: ", $!, "\n";
        die $exterrmsg;
    }
    if (! open(OUT, ">$tempfile")) {
        print STDERR "Resource_Form.cgi: failed to open $tempfile for ",
                     "date update: ", $!, "\n";
        die $exterrmsg;
    }

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=gmtime (time);
    my $timezone = 'Z';
    my $datestring = sprintf ("%04d-%02d-%02dT%02d:%02d:%02d%s",
                    $year+1900, $mon+1, $mday, $hour, $min, $sec,
                    $timezone);

    while (<IN>) {
        if (/\<Resource [^>]*updated=/) {
            s/updated=\"[^\"]*\"/updated=\"$datestring\"/;
        }
        elsif (/\<datestamp\>/) {
            s/\<datestamp\>[^\<]*\</\<datestamp\>$datestring\</;
        }
        print OUT $_;
    }
    close(OUT);
    close(IN);

    if (! rename($tempfile, $outfile)) {
        # this message will go into error_log
        print STDERR "Resource_Form.cgi: failed to rename $tempfile ",
                     "to $outfile.\n";
        # user will see this message:
        die $exterrmsg;
    }
    if (! unlink($infile)) {
        # this message will go into error_log
        print STDERR "Resource_Form.cgi: failed to remove $infile during ",
                     "update_date to $outfile.\n";
        # user will see this message:
        die $exterrmsg;
    }

    return;
}

###############################################################################
#
# getAttr: Grab the attribute part of a tag
#
###############################################################################

sub getAttr {

    my ($infile, $tag) = @_;

    %attribute = ();
    my ($subdir, $fname) = split (/\//, $infile);
    chdir "$path/$subdir";

    my $parser = new Pure::EZXML;

    my $document = $parser->parsefile ($fname);
    my $xml_line = $document->getElementsByTagName ($tag);
    my $attr = $xml_line->toString;
    my $attr_left = index($attr, "$tag");
    my $attr_right = index($attr, ">");
    my $test = substr($attr, $attr_right - 1, 1);
    if ($test eq '/') { # tag is closed  
       $attr = substr($attr, $attr_left, $attr_right - 2);
    } else {
       $attr = substr($attr, $attr_left, $attr_right - 1);
    }
    $attr =~ s/$tag //;
    my @pairs = split(/\"\ /, $attr);
    foreach my $pair (@pairs) {
       my ($name, $value) = split(/=/, $pair);
       $value =~ s/\"//g;
       $attribute{$name} = $value;
    }
    chdir "$FindBin::Bin";

    return;

}

###############################################################################
#
# getAttrArray: Grab one attribute of multiple instances of a tag
#
###############################################################################

sub getAttrArray {

    my ($infile, $tag) = @_;

    my @attribute; 
    my ($subdir, $fname) = split (/\//, $infile);
    chdir "$path/$subdir";

    my $parser = new Pure::EZXML;

    my $document = $parser->parsefile ($fname);
    my @xml_line = $document->getElementsByTagName ($tag);
    foreach my $xml_line (@xml_line) {
       my $attr = $xml_line->toString;
       my $attr_left = index($attr, "$tag");
       my $attr_right = index($attr, ">");
       my $test = substr($attr, $attr_right - 1, 1);
       if ($test eq '/') { # tag is closed  
          $attr = substr($attr, $attr_left, $attr_right - 2);
       } else {
          $attr = substr($attr, $attr_left, $attr_right - 1);
       }
       $attr =~ s/$tag //;
       my @pairs = split(/\"\ /, $attr);
       foreach my $pair (@pairs) {
           my ($name, $value) = split(/=/, $pair);
           $value =~ s/\"//g;
           push(@attribute, $value);
       }
    }
    chdir "$FindBin::Bin";

    return @attribute;

}
###############################################################################
#
# save_config: The the repository configuration paramaters in a file
#
###############################################################################

sub save_config {

    # Print the configuration parameters to our repository configuration file:
    # rfconfig.xml in this directory!
    my ($username, $password) = @_;
    my $sets = $form->param ('listSets');
    my @sets = split("\n", $sets);
    my $wrk = $path;
    my $cfile = $username."config.xml";
    open (CONF, ">$wrk/$cfile");
    print CONF "# Configuration file for the Repository\n";
    print CONF "<Config>\n";
    print CONF "   <Username>$username</Username>\n";
    print CONF "   <Password>$password</Password>\n";

    # This section for user-defined sets; may be implemented later; for now will
    # always fail, since no values are given. RLW 4/17/03.
    my $num_sets = scalar (@sets);
    if ($num_sets > 0) {
        print CONF "   <ListSets>\n";
        my $ctr = 1;
        foreach my $setname (@sets) {
            if ($ctr < $num_sets) {
               chop $setname;
               $ctr++;
            }
            `mkdir "$path/$setname"` unless -e "$path/$setname";
            `chmod 0755 "$path/$setname"` unless -e "$path/$setname";
            print CONF "      <set>\n";
            print CONF "         <setName>$setname</setName>\n";
            print CONF "      </set>\n";
        }
        print CONF "   </ListSets>\n";
    }
    print CONF "</Config>\n";
    close (CONF);

} 

###############################################################################
#
# get_status:  extract the contents of the <status> element
#
###############################################################################

sub get_status {
    my($file) = shift;

    open(IN, "$file") || return '';
    my($status) = grep(/\<status\>/, <IN>);
    close(IN);

    if ($status) {
        chomp $status;
        $status =~ s!^.*\<status\>\s*!!;
        $status =~ s!\s*\</status\>.*$!!;
    }

    return $status;
}

###############################################################################
#
# save_resource: Save the resource information to a file, either a new resource 
# or a prexisting one
#
###############################################################################

sub save_resource {
    use vars qw($manageBy $title  $sname  $creator  $logo  @subject  $description  $contributor  $date  $version  $authorityId $resourceKey  $refURL  $servURL  $source $contact_name  $contact_email  $contact_address $contact_telephone $region  $spatial_res @waveband  $temporal_start  $temporal_end  $temporal_res @ctype @content  $facility  $instrument  @format  @rights  $ifaceURL $output $outputMime
$imServType  $maxRegSize_long  $maxRegSize_lat  $maxImExt_long  $maxImExt_lat  $maxImSize_long  $maxImSize_lat  $maxFSize $verbosity $maxSR $maxRec $create_date $update_date);

    read_config();
    my ($action, $infile, $typeFlag) = @_;
    my $resourceType = "";
    my $outfile = "";
    my $tempfile = "$path/tempfile".(rand 65534).".xml";
    my $set = "";
    my @sets = ();

    my $status = '';
    if ($action eq "delete") {
        $status = "deleted";
    }
    elsif ($action eq "undelete") {
        $status = "published"
    }
    else {
        $status = get_status("$path/$infile");
    }
    $status = "published" if ($status =~ /^\s*$/);

    $path = $form->param('path');
    if ($typeFlag eq "Organisation") {
       $resourceType = "Organisation";
       $outfile = "$path/temp.xml";
    } elsif ($typeFlag eq "Authority") {
       $resourceType = "Authority";
       $outfile = "$path/temp2.xml";
    } elsif ($typeFlag eq "Registry") {
       $resourceType = "Registry";
       $outfile = "$path/temp3.xml";
    } else {
       if ( $action eq "add" || $action eq "new"){
          $resourceType = $form->param('defset');
       } else {
          $resourceType = $defSet;
       }

       if ($infile =~ /.xml.uc_/) {
          # An added resource remains so until committed.
          if ($infile =~ /.xml.uc_add/) {
            $action = "add";        
          }
          unlink "$path/$infile";
          my ($left, $right) = split(/\./, $infile);
          $infile = $left.".xml";
       }
       $outfile = "$path/$infile.uc_add" if $action eq "add";
       $outfile = "$path/$infile.uc_add" if $action eq "new";
       $outfile = "$path/$infile.uc_edit" if $action eq "edit";
       $outfile = "$path/$infile.uc_del" if $action eq "delete";
       $outfile = "$path/$infile.uc_undel" if $action eq "undelete";
       

    }
    # If delete or undelete, already read this, so don't overwrite
    # Need to convert back to XML before saving for delete or undelete
    if($action ne "delete" && $action ne "undelete") {
      $authorityId = toXML($form->param('authorityId'));
      $resourceKey = toXML($form->param('resourceKey'));
      $title = toXML($form->param('title'));
      $sname = toXML($form->param('sname'));
      @ctype = $form->param('type');
      my $sub = toXML($form->param('subject'));
      @subject = split(/\n/, $sub);
      map {s/[\r\n]+//} @subject;
      map {s/^\s+//} @subject;
      map {s/\s+$//} @subject;
      @content = $form->param('content');
      $facilityId = toXML($form->param('facilityId'));
      $facility = toXML($form->param('facility'));
      $instrument = toXML($form->param('instrument'));
      $instrumentId = toXML($form->param('instrumentId'));
      $relationType = $form->param('relation');
      $relation_id = $form->param('relatedResourceId');
      $rel_title = $form->param('rel_title');
      $log_authorityId = $form->param('log_authorityId'); 
      $log_resourceKey = $form->param('log_resourceKey'); 
      $description = toXML($form->param('description'));
      $refURL = toXML($form->param('refURL'));
      $source = toXML($form->param('source'));
      $publisherId = $form->param('publisherId'); 
      $pub_title = $form->param('pub_title'); 
      $pub_description = $form->param('pub_description'); 
      $pub_refURL = $form->param('pub_refURL'); 
      $creator = toXML($form->param('creator'));
      $logo = toXML($form->param('logo'));
      $contributor = toXML($form->param('contributor'));
      $date = toXML($form->param('date'));
      $version = toXML($form->param('version'));
      $servURL = toXML($form->param('servURL'));
      $contact_name = toXML($form->param('contact'));
      $contact_address = toXML($form->param('contactAddress'));
      $contact_email = toXML($form->param('contactEmail'));
      $contact_telephone = toXML($form->param('contactTelephone'));
      my $format = toXML($form->param('format'));
      @format = split(/\n/, $format);
      map {s/[\r\n]+//} @format;
      map {s/^\s+//} @format;
      map {s/\s+$//} @format;
      @rights = $form->param('rights');
      $regionFrame = toXML($form->param('regionFrame'));
      $region_lat = toXML($form->param('region_lat'));
      $region_long = toXML($form->param('region_long'));
      $region_rad = toXML($form->param('region_rad'));
      $rangeFrame = toXML($form->param('rangeFrame'));
      $range_lat_min = toXML($form->param('range_lat_min'));
      $range_lat_max = toXML($form->param('range_lat_max'));
      $range_long_min = toXML($form->param('range_long_min'));
      $range_long_max = toXML($form->param('range_long_max'));
      $region = toXML($form->param('region'));
      $spatial_res = toXML($form->param('spatial_res'));
      $region_regard = toXML($form->param('region_regard'));
      @waveband = $form->param('waveband');
      $wave_min = $form->param ('wave_min'); 
      $wave_min_units = $form->param ('wave_min_units'); 
      $wave_min = convert_wave($wave_min, $wave_min_units);
      $wave_min = toXML($wave_min);
      $wave_max = $form->param ('wave_max'); 
      $wave_max_units = $form->param ('wave_max_units'); 
      $wave_max = convert_wave($wave_max, $wave_max_units);
      $wave_max = toXML($wave_max);
      $spec_res = toXML($form->param ('spec_res')); 
      $temporal_start = toXML($form->param('temporal_start'));
      $temporal_end = toXML($form->param('temporal_end'));
      $temporal_res = toXML($form->param('temporal_res'));
      $imServType= toXML($form->param ('imServType')); 
      $maxRegSize_long= toXML($form->param ('maxRegSize_long')); 
      $maxRegSize_lat= toXML($form->param ('maxRegSize_lat')); 
      $maxImExt_long= toXML($form->param ('maxImExt_long')); 
      $maxImExt_lat= toXML($form->param ('maxImExt_lat')); 
      $maxImSize_long= toXML($form->param ('maxImSize_long')); 
      $maxImSize_lat= toXML($form->param ('maxImSize_lat')); 
      $maxFSize= toXML($form->param ('maxFSize')); 
      $verbosity = toXML($form->param ('verbosity')); 
      $maxSR = toXML($form->param ('maxSR')); 
      $maxRec= toXML($form->param ('maxRec')); 
      $ifaceURL = toXML($form->param('ifaceURL'));
      $output = toXML($form->param('output'));
      $outputMime = toXML($form->param('outputMime'));
      for (my $i = 0; $i < $numParam; $i++) {
           push (@param_name, $form->param ("param_name$i")); 
           push (@param_description, $form->param ("param_desc$i")); 
           push (@param_dataType, $form->param ("param_dataType$i")); 
           push (@param_unit, $form->param ("param_unit$i")); 
           push (@param_ucd, $form->param ("param_ucd$i")); 
           if ($form->param ("param_req$i")) {
               push (@param_req, 1);
           } else {
               push (@param_req, 0);
           }
      }
    
      @param_name = toXMLArray(\@param_name);
      @param_description = toXMLArray(\@param_description);
      @param_dataType = toXMLArray(\@param_dataType);
      @param_unit = toXMLArray(\@param_unit);
      @param_ucd = toXMLArray(\@param_ucd);
      @param_req = toXMLArray(\@param_req);
    } else {    # delete or undelete, convert XML strings 
      $authorityId = toXML($authorityId);
      $resourceKey = toXML($resourceKey);
      $title = toXML($title);
      $sname = toXML($sname);
      @ctype = toXMLArray(\@ctype);
      @subject = toXMLArray(\@subject);
      $facilityId = toXML($facilityId);
      $facility = toXML($facility);
      $instrument = toXML($instrument);
      $instrumentId = toXML($instrumentId);
      $relationType = toXML($relationType);
      $relation_id = toXML($relation_id);
      $rel_title = toXML($rel_title);
      $log_authorityId = toXML($log_authorityId); 
      $log_resourceKey = toXML($log_resourceKey); 
      $description = toXML($description);
      $refURL = toXML($refURL);
      $source = toXML($source);
      $publisherId = toXML($publisherId); 
      $pub_title = toXML($pub_title); 
      $pub_description = toXML($pub_description); 
      $pub_refURL = toXML($pub_refURL); 
      $creator = toXML($creator);
      $logo = toXML($logo);
      $contributor = toXML($contributor);
      $date = toXML($date);
      $version = toXML($version);
      $servURL = toXML($servURL);
      $contact_name = toXML($contact_name);
      $contact_address = toXML($contact_address);
      $contact_email = toXML($contact_email);
      $contact_telephone = toXML($contact_telephone);
      $region = toXML($region);
      $spatial_res = toXML($spatial_res);
      @waveband = toXMLArray(\@waveband);
      $wave_min = toXML($wave_min);
      $wave_max = toXML($wave_max);
      $spec_res = toXML($spec_res);
      $temporal_start = toXML($temporal_start);
      $temporal_end = toXML($temporal_end);
      $temporal_res = toXML($temporal_res);
      @content = toXMLArray(\@content);
      @format = toXMLArray(\@format);
      @rights = toXMLArray(\@rights);
      $imServType= toXML($imServType); 
      $maxRegSize_long= toXML($maxRegSize_long); 
      $maxRegSize_lat= toXML($maxRegSize_lat); 
      $maxImExt_long= toXML($maxImExt_long); 
      $maxImExt_lat= toXML($maxImExt_lat); 
      $maxImSize_long= toXML($maxImSize_long); 
      $maxImSize_lat= toXML($maxImSize_lat); 
      $maxFSize= toXML($maxFSize); 
      $verbosity = toXML($verbosity); 
      $maxSR = toXML($maxSR); 
      $maxRec= toXML($maxRec); 
      $ifaceURL = toXML($ifaceURL);
      $outputMime = toXML($outputMime);
      @param_name = toXMLArray(\@param_name);
      @param_description = toXMLArray(\@param_description);
      @param_dataType = toXMLArray(\@param_dataType);
      @param_unit = toXMLArray(\@param_unit);
      @param_ucd = toXMLArray(\@param_ucd);
      @param_req = toXMLArray(\@param_req);
    }
    open (OUT, ">$tempfile")|| die "Cannot open file $tempfile - Protection problem?";
    print OUT "<\?xml version=\"1.0\" encoding=\"UTF-8\"\?>\n";
    print OUT "<ResourceForm\n";
    print OUT "     xmlns=\"http://www.ivoa.net/xml/VOResource/v0.10\"\n";
    print OUT "     xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v0.10\"\n";
    print OUT "     xmlns:vc=\"http://www.ivoa.net/xml/VOCommunity/v0.2\"\n";
    print OUT "     xmlns:vg=\"http://www.ivoa.net/xml/VORegistry/v0.3\"\n";
    print OUT "     xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v0.5\"\n";
    print OUT "     xmlns:cs=\"http://www.ivoa.net/xml/ConeSearch/v0.3\"\n";
    print OUT "     xmlns:sia=\"http://www.ivoa.net/xml/SIA/v0.7\"\n";
    print OUT "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
    print OUT ">\n";

#
# Resource Tag
#
    my $res = "";
    my $schemaLocation = "";
    if ($resourceType eq "SIAService") {
        $res = "sia:SimpleImageAccess";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd http://www.ivoa.net/xml/SIA/v0.7 SIA-v0.7.xsd\"";
    } elsif ($resourceType eq "SSAService") {
        $res = "vs:TabularSkyService";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
    } elsif ($resourceType eq "ConeSearch") {
        $res = "cs:ConeSearch";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd http://www.ivoa.net/xml/ConeSearch/v0.3 ConeSearch-v0.3.xsd\"";
    } elsif ($resourceType eq "BrowserBasedService" 
            || $resourceType eq "GLUService") {
        $res = "vs:SkyService";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
    } elsif ($resourceType eq "CGIService") {
        if ($skyservice && !$tabskyservice) {
            $res = "vs:SkyService";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
        } elsif ($tabskyservice) {
            $res = "vs:TabularSkyService";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
        } else {
            $res = "vr:Service";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd\"";
        } 
    } elsif ($resourceType eq "WebService" || $service eq "Service") {
        if ($skyservice) {
            $res = "vs:SkyService";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
        } elsif ($tabskyservice) {
            $res = "vs:TabularSkyService";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
        } else {
            $res = "vs:Service";
            $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
        } 
    } elsif ($resourceType eq "Authority" 
            || $resourceType eq "Registry") {
        $res = "vg:$resourceType";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VORegistry/v0.3 VORegistry-v0.3.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
    } elsif ($resourceType eq "Organisation") {
        $res = "vr:$resourceType";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd\"";
    } elsif ($resourceType eq "DataCollection") { 
        $res = "vs:$resourceType";
        $schemaLocation = "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd http://www.ivoa.net/xml/VODataService/v0.5 VODataService-v0.5.xsd\"";
    } else {
        $res = $resourceType;
        $schemaLocation = "xsi:schemLocation=\"http://www.ivoa.net/xml/VOResource/v0.10 VOResource-v0.10.xsd\"";
    }
        
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=gmtime (time);
    my $timezone = 'Z';
    my $datestring = sprintf ("%04d-%02d-%02dT%02d:%02d:%02d%s",
                    $year+1900, $mon+1, $mday, $hour, $min, $sec,
                    $timezone);
    if ($action eq "add" || $action eq "new") {
        $create_date = $datestring;
    } elsif ($action eq "edit" || $action eq "editupdate"){
        $create_date = $form->param('create_date');
    }
    if ($status eq "delete") {
    print OUT 
        "   <Resource xsi:type=\"$res\" xmlns=\"$nspace\" created=\"$create_date\" updated=\"$datestring\" status=\"deleted\" $schemaLocation>\n";
    } else { 
    print OUT 
        "   <Resource xsi:type=\"$res\" xmlns=\"$nspace\" created=\"$create_date\" updated=\"$datestring\" $schemaLocation>\n";
   } 

    if ($typeFlag eq "Authority") {
       print OUT "      <title>$title Naming Authority</title>\n" unless $title eq "";
       print OUT "      <shortName>$sname auth</shortName>\n" unless $sname eq "";
    } elsif ($typeFlag eq "Registry") {
       print OUT "      <title>$title Registry</title>\n" unless $title eq "";
       print OUT "      <shortName>$sname reg</shortName>\n" unless $sname eq "";
    } else {
       print OUT "      <title>$title</title>\n" unless $title eq "";
       print OUT "      <shortName>$sname</shortName>\n" unless $sname eq "";
    }
#
# Identifier Tags
#

    if ($authorityId ne "" || $resourceKey ne "") {
       print OUT "         <identifier>\n";
       print OUT "             <authorityID>$authorityId</authorityID>\n" unless $authorityId eq "";
       if ($typeFlag eq "Registry") {
             print OUT "             <resourceKey>registry</resourceKey>\n";
       } elsif ($typeFlag eq "Authority") {
             print OUT "             <resourceKey xsi:nil=\"true\"/>\n";
       } elsif ($resourceKey ne "" && $typeFlag ne "Registry") {
          print OUT "             <resourceKey>$resourceKey</resourceKey>\n";
       } elsif ($resourceKey eq "" && $typeFlag ne "Registry") {
          print OUT "             <resourceKey xsi:nil=\"true\"/>\n";
       }
       print OUT "         </identifier>\n";
    }


#
# Curation Tag
#
    if ($publisherId ne "" || $pub_title ne "" || $pub_description ne "" ||
            $pub_refURL ne "" || $creator ne "" ||$logo ne "" || 
            $contributor ne "" || $date ne "" || $version ne "" ||
            $contact_name ne "" || $contact_address ne "" || 
            $contact_telephone ne "" || $contact_email ne "") {
        print OUT "      <curation>\n";
        #Publisher
        if ($publisherId ne "" || $pub_title ne "") {
            my $pub_Id = "";
            my $pub_Key = "";

            if ($action ne "delete" && $action ne "undelete") {
               my @list = read_resources();
	           foreach my $res (@list) {
                   my $test_string = "$res->[2] (ivo://$res->[4]/$res->[5])";
	               if ($test_string eq "$publisherId"){
                    $pub_Id = $res->[4]; 
                    $pub_Key = $res->[5]; 
                    $pub_title = $res->[2]; 
                    $pub_title =~ s/^\s+//; 
                    $pub_description = $res->[7] unless $pub_description ne ""; 
                    $pub_refURL = $res->[8] unless $pub_refURL ne ""; 
                   }
	            }
            } else {
                $pub_Id = $pub_auth;
                $pub_Key = $pub_key;
            }
        if ($pub_Id ne "") {
            print OUT "         <publisher ivo-id=\"ivo://$pub_Id/$pub_Key\">";
        } else { 
            print OUT "         <publisher>";
        }
        print OUT "$pub_title"    
                        unless $pub_title eq "";
        print OUT "</publisher>\n" 
                unless ($pub_title eq "" && $publisherId eq "");
            
        }
        #Creator    
        if ($creator ne "" || $logo ne "") {
            print OUT "         <creator>\n";
            print OUT "             <name>$creator</name>\n" unless $creator eq "";
            print OUT "             <logo>$logo</logo>\n" unless $logo eq "";
            print OUT "         </creator>\n";
         }
        #Contributor
        if ($contributor ne "") {
            print OUT "         <contributor>$contributor</contributor>\n";
         }
        #Date
        $date = convertDate($date);
        print OUT "         <date>$date</date>\n" unless $date eq "";
        #Version
        print OUT "         <version>$version</version>\n" unless $version eq "";
        #Contact    
        if ($contact_name ne "" || $contact_email ne "" 
            || $contact_address ne "" || $contact_telephone ne "") {
            print OUT "         <contact>\n" unless $contact_name eq "";
            print OUT "             <name>$contact_name</name>\n" unless $contact_name eq "";
            print OUT "             <address>$contact_address</address>\n" unless $contact_address eq "";
            print OUT "             <email>$contact_email</email>\n" unless $contact_email eq "";
            print OUT "             <telephone>$contact_telephone</telephone>\n" unless $contact_telephone eq "";
            print OUT "         </contact>\n" unless $contact_name eq "";
        }
        print OUT "      </curation>\n";

    }
        
#  
# Content Tag
#

    if ($subject[0] ne "" ||
            $description ne "" || $source ne "" || $refURL ||
            $ctype[0] ne "" || $content[0] ne "" || 
            $relationType ne "" ) {

        print OUT "      <content>\n";
        if ($subject[0] ne "") {
           foreach my $s (@subject) {
              print OUT "         <subject>$s</subject>\n" ;
           }
        }

        print OUT "         <description>\n$description\n   </description>\n" 
                        unless $description eq "";
        print OUT "         <source>$source</source>\n" unless $source eq "";
        print OUT "         <referenceURL>$refURL</referenceURL>\n" 
                        unless $refURL eq "";
   
        my $num_type = scalar (@ctype);
        if ($num_type > 0) {
           foreach my $t (@ctype) {
              print OUT "          <type>$t</type>\n";
           }
        }
        my $num_content = scalar (@content);
        if ($num_content > 0 && $content[0] ne "") {
           foreach my $con (@content) {
              if ($con eq "Middle") {
                 $con = "Middle School Education";
              }
              if ($con eq "Secondary") {
                 $con = "Secondary Education";
              }
              if ($con eq "Elementary") {
                 $con = "Elementary Education";
              }
              if ($con eq "Community") {
                 $con = "Community College";
              }
              if ($con eq "Informal") {
                 $con = "Informal Education";
              }
              print OUT "          <contentLevel>$con</contentLevel>\n";
           }
        }
        if ($relationType ne "" && ($relation_id ne "" || $rel_title ne "")) {
           print OUT "          <relationship>\n"; 
           print OUT "          <relationshipType>$relationType</relationshipType>\n"; 

           if ($relation_id ne "" ) {
               my $rel_Id = "";
               my $rel_Key = "";

               my @list = read_resources();
	           foreach my $res (@list) {
                  my $test_string = "$res->[2] (ivo://$res->[4]/$res->[5])";
	              if ($test_string eq "$relation_id"){
                     $rel_Id = $res->[4]; 
                     $rel_Key = $res->[5]; 
                     $rel_title = $res->[2]; 
                     $rel_title =~ s/^\s+//; 
                  }
	           }
               if ($rel_Id ne "") {
                   print OUT "             <relatedResource ivo-id=\"ivo://$rel_Id/$rel_Key\">";
               } else { 
                   print OUT "             <relatedResource>";
               }
            }
            if ($relation_id eq "") {
                   print OUT "             <relatedResource>";
            }

            print OUT "$rel_title"    
                           unless $rel_title eq "";
            print OUT "</relatedResource>\n" 
                            unless ($rel_title eq "");
            print OUT "          </relationship>\n" unless $relationType eq "";
        }
        print OUT "      </content>\n";
    }

 
# Service Specific Output
#
# Different Service Access Outputs
#
    if ($resourceType eq "SIAService" || $resourceType eq "ConeSearch"
            || $resourceType eq "SSAService") {
        if ($ifaceURL ne "") {
            print OUT 
                "      <vr:interface xsi:type=\"vs:ParamHTTP\" qtype=\"GET\">\n";
            print OUT 
              "          <vr:accessURL use=\"base\">$ifaceURL</vr:accessURL>\n";
            print OUT "          <vs:resultType>application/xml+votable</vs:resultType>\n";
            print OUT "      </vr:interface>\n";
        }
    }
    if ($resourceType eq "BrowserBasedService") {
        if ($ifaceURL ne "") {
            print OUT "       <vr:interface xsi:type=\"vr:WebBrowser\">\n";
            print OUT "           <vr:accessURL use=\"dir\">$ifaceURL</vr:accessURL>\n";
            print OUT "       </vr:interface>\n";
        }
    }
    if ($resourceType eq "GLUService") {
        if ($ifaceURL ne "") {
            print OUT "       <vr:interface xsi:type=\"vr:GLUService\">\n";
            print OUT "           <vr:accessURL use=\"dir\">$ifaceURL</vr:accessURL>\n";
            print OUT "       </vr:interface>\n";
        }
    }
    if ($resourceType eq "WebService" || $resourceType eq "Service") {
        if ($ifaceURL ne "") {
                print OUT "       <vr:interface  xmlns=\"http://www.ivoa.net/xml/VODataService/v0.5\" xsi:type=\"vs:WebService\">\n";
            print OUT 
              "          <vr:accessURL use=\"base\">$ifaceURL</vr:accessURL>\n";
            print OUT "      </vr:interface>\n";
        }
    }
    if ($resourceType eq "CGIService") {
        if ($ifaceURL ne "") {
                print OUT "       <vr:interface  xmlns=\"http://www.ivoa.net/xml/VODataService/v0.5\" xsi:type=\"vs:ParamHTTP\" qtype=\"GET\">\n";
                print OUT "           <vr:accessURL use=\"dir\">$ifaceURL</vr:accessURL>\n";
            if ($output eq "VOTable") {
              print OUT "          <vs:resultType>application/xml+votable</vs:resultType>\n";
            } elsif ($output eq "CSVTable") { 
              print OUT "          <vs:resultType>application/csv</vs:resultType>\n";
            } elsif ($output eq "HTMLTable") { 
              print OUT "          <vs:resultType>text/html</vs:resultType>\n";
            } elsif ($output eq "Other") {
              print OUT "          <vs:resultType>$outputMime</vs:resultType>\n";
            }
                if (scalar(@param_name) > 0) {
                    for (my $j = 0; $j < $numParam; $j++) {
                        if ($param_name[$j] ne "") {
                            if ($param_req[$j]) {
                                print OUT "           <param required=\"YES\" >\n";
                            } else {
                                print OUT "           <param required=\"NO\" >\n";
                            }    
                            print OUT "              <name>$param_name[$j]</name>\n" if $param_name[$j];
                            print OUT "              <description>$param_description[$j]</description>\n" if $param_description[$j];
                            if ($param_dataType[$j] eq "string") { 
                               print OUT "              <dataType arraysize=\"*\">char</dataType>\n" 
                            } else {
                                print OUT "              <dataType>$param_dataType[$j]</dataType>\n";
                            }
                            if ($param_unit[$j] eq "") {
                            print OUT "              <unit xsi:nil=\"true\"/>\n";
                            } else {
                               print OUT "              <unit>$param_unit[$j]</unit>\n"; 
                            }
                            if ($param_ucd[$j] eq "") {
                            print OUT "              <ucd xsi:nil=\"true\"/>\n";
                            } else {
                               print OUT "              <ucd>$param_unit[$j]</ucd>\n"; 
                            }
                            print OUT "           </param>\n";
                        }
                    }
                }
                print OUT "       </vr:interface>\n";
        }
    }

#
#   Facility Tag
#
        if ($facility ne "" || $facilityId ne "") {
           my $fac_Id = "";
           my $fac_Key = "";

           my @list = read_resources();
	       foreach my $res (@list) {
              my $test_string = "$res->[2] (ivo://$res->[4]/$res->[5])";
	          if ($test_string eq "$facilityId"){
                 $fac_Id = $res->[4]; 
                 $fac_Key = $res->[5]; 
                 $facility = $res->[2]; 
                 $facility =~ s/^\s+//; 
              }
	       }
           if ($fac_Id ne "") {
              print OUT "      <vs:facility ivo-id=\"ivo://$fac_Id/$fac_Key\">";
           } else { 
              print OUT "      <vs:facility>";
           }
           print OUT "$facility"    
                       unless $facility eq "";
           print OUT "</vs:facility>\n" 
                       unless ($facility eq "");
        }
#
#   Instrument Tag
#
        if ($instrument ne "" || $instrumentId ne "") {
           my $ins_Id = "";
           my $ins_Key = "";

           my @list = read_resources();
	       foreach my $res (@list) {
               my $test_string = "$res->[2] (ivo://$res->[4]/$res->[5])";
	           if ($test_string eq "$instrumentId"){
                   $ins_Id = $res->[4]; 
                   $ins_Key = $res->[5]; 
                   $instrument = $res->[2]; 
                   $instrument =~ s/^\s+//; 
               }
	       }
           if ($ins_Id ne "") {
              print OUT "      <vs:instrument ivo-id=\"ivo://$ins_Id/$ins_Key\">";
           } else { 
              print OUT "      <vs:instrument>";
           }
           print OUT "$instrument"    
                       unless $instrument eq "";
           print OUT "</vs:instrument>\n" 
                       unless ($instrument eq "");
        }

#  
# Coverage Tag
#

#
# Spatial Coverage
#
    if ($region ne "" || $waveband[0] ne "" || $wave_min ne "" ||
            $wave_max ne ""  || $temporal_start ne "") { 
        print OUT "      <coverage xmlns=\"http://www.ivoa.net/xml/VODataService/v0.5\">\n";
        if ($region ne "") {
           print OUT "         <spatial>\n";
           if ($region eq "AllSky") {
              print OUT "         <region xsi:type=\"vs:AllSky\"/>\n";
           } elsif ($region eq "CircleRegion") {
              print OUT "            <region xsi:type=\"vs:CircleRegion\">\n";
              print OUT "               <coordFrame>$regionFrame</coordFrame>\n";
              print OUT "               <center>\n";
              print OUT "                  <long>$region_long</long>\n";
              print OUT "                  <lat>$region_lat</lat>\n";
              print OUT "               </center>\n";
              print OUT "               <radius>$region_rad</radius>\n";
              print OUT "            </region>\n";
               
           } elsif ($region eq "CoordRange") {
              print OUT "            <region xsi:type=\"vs:CoordRange\">\n";
              print OUT "               <coordFrame>$rangeFrame</coordFrame>\n";
              print OUT "               <long>\n";
              print OUT "                  <min>$range_long_min</min>\n";
              print OUT "                  <max>$range_long_max</max>\n";
              print OUT "               </long>\n";
              print OUT "               <lat>\n";
              print OUT "                  <min>$range_lat_min</min>\n";
              print OUT "                  <max>$range_lat_max</max>\n";
              print OUT "               </lat>\n";
              print OUT "            </region>\n";
           }
           print OUT
                "            <resolution>$spatial_res</resolution>\n" unless $spatial_res eq "";
           print OUT
                "            <regionOfRegard>$region_regard</regionOfRegard>\n"
                    unless $region_regard eq "";
           print OUT "         </spatial>\n";
        }
#
# Spectral Coverage
#
        if ($waveband[0] ne "" || $wave_min ne "" || $wave_max ne ""
                || $spec_res ne "") {
           print OUT "         <spectral>\n";
           foreach my $item (@waveband) {
              print OUT "           <waveband>$item</waveband>\n";
           }
           if ($wave_min ne "" || $wave_max ne "") {
              print OUT "           <range>\n";
              print OUT "               <min>$wave_min</min>\n" 
                            unless $wave_min eq "";
              print OUT "               <max>$wave_max</max>\n" 
                            unless $wave_max eq "";
              print OUT "           </range>\n";
           }
           print OUT "           <resolution>$spec_res</resolution>\n"
                    unless $spec_res eq "";
           print OUT "         </spectral>\n"
        }
 
#
# Temporal Coverage
#
        if ($temporal_start ne "" || $temporal_end ne "" || $temporal_res ne "") { 
            $temporal_start = convertDate($temporal_start)
                                unless $temporal_start eq "";
            $temporal_end = convertDate($temporal_end)
                                unless $temporal_end eq "";
            print OUT "         <temporal>\n"; 
            print OUT "             <startTime>$temporal_start</startTime>\n" 
                                unless $temporal_start eq "";
            print OUT "             <endTime>$temporal_end</endTime>\n" 
                                unless $temporal_end eq "";
            print OUT "             <resolution>$temporal_res</resolution>\n"
                                unless $temporal_res eq "";
            print OUT "         </temporal>\n";
        }
        print OUT "      </coverage>\n";
    }
#
# Capability (SIA and ConeSearch)
#

    if ($resourceType eq "SIAService") {
        print OUT 
         "      <sia:capability xmlns=\"http://www.ivoa.net/xml/SIA/v0.7\">\n";
        print OUT "          <imageServiceType>$imServType</imageServiceType>\n"
                unless $imServType eq "";
        if ($maxRegSize_long ne "" || $maxRegSize_lat ne "") {
           print OUT "          <maxQueryRegionSize>\n";
           print OUT "              <long>$maxRegSize_long</long>\n" 
                unless $maxRegSize_long eq "";
           print OUT "              <lat>$maxRegSize_lat</lat>\n" 
                unless $maxRegSize_lat eq "";
           print OUT "          </maxQueryRegionSize>\n"; 
        }

        if ($maxImExt_long ne "" || $maxImExt_lat ne "") {
           print OUT "          <maxImageExtent>\n";
           print OUT "              <long>$maxImExt_long</long>\n" 
                unless $maxImExt_long eq "";
           print OUT "              <lat>$maxImExt_lat</lat>\n"
                 unless $maxImExt_lat eq "";
           print OUT "          </maxImageExtent>\n";
        }

        if ($maxImSize_long ne "" || $maxImSize_lat ne "") {
           print OUT "          <maxImageSize>\n";
           print OUT "              <long>$maxImSize_long</long>\n" 
                unless $maxImSize_long eq "";
           print OUT "              <lat>$maxImSize_lat</lat>\n" 
                unless $maxImSize_lat eq "";
           print OUT "          </maxImageSize>\n";
        }

        print OUT "          <maxFileSize>$maxFSize</maxFileSize>\n" 
                unless $maxFSize eq "";
        print OUT "          <maxRecords>$maxRec</maxRecords>\n"   
                unless $maxRec eq "";
        print OUT "      </sia:capability>\n";
    }

    if ($resourceType eq "ConeSearch") {
        print OUT 
         "      <cs:capability>\n";
        print OUT "          <cs:maxSR>$maxSR</cs:maxSR>\n"
                unless $maxSR eq "";
        print OUT "          <cs:maxRecords>$maxRec</cs:maxRecords>\n"   
                unless $maxRec eq "";
        if ($verbosity) {
           print OUT "          <cs:verbosity>true</cs:verbosity>\n";   
        } else {
           print OUT "          <cs:verbosity>false</cs:verbosity>\n";   
        }
        print OUT "      </cs:capability>\n";
    }

#
#   Format Tag
#
    my $num_rights = scalar (@rights);
    my $num_format = scalar(@format);

    if ($num_format  > 0) { 
      foreach my $fff (@format) {
         print OUT "     <vs:format>$fff</vs:format>\n";
      }
    }
#
#   Rights Tag
#
        if ($num_rights > 0) {
            foreach my $rrr (@rights) {
                print OUT "     <vs:rights>$rrr</vs:rights>\n" 
            }
        }
# 
# Registry Specific Tags
#
    if ($typeFlag eq "Registry") {
        print OUT "       <interface xsi:type=\"vs:WebService\">\n";
        print OUT "           <accessURL use=\"dir\">$ifaceURL</accessURL>\n";
        print OUT "       </interface>\n";
    
    # Managed Authority
        print OUT "       <vg:managedAuthority>$authorityId</vg:managedAuthority>\n";
    }
    print OUT "   </Resource>\n";

    # Add reserved keywords datestamp and status
    print OUT "     <reserved>\n";
    print OUT "        <datestamp>$datestring</datestamp>\n";
    print OUT "        <status>$status</status>\n";
    print OUT "     </reserved>\n";

    print OUT "</ResourceForm>\n";
    close (OUT);
    if (! rename($tempfile, $outfile)) {
        # this message will go into error_log
        print STDERR "Resource_Form.cgi: failed to move $resourceType record ",
                     "($tempfile) into archive as $outfile.\n";
        # user will see this message:
        die "Resource_Form.cgi: failed to add $resourceType record to archive.";
    }
    
    # If a new Authority, need to add a line to the Registry resource file.
    if ($resourceType eq "Authority" && $action eq "add") {
        my $reg_file = find_registry_file();
        add_authority_to_registry_file($authorityId, $reg_file);
    }
}

###############################################################################
#
# find_org: Find the first organisation, which will be the registering
# organisation. Returns either the title of the organisation, or the filename.
#
###############################################################################

sub find_org {

    my $returnType = shift;
    @list = read_resources();
    my @orgs = ();
    my $org_title = "";
    my $org_file = "";

    # Find the Organisations
    foreach my $res (@list) {
       if ($res->[6] eq "Organisation") {
          push (@orgs, $res);
       }
    }

    # Now find the earliest org (by the filename number), which will be what 
    # we want.
    my $org_num = 1000; 
    foreach my $org (@orgs) {
       my $file =  $org->[0];
       my ($root, $suffix) = split (/\./, $file);
       my $num = $root;
       my $test = "Organisation/$username";
       $num =~ s/$test//g;
       if ($num < $org_num){
          $org_num = $num; 
          $org_title = $org->[2];
          $org_file = $org->[0];
          my $test2 = "Organisation/";
          $org_file =~ s/$test2//g;
       }
    }
    if ($returnType eq "title") {
        return $org_title;
    }
    
    if ($returnType eq "file") {
        return $org_file;
    }
}
###############################################################################
#
# find_filename: Find the next number in the series to name the new output XML
# file. 
#
###############################################################################

sub find_filename {

     my @allfiles; 
     foreach my $set (@defSets) {
        my $rdir = "$path/$set";
        opendir (DIR, $rdir);
        my @dirnames = grep(/^$username\d+.xml$|^$username\d+.xml.uc/, readdir (DIR));
        close (DIR);
   
        my $adir;

        foreach $adir (@dirnames) {
            my ($root, $suffix) = split (/\./, $adir);
            my $num = $root;
            $num =~ s/$username//g;
            push (@allfiles, $num);
         }
     }
     my $bnum = 0;
     foreach my $zz (@allfiles) {
        if ($zz > $bnum) {
           $bnum = $zz;
        }
     }
     $bnum++;
     if ($bnum < 10) {
        $fname = $username."0000".$bnum.".xml";
     } 
     if ($bnum >= 10 && $bnum < 100) {
        $fname = $username."000".$bnum.".xml";
     } 
     if ($bnum >= 100 && $bnum < 1000) {
        $fname = $username."00".$bnum.".xml";
     } 
     if ($bnum >= 1000 && $bnum < 10000) {
        $fname = $username."0".$bnum.".xml";
     } 
     if ($bnum >= 10000 ) {
        $fname = $username.$bnum.".xml";
     } 
     return $fname;
}
###############################################################################
#
# check_fields: Check the fields input and make sure our required fields
# are filled in and in the case of Identifier, unique to the repository
# (all users).
#
#   10/26/2004 - Add sanity check for integer, real fields
###############################################################################

sub check_fields {

  my $action = shift;
  my $message = "";
  my $defset = $form->param('defset');
  my $title = $form->param('title');
  my $authorityId = $form->param('authorityId');
  my $resourceKey = $form->param('resourceKey');
  my $sname = $form->param('sname');
  my @type = $form->param('type');
  my $description = $form->param('description');
  my $refURL = $form->param('refURL');
  my $pub_title = $form->param('pub_title');
  my $publisherId = $form->param('publisherId');
  my $contact = $form->param('contact');
  my $contactEmail = $form->param('contactEmail');
  my $maxRec= $form->param ('maxRec');                       
  my $maxFSize= $form->param ('maxFSize'); 
  my $maxSR  = $form->param('maxSR');
  my $spatial_res = $form->param('spatial_res');
  my $temporal_res = $form->param('temporal_res');
  my $spec_res = $form->param ('spec_res'); 
  my $region_regard  = $form->param('region_regard');
  my $wave_min = $form->param ('wave_min'); 
  my $wave_max = $form->param ('wave_max'); 
  my $region_rad = $form->param('region_rad');
  my $maxRegSize_long= $form->param ('maxRegSize_long'); 
  my $maxRegSize_lat= $form->param ('maxRegSize_lat'); 
  my $maxImExt_long= $form->param ('maxImExt_long'); 
  my $maxImExt_lat= $form->param ('maxImExt_lat'); 
  my $maxImSize_long= $form->param ('maxImSize_long'); 
  my $maxImSize_lat= $form->param ('maxImSize_lat'); 
  my $maxRegSize_long= $form->param ('maxRegSize_long'); 
  my $maxRegSize_lat= $form->param ('maxRegSize_lat'); 
  my $region_lat = $form->param('region_lat');
  my $region_long = $form->param('region_long');
  my $range_lat_min = $form->param('range_lat_min');
  my $range_lat_max = $form->param('range_lat_max');
  my $range_long_min = $form->param('range_long_min');
  my $range_long_max = $form->param('range_long_max');
  my $date = $form->param('date');
     $date = convertDate($date);
  my $temporal_start = $form->param('temporal_start');
     $temporal_start = convertDate($temporal_start);
  my $temporal_end = $form->param('temporal_end');
     $temporal_end = convertDate($temporal_end);
  my $logo = $form->param('logo');
  my $refURL = $form->param('refURL');
  my $servURL = $form->param('servURL');
  my $ifaceURL = $form->param('ifaceURL');
  my $output = $form->param('output');
  my $outputMime = $form->param('outputMime');
  my $pub_refURL = $form->param('pub_refURL'); 

# Required fields - check that they are filled and valid
  if ($title eq "") {$message .= "Title field is empty!<br>"};
  if (length($sname) > 16) {$message .= "Short Name too long. Max length is 16
chars!<br>"};
  if ($type[0] eq "") {$message .= "Type field is empty!<br>"};
  if ($description eq "") {$message .= "Description field is empty!<br>"};
  if ($refURL eq "") {$message .= "Reference URL field is empty!<br>"};
  if ($pub_title eq "" && $publisherId eq "") {
     $message .= "Publisher information is not given!<br>";
  }
  if ($contact eq "") {$message .= "Contact Name field is empty!<br>"};
#  if ($contactEmail eq "") {$message .= "Contact Email field is empty!<br>"};
  # If a CGI service, output mimetype is required
  if ($defset eq "CGIService" && $output eq "") {$message .= "Output type not selected!<br>"};
  if ($action eq "add" ) {
     if ($authorityId eq "") {$message .= "Authority ID field is empty!<br>"};
     if ($authorityId !~ /^[\w\d][\w\d\-_\.!~\*'\(\)]{2}/ ) {   # '
        $message .= "Authority ID is not a valid string!<br>";
     }
     if ($defset ne "Authority") {
       if ($resourceKey !~ /^[\w\d\-_\.!~\*'\(\)]+(\/[\w\d\-_\.!~\*'\(\)])*/) { 
       $message .= "Resource Key is not a valid string!<br>";
       }
     }
     my $unique = isUniqueID($authorityId, $resourceKey);
     if ($unique) {
       $message .= "Resource Key $resourceKey is not unique for Authority $authorityId in this Repository; Please give another value.<br>";
     }
  }
# Optional fields, if filled still check data type - look at integer, float,
# double, and anyURI.
  # Integer Types
  if ($maxRec ne "" && $maxRec !~ /^[+-]?\d+$/ ) { 
       $message .= "Maximum Number of Records is not a valid integer!<br>";
  }
  if ($maxRec ne "" && $maxRec > 2147483647) { 
       $message .= "Maximum Number of Records is not a valid integer value (> 2147483647)!<br>";
  }
  if ($maxFSize ne "" && $maxFSize !~ /^[+-]?\d+$/ ) { 
       $message .= "Maximum File Size is not a valid integer!<br>";
  }
  if ($maxFSize ne "" && $maxFSize > 2147483647) { 
       $message .= "Maximum File Size is not a valid integer value (> 2147483647)!<br>";
  }
  # Float Types
  if ($maxSR ne ""  && $maxSR !~ /^-?\d+\.?\d*$/ && $maxSR !~ /^-?\d+\.?\d+e-?\d+/ && $maxSR !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Search Radius is not a valid float!<br>";
  } 
  if ($spatial_res ne ""  && $spatial_res !~ /^-?\d+\.?\d*$/ && $spatial_res !~ /^-?\d+\.?\d+e-?\d+/ && $spatial_res !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Spatial Resolution is not a valid float!<br>";
  } 
  if ($temporal_res ne ""  && $temporal_res !~ /^-?\d+\.?\d*$/ && $temporal_res !~ /^-?\d+\.?\d+e-?\d+/ && $temporal_res !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Temporal Resolution is not a valid float!<br>";
  } 
  if ($spec_res ne ""  && $spec_res !~ /^-?\d+\.?\d*$/ && $spec_res !~ /^-?\d+\.?\d+e-?\d+/ && $spec_res !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Spectral Resolution is not a valid float!<br>";
  } 
  if ($region_regard ne ""  && $region_regard !~ /^-?\d+\.?\d*$/ && $region_regard !~ /^-?\d+\.?\d+e-?\d+/ && $region_regard !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Region of Regard is not a valid float!<br>";
  } 
  if ($wave_min ne ""  && $wave_min !~ /^-?\d+\.?\d*$/ && $wave_min !~ /^-?\d+\.?\d+e-?\d+/ && $wave_min !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Minimum Wavelength is not a valid float!<br>";
  } 
  if ($wave_max ne ""  && $wave_max !~ /^-?\d+\.?\d*$/ && $wave_max !~ /^-?\d+\.?\d+e-?\d+/ && $wave_max !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Wavelength is not a valid float!<br>";
  } 
  if ($region_rad ne ""  && $region_rad !~ /^-?\d+\.?\d*$/ && $region_rad !~ /^-?\d+\.?\d+e-?\d+/ && $region_rad !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Region Radius is not a valid float!<br>";
  } 
  if ($maxRegSize_long ne ""  && $maxRegSize_long !~ /^-?\d+\.?\d*$/ && $maxRegSize_long !~ /^-?\d+\.?\d+e-?\d+/ && $maxRegSize_long !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Region Size long is not a valid float!<br>";
  } 
  if ($maxRegSize_lat ne ""  && $maxRegSize_lat !~ /^-?\d+\.?\d*$/ && $maxRegSize_lat !~ /^-?\d+\.?\d+e-?\d+/ && $maxRegSize_lat !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Region Size lat is not a valid float!<br>";
  } 
  if ($maxImExt_long ne ""  && $maxImExt_long !~ /^-?\d+\.?\d*$/ && $maxImExt_long !~ /^-?\d+\.?\d+e-?\d+/ && $maxImExt_long !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Image Extent Size long is not a valid float!<br>";
  } 
  if ($maxImExt_lat ne ""  && $maxImExt_lat !~ /^-?\d+\.?\d*$/ && $maxImExt_lat !~ /^-?\d+\.?\d+e-?\d+/ && $maxImExt_lat !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Image Extent Size lat is not a valid float!<br>";
  } 
  if ($maxImSize_long ne ""  && $maxImSize_long !~ /^-?\d+\.?\d*$/ && $maxImSize_long !~ /^-?\d+\.?\d+e-?\d+/ && $maxImSize_long !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Image Size long is not a valid float!<br>";
  } 
  if ($maxImSize_lat ne ""  && $maxImSize_lat !~ /^-?\d+\.?\d*$/ && $maxImSize_lat !~ /^-?\d+\.?\d+e-?\d+/ && $maxImSize_lat !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Maximum Image Size lat is not a valid float!<br>";
  } 
  # Double Types - check that it is at least real
  if ($region_long ne ""  && $region_long !~ /^-?\d+\.?\d*$/ && $region_long !~ /^-?\d+\.?\d+e-?\d+/ && $region_long !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Region long is not a valid double!<br>";
  } 
  if ($region_lat ne ""  && $region_lat !~ /^-?\d+\.?\d*$/ && $region_lat !~ /^-?\d+\.?\d+e-?\d+/ && $region_lat !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Region lat is not a valid double!<br>";
  } 
  if ($range_long_max ne ""  && $range_long_max !~ /^-?\d+\.?\d*$/ && $range_long_max !~ /^-?\d+\.?\d+e-?\d+/ && $range_long_max !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Range max long is not a valid double!<br>";
  } 
  if ($range_lat_max ne ""  && $range_lat_max !~ /^-?\d+\.?\d*$/ && $range_lat_max !~ /^-?\d+\.?\d+e-?\d+/ && $range_lat_max !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Range max lat is not a valid double!<br>";
  } 
  if ($range_long_min ne ""  && $range_long_min !~ /^-?\d+\.?\d*$/ && $range_long_min !~ /^-?\d+\.?\d+e-?\d+/ && $range_long_min !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Range min long is not a valid double!<br>";
  } 
  if ($range_lat_min ne ""  && $range_lat_min !~ /^-?\d+\.?\d*$/ && $range_lat_min !~ /^-?\d+\.?\d+e-?\d+/ && $range_lat_min !~ /^-?\d+\.?\d+E-?\d+/) {
       $message .= "Range min lat is not a valid double!<br>";
  } 
  # Date Types
  if ($date ne ""  && $date !~ /^\d\d\d\d-\d\d-\d\d/) {
       $message .= "Date is not a valid date (YYYY-MM-DD)!<br>";
  } 
  if ($temporal_start ne ""  && $temporal_start !~ /^\d\d\d\d-\d\d-\d\d/) {
       $message .= "Temporal coverage Start Time is not a valid date (YYYY-MM-DD)!<br>";
  } 
  if ($temporal_end ne ""  && $temporal_end !~ /^\d\d\d\d-\d\d-\d\d/) {
       $message .= "Temporal coverage End Time is not a valid date (YYYY-MM-DD)!<br>";
  } 
  # anyURI types 
  if ($logo ne "" && &validateUrl($logo)) {
       $message .= "Logo URL is not a valid URI! <br>";
  }
  if ($refURL ne "" && &validateUrl($refURL)) {
       $message .= "Reference URL is not a valid URI! <br>";
  }        
  if ($ifaceURL ne "" && &validateUrl($ifaceURL)) {
       $message .= "Interface URL is not a valid URI! <br>";
  }        
  if ($servURL ne "" && &validateUrl($servURL)) {
       $message .= "Service URL is not a valid URI! <br>";
  }        
  if ($message eq "") {$message = "Verified"};

  return $message;
}

###############################################################################
#
# Validate a URL
# See http://www.ipdg3.com/forums/showthread.php?s=4ba63be2b1feb79775cdeba1e85d9f4a&p=1266#post1266 
#
###############################################################################

sub validateUrl
{
  my($strUrl) = shift;
  return
  $strUrl =~ m!(http:|ftp:)//([A-z\d]+)\:([A-z\d]+)\@([A-z\d\-\.]+\.)+[A-z]!i ||
  $strUrl =~ m!^(http:|ftp:)//([A-z\d\-\.]+\.)+[A-z]!i ||
  $strUrl=~ m!^(http:|ftp:)//(\d){1,3}\.(\d){1,3}\.(\d){1,3}\.(\d){1,3}!i ? 0 : 1;
}

###############################################################################
#
# convertDate: Take a date-type string and convert it to YYYY-MM-DD format.
# Expecting something like YYYY, YYYY-MM, or YYYY-MM-DD.
#
###############################################################################

sub convertDate {

    use vars qw($string);
    $string = shift;

# Return if blank
    if ($string eq "") {return $string;}

#   Check the number of "-"s in the string
    my $numdash = ($string =~ tr/-//);
# Modify accordingly
    if ($numdash == 0) { # Year only
        $string = $string ."-01-01";
    } elsif ($numdash == 1) { # Year and month
        $string = $string ."-01";
    } else {
        return $string;
    }
    return $string;
}

###############################################################################
#
# convertID: Take a IVO Identifier and split it up into the AuthorityID and the
# Resource Key.
# IVO Identifier of form ivo://authorityID/resourceKey
#
###############################################################################

sub convertID {

    use vars qw($string);
    $string = shift;

# Return if blank
    if ($string eq "") {return $string;}

    my $string = substr $string, 6;
    my @parts = split('/', $string);
    my $id = splice(@parts, 0, 1);
    my $key = join ('/', @parts);
    my @ret;
    push(@ret, $id);
    push(@ret, $key);
    return @ret;
}

###############################################################################
#
# getInfo: Get the shortname and IVO Identifier from a pulldown menu list item
# IVO Identifier of form ivo://authorityID/resourceKey
#
###############################################################################

sub getInfo {

    use vars qw($string);
    $string = shift;

# Return if blank
    if ($string eq "") {return $string;}

    (my $sname, my $id) = split('\(', $string);
       chop $id;
       my $ivoId = $id;
       (my $auth, my $key) = &convertID($ivoId);
    my @ret;
    push(@ret, $sname);
    push(@ret, $auth);
    push(@ret, $key);
    return @ret;
}
###############################################################################
#
# isUniqueID: Check all of the XML files looking for the identifier keywords,
# and compare with the value input.  If it matches, return 1, if not, return
# 0.
#
###############################################################################

sub isUniqueID {

     my $idcheck = shift;
     my $keycheck = shift;
     my @allfiles; 
     foreach my $set (@defSets) {
        my $rdir = "$path/$set";
        opendir (DIR, $rdir);
        my @dirnames = grep(/.xml/, readdir (DIR));
        close (DIR);
   
        my $adir;

        foreach $adir (@dirnames) {
            my $in = new Pure::X2D ("$rdir/$adir");
            my $ident = $in->param ("Resource/identifier/authorityID", "");
            my $key = $in->param ("Resource/identifier/resourceKey", "");
            if ($ident eq $idcheck && $key eq $keycheck) {
                return 1;
            }
        }
     }
    return 0;
}

###############################################################################
#
# toXML: Take a string and substitute special characters to XML to escaped
# values
#
###############################################################################

sub toXML {

    my $string = shift;

#   Convert < and > first, so & in their definitions is also converted!
    $string =~ s/</&lt;/g;
    $string =~ s/>/&gt;/g;
    $string =~ s/&/&amp;/g;
    return $string;
}

###############################################################################
#
# toXMLArray: Take a array and substitute escaped XML values for regular 
# characters for each member
#
###############################################################################

sub toXMLArray {

    my $array = shift;
    my @out;

    foreach my $item (@$array) {
#   Convert < and > first, so & in their definitions is also converted!
        $item =~ s/</&lt;/g;
        $item =~ s/>/&gt;/g;
        $item =~ s/&/&amp;/g;
        push(@out, $item);
    }
    return @out;
}

###############################################################################
#
# fromXML: Take a string and take out escaped characters and convert to 
# normal string
#
###############################################################################

sub fromXML {

    my $string = shift;

#   Convert & first, so & in < and > definitions is also converted!
    $string =~ s/&amp;/&/g;
    $string =~ s/&lt;/</g;
    $string =~ s/&gt;/>/g;
    return $string;
}

###############################################################################
#
# fromXMLArray: Take a array and substitute regular character for escpaed 
#  XML values for each member
#
###############################################################################

sub fromXMLArray {

    my $array = shift;
    my @out;

    foreach my $item (@$array) {
#   Convert < and > first, so & in their definitions is also converted!
        $item =~ s/&lt;/</g;
        $item =~ s/&gt;/>/g;
        $item =~ s/&amp;/&/g;
        push(@out, $item);
    }
    return @out;
}

###############################################################################
#
# xmlheader: Output the XML Header for the Static Repository
#
###############################################################################

sub xmlheader
{

      print STDOUT "Content-type: text/plain\n";
      print "\n";
      print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
      print "\n";
      print "<Repository xmlns=\"http://www.openarchives.org/OAI/2.0/static-repository\""; 
      print "\n";
      print "          xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\""; 
      print "\n";
      print "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""; 
      print "\n";
      print "          xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/static-repository"; 
      print "\n";
      print "                      http://www.openarchives.org/OAI/2.0/static-repository.xsd\">";
      print "\n";

}

###############################################################################
#
# xmlfooter: Output the XML Footer for the Static Repository
#
###############################################################################


sub xmlfooter
{

      print "</Repository>"; 
      print "\n";

}

###############################################################################
#
# identify: Prints out the Identify portion of the Static Repository
#
###############################################################################

sub identify {

      my $username = shift;
      my $infile = &find_org("file");
      my $in = new Pure::X2D ("$path/Organisation/$infile");
      my $repName = $in->param ('Organisation/Title', "");
      my $baseURL = $in->param ('Organisation/Summary/ReferenceURL',"");
      my $email = $in->param ('Organisation/Curation/Contact/Email',"");
      my $datestamp = $in->param ('Organisation/Curation/Date',"");
      print <<EOT;
    <Identify>
        <oai:repositoryName>$repName</oai:repositoryName>
        <oai:baseURL>$baseURL</oai:baseURL> 
        <oai:protocolVersion>2.0</oai:protocolVersion>
        <oai:adminEmail>$email</oai:adminEmail>
        <oai:earliestDatestamp>$datestamp</oai:earliestDatestamp>
        <oai:deletedRecord>no</oai:deletedRecord>
        <oai:granularity>YYYY-MM-DD</oai:granularity>
    </Identify>
EOT

}

###############################################################################
#
# listMetadataFormats: Hardwired ListMetadataFormats output for Static
# Repository
#
###############################################################################

sub listMetadataFormats{

      print "    <ListMetadataFormats>\n";
      print "        <oai:metadataFormat>\n";
      print "           <oai:metadataPrefix>oai_dc</oai:metadataPrefix>\n";
      print "           <oai:schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</oai:schema>\n";
      print "           <oai:metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/\n";
      print "                      </oai:metadataNamespace>\n";
      print "        </oai:metadataFormat>\n";
      print "        <oai:metadataFormat>\n";
      print "           <oai:metadataPrefix>ivo_vor</oai:metadataPrefix>\n";
      print "           <oai:schema>http://rai.ncsa.uiuc.edu/schemas/VOResource.xsd</oai:schema>\n";
      print "           <oai:metadataNamespace>http://rai.ncsa.uiuc.edu/</oai:metadataNamespace>\n";
      print "        </oai:metadataFormat>\n";
      print "    </ListMetadataFormats>\n";

}

###############################################################################
#
# listRecords: Read all of the resource files and print them as part of the
# Static Repository output
#
###############################################################################

sub listRecords {

      use vars qw($title $creator $logo @subject $description $contributor $date $version $authorityId $resourceKey $refURL $servURL $contact_name $contact_email $contact_address $contact_telephone $num_types $spatial @waveband $temporal_start $temporal_end $temporal_res @ctype @content $facility $instrument @format @rights $datestamp $status);
      
      my @list = read_resources();
      my $rfile = &find_org("file");
      my $in = new Pure::X2D ("$path/Organisation/$rfile");
      my $authId = $in->param ('Resource/identifier/authorityID', "");
      my $keyId = $in->param ('Resource/identifier/resourceKey', "");
      my $repId = "ivo://$authId/$keyId";

# OAI-DC output
      print "    <ListRecords metadataPrefix=\"oai_dc\">\n";
     foreach my $res (@list) {
         my $temp = $res->[0];
         $temp =~ s/\//\:/g;
         my $id = $repId.":".$temp;
         read_resource_file($res->[0]);
         next if $status eq "deleted";
         print "        <oai:record>\n";
         print "            <oai:header>\n";
         print "                <oai:identifier>$id</oai:identifier>\n";
         print "                <oai:datestamp>$datestamp</oai:datestamp>\n";
         print "            </oai:header>\n";
         print "            <oai:metadata>\n";
         print "                <oai_dc:dc \n";
         print "                     xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"\n"; 
         print "                     xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"; 
         print "                     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"; 
         print "                     xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ \n";
         print "                            http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n";
         print "                <dc:title>$title</dc:title>\n" unless $title eq
"";
         print "                <dc:creator>$creator</dc:creator>\n" unless
$creator eq "";
         if ($subject[0] ne "") {
            foreach my $ss (@subject){
                 $ss = toXML($ss);
                 print "                <dc:subject>$ss</dc:subject>\n";
            }
         }
         $description = toXML($description);
         $contributor = toXML($contributor);
         $date = toXML($date);
         $authorityId = toXML($authorityId);
         $resourceKey = toXML($resourceKey);
         my $identifier = "";
         if ($resourceKey eq "") {
            $identifier = "ivo://$authorityId";
         } else {
            $identifier = "ivo://$authorityId/$resourceKey";
         }   
         $spatial = toXML($spatial);
         print "                <dc:description>$description</dc:description>\n" unless $description eq "";
         print "                <dc:contributor>$contributor</dc:contributor>\n" unless $contributor eq "";
         print "                <dc:date>$date</dc:date>\n" unless $date eq "";
         print "                <dc:identifier>$identifier</dc:identifier>\n" unless $identifier eq "";
         print "                <dc:coverage>Spatial: $spatial</dc:coverage>\n" unless ($spatial eq "");
         if ($waveband[0] ne "") {
            foreach my $spec (@waveband){
               print "                <dc:coverage>Spectral: $spec</dc:coverage>\n" unless $spec eq "";
            }
         }
         if ($temporal_start ne "" || $temporal_end ne "" || $temporal_res ne "" ) { 
            my $temp_string = "<dc:coverage> Temporal: ";
            if ($temporal_start ne "") {
               $temp_string.$temporal_start;
            }
            if ($temporal_end ne "") {
               $temp_string." - $temporal_end";
            }
            if ($temporal_res ne "") {
               $temp_string." Resolution: $temporal_res";
            }
            $temp_string." </dc:coverage>\n";
         }
         my $num_content = scalar(@content);
         if ($num_content > 0) {
             print OUT "      <dc:contentlevel>\n";
             foreach my $con (@content) {
                if ($con eq "Middle") {
                   $con = "Middle School Education";
                }
                if ($con eq "Secondary") {
                   $con = "Secondary Education";
                }
                if ($con eq "Elementary") {
                   $con = "Elementary Education";
                }
                if ($con eq "Community") {
                   $con = "Community College";
                }
                if ($con eq "Informal") {
                   $con = "Informal Education";
                }
                print OUT "          <item>$con</item>\n";
             }
             print OUT "      </dc:contentlevel>\n";
         }
         $facility = toXML($facility);
         print "                <dc:facility>$facility</dc:facility>\n" unless
$facility eq "";
         my $num_format = scalar(@format);
         if ($num_format > 0 && $format[0] ne "") {
            foreach my $format (@format){
                 $format = toXML($format);
                 print "                <dc:format>$format</dc:format>\n" unless
$format eq "";
            }
         }
         my $num_rights = scalar(@rights);
         if ($num_rights > 0 && $rights[0] ne "") {
            foreach my $right (@rights){
               my $rights = toXML($right);
               print "                <dc:rights>$rights</dc:rights>\n" unless $rights eq "";
            }
         }
         print "                </oai_dc:dc> \n";
         print "            </oai:metadata>\n";
         print "        </oai:record>\n";
    }
    print "    </ListRecords>\n";

#VOR format output
    print "    <ListRecords metadataPrefix=\"ivo_vor\">\n";
     foreach my $res (@list) {
         my $temp = $res->[0];
         $temp =~ s/\//\:/g;
         my $id = $repId.":".$temp;
        #Header
         read_resource_file($res->[0]);
         next if $status eq "deleted";
         print "        <oai:record>\n";
         print "            <oai:header>\n";
         print "                <oai:identifier>$id</oai:identifier>\n";
         print "                <oai:datestamp>$datestamp</oai:datestamp>\n";
         print "            </oai:header>\n";
         print "            <oai:metadata>\n";
         
      #Files are almost in VOR format, so just print 'em, except for xml
      #definition and timestamp and status tags and top-level tags!
         my $ctr = 1;
         open (INPUT, "$path/$res->[0]")         || die "can't open $res->[0]: $!";

# Header

          print <<EOT;
                <Resource
                      xmlns="http://www.ivoa.net/xml/VOResource/v0.10"
                      xmlns:vr="http://www.ivoa.net/xml/VOResource/v0.10"
                      xmlns:vc="http://www.ivoa.net/xml/VOCommunity/v0.2"           
                      xmlns:vg="http://www.ivoa.net/xml/VORegistry/v0.3"
                      xmlns:vs="http://www.ivoa.net/xml/VODataService/v0.5"
                      xmlns:vt="http://www.ivoa.net/xml/VOTable/v0.1"
                      xmlns:cs="http://www.ivoa.net/xml/ConeSearch/v0.3"
                      xmlns:sia="http://www.ivoa.net/xml/SIA/v0.7"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://www.ivoa.net/xml/VOResource/v0.10
                                          VOResource-v0.10.xsd
                                          http://www.ivoa.net/xml/VOCommunity/v0.2
                                          VOCommunity-v0.2.xsd
                                          http://www.ivoa.net/xml/VORegistry/v0.3
                                          VORegistry-v0.3.xsd
                                          http://www.ivoa.net/xml/ConeSearch/v0.3
                                          ConeSearch-v0.3.xsd
                                          http://www.ivoa.net/xml/SIA/v0.7
                                          SIA-v0.7.xsd"
EOT
         my $start_namespace = 0;
         my $end_namespace = 0;

         while (<INPUT>) {
           chomp;

           $end_namespace = "1" if ($start_namespace eq "1" && $_ =~ />/);

           next if ($start_namespace eq "1" && $end_namespace eq "0");

           if ($ctr == 1) {
              $ctr = 2;
              next;
           }
           # Skip over reserved tags
           next if $_ eq "     <reserved>";
           next if $_ eq "     </reserved>";
           next if ($_ =~ /datestamp/);
           next if ($_ =~ /status/);
          # top-level tags
           if  ($_ =~ /<\/ResourceForm>/) {
              print "               </Resource>\n";
           } elsif ($_ =~ /ResourceForm/){
                $start_namespace = "1";
                next;
           } elsif ($_ =~ /<param/){ #"required" attribute not legal, so remove
                print "                          <param>\n";
                next;
           } else {
              print "               $_\n";
           } 
         }
         close(INPUT)                || die "can't close $res->[0]: $!";
         print "            </oai:metadata>\n";
         print "        </oai:record>\n";
    }
    print "    </ListRecords>\n";
}

###############################################################################
#
# formatBanner: return the HTML that makes up the top banner for all pages
# generated by this script.
#
###############################################################################

sub formatBanner {

    return <<EOT;
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="112" height="32" align="center" valign="top">
      <a href="http://www.us-vo.org" class="nvolink"><img 
         src="http://www.us-vo.org/images/NVO_100pixels.jpg" alt="NVO HOME"
         border="0"/></a>
             <br>
      <span class="nvolink"><a
             href="http://www.us-vo.org/">National Virtual Observatory</a>
      </span><br>
    </td>

    <td valign="top">
      <table  width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td bgcolor="white" width="2"></td>
          <td bgcolor="#CFE5FC" valign="middle" align="center" height="32"
              class="nvoapptitle">

       <!-- Name of Application -->
       NVO Registration Portal

          </td>
          <td bgcolor="white" width="2"></td>
       </tr>
       <tr>
          <td bgcolor="white" width="2"></td>
          <td height="10" valign="top"
              background="http://www.us-vo.org/app_templates/stars.jpg" >
          </td>
          <td bgcolor="white" width="2"></td>
       </tr>
       <tr>
         <td align="center" valign="top" colspan="3">
         <table cellspacing="2" cellpadding="0" border="0" width="100%"
                style="margin: 0pt;">
           <tr>
             <!-- the local links -->
             <td class="navlink"><a href="/nvoregistration.html">Re-enter Publishing Portal</a></td>
             <td class="navlink"><a href="http://www.ivoa.net/doc/latest/ConeSearch.html">How to Publish</a></td>
             <td class="navlink"><a href="http://nvo.stsci.edu/voregistry/index.aspx">Search the STScI Registry</a></td>
             <td class="navlink"><a href="http://nvo.caltech.edu:8080/carnivore">Search the Carnivore Registry</a></td>
             <td class="navlink"><a href="http://rofr.ivoa.net/">RofR</a></td>
             <td class="helplink"><a href="http://www.us-vo.org/feedback/">Contact Us</a></td>
           </tr>
         </table>
         </td>
       </tr>
    </table>
    </td>
    <td width="140" align="center" valign="top">

      <!-- local logo and link -->
      <a href="http://nvo.ncsa.uiuc.edu"><img height="54"
         src="/UofI-NCSA-black_logo_color.md.jpg"
         alt="NVO @ NCSA" border="0"/></a>
         
      <br>
      <span class="nvolink"><a
            href="http://nvo.ncsa.uiuc.edu/">National Center for 
      Supercomputing Applications</a> </span>
    </td>
   </tr>

</table>

EOT
}

###############################################################################
#
# formatBanner: return the HTML that makes up the top banner for all pages
# generated by this script.
#
###############################################################################

sub formatFooter {

    return <<EOT;

<hr noshade>
<table width="100%"  border="0">
  <tr>
    <td><div align="center"><a href="http://www.nsf.gov"><img src="http://www.us-vo.org/images/nsflogo_64x.gif" width="64" height="64" border="0"></a></div></td>
    <td><div align="center"><a href="http://www.nasa.gov"><img src="http://www.us-vo.org/images/nasa_logo.gif" width="72" height="60" border="0"></a></div></td>
    <td><p class="tiny" align="center">Developed with the support of the National Science Foundation under Cooperative Agreement AST0122449 with The Johns Hopkins University.</p>
      <p class="tiny" align="center">The NVO is a member of the International Virtual Observatory Alliance.<br>
      </p></td>
    <td><div align="center"><a href="http://www.ivoa.net"><img src="http://www.us-vo.org/images/IVOAlogo.gif" width="80" height="44" border="0"></a></div></td>

    <td><div align="center"><a href="#"><img src="http://www.us-vo.org/images/bee_hammer.gif" border=0><br>
          <span class="tiny">meet the<br>
    developers</span></a></div></td>
  </tr>
</table>

EOT
}

##############################################################################
#
# convert_wave - Convert the wavelength from input format to meters.
#
##############################################################################

sub convert_wave {
    my ($wave, $units) = @_;

    my $c = 2.9979e8; #speed of light (m/s)
    my $h = 6.62608e-34; #Planck's constant (J*s)
    my $Joules_per_eV = 1.602e-19; # Joules/eV
    my $meters_per_angstrom = 10e-10;   # meters/angstrom

    my $results = "";   
 
# Important formulae:
#   E(J) = hv v=frequency in Hz, h is J*s, E will be in Joules
#   v = c/lambda - lambda in meters, c in meters/s
#   -> lambda(meters) = (h * c)/(eV_to_Joules *E(eV))
 
    if ($units eq "meters") { return $wave};   #Already in meters!
    if ($units eq "GeV"){
         $results = ($h * $c)/($wave * 1e9 * $Joules_per_eV);
    }
    if ($units eq "MeV"){
         $results = ($h * $c)/($wave * 1e6 * $Joules_per_eV);
    }
    if ($units eq "KeV"){
         $results = ($h * $c)/($wave * 1e3 * $Joules_per_eV);
    }
    if ($units eq "eV"){
         $results = ($h * $c)/($wave * $Joules_per_eV);
    }
    if ($units eq "GHz") {
         $results = $c/(1.e9 * $wave);
    }
    if ($units eq "MHz") {
         $results = $c/(1.e6 * $wave);
    }
    if ($units eq "Hz") {
         $results = $c/$wave;
    }
    if ($units eq "mm") {
         $results = $wave/1000.;
    }
    if ($units eq "microns") {
         $results = $wave/1.e6;
    }
    if ($units eq "Angstroms") {
         $results = $wave*$meters_per_angstrom;
    }
    return $results;
}

#####################################################################################
#
# find_registry_file - Find the Registry resource file so we can add an
#                      Authority ID to it.
#
#####################################################################################
sub find_registry_file {

# Open up the Registry directory. find the xml file.
# It should always be of the form username00003.xml
        my $rdir = "$path/Registry";
        opendir (DIR, $rdir);
        my @reg = grep(/00003.xml$/, readdir (DIR));
        close (DIR);
        return @reg[0];
}

#####################################################################################
#
# add_authority_to_registry_file - Open the registry file and add a new
# Authority to its list of Authorities.  Should only be called when a
# new Authority is added.
#
#####################################################################################

sub add_authority_to_registry_file {

    my ($authorityId, $reg_file) = @_;
    my $add_auth_temp = "$path/add_auth_tempfile";

}
   
1;
