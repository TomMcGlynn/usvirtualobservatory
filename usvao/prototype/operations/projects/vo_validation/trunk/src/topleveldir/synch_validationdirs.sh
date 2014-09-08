#!/usr1/local/bin/tcsh
#
#
#
# synch validation dev and prod areas
#

set prod = '/www.prod/htdocs/vo/validation';

cp -R HTML/   $prod/
cp -R SQL/    $prod/
cp -R Util    $prod/ 
cp -R css/    $prod/
cp -R Table/  $prod/
cp -R data/   $prod/
cp -R java/   $prod/
cp -R perl/   $prod/
cp -R doc/    $prod/
cp -R nom/    $prod/
cp -R Connect/ $prod/


set  a = `ls *pl | grep -v run_java.pl`;
foreach n ($a)
   cp $n $prod;
end
