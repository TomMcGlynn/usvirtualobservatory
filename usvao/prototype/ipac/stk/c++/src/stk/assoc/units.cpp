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
    \brief  Implementation of utility functions for reading binary data
            written with Hadoop.
    \author Serge Monkewitz
  */
#include "ipac/stk/assoc/units.h"

#include "boost/regex.hpp"

#include "ipac/stk/assoc/constants.h"


namespace ipac { namespace stk { namespace assoc {

using std::string;
using boost::regex;
using boost::regex_match;

namespace {
  regex const WS_REGEX("^\\s*$");
  regex const RAD_REGEX("^\\s*r(ad(ians?)?)?\\s*$",
                        regex::perl | regex::icase);
  regex const DEG_REGEX("^\\s*d(eg(rees?)?)?\\s*$",
                        regex::perl | regex::icase);
  regex const ARCMIN_REGEX("^\\s*('|a(rc)?-?min(utes?)?)\\s*$",
                           regex::perl | regex::icase);
  regex const ARCSEC_REGEX("^\\s*(\"|a(rc)?-?sec(onds?)?)\\s*$",
                           regex::perl | regex::icase);
}

/** Returns the number of radians per \c unit, or 0.0 if \c unit is not
    recognizable as an angular unit specification. A default of
    \c defaultRadiansPer is returned for empty/whitespace-only unit strings.
  */
double radiansPer(string const & unit, double defaultRadiansPer) {
  if (regex_match(unit, WS_REGEX)) {
    return defaultRadiansPer;
  } if (regex_match(unit, RAD_REGEX)) {
    return 1.0;
  } else if (regex_match(unit, DEG_REGEX)) {
    return RAD_PER_DEG;
  } else if (regex_match(unit, ARCMIN_REGEX)) {
    return RAD_PER_ARCMIN;
  } else if (regex_match(unit, ARCSEC_REGEX)) {
    return RAD_PER_ARCSEC;
  }
  return 0.0;
}

/** Returns \c true if the given unit string corresponds to an angular unit;
    \c true is returned for empty/whitespace-only unit strings.
  */
bool isAngle(string const & unit) {
  return regex_match(unit.begin(), unit.end(), WS_REGEX) ||
         regex_match(unit.begin(), unit.end(), RAD_REGEX) ||
         regex_match(unit.begin(), unit.end(), DEG_REGEX) ||
         regex_match(unit.begin(), unit.end(), ARCMIN_REGEX) ||
         regex_match(unit.begin(), unit.end(), ARCSEC_REGEX);
}


}}} // namespace ipac::stk::assoc

