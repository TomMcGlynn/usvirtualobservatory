
function openList(fromjob){
    
     var url = document.location.href ;
     url = url.substr(0,url.lastIndexOf("/"));
     if(fromjob == 'display')
       window.open(url, "_self");     
     else
        window.open(url+"/async","_self" );
 }

 function test()
 {
     var url = document.location.href ;
     url = url.substr(0,url.lastIndexOf("/")+1);
     //alert("intest!!"+url);
 }
 
  function newJob(){
    var url = document.location.href ;
    url = url.substr(0,url.lastIndexOf("/")+1);                   
   window.open(url+"async?newjob=newjob","_self");
 }
 function newJobPage(){
    var url = document.location.href ;
    url = url.substr(0,url.lastIndexOf("/tap/")+1);                   
    window.open(url+"newasync.jsp","_self");
 }
 function showJob(jobid){
   //alert(jobid)  ;
   var url = document.location.href ;
   url = url.substr(0,url.lastIndexOf("/")+1);                   
   window.open(url+"async/"+jobid,"_self");
 }

function simplesubmit(jobid){

    var url = document.location.href ;
        url = url.substr(0,url.lastIndexOf("/"));
        alert(url);
    var somedata = url+"/async?"
        somedata +='REQUEST=doQuery';
        somedata +='&LANG='+ document.getElementById('langText').value;
        somedata +='&QUERY='+ escape(document.getElementById('queryText').value);
        somedata +='&newJobId='+jobid;
        somedata +='&Destruction='+document.getElementById('destructText').value;
    //alert(somedata);
    window.open(somedata,"_self" );
}

 function myfunction(){
      alert("First Submit your query");
 }

 
function displayResult(jobid){
   var url = document.location.href ;
    //url = url.substr(0,url.lastIndexOf("/")+1);
     window.open(url+"/results/result", "self");
}

function runJob(jobid){
     var url = document.location.href ;
     url = url.substr(0,url.lastIndexOf("/"));
     
     var params =  {PHASE : "RUN"}
    post_to_url(url+"/"+jobid+"/phase",params,"POST");
}


function post_to_url(path, params, method) {
    method = method || "post"; // Set method to post by default, if not specified.

    // The rest of this code assumes you are not using a library.
    // It can be made less wordy if you use one.
    var form = document.createElement("form");
    form.setAttribute("method", method);
    form.setAttribute("action", path);

    for(var key in params) {
        if(params.hasOwnProperty(key)) {
            var hiddenField = document.createElement("input");
            hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", key);
            hiddenField.setAttribute("value", params[key]);

            form.appendChild(hiddenField);
         }
    }

    document.body.appendChild(form);
    form.submit();
}