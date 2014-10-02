#! /bin/csh -f 
#
if ($#argv == 0 || $argv[1] == "-h" || $argv[1] == "--help") then
    if ($#argv == 0) then
        echo "${0}: missing filename"
    endif
    echo "Usage: $0 file [docpath [cgipath]]"
    exit 1
endif

set file = $argv[1]
set docpath = "vopub"
if ($#argv > 1) then
    set docpath = $argv[2]
endif
if ($docpath =~ "/*") set docpath = `echo $docpath | cut -c 2-`
if ($docpath =~ "*/*") set docpath = `echo $docpath | sed -e 's/\//\\\//g'`
set cgipath = "cgi-bin/vopub"
if ($#argv > 2) then
    set cgipath = $argv[3]
endif
if ($cgipath =~ "/*") set cgipath = `echo $cgipath | cut -c 2-`
if ($cgipath =~ "*/*") set cgipath = `echo $cgipath | sed -e 's/\//\\\//g'`

# echo sed -e s/@CGIPATH@/\\/$cgipath/g -e s/@DOCPATH@/\\/$docpath/g $file
sed -e s/@CGIPATH@/\\/$cgipath/g -e s/@DOCPATH@/\\/$docpath/g $file
