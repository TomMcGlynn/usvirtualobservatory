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

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.io.compress.zlib.ZlibCompressor;
import org.apache.hadoop.io.compress.zlib.ZlibDecompressor;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import ipac.stk.json.JSONOutput;
import ipac.stk.json.JSONWritable;


/**
 * Class representing chunk files consisting of spatially binned tuples.
 * Each tuple corresponds to a UTF-8 string with individual fields separated
 * by null ('\0') characters or to binary (unique integer key, longitude,
 * latitude) tuples. Individual bins are optionally compressed using
 * zlib (http://www.zlib.net).
 *
 * TODO:  Document file format!
 */
public final class ChunkFile implements JSONWritable {

  public static final long MAGIC = 0xc0ffeefeedL;
  public static final int VERSION = 1;
  // Offset of trailer offset relative to the end of the chunk file.
  private static final int TRAILER_OFF_OFF = 20;
  private static final int MIN_TRAILER_SIZE = 16;
  // Minimum trailer offset relative to the end of the chunk file. If
  // the trailer data written out by RecordReader.close() changes, this
  // value will need to be updated.
  private static final int MIN_TRAILER_OFF =
    MIN_TRAILER_SIZE + TRAILER_OFF_OFF;

  private Path path = null;
  private FileSystem fs = null;
  private int numS = 0;
  private int stripe = 0;
  private int chunk = 0;
  private long numEntries = 0;
  private long numOverlapEntries = 0;
  private int overlapSS = 0;
  private int numSS = 0;
  private boolean zipped = false;
  private boolean binary = false;
  private boolean written = false;
  private int[] ssLanes = null;
  private int[] overlapLanes = null;
  private int[][] population = null;
  private int[][] laneSize = null;
  private long[][] laneOffsets = null;
  private RecordWriter writer = null;

  /**
   * Class for reading chunk file records.
   */
  public final class RecordReader {
    private FSDataInputStream stream = null;
    private DataInputBuffer buffer = null;
    private ZlibDecompressor decompressor = null;
    private byte[] inBuffer = null;
    private byte[] unzipBuffer = null;
    private ChunkIndex.Record[] records = null;
    private int numRecords = 0;
    private int subStripe = 0;
    private int lane = -1;
    private boolean done = false;

    private void readLane() throws IOException {
      if (!hasNext()) {
        throw new IOException("All records in chunk file have been read");
      }
      int sz = (int) (laneOffsets[subStripe][lane + 1] -
                      laneOffsets[subStripe][lane]);
      if (stream.getPos() != laneOffsets[subStripe][lane]) {
        throw new RuntimeException(String.format(
            "Stream position %d does not match beginning of lane offset %d",
            stream.getPos(), laneOffsets[subStripe][lane]));
      }
      stream.readFully(inBuffer, 0, sz);
      if (zipped) {
        // decompress the lane
        int dsz = laneSize[subStripe][lane];
        decompressor.setInput(inBuffer, 0, sz);
        int off = 0;
        while (!decompressor.needsInput()) {
          off += decompressor.decompress(unzipBuffer, off, dsz - off);
        }
        while (!decompressor.finished()) {
          off += decompressor.decompress(unzipBuffer, off, dsz - off);
        }
        if (off != dsz) {
          throw new IOException("Failed to decompress lane");
        }
        decompressor.reset();
        buffer.reset(unzipBuffer, dsz);
      } else {
        buffer.reset(inBuffer, sz);
      }
      if (binary) {
        // read in binary records
        long refTheta = 0;
        long refPhi = 0;
        for (int i = 0; i < numRecords; ++i) {
          records[i].setUIK(WritableUtils.readVLong(buffer));
          long theta = refTheta + WritableUtils.readVLong(buffer);
          long phi = refPhi + WritableUtils.readVLong(buffer);
          records[i].setTheta(theta);
          records[i].setPhi(phi);
          refTheta = theta;
          refPhi = phi;
        }
      } else {
        // read in text records
        for (int i = 0; i < numRecords; ++i) {
          Text data = records[i].getData();
          data.readFields(buffer);
          byte[] buf = data.getBytes();
          int last = 0;
          int j = 0;
          // extract uik from data
          for (; buf[j] != 0; ++j) { }
          records[i].setUIK(Long.parseLong(Text.decode(buf, last, j - last)));
          // extract theta from data
          for (last = ++j; buf[j] != 0; ++j) { }
          records[i].setTheta(
            Double.parseDouble(Text.decode(buf, last, j - last)));
          // extract phi from data
          for (last = ++j; buf[j] != 0; ++j) { }
          records[i].setPhi(
            Double.parseDouble(Text.decode(buf, last, j - last)));
        }
      }
    }

