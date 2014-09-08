vao
vaotest
vaotest.descr = "Test VOTable Operation on a Column"
fcache init
string myfile
string listfile
listfile = "listfile"
myfile = data_url // "/usno-b.xml"
print(myfile, >> listfile)
tstat(myfile, "B1")
myfile = "file://" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tstat(myfile, "B1")
myfile = "file://localhost/" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tstat(myfile, "B1")
tstat data$usno-b.xml "I2"
tstat("@" // listfile, "B1")
delete(listfile)
