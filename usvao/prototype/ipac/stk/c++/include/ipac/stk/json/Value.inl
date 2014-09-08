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
    \brief  Inline and templated member function definitions for Value.h
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_JSON_VALUE_H_
# error This file must be included from ipac/stk/json/Value.h
#else
# ifndef IPAC_STK_JSON_VALUE_INL_
# define IPAC_STK_JSON_VALUE_INL_

#include "boost/mpl/assert.hpp"
#include "boost/mpl/begin_end.hpp"
#include "boost/mpl/find.hpp"
#include "boost/mpl/if.hpp"
#include "boost/mpl/not.hpp"
#include "boost/type_traits/is_floating_point.hpp"
#include "boost/type_traits/is_same.hpp"
#include "boost/type_traits/is_signed.hpp"
#include "boost/type_traits/is_unsigned.hpp"

#include "ipac/stk/except.h"

namespace ipac { namespace stk { namespace json {

namespace detail {
  template <typename T> struct MaxSignedType {
    typedef long long type;
  };
  template <typename T> struct MaxUnsignedType {
    typedef unsigned long long type;
  };
  template <typename T> struct MaxFpType {
    typedef long double type;
  };
  template <typename T> struct SameType {
    typedef T type;
  };
  template <typename T> struct RefType {
    typedef T const & type;
  };

