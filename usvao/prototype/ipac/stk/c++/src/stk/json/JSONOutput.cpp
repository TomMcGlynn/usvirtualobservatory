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
    \brief  Implementation of the JSONOutput class.
    \author Serge Monkewitz
  */
#include "ipac/stk/json/JSONOutput.h"

#include "boost/lexical_cast.hpp"

#include "ipac/stk/except.h"


namespace ipac { namespace stk { namespace json {

using std::string;
using std::swap;
using std::ostream;
using std::ostringstream;

namespace except = ipac::stk::except;

JSONOutput::Transaction::Transaction(JSONOutput & out) :
  _out(&out),
  _level(out._stack.size()),
  _snapshot(),
  _state(),
  _common(out._common)
{
  if (_common.started) {
    _state = out._stack.back();
    if (_state.value && _state.inObject) {
      _snapshot = _state.value->get<Value::Map>();
    }
  }
}

void JSONOutput::Transaction::rollback() {
  if (_out) {
    if (_level < _out->_stack.size()) {
      _out->_stack.erase(_out->_stack.begin() + _level, _out->_stack.end());
    }
    if (_common.started) {
      if (_state.value) {
        if (_state.inObject) {
          *_state.value = _snapshot;
        } else {
          Value::Vector * vv = &_state.value->get<Value::Vector>();
          vv->erase(vv->begin() + _state.n, vv->end());
        }
      }
      _out->_stack.back() = _state;
    } else {
      _out->_value = Value();
    }
    _out->_common = _common;
    _out = 0;
  }
}

FormattingOptions const JSONOutput::DEFAULT(FormattingOptions::create());

FormattingOptions const JSONOutput::ASCII(
  FormattingOptions::create().withEscapeUnicode(true));

FormattingOptions const JSONOutput::PRETTY(
  FormattingOptions::create().withPrettyPrint(true));

FormattingOptions const JSONOutput::PRETTY_ASCII(
  FormattingOptions::create(JSONOutput::PRETTY).withEscapeUnicode(true));

FormattingOptions const JSONOutput::IPAC_SVC(
  FormattingOptions::create()
    .withEscapeUnicode(true)
    .withSpacer("")
    .withKeyValueSeparator("=")
    .withArrayOpen("[array ")
    .withArrayClose("]")
    .withObjectOpen("[struct ")
    .withObjectClose("]")
    .withLiteralKeys(true)
    .withKeyRegex("^[a-zA-Z_]+[a-zA-Z0-9_.\\-]*$"));


JSONOutput::JSONOutput(bool createValue) :
  _value(),
  _opts(DEFAULT),
  _stack(),
  _common(),
  _out(0),
  _createValue(createValue)
{ }

JSONOutput::JSONOutput(ostream & out,
                       FormattingOptions const & opts,
                       bool createValue) :
  _value(),
  _opts(opts),
  _stack(),
  _common(),
  _out(&out),
  _createValue(createValue)
{ }

JSONOutput::~JSONOutput() { }

/** Outputs a null value.
  */
JSONOutput & JSONOutput::value() {
  valueOK();
  if (_out) {
    writeValue("null");
  }
  Value * v = _stack.back().value;
  if (v) {
    v->addNull();
  }
  ++_stack.back().n;
  _common.sawKey = false;
  return *this;
}

/** Outputs the string pointed to or null.
  */
JSONOutput & JSONOutput::value(char const * val) {
  if (!val) {
    return value();
  }
  return value(string(val));
}

/** Outputs the string pointed to or null.
  */
JSONOutput & JSONOutput::value(char const * val, size_t n) {
  if (!val) {
    return value();
  }
  return value(string(val, n));
}

JSONOutput & JSONOutput::value(string const & val) {
  valueOK();
  if (_out) {
    writeValue(encode(val));
  }
  Value * v = _stack.back().value;
  if (v) {
    if (_common.sawKey) {
      v->add(_common.lastKey, val);
    } else {
      v->add(val);
    }
  }
  ++_stack.back().n;
  _common.sawKey = false;
  return *this;
}

JSONOutput & JSONOutput::value(bool val) {
  valueOK();
  if (_out) {
    writeValue(val ? "true" : "false");
  }
  Value * v = _stack.back().value;
  if (v) {
    if (_common.sawKey) {
      v->add(_common.lastKey, val);
    } else {
      v->add(val);
    }
  }
  ++_stack.back().n;
  _common.sawKey = false;
  return *this;
}

#define IPAC_STK_JSON_VALUE(t, c) \
  JSONOutput & JSONOutput::value(t val) { \
    valueOK(); \
    if (_out) { \
      writeValue(boost::lexical_cast<string>(static_cast<c>(val))); \
    } \
    Value * v = _stack.back().value; \
    if (v) { \
      if (_common.sawKey) { \
        v->add(_common.lastKey, val); \
      } else { \
        v->add(val); \
      } \
    } \
    ++_stack.back().n; \
    _common.sawKey = false; \
    return *this; \
  }

IPAC_STK_JSON_VALUE(unsigned char, unsigned long long)
IPAC_STK_JSON_VALUE(unsigned short, unsigned long long)
IPAC_STK_JSON_VALUE(unsigned int, unsigned long long)
IPAC_STK_JSON_VALUE(unsigned long, unsigned long long)
IPAC_STK_JSON_VALUE(unsigned long long, unsigned long long)
IPAC_STK_JSON_VALUE(signed char, long long)
IPAC_STK_JSON_VALUE(short, long long)
IPAC_STK_JSON_VALUE(int, long long)
IPAC_STK_JSON_VALUE(long, long long)
IPAC_STK_JSON_VALUE(long long, long long)
IPAC_STK_JSON_VALUE(float, long double)
IPAC_STK_JSON_VALUE(double, long double)
IPAC_STK_JSON_VALUE(long double, long double)

#undef IPAC_STK_JSON_VALUE

/** Begins outputting an array. Arrays may contain values, nested arrays
    and nested objects. Once the array has been completely written, \c close()
    should be called.
  */
JSONOutput & JSONOutput::array() {
  // ensure one subsequent push_back() will not throw
  _stack.reserve(_stack.size() + 1);
  if (!_common.started) {
    if (_out) {
      *_out << _opts.getObjectOpen();
    }
    if (_createValue) {
      _value = Value::vector();
      _stack.push_back(detail::State(&_value, false));
    } else {
      _stack.push_back(detail::State(0, false));
    }
    _common.started = true;
  } else if (_stack.size() == 0) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Root JSON object or array "
                          "has been closed; further output of JSON arrays "
                          "is not allowed.");
  } else {
    detail::State & state = _stack.back();
    Value * subValue = 0;
    if (state.inObject) {
      if (!_common.sawKey) {
        throw IPAC_STK_EXCEPT(except::IllegalState, "JSON arrays "
                              "cannot be written as JSON object keys");
      }
      if (_out) {
        *_out << _opts.getKeyValueSeparator();
        *_out << _opts.getArrayOpen();
      }
      if (state.value) {
        state.value->add(_common.lastKey, Value::vector());
        subValue = &(state.value->at(_common.lastKey));
      }
      _common.sawKey = false;
      ++state.n;
      _stack.push_back(detail::State(subValue, false));
    } else {
      if (_out) {
        *_out << (state.n > 0 ? _opts.getSeparator() : _opts.getSpacer());
        *_out << _opts.getArrayOpen();
      }
      if (state.value) {
        state.value->add(Value::vector());
        subValue = &(state.value->at(-1));
      }
      ++state.n;
      _stack.push_back(detail::State(subValue, false));
    }
  }
  return *this;
}

JSONOutput & JSONOutput::key(string const & k) {
  if (!_common.started) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Key value pairs "
                          "can only be written inside JSON objects");
  }
  if (_stack.size() == 0) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Root JSON object or array "
                          "has been closed; further output of key/value pairs "
                          "is not allowed.");
  }
  detail::State & state = _stack.back();
  if (!state.inObject || _common.sawKey) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Key value pairs "
                          "can only be written inside JSON objects");
  }
  boost::regex const * keyRe = _opts.getKeyRegex().get();
  if (keyRe) {
    if (!boost::regex_match(k.begin(), k.end(), *keyRe)) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, "JSON key does "
                            "not match key pattern " + keyRe->str());
    }
  }
  string key = k;
  string encodedKey = _opts.literalKeys() ? k : encode(k);
  if (_out) {
    if (_opts.prettyPrint()) {
      if (state.n > 0) {
        *_out << _opts.getSeparator();
      }
      _out->put('\n');
      indent();
    } else {
      *_out << (state.n == 0 ? _opts.getSpacer() : _opts.getSeparator());
    }
    *_out << encodedKey;
  }
  swap(_common.lastKey, key);
  _common.sawKey = true;
  return *this;
}

