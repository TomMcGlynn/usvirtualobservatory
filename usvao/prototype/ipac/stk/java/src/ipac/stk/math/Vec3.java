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

import org.apache.hadoop.io.Writable;


/**
 * A 3 dimensional column vector.
 */
public final class Vec3 implements Cloneable, Writable {

  public static final Vec3 X = new Vec3(1.0, 0.0, 0.0);
  public static final Vec3 Y = new Vec3(0.0, 1.0, 0.0);
  public static final Vec3 Z = new Vec3(0.0, 0.0, 1.0);
  public static final Vec3 NEG_X = new Vec3(-1.0, 0.0, 0.0);
  public static final Vec3 NEG_Y = new Vec3(0.0, -1.0, 0.0);
  public static final Vec3 NEG_Z = new Vec3(0.0, 0.0, -1.0);

  private double x;
  private double y;
  private double z;

  /** Constructs a <code>Vec3</code> with all components set to zero. */
  public Vec3() { }

  /**
   * Constructs a unit vector corresponding to the given spherical
   * coordinates.
   */
  public Vec3(SphericalCoords sc) {
    set(sc);
  }

  /**
   * Constructs a unit vector corresponding to the given spherical
   * coordinates.
   */
  public Vec3(double theta, double phi) {
    set(theta, phi);
  }

  /** Constructs a new <code>Vec3</code> with the given components. */
  public Vec3(double x, double y, double z) {
    set(x, y, z);
  }

  /** Constructs a copy of <code>v</code>. */
  public Vec3(Vec3 v) {
    set(v);
  }

  /** Constructs a copy of <code>v</code> scaled by scalar <code>s</code>. */
  public Vec3(Vec3 v, double s) {
    scale(v, s);
  }

  /**
   * Constructs a new <code>Vec3</code> equal to the cross product of vectors
   * <code>v1</code> and <code>v2</code>.
   */
  public Vec3(Vec3 v1, Vec3 v2) {
    cross(v1, v2);
  }

  /**
   * Constructs a new <code>Vec3</code> equal to the matrix-vector product of
   * <code>m</code> and <code>v</code>.
   */
  public Vec3(Mat33 m, Vec3 v) {
    multiply(m, v);
  }

  /**
   * Constructs a new <code>Vec3</code> equal to the matrix-vector product of
   * <code>m</code>-transpose and <code>v</code>.
   */
  public Vec3(Vec3 v, Mat33 m) {
    multiply(v, m);
  }

  @Override public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public double getX() {
    return x;
  }
  public double getY() {
    return y;
  }
  public double getZ() {
    return z;
  }
  public void setX(double xc) {
    x = xc;
  }
  public void setY(double yc) {
    y = yc;
  }
  public void setZ(double zc) {
    z = zc;
  }

  /** Copies the components of <code>v</code> into this <code>Vec3</code>. */
  public void set(Vec3 v) {
    x = v.x; y = v.y; z = v.z;
  }

  /** Copies the given components into this <code>Vec3</code>. */
  public void set(double xc, double yc, double zc) {
    x = xc; y = yc; z = zc;
  }

  /**
   * Sets the components of this <code>Vec3</code> to the (euclidian) unit
   * sphere coordinates corresponding to the given spherical coordinates.
   */
  public void set(SphericalCoords sc) {
    set(sc.getTheta(), sc.getPhi());
  }

  /**
   * Sets the components of this <code>Vec3</code> to the (euclidian) unit
   * sphere coordinates corresponding to the given spherical coordinates.
   */
  public void set(double theta, double phi) {
    theta = Math.toRadians(theta);
    phi   = Math.toRadians(phi);
    double c = Math.cos(phi);
    x = Math.cos(theta) * c;
    y = Math.sin(theta) * c;
    z = Math.sin(phi);
  }

  /** Sets all components of this <code>Vec3</code> to zero. */
  public void zero() {
    x = 0.0; y = 0.0; z = 0.0;
  }

  /**
   * Returns the dot product of vectors <code>v1</code> and <code>v2</code>.
   */
  public static double dotProduct(Vec3 v1, Vec3 v2) {
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
  }

