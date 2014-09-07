#!/usr1/local/bin/perl
#
# Render a datascope query page.
#
require  "./Common.pm";
$ENV{VOCLI_HOME} = '@VOCLIENT@';
run("net.ivoa.query.Querier");
