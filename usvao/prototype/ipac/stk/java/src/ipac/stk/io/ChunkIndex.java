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



package ipac.stk.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.util.StringUtils;

import ipac.stk.geom.GeomUtils;
import ipac.stk.json.JSONOutput;
import ipac.stk.json.JSONWritable;
import ipac.stk.math.SphericalCoords;


/**
 * A <code>ChunkIndex</code> is a top-level index file that records
 * information about a set of chunk files that tile the sky. These files
 * can be binary, storing only a unique integer key, longitude angle, and
 * latitude angle for each record, or textual, in which case an arbitrary
 * set of fields can be stored. Textual chunk files store field data as
 * a UTF-8 string consisting of null delimited field value UTF-8 strings.
 * The first three fields in a record are always the unique integer key,
 * longitude angle, and latitude angle; their values are never null/empty.
 * The longitude and latitude angles must be in decimal degrees.
 *
 * <p>During chunk index creation, input points are mapped to (location
 * integer, scaled latitude angle, unique integer key) tuples, dubbed
 * {@link Location locations}. The location integer consists of a
 * stripe number, chunk number, sub-stripe number and a lane number.
 * One bit (stored in the LSB of the <code>long</code> containing the
 * scaled latitude angle) indicates whether or not the location is an
 * overlap location. The stripe number identifies a latitude angle range
 * and the chunk number a longitude range within the stripe; together,
 * they identify a particular chunk (i.e. a longitude/latitude angle box).
 * Input positions are bucket sorted into chunks and each chunk is stored
 * in a single file.
 * </p>
 *
 * <p>Each chunk is further broken up into sub-stripes (smaller latitude
 * angle ranges) and lanes within each sub-stripe (smaller longitude
 * angle ranges). Points within a chunk are bucket-sorted by sub-stripe
 * and lane; within a lane, points are sorted by their scaled latitude
 * angles and finally by unique integer key.
 * </p>
 *
 * <p>Note that there is exactly one set of chunks corresponding to an input
 * set of points; repeatedly indexing the same set will result in bit-wise
 * identical chunk files. This is particularly important since chunk indexes
 * can be merged: merging the chunk indexes of two sets of points should
 * yield exactly the same results as indexing the union of the two sets.
 * Note that the current implementation only supports merging distinct sets
 * of points.
 * </p>
 *
 * <p>Chunks and also contain all positions within the angular
 * overlap distance of their boundaries; these are marked as overlap
 * locations. They allow spatial searches to be performed without accessing
 * data from neighboring chunks or sub-stripes so long as the match extents
 * are sufficiently small. A direct consequence is that chunk files can be
 * distributed across multiple nodes and spatial searches can be
 * performed in parallel with no inter-node communication.
 * </p>
 *
 * TODO: Document file format
 */
public final class ChunkIndex implements Writable, JSONWritable {

  public static final long MAGIC = 0x0000c0ffeec0ffeeL;
  public static final int VERSION = 1;
  public static final int MAX_STRIPES = 180;
  public static final int MAX_SS_PER_STRIPE = 512;
  public static final int MAX_CHUNKS_PER_STRIPE = 360;
  public static final int MAX_LANES_PER_CSS = 65536;
  public static final int MIN_COLUMNS = 3;
  public static final int MAX_COLUMNS = 8192;

  public static final String COLUMN_SPEC_KEY =
    "ipac.stk.io.ChunkIndex.columnSpec";
  public static final String TYPE_SPEC_KEY =
    "ipac.stk.io.ChunkIndex.typeSpec";
  public static final String UNIT_SPEC_KEY =
    "ipac.stk.io.ChunkIndex.unitSpec";
  public static final String NUM_STRIPES_KEY =
    "ipac.stk.io.ChunkIndex.numStripes";
  public static final int DEF_NUM_STRIPES = 18;
  public static final String NUM_SUB_STRIPES_PER_STRIPE_KEY =
    "ipac.stk.io.ChunkIndex.numSubStripesPerStripe";
  public static final int DEF_NUM_SUB_STRIPES_PER_STRIPE = 20;
  public static final String LANE_WIDTH_DEG_KEY =
    "ipac.stk.io.ChunkIndex.laneWidthDeg";
  public static final double DEF_LANE_WIDTH_DEG = 1.0 / 6.0; // 10 arcmin
  public static final String OVERLAP_DEG_KEY =
    "ipac.stk.io.ChunkIndex.overlapDeg";
  public static final double DEF_OVERLAP_DEG = 1.0 / 6.0;    // 10 arcmin
  public static final String ZIPPED_KEY =
    "ipac.stk.io.ChunkIndex.zipped";
  public static final boolean DEF_ZIPPED = false;
  public static final String BINARY_KEY =
    "ipac.stk.io.ChunkIndex.binary";
  public static final boolean DEF_BINARY = false;
  public static final String DEF_UIK_COLUMN_NAME = "cntr";
  public static final String DEF_THETA_COLUMN_NAME = "ra";
  public static final String DEF_PHI_COLUMN_NAME = "dec";

  /**
   * Metadata for columns in the index.
   */
  public static final class Column
    implements Cloneable, Writable, JSONWritable {

    /** Regular expression for validating column names. */
    public static final Pattern FIELD_NAME_REGEX = Pattern.compile(
      "[a-zA-Z_]+[a-zA-Z_0-9]*");
    /** Regular expression for validating column types. */
    public static final Pattern FIELD_TYPE_REGEX = Pattern.compile(
      "c(?:har)?|date|d(?:ouble)?|i(?:nt(?:eger)?)?|l(?:ong)?|r(?:eal)?");
    /**
     * Regular expression for validating column units;
     * only checks for valid characters.
     */
    public static final Pattern FIELD_UNITS_REGEX = Pattern.compile(
      "[a-zA-Z0-9+-.*^()\\[\\]/ \t]*");

    /** Name of column. */
    private String name = null;
    /** Column data type string. */
    private String type = null;
    /** Unit string. */
    private String units = null;
    /** Maximum width of column values. */
    private int width = 0;
    /** Column index. */
    private int index = -1;

    public Column() { }

    public Column(String name, String type, String units, int index) {
      if (name == null) {
        name = "";
      }
      if (type == null) {
        type = "";
      }
      if (units == null) {
        units = "";
      }
      if (!isValidName(name)) {
        throw new IllegalArgumentException("Invalid column name: " + name);
      }
      if (!isValidType(type)) {
        throw new IllegalArgumentException("Invalid column type: " + type);
      }
      if (!isValidUnits(units)) {
        throw new IllegalArgumentException("Invalid column units: " + units);
      }
      if (index < 0) {
        throw new IllegalArgumentException("Invalid column index: " + index);
      }
      this.name = name;
      this.type = type;
      this.units = units;
      this.index = index;
    }

    @Override public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }

    @Override public int hashCode() {
      return index + name.hashCode() + type.hashCode() + units.hashCode();
    }

    @Override public boolean equals(Object obj) {
      if (obj instanceof Column) {
        Column c = (Column) obj;
        return name.equals(c.name) && type.equals(c.type) &&
               units.equals(c.units) && index == c.index;
      }
      return false;
    }

    @Override public String toString() {
      return JSONOutput.toString(this);
    }

    /**
     * Converts this Column to a JSON string.
     */
    @Override public void writeJSON(JSONOutput out) throws IOException {
      out.object();
      out.pair("name", name);
      out.pair("type", type);
      out.pair("units", units);
      out.pair("index", index);
      out.pair("width", width);
      out.finish();
    }

    String getName() {
      return name;
    }
    String getType() {
      return type;
    }
    String getUnits() {
      return units;
    }
    int getWidth() {
      return width;
    }
    int getIndex() {
      return index;
    }

    public void setWidth(int newWidth) {
      width = newWidth;
    }

    public void readFields(DataInput in) throws IOException {
      name = Text.readString(in);
      type = Text.readString(in);
      units = Text.readString(in);
      width = WritableUtils.readVInt(in);
      index = WritableUtils.readVInt(in);
    }

    public void write(DataOutput out) throws IOException {
      Text.writeString(out, name);
      Text.writeString(out, type);
      Text.writeString(out, units);
      WritableUtils.writeVInt(out, width);
      WritableUtils.writeVInt(out, index);
    }

