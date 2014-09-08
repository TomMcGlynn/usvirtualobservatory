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

/**
 * A class representing 3 by 3 matrices, aimed primarily at supporting
 * rotations.
 */
public final class Mat33 implements Cloneable {

  enum RotationAxis { X, Y, Z }

  private double m00;
  private double m01;
  private double m02;
  private double m10;
  private double m11;
  private double m12;
  private double m20;
  private double m21;
  private double m22;

  /** Creates an identity matrix. */
  public Mat33() {
    identity();
  }

  /** Creates a copy of <code>m</code>. */
  public Mat33(Mat33 m) {
    assign(m);
  }

  /**
   * Creates a <code>Mat33</code> equal to the product of m1 and m2.
   */
  public Mat33(Mat33 m1, Mat33 m2) {
    multiply(m1, m2);
  }

  /**
   * Creates a <code>Mat33</code> corresponding to a rotation of
   * angle degrees around the given axis.
   */
  public Mat33(double angle, RotationAxis axis) {
    switch (axis) {
    case X :
      xRotation(angle);
      break;
    case Y :
      yRotation(angle);
      break;
    case Z :
      zRotation(angle);
      break;
    default :
      identity();
      break;
    }
  }

  /**
   * Creates a <code>Mat33</code> corresponding to a rotation of angle degrees
   * around the given axis, which is assumed to be a unit vector.
   */
  public Mat33(double angle, Vec3 axis) {
    rotation(angle, axis);
  }

  @Override public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /** Sets this <code>Mat33</code> to the identity matrix. */
  public void identity() {
    m00 = 1.0; m01 = 0.0; m02 = 0.0;
    m10 = 0.0; m11 = 1.0; m12 = 0.0;
    m20 = 0.0; m21 = 0.0; m22 = 1.0;
  }

  /** Sets each element of this <code>Mat33</code> to zero. */
  public void zero() {
    m00 = 0.0; m01 = 0.0; m02 = 0.0;
    m10 = 0.0; m11 = 0.0; m12 = 0.0;
    m20 = 0.0; m21 = 0.0; m22 = 0.0;
  }

  /** Copies the matrix <code>m</code> into this <code>Mat33</code>. */
  public void assign(Mat33 m) {
    m00 = m.m00; m01 = m.m01; m02 = m.m02;
    m10 = m.m10; m11 = m.m11; m12 = m.m12;
    m20 = m.m20; m21 = m.m21; m22 = m.m22;
  }

  /**
   * Sets this <code>Mat33</code> to the matrix corresponding to
   * a rotation of <code>angle</code> degrees around the x-axis.
   */
  public void xRotation(double angle) {
    double a = Math.toRadians(angle);
    double s = Math.sin(a);
    double c = Math.cos(a);
    m00 = 1.0; m01 = 0.0; m02 = 0.0;
    m10 = 0.0; m11 = c;   m12 = s;
    m20 = 0.0; m21 = -s;  m22 = c;
  }

  /**
   * Sets this <code>Mat33</code> to the matrix corresponding to
   * a rotation of <code>angle</code> degrees around the y-axis.
   */
  public void yRotation(double angle) {
    double a = Math.toRadians(angle);
    double s = Math.sin(a);
    double c = Math.cos(a);
    m00 = c;   m01 = 0.0; m02 = -s;
    m10 = 0.0; m11 = 1.0; m12 = 0.0;
    m20 = s;   m21 = 0.0; m22 = c;
  }

  /**
   * Sets this <code>Mat33</code> to the matrix corresponding to
   * a rotation of <code>angle</code> degrees around the z-axis.
   */
  public void zRotation(double angle) {
    double a = Math.toRadians(angle);
    double s = Math.sin(a);
    double c = Math.cos(a);
    m00 = c;   m01 = s;   m02 = 0.0;
    m10 = -s;  m11 = c;   m12 = 0.0;
    m20 = 0.0; m21 = 0.0; m22 = 1.0;
  }

  /**
   * Sets this <code>Mat33</code> to the matrix corresponding to a rotation of
   * <code>angle</code> degrees around the specified axis, which is assumed to
   * be a unit vector.
   */
  public void rotation(double angle, Vec3 axis) {
    double a = Math.toRadians(angle);
    double s = Math.sin(a);
    double c = Math.cos(a);
    double f  = 1.0 - c;
    double xy = axis.getX() * axis.getY();
    double xz = axis.getX() * axis.getZ();
    double xs = axis.getX() * s;
    double yz = axis.getY() * axis.getZ();
    double ys = axis.getY() * s;
    double zs = axis.getZ() * s;
    m00 = axis.getX() * axis.getX() * f + c;
    m01 = xy * f + zs;
    m02 = xz * f - ys;
    m10 = xy * f - zs;
    m11 = axis.getY() * axis.getY() * f + c;
    m12 = yz * f + xs;
    m20 = xz * f + ys;
    m21 = yz * f - xs;
    m22 = axis.getZ() * axis.getZ() * f + c;
  }

  /**
   * Returns a new <code>Mat33</code> containing the product of
   * <code>m1</code> and <code>m2</code>.
   */
  public static Mat33 product(Mat33 m1, Mat33 m2) {
    return new Mat33(m1, m2);
  }

  /**
   * Sets this <code>Mat33</code> to the product of <code>m1</code> and
   * <code>m2</code>.
   */
  public void multiply(Mat33 m1, Mat33 m2) {
    double rm00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20;
    double rm01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21;
    double rm02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22;
    double rm10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20;
    double rm11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21;
    double rm12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22;
    double rm20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20;
    double rm21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21;
    double rm22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22;

    m00 = rm00; m01 = rm01; m02 = rm02;
    m10 = rm10; m11 = rm11; m12 = rm12;
    m20 = rm20; m21 = rm21; m22 = rm22;
  }

  /** Sets this <code>Mat33</code> to the transpose of itself. */
  public void transpose() {
    double tmp;
    tmp = m01; m01 = m10; m10 = tmp;
    tmp = m02; m02 = m20; m20 = tmp;
    tmp = m10; m10 = m01; m01 = tmp;
    tmp = m12; m12 = m21; m21 = tmp;
    tmp = m20; m20 = m02; m02 = tmp;
    tmp = m21; m21 = m12; m12 = tmp;
  }

  /**
   * Sets this <code>Mat33</code> to the transpose of <code>m</code>.
   */
  public void transpose(Mat33 m) {
    if (this == m) {
      transpose();
    } else {
      m00 = m.m00; m01 = m.m10; m02 = m.m20;
      m10 = m.m01; m11 = m.m11; m12 = m.m21;
      m20 = m.m02; m21 = m.m12; m22 = m.m22;
    }
  }

  /**
   * Returns the matrix-vector product of
   * this <code>Mat33</code> and <code>v</code>.
   */
  public Vec3 multiply(Vec3 v) {
    double x = m00 * v.getX() + m01 * v.getY() + m02 * v.getZ();
    double y = m10 * v.getX() + m11 * v.getY() + m12 * v.getZ();
    double z = m20 * v.getX() + m21 * v.getY() + m22 * v.getZ();
    return new Vec3(x, y, z);
  }

  /**
   * Returns the matrix-vector product of the transpose
   * of this <code>Mat33</code> and <code>v</code>.
   */
  public Vec3 multiplyTranspose(Vec3 v) {
    double x = m00 * v.getX() + m10 * v.getY() + m20 * v.getZ();
    double y = m01 * v.getX() + m11 * v.getY() + m21 * v.getZ();
    double z = m02 * v.getX() + m12 * v.getY() + m22 * v.getZ();
    return new Vec3(x, y, z);
  }
}
