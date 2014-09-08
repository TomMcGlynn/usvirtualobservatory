#!/bin/sh

# useage: > scc.sh <upload-tble-name> <radius> <search_catalog>
# for example:
# > scc.sh TSC_tbl.txt 10. IRAC_PSC
#
# supported catalogs: 
#    IRAS_PSC, SDSS_DR7, TWOMASS_PSC, and USNO_B1 - these are still changing

# Upload table specified in argument #1
echo "Uploading the file: " [$1]
echo ""

# remote path variable set to the path where the table is now uploaded to for compare
remotepath=`curl -F "file=@$1" http://vao-web.ipac.caltech.edu/cgi-bin/VAOPortal/nph-fileupload`
echo "File uploaded, remote path = $remotepath"
echo ""

# call the catalogCompare, using arguement #2 for radius, and arg #3 for Catalog
outputxml=`curl -d "maxdist=$2&tableA=$remotepath&tableB=$3&custom_cntr1=cntr&custom_ra1=ra&custom_dec1=dec" http://vao-web.ipac.caltech.edu/cgi-bin/VAOPortal/nph-catalogCompare`

# display the results (for testing, it may be best to alter this so
# that we pip the results to a file so repeated tests can be called and
# multiple results captured).
echo $outputxml
echo ""

## # this (untested) statement could be used to gather results in a file named
## # in the format of "<tablename>_<radius>_<catalogname>_cross-compare"
##
## echo $outputxml >& ${1}_${2}_${3}_cross-compare
## echo ""
