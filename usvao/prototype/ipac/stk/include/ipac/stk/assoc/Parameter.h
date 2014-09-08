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
    \brief  Classes that provide uniform access to a parameter that
            can either be fixed or extracted from table record.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_PARAMETER_H_
#define IPAC_STK_ASSOC_PARAMETER_H_

#include <string>

#include "boost/program_options.hpp"
#include "boost/shared_ptr.hpp"

#include "ipac/stk/json/JSONOutput.h"
#include "ipac/stk/table/IPACTableReader.h"
#include "ipac/stk/table/Record.h"


namespace ipac { namespace stk { namespace assoc {

/** Class which provides uniform access to a parameter.
  */
class Parameter {
public:
  virtual ~Parameter();

  virtual double getValue(ipac::stk::table::Record const & record) const = 0;
  virtual bool isAvailable() const = 0;
  virtual std::string const getErrorPrefix(
    ipac::stk::table::Record const & record) const = 0;
};


/** Class which provides uniform access to a parameter; parameter
    values can be fixed, or obtained from a table column. Angular
    parameter values are always returned in radians.
  */
class IPACColOrVal : public Parameter {
public:
  IPACColOrVal(boost::program_options::variables_map const & vm,
               std::string const & opt,
               ipac::stk::table::IPACTableReader const & reader);
  IPACColOrVal(boost::program_options::variables_map const & vm,
               std::string const & opt,
               ipac::stk::table::IPACTableReader const & reader,
               double defaultRadiansPer);
  IPACColOrVal(boost::program_options::variables_map const & vm,
               std::string const & opt,
               ipac::stk::table::IPACTableReader const & reader,
               double defaultValue,
               double defaultRadiansPer);

  virtual ~IPACColOrVal();

  virtual double getValue(ipac::stk::table::Record const & record) const;
  virtual bool isAvailable() const;
  virtual std::string const getErrorPrefix(
    ipac::stk::table::Record const & record) const;

  bool equals(IPACColOrVal const & other) const;

  bool isConstant() const {
    return _available && _column.getIndex() < 0;
  }
  double getConstantValue() const {
    return _value * _radPerValue;
  }
  ipac::stk::table::IPACTableReader::Column const & getColumn() const {
    return _column;
  }

private:
  ipac::stk::table::IPACTableReader::Column _column;
  double _value;
  double _radPerValue;
  bool _available;

  void init(boost::program_options::variables_map const & vm,
            std::string const & opt,
            ipac::stk::table::IPACTableReader const & reader,
            double defaultRadiansPer,
            bool unitless);
};

inline bool operator==(IPACColOrVal const & v1, IPACColOrVal const & v2) {
  return v1.equals(v2);
}

inline bool operator!=(IPACColOrVal const & v1, IPACColOrVal const & v2) {
  return !v1.equals(v2);
}

void outputJSON(IPACColOrVal const & cv, ipac::stk::json::JSONOutput & out);

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_PARAMETER_H_
