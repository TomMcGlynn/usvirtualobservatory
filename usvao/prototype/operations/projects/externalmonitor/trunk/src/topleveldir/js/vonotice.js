function test()
{  		
		
         var now = new Date();
         nowmili = now.getTime();
       
         arrayzone     =  determinezoneoffset();
         var julytz    = arrayzone[0];
         var jantz     = arrayzone[1];
         var currenttz = arrayzone[2];
         
         // timezone types
         var zonename = new Array("GMT","","","","","EST","CST","MST","PST");
         var zonenameb = new Array("GMT","","","","EDT","CDT","MDT","PDT");
         
         //priority types
         var priority = new Array("Highest", "High", "Medium","Low");
	
         //see if   user chose a zone manually
         var zonechosencontents = document.getElementById("zonechosen"); 
         var zonechosen         = zonechosencontents.getAttribute("value");
         var userzonename;
         if (zonechosen == 'none')
         {
             if ((currenttz == julytz) && (julytz < jantz))
             {
                 userzonename = zonenameb[Number(currenttz)];                   
             }     
             else 
             {
                 userzonename = zonename[Number(currenttz)];             
             }
         }
         else
         {          
            for (i =0; i<zonename.length;i++)
            {
                if (zonename[i] == zonechosen)
                {
                    userzonename = zonechosen;
                    currenttz    = i;               
                }
            }
            for (i =0; i<zonenameb.length;i++)
            {
                if (zonenameb[i] == zonechosen)
                {
                    userzonename = zonechosen;
                    currenttz    = i;
                }
            }
        }
               
         var table = document.getElementById("note_table");
         var tags =  table.getElementsByTagName("td");
         var pops =  table.getElementsByTagName("a");
         var trs  =  table.getElementsByTagName("tr");
         for (var i = 0;i<pops.length; i++)
         {
             adjust_title(pops,i);           
         }
       
         var dayadjust = Number(currenttz)/24;
      
         for (var i = 0; i < tags.length; i++)
         {    
             status =  tags[i].innerHTML;                    
             var pattern = /\d{4}-\d{2}.*/;
             //var pattern   = /\\d+.*/;
            
             var result = status.match(pattern);
             if (result != null)
             {           	         
                 arraydateandtime = status.split(' ');
                 var  ymd         =  arraydateandtime[0];                
                 arraydate        = ymd.split('-');
                 var y            = Number(arraydate[0]);
                 var  m           = Number(arraydate[1]);
                 var  d           = Number(arraydate[2]);
                 var p            = /0\\d/;
                 if  ( p.exec(m)){ m  = m.replace(/^[0]/g,"");}
                 if  ( p.exec(d)){ d  = d.replace(/^[0]/g,"");}
                 
                 arraytime        =  arraydateandtime[1].split(':');
                 //apply zone adjustment;                
                 var h          = Number(arraytime[0]);
                 
                 var jd = cal_to_jd('CE',y,m,d,h,"0","0");
                        
                 //special test (compare chosen date to current time to determine active or inactive)
                 //date added by user is stored in GMT initially so ...convert current time to GMT millisecondsn 
                 // and compare  
                     
                 var newjd = Number(jd)-Number(dayadjust);
                    
                 var newcaldate = new Array();
                 newcaldate = jd_to_cal(newjd);
                 var newcaldateh; 
                
                 if ((newcaldate[4] == '59') && (newcaldate[5] == '60'))
                 {
                     newcaldateh =   Number(newcaldate[3])+1;
                 }
                 else
                 {
                     newcaldateh = newcaldate[3];
                 }
                 var newm    =  zeropad(Number(newcaldate[1]));
                 var newmextra = newcaldate[1]-1;
                 var newd    =  zeropad(Number(newcaldate[2]));
                 var newh    =  zeropad(Number(newcaldateh));                           
                 var neatdate = newcaldate[0] + "-" + newm + "-" + newd + " " + newh  + ":" + "00";              
                 tags[i].innerHTML  = neatdate;
                  
                 //alert (newcaldate[0]  + " "  + newmextra + " " + newcaldate[2] + " " + newcaldateh);
                 var datechosen = new Date(newcaldate[0],newmextra,newcaldate[2], newcaldateh,'0','0','0');
                 var datechosenmili  =  datechosen.getTime();

                 //alert("PRRR"  + datechosenmili + " " + nowmili + " "  + datechosen  );
                 if (datechosenmili < nowmili)
                 { 
                     //change to active
                        
                     var hold =  tags[i+4].innerHTML;
                    
                     if (hold == "pending") 
                     { 
                         tags[i+4].innerHTML = "active";
                         tags[i].parentNode.setAttribute('class','active');
                     }
                 }                                
             }
             else 
             {           
                 var subpattern = /\d{1}$/;
                 var subresult  = status.match(subpattern); 
                 if (subresult != null)
                 {                  
                     var name = priority[status-1];
                     tags[i].innerHTML = name;
                 }               
             }
         }
          
         function adjust_title(pops,i)
         {                          
             name = pops[i].innerHTML;
             
             if ((name == "Expiration Date") || (name == "Effective Date"))
             {
                 name  =  name +  " (" + userzonename + ")";             
                 pops[i].innerHTML = name;
             }             
         }
}
function gmt_to_edt()
{         
        var t  = new Date();
         
	arrayzone     = determinezone();
        var julytz    = arrayzone[0];
        var jantz     = arrayzone[1];
        var currenttz = arrayzone[2];

        var userzonename;
        // timezone types
	var zonename = new Array("GMT","","","","","EST","CST","MST","PST");
        var zonenameb = new Array("GMT","","","","EDT","CDT","MDT","PDT");


	var table = document.getElementById("notes");
	var tags  = table.getElementsByTagName("td");
	if (tags.length > 1)
	{
		
       		if ((currenttz == julytz) && (julytz < jantz))
        	{
            	userzonename = zonenameb[Number(currenttz)];                   
        	}     	
        	else 
        	{
            	userzonename = zonename[Number(currenttz)];             
        	}
	
		tags[1].innerHTML = "Effective Date (" + userzonename + ")"; 
		tags[2].innerHTML = "Expiration Date (" + userzonename + ")";
 		for (var i = 4; i<tags.length;i++)
		{
	 
	        	status = tags[i].innerHTML;
	        	var pattern = /\d{4}-\d{2}.*/;
			var result = status.match(pattern)
			if (result != null) 
	       		{           
			
				arraydateandtime = status.split(' ');
                 		var  ymd         =  arraydateandtime[0];                
                 		arraydate        = ymd.split('-');
                 		var y            = Number(arraydate[0]);
                 		var  m           = Number(arraydate[1]);
                		var  d           = Number(arraydate[2]);
              	  		var p            = /0\d/;
                 		if  ( p.exec(m)){ m  = m.replace(/^[0]/g,"");}
                 		if  ( p.exec(d)){ d  = d.replace(/^[0]/g,"");}
                 
                 		arraytime        =  arraydateandtime[1].split(':');
                	 	//apply zone adjustment;                
                 		var h          = Number(arraytime[0]);
                 	
                		var jd = cal_to_jd('CE',y,m,d,h,"0","0");
                        
                		var q = Number(currenttz)/24;
                         				 
                		var newjd = Number(jd)-Number(q);            
                 		var newcaldate = new Array();
                 		newcaldate = jd_to_cal(newjd);
		
                 		var newcaldateh; 
 	        	       
        	      	  	if ((newcaldate[4] == '59') && (newcaldate[5] == '60'))
                		{
                	    		newcaldateh =   Number(newcaldate[3])+1;
                 		}	
                 		else
                	 	{
                   	  		newcaldateh = newcaldate[3];
                 		}	
               			var newm    =  zeropadedt(Number(newcaldate[1]));
	  	 		var g       = zeropadedt(Number(9));
                 		var newmextra = newcaldate[1]-1;
                		var newd    =  zeropadedt(Number(newcaldate[2]));
                 		var newh    =  zeropadedt(Number(newcaldateh));
	 
                 		var neatdate = newcaldate[0] + "-" + newm + "-" + newd + " " + newh  + ":" + "00";       
                 		tags[i].innerHTML  = neatdate;		
			}
		}
        }
}

function zeropadedt(number)
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
function determinezone()
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


