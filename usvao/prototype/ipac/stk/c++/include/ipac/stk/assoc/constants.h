/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

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

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



/** \file
    \brief  Various generally useful constants.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_CONSTANTS_H_
#define IPAC_STK_ASSOC_CONSTANTS_H_

#include "ipac/stk/util/macros.h"


namespace ipac { namespace stk { namespace assoc {

/** Scaling factor that maps 64-bit integer fixed point angles
    to radians; equal to &pi;/(180 * 18014398509481984)
  */
double const RAD_PER_FIX = 9.68852360557953305410900251275e-19;

/** Scaling factor that maps 64-bit integer fixed point angles
    to degrees; equal to 1/18014398509481984
  */
double const DEG_PER_FIX = 5.55111512312578270211815834045e-17;

/** Scaling factor for converting radians to degrees.
  */
double const DEG_PER_RAD = 57.2957795130823208767981548141;

/** Scaling factor for converting degrees to radians.
  */
double const RAD_PER_DEG = 0.0174532925199432957692369076849;

/** Scaling factor for converting arcminutes to radians.
  */
double const RAD_PER_ARCMIN = 0.000290888208665721596153948461415;

/** Scaling factor for converting radians to arcminutes.
  */
double const ARCMIN_PER_RAD = 3437.74677078493925260788928885;

/** Scaling factor for converting arcseconds to radians.
  */
double const RAD_PER_ARCSEC = 0.00000484813681109535993589914102357;

/** Scaling factor for converting radians to arcseconds.
  */
double const ARCSEC_PER_RAD = 2.06264806247096355156473357331e5;

double const PI = 3.14159265358979323846264338328;

/** \internal
    Granularity of double array allocations for SIMD friendly code;
    currently set to 32 bytes (AVX operates on 256bit vectors, SSEx
    on 128bit vectors).
  */
size_t const DOUBLE_BUF_GRANULARITY = 32 / sizeof(double);

IPAC_STK_STATIC_ASSERT(DOUBLE_BUF_GRANULARITY >= 1,
                       "Double buffer granularity is zero!");


}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_CONSTANTS_H_

  