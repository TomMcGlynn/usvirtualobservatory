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
    \brief  MatchableCircle class implementation.
    \author Serge Monkewitz
  */
#include "ipac/stk/assoc/Matchables.h"

#include "boost/lexical_cast.hpp"
#include "boost/regex.hpp"

#include "ipac/stk/assoc/constants.h"
#include "ipac/stk/assoc/units.h"
#include "ipac/stk/util/string.h"


namespace ipac { namespace stk { namespace assoc {

using std::string;
using std::vector;

using ipac::stk::table::IPACTableReader;
using ipac::stk::table::Record;
using ipac::stk::json::JSONOutput;
using ipac::stk::util::split;

namespace except = ipac::stk::except;
namespace po = boost::program_options;

Parameter::~Parameter() { }

IPACColOrVal::IPACColOrVal(po::variables_map const & vm,
                           string const & opt,
                           IPACTableReader const & reader) :
  _column(),
  _value(0.0),
  _radPerValue(1.0),
  _available(false)
{
  init(vm, opt, reader, 1.0, true);
}

IPACColOrVal::IPACColOrVal(po::variables_map const & vm,
                           string const & opt,
                           IPACTableReader const & reader,
                           double defaultRadiansPer) :
  _column(),
  _value(0.0),
  _radPerValue(defaultRadiansPer),
  _available(false)
{
  init(vm, opt, reader, defaultRadiansPer, false);
}

IPACColOrVal::IPACColOrVal(po::variables_map const & vm,
                           string const & opt,
                           IPACTableReader const & reader,
                           double defaultValue,
                           double defaultRadiansPer) :
  _column(),
  _value(defaultValue),
  _radPerValue(defaultRadiansPer),
  _available(true)
{
  init(vm, opt, reader, defaultRadiansPer, false);
}

void IPACColOrVal::init(po::variables_map const & vm,
                        string const & opt,
                        IPACTableReader const & reader,
                        double const defaultRadiansPer,
                        bool unitless)
{
  static boost::regex const INDEX_REGEX("^\\s*:(\\d+)\\s*$");
  static boost::regex const VALUE_REGEX(
    "^\\s*([+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?)\\s*(.*)$");

  int n = vm.count(opt);
  if (n == 0) {
    return;
  } else if (n > 1) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "The --" + opt +
                          " option is specified more than once.");
  }
  string s = vm[opt].as<string>();
  boost::smatch m1;
  boost::smatch m2;
  IPACTableReader::Metadata const & meta = reader.getMetadata();
  if (boost::regex_match(s, m1, INDEX_REGEX)) {
    // got a column index
    int which = boost::lexical_cast<int>(m1.str(1));
    int n = static_cast<int>(meta.getColumns().size());
    if (which < 1 || which > n) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "The column index %d specified via --%s in table file %s is out "
        "of bounds; please specify a value between 1 and %d", which,
        opt.c_str(), meta.getPath().file_string().c_str(), n));
    }
    _column = meta.getColumns()[which - 1];
    _available = true;
  } else if (boost::regex_match(s, m2, VALUE_REGEX)) {
    // got a value
    _value = boost::lexical_cast<double>(m2.str(1));
    if (unitless) {
      if (m2.str(2).size() != 0) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter,
          "The --" + opt + " option value " + s + " is not an input "
          "table column name, index or unit-less numerical value.");
      }
      _radPerValue = 1.0;
    } else {
      _radPerValue = radiansPer(m2.str(2), defaultRadiansPer);
      if (_radPerValue == 0.0) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, "The --" + opt +
          " option value " + s + " has an invalid unit suffix.");
      }
    }
    _available = true;
  } else {
    // got a column name or name list
    typedef vector<string>::const_iterator Iter;
    vector<string> names(split(s, ','));
    for (Iter i(names.begin()), e(names.end()); i != e; ++i) {
      if (meta.hasColumn(*i)) {
        _column = meta.getColumn(*i);
        _available = true;
        break;
      }
    }
  }
  if (_column.getIndex() >= 0) {
    // got a column
    if (unitless) {
      _radPerValue = 1.0;
    } else {
      _radPerValue = radiansPer(_column.getUnits(), defaultRadiansPer);
      if (_radPerValue == 0.0) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, "The column " + s +
          " specified via --" + opt + " in table file " +
          meta.getPath().file_string() + " must have an angular unit; got " +
          _column.getUnits());
      }
    }
  }
}

IPACColOrVal::~IPACColOrVal() { }

double IPACColOrVal::getValue(Record const & record) const {
  assert(_available && "Attempt to retrieve an unavailable column or value");
  double v;
  if (_column.getIndex() < 0) {
    v = _value;
  } else {
    v = _column.getDouble(record);
  }
  return _radPerValue * v;
}

bool IPACColOrVal::isAvailable() const {
  return _available;
}

string const IPACColOrVal::getErrorPrefix(Record const & record) const {
  if (!_available || _column.getIndex() < 0) {
    return string();
  }
  return except::message("Row %llu column '%s': ",
                         static_cast<unsigned long long>(record.rowid + 1),
                         _column.getName().c_str());
}

bool IPACColOrVal::equals(IPACColOrVal const & other) const {
  return _column == other._column &&
         _value == other._value &&
         _radPerValue == other._radPerValue &&
         _available == other._available;
}

void outputJSON(IPACColOrVal const & cv, JSONOutput & out) {
  out.object();
  if (cv.isConstant()) {
    out.pair("value", cv.getConstantValue());
  } else {
    out.pair("column", cv.getColumn().getName());
  }
  out.close();
}

}}} // namespace ipac::stk::assoc
