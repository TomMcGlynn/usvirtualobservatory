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
    \brief  Class for writing JSON formatted output to a stream.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_JSON_JSONOUTPUT_H_
#define IPAC_STK_JSON_JSONOUTPUT_H_

#include <map>
#include <ostream>
#include <string>
#include <vector>

#include "boost/scoped_ptr.hpp"
#include "boost/shared_ptr.hpp"

#include "ipac/stk/except.h"
#include "ipac/stk/util/macros.h"

#include "FormattingOptions.h"
#include "Value.h"
#include "detail/State.h"

namespace ipac { namespace stk { namespace json {

/** Class for JSON serialization. Supports formatted output to a
    \c std::ostream and/or creation of a \c Value object.

    \p The underlying stream (if one is specified) is held by pointer;
    users are responsible for ensuring that the lifetime of a stream exceeds
    that of any JSONOutput instances using that stream.

    \p The methods of this class provide the strong exception safety
    guarantee, modulo the guarantees provided by the underlying output
    stream. In particular, writes to the underlying output stream that
    fail may result in stream output that is inconsistent with the
    end state of the JSONOutput object.
 */
class JSONOutput {
public:

  /// \name Commonly used formatting options
  //@{
  static FormattingOptions const DEFAULT;      ///< UTF-8, no pretty printing
  static FormattingOptions const ASCII;        ///< ASCII, no pretty printing
  static FormattingOptions const PRETTY;       ///< UTF-8, pretty printing
  static FormattingOptions const PRETTY_ASCII; ///< ASCII, pretty printing
  static FormattingOptions const IPAC_SVC;     ///< IPAC SVC format
  //@}

  explicit JSONOutput(bool createValue=true);
  explicit JSONOutput(std::ostream & out,
                      FormattingOptions const & options=DEFAULT,
                      bool createValue=false);
  ~JSONOutput();

  inline FormattingOptions const & getFormattingOptions() const;
  inline Value const getValue() const;
  inline std::ostream * getOutputStream();

  /// \name Outputting values
  //@{
  JSONOutput & value();
  JSONOutput & value(char const * val);
  JSONOutput & value(char const * val, size_t n);
  JSONOutput & value(std::string const & val);
  JSONOutput & value(bool val);
  JSONOutput & value(unsigned char val);
  JSONOutput & value(signed char val);
  JSONOutput & value(unsigned short val);
  JSONOutput & value(short val);
  JSONOutput & value(unsigned int val);
  JSONOutput & value(int val);
  JSONOutput & value(unsigned long val);
  JSONOutput & value(long val);
  JSONOutput & value(unsigned long long val);
  JSONOutput & value(long long val);
  JSONOutput & value(float val);
  JSONOutput & value(double val);
  JSONOutput & value(long double val);

  template <typename T> inline JSONOutput & value(T const & val);
  template <typename T> inline JSONOutput & value(T const * val);
  template <typename T>
  inline JSONOutput & value(boost::shared_ptr<T> const & val);
  template <typename T>
  inline JSONOutput & value(boost::shared_ptr<T const> const & val);
  template <typename T>
  inline JSONOutput & value(boost::scoped_ptr<T> val);
  template <typename T>
  inline JSONOutput & value(boost::scoped_ptr<T const> val);
  //@}

  /// \name Outputting arrays
  //@{
  JSONOutput & array();

  template <typename ForwardsIterator>
  JSONOutput & array(ForwardsIterator begin, ForwardsIterator end);
  //@}

  /// \name Outputting key/value pairs
  //@{
  JSONOutput & key(std::string const & k);

  template <typename T>
  JSONOutput & pair(std::string const & k, T const & v);

  template <typename T>
  JSONOutput & pair(std::pair<std::string const, T> const & kv);
  //@}

  /// \name Outputting objects
  //@{
  JSONOutput & object();

  template <typename T>
  JSONOutput & object(std::map<std::string, T> const & map);

  template <typename T>
  static std::string const toString(T const & obj,
                                    FormattingOptions const & opts=DEFAULT);

  template <typename T> static Value const toValue(T const & obj);
  //@}

  /// \name Closing objects/arrays
  //@{
  JSONOutput & close(size_t n=1);
  inline JSONOutput & closeAll();
  //@}

  /// \name Encoding strings
  //@{
  static void append(std::ostream & out,
                     FormattingOptions const & opts,
                     std::string const & s);

  std::string const encode(std::string const & s);
  inline std::string const encode(char const * s);
  inline std::string const encode(char const * s, size_t n);
  //@}

private:
  /** \internal
      Class that enables strongly exception safe operation on a
      JSONOutput object. Any sequence of operations can be recovered
      from, so long as #JSONOutput::close() has not been used to close
      more JSON arrays/objects than were present at the time of Transaction
      creation.
    */
  class Transaction {
  public:
    Transaction(JSONOutput & out);
    ~Transaction() {
      rollback();
    }
    void commit() {
      _out = 0;
    }
    void rollback();
  private:
    JSONOutput * _out;
    size_t _level;
    Value _snapshot;
    detail::State _state;
    detail::CommonState _common;
  };

  Value _value;
  FormattingOptions _opts;
  std::vector<detail::State> _stack;
  detail::CommonState _common;
  std::ostream * _out;
  bool const _createValue;

  void valueOK();
  void writeValue(std::string const & encodedValue);
  inline void indent();
  void indent(size_t n);

  friend class Transaction;

  IPAC_STK_NONCOPYABLE(JSONOutput);
};

}}} // namespace ipac::stk::json

#include "JSONOutput.inl"

#endif //IPAC_STK_JSON_JSONOUTPUT_H_
