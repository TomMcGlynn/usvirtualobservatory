#
#
#
#

package SQL::QueriesExtra;

use Exporter();

@ISA =qw(Exporter);
@EXPORT = qw(getCurrentStats getLastSet getShortCurrentStats);

use strict;

sub getCurrentStats
{
    my ($dbh) = @_;
    my $sql = qq (
		  select m.serviceId as serviceId,m.shortname as shortname, 
		  Tests.validationstatus as status,m.bigdate as time, m.serviceURL as 
		  url,m.xsitype as type, m.ivoid as ivoid, m.test_ra as test_ra, 
		  m.test_dec as test_dec, m.radius as radius,Tests.runid as runid, 
		  m.deleted as deleted from 
		    (select s.shortname, s.xsitype, s.ivoid,  
		       s.test_dec, s.radius,s.test_ra, s.serviceURL, s.deleted, t.serviceId, 
		       max(t.time) as bigdate from Tests t, Services s where s.serviceId = 
		       t.serviceId group by  t.serviceId 
		     ) 
		     as  m 
		  join  Tests on Tests.serviceId = m.serviceId and Tests.time     = m.bigdate 
		  union all 
		     (select 
		      s.serviceId,s.shortname, Tests.validationstatus, null, s.serviceURL, 
		      s.xsitype, s.ivoid, s.test_ra,s.test_dec, s.radius,null,null as deleted 
		      from Services s  LEFT JOIN Tests on s.serviceId = Tests.serviceId  where 
		      Tests.serviceId IS NULL 
		      ) 
		  order by  deleted);
    
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $res = $sth->fetchall_arrayref();
    return $res;
}
sub  getLastSet
{
    my ($dbh) = @_;


    #my $q1 = qq(create table TMP (serviceId int, runid int, validationstatus varchar (10),time varchar(30),index(serviceId)));
    #$dbh->do($q1);
    
    my $q2 = qq(insert into TMP  select t.serviceId ,t.runid, t.validationstatus,  t.time  from Tests t where date_sub(curdate(), interval 100 day) <= t.time);
    $dbh->do($q2);
    
    
    
    my $q3 =   qq(select t.serviceId ,t.runid, t.validationstatus, t.time  from TMP as t where time = (select max(time) from TMP   where serviceId = t.serviceId and time < (select max(time) from TMP  where serviceId  = t.serviceId)) order by t.serviceId);

    my $sth = $dbh->prepare($q3);
    $sth->execute();
    
    
   
    my $res = $sth->fetchall_arrayref();

    my $q4 = qq(delete from TMP);
    $dbh->do($q4);
    return $res;
}

sub getShortCurrentStats
{
    my ($dbh, $name) = @_;
    my $sql = qq( (select m.shortname as shortname,  Tests.validationstatus as status, m.ivoid as ivoid, m.deleted as deleted from
		   ( select s.shortname, s.xsitype, s.ivoid,  s.test_dec, s.radius,s.test_ra, s.serviceURL, s.deleted, t.serviceId, max(t.time)             
		     as bigdate                          from  Tests t, Services s          
		     where                              
		     s.serviceId = t.serviceId  and ivoid like '%$name%'                       
		     group by                              
		     t.serviceId                          
		     )                     
		   as   m                     join Tests on Tests.serviceId = m.serviceId                     
		   and                       Tests.time = m.bigdate                      
		   order by  m.shortname) 
	union all (select s.shortname, Tests.validationstatus, s.ivoid,null as deleted from Services s  LEFT JOIN Tests 	
		   on s.serviceId = Tests.serviceId where Tests.serviceId IS NULL and s.ivoid like '%$name%' ) order by deleted); 
    my $sth  = $dbh->prepare($sql);
    $sth->execute();
    my $array  = $sth->fetchall_arrayref();
    return $array;
}
1;