    private void advanceLane() throws IOException {
      if (!hasNext()) {
        return;
      }
      int j = lane + 1;
      for (int i = subStripe; i < numSS; ++i) {
        for (; j < population[i].length; ++j) {
          int n = population[i][j];
          if (n != 0) {
            numRecords = n;
            subStripe = i;
            lane = j;
            return;
          }
        }
        j = 0;
      }
      numRecords = 0;
      subStripe = Integer.MAX_VALUE;
      lane = Integer.MAX_VALUE;
      done = true;
      stream.close();
      stream = null;
    }

    public RecordReader() throws IOException {
      if (!written) {
        throw new IOException(
          "Cannot read an unwritten or incomplete chunk file");
      }
      stream = fs.open(path);
      // Find max # of lane entries and maximum lane sizes
      int maxLaneEntries = 0;
      int maxLaneSize = 0;
      int maxCompressedLaneSize = 0;
      for (int i = 0; i < numSS; ++i) {
        for (int j = 0; j < population[i].length; ++j) {
          int ne = population[i][j];
          int lb = (int) (laneOffsets[i][j + 1] - laneOffsets[i][j]);
          maxLaneEntries = Math.max(maxLaneEntries, ne);
          maxLaneSize = Math.max(maxLaneSize, laneSize[i][j]);
          maxCompressedLaneSize = Math.max(maxCompressedLaneSize, lb);
        }
      }
      buffer = new DataInputBuffer();
      records = new ChunkIndex.Record[maxLaneEntries];
      for (int i = 0; i < records.length; ++i) {
        ChunkIndex.Record rec = new ChunkIndex.Record();
        if (!binary) {
          rec.setData(new Text());
        }
        records[i] = rec;
      }
      inBuffer = new byte[maxCompressedLaneSize];
      if (zipped) {
        decompressor = new ZlibDecompressor(
          ZlibDecompressor.CompressionHeader.DEFAULT_HEADER,
          maxLaneSize);
        unzipBuffer = new byte[maxLaneSize];
      }
      advanceLane();
    }

    /**
     * Returns the sub-stripe number of the next non-empty lane.
     * If all records have been read, Integer.MAX_VALUE is returned.
     */
    public int getSubStripe() {
      return subStripe;
    }

    /**
     * Returns the lane number of the next non-empty lane.
     * If all records have been read, Integer.MAX_VALUE is returned.
     */
    public int getLane() {
      return lane;
    }

    /**
     * Returns the number of records in the next non-empty lane.
     * If all records have been read, 0 is returned.
     */
    public int getNumRecords() {
      return numRecords;
    }

    /**
     * Returns the array of records in the next non-empty lane of the
     * chunk file. The number of valid entries can be obtained by calling
     * {@link #getNumRecords()} <b>before</b> calling next(). The
     * returned array will remain valid until a subsequent call to
     * next().
     */
    public ChunkIndex.Record[] next() throws IOException {
      readLane();
      advanceLane();
      return records;
    }

    /**
     * Returns <code>true</code> if there are any more records to read.
     */
    public boolean hasNext() {
      return !done;
    }
  }


  /**
   * Base class for ChunkFile record writers.
   */
  abstract class RecordWriter {
    private static final int ZIP_BUFFER_SIZE = 64 * 1024;

