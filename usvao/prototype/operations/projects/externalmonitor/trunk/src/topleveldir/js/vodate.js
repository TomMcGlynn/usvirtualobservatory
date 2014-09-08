function setpriority(document)
{
     var p =  document.getElementById("priority");
     p.options[2].selected = true;
     return document;
}
function setmydate(content, inputdate)
{              
    for (i=0 ; i<content.options.length; i++)
    {
          var y_or_m_or_d_or_h  = content.options[i].value;          
          if (y_or_m_or_d_or_h   ==  inputdate)
          {
               content.options[i].selected = true;
          }            
     }
     return content;
} 
function gettimezone()
{
    var d = new Date();
    var gmtHours = d.getTimezoneOffset()/60;
    return gmtHours;
}         
         
function setmymonthoryear(Content,input)
{
    for (i=0; i<Content.options.length;i++)
    {
         var x  = Content.options[i].text;
         if (x == input)
         {               
              Content.options[i].selected  = true;   
         }
     }
     return Content;
}
function determinezoneoffset()
{
 	         
	var jandate          = new Date();
       	var julydate         = new Date();
      	var today            = new Date();
      	var currentoffset  = today.getTimezoneOffset();
      	currentoffset     = currentoffset/60;
      	julydate.setMonth(7);
      	julydate.setDate(1);
          
      	jandate.setMonth(0);
       	jandate.setDate(1);
       	var julytzoffset  = julydate.getTimezoneOffset();
       	julytzoffset   = julytzoffset/60;
      	var jantzoffset = jandate.getTimezoneOffset();
       	jantzoffset     = jantzoffset/60;
       	var janday    = jandate.getDate();
       	var janyear   = jandate.getFullYear();
       	var janmonth  = jandate.getMonth();
       	var julyday   = julydate.getDate();
       	var julymonth = julydate.getMonth();
       	var julyyear   = julydate.getFullYear();
          
      	//alert("WWW" + julyyear+ " " + julymonth + " " + julyday + " " + julytzoffset  + " " + jantzoffset);
          
      	var newArray = [julytzoffset, jantzoffset,currentoffset];
       	return newArray;
}
function zeropad(number)
{           
     
     var newnumber;
     var result;
     var pattern         = /\d{2}/;           
     if  (pattern.exec(number) == null)
     {
          newnumber  = "0" + number;               
     }
     else 
     {
          newnumber = number;
     }
     return newnumber;
}


                              
