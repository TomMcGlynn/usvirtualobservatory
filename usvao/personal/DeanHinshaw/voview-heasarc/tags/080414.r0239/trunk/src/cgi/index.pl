#!/usr/bin/perl -w

use strict;
use CGI qw(:standard);

print header;

my $autosubmit = 'false';
my $url = param('url') || "";
my $stream = param('stream') || "";
my $file = param('file') || "";
$autosubmit = 'true' if ( $url || $stream || $file );
if ( $file && !$stream ) {
	while ( <$file> ) {
		$stream .= $_;
	}
}

my $selector = "<select> <option>Export to ...</option> <option id='renderAscii'>ASCII File</option> <option id='renderXML'>XML File</option><option id='renderQueryDEV'>Simple Query DEV</option><option id='renderQuery'>Simple Query</option><option id='renderVIM'>VIM Service</option><option id='renderInventory'>Inventory Service</option></select>";

my $jscript =<<END;
// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined
\$(function() {
	StateManager = EXANIMO.managers.StateManager;
	rd = new readdata({
			output: document.getElementById("output"),
			form: 'viewForm',
			searchparam: 'url',
			stream: 'stream',
			xsltdir: "@XSL@/",
			filename: '$file',
			exporter: "$selector",
			titletext: "VO Table Viewer, Filterer, Editor, Floter and Exporter"
		});
// filename contains actual filename rather than field
// because it is bounced of the server to be read
// forcing a page reload.
	rd.errorMessage = function(msg) {
		if ( msg.match("Searching") ) {
			var div = document.createElement('div');
			div.innerHTML = "<center><h1>Reading...</h1><br/><img src='@IMG@/PleaseWait.gif' /></center>";
			rd.clearOutput(div);
		} else {
			var p = document.createElement('p');
			p.innerHTML = msg;
			rd.clearOutput(p);
		}
		return false;
	}
	StateManager.onstaterevisit = rd.restoreState;
	StateManager.initialize();
	originalState = readCookie("voviewReloadState");
	if (originalState) {
		// restore the original state using the cookie value
		eraseCookie("voviewReloadState");
		rd.restoreState({id: originalState});
	}
	if ( $autosubmit ) {
		\$('input#submit').click();
	}
});
function validate(f) {
	if (f.file.value || f.stream.value || f.url.value ) {
		if(!f.file.value) {
			return rd.setView();
		}
		rd.errorMessage("Searching");
	} else {
		return false;
	}
}
END

#	voview.js sets the window title, overwriting the following setting
print start_html(
		-title   => "VO Table Viewer: $file", 
		-style   => { src => "css/voview.css" },
		-script  => [ 	{ -src => "js/voview.js" },
							{ -src => "js/cookie.js" },
							{ -src => "js/sarissa.js" },
							{ -src => "js/statemanager.js" },
							{ -src => "js/query.js" },
							{ -src => "js/filter.js" },
							{ -src => "js/fsm.js" },
							{ -src => "js/tablednd.js" },
							{ -src => "js/jquery.pack.js" }, 
							{ -src => "js/jquery.listen-min.js" }, 
							{ -src => "js/export.js" }, 
							$jscript
						],
	);

print <<EOF;
<div id='output' >This is where your votable should be. (You should never see this.  If you do, you probably have javascript disabled.)</div>
<center>
<hr />
<form id='viewForm' name='viewForm' onsubmit='return validate(this);' action='/vo/view/index.pl' method='post' enctype='multipart/form-data'>
<table>
<tr><td align='right'>URL</td><td><input size='100' name='url' id='url' value="$url" /></td></tr>
<tr><td align='right'>File</td><td><input size='90'  name='file' id='file' type='file' /></td></tr>
<tr><td align='right'>Text</td><td><textarea rows='30' cols='100' id='stream' name='stream' >$stream</textarea></td></tr>
<tr><td colspan='2' align='center'><input id='submit' type='submit' name='.submit' value='Load VOTable' />
<input type='reset' name='.reset' onclick='return rd.clearState();' /></td></tr>
</table>
EOF
foreach ( param() ) {
	print "<input type='hidden' id='$_' name='$_' value='".param($_)."' />\n"
		unless ( $_ =~ /^(url|file|stream)$/ );
}
print <<EOF;
</form>
<span class="searchnote compact">Requires Firefox, Safari, or compatible browser</span><br />
<span class="searchnote compact">Usage of the local file selector WILL require bouncing off the server to read the file.</span><br />
<span class="searchnote compact">Usage of the URL field MAY require bouncing off the server to read the file.</span>
</center>
EOF

print "<div id='outputformdiv' style='display: none;'>\n";
print "<form id='outputform' method='post'>\n";# enctype='multipart/form-data'>\n";
print "<input type='hidden' id='findResources' name='findResources' value='1' /><!-- for Inventory -->\n";
print "<input type='hidden' id='radius' name='radius' value='15' /><!-- for Inventory -->\n";
print "<input type='hidden' id='units' name='units' value='arcmin' /><!-- for Inventory -->\n";
print "<input type='hidden' id='toolName' name='toolName' value='sources' /><!-- for VIM -->\n";
print "<input type='hidden' id='sources' name='sources' />\n";
print "<input type='hidden' id='referralURL' name='referralURL' value='http://heasarc.gsfc.nasa.gov/vo/view/'/>\n";
foreach ( param() ) {
	print "<input type='hidden' id='$_' name='$_' value='".param($_)."' />\n"
		unless ( $_ =~ /^(url|file|stream|sources|referralURL|findResources|radius|units|toolname)$/i );
}
print "</form></div>\n";

print end_html();

