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
    \brief  Commonly useful macros.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_UTIL_MACROS_H_
#define IPAC_STK_UTIL_MACROS_H_

#include "ipac/stk/config.h"

#if !HAVE_STATIC_ASSERT
# include "boost/static_assert.hpp"
#endif

/** Disables copy construction and assignment for a given type. The macro
    must be invoked in a \c private: section of the class declaration for
    \c TypeName.
  */
#define IPAC_STK_NONCOPYABLE(TypeName) \
  TypeName(const TypeName&);       \
  void operator=(const TypeName&)

/** \def IPAC_STK_STATIC_ASSERT(predicate, message)
    Evaluates a predicate at compile time, aborting compilation if its value
    is false.

    \param[in]  pred  Predicate to evaluate at compile time.
    \param[in]  msg   String literal to print if predicate evaluates
                      to false. Some compilers don't support this; in
                      this case the message string still serves as
                      documentation.
  */
#if !HAVE_STATIC_ASSERT
# define IPAC_STK_STATIC_ASSERT(pred, msg) BOOST_STATIC_ASSERT(pred)
#else
# define IPAC_STK_STATIC_ASSERT(pred, msg) static_assert(pred, msg)
#endif

#endif // IPAC_STK_UTIL_MACROS_H_
