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
    \brief  Value class implementation
    \author Serge Monkewitz
  */
#include "ipac/stk/json/Value.h"

#include "boost/lexical_cast.hpp"

#include "ipac/stk/json/JSONOutput.h"


namespace ipac { namespace stk { namespace json {

using std::string;
using boost::any;
using boost::any_cast;
using boost::shared_ptr;

namespace except = ipac::stk::except;
namespace json = ipac::stk::json;

/** Creates a shallow copy of the specified Value vector.
  */
Value::Value(Value::Vector const & value) :
  _v(new any(value))
{ }

/** Creates a shallow copy of the specified Value map.
  */
Value::Value(Value::Map const & value) :
  _v(new any(value))
{ }

/** Creates a copy of the specified string.
  */
Value::Value(char const * value) :
  _v(new any(string(value)))
{ }

Value::~Value() { }

/** Returns the \c std::type_info for the underlying value type, or
    \c typeid(void) if the value is null.
  */
std::type_info const & Value::getType() const {
  if (isNull()) {
    return typeid(void);
  }
  return _v->type();
}

/** Returns the size of this Value: 0 for null values, 1 for scalar values,
    and the number of sub-values for maps and vectors of values.
  */
size_t Value::size() const {
  if (isNull()) {
    return 0;
  } else if (is<Vector>()) {
    return boost::any_cast<Vector>(*_v).size();
  } else if (is<Map>()) {
    return boost::any_cast<Map>(*_v).size();
  }
  return 1;
}

/// \name Setting values
//@{
/** Sets this Value to \c value.
  */
void Value::set(Value const & value) {
  if (_v.get() == value._v.get()) {
    return; // self-assignment has no effect
  }
  if (cycle(value)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Value assignment creates a reference cycle");
  }
  _v = value._v;
}

/** Sets this Value to the given value vector.
  */
void Value::set(Value::Vector const & value) {
  if (cycle(value)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Value assignment creates a reference cycle");
  }
  _v.reset(new any(value));
}

/** Sets this Value to the given value map.
  */
void Value::set(Value::Map const & value) {
  if (cycle(value)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Value assignment creates a reference cycle");
  }
  _v.reset(new any(value));
}
//@}

/// \name Untyped value access
//@{
/** Returns the i-th Value in the underlying value vector or throws an
    exception. Negative indexes are interpreted relative to the end of the
    underlying value vector; for example, -1 refers to the last element
    of a non-empty value vector.

    \throw ipac::stk::except::OutOfBounds
    \throw ipac::stk::except::TypeError
  */
Value const & Value::at(int i) const {
  Vector const * vv = boost::any_cast<Vector>(_v.get());
  if (vv == 0) {
    throw IPAC_STK_EXCEPT(ipac::stk::except::TypeError, "Value is a map, "
                          "scalar, or null: cannot retrieve underlying "
                          "values by index.");
  }
  Vector::size_type j = static_cast<Vector::size_type>(i);
  if (i >= 0) {
    if (j >= vv->size()) {
      throw IPAC_STK_EXCEPT(ipac::stk::except::OutOfBounds,
                            "Index out of bounds");
    }
  } else {
    Vector::size_type ni = static_cast<Vector::size_type>(-i);
    if (ni > vv->size()) {
      throw IPAC_STK_EXCEPT(ipac::stk::except::OutOfBounds,
                            "Index out bounds ");
    }
    j = vv->size() - ni;
  }
  return (*vv)[j];
}

/** Returns the underlying Value named \c key or throws an exception.

    \throw ipac::stk::except::NotFound
    \throw ipac::stk::except::TypeError
  */
Value const & Value::at(string const & key) const {
  Map const * vm = boost::any_cast<Map>(_v.get());
  if (vm == 0) {
    throw IPAC_STK_EXCEPT(ipac::stk::except::TypeError, "Value is a vector, "
                          "scalar, or null: cannot retrieve underlying "
                          "values by name.");
  }
  Map::const_iterator v = vm->find(key);
  if (v == vm->end()) {
    throw IPAC_STK_EXCEPT(ipac::stk::except::NotFound,
                          "value named " + key + " not found");
  }
  return v->second;
}
//@}


/// \name Removing values
//@{
/** Removes the \c i-th entry of this Value if this Value contains a vector
    with an entry at the specified index.

    \return   \c true if an entry was removed.
  */
bool Value::remove(int i) {
  Vector * vv = boost::any_cast<Vector>(_v.get());
  if (vv != 0) {
    if (i >= 0) {
      if (static_cast<Vector::size_type>(i) < vv->size()) {
        vv->erase(vv->begin() + i);
        return true;
      }
    } else if (static_cast<Vector::size_type>(-i) <= vv->size()) {
      vv->erase(vv->end() + i);
      return true;
    }
  }
  return false;
}

/** Removes the given entry from this Value if this Value contains a
    map with an entry for the specified key.

    \return   \c true if an entry was removed.
  */
bool Value::remove(std::string const & key) {
  Map * vm = boost::any_cast<Map>(_v.get());
  if (vm != 0) {
    return vm->erase(key) != 0;
  }
  return false;
}
//@}

/// \name Membership testing
//@{
/** Returns \c true if this Value contains a value vector with an entry
    at the specified index. Negative indexes are interpreted relative
    to the end of the underlying value vector; for example, -1 refers
    to the last element of a non-empty value vector.
  */
bool Value::contains(int i) const {
  Vector const * vv = boost::any_cast<Vector>(_v.get());
  if (vv == 0) {
    return false;
  }
  return (i >= 0 && static_cast<Vector::size_type>(i) < vv->size()) ||
         (i < 0 && static_cast<Vector::size_type>(-i) <= vv->size());
}

/** Returns \c true if this Value contains a value map with an entry
    named \c key.
  */
bool Value::contains(string const & key) const {
  Map const * vm = boost::any_cast<Map>(_v.get());
  if (vm == 0) {
    return false;
  }
  return vm->find(key) != vm->end();
}
//@}

/// \name Comparison
//@{
/** \internal
    Comparison implementation.
  */
template <typename T> bool Value::equalsImpl(T const & value) const {
  throw IPAC_STK_EXCEPT(except::NotSupported, "equalsImpl() not specialized "
                        "for type " + string(typeid(T).name()));
}

// equalsImpl<T> specializations
template <> bool Value::equalsImpl<Value::Map>(Value::Map const & value) const {
  Value::Map const * v = any_cast<Value::Map>(_v.get());
  return (v != 0) ? value == *v : false;
}

template <> bool Value::equalsImpl<Value::Vector>(Value::Vector const & value) const {
  Value::Vector const * v = any_cast<Value::Vector>(_v.get());
  return (v != 0) ? value == *v : false;
}

template <> bool Value::equalsImpl<string>(string const & value) const {
  string const * v = any_cast<string>(_v.get());
  return (v != 0) ? value == *v : false;
}

template <> bool Value::equalsImpl<bool>(bool const & value) const {
  bool const * v = any_cast<bool>(_v.get());
  return (v != 0) ? value == *v : false;
}

template <> bool Value::equalsImpl<long double>(
  long double const & value) const
{
  if (is<long double>()) {
    return value == *any_cast<long double>(_v.get());
  } else if (is<double>()) {
    return value == *any_cast<double>(_v.get());
  } else if (is<float>()) {
    return value == *any_cast<float>(_v.get());
  }
  return false;
}

template <> bool Value::equalsImpl<long long>(long long const & value) const {
  if (is<long long>()) {
    return value == *any_cast<long long>(_v.get());
  } else if (is<long>()) {
    return value == *any_cast<long>(_v.get());
  } else if (is<int>()) {
    return value == *any_cast<int>(_v.get());
  } else if (is<short>()) {
    return value == *any_cast<short>(_v.get());
  } else if (is<signed char>()) {
    return value == *any_cast<signed char>(_v.get());
  }
  return false;
}

template <> bool Value::equalsImpl<unsigned long long>(
  unsigned long long const & value) const
{
  if (is<unsigned long long>()) {
    return value == *any_cast<unsigned long long>(_v.get());
  } else if (is<unsigned long>()) {
    return value == *any_cast<unsigned long>(_v.get());
  } else if (is<unsigned int>()) {
    return value == *any_cast<unsigned int>(_v.get());
  } else if (is<unsigned short>()) {
    return value == *any_cast<unsigned short>(_v.get());
  } else if (is<unsigned char>()) {
    return value == *any_cast<unsigned  char>(_v.get());
  }
  return false;
}

bool Value::equals(Value const & value) const {
#define _IPAC_STK_EQUALS(V) \
  else if (is<V>()) { \
    return equalsImpl(static_cast<detail::MaxType<V>::type>( \
                    *any_cast<V>(value._v.get()))); \
  }

  if (_v.get() == value._v.get()) {
    return true;
  }
  if (isNull() || value.isNull()) {
    return false;
  }
  _IPAC_STK_EQUALS(Value::Map)
  _IPAC_STK_EQUALS(Value::Vector)
  _IPAC_STK_EQUALS(string)
  _IPAC_STK_EQUALS(bool)
  _IPAC_STK_EQUALS(long double)
  _IPAC_STK_EQUALS(double)
  _IPAC_STK_EQUALS(float)
  _IPAC_STK_EQUALS(long long)
  _IPAC_STK_EQUALS(long)
  _IPAC_STK_EQUALS(int)
  _IPAC_STK_EQUALS(short)
  _IPAC_STK_EQUALS(signed char)
  _IPAC_STK_EQUALS(unsigned long long)
  _IPAC_STK_EQUALS(unsigned long)
  _IPAC_STK_EQUALS(unsigned int)
  _IPAC_STK_EQUALS(unsigned short)
  _IPAC_STK_EQUALS(unsigned char)
  return false;
#undef _IPAC_STK_EQUALS
}
//@}

/** Returns a deep copy of this Value.
  */
Value const Value::deepCopy() const {
  Value copy;
  if (is<Vector>()) {
    Vector const * vv = boost::any_cast<Vector>(_v.get());
    copy._v.reset(new any(Vector()));
    Vector * vvcopy = boost::any_cast<Vector>(copy._v.get());
    vvcopy->reserve(vv->size());
    for (Vector::const_iterator i(vv->begin()), e(vv->end()); i != e; ++i) {
      vvcopy->push_back(i->deepCopy());
    }
  } else if (is<Map>()) {
    Map const * vm = boost::any_cast<Map>(_v.get());
    copy._v.reset(new any(Map()));
    Map * vmcopy = boost::any_cast<Map>(copy._v.get());
    for (Map::const_iterator i(vm->begin()), e(vm->end()); i != e; ++i) {
      vmcopy->insert(std::make_pair(i->first, (i->second).deepCopy()));
    }
  } else if (!isNull()) {
    copy._v.reset(new any(*_v));
  }
  return copy;
}

Value & Value::addNull() {
  return add(Value());
}

Value & Value::addNull(std::string const & key) {
  return add(key, Value());
}

/** Writes out this Value in JSON format.
  */
void Value::outputJSON(JSONOutput & out) const {
#define _IPAC_STK_VALUE(V) \
  else if (is<V>()) { \
    out.value(static_cast<detail::MaxType<V>::type>(*any_cast<V>(_v.get()))); \
  }

  if (isNull()) {
    out.value();
  } else if (is<Map>()) {
    out.object(*any_cast<Map>(_v.get()));
  } else if (is<Vector>()) {
    Vector const * vv = boost::any_cast<Vector>(_v.get());
    out.array(vv->begin(), vv->end());
  }
  _IPAC_STK_VALUE(string)
  _IPAC_STK_VALUE(bool)
  _IPAC_STK_VALUE(long double)
  _IPAC_STK_VALUE(double)
  _IPAC_STK_VALUE(float)
  _IPAC_STK_VALUE(long long)
  _IPAC_STK_VALUE(long)
  _IPAC_STK_VALUE(int)
  _IPAC_STK_VALUE(short)
  _IPAC_STK_VALUE(signed char)
  _IPAC_STK_VALUE(unsigned long long)
  _IPAC_STK_VALUE(unsigned long)
  _IPAC_STK_VALUE(unsigned int)
  _IPAC_STK_VALUE(unsigned short)
  _IPAC_STK_VALUE(unsigned char)
  else {
    throw IPAC_STK_EXCEPT(except::IllegalState,
                          "Value is of unsupported type");
  }
#undef _IPAC_STK_VALUE
}

/** Returns an empty value vector.
  */
Value const Value::vector() {
  return Value(Vector());
}

/** Returns an empty value map.
  */
Value const Value::map() {
  return Value(Map());
}

template <typename T> T Value::asImpl() const {
  return get<T>();
}

// specializations
template<> short Value::asImpl<short>() const {
  if (is<short>()) {
    return *any_cast<short>(_v.get());
  } else if (is<signed char>()) {
    return *any_cast<signed char>(_v.get());
  } else {
    throw IPAC_STK_EXCEPT(except::TypeError, "Cannot perform loss-less "
                          "cast from " + string(getType().name()) +
                          " to target signed integral type");
  }
}

template<> int Value::asImpl<int>() const {
  if (is<int>()) {
    return *any_cast<int>(_v.get());
  }
  return asImpl<short>();
}

template<> long Value::asImpl<long>() const {
  if (is<long>()) {
    return *any_cast<long>(_v.get());
  }
  return asImpl<int>();
}

template<> long long Value::asImpl<long long>() const {
  if (is<long long>()) {
    return *any_cast<long long>(_v.get());
  }
  return asImpl<long>();
}

template<> unsigned short Value::asImpl<unsigned short>() const {
  if (is<unsigned short>()) {
    return *any_cast<unsigned short>(_v.get());
  } else if (is<unsigned char>()) {
    return *any_cast<unsigned char>(_v.get());
  } else {
    throw IPAC_STK_EXCEPT(except::TypeError, "Cannot perform loss-less "
                          "cast from " + string(getType().name()) +
                          " to target unsigned integral type");
  }
}

template<> unsigned int Value::asImpl<unsigned int>() const {
  if (is<unsigned int>()) {
    return *any_cast<unsigned int>(_v.get());
  }
  return asImpl<unsigned short>();
}

template<> unsigned long Value::asImpl<unsigned long>() const {
  if (is<unsigned long>()) {
    return *any_cast<unsigned long>(_v.get());
  }
  return asImpl<unsigned int>();
}

template<> unsigned long long Value::asImpl<unsigned long long>() const {
  if (is<unsigned long long>()) {
    return *any_cast<unsigned long long>(_v.get());
  }
  return asImpl<unsigned long>();
}

template<> double Value::asImpl<double>() const {
  if (is<double>()) {
    return *any_cast<double>(_v.get());
  } else if (is<float>()) {
    return *any_cast<float>(_v.get());
  } else {
    throw IPAC_STK_EXCEPT(except::TypeError, "Cannot perform loss-less "
                          "cast from " + string(getType().name()) +
                          " to target floating point type");
  }
}

template<> long double Value::asImpl<long double>() const {
  if (is<long double>()) {
    return *any_cast<long double>(_v.get());
  }
  return asImpl<double>();
}

// explicit instantiations
template bool Value::asImpl<bool>() const;
template string Value::asImpl<string>() const;
template Value::Vector Value::asImpl<Value::Vector>() const;
template Value::Map Value::asImpl<Value::Map>() const;
template signed char Value::asImpl<signed char>() const;
template unsigned char Value::asImpl<unsigned char>() const;
template float Value::asImpl<float>() const;


/** \internal
    Checks that the underlying value is not null and has type \c T.
  */
void Value::check(std::type_info const & type) const {
  if (isNull()) {
    throw IPAC_STK_EXCEPT(except::TypeError, "Value is null, not a " +
                          string(type.name()));
  }
  if (_v->type() != type) {
    throw IPAC_STK_EXCEPT(except::TypeError, "Value is a " +
                          string(_v->type().name()) + " not a " +
                          string(type.name()));
  }
}

/** \internal
    Returns \c true if appending/assigning \c value creates a reference cycle.
  */
bool Value::cycle(Value const & value) const {
  if (isNull() || value.isNull()) {
    return false;
  } else if (_v.get() == value._v.get()) {
    return true;
  } else if (value.is<Vector>()) {
    return cycle(*any_cast<Vector>(value._v.get()));
  } else if (value.is<Map>()) {
    return cycle(*any_cast<Map>(value._v.get()));
  }
  return false;
}

/** \internal
    Returns \c true if appending/assigning \c value creates a reference cycle.
  */
bool Value::cycle(Vector const & value) const {
  for (Vector::const_iterator i(value.begin()), e(value.end()); i != e; ++i) {
    if (cycle(*i)) {
      return true;
    }
  }
  return false;
}

/** \internal
    Returns \c true if appending/assigning \c value creates a reference cycle.
  */
bool Value::cycle(Map const & value) const {
  for (Map::const_iterator i(value.begin()), e(value.end()); i != e; ++i) {
    if (cycle(i->second)) {
      return true;
    }
  }
  return false;
}


// Explit template instantiations
#define IPAC_STK_JSON_VALUE_INSTANTIATIONS(t) \
  template Value & Value::add<t>(t const &); \
  template Value & Value::add<t>(string const &, t const &);

IPAC_STK_JSON_VALUE_INSTANTIATIONS(bool)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(signed char)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(unsigned char)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(short)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(unsigned short)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(int)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(unsigned int)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(long)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(unsigned long)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(long long)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(unsigned long long)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(float)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(double)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(string)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(Value)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(Value::Vector)
IPAC_STK_JSON_VALUE_INSTANTIATIONS(Value::Map)

#undef IPAC_STK_JSON_VALUE_INSTANTIATIONS

}}} // namespace ipac::stk::json

template class std::map<std::string, ipac::stk::json::Value>;
template class std::vector<ipac::stk::json::Value>;

