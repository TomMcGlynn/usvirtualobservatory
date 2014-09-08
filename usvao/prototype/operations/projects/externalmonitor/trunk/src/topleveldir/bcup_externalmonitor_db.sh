#!/usr1/local/bin/tcsh

set a = `date '+%m.%d.%y'`
set dir = '/www/htdocs/vo/external_monitor/bcups';


echo $a

/usr/bin/mysqldump -h asddb.gsfc.nasa.gov  -umpreciad  -pMpVAO\!\@\#Temp monitor > $dir/monitor_db.$a

gzip $dir/monitor_db.$a


exit