    private FSDataOutputStream stream = null;
    private DataOutputBuffer buffer = null;
    private ZlibCompressor compressor = null;
    private byte[] zipBuffer = null;
    private boolean closed = false;
    private int lastSS = 0;
    private int lastLane = 0;
    private long lastPhi = Long.MIN_VALUE;
    private long lastUIK = Long.MIN_VALUE;
    private int laneEntries = 0;
    private long laneOffset = 0;

    RecordWriter() throws IOException {
      if (written) {
        throw new IOException(
          "Cannot create a record writer for a chunk file " +
          "that has already been written.");
      }
      stream = fs.create(path, false);
      buffer = new DataOutputBuffer();
      if (zipped) {
        compressor = new ZlibCompressor(
          ZlibCompressor.CompressionLevel.BEST_COMPRESSION,
          ZlibCompressor.CompressionStrategy.DEFAULT_STRATEGY,
          ZlibCompressor.CompressionHeader.DEFAULT_HEADER,
          64 * 1024);
        zipBuffer = new byte[ZIP_BUFFER_SIZE];
      }
    }

    private void flushLane(int nextSS, int nextLane) throws IOException {
      population[lastSS][lastLane] = laneEntries;
      laneSize[lastSS][lastLane] = buffer.getLength();
      if (isOverlap(lastSS, lastLane)) {
        numOverlapEntries += laneEntries;
      } else {
        numEntries += laneEntries;
      }
      laneEntries = 0;
      if (buffer.getLength() > 0) {
        if (zipped) {
          compressor.setInput(buffer.getData(), 0, buffer.getLength());
          int len = 0;
          while (!compressor.needsInput()) {
            int n = compressor.compress(zipBuffer, 0, zipBuffer.length);
            len += n;
            if (n > 0) {
              stream.write(zipBuffer, 0, n);
            }
          }
          compressor.finish();
          while (!compressor.finished()) {
            int n = compressor.compress(zipBuffer, 0, zipBuffer.length);
            len += n;
            if (n > 0) {
              stream.write(zipBuffer, 0, n);
            }
          }
          laneOffset += len;
          compressor.reset();
        } else {
          laneOffset += buffer.getLength();
          buffer.writeTo(stream);
        }
        buffer.reset();
      }
      for (int i = lastSS; i <= nextSS; ++i) {
        int j = (i == lastSS) ? lastLane + 1 : 0;
        int k = (i == nextSS) ? nextLane + 1 : laneOffsets[i].length;
        for (; j < k; ++j) {
          laneOffsets[i][j] = laneOffset;
        }
      }
      lastSS = nextSS;
      lastLane = nextLane;
    }

    /**
     * Returns a DataOutput instance that can be used to serialize data.
     * To be called inside append() implementations only.
     */
    protected DataOutput getDataOuput() {
      return buffer;
    }

    /**
     * Sanity checks input data parameters and flushes the current lane if
     * the input record falls into a new lane.
     *
     * @return  <code>true</code> if the input record fell into a new lane.
     */
    protected boolean prepareAppend(int ss, int lane, long phi, long uik)
      throws IOException {

      if (closed) {
        throw new IOException("Cannot append data: record writer is closed.");
      }
      boolean newLane = false;
      if (ss < 0 || lane < 0) {
        throw new IllegalArgumentException(
          "Negative lane or sub-stripe number.");
      }
      if (ss == lastSS && lane == lastLane) {
        if ((phi == lastPhi && uik <= lastUIK) || phi < lastPhi) {
          throw new IllegalArgumentException(String.format(
            "Entries must be appended to lanes in ascending latitude " +
            "angle, unique integer key order - no duplicates allowed." +
            "Previous entry had (phi, uik) = (%d, %d) >= (%d, %d).",
            lastPhi, lastUIK, phi, uik));
        }
      } else if (ss < lastSS || (ss == lastSS && lane < lastLane)) {
        throw new IllegalArgumentException(String.format(
          "Entries must be appended to chunk files in ascending " +
          "sub-stripe, lane order. Previous entry had (ss, lane) = " +
          "(%d, %d) > (%d, %d)", lastSS, lastLane, ss, lane));
      } else if (ss >= ssLanes.length || lane >= population[ss].length) {
        throw new IllegalArgumentException(String.format(
          "Sub-stripe %d (lane number %d) out of bounds [0, %d).",
          ss, lane, ssLanes.length));
      } else {
        flushLane(ss, lane);
        newLane = true;
      }
      lastPhi = phi;
      lastUIK = uik;
      ++laneEntries;
      return newLane;
    }

