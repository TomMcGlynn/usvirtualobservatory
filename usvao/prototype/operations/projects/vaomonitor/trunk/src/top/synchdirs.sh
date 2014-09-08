#!/usr1/local/bin/tcsh 
#
#
#
set a = '/www/htdocs/vaomonitor_test/'
set b = (HTML SQLVao java_automonitor Stats Tests Types Util XML)
foreach n ($b)
    echo "Copying: $n"
    cp ./$n/* $a/$n
end
