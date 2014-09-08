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



#include "ipac/stk/assoc/SphericalBox.h"

#include "ipac/stk/except.h"


namespace ipac { namespace stk { namespace assoc {

using std::min;
using std::max;

namespace except = ipac::stk::except;

/** Creates an empty spherical box.
  */
SphericalBox::SphericalBox() {
  setEmpty();
}

/** Creates a degenerate spherical box containing
    a single point \code (theta, phi) \endcode.
  */
SphericalBox::SphericalBox(double theta, double phi) :
  _minPhi(phi),
  _begTheta(theta),
  _maxPhi(phi),
  _endTheta(theta)
{
  if (phi < -90.0 || phi > 90.0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "latitude angle not in range [-90.0, 90.0]");
  }
  if (theta < 0.0 || theta >= 360.0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "longitude angle not in range [0, 360)");
  }
}

/** Creates a spherical box with extremities \code (begTheta, minPhi) \endcode
    and \code (endTheta, maxPhi) \endcode. Note that \c begTheta can be greater
    than \c endTheta; this indicates that the box wraps around the 0/360
    degree longitude angle discontinuity.
  */
SphericalBox::SphericalBox(double minPhi, double begTheta, 
                           double maxPhi, double endTheta) :
  _minPhi(minPhi),
  _begTheta(begTheta),
  _maxPhi(maxPhi),
  _endTheta(endTheta)
{
  if (maxPhi < minPhi) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "maximum latitude angle "
                          "must be greater than or equal to minimum latitude "
                          "angle");
  }
  if (minPhi < -90.0 || maxPhi > 90.0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "latitude angle out of bounds");
  }
  if (begTheta < 0.0 || begTheta >= 360.0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "beginning longitude "
                          "angle not in range [0, 360)");
  }
  if (endTheta < 0.0 || endTheta > 360.0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "ending longitude angle not in range [0, 360]");
  }
}

/** Computes the smallest box S containing the union of this box B and \c b,
    then copies S to B.
  */
SphericalBox & SphericalBox::extend(SphericalBox const & b) {
  if (b.isEmpty()) {
    return *this;
  } else if (isEmpty()) {
    *this = b;
    return *this;
  }
  _minPhi = min(_minPhi, b._minPhi);
  _maxPhi = max(_maxPhi, b._maxPhi);
  if (_begTheta > _endTheta) {
    // this box wraps
    if (b._begTheta > b._endTheta) {
      // b wraps
      double minBeg = min(_begTheta, b._begTheta);
      double maxEnd = max(_endTheta, b._endTheta);
      if (maxEnd >= minBeg) {
        _begTheta = 0.0;
        _endTheta = 360.0;
      } else {
        _begTheta = minBeg;
        _endTheta = maxEnd;
      }
    } else {
      // b does not wrap
      if (b._begTheta <= _endTheta && b._endTheta >= _begTheta) {
        _begTheta = 0.0;
        _endTheta = 360.0;
      } else if (b._begTheta - _endTheta > _begTheta - b._endTheta) {
        _begTheta = b._begTheta;
      } else {
        _endTheta = b._endTheta;
      }
    }
  } else {
    // this box does not wrap
    if (b._begTheta > b._endTheta) {
      // b wraps
      if (_begTheta <= b._endTheta && _endTheta >= b._begTheta) {
        _begTheta = 0.0;
        _endTheta = 360.0;
      } else if (_begTheta - b._endTheta > b._begTheta - _endTheta) {
        _endTheta = b._endTheta;
      } else {
        _begTheta = b._begTheta;
      }
    } else {
      // b does not wrap
      if (b._begTheta > _endTheta) {
        if (360.0 - b._begTheta + _endTheta < b._endTheta - _begTheta) {
          _begTheta = b._begTheta;
        } else {
          _endTheta = b._endTheta;
        }
      } else if (_begTheta > b._endTheta) {
        if (360.0 - _begTheta + b._endTheta < _endTheta - b._begTheta) {
          _endTheta = b._endTheta;
        } else {
          _begTheta = b._begTheta;
        }
      } else {
        _begTheta = min(_begTheta, b._begTheta);
        _endTheta = max(_endTheta, b._endTheta);
      }
    }
  }
  return *this;
}

