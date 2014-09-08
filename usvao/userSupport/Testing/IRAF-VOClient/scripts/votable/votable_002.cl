vao
vaotest
vaotest.descr = "Test VOTable Handling; List Column information "
fcache init
tlcol data$usno-b.xml
string myfile
string listfile
listfile = "listfile"
myfile = data_url // "/usno-b.xml"
print(myfile, >> listfile)
tlcol(myfile)
myfile = "file://" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tlcol(myfile)
myfile = "file://localhost" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tlcol(myfile)
tlcol("@" // listfile)
delete(listfile)
