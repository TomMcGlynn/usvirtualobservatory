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

import org.apache.hadoop.io.Writable;

import ipac.stk.math.SphericalCoords;
import ipac.stk.math.Vec3;


/**
 * A region of the sky.
 */
public interface Region extends Writable {

  /**
   * Returns a bounding box for the region.
   */
  BoundingBox getBoundingBox();

  /**
   * Tests whether or not the specified position lies in this region.
   *
   * @param pos   The position to test for region membership.
   * @return      <code>true</code> if the given position lies in this region.
   */
  boolean contains(Vec3 pos);

  /**
   * Tests whether or not the specified position lies in this region.
   *
   * @param pos   The position to test for region membership.
   * @return      <code>true</code> if the given position lies in this region.
   */
  boolean contains(SphericalCoords pos);

  /**
   * Tests whether or not the specified region intersects this one
   * (optional operation).
   *
   * @param region  The region to test for overlap.
   * @return        <code>true</code> if the given region intersects this one.
   * @throws java.lang.UnsupportedOperationException
   */
  boolean intersects(Region region);

  /**
   * Tests whether or not this region contains the specified one
   * (optional operation).
   *
   * @param region  The region to test for region membership.
   * @return        <code>true</code> if this region contains the given one.
   * @throws java.lang.UnsupportedOperationException
   */
  boolean contains(Region region);

  /**
   * Tests whether or not the specified region contains this one
   * (optional operation).
   *
   * @param region  The region to test.
   * @return        <code>true</code> if the specified region contains
   *                this one.
   * @throws java.lang.UnsupportedOperationException
   */
  boolean isContainedBy(Region region);
}
