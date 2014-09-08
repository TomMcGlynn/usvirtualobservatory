#
# Vaomonitor page top
#
#
#

package HTML::Top;

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
    
         <script type = "text/javascript" language = "JavaScript">
          function run_test(x)
          {
       
     
            window.location =  x + '&getresponse=yes';
          }
         function test(x)
          {
            window.location = x;
       
          }

         </script>
      </HEAD> 
   EOF

}
