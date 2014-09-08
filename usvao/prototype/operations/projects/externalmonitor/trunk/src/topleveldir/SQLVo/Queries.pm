#
#
#
package SQLVo::Queries;


use Exporter ();
use DBI;
@ISA = qw(Exporter);

@EXPORT = qw (getTestUrls getServicenamesTestid getData getServiceandTest getcurrentHealth 
	      getStatsAllPassing  getStatsAllFailing getStatsPassing getStatsFailing 
	      getRangeStatsPassing getRangeStatsFailing get_list getCurrentHealthService
	      getPenultimateStats get_email_addresses getErrorMessage updateTable  getServicesandTests);



use warnings;
#use strict;
#
#
#
sub getErrorMessage
{
    my ($dbh, $runid) = @_;
    my $sql =  qq ( select  description  from Errors e,ErrorCodes c  where e.monitorResCode   = c.monitorResCode and runid = ?);
    my $sth =  $dbh->prepare($sql);
    $sth->execute($runid);
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
	
}
sub updateTable
{
    my ($con,$service) = @_;
    
    foreach my $testname (keys %$service)
    { 
	my $runid  = $service->{$testname}->{runid};

        my $sql = qq(update Testhistory set validtest = 'T' where runid = ?);
	my $sth = $con->{dbh}->prepare($sql);
	$sth->execute($runid);	
    } 
}
####################
sub get_email_addresses
{
    my ($dbh) = @_;
    my $sql = qq(select serviceName,addresses,type from notification_emails_vo);
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref  = $sth->fetchall_arrayref();
    return $arrayref;    
}
sub getPenultimateStats
{
    my ($con,$service) = @_;


    my $sql =  qq(select t.testid, unix_timestamp(t.time) as g,t.monitorstatus 
                 from Testhistory t,    
                      ( select J.testid,max(J.bigdate) as G  from (select  m.runid,m.testid,m.monitorstatus, m.bigdate from  
                           ( select t.serviceId,t.runid , t.monitorstatus, unix_timestamp(t.time)   as bigdate,t.testid,s.type, s.name, g.testname,g.url
                             from Testhistory t, Tests g,  Services s where    s.serviceId = t.serviceId  and  g.serviceId =t.serviceId and 
                             g.testid = t.testid and s.name =  '$service'   and t.validtest = 'T' and unix_timestamp(t.time) <  (unix_timestamp(now())  -1600) 
                             and unix_timestamp(t.time) > ( unix_timestamp(now()) -7800)) as m
                            ) as J join Testhistory on 
                          unix_timestamp(Testhistory.time) = J.bigdate and J.monitorstatus =  Testhistory.monitorstatus group by testid
                       )  b  
                 where  t.testid = b.testid and unix_timestamp(t.time) =  b.G;);

    #print "SS: $sql\n";
    my $sth  = $con->{dbh}->prepare($sql);
    $sth->execute();
    
    my $array  = $sth->fetchall_arrayref();
    return $array;

}

