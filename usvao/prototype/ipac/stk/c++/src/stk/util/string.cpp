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
    \brief  String utility function implementations.
    \author Serge Monkewitz
  */
#include "ipac/stk/util/string.h"

#include <cstring>
#include <limits>

#include "boost/regex.hpp"

#include "ipac/stk/except.h"


namespace ipac { namespace stk { namespace util {

using std::make_pair;
using std::numeric_limits;
using std::pair;
using std::string;
using std::vector;

namespace except = ipac::stk::except;

/** \copydoc split(std::string,char,int)
  */
vector<string> split(char const * s, char delimiter, size_t maxTokens) {
  vector<string> pieces;
  if (!s) {
    return pieces;
  }
  char const * str = s;
  char const * next = s;
  while (true) {
    while (*next != delimiter && *next != '\0') {
      ++next;
    }
    pieces.push_back(string(str, next - str));
    if ((*next == '\0') || (maxTokens > 0 && pieces.size() == maxTokens)) {
       break;
    }
    ++next;
    str = next;
  }
  return pieces;
}

/** Breaks \c s into a list of tokens separated by the \c delimiter, returning
    each token as a \c std::string. Successive \c delimiter characters produce
    empty tokens; a trailing delimiter character produces a trailing empty
    token.

    \param[in] s          The string to split.
    \param[in] delimiter  Delimiter character.
    \param[in] maxTokens  The maximum number of tokens to return. If zero,
                          all tokens in \c s are returned.

    \return   A list of the tokens in \c s.
  */
vector<string> split(string const & s, char delimiter, size_t maxTokens) {
  return split(s.c_str(), delimiter, maxTokens);
}

/** \copydoc split(std::string,std::string,size_t)

    \param[in] size   The length in characters of \c s.
  */
vector<string> split(char const * s,
                     size_t size,
                     string const & regex,
                     size_t maxTokens)
{
  vector<string> pieces;
  boost::regex re(regex);
  boost::cregex_token_iterator i(s, s + size, re, -1);
  boost::cregex_token_iterator j;
  for (; i != j; ++i) {
    pieces.push_back(i->str());
    if (maxTokens > 0 && pieces.size() == maxTokens) {
      break;
    }
  }
  return pieces;
}

/** \copydoc split(std::string,std::string,size_t)
  */
vector<string> split(char const * s, string const & regex, size_t maxTokens) {
  return split(s, std::strlen(s), regex, maxTokens);
}

/** Breaks \c s into a list of tokens separated by the regular expression
    \c regex, returning each token as a \c std::string. Successive regular
    expression matches produce empty tokens; a trailing delimiter character
    does not produce a trailing empty token.

    \param[in] s          The string to split.
    \param[in] regex      Delimiting regular expression.
    \param[in] maxTokens  The maximum number of tokens to return. If zero,
                          all tokens in \c s are returned.

    \return   A list of the tokens in \c s.
  */
vector<string> split(string const & s,
                     string const & regex,
                     size_t maxTokens)
{
  return split(s.c_str(), s.size(), regex, maxTokens);
}

/** Return a copy of \c s with leading and trailing whitespace removed.

    The characters recognized as whitespace are ' ', '\t', '\n', '\r',
    and '\f'.

    \param[in] s  The string to trim.
  */
string const trim(string const & s) {
  static string const WS(" \t\n\r\f");
  return trim(s, WS);
}

/** Return a copy of \c s with leading and trailing characters matching
    \c chars removed.

    \param[in] s      The string to trim.
    \param[in] chars  The characters to trim. 
  */
string const trim(string const & s, string const & chars) {
  size_t start = s.find_first_not_of(chars);
  if (start == string::npos) {
    return string();
  } else {
    size_t end = s.find_last_not_of(chars);
    return s.substr(start, (end - start) + 1);
  }
}

}}} // namespace ipac::stk::util
