#!/usr1/local/bin/perl
#
# Convert VOTable to a simple table for display.
require  "./Common.pm";
run("net.ivoa.datascope.VOTFormatter", @ARGV);
