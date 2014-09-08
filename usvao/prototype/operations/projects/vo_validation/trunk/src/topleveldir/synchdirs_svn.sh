#!/usr1/local/bin/tcsh 
#
#
#

set jira = '/software/jira/software2/projects/vo_validation/trunk/src'
 
set b = (HTML SQL Util Connect Table nom javalib)
foreach n ($b)
    echo "Copying: $n"
    cp  -r ./$n/* $jira/topleveldir/$n
end


set c  = (java css  doc data perl/Connect perl/HTML perl/Service  perl/Util perl)
foreach n ($c)
   echo "Copying: $n"
   cp ./$n/* $jira/$n
end


set e  = `ls *pl *.sh`
foreach n ($e)
    echo "Copying: $n"
    cp $n  $jira/topleveldir
end

