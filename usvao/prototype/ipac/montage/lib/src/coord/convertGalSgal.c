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
#include <math.h>
#include <coord.h>

/***************************************************************************/
/*                                                                         */
/* convertGalToSgal computes equatorial coords from galactic coords.       */
/* ----------------                                                        */
/*                                                                         */
/* Inputs:  glon (l) in degrees, glat (b) in degrees.                      */
/* Outputs: sglon    in degrees, sglat    in degrees.                      */
/*                                                                         */
/*                                                                         */
/* Right hand rule coordinates:                                            */
/*                                                                         */
/*    x = cos(lat)*cos(lon)                                                */
/*    y = sin(lon)*cos(lat)                                                */
/*    z = sin(lat)                                                         */
/*                                                                         */
/*                                                                         */
/* Gal <==> Sgal equations                                                 */
/*                                                                         */
/*  Computed values of rotation used for the Eulerian angles:              */
/*                                                                         */
/*   phi = 137.37 deg.;  theta = 83.68 deg.;  psi = 0. deg.                */
/*                                                                         */
/*    Based on Gal (l,b) = 137.37, 0.00) at Sgal (lon,lat)= (0., 0.)       */
/*    and Sgal North Pole (0, +90) at Gal (l,b) = (47.37, +6.32)           */
/*                                                                         */
/* References:                                                             */
/*                                                                         */
/*    de Vaucouleurs,G., A. de Vaucouleurs, and G.H.Corwin,                */
/*       Second Reference Catalog of Bright Galaxies, (1976), p.8          */
/*                                                                         */
/*    Tully,B., Nearby Galaxies Catalog, (1988) p. 1,4-5                   */
/*                                                                         */
/*    Note: de Vaucouleurs gives gal l,b 137.29, 0 for SGL,SGB= 0,0;       */
/*          this is not 90 degrees from Galactic North Pole.- used         */
/*          Tully's value of 137.37, 0 deg.                                */
/*                                                                         */
/***************************************************************************/


void convertGalToSgal(double glon, double glat, double *sglon, double *sglat)
{
   static int nthru = 0;

   static double dtor, rtod;
   static double trans[3][3];

   double glonr, glatr;
   double cosl, cosL, sinL;

   double x, y, z;
   double xsgal, ysgal, zsgal;

   double cosph, sinph, cosps, sinps, costh, sinth;

   if(coord_debug)
   {
      fprintf(stderr, "DEBUG: convertGalToSgal()\n");
      fflush(stderr);
   }




   /* First time using this function, compute the        */
   /* degrees <-> radians conversion factors dynamically */
   /* (ensuring computational consistency).  Also        */
   /* calculate xyz transformation matrix from galactic  */
   /* to supergalactic.                                  */

   if(nthru == 0) 
   {
      /* Angles defining the relationship   */
      /* between galactic and supergalactic */

      double psi = 0.00, theta = 83.68, phi = 137.37;

      dtor = atan(1.0) / 45.0;
      rtod = 1.0 / dtor;

      cosps = cos(  psi*dtor);
      sinps = sin(  psi*dtor);

      cosph = cos(  phi*dtor);
      sinph = sin(  phi*dtor);

      costh = cos(theta*dtor);
      sinth = sin(theta*dtor);

      trans[0][0] =  cosps*cosph - costh*sinph*sinps;
      trans[0][1] =  cosps*sinph + costh*cosph*sinps;
      trans[0][2] =  sinps*sinth;

      trans[1][0] = -sinps*cosph - costh*sinph*cosps;
      trans[1][1] = -sinps*sinph + costh*cosph*cosps;
      trans[1][2] =  cosps*sinth;

      trans[2][0] =  sinth*sinph;
      trans[2][1] = -sinth*cosph;
      trans[2][2] =  costh;

      nthru = 1;
   }


   /* Compute the xyz (galactic) coordinates of the point */
   
   glonr = glon * dtor;
   glatr = glat * dtor;

   cosL = cos(glonr);
   sinL = sin(glonr);
   cosl = cos(glatr);

   x = cosL*cosl;
   y = sinL*cosl;
   z = sin(glatr);



   /* Convert to xyz supergalactic (with various range checks */
   /* and renormalizations to prevent invalid values)         */

   zsgal = trans[2][0]*x+ trans[2][1]*y + trans[2][2]*z;


   /* Special case for the poles (set ra to 0) */

   if(fabs(zsgal) >= 1.0)
   {
      zsgal = zsgal/fabs(zsgal);

      *sglat =  asin(zsgal);
      *sglon =  0.0;
   }


   /* Normal case */

   else
   {
      xsgal = trans[0][0]*x + trans[0][1]*y + trans[0][2]*z;
      ysgal = trans[1][0]*x + trans[1][1]*y + trans[1][2]*z;

      *sglat =  asin(zsgal);
      *sglon = atan2(ysgal, xsgal);
   }



   /* Convert to degrees and adjust range */

   *sglon = *sglon * rtod;

   while(*sglon <   0.0) *sglon += 360.0;
   while(*sglon > 360.0) *sglon -= 360.0;

   *sglat = *sglat * rtod;



   /* Double check for the poles */

   if(fabs(*sglat) >= 90.0) 
   {
      *sglon = 0.0;
      if(*sglat >  90.0) *sglat =  90.0;
      if(*sglat < -90.0) *sglat = -90.0;
   }

   return;
}




