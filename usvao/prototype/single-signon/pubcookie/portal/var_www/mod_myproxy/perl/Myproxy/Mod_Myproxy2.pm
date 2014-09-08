package Myproxy::Mod_Myproxy2;

# To Do for release:
#   - set GLOBUS_LOCATION, MYPROXY_SERVER to undef
#   - redirect myproxy output to /dev/null
#   - delete this comment

use strict;
use warnings;
  
use CGI::Cookie ();
use Apache2::Log ();
use Apache2::RequestRec ();
use APR::Table ();

use Apache2::Const -compile => qw(OK);

use constant GLOBUS_LOCATION => "/usr/local/globus";
use constant MYPROXY_SERVER => "nvologin.ncsa.uiuc.edu";
#use constant MYPROXY_EXTRA_PARAMS => "-p 7513";
use constant MYPROXY_EXTRA_PARAMS => ""; # "-p 7513";
# where we will put myproxy-logon output -- change to a log file for debugging
#use constant MYPROXY_LOG_FILE => "/dev/null";
use constant MYPROXY_LOG_FILE => "/tmp/myproxy_out";

# use constant CRED_STORE_DIR => "/var/mod_myproxy";
use constant CRED_STORE_DIR => "/tmp/mod_myproxy";

# environment/HTTP constants
use constant X509_USER_PROXY => "X509_USER_PROXY";

# for more debugging information, uncomment $r->log->notice in debug()
# subroutine below; results will most likely show up in Apache's
# ssl_error_log

my $r;         # the current request
my $user;      # the username of the current user
my $cred_path; # where to store the credentials

sub handler {
    # 1. initialize all package variables (for semi-OO'ness)

    # Note on Apache threading and object instantiation: each Apache
    # sub-process will have an instance of this class which it will
    # reuse each time it handles an HTTP request.  So we can count on
    # being single-threaded for the duration of the call to handler(),
    # but we have to be *very thorough* about initialization/cleanup
    # because otherwise we will have leftover values from the previous
    # call to handler().  Be sure to update clear_state() whenever you
    # create a new instance variable.

    system "touch /tmp/touched";

    $r = shift; # ($package, $r) = $_;
    $user = $r->user();
    if (defined($user) && $user =~ m/^\W/) {
	# unfortunately, if just removing non-alphanumeric characters
	# from $user could have unintended consequences -- someone
	# with the username joe-user, for example, could impersonate
	# joeuser
	notice($r, "Non-alphanumeric characters in username ($user) "
	       . "are not supported.");
	$user = undef;
    }

    if (defined($user)) {
	$cred_path = CRED_STORE_DIR . "/" . $user;
    } else {
	$cred_path = undef;
    }

    # 1. if granting cookie is present, use it to get a credential
    my %cookies = CGI::Cookie->fetch($r);
    my $cookies = %cookies;
    if (!defined($user)) {
	debug($r, "No username found; couldn't attempt myproxy-logon");
    } elsif (!defined($cookies)) {
	debug($r, "No cookies found; couldn't do myproxy-logon");
    } else {
	my $granting_cookie = $cookies{"pubcookie_g"};
	if (!defined($granting_cookie)) {
	    debug($r, "No granting cookie; not attempting myproxy-logon");
	} else {
	    my $notes = $r->notes();
	    my $granting_plain = $notes->get("plain_pubcookie_g");
	    notice($r, "Unencrypted granting cookie: $granting_plain");
	    handle_login($granting_cookie, $granting_plain);
	}
    }

    # 2. Set X509_USER_PROXY, if we know what it should be
    if (defined($cred_path)) {
	if (! -e $cred_path) {
	    notice($r, "Expected to see X509 credential at $cred_path, "
		   . "but file does not exist.");
	} else {
	    # If the file exists, let's hope it's actually a
	    # credential -- if it isn't, then something is probably
	    # configured wrong.  For example, the credential storage
	    # directory may be written to by another application.
	    set_x509_env($cred_path);
	}
    }

    # don't leave any variables set
    clear_state();

    return Apache2::Const::OK;
}

