#!/usr1/local/bin/tcsh 
#
#
#
set a = '/web_chroot.prod/.www_mountpnt/www/htdocs/vo/vaomonitor/'
set b = `ls -lp | grep '/' | awk '{print $NF}' | grep '^[A-Z]'`;
set c = (data css doc js)
foreach n ($b)
    echo "Copying: $n"
    cp ./$n/* $a/$n
end
foreach n ($c)
    echo "Copying: $n"
    cp ./$n/* $a/$n
end
echo "Copying *pl files"
cp vaomonitor.pl $a
cp vaodb.pl $a
cp vaostats.pl $a
cp vaostats_excel.pl $a
cp test.pl $a
exit
