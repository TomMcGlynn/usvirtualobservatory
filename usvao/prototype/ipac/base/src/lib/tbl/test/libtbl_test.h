#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <math.h>
#include "../tbl.h"

typedef enum testtype {COL_SEARCH = 1, COL_INFO = 2, COL_TYPE = 3, COL_STRING_VALUE = 4, COL_ACTUAL_VALUE = 5, KEY_INFO = 6, KEY_VALUE = 7, GET_REC = 8, FILE_INFO = 9,
					   NUM_FILES = 10, NUM_COLS = 11, NUM_ROWS = 12, NUM_KEYS = 13 } 
eTBLTestType;

/* test case functions */
void file_info_test (char * pstrFileName);

void get_record_test (char * pstrFileName);

void key_info_test (char * pstrFileName, int * pastrKeyIndices, int nNumKeys);

void key_val_test (char * pstrFileName, char * pastrKeyNames[], int nNumKeys);

void number_check_test (char * pstrFileName);

void column_type_test (char * pstrFileName);

void column_info_test (char * pstrFileName);

void column_string_value_test (char * pstrFileName);

void column_actual_value_test (char * pstrFileName, char * pastrColumnNames[], int nNumColumns);

void column_index_test (char * pstrFileName, char * pastrColumnNames[], int nNumColumns);

void exceed_key_num_limit (char *pstrFileName);

void exceed_col_num_limit (char *pstrFileName);

void exceed_file_num_limit (void);

/* helper functions */
char * type_to_string (int nType);

char * file_type_to_string (int nType);
