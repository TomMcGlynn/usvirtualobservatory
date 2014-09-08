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
 * A circular {@link Region} on the sky.
 */
public class Circle extends AbstractRegion {

  private static final double EPSILON = 0.001;

  private SphericalCoords center = new SphericalCoords();
  private Vec3 centerVec = new Vec3(1.0, 0.0, 0.0);
  private double radius = 0.0;
  private double d2Limit = 0.0;

  public Circle() { }

  /**
   * Creates a new Circle.
   *
   * @param theta         longitude of circle center (deg)
   * @param phi           latitude of circle center (deg)
   * @param openingAngle  circle radius/opening angle (deg)
   */
  public Circle(double theta, double phi, double openingAngle) {
    initialize(theta, phi, openingAngle);
  }

  SphericalCoords getCenter() {
    return center;
  }

  Vec3 getCenterVec() {
    return centerVec;
  }

  double getRadius() {
    return radius;
  }

  private void initialize(double theta, double phi, double openingAngle) {
    if (theta < 0.0 || theta >= 360.0 || phi < -90.0 || phi > 90.0) {
      throw new IllegalArgumentException("coordinate values out of range");
    }
    if (openingAngle < 0.0 || openingAngle > 180.0) {
      throw new IllegalArgumentException("circle opening angle out of range");
    }
    center.set(theta, phi);
    radius = openingAngle;
    centerVec.set(center);
    double shr = Math.sin(Math.toRadians(radius * 0.5));
    d2Limit = 4.0 * shr * shr;
    double deltaTheta = GeomUtils.maxAlpha(openingAngle, phi);
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
      SphericalCoords.clampPhi(phi - openingAngle),
      SphericalCoords.clampPhi(phi + openingAngle)
    ));
  }

  @Override public boolean contains(Vec3 pos) {
    double dx = pos.getX() - centerVec.getX();
    double dy = pos.getY() - centerVec.getY();
    double dz = pos.getZ() - centerVec.getZ();
    return (dx * dx + dy * dy + dz * dz <= d2Limit);
  }

  @Override public boolean contains(SphericalCoords pos) {
    return (center.angularSeparation(pos) <= radius);
  }

  @Override public boolean intersects(Region region) {
    if (region instanceof Circle) {
      Circle other = (Circle) region;
      return Vec3.angularSeparation(centerVec, other.centerVec) <=
             radius + other.radius;
    } else if (region instanceof Polygon) {
      return ((Polygon) region).intersects(this);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public boolean contains(Region region) {
    if (region instanceof Circle) {
      Circle other = (Circle) region;
      return Vec3.angularSeparation(centerVec, other.centerVec) +
             other.radius <= radius;
    } else if (region instanceof Polygon) {
      return ((Polygon) region).isContainedBy(this);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public boolean isContainedBy(Region region) {
    if (region instanceof Circle) {
      Circle other = (Circle) region;
      return Vec3.angularSeparation(centerVec, other.centerVec) +
             other.radius <= radius;
    } else if (region instanceof Polygon) {
      return ((Polygon) region).contains(this);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeDouble(center.getTheta());
    out.writeDouble(center.getPhi());
    out.writeDouble(radius);
  }

  @Override public void readFields(DataInput in) throws IOException {
    double theta = in.readDouble();
    double phi = in.readDouble();
    double openingAngle = in.readDouble();
    initialize(theta, phi, openingAngle);
  }
}

