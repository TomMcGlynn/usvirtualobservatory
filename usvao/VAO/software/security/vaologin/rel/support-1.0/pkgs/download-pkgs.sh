#! /bin/bash
#
#  Download a list of URLs representing the vaologin prerequisite packages
#
#  Usage: download-pkgs.sh urls-file
#
[ "$1" = "" ] && {
    echo "download-pkgs.sh: Missing URLs file argument" 1>&2
    exit 1
}
[ -f "$1" -a -r "$1" ] || {
    echo "${1}: file not found (with read permission)" 1>&2
}

urlsfile=$1
dolist=$2
shift 2

[ "$dolist" = "list" ] || which wget > /dev/null || {
    echo "download-pkgs.sh: Unable to download: can't find wget" 1>&2
    exit 5
}

while read line; do
    line=`echo $line | sed -e 's/^\s*//' -e 's/\s*$//'`
    [ "$line" = "" ] && continue
    p=($line)
    [ ${#p[@]} -lt 3 ] && p[2]=`echo ${p[1]} | sed -e 's/.*\///'`

    [ "$dolist" = "list" ] && {
        { [ $# -eq 0 ] || { echo $@ | grep -Eqs ' '${p[0]}' |^'${p[0]}' | '${p[0]}'$|^'${p[0]}'$'; }; } && \
            echo ${p[0]}: ${p[2]}
        continue
    }

    [ -f "${p[2]}" ] && {
        echo "${p[2]}: already downloaded; skipping (remove to re-download)" 1>&2
        continue
    }

    wget -O ${p[2]} ${p[1]} || {
        echo "Warning: Problem downloading ${p[0]} from ${p[1]}" 1>&2
    }
done < $urlsfile
