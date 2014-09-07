// Functions related to be the basic functionality of querying
// in the SimpleQuery tool.

var candidateIVOID;
var candidateIVOIDIndex;
var myModal;

// Define functionality associated with actions on the form.
$(function() {

    // Get the metadata for a resource.	
    $.listen('click','#getmetadata', function(){
	$.get("@URL_PATH@metaquery.pl", {IVOID: $('#IVOID').val() }, function(data){
	     myModal = $(data).modal({close:true});
        });
    });
	
    // If the user leaves the IVOID identifier field, then
    // update the caption for the field.
	
    $.listen('blur','#IVOID', updateCaption );

    // If the user leaves the inputs source function then ???
    $('input.src').blur( function() {
	if ( ( $(this).val().length <= 0 ) || ( ( ! $(this).attr('checked') ) && ( $(this).val() == "checked" ) ) ) {
	    $('input.src').attr('disabled',false);
	} else {
	    $('input.src').attr('disabled',true);
	    $(this).attr('disabled',false);
	}
    });

    // If the user resets the inputs then clean up.
    $('input#reset').click( function() {
        $('#errorHolder').empty();
        $('form#basequery tr').removeClass('errorField');
	$('input.src').attr('disabled',false);
	$('input.src').val(''); // clear out hidden src parameters (sources, sourcesURL) as well
      	$('input.src').fadeIn();
	$('#IVOID').val('');
	$('#viewtable').fadeIn();
	$('#positiontable').fadeIn();
        updateCaption();
    });

    $('#positionmodal').click(function () {
	$.get("@URL_PATH@position.html", function(data){
	    $(data).modal({ });
        });
    });
    
    $('#radiusmodal').click(function () {
        $.get("@URL_PATH@radius.html", function(data){
	     $(data).modal({ });
	});
    });
    
    $('#alldatamodal').click(function () {
	$.get("@URL_PATH@alldata.html", function(data){
	    $(data).modal({ });
        });
    });
        
    $('#allskymodal').click(function () {
        $.get("@URL_PATH@allsky.html", function(data){
	    $(data).modal({ });
        });
    });
    
    $('#ivoidmodal').click(function () {
	$.get("@URL_PATH@ivoid.html", function(data){
	    $(data).modal({ });
	});
    });
    
    $('#querytoggle').click(function () {
        $('#innerquerydiv').slideDown();
        $('#innerviewdiv').slideUp();
    });
    
    $('#viewtoggle').click(function () {
        $('#innerviewdiv').slideDown();
        $('#innerquerydiv').slideUp();
    });
    
    updateCaption();
    $('#innerviewdiv').slideUp();
    
    if ( $('#POSITION').val() || $('#sources').val() || $('#sourcesURL').val() ) {
	$('#viewtable').fadeOut();
//      $('input.src').fadeOut();
//	$('#querytitle').append('<br/>Using position(s) from given POSITION/sources/sourcesURL');
    }
    
    if ( $('#IVOID').val() ) {
	$('#viewtable').fadeOut();
    }
});


function doUpdateCaption(index) {
    var ivo = candidateIVOID[index].split(/\s+/);
    $('#IVOID').val(ivo.shift()); // insert complete IVOID as the search will match partial
    $('#querycaption').html("Query resource:&nbsp;"+ivo.join(' ')+"&nbsp;(<a class='small' href='javascript:void(0)' id='getmetadata'>Columns</a>)");
    candidateIVOIDIndex = index;
}
    
function updateCaption () {
    if ( $('#IVOID').val() ) {
        $.get('@URL_PATH@getIVOnames.pl', { IVOID: $('#IVOID').val() },
          function(data){
	      if (data) {
		  candidateIVOID = data.split("\n");
		  if (candidateIVOID[candidateIVOID.length-1].length == 0) {
		      candidateIVOID.pop();
		  }
		  if (candidateIVOID.length > 0) {
		      doUpdateCaption(0);
		  }
		  if (candidateIVOID.length > 1) {
		      showCandidates();
		  }
              }
	  }
	);
    } else {
        candidateIVOID      = null;
	candidataIVOIDIndex = null;
    }
}

function showCandidates() {

    var str = "<td><td><a href='javascript:void(0)' id='ivoidmodal'>Multiple IVO Identifiers</a><br> Please select a table:</td>"+
                   "<td><select name='IVOID' id='IVOID' onchange='doUpdateCaption(this.selectedIndex); return true;'> ";
    for (var i=0; i<candidateIVOID.length; i += 1) {
        var ivo = candidateIVOID[i].split(/\s+/);
	
	var selected = "";
	if (i == 0) {
	    selected = " selected ";
	}
	var id = ivo[0];
	id = id.replace(/^.*VizieR\//g,"");
        str += "<option value='" + ivo[0] + "'" + selected + "> " + id+"\n";
    } 
    str += "</select></td>"+
           "<td align='center'><a href='javascript:void(0)' id='alldatamodal'>All columns?</a> <input id='Verbosity' type='checkbox' name='Verbosity' /></td>";
    document.getElementById("trIVOID").innerHTML = str;
}