/** Begins outputting an object. Objects may contain key/value pairs, where
    a value can be any of the simple values, or a nested array or object.
    Once the object has been completely written, \c close() should be called.
  */
JSONOutput & JSONOutput::object() {
  // ensure one subsequent push_back() will not throw
  _stack.reserve(_stack.size() + 1);
  if (!_common.started) {
    if (_out) {
      *_out << _opts.getObjectOpen();
    }
    if (_createValue) {
      _value = Value::map();
      _stack.push_back(detail::State(&_value, true));
    } else {
      _stack.push_back(detail::State(0, true));
    }
    _common.started = true;
  } else if (_stack.size() == 0) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Root JSON object or array "
                          "has been closed; further output of JSON objects "
                          "is not allowed.");
  } else {
    detail::State & state = _stack.back();
    Value * subValue = 0;
    if (!state.inObject) {
      if (_out) {
        if (state.n > 0) {
          *_out << _opts.getSeparator();
        } else if (_opts.prettyPrint()) {
          _out->put('\n');
          indent();
        } else {
          *_out << _opts.getSpacer();
        }
        *_out << _opts.getObjectOpen();
      }
      if (state.value) {
        state.value->add(Value::map());
        subValue = &(state.value->at(-1));
      }
      ++state.n;
      _stack.push_back(detail::State(subValue, true));
    } else {
      if (!_common.sawKey) {
        throw IPAC_STK_EXCEPT(except::IllegalState,
                              "JSON objects cannot be written as JSON keys");
      }
      if (_out) {
        *_out << _opts.getKeyValueSeparator();
        *_out << _opts.getObjectOpen();
      }
      if (state.value) {
        state.value->add(_common.lastKey, Value::map());
        subValue = &(state.value->at(_common.lastKey));
      }
      _common.sawKey = false;
      ++state.n;
      _stack.push_back(detail::State(subValue, true));
    }
  }
  return *this;
}

