/*************************************************************************

   Copyright (c) 2014 California Institute of Technology, Pasadena,
   California.    Based on Cooperative Agreement Number NCC5-626 between
   NASA and the California Institute of Technology. All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed at the Infrared Processing and Analysis
   Center (IPAC) and the Jet Propulsion Laboratory (JPL) by Bruce
   Berriman, John Good, Joseph Jacob, Daniel S. Katz, and Anastasia
   Laity.

*************************************************************************/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "redefine_pointing.h"
#define KEY_LENTH 8

/* read the values for keywords CRVAL1, CRVAL2, 
RA_MOVING, and DEC_MOVING
move crval1 and crval2 to account for the shift of the moving object
crval1 += (restRA - RA_Moving);
crval2 += (restDec - Dec_Moving);
*/

int moving_object_pointing_replacement(char *fitsheader, double restRA, double restDec,
							  int verbose){

  double crval1, crval2, RA_Moving, Dec_Moving;

  int ireturn = 0;
    
  if(parse_double(fitsheader,&crval1,CRVAL1)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword\n",CRVAL1);
      ireturn += 1;
  }	
    if(parse_double(fitsheader,&crval2,CRVAL2)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", CRVAL2);
      ireturn += 2;
    }
    if(parse_double(fitsheader,&RA_Moving,RA_MOVING)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", RA_MOVING);
      ireturn += 4;
    }
    if(parse_double(fitsheader,&Dec_Moving,DEC_MOVING)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", DEC_MOVING);
      ireturn += 8;
    }
    if(ireturn)
      return ireturn;

   crval1 += (restRA - RA_Moving);
   crval2 += (restDec - Dec_Moving);

  if(replace_keyword(fitsheader,crval1,CRVAL1)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CRVAL1);
    ireturn += 1;
  }
  if(replace_keyword(fitsheader,crval2,CRVAL2)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CRVAL2);
    ireturn += 2;
  }

  if(ireturn)
    return -ireturn;

  return 0;
}

/* read the values for keywords RARFND, DECRFND, CT2RFND
if all three are found replace the values for keywords
CRVAL1, CRVAL2, CROTA2 with the new values */
int redefine_pointing(char *fitsheader, int verbose){

  double newRA, newDec, newCROTA2;
  double newCD11, newCD12, newCD21, newCD22;

  int ireturn = 0;
  int have_cdmatrix = 0;

  if( !parse_double(fitsheader,&newRA,CD11) &&
      !parse_double(fitsheader,&newRA,CD12) &&
      !parse_double(fitsheader,&newRA,CD21) &&
      !parse_double(fitsheader,&newRA,CD22)){

    if(parse_double(fitsheader,&newCD11,RefinedCD11)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword\n",RefinedCD11);
      ireturn += 1;
    }
    if(parse_double(fitsheader,&newCD12,RefinedCD12)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", RefinedCD12);
      ireturn += 2;
    }
    if(parse_double(fitsheader,&newCD21,RefinedCD21)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", RefinedCD21);
      ireturn += 4;
    }
    if(parse_double(fitsheader,&newCD22,RefinedCD22)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%8s\" keyword \n", RefinedCD22);
      ireturn += 8;
    }
    if(ireturn)
      return ireturn;
    have_cdmatrix = 1;
  }    

 
  /* CDmatrix is not present look at CROTA*/
  if(parse_double(fitsheader,&newRA,RefinedCRVAL1)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't find \"%6s\" keyword\n",RefinedCRVAL1);
    ireturn += 1;
  }
  if(parse_double(fitsheader,&newDec,RefinedCRVAL2)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't find \"%7s\" keyword \n", RefinedCRVAL2);
      ireturn += 2;
  }
  if(!have_cdmatrix){
    if(parse_double(fitsheader,&newCROTA2,RefinedCROTA2)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't find \"%7s\" keyword \n", RefinedCROTA2);
      ireturn += 4;    
    }
  }

  if(ireturn)
    return ireturn;
    

  if(replace_keyword(fitsheader,newRA,CRVAL1)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CRVAL1);
    ireturn += 1;
  }
  if(replace_keyword(fitsheader,newDec,CRVAL2)){
    if(verbose)
      fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CRVAL2);
    ireturn += 2;
  }
  if(have_cdmatrix){
    if(replace_keyword(fitsheader,newCD11,CD11)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CD11);
      ireturn += 4;
    }
    if(replace_keyword(fitsheader,newCD12,CD12)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CD12);
      ireturn += 8;
    }
    if(replace_keyword(fitsheader,newCD21,CD21)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CD21);
      ireturn += 16;
    }
    if(replace_keyword(fitsheader,newCD22,CD22)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CD22);
      ireturn += 32;
    }

  }
  else{
    if(replace_keyword(fitsheader,newCROTA2,CROTA2)){
      if(verbose)
	fprintf(stderr, "redefine_pointing(): couldn't replace \"%s\" keyword \n",CROTA2);
      ireturn += 4;
    }
  }

  if(ireturn)
    return -ireturn;

  return 0;
}