    /**
     * Appends a record in the given lane to the chunk file.
     */
    public abstract void append(int subStripe, int lane, ChunkIndex.Record rec)
      throws IOException;

    /**
     * Appends a record with the given location to the chunk file.
     */
    public void append(ChunkIndex.Location loc, ChunkIndex.Record rec)
      throws IOException {

      append(loc.getSubStripe(), loc.getLane(), rec);
    }

    /**
     * Flushes any remaining lane data to disk and writes out the
     * chunk file trailer. After calling this method all other record
     * writer method calls except close() will fail; subsequent calls
     * to close() have no effect.
     */
    public void close() throws IOException {
      if (closed) {
        return;
      }
      flushLane(numSS - 1, laneOffsets[numSS - 1].length - 1);
      // Write header
      WritableUtils.writeVInt(stream, numS);
      WritableUtils.writeVInt(stream, stripe);
      WritableUtils.writeVInt(stream, chunk);
      WritableUtils.writeVLong(stream, numEntries);
      WritableUtils.writeVLong(stream, numOverlapEntries);
      WritableUtils.writeVInt(stream, overlapSS);
      WritableUtils.writeVInt(stream, numSS);
      stream.writeBoolean(zipped);
      stream.writeBoolean(binary);
      for (int i = 0; i < ssLanes.length; ++i) {
        WritableUtils.writeVInt(stream, ssLanes[i]);
        WritableUtils.writeVInt(stream, overlapLanes[i]);
        int numLanes = population[i].length;
        WritableUtils.writeVInt(stream, numLanes);
        for (int j = 0; j < numLanes; ++j) {
          WritableUtils.writeVInt(stream, population[i][j]);
          WritableUtils.writeVInt(stream, laneSize[i][j]);
          WritableUtils.writeVLong(stream, laneOffsets[i][j]);
        }
        WritableUtils.writeVLong(stream, laneOffsets[i][numLanes]);
      }
      // Write header starting offset and magic bytes
      stream.writeLong(laneOffset);
      stream.writeInt(VERSION);
      stream.writeLong(MAGIC);
      stream.close();
      stream = null;
      buffer = null;
      compressor = null;
      zipBuffer = null;
      closed = true;
      written = true;
    }

    /**
     * Returns <code>true</code> if this record writer has been closed.
     */
    public boolean isClosed() {
      return closed;
    }
  }


  /**
   * An inner class for writing UTF-8 chunk file records.  These must be
   * appended in lexicographic sub-stripe, lane, scaled latitude angle,
   * unique integer key order.
   */
  public class TextRecordWriter extends RecordWriter {
    TextRecordWriter() throws IOException {
      super();
    }

    public void append(ChunkIndex.Location loc, Text data) throws IOException {
      prepareAppend(loc.getSubStripe(), loc.getLane(),
                    loc.getPhi(), loc.getUIK());
      data.write(getDataOuput());
    }

    @Override public void append(int subStripe, int lane, ChunkIndex.Record rec)
      throws IOException {

      prepareAppend(subStripe, lane, rec.getPhi(), rec.getUIK());
      rec.getData().write(getDataOuput());
    }
  }


  /**
   * A class for writing binary chunk file records. These must be passed to
   * append() in lexicographic sub-stripe, lane, scaled latitude angle,
   * unique integer key order.
   */
  public class BinaryRecordWriter extends RecordWriter {
    private long refTheta = 0;
    private long refPhi = 0;

    BinaryRecordWriter() throws IOException {
      super();
    }

    private void append(int subStripe, int lane, long phi, long uik, long theta)
      throws IOException {
      if (prepareAppend(subStripe, lane, phi, uik)) {
        refTheta = 0;
        refPhi = 0;
      }
      // Write delta-encoded record
      WritableUtils.writeVLong(getDataOuput(), uik);
      WritableUtils.writeVLong(getDataOuput(), theta - refTheta);
      WritableUtils.writeVLong(getDataOuput(), phi - refPhi);
      refTheta = theta;
      refPhi = phi;
    }

