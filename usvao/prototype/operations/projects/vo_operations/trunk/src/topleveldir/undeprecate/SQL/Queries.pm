# used to build different types of table
# objects. 
#
#
#
package SQL::Queries;

use Exporter ();
use DBI;
@ISA = qw(Exporter);

@EXPORT = qw (getTodaysList);

use warnings;




#####################
sub getTodaysList
{
    my ($container) = @_;
    #my $index     = $container->{'index'};
    #my $orderby   = $container->{'orderby'};
    #my $hash = load_cnamealiases();
    #$hash->{status} = 'validationstatus';
    #$hash->{type}   = 'xsitype';
    #$hash->{serviceURL} = 'serviceURL';
    my $sql  = qq(select *  from (select s.serviceId,s.shortname, t.validationstatus,t.time, 
				   s.serviceURL, s.xsitype, s.ivoid, s.test_ra,s.test_dec, s.radius,t.runid, s.deleted 
				   from Services s, Tests t where s.serviceId = t.serviceId 
				   and time > now() - interval 3 hour order by time desc
				   ) 
		   as m group by m.serviceId having max(m.runid) order by m.deleted);
   # print "$sql\n";
     
    my $sth = $container->{'dbh'}->prepare($sql);
    $sth->execute() || die "cannot run statement";   
    my $array  = $sth->fetchall_arrayref();   
    
    return $array;

}
1;
