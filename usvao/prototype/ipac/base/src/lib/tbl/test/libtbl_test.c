#include "libtbl_test.h"

int main(int argc, char **argv)
{
	if (argc > 1)
	{
		eTBLTestType eTest = (eTBLTestType)atoi(argv[1]);
	
		if (argc > 2 || (argc == 2 && eTest == NUM_FILES))
		{
			char strInputFile[256] = "";

			int nFileType = 0;
			
			if (eTest != NUM_FILES)
			{
				nFileType = atoi (argv[2]);
			}

			if (eTest != NUM_FILES && (nFileType > 3 || nFileType < 1))
			{
				printf ("You must specify a file type between 1 and 3.\n");
			}
			else
			{
				switch (eTest)
				{
					case COL_SEARCH:
					{
						/* we have six columns + 1 fake.  Let's retrieve them out of order... */
						printf ("Test getting column index by name (tbl_column()) on an %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							char * astrColumnNames[7] = {"Phase", "Date_WRT_Transit_Midpoint", "HJD", "Relative_Flux", "Accepted", "Abraxas", "Relative_Flux_Uncertainty" };
							strcpy (strInputFile, "input/SingleFile.tbl");
							column_index_test (strInputFile, astrColumnNames, 7);
						}
						else 
						{
							char *astrFITSColumnNames[7] = {"string_col", "float_col", "int_col", "foo", "double_col", "real_as_string", "date_as_string"};
							if (nFileType == FITS_ASCII)
							{
								strcpy (strInputFile, "input/SingleFile_ascii.FITS");
							}
							else if (nFileType == FITS_BINARY)
							{
								strcpy (strInputFile, "input/SingleFile_bin.FITS");
							}

							column_index_test (strInputFile, astrFITSColumnNames, 7);
						}

						break;
					}
					case COL_INFO:
					{
						printf ("Test getting column information (tbl_colinfo()) on a %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						column_info_test (strInputFile);

						break;
					}
					case COL_TYPE:
					{
						printf ("Test getting column type (tbl_type()) on a %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						column_type_test (strInputFile);

						break;
					}
					case COL_STRING_VALUE:
					{
						printf ("Test getting column values as strings (tbl_colstr) on a %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						column_string_value_test (strInputFile);

						break;
					}
					case COL_ACTUAL_VALUE:
					{
						/* we know that these three columns represent one of the three TBL_TYPES */
						char * astrColNames[3] = {"HJD", "Phase", "Accepted"};
						printf ("Test getting column values by type (tbl_colval) on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
							column_actual_value_test (strInputFile, astrColNames, 3);
						}
						else 
						{
							char *astrFITSColumnNames[6] = {"string_col", "float_col", "int_col", "double_col", "real_as_string", "date_as_string"};

							if (nFileType == FITS_ASCII)
							{
								strcpy (strInputFile, "input/SingleFile_ascii.FITS");
							}
							else if (nFileType == FITS_BINARY)
							{
								strcpy (strInputFile, "input/SingleFile_bin.FITS");
							}

							column_actual_value_test (strInputFile, astrFITSColumnNames, 6);
						}

						break;
					}
					case KEY_INFO:
					{
						/* try to get the 0th (first), 5th, 40th, 67th, 98th (last) and 112th keywords.  We expect the 112th to fail, as there are not that many keywords in our sample file */
						int nIndexes[7] = {-1, 0,5,67,40,112,98};
						printf ("Test getting key information (tbl_keyinfo()) on a %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						key_info_test (strInputFile, nIndexes, 7);

						break;
					}
					case KEY_VALUE:
					{
						/* try to get the PERIOD, MAXIMUM_DATE_UNITS, BASE_DATE, OBSERVATORY_SITE and FOO keywords.  We expect the FOO to fail, as there is no such keyword in our input file */
						char  * astrKeyNames[5] = {"PERIOD", "MAXIMUM_DATE_UNITS", "BASE_DATE", "OBSERVATORY_SITE", "FOO"};
						printf ("Test getting key values by name (tbl_keyval()) on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
							key_val_test (strInputFile, astrKeyNames, 5);
						}
						else 
						{
							char  * astrFITSKeyNames[5] = {"KEY023", "KEY001", "KEY099", "KEY056", "FOO"};

							if (nFileType == FITS_ASCII)
							{
								strcpy (strInputFile, "input/SingleFile_ascii.FITS");
							}
							else if (nFileType == FITS_BINARY)
							{
								strcpy (strInputFile, "input/SingleFile_bin.FITS");
							}

							key_val_test (strInputFile, astrFITSKeyNames, 5);
						}

						break;
					}
					case GET_REC:
					{
						printf ("Test getting a record (tbl_getrec()) on a %s file.\n", file_type_to_string(nFileType));

						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						get_record_test (strInputFile);

						break;
					}
					case FILE_INFO:
					{
						printf ("Test getting file information (tbl_fileinfo()) on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						file_info_test (strInputFile);

						break;
					}
					case NUM_FILES:
					{
						/* this test is independent of file type */
						printf ("Test opening more files than the nominal file number limit on a mixture of the various suported file types.\n");
						exceed_file_num_limit ();

						break;
					}
					case NUM_COLS:
					{
						printf ("Test reading more columns than the nominal limit on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/ManyColumns.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/ManyColumns_bin.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/ManyColumns_ascii.FITS");
						}

						exceed_col_num_limit (strInputFile);

						break;
					}
					case NUM_ROWS:
					{
						printf ("Test getting number of records (tbl_numcheck()) on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/SingleFile_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/SingleFile_bin.FITS");
						}

						number_check_test (strInputFile);

						break;
					}
					case NUM_KEYS:
					{
						printf ("Test reading more keywords than the nominal limit on a %s file.\n", file_type_to_string(nFileType));
						if (nFileType == ISIS_ASCII)
						{
							strcpy (strInputFile, "input/ManyKeys.tbl");
						}
						else if (nFileType == FITS_ASCII)
						{
							strcpy (strInputFile, "input/ManyKeys_ascii.FITS");
						}
						else if (nFileType == FITS_BINARY)
						{
							strcpy (strInputFile, "input/ManyKeys_bin.FITS");
						}
						exceed_key_num_limit (strInputFile);

						break;
					}
					default:
					{
						printf ("That is an unspecified test.\n");
						break;
					}
				}
			}
		}
		else
		{
			printf ("You must specify a test type (integer from 1 - 13) and a file type (integer from 1 - 3).\n");
		}
	}
	else
	{
		printf ("You must specify a test type (integer from 1 - 13) and a file type (integer from 1 - 3).\n");
	}
	

	return 0;
}


void file_info_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		iStatus = tbl_fileinfo (iTableHandle, &pstFileInfo);

		if (iStatus == -1)
		{
			printf ("  Call to tbl_fileinfo failed on %s.\n", pstrFileName);
		}
		else
		{
			printf ("  Call to tbl_fileinfo succeeded on %s.\n", pstrFileName);
			printf ("  --- File Information for %s ---\n", pstrFileName);
			printf ("      table index :      %8d\n",  iTableHandle);
			printf ("      number of columns: %8d\n",  pstFileInfo->ncols);
			printf ("      number of keyvals: %8d\n",  pstFileInfo->nkeywords);
			printf ("      number of rows:    %8lld\n",pstFileInfo->nrows);
			printf ("      file index   :     %8d\n",  pstFileInfo->file_handle);
			printf ("      file type    :     %8d (%s)\n", pstFileInfo->table_type, file_type_to_string(pstFileInfo->table_type));
		}
		
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}


void get_record_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
	char *pstrRecord = NULL;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		pstrRecord = tbl_getrec(iTableHandle);

		printf ("  Call to tbl_getrec on file %s retrieved the following:\n", pstrFileName);
		printf ("  --- Record for %s ---\n", pstrFileName);
		printf ("      [%s]\n",  pstrRecord);
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void key_info_test (char * pstrFileName, int * pastrKeyIndices, int nNumKeys)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		char strKeyName[256], strKeyVal[TBL_KEYLEN + 1];
		
		for (nCtr = 0; nCtr < nNumKeys; nCtr++)
		{
			iStatus = tbl_keyinfo(iTableHandle, pastrKeyIndices[nCtr], strKeyName, strKeyVal);
			
			if (iStatus == TBL_OK)
			{
				printf ("  Call to tbl_keyinfo on file %s retrieved key number %d:\n", pstrFileName, pastrKeyIndices[nCtr]);
				printf ("      %s = [%s]\n",  strKeyName, strKeyVal);
			}
			else
			{
				printf ("  Call to tbl_keyinfo on file %s could not retrieve key number %d:\n", pstrFileName, pastrKeyIndices[nCtr]);
			}
			
			strKeyName[0] = '\0';
			strKeyVal[0] = '\0';
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void key_val_test (char * pstrFileName, char * pastrKeyNames[], int nNumKeys)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0, nKeyIndex;
		char strKeyVal[TBL_KEYLEN + 1];
		
		for (nCtr = 0; nCtr < nNumKeys; nCtr++)
		{
			iStatus = tbl_keyval(iTableHandle, pastrKeyNames[nCtr], strKeyVal, &nKeyIndex);
			
			if (iStatus == 0)
			{
				printf ("  Call to tbl_keyval on file %s retrieved %s key:\n", pstrFileName, pastrKeyNames[nCtr]);
				printf ("      %s = [%s], index = [%d]\n",   pastrKeyNames[nCtr], strKeyVal, nKeyIndex);
			}
			else
			{
				printf ("  Call to tbl_keyinfo on file %s could not retrieve key %s:\n", pstrFileName, pastrKeyNames[nCtr]);
			}
			
			strKeyVal[0] = '\0';
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void number_check_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
	long long llNumRecords;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		iStatus = tbl_numcheck(iTableHandle, (long long *) (long long *) &llNumRecords);
		
		if (iStatus == 0)
		{
			printf ("  Call to tbl_numcheck on file %s reports %lld rows.\n", pstrFileName, llNumRecords);
		}
		else
		{
			printf ("  Call to tbl_numcheck on file %s failed with this status: [%d]\n", pstrFileName, iStatus);
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void column_type_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		
		printf ("  Call tbl_type on %s.\n", pstrFileName);
		/* let's go one further to see what it does when you ask for a column that doesn't exist */
		for (nCtr = 0; nCtr <= pstFileInfo->ncols; nCtr++)
		{
			iStatus = tbl_type(iTableHandle, nCtr);
			
			printf ("  Type for column %d = [%s]\n", nCtr, type_to_string(iStatus));
		}
	
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void column_info_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		struct tbl_colinfo stColInfo;
		
		printf ("  Call tbl_colinfo on %s.\n", pstrFileName);
		/* let's start with a negative index and go one further than the number of columns to see what it does when you ask for columns that doesn't exist */
		for (nCtr = -1; nCtr <= pstFileInfo->ncols; nCtr++)
		{
			memset (&tbl_colinfo, sizeof (struct tbl_colinfo), 0);
            iStatus = tbl_colinfo(iTableHandle, nCtr, &stColInfo); 
			 
			if (iStatus == TBL_OK)
			{
				printf ("  --- Column Information: [%s] ---\n", stColInfo.name);
				printf ("     Data Type =  [%6s]\n", stColInfo.data_type);
				printf ("     Unit =       [%8s]\n", stColInfo.unit);
				printf ("     Null =       [%4s]\n", stColInfo.null_string);
				printf ("     Scale =      [%8s]\n", stColInfo.scale);
				printf ("     Offset =     [%8s]\n", stColInfo.offset);
				printf ("     Display =    [%6s]\n", stColInfo.display);
				printf ("     Byte Width = %4d\n", stColInfo.byte_width);
				printf ("     End Col =    %4d\n", stColInfo.endcol);
				printf ("\n");
			}
			else
			{
				printf ("  Call to tbl_colinfo on file %s failed for column [%d].\n", pstrFileName, nCtr);  
			}
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}


void column_string_value_test (char * pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		/* for each column... */
		int nCtr = 0;
		char *pstrStringVal = NULL;

		printf ("  Call tbl_colstr on %s.\n", pstrFileName);
		/* let's go one further to see what it does when you ask for a column that doesn't exist */
		for (nCtr = 0; nCtr <= pstFileInfo->ncols; nCtr++)
		{
			pstrStringVal = tbl_colstr(iTableHandle, nCtr, nCtr, &iStatus);
			
			if (iStatus == 0)
			{
				printf("    Call to tbl_colstr on file %s[%d:%d] returned this value: [%s]\n", pstrFileName, nCtr, nCtr, pstrStringVal);
			}	
			else
			{
				printf("    Call to tbl_colstr on file %s[%d:%d] failed with this status: [%d]\n", pstrFileName, nCtr, nCtr, iStatus);
			}
			
			pstrStringVal = NULL;
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void column_actual_value_test (char * pstrFileName, char * pastrColumnNames[], int nNumColumns)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		/* for each column... */
		int nColCtr = 0, nTypeCtr = 0;
		int anTypes[3] = {TBL_CHARACTER, TBL_DOUBLE, TBL_INTEGER};
		int iColumnIndex;
		
				
		printf ("  Call tbl_colval on %s.\n", pstrFileName);

		/* get one of each column by it's own type, and by the other types - we want to test conversion success and failure */
		for (nColCtr = 0; nColCtr < nNumColumns; nColCtr++)
		{
			/* get info for column */
			iColumnIndex = tbl_column(iTableHandle, pastrColumnNames[nColCtr]);
			int nType = tbl_type(iTableHandle, iColumnIndex);
		
			for (nTypeCtr = 0; nTypeCtr < 3; nTypeCtr++)
			{
				printf ("  Get value for %s, of type %s, as type %s:\n", 
						pastrColumnNames[nColCtr], 
						type_to_string(nType), 
						type_to_string(anTypes[nTypeCtr]));
				
				switch(anTypes[nTypeCtr]) 
				{
	    			case TBL_DOUBLE:
					{
						double dVal;
		 				iStatus = tbl_colval (iTableHandle, 0, iColumnIndex, anTypes[nTypeCtr], &dVal);
				
               			if (iStatus != TBL_OK)
						{
							printf ("    Retrieval of value as stated type failed with this status: [%d].\n", iStatus);
						}
						else
						{
							printf ("    Value is %g.\n", dVal);
						}
						
						break;
					}
	    			case TBL_INTEGER:
					{
						long int iVal;
		 				iStatus = tbl_colval (iTableHandle, 0, iColumnIndex, anTypes[nTypeCtr], &iVal);
				
               			if (iStatus != TBL_OK)
						{
							printf ("    Retrieval of value as stated type failed with this status: [%d].\n", iStatus);
						}
						else
						{
							printf ("    Value is %ld.\n", iVal);
						}
						
						break;
					}
	    			case TBL_CHARACTER:
					{
						char * pstrVal = NULL; 
		 				iStatus = tbl_colval (iTableHandle, 0, iColumnIndex, anTypes[nTypeCtr], &pstrVal);
				
               			if (iStatus != TBL_OK)
						{
							printf ("    Retrieval of value as stated type failed with this status: [%d].\n", iStatus);
						}
						else
						{
							printf ("    Value is %s.\n", pstrVal);
						}
						
						break;
					}
				}

			}
		}
		
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void column_index_test (char * pstrFileName, char * pastrColumnNames[], int nNumColumns)
{
	int iTableHandle, iColumnIndex = 0, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		
		for (nCtr = 0; nCtr < nNumColumns; nCtr++)
		{
			iColumnIndex = tbl_column(iTableHandle, pastrColumnNames[nCtr]);
			
			if (iColumnIndex < 0)
			{
				printf ("  Call to tbl_column on file %s could not find column %s.\n", pstrFileName, pastrColumnNames[nCtr]);
			}
			else
			{
				printf ("  Call to tbl_column on file %s reported column %s has index [%d]:\n", pstrFileName, pastrColumnNames[nCtr], iColumnIndex);
			}
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void exceed_key_num_limit (char *pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_filinfo	*pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		char strKeyName[256], strKeyVal[TBL_KEYLEN + 1];
		
		printf ("  The nominal limit on the number of keywords is %d.\n", TBL_KEYNUM);
		printf ("  %s has %d keywords.\n", pstrFileName, pstFileInfo->nkeywords);
		printf ("  The additional keys are:\n");
		
		for (nCtr = TBL_KEYNUM; nCtr <= pstFileInfo->nkeywords; nCtr++)
		{
			iStatus = tbl_keyinfo(iTableHandle, nCtr - 1, strKeyName, strKeyVal);
			
			if (iStatus == 0)
			{
				printf ("      %s = [%s]\n",  strKeyName, strKeyVal);
			}
			else
			{
				printf ("      Call to tbl_keyinfo on file %s could not retrieve key number %d:\n", pstrFileName, nCtr);
			}
			
			strKeyName[0] = '\0';
			strKeyVal[0] = '\0';
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void exceed_col_num_limit (char *pstrFileName)
{
	int iTableHandle, iStatus;
  	struct tbl_colinfo	stColInfo;
  	struct tbl_filinfo	* pstFileInfo;

	iTableHandle = tbl_open(pstrFileName, &pstFileInfo);
	if (iTableHandle < 0)
	{
		printf ("  Failed to open file %s.\n", pstrFileName);
	}
	else
	{
		int nCtr = 0;
		
		printf ("  The nominal limit on the number of columns is %d.\n", TBL_MAXCOLS);
		printf ("  %s has %d columns.\n", pstrFileName, pstFileInfo->ncols);
		printf ("  The additional columns are:\n");
		
		for (nCtr = TBL_MAXCOLS; nCtr < pstFileInfo->ncols; nCtr++)
		{
			iStatus = tbl_colinfo(iTableHandle, nCtr, &stColInfo);
			
			if (iStatus == 0)
			{
				printf ("      %s = [%s]\n",  stColInfo.name, tbl_colstr(iTableHandle, 0, nCtr, &iStatus));
			}
			else
			{
				printf ("      Call to tbl_colinfo on file %s could not retrieve key number %d:\n", pstrFileName, nCtr);
			}
		}
	
		iStatus = tbl_close(iTableHandle);
		
		if (iStatus == -1)
		{
			printf ("  Failed to close file %s.\n", pstrFileName);
		}
	}
	
	printf ("\n");
}

void exceed_file_num_limit (void)
{
	int iTableHandle, nCtr = 1;
	int nNumFilesOpened = 0;
	char * pFileName = NULL;
	char strFiles[11][256] = {"input/ManyTables1.tbl", "input/ManyTables2.tbl", "input/ManyTables3.tbl",
							  "input/ManyTables1_bin.FITS", "input/ManyTables2_bin.FITS", "input/ManyTables3_bin.FITS",
							   "input/ManyTables1_ascii.FITS", "input/ManyTables2_ascii.FITS", "input/ManyTables3_ascii.FITS",
							   "input/ManyTables4.tbl", "input/ManyTables5.tbl"
	};
	int anFileHandles[11];
  	struct tbl_filinfo	*pstFileInfo;
	
	memset (anFileHandles, -1, 11);
	
	printf ("  The nominal limit on the number of files is %d.\n", TBL_MAXFILES);
	for (nCtr = 1; nCtr <= 11; nCtr++)
	{
		pFileName = strFiles[nCtr - 1];
	
//		printf ("Open file %d.\n", nCtr); fflush(stdout);
		
		iTableHandle = tbl_open(pFileName, &pstFileInfo);
		
		if (iTableHandle >= 0)
		{
			nNumFilesOpened++;
			printf ("    Successfully opened file number %d, %s.\n", nCtr, pFileName);
		}
		else
		{
			printf ("    Failed to open file number %d, %s.\n", nCtr, pFileName);
		}
		
		//fflush(stdout);	
		anFileHandles[nCtr - 1] = iTableHandle;
	}

	printf (" Opened %d files concurrently.\n", nNumFilesOpened);

	/* close files */
	for (nCtr = 1; nCtr <= 11; nCtr++)
	{
		if (anFileHandles[nCtr - 1] > 0)
		{
			tbl_close(anFileHandles[nCtr - 1]);
		}
	}

	printf ("\n");
}

char * type_to_string (int nType)
{
	char *pstrReturnVal = NULL;
	static char strDouble[] = "double";
	static char strInteger[] = "integer";
	static char strCharacter[] = "character";
	static char strUnknown[] = "unknown";
	
	switch (nType)
	{
		case TBL_DOUBLE:
		{
			pstrReturnVal = strDouble;
			break;
		}
		case TBL_INTEGER:
		{
			pstrReturnVal = strInteger;
			break;
		}
		case TBL_CHARACTER:
		{
			pstrReturnVal = strCharacter;
			break;
		}
		default:
		{
			pstrReturnVal = strUnknown;
			break;
		}
	}

	return pstrReturnVal;
}

char * file_type_to_string (int nType)
{
	char *pstrReturnVal = NULL;
	static char strFITSASCII[] = "FITS ASCII";
	static char strFITSBinary[] = "FITS Binary";
	static char strIPAC[] = "IPAC ASCII";
	static char strUnknown[] = "unknown";
	
	switch (nType)
	{
		case FITS_ASCII:
		{
			pstrReturnVal = strFITSASCII;
			break;
		}
		case FITS_BINARY:
		{
			pstrReturnVal = strFITSBinary;
			break;
		}
		case ISIS_ASCII:
		{
			pstrReturnVal = strIPAC;
			break;
		}
		default:
		{
			pstrReturnVal = strUnknown;
			break;
		}
	}
	
	return pstrReturnVal;
}
