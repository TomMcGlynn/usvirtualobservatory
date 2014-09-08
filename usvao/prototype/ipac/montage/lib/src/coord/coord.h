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



#ifndef ISIS_COORD_LIB


int coord_debug;


struct COORD                /* Definition of coordinate structure            */
{                           /*                                               */ 
  char sys[3];              /* Coordinate system                             */
  char clon[25], clat[25];  /* Coordinates (when expressed as char string)   */
  double lon, lat;          /* Coordinates (when expressed as a real number) */
  char fmt[6];              /* Units                                         */
  char epoch[10];           /* Epoch type and year                           */
};


/* "sys" can be one of the following:                                        */
/*                                                                           */
/*    "EQ"   -   Equatorial                                                  */
/*    "GA"   -	 Galactic                                                    */
/*    "EC"   -	 Ecliptic                                                    */
/*    "SG"   -	 Supergalactic                                               */
/*                                                                           */
/* "fmt" can be any of the following:                                        */
/*                                                                           */
/*    "DD" or "DDR"     -  Decimal Degrees (expressed as a real number)      */
/*    "DDC"             -  Decimal Degrees (expressed as a char string)      */
/*    "SEXR"            -  Sexigesimal (expressed as a real number)          */
/*    "SEX" or "SEXC"   -  Sexigesimal (expressed as a char string)          */
/*    "RAD" or "RADR"   -  Radians (expressed as a real number)              */
/*    "RADC"            -  Radians (expressed as a char string)              */
/*    "MRAD" or "MRADR" -  Milliradians (expressed as a real number)         */
/*    "MRADC"           -  Milliradians	(expressed as a char string)         */
/*    "AS" or "ASR"     -  Arc-seconds (expressed as a real number)          */
/*    "ASC"             -  Arc-seconds (expressed as a char string)          */
/*    "MAS" or "MASR"   -  Milliarcseconds (expressed as a real number)      */
/*    "MASC"            -  Milliarcseconds (expressed as a char string)      */
/*                                                                           */
/* "epoch" must start with the characters "B" or "J" followed by a           */
/*    four-digit year (e.g. "J2000", "B1950").                               */



typedef enum {
	      DD = 0,      /* 0 */
	      SEX   ,      /* 1 */
	      RAD   ,      /* 2 */
	      MRAD  ,      /* 3 */
	      AS    ,      /* 4 */
	      MAS          /* 5 */
	     }
	      CoordUnit;


typedef enum {A = 0 ,      /* 0 */
	      T     ,      /* 1 */
	      H     ,      /* 2 */
	      M            /* 3 */
	     }
	      ArcPrec;





/* ERROR codes returned by ccalc()                                           */
/*                                                                           */
#define ERR_CCALC_INVETYPE  -1 /* Invalid epoch type was specified           */
#define ERR_CCALC_INVEYEAR  -2 /* Invalid epoch year was specified           */
#define ERR_CCALC_INVSYS    -3 /* Invalid coordinate system was specified    */
#define ERR_CCALC_INVDOUBL  -4 /* Couldn't convert a value to double         */
#define ERR_CCALC_SEXCONV   -5 /* Sexigesimal conversion failed              */
#define ERR_CCALC_SEXERR    -6 /* Internal error with sexigesimal conversion */
#define ERR_CCALC_INVFMT    -7 /* Invalid format was specified               */
#define ERR_CCALC_INVPREC   -8 /* Invalid precision was specified            */
#define ERR_CCALC_INVCOORD  -9 /* Invalid coordinate value was specified     */



/* Coordinate system codes           */
/* (used in transformation routines) */

#define EQUJ      0
#define EQUB      1
#define ECLJ      2
#define ECLB      3
#define GAL       4
#define SGAL      5

#define JULIAN    0
#define BESSELIAN 1



/* Prototypes of callable functions */

void convertCoordinates();
void convertEclToEqu();
void convertEquToEcl();
void convertEquToGal();
void convertGalToEqu();
void convertGalToSgal();
void convertSgalToGal();

void convertBesselianToJulian();
void convertJulianToBesselian();
void precessBesselian();
void precessBesselianWithProperMotion();
void precessJulian();
void precessJulianWithProperMotion();
void julianToBesselianFKCorrection();
void besselianToJulianFKCorrection();

int  ccalc();
int  degreeToDMS();
int  degreeToHMS();
int  degreeToSex();
int  sexToDegree();
int  parseCoordinateString();

double roundValue();


#define ISIS_COORD_LIB
#endif
