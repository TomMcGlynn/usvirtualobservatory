#!/usr1/local/bin/tcsh 
#
#
#


set jira = '/software/jira/software2/projects/vaomonitor/trunk/src'
 

set b = `ls -lp | grep '/' | awk '{print $NF}' | grep '^[A-Z]'`;


foreach n ($b)
    if(! -d $jira/top/$n) then
    mkdir $jira/top/$n
    endif
    echo "Copying: $n"
    cp -R $n $jira/top/
end


set c  = `ls -lp | grep '/' | awk '{print $NF}' | grep '^[a-z]' |grep -v bcups`
foreach n ($c)
    if (! -d $jira/$n) then
    mkdir $jira/$n
    endif
    echo "Copying: $n"
    cp -R $n  $jira
end


set e  = `ls *pl  *.sh`
foreach n ($e)
    echo "Copying: $n"
    cp $n  $jira/top
end

set g = `ls -F | grep -v '/' | grep -v '*'`
foreach n ($g)
   echo "Copying: $n"
   cp $n $jira/top
end

