function run_test(x)
{           
    window.location =  x + '&getresponse=yes';
}
function test(x)
{
    window.location = x;       
}
function getsupport(n,s,sup)
{
        
        //alert("PP " + s); 
        var p  = document.getElementById(s);           
        //var q = p.s.options[s.selectedIndex].value; 
        var q = p.options[p.selectedIndex].value;
 
          
       //document.n.s.options[document.n.selectedIndex].value;
       //alert ("G G " + q);
       location.href =  q;
       //document.n.submit(); 
       var y = formname.name;
   	
}
function wrapTestURLResponse(url,im)
{ 
    var  xmlHttp = new XMLHttpRequest();

    //xmlHttp.addEventListener("progress", updateProgress, false);
    //xmlHttp.addEventListener("load", transferComplete, false);
    //xmlHttp.addEventListener("error", transferFailed, false);
    //xmlHttp.addEventListener("abort", transferCanceled, false);
    
    xmlHttp.onreadystatechange = function(){ 
    if ((xmlHttp.readyState == 4) && (xmlHttp.status = 200)) 
    { 
      if (xmlHttp.responseText == 'Not found') {}
       else
       {
           document.getElementById(im).style.visibility = "hidden";             
           alert(xmlHttp.responseText);
       }

     }
   }
   xmlHttp.open( "GET", url, true);   
   xmlHttp.send( null );
    
}
