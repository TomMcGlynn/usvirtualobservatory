#
# Vaomonitor page top
#
#
#

package HTML::TopVAOStats;

use Exporter();

@ISA = qw (Exporter);
@EXPORT = qw (print_h);


sub print_h
{
     print  <<  "   EOF";  
<html> 
    <HEAD>
      <TITLE>VAO Monitor</TITLE>
        <LINK REL="StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css'
         TYPE="text/css">
         <LINK REL="StyleSheet" HREF='./css/styles.css' TYPE="text/css">
         <LINK REL="StyleSheet" HREF='./css/vao.css' TYPE="text/css"> 
	 <script language='JavaScript' type='text/javascript' src="js/julian.js"></script>
       <script language='JavaScript' type='text/javascript' src="js/vonotice.js"></script>

    
         <script type = "text/javascript" language = "JavaScript">
          
function run_test(x)
{
window.location =  x + '&getresponse=yes';

}
	 function run_convert()
          {
	      var caldate         = document.getElementById("date1");
	      var newcaldate   = document.getElementById("date2");
	      var val1  = newcaldate.innerHTML;
	      var  val    = caldate.innerHTML;
	      a = val1.split('-');
              array       = val.split('-'); 
	      var append  =  array[0];
	      var string  = array[1];
	      array1      = string.split('/'); 
	      
              var srchb          = RegExp("^0+");
              dB                 = array1[2].replace(srchb,"");
              yB                 = Number(array1[0]);
              var srch           = RegExp("^0+");
              mB                 = array1[1].replace(srch,"");
              mB     =           Number(mB);
              dB            =  Number(dB);
              
	   
              var JD = cal_to_jd('CE',yB,mB,dB,'0','0','0');
             
               //default new JD
	       var newJD = Number(JD)-1;
	      
               //convert newJD to ymd 
               arrayQ    = jd_to_cal(newJD);
	      yB      = arrayQ[0];
	      mB    = zeroPad(arrayQ[1],2);
	      dB    = zeroPad(arrayQ[2],2);
	      
	      var n  = append + "-"    + yB + "/" + mB + "/"  + dB; 
              var y  = a[0] + "-"    + yB + "/" + mB + "/"  + dB; 
	      
              document.getElementById("date1").innerHTML = n;
	      document.getElementById("date2").innerHTML = y;


	 
          }
          function zeroPad(num,count)
          {
            var numZeropad = num + '';
            while(numZeropad.length < count) {
            numZeropad = "0" + numZeropad;
           }
           return numZeropad;
          }

         function test(x)
          {
            window.location = x;
       
          }

         </script>
      </HEAD> 
   EOF

}
