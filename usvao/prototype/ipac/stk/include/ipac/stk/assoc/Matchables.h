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
    \brief  Spatially matchable objects; currently circles (cones)
            and ellipses are supported.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_MATCHABLES_H_
#define IPAC_STK_ASSOC_MATCHABLES_H_

#include <vector>

#include "Eigen/Core"

#include "ipac/stk/json/Value.h"
#include "Matchable.h"
#include "Parameter.h"


namespace ipac { namespace stk { namespace assoc {

class CircleBuilder;
class EllipseBuilder;
  
/** A spatially matchable circle (cone) on the unit sphere.
  */
class MatchableCircle : public Matchable {
public:
  MatchableCircle(ipac::stk::table::Record const & data);
  virtual ~MatchableCircle();

  virtual bool matches(Matchable::Match & candidate) const;

private:
  Eigen::Vector4d _center;

  friend class CircleBuilder;
};

/** A spatially matchable ellipse on the unit sphere. These are 2D ellipses
    defined on the plane tangent to the unit sphere at the ellipse center and
    then centrally projected onto the surface of the unit sphere.
 */
class MatchableEllipse : public Matchable {
public:
  MatchableEllipse(ipac::stk::table::Record const & data);
  virtual ~MatchableEllipse();

  virtual bool matches(Matchable::Match & candidate) const;

private:
  double _sinPhi;    ///< sine of ellipse center latitude angle
  double _cosPhi;    ///< cosine of ellipse center latitude angle
  double _sinTheta;  ///< sine of ellipse center longitude angle
  double _cosTheta;  ///< cosine of ellipse center longitude angle
  double _sinAng;    ///< sine of ellipse position angle
  double _cosAng;    ///< cosine of ellipse position angle
  double _invMinor2; ///< 1/(smia*smia); smia = semi-minor axis length (rad)
  double _invMajor2; ///< 1/(smaa*smaa); smaa = semi-major axis length (rad)

  friend class EllipseBuilder;
};


/** Class for building vectors of MatchableCircle objects from
    vectors of table rows.
  */
class CircleBuilder {
public:
  static size_t const DEF_NUM_WARNINGS = 5;

  CircleBuilder(boost::shared_ptr<Parameter> const & theta,
                boost::shared_ptr<Parameter> const & phi,
                boost::shared_ptr<Parameter> const & radius,
                size_t numWarnings=DEF_NUM_WARNINGS);
  ~CircleBuilder();

  void build(std::vector<ipac::stk::table::Record> const & rows,
             double const maxRadius,
             std::vector<ipac::stk::table::Record> * badRows,
             std::vector<MatchableCircle> & out);

   ipac::stk::json::Value const getWarnings() const {
     return _warnings;
   }

private:
  boost::shared_ptr<Parameter> _theta;
  boost::shared_ptr<Parameter> _phi;
  boost::shared_ptr<Parameter> _radius;
  ipac::stk::json::Value _warnings;
  size_t _numWarnings;
};


/** Class for building vectors of MatchableEllipse objects from
    vectors of table rows.
  */
class EllipseBuilder {
public:
  static size_t const DEF_NUM_WARNINGS = 5;

  EllipseBuilder(boost::shared_ptr<Parameter> const & theta,
                 boost::shared_ptr<Parameter> const & phi,
                 boost::shared_ptr<Parameter> const & axisAngle,
                 boost::shared_ptr<Parameter> const & majorAxis,
                 boost::shared_ptr<Parameter> const & minorAxis,
                 boost::shared_ptr<Parameter> const & axisRatio,
                 size_t numWarnings=DEF_NUM_WARNINGS);
  ~EllipseBuilder();

  void build(std::vector<ipac::stk::table::Record> const & rows,
             double const maxRadius,
             std::vector<ipac::stk::table::Record> * badRows,
             std::vector<MatchableEllipse> & out);

  ipac::stk::json::Value const getWarnings() const {
    return _warnings;
  }

private:
  boost::shared_ptr<Parameter> _theta;
  boost::shared_ptr<Parameter> _phi;
  boost::shared_ptr<Parameter> _axisAngle;
  boost::shared_ptr<Parameter> _majorAxis;
  boost::shared_ptr<Parameter> _minorAxis;
  boost::shared_ptr<Parameter> _axisRatio;
  ipac::stk::json::Value _warnings;
  size_t _numWarnings;
};

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_MATCHABLES_H_
