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
    \brief  A spherical coordinate space bounding box.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_SPHERICAL_BOX_H_
#define IPAC_STK_ASSOC_SPHERICAL_BOX_H_

#include <algorithm>

#include "Eigen/Core"


namespace ipac { namespace stk { namespace assoc {

/** \brief A spherical coordinate space bounding box.

    This is similar to a bounding box in cartesian space in that
    it is specified by a pair of points; however, the box may
    correspond to the entire unit-sphere, a spherical cap, a
    lune or a rectangle, and can span the 0/360 degree longitude
    angle discontinuity.

    Points falling exactly on spherical box edges are considered
    to be inside (contained by) the box.
  */
class SphericalBox {
public:
  SphericalBox();
  SphericalBox(double theta, double phi);
  SphericalBox(double minPhi, double begTheta, double maxPhi, double endTheta);

  double getMinPhi() const {
    return _minPhi;
  }

  double getMaxPhi() const {
    return _maxPhi;
  }

  double getPhiExtent() const {
    return _maxPhi - _minPhi;
  }

  double getBegTheta() const {
    return _begTheta;
  }

  double getEndTheta() const {
    return _endTheta;
  }

  double getThetaExtent() const {
    if (_begTheta > _endTheta) {
      return 360.0 - _begTheta + _endTheta;
    }
    return _endTheta - _begTheta;
  }

  Eigen::Vector2d const getCenter() const {
    Eigen::Vector2d center(0.5 * (_begTheta + _endTheta),
                           0.5 * (_minPhi + _maxPhi));
    if (_begTheta > _endTheta) {
      center.x() += (center.x() >= 180.0) ? -180.0 : 180.0;
    }
    return center;
  }

  bool equals(SphericalBox const & b) const {
    return _minPhi == b._minPhi && _maxPhi == b._maxPhi &&
           _begTheta == b._begTheta && _endTheta == b._endTheta;
  }

  /** Returns \c true if this spherical box is empty, i.e. contain no points.
    */
  bool isEmpty() const {
    return _minPhi > _maxPhi;
  }

  /** Returns \c true if this spherical box is full, i.e. contains every point
      on the unit sphere.
    */
  bool isFull() const {
    return _minPhi <= -90.0 && _maxPhi >= 90.0 &&
           _begTheta <= 0.0 && _endTheta >= 360.0;
  }

  /** Returns \c true if the bounding box wraps across the 0.0/360.0
      longitude angle discontinuity.
    */
  bool wraps() const {
    return _begTheta > _endTheta;
  }

  /** Returns \c true if \c (theta,phi) is inside this bounding box.
    */
  bool contains(double theta, double phi) const {
    if (phi < _minPhi || phi > _maxPhi) {
      return false;
    }
    if (_begTheta > _endTheta) {
      return theta >= _begTheta || theta <= _endTheta;
    } else {
      return theta >= _begTheta && theta <= _endTheta;
    }
  }

  /** Returns \c true if \c theta is inside this bounding box.
    */
  bool containsTheta(double theta) const {
    if (_begTheta > _endTheta) {
      return theta >= _begTheta || theta <= _endTheta;
    } else {
      return theta >= _begTheta && theta <= _endTheta;
    }
  }

  bool contains(SphericalBox const & box) const;
  bool intersects(SphericalBox const & box) const;

  /** Empties this spherical box.
    */
  void setEmpty() {
    _minPhi = 90.0;
    _maxPhi = -90.0;
    _begTheta = 0.0;
    _endTheta = 0.0;
  }

  /** Expands this spherical box to fill the unit sphere.
    */
  void setFull() {
    _minPhi = -90.0;
    _maxPhi = 90.0;
    _begTheta = 0.0;
    _endTheta = 360.0;
  }

  /** Sets this box to have a center of \code (theta, phi) \endcode,
      a longitude angle half-extent of \c thetaExt and a latitude angle
      half-extent of \c phiExt.
    */
  void setCenterAndExtents(double theta,
                           double phi,
                           double thetaExt,
                           double phiExt)
  {
    _minPhi = std::max(-90.0, phi - phiExt);
    _maxPhi = std::min(90.0, phi + phiExt);
    double bt = theta - thetaExt;
    double et = theta + thetaExt;
    _begTheta = bt - 360.0 * std::floor(bt / 360.0);
    _endTheta = et - 360.0 * std::floor(et / 360.0);
  }

  void swap(SphericalBox & b) {
    using std::swap;
    swap(_minPhi, b._minPhi);
    swap(_maxPhi, b._maxPhi);
    swap(_begTheta, b._begTheta);
    swap(_endTheta, b._endTheta);
  }

  SphericalBox & extend(SphericalBox const & b);
  SphericalBox & extend(double theta, double phi);
  SphericalBox & shrink(SphericalBox const & b);

private:
  double _minPhi;
  double _begTheta;
  double _maxPhi;
  double _endTheta;
};

inline void swap(SphericalBox & b1, SphericalBox & b2) {
  b1.swap(b2);
}

inline bool operator==(SphericalBox const & b1, SphericalBox const & b2) {
  return b1.equals(b2);
}

inline bool operator!=(SphericalBox const & b1, SphericalBox const & b2) {
  return !(b1.equals(b2));
}

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_SPHERICAL_BOX_H_
