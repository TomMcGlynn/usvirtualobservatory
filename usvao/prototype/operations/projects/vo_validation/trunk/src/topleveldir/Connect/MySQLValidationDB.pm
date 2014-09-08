#
#
# Connection to MySql Validation db.
# Changes should be made here...
# 
#
#
#
package Connect::MySQLValidationDB;

use Exporter();
use DBI;
@ISA = qw(Exporter);
@EXPORT  = qw(vodb_connect);

sub vodb_connect
{    
   
    my $dbh;
	  $dbh = DBI->connect("DBI:mysql:host=asddb.gsfc.nasa.gov:database=validation:user=$::user:password=$::pw")
   #$dbh = DBI->connect("DBI:mysql:host=asddb.gsfc.nasa.gov:database=validation:user=webuser:password=webpwd")
	or die "$DBI->errstr\n";
    return $dbh;
}
1;
