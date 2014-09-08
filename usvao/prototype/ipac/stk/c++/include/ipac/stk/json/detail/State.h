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
    \brief  Class encapsulating JSON formatting options.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_JSON_DETAIL_STATE_H_
#define IPAC_STK_JSON_DETAIL_STATE_H_

#include <string>

#include "../JSONOutput.h"
#include "../Value.h"


namespace ipac { namespace stk { namespace json { namespace detail {

/** \internal
    JSONOutput state at a particular level in the depth-first
    traversal of a JSON object/array.
  */
struct State {
  State(Value * val, bool inObj) : value(val), n(0), inObject(inObj) { }
  State() : value(0), n(0), inObject(false) { }
  ~State() {
    value = 0;
  }

  Value * value;
  size_t n;
  bool inObject;
};

/** \internal
    JSONOutput state shared across all levels in the depth-first
    traversal of a JSON object/array.
  */
struct CommonState {
  CommonState() : lastKey(), sawKey(false), started(false) { }
  ~CommonState() { }

  std::string lastKey;
  bool sawKey;
  bool started;
};

}}}} // namespace ipac::stk::json::detail

#endif // IPAC_STK_JSON_DETAIL_STATE_H_
