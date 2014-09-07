
function export_resources_to(where) {
/*
   create a votable object with the following string fields
      "title", "identifier", "accessURL", "capabilityClass"
   Loop over all resources
   selResources[] (may be completely empty or contain lots of 'undefined' and 'null')
      if (selResource[i])  
         add a row to the resource votable with the values ...
            metadata[i][DMfields["Title"]]
            metadata[i][DMfields["Identifier"]]
            metadata[i][DMfields["ServiceURL"]]
            metadata[i][DMfields["ServiceType"]]
   convert votable object to string
*/
   switch(where){
      case 'ascii':     return render_resources_to_ascii();
      case 'xml':       return render_resources_to_xml();
      case 'vim':       return send_resources_to(VIM);
      case 'inventory': return send_resources_to(Inventory);
   }
}

function render_resources_to_ascii() {
   var str = "title | identifier | shortName | accessURL | capabilityClass\n";
   for ( var i=0; i <= selResource.length; i++ ){
      if ( !!selResource[i] ) {
/*
         str += metadata[i][DMfields["Title"]] + " | "
         str += metadata[i][DMfields["Identifier"]] + " | "
         str += metadata[i][DMfields["ShortName"]] + " | "
         str += metadata[i][DMfields["ServiceURL"]] + " | "
         str += metadata[i][DMfields["ServiceType"]] + "\n"
*/
         str += metadata[i][DMfields["title"]] + " | "
         str += metadata[i][DMfields["identifier"]] + " | "
         str += metadata[i][DMfields["shortName"]] + " | "
         str += metadata[i][DMfields["accessURL"]] + " | "
         str += metadata[i][DMfields["capabilityClass"]] + "\n"
      }
   }
   top.twin = window.open(null);
   top.twin.document.write("<html><head><title>ASCII table</title></head><body>"+
      "<pre>"+str+"</pre>"+
      "</body></html>");
   top.twin.document.close();
}

function create_resource_votable() {
   var votable = "<VOTABLE><RESOURCE><TABLE>\n";
   votable += "<FIELD name='title' datatype='char' arraysize='*' />\n";
   votable += "<FIELD name='identifier' datatype='char' arraysize='*' />\n";
   votable += "<FIELD name='shortName' datatype='char' arraysize='*' />\n";
   votable += "<FIELD name='accessURL' datatype='char' arraysize='*' />\n";
   votable += "<FIELD name='capabilityClass' datatype='char' arraysize='*' />\n";
   votable += "<DATA><TABLEDATA>\n"
   for ( var i=0; i <= selResource.length; i++ ){
      if ( !!selResource[i] ) {
/*
         votable += "<TR><TD>" + metadata[i][DMfields["Title"]] + "</TD>";
         votable += "<TD>" + encodeURIComponent(metadata[i][DMfields["Identifier"]]) + "</TD>";
         votable += "<TD>" + metadata[i][DMfields["ShortName"]] + "</TD>";
         votable += "<TD>" + encodeURIComponent(metadata[i][DMfields["ServiceURL"]]) + "</TD>";
         votable += "<TD>" + metadata[i][DMfields["ServiceType"]] + "</TD>";
*/
         votable += "<TR><TD>" + metadata[i][DMfields["title"]] + "</TD>";
         votable += "<TD>" + encodeURIComponent(metadata[i][DMfields["identifier"]]) + "</TD>";
         votable += "<TD>" + metadata[i][DMfields["shortName"]] + "</TD>";
         votable += "<TD>" + encodeURIComponent(metadata[i][DMfields["accessURL"]]) + "</TD>";
         votable += "<TD>" + metadata[i][DMfields["capabilityClass"]] + "</TD>";
         votable += "</TR>\n";
      }
   }
   votable += "</TABLEDATA></DATA></TABLE></RESOURCE></VOTABLE>";
   return votable;
}

function render_resources_to_xml() {
   var form = document.getElementById('resource_outputform');
   form.action = "votable2xml.pl";
   form.elements["resources"].value = create_resource_votable();
   form.submit();
   return false;
}

function send_resources_to(url) {
   var form = document.getElementById('resource_outputform');
   form.action = url;
   form.target = Math.random();   // create a random target for window to be opened
   form.elements["resources"].value = create_resource_votable();
   form.submit();
   return false;
}
