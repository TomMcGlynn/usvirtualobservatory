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
    \brief  Inline and templated member function definitions for JSONOutput.h
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_JSON_JSONOUTPUT_H_
# error This file must be included from ipac/stk/json/JSONOutput.h
#else
# ifndef IPAC_STK_JSON_JSONOUTPUT_INL_
# define IPAC_STK_JSON_JSONOUTPUT_INL_

#include <sstream>


namespace ipac { namespace stk { namespace json {

/** Returns the formatting options for this JSONOutput.
  */
inline FormattingOptions const & JSONOutput::getFormattingOptions() const {
  return _opts;
}

/** Returns the underlying output stream or null.
  */
inline std::ostream * JSONOutput::getOutputStream() {
  return _out;
}

/** Returns the underlying Value or a null Value.
  */
inline Value const JSONOutput::getValue() const {
  return _value;
}

/** Outputs the given object. A free function
    \code outputJSON(T const &, JSONOutput &) \endcode
    must have been defined in order for compilation to succeed.
  */
template <typename T>
inline JSONOutput & JSONOutput::value(T const & val) {
  Transaction tx(*this);
  outputJSON(val, *this);
  tx.commit();
  return *this;
}

//@{
/** Outputs the value pointed to or null.
  */
template <typename T> inline JSONOutput & JSONOutput::value(T const * val) {
  if (!val) {
    return value();
  }
  return value(*val);
}
template <typename T>
inline JSONOutput & JSONOutput::value(boost::shared_ptr<T> const & val) {
  return value(val.get());
}
template <typename T>
inline JSONOutput & JSONOutput::value(boost::shared_ptr<T const> const & val) {
  return value(val.get());
}
template <typename T>
inline JSONOutput & JSONOutput::value(boost::scoped_ptr<T> val) {
  return value(val.get());
}
template <typename T>
inline JSONOutput & JSONOutput::value(boost::scoped_ptr<T const> val) {
  return value(val.get());
}
//@}

/** Outputs the values in the given range as a JSON array.
  */
template <typename ForwardsIterator>
JSONOutput & JSONOutput::array(ForwardsIterator begin,
                               ForwardsIterator end)
{
  Transaction tx(*this);
  array();
  for (; begin != end; ++begin) {
    value(*begin);
  }
  close();
  tx.commit();
  return *this;
}

/** Outputs \c k/v as a key/value pair in a JSON object.
  */
template <typename T>
JSONOutput & JSONOutput::pair(std::string const & k, T const & v) {
  Transaction tx(*this);
  key(k);
  value(v);
  tx.commit();
  return *this;
}

/** Outputs \c kv as a key/value pair in a JSON object.
  */
template <typename T>
inline JSONOutput & JSONOutput::pair(std::pair<std::string const, T> const & kv) {
  return pair(kv.first, kv.second);
}

/** Outputs a map<std::string, T> as a JSON object.
  */
template <typename T>
JSONOutput & JSONOutput::object(std::map<std::string, T> const & map) {
  typedef typename std::map<std::string, T>::const_iterator MapIter;
  Transaction tx(*this);
  object();
  for (MapIter i(map.begin()), e(map.end()); i != e; ++i) {
    pair(*i);
  }
  close();
  tx.commit();
  return *this;
}

/** Returns a JSON string representation of \c obj. A free function
    \code outputJSON(T const &, JSONOutput &) \endcode must have been
    defined in order for compilation to succeed.
  */
template <typename T>
std::string const JSONOutput::toString(T const & obj,
                                       FormattingOptions const & opts)
{
  std::ostringstream oss;
  JSONOutput out(oss, opts);
  outputJSON(obj, out);
  return oss.str();
}

/** Returns a representation of \c obj as a heirarchical value. A free
    function \code outputJSON(T const &, JSONOutput &) \endcode must have
    been defined in order for compilation to succeed.
  */
template <typename T>
Value const JSONOutput::toValue(T const & obj) {
  JSONOutput out(true);
  outputJSON(obj, out);
  return out.getValue();
}

/** Closes all currently open JSON objects and arrays.
  */
inline JSONOutput & JSONOutput::closeAll() {
  return close(_stack.size());
}

inline std::string const JSONOutput::encode(char const * s) {
  return encode(std::string(s));
}

inline std::string const JSONOutput::encode(char const * s, size_t n) {
  return encode(std::string(s, n));
}

inline void JSONOutput::indent() {
  indent(_stack.size());
}

}}} // namespace ipac::stk::json

# endif // IPAC_STK_JSON_JSONOUTPUT_INL_
#endif // IPAC_STK_JSON_JSONOUTPUT_H_
