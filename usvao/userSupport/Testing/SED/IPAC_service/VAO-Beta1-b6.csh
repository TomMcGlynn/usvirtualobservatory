#! /bin/csh
#
# This script copyright Caltech
#
# Configuration
set curl=(/usr/bin/curl)
set statsfilename=stats.txt
set outdir=test
## end of configuration
#
if ( $#argv > 0 ) then
  set outdir=$argv[1]
  echo "INFO: Test output to ${outdir}"
endif
set statsfile=${outdir}/${statsfilename}
if ( ! -d $outdir ) then
   echo "ERROR: cannot continue:  Directory $outdir does not exist"
   exit
endif
if ( -e ${statsfile} ) then
   echo "WARNING: found ${statsfile}: rename to ${statsfile}.old and continue"
   /bin/mv ${statsfile} ${statsfile}.old
endif
if ( -e ${statsfile} ) then
   echo "ERROR: cannot continue:  ${statsfile} persists"
   exit
endif
/bin/date -u "+%Y%m%dT%H%M%S" > ${statsfile}
/bin/cp $0 ${outdir}
set curlflags=(-sw "%{time_total},%{time_namelookup},%{time_connect},%{time_appconnect},%{time_pretransfer},%{time_redirect},%{time_starttransfer},%{size_download}")
# validation set definition
set domain=http://vaobeta.ipac.caltech.edu/services/
set noglob
set validationset=(\
'accessSED?REQUEST=getData&TARGETNAME=m31' \
'accessSED?REQUEST=getData&TARGETNAME=nnn' \
'accessSED?TARGETNAME=3c273' \
'accessSED?REQUEST=getData&TARGETNAME=' \
'querySED?REQUEST=queryData&TARGETNAME=mmmm' \
'querySED?REQUEST=queryData&TARGETNAME=3c273' \
'querySED?REQUEST=queryData&TARGETNAME=' \
'querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.01' \
'querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.1' \
'querySED?REQUEST=queryData&POS=187.2779154' \
'querySED?REQUEST=queryData&POS=187.2779154,uuu&SIZE=0.02' \
'querySED?REQUEST=queryData&POS=187.2779154,1.0&SIZE=' \
'querySED?REQUEST=queryData&POS=187.2779154,1.0&SIZE=0.8' \
'querySED?POS=187.2779154,2.0523883&SIZE=.01' \
'accessSED?REQUEST=getData&TARGETNAME=m31' \
'accessSED?REQUEST=getData&TARGETNAME=nnn' \
'accessSED?TARGETNAME=3c273' \
'accessSED?REQUEST=getData&TARGETNAME=' \
'querySED?REQUEST=queryData&TARGETNAME=mmmm' \
'querySED?REQUEST=queryData&TARGETNAME=3c273' \
'querySED?REQUEST=queryData&TARGETNAME=' \
'querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.01' \
'querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.1' \
'querySED?REQUEST=queryData&POS=187.2779154' \
'querySED?REQUEST=queryData&POS=187.2779154,uuu&SIZE=0.02' \
'querySED?REQUEST=queryData&POS=187.2779154,1.0&SIZE=' \
'querySED?REQUEST=queryData&POS=187.2779154,1.0&SIZE=0.8' \
'querySED?POS=187.2779154,2.0523883&SIZE=.01' \
'querySED' \
'querySED?REQUEST=queryData&POS=187.2779154,+95.6&SIZE=0.02' \
'querySED?REQUEST=queryData&POS=366.0,1.0.6&SIZE=0.02' \
'querySED?REQUEST=queryData&TARGETNAME=NGC%207839' \
'querySED?REQUEST=queryData&pos=1.75263,27.63521&size=0.01' \
'querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.01' \
'querySED?REQUEST=queryData&TARGETNAME=3c273' \
'accessSED?REQUEST=getData&TARGETNAME=3c273' \
)
# end validation set definition
set vq=0
foreach Q ( $validationset )
  @ vq ++
  echo "Test " $vq " $Q"
  echo -n $vq " $Q " >> ${statsfile}
  ${curl} ${curlflags} "${domain}${Q}" -o ${outdir}/${vq}.xml >> ${statsfile}
  echo "" >> ${statsfile}
end
/bin/date -u "+%Y%m%dT%H%M%S"