/** Computes the smallest box S containing this box B and the given point,
    then copies S to B.
  */
SphericalBox & SphericalBox::extend(double theta, double phi) {
  if (contains(theta, phi)) {
    return *this;
  } else if (isEmpty()) {
    *this = SphericalBox(theta, phi);
    return *this;
  }
  _minPhi = min(_minPhi, phi);
  _maxPhi = max(_maxPhi, phi);
  if (_begTheta > _endTheta) {
    if (_begTheta - theta > theta - _endTheta) {
      _endTheta = theta;
    } else {
      _begTheta = theta;
    }
  } else if (theta < _begTheta) {
    if (_begTheta - theta <= 360.0 - _endTheta + theta) {
      _begTheta = theta;
    } else {
      _endTheta = theta;
    }
  } else {
    if (theta - _endTheta <= 360.0 - theta + _begTheta) {
      _endTheta = theta;
    } else {
      _begTheta = theta;
    }
  }
  return *this;
}

/** Computes the smallest box S containing the intersection of this box B
    and \c b, then copies S to B.
  */
SphericalBox & SphericalBox::shrink(SphericalBox const & b) {
  _minPhi = max(_minPhi, b._minPhi);
  _maxPhi = min(_maxPhi, b._maxPhi);
  if (_begTheta > _endTheta) {
    if (b._begTheta > b._endTheta) {
      _begTheta = max(_begTheta, b._begTheta);
      _endTheta = min(_endTheta, b._endTheta);
    } else {
      if (b._endTheta >= _begTheta) {
        if (b._begTheta <= _endTheta &&
            b._endTheta - b._begTheta <= 360.0 - _endTheta + _begTheta) {
          *this = b;
        } else {
          _endTheta = b._endTheta;
        }
      } else if (b._begTheta <= _endTheta) {
        _begTheta = b._begTheta;
      } else {
        setEmpty();
      }
    }
  } else {
    if (b._begTheta > b._endTheta) {
      if (_endTheta >= b._begTheta) {
        if (_begTheta <= b._endTheta &&
            _endTheta - _begTheta > 360.0 - b._endTheta + b._begTheta) {
          *this = b;
        } else {
          _begTheta = b._begTheta;
        }
      } else if (_begTheta <= b._endTheta) {
        _endTheta = b._endTheta;
      } else {
        setEmpty();
      }
    } else {
      _begTheta = min(_begTheta, b._begTheta);
      _endTheta = max(_endTheta, b._endTheta);
    }
  }
  return *this;
}

/** Returns \c true if \c box is inside this bounding box.
  */
bool SphericalBox::contains(SphericalBox const & b) const {
  if (isEmpty() || b.isEmpty()) {
    return false;
  }
  if (b._minPhi < _minPhi || b._maxPhi > _maxPhi) {
    return false;
  }
  if (_begTheta > _endTheta) {
    if (b._begTheta > b._endTheta) {
      return b._begTheta >= _begTheta && b._endTheta <= _endTheta;
    } else {
      return b._begTheta >= _endTheta || b._endTheta <= _begTheta;
    }
  } else {
    if (b._begTheta > b._endTheta) {
      return _begTheta == 0.0 && _endTheta == 360.0;
    } else {
      return b._begTheta >= _begTheta && b._endTheta <= _endTheta;
    }
  }
}

/** Returns \c true if \c box intersects this spherical box.
  */
bool SphericalBox::intersects(SphericalBox const & b) const {
  if (isEmpty() || b.isEmpty()) {
    return false;
  }
  if (b._minPhi > _maxPhi || b._maxPhi < _minPhi) {
    return false;
  }
  if (_begTheta > _endTheta) {
    if (b._begTheta > b._endTheta) {
      return true;
    } else {
      return b._begTheta <= _endTheta || b._endTheta >= _begTheta;
    }
  } else {
    if (b._begTheta > b._endTheta) {
      return _begTheta <= b._endTheta || _endTheta >= b._begTheta;
    } else {
      return _begTheta <= b._endTheta && _endTheta >= b._begTheta;
    }
  }
}

}}} // namespace ipac::stk::assoc