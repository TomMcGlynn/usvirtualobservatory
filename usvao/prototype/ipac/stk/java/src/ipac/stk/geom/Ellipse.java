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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ipac.stk.math.SphericalCoords;
import ipac.stk.math.Vec3;

/**
 * An elliptical {@link Region}, defined as the intersection of a cone with
 * elliptical support and the unit sphere.
 *
 * Determining whether or not two ellipses overlap is non-trivial, though an
 * approach is outlined here: Consider the intersection of the second cone
 * with the plane tangent to the unit sphere S at the point P corresponding
 * to the intersection of S and the central axis of the first cone. This
 * yields an ellipse A and some quadratic form B.
 *
 * A method for characterizing the relative positions (i.e. the morphology of
 * the intersection and rigid isotopy class) of two projective conics can then
 * be applied, see:
 *
 * Invariant-based characterization of the relative position of two
 * projective conics
 * Sylvain Petitjean
 * Non-Linear Computational Geometry,
 * I. Emiris, F. Sottile and T. Theobald, Editors, Springer, 2009, to appear
 * http://www.loria.fr/~petitjea/papers/imaconics.pdf
 *
 * This seems extremely tricky to get right (especially where rounding is
 * concerned) and test, and so is currently left unimplemented.
 */
public class Ellipse extends AbstractRegion {
  /** Maximum semi-major axis length (arcsec). */
  public static final double MAX_SEMI_MAJ_LEN =
    45.0 * GeomUtils.ARCSEC_PER_DEG;
  private static final double EPSILON = 0.001;

  /** Ellipse center. */
  private SphericalCoords center = new SphericalCoords();
  /** Sine of ellipse center latitude. */
  private double sinPhi = 0.0;
  /** Cosine of ellipse center latitude. */
  private double cosPhi = 1.0;
  /** Sine of ellipse center longitude. */
  private double sinTheta = 0.0;
  /** Cosine of ellipse center longitude. */
  private double cosTheta = 1.0;
  /** Ellipse major axis angle (E of N, deg). */
  private double majorAxisAngle = 0.0;
  /** Sine of ellipse major axis angle (E of N). */
  private double sinMajorAxisAngle = 0.0;
  /** Cosine of ellipse major axis angle (E of N). */
  private double cosMajorAxisAngle = 1.0;
  /** The semi-major axis length (arcsec) of the ellipse. */
  private double semiMajorAxisLength = 0.0;
  /** The semi-minor axis length (arcsec) of the ellipse. */
  private double semiMinorAxisLength = 0.0;

  public Ellipse() { }

  /**
   * Constructs a new Ellipse.
   *
   * @param theta     longitude of ellipse center (deg)
   * @param phi       latitude of ellipse center (deg)
   * @param angle     ellipse major axis angle (E of N, deg)
   * @param semiMaj   ellipse semi-major axis length (arcsec)
   * @param semiMin   ellipse semi-minor axis length (arcsec)
   */
  public Ellipse(double theta, double phi, double angle,
                 double semiMaj, double semiMin) {
    initialize(theta, phi, angle, semiMaj, semiMin);
  }

  /**
   * Constructs a new Ellipse.
   *
   * @param center    ellipse center
   * @param angle     ellipse major axis angle (E of N, deg)
   * @param semiMaj   ellipse semi-major axis length (arcsec)
   * @param semiMin   ellipse semi-minor axis length (arcsec)
   */
  public Ellipse(SphericalCoords center, double angle,
                 double semiMaj, double semiMin) {
    initialize(center.getTheta(), center.getPhi(), angle, semiMaj, semiMin);
  }

  public SphericalCoords getCenter() {
    return center;
  }

  public double getSemiMinorAxisLength() {
    return semiMinorAxisLength;
  }

  public double getSemiMajorAxisLength() {
    return semiMajorAxisLength;
  }

  public double getMajorAxisAngle() {
    return majorAxisAngle;
  }

  private void initialize(double theta, double phi, double angle,
                          double semiMaj, double semiMin) {
    if (theta < 0.0 || theta >= 360.0 || phi < -90.0 || phi > 90.0) {
      throw new IllegalArgumentException("coordinate values out of range");
    }
    if (semiMaj <= 0.0 || semiMaj > MAX_SEMI_MAJ_LEN ||
        semiMin <= 0.0 || semiMin > semiMaj) {
      throw new IllegalArgumentException("semi-axis lengths out of range");
    }

    center.set(theta, phi);
    this.majorAxisAngle = angle;
    this.semiMajorAxisLength = semiMaj;
    this.semiMinorAxisLength = semiMin;
    // Compute bounding box for ellipse; note that this is currently a
    // conservative computation: the bounding box is computed as for a circle
    // with radius equal to the semi-major axis length.
    semiMaj /= GeomUtils.ARCSEC_PER_DEG;
    double deltaTheta = GeomUtils.maxAlpha(semiMaj, phi);
    double startTheta, endTheta;
    if (deltaTheta >= 180.0 - EPSILON) {
      startTheta = 0.0;
      endTheta = 360.0;
    } else {
      startTheta = theta - deltaTheta;
      endTheta = theta + deltaTheta;
      if (startTheta < 0.0) {
        startTheta += 360.0;
      }
      if (endTheta > 360.0) {
        endTheta -= 360.0;
      }
    }
    setBoundingBox(new BoundingBox(
      startTheta,
      endTheta,
      SphericalCoords.clampPhi(phi - semiMaj),
      SphericalCoords.clampPhi(phi + semiMaj)
    ));

    theta = Math.toRadians(theta);
    phi = Math.toRadians(phi);
    angle = Math.toRadians(angle);
    this.sinPhi = Math.sin(phi);
    this.cosPhi = Math.cos(phi);
    this.sinTheta = Math.sin(theta);
    this.cosTheta = Math.cos(theta);
    this.sinMajorAxisAngle = Math.sin(angle);
    this.cosMajorAxisAngle = Math.cos(angle);
  }

  @Override public boolean contains(Vec3 pos) {
    // get coords of input point in (N,E) basis
    double xne = cosPhi * pos.getZ() -
                 sinPhi * (sinTheta * pos.getY() + cosTheta * pos.getX());
    double yne = cosTheta * pos.getY() - sinTheta * pos.getX();
    // rotate by negated major axis angle,
    // scale by inverse of semi-axis lengths
    double xr = (sinMajorAxisAngle * yne + cosMajorAxisAngle * xne) /
                Math.toRadians(semiMajorAxisLength / GeomUtils.ARCSEC_PER_DEG);
    double yr = (cosMajorAxisAngle * yne - sinMajorAxisAngle * xne) /
                Math.toRadians(semiMinorAxisLength / GeomUtils.ARCSEC_PER_DEG);
    // Apply standard formulation for the unit circle centered at the origin
    return (xr * xr + yr * yr <= 1.0);
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeDouble(center.getTheta());
    out.writeDouble(center.getPhi());
    out.writeDouble(majorAxisAngle);
    out.writeDouble(semiMajorAxisLength);
    out.writeDouble(semiMinorAxisLength);
  }

  @Override public void readFields(DataInput in) throws IOException {
    double theta = in.readDouble();
    double phi = in.readDouble();
    double angle = in.readDouble();
    double semiMaj = in.readDouble();
    double semiMin = in.readDouble();
    initialize(theta, phi, angle, semiMaj, semiMin);
  }
}
