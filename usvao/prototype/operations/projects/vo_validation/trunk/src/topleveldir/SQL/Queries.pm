# used to build different types of table
# objects. 
#
#
#
package SQL::Queries;

use Exporter ();
use DBI;
@ISA = qw(Exporter); 

@EXPORT = qw (getToValidate getReValidate getNeverValidated getTableData getTodaysList
	      getTotalServiceTests getCurrentSnapshot  getStatusTable getCenterStats getserviceURL
	      getnumberValidated  getRegistryServices get_types get_lastvaltime get_authids getData
	      getid_fromdb getServicesByType  getSkipped getResults getTypesSkipped getDeleted getDeprecated);

use warnings;

sub get_lastvaltime
{
    my ($dbh) = @_;
    my $time;
    my $sql  = qq( select max(time) from Tests );
    my $sth  = $dbh->prepare($sql); 
    $sth->execute 
	||die "cannot run lastvaltime query";   
    my $array = $sth->fetchall_arrayref();   
    my $n  = shift(@$array);
   
    if (! @$n[0])
    {	
	$time = "(undef)";
    }
    else
    {
	$time = "@$n[0]";
    }
    return $time;
}
sub getserviceURL
{
    my ($dbh,$id,$type) = @_;
    my $sql = qq(select serviceURL from Services where ivoid = '$id' and xsitype like '$type');
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}
sub getData
{
   my ($dbh,$type,$id) = @_;
   
   my $sql = qq(select t.type, t.url,t.id,t.classmethod, ivoid, test_dec,test_ra,radius  from Services s, Test t
                 where t.classmethod = '$type' and t.type = s.xsitype and s.ivoid = '$id');
   my $sth = $dbh->prepare($sql);
   $sth->execute();
   my $arrayref = $sth->fetchall_arrayref();
   return $arrayref;
}
sub get_id_fromdb
{
    my ($sid,$dbh) = @_;
    my $q = qq(select ivoid from Services where serviceId = ?);
    my $sth = $dbh->prepare($q);
    $sth->execute($sid);
    my $data    = $sth->fetchall_arrayref();
    my $array   =  @$data[0];
    my $string  =  @$array[0];
    return $string;
}
sub getSkipped
{
    my ($con) = @_;
    my $dbh = $con->{dbh};
    my $hash = $con->{ignore};
    my @a         = values %$hash;
    my $string = '';
    my $sql;
    if (@a)
    {    
        $sql = "select count(*)  from Services where  xsitype in (select xsitype from vao_operations.xsitype_restrictions where xsitype like";
        for(my $i=0;$i<=scalar(@a);$i++)
        {
	    $string .= " '%$a[$i]%' " if ($i  == '0');
	    $string .=  "or xsitype like '%$a[$i]%'"  if (($i > 0) and ($i < scalar(@a)));
        }
        $sql =  $sql . $string . ")";

    }
    else
    {
       $sql =  qq( select count(*)  from Services where  xsitype in (null));

    }
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $array = $sth->fetchall_arrayref();
    return $array;
}
sub getTypesSkipped
{
    my ($dbh ) = @_;
    my $sql = qq(select xsitype from vao_operations.xsitype_restrictions where status = 'T');
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $array = $sth->fetchall_arrayref();
    return $array;
}
sub getTodaysList
{
    my ($container) = @_;
    my $index     = $container->{'index'};
    my $orderby   = $container->{'orderby'};
    my $hash = load_cnamealiases();
    $hash->{status} = 'validationstatus';
    $hash->{type}   = 'xsitype';
    $hash->{serviceURL} = 'serviceURL';
    my $sql  = qq(select *  from (select s.serviceId,s.shortname, t.validationstatus,t.time, 
				   s.serviceURL, s.xsitype, s.ivoid, s.test_ra,s.test_dec, s.radius,t.runid,
				   s.deleted,s.deprecated
				   from Services s, Tests t where s.serviceId = t.serviceId 
				   and time > now() - interval 24 hour  and s.xsitype not in (select xsitype 
				   from vao_operations_test.xsitype_restrictions
                                   where status = 'T')order by time desc
				   ) 
		   as m group by m.serviceId having max(m.runid) order by m.deleted,m.deprecated,$$hash{$orderby} $index);
    my $sth = $container->{'dbh'}->prepare($sql);
    $sth->execute() || die "cannot run statement";   
    my $array  = $sth->fetchall_arrayref();   
    
    return $array;

}
sub getStatusTable
{    
    my ($container) = @_;   
    my $runid = $container->{'runid'};   
    
    my $sql1 = qq  ( create temporary table detailstmp (serviceId int, runid int,subtestid varchar(100), description varchar(300)));
    my $sth1 = $container->{'dbh'}->prepare($sql1);
    $sth1->execute() || die "cannot run statement"; 
    

    my $sq2 = qq ( 
		   insert into detailstmp (select STRAIGHT_JOIN   t.serviceId, t.runid, e.subtestid, c.description  
		   from Tests t, Errors e, ErrorCodes c  
		   where  t.runid  = e.runid   and  c.validationResCode = e.validationResCode and 
		   t.runid = ?)	
		 );
    #print "$sq2"; 
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
sub getOldTables
{    
    my ($container) = @_;
    my $sid = $container->{'sid'};
    my $runid = $container->{'runid'};
    my $column = $container->{'orderby'};
    my $index = $container->{'index'};
    if ($column eq 'status')
    {
	$column = 'validationstatus';
    }
     
    my $hash = {};
    my $sql = qq ( 
		   select    t.serviceId, t.runid, t.validationstatus, t.type,t.time
		   from 
		     Tests t  
		   where 
		   t.serviceId = ?  order by t.$column $index;
				  		  
		   ); 
    my $sth = $container->{'dbh'}->prepare($sql);
    $sth->execute($sid) || die "cannot run statement";  
    my $array  = $sth->fetchall_arrayref();    
    return $array;  
}
sub get_types
{
    my ($dbh) = @_;
    my $sql = qq (select distinct type from Test);
    my $sth = $dbh->prepare($sql);
    $sth->execute() || die "cannot run query";
    my $array =  $sth->fetchall_arrayref();
    my $hash = {};
    foreach my $n (@$array)
    {
	$hash->{"@$n"} = "@$n";       
    }
    return $hash;
}
sub load_cnamealiases
{
    my %hash = ( 
		 'serviceId'  => 'serviceId',
		 'shortname'  => 'shortname',
		 'status'     => 'status',
		 'serviceURL' => 'url',
		 'type'       => 'type',
		 'ivoid'      => 'ivoid',
		 'test_ra'   => 'test_ra',
		 'test_dec'   => 'test_dec',
		 'radius'     => 'radius',
		 'runid'      => 'Tests.runid',
		 'time'     => 'time',
		 );
    return \%hash;
}
sub getResults
{
    my ($con) = @_;
    my $dbh = $con->{dbh};
    my $sql = qq( select b.serviceId,b.shortname,b.status,b.time,b.url,b.type, b.ivoid,b.test_ra,b.test_dec,
                  b.radius,b.runid,b.deleted,b.deprecated  from 
		  ((select m.serviceId as  serviceId,m.shortname as shortname, Tests.validationstatus as 
		    status,m.bigdate as time, m.serviceURL as  url,m.xsitype as type, m.ivoid as ivoid,                         
		    m.test_ra as test_ra, m.test_dec as test_dec, m.radius as 
		    radius,Tests.runid as runid, m.deleted as deleted,m.deprecated from                          
                    (select s.shortname,  s.deprecated,s.xsitype, s.ivoid,  s.test_dec, s.radius,s.test_ra,  
		    s.serviceURL, s.deleted, t.serviceId, max(t.time)  as bigdate from Tests t,Services  s  
		    where s.serviceId = t.serviceId
                    and date_sub(curdate(), interval 40 day) <=  t.time  group by  t.serviceId ) as   
		    m  join  Tests on  Tests.serviceId = m.serviceId  and Tests.time = m.bigdate order 
		    by m.ivoid 
                     ) 
		    union all   
		     ( select  s.serviceId,s.shortname, Tests.validationstatus,
                    null, s.serviceURL, s.xsitype, s.ivoid, s.test_ra,s.test_dec, s.radius,  null, null  as
                    deleted,null as deprecated from Services s  LEFT JOIN Tests on s.serviceId = Tests.serviceId
		    where Tests.serviceId IS NULL 
                      )
                  ) as b order by b.ivoid
                 );
    
    my $sth = $dbh->prepare($sql);

    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
    
	
}
sub getDeleted
{
    my ($con) = @_;
    my $sql = qq(select count(distinct ivoid,deleted) from Services);
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();    
    return $arrayref;
   
}
sub getDeprecated
{
    my ($con) = @_;
    my $sql = qq(select count(distinct ivoid,deprecated) from Services);
    my $sth = $con->{dbh}->prepare($sql);
    $sth->execute();
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}
sub getCenterStats
{
    my ($container) = @_;  
    my $runid       = $container->{'runid'}; 
    my $orderby     = $container->{'orderby'};
    $orderby        = "ivoid" if ($orderby =~ m/ivoid(.*)/);    
    my $index       = $container->{'index'};
    my $show        = $container->{'show'};
    my $xsitypes    = $container->{'xsitypes'};
    my $querystring = $container->{'querystring'};
    my $hash = load_cnamealiases(); 
    my ($narrowby,$string); 
   
    
    if ($show)
    {  	
        if ($show eq 'all')
	{
	    $narrowby = '';	    
	}
	elsif (exists ($xsitypes->{$show}))
	{	   	   	   
	    $narrowby = "and s.xsitype like ?";
	    $string = $show;
	}
	else
	{  
	   $narrowby = "and s.ivoid like ?";
	   $string = $show;
       }	
    }
    else 
    {  	
	$narrowby = "and s.ivoid  like ?";
	$string = $querystring;
    } 
    
    my $sql = qq ( 
		   (select m.serviceId as serviceId,m.shortname as shortname, Tests.validationstatus as status,m.bigdate as time, 
                      m.serviceURL as url,m.xsitype as type, m.ivoid as ivoid, 
		       m.test_ra as test_ra, m.test_dec as test_dec, m.radius as radius,Tests.runid as runid, m.deleted as deleted,m.deprecated from  
		       ( select s.shortname, s.deprecated,s.xsitype, s.ivoid,  s.test_dec, s.radius,s.test_ra, s.serviceURL, s.deleted, t.serviceId, max(t.time) 
		           as bigdate from  Tests t, Services s 
			   where 
			    s.serviceId = t.serviceId  $narrowby and date_sub(curdate(), interval 40 day) <=   t.time  group by  t.serviceId 
			)  as   m  
		     join   Tests on Tests.serviceId = m.serviceId  and 
		     Tests.time = m.bigdate
		     order by $$hash{$orderby} 		  
		   )
		   union all
		   ( 
		     select s.serviceId,s.shortname, Tests.validationstatus, null, s.serviceURL, s.xsitype, s.ivoid, s.test_ra,s.test_dec, s.radius, 
		     null, null  as deleted,null as deprecated from Services s 
		     LEFT JOIN Tests on s.serviceId = Tests.serviceId 
		     where Tests.serviceId IS NULL $narrowby
		     )
		   order by  deleted,deprecated,$$hash{$orderby} $index;
		   );
    my $sth = $container->{'dbh'}->prepare($sql);
    $sth->execute('%' . $string .'%','%' . $string .'%') if ($narrowby ne '');
    $sth->execute() if ($narrowby eq '');
    my $array  = $sth->fetchall_arrayref();       
    return $array;

}
sub get_services_matching_errortype
{
    my ($container) = @_;
    my $dbh         = $container->{dbh};
    my $xsitypes    = $container->{'xsitypes'};
    my $pid         = $$;
    #create temporary tables
    my $q1 = qq(create temporary table errortmp_$pid (runid int));
    $dbh->do($q1);
    
    my $q2 = qq(create temporary table errortmpA_$pid(runid int));
    $dbh->do($q2);
  
    my $q3 = qq(create temporary table dump_$pid(runid int, list_char char));
    $dbh->do($q3);
    
    my $b  = qq(create temporary table out_$pid(runid int));
    $dbh->do($b);
    
    my $narrowby;
    my $show = $container->{show};
    if ($container->{centernames}->{$show})
    {
	$show = $container->{centernames}->{$show};
    }
    my ($column,$entry);
    if ( $show ne 'all')
    {    
        if (exists ($xsitypes->{$show}))
        {      
	   $column = "s.xsitype";           
           $entry =  $show;
        }
        else
        {
            $column = "s.ivoid";
	    $entry  = $show;
        }
    }
  
    #insert  all the LATEST failed runids for a particular identifier
    insert_into_errortmp($column,$entry,$dbh,$pid);

    #insert all runids containing a specific errorID
    insert_into_errortmpA($container->{error},$dbh,$pid);
   


    #combine errortmp and errortmpA into an organized list in table dump
    my $q4 = qq(insert into dump_$pid select runid, 'B' from errortmpA_$pid group by runid);
    my $sth4 = $dbh->prepare($q4);
    $sth4->execute;
    
    my $q5 = qq(insert into dump_$pid select runid,'A' from errortmp_$pid);
    my $sth5 = $dbh->prepare($q5);
    $sth5->execute;
    
    my $q6 = qq(insert into out_$pid select  runid from dump_$pid group by runid having count(*) = '2');
    my $sth6 = $dbh->prepare($q6);
    $sth6->execute();

    #fetch relevant data
    my $data = retrieve_data($container,$pid);
    return $data;
}
sub insert_into_errortmpA
{
    my ($error, $dbh,$pid) = @_;
    my $q  = qq(insert into errortmpA_$pid (select runid from Errors where subtestid = ?));
    my $sth = $dbh->prepare($q);
    $sth->execute($error);
}
sub insert_into_errortmp
{
    my ($column,$entry, $dbh,$pid) = @_;
      #add data to temporary table (runids)
    my $q2 =  qq( insert into errortmp_$pid 
                     (select Tests.runid  from                                   
                      (  select s.shortname, s.xsitype, t.validationstatus, s.serviceURL, 
                         t.serviceId, max(t.time)  as bigdate from Tests t, Services s where 
                         s.serviceId = t.serviceId and $column like ?  group by t.serviceId
                         ) 
                      as m 
                      join Tests on Tests.serviceId = m.serviceId 
                      and Tests.time  = m.bigdate  where Tests.validationstatus != 'pass'
		      
                      )                         
                    );
    my $stmt =  $dbh->prepare($q2);
    $stmt->execute('%' . $entry . '%');
}  
sub retrieve_data
{ 
    my ($container,$pid)= @_;
    my $dbh             = $container->{dbh};
    my $index           = $container->{index};
   
    if (!$index) 
    { 
	$index = 'asc';
    }
    my $orderby = $container->{orderby};
    
   
    my $q3  = qq (
         select s.serviceId,s.shortname,t.validationstatus,t.time,s.serviceURL,s.xsitype,s.ivoid , 
		  s.test_ra,s.test_dec,s.radius,t.runid,s.deleted,s.deprecated  from out_$pid o, Tests t,Services s where 
	     s.serviceId = t.serviceId and t.runid = o.runid
		order by deleted,  ?  $index
		  );
    my $sth3 = $dbh->prepare($q3);
    $sth3->execute($orderby);
    my $data = $sth3->fetchall_arrayref();
    return $data;
}
sub get_error_frequency
{

    my ($container) = @_;
    my $dbh = $container->{dbh};
    my $pid = $$;
     
    my $q1 = "create temporary  table zztemp_$pid(runid int) engine memory";
    $dbh->do($q1);
     
    my ($column,$entry);
    
    if ($container->{show})
    {	
	my $string = $container->{show};
        #see if this string is in the centernames hash
        if (exists $container->{centernames}->{$string})
        {     
	    $column = 's.ivoid';	     
	    $entry = $container->{centernames}->{$string};
        }
        elsif (exists ($container->{xsitypes}->{$string}))
        {
	    $column = "s.xsitype";
            $entry = $container->{show};
	}
        else
        {
            $column  = "s.ivoid";
	    $entry = $container->{show};         
        }
    }     
    my $q2 = qq(insert into zztemp_$pid (select max(runid) from Tests t,Services s where s.serviceId = t.serviceId 
					 and  $column like ?  and s.deleted is null  group by t.serviceId));
    my $sth2 = $dbh->prepare($q2); 
    $sth2->execute('%' . $entry . '%'); 
    my $q3   = qq (select a.subtestid, b.description,count(*) from  Errors a, ErrorCodes b,zztemp_$pid c  
		   where a.validationResCode = b.validationResCode and 
                   a.runid = c.runid group by a.subtestid order by count(*) desc;
		  );
   
    my $sth3    = $dbh->prepare($q3);
    $sth3->execute();
  
    my $rowref = $sth3->fetchall_arrayref();
    return $rowref;
}
######################
sub getNeverValidated
{
    my ($con) = @_;
    my $dbh       = $con->{'dbh'};
    my $show      = $con->{'show'};
    my $condition = $con->{'condition'};
    #$show is either an identifier or a type (eg. cs:ConeSearch)
   
   my $sql = "select serviceId  from 
		 ( select serviceId 	
		        from 
			(
			   (select serviceId from Services)
			    union all  
			     (select distinct(serviceId) from Tests)
			) 
			as tmp group by serviceId having count(*)=1  order by serviceId		    
		 )
		 as new";

    my $sth = $dbh->prepare($sql);
    $sth->execute() || die "cannot run statement";
   
    my $array  = $sth->fetchall_arrayref();
    my $number = scalar(@$array);
    return $number;                 
}
sub getCurrentSnapshot
{
    my ($con) = @_;
  
    my $dbh  = $con->{dbh};
    my $hash = $con->{ignore};
	my $sql;
    my @a    = values %$hash;
    if (scalar(@a) > 0) 
    {
       my $string = "xsitype like";
    
         for(my $i=0;$i<scalar(@a);$i++)
         {	
             $string .= " '%$a[$i]%' or xsitype like ";
         }
        $string =~ s/like\s$//;
        $string =~ s/xsitype\s$//;
        $string =~ s/or\s$//;
	    

        $sql = qq(select m.serviceId, Tests.time, Tests.validationstatus, m.deleted,m.deprecated  
		 from 
		 (select t.serviceId, max(t.time) as bigdate,t.validationstatus,s.xsitype,
                  s.deleted,s.deprecated  from Tests  t , Services s where  
		  t.serviceId =  s.serviceId and date_sub(curdate(),interval 40 day) <= t.time 
                  and s.deleted is null and s.deprecated is null group
                  by t.serviceId having max(t.time)  and xsitype
                  not in (select xsitype from vao_operations.xsitype_restrictions
		 where $string)) as 
		 m join Tests on Tests.serviceId = m.serviceId 
		 where m.bigdate = Tests.time
		 );

      }
      else
	{

	  $sql =  qq(select m.serviceId, Tests.time, Tests.validationstatus, m.deleted,m.deprecated  
                 from 
                 (select t.serviceId, max(t.time) as bigdate,t.validationstatus,s.xsitype,
                  s.deleted,s.deprecated  from Tests  t , Services s where  
                  t.serviceId =  s.serviceId and date_sub(curdate(),interval 40 day) <= t.time 
                  and s.deleted is null and s.deprecated is null group
                  by t.serviceId having max(t.time)) as 
                 m join Tests on Tests.serviceId = m.serviceId 
                 where m.bigdate = Tests.time
                 );
        }

    my $sth = $dbh->prepare($sql);
    $sth->execute() || die "cannot run sql query";
    my $arrayref = $sth->fetchall_arrayref();
    return $arrayref;
}

###########################
sub getTotalServiceTests
{
    my ($dbh) = @_;
    my $sql = qq(select count(distinct ivoid, xsitype) from Services;);
    my $sth = $dbh->prepare($sql);
    $sth->execute() || die "cannot run statement";
   
    my $array      = $sth->fetchall_arrayref();
    my $row        = pop(@$array);  
    my $number     = "@$row";
    return $number;
}
###########################
sub getnumberValidated
{
    my ($dbh) = @_;
    my $sql = qq(select count(distinct serviceId) from Tests;);
    my $sth = $dbh->prepare($sql);
    $sth->execute() || die "cannot run statement";
    
    my $array   = $sth->fetchall_arrayref();
    my $row     =  pop(@$array);
    my $number = "@$row";
    return $number;
}
############################
sub get_authids
{
    my ($dbh) = @_;
    my $sql = qq(select concat('ivo://', authidtable.authid, '/') 
		 from 
		   (select distinct substring_index(tmp.authid,'/',+1) 
		 as authid
		 from 
		    ( select substring_index(ivoid,'//',-1) as authid from Services) 
		 as tmp)
		 as authidtable);
   
    my $sth = $dbh->prepare($sql);
    $sth->execute()||die "cannot run get_authid";
    my $array = $sth->fetchall_arrayref();
    return $array;
}
1;
