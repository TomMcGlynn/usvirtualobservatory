#!/bin/sh

rm -f actual_output/*

# the arguments to the test application are the testtype, an integer from 1 to 13, and a file type (test 10, testing the max number of files
#  limit, does not require a file type), an integer from 1 to 3.  3 is for IPAC ASCII, 1 and 2 are FITS formats.
#
# nominally, the tbl library reads FITS ASCII and FITS Binary files, but in fact a lot of the functionality does not seem to work properly
#  at present.  Since the IPAC format is what is commonly in use, with no known use of FITS, we will proceed with only IPAC ASCII tests for now,
#  and put a lien on the library to assess the FITS support later (remove or fix). (DLM conversation with JCG, 8/6/09)

# test searching for a column by name
./libtbl_test 1 3 > actual_output/column_search.txt

# test getting column info by index
./libtbl_test 2 3 > actual_output/column_info.txt

# test getting column types
./libtbl_test 3 3 > actual_output/column_type.txt

# test getting column values as strings, regardless of type
./libtbl_test 4 3 > actual_output/column_val_by_string.txt

# test getting column values by their actual type.  test conversion
./libtbl_test 5 3 > actual_output/column_val_by_type.txt

# test getting keyword information by index
./libtbl_test 6 3 > actual_output/key_info.txt

# test getting keyword value by name
./libtbl_test 7 3 > actual_output/key_value.txt

# test getting a record from the file
./libtbl_test 8 3 > actual_output/get_record.txt

# test getting file information
./libtbl_test 9 3 > actual_output/file_info.txt

# test opening more files than the nominal limit
./libtbl_test 10 > actual_output/file_limit.txt

# test opening a file with more columns than the nominal limit
./libtbl_test 11 3 > actual_output/column_limit.txt

# test reading the number of rows in a file
./libtbl_test 12 3 > actual_output/num_records.txt

# test opening a file with more keywords than the nominal limit
./libtbl_test 13 3 > actual_output/keyword_limit.txt
