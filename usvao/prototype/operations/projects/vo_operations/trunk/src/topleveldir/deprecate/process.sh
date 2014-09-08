#!/usr1/local/bin/tcsh
#
#
#
# this script is extra. In some cases we want to find out
# which services are failing due to a possible broken service url. In these cases,
# the validator will respond with an "apparent communication error".
# 
# In order to get a list of services failing with this error, run this script on
# the fail.out file produced by get_list_to_deprecate.pl. This script will dump
# output to screen.

 set file  = $argv[1];
 cat file.out | awk -F '|' '{print "insert into deprecation_tmp values ("$5"|"$1"); " }' | sed -e "s/)/')/g" | sed -e "s/(/('/g" | sed -e "s/|/','/" > ! ./out
 mysql -h asddb.gsfc.nasa.gov validation  -u mpreciad -pMpVAO\!\@\#Temp < ./out

 mysql -h asddb.gsfc.nasa.gov validation  -u mpreciad -pMpVAO\!\@\#Temp -e "select q.runid, q.ivoid,er.description  from deprecation_tmp q,Errors e, ErrorCodes er  where q.runid = e.runid  and  e.validationResCode = er.validationResCode and er.description like  '%Apparent communication error produced an exception inside the validater%' group by runid";
  
 #mysql -h asddb.gsfc.nasa.gov validation  -u mpreciad -pMpVAO\!\@\#Temp -e 'select s.serviceURL,s.ivoid,t.runid, t.validationstatus,e.validationResCode,ec.description  from Tests t,Services s,Errors e,ErrorCodes ec where t.runid in  ( select max(t.runid)  from Services s, Tests t where s.ivoId in (select * from deprecation_tmp) and s.serviceId = t.serviceId) and s.serviceId = t.serviceId and e.runid = t.runid and ec.validationResCode = e.validationResCode';
 

 mysql -h asddb.gsfc.nasa.gov validation -u mpreciad -pMpVAO\!\@\#Temp -e 'delete from deprecation_tmp';
 exit();
