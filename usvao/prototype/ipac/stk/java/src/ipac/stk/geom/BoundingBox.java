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


/**
 * A longitude/latitude angle bounding box.
 *
 * <p>A bounding box consists of a minimum and maximum latitude angle, both
 * inclusive, which can be retrieved using {@link #getMinPhi()} and
 * {@link #getMaxPhi()}. In addition, the bounding box has a starting and
 * ending longitude angle; this is not the same as minimum and maximum
 * longitude angle because of the 0/360 degree longitude angle discontinuity.
 * These can be accessed with {@link #getStartTheta()} and
 * {@link #getEndTheta()}.
 * </p>
 *
 * <p>The starting longitude angle of a bounding box is the longitude of the
 * edge S such that traversing S from left to right (in the direction of
 * increasing longitude angle) corresponds to entering the bounding box.
 * Similarly, the ending longitude angle corresponds to the edge E such that
 * traversing E from left to right corresponds to leaving the bounding box.
 * </p>
 *
 * <p>The longitude interval of the bounding box wraps from 360 to 0 deg when
 * <code>{@link #getStartTheta()} &gt; {@link #getEndTheta()}</code>. Code
 * that computes bounding boxes for small regions must therefore excercise
 * extra care to avoid inadvertently generating huge boxes.
 * </p>
 *
 * TODO: BoundingBox should implement Region
 */
public class BoundingBox {

  private double startTheta;
  private double endTheta;
  private double minPhi;
  private double maxPhi;

  /**
   * Create a new bounding box with the given starting/ending longitude
   * angles and minimum/maximum latitude angles.
   */
  public BoundingBox(double startTheta, double endTheta,
                     double minPhi, double maxPhi) {
    this.startTheta = startTheta;
    this.endTheta = endTheta;
    this.minPhi = minPhi;
    this.maxPhi = maxPhi;
  }

  /**
   * Returns <code>true</code> if the intersection of the two bounding
   * boxes is non-empty.
   */
  public boolean intersects(BoundingBox bbox) {
    if (minPhi > bbox.maxPhi || maxPhi < bbox.minPhi) {
      return false;
    }
    if (startTheta <= endTheta) {
      // this bounding box doesn't wrap
      if (bbox.startTheta <= bbox.endTheta) {
        return (startTheta <= bbox.endTheta && endTheta >= bbox.startTheta);
      } else {
        // bbox wraps
        return (startTheta <= bbox.endTheta || endTheta >= bbox.startTheta);
      }
    } else if (bbox.startTheta <= bbox.endTheta) {
      return (bbox.startTheta <= endTheta || bbox.endTheta >= startTheta);
    }
    // both bounding boxes wrap, i.e. contain longitude = 0
    return true;
  }

  /**
   * Computes and returns the bounding box of two bounding boxes.
   *
   * @return The bounding box of the two input bounding boxes.
   */
  public static BoundingBox extend(BoundingBox bbox1, BoundingBox bbox2) {
    double minPhi = Math.min(bbox1.minPhi, bbox2.minPhi);
    double maxPhi = Math.max(bbox1.maxPhi, bbox2.maxPhi);
    double startTheta = 0.0;
    double endTheta = 360.0;
    if (bbox1.startTheta <= bbox1.endTheta) {
      if (bbox2.startTheta <= bbox2.endTheta) {
        startTheta = Math.min(bbox1.startTheta, bbox2.startTheta);
        endTheta = Math.max(bbox1.endTheta, bbox2.endTheta);
      } else {
        // bbox2 wraps
        if (bbox2.startTheta <= bbox1.endTheta) {
          if (bbox2.endTheta < bbox1.startTheta) {
            startTheta = Math.min(bbox1.startTheta, bbox2.startTheta);
            endTheta = bbox2.endTheta;
          }
        } else if (bbox2.endTheta >= bbox1.startTheta) {
          startTheta = bbox2.startTheta;
          endTheta = Math.max(bbox1.endTheta, bbox2.endTheta);
        } else {
          // bbox1, bbox2 are disjoint; find bounding box with the smaller
          // longitude range
          if (bbox2.startTheta - bbox1.endTheta >
              bbox1.startTheta - bbox2.endTheta) {
            startTheta = bbox2.startTheta;
            endTheta = bbox1.endTheta;
          } else {
            startTheta = bbox1.startTheta;
            endTheta = bbox2.endTheta;
          }
        }
      }
    } else {
      // bbox1 wraps
      if (bbox2.startTheta <= bbox2.endTheta) {
        if (bbox1.startTheta <= bbox2.endTheta) {
          if (bbox1.endTheta < bbox2.startTheta) {
            startTheta = Math.min(bbox2.startTheta, bbox1.startTheta);
            endTheta = bbox1.endTheta;
          }
        } else if (bbox1.endTheta >= bbox2.startTheta) {
          startTheta = bbox1.startTheta;
          endTheta = Math.max(bbox2.endTheta, bbox1.endTheta);
        } else {
          // bbox1, bbox2 are disjoint; find bounding box with the smaller
          // longitude range
          if (bbox1.startTheta - bbox2.endTheta >
              bbox2.startTheta - bbox1.endTheta) {
            startTheta = bbox1.startTheta;
            endTheta = bbox2.endTheta;
          } else {
            startTheta = bbox2.startTheta;
            endTheta = bbox1.endTheta;
          }
        }
      } else {
        // both bbox1 and bbox2 wrap
        double st = Math.min(bbox1.startTheta, bbox2.startTheta);
        double et = Math.max(bbox1.endTheta, bbox2.endTheta);
        if (st > et) {
          startTheta = st;
          endTheta = et;
        }
      }
    }
    return new BoundingBox(startTheta, endTheta, minPhi, maxPhi);
  }

  /**
   * Returns the longitude angle (inclusive) of the edge E of the bounding
   * box such that traversing E from left to right in the direction
   * of increasing longitude angle corresponds to entering the
   * bounding box. This is <strong>not</strong> necessarily the same
   * as the minimum longitude angle of the bounding box, since a box
   * can wrap from a longitude of 360 to 0 degrees.
   */
  public double getStartTheta() {
    return startTheta;
  }

  /**
   * Returns the longitude angle (inclusive) of the edge E of the bounding
   * box such that traversing E from left to right in the direction
   * of increasing longitude angle corresponds to leaving the
   * bounding box. This is <strong>not</strong> necessarily the same
   * as the minimum longitude angle of the bounding box, since a box
   * can wrap from a longitude of 360 to 0 degrees.
   */
  public double getEndTheta() {
    return endTheta;
  }

  /**
   * Returns the minimum latitude angle (inclusive) of the bounding box
   * for this region.
   */
  public double getMinPhi() {
    return minPhi;
  }

  /**
   * Returns the maximum latitude angle (inclusive) of the bounding box
   * for this region.
   */
  public double getMaxPhi() {
    return maxPhi;
  }

}

