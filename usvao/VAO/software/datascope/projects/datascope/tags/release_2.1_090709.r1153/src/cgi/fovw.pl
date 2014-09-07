#!/usr1/local/bin/perl
#
# Wrap the field of view image in appropriate HTML.
# This script generates an image whose source is a call
# to the fov.pl script.
require  "./Common.pm";
run("net.ivoa.datascope.FOVWrapper");
