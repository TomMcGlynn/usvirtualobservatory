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
    \brief  Implementation of the FormattingOptions class.
    \author Serge Monkewitz
  */
#include "ipac/stk/json/FormattingOptions.h"
#include "ipac/stk/except.h"


namespace ipac { namespace stk { namespace json {

using std::string;

namespace except = ipac::stk::except;

/** Creates default JSON formatting options; output will be UTF-8 encoded.
    Output will contain no new-lines, and little formatting whitespace.
 */
FormattingOptions::FormattingOptions() :
  _escapeUnicode(false),
  _escapeSolidus(false),
  _prettyPrint(false),
  _literalKeys(false),
  _keyRegex(),
  _indentation("\t"),
  _spacer(" "),
  _separator(", "),
  _kvSeparator(": "),
  _arrayOpen("["),
  _arrayClose("]"),
  _objectOpen("{"),
  _objectClose("}")
{ }

FormattingOptions::FormattingOptions(FormattingOptions const & opts) :
  _escapeUnicode(opts._escapeUnicode),
  _escapeSolidus(opts._escapeSolidus),
  _prettyPrint(opts._prettyPrint),
  _literalKeys(opts._literalKeys),
  _keyRegex(opts._keyRegex),
  _indentation(opts._indentation),
  _spacer(opts._spacer),
  _separator(opts._separator),
  _kvSeparator(opts._kvSeparator),
  _arrayOpen(opts._arrayOpen),
  _arrayClose(opts._arrayClose),
  _objectOpen(opts._objectOpen),
  _objectClose(opts._objectClose)
{ }

FormattingOptions::~FormattingOptions() { }

/** Sets unicode character escaping in keys/strings.

    \param esc  If \c true, unicode characters in keys/strings are escaped.
 */
FormattingOptions FormattingOptions::withEscapeUnicode(bool esc) const {
  FormattingOptions opts(*this);
  opts._escapeUnicode = esc;
  return opts;
}

/** Sets solidus character escaping in keys/strings.

    \param esc  If \c true, solidus characters in keys/strings are escaped.
 */
FormattingOptions FormattingOptions::withEscapeSolidus(bool esc) const {
  FormattingOptions opts(*this);
  opts._escapeSolidus = esc;
  return opts;
}

/** Sets JSON pretty-printing mode.

    \param pretty   If \c true, JSON is pretty printed.
 */
FormattingOptions FormattingOptions::withPrettyPrint(bool pretty) const {
  FormattingOptions opts(*this);
  opts._prettyPrint = pretty;
  return opts;
}

/** Sets literal-mode for JSON object keys.

    \param literal  If \c true, keys are not encoded or quoted prior
                    to output.
 */
FormattingOptions FormattingOptions::withLiteralKeys(bool literal) const {
  FormattingOptions opts(*this);
  opts._literalKeys = literal;
  return opts;
}

/** Sets a regular expression which JSON object keys must match.
 */
FormattingOptions FormattingOptions::withKeyRegex(string const & keyRe) const {
  FormattingOptions opts(*this);
  opts._keyRegex.reset(keyRe.empty() ? 0 : new boost::regex(keyRe));
  return opts;
}

/** Sets the string used to indent JSON array values and key/value pairs.
    Ignored if pretty-printing is turned off.
 */
FormattingOptions FormattingOptions::withIndentation(
  string const & indent) const
{
  FormattingOptions opts(*this);
  opts._indentation = indent;
  return opts;
}

/** Sets the string used to pad JSON array/object opening/closing strings.
 */
FormattingOptions FormattingOptions::withSpacer(string const & space) const {
  FormattingOptions opts(*this);
  opts._spacer = space;
  return opts;
}

/** Sets the string used to separate values and key/value pairs.
    Note that if this string does not consist of whitespace and exactly
    one comma, invalid JSON will be produced.
 */
FormattingOptions FormattingOptions::withSeparator(string const & sep) const {
  if (sep.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty separator string");
  }
  FormattingOptions opts(*this);
  opts._separator = sep;
  return opts;
}

/** Sets the string used to separate keys from their values.
    Note that if this string does not consist of whitespace and a
    single colon, invalid JSON will be produced.
 */
FormattingOptions FormattingOptions::withKeyValueSeparator(
  string const & kvSep) const
{
  if (kvSep.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty key-value separator string");
  }
  FormattingOptions opts(*this);
  opts._kvSeparator = kvSep;
  return opts;
}

/** Sets the string used to open a JSON array. Note that if this does
    not consist of whitespace and a single '[', invalid JSON will be
    produced.
  */
FormattingOptions FormattingOptions::withArrayOpen(
  string const & open) const
{
  if (open.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty array opening string");
  }
  FormattingOptions opts(*this);
  opts._arrayOpen = open;
  return opts;
}

/** Sets the string used to close a JSON array. Note that if this does
    not consist of whitespace and a single ']', invalid JSON will be
    produced.
  */
FormattingOptions FormattingOptions::withArrayClose(
  string const & close) const
{
  if (close.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty array closing string");
  }
  FormattingOptions opts(*this);
  opts._arrayClose = close;
  return opts;
}

/** Sets the string used to open a JSON object. Note that if this does
    not consist of whitespace and a single '{', invalid JSON will be
    produced.
  */
FormattingOptions FormattingOptions::withObjectOpen(
  string const & open) const
{
  if (open.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty object opening string");
  }
  FormattingOptions opts(*this);
  opts._objectOpen = open;
  return opts;
}

/** Sets the string used to open a JSON object. Note that if this does
    not consist of whitespace and a single '}', invalid JSON will be
    produced.
  */
FormattingOptions FormattingOptions::withObjectClose(
  string const & close) const
{
  if (close.empty()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Null or empty object closing string");
  }
  FormattingOptions opts(*this);
  opts._objectClose = close;
  return opts;
}

}}} // namespace ipac::stk::json
