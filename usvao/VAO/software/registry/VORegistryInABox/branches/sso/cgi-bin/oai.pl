#!/usr/bin/perl -w -I.
use strict;

#  ----------------------------------------------------------------------
# | Open Archives Initiative Harvesting Framework version 2.0            |
# | Hussein Suleman                                                      |
# | June 2002                                                            |
#  ----------------------------------------------------------------------
# |  Virginia Polytechnic Institute and State University                 |
# |  Department of Computer Science                                      |
# |  Digital Library Research Laboratory                                 |
#  ----------------------------------------------------------------------

# Installation :
#   copy all files into a directory from which you can execute scripts
#
# Testing :
#   use the repository explorer (http://purl.org/net/oai_explorer)
#   to test your interface

BEGIN {
    use FindBin;
#    $main::home = '.';
#    if ($ENV{'VOPUB_HOME'} ne '') {
#        $main::home = $ENV{'VOPUB_HOME'};
#    } else {
#        $main::home = '@INSTALLDIR@';
#        $main::home = "$FindBin::Bin/.." if ($main::home =~ /^@/);
#    }
#    $main::home = "." if ($main::home eq '');
#    print STDERR "Using home: $main::home\n";
#    use lib $main::home."/lib/perl";

    use lib "$ENV{'VOPUB_HOME'}/lib/perl";
    use lib '@INSTALLDIR@/lib/perl';
    use lib "$FindBin::Bin/../lib/perl";

    use VORegInABox::FileStoreOAI;
}

sub main
{
   my $home = '.';
   if ($ENV{'VOPUB_HOME'} ne '') {
       $home = $ENV{'VOPUB_HOME'};
   } else {
       $home = '@INSTALLDIR@';
       $home = "$FindBin::Bin/.." if ($home =~ /^@/);
   }
   $home = "." if ($home eq '');
   print STDERR "Using home: $home\n";

   chdir "$home" if ($home ne '.');
   my $OAI = new VORegInABox::FileStoreOAI ("conf/config.xml");
   $OAI->Run;
   $OAI->dispose;
}

main;
