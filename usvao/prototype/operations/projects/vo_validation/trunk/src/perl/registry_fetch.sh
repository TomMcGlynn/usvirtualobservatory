#!/usr1/local/bin/tcsh
#
#
#
#
set a = '/web_chroot/.www_mountpnt/www/htdocs/vo/validation/perl';
$a/get_datatypes.pl
$a/get_datatypes.pl -d
$a/parse_registry_resources.pl
$a/parse_registry_resources.pl -dep
$a/get_publishing_registries.pl 
$a/update_registry_file.pl 
$a/updatedb.pl


exit(1);
