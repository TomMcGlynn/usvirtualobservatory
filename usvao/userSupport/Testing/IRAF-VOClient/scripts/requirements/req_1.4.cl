#
#  Demonstrate support for latest VOTable standard spec.

# Set the test description string.
vaotest.descr = "Demonstrate support for latest VOTable standard spec."

# Convert the data$logical to a local path.
s1 = data_url // "/usno-b.xml"
s2 = "file://" // data_path // "/usno-b.xml"
s3 = "file:///localhost" // data_path // "/usno-b.xml"

print ("------------------------------------------------------------------")
print ("Req  1.4:  VOTable Interface code will support the IVOA Standard")
print ("           specification of the VOTable format at time of release.")
print ("------------------------------------------------------------------")


fcache init

# Execute the test commands.
print ("\nLogical Path:  data$usno-b.xml")		# logical path


print ("\nHTTP URI:  " // s1)				# remote http URI
tcopy(s1, "mytest.fits")
tinfo(s1)
tinfo mytest.fits
tdelete mytest.fits


print ("\nFile URI:  " // s2)				# file URI
tcopy(s2, "mytest.fits")
tinfo(s2)
tinfo mytest.fits
tdelete mytest.fits


print ("\nFile URI:  " // s3)				# file URI
tcopy(s3, "mytest.fits")
tinfo(s3)
tinfo mytest.fits
tdelete mytest.fits

