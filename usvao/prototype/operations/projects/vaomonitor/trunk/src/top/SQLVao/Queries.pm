#
#
#
package SQLVao::Queries;


use Exporter ();
use DBI;
@ISA = qw(Exporter);

@EXPORT = qw (getTestUrls getServicenamesTestid getData getServiceandTest getcurrentHealth 
	      getStatsAllPassing  getStatsAllFailing getStatsPassing getStatsFailing 
	      getRangeStatsPassing getRangeStatsFailing getcurrentHealthService get_list
	      getPenultimateStats get_email_addresses getErrorMessage get_id_fromdb
	      updateTable getcurrentHealthOnlyVAO load_notices getHeasarcStats);



use warnings;
#use strict;
#
#
#
sub getErrorMessage
{
    my ($dbh, $runid) = @_;
    my $sql =  qq ( select  description  from Errors e,ErrorCodes c  where e.monitorResCode   = c.monitorResCode and runid = $runid);
    my $sth =  $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
	
}
sub updateTable
{
    my ($con,$service) = @_;
    
    foreach my $testname (keys %$service)
    { 
	my $runid  = $service->{$testname}->{runid};

        my $sql = qq(update Testhistory set validtest = 'T' where runid = '$runid');
	my $sth = $con->{dbh}->prepare($sql);
	$sth->execute();	
    } 
}
sub load_notices
{
    my ($dbh,$tablename,$condition) = @_;
    my $clause = '';    
    if ($condition eq 'currentnotes')
    { 
       $clause = 'where  deleted is null';
    }
    elsif ($condition eq 'deletednotes')
    {

        $clause  = "where deleted =   'T'";
    }   
    my $sql = qq(select * from $tablename $clause);
   
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();   
    return $arrayref;
}
sub get_id_fromdb
{
    my ($sid,$dbh) = @_;
    my $q = qq(select name from Services where serviceId = ?);
    my $sth = $dbh->prepare($q);
    $sth->execute($sid);
    my $data    = $sth->fetchall_arrayref();
    my $array   =  @$data[0];
    my $string  =  @$array[0];
    return $string;
}
sub get_email_addresses
{
    my ($dbh) = @_;
    my $sql = qq(select serviceName,addresses from notification_emails);
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref  = $sth->fetchall_arrayref();
    return $arrayref;    
}
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
sub getRangeStatsPassing 
{
    my ($con) = @_;
    my $start = $con->{caldate};
    my $end   = $con->{newcaldate};
    my $sql = qq {select s.name,s.homeurl, t.serviceId, count(t.monitorstatus)   from  Services s,   
    Testhistory t where monitorstatus  = 'pass' and s.serviceId = t.serviceId and time >= ?   
		and  time <= ?   group by serviceId};
   
    my $sth = $con->{dbh}->prepare($sql); 
    $sth->execute($start,$end);
    my $arrayref = $sth->fetchall_arrayref();
    return  $arrayref;
}
sub getHeasarcStats
{
    
    my ($con) = @_;
    my $start = $con->{caldate};
    my $end   = $con->{newcaldate};
    my $sql = qq {select name, count(g.monitorstatus)  from Services s,Tests t,Testhistory g  
		      where name in ('DataScope','VO Validation Service','Simple Query','DataScope Portal',
				     'VAO Notification Service','Data Discovery') 
		      and s.serviceId = t.serviceId and g.time >= ?
		      and g.time <= ? and g.testid = t.testid group by t.testId};
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
    my $sid  = $con->{sid};
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
    my $sql  = qq( select distinct name, homeinst, homeurl from  Services);
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
    if ($con->{type})
    {
	$string = "and m.type = ?";
    }
    $stringb = "order by  $con->{orderby}" if ($con->{orderby});
    $con->{index} = '' if (! $con->{index});
    my $hash = {};
    my $sql = qq( select m.name as name, m.serviceId as serviceId, m.testid as testid, m.testname, m.type as type,  
		  Testhistory.monitorstatus 
		  as status,m.bigdate as time,  
		  Testhistory.runid as runid,m.url as url,m.params as params,m.displayorder
		  from 
		  (  select t.serviceId, max(t.time) as bigdate, t.testid,s.type, s.name, g.testname,g.url,g.params,s.displayorder
		     from Testhistory t, Tests g, Services s where 
		     s.serviceId = t.serviceId  and t.time > now() - interval 3 hour and g.serviceId = t.serviceId and g.deleted 
                     is null and g.testid = t.testid  group by t.testid 
		  ) 
		  as m 
		  join Testhistory on Testhistory.serviceId = m.serviceId 
		  and Testhistory.time = m.bigdate  $string $stringb $con->{index});
    
    
   my $sth = $con->{dbh}->prepare($sql);
   #print "RR: $sql<br>";
    
    $sth->execute($con->{type}) if ($con->{type});
   
    $sth->execute() if (! $con->{type});

    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;

}
sub getcurrentHealthOnlyVAO
{
    my ($con) = @_;
    my $hash = {};
    my $sql = qq( select m.name as name, m.serviceId as serviceId, m.testid as testid, m.testname, m.type as type,  
                  Testhistory.monitorstatus 
                  as status,m.bigdate as time,  
                  Testhistory.runid as runid,m.url as url,m.params as params,m.displayorder
                  from 
                  (  select t.serviceId, max(t.time) as bigdate, t.testid,s.type, s.name, g.testname,g.url,g.params,s.displayorder
                     from Testhistory t, Tests g, Services s where date_sub(curdate(), interval 3 hour ) <= t.time and
                     s.serviceId = t.serviceId and g.serviceId = t.serviceId and g.deleted is null and g.testid = t.testid  group by  
                     t.testid 
                  ) 
                  as m 
                  join Testhistory on Testhistory.serviceId = m.serviceId 
                  and Testhistory.time = m.bigdate and m.type not like '%Legacy%'  order by m.displayorder);
    
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
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
    my $show = $con->{show};
    my $sql  =  qq  ( create temporary table runids_$pid ( runid int(10)));
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    
    my $sql_insert = qq (
			 insert into runids_$pid 
			 ( select  max(runid) from Testhistory a, Tests b, Services c  where c.name = ?
			   and c.serviceId = a.serviceId and b.deleted is null  and a.testid = b.testid group by a.testid);
			 );
    
    my $stha = $con->{dbh}->prepare($sql_insert); 
    $stha->execute($show);

   
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
   
    my ($container) = @_;
    my $name = $container->{'show'};
    my $runid = $container->{'runid'};
    my $column = $container->{'orderby'};
    my $index = $container->{'index'};
    my $offset = $container->{'offset'};
  
    
    my $var  = 't';
    $var = 'a' if (($column eq 'testname') || ($column eq 'testid'));
    $var = 's' if ($column eq 'name');
    if ($column eq 'status')
    {
        $column = 'monitorstatus';
    }
     
    my $sql = qq ( 
                    select s.name,s.serviceId, a.testid, a.testname, s.type, t.monitorstatus, t.time,t.runid from Services s, Testhistory t, 
		    Tests a  where s.name = ?  and a.deleted is null and s.serviceId = t.serviceId and t.testid = a.testid order by 
		   $var.$column $index limit 100 offset $offset;
                                                  
                   ); 
    my $sth = $container->{'dbh'}->prepare($sql);
    $sth->execute($name) || die "cannot run statement";  
    my $array  = $sth->fetchall_arrayref();    
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
		 a.deleted is null and s.serviceId = t.serviceId and a.testid = t.testid   and t.serviceId =  ?
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
sub getLastTimeUp
{
    my ($con) = @_;
    my $pid = $$;
    my $sid = $con->{sid}; 
   
    #my $s = qq (delete from runids_$pid);
  #  my $st = $con->{dbh}->prepare($s);
   # $st->execute();    


    my $sql = qq( create temporary table runidsnew_$pid ( runid int(10)));
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();  
  
    my $sql1 =  qq(  insert into runidsnew_$pid (select max(runid)   from Testhistory t,Tests  a 
		     where t.serviceId = ? and monitorstatus = 'pass' 
		    and a.testid = t.testid and a.deleted is null));
    my $sth1  = $con->{dbh}->prepare($sql1);
    $sth1->execute($sid); 
    my $sql2 = qq(  select  runid ,testid,unix_timestamp(time)  from Testhistory where runid in   
		    (select * from runidsnew_$pid));
    my $sth2 = $con->{dbh}->prepare($sql2);
    $sth2->execute();
    
    my $res =  $sth2->fetchall_arrayref();
    return $$res[0][2];
     
}
1;
