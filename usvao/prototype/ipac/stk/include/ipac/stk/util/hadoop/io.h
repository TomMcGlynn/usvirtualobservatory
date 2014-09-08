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
    \brief  Utility functions for reading binary data written with Hadoop.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_UTIL_HADOOP_IO_H_
#define IPAC_STK_UTIL_HADOOP_IO_H_

#include "ipac/stk/config.h"
#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif

#include <istream>
#include <string>


namespace ipac { namespace stk { namespace util { namespace hadoop {

bool readBool(std::istream & in);
bool readBool(unsigned char const * buf);
int16_t readInt16(std::istream & in);
int16_t readInt16(unsigned char const * buf);
int32_t readInt32(std::istream & in);
int32_t readInt32(unsigned char const * buf);
int64_t readInt64(std::istream & in);
int64_t readInt64(unsigned char const * buf);
float readFloat(std::istream & in);
float readFloat(unsigned char const * buf);
double readDouble(std::istream & in);
double readDouble(unsigned char const * buf);
int32_t readVInt32(std::istream & in);
int32_t readVInt32(unsigned char const * buf, unsigned char const ** end);
int64_t readVInt64(std::istream & in);
int64_t readVInt64(unsigned char const * buf, unsigned char const ** end);
std::string readString(std::istream & in);
std::string readString(unsigned char const * buf, unsigned char const ** end);

}}}} // namespace ipac::stk::util::hadoop

#endif // IPAC_STK_UTIL_HADOOP_IO_H_
