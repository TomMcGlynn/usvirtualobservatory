#
#  Demonstrate general use of VOTable documents.

# Set the test description string.
vaotest.descr = "Demonstrate general use of VOTable documents."


print ("------------------------------------------------------------------")
print ("Req  1:  Users shall be able to use a VOTable in places where")
print ("	 tasks accept tabular data in other formats (ASCII files,")
print ("	 FITS bintables, .tab files, etc) for input.")
print ("------------------------------------------------------------------")


fcache init

# Demonstration of the general use of VOTables is done using the various
# individual test scripts for VOTable access.  Specific uses of VOTable
# are demonstrated using the detailed requirement test scripts.

# test votable