    public void append(ChunkIndex.Location loc, long theta) throws IOException {
      append(loc.getSubStripe(), loc.getLane(),
             loc.getPhi(), loc.getUIK(), theta);
    }

    @Override public void append(int subStripe, int lane, ChunkIndex.Record rec)
      throws IOException {
      append(subStripe, lane, rec.getPhi(), rec.getUIK(), rec.getTheta());
    }
  }


  /**
   * Helper class for chunk merging.
   */
  private static final class LaneReader implements Comparable<LaneReader> {
    private ChunkFile.RecordReader reader = null;
    private ChunkIndex.Record[] lane = null;
    private int index = 0;
    private int length = 0;

    LaneReader(ChunkFile f) throws IOException {
      reader = f.new RecordReader();
    }

    int getSubStripe() {
      return reader.getSubStripe();
    }

    int getLane() {
      return reader.getLane();
    }

    boolean hasNext() {
      return reader.hasNext();
    }

    boolean isLaneFinished() {
      return index >= length;
    }

    void readLane() throws IOException {
      if (!hasNext()) {
        throw new IOException("No more lanes to read.");
      }
      index = 0;
      length = reader.getNumRecords();
      lane = reader.next();
    }

    @Override public boolean equals(Object obj) {
      if (obj instanceof LaneReader) {
        LaneReader r = (LaneReader) obj;
        return compareTo(r) == 0;
      }
      return false;
    }

    @Override public int hashCode() {
      assert false : "hashCode() not designed";
      return 0;
    }

    @Override public int compareTo(LaneReader r) {
      ChunkIndex.Record r1 = lane[index];
      ChunkIndex.Record r2 = r.lane[r.index];
      long phi1 = r1.getPhi();
      long phi2 = r2.getPhi();
      if (phi1 < phi2) {
        return -1;
      } else if (phi1 > phi2) {
        return 1;
      }
      long uik1 = r1.getUIK();
      long uik2 = r2.getUIK();
      if (uik1 < uik2) {
        return -1;
      } else if (uik1 == uik2) {
        throw new RuntimeException(String.format(
          "Cannot merge chunk files with duplicate entries: UIK = %d", uik1));
      }
      return 1;
    }

    void writeAll(int ss, int l, RecordWriter writer) throws IOException {
      if (isLaneFinished()) {
        throw new IOException("No more lane entries to write.");
      }
      for (; index < length; ++index) {
        writer.append(ss, l, lane[index]);
      }
    }

    void writeOne(int ss, int l, RecordWriter writer) throws IOException {
      if (isLaneFinished()) {
        throw new IOException("No more lane entries to write.");
      }
      writer.append(ss, l, lane[index]);
      ++index;
    }
  }


  /**
   * Implementation class for merging chunk files.
   */
  private class Merger {
    private RecordWriter writer = null;
    private LaneReader[] readers = null;
    private LaneReader[] active = null;
    private PriorityQueue<LaneReader> pq = null;

    Merger(List<ChunkFile> chunks) throws IOException {
      writer = getRecordWriter();
      readers = new LaneReader[chunks.size()];
      active = new LaneReader[chunks.size()];
      pq = new PriorityQueue<LaneReader>();
      for (int i = 0; i < chunks.size(); ++i) {
        readers[i] = new LaneReader(chunks.get(i));
      }
    }