  /**
   * Returns the dot product of this <code>Vec3</code> with <code>v</code>.
   */
  public double dot(Vec3 v) {
    return dotProduct(this, v);
  }

  /** Returns the norm of this <code>Vec3</code>. */
  public double norm() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  /**
   * Sets this <code>Vec3</code> to a normalized copy of <code>v</code>.
   *
   * @return  the norm of <code>v</code>
   */
  public double normalize(Vec3 v) {
    double n = v.norm();
    if (n != 0.0) {
      x = v.x / n;
      y = v.y / n;
      z = v.z / n;
    } else {
      x = Double.NaN;
      y = Double.NaN;
      z = Double.NaN;
    }
    return n;
  }

  /**
   * Normalizes this <code>Vec3</code>.
   *
   * @return  the norm of this <code>Vec3</code> prior to normalization.
   */
  public double normalize() {
    return normalize(this);
  }

  /**
   * Sets this <code>Vec3</code> to a copy of <code>v</code> scaled by
   * scalar <code>s</code>.
   */
  public void scale(Vec3 v, double s) {
    x = v.x * s;
    y = v.y * s;
    z = v.z * s;
  }

  /**
   * Multiplies each component of this <code>Vec3</code> by <code>s</code>.
   */
  public void scale(double s) {
    scale(this, s);
  }

  /**
   * Sets this <code>Vec3</code> to a copy of <code>v</code> divided by
   * scalar <code>s</code>.
   */
  public void divide(Vec3 v, double s) {
    x = v.x / s;
    y = v.y / s;
    z = v.z / s;
  }

  /** Divides each component of this <code>Vec3</code> by <code>s</code>. */
  public void divide(double s) {
    divide(this, s);
  }

  /** Returns a copy of <code>v</code> scaled by scalar <code>s</code>. */
  public static Vec3 product(Vec3 v, double s) {
    return new Vec3(v, s);
  }

  /**
   * Sets this <code>Vec3</code> to the vector sum of <code>v1</code> and
   * <code>v2</code>.
   */
  public void add(Vec3 v1, Vec3 v2) {
    x = v1.x + v2.x;
    y = v1.y + v2.y;
    z = v1.z + v2.z;
  }

  /** Adds <code>v</code> to this <code>Vec3</code>. */
  public void add(Vec3 v) {
    add(this, v);
  }

  /** Returns the vector sum of <code>v1</code> and <code>v2</code>. */
  public static Vec3 sum(Vec3 v1, Vec3 v2) {
    Vec3 sum = new Vec3();
    sum.add(v1, v2);
    return sum;
  }

  /**
   * Sets this <code>Vec3</code> to the vector difference of <code>v1</code>
   * and <code>v2</code>.
   */
  public void subtract(Vec3 v1, Vec3 v2) {
    x = v1.x - v2.x;
    y = v1.y - v2.y;
    z = v1.z - v2.z;
  }

  /** Subtracts <code>v</code> from this <code>Vec3</code>. */
  public void subtract(Vec3 v) {
    subtract(this, v);
  }

  /** Returns the vector difference of <code>v1</code> and <code>v2</code>. */
  public static Vec3 difference(Vec3 v1, Vec3 v2) {
    Vec3 diff = new Vec3();
    diff.subtract(v1, v2);
    return diff;
  }

  /**
   * Sets this <code>Vec3</code> to the vector sum of <code>v1</code>
   * and the product of <code>v2</code> and scalar <code>s</code>.
   */
  public void multiplyAdd(Vec3 v1, Vec3 v2, double s) {
    x = v1.x + v2.x * s;
    y = v1.y + v2.y * s;
    z = v1.z + v2.z * s;
  }

  /**
   * Returns <code>v1 + v2 * s</code>, the vector sum of <code>v1</code> and
   * the product of <code>v2</code> and scalar <code>s</code>.
   */
  public static Vec3 productSum(Vec3 v1, Vec3 v2, double s) {
    Vec3 v = new Vec3();
    v.multiplyAdd(v1, v2, s);
    return v;
  }

