###
#
# datascope test using selenium
#
#

use strict;
use warnings;
{
    $sel->set_speed("6000");     
    $sel->wait_for_page_to_load("40000");
    $sel->click("link=DataScope");
    $sel->wait_for_page_to_load("30000");
    $sel->type("position", "m64");
    $sel->capture_screenshot("./P.png");
    $sel->click("skipcache");
    $sel->click("submit");
    #$sel->wait_for_pop_up("DSW","30000");
    sleep(16);
    $sel->capture_screenshot("./U.png");  
    $sel->select_window("title=DataScope query:m64");
    sleep(19);
    $sel->click("p__matches");     
    sleep(10);    
    $sel->capture_screenshot("./nn.png");
    $sel->is_text_present("Simbad");
    
}
