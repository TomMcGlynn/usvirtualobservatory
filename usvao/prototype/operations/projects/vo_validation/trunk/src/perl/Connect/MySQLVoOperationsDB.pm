#
#
# Connection to MySql Validation db.
# Changes should be made here...
# 
#
#
#
package Connect::MySQLVoOperationsDB;

use Exporter();
use DBI;
@ISA = qw(Exporter);
@EXPORT  = qw(vao_operations_connect);

sub vao_operations_connect
{    
  
    my $dbh;
    $dbh = DBI->connect("DBI:mysql:host=asddb.gsfc.nasa.gov:database=vao_operations_test:user=mpreciad:password=MpVAO\!\@\#Temp")
	or die "$DBI->errstr\n";
    return $dbh;
}
1;