  /**
   * Sets this <code>Vec3</code> to the cross product of <code>v1</code>
   * and <code>v2</code>.
   */
  public void cross(Vec3 v1, Vec3 v2) {
    double xc = v1.y * v2.z - v1.z * v2.y;
    double yc = v1.z * v2.x - v1.x * v2.z;
    double zc = v1.x * v2.y - v1.y * v2.x;
    x = xc; y = yc; z = zc;
  }

  /** Returns the cross product of <code>v1</code> and <code>v2</code>. */
  public static Vec3 crossProduct(Vec3 v1, Vec3 v2) {
    return new Vec3(v1, v2);
  }

  /**
   * Sets this <code>Vec3</code> to the matrix-vector product of
   * <code>m</code> and <code>v</code>.
   */
  public void multiply(Mat33 m, Vec3 v) {
    set(m.multiply(v));
  }

  /**
   * Returns the matrix-vector product of <code>m</code> and <code>v</code>.
   */
  public static Vec3 product(Mat33 m, Vec3 v) {
    return new Vec3(m, v);
  }

  /**
   * Sets this <code>Vec3</code> to the matrix-vector product of
   * <code>m</code>-transpose and <code>v</code>.
   */
  public void multiply(Vec3 v, Mat33 m) {
    set(m.multiplyTranspose(v));
  }

  /**
   * Returns the matrix-vector product of <code>m</code>-transpose and
   * <code>v</code>.
   */
  public static Vec3 product(Vec3 v, Mat33 m) {
    return new Vec3(v, m);
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeDouble(x);
    out.writeDouble(y);
    out.writeDouble(z);
  }

  @Override public void readFields(DataInput in) throws IOException {
    x = in.readDouble();
    y = in.readDouble();
    z = in.readDouble();
  }

  /**
   * Returns the angular separation (degrees) between vectors
   * <code>v1</code> and <code>v2</code>.
   */
  public static double angularSeparation(Vec3 v1, Vec3 v2) {
    double x = v1.y * v2.z - v1.z * v2.y;
    double y = v1.z * v2.x - v1.x * v2.z;
    double z = v1.x * v2.y - v1.y * v2.x;
    double s = Math.sqrt(x * x + y * y + z * z);
    double c = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    if (c == 0.0 && s == 0.0) {
      return 0.0;
    }
    return Math.toDegrees(Math.atan2(s, c));
  }

  /**
   * Returns the position angle (degrees) between the two specified vectors,
   * which can be of arbitrary length.
   */
  public static double positionAngle(Vec3 v1, Vec3 v2) {
    Vec3   unit = new Vec3();
    double mv1 = unit.normalize(v1);
    double mv2 = v2.dot(v2);
    if (mv1 == 0.0 || mv2 == 0.0) {
      return 0.0;
    }
    Vec3 north = new Vec3(-v1.x * v1.z,
                          -v1.y * v1.z,
                          v1.x * v1.x + v1.y * v1.y);
    Vec3 east  = Vec3.crossProduct(north, unit);
    Vec3 delta = Vec3.difference(v2, v1);
    double s = Vec3.dotProduct(east, delta);
    double c = Vec3.dotProduct(north, delta);
    if (s == 0.0 && c == 0.0) {
      return 0.0;
    }
    return Math.toDegrees(Math.atan2(s, c));
  }

  /**
   * Returns the position angle (degrees) between 2 vectors which are
   * both assumed to be of unit length.
   */
  public static double unitVectorPositionAngle(Vec3 v1, Vec3 v2) {
    Vec3 north = new Vec3(-v1.x * v1.z,
                          -v1.y * v1.z,
                          v1.x * v1.x + v1.y * v1.y);
    Vec3 east  = Vec3.crossProduct(north, v1);
    Vec3 delta = Vec3.difference(v2, v1);
    double s = Vec3.dotProduct(east, delta);
    double c = Vec3.dotProduct(north, delta);
    if (s == 0.0 && c == 0.0) {
      return 0.0;
    }
    return Math.toDegrees(Math.atan2(s, c));
  }

}
