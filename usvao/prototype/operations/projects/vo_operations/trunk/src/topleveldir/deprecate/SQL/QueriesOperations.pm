#
#
#
#

package SQL::QueriesOperations;

use Exporter();

@ISA =qw(Exporter);
@EXPORT = qw(getStatsFailingInInterval);

use strict;

sub getStatsFailingInInterval
{
    my ($dbh,$interval) = @_;
    my $sql = qq (

		 select  s.serviceId,t.runid, s.ivoid,s.serviceURL,s.xsiType, t.validationstatus,   t.time  from Services s,  Tests   t 
		  where s.serviceId = t.serviceId and s.deleted is null and s.deprecated is null and time > now() - interval $interval day order by s.serviceId
		  
		  );
    
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $res = $sth->fetchall_arrayref();
    return $res;
}
sub getStatsFailingInInterval_old
{
    my ($dbh,$interval) = @_;
    my $sql = qq (

		  select  m.serviceId as serviceId,m.shortname as shortname,   
		  Tests.validationstatus as status,m.bigdate as time, m.serviceURL as   
		  url,m.xsitype as type, m.ivoid as ivoid, m.test_ra as test_ra,   
		  m.test_dec as test_dec, m.radius as radius,Tests.runid as runid, 
		  m.deleted as deleted 
		  from  
		  (select s.shortname, s.xsitype, s.ivoid, s.test_dec, s.radius,s.test_ra, s.serviceURL, s.deleted, t.serviceId, 
		   max(t.time) as bigdate from Tests t, Services s 
		   where s.serviceId =  t.serviceId and  date_sub(curdate(), interval $interval  day) <= t.time  
		   group 
		   by t.serviceId order by bigdate) as m 
		  join  Tests on Tests.serviceId = m.serviceId and Tests.time = m.bigdate where Tests.validationstatus 
		  in ('fail','abort');
		  
		  );
    
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $res = $sth->fetchall_arrayref();
    return $res;
}
1;
