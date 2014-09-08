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



package ipac.stk.mapreduce.job;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;

import ipac.stk.io.ChunkFile;
import ipac.stk.io.ChunkIndex;
import ipac.stk.math.SphericalCoords;


/**
 * Map/reduce job that reads in ASCII (CSV, TSV, pipe-delimited) files and
 * splits them into a set of spatial chunk files, each further split into
 * fixed height latitude sub-stripes composed of fixed width in longitude
 * lanes.
 *
 * @see ipac.stk.io.ChunkIndex
 * @see ipac.stk.io.ChunkFile
 */
public final class CreateChunks {

  private CreateChunks() { }

  public static final String DELIMITER_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.delimiter";
  public static final String DEF_DELIMITER = "|";
  public static final String ESCAPE_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.escape";
  public static final String DEF_ESCAPE = "\\";
  public static final String IGNORE_TRAILING_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.ignore-trailing";
  public static final boolean DEF_IGNORE_TRAILING = false;
  public static final String WRAP_THETA_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.wrap-theta";
  public static final boolean DEF_WRAP_THETA = false;
  public static final String OUT_COLUMNS_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.out-columns";
  public static final String NUM_COLUMNS_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.num-columns";
  public static final String UIK_COLUMN_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.uik-column";
  public static final int DEF_UIK_COLUMN_INDEX = 0;
  public static final String THETA_COLUMN_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.theta-column";
  public static final int DEF_THETA_COLUMN_INDEX = 1;
  public static final String PHI_COLUMN_KEY =
    "ipac.stk.mapreduce.job.CreateChunks.phi-column";
  public static final int DEF_PHI_COLUMN_INDEX = 2;


  /**
   * Extracts a delimiter or escape byte from a String.
   */
  public static byte getFormatByte(String key, String value) {
    if (value.length() != 1) {
      throw new IllegalArgumentException(String.format(
        "%s: value must be a single character, got: %s", key, value));
    }
    char c = value.charAt(0);
    if (c < 1 || c == '\r' || c == '\n' || c > 0x7f) {
      throw new IllegalArgumentException(String.format(
        "%s: invalid field delimiter %02x (hex)", key, (int) c));
    }
    return (byte) c;
  }

  /**
   * Extracts output column indexes from the given Configuration.
   */
  public static int[] getOutputColumnIndexes(Configuration conf) {
    String[] cols = conf.getStrings(OUT_COLUMNS_KEY);
    int[] indexes = new int[cols.length];
    for (int i = 0; i < cols.length; ++i) {
      indexes[i] = Integer.parseInt(cols[i]);
    }
    return indexes;
  }

  /**
   * Partitions based on the chunk of the location.
   */
  public static final class ChunkPartitioner
    extends Partitioner<ChunkIndex.Location, Writable> {
    @Override public int getPartition(ChunkIndex.Location location,
                                      Writable value, int numPartitions) {
      return Math.abs(location.getChunkId() * 127) % numPartitions;
    }
  }


