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

import java.util.ArrayList;
import java.util.List;

import ipac.stk.math.SphericalCoords;
import ipac.stk.math.Vec3;


/**
 * A convex polygon with great circle edges on the sky.
 */
public class Polygon extends AbstractRegion {

  private static final double EPSILON = 5e-8;

  private static final Vec3[] DEFAULT_VERTS = {Vec3.X, Vec3.Y, Vec3.Z};
  private static final Vec3[] DEFAULT_EDGES = {Vec3.Y, Vec3.Z, Vec3.X};
  private static final int MIN_VERTICES = 3;

  // Polygon vertices as unit vectors, arranged in counter clockwise order
  // when viewed in a right handed coordinate system from outside the unit
  // sphere.
  private Vec3[] vertices = DEFAULT_VERTS;
  // Polygon edge plane normals, oriented towards the inside of the polygon.
  // The i-th normal corresponds to the edge entering the i-th vertex.
  private Vec3[] edges = DEFAULT_EDGES;

  /**
   * Creates a new Polygon object.
   */
  public Polygon() { }

  /**
   * Creates a new Polygon corresponding to the convex hull of the given
   * set of points.
   */
  public Polygon(Vec3[] points) {
    convexHull(points);
  }

  /**
   * Creates a new Polygon corresponding to the convex hull of the given
   * set of points.
   */
  public Polygon(SphericalCoords[] points) {
    Vec3[] verts = new Vec3[points.length];
    for (int i = 0; i < points.length; ++i) {
      verts[i] = new Vec3(points[i]);
    }
    convexHull(verts);
  }

  /**
   * Returns a copy of the vertices that define this Polygon.
   */
  public Vec3[] getVertices() {
    Vec3[] verts = new Vec3[vertices.length];
    for (int i = 0; i < vertices.length; ++i) {
      verts[i] = new Vec3(vertices[i]);
    }
    return verts;
  }

  /**
   * Computes edge normals from polygon vertices.
   */
  private void computeEdges() {
    if (vertices.length < MIN_VERTICES) {
      throw new IllegalStateException(String.format(
        "polygon must have at least %1$d vertices", MIN_VERTICES));
    }
    edges = new Vec3[vertices.length];
    int prev = vertices.length - 1;
    for (int i = 0; i < vertices.length; prev = i, ++i) {
      edges[i] = Vec3.crossProduct(vertices[prev], vertices[i]);
      edges[i].normalize();
    }
  }

  /**
   * Sets vertices to those in the index cycle starting with i.
   */
  private void setCycle(Vec3[] points, List<Integer> indexes, int i) {
    // found vertex cycle - done!
    int j = 0;
    while (indexes.get(j) != i) {
      ++j;
    }
    vertices = new Vec3[indexes.size() - j];
    for (int k = j; k < indexes.size(); ++k) {
      vertices[k - j] = new Vec3(points[indexes.get(k)]);
    }
    computeEdges();
  }

