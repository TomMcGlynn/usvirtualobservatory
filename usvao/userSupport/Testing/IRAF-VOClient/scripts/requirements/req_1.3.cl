#
#  Demonstrate selection of column for tasks expecting a list.

# Set the test description string.
vaotest.descr = "Demonstrate selection of column for tasks expecting a list"

# Convert the data$logical to a local path.
s1 = data_url // "/usno-b.xml"
s2 = "file://" // data_path // "/usno-b.xml"
s3 = "file:///localhost" // data_path // "/usno-b.xml"


print ("------------------------------------------------------------------")
print ("Req  1.3:  Users shall be able to select a column in a VOTable")
print ("           for use in tasks that expect a list of values.")
print ("------------------------------------------------------------------")


fcache init

# Execute the test commands.
print ("\nLogical Path:  data$usno-b.xml")		# logical path


print ("\nHTTP URI:  " // s1)				# remote http URI
tproject(s1, "mytab2.fits","B1 B2")
tdump mytab2.fits
tdelete mytab2.fits


print ("\nFile URI:  " // s2)				# file URI
tproject(s2, "mytab2.fits","B1 B2")
tdump mytab2.fits
tdelete mytab2.fits


print ("\nFile URI:  " // s3)				# file URI
tproject(s3, "mytab2.fits","B1 B2")
tdump mytab2.fits
tdelete mytab2.fits

tproject data$usno-b.xml  "mytab2.fits" "B1 B2"
tdump mytab2.fits
tdelete mytab2.fits

