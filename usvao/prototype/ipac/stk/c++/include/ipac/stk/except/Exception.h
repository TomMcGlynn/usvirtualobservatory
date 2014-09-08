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
    \brief  Exception class and associated macros.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_EXCEPT_EXCEPTION_H_
#define IPAC_STK_EXCEPT_EXCEPTION_H_

#include <cstdarg>
#include <exception>
#include <iosfwd>
#include <string>

#include "boost/current_function.hpp"


namespace ipac { namespace stk { namespace except {

/// Equivalent to the name of the current function.
#define IPAC_STK_CURRENT_FUNCTION BOOST_CURRENT_FUNCTION

#define IPAC_STK_EXCEPT_HERE __FILE__, __LINE__, IPAC_STK_CURRENT_FUNCTION

/** Create an exception object with the given type, informational message,
    and type-dependant additional arguments.

    \param[in] type   C++ exception type.
  */
#define IPAC_STK_EXCEPT(type, ...) type(IPAC_STK_EXCEPT_HERE, __VA_ARGS__)

/** Define a new exception type.

    \param[in] t  Name of the exception class.
    \param[in] b  Base class of the exception.
    \param[in] c  Fully qualified name of the exception class.
  */
#define IPAC_STK_EXCEPTION_TYPE(t, b, c) \
  class t : public b { \
  public: \
    t(char const * file, int line, char const * func, \
      std::string const & msg) : \
      b(file, line, func, msg) {}; \
    virtual ~t() throw(); \
    virtual char const * getTypeName() const throw(); \
  };

/// Base class for all exceptions.
class Exception : public std::exception {
public:
  Exception(char const * file, int line, char const * func,
            std::string const & msg);
  virtual ~Exception() throw();
  virtual std::ostream & write(std::ostream & stream) const;
  virtual char const * what() const throw();
  virtual char const * getTypeName() const throw();
  char const * getFile() const throw() { return _file; }
  int getLine() const throw() { return _line; }
  char const * getFunction() const throw() { return _func; }
  std::string const & getMessage() const throw() { return _msg; }
private:
  char const * _file; // compiled strings only; does not require deletion
  int _line;
  char const * _func; // compiled strings only; does not require deletion
  std::string _msg;
  mutable std::string _what;
};

std::ostream & operator<<(std::ostream & stream, Exception const & e);

std::string const message(char const * fmt, ...);

}}} // namespace ipac::stk::except

#endif // IPAC_STK_EXCEPT_EXCEPTION_H_