int parse_str(char *fitsheader, char *value, const char *key){


  int i,length;
  char *temp;
  char empty[] = " ";
  char char_value[80];
  int key_length = KEY_LENTH;
  char mod_key[KEY_LENTH+2];

  strcpy(mod_key,key);
  length = strlen(mod_key);
  for(i=length;i<key_length;i++)
    strcat(mod_key," ");
  strcat(mod_key,"=");
  temp = strstr(fitsheader, mod_key);
  if(temp == NULL)
    return 1;
  temp = strchr(temp,'=');
  if(temp == NULL)
    return 1;
  while(*(++temp) == ' ');

  length = strcspn(temp,empty);
  if(length >= 80)
    return 1;
  strncpy(char_value,temp,length);
  char_value[length] = '\0';
  
  strcpy(value, char_value);
  
  return 0;
}

int parse_double(char *fitsheader, double *value, const char *key){


  int i,length;
  int key_length = KEY_LENTH;
  char *temp;
  char empty[] = " ";
  char char_value[80];
  char mod_key[KEY_LENTH+2];

  strcpy(mod_key,key);
  length = strlen(mod_key);
  for(i=length;i<key_length;i++)
    strcat(mod_key," ");
  strcat(mod_key,"=");
  temp = strstr(fitsheader, mod_key);
  if(temp == NULL)
    return 1;
  temp = strchr(temp,'=');
  if(temp == NULL)
    return 1;
  while(*(++temp) == ' ');

  length = strcspn(temp,empty);
  if(length >= 80)
    return 1;
  strncpy(char_value,temp,length);
  char_value[length] = '\0';
  
  *value = atof(char_value);
  
  return 0;
}

int parse_int(char *fitsheader, int *value, const char *key){


  int i,length;
  char *temp;
  char empty[] = " ";
  char char_value[80];

  int key_length = KEY_LENTH;
  char mod_key[KEY_LENTH+2];

  strcpy(mod_key,key);
  length = strlen(mod_key);
  for(i=length;i<key_length;i++)
    strcat(mod_key," ");
  strcat(mod_key,"=");
  temp = strstr(fitsheader, mod_key);
  if(temp == NULL)
    return 1;
  temp = strchr(temp,'=');
  if(temp == NULL)
    return 1;
  while(*(++temp) == ' ');

  length = strcspn(temp,empty);
  if(length >= 80)
    return 1;
  strncpy(char_value,temp,length);
  char_value[length] = '\0';
  
  *value = atoi(char_value);
  
  return 0;
}



int replace_keyword(char *fitsheader, double value, const char *key){


  int i,length, total_length, offset;
  char *temp;
  char empty[] = " ";
  char char_value[80];

  int key_length = KEY_LENTH;
  char mod_key[KEY_LENTH+2];

  strcpy(mod_key,key);
  length = strlen(mod_key);
  for(i=length;i<key_length;i++)
    strcat(mod_key," ");
  strcat(mod_key,"=");
  temp = strstr(fitsheader, mod_key);
  if(temp == NULL)
    return 1;
  temp = strchr(temp,'=');
  if(temp == NULL)
    return 1;
  if(*(++temp) == ' ')
    temp++;

  sprintf(char_value,"%9.8f",value);
  total_length = strlen(char_value);
  strncpy(temp,char_value,total_length);
  temp += total_length;
  while(*(temp) != ' '){
    *(temp++) = ' ';
  }
  
  return 0;
}