  template <typename T> struct MaxType :
    boost::mpl::if_<
      boost::is_same<T, bool>,
      SameType<T>,
      typename boost::mpl::if_<
        boost::is_floating_point<T>,
        MaxFpType<T>,
        typename boost::mpl::if_<
          boost::is_signed<T>,
          MaxSignedType<T>,
          typename boost::mpl::if_<
            boost::is_unsigned<T>,
            MaxUnsignedType<T>,
            RefType<T>
          >::type
        >::type
      >::type
    >::type
  { };
} // namespace detail

#define _IPAC_STK_TYPECHECKVALUE(t) \
  BOOST_MPL_ASSERT_NOT(( boost::is_same< typename boost::mpl::find<Value::Types, t >::type, boost::mpl::end<Value::Types>::type > ))

/** Creates a null Value.
  */
inline Value::Value() : _v() { }

/** Creates a shallow copy of the specified Value.
  */
inline Value::Value(Value const & value) : _v(value._v) { }

/** Creates a Value containing a copy of \c value.
  */
template <typename T>
Value::Value(T const & value) :
  _v(new boost::any(value))
{
  _IPAC_STK_TYPECHECKVALUE(T);
}

/** Creates a Value containing a copy of the given vector.
    Each input element is cast to a Value.
  */
template <typename T>
Value::Value(std::vector<T> const & value) :
 _v()
{
  _IPAC_STK_TYPECHECKVALUE(T);
  typedef typename std::vector<T>::const_iterator Iter;
  boost::shared_ptr<boost::any> v(new boost::any(Vector()));
  Vector * vv = boost::any_cast<Vector>(v.get());
  vv->reserve(value.size());
  for (Iter i(value.begin()), e(value.end()); i != e; ++i) {
    vv->push_back(Value(*i));
  }
  using std::swap;
  swap(_v, v);
}

/** Creates a Value containing a copy of the given map.
    Each named input element is cast to a Value.
  */
template <typename T>
Value::Value(std::map<std::string, T> const & value) :
 _v()
{
  _IPAC_STK_TYPECHECKVALUE(T);
  typedef typename std::map<std::string, T>::const_iterator Iter;
  boost::shared_ptr<boost::any> v(new boost::any(Map()));
  Map * vm = boost::any_cast<Map>(v.get());
  for (Iter i(value.begin()), e(value.end()); i != e; ++i) {
    vm->insert(std::make_pair(i->first, Value(i->second)));
  }
  using std::swap;
  swap(_v, v);
}

/// \name Typed value retrieval
//@{

/** Returns \c true if the underying value is \c null.
  */
inline bool Value::isNull() const {
  return _v.get() == 0;
}

/** Returns \c true if the \c size() of this Value is 0.
  */
inline bool Value::isEmpty() const {
  return size() == 0;
}

/** Returns \c true if the underlying value has type \c T.
  */
template <typename T> inline bool Value::is() const {
  return boost::any_cast<T>(_v.get()) != 0;
}

//@{
/** Returns the underlying value of type \c T or throws an exception.

    \throw ipac::stk::except::TypeError
  */
template <typename T> inline T const & Value::get() const {
  _IPAC_STK_TYPECHECKVALUE(T);
  check(typeid(T));
  return *boost::any_cast<T>(_v.get());
}

template <typename T> inline T & Value::get() {
  _IPAC_STK_TYPECHECKVALUE(T);
  check(typeid(T));
  return *boost::any_cast<T>(_v.get());
}
//@}

//@{
/** Returns the i-th underlying value of type \c T or throws an exception.
  */
template <typename T> inline T const & Value::get(int i) const {
  return at(i).get<T>();
}

template <typename T> inline T & Value::get(int i) {
  return at(i).get<T>();
}
//@}

//@{
/** Returns the underlying value of type \c T named \c key or throws an
    exception.
  */
template <typename T>
inline T const & Value::get(std::string const & key) const {
  return at(key).get<T>();
}

template <typename T> inline T & Value::get(std::string const & key) {
  return at(key).get<T>();
}
//@}

/** Returns a copy of the underlying value if it is convertible
    to type \c T or throws an exception. Only defined for values of
    built-in type and strings.

    \throw ipac::stk::except::TypeError
  */
template <typename T> inline T Value::as() const {
  _IPAC_STK_TYPECHECKVALUE(T);
  BOOST_MPL_ASSERT((boost::mpl::not_<
    boost::mpl::or_< boost::is_same<T, Map>,  boost::is_same<T, Vector> >
  >));
  return asImpl<T>();
}

/** Returns a copy of the i-th underlying value if it is convertible
    to type \c T or throws an exception. Negative indexes are interpreted
    relative to the end of the underlying value vector; for example, -1
    refers to the last element of a non-empty value vector.
  */
template <typename T> inline T Value::as(int i) const {
  Value const & v = this->operator[](i);
  return v.as<T>();
}

/** Returns the underlying value named \c key if it is convertible to
    type \c T or throws an exception.
  */
template <typename T> inline T Value::as(std::string const & key) const {
  Value const & v = this->operator[](key);
  return v.as<T>();
}
//@}

/// \name Untyped value access
//@{
/** Returns the i-th Value in the underlying value vector or
    throws an exception.
  */
inline Value & Value::at(int i) {
  return const_cast<Value &>(static_cast<Value const *>(this)->at(i));
}
inline Value const & Value::operator[](int i) const {
  return at(i);
}
inline Value & Value::operator[](int i) {
  return at(i);
}
//@}

/// \name Untyped value access
//@{
/** Returns the underlying Value named \c key or throws an exception.
  */
inline Value & Value::at(std::string const & key) {
  return const_cast<Value &>(static_cast<Value const *>(this)->at(key));
}
inline Value const & Value::operator[](std::string const & key) const {
  return at(key);
}
inline Value & Value::operator[](std::string const & key) {
  return at(key);
}
//@}

/// \name Setting values
//@{
/** Sets this Value to null.
  */
inline void Value::set() {
  _v.reset();
}

/** Sets this Value to a shallow copy of \c value.
  */
template <typename T> inline void Value::set(T const & value) {
  set(Value(value));
}

/** Assigns a shallow copy of \c value to this Value.
  */
template <typename T> inline Value & Value::operator=(T const & value) {
  set(Value(value));
  return *this;
}

/** Swaps the contents of this Value with \c value.
  */
void Value::swap(Value & value) {
  using std::swap;
  swap(_v, value._v);
}
//@}

/// \name Comparison
//@{
template <typename T> bool Value::equals(T const & value) const {
  _IPAC_STK_TYPECHECKVALUE(T);
  return equalsImpl(static_cast<typename detail::MaxType<T>::type>(value));
}

inline bool operator==(Value const & v1, Value const & v2) {
  return v1.equals(v2);
}
template <typename T> inline bool operator==(Value const & v1, T const & v2) {
  return v1.equals(v2);
}
template <typename T> inline bool operator==(T const & v1, Value const & v2) {
  return v2.equals(v1);
}
inline bool operator!=(Value const & v1, Value const & v2) {
  return !v1.equals(v2);
}
template <typename T> inline bool operator!=(Value const & v1, T const & v2) {
  return !v1.equals(v2);
}
template <typename T> inline bool operator!=(T const & v1, Value const & v2) {
  return !v2.equals(v1);
}
//@}

/// \name Appending values
//@{
template <typename T> Value & Value::add(T const & value) {
  if (!is<Vector>()) {
    throw IPAC_STK_EXCEPT(ipac::stk::except::TypeError, "Cannot add "
                          "values unless target Value is a vector");
  }
  Value v(value);
  if (cycle(v)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Appending value creates a reference cycle");
  }
  boost::any_cast<Vector>(_v.get())->push_back(v);
  return *this;
}

template <typename T>
Value & Value::add(std::string const & key, T const & value) {
  if (!is<Map>()) {
    throw IPAC_STK_EXCEPT(ipac::stk::except::TypeError, "Cannot add "
                          "key/value pairs unless target Value is a map");
  }
  Value v(value);
  if (cycle(v)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Appending value creates a reference cycle");
  }
  boost::any_cast<Map>(_v.get())->insert(std::make_pair(key, v));
  return *this;
}

template <typename T>
inline Value & Value::add(std::pair<std::string, T> const & value) {
  return add(value.first, value.second);
}
//@}

inline void swap(Value & v1, Value & v2) {
  v1.swap(v2);
}

inline void outputJSON(Value const & v, JSONOutput & out) {
  v.outputJSON(out);
}

}}}

# endif // IPAC_STK_JSON_VALUE_INL_
#endif // IPAC_STK_JSON_VALUE_H_
