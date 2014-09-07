#! /bin/bash
#
#  Usage -- runRI1testqueries.sh stylesheet samplesdir outputfile
#
prog=`basename $0`
label=`echo $prog | sed -e 's/\.sh$//'` 
makepretty=$PRETTY
makepretty=1
function usage {
    echo "Usage: $prog stylesheet samplesdir outputfile" 1>&2
}

function errmsg {
    echo ${label}: $1 1>&2
}

function fail {
    exval=$2
    [ -z "$exval" ] && exval=2
    errmsg $1
    exit $exval
}

function clifail {
    errmsg "$@"
    usage
    exit 1
}

function do_xsltproc {
    pretty="--param pretty 1"
    pretty_param=
    [ -n "$makepretty" ] && pretty_param=$pretty
    xsltproc $pretty_param $1 $2
}
function do_xalan {
    pretty="-PARAM pretty 1"
    pretty_param=
    [ -n "$makepretty" ] && pretty_param=$pretty
    xalan $pretty_param -XSL $1 -IN $2
}
function do_xslt {
    $xslt $1 $2
}
xslt=do_xalan

##########
# main
##########

sheet=$1
indir=$2
outfile=$3

[ -z "$sheet" ] && clifail "missing arguments"
[ -z "$indir" ] && clifail "missing input directory"
[ -z "$outfile" ] && clifail "missing output filename"

[ -f "$sheet" ] || fail "${sheet}: not an existing file"
[ -d "$indir" ] || fail "${indir}: not an existing directory"
if [ -e "$outfile" ]; then
    [ -w "$outfile" ] || fail "${outfile}: write permission failed"
else
    outdir=`dirname $outfile`
    [ -d "$outdir" -a -w "$outdir" ] || {
        fail "${outdir}: notan existing diretory with write permission"
    }
fi

testfiles=(`ls $indir/*.xml`)
[ ${#testfiles[@]} -eq 0 ] && fail "No test files found in input dir: $indir"
echo -n Processing ${#testfiles[@]} "files "

for file in "${testfiles[@]}"; do
    echo -n "." 1>&2
    echo "-- File: $file:" >> $outfile
    do_xslt $sheet $file  >> $outfile
    echo >> $outfile
    echo >> $outfile
done

echo " done."

