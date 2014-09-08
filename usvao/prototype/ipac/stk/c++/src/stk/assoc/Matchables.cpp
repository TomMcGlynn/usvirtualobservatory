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
#include "ipac/stk/config.h"
#if HAVE_INTEL_IPP
# include "ippvm.h"
#endif

#include <algorithm>
#include <cmath>

#include "Eigen/Geometry"

#include "ipac/stk/assoc/constants.h"


namespace ipac { namespace stk { namespace assoc {

using std::abs;
using std::asin;
using std::atan2;
using std::cos;
using std::min;
using std::sin;
using std::sqrt;
using std::string;
using std::vector;

using Eigen::Vector3d;
using Eigen::Vector4d;

using ipac::stk::json::Value;
using ipac::stk::table::Record;

namespace except = ipac::stk::except;

namespace {

/** \internal
    Computes the sine and cosine of the angles in \c theta.
  */
void computeSinCos(double const * __restrict theta,
                   double * __restrict sinTheta,
                   double * __restrict cosTheta,
                   int n)
{
#if HAVE_INTEL_IPP
  ::ippsSinCos_64f_A53(theta, sinTheta, cosTheta, n);
#else
  for (int i = 0; i < n; ++i) {
    sinTheta[i] = sin(theta[i]);
    cosTheta[i] = cos(theta[i]);
  }
#endif
}

/** \internal
    Transforms the array of spherical coordinates \code (theta, phi) \endcode
    to cartesian unit vectors \code (x, y, z) \endcode.
  */
void computeVec(double const * __restrict theta,
                double const * __restrict phi,
                double * __restrict x,
                double * __restrict y,
                double * __restrict z,
                double * __restrict tmp,
                int n)
{
  computeSinCos(theta, y, x, n);
  computeSinCos(phi, z, tmp, n);
#if HAVE_INTEL_IPP
  ::ippsMul_64f_I(tmp, x, n);
  ::ippsMul_64f_I(tmp, y, n);
#else
  for (int i = 0; i < n; ++i) {
    x[i] *= tmp[i];
    y[i] *= tmp[i];
  }
#endif
}

/** \internal
    Computes \code 2.0 * sin(0.5 * radius) \endcode for each element in
    the given array of radii.
  */
void computeLimit(double const * __restrict radius,
                  double * __restrict limit,
                  double * __restrict tmp,
                  int n)
{
#if HAVE_INTEL_IPP
  ::ippsMulC_64f(radius, 0.5, limit, n);
  ::ippsSin_64f_A53(limit, tmp, n);
  ::ippsMulC_64f(tmp, 2.0, limit, n);
#else
  for (int i = 0; i < n; ++i) {
    limit[i] = 0.5 * radius[i];
  }
  for (int i = 0; i < n; ++i) {
    tmp[i] = sin(limit[i]);
  }
  for (int i = 0; i < n; ++i) {
    limit[i] = 2.0 * tmp[i];
  }
#endif
}

/** \internal
    Computes the longitude angle extent \c alpha (deg) of the circles with
    centers having the given latitude angles (\c phi) and radii (\c radius).
  */
void computeAlpha(double const * __restrict phi,
                  double * __restrict radius,
                  double * __restrict alpha,
                  double * __restrict tmp1,
                  double * __restrict tmp2,
                  double * __restrict tmp3,
                  int n)
{
  double const POLAR_CAP = PI * 0.5 - 0.0001;
#if HAVE_INTEL_IPP
  ::ippsAdd_64f(phi, radius, tmp1, n);
  ::ippsSub_64f(phi, radius, tmp2, n);
  ::ippsCos_64f_A53(tmp1, alpha, n);
  ::ippsCos_64f_A53(tmp2, tmp3, n);
  ::ippsMul_64f(alpha, tmp3, tmp1, n);
  ::ippsAbs_64f(tmp1, tmp2, n);
  ::ippsSqrt_64f_A53(tmp2, tmp1, n);
  ::ippsSin_64f_A53(radius, tmp2, n);
  ::ippsAtan2_64f_A53(tmp2, tmp1, alpha, n);
  ::ippsMulC_64f_I(DEG_PER_RAD, alpha, n);
#else
  for (int i = 0; i < n; ++i) {
    tmp1[i] = phi[i] + radius[i];
    tmp2[i] = phi[i] - radius[i];
  }
  for (int i = 0; i < n; ++i) {
    alpha[i] = cos(tmp1[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp3[i] = cos(tmp2[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp2[i] = abs(alpha[i] * tmp3[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp1[i] = sqrt(tmp2[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp2[i] = sin(radius[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp3[i] = atan2(tmp2[i], tmp1[i]);
  }
  for (int i = 0; i < n; ++i) {
    alpha[i] = DEG_PER_RAD * tmp3[i];
  }
#endif
  for (int i = 0; i < n; ++i) {
    if (radius[i] == 0.0) {
      alpha[i] = 0.0;
    }
    if (abs(phi[i]) + radius[i] > POLAR_CAP) {
      alpha[i] = 180.0;
    }
  }
}

/** \internal
    Computes the square inverse of \c x.
  */
void computeSquareInv(double const * __restrict x,
                      double * __restrict squareInverse,
                      double * __restrict tmp,
                      int n)
{
#if HAVE_INTEL_IPP
  ::ippsInv_64f_A53(x, tmp, n);
  ::ippsMul_64f(tmp, tmp, squareInverse, n);
#else
  for (int i = 0; i < n; ++i) {
    tmp[i] = 1.0 / x[i];
  }
  for (int i = 0; i < n; ++i) {
    squareInverse[i] = tmp[i] * tmp[i];
  }
#endif
}

/** \internal
    Computes bounding circle radii for ellipses with semi-major
    axis lengths \c smaa.
  */
void computeBoundingCircleRadius(double const * __restrict smaa,
                                 double * __restrict radius,
                                 int n)
{
#if HAVE_INTEL_IPP
  ::ippsAsin_64f_A53(smaa, radius, n); 
#else
  for (int i = 0; i < n; ++i) {
    radius[i] = asin(smaa[i]);
  }
#endif
}

/** \internal
    Computes the position angle (rad) between the two unit 3-vectors
    given in the first 3 components of \c v1 and \c v2.
  */
double positionAngle(Vector4d const & v1, Vector4d const & v2) {
  Vector3d north(-v1.x() * v1.z(),
                 -v1.y() * v1.z(),
                 v1.x() * v1.x() + v1.y() * v1.y());
  Vector3d east = north.cross(v1.start<3>());
  Vector3d delta = (v2 - v1).start<3>();
  double s = east.dot(delta);
  double c = north.dot(delta);
  if (s == 0.0 && c == 0.0) {
    return 0.0;
  }
  return atan2(s, c);
}

} // namespace


// -- MatchableCircle -------

MatchableCircle::MatchableCircle(Record const & data) : Matchable(data) { }

MatchableCircle::~MatchableCircle() { }

bool MatchableCircle::matches(Matchable::Match & candidate) const {
  Vector4d d = _center - candidate.entry.v;
  Vector4d d2 = d.cwise() * d;
  double x = d2.sum() - d2.w();
  if (x <= d2.w()) {
    candidate.distance = 2.0 * asin(0.5 * sqrt(x));
    candidate.positionAngle = positionAngle(_center, candidate.entry.v);
    return true;
  }
  return false;
}


// -- MatchableEllipse -------

MatchableEllipse::MatchableEllipse(Record const & data) : Matchable(data) { }

MatchableEllipse::~MatchableEllipse() { }

bool MatchableEllipse::matches(Matchable::Match & candidate) const {
  Vector4d v = candidate.entry.v;
  // get coords of input point in (N,E) basis
  double xne = _cosPhi * v.z() -
               _sinPhi * (_sinTheta * v.y() + _cosTheta * v.x());
  double yne = _cosTheta * v.y() - _sinTheta * v.x();
  // rotate by negated position angle
  double xr  = _sinAng * yne + _cosAng * xne;
  double yr  = _cosAng * yne - _sinAng * xne;
  // now in position to use standard 2D axis-aligned formulation for an ellipse
  if (xr * xr * _invMajor2 + yr * yr * _invMinor2 <= 1.0) {
    Vector4d center(_cosTheta * _cosPhi, _sinTheta * _cosPhi, _sinPhi, 0.0);
    candidate.distance = 2.0 * asin(0.5 * (center - v).norm());
    candidate.positionAngle = positionAngle(center, v);
    return true;
  }
  return false;
}


// -- CircleBuilder -------

CircleBuilder::CircleBuilder(
  boost::shared_ptr<Parameter> const & theta,
  boost::shared_ptr<Parameter> const & phi,
  boost::shared_ptr<Parameter> const & radius,
  size_t numWarnings
) :
  _theta(theta),
  _phi(phi),
  _radius(radius),
  _warnings(Value::vector()),
  _numWarnings(numWarnings)
{ }

CircleBuilder::~CircleBuilder() { }

void CircleBuilder::build(vector<Record> const & rows,
                          double const maxRadius,
                          std::vector<Record> * badRows,
                          vector<MatchableCircle> & circles)
{
  double const EPSILON = 1.0e-6 * RAD_PER_ARCSEC;
  size_t const BLOCK_SIZE = 256;
  circles.clear();
  circles.reserve(rows.size());
  if (badRows) {
    badRows->clear();
  }
#if HAVE_INTEL_IPP
  // Use the IPP (32 byte) aligned memory allocation function
  boost::shared_ptr<double> tmp(::ippsMalloc_64f(BLOCK_SIZE * 8), ::ippsFree);
  if (!tmp) {
    throw std::bad_alloc("ippsMalloc() failed");
  }
#else
  boost::scoped_array<double> tmp(new double[BLOCK_SIZE * 8]);
#endif
  double * theta = tmp.get() + 0 * BLOCK_SIZE;
  double * phi = tmp.get() + 1 * BLOCK_SIZE;
  double * radius = tmp.get() + 2 * BLOCK_SIZE;
  for (size_t i = 0; i < rows.size(); i += BLOCK_SIZE) {
    size_t const n = min(BLOCK_SIZE, rows.size() - i);
    size_t base = circles.size();
    size_t ngood = 0;
    // Construct circles (with bad row tracking)
    for (size_t j = 0; j < n; ++j) {
      try {
        theta[ngood] = _theta->getValue(rows[i + j]);
        phi[ngood] = _phi->getValue(rows[i + j]);
        if (phi[ngood] < -0.5 * PI || phi[ngood] > 0.5 * PI) {
          if (phi[ngood] >= -0.5 * PI - EPSILON && phi[ngood] < -0.5 * PI) {
            phi[ngood] = -0.5 * PI;
          } else if (phi[ngood] > 0.5 * PI &&
                     phi[ngood] <= 0.5 * PI + EPSILON) {
            phi[ngood] = 0.5 * PI;
          } else {
            throw IPAC_STK_EXCEPT(except::Format, except::message(
              "%slatitude angle must be in range [-90, 90]; got %.17g deg",
              _phi->getErrorPrefix(rows[i + j]).c_str(),
              phi[ngood] * DEG_PER_RAD));
          }
        }
        radius[ngood] = _radius->getValue(rows[i + j]);
        if (radius[ngood] < 0.0 || radius[ngood] > maxRadius) {
          throw IPAC_STK_EXCEPT(except::Format, except::message(
            "%ssearch radius (%.17g arcsec) is negative or exceeds the "
            "allowed maximum (%.17g arcsec).",
            _radius->getErrorPrefix(rows[i + j]).c_str(),
            radius[ngood] * ARCSEC_PER_RAD, maxRadius * ARCSEC_PER_RAD));
        }
        circles.push_back(rows[i + j]);
        ++ngood;
      } catch (except::Format & fmt) {
        if (badRows) {
          badRows->push_back(rows[i + j]);
          if (_warnings.size() < _numWarnings) {
            _warnings.add(fmt.getMessage());
          }
        } else {
          throw;
        }
      }
    }
    double * const __restrict x = tmp.get() + 3 * BLOCK_SIZE;
    double * const __restrict y = tmp.get() + 4 * BLOCK_SIZE;
    double * const __restrict z = tmp.get() + 5 * BLOCK_SIZE;
    double * const __restrict limit = tmp.get() + 6 * BLOCK_SIZE;
    double * const __restrict alpha = tmp.get() + 7 * BLOCK_SIZE;
    computeVec(theta, phi, x, y, z, alpha, static_cast<int>(ngood));
    computeLimit(radius, limit, alpha, static_cast<int>(ngood));
    // Store center vector and distance limit
    for (size_t j = 0; j < ngood; ++j) {
      circles[base + j]._center = Vector4d(x[j], y[j], z[j], limit[j]);
    }
    // Compute circle bounding boxes
    computeAlpha(phi, radius, alpha, x, y, z, static_cast<int>(ngood));
    for (size_t j = 0; j < ngood; ++j) {
      circles[base + j].getEnvelope().setCenterAndExtents(
        theta[j] * DEG_PER_RAD,
        phi[j] * DEG_PER_RAD,
        alpha[j],
        radius[j] * DEG_PER_RAD
      );
    }
  }
  if (badRows) {
    if (badRows->size() > _numWarnings) {
      size_t n = badRows->size() - _numWarnings;
      _warnings.add(except::message(
        "... (%llu more)", static_cast<unsigned long long>(n)));
    }
  }
}


// -- EllipseBuilder -------

EllipseBuilder::EllipseBuilder(
  boost::shared_ptr<Parameter> const & theta,
  boost::shared_ptr<Parameter> const & phi,
  boost::shared_ptr<Parameter> const & axisAngle,
  boost::shared_ptr<Parameter> const & majorAxis,
  boost::shared_ptr<Parameter> const & minorAxis,
  boost::shared_ptr<Parameter> const & axisRatio,
  size_t numWarnings
) :
  _theta(theta),
  _phi(phi),
  _axisAngle(axisAngle),
  _majorAxis(majorAxis),
  _minorAxis(minorAxis),
  _axisRatio(axisRatio),
  _warnings(Value::vector()),
  _numWarnings(numWarnings)
{ }

EllipseBuilder::~EllipseBuilder() { }

void EllipseBuilder::build(vector<Record> const & rows,
                           double const maxRadius,
                           std::vector<Record> * badRows,
                           vector<MatchableEllipse> & ellipses)
{
  double const EPSILON = 1.0e-6 * RAD_PER_ARCSEC;
  size_t const BLOCK_SIZE = 256;
  ellipses.clear();
  ellipses.reserve(rows.size());
  if (badRows) {
    badRows->clear();
  }
#if HAVE_INTEL_IPP
  // use the IPP (32 byte) aligned memory allocation function
  boost::shared_ptr<double> tmp(::ippsMalloc_64f(BLOCK_SIZE * 8), ::ippsFree);
  if (!tmp) {
    throw std::bad_alloc("ippsMalloc() failed");
  }
#else
  boost::scoped_array<double> tmp(new double[BLOCK_SIZE * 8]);
#endif
  double * const __restrict theta = tmp.get() + 0 * BLOCK_SIZE;
  double * const __restrict phi = tmp.get() + 1 * BLOCK_SIZE;
  double * const __restrict smaa = tmp.get() + 2 * BLOCK_SIZE;
  double * const __restrict smia = tmp.get() + 3 * BLOCK_SIZE;
  double * const __restrict ang = tmp.get() + 4 * BLOCK_SIZE;
  for (size_t i = 0; i < rows.size(); i += BLOCK_SIZE) {
    size_t const n = min(BLOCK_SIZE, rows.size() - i);
    size_t base = ellipses.size();
    size_t ngood = 0;
    // construct ellipses (with bad row tracking)
    for (size_t j = 0; j < n; ++j) {
      try {
        theta[ngood] = _theta->getValue(rows[i + j]);
        phi[ngood] = _phi->getValue(rows[i + j]);
        if (phi[ngood] < -0.5 * PI || phi[ngood] > 0.5 * PI) {
          if (phi[ngood] >= -0.5 * PI - EPSILON && phi[ngood] < -0.5 * PI) {
            phi[ngood] = -0.5 * PI;
          } else if (phi[ngood] > 0.5 * PI &&
                     phi[ngood] <= 0.5 * PI + EPSILON) {
            phi[ngood] = 0.5 * PI;
          } else {
            throw IPAC_STK_EXCEPT(except::Format, except::message(
              "%slatitude angle must be in range [-90, 90]; got %.17g deg",
              _phi->getErrorPrefix(rows[i + j]).c_str(),
              phi[ngood] * DEG_PER_RAD));
          }
        }
        ang[ngood] = _axisAngle->getValue(rows[i + j]);
        if (_majorAxis->isAvailable()) {
          smaa[ngood] = _majorAxis->getValue(rows[i + j]);
          if (_minorAxis->isAvailable()) {
            smia[ngood] = _minorAxis->getValue(rows[i + j]);
          } else {
            double ratio = _axisRatio->getValue(rows[i + j]);
            if (ratio <= 0.0 || ratio > 1.0) {
              throw IPAC_STK_EXCEPT(except::Format, except::message(
                "%sminor to major axis length ratio (%.17g) is negative, "
                "zero, or greater than 1",
                _axisRatio->getErrorPrefix(rows[i + j]).c_str(), ratio));
            }
            smia[ngood] = smaa[ngood] * ratio;
          }
        } else {
          smia[ngood] = _minorAxis->getValue(rows[i + j]);
          double ratio = _axisRatio->getValue(rows[i + j]);
          if (ratio <= 0.0 || ratio > 1.0) {
            throw IPAC_STK_EXCEPT(except::Format, except::message(
              "%sminor to major axis length ratio (%.17g) is negative, "
              "zero, or greater than 1",
              _axisRatio->getErrorPrefix(rows[i + j]).c_str(), ratio));
          }
          smaa[ngood] = smia[ngood] / ratio;
        }
        if (smaa[ngood] < smia[ngood]) {
          throw IPAC_STK_EXCEPT(except::Format, except::message(
            "%ssemi-major axis length (%.17g arcsec) exceeds semi-minor "
            "axis length (%.17g arcsec)",
            _majorAxis->getErrorPrefix(rows[i + j]).c_str(),
            smaa[ngood] * ARCSEC_PER_RAD, smia[ngood] * ARCSEC_PER_RAD));
        }
        if (smia[ngood] <= 0.0) {
          throw IPAC_STK_EXCEPT(except::Format, except::message(
            "%sinvalid semi-minor axis length (%g arcsec). Length is "
            "negative or 0.", _minorAxis->getErrorPrefix(rows[i + j]).c_str(),
            smia[ngood] * ARCSEC_PER_RAD));
        }
        if (smaa[ngood] <= 0.0 || smaa[ngood] > maxRadius) {
          throw IPAC_STK_EXCEPT(except::Format, except::message(
            "%sinvalid semi-major axis length (%.17g arcsec). Length is "
            "negative, 0, or exceeds the allowed maximum (%.17g arcsec).",
            _majorAxis->getErrorPrefix(rows[i + j]).c_str(),
            smaa[ngood] * ARCSEC_PER_RAD, maxRadius * ARCSEC_PER_RAD));
        }
        ellipses.push_back(rows[i + j]);
        ++ngood;
      } catch (except::Format & fmt) {
        if (badRows != 0) {
          badRows->push_back(rows[i + j]);
          if (_warnings.size() < _numWarnings) {
            _warnings.add(fmt.getMessage());
          }
        } else {
          throw;
        }
      }
    }
    double * const __restrict tmp1 = tmp.get() + 5 * BLOCK_SIZE;
    double * const __restrict tmp2 = tmp.get() + 6 * BLOCK_SIZE;
    double * const __restrict tmp3 = tmp.get() + 7 * BLOCK_SIZE;
    // compute square inverse of semi axis lengths and
    // sin/cos of major axis angle
    computeSquareInv(smia, tmp1, tmp3, static_cast<int>(ngood));
    computeSquareInv(smaa, tmp2, tmp3, static_cast<int>(ngood));
    double * const __restrict tmp4 = smia; // re-use smia as a temporary array
    computeSinCos(ang, tmp3, tmp4, static_cast<int>(ngood));
    for (size_t j = 0; j < ngood; ++j) {
      ellipses[base + j]._sinAng = tmp3[j];
      ellipses[base + j]._cosAng = tmp4[j];
      ellipses[base + j]._invMinor2 = tmp1[j];
      ellipses[base + j]._invMajor2 = tmp2[j];
    }
    // compute sin/cos of center coordinates
    computeSinCos(phi, tmp1, tmp2, static_cast<int>(ngood));
    computeSinCos(theta, tmp3, tmp4, static_cast<int>(ngood));
    for (size_t j = 0; j < ngood; ++j) {
      ellipses[base + j]._sinPhi = tmp1[j];
      ellipses[base + j]._cosPhi = tmp2[j];
      ellipses[base + j]._sinTheta = tmp3[j];
      ellipses[base + j]._cosTheta = tmp4[j];
    }
    // compute ellipse bounding boxes (simplified: compute bounds
    // for a circle with radius smaa).
    double * const __restrict alpha = smaa;
    double * const __restrict radius = smia;
    computeBoundingCircleRadius(smaa, radius, static_cast<int>(ngood));
    computeAlpha(phi, radius, alpha, tmp1, tmp2, tmp3, static_cast<int>(ngood));
    for (size_t j = 0; j < ngood; ++j) {
      ellipses[base + j].getEnvelope().setCenterAndExtents(
        theta[j] * DEG_PER_RAD,
        phi[j] * DEG_PER_RAD,
        alpha[j],
        radius[j] * DEG_PER_RAD
      );
    }
  }
  if (badRows) {
    if (badRows->size() > _numWarnings) {
      size_t n = badRows->size() - _numWarnings;
      _warnings.add(except::message(
        "... (%llu more)", static_cast<unsigned long long>(n)));
    }
  }
}

}}} // namespace ipac::stk::assoc
