
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
    //var url = document.location.href ;
    //url = url.substr(0,url.lastIndexOf("/")+1);                   
    window.open("/sdss/newasync.jsp","_self");
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
        //alert(url);
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