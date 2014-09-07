#! /bin/bash
#
# Insert properties into a hibernate_cfg.xml file
# 
# Usage: update_hibernate_cfg.sh <srcxml> <propsfile> <destfile>
#
# Args:
#  <srcxml>      the template hibernate_cfg.xml file
#  <propsfile>   the properties file containing values to insert 
#  <destfile>    the path to the file to write out
#
prog=`basename $0`

[ $# -lt 1 ] && {
    echo Missing arguments:  "<srcxml> <propsfile> <destfile>"
    exit 1
}
[ $# -lt 2 ] && {
    echo Missing arguments:  "<propsfile> <destfile>"
    exit 1
}
[ $# -lt 3 ] && {
    echo Missing arguments:  "<destfile>"
    exit 1
}
[ -r "$1" ] || {
    echo Source template file not found: $1
    exit 1
}
[ -r "$2" ] || {
    echo Input properties file not found: $2
    exit 1
}

tmpdir=`dirname $2` 
sed -E -e '/^ *#/ d' -e 's/\//\\\//g' \
       -e 's/ *= */\/ s\/>.*<\\\/property\/>/' -e 's/^ */\/<property name="/' \
       -e 's/ *$/<\\\/property\//' $2 > $tmpdir/update_hibernate_cfg.sed ||   \
{
    echo sed script creation failed
    exit 2
}
sed -f $tmpdir/update_hibernate_cfg.sed $1 > $3 || {
    echo sed script tranformation failed
    exit 3
}