    public static boolean isValidName(String name) {
      return FIELD_NAME_REGEX.matcher(name).matches();
    }
    public static boolean isValidType(String type) {
      return type == null || type.length() == 0 ||
             FIELD_TYPE_REGEX.matcher(type).matches();
    }
    public static boolean isValidUnits(String units) {
      return units == null || units.length() == 0 ||
             FIELD_UNITS_REGEX.matcher(units).matches();
    }
  }

  /**
   * The location tuple (unique integer key, longitude, latitude) of a record
   * in a ChunkIndex.  The serialization format is byte-comparable; Location
   * is suitable for use as map-reduce key class.
   */
  public static final class Location
    implements WritableComparable<Location>, JSONWritable {

    static final int CHUNK_ID_MASK = 0x1ffff;
    static final int CHUNK_MASK = 0x1ff;
    static final int CHUNK_SHIFT = 25;
    static final int STRIPE_MASK = 0xff;
    static final int STRIPE_SHIFT = 34;
    static final int SUB_STRIPE_MASK = 0x1ff;
    static final int SUB_STRIPE_SHIFT = 16;
    static final int LANE_MASK = 0xffff;
    
    private long location;
    private long phi;
    private long uik;

    public Location() {
      location = 0;
      phi = 0;
      uik = 0;
    }

    public Location(long location, double phiDeg, long uik) {
      this.location = location;
      this.phi = SphericalCoords.phiToLong(phiDeg);
      this.uik = uik;
    }

    /**
     * Returns the id of the chunk this Location belongs to.
     */
    public int getChunkId() {
      return ((int) (location >> CHUNK_SHIFT)) & CHUNK_ID_MASK;
    }

    /**
     * Returns the chunk number of this Location. In range
     * [0, {@link ChunkIndex#getNumChunks(int)}).
     */
    public int getChunk() {
      return ((int) (location >> CHUNK_SHIFT)) & CHUNK_MASK;
    }

    /**
     * Returns the stripe number of this Location. In range
     * [0, {@link ChunkIndex#getNumStripes()}).
     */
    public int getStripe() {
      return ((int) (location >> STRIPE_SHIFT)) & STRIPE_MASK;
    }

    /**
     * Returns the sub-stripe number of this location; in range
     * [0, {@link ChunkIndex#getNumSubStripesPerStripe()}.
     */
    public int getSubStripe() {
      return ((int) (location >> SUB_STRIPE_SHIFT)) & SUB_STRIPE_MASK;
    }

    /**
     * Returns the lane number of this location; in range
     * [0, {@link ChunkIndex#getNumLanes(int,int)}).
     */
    public int getLane() {
      return ((int) location) & LANE_MASK;
    }

    /**
     * Returns the latitude angle of the record this Location was created from,
     * scaled to an integer using
     * {@link ipac.stk.math.SphericalCoords#phiToLong(double)}.
     */
    public long getPhi() {
      return phi;
    }

    /**
     * Returns the unique integer key of the record this Location was
     * created from.
     */
    public long getUIK() {
      return uik;
    }

    @Override public String toString() {
      return JSONOutput.toString(this);
    }

    /**
     * Converts this Location to a JSON string.
     */
    @Override public void writeJSON(JSONOutput out) throws IOException {
      out.object();
      out.pair("stripe", getStripe());
      out.pair("chunk", getChunk());
      out.pair("chunkId", getChunkId());
      out.pair("subStripe", getSubStripe());
      out.pair("lane", getLane());
      out.pair("phi", getPhi());
      out.pair("uik", getUIK());
      out.finish();
    }

    @Override public void readFields(DataInput in) throws IOException {
      location = in.readLong() + Long.MIN_VALUE;
      phi = in.readLong() + Long.MIN_VALUE;
      uik = in.readLong() + Long.MIN_VALUE;
    }

    @Override public void write(DataOutput out) throws IOException {
      out.writeLong(location - Long.MIN_VALUE);
      out.writeLong(phi - Long.MIN_VALUE);
      out.writeLong(uik - Long.MIN_VALUE);
    }

    @Override public int hashCode() {
      return (int) (uik ^ (uik >> Integer.SIZE));
    }

    @Override public boolean equals(Object right) {
      if (right instanceof Location) {
        Location l = (Location) right;
        return l.location == location &&
               l.phi == phi && l.uik == uik;
      }
      return false;
    }

    @Override public int compareTo(Location loc) {
      if (location < loc.location) {
        return -1;
      } else if (location > loc.location) {
        return 1;
      }
      if (phi < loc.phi) {
        return -1;
      } else if (phi > loc.phi) {
        return 1;
      }
      return uik < loc.uik ? -1 : (uik == loc.uik ? 0 : 1);
    }

    /** Comparator that compares serialized locations. */
    public static final class Comparator extends WritableComparator {
      public Comparator() {
        super(Location.class);
      }

      public int compare(byte[] b1, int s1, int l1,
                         byte[] b2, int s2, int l2) {
        return compareBytes(b1, s1, l1, b2, s2, l2);
      }
    }

    static {
      // register comparator class
      WritableComparator.define(Location.class, new Comparator());
    }
  }

  /**
   * Record class for chunk indexes.  Records optionally have an associated
   * byte string containing null delimited field value byte-strings; records
   * with no associated field data are called binary records.
   */
  public static final class Record implements JSONWritable {
    private long uik = 0;
    private long theta = 0;
    private long phi = 0;
    private Text data = null;

    public Record() { }

    public Record(long uik, long theta, long phi) {
      setUIK(uik);
      setTheta(theta);
      setPhi(phi);
    }

    public Record(long uik, double theta, double phi) {
      setUIK(uik);
      setTheta(theta);
      setPhi(phi);
    }

    public Record(long uik, long theta, long phi, Text data) {
      this(uik, theta, phi);
      setData(data);
    }

    public Record(long uik, double theta, double phi, Text data) {
      this(uik, theta, phi);
      setData(data);
    }

    /**
     * Returns the unique integer key of this record.
     */
    public long getUIK() {
      return uik;
    }

    /**
     * Sets the unique integer key of this record.
     */
    public void setUIK(long uniqueIntegerKey) {
      uik = uniqueIntegerKey;
    }

    /**
     * Returns the longitude angle of this record, scaled to an integer using
     * {@link ipac.stk.math.SphericalCoords#thetaToLong(double)}.
     */
    public long getTheta() {
      return theta;
    }

    /**
     * Sets the scaled longitude angle of this record.
     */
    public void setTheta(long thetaScaled) {
      theta = thetaScaled;
    }

    /**
     * Sets the scaled longitude angle of this record.
     */
    public void setTheta(double thetaDeg) {
      theta = SphericalCoords.thetaToLong(thetaDeg);
    }

    /**
     * Returns the latitude angle of this record, scaled to an integer using
     * {@link ipac.stk.math.SphericalCoords#phiToLong(double)}.
     */
    public long getPhi() {
      return phi;
    }

    /**
     * Sets the scaled latitude angle of this record.
     */
    public void setPhi(long phiScaled) {
      phi = phiScaled;
    }

    /**
     * Sets the scaled latitude angle of this record.
     */
    public void setPhi(double phiDeg) {
      phi = SphericalCoords.phiToLong(phiDeg);
    }

    /**
     * Returns the fields associated with this record. The return value is
     * the concatenation of all null terminated field value byte-strings.
     */
    public Text getData() {
      return data;
    }

    /**
     * Sets the field data associated with this record.
     */
    public void setData(Text fieldData) {
      data = fieldData;
    }

    @Override public String toString() {
      return JSONOutput.toString(this);
    }

    /**
     * Converts this Record to a JSON string.
     */
    @Override public void writeJSON(JSONOutput out) throws IOException {
      out.object();
      out.pair("uik", getUIK());
      out.pair("theta", getTheta());
      out.pair("phi", getPhi());
      out.pair("data", getData());
      out.finish();
    }

    /**
     * Returns <code>true</code> if this is a binary record (that is, a
     * record with no associated field data).
     */
    public boolean isBinary() {
      return data == null;
    }

    /**
     * Discards the field data for this record.
     */
    public void setBinary() {
      data = null;
    }
  }

  /** Number of stripes. */
  private int numS = DEF_NUM_STRIPES;
  /** Number of sub-stripes per stripe. */
  private int numSSPerS = DEF_NUM_SUB_STRIPES_PER_STRIPE;
  /** Number of overlap sub-stripes on one side of a stripe. */
  private int overlapSS = 0;
  /** The sub-stripe height, not including overlap (deg). */
  private double ssHeight = 0.0;
  /**
   * The inverse of the sub-stripe height,
   * not including overlap (deg<sup>-1</sup>).
   */
  private double invSSHeight = 0.0;
  private double laneWidthDeg = DEF_LANE_WIDTH_DEG;
  private double overlapDeg = DEF_OVERLAP_DEG;
  private boolean zipped = DEF_ZIPPED;
  private boolean binary = DEF_BINARY;
  private List<Column> columns = null;

  /**
   * The number of entries for each chunk in each stripe,
   * not including overlap entries.
   */
  private long[][] population = null;

  /** The number of overlap entries for each chunk in each stripe. */
  private long[][] overlapPopulation = null;

  /** The number of lanes in a sub-stripe, not including overlap lanes. */
  private int[] ssLanes = null;

  /**
   * The number of lanes in a sub-stripe of a chunk,
   * not including overlap lanes.
   */
  private int[] ssLanesPerC = null;

  /**
   * The number of overlap lanes on one side of a sub-stripe of a chunk;
   * the total number of overlap lanes in a sub-stripe of a chunk is twice
   * this number.
   */
  private int[] overlapLanes = null;

  /**
   * The inverse of the lane width in degrees for each sub-stripe.
   */
  private double[] invLaneWidth = null;

  /** Validate proposed member variables. */
  private static void validate(int numStripes, int numSSPerS,
                               List<Column> columns) {
    if (columns == null || columns.size() < MIN_COLUMNS ||
        columns.size() > MAX_COLUMNS) {
      throw new IllegalArgumentException(String.format(
        "Chunk index must contain a unique integer key, longitude, and " +
        "latitude columns, and may have up to %1$d columns", MAX_COLUMNS));
    }
    if (numStripes < 1 || numStripes > MAX_STRIPES) {
      throw new IllegalArgumentException(String.format(
        "Invalid number of stripes (%1$d): please specify a " +
        "number between 1 and %2$d", numStripes, MAX_STRIPES));
    }
    if (numSSPerS < 1 || numSSPerS > MAX_SS_PER_STRIPE) {
      throw new IllegalArgumentException(String.format(
        "Invalid number of sub-stripes per stripe (%1$d): please specify a " +
        "number between 1 and %2$d", numSSPerS, MAX_SS_PER_STRIPE));
    }
  }

  /** Initializes ChunkIndex member variables. */
  private void initialize() {
    validate(numS, numSSPerS, columns);
    int numSS = numS * numSSPerS;
    final double stripeHeight = 180.0 / numS;
    ssHeight = 180.0 / numSS;
    if (stripeHeight < overlapDeg) {
      throw new IllegalArgumentException(String.format(
        "Stripe height %1$f is less than overlap %2$f: use coarser " +
        "partitioning parameters or decrease overlap",
        stripeHeight, overlapDeg));
    }
    invSSHeight = numSS / 180.0;
    if (numS > 1) {
      overlapSS = (int) Math.ceil(overlapDeg * invSSHeight);
    } else {
      overlapSS = 0;
    }
    if (overlapSS < 0 || numSSPerS  + 2 * overlapSS > MAX_SS_PER_STRIPE) {
      throw new IllegalArgumentException(String.format(
        "Invalid number of overlap sub-stripes per stripe (%1$d)",
        overlapSS, MAX_SS_PER_STRIPE));
    }
    population = new long[numS][];
    overlapPopulation = new long[numS][];
    ssLanes = new int[numSS];
    ssLanesPerC = new int[numSS];
    overlapLanes = new int[numSS];
    invLaneWidth = new double[numSS];
    for (int i = 0; i < numS; ++i) {
      double phiMin = ((double) i) / numS * 180.0 - 90.0;
      double phiMax = ((double) i + 1) / numS * 180.0 - 90.0;
      int numC = GeomUtils.chunksPerStripe(phiMin, phiMax, stripeHeight);
      if (numC <= 0 || numC > MAX_CHUNKS_PER_STRIPE) {
        throw new IllegalArgumentException(String.format(
          "Number of chunks per stripe (%1$d) not in range (0, %2$d]: " +
          "please use coarser partitioning parameters", numC,
          MAX_CHUNKS_PER_STRIPE));
      }
      population[i] = new long[numC];
      overlapPopulation[i] = new long[numC];
      for (int j = 0; j < numSSPerS; ++j) {
        int k = i * numSSPerS + j;
        double subPhiMin = ((double) k) / numSS * 180.0 - 90.0;
        double subPhiMax = ((double) k + 1) / numSS * 180.0 - 90.0;
        if (laneWidthDeg > 0.0) {
          int numLanes = GeomUtils.chunksPerStripe(subPhiMin, subPhiMax,
                                                   laneWidthDeg);
          // Make sure the # of lanes is a multiple of the # of chunks.
          int n = numLanes / numC;
          numLanes = n * numC;
          ssLanes[k] = numLanes;
          ssLanesPerC[k] = n;
          invLaneWidth[k] = ((double) numLanes) / 360.0;
          int ov = 0;
          if (overlapDeg > 0.0 && numC > 1) {
            double alpha = GeomUtils.maxAlpha(
              overlapDeg, Math.max(Math.abs(subPhiMin), Math.abs(subPhiMax)));
            ov = (int) Math.ceil((alpha * numLanes) / 360.0);
          }
          if (n + 2 * ov > MAX_LANES_PER_CSS) {
            throw new IllegalArgumentException(String.format(
              "Number of lanes (%1d + 2 * %2$d) per sub-stripe per chunk " +
              "exceeds %3$d: please use coarser partitioning parameters",
              n + 2 * ov, ov, MAX_LANES_PER_CSS));
          }
          overlapLanes[k] = ov;
        } else {
          ssLanes[k] = 1;
          overlapLanes[k] = 0;
        }
      }
    }
  }

  /**
   * Creates a new ChunkIndex with default parameters.
   */
  public ChunkIndex() {
    columns = new ArrayList<Column>();
    columns.add(new Column(DEF_UIK_COLUMN_NAME, "long", "", 0));
    columns.add(new Column(DEF_THETA_COLUMN_NAME, "double", "deg", 0));
    columns.add(new Column(DEF_PHI_COLUMN_NAME, "double", "deg", 0));
    initialize();
  }

  /**
   * Creates a new ChunkIndex, obtaining all index parameters from the
   * specified file.
   */
  public ChunkIndex(Path indexFile, FileSystem fs) throws IOException {
    FSDataInputStream inputStream = fs.open(indexFile);
    readFields(inputStream);
  }

  /**
   * Creates a new ChunkIndex with parameters extracted from the given
   * {@link org.apache.hadoop.conf.Configuration}. These are:
   * <ul>
   * <li>NUM_STRIPES_KEY</li>
   * <li>NUM_SUB_STRIPES_PER_STRIPE_KEY</li>
   * <li>LANE_WIDTH_DEG_KEY</li>
   * <li>OVERLAP_DEG_KEY</li>
   * <li>COLUMN_SPEC_KEY</li>
   * <li>UNIT_SPEC_KEY</li>
   * <li>TYPE_SPEC_KEY</li>
   * <li>ZIPPED_KEY</li>
   * <li>BINARY_KEY</li>
   * </ul>
   */
  public ChunkIndex(Configuration conf) {
    // Lookup basic index parameters
    numS = conf.getInt(NUM_STRIPES_KEY, DEF_NUM_STRIPES);
    numSSPerS = conf.getInt(NUM_SUB_STRIPES_PER_STRIPE_KEY,
                            DEF_NUM_SUB_STRIPES_PER_STRIPE);
    laneWidthDeg = Math.max(Double.parseDouble(conf.get(
      LANE_WIDTH_DEG_KEY, Double.toString(DEF_LANE_WIDTH_DEG))), 0.0);
    overlapDeg = Math.max(Double.parseDouble(conf.get(
      OVERLAP_DEG_KEY, Double.toString(DEF_OVERLAP_DEG))), 0.0);
    zipped = conf.getBoolean(ZIPPED_KEY, DEF_ZIPPED);
    binary = conf.getBoolean(BINARY_KEY, DEF_BINARY);
    // Lookup column info
    String[] types = null;
    String[] units = null;
    String columnSpec = conf.get(COLUMN_SPEC_KEY);
    String[] names = StringUtils.split(columnSpec);
    if (names.length < MIN_COLUMNS || names.length > MAX_COLUMNS) {
      throw new IllegalArgumentException(String.format(
        "ChunkIndex must contain between 3 and %1$d columns", MAX_COLUMNS));
    }
    // Process column type and unit specifications
    String typeSpec = conf.get(TYPE_SPEC_KEY);
    if (typeSpec == null) {
      types = new String[names.length];
    } else {
      types = Arrays.copyOf(StringUtils.split(typeSpec), names.length);
    }
    String unitSpec = conf.get(UNIT_SPEC_KEY);
    if (unitSpec == null) {
      units = new String[names.length];
    } else {
      units = Arrays.copyOf(StringUtils.split(unitSpec), names.length);
    }
    columns = new ArrayList<Column>(names.length);
    for (int i = 0; i < names.length; ++i) {
      columns.add(new Column(names[i], types[i], units[i], i));
    }
    // Initialize member variables and compute look up tables
    initialize();
  }

  /**
   * Stores the parameters of this ChunkIndex in the given
   * Configuration.
   */
  public void storeConfiguration(Configuration conf) {
    conf.setInt(NUM_STRIPES_KEY, numS);
    conf.setInt(NUM_SUB_STRIPES_PER_STRIPE_KEY, numSSPerS);
    conf.set(LANE_WIDTH_DEG_KEY, Double.toString(laneWidthDeg));
    conf.set(OVERLAP_DEG_KEY, Double.toString(overlapDeg));
    conf.setBoolean(ZIPPED_KEY, zipped);
    conf.setBoolean(BINARY_KEY, binary);
    StringBuilder columnSpec = new StringBuilder();
    StringBuilder typeSpec = new StringBuilder();
    StringBuilder unitSpec = new StringBuilder();
    boolean first = true;
    for (Column c : columns) {
      if (first) {
        first = false;
      } else {
        columnSpec.append(',');
        typeSpec.append(',');
        unitSpec.append(',');
      }
      columnSpec.append(StringUtils.escapeString(c.getName()));
      typeSpec.append(StringUtils.escapeString(c.getType()));
      unitSpec.append(StringUtils.escapeString(c.getUnits()));
    }
    conf.set(COLUMN_SPEC_KEY, columnSpec.toString());
    conf.set(TYPE_SPEC_KEY, typeSpec.toString());
    conf.set(UNIT_SPEC_KEY, unitSpec.toString());
  }

  @Override public void readFields(DataInput in) throws IOException {
    if (in.readLong() != MAGIC) {
      throw new IOException(
        "Could not deserialize ChunkIndex instance: missing magic bytes");
    }
    if (in.readInt() != VERSION) {
      throw new IOException("Could not deserialize ChunkIndex instance: " +
                            "file format version mismatch");
    }
    int tmpNumS = WritableUtils.readVInt(in);
    int tmpNumSSPerS = WritableUtils.readVInt(in);
    int tmpOverlapSS = WritableUtils.readVInt(in);
    double tmpSSHeight = in.readDouble();
    double tmpInvSSHeight = in.readDouble();
    double tmpLaneWidthDeg = in.readDouble();
    double tmpOverlapDeg = in.readDouble();
    boolean tmpZipped = in.readBoolean();
    boolean tmpBinary = in.readBoolean();
    int tmpNumColumns = WritableUtils.readVInt(in);
    List<Column> tmpColumns = new ArrayList<Column>(tmpNumColumns);
    for (int i = 0; i < tmpNumColumns; ++i) {
      Column c = new Column();
      c.readFields(in);
      tmpColumns.add(c);
    }
    // Validate metadata
    validate(tmpNumS, tmpNumSSPerS, tmpColumns);
    if (tmpOverlapSS < 0 ||
        tmpNumSSPerS  + 2 * tmpOverlapSS > MAX_SS_PER_STRIPE) {
      throw new IllegalArgumentException(String.format(
        "Invalid number of overlap sub-stripes per stripe (%1$d)",
        tmpOverlapSS, MAX_SS_PER_STRIPE));
    }
    // Read in lookup tables; perform basic sanity checks
    long[][] tmpPopulation = new long[tmpNumS][];
    long[][] tmpOverlapPopulation = new long[tmpNumS][];
    final int numSS = tmpNumS * tmpNumSSPerS;
    int[] tmpSSLanes = new int[numSS];
    int[] tmpSSLanesPerC = new int[numSS];
    int[] tmpOverlapLanes = new int[numSS];
    double[] tmpInvLaneWidth = new double[numSS];
    for (int i = 0; i < tmpNumS; ++i) {
      int numC = WritableUtils.readVInt(in);
      if (numC > MAX_CHUNKS_PER_STRIPE) {
        throw new IOException(String.format(
          "Number of chunks per stripe (%1$d) exceeds %2$d",
          numC, MAX_CHUNKS_PER_STRIPE));
      }
      tmpPopulation[i] = new long[numC];
      tmpOverlapPopulation[i] = new long[numC];
      for (int j = 0; j < numC; ++j) {
        long pop = WritableUtils.readVLong(in);
        long overlapPop = WritableUtils.readVLong(in);
        if (pop < 0 || overlapPop < 0) {
          throw new IOException("Negative population count in ChunkIndex");
        }
        tmpPopulation[i][j] = pop;
        tmpOverlapPopulation[i][j] = overlapPop;
      }
    }
    for (int i = 0; i < tmpNumS; ++i) {
      int c = tmpPopulation[i].length;
      int k = i * tmpNumSSPerS;
      for (int j = 0; j < tmpNumSSPerS; ++j) {
        int sslpc = WritableUtils.readVInt(in);
        int ol = WritableUtils.readVInt(in);
        int l = sslpc * c;
        if (sslpc <= 0 || ol < 0) {
          throw new IOException(
            "Lane count in ChunkIndex is negative or zero");
        }
        if (sslpc + 2 * ol > MAX_LANES_PER_CSS) {
          throw new IOException(String.format(
            "Number of lanes (%1d + 2 * %2$d) per sub-stripe per chunk " +
            "exceeds %2$d", sslpc + 2 * ol, MAX_LANES_PER_CSS));
        }
        tmpSSLanesPerC[k + j] = sslpc;
        tmpSSLanes[k + j] = l;
        tmpOverlapLanes[k + j] = ol;
        tmpInvLaneWidth[k + j] = in.readDouble();
      }
    }
    // No exceptions can be thrown past this point, so initialize
    // member variables
    numS = tmpNumS;
    numSSPerS = tmpNumSSPerS;
    overlapSS = tmpOverlapSS;
    ssHeight = tmpSSHeight;
    invSSHeight = tmpInvSSHeight;
    laneWidthDeg = Math.max(tmpLaneWidthDeg, 0.0);
    overlapDeg = Math.max(tmpOverlapDeg, 0.0);
    zipped = tmpZipped;
    binary = tmpBinary;
    columns = tmpColumns;
    population = tmpPopulation;
    overlapPopulation = tmpOverlapPopulation;
    ssLanes = tmpSSLanes;
    ssLanesPerC = tmpSSLanesPerC;
    overlapLanes = tmpOverlapLanes;
    invLaneWidth = tmpInvLaneWidth;
  }

  @Override public void write(DataOutput out) throws IOException {
    out.writeLong(MAGIC);
    out.writeInt(VERSION);
    WritableUtils.writeVInt(out, numS);
    WritableUtils.writeVInt(out, numSSPerS);
    WritableUtils.writeVInt(out, overlapSS);
    out.writeDouble(ssHeight);
    out.writeDouble(invSSHeight);
    out.writeDouble(laneWidthDeg);
    out.writeDouble(overlapDeg);
    out.writeBoolean(zipped);
    out.writeBoolean(binary);
    WritableUtils.writeVInt(out, columns.size());
    for (int i = 0; i < columns.size(); ++i) {
      columns.get(i).write(out);
    }
    for (int i = 0; i < numS; ++i) {
      WritableUtils.writeVInt(out, population[i].length);
      for (int j = 0; j < population[i].length; ++j) {
        WritableUtils.writeVLong(out, population[i][j]);
        WritableUtils.writeVLong(out, overlapPopulation[i][j]);
      }
    }
    int numSS = numS * numSSPerS;
    for (int i = 0; i < numSS; ++i) {
      WritableUtils.writeVInt(out, ssLanesPerC[i]);
      WritableUtils.writeVInt(out, overlapLanes[i]);
      out.writeDouble(invLaneWidth[i]);
    }
  }

  public int getNumStripes() {
    return numS;
  }

  public int getNumChunks(int stripe) {
    return population[stripe].length;
  }

  public int getNumSubStripesPerStripe() {
    return numSSPerS;
  }

  public int getNumSubStripes() {
    return numS * numSSPerS;
  }

  public int getNumOverlapSubStripes() {
    return overlapSS;
  }

  public int getNumLanes(int stripe, int subStripe) {
    return ssLanes[stripe * numSSPerS + subStripe];
  }

  public int getNumLanesPerChunk(int stripe, int subStripe) {
    return ssLanesPerC[stripe * numSSPerS + subStripe];
  }

  public int getNumOverlapLanes(int stripe, int subStripe) {
    return overlapLanes[stripe * numSSPerS + subStripe];
  }

  public double getSubStripeHeightDeg() {
    return ssHeight;
  }

  public double getOverlapDeg() {
    return overlapDeg;
  }

  public boolean isZipped() {
    return zipped;
  }

  public boolean isBinary() {
    return binary;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void setPopulation(int stripe, int chunk, long pop) {
    population[stripe][chunk] = pop;
  }

  public void setOverlapPopulation(int stripe, int chunk, long pop) {
    overlapPopulation[stripe][chunk] = pop;
  }

  @Override public String toString() {
    return JSONOutput.toString(this);
  }

  @Override public void writeJSON(JSONOutput out) throws IOException {
    writeJSON(out, false);
  }

  public void writeJSON(JSONOutput out, boolean verbose) throws IOException {
    out.object();
    out.pair("numStripes", numS);
    out.pair("numSubStripesPerStripe", numSSPerS);
    out.pair("subStripeHeight", ssHeight);
    out.pair("inverseSubStripeHeight", invSSHeight);
    out.pair("laneWidthDeg", laneWidthDeg);
    out.pair("overlapDeg", overlapDeg);
    out.pair("zipped", zipped);
    out.pair("binary", binary);
    // output columns
    out.key("columns");
    out.object();
    for (Column c : columns) {
      out.pair(c.getName(), c);
    }
    out.finish();
    if (verbose) {
      // output stripes
      out.key("stripes");
      out.array();
      for (int s = 0; s < numS; ++s) {
        out.object();
        out.key("chunkPopulation").array(population[s]);
        out.key("chunkOverlapPopulation").array(overlapPopulation[s]);
        out.key("subStripes");
        out.object();
        int from = s * numSSPerS;
        int to = from + numSSPerS;
        out.key("lanes").array(ssLanes, from, to);
        out.key("lanesPerChunk").array(ssLanesPerC, from, to);
        out.key("overlapLanes").array(overlapLanes, from, to);
        out.key("inverseLaneWidth").array(invLaneWidth, from, to);
        out.finish(2);
      }
      out.finish();
    }
    out.finish();
  }

  @Override public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ChunkIndex)) {
      return false;
    }
    ChunkIndex index = (ChunkIndex) obj;
    if (numS != index.numS ||
        numSSPerS != index.numSSPerS) {
      return false;
    }
    if (Math.abs(overlapDeg - index.overlapDeg) > 2.777778e-10 ||
        zipped != index.zipped ||
        binary != index.binary) {
      return false;
    }
    if (!columns.equals(index.columns)) {
      return false;
    }
    if (!Arrays.equals(ssLanes, index.ssLanes) ||
        !Arrays.equals(ssLanesPerC, index.ssLanesPerC) ||
        !Arrays.equals(overlapLanes, index.overlapLanes)) {
      return false;
    }
    return true;
  }

  @Override public int hashCode() {
    int sh = (numS << 16) ^ numSSPerS;
    int ah = Arrays.hashCode(ssLanes) ^ Arrays.hashCode(ssLanesPerC) ^
             Arrays.hashCode(overlapLanes);
    return columns.hashCode() ^ sh ^ ah;
  }

  /**
   * Zeroes the population counts of this ChunkIndex.
   */
  public void clear() {
    for (long[] pop : population) {
      Arrays.fill(pop, 0);
    }
    for (long[] pop : overlapPopulation) {
      Arrays.fill(pop, 0);
    }
  }

  /**
   * Merges the contents of the given ChunkIndex with this one. Both
   * ChunkIndex objects must be compatible; that is, they must have been
   * created with the same chunking parameters and columns. Merging consists
   * of adding the population counts of <code>index</code> to this ChunkIndex,
   * and of setting the maximum width of each column to the maximum in both
   * ChunkIndex objects.
   *
   * Note that the current implementation assumes that the point sets in
   * both chunk indexes are disjoint.
   */
  public void merge(ChunkIndex index) {
    if (index == this) {
      return;
    }
    if (!equals(index)) {
      throw new IllegalArgumentException(
        "Cannot merge incompatible chunk-indexes!");
    }
    for (int i = 0; i < columns.size(); ++i) {
      Column c = columns.get(i);
      c.setWidth(Math.max(c.getWidth(), index.columns.get(i).getWidth()));
    }
    for (int i = 0; i < population.length; ++i) {
      for (int j = 0; j < population[i].length; ++j) {
        population[i][j] += index.population[i][j];
        overlapPopulation[i][j] += index.overlapPopulation[i][j];
      }
    }
  }

  /**
   * Return the primary (non-overlap) location of the given point.
   *
   * @param uik       Unique integer key for the point to locate.
   * @param thetaDeg  longitude angle of the point to locate in degrees.
   * @param phiDeg    latitude angle of the point to locate in degrees.
   *
   * @return  The location of the given point.
   */
  public Location getLocation(long uik, double thetaDeg, double phiDeg) {
    int ss = (int) Math.floor((phiDeg + 90.0) * invSSHeight);
    if (ss >= invLaneWidth.length) {
      ss = invLaneWidth.length - 1;
    }
    int l = (int) Math.floor(thetaDeg * invLaneWidth[ss]);
    if (l >= ssLanes[ss]) {
      l = ssLanes[ss] - 1;
    }
    int s = ss / numSSPerS;
    int ssc = ss % numSSPerS;
    int c = l / ssLanesPerC[ss];
    int lc = l % ssLanesPerC[ss];
    long loc = (((long) s) << Location.STRIPE_SHIFT) |
               (((long) c) << Location.CHUNK_SHIFT) |
               (ssc << Location.SUB_STRIPE_SHIFT) |
               (lc + overlapLanes[ss]);
    return new Location(loc, phiDeg, uik);
  }

  /**
   * Stores the primary and overlap locations within the sub-stripe
   * <code>ss</code> for the given position in <code>locations</code>.
   * The <code>overlap</code> argument can be used to mark all generated
   * locations as overlap locations.
   */
  private void getSSLocations(int ss, int s, int ssc, int c, int lc,
                              double phi, long uik, List<Location> locs) {
    long loc = (((long) s) << Location.STRIPE_SHIFT) |
               (((long) c) << Location.CHUNK_SHIFT) |
               (ssc << Location.SUB_STRIPE_SHIFT) |
               (lc + overlapLanes[ss]);
    locs.add(new Location(loc, phi, uik));
    if (overlapDeg <= 0.0 || population[s].length == 1) {
      return;
    }
    // compute overlap locations
    if (lc < overlapLanes[ss]) {
      // position is in overlap region of chunk to the left
      loc = (((long) s) << Location.STRIPE_SHIFT) |
            (ssc << Location.SUB_STRIPE_SHIFT);
      if (c == 0) {
        loc |= ((long) population[s].length - 1) << Location.CHUNK_SHIFT;
      } else {
        loc |= ((long) c - 1) << Location.CHUNK_SHIFT;
      }
      loc |= ssLanesPerC[ss] + overlapLanes[ss] + lc;
      locs.add(new Location(loc, phi, uik));
    }
    if (lc >= ssLanesPerC[ss] - overlapLanes[ss]) {
      // position is in overlap region of chunk to the right
      loc = (((long) s) << Location.STRIPE_SHIFT) |
            (ssc << Location.SUB_STRIPE_SHIFT);
      if (c != population[s].length - 1) {
        loc |= ((long) c + 1) << Location.CHUNK_SHIFT;
      }
      loc |= lc - ssLanesPerC[ss] + overlapLanes[ss];
      locs.add(new Location(loc, phi, uik));
    }
  }

  /**
   * Stores all index locations for the given point; if the index
   * overlap is 0, then points are mapped to a single, primary
   * non-overlap location.
   *
   * @param uik       Unique integer key for the point to locate.
   * @param thetaDeg  Longitude angle of the point to locate in degrees.
   * @param phiDeg    Latitude angle of the point to locate in degrees.
   * @param locations The list to store locations in.
   */
  public void getAllLocations(long uik, double thetaDeg, double phiDeg,
                              List<Location> locations) {
    int ss = (int) Math.floor((phiDeg + 90.0) * invSSHeight);
    if (ss >= invLaneWidth.length) {
      ss = invLaneWidth.length - 1;
    }
    int l = (int) Math.floor(thetaDeg * invLaneWidth[ss]);
    if (l >= ssLanes[ss]) {
      l = ssLanes[ss] - 1;
    }
    int s = ss / numSSPerS;
    int ssc = ss % numSSPerS;
    int c = l / ssLanesPerC[ss];
    int lc = l % ssLanesPerC[ss];
    // Get primary location and overlap locations in this sub-stripe
    getSSLocations(ss, s, ssc + (s > 0 ? overlapSS : 0), c, lc,
                   phiDeg, uik, locations);
    if (overlapDeg > 0.0) {
      double sPhiMin = (s * numSSPerS) * ssHeight - 90.0;
      double sPhiMax = (s * numSSPerS + numSSPerS) * ssHeight - 90.0;
      if (s > 0 && phiDeg < sPhiMin + overlapDeg) {
        // position is in overlap region of stripe below; use lane
        // count and width from top-most sub-stripe of stripe below
        int oss = s * numSSPerS - 1;
        l = (int) Math.floor(thetaDeg * invLaneWidth[oss]);
        if (l >= ssLanes[oss]) {
          l = ssLanes[oss] - 1;
        }
        int ossc = (s > 1 ? overlapSS : 0) + numSSPerS + ssc;
        c = l / ssLanesPerC[oss];
        lc = l % ssLanesPerC[oss];
        getSSLocations(oss, s - 1, ossc, c, lc, phiDeg, uik, locations);
      }
      if (s < numS - 1 && phiDeg >= sPhiMax - overlapDeg) {
        // position is in overlap region of stripe above; use lane
        // count and width from bottom-most sub-stripe of stripe above
        int oss = s * numSSPerS + numSSPerS;
        l = (int) Math.floor(thetaDeg * invLaneWidth[oss]);
        if (l >= ssLanes[oss]) {
          l = ssLanes[oss] - 1;
        }
        int ossc = numSSPerS - ssc - 1;
        c = l / ssLanesPerC[oss];
        lc = l % ssLanesPerC[oss];
        getSSLocations(oss, s + 1, ossc, c, lc, phiDeg, uik, locations);
      }
    }
  }

  /**
   * Return all index locations for the given point; if the index
   * overlap is 0, then points are mapped to a single, primary
   * non-overlap location.
   *
   * @param uik       Unique integer key for the point to locate.
   * @param thetaDeg  Longitude angle of the point to locate in degrees.
   * @param phiDeg    Latitude angle of the point to locate in degrees.
   *
   * @return  The list of index locations for the given point.
   */
  public List<Location> getAllLocations(long uik, double thetaDeg,
                                        double phiDeg) {
    List<Location> locations = new ArrayList<Location>();
    getAllLocations(uik, thetaDeg, phiDeg, locations);
    return locations;
  }

  private static byte[] extractField(byte[] buf, int from, int to,
                                     byte escape) {
    if (to <= from) {
      return new byte[0];
    }
    int len = to - from;
    for (int i = from; i < to; ++i) {
      if (buf[i] == 0) {
        throw new IllegalArgumentException(
          "Field contains embedded null character(s)");
      }
      if (buf[i] == escape) {
        if (i == to - 1) {
          throw new IllegalArgumentException(
            "Field terminated by escape character");
        }
        --len;
        ++i;
      }
    }
    byte[] field = new byte[len];
    for (int i = from, j = 0; i < to; ++i, ++j) {
      if (buf[i] == escape) {
        field[j] = buf[i + 1];
        ++i;
      } else {
        field[j] = buf[i];
      }
    }
    return field;
  }

  /**
   * Splits a text record into fields using the given delimiter and
   * delimiter escape character.
   *
   * @param record    The record to split into fields.
   * @param delimiter The delimiter byte to split
   *                  <code>record</code> with.
   * @param escape    The escape byte to use for occurrences of
   *                  <code>delimiter</code> inside fields.
   * @param ignoreTrailing  If true, a trailing delimiter in the record
   *                        is ignored. Otherwise, a trailing delimiter
   *                        indicates a trailing null value.
   *
   * @return  A list of field values.
   */
  public static List<byte[]> split(Text record, byte delimiter,
                                   byte escape, boolean ignoreTrailing) {
    List<byte[]> fields = new ArrayList<byte[]>();
    split(record, delimiter, escape, ignoreTrailing, fields);
    return fields;
  }

  /**
   * Splits a text record into fields using the given delimiter and
   * delimiter escape character.
   *
   * @param record    The record to split into fields.
   * @param delimiter The delimiter byte to split
   *                  <code>record</code> with.
   * @param escape    The escape byte to use for occurrences of
   *                  <code>delimiter</code> inside fields.
   * @param ignoreTrailing  If true, a trailing delimiter in the record
   *                        is ignored. Otherwise, a trailing delimiter
   *                        indicates a trailing null value.
   * @param fields    The list to store field values in.
   */
  public static void split(Text record, byte delimiter, byte escape,
                           boolean ignoreTrailing, List<byte[]> fields) {
    byte[] buf = record.getBytes();
    int len = record.getLength();
    int last = 0;
    for (int i = 0; i < len; ++i) {
      if (buf[i] == delimiter && (i == 0 || buf[i - 1] != escape)) {
        fields.add(extractField(buf, last, i, escape));
        last = i + 1;
      }
    }
    if (last < len) {
      fields.add(extractField(buf, last, len, escape));
    } else if (!ignoreTrailing) {
      fields.add(new byte[0]);
    }
  }

  /**
   * Splits a null delimited text record into fields.
   * Fields must not contain embedded null characters.
   *
   * @param record  The record to split into fields.
   * @return  A list of field values.
   */
  public static List<byte[]> split(Text record) {
    List<byte[]> fields = new ArrayList<byte[]>();
    split(record, fields);
    return fields;
  }

  /**
   * Splits a null delimited text record into fields.
   * Fields must not contain embedded null characters.
   *
   * @param record  The record to split into fields.
   * @param fields  The list to store field values in.
   */
  public static void split(Text record, List<byte[]> fields) {
    byte[] buf = record.getBytes();
    int len = record.getLength();
    int last = 0;
    for (int i = 0; i < len; ++i) {
      if (buf[i] == 0) {
        fields.add(Arrays.copyOfRange(buf, last, i));
        last = i + 1;
      }
    }
    fields.add(Arrays.copyOfRange(buf, last, len));
  }

  /**
   * Returns the path of the specified chunk relative to the directory
   * containing the chunk index file.
   */
  public static Path chunkPath(int stripe, int chunk) {
    return new Path(String.format("stripe_%1$03d/chunk_%1$03d_%2$03d.cf",
                                  stripe, chunk));
  }

  /**
   * Returns the path of the specified chunk relative to the directory
   * containing the chunk index file. The given stripe and chunk numbers
   * are verified to lie within this ChunkIndex.
   */
  public Path getChunkPath(int stripe, int chunk) {
    if (stripe < 0 || stripe >= numS) {
      throw new IndexOutOfBoundsException("Stripe number is out of range");
    }
    int numC = population[stripe].length;
    if (chunk < 0 || chunk >= numC) {
      throw new IndexOutOfBoundsException("Chunk number is out of range");
    }
    return chunkPath(stripe, chunk);
  }

  /**
   * Returns the path of a chunk index file to be stored in the given
   * directory.
   *
   * @param dir   The chunk index directory
   * @param merge <code>true</code> if the chunk index is to be created by
   *              merging other chunk indexes.
   */
  public static Path indexPath(Path dir, boolean merge) {
    return new Path(dir, merge ? "merge.ci" : "index.ci");
  }
}

