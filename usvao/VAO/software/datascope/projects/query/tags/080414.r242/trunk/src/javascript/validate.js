
function validate(f) {
	$('#errorHolder').empty();
	var valid = true;

	$('form#basequery tr').removeClass('errorField');

//	if ( f.POSITION.value.is_empty()
//		&& f.sources.value.is_empty()
//		&& f.sourcesURL.value.is_empty()
//		&& f.viewLocal.value.is_empty()
//		&& f.viewURL.value.is_empty()
//		&& !f.allsky.checked
	if (  !$('#POSITION').val()
		&& !$('#sources').val()
		&& !$('#sourcesURL').val()
		&& !$('#viewLocal').val()
		&& !$('#viewURL').val()
		&& !$('#allsky[@checked]').val()
		) {
		$('#errorHolder').append('<p class="errorMessage">Position, sources, sourcesURL, viewLocal, viewURL or all-sky required.</p>');
		$('tr#trPOSITION').addClass('errorField');
		$('tr#trviewLocal').addClass('errorField');
		$('tr#trviewURL').addClass('errorField');
		$('tr#trallsky').addClass('errorField');
		valid = false;
	}
	var i = 0;
	if ( !!$('#POSITION').val()  ) { i++; }
	if ( !!$('#sources').val()  ) { i++; }
	if ( !!$('#sourcesURL').val()  ) { i++; }
	if ( !!$('#viewLocal').val()  ) { i++; }
	if ( !!$('#viewURL').val()  ) { i++; }
	if ( !!$('#allsky[@checked]').val() ) { i++; }
//	if ( ! f.POSITION.value.is_empty() ) { i++; }
//	if ( ! f.sources.value.is_empty() ) { i++; }
//	if ( ! f.sourcesURL.value.is_empty() ) { i++; }
//	if ( ! f.viewLocal.value.is_empty() ) { i++; }
//	if ( ! f.viewURL.value.is_empty() ) { i++; }
//	if (   f.allsky.checked ) { i++; }
	if ( i > 1 ) {
		$('#errorHolder').append('<p class="errorMessage">Only one of Position, sources, sourcesURL, viewLocal, viewURL or all-sky allowed.</p>');
		$('tr#trPOSITION').addClass('errorField');
		$('tr#trviewLocal').addClass('errorField');
		$('tr#trviewURL').addClass('errorField');
		$('tr#trallsky').addClass('errorField');
		valid = false;
	}
//	if ( f.IVOID.value.is_empty() && f.viewLocal.value.is_empty() && f.viewURL.value.is_empty() ) {
	if ( !!$('#IVOID').val() && !!$('#viewLocal').val() && !!$('#viewURL').val() ) {
		$('#errorHolder').append('<p class="errorMessage">IVOID required with Position, sources, sourcesURL or all-sky.</p>');
		$('tr#trIVOID').addClass('errorField');
		valid = false;
	}	
/*
sometimes this works and sometimes it doesn't ????
	if ( valid ) {
//		if ( ! f.POSITION.value.is_empty() ) {
//			if ( f.POSITION.value.match('http:') ) {
		if ( !!$('#POSITION').val() ) {
			if ( !!$('#POSITION').val().match('^http:') ) {
				$('#sourcesURL').val($('#POSITION').val());
//				document.getElementById('sourcesURL').value = document.getElementById('POSITION').value;
				$('#POSITION').val("");
			}
		}
	}
*/
	return valid;
}
/*
String.prototype.is_empty = function() {
	if ( this == null || this == "" ) {
		return true;
	} else {
		return false;
	}
}
*/