  /**
   * Sets this polygon to the convex hull of the given point set.
   *
   * @throws  java.lang.IllegalArgumentException
   *    If the point set is not hemispherical or is otherwise degenerate.
   */
  public void convexHull(Vec3[] points) {
    if (points.length < MIN_VERTICES) {
      throw new IllegalArgumentException(String.format(
        "point cloud must contain at least %1$d vertices", MIN_VERTICES));
    }
    Vec3 edge = new Vec3();
    List<Integer> verts = new ArrayList<Integer>();
    boolean[] onhull = new boolean[points.length];
    boolean[] ignore = new boolean[points.length];

    // The convex hull of a point set is the whole sphere unless the points
    // are hemispherical (contained in an arbitrary hemisphere). In the
    // hemispherical case, points can be centrally projected to a plane,
    // and then planar algorithms apply.

    // Note that any edge on the convex hull provides such a hemisphere. To
    // find an initial edge, we simply consider all possible vertex pairs and
    // test whether the corresponding plane encloses (or excludes) all points.
    // Thereafter, the gift wrapping algorithm (R. A. Jarvis) is applied.
    // Weighing in favor of this quadratic and naive algorithm is robustness
    // and the expectation that input sets will be small.

    // Find initial edge
    int lastIndex = 0;
    int curIndex = 0;
    for (int i = 0; i < points.length; ++i) {
      if (ignore[i]) {
        continue;
      }
      for (int j = i + 1; j < points.length; ++j) {
        edge.cross(points[i], points[j]);
        double norm = edge.norm();
        if (norm < EPSILON) {
          if (points[i].dot(points[j]) < 0.0) {
            throw new IllegalArgumentException(
              "point set contains antipodal points");
          } else if (norm == 0.0) {
            // point j indistinguishable from i:
            // skip the edge and mark j as ignorable
            ignore[j] = true;
            continue;
          }
        }
        edge.divide(norm);
        boolean in = false;
        boolean out = false;
        for (int k = 0; k < points.length && !(in && out); ++k) {
          if (k == i || k == j) {
            continue;
          }
          double d = edge.dot(points[k]);
          if (d > EPSILON) {
            in = true;
          } else if (d < -EPSILON) {
            out = true;
          }
        }
        if (in) {
          if (!out) {
            // vertices all inside or on edge
            lastIndex = i;
            curIndex = j;
            break;
          }
          // vertices found on both sides of edge, continue searching
        } else if (out) {
          lastIndex = j;
          curIndex = i;
          break;
        } else {
          // vertices all on edge
          throw new IllegalArgumentException("degenerate point set");
        }
      }
    }
    if (lastIndex == curIndex) {
      throw new IllegalArgumentException("point set is not hemispherical");
    }

    // initial edge found, proceed with gift wrapping
    verts.add(lastIndex);
    verts.add(curIndex);
    onhull[lastIndex] = true;
    onhull[curIndex] = true;

    // Find next vertex after curIndex, stopping once a vertex
    // that is already on the hull is found.
    while (true) {
      int i = 0;
      for (; i < points.length; ++i) {
        if (ignore[i] || i == curIndex || i == lastIndex) {
          continue;
        }
        edge.cross(points[curIndex], points[i]);
        double norm = edge.norm();
        if (norm < EPSILON) {
          if (points[curIndex].dot(points[i]) < 0.0) {
            throw new IllegalArgumentException(
              "point set contains antipodal points");
          } else if (norm == 0.0) {
            // i is indistinguishable from curIndex - if i is on the hull,
            // then have cycle of vertices containing all others, otherwise
            // skip the edge and mark i as ignorable.
            if (onhull[i]) {
              verts.remove(verts.size() - 1);
              setCycle(points, verts, i);
              return;
            }
            ignore[i] = true;
            continue;
          }
        }
        edge.divide(norm);
        boolean in = false;
        boolean out = false;
        for (int j = 0; j < points.length && !(in && out); ++j) {
          if (i == j || j == curIndex) {
            continue;
          }
          double d = edge.dot(points[j]);
          if (d > EPSILON) {
            in = true;
          } else if (d < EPSILON) {
            out = true;
          }
        }
        if (in) {
          if (!out) {
            // vertices all inside or on edge
            break;
          }
          // vertices found on both sides of edge, continue searching
        } else if (!out) {
          // vertices all on edge
          throw new IllegalArgumentException("degenerate point set");
        }
      }

      if (i == points.length) {
        // no edge leaving curIndex found
        throw new IllegalArgumentException("point set is not hemispherical");
      } else if (onhull[i]) {
        // found vertex cycle - done!
        setCycle(points, verts, i);
        return;
      } else {
        lastIndex = curIndex;
        curIndex = i;
        verts.add(i);
        onhull[i] = true;
      }
    }
  }

  /**
   * Returns the polygon that is the intersection of the input
   * <code>poly</code> with the positive half space defined by the given
   * <code>plane</code>. Uses the Sutherland-Hodgman algorithm, adapted
   * to spherical polygons.
   *
   * @return  The intersection of <code>poly</code> and <code>plane</code>,
   *          or <code>null</code> if the intersection is empty.
   * @throws  java.lang.IllegalArgumentException
   *    If the input polygon and planes are coplanar.
   */
  public static Polygon clip(Polygon poly, Vec3 plane) {
    List<Vec3> outVertices = new ArrayList<Vec3>();
    List<Vec3> outEdges = new ArrayList<Vec3>();
    double[] classification = new double[poly.vertices.length];
    boolean in = false;
    boolean out = false;
    for (int i = 0; i < poly.vertices.length; ++i) {
      double d = plane.dot(poly.vertices[i]);
      if (d > EPSILON) {
        in = true;
      } else if (d < -EPSILON) {
        out = true;
      } else {
        d = 0.0;
      }
      classification[i] = d;
    }
    if (!in && !out) {
      throw new IllegalArgumentException(
        "polygon and clipping plane are coplanar");
    }
    if (!out) {
      return poly;
    } else if (!in) {
      return null;
    }

    // Polygon overlaps plane
    int prev = poly.vertices.length - 1;
    double d0 = classification[prev];

    for (int i = 0; i < poly.vertices.length; prev = i, ++i) {
      double d1 = classification[i];
      if (d0 > 0.0) {
        if (d1 >= 0.0) {
          // positive to positive, positive to zero: no intersection
          // add current input vertex to output polygon.
          outVertices.add(poly.vertices[i]);
          outEdges.add(poly.edges[i]);
        } else {
          // positive to negative: add intersection point to polygon
          Vec3 x = Vec3.difference(poly.vertices[i], poly.vertices[prev]);
          x.multiplyAdd(poly.vertices[prev], x, d0 / (d0 - d1));
          x.normalize();
          outVertices.add(x);
          outEdges.add(poly.edges[i]);
        }
      } else if (d0 == 0.0) {
        if (d1 >= 0.0) {
          // zero to positive: no intersection
          // add current input vertex to output polygon.
          outVertices.add(poly.vertices[i]);
          outEdges.add(poly.edges[i]);
        }
        // zero to zero: coplanar edge.
        //    Since the polygon has vertices on both sides of
        //    the plane, this implies concavity or a repeated vertex. We
        //    know the polygon is convex, therefore assume a repeated vertex
        //    (or vertices that are extremely close together such that
        //    limited floating point precision yields a coplanar edge).
        // zero to negative: no intersection
        //
        // in either case, skip the vertex.
      } else {
        if (d1 > 0.0) {
          // negative to positive: add intersection point to output polygon,
          // followed by the current input vertex
          Vec3 x = Vec3.difference(poly.vertices[i], poly.vertices[prev]);
          x.multiplyAdd(poly.vertices[prev], x, d0 / (d0 - d1));
          x.normalize();
          outVertices.add(x);
          outEdges.add(plane);
          outVertices.add(poly.vertices[i]);
          outEdges.add(poly.edges[i]);
        } else if (d1 == 0.0) {
          // negative to zero: add current input vertex to output polygon
          outVertices.add(poly.vertices[i]);
          outEdges.add(plane);
        }
        // negative to negative: no intersection, skip vertex
      }
    }
    Polygon outPoly = new Polygon();
    outPoly.vertices = outVertices.toArray(new Vec3[0]);
    outPoly.edges = outEdges.toArray(new Vec3[0]);
    return outPoly;
  }

