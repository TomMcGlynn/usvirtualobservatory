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
#include "ipac/stk/util/hadoop/io.h"

#include <algorithm>
#include <iostream>

#include "ipac/stk/except.h"


namespace ipac { namespace stk { namespace util { namespace hadoop {

using std::istream;
using std::string;

namespace {

/// Determines the number of bytes (1-9) occupied by a zero-compressed integer.
inline int decodeVIntSize(int8_t firstByte) {
  if (firstByte >= -112) {
    return 1;
  } else if (firstByte < -120) {
    return -119 - firstByte;
  }
  return -111 - firstByte;
}

/// Determines the sign of a zero-compressed encoded integer.
inline bool isNegativeVInt(int8_t firstByte) {
  return firstByte < -120 || (firstByte >= -112 && firstByte < 0);
}

/// Union for extracting single precision values from raw memory.
union FloatGuts {
  int32_t int32Value;
  float floatValue;
};

/// Union for extracting double precision values from raw memory.
union DoubleGuts {
  int64_t int64Value;
  double doubleValue;
};

} // namespace


/** Reads a \c bool from an input stream.  The \c bool is expected to
    be stored as if written by the java.io.DataOutput.writeBoolean() method;
    exactly 1 byte is consumed.
  */
bool readBool(istream & in) {
  return in.get() == 1;
}

/** Reads and returns a \c bool from a buffer.  The \c bool is expected to
    be stored as if written by the java.io.DataOutput.writeBoolean() method;
    exactly 1 byte is consumed.
  */
bool readBool(unsigned char const * buf) {
  return buf[0] == 1;
}

/** Reads a signed 16 bit integer from an input stream.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeShort() method; exactly 2 bytes are consumed.
  */
int16_t readInt16(istream & in) {
  int16_t b1 = static_cast<int16_t>(in.get() & 0xff);
  int16_t b2 = static_cast<int16_t>(in.get() & 0xff);
  return  (b1 << 8) | b2;
}

/** Reads a signed 16 bit integer from a buffer.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeShort() method; exactly 2 bytes are consumed.
  */
int16_t readInt16(unsigned char const * buf) {
  int16_t b1 = buf[0];
  int16_t b2 = buf[1];
  return  (b1 << 8) | b2;
}

/** Reads a signed 32 bit integer from an input stream.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeInt() method; exactly 4 bytes are consumed.
  */
int32_t readInt32(istream & in) {
  int32_t b1 = static_cast<int32_t>(in.get() & 0xff);
  int32_t b2 = static_cast<int32_t>(in.get() & 0xff);
  int32_t b3 = static_cast<int32_t>(in.get() & 0xff);
  int32_t b4 = static_cast<int32_t>(in.get() & 0xff);
  return  (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
}

/** Reads a signed 32 bit integer from a buffer.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeInt() method; exactly 4 bytes are consumed.
  */
int32_t readInt32(unsigned char const * buf) {
  int32_t b1 = buf[0];
  int32_t b2 = buf[1];
  int32_t b3 = buf[2];
  int32_t b4 = buf[3];
  return  (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
}

/** Reads a signed 64 bit integer from an input stream.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeLong() method; exactly 8 bytes are consumed.
  */
int64_t readInt64(istream & in) {
  int64_t i1 = readInt32(in);
  int64_t i2 = readInt32(in) & 0xffffffff;
  return (i1 << 32) | i2;
}

/** Reads a signed 64 bit integer from a buffer.
    The integer is expected to be stored as if written by the
    java.io.DataOutput.writeLong() method; exactly 8 bytes
    are consumed.
  */
int64_t readInt64(unsigned char const * buf) {
  int64_t i1 = readInt32(buf);
  int64_t i2 = readInt32(buf + 4) & 0xffffffff;
  return (i1 << 32) | i2;
}

/** Reads an IEEE single precision value from an input stream.
    The value is expected to be stored as if written by the
    java.io.DataOutput.writeFloat() method; exactly 4 bytes are consumed.
  */
float readFloat(istream & in) {
  FloatGuts d;
  d.int32Value = readInt32(in);
  return d.floatValue;
}

/** Reads an IEEE single precision value from a buffer.
    The value is expected to be stored as if written by the
    java.io.DataOutput.writeFloat() method; exactly 4 bytes are consumed.
  */
float readFloat(unsigned char const * buf) {
  FloatGuts d;
  d.int32Value = readInt32(buf);
  return d.floatValue;
}

/** Reads an IEEE double precision value from an input stream.
    The value is expected to be stored as if written by the
    java.io.DataOutput.writeDouble() method; exactly 8 bytes are consumed.

    The implementation assumes that double and int64_t have the same
    endianness.
  */
double readDouble(istream & in) {
  DoubleGuts d;
  d.int64Value = readInt64(in);
  return d.doubleValue;
}

/** Reads an IEEE double precision value from a buffer.
    The value is expected to be stored as if written by the
    java.io.DataOutput.writeDouble() method; exactly 8 bytes are consumed.

    The implementation assumes that double and int64_t have the same
    endianness.
  */
double readDouble(unsigned char const * buf) {
  DoubleGuts d;
  d.int64Value = readInt64(buf);
  return d.doubleValue;
}

/** Reads a zero-compressed signed 32 bit integer from an input stream.
    The integer is expected to be stored as if written by the
    org.apache.hadoop.io.WritableUtils.writeVInt() method.
  */
int32_t readVInt32(istream & in) {
  return static_cast<int32_t>(readVInt64(in));
}

/** Reads a zero-compressed signed 32 bit integer from a buffer.
    The integer is expected to be stored as if written by the
    org.apache.hadoop.io.WritableUtils.writeVInt() method.
  */
int32_t readVInt32(unsigned char const * buf, unsigned char const **end) {
  return static_cast<int32_t>(readVInt64(buf, end));
}

/** Reads a zero-compressed signed 64 bit integer from an input stream.
    The integer is expected to be stored as if written by the
    org.apache.hadoop.io.WritableUtils.writeVLong() method.
  */
int64_t readVInt64(istream & in) {
  int8_t firstByte = static_cast<int8_t>(in.get());
  int len = decodeVIntSize(firstByte);
  if (len == 1) {
    return firstByte;
  }
  int64_t i = 0;
  for (int idx = 1; idx < len; ++idx) {
    i = i << 8;
    i = i | (in.get() & 0xff);
  }
  return isNegativeVInt(firstByte) ? (i ^ static_cast<int64_t>(-1)) : i;
}

/** Reads a zero-compressed signed 64 bit integer from a buffer.
    The integer is expected to be stored as if written by the
    org.apache.hadoop.io.WritableUtils.writeVLong() method.
  */
int64_t readVInt64(unsigned char const * buf, unsigned char const **end) {
  int8_t firstByte = static_cast<int8_t>(buf[0]);
  int len = decodeVIntSize(firstByte);
  if (len == 1) {
    if (end) {
      *end = buf + 1;
    }
    return firstByte;
  }
  int64_t i = 0;
  for (int idx = 1; idx < len; ++idx) {
    i = i << 8;
    i = i | (buf[idx]);
  }
  if (end) {
    *end = buf + len;
  }
  return isNegativeVInt(firstByte) ? (i ^ static_cast<int64_t>(-1)) : i;
}

/** Reads a string from an input stream.
    The string is expected to be encoded with the UTF-8 encoding,
    as if written by the org.apache.hadoop.io.Text.writeString() method.
  */
string readString(istream & in) {
  size_t const BLK_SIZE = 256;
  char buf[BLK_SIZE];
  int32_t n = readVInt32(in);
  if (n < 0) {
    throw IPAC_STK_EXCEPT(except::Format, "negative string length");
  }
  size_t len = static_cast<size_t>(n);
  string result;
  result.reserve(len);
  while (len != 0) {
    size_t n = std::min(BLK_SIZE, len);
    in.read(buf, n);
    result.append(buf, n);
    len -= n;
  }
  return result;
}

/** Reads a string from a buffer.
    The string is expected to be encoded with the UTF-8 encoding,
    as if written by the org.apache.hadoop.io.Text.writeString() method.
  */
string readString(unsigned char const * buf, unsigned char const **end) {
  unsigned char const * e = 0;
  int32_t n = readVInt32(buf, &e);
  if (n < 0) {
    throw IPAC_STK_EXCEPT(except::Format, "negative string length");
  }
  if (end) {
    *end = e + n;
  }
  return string(reinterpret_cast<char const *>(e),
                static_cast<size_t>(n));
}

}}}} // namespace ipac::stk::util::hadoop
