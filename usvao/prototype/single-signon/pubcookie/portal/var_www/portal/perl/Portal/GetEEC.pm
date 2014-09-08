package Portal::GetEEC;

use strict;
use warnings;

use CGI qw(:standard);  
use CGI::Cookie ();
use Apache2::Log ();
use Apache2::RequestRec ();
use APR::Table ();

use Apache2::Const -compile => qw(OK SERVER_ERROR);

use constant MYPROXY_SERVER => "sso.us-vo.org";
#use constant MYPROXY_SERVER_DN => "/C=US/O=National Virtual Observatory/OU=Certificate Authorities/CN=sso.us-vo.org";

use constant MYPROXY_EXTRA_PARAMS => "-p 7513 "; # this is a custom server for 
                                                 # this service

# where we will put myproxy-logon output -- change to a log file for debugging
#use constant MYPROXY_LOG_FILE => "/dev/null";
use constant MYPROXY_LOG_FILE => "/tmp/myproxy_eec_out";

# use constant CRED_STORE_DIR => "/var/mod_myproxy";
use constant CRED_STORE_DIR => "/tmp/mod_myproxy";

# for more debugging information, uncomment $r->log->notice in debug()
# subroutine below; results will most likely show up in Apache's
# ssl_error_log

my $r;         # the current request
my $user;      # the username of the current user
my $cred_path; # where to store the credentials
my $query;     # the CGI query object

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

    $r = shift; # ($package, $r) = $_;
    $user = $r->user();
    if (defined($user) && $user =~ m/^\W/) {
	# unfortunately, if just removing non-alphanumeric characters
	# from $user could have unintended consequences -- someone
	# with the username joe-user, for example, could impersonate
	# joeuser
	return sys_error($r, "Non-alphanumeric characters in username ($user) "
                         . "are not supported.");
    }

    if (defined($user)) {
	$cred_path = CRED_STORE_DIR . "/$user.4pk12$$";
    } else {
	$cred_path = undef;
    }

    $query = new CGI();
    my $lifehours = $query->param('lifehours');
    if ($lifehours !~ /^\d+/) {
        # only allow whole number hours
        notice($r, "called with non-integer lifetime");
        $lifehours = 12;
    }
    my $format = $query->param('format');
    my $pkcskey = $query->param('pkcskey');
    return usr_error($r, "Bad user key: $pkcskey, $lifehours",
                     "A packing key containing no spaces or illegal " .
                     "characters (i.e. ';', '(', ')', and '&') is required")
        if ($pkcskey =~ /^\s*$/ || $pkcskey =~ /[;\(\)\s]/);

    # 1. if granting cookie is present, use it to get a credential
    my %cookies = CGI::Cookie->fetch($r);
    my $cookies = %cookies;
    debug($r, "Available cookies: ".join(', ', keys(%cookies)));
    if (!defined($user)) {
	return sys_error($r, "Username is needed but not set");
    } else {
	my $granting_cookie = $cookies{"pubcookie_g"};
        my $notes = $r->notes();
        my $granting_plain = $notes->get("plain_pubcookie_g");
	if (!defined($granting_plain)) {
            $granting_plain = get_cached_gcookie($r);
	    return sys_error($r, "No granting cookie available for $user; ".
                             "aborting.")
                if (! defined($granting_plain))
	} 
#        debug($r, "Unencrypted granting cookie: $granting_plain");
        handle_login($granting_cookie, $granting_plain, $lifehours);
    }

    # 2. 
    if (defined($cred_path)) {
	if (! -e $cred_path) {
	    return sys_error($r, "Expected to see X509 credential at "
                             . "$cred_path, but file does not exist.");
	} 
	my $out;
	my $cmd;
	if ($format =~ m/PKCS12/) {
	    $out = CRED_STORE_DIR . "/$user.p12_$$";
	    $cmd = "openssl pkcs12 -export -in $cred_path -out $out" . 
		" -passout pass:$pkcskey";
	    $r->content_type("application/x-pkcs12\n\n");
	} elsif ($format =~ m/PEM/) {
	    $out = CRED_STORE_DIR . "/$user.pem_$$";
	    $cmd = "openssl rsa -des3 -in $cred_path -out $out" . 
		" -passout pass:$pkcskey";
	    # Which of these is right?  Any of them?
	    $r->content_type("application/x-pem-file\n\n");
#	    $r->content_type("application/x-pem-key\n\n");
#	    $r->content_type("application/pem-keys\n\n");
	} else {
	    return sys_error($r, "unsupported format requested: " . $format);
	}
	debug($r, $cmd);
	my $cout = `$cmd 2>&1`;
	if ($?) {
	    chomp $cout;
	    if ($cout =~ /^Usage:/) {
		$cout = "Usage problem";
	    }
	    $cout .= ": $cmd";
	    return sys_error($r, "openssl failed to package output: $cout");
	}
	if ($format =~ m/PEM/) {
	    # combine to make new pem-encoded credential
	    #   (1) certificate from original (unencrypted) credential
	    #   (2) encrypted private key
	    my $new_out = CRED_STORE_DIR . "/$user.pem2_$$";
#	    debug($r, "writing encrypted PEM to $new_out; old is $out");
	    open(NEW_PEM, ">" . $new_out);
	    open(ORIG_PEM, $cred_path);
	    my $line = <ORIG_PEM>;
	    debug($r, "first line of $cred_path: $line");
	    until ($line =~ m/BEGIN CERTIFICATE/) {
		$line = <ORIG_PEM>;
#		debug($r, "looking for beginning of cert: $line");
	    }
	    until ($line =~ m/BEGIN RSA PRIVATE KEY/) {
		print NEW_PEM $line;
#		debug($r, "looking for beginning of private key: $line");
		$line = <ORIG_PEM>;
	    }
	    close(NEW_PEM);
	    close(ORIG_PEM);
	    `cat $out >> $new_out`;
	    unlink($out);
	    $out = $new_out;
	}
	unlink($cred_path);
	$r->sendfile($out);
	$r->rflush();
	unlink($out);
    }

    # don't leave any variables set
    clear_state();

    return Apache2::Const::OK;
}

