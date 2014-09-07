#!/usr1/local/bin/perl
#
# Create the metadata file from a registry
# query.  This should be invoked periodically
# by a cron job.  The output file must
# also be copied to the operational environment.
# This must be run on the development node,
# since on the operational environment,
# the output directories are mounted read-only
#use lib "/www/htdocs/cgi-bin/vo/datascope";
use lib "@ABS_PATH@";
use  Common;
system("/usr1/local/bin/java","-Xmx500M","net.ivoa.registry.RegistrySearch");
