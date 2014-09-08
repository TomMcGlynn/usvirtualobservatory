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



package ipac.stk.mapreduce.lib.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.util.StringUtils;


/**
 * An input format that creates a map task for each line of an input file.
 * The map key is the line offset and the value is the line itself.
 *
 * Each line is interpreted as a comma-separated list of file names. From
 * these names the rack hosting the most input data is found. Within that
 * rack, nodes are sorted from most to least data locality and returned
 * as location hints.
 */
public class OneLineInputFormat
  extends FileInputFormat<LongWritable, Text> {

  /**
   * Finds the rack containing the largest subset of the files listed
   * in <code>line</code> and returns nodes in that rack sorted by
   * locality.
   */
  protected static String[] getHosts(String[] files, Configuration conf)
    throws IOException {

    HashMap<String, Integer> rackMap = new HashMap<String, Integer>();
    HashMap<String, Integer> topoMap = new HashMap<String, Integer>();
    for (String s : files) {
      Path p = new Path(s);
      FileSystem fs = p.getFileSystem(conf);
      FileStatus stat = fs.getFileStatus(p);
      BlockLocation[] locs = fs.getFileBlockLocations(stat, 0, stat.getLen());
      for (BlockLocation loc : locs) {
        for (String topo : loc.getTopologyPaths()) {
          String rack = topo.substring(0, topo.lastIndexOf('/'));
          if (rackMap.containsKey(rack)) {
            rackMap.put(rack, rackMap.get(rack) + 1);
          } else {
            rackMap.put(rack, 1);
          }
          if (topoMap.containsKey(topo)) {
            topoMap.put(topo, topoMap.get(topo) + 1);
          } else {
            topoMap.put(topo, 1);
          }
        }
      }
    }
    // find rack with the most locality
    String rack = null;
    int rackMax = 0;
    for (Map.Entry<String, Integer> entry : rackMap.entrySet()) {
      if (entry.getValue() > rackMax) {
        rack = entry.getKey();
      }
    }
    // retrieve nodes from that rack
    ArrayList<Map.Entry<String, Integer>> nodes =
      new ArrayList<Map.Entry<String, Integer>>();
    for (Map.Entry<String, Integer> entry : topoMap.entrySet()) {
      if (entry.getKey().startsWith(rack)) {
        nodes.add(entry);
      }
    }
    // sort them in descending locality order
    Collections.sort(nodes, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> e1,
                         Map.Entry<String, Integer> e2) {
        return e2.getValue().compareTo(e1.getValue());
      }
    });
    String[] hosts = new String[nodes.size()];
    for (int i = 0; i < nodes.size(); ++i) {
      String topo = nodes.get(i).getKey();
      hosts[i] = topo.substring(topo.lastIndexOf('/') + 1);
    }
    return hosts;
  }

  @Override public RecordReader<LongWritable, Text> createRecordReader(
    InputSplit split, TaskAttemptContext context) {

    return new LineRecordReader();
  }

  /**
   * Returns splits containing one line of input each.
   */
  @Override public List<InputSplit> getSplits(JobContext context)
    throws IOException {

    List<InputSplit> splits = new ArrayList<InputSplit>();
    for (FileStatus status : listStatus(context)) {
      Path p = status.getPath();
      if (status.isDir()) {
        throw new IOException("Not a file: " + p);
      }
      LineRecordReader lr = new LineRecordReader();
      try {
        FileSplit fs = new FileSplit(p, 0, status.getLen(), new String[]{});
        if (context instanceof TaskAttemptContext) {
          lr.initialize(fs, (TaskAttemptContext) context);
        } else {
          TaskAttemptContext ctx = new TaskAttemptContext(
            context.getConfiguration(), new TaskAttemptID());
          lr.initialize(fs, ctx);
        }
        while (lr.nextKeyValue()) {
          LongWritable key = lr.getCurrentKey();
          Text line = lr.getCurrentValue();
          String[] hosts = getHosts(StringUtils.split(line.toString()),
                                    context.getConfiguration());
          fs = new FileSplit(p, key.get(), line.getLength(), hosts);
          splits.add(fs);
        }
      } finally {
        lr.close();
      }
    }
    return splits;
  }
}
