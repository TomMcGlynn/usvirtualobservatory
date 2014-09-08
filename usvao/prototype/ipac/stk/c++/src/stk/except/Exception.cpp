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
    \brief  Implementation of the Exception class.
    \author Serge Monkewitz
  */
#include "ipac/stk/except/Exception.h"

#include <stdio.h>

#include <cstring>
#include <ostream>
#include <sstream>
#include <string>

#include "boost/scoped_array.hpp"


namespace ipac { namespace stk { namespace except {

using std::endl;
using std::ostream;
using std::string;


/** Creates an Exception from throw-site information.

    \param[in] file   Filename; must be a compile-time string that does
                      not require deletion, automatically passed in by
                      IPAC_STK_EXCEPT.
    \param[in] line   Line number, automatically passed in by IPAC_STK_EXCEPT.
    \param[in] func   Function name; must be a compile-time string that does
                      not require deletion, automatically passed in by
                      IPAC_STK_EXCEPT.
    \param[in] msg    Informational string.
  */
Exception::Exception(char const * file, int line,
                     char const * func, string const & msg) :
  _file(file), _line(line), _func(func), _msg(msg) {}

Exception::~Exception() throw() {}

/** Writes a textual representation of this exception, including both the
    location of the throw site and any associated message, to the given
    stream.

    \param[in] stream   Reference to an output stream.
    \return             Reference to the output \c stream.
  */
ostream & Exception::write(ostream & stream) const {
  stream << getTypeName() << " thrown at " <<
            _file << ":" << _line << " in " << _func << endl;
  stream << "    Message: " << _msg << endl;
  return stream;
}

/** Returns a character string representing this exception.  Tries to use
    write(std::ostream &) and falls back on getTypeName() if an exception
    is thrown.

    \return     String representation of this exception; must not be deleted.
  */
char const * Exception::what() const throw() {
  try {
    if (_what.size() == 0) {
      std::ostringstream s;
      write(s);
      _what = s.str(); // copies underlying string
    }
    return _what.c_str();
  } catch (...) {
    return getTypeName();
  }
}

/** Returns the fully-qualified type name of the exception.  This must be
    overridden by derived classes (automatically if the
    IPAC_STK_EXCEPTION_TYPE macro is used).

    \return   Fully qualified exception type name; must not be deleted.
  */
char const * Exception::getTypeName() const throw() {
  return "ipac::stk::except::Exception";
}

/** Writes a textual representation of the exception to a stream.

    \param[in] stream   Output stream.
    \param[in] e        Exception to write out.

    \return   Reference to the output \c stream.
  */
ostream & operator<<(ostream & stream, Exception const & e) {
  return e.write(stream);
}

/** Returns a formatted string obtained by passing \c fmt and any trailing
    arguments to the C \c vsnprintf function. This is a faster alternative
    to the \c boost::format library.
  */
string const message(char const * fmt, ...) {
  static string const FAILED = "Failed to format message";
  struct VarArgs {
    std::va_list * ap;
    VarArgs(std::va_list & list) : ap(&list) { }
    ~VarArgs() { va_end(*ap); }
  };

  std::va_list list;
  char buf[256];
  VarArgs args(list);
  // Try formatting using a stack allocated buffer
  va_start(list, fmt);
  int n = ::vsnprintf(buf, sizeof(buf), fmt, list);
  try {
    if (n >= static_cast<int>(sizeof(buf))) {
      // buf was too small, allocate the necessary memory on the heap
      boost::scoped_array<char> bigbuf(new char[n]);
      va_end(list);
      va_start(list, fmt);
      n = ::vsnprintf(bigbuf.get(), static_cast<size_t>(n), fmt, list);
      if (n >= 0) {
        return string(bigbuf.get());
      }
    } else if (n >= 0) {
      return string(buf);
    }
  } catch(...) { }
  return FAILED;
}

}}} // namespace ipac::stk::except
