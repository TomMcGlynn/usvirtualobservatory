#!/usr1/local/bin/perl

#######################################################################
# Perl script to run selemium tests to verify that the NVO website is
# up and running.
#######################################################################
use strict;

my $status = 0;
my $test_home = "/software/jira/usvo/projects/voview_test";
my @tests = ("net.ivoa.voview.tests.AllRenderTests");

# Set enviroment variables for selenium.  Selenium requires that the
# actual browser executable (not a script) be in PATH.

$ENV{PATH} = "/usr1/local/firefox-3:$ENV{PATH}";

$ENV{LD_LIBRARY_PATH} = "/usr1/local/firefox-3.6:$ENV{LD_LIBRARY_PATH}";

$ENV{CLASSPATH} = "$test_home/java:$test_home/lib/selenium-java-2.0b3.jar:$test_home/lib/junit.jar";

# Look for tests in the java directory, build and make a list.

chdir "$test_home/java/net/ivoa/voview/tests" or die "No directory $test_home/java .";
my @test_files = glob('*.java');
die "No tests found." unless @test_files;

print "Compiling ...\n";
if( system("javac",@test_files) ){
  $status = 1;
}


# Start an X server with a virtual frame buffer, so firefox has something to connect to
# unless a server has been specified on the command line.

my $xpid;

if( $ARGV[0] eq '-server' ){
	if( $ARGV[1] ){
  		$ENV{DISPLAY} = $ARGV[1];
	}

}else{

  $ENV{DISPLAY} = ":1";
  unless( $xpid = fork() ){

    # Child process

    my $command = "/usr/bin/Xvfb $ENV{DISPLAY} -screen 1 1280x1024x8 2>&1";

    exec $command;
    exit 0;
  }
}

# Now start the selenium server

my $selid;

unless( $selid = fork() ){

  # Child process

  my $command = "java -jar $test_home/lib/selenium-server-standalone-2.0b3.jar";

  exec $command;
  exit 0;
}

# Make sure servers have a chance to start

sleep(2);

# Everything is set up, now we can run the actual tests.

foreach my $testname (@tests){

  print "Running $testname.\n";

  my $test_out = `java $testname 2>&1`;

  if($?){
    print STDERR "Unable to execute command $testname, return code is $?.\n";
    $status = 1;
  }

  # Parse the output to see if there were any failures

  unless( $test_out =~ /OK \(1 test\)/ ){
    if( $test_out =~ /Tests run: +(\d+), +Failures: (\d+), +Errors: (\d+)/ ){
      my ($num_tests, $num_fails, $num_errors) = ($1, $2, $3);
      if( $num_tests != 1 || $num_fails != 0 || $num_errors != 0 ){
	print STDERR "Test $testname failed.  Output: \n$test_out\n";
	$status = 1;
      }
    }else{
      print STDERR "Unable to parse report from test $testname:\n$test_out\n";
      $status = 1;
    }
  }

}

# kill the servers

## print STDERR "Server ids: $xpid, $selid\n";

unless( kill(15, $selid) ){
  print STDERR "Unable to send kill signal to selenium server, pid ", $selid, "\n";
  $status = 1;
}

if( $xpid && ! kill(15, $xpid) ){
  print STDERR "Unable to send kill signal to Xvfb server, pid ", $xpid, "\n";
  $status = 1;
}

exit $status;
