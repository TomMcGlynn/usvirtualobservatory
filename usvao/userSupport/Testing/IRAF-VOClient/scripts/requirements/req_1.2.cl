#
#  Demonstrate row and column selection in a VOTable.

# Set the test description string.
vaotest.descr = "Demonstrate row and column selection in a VOTable"

# Convert the data$logical to a local path.
s1 = data_url // "/usno-b.xml"
s2 = "file://" // data_path // "/usno-b.xml"
s3 = "file:///localhost" // data_path // "/usno-b.xml"

print ("------------------------------------------------------------------")
print ("Req  1.2:  Users shall be able to select specific rows and/or")
print ("           columns of a VOTable using the existing task functionality.")
print ("------------------------------------------------------------------")


fcache init

# Execute the test commands.
print ("\nLogical Path:  data$usno-b.xml")		# logical path
tprint ("data$usno-b.xml", columns="RA,DEC", rows="1-10")

print ("\nHTTP URI:  " // s1)				# remote http URI
tprint (s1, columns="RA,DEC", rows="1-10")

print ("\nFile URI:  " // s2)				# file URI
tprint (s2, columns="RA,DEC", rows="1-10")

print ("\nFile URI:  " // s3)				# file URI
tprint (s3, columns="RA,DEC", rows="1-10")
tabpar data$usno-b.xml I2 10
print("I2 = ",tabpar.value)
string myfile
string listfile
listfile = "listfile"

#Fetch a specific column and row
myfile = data_url // "/usno-b.xml"
print(myfile, >> listfile)
tabpar(myfile, "I2", 10)
print("I2 = ",tabpar.value)
myfile = "file://" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tabpar(myfile, "I2", 10)
print("I2 = ",tabpar.value)
myfile = "file://localhost" // data_path // "/usno-b.xml"
print(myfile, >> listfile)
tabpar(myfile, "I2", 10)
print("I2 = ",tabpar.value)
