#!/usr1/local/bin/tcsh

set a = `date '+%m.%d.%y'`
set dir = '/www/htdocs/vo/vaomonitor/'


echo $a

/usr/bin/mysqldump -h asddb.gsfc.nasa.gov  -umpreciad  -pMpVAO\!\@\#Temp vaomonitor > $dir/bcups/vaomonitor_db.$a

gzip $dir/bcups/vaomonitor_db.$a


exit
