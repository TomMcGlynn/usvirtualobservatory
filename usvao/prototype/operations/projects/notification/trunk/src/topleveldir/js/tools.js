function datehandler(string)
{ 
	 var caldate        = document.getElementById("DPC_edit1_YYYY-MM-DD");
	 var caldateb  	     = document.getElementById("DPC_editnew_YYYY-MM-DD");
		 	 	
	 arraymonths        = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
	 array              = caldate.value.split('-');
	 arrayb             = caldateb.value.split('-');
	  var dayAcontent    = document.getElementById("dayA"); 
	  var monthAcontent  = document.getElementById("monthA");
	  var yearAcontent   = document.getElementById("yearA");
	  var hourAcontent   = document.getElementById("hourA");
	  var dayBcontent    = document.getElementById("dayB");
	  var monthBcontent  = document.getElementById("monthB");
	  var yearBcontent   = document.getElementById("yearB");
	  var hourBcontent   = document.getElementById("hourB");	  
	  var currentday;
		  
	  var currentyear   = array[0];	  	  
	  var currentmonth  = array[1];
	  currentmonth      = arraymonths[Number(currentmonth)-1];
	  var currentyearb  = arrayb[0];
	  var currentmonthb = arrayb[1];	 
	  var setday        = array[2];
	  var srch          = RegExp("^0+");
	  setday            = setday.replace(srch, "");	  
	    	 
	  dayAcontent       = setmydate(dayAcontent, setday);    	  	  
	  monthAcontent     = setmymonthoryear(monthAcontent,currentmonth);	  
	  
	  var dayA          = (dayAcontent.selectedIndex)+1;
	  var monthA        = (monthAcontent.selectedIndex)+1;

	  yearAcontent.options[0].value  = currentyear;	     
          var hourA         = (hourAcontent.selectedIndex)+1;	
	  var hourBtest     = (hourBcontent.selectedIndex)+1;	     
		
	  var dB, yB, mB;
          if (caldateb.value) 
	  {  	  	     
	      var srchb          = RegExp("^0+");
	      dB                 = arrayb[2].replace(srchb,"");
	      yB                 = arrayb[0];
               var srch           = RegExp("^0+");
	      mB                 = arrayb[1].replace(srch,"");
	      if  (string == "changetime")
	      {		  
		 hourB     = (hourBcontent.selectedIndex)+1;
	      }
	    	     

	   }
	   else
	   {
	       //convert cal date to julian date
	       var JD = cal_to_jd('CE',currentyear,monthA, dayA,hourA,'0','0');
	       //default new JD
	          var newJD = Number(JD)+2;
	       //convert newJD to ymd 
	       arrayQ    = jd_to_cal(newJD);
	       yB    = arrayQ[0];
	       mB    = arrayQ[1];
	       dB    = arrayQ[2];
	       var hourB = arrayQ[3];
           }	       
         
	   caldateb.value      = yB+ "-" + zeropad(mB) + "-" + zeropad(dB);
	   var D = formfoo.elements["dayB"];
	   D.options[dB-1].selected  = true;
	   hourBcontent.options[hourB-1].selected  = true;
	   monthBcontent.options[mB-1].selected    = true;
	   yearBcontent.options[0].value = yB;
	   //determine  user's time zone offset     
	   arrayzone      =  determinezoneoffset();
	   var zadjust    =  document.getElementById("zoneadjust");
	   zadjust.value  =  arrayzone[0]+","+arrayzone[1]+","+arrayzone[2];	      	       
          
}
        
function evalform()
{	 	  		  
           var divB = document.getElementById('datedivB');
	   
           var test = divB.attributes[0].nodeValue;
	   
	   
	   datehandler("changetime");
	   return true;	   
}  	
function load_dates()
{      
	      //sets the default date values in divA 
	      // default values seen by user on form
	       document.formfoo.reset();
	       
	       var caldate         = document.getElementById("DPC_edit1_YYYY-MM-DD");	      
	       var d               = new Date();
	       var current_date    = d.getDate();
	       var m_names         = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep","Oct", "Nov", "Dec");
	       var currentyear     = d.getFullYear();
	      	      	       
	       var currentmonth    = d.getMonth();
	       currentmonth        = Number(currentmonth)+1;

	       //O pad m,d
	       var Currentmonth    = zeropad(currentmonth);
	       var Currentdate     = zeropad(current_date);
	       caldate.value       = currentyear + "-" + Currentmonth + "-" + Currentdate;
	       	       
	       var currentday      = d.getDay();
	       var c_month         = m_names[currentmonth];	      	     	      
	       var current_hour    = d.getHours(); 	      
	       var hour_plus_one   = Number(current_hour)+1;
	       var dayAcontent     = document.getElementById("dayA");
	       var monthAcontent   = document.getElementById("monthA");
	       var yearAcontent    = document.getElementById("yearA");
	       var hourAcontent    = document.getElementById("hourA");
	       var zoneAcontent    = document.getElementById("zone");
              //set current day
	      dayAcontent          = setmydate(dayAcontent,current_date);
	     
              monthAcontent        = setmymonthoryear(monthAcontent,c_month);
              yearAcontent         = setmymonthoryear(yearAcontent, currentyear);
	      
	   	      
	      //set current hour + 1
	      hourAcontent   = setmydate(hourAcontent,hour_plus_one);
	      


	      //set default time zone
	       var zonename = new Array("GMT","","","","","EST","CST","MST","PST");
               var zonenameb = new Array("GMT","","","","EDT","CDT","MDT","PDT");

	      arrayzone      =  determinezoneoffset();
	      var julytz     =  arrayzone[0];
	      var jantz      =  arrayzone[1];
	      var currenttz   =  arrayzone[2]; 
	      var userzonename;	
	                  
	      if ((currenttz == julytz) && (julytz < jantz))
	      {
	         userzonename = zonenameb[Number(currenttz)];
		    
              }	    
	      else 
	      {
	         userzonename = zonename[Number(currenttz)];             
              }
	      
	      for (i=0; i<zoneAcontent.options.length;i++)
	      {
	          var zone  = zoneAcontent.options[i].value;
		  if (zone == userzonename)
		  {		      
		      zoneAcontent.options[i].selected = true;
		  }

	      }
	      
	      //set default priority (set to 3)
	      setpriority(document);


}	      