/** Closes \c n currently open JSON objects and arrays. By default, one
    object/array is closed.
  */
JSONOutput & JSONOutput::close(size_t n) {
  if (n > _stack.size()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Cannot close more than "
                          "the number of currently open JSON objects/arrays");
  }
  if (n > 0 && _stack.back().inObject && _common.sawKey) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Cannot close a JSON "
                          "object immediately after writing a key.");
  }
  if (_out) {
    for (size_t i = 0; i < n; ++i) {
      size_t j = _stack.size() - i - 1; 
      if (_opts.prettyPrint() && _stack[j].n > 0) {
        _out->put('\n');
        indent(j);
      } else {
        *_out << _opts.getSpacer();
      }
      if (_stack[j].inObject) {
        *_out << _opts.getObjectClose();
      } else {
        *_out << _opts.getArrayClose();
      }
    }
  }
  _stack.erase(_stack.end() - n, _stack.end());
  return *this;
}

/** Appends a JSON encoded version of \c s to \c out.
    Note that \c s is assumed to be a UTF-8 string.
  */
void JSONOutput::append(ostream & out,
                        FormattingOptions const & opts,
                        string const & s)
{
  static char const * const DIGITS = "0123456789abcdef";
  static char const * const ESCAPE_TO = "bfnrt";
  static string const ALWAYS_ESCAPE = "\b\f\n\r\t";

  out.put('"');
  for (size_t i = 0; i < s.size(); ++i) {
    int cp = static_cast<int>(s[i]) & 0xff;
    if (cp >= 0x80 && opts.escapeUnicode()) {
      // Decode unicode code point
      if (cp <= 0xc1 || cp >= 0xf5) {
        throw IPAC_STK_EXCEPT(except::Format, "Illegal leading UTF-8 byte");
      }
      int numBytes = 0;
      if (cp < 0xe0) {
        numBytes = 2;
        cp &= 0x1f;
      } else if (cp <= 0xf0) {
        numBytes = 3;
        cp &= 0xf;
      } else {
        numBytes = 4;
        cp &= 0x7;
      }
      if (i + numBytes > s.size()) {
        throw IPAC_STK_EXCEPT(except::Format, "UTF-8 byte sequence extends "
                              "past end of string");
      }
      for (++i; numBytes > 1; --numBytes, ++i) {
        int b = static_cast<int>(s[i]) & 0xff;
        if (b < 0x80 || b > 0xbf) {
          throw IPAC_STK_EXCEPT(except::Format,
                                "Missing UTF-8 continuation byte");
        }
        cp = (cp << 6) | (b & 0x3f);
      }
      if (cp >= 0xd800 && cp <= 0xdfff) {
        throw IPAC_STK_EXCEPT(except::Format, "Invalid Unicode code point");
      }
      // Transform to UTF-16, output hex-encoded integers
      out.put('\\');
      out.put('u');
      if (cp > 0x10000) {
        cp -= 0x10000;
        int cu1 = 0xD800 | ((cp >> 10) & 0x3ff);
        int cu2 = 0xDC00 | (cp & 0x3ff);
        out.put(DIGITS[(cu1 >> 12) & 0xf]);
        out.put(DIGITS[(cu1 >> 8) & 0xf]);
        out.put(DIGITS[(cu1 >> 4) & 0xf]);
        out.put(DIGITS[cu1 & 0xf]);
        out.put('\\');
        out.put('u');
        out.put(DIGITS[(cu2 >> 12) & 0xf]);
        out.put(DIGITS[(cu2 >> 8) & 0xf]);
        out.put(DIGITS[(cu2 >> 4) & 0xf]);
        out.put(DIGITS[cu2 & 0xf]);
      } else {
        out.put(DIGITS[(cp >> 12) & 0xf]);
        out.put(DIGITS[(cp >> 8) & 0xf]);
        out.put(DIGITS[(cp >> 4) & 0xf]);
        out.put(DIGITS[cp & 0xf]);
      }
    } else if (cp <= 0x1f) {
      size_t j = ALWAYS_ESCAPE.find(static_cast<char>(cp));
      if (j != string::npos) {
        out.put('\\');
        out << ESCAPE_TO[j];
      } else {
        out.put('\\');
        out.put('u');
        out.put(DIGITS[(cp >> 12) & 0xf]);
        out.put(DIGITS[(cp >> 8) & 0xf]);
        out.put(DIGITS[(cp >> 4) & 0xf]);
        out.put(DIGITS[cp & 0xf]);
      }
    } else {
      if ((cp == '/' && opts.escapeSolidus()) || cp == '\\' || cp == '"') {
        out.put('\\');
      }
      out.put(static_cast<char>(cp));
    }
  }
  out.put('"');
}