#######################
sub getTestUrls
{
    my ($dbh,$name, $testid) = @_;
    my $time;
    my $sql   = qq( select  url, response_type,class,testname,notAvailable, params  from  Tests  where testid =  ?);
    my $sth  = $dbh->prepare($sql);
    $sth->execute($testid);
    my $arrayref = $sth->fetchall_arrayref();
    if (scalar(@$arrayref) == '0')
    {
	return;
    }
    return $arrayref;

}
sub getServicesandTests
{
    my ($con) = @_;
    my $sql = qq { select s.name,t.serviceId,t.url,t.testname from Services s, Tests t where s.serviceId = t.serviceId order by serviceId};
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    my $hash= {};
    my $arrayref =  $sth->fetchall_arrayref();
    foreach my $n (@$arrayref)
    {
	my $name     = @$n[0];
	my $url      = @$n[2];
	my $testname = @$n[3];
	if (! exists $hash->{$name})
	{
	    $hash->{$name}->{$testname}  =  $url;
	}
	else
	{
	    my $h           = $hash->{$name};
	    $h->{$testname} =  $url;
	    $hash->{$name}  = $h;
	}
    }
    return $hash;
}
sub getRangeStatsPassing 
{
    my ($con) = @_;
    my $start = $con->{caldate};
    my $end   = $con->{newcaldate};
    my $sql = qq {select s.name,s.homeurl, t.serviceId, count(t.monitorstatus)   from  Services s,   
    Testhistory t where monitorstatus  = 'pass' and s.serviceId = t.serviceId and time >= ?   
		and  time <= ?   group by serviceId};
  
    #print "S: $sql<br>"; 
    my $sth = $con->{dbh}->prepare($sql); 
    $sth->execute($start,$end);
    
    my $arrayref = $sth->fetchall_arrayref();
    return  $arrayref;


}
sub getRangeStatsFailing 
{
    my ($con) = @_;
    my $start = $con->{caldate};
    my $end   = $con->{newcaldate};
    
    my $sql = qq {select s.name,s.homeurl,t.serviceId, count(t.monitorstatus)   from  Services s,   
    Testhistory t where monitorstatus  in ('fail', 'abort') and s.serviceId = t.serviceId and time >= ? 
	and  time <= ?   group by serviceId};

    my $sth = $con->{dbh}->prepare($sql); 
    $sth->execute($start,$end);
    my $arrayref = $sth->fetchall_arrayref();
    return  $arrayref;


}
sub getServiceName
{
    my ($con) = @_;
    my $sid = $con->{sid};
    my $sql =  qq(select name from Services where serviceId = ?);
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute($sid);
    my $a  = $sth->fetchall_arrayref();
    my $line = @$a[0];
    return @$line[0];
}
sub getEarliestTime
{
    my ($con) = @_;
    my $sid = $con->{sid};
    my $sql =  qq(select unix_timestamp(time)  from Testhistory t where  serviceId= ? order by time asc  limit 1 );
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute($sid);
    my $a  = $sth->fetchall_arrayref();
    my $line = @$a[0];
    return @$line[0];
}
sub getServicenamesTestid
{
    my ($dbh,$type)  = @_;
    my $list = {};
    my $sql_sub1 = qq(select s.name, t.testid, testname  from Tests t, Services s where );    
    my $sql_sub2 = '';
    $sql_sub2    =   qq(type =  '$type' and ) if ($type);
    my $sql_sub3 =  qq(t.serviceId  = s.serviceId group by t.testid);
    my $sql   = $sql_sub1  . $sql_sub2 . $sql_sub3;
    
    my $sth  = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    foreach my $n (@$arrayref)
    {
	my $name = shift  @$n;
	my $number = shift @$n;
	my $testname = shift @$n;
        if (exists ($list->{$name}))
	{
	    my $value = $list->{$name};
	    if (! exists ($value->{$number}))
	    {
		$value->{$number} = $testname;
	    }
	   
	}
	else
	{
	    my $hash = {$number => $testname}; 
	    $list->{$name} = $hash;
        }
    }
    return $list;

}
sub getData
{
    my ($dbh) = @_;
    my $hash = {};
    my $sql  = qq( select distinct name, homeinst, homeurl,vao_services from  Services);
    my $sth  = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();	
    my @array;
    foreach my $n (@$arrayref)
    {
	 my $name = shift  @$n;
	 
	 $hash->{$name}  = $n;
    }
    return $hash;
}
sub getcurrentHealth
{

    my ($con) = @_;
    my $string = '';
    my $stringb = '';
    my $pid = $$;
    
    
    $stringb = "order by  $con->{orderby}" if ($con->{orderby});
    $con->{index} = '' if (! $con->{index});
    
    my $hash = {};

    my $sql = qq(create temporary table alltests_$pid (runid int(11), serviceId int(11), testid int(11), monitorstatus varchar(10), time varchar (30), validtest varchar(3)));
    $con->{dbh}->do($sql);
 
    my $sql1 = qq(insert into alltests_$pid (select *  from Testhistory th where th.time > now() -interval 2 hour order by serviceId));  
    
    $con->{dbh}->do($sql1);
   
    my $sql2 = qq(select r.name as name, Testhistory.serviceId as serviceId,r.testid as testid,r.authid as authid,
               r.testname,r.type as type, Testhistory.monitorstatus as status, r.bigdate as time, Testhistory.runid
               as runid, r.url as url,r.params as params,r.displayorder,r.deleted,r.notAvailable
                 from (select s.name, t.serviceId, t.testid, s.authid, g.testname,s.type,
                      t.monitorstatus, max(t.time) as bigdate,t.runid,g.params,g.url,s.displayorder,  g.deleted,
                      g.notAvailable from alltests_$pid  t, Tests g, Services s where g.testid = t.testid and  
                      s.serviceId = t.serviceId group by t.testid) as r 
                 join Testhistory on Testhistory.testid = r.testid and Testhistory.time = r.bigdate); 
                  
    my $sth2 = $con->{dbh}->prepare($sql2);
    
    $sth2->execute();
    my $arrayref = $sth2->fetchall_arrayref();
    return $arrayref;

}
sub get_list
{
    my ($dbh ) = @_;
    
    my $sql = qq(select name from Services);
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}
sub getCurrentHealthService
{
    my ($con) = @_;
    my $pid = $$;
    my $hash = {};
    my $sql =  qq  ( create temporary table runids_$pid ( runid int(10)));
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    
    my $sql_insert = qq (
			 insert into runids_$pid 
			 ( select  max(runid) from Testhistory a, Tests b, Services c  where c.name = '$con->{show}'
			   and c.serviceId = a.serviceId and b.deleted is null  and a.testid = b.testid group by a.testid);
			 );
    my $stha = $con->{dbh}->prepare($sql_insert); 
    $stha->execute();
   
    $con->{index} = '' if (! $con->{index});
    my $sql1 = qq( 
		  
		  select c.name,a.serviceId, a.testid, g.testname, c.type,a.monitorstatus, a.time,a.runid  
		   from Testhistory a, Tests g, Services c  where runid in  (select * from runids_$pid)   
		   and c.serviceId = a.serviceId and g.testid =a.testid  order by $con->{orderby} $con->{index}
		   );
    
    
    my $sth1 = $con->{'dbh'}->prepare($sql1);
    $sth1->execute();
    my $arrayref = $sth1->fetchall_arrayref();
    return $arrayref;

}
sub getOldTables
{
   
    my ($con) = @_;
    my $name = $con->{'show'};
    my $runid = $con->{'runid'};
    my $column = $con->{'orderby'};

    my $index = $con->{'index'};
    my $offset = $con->{'offset'};
    my $pid = $$;
    
    my $var  = 't';
    $var = 'a' if (($column eq 'testname') || ($column eq 'testid'));
    $var = 's' if ($column eq 'name');
    if ($column eq 'status')
    {
        $column = 'monitorstatus';
    }

    my $sql1 = qq( create temporary table Tr_$pid (runid int(11), serviceId int(4),testid int(4),
                   monitorstatus varchar(10), time varchar(30), validtest varchar(3)));
    my $sth1 = $con->{dbh}->prepare($sql1);
    $sth1->execute();

    my $sql2 = qq(insert into Tr_$pid select runid, s.serviceId, testid, monitorstatus,  time, validtest 
		  from Testhistory t, Services s where s.name = ? and s.serviceId = t.serviceId);
    my $sth2 = $con->{dbh}->prepare($sql2);
    $sth2->execute($name);
    my $sql3 = qq(select s.name,s.serviceId, a.testid, a.testname, s.type, t.monitorstatus, t.time,t.runid
               from Services s, Tr_$pid  t, Tests a  where s.name = ? and s.serviceId = t.serviceId and
	       t.testid = a.testid order by t.time desc limit 100 offset $offset);
    my $sth3 = $con->{'dbh'}->prepare($sql3);
    $sth3->execute($name) || die "cannot run statement";  
    my $array  = $sth3->fetchall_arrayref();    
    return $array;  
}
sub getLastValTime
{
    my ($dbh) = @_;
    my $time  = "(undef)";
    my $sql  = qq( select max(time) from Testhistory );
    my $sth  = $dbh->prepare($sql);
    $sth->execute 
        ||die "cannot run lastvaltime query";
    
    my $array = $sth->fetchall_arrayref();
   
    my @n  =  @$array;
    
    my $timearray = $n[0] if ($n[0]);
    $time     =  @$timearray[0];
    return $time;
}
sub getStatusTable
{    
    my ($container) = @_;   
   
    my $runid = $container->{'runid'};   
    
    my $sql1 = qq  ( create temporary table detailstmp (serviceId int,runid int,subtestid varchar(100), description varchar(300)));
    my $sth1 = $container->{'dbh'}->prepare($sql1);
    $sth1->execute() || die "cannot run statement"; 
    

    my $sq2 = qq ( 
                   insert into detailstmp (select STRAIGHT_JOIN   t.serviceId, t.runid, e.subtestid, c.description  
                   from Testhistory t, Errors e, ErrorCodes c  
                   where t.runid  = e.runid  
                   and  c.monitorResCode = e.monitorResCode 
		   and  t.runid = ?)
                   );
    my $sth2 = $container->{'dbh'}->prepare($sq2);
    $sth2->execute($runid) || die "cannot run statement";
        
    my $sql3 = qq (select s.serviceId,runid,subtestid,description 
                   from 
                   detailstmp d, Services s where d.serviceId = s.serviceId;
                   );
    my $sth3 = $container->{'dbh'}->prepare($sql3);
    $sth3->execute();
    my $array = $sth3->fetchall_arrayref(); 

    
    return $array;        
}
sub getLastDownTime
{
    my ($con) =  @_;
    my $sid = $con->{sid};
    my $sql = qq(select name, monitorstatus,unix_timestamp(time),runid  from Testhistory t,Services s, Tests a  where  
		 a.deleted is null and s.serviceId = t.serviceId and a.testid = t.testid   and t.serviceId = ?
		 and monitorstatus !='pass' order by time desc limit 1);
    
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute($sid);
    
    my $array = $sth->fetchall_arrayref();
    return $array;

}
sub getCurrentTime
{

    my ($con) = @_;
    my $sql =  qq(select unix_timestamp(date_sub(now(),interval 0 hour)));
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    
    my $array = $sth->fetchall_arrayref();
    return $array;

}
sub getLastTimeUp
{
    my ($con) = @_;
    my $pid = $$;
    my $sid = $con->{sid};
    my $sql = qq( create temporary table runids_$pid ( runid int(10)));
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();   
    
    my $sql1 =  qq(  insert into runids_$pid (select max(runid)   from Testhistory t,Tests  a 
		     where t.serviceId =   ? and monitorstatus = 'pass' 
		    and a.testid = t.testid and a.deleted is null));
    my $sth1  = $con->{dbh}->prepare($sql1);
    $sth1->execute($sid);
    my $sql2 = qq(  select  runid ,testid,unix_timestamp(time)  from Testhistory where runid in   
		    (select * from runids_$pid));
    my $sth2 = $con->{dbh}->prepare($sql2);
    $sth2->execute();
    my $res =  $sth2->fetchall_arrayref();
    return $$res[0][2];
}
1;
