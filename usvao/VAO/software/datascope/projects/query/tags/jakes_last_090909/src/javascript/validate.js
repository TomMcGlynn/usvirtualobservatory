
function validate(f) {
   $('#errorHolder').empty();
   var valid = true;

   $('form#basequery tr').removeClass('errorField');
   $('form#basequery tr td').removeClass('errorField');

   var pos = 0;
   var rad = 0;
   var parameters = ""
   if ( !!$('#POSITION').val()  ) { 
      parameters += "POSITION:"+$('#POSITION').val()+":<br/>";
      pos++; 
   }
   if ( !!$('#sources').val()  ) { 
      parameters += "sources:Not Showing Value:<br/>";
      pos++; 
   }
   if ( !!$('#sourcesURL').val()  ) { 
      parameters += "sourcesURL:"+$('#sourcesURL').val()+":<br/>";
      pos++; 
   }
   if ( !!$('#viewLocal').val()  ) { 
      parameters += "viewLocal:Not Showing Value:<br/>";
      pos++; 
   }
   if ( !!$('#viewURL').val()  ) { 
      parameters += "viewURL:"+$('#viewURL').val()+":<br/>";
      pos++; 
   }

   if ( pos > 1 ) {
      $('#errorHolder').append('<p class="errorMessage">JavaScript Error: Only one of Position, sources, sourcesURL, viewLocal or viewURL.<br/>'+parameters+'</p>');
      $('tr#trPOSITION').addClass('errorField');
      $('tr#trviewLocal').addClass('errorField');
      $('tr#trviewURL').addClass('errorField');
      $('td#tdallsky').addClass('errorField');
      valid = false;
   }

   // DO include allsky in this check
   if ( !!$('#allsky[@checked]').val() ) { 
      parameters += "allsky:"+$('#allsky').val()+":<br/>";
      pos++; 
      rad++; 
   }
   if ( pos <= 0 ) {
      $('#errorHolder').append('<p class="errorMessage">JavaScript Error: Position, sources, sourcesURL, viewLocal, viewURL or all-sky required.<br/>'+parameters+'</p>');
      $('tr#trPOSITION').addClass('errorField');
      $('tr#trviewLocal').addClass('errorField');
      $('tr#trviewURL').addClass('errorField');
      $('td#tdallsky').addClass('errorField');
      valid = false;
   }

   if ( !!$('#RADIUS').val() ) { 
      parameters += "RADIUS:"+$('#RADIUS').val()+":<br/>";
      rad++; 
   }
   if ( rad > 1 ) {
      $('#errorHolder').append('<p class="errorMessage">JavaScript Error: Only one of RADIUS or allsky.<br/>'+parameters+'</p>');
      $('td#tdRADIUS').addClass('errorField');
      $('td#tdallsky').addClass('errorField');
      valid = false;
   }
   if ( !$('#IVOID').val() && !$('#viewLocal').val() && !$('#viewURL').val() ) {
      $('#errorHolder').append('<p class="errorMessage">JavaScript Error: IVOID required with Position, sources, sourcesURL or all-sky.<br/>'+parameters+'</p>');
      $('tr#trIVOID').addClass('errorField');
      valid = false;
   }   

   return valid;
}