    public void merge(TaskAttemptContext context) throws IOException {
      try {
        while (true) {
          // Find the next non-empty lane and place all readers
          // with data for that lane into _active.
          int ss = Integer.MAX_VALUE;
          int lane = 0;
          int n = 0;
          for (LaneReader r : readers) {
            if (r.hasNext()) {
              int rss = r.getSubStripe();
              if (rss < ss) {
                ss = rss;
                lane = r.getLane();
                active[0] = r;
                n = 1;
              } else if (rss == ss) {
                if (r.getLane() < lane) {
                  lane = r.getLane();
                  active[0] = r;
                  n = 1;
                } else if (r.getLane() == lane) {
                  active[n++] = r;
                }
              }
            }
          }
          if (n == 0) {
            // All record readers finished
            break;
          }
          // Collect records for the lane
          for (int i = 0; i < n; ++i) {
            active[i].readLane();
          }
          context.setStatus(String.format(
              "Processing sub-stripe %1$d, lane %2$d in stripe %3$d chunk %4$d",
              ss, lane, stripe, chunk));
          // n-way lane merge using a min-heap
          if (n == 1) {
            active[0].writeAll(ss, lane, writer);
          } else if (n == 2) {
            LaneReader left = active[0];
            LaneReader right = active[1];
            while (true) {
              if (left.compareTo(right) < 0) {
                left.writeOne(ss, lane, writer);
                if (left.isLaneFinished()) {
                  break;
                }
              } else {
                right.writeOne(ss, lane, writer);
                if (right.isLaneFinished()) {
                  break;
                }
              }
            }
            if (!left.isLaneFinished()) {
              left.writeAll(ss, lane, writer);
            } else {
              right.writeAll(ss, lane, writer);
            }
          } else {
            for (int i = 0; i < n; ++i) {
              pq.add(active[i]);
            }
            for (LaneReader r = pq.poll(); r != null; r = pq.poll()) {
              r.writeOne(ss, lane, writer);
              if (!r.isLaneFinished()) {
                pq.add(r);
              }
            }
          }
        }
      } finally {
        writer.close();
      }
    }
  }


  /**
   * Initializes ChunkFile fields with the contents of the given chunk file.
   */
  private void initialize(Path p, FileSystem filesys) throws IOException {
    FileStatus stat = filesys.getFileStatus(p);
    FSDataInputStream in = filesys.open(p);
    if (stat.getLen() < TRAILER_OFF_OFF) {
      throw new IOException("Could not read ChunkFile trailer: file too small");
    }
    path = p;
    fs = filesys;
    in.seek(stat.getLen() - TRAILER_OFF_OFF);
    long trailerOffset = in.readLong();
    if (in.readInt() != VERSION) {
      throw new IOException("Could not read ChunkFile trailer: " +
                            "file format version mismatch");
    }
    if (in.readLong() != MAGIC) {
      throw new IOException(
        "Could not read ChunkFile trailer: missing magic bytes");
    }
    if (trailerOffset > stat.getLen() - MIN_TRAILER_OFF) {
      throw new IOException(
        "Could not read ChunkFile trailer: invalid trailer offset");
    }
    in.seek(trailerOffset);
    numS = WritableUtils.readVInt(in);
    stripe = WritableUtils.readVInt(in);
    chunk = WritableUtils.readVInt(in);
    numEntries = WritableUtils.readVLong(in);
    numOverlapEntries = WritableUtils.readVLong(in);
    overlapSS = WritableUtils.readVInt(in);
    numSS = WritableUtils.readVInt(in);
    zipped = in.readBoolean();
    binary = in.readBoolean();
    written = true;
    ssLanes = new int[numSS];
    overlapLanes = new int[numSS];
    population = new int[numSS][];
    laneSize = new int[numSS][];
    laneOffsets = new long[numSS][];
    long lastOff = 0;
    for (int i = 0; i < numSS; ++i) {
      ssLanes[i] = WritableUtils.readVInt(in);
      overlapLanes[i] = WritableUtils.readVInt(in);
      int numLanes = WritableUtils.readVInt(in);
      if (numLanes != ssLanes[i] + 2 * overlapLanes[i]) {
        throw new IOException("Inconsistent sub-stripe lane count");
      }
      population[i] = new int[numLanes];
      laneSize[i] = new int[numLanes];
      laneOffsets[i] = new long[numLanes + 1];
      for (int j = 0; j < numLanes; ++j) {
        int pop = WritableUtils.readVInt(in);
        int sz = WritableUtils.readVInt(in);
        long off = WritableUtils.readVLong(in);
        if (pop < 0) {
          throw new IOException("Negative lane population count");
        }
        if (sz < 0) {
          throw new IOException("Negative lane size");
        }
        if ((sz == 0 && pop != 0) || (pop == 0 && sz != 0)) {
          throw new IOException(
            "Invariant violated: lane size is 0 " +
            "if and only if lane population is 0.");
        }
        if (off < lastOff) {
          throw new IOException("Decreasing lane offsets");
        }
        population[i][j] = pop;
        laneSize[i][j] = sz;
        laneOffsets[i][j] = off;
        lastOff = off;
      }
      laneOffsets[i][numLanes] = WritableUtils.readVLong(in);
    }
  }

