$(function() {

	//	Must use listen because the element changes
//	$.listen('blur','#IVOID', checkIVOID );
//	$.listen('click','option.ivo', checkIVOID );
//	$.listen('submit','form#basequery',function() {
//		$('select#IVOID').replaceWith("<input id='IVOID' value='"+$('#IVOID').val()+"' name='IVOID' size='30'/>");
//	});

	$.listen('click','#getmetadata', function(){
		$.get("@URL_PATH@/metaquery.pl", {IVOID: $('#IVOID').val() }, function(data){
			$(data).modal();
		});
	});
	$.listen('blur','#IVOID', updateCaption );

	$('input.src').blur( function() {
		if ( ( $(this).val().length <= 0 ) || ( ( ! $(this).attr('checked') ) && ( $(this).val() == "checked" ) ) ) {
			$('input.src').attr('disabled',false);
		} else {
			$('input.src').attr('disabled',true);
			$(this).attr('disabled',false);
		}
	});

	$('input#reset').click( function() {
//		$('input.src').attr('disabled',false);
	});

	$('#positionmodal').click(function () {
		$.get("@URL_PATH@/position.html", function(data){
			$(data).modal({ });
		});
	});
	$('#alldatamodal').click(function () {
		$.get("@URL_PATH@/alldata.html", function(data){
			$(data).modal({ });
		});
	});
	$('#allskymodal').click(function () {
		$.get("@URL_PATH@/allsky.html", function(data){
			$(data).modal({ });
		});
	});
	$('#ivoidmodal').click(function () {
		$.get("@URL_PATH@/ivoid.html", function(data){
			$(data).modal({ });
		});
	});
	updateCaption();
	if ( $('#POSITION').val() || $('#sources').val() || $('#sourcesURL').val() ) {
		$('#viewtable').fadeOut();
		$('#positiontable').fadeOut();
		$('#querytitle').append('<br/>Using position(s) from given POSITION/sources/sourcesURL');
	}
	if ( $('#IVOID').val() ) {
		$('#viewtable').fadeOut();
	}
});

function updateCaption () {
	if ( $('#IVOID').val() ) {
		$.get('@URL_PATH@/getIVOnames.pl', { IVOID: $('#IVOID').val() },
			function(data){
/*
ivo://nasa.heasarc/a1       A1
ivo://nasa.heasarc/a1point  A1POINT
ivo://nasa.heasarc/ascalss  ASCA LSS
Note that the ShortName can be more than one "word"!
*/
				if(data){
					names = data.split("\n");
					ivo = names[0].split(/\s+/);
					$('#IVOID').val(ivo.shift()); // insert complete IVOID as the search will match partial
					$('#querycaption').html("Query resource:&nbsp;"+ivo.join(' ')+"&nbsp;<span id='getmetadata'>(meta data)</span>"); 
				}
			}
		);
	} else {
		$('#querycaption').html("Query a single NVO resource");
	}
}

/*
	checkIVOID converts the IVOID into a selector with the matching IVOIDs

	This is no longer used as it was a bit buggy
*/
function checkIVOID () {
	var longname = $('#IVOID').val();
//	try to implement getIVOnames.pl which returns stuff like ...
//Content-Type: text/html; charset=ISO-8859-1
//  
//ivo://nasa.heasarc/a1          A1
//ivo://nasa.heasarc/a2led       A2LED
//ivo://nasa.heasarc/a2pic       A2PIC
//ivo://nasa.heasarc/a3          A3
//
// to ensure that the correct short and long name is used as
// there is no guarantee that they are returned in the same order
// although that shouldn't make any difference ???
//
// getShortname may not work correctly as the exact match may not be first
// so may want to try to implement this instead of getShortname
// and do an exact match.
	if ( longname.length > 0 ) {
		$.get('@URL_PATH@/getLongnames.pl', { IVOID: longname },
			function(data){
				names = data.split("\n");
				if ( names.length <= 1 ) {
					// Nothing found so do nothing.
				} else if ( names.length == 2 ) {
					$('#IVOID').val(names[0]);
					$('select#IVOID').replaceWith("<input id='IVOID' value='"+$('#IVOID').val()+"' name='IVOID' size='30'/>");
					$.get('@URL_PATH@/getShortname.pl', { IVOID: names[0] },
						function(data){ $('#querycaption').html('Query resource: '+data); }
					);
				} else {
					$('#IVOID').replaceWith("<select id='IVOID'></select>");
					names.length = names.length-1;
					names.sort;
					for ( i=0;i<names.length;i++) {
						$('select#IVOID').append("<option class='ivo' value='"+names[i]+"'>"+names[i]+"</option>");
					}
// can cause some problems
//					$('#IVOID').focus();
				}
			}
		);
	}
}

