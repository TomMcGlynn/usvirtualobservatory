#!/bin/csh
while (.$1 != .) 
    sed -e s,/vo/squery,/vo/test/squery, -e s,/server/vo,/server/vo/test, $1 > tmp.1
    mv tmp.1 $1
    chmod 755 $1
    shift
end
