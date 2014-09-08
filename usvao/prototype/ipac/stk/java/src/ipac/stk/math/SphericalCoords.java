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



package ipac.stk.math;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.io.Writable;


/**
 * A position on the unit sphere specified using spherical coordinates.
 */
public final class SphericalCoords implements Cloneable, Writable {

  /**
   * Constant (<code>1073741824.0/90.0</code>) used to scale a longitude
   * angle in degrees and produce an <code>int</code>.
   */
  private static final double THETA_INT_SCALE =
    1.19304647111111111111111111111e07;

  /**
   * Constant (<code>1073741824.0/90.0</code>) used to scale a latitude
   * angle in degrees and produce an <code>int</code>.
   */
  private static final double PHI_INT_SCALE = THETA_INT_SCALE;

  /** The inverse of PHI_INT_SCALE. */
  private static final double INV_PHI_INT_SCALE =
    8.38190317153930664062500000000e-08;

  /**
   * Constant (<code>2^54</code>) used to scale a longitude
   * angle in degrees and produce a <code>long</code>.
   */
  private static final double THETA_LONG_SCALE = 18014398509481984.0;

  /**
   * Constant (<code>2^54</code>) used to scale a latitude
   * angle in degrees and produce a <code>long</code>.
   */
  private static final double PHI_LONG_SCALE = THETA_LONG_SCALE;

  /** Longitude angle in degrees; in range [0.0, 360.0). */
  private double theta;
  /** Latitude angle in degrees; in range [-90.0, 90.0]. */
  private double phi;

  /** Creates a new position with both spherical coordinates set to zero. */
  public SphericalCoords() {
    theta = 0.0;
    phi = 0.0;
  }

  /** Creates a copy of <code>sc</code>. */
  public SphericalCoords(SphericalCoords sc) {
    set(sc);
  }

  /** Creates a new position with the given spherical coordinates. */
  public SphericalCoords(double theta, double phi) {
    set(theta, phi);
  }

  /**
   * Creates a new position with spherical coordinates corresponding
   * to the given euclidean coordinates.
   */
  public SphericalCoords(double x, double y, double z) {
    set(x, y, z);
  }

  /**
   * Creates a new position with spherical coordinates corresponding
   * to the given euclidean vector.
   */
  public SphericalCoords(Vec3 v) {
    set(v);
  }

  @Override public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public double getTheta() {
    return theta;
  }
  public double getPhi() {
    return phi;
  }
  public void setTheta(double thetaDeg) {
    theta = thetaDeg;
  }
  public void setPhi(double phiDeg) {
    phi = phiDeg;
  }

  /**
   * Converts the given euclidian vector to spherical coordinates and
   * stores the results in this <code>SphericalCoords</code>.
   */
  public void set(Vec3 v) {
    set(v.getX(), v.getY(), v.getZ());
  }

  /**
   * Converts the given euclidian vector to spherical coordinates and
   * stores the results in this <code>SphericalCoords</code>.
   */
  public void set(double x, double y, double z) {
    double d2 = x * x + y * y;
    if (d2 == 0.0) {
      theta = 0.0;
    } else {
      theta = Math.toDegrees(Math.atan2(y, x));
      if (theta < 0.0) {
        theta += 360.0;
      }
    }
    if (z == 0.0) {
      phi = 0.0;
    } else {
      phi = Math.toDegrees(Math.atan2(z, Math.sqrt(d2)));
    }
  }

  /** Copies the components of <code>sc</code>. */
  public void set(SphericalCoords sc) {
    set(sc.theta, sc.phi);
  }

  /**
   * Sets the components of this <code>SphericalCoords</code>
   * to the given spherical coordinates.
   */
  public void set(double angleTheta, double anglePhi) {
    theta = angleTheta;
    phi = anglePhi;
  }

  /**
   * Returns the angular separation in degrees between this position and
   * <code>sc</code>.
   */
  public double angularSeparation(SphericalCoords sc) {
    return angularSeparation(this, sc);
  }

  /**
    * Returns the angular separation in degrees between this position
    * and the given one.
    */
  public double angularSeparation(double angleTheta, double anglePhi) {
    return angularSeparation(theta, phi, angleTheta, anglePhi);
  }

  /**
   * Returns the angular separation in degrees between 2 positions
   * specified using spherical coordinates.
   */
  public static double angularSeparation(SphericalCoords sc1,
                                         SphericalCoords sc2) {
    return angularSeparation(sc1.theta, sc1.phi, sc2.theta, sc2.phi);
  }