  /**
   * Creates a new ChunkFile for the given ChunkIndex.
   *
   * @param dir       The directory containing the ChunkIndex to create
   *                  a chunk file for.
   * @param filesys   The FileSystem hosting <code>dir</code>.
   * @param index     The chunk index to create a chunk file for.
   * @param stripeNum The stripe number of the chunk.
   * @param chunkNum  The chunk number of the chunk.
   */
  public ChunkFile(Path dir, FileSystem filesys, ChunkIndex index,
                   int stripeNum, int chunkNum) throws IOException {
    Path p = new Path(dir, index.getChunkPath(stripeNum, chunkNum));
    if (filesys.exists(p)) {
      throw new IllegalArgumentException(String.format(
        "Chunk file %s already exists", p));
    } else {
      path = p;
      fs = filesys;
      numS = index.getNumStripes();
      stripe = stripeNum;
      chunk = chunkNum;
      overlapSS = index.getNumOverlapSubStripes();
      final int nss = index.getNumSubStripesPerStripe();
      numSS = nss + (stripeNum > 0 ? overlapSS : 0) +
              (stripeNum < index.getNumStripes() - 1 ? overlapSS : 0);
      binary = index.isBinary();
      zipped = index.isZipped();
      ssLanes = new int[numSS];
      overlapLanes = new int[numSS];
      population = new int[numSS][];
      laneSize = new int[numSS][];
      laneOffsets = new long[numSS][];
      int j = 0;
      int off = 0;
      if (stripe > 0) {
        off = overlapSS;
        for (; j < overlapSS; ++j) {
          ssLanes[j] = index.getNumLanesPerChunk(stripe, 0);
          overlapLanes[j] = index.getNumOverlapLanes(stripe, 0);
          int n = ssLanes[j] + 2 * overlapLanes[j];
          population[j] = new int[n];
          laneSize[j] = new int[n];
          laneOffsets[j] = new long[n + 1];
        }
      }
      for (; j < nss + off; ++j) {
        ssLanes[j] = index.getNumLanesPerChunk(stripe, j - off);
        overlapLanes[j] = index.getNumOverlapLanes(stripe, j - off);
        int n = ssLanes[j] + 2 * overlapLanes[j];
        population[j] = new int[n];
        laneSize[j] = new int[n];
        laneOffsets[j] = new long[n + 1];
      }
      off += nss;
      if (stripe < index.getNumStripes() - 1) {
        for (; j < off + overlapSS; ++j) {
          ssLanes[j] = index.getNumLanesPerChunk(stripe, nss - 1);
          overlapLanes[j] = index.getNumOverlapLanes(stripe, nss - 1);
          int n = ssLanes[j] + 2 * overlapLanes[j];
          population[j] = new int[n];
          laneSize[j] = new int[n];
          laneOffsets[j] = new long[n + 1];
        }
      }
    }
  }

  /**
   * Creates a new ChunkFile object corresponding to the specified file.
   */
  public ChunkFile(Path path, FileSystem fs) throws IOException {
    initialize(path, fs);
  }

