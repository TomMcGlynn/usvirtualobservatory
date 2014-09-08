# May 3, 2007

# pygrplib.t.py
# this file

# pygrplib.t
# unix sh script, to activate grpglue.t.py for running regression testing

# OUTFILE and/or INFILE
# global variables exported in grpglue.t


#------------  Beginning of Template ----------------------
# import the packge
from group import *
import numpy
import os  #used for environment vars
import pycrates #used for file input

#rename the function to get the environment variable for readability
getenv = os.environ.get

# get infile and outfile by invoking environment variables
# exported from "pygrpglue.t"
TESTID     = getenv('TESTID')
if( TESTID == None ):
  print "No TESTID specified\n"

OUTFILE = getenv('OUTFILE')
if( OUTFILE != None):
  OutFilePtr = open (OUTFILE, 'w')
  if( OutFilePtr == None):
    print "Unable to open %s\n" % OUTFILE

INFILE = getenv('INFILE')
if( INFILE != None):
  InFilePtr = pycrates.TABLECrate()
  InFilePtr.read(INFILE)
  if (InFilePtr == None):
    print "Unable to open %s\n" % INFILE


BINFILE = getenv('BINFILE')
if( BINFILE != None):
  BinFilePtr = pycrates.TABLECrate()
  BinFilePtr.read(BINFILE)
  if (BinFilePtr == None):
    print "Unable to open %s\n" % BINFILE

#------------  End of Template ----------------------


# !! 2
# Below are pecific subroutines for regression testing
#=============================================================================
#
#  --- Subroutines ---
#
#=============================================================================



# !!4
#=============================================================================
#
#   --- Main Routine ---
#
#=============================================================================

#Python does not have a native switch statement, so the code below
#tries to duplicate the C switch format/behavior

class switch(object):
    def __init__(self, value):
        self.value = value
        self.fall = False

    def __iter__(self):
        """Return the match method once, then stop"""
        yield self.match
        raise StopIteration

    def match(self, *args):
        """Indicate whether or not to enter a case suite"""
        if self.fall or not args:
            return True
        elif self.value in args:
            self.fall = True
            return True
        else:
            return False

#Switch statement for TESTID
for case in switch(TESTID):
  if case('test1'):
      i_stopspec = numpy.array([0,0,0,1,1,0,0,0,0,0,0])
      (o_group,o_qual) = grpNumCounts(numCounts=1000, countsArray=InFilePtr.get_column_values('COUNTS'), tabStops=i_stopspec, maxLength=0);
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test2'):
      i_stopspec = numpy.array([0,0,0,1,1,0,0,0,0,0,0])
      i_numchans = len(InFilePtr.get_column_values('COUNTS'))
      (o_group,o_qual) = grpNumBins(i_numchans, 5)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test3'):
      i_numchans = len(InFilePtr.get_column_values('COUNTS'))
      (o_group,o_qual) = grpBinWidth(i_numchans, 4)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test4'):
      i_stopspec = numpy.array([0,0,0,0,0,0,0,0,0,0,0])
      (o_group,o_qual) = grpSnr(InFilePtr.get_column_values('COUNTS'), 50, 0, i_stopspec, errorCol=InFilePtr.get_column_values('STAT_ERR'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test5'):
      i_stopspec = numpy.array([0,0,0,0,0,0,0,0,0,0,0])
      (o_group,o_qual) = grpAdaptive(InFilePtr.get_column_values('COUNTS'), 700, 3, i_stopspec)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test6'):
      (o_group,o_qual) = grpAdaptiveSnr(InFilePtr.get_column_values('COUNTS'), 17.0)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test7'):
      (o_group,o_qual) = grpMaxSlope(InFilePtr.get_column_values('CHANNEL'), InFilePtr.get_column_values('COUNTS'), 50)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test8'):
      (o_group,o_qual) = grpMinSlope(InFilePtr.get_column_values('CHANNEL'), InFilePtr.get_column_values('COUNTS'), 20)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test9'):
      i_binlow = numpy.array([1,4,7,10])
      i_binhigh = numpy.array([3,6,9,11])
      i_stopspec = numpy.array([0,0,0,0,1,0,0,0,0,0,0])
      (o_group,o_qual) = grpBin(InFilePtr.get_column_values('CHANNEL'), i_binlow, i_binhigh, i_stopspec)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test10'):
      (o_group,o_qual) = grpBinFile(InFilePtr.get_column_values('CHANNEL'), BinFilePtr.get_column_values('CHANNEL'), \
                                    BinFilePtr.get_column_values('GROUPING'), BinFilePtr.get_column_values('QUALITY'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('COUNTS'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test11'):
      i_binlow = numpy.array([0,50,100,150,200,250,300,350,600,700,800,900])
      i_binhigh = numpy.array([50,100,150,200,250,300,350,400,700,800,900,1000])
      i_stopspec = numpy.array([0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,1,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0])
      (o_group,o_qual) = grpBin(InFilePtr.get_column_values('PI'), i_binlow, i_binhigh, i_stopspec)
      print >> OutFilePtr, str(InFilePtr.get_column_values('CHANNEL'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('PI'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case('test12'):
      i_binlow = numpy.array([3.0])
      i_binhigh = numpy.array([7.5])
      (o_group,o_qual) = grpBin(InFilePtr.get_column_values('BOO'), i_binlow, i_binhigh)
      print >> OutFilePtr, str(InFilePtr.get_column_values('pi'))
      print >> OutFilePtr, str(InFilePtr.get_column_values('BOO'))
      print >> OutFilePtr, str(o_group)
      print >> OutFilePtr, str(o_qual)
      break
  if case(): # default
        print "Invalid TESTID"
OutFilePtr.close()
