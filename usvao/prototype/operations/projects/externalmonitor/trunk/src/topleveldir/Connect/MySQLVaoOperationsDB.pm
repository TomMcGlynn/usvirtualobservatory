#
#
# Connection to MySql Validation db.
# Changes should be made here...
# 
#
#
#
package Connect::MySQLVaoOperationsDB;

use Exporter();
use DBI;
@ISA = qw(Exporter);
@EXPORT  = qw(vao_operations_connect);

sub vao_operations_connect
{    
  
    my $dbh;
    $dbh = DBI->connect("DBI:mysql:host=asddb.gsfc.nasa.gov:database=$::opsdb:user=webuser:password=$::pw")
	or die "$DBI->errstr\n";
    return $dbh;
}
1;
