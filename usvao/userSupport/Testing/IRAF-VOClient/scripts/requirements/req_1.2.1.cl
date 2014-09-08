#
#  Demonstrate column selection by id/name/ucd <FIELD> attribute.

# Set the test description string.
vaotest.descr = "Demonstrate column selection by id/name/ucd <FIELD> attribute"

# Convert the data$logical to a local path.
s1 = data_url // "/usno-b.xml"
s2 = "file://" // data_path // "/usno-b.xml"
s3 = "file:///localhost" // data_path // "/usno-b.xml"

print ("------------------------------------------------------------------")
print ("Req  1.2.1: Users shall be able to identify a column in a VOTable")
print ("            by the 'id', 'name' or 'ucd' attribute of a <FIELD> or")
print ("            by column number.")
print ("------------------------------------------------------------------")


fcache init

# Execute the test commands.
print ("\nLogical Path:  data$usno-b.xml")		# logical path
print ("Not Yet Implemented")

print ("\nHTTP URI:  " // s1)				# remote http URI
print ("Not Yet Implemented")

print ("\nFile URI:  " // s2)				# file URI
print ("Not Yet Implemented")

print ("\nFile URI:  " // s3)				# file URI
print ("Not Yet Implemented")
