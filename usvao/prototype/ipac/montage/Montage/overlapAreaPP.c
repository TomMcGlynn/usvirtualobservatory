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



/* Module: overlapAreaPP.c

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
1.0      John Good        27Jan04  Baseline code

*/

#include <stdio.h>
#include <math.h>

double computeOverlapPP(double *ix, double *iy,
                        double minX, double maxX, 
			double minY, double maxY,
			double pixelArea);

double polyArea(int npts, double *nx, double *ny);

int rectClip (int n, double *x, double *y, double *nx, double *ny,
              double minX, double minY, double maxX, double maxY);
int lineClip (int n, 
              double  *x, double  *y, 
              double *nx, double *ny,
              double val, int dir);
int inPlane  (double test, double divider, int direction);
int ptInPoly ( double x, double y, int n, double *xp, double *yp);

double tmpX0[100];
double tmpX1[100];
double tmpY0[100];
double tmpY1[100];

extern int  debug;


/***************************************************/
/*                                                 */
/* computeOverlapPP()                              */
/*                                                 */
/* Sets up the polygons, runs the overlap          */
/* computation, and returns the area of overlap.   */
/* This version works in pixel space rather than   */
/* on the celestial sphere.                        */
/*                                                 */
/***************************************************/

double computeOverlapPP(double *ix, double *iy,
                        double minX, double maxX, 
			double minY, double maxY,
			double pixelArea)
{
   int    npts;
   double area;

   double nx[100];
   double ny[100];

   double xp[4], yp[4];


   /* Clip the input pixel polygon with the */
   /* output pixel range                    */

   npts = rectClip(4, ix, iy, nx, ny, minX, minY, maxX, maxY);


   /* If no points, it may mean that     */
   /* the output is completely contained */
   /* in the input                       */

   if(npts < 3)
   {
      xp[0] = minX; yp[0] = minY;
      xp[1] = maxX; yp[1] = minY;
      xp[2] = maxX; yp[2] = maxY;
      xp[3] = minX; yp[3] = maxY;

      if(ptInPoly(ix[0], iy[0], 4, xp, yp))
      {
	 area = pixelArea;
	 return area;
      }

      return 0.;
   }

   area = polyArea(npts, nx, ny) * pixelArea;

   return(area);
}



int rectClip(int n, double *x, double *y, double *nx, double *ny,
             double minX, double minY, double maxX, double maxY) 
{
   int nCurr;

   nCurr = lineClip(n, x, y, tmpX0, tmpY0, minX, 1);

   if (nCurr > 0) 
   {
      nCurr = lineClip(nCurr, tmpX0, tmpY0, tmpX1, tmpY1, maxX, 0);

      if (nCurr > 0) 
      {
	 nCurr = lineClip(nCurr, tmpY1, tmpX1, tmpY0, tmpX0, minY, 1);

	 if (nCurr > 0)
	 {
	    nCurr = lineClip(nCurr, tmpY0, tmpX0, ny, nx, maxY, 0);
	 }
      }
   }

   return nCurr;
}



int lineClip(int n, 
             double  *x, double  *y, 
             double *nx, double *ny,
             double val, int dir) 
{
   int i;
   int nout;
   int last;

   double ycross;

   nout = 0;
   last = inPlane(x[n-1], val, dir);

   for(i=0; i<n; ++i) 
   {
      if (last)
      {
	 if (inPlane(x[i], val, dir)) 
	 {
	    /* Both endpoints in, just add the new point */

	    nx[nout] = x[i];
	    ny[nout] = y[i];

	    ++nout;
	 }
	 else
	 {
	    /* Moved out of the clip region, add the point we moved out */

	    if (i == 0) 
	       ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[0]-x[n-1]);
	    else
	       ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);

	    nx[nout] = val;
	    ny[nout] = ycross;

	    ++nout;

	    last = 0;
	 }
      }
      else
      {
	 if (inPlane(x[i], val, dir)) 
	 {
	    /* Moved into the clip region.  Add the point */
	    /* we moved in, and the end point.            */

	    if (i == 0) 
	    ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[i]-x[n-1]);
	    else
	    ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);

	    nx[nout] = val;
	    ny[nout] = ycross;

	    ++nout;

	    nx[nout] = x[i];
	    ny[nout] = y[i];

	    ++nout;

	    last = 1;
	 }
	 else 
	 {
	    /* Segment entirely clipped. */
	 }
      }
   }

   return nout;
}


int inPlane(double test, double divider, int direction) 
{
   if (direction) 
      return test >= divider;
   else
      return test <= divider;
}
    


double polyArea(int npts, double *nx, double *ny)
{
   int    i, inext;
   double area;

   area = 0.;

   for(i=0; i<npts; ++i)
   {
      inext = (i+1)%npts;

      area += nx[i]*ny[inext] - nx[inext]*ny[i];
   }

   area = fabs(area) / 2;

   return area;
}



int ptInPoly( double x, double y, int n, double *xp, double *yp)
{
   int    i, inext, count;
   double t;

   count = 0;

   for (i=0; i<n; ++i) 
   {
      inext = (i+1)%n;

      if(   ((yp[i] <= y) && (yp[inext] >  y))
	 || ((yp[i] >  y) && (yp[inext] <= y)))
      { 
	 t = (y - yp[i]) / (yp[inext] - yp[i]);

	 if (x < xp[i] + t * (xp[inext] - xp[i]))
	    ++count;
      }
   }

   return (count&1);
}
