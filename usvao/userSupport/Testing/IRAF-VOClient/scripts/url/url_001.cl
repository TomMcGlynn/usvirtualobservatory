vao
vaotest
vaotest.descr = "Rudimentary URL handling"
fcache init
string myfile
string listfile
listfile = "listfile"
#
myfile = data_url // "usno-b.xml"
tlcol(myfile)
myfile = "file://"// data_path // "usno-b.xml"
#
tlcol(myfile)
type http://casa.nrao.edu/index.shtml
type http://casa.nrao.edu/index.html
#
type http://nraovo-vm1.aoc.nrao.edu/ivoa-dal/scsVlaObs?REQUEST=queryData&RA=180.0&DEC=1.0&SR=1.0&FROM=OBSSUMMARY&FORMAT=votable
tlcol http://nraovo-vm1.aoc.nrao.edu/ivoa-dal/scsVlaObs?REQUEST=queryData&RA=180.0&DEC=1.0&SR=1.0&FROM=OBSSUMMARY&FORMAT=votable
#
myfile = data_url // "dpix.fits"
print(myfile, >> listfile)
imstat(myfile)
#
myfile = "file://" // data_path // "dpix.fits"
print(myfile, >> listfile)
imstat(myfile)
print("http://casa.nrao.edu/vao/dpix.fits", >> listfile)
imstat http://casa.nrao.edu/vao/dpix.fits
#
imstat("@" // listfile)
fcache init
imstat ("@" // listfile)
delete(listfile)
