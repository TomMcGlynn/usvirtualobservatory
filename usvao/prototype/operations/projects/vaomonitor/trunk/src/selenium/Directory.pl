#!/usr/bin/perl -w
#
#
# Directory test using Selenium
#
use strict;
use warnings;
{  
    $sel->click("link=Directory");
    $sel->wait_for_page_to_load("30000");
    $sel->click("sterm");
    $sel->type("sterm", "mast stsci appp");
    $sel->click(".submit");
    $sel->pause("6000");
    $sel->click("link=Full Record");
    $sel->wait_for_page_to_load("30000");

}
