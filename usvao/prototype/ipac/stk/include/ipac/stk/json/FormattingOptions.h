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
#ifndef IPAC_STK_JSON_FORMATTINGOPTIONS_H_
#define IPAC_STK_JSON_FORMATTINGOPTIONS_H_

#include <string>

#include "boost/regex.hpp"
#include "boost/shared_ptr.hpp"


namespace ipac { namespace stk { namespace json {

/** Immutable class encapsulating JSON formatting options.
  */
class FormattingOptions {
public:
  FormattingOptions(FormattingOptions const & opts);
  ~FormattingOptions();

  /** Returns \c true if unicode characters in keys/strings are escaped.
    */
  bool escapeUnicode() const {
    return _escapeUnicode;
  }

  /** Returns \c true if solidus characters in keys/strings are escaped.
    */
  bool escapeSolidus() const {
    return _escapeSolidus;
  }

  /** Returns \c true if pretty-printing mode is on.
    */
  bool prettyPrint() const {
    return _prettyPrint;
  }

  /** Returns \c true if JSON keys are written out without being
      encoded and quoted.
    */
  bool literalKeys() const {
    return _literalKeys;
  }

  /** Returns the regular expression JSON object keys must match.
    */
  boost::shared_ptr<boost::regex const> const getKeyRegex() const {
    return _keyRegex;
  }

  /** Returns the string used to indent content in pretty-printing mode.
    */
  std::string const & getIndentation() const {
    return _indentation;
  }

  /** Returns the string used to pad JSON array/object opening/closing
      strings in pretty-printing mode.
    */
  std::string const & getSpacer() const {
    return _spacer;
  }

  /** Returns the string used to separate values or key/value pairs.
    */
  std::string const & getSeparator() const {
    return _separator;
  }

  /** Returns the string used to separate keys from their values.
    */
  std::string const & getKeyValueSeparator() const {
    return _kvSeparator;
  }

  /** Returns the string used to open JSON arrays.
    */
  std::string const & getArrayOpen() const {
    return _arrayOpen;
  }

  /** Returns the string used to close JSON arrays.
    */
  std::string const & getArrayClose() const {
    return _arrayClose;
  }

  /** Returns the string used to open JSON objects.
    */
  std::string const & getObjectOpen() const {
    return _objectOpen;
  }

  /** Returns the string used to close JSON objects.
    */
  std::string const & getObjectClose() const {
    return _objectClose;
  }

  // Descriptive builder methods that COW to preserve immutability

  /** Creates an FormattingOptions object with default attributes. */
  static FormattingOptions create() {
    return FormattingOptions();
  }

  /** Creates a copy of \c proto. */
  static FormattingOptions create(FormattingOptions proto) {
    return FormattingOptions(proto);
  }

  FormattingOptions withEscapeUnicode(bool esc) const;
  FormattingOptions withEscapeSolidus(bool escSolidus) const;
  FormattingOptions withPrettyPrint(bool pretty) const;
  FormattingOptions withLiteralKeys(bool literal) const;
  FormattingOptions withKeyRegex(std::string const & re) const;
  FormattingOptions withIndentation(std::string const & indent) const;
  FormattingOptions withSpacer(std::string const & space) const;
  FormattingOptions withSeparator(std::string const & sep) const;
  FormattingOptions withKeyValueSeparator(std::string const & sep) const;
  FormattingOptions withArrayOpen(std::string const & open) const;
  FormattingOptions withArrayClose(std::string const & close) const;
  FormattingOptions withObjectOpen(std::string const & open) const;
  FormattingOptions withObjectClose(std::string const & close) const;

private:
  bool _escapeUnicode;
  bool _escapeSolidus;
  bool _prettyPrint;
  bool _literalKeys;
  boost::shared_ptr<boost::regex const> _keyRegex;
  std::string _indentation;
  std::string _spacer;
  std::string _separator;
  std::string _kvSeparator;
  std::string _arrayOpen;
  std::string _arrayClose;
  std::string _objectOpen;
  std::string _objectClose;

  FormattingOptions();
  void operator=(FormattingOptions const &);
};

}}} // namespace ipac::stk::json

#endif // IPAC_STK_JSON_FORMATTINGOPTIONS_H_
