#
# This is a startup file. 
#
# This file contains urls and paths
# that can be altered by the user.
# However these are the default paths.
#
# If changes are made to this file,
# user must change the location of
# any code referenced by this file.
#
#
#
#####################################
#   Global variable definitions     #
#####################################
#
#
# $::cssA           - the css file containing classes for the validation software.
#
#


package data::startup;
{
   
    $::cssA            = "./css/validation.css";
    $::cssB            = "./css/styles.css";
    $::valcode         = "./vaodb.pl";

    #enter pw for your db 
    $::pw              = 
    $::db              = "vaomonitor";
    $::opsdb           = "vao_operations";

    #enter pw for your operations db
    $::opspw           =  
    $::user            = 'webuser';
    $::notificationdir = "vo/notification/";
    $::server          = "heasarc";
    $::hosts_file_val  = "/tmp.shared/vo/vaomonitor/vohostnames";

 
    BEGIN
    {
	
	my $www = 'www';
	#$www    =  'www.prod' if ( $ENV{SERVER_NAME} !~ /^heasarcdev(.*)/);
	@::libs = ("/$www/htdocs/cgi-bin/W3Browse/lib",
		   "/$www/htdocs/cgi-bin/lib/heasarc",
		   "/heasarc/src/misc/perl/lib",
	           "/$www/htdocs/vo/notification",
                   "/$www/htdocs/vo/monitor");
        unshift(@INC,@::libs);
	
    }

}
1;
