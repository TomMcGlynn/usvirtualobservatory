#
#
# The notificcode uses a startup file that
# is not included with this installation. This file
# therefore describes what the content needs to be
# in order to run the system. The user should feel
# free to modify this file as he sees fit.
#
#
#####################################
#   Global variable definitions     #
#####################################
# $::cssA           - a css file
# $::cssB           - a css file
# $::valcode        -referece to vaomonitor home page
# $::pw             -vaomonitor db pw 
# $::opsdb          -vao operations db
# $::opspw          -operations db pw
# $::user           -username associated with connection
# $::notficationdir -notification (top level) directory
# $::server         -servername. This is only needed when for example, you
#                    need to reference two different instances of the notification service.
#                    E.g. you might have a development and production version of
#                    the code at "$::server.<some domain name>" where $::server is different   
#                    for your production and development areas
# $::login          -pointer to login script (url);E.g.   "http://heasarcdev.gsfc.nasa.gov/vo/notification/login.pl";
# $::displaynote    -pointer to displaynote script; E.g.http://heasarcdev.gsfc.nasa.gov/vo/notification/displaynote.pl
# $::composenote    -pointer to composenote script;E.g. http://heasarcdev.gsfc.nasa.gov/vo/notification/composenote.pl";  
# $::tmp            -path to Python VAOSessions directory; E.g./tmp/vo/notification/vaologin/var/VAOsessions"; 
# $::gpghome        -path to gpg tool home directory;E.g. /www/server/vo/notification/.gnupg";




package data::startup;
{
   
    $::cssA            = "./css/validation.css";
    $::cssB            = "./css/styles.css";
    $::valcode         = "./vaodb.pl";	  
    $::pw              = '<vaomonitor db pw>';
    $::db              = "vaomonitor";
    $::opsdb           = "vao_operations";
    $::opspw           = '<vao operations pw>';
    $::user            = '<username associated with connection>';
    $::notificationdir = "<notification project top level dir>";
    $::server          = "heasarc";
    $::login           = "http://heasarcdev.gsfc.nasa.gov/vo/notification/login.pl";
    $::displaynote     = "http://heasarcdev.gsfc.nasa.gov/vo/notification/displaynote.pl";
    $::composenote     = "http://heasarcdev.gsfc.nasa.gov/vo/notification/composenote.pl";  
    $::tmp             = "/tmp/vo/notification/vaologin/var/VAOsessions"; 
    $::gpghome         = "/www/server/vo/notification/.gnupg";


     
    BEGIN
    {
	
	my $www = 'www';
	@::libs = ("<path to perl installation>",
		   "<path to perl libraries>",
	           "<notification system top level dir>",
                  );
        unshift(@INC,@::libs);
        #path to python,python vaologin code,gpg,curl and associated files 
        #There should be a python path, ld lib path and path.
        $ENV{'PYTHONPATH'} = "<path>/python"
        .":<path>/python/lib/"
        .":<path>/python/bin/"
        .":<path>/vaologin/cacerts"; #vaologin cacerts dir
         $ENV{'LD_LIBRARY_PATH'} =  "<path>/vaologin/bin/vaoopenid" #vaoopenid script
        .":<path>/curl/"
        .":<path>/python-openid/lib/python" #path to python openid
        .":<path>/pycurl" #path to pycurl 
        .":<path>/python/lib/python2.7/site-packages/" #another python path
        .":<path>/vaologin" #vaologin home
        .":<path>/gpg" #path to gpg
        .":<path>/notification (dir containing vaologin,pycurl,etc...presumably in parallel"; 
        $ENV{'PATH'} = "<path>/vaologin" #vaologin home
                      .":<path>/notification (dir containing vaologin,pycurl,etc...presumably in parallel"; 
      }

}
1;