  /**
   * Returns the angular separation in degrees between 2 positions
   * specified using spherical coordinates.
   */
  public static double angularSeparation(double theta1, double phi1,
                                         double theta2, double phi2) {
    // halversine distance formula
    double x = Math.sin(Math.toRadians(theta1 - theta2) * 0.5);
    double y = Math.sin(Math.toRadians(phi1 - phi2) * 0.5);
    double z = Math.cos(Math.toRadians(phi1 + phi2) * 0.5);
    x *= x;
    y *= y;
    z *= z;
    double a = Math.asin(Math.min(1.0, Math.sqrt(x * (z - y) + y)));
    return 2.0 * Math.toDegrees(a);
  }

  /**
   * Returns the position angle in degrees between this position and
   * <code>sc</code>.
   */
  public double positionAngle(SphericalCoords sc) {
    return positionAngle(this, sc);
  }

  /**
   * Returns the position angle in degrees between this position and the
   * given one.
   */
  public double positionAngle(double angleTheta, double anglePhi) {
    return positionAngle(theta, phi, angleTheta, anglePhi);
  }

  /**
   * Returns the position angle in degrees between a first and second
   * position specified using spherical coordinates.
   */
  public static double positionAngle(SphericalCoords sc1,
                                     SphericalCoords sc2) {
    return positionAngle(sc1.theta, sc1.phi, sc2.theta, sc2.phi);
  }

  /**
   * Returns the position angle in degrees between a first and second
   * position specified using spherical coordinates.
   */
  public static double positionAngle(double theta1, double phi1,
                                     double theta2, double phi2) {
    double dTheta = Math.toRadians(theta2 - theta1);
    phi1 = Math.toRadians(phi1);
    phi2 = Math.toRadians(phi2);
    double cphi2 = Math.cos(phi2);
    double y = Math.sin(dTheta) * cphi2;
    double x = Math.sin(phi2) * Math.cos(phi1) -
               cphi2 * Math.sin(phi1) * Math.cos(dTheta);
    if (x == 0.0 && y == 0.0) {
      return 0.0;
    }
    return Math.toDegrees(Math.atan2(y, x));
  }

  /** Returns the north vector tangential to this position. */
  public Vec3 northOf() {
    return northOf(theta, phi);
  }

  /** Returns the north vector tangential to the given position. */
  public static Vec3 northOf(double theta, double phi) {
    theta = Math.toRadians(theta);
    phi = Math.toRadians(phi);
    double sphi = Math.sin(phi);
    return new Vec3(-Math.cos(theta) * sphi,
                    -Math.sin(theta) * sphi,
                    Math.cos(phi));
  }

  /** Returns the east vector tangential to this position. */
  public Vec3 eastOf() {
    return eastOf(theta);
  }

  /**
   * Returns the east vector tangential to any position with the given
   * longitude angle.
   */
  public static Vec3 eastOf(double theta) {
    theta = Math.toRadians(theta);
    return new Vec3(-Math.sin(theta), Math.cos(theta), 0.0);
  }

  /** Clamps the given latitude angle to <code>[-90, 90]</code> degrees. */
  public static double clampPhi(double phi) {
    if (phi <= -90.0) {
      return -90.0;
    } else if (phi >= 90.0) {
      return 90.0;
    }
    return phi;
  }

  /**
   * Reduces the range of a longitude angle; return a value in the range
   * <code>[0, 360)</code> degrees.
   */
  public static double reduceTheta(double theta) {
    return theta - 360.0 * Math.floor(theta / 360.0);
  }

  /**
   * Converts a latitude angle in the range <code>[-90.0, 90.0]</code>
   * to an integer in the range <code>[-2^30, 2^30]</code>.
   */
  public static int phiToInt(double phi) {
    return (int) Math.floor(phi * PHI_INT_SCALE);
  }

  /**
   * Converts an integer in the range <code>[-2^30, 2^30]</code> to a
   * latitude angle in the range <code>[-90.0, 90.0]</code>.
   */
  public static double intToPhi(int phi) {
    return SphericalCoords.clampPhi(((double) phi) * INV_PHI_INT_SCALE);
  }

  /**
   * Converts a longitude angle in the range <code>[0.0, 360.0)</code>
   * to an integer in the range <code>[-2^31, 2^31)</code>.
   */
  public static int thetaToInt(double theta) {
    return (int) Math.floor((theta - 180.0) * THETA_INT_SCALE);
  }

  /** Converts a latitude angle delta to an integer. */
  public static int deltaPhiToInt(double phi) {
    return (int) Math.ceil(phi * PHI_INT_SCALE);
  }

  /** Converts a longitude angle delta to an integer. */
  public static int deltaThetaToInt(double theta) {
    return (int) ((long) Math.ceil(theta * THETA_INT_SCALE));
  }

