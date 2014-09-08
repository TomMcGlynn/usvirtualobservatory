#!/usr/bin/perl -w
#
# Data Discovery Tool test using Selenium
#
#
#

use strict;
use warnings;
{

    $sel->set_speed("6000");
    
    $sel->click("//input[\@value='Launch']");
    $sel->wait_for_page_to_load("30000");

 
    $sel->open("http://vao.stsci.edu/portal/Mashup/Clients/Portal/DataDiscovery.html?useAV");
    $sel->select_window("Data Discovery");
    sleep(6);	


    #$sel->capture_screenshot("./selA.png");

    #$sel->type("triggerfield-1022-inputEl", "14 03 12.6 +54 20 56.9");
    #$sel->select_window("Data Discovery"); 
    #$sel->capture_screenshot("./selC.png"); 
    #$sel->click("button-1025-btnEl");	
    #sleep(10);
    #$sel->capture_screenshot("./selB.png");
    
    #is text present needs to be the last line 
    #$sel->is_text_present("CADC Image Search");

    
    
}