  @Override public boolean contains(Vec3 pos) {
    for (Vec3 e : edges) {
      if (e.dot(pos) < 0.0) {
        return false;
      }
    }
    return true;
  }

  @Override public boolean intersects(Region region) {
    if (region instanceof Polygon) {
      Polygon poly = (Polygon) region;
      // Find intersection of the 2 polygons and check whether it is empty.
      Polygon p = poly;
      for (Vec3 edge : poly.edges) {
        p = clip(p, edge);
        if (p == null) {
          break;
        }
      }
      return (p != null);
    } else if (region instanceof Circle) {
      Circle circle = (Circle) region;
      // If the circle center is inside the polygon,
      // there is obviously overlap.
      if (contains(circle.getCenterVec())) {
        return true;
      }
      // Otherwise, test if the minimum distance between the circle center
      // and a polygon edge is less than or equal to the circle radius;
      // if so, there is overlap.
      for (int i = 0; i < vertices.length; ++i) {
        int j = (i + 1 == vertices.length) ? 0 : i + 1;
        double d = GeomUtils.minAngularSeparation(
          circle.getCenterVec(), edges[i], vertices[i], vertices[j]);
        if (d <= circle.getRadius()) {
          return true;
        }
      }
      return false;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public boolean contains(Region region) {
    if (region instanceof Polygon) {
      Polygon poly = (Polygon) region;
      for (Vec3 v : poly.vertices) {
        if (!contains(v)) {
          return false;
        }
      }
      return true;
    } else if (region instanceof Circle) {
      Circle circle = (Circle) region;
      if (contains(circle.getCenterVec())) {
        // find minimum distance between circle center and any polygon edge
        double mind = 180.0;
        for (int i = 0; i < vertices.length; ++i) {
          double d = GeomUtils.minAngularSeparation(
            circle.getCenterVec(), edges[i], vertices[i],
            vertices[(i + 1 == vertices.length) ? 0 : i + 1]);
          if (d < mind) {
            mind = d;
          }
        }
        return mind >= circle.getRadius();
      }
      return false;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public boolean isContainedBy(Region region) {
    if (region instanceof Polygon) {
      Polygon poly = (Polygon) region;
      for (Vec3 v : vertices) {
        if (!poly.contains(v)) {
          return false;
        }
      }
      return true;
    } else if (region instanceof Circle) {
      Circle circle = (Circle) region;
      for (Vec3 v : vertices) {
        if (!circle.contains(v)) {
          return false;
        }
      }
      return true;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeInt(vertices.length);
    for (Vec3 v : vertices) {
      v.write(out);
    }
  }

  @Override public void readFields(DataInput in) throws IOException {
    int size = in.readInt();
    if (size < MIN_VERTICES) {
      throw new IOException(String.format(
        "polygon must have at least %1$d vertices", MIN_VERTICES));
    }
    vertices = new Vec3[size];
    for (int i = 0; i < size; ++i) {
      Vec3 v = new Vec3();
      v.readFields(in);
      vertices[i] = v;
    }
    computeEdges();
  }
}
