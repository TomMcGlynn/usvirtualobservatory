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
    \brief  A heirarchical collection of hetergeneous values.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_JSON_VALUE_H_
#define IPAC_STK_JSON_VALUE_H_

#include <map>
#include <string>
#include <vector>
#include <utility>

#include "boost/any.hpp"
#include "boost/mpl/vector.hpp"
#include "boost/shared_ptr.hpp"


namespace ipac { namespace stk { namespace json {

class JSONOutput;

/** A heirarchical collection of heterogeneous values.

    All methods except those involving output provide the
    strong exception safety guarantee.
  */
class Value {
public:
  typedef std::map<std::string, Value> Map;
  typedef std::vector<Value> Vector;

  typedef boost::mpl::vector<
    Map,
    Vector,
    std::string,
    long double,
    double,
    float,
    long long,
    unsigned long long,
    long,
    unsigned long,
    int,
    unsigned int,
    short,
    unsigned short,
    signed char,
    unsigned char,
    bool
  > Types;

  inline Value();
  inline Value(Value const & value);
  Value(Vector const & value);
  Value(Map const & value);
  Value(char const * value);
  template <typename T> Value(T const & value);
  template <typename T> Value(std::vector<T> const & value);
  template <typename T> Value(std::map<std::string, T> const & value);
  ~Value();

  inline bool isNull() const;
  inline bool isEmpty() const;
  size_t size() const;
  template <typename T> inline bool is() const;
  std::type_info const & getType() const;

  template <typename T> inline T const & get() const;
  template <typename T> inline T const & get(int i) const;
  template <typename T> inline T const & get(std::string const & key) const;
  template <typename T> inline T & get();
  template <typename T> inline T & get(int i);
  template <typename T> inline T & get(std::string const & key);

  template <typename T> inline T as() const;
  template <typename T> inline T as(int i) const;
  template <typename T> inline T as(std::string const & key) const;

  Value const & at(int i) const;
  Value const & at(std::string const & key) const;
  inline Value const & operator[](int i) const;
  inline Value const & operator[](std::string const & key) const;
  inline Value & at(int i);
  inline Value & at(std::string const & key);
  inline Value & operator[](int i);
  inline Value & operator[](std::string const & key);

  bool contains(int i) const;
  bool contains(std::string const & key) const;
  
  bool remove(int i);
  bool remove(std::string const & key);
  inline void set();
  void set(Value const & value);
  void set(Vector const & value);
  void set(Map const & value);
  template <typename T> inline void set(T const & value);
  template <typename T> inline Value & operator=(T const & value);
  
  bool equals(Value const & value) const;
  inline bool equals(char const * value) const;
  template <typename T> bool equals(T const & value) const;

  inline void swap(Value & value);
  Value const deepCopy() const;

  Value & addNull();
  Value & addNull(std::string const & key);
  template <typename T> Value & add(T const & value);
  template <typename T> Value & add(std::string const & key, T const & value);
  template <typename T> inline Value & add(std::pair<std::string, T> const & value);

  void outputJSON(JSONOutput & out) const;

  static Value const vector();
  static Value const map();

private:
  boost::shared_ptr<boost::any> _v;

  void check(std::type_info const & type) const;
  bool cycle(Value const & value) const;
  bool cycle(Vector const & value) const;
  bool cycle(Map const & value) const;
  template <typename T> bool equalsImpl(T const & value) const;
  template <typename T> T asImpl() const;
};

inline void swap(Value & v1, Value & v2);

inline void outputJSON(Value const & v, JSONOutput & out);

inline bool operator==(Value const & v1, Value const & v2);
template <typename T> inline bool operator==(Value const & v1, T const & v2);
template <typename T> inline bool operator==(T const & v1, Value const & v2);
inline bool operator!=(Value const & v1, Value const & v2);
template <typename T> inline bool operator!=(Value const & v1, T const & v2);
template <typename T> inline bool operator!=(T const & v1, Value const & v2);

// Extern declarations for pre-instantiated templates
#define IPAC_STK_JSON_VALUE_EXTERNS(t) \
  extern template Value & Value::add<t>(t const &); \
  extern template Value & Value::add<t>(std::string const &, t const &);

IPAC_STK_JSON_VALUE_EXTERNS(bool)
IPAC_STK_JSON_VALUE_EXTERNS(signed char)
IPAC_STK_JSON_VALUE_EXTERNS(unsigned char)
IPAC_STK_JSON_VALUE_EXTERNS(short)
IPAC_STK_JSON_VALUE_EXTERNS(unsigned short)
IPAC_STK_JSON_VALUE_EXTERNS(int)
IPAC_STK_JSON_VALUE_EXTERNS(unsigned int)
IPAC_STK_JSON_VALUE_EXTERNS(long)
IPAC_STK_JSON_VALUE_EXTERNS(unsigned long)
IPAC_STK_JSON_VALUE_EXTERNS(long long)
IPAC_STK_JSON_VALUE_EXTERNS(unsigned long long)
IPAC_STK_JSON_VALUE_EXTERNS(float)
IPAC_STK_JSON_VALUE_EXTERNS(double)
IPAC_STK_JSON_VALUE_EXTERNS(std::string)
IPAC_STK_JSON_VALUE_EXTERNS(Value::Vector)
IPAC_STK_JSON_VALUE_EXTERNS(Value::Map)

#undef IPAC_STK_JSON_VALUE_EXTERNS

}}} // namespace ipac::stk::json

extern template class std::map<std::string, ipac::stk::json::Value>;
extern template class std::vector<ipac::stk::json::Value>;

#include "Value.inl"

#endif // IPAC_STK_JSON_VALUE_H_
