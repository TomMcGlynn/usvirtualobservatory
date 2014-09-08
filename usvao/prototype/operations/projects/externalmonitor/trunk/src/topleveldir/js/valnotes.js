function adderrors()
{

	var cds = document.getElementById("cds");
	var tags = cds.getElementsByTagName("td");
	

	for (var i= 4; i<tags.length; i+=3)
	{ 				 			 	
	  	status = tags[i].innerHTML;
		
		tags[i].innerHTML = "<a href = '/vo/valnotes/errors.html'>" + status + "</a>";
		p = tags[i].innerHTML;	        
	  		       
	}
	//errornames();
}
function errornames()
{
}
		