  /**
   * Returns <code>true</code> if this ChunkFile is mergeable
   * with <code>f</code>.
   */
  public boolean isMergeable(ChunkFile f) {
    if (numS != f.numS || stripe != f.stripe || chunk != f.chunk ||
        numSS != f.numSS || overlapSS != f.overlapSS ||
        zipped != f.zipped || binary != f.binary) {
      return false;
    }
    for (int i = 0; i < numSS; ++i) {
      if (ssLanes[i] != f.ssLanes[i] ||
          overlapLanes[i] != f.overlapLanes[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a record writer for this chunk file.
   */
  public RecordWriter getRecordWriter() throws IOException {
    if (written) {
      throw new IllegalStateException(
        "Chunk file already exists on disk and cannot be overwritten.");
    }
    if (writer == null) {
      if (binary) {
        writer = new BinaryRecordWriter();
      } else {
        writer = new TextRecordWriter();
      }
    }
    return writer;
  }

  /**
   * Merges all the given input chunks into this one and
   * writes the results to disk.
   */
  public void merge(List<Path> inputs, Configuration conf,
                    TaskAttemptContext context) throws IOException {
    if (written) {
      throw new IllegalStateException(
        "Chunk file already exists on disk and cannot be overwritten.");
    }
    List<ChunkFile> chunks = new ArrayList<ChunkFile>();
    for (Path p : inputs) {
      ChunkFile f = new ChunkFile(p, p.getFileSystem(conf));
      if (!isMergeable(f)) {
        throw new IllegalArgumentException(String.format(
          "Inconsistent chunk file parameters: cannot merge with %s", p));
      }
      chunks.add(f);
    }
    Merger merger = new Merger(chunks);
    merger.merge(context);
  }

  public int getStripe() {
    return stripe;
  }

  public int getChunk() {
    return chunk;
  }

  public long getNumEntries() {
    return numEntries;
  }

  public long getNumOverlapEntries() {
    return numOverlapEntries;
  }

  /**
   * Returns the number of sub-stripes in the chunk, including overlap
   * sub-stripes.
   */
  public int getNumSubStripes() {
    return numSS;
  }

  /**
   * Returns the number of overlap sub-stripes on one side of the chunk;
   * non polar chunks will have twice this number of overlap sub-stripes,
   * whereas polar chunks only store overlap sub-stripes on one side.
   */
  public int getNumOverlapSubStripes() {
    return overlapSS;
  }

  public boolean isZipped() {
    return zipped;
  }

  public boolean isBinary() {
    return binary;
  }

  /**
   * Tests whether the given lane of the given sub-stripe contains overlap
   * entries.
   */
  public boolean isOverlap(int subStripe, int lane) {
    if (stripe < 0 || stripe >= numSS) {
      throw new IndexOutOfBoundsException("sub-stripe number out of range");
    }
    if (lane < 0 || lane >= ssLanes[subStripe] + 2 * overlapLanes[subStripe]) {
      throw new IndexOutOfBoundsException("lane number out of range");
    }
    if (stripe > 0 && subStripe < overlapSS) {
      return true;
    }
    if (stripe < numS - 1 && subStripe >= numSS - overlapSS) {
      return true;
    }
    if (lane < overlapLanes[subStripe] ||
        lane >= ssLanes[subStripe] + overlapLanes[subStripe]) {
        return true;
    }
    return false;
  }

  @Override public String toString() {
    return JSONOutput.toString(this);
  }

  @Override public void writeJSON(JSONOutput out) throws IOException {
    writeJSON(out, false);
  }

  public void writeJSON(JSONOutput out, boolean verbose) throws IOException {
    out.object();
    out.pair("path", path);
    out.pair("numStripes", numS);
    out.pair("stripe", stripe);
    out.pair("chunk", chunk);
    out.pair("numEntries", numEntries);
    out.pair("numOverlapEntries", numOverlapEntries);
    out.pair("numOverlapSubStripes", overlapSS);
    out.pair("numSubStripes", numSS);
    out.pair("zipped", zipped);
    out.pair("binary", binary);
    out.key("lanes").array(ssLanes);
    out.key("overlapLanes").array(overlapLanes);
    if (verbose) {
      out.key("subStripes");
      out.array();
      for (int ss = 0; ss < numSS; ++ss) {
        out.object();
        out.key("population").array(population[ss]);
        out.key("laneSize").array(laneSize[ss]);
        out.key("laneOffsets").array(laneOffsets[ss]);
        out.finish();
      }
      out.finish();
    }
    out.finish();
  }
}

