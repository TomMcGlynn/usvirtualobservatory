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
    
    my $dbh = DBI->connect("DBI:mysql:database=validation:host=asddb.gsfc.nasa.gov:user=mpreciad:password=")
        or die "cannot connect to the mysql validation db\n";
    return $dbh;
}
1;
