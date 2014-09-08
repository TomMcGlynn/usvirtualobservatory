function addheader()
{
	alert ("here");
	var request;
	// Mozilla-based browsers

	if (window.XMLHttpRequest) 
	{	
		request = new XMLHttpRequest();
	}
	else if (window.ActiveXObject) 
	{
		request = new ActiveXObject("Msxml2.XMLHTTP");

		if (!request) 
		{
			request = new ActiveXObject("Microsoft.XMLHTTP");
		}		
	}	
	if (request != null)
	{
	        request.onreadystatechange= function()
		{						
			if (request.readyState == 4)	
			{
				if (request.status == 200)
				{		
					var text  = request.responseText;
					alert(text);
					if (text)
					{	alert ("good");					
						parent.document.getElementById('child').innerHTML = text;
												
						var outer = parent.document.getElementById('child');	
						var tables = outer.getElementsByTagName("table");						
					}					
				}
			}
			else
			{	
				//alert("There is an error on the page. Request status is:" + request.status);
			}		
		}			
		var hostname  = window.location.hostname;
		var a          = "http://" + hostname; 
	        var address    = a + "/vo/vaomonitor/data/banner.html";
		
		request.open("GET",address,true);
		request.send(null);	
	}
	else
	{
		alert("SHould not be in this else");
	}	
}
