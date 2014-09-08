vao
vaotest
vaotest.descr = "Test VOTable Handling; Info "
fcache init
# Convert the data$logical to a local path.
string myfile
string listfile
listfile = "listfile"
#
tinfo data$usno-b.xml
#
myfile = data_url // "usno-b.xml"
print(myfile, >>listfile)
tinfo(myfile)
#
myfile = "file://" // data_path // "usno-b.xml"
print(myfile, >>listfile)
tinfo(myfile)
#
myfile = "file:///localhost" // data_path // "usno-b.xml"
print(myfile, >>listfile)
tinfo(myfile)
listfile = "@" // listfile
#
tinfo(listfile)
delete "listfile"
