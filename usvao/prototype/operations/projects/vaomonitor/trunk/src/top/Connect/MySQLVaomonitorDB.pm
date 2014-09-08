#
#
# Connection to MySql Validation db.
# Changes should be made here...
# 
#
#
#
package Connect::MySQLVaomonitorDB;

use Exporter();
use DBI;
@ISA = qw(Exporter);
@EXPORT  = qw(vaomonitor_connect);

sub vaomonitor_connect
{    
  
    my $dbh;
    
    $dbh = DBI->connect("DBI:mysql:host=asddb.gsfc.nasa.gov:database=$::db:user=$::user:password=$::pw")
	or die "$DBI->errstr\n";
    return $dbh;
}
1;