  /**
   * Converts a latitude angle in the range <code>[-90.0, 90.0]</code>
   * to an integer in the range <code>[-2^50, 2^50]</code>.
   */
  public static long phiToLong(double phi) {
    return (long) Math.floor(phi * PHI_LONG_SCALE);
  }

  /**
   * Converts a longitude angle in the range <code>[0.0, 360.0)</code>
   * to an integer in the range <code>[0, 2^52)</code>.
   */
  public static long thetaToLong(double phi) {
    return (long) Math.floor(phi * THETA_LONG_SCALE);
  }

  /**
   * Returns a random number in the interval <code>[phiMin, phiMax]</code>.
   * Both latitude angles are clamped to lie in the range
   * <code>[-90, 90]</code> (deg). Note that the latitude angles may be
   * swapped (<code>phiMin &gt; phiMax</code>).
   */
  public static double randomPhi(Random generator, double phiMin,
                                 double phiMax) {
    double p1 = clampPhi(phiMin);
    double p2 = clampPhi(phiMax);
    double pMin = p1 <= p2 ? p1 : p2;
    double pMax = p1 <= p2 ? p2 : p1;
    double zMin = Math.sin(Math.toRadians(pMin));
    double zMax = Math.sin(Math.toRadians(pMax));
    double z = zMin + generator.nextDouble() * (zMax - zMin);
    double phi = Math.toDegrees(Math.asin(z));
    return phi < pMin ? pMin : (phi > pMax ? pMax : phi);
  }

  /**
   * Returns a new position picked approximately uniformly at random
   * from the unit sphere.
   */
  public static SphericalCoords random(Random generator) {
    double z = -1.0 + 2.0 * generator.nextDouble();
    double phi = Math.toDegrees(Math.asin(z));
    double theta = generator.nextDouble() * 360.0;
    return new SphericalCoords(theta, phi);
  }

  /**
   * Returns a new position picked approximately uniformly at
   * random from the given latitude angle range.
   */
  public static SphericalCoords random(Random generator, double phiMin,
                                       double phiMax) {
    return new SphericalCoords(generator.nextDouble() * 360.0,
                               randomPhi(generator, phiMin, phiMax));
  }

  /**
   * Returns a new position picked approximately uniformly at random from
   * the given spherical coordinate box. Note that if
   * <code>thetaMin &gt; thetaMax</code>, the box is assumed to wrap across
   * the 0/360 degree longitude boundary.
   */
  public static SphericalCoords random(Random generator,
                                       double thetaMin, double thetaMax,
                                       double phiMin, double phiMax) {
    thetaMin = reduceTheta(thetaMin);
    thetaMax = reduceTheta(thetaMax);

    double theta;
    if (thetaMin <= thetaMax) {
      theta = thetaMin + generator.nextDouble() * (thetaMax - thetaMin);
      if (theta > thetaMax) {
        theta = thetaMax;
      }
    } else {
      // wrap-around
      double m = thetaMin - 360.0;
      theta = m + generator.nextDouble() * (thetaMax - m);
      if (theta < 0) {
        theta += 360.0;
      }
    }
    return new SphericalCoords(theta, randomPhi(generator, phiMin, phiMax));
  }

  /**
   * Randomly perturbs this position according to a normal distribution
   * having a standard deviation of <code>sigma</code> degrees.
   */
  public void perturb(Random generator, double sigma) {
    perturb(generator, sigma, generator.nextDouble() * 360.0);
  }

  /**
   * Randomly perturbs this position according to a normal distribution
   * having a standard deviation of <code>sigma</code> degrees. The
   * results are additionally constrained to lie at a position angle of
   * <code>positionAngle</code> degrees relative to the original position.
   */
  public void perturb(Random generator, double sigma, double positionAngle) {
    Vec3 pos = new Vec3(theta, phi);
    Vec3 north = northOf();
    Vec3 east = eastOf();
    positionAngle = Math.toRadians(positionAngle);

    // rotate north vector at pos by -positionAngle
    Vec3 dir = Vec3.product(east, Math.sin(positionAngle));
    dir = Vec3.productSum(dir, north, Math.cos(positionAngle));

    // perturb in this direction by a random angle that is normally
    // distributed with a standard deviation of sigma degrees
    double mag  = Math.toRadians(sigma * generator.nextGaussian());

    // obtain the perturbed position and convert back to spherical coordinates
    dir.scale(Math.sin(mag));
    set(Vec3.productSum(dir, pos, Math.cos(mag)));
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeDouble(theta);
    out.writeDouble(phi);
  }

  @Override public void readFields(DataInput in) throws IOException {
    theta = in.readDouble();
    phi = in.readDouble();
  }
}
