#!/bin/sh

# Note: either provide full relative local path for the upload table, or 
# execute from the directory containing the upload table.
#
# supported catalogs: 
#    IRAS_PSC, SDSS_DR7, TWOMASS_PSC, and USNO_B1 - these are still changing


# John's original command line in the script:
# scc.sh tmass.tbl 10. USNO_B1

# New command line - uploading a TSC_tbl.txt, radius 10, compare to catalog IRAS_PSC:
./scc.sh TSC_tbl.txt 10. IRAC_PSC

# format for adding more lines:
# ./scc.sh <tableToUpload> <radius> <Catalog>