/***************************************************************************/
/*                                                                         */
/* convertSgalToGal computes galactic coords from equatorial coords.       */
/* ----------------                                                        */
/*                                                                         */
/* Inputs:  sglon    in degrees, sglat    in degrees.                      */
/* Outputs: glon (l) in degrees, glat (b) in degrees.                      */
/*                                                                         */
/*                                                                         */
/* see information on convertGalToSgal().                                  */
/*                                                                         */
/***************************************************************************/


void convertSgalToGal(double sglon, double sglat, double *glon, double *glat)
{
   static int nthru = 0;

   static double dtor, rtod;
   static double trans[3][3];

   double sglonr, sglatr;
   double cosl, cosL, sinL;

   double x, y, z;
   double xgal, ygal, zgal;

   double cosph, sinph, cosps, sinps, costh, sinth;



   /* First time using this function, compute the         */
   /* degrees <-> radians conversion factors dynamically  */
   /* (ensuring computational consistency).  Also         */
   /* calculate xyz transformation matrix from equatorial */
   /* to galactic.                                        */

   if(nthru == 0)
   {
      /* Angles defining the relationship   */
      /* between galactic and supergalactic */

      double psi = 0.00, theta = 83.68, phi = 137.37;

      dtor = atan(1.0) / 45.0;
      rtod = 1.0 / dtor;

      cosps = cos(  psi*dtor);
      sinps = sin(  psi*dtor);
      cosph = cos(  phi*dtor);
      sinph = sin(  phi*dtor);
      costh = cos(theta*dtor);
      sinth = sin(theta*dtor);

      trans[0][0] =  cosps*cosph - costh*sinph*sinps;
      trans[0][1] = -sinps*cosph - costh*sinph*cosps;
      trans[0][2] =  sinth*sinph;

      trans[1][0] =  cosps*sinph + costh*cosph*sinps;
      trans[1][1] = -sinps*sinph + costh*cosph*cosps;
      trans[1][2] = -sinth*cosph;

      trans[2][0] =  sinth*sinps;
      trans[2][1] =  sinth*cosps;
      trans[2][2] =  costh;

      nthru = 1; 
   }



   /* Compute the xyz (supergalactic) coordinates of the point */

   sglonr = sglon * dtor;
   sglatr = sglat * dtor;

   cosl = cos(sglatr);
   cosL = cos(sglonr);
   sinL = sin(sglonr);

   x = cosl*cosL;
   y = sinL*cosl;
   z = sin(sglatr);



   /* Convert to xyz galactic (with various range checks */
   /* and renormalizations to prevent invalid values)    */

   zgal = trans[2][0]*x+ trans[2][1]*y + trans[2][2]*z;


   /* Special case for the poles (set ra to 0) */

   if(fabs(zgal) >= 1.0)
   {
      zgal = zgal/fabs(zgal);

      *glat =  asin(zgal);
      *glon =  0.0;
   }


   /* Normal case */

   else
   {
      xgal = trans[0][0]*x + trans[0][1]*y + trans[0][2]*z;
      ygal = trans[1][0]*x + trans[1][1]*y + trans[1][2]*z;

      *glat =  asin(zgal);
      *glon = atan2(ygal, xgal);
   }



   /* Convert to degrees and adjust range */

   *glon = *glon * rtod;

   while(*glon <   0.0) *glon += 360.0;
   while(*glon > 360.0) *glon -= 360.0;

   *glat = *glat * rtod;



   /* Double check for the poles */

   if(fabs(*glat) >= 90.0) 
   {
      *glon = 0.0;
      if(*glat >  90.0) *glat =  90.0;
      if(*glat < -90.0) *glat = -90.0;
   }

   return;
}
