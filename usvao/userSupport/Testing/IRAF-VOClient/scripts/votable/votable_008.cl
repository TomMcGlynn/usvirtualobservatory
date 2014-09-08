vao
vaotest
vaotest.descr = "Test VOTable Copy"
fcache init
string myfile
myfile = data_url // "usno-b.xml"
tcopy(myfile, "mytest2.fits")
tinfo(myfile)
tinfo mytest2.fits
tdelete mytest2.fits
#
myfile = "file://" // data_path // "usno-b.xml"
tcopy(myfile, "mytest2.fits")
tinfo(myfile)
tinfo mytest2.fits
tdelete mytest2.fits
#
myfile = "file://localhost" // data_path // "usno-b.xml"
tcopy(myfile, "mytest2.fits")
tinfo(myfile)
tinfo mytest2.fits
tdelete mytest2.fits