string const JSONOutput::encode(string const & s) {
  ostringstream oss;
  append(oss, _opts, s);
  return oss.str();
}

void JSONOutput::valueOK() {
  if (!_common.started) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "JSON values can only be "
                          "written in JSON arrays or after JSON object keys.");
  }
  if (_stack.size() == 0) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "Root JSON object or array "
                          "has been closed; further value output is not "
                          "allowed.");
  }
  if (_stack.back().inObject && !_common.sawKey) {
    throw IPAC_STK_EXCEPT(except::IllegalState, "JSON values can only be "
                          "written in JSON arrays or after JSON object keys.");
  }
}

void JSONOutput::writeValue(std::string const & encodedValue) {
  if (_stack.back().inObject) {
    *_out << _opts.getKeyValueSeparator();
  } else {
    if (_opts.prettyPrint()) {
      if (_stack.back().n > 0) {
        *_out << _opts.getSeparator();
      }
      _out->put('\n');
      indent();
    } else if (_stack.back().n > 0) {
      *_out << _opts.getSeparator();
    } else {
      *_out << _opts.getSpacer();
    }
  }
  *_out << encodedValue;
}

void JSONOutput::indent(size_t level) {
  if (_out) {
    for (; level > 0; --level) {
      *_out << _opts.getIndentation();
    }
  }
}

}}} // namespace ipac::stk::json