sub handle_login {
    my $granting_cookie = $_[0];
    my $granting_plain = $_[1];

    # ensure existence of credential storage directory
    my $mkdir_result = mkdir_check_perm(CRED_STORE_DIR);
    if (!defined($mkdir_result)) { # did it fail?
	$cred_path = undef;
	return undef;
    }

    # 1. get granting cookie
    my $pubcookie_g = $granting_cookie->value();
    $pubcookie_g = cleanup_cookie($pubcookie_g);
    $granting_plain = cleanup_cookie($granting_plain);
    debug($r, "Granting cookie value = $pubcookie_g");
    debug($r, "Plain granting value = $granting_plain");

    # pre-2. Set up environment
    $ENV{"GLOBUS_LOCATION"} = GLOBUS_LOCATION;
    $ENV{"LD_LIBRARY_PATH"} = GLOBUS_LOCATION . "/lib";
    $ENV{"DYLD_LIBRARY_PATH"} = $ENV{"LD_LIBRARY_PATH"};
    $ENV{"LIBPATH"} = GLOBUS_LOCATION . "/lib:/usr/lib:/lib";
    $ENV{"SHLIBPATH"} = GLOBUS_LOCATION . "/lib";
    $ENV{"SASL_PATH"} = GLOBUS_LOCATION . "/lib/sasl";

    # 2. call myproxy-logon
    system("touch \"$cred_path.touched\"");
    debug($r, `whoami`);
    my $myproxy_command 
	= "| " . GLOBUS_LOCATION . "/bin/myproxy-logon"
	. " -s " . MYPROXY_SERVER 
	. ' -l "' . $user . '" --stdin_pass'
	. ' -o "' . $cred_path . '"'
#	. " -k pubcookie"
	. " " . MYPROXY_EXTRA_PARAMS
#	. ' 2>&1 >> "' . MYPROXY_LOG_FILE . '"'; # send stdout, stderr to log
	. ' 2>> "' . MYPROXY_LOG_FILE . '"' # send stderr to log
	. ' >> "' . MYPROXY_LOG_FILE . '"'; # send stdout to log
    debug($r, "Myproxy command: $myproxy_command");
    my $opened = open(myproxy_handle, $myproxy_command);
    if (!$opened) {
	return notice($r, "Unable to run myproxy-logon: $!");
    }

    # 3. pass granting cookie to myproxy-logon as password
    my $pwcookie = undef;
    if (!defined($granting_plain)) {
	debug($r, "No plaintext granting cookie; using encrypted cookie as password");
        $pwcookie = $pubcookie_g;
    } else {
	debug($r, "Sending plaintext granting cookie as password to Myproxy");
        $pwcookie = $granting_plain;
    }
    print myproxy_handle "$pwcookie";
    debug($r, "Sent cookie as password to Myproxy");
    # signal end of input so that myproxy_logon can exit
    my $closed_success = close(myproxy_handle);
    debug($r, "Closed? " 
	  . ($closed_success ? "yes" : "no") 
	  . " ($closed_success)");
    my $myproxy_exit = $?; # retval stored in $? by close()
    debug($r, "Exit value = $myproxy_exit");
    if ($myproxy_exit != 0) {
	notice($r, "myproxy-logon failed -- see " . MYPROXY_LOG_FILE);
    }
    else {
	debug($r, "Stored MyProxy credential for $user in $cred_path");
    }

    # 4. cache the granting cookie
    if (! open(PCG, ">$cred_path.pcg")) {
        notice($r, "failed to open $cred_path.pcg");
    }
    else {
        print PCG $pwcookie;
        close(PCG);
    }
}

# set the X509_USER_PROXY environment variable
sub set_x509_env {
    my $cred_path = $_[0];

    my $sub_env = $r->subprocess_env();
    my $env_x509 = $sub_env->get(X509_USER_PROXY);
    if (defined($env_x509)) {
	notice($r, "X509_USER_PROXY already defined: $env_x509");
    } else {
	# don't need to set REMOTE_USER -- pubcookie already does it
	# $sub_env->add("REMOTE_USER", $user);
	$sub_env->add(X509_USER_PROXY, $cred_path);
	# integration with mod_proxy_ajp and mod_jk
	$sub_env->add("AJP_" . X509_USER_PROXY, $cred_path);
	$sub_env->add("AJP" . X509_USER_PROXY, $cred_path);
    }
}

# create a directory, if it doesn't exist; if it does exist, check
# that it is read/writable only by its owner; if the permissions are
# wrong, complain and fail (return undef)
#
# note that the credential file permissions will always be 0600,
# thanks to myproxy-logon
sub mkdir_check_perm {
    my $path = $_[0];

    my $mkdir_result = mkdir($path, 0700);
    my $mkdir_error = $!;
    debug($r, "mkdir result = $mkdir_result ($mkdir_error)");

    if (!(-d $path)) {
	notice($r, "unable to create " . CRED_STORE_DIR 
	       . ": $mkdir_error ($mkdir_result)");
	# would rather return DONE here, but getting language errors
	return undef;
    }

    # check directory permissions
    my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,
	$atime,$mtime,$ctime,$blksize,$blocks)
	= stat($path);
    # note: we don't check ownership -- if it's wrong, we simply won't
    # be able to write to the directory
    $mode = $mode & 07777; # mask out file type
    debug($r, "file mode of $path = $mode");
    if ($mode != 0700) {
	notice($r, "directory $path has wrong permissions; it should be "
	       . "accessible only to its owner, which should match the "
	       . "uid of the web server process");
	return undef;
    }

    return 1;
}

# call this at the end of request-handling to clear instance variables
sub clear_state {
    $r = undef;
    $user = undef;
    $cred_path = undef;
}

# log something, but only in debug mode
# takes a request object and a message
sub debug {
    my $r = $_[0];
    my $msg = $_[1];
    $r->log->notice("mod_myproxy: $msg");
}

# log something regardless of debug mode
# takes a request object and a message
sub notice {
    my $r = $_[0];
    my $msg = $_[1];
    $r->log->notice("mod_myproxy: $msg");
}

# restore Base64 characters that were HTTP-ified
sub cleanup_cookie {
    my $value = $_[0];
    $value =~ s/%20/ /g;  # space was escaped ...
    $value =~ s/ /+/g;    # ... after plus had been turned into a space
    $value =~ s|%2F|/|g;  # slash was escaped
    $value =~ s/%3D/=/g;  # equals was escaped
    return $value;
}
1;
