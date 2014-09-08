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



package ipac.stk.geom;

import ipac.stk.math.Vec3;


/**
 * Miscellaneous utility methods related to positions and
 * geometrical constructs on the unit sphere.
 */
public final class GeomUtils {

  private GeomUtils() { }

  /** Number of arc seconds per degree. */
  public static final double ARCSEC_PER_DEG = 3600.0;

  /** Number of arc seconds per arc minute. */
  public static final double ARCSEC_PER_ARCMIN = 60.0;

  /** Number of arc minutes per degree. */
  public static final double ARCMIN_PER_DEG = 60.0;

  /** Small constant (degrees) used to test for proximity to a pole. */
  private static final double POLE_EPSILON = 0.01;

  /**
   * Computes the extent in right ascension <em>[-&alpha;, &alpha;]</em> of
   * the circle with the given radius and center <em>(0, &phi;<sub>c</sub>)</em>
   * on the unit sphere.
   *
   * @param radius    Radius of the input circle (degrees).
   * @param centerPhi Latitude angle of the circle center (degrees).
   *
   * @return     Maximum longitude angle of any point on the input circle.
   */
  public static double maxAlpha(double radius, double centerPhi) {
    assert radius >= 0.0 && radius <= 180.0;
    assert centerPhi >= -90.0 && centerPhi <= 90.0;

    if (radius == 0.0) {
      return 0.0;
    }
    if (Math.abs(centerPhi) + radius > 90.0 - POLE_EPSILON) {
      return 180.0;
    }
    double y  = Math.sin(Math.toRadians(radius));
    double c1 = Math.cos(Math.toRadians(centerPhi - radius));
    double c2 = Math.cos(Math.toRadians(centerPhi + radius));
    double x  = Math.sqrt(Math.abs(c1 * c2));
    return Math.toDegrees(Math.abs(Math.atan(y / x)));
  }

  /**
   * Computes the maximum number of equal-width chunks that can fit into the
   * given latitude angle stripe, where each cell has the given minimum width.
   * The minimum width of a chunk is defined as the minimum allowable angular
   * separation between two points in non-adjacent cells belonging to the same
   * stripe.
   *
   * @param phiMin       Minimum latitude angle of the stripe (degrees).
   * @param phiMax       Maximum latitude angle of the stripe (degrees).
   * @param minWidth     Minimum width of a cell (degrees).
   */
  public static int chunksPerStripe(double phiMin, double phiMax,
                                    double minWidth) {
    final double minMinWidth = 1.6764e-07; // > 360.0/2147483647
    assert phiMin >= -90.0 && phiMin <= 90.0;
    assert phiMax >= -90.0 && phiMax <= 90.0;
    assert phiMin < phiMax;
    assert minWidth > 0.0;
    double p1 = Math.abs(phiMin);
    double p2 = Math.abs(phiMax);
    double maxAbsPhi = Math.max(p1, p2);
    if (maxAbsPhi > 90.0 - POLE_EPSILON) {
      return 1;
    }
    if (minWidth >= 180.0) {
      return 1;
    } else if (minWidth < minMinWidth) {
      // do not generate more than 2^31 - 1 chunks
      minWidth = minMinWidth;
    }
    double cosWidth = Math.cos(Math.toRadians(minWidth));
    double sinPhi   = Math.sin(Math.toRadians(maxAbsPhi));
    double cosPhi   = Math.cos(Math.toRadians(maxAbsPhi));
    cosWidth = (cosWidth - sinPhi * sinPhi) / (cosPhi * cosPhi);
    if (cosWidth < 0) {
      return 1;
    }
    return (int) Math.floor(2.0 * Math.PI / Math.acos(cosWidth));
  }

  /**
   * Returns the minimum angular separation in degrees between <code>c</code>
   * and the edge <code>[v1, v2]</code> lying on the plane with normal
   * <code>n</code>.
   */
  public static double minAngularSeparation(Vec3 c, Vec3 n,
                                             Vec3 v1, Vec3 v2) {
    Vec3 p1 = Vec3.crossProduct(n, v1);
    Vec3 p2 = Vec3.crossProduct(n, v2);
    if (p1.dot(c) >= 0 && p2.dot(c) <= 0) {
      return Math.abs(90.0 - Vec3.angularSeparation(c, n));
    } else {
      return Math.min(Vec3.angularSeparation(c, v1),
                      Vec3.angularSeparation(c, v2));
    }
  }

  /**
   * Computes the quotient of <code>dividend/divisor</code> using floored
   * division (rounding down) rather than truncated division (rounding
   * towards zero).
   *
   * @param dividend  Numerator.
   * @param divisor   Denominator; must be positive.
   * @throws  java.lang.IllegalArgumentException
   *    If <code>x</code> &le; 0
   */
  public static int floorDiv(int dividend, int divisor) {
    if (divisor <= 0) {
      throw new IllegalArgumentException("Non-positive divisor");
    }
    if (dividend >= 0) {
      return dividend / divisor;
    } else {
      return (dividend - divisor + 1) / divisor;
    }
  }

  /**
   * Computes the remainder of <code>dividend/divisor</code> using floored
   * division (rounding down) rather than truncated division (rounding
   * towards zero).
   *
   * @param dividend  Numerator.
   * @param divisor   Denominator; must be positive.
   * @throws  java.lang.IllegalArgumentException
   *    If <code>x</code> &le; 0
   */
  public static int floorRem(int dividend, int divisor) {
    int quotient = floorDiv(dividend, divisor);
    return dividend - quotient * divisor;
  }

  /**
   * Returns the floor of the base-2 logarithm of <code>x</code>.
   *
   * @throws  java.lang.IllegalArgumentException
   *    If <code>x</code> &le; 0
   */
  public static int floorLog2(int x) {
    return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(x);
  }
}

