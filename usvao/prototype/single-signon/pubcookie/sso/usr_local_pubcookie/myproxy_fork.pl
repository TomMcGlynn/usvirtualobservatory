#! /usr/bin/perl

# set up environment (change according to local configuration)
$GLOBUS_LOCATION = "/usr/local/globus";
$MYPROXY_SERVER = "nvologin.ncsa.uiuc.edu";
$LOG_STDOUT = "/tmp/myproxy_out";
$LOG_STDERR = "/tmp/myproxy_err";

# To run this script, use pubcookie's verify_fork mechanism, which
# calls an external executable and bases its authentication decision
# on the return value.  A return value of zero is interpreted as pass,
# and non-zero is interpreted as fail.  The config file will have a
# section like this:
#
#   basic_verifier: verify_fork
#   verify_exe: /usr/local/pubcookie/myproxy_fork.pl

# Note on debugging: as of pubcookie 3.3.0, writing anything to STDOUT
# or STDERR will trigger an "internal server error" in pubcookie's CGI
# script.  Instead, you can debug this script in two steps:
#
# 1. Write pubcookie's output to an external file, for example with
#    this shell script (which you can also run via verify_fork, by
#    substituting its name for myproxy_fork.pl in the config file):
#
#       #! /bin/sh
#       cat > /tmp/pubcookie_input
#
# 2. Add debugging output to this script and remove the redirection of
#    myproxy-logon's STDOUT and STDERR to /dev/null, so that you can
#    see what's going on.
#
# 3. Run this script outside of pubcookie
#
#       /usr/local/pubcookie/myproxy_fork.pl < /tmp/pubcookie_input

# read up to 1000 characters from STDIN
read(STDIN, $contents, 1000);
# read up to 1000 characters of a null-terminated string from the
# beginning of $contents
my ($username) = unpack("Z1000", $contents);

# skip past the first null, which terminates the username, and read up
# to 1000 more characters of null-terminated string from $contents
my ($password) = unpack("Z1000", substr($contents,length($username)+1));

# pass username and password to myproxy-logon
$myproxy_handle;
mkdir "/tmp/creds";
my $cred_location = "/tmp/creds/$username";
my $myproxy_command = "| $GLOBUS_LOCATION/bin/myproxy-logon -t 9 -s $MYPROXY_SERVER -l $username -o $cred_location --stdin_pass >> $LOG_STDOUT 2>> $LOG_STDERR";

# debugging
$stamp = &timestamp();
if (open(LOG, ">>$LOG_STDOUT")) {
    print LOG "pubcookie authentication: $stamp\n";
    print LOG "--- username = $username\n";
#    print LOG "--- password = $password\n";
#    print LOG "--- raw input = $contents\n";
    print LOG $myproxy_command, "\n";
    close(LOG)
}
if (open(LOG, ">>$LOG_STDERR")) {
    print LOG "pubcookie authentication: $stamp\n";
    close(LOG)
}

open(myproxy_handle, $myproxy_command)
    or die "Unable to run myproxy-logon: $!\n";

# debugging version, to be used outside of pubcookie (see above), so that you can see STDOUT and STDERR
#open(myproxy_handle, "| $GLOBUS_LOCATION/bin/myproxy-logon -v -s $MYPROXY_SERVER -l $username --stdin_pass")
#    or die "Unable to run myproxy-logon: $!\n";

print myproxy_handle "$password\n";

$closed_successfully = close(myproxy_handle);
$myproxy_exit = $?; # return value is stored in $? by close()

if ($myproxy_exit != 0) {
    # note: unfortunately, this message will not be visible in
    # Pubcookie's login web page; authentication will simply fail
    die "login failed ($myproxy_exit)";
}

# create unencrypted proxy based on login credential
$ENV{"X509_USER_CERT"} = $cred_location;
$ENV{"X509_USER_KEY"} = $cred_location;
$ENV{"PATH"} = $ENV{"PATH"}
    . ":" . $GLOBUS_LOCATION . "/bin"
    . ":" . $GLOBUS_LOCATION . "/sbin";
my $myproxy_init_cmd = "$GLOBUS_LOCATION/bin/myproxy-init -c 8 -k pubcookie -n -s $MYPROXY_SERVER -l $username  >> $LOG_STDOUT 2>> $LOG_STDERR";

if (open(LOG, ">>$LOG_STDOUT")) {
    print LOG $myproxy_init_cmd, "\n";
    close(LOG)
}

system($myproxy_init_cmd);

# success!
exit 0;

sub timestamp {
    my @t = localtime;
    $t[5] += 1900;
    $t[4]++;
    $t[4] = "0$t[4]" if ($t[4] < 10);
    $t[3] = "0$t[3]" if ($t[3] < 10);
    return "[$t[5]-$t[4]-$t[3] $t[2]:$t[1]:$t[0]] ";
}
