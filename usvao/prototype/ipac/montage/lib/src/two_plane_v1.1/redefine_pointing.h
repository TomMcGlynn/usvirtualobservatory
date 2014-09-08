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



#ifndef _redefine_pointing_h_
#define _redefine_pointing_h_
const char *RefinedCRVAL1 = "RARFND";
const char *RefinedCRVAL2 = "DECRFND";
const char *RefinedCROTA2 = "CT2RFND";
const char *CRVAL1 = "CRVAL1";
const char *CRVAL2 = "CRVAL2";
const char *CROTA2 = "CROTA2";
const char *RA_MOVING = "RA_REF";
const char *DEC_MOVING = "DEC_REF"; 

const char *CD11 = "CD1_1";
const char *CD12 = "CD1_2";
const char *CD21 = "CD2_1";
const char *CD22 = "CD2_2";

const char *RefinedCD11 = "CD11RFND";
const char *RefinedCD12 = "CD12RFND";
const char *RefinedCD21 = "CD21RFND";
const char *RefinedCD22 = "CD22RFND";

/*if unable to read the refined values return value
has the bit corresponding to the failed keywords set
if unable to rewrite the refined values return value
has the bit corresponding to the failed keywords set
times (-1)
*/
int redefine_pointing(char *fitsheader, int verbose);
int parse_double(char *fitsheader, double *value, const char *key);
int parse_int(char *fitsheader, int *value, const char *key);
int replace_keyword(char *fitsheader, double value, const char *key);
int moving_object_pointing_replacement(char *fitsheader, double restRA, double restDec,
							  int verbose);
#endif