sub sys_error {
    my $r = shift;
    my $msg = shift;
    notice($r, "System error: $msg");
    clear_state();
    return Apache2::Const::SERVER_ERROR;
}

sub usr_error {
    my $r = shift;
    my $msg = shift;
    my $explanation = shift;
    notice($r, "User error: $msg");
    
    print $query->header
        , $query->start_html('User Service Error'),
        $query->h1('User Service Error'),
        "\n<p>An error was encountered in your inputs: </p>\n\n",
        "<blockquote>$msg: $explanation</blockquote>\n\n",
        "<p>Please use your back button to correct your inputs ",
        "and try again.</p>\n",
        $query->end_html;
    clear_state();
    return Apache2::Const::OK;
}

sub handle_login {
    my $granting_cookie = $_[0];
    my $granting_plain = $_[1];
    my $lifehours = $_[2];

    # ensure existence of credential storage directory
    my $mkdir_result = mkdir_check_perm(CRED_STORE_DIR);
    if (!defined($mkdir_result)) { # did it fail?
	$cred_path = undef;
	return undef;
    }

    # 1. get granting cookie
    my $pwcookie = cleanup_cookie($granting_plain);
    my $cryptstate = "plain";
    if (!defined($pwcookie)) {
        my $pubcookie_g = $granting_cookie->value();
        $pubcookie_g = cleanup_cookie($pubcookie_g);
        $cryptstate = "encrypted";
    }
#    debug($r, "Using $cryptstate granting value = $pwcookie");
    return undef if (!defined($pwcookie));

    # 2. call myproxy-logon
#    system("touch \"$cred_path.touched\"");
#    debug($r, `whoami`);
    my $globus_location = $ENV{"GLOBUS_LOCATION"};
#    $ENV{"MYPROXY_SERVER_DN"} = MYPROXY_SERVER_DN;
    my $myproxy_command 
	= "| " . $globus_location . "/bin/myproxy-logon"
#	= "| " . $globus_location . "/bin/myproxy-indirect"
	. " -s " . MYPROXY_SERVER 
	. ' -l "' . $user . '" --stdin_pass'
	. ' -o "' . $cred_path . '"'
        . ' -t ' . $lifehours
	. " " . MYPROXY_EXTRA_PARAMS
	. ' 2>> "' . MYPROXY_LOG_FILE . '"' # send stderr to log
	. ' >> "' . MYPROXY_LOG_FILE . '"'; # send stdout to log
    debug($r, "Myproxy command: $myproxy_command");
    my $opened = open(myproxy_handle, $myproxy_command);
    if (!$opened) {
	return notice($r, "Unable to run myproxy-logon: $!");
    }

    # 3. pass granting cookie to myproxy-logon as password
    print myproxy_handle "$pwcookie";
    print myproxy_handle 0;
    print myproxy_handle 4;
    debug($r, "Sent cookie as password to Myproxy");
    # signal end of input so that myproxy_logon can exit
    my $closed_success = close(myproxy_handle);
#    debug($r, "Closed? " 
#	  . ($closed_success ? "yes" : "no") 
#	  . " ($closed_success)");
    my $myproxy_exit = $?; # retval stored in $? by close()
#    debug($r, "Exit value = $myproxy_exit");
    if ($myproxy_exit != 0) {
	notice($r, "myproxy-logon failed -- see " . MYPROXY_LOG_FILE);
    }
    else {
	debug($r, "Stored MyProxy credential for $user in $cred_path");
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
#    debug($r, "mkdir result = $mkdir_result ($mkdir_error)");

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
#    debug($r, "file mode of $path = $mode");
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
    $r->log->notice("geteec: $msg");
}

# log something regardless of debug mode
# takes a request object and a message
sub notice {
    my $r = $_[0];
    my $msg = $_[1];
    $r->log->notice("geteec: $msg");
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

sub get_cached_gcookie {
    my $r = shift;
    my $cookpath = CRED_STORE_DIR . "/$user.pcg";
    if (!open(PCG, "$cookpath")) {
        notice($r, "$cookpath: unable to open: $!");
        return undef;
    }
    my $out = <PCG>;
    close(PCG);
    return $out;
}

1;