  /**
   * Parses input lines and maps them to their locations. Only the
   * the unique integer key and longitude / latitude angle columns
   * are sent onwards to reducers.
   */
  public static final class BinaryChunkMapper
    extends Mapper<LongWritable, Text, ChunkIndex.Location, LongWritable> {

    private ChunkIndex index = null;
    private LongWritable theta = new LongWritable();
    private List<byte[]> fields = new ArrayList<byte[]>();
    private List<ChunkIndex.Location> locs =
      new ArrayList<ChunkIndex.Location>();
    private int numColumns = 0;
    private byte delimiter = 0;
    private byte escape = 0;;
    private boolean ignoreTrailing = DEF_IGNORE_TRAILING;
    private boolean wrapTheta = DEF_WRAP_THETA;
    private int uikColumn = -1;
    private int thetaColumn = -1;
    private int phiColumn = -1;

    @Override protected void setup(Context context)
      throws IOException, InterruptedException {

      Configuration conf = context.getConfiguration();
      numColumns = Integer.parseInt(conf.get(NUM_COLUMNS_KEY));
      index = new ChunkIndex(conf);
      delimiter = getFormatByte(DELIMITER_KEY,
                                conf.get(DELIMITER_KEY, DEF_DELIMITER));
      escape = getFormatByte(ESCAPE_KEY, conf.get(ESCAPE_KEY, DEF_ESCAPE));
      ignoreTrailing = conf.getBoolean(IGNORE_TRAILING_KEY,
                                       DEF_IGNORE_TRAILING);
      wrapTheta = conf.getBoolean(WRAP_THETA_KEY, DEF_WRAP_THETA);
      uikColumn = conf.getInt(UIK_COLUMN_KEY, DEF_UIK_COLUMN_INDEX);
      thetaColumn = conf.getInt(THETA_COLUMN_KEY, DEF_THETA_COLUMN_INDEX);
      phiColumn = conf.getInt(PHI_COLUMN_KEY, DEF_PHI_COLUMN_INDEX);
    }

    @SuppressWarnings("unchecked") @Override
    public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

      // extract fields from input record
      fields.clear();
      ChunkIndex.split(value, delimiter, escape, ignoreTrailing, fields);
      if (fields.size() != numColumns) {
        throw new IOException(String.format(
          "Input record contains %d fields, expected %d.",
          fields.size(), numColumns));
      }
      // extract uik, theta, and phi values
      byte[] uikF = fields.get(uikColumn);
      byte[] thetaF = fields.get(thetaColumn);
      byte[] phiF = fields.get(phiColumn);
      if (uikF.length == 0 || thetaF.length == 0 || phiF.length == 0) {
        throw new IOException("Unique integer key and longitude/latitude " +
                              "angle columns must not be NULL.");
      }
      long uik = Long.parseLong(Text.decode(uikF));
      double thetaDeg = Double.parseDouble(Text.decode(thetaF));
      if (thetaDeg < 0.0 || thetaDeg >= 360.0) {
        if (wrapTheta) {
          thetaDeg = SphericalCoords.reduceTheta(thetaDeg);
        } else {
          throw new IOException("Longitude angle not in range [0, 360) deg");
        }
      }
      double phiDeg = Double.parseDouble(Text.decode(phiF));
      if (phiDeg < -90.0 || phiDeg > 90.0) {
        throw new IOException("Latitude angle not in range [-90, 90] deg");
      }
      // compute index locations
      theta.set(SphericalCoords.thetaToLong(thetaDeg));
      locs.clear();
      index.getAllLocations(uik, thetaDeg, phiDeg, locs);
      // write output
      for (ChunkIndex.Location loc : locs) {
        context.write(loc, theta);
      }
    }
  }


  /**
   * Parses input lines and maps them to their locations. Only requested
   * output columns are sent onwards to reducers.
   */
  public static final class TextChunkMapper
    extends Mapper<LongWritable, Text, ChunkIndex.Location, Text> {

    private ChunkIndex index = null;
    private List<byte[]> fields = new ArrayList<byte[]>();
    private List<ChunkIndex.Location> locs =
      new ArrayList<ChunkIndex.Location>();
    private int numColumns = 0;
    private int[] columns = null;
    private byte delimiter = 0;
    private byte escape = 0;
    private boolean ignoreTrailing = DEF_IGNORE_TRAILING;
    private boolean wrapTheta = DEF_WRAP_THETA;
    private int uikColumn = -1;
    private int thetaColumn = -1;
    private int phiColumn = -1;

    @Override protected void setup(Context context)
      throws IOException, InterruptedException {

      Configuration conf = context.getConfiguration();
      numColumns = Integer.parseInt(conf.get(NUM_COLUMNS_KEY));
      index = new ChunkIndex(conf);
      columns = getOutputColumnIndexes(conf);
      delimiter = getFormatByte(DELIMITER_KEY,
                                conf.get(DELIMITER_KEY, DEF_DELIMITER));
      escape = getFormatByte(ESCAPE_KEY, conf.get(ESCAPE_KEY, DEF_ESCAPE));
      ignoreTrailing = conf.getBoolean(IGNORE_TRAILING_KEY,
                                       DEF_IGNORE_TRAILING);
      wrapTheta = conf.getBoolean(WRAP_THETA_KEY, DEF_WRAP_THETA);
      uikColumn = conf.getInt(UIK_COLUMN_KEY, DEF_UIK_COLUMN_INDEX);
      thetaColumn = conf.getInt(THETA_COLUMN_KEY, DEF_THETA_COLUMN_INDEX);
      phiColumn = conf.getInt(PHI_COLUMN_KEY, DEF_PHI_COLUMN_INDEX);
    }

    @SuppressWarnings("unchecked") @Override
    public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

      // extract fields from input record
      fields.clear();
      ChunkIndex.split(value, delimiter, escape, ignoreTrailing, fields);
      if (fields.size() != numColumns) {
        throw new IOException(String.format(
          "Input record contains %d fields, expected %d.",
          fields.size(), numColumns));
      }
      // extract uik, theta, and phi values
      byte[] uikF = fields.get(uikColumn);
      byte[] thetaF = fields.get(thetaColumn);
      byte[] phiF = fields.get(phiColumn);
      if (uikF.length == 0 || thetaF.length == 0 || phiF.length == 0) {
        throw new IOException("Unique integer key and longitude/latitude " +
                              "angle columns cannot be NULL.");
      }
      long uik = Long.parseLong(Text.decode(uikF));
      double theta = Double.parseDouble(Text.decode(thetaF));
      if (theta < 0.0 || theta >= 360.0) {
        if (wrapTheta) {
          theta = SphericalCoords.reduceTheta(theta);
        } else {
          throw new IOException("Longitude angle not in range [0, 360] deg");
        }
      }
      double phi = Double.parseDouble(Text.decode(phiF));
      if (phi < -90.0 || phi > 90.0) {
        throw new IOException("Latitude angle not in range [-90, 90] deg");
      }
      // compute index locations of input point
      locs.clear();
      index.getAllLocations(uik, theta, phi, locs);
      // join output fields into a null delimited text record
      int len = columns.length;
      for (int i : columns) {
        len += fields.get(i).length;
      }
      byte[] buf = new byte[len];
      int dest = 0;
      for (int i : columns) {
        byte[] f = fields.get(i);
        System.arraycopy(f, 0, buf, dest, f.length);
        dest += f.length + 1;
      }
      Text outValue = new Text(buf);
      // write output
      for (ChunkIndex.Location loc : locs) {
        context.write(loc, outValue);
      }
    }
  }


  /**
   * Collects records falling into chunks and writes them to disk
   * as {@link ipac.stk.io.ChunkFile binary chunk files}.
   */
  public static final class BinaryChunkReducer extends
    Reducer<ChunkIndex.Location, LongWritable, NullWritable, NullWritable> {

    private ChunkIndex index = null;
    private ChunkFile file = null;
    private ChunkFile.BinaryRecordWriter writer = null;
    private int[] widths = null;
    private Path outputDir = null;
    private FileSystem fs = null;
    private int chunkId = Integer.MIN_VALUE;

    @Override protected void setup(Context context)
      throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      index = new ChunkIndex(conf);
      outputDir = FileOutputFormat.getWorkOutputPath(context);
      fs = outputDir.getFileSystem(conf);
      widths = new int[3];
      widths[1] = 24;
      widths[2] = 24;
    }

    @Override public void reduce(ChunkIndex.Location location,
                                 Iterable<LongWritable> values,
                                 Context context)
      throws IOException, InterruptedException {

      if (location.getChunkId() != chunkId) {
        // open a new chunk file
        if (writer != null) {
          writer.close();
        }
        int stripe = location.getStripe();
        int chunk = location.getChunk();
        chunkId = location.getChunkId();
        file = new ChunkFile(outputDir, fs, index, stripe, chunk);
        writer = (ChunkFile.BinaryRecordWriter) file.getRecordWriter();
      }
      for (LongWritable value : values) {
        long theta = value.get();
        widths[0] = Math.max(widths[0], Long.toString(theta).length());
        writer.append(location, theta);
      }
    }

    @Override protected void cleanup(Context context)
      throws IOException, InterruptedException {

      if (writer != null) {
        writer.close();
      }
      writer = null;
      file = null;
      // write out a file containing the maximum column widths
      // encountered by this reducer
      Configuration conf = context.getConfiguration();
      String fileName = String.format(
        "column-widths-%05d", conf.getInt("mapred.task.partition", 0));
      Path path = new Path(outputDir, fileName);
      FSDataOutputStream stream = fs.create(path, false);
      try {
        stream.writeInt(widths.length);
        for (int w : widths) {
          stream.writeInt(w);
        }
      } finally {
        stream.close();
      }
    }
  }


  /**
   * Collects all records falling into chunks and writes them to disk
   * as {@link ipac.stk.io.ChunkFile text chunk files}.
   */
  public static final class TextChunkReducer
    extends Reducer<ChunkIndex.Location, Text, NullWritable, NullWritable> {

    private ChunkIndex index = null;
    private ChunkFile file = null;
    private ChunkFile.TextRecordWriter writer = null;
    private int[] widths = null;
    private Path outputDir = null;
    private FileSystem fs = null;
    private int chunkId = Integer.MIN_VALUE;

    @Override protected void setup(Context context)
      throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      index = new ChunkIndex(conf);
      outputDir = FileOutputFormat.getWorkOutputPath(context);
      fs = outputDir.getFileSystem(conf);
      widths = new int[getOutputColumnIndexes(conf).length];
    }

    @Override public void reduce(ChunkIndex.Location location,
                                 Iterable<Text> values,
                                 Context context)
      throws IOException, InterruptedException {

      if (location.getChunkId() != chunkId) {
        // open a new chunk file
        if (writer != null) {
          writer.close();
        }
        chunkId = location.getChunkId();
        int stripe = location.getStripe();
        int chunk = location.getChunk();
        file = new ChunkFile(outputDir, fs, index, stripe, chunk);
        writer = (ChunkFile.TextRecordWriter) file.getRecordWriter();
      }
      for (Text value : values) {
        writer.append(location, value);
        byte[] buf = value.getBytes();
        int len = value.getLength();
        int j = 0;
        int last = 0;
        for (int i = 0; i < len; ++i) {
          if (buf[i] == 0) {
            widths[j] = Math.max(widths[j], i - last);
            last = i + 1;
            ++j;
          }
        }
      }
    }

    @Override protected void cleanup(Context context)
      throws IOException, InterruptedException {

      if (writer != null) {
        writer.close();
      }
      writer = null;
      file = null;
      // write out a file containing the maximum column widths
      // encountered by this reducer
      Configuration conf = context.getConfiguration();
      String fileName = String.format(
        "column-widths-%05d", conf.getInt("mapred.task.partition", 0));
      Path path = new Path(outputDir, fileName);
      FSDataOutputStream stream = fs.create(path, false);
      try {
        stream.writeInt(widths.length);
        for (int w : widths) {
          stream.writeInt(w);
        }
      } finally {
        stream.close();
      }
    }
  }


  /**
   * Create and return command line option set.
   */
  private static Options buildOptions() {
    Options opts = new Options();
    Option help = OptionBuilder
      .withLongOpt("help")
      .withDescription("Print usage information")
      .create('h');
    Option fs = OptionBuilder
      .hasArg()
      .withDescription(
        "Specifies an HDFS name node; must be either 'file:///' or " +
        "a string of the form 'hdfs://<host>:<port>'.")
      .create("fs");
    Option jt = OptionBuilder
      .hasArg()
      .withDescription(
        "Specifies a Hadoop map-reduce job tracker; must be either " +
        "'local' or a string of the form '<host>:<port>'.")
      .create("jt");
    Option property = OptionBuilder.withArgName("property=value")
      .hasArgs()
      .withDescription("use value for given property")
      .create('D');
    Option numStripes = OptionBuilder
      .withArgName("S")
      .hasArg()
      .withLongOpt("num-stripes")
      .withDescription(
        "The number of equal-height latitude stripes to spread points " +
        "across.  Each stripe is further divided into a number of " +
        "equal-width (in longitude) chunks, such that each chunk is at " +
        "least as wide as it is high.  Each chunk is stored in a separate " +
        "file named <output directory>/sSSSS_cCCCC, where SSSS and CCCC " +
        "are stripe and chunk numbers respectively.  By default, " +
        ChunkIndex.DEF_NUM_STRIPES + " stripes are used.")
      .create('S');
    Option numSubStripes = OptionBuilder
      .withArgName("SS")
      .hasArg()
      .withLongOpt("num-ss-per-stripe")
      .withDescription(
        "Number of sub-stripes per stripe.  Each chunk is subdivided into " +
        "sub-stripes in much the same way as the sky is broken into " +
        "stripes.  A default of " + ChunkIndex.DEF_NUM_SUB_STRIPES_PER_STRIPE +
        " sub-stripes is used if this parameter is left unspecified.")
      .create('s');
    Option laneWidthDeg = OptionBuilder
      .withArgName("WIDTH")
      .hasArg()
      .withLongOpt("lane-width-deg")
      .withDescription(
        "If specified, points within each sub-stripe are organized into " +
        "constant width longitude \"lanes\" of at least the given width.  " +
        "Within each lane, points are stored in order of ascending " +
        "latitude.  If this option is negative or zero, a sub-stripe is " +
        "considered to consist of a single lane. The default is " +
        ChunkIndex.DEF_LANE_WIDTH_DEG + " deg.")
      .create('w');
    Option overlapDeg = OptionBuilder
      .withArgName("OVERLAP")
      .hasArg()
      .withLongOpt("overlap-deg")
      .withDescription(
        "Each chunk and sub-stripe is padded to includes points that fall " +
        "within at least the overlap distance of the chunk/sub-stripe " +
        "boundary.  The default is " + ChunkIndex.DEF_OVERLAP_DEG +
        " deg.")
      .create('o');
    Option uikColumn = OptionBuilder
      .withArgName("UIK")
      .hasArg()
      .withLongOpt("uik-column")
      .withDescription(
        "Name or index (0 based) of a unique integer key column in the " +
        "input files. The default is \"" + ChunkIndex.DEF_UIK_COLUMN_NAME +
        "\".")
      .create('K');
    Option lonColumn = OptionBuilder
      .withArgName("THETA")
      .hasArg()
      .withLongOpt("theta-column")
      .withDescription(
        "Name or index (0 based) of longitude angle column in input files. " +
        "The default is \"" + ChunkIndex.DEF_THETA_COLUMN_NAME + "\".")
      .create('T');
    Option latColumn = OptionBuilder
      .withArgName("PHI")
      .hasArg()
      .withLongOpt("phi-column")
      .withDescription(
        "Name or index (0 based) of latitude angle column in input files. " +
        "The default is \"" + ChunkIndex.DEF_PHI_COLUMN_NAME + "\".")
      .create('P');
    Option columnSpec = OptionBuilder
      .withArgName("COLS")
      .hasArg()
      .withLongOpt("column-spec")
      .withDescription(
        "A comma separated list of column names in the input file(s).")
      .create('c');
    Option unitSpec = OptionBuilder
      .withArgName("UNITS")
      .hasArg()
      .withLongOpt("unit-spec")
      .withDescription(
        "An optional comma separated list of unit strings for columns in " +
        "the input file(s)")
      .create('u');
    Option typeSpec = OptionBuilder
      .withArgName("TYPES")
      .hasArg()
      .withLongOpt("type-spec")
      .withDescription(
        "An optional comma separated list of IPAC ASCII data type strings " +
        "for columns in the input file(s)")
      .create('t');
    Option columns = OptionBuilder
      .withArgName("COLS")
      .hasArg()
      .withLongOpt("columns")
      .withDescription(
        "A comma separated list of columns (names or 0-based indices) to " +
        "include in the chunk files.  By default, only the unique integer " +
        "key and longitude/latitude angle columns are included.")
      .create('C');
    Option binary = OptionBuilder
      .withLongOpt("binary")
      .withDescription(
        "When this option is specified, the --columns option is ignored, " +
        "and the only columns stored are the unique integer key, longitude, " +
        "and latitude columns.  Furthermore, values are stored in binary " +
        "form to save space and IO bandwidth.")
      .create('b');
    Option zipped = OptionBuilder
      .withLongOpt("zipped")
      .withDescription(
        "When this option is specified, individual lanes in a chunk are " +
        "compressed with zlib (http://www.zlib.net/).")
      .create('z');
    Option delimiter = OptionBuilder
      .hasArg()
      .withLongOpt("delimiter")
      .withDescription(
        "The ASCII character used to delimit fields in the input " +
        "records.  The default is \"" + DEF_DELIMITER + "\"")
      .create('d');
    Option escape = OptionBuilder
      .hasArg()
      .withLongOpt("escape")
      .withDescription(
        "The ASCII character used to escape delimiter characters occurring " +
        "inside fields.  The default is \"" + DEF_ESCAPE + "\"")
      .create('e');
    Option ignoreTrailing = OptionBuilder
      .withLongOpt("ignore-trailing")
      .withDescription(
        "Ignore the trailing delimiter character in a record. If " +
        "unspecified, a trailing delimiter indicates a trailing empty field.")
      .create('i');
    Option wrapTheta = OptionBuilder
      .withLongOpt("wrap-theta")
      .withDescription(
        "Wrap longitude angles to lie in range [0, 360) instead of " +
        "reporting an error.")
      .create('W');

    opts.addOption(help);
    opts.addOption(fs);
    opts.addOption(jt);
    opts.addOption(property);
    opts.addOption(numStripes);
    opts.addOption(numSubStripes);
    opts.addOption(laneWidthDeg);
    opts.addOption(overlapDeg);
    opts.addOption(uikColumn);
    opts.addOption(lonColumn);
    opts.addOption(latColumn);
    opts.addOption(columnSpec);
    opts.addOption(unitSpec);
    opts.addOption(typeSpec);
    opts.addOption(columns);
    opts.addOption(binary);
    opts.addOption(zipped);
    opts.addOption(delimiter);
    opts.addOption(escape);
    opts.addOption(ignoreTrailing);
    opts.addOption(wrapTheta);
    return opts;
  }

  private static void printUsage(Options options, OutputStream out) {
    HelpFormatter help = new HelpFormatter();
    PrintWriter writer = new PrintWriter(out);
    final int width = 80;
    final int leftPad = 4;
    final int descPad = 2;
    help.printHelp(
      writer, width,
      "create_chunks [options] <path 1> ... <path N> <output directory>",
      "Generate chunk files and a chunk index for the given input files",
      options, leftPad, descPad, null, false);
    writer.flush();
  }

  private static int getColumnIndex(Map<String, Integer> nameMap,
                                    String column) {
    try {
      int i = Integer.parseInt(column);
      if (i < 0 || i >= nameMap.size()) {
        System.err.println(String.format(
          "Column index %d is out of range", i));
        System.exit(2);
      }
      return i;
    } catch (NumberFormatException nfe) {
      Integer i = nameMap.get(column);
      if (i == null) {
        System.err.println(String.format(
          "There is no column named %s in the input files", column));
        System.exit(2);
      }
      return i.intValue();
    }
  }

  private static String join(List<? extends Object> components) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Object o : components) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(StringUtils.escapeString(o.toString()));
    }
    return sb.toString();
  }

  private static String join(Object[] components, List<Integer> indexes) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i : indexes) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      if (components[i] != null) {
        sb.append(StringUtils.escapeString(components[i].toString()));
      }
    }
    return sb.toString();
  }

  private static void processColumns(CommandLine cmd, Configuration conf) {
    // Extract column specification
    if (!cmd.hasOption("column-spec")) {
      System.err.println("Missing column-spec!");
      System.exit(2);
    }
    String columnSpec = cmd.getOptionValue("column-spec");
    String[] names = StringUtils.split(columnSpec);
    if (names.length < ChunkIndex.MIN_COLUMNS ||
        names.length > ChunkIndex.MAX_COLUMNS) {
      System.err.println(String.format(
        "Input files must contain between 3 and %1$d columns",
        ChunkIndex.MAX_COLUMNS));
      System.exit(2);
    }
    // Process column type and unit specifications
    String[] types = null;
    String typeSpec = cmd.getOptionValue("type-spec");
    if (typeSpec == null) {
      types = new String[names.length];
    } else {
      types = Arrays.copyOf(StringUtils.split(typeSpec), names.length);
    }
    String unitSpec = cmd.getOptionValue("unit-spec");
    String[] units = null;
    if (unitSpec == null) {
      units = new String[names.length];
    } else {
      units = Arrays.copyOf(StringUtils.split(unitSpec), names.length);
    }
    Map<String, Integer> n2i = new HashMap<String, Integer>();
    for (int i = 0; i < names.length; ++i) {
      n2i.put(names[i], i);
    }
    // Extract standard column info
    int uikColumn = getColumnIndex(n2i,
      cmd.getOptionValue("uik-column", ChunkIndex.DEF_UIK_COLUMN_NAME));
    int thetaColumn = getColumnIndex(n2i,
      cmd.getOptionValue("theta-column", ChunkIndex.DEF_THETA_COLUMN_NAME));
    int phiColumn = getColumnIndex(n2i,
      cmd.getOptionValue("phi-column", ChunkIndex.DEF_PHI_COLUMN_NAME));
    if (uikColumn == thetaColumn || uikColumn == phiColumn ||
        thetaColumn == phiColumn) {
      System.err.println(
        "Longitude angle, latitude angle, and unique integer key " +
        "columns must be distinct");
      System.exit(2);
    }
    conf.setInt(UIK_COLUMN_KEY, uikColumn);
    conf.setInt(THETA_COLUMN_KEY, thetaColumn);
    conf.setInt(PHI_COLUMN_KEY, phiColumn);
    List<Integer> indexes = Arrays.asList(uikColumn, thetaColumn, phiColumn);
    if (cmd.hasOption("binary")) {
      types[uikColumn] = "long";
      types[thetaColumn] = "double";
      types[phiColumn] = "double";
      units[uikColumn] = "";
      units[thetaColumn] = "deg";
      units[phiColumn] = "deg";
    } else if (cmd.hasOption("columns")) {
      // Add user requested columns to output column list
      String[] columns = StringUtils.split(cmd.getOptionValue("columns"));
      List<Integer> out = new ArrayList<Integer>();
      for (String c : columns) {
        out.add(getColumnIndex(n2i, c));
      }
      out.removeAll(indexes);
      out.addAll(0, indexes);
      indexes = out;
    }
    conf.setInt(NUM_COLUMNS_KEY, names.length);
    conf.set(OUT_COLUMNS_KEY, join(indexes));
    conf.set(ChunkIndex.COLUMN_SPEC_KEY, join(names, indexes));
    conf.set(ChunkIndex.TYPE_SPEC_KEY, join(types, indexes));
    conf.set(ChunkIndex.UNIT_SPEC_KEY, join(units, indexes));
  }

  /**
   * Validates command line options, modifies the job configuration
   * accordingly, and returns a new ChunkIndex built from that configuration.
   */
  private static ChunkIndex createChunkIndex(Options options, CommandLine cmd,
                                             Configuration conf) {
    if (cmd.hasOption('h')) {
      printUsage(options, System.out);
      System.exit(0);
    }
    if (cmd.hasOption("fs")) {
      FileSystem.setDefaultUri(conf, cmd.getOptionValue("fs"));
    }
    if (cmd.hasOption("jt")) {
      conf.set("mapred.job.tracker", cmd.getOptionValue("jt"));
    }
    if (cmd.hasOption('D')) {
      String[] properties = cmd.getOptionValues('D');
      for (String prop : properties) {
        String[] keyval = prop.split("=", 2);
        if (keyval.length == 2) {
          conf.set(keyval[0], keyval[1]);
        }
      }
    }
    int numS = ChunkIndex.DEF_NUM_STRIPES;
    if (cmd.hasOption("num-stripes")) {
      numS = Integer.parseInt(cmd.getOptionValue("num-stripes"));
    }
    conf.setInt(ChunkIndex.NUM_STRIPES_KEY, numS);
    int numSSPerS = ChunkIndex.DEF_NUM_SUB_STRIPES_PER_STRIPE;
    if (cmd.hasOption("num-ss-per-stripe")) {
      numSSPerS = Integer.parseInt(cmd.getOptionValue("num-ss-per-stripe"));
    }
    conf.setInt(ChunkIndex.NUM_SUB_STRIPES_PER_STRIPE_KEY, numSSPerS);
    double laneWidthDeg = ChunkIndex.DEF_LANE_WIDTH_DEG;
    if (cmd.hasOption("lane-width-deg")) {
      laneWidthDeg = Double.parseDouble(cmd.getOptionValue("lane-width-deg"));
    }
    conf.set(ChunkIndex.LANE_WIDTH_DEG_KEY,
             Double.toString(Math.max(laneWidthDeg, 0.0)));
    double overlapDeg = ChunkIndex.DEF_OVERLAP_DEG;
    if (cmd.hasOption("overlap-deg")) {
      overlapDeg = Double.parseDouble(cmd.getOptionValue("overlap-deg"));
    }
    conf.set(ChunkIndex.OVERLAP_DEG_KEY,
             Double.toString(Math.max(overlapDeg, 0.0)));
    conf.setBoolean(ChunkIndex.ZIPPED_KEY, cmd.hasOption("zipped"));
    conf.setBoolean(ChunkIndex.BINARY_KEY, cmd.hasOption("binary"));

    String delimiter = cmd.getOptionValue("delimiter", DEF_DELIMITER);
    String escape = cmd.getOptionValue("escape", DEF_ESCAPE);
    getFormatByte(DELIMITER_KEY, delimiter);
    getFormatByte(ESCAPE_KEY, escape);
    conf.set(DELIMITER_KEY, delimiter);
    conf.set(ESCAPE_KEY, escape);
    conf.setBoolean(IGNORE_TRAILING_KEY, cmd.hasOption("ignore-trailing"));
    conf.setBoolean(WRAP_THETA_KEY, cmd.hasOption("wrap-theta"));
    processColumns(cmd, conf);
    return new ChunkIndex(conf);
  }

  /**
   * Reads in all generated chunk file headers, and sets the chunk-index
   * (overlap) population count for each chunk.
   */
  private static void collectChunkPopulations(ChunkIndex index,
                                              Path outputDir, FileSystem fs)
    throws IOException {

    for (int s = 0; s < index.getNumStripes(); ++s) {
      for (int c = 0; c < index.getNumChunks(s); ++c) {
        Path p = new Path(outputDir, index.getChunkPath(s, c));
        if (fs.exists(p)) {
          ChunkFile f = new ChunkFile(p, fs);
          index.setPopulation(s, c, f.getNumEntries());
          index.setOverlapPopulation(s, c, f.getNumOverlapEntries());
        }
      }
    }
  }

  /**
   * Reads in all column width files produced by reducers, computes
   * the maximum width of each column, and stores the results in
   * the given ChunkIndex.
   */
  private static void collectColumnWidths(ChunkIndex index, Path outputDir,
                                          FileSystem fs)
    throws IOException {

    Path widthFilePattern = new Path(outputDir, "column-widths-*");
    List<ChunkIndex.Column> columns = index.getColumns();
    int[] maxWidths = new int[columns.size()];
    for (FileStatus stat : fs.globStatus(widthFilePattern)) {
      FSDataInputStream stream = fs.open(stat.getPath());
      int[] widths = new int[stream.readInt()];
      if (widths.length != columns.size()) {
        throw new IOException(String.format(
          "Number of columns in width file %1$d does not match  number " +
          "of columns in index (%2$d)", widths.length, columns.size()));
      }
      for (int i = 0; i < widths.length; ++i) {
        widths[i] = stream.readInt();
      }
      for (int i = 0; i < widths.length; ++i) {
        maxWidths[i] = Math.max(widths[i], maxWidths[i]);
      }
    }
    for (int i = 0; i < maxWidths.length; ++i) {
      columns.get(i).setWidth(maxWidths[i]);
    }
  }

  /**
   * Writes the given ChunkIndex to the given output directory.
   */
  private static void writeChunkIndex(ChunkIndex index,
                                      Path outputDir, FileSystem fs)
    throws IOException {

    Path indexPath = ChunkIndex.indexPath(outputDir, false);
    FSDataOutputStream stream = fs.create(indexPath, false);
    try {
      index.write(stream);
    } finally {
      stream.close();
    }
  }

  /**
   * Launches CreateChunks map-reduce jobs.
   */
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Options opts = buildOptions();
    GenericOptionsParser parser = new GenericOptionsParser(conf, opts, args);
    CommandLine cmd = parser.getCommandLine();
    ChunkIndex index = createChunkIndex(opts, cmd, conf);
    String[] paths = parser.getRemainingArgs();
    if (paths.length < 2) {
      System.err.println(
        "Missing input path(s) and/or output directory. " +
        "Use -help for usage details.");
      System.exit(2);
    }
    // setup map-reduce job
    Job job = new Job(conf, "ipac.stk.mapreduce.job.CreateChunks");
    job.setJarByClass(CreateChunks.class);
    if (cmd.hasOption("binary")) {
      job.setMapperClass(BinaryChunkMapper.class);
      job.setReducerClass(BinaryChunkReducer.class);
      job.setMapOutputValueClass(LongWritable.class);
    } else {
      job.setMapperClass(TextChunkMapper.class);
      job.setReducerClass(TextChunkReducer.class);
      job.setMapOutputValueClass(Text.class);
    }
    job.setPartitionerClass(ChunkPartitioner.class);
    job.setMapOutputKeyClass(ChunkIndex.Location.class);
    job.setOutputKeyClass(NullWritable.class);
    job.setOutputValueClass(NullWritable.class);
    for (int i = 0; i < paths.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(paths[i]));
    }
    Path outputDir = new Path(paths[paths.length - 1]);
    FileOutputFormat.setOutputPath(job, outputDir);
    // launch job and wait for completion
    if (!job.waitForCompletion(true)) {
      System.exit(1);
    }
    FileSystem fs = outputDir.getFileSystem(conf);
    collectChunkPopulations(index, outputDir, fs);
    collectColumnWidths(index, outputDir, fs);
    writeChunkIndex(index, outputDir, fs);
    System.exit(0);
  }
}
