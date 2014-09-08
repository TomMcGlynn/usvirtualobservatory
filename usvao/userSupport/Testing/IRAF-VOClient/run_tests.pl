#!/usr/bin/perl

# A PERL test harness for IRAF testing
# should be run in the home directory
# of the tester, as follows:
#
# > perl /path/to/run_tests.pl 

use strict; 

my $DEBUG = 0;

my $user = $ENV{USER};
my $cl_test_script = "run_tests.cl";
my $cl_test_script_guts = <<CL; 
vao             
vaotest
test
reset tests = /home/$user/testcases/scripts/url/
test

reset tests = /home/$user/testcases/scripts/votable/
test

reset tests = /home/$user/testcases/scripts/samp/
test

reset tests = /home/$user/testcases/scripts/errorhandle/
test

#reset tests = /home/$user/testcases/scripts/dataquery/
#test

reset tests = /home/$user/testcases/scripts/voservices/
test

lo
CL

open (TEST_FILE, ">$cl_test_script");
print TEST_FILE $cl_test_script_guts;
close TEST_FILE;

print STDOUT "Run CL Tests \n";
open (OUTPUT, "cl < $cl_test_script |") or die "Can't run cl test script $!\n";

my $tests = 0;
my $test_fails = 0;
while (<OUTPUT>) {
   my $line = $_;
   print STDERR $_ if $DEBUG;
   next unless ($line =~ m/\[PASS\]/ or $line =~ m/\[FAIL\]/ or $line =~ m/Summary/);
   if ($line !~ m/Summary/) { $tests = $tests + 1; }
   if ($line =~ m/\[FAIL\]/) { $test_fails = $test_fails + 1; }
   print STDOUT $_ unless $DEBUG;
}
print STDOUT "CL Tests - Finished  $tests Total Tests  $test_fails Test Fails \n";

