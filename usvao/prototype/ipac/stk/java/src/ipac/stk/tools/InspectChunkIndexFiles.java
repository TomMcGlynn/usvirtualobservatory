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



package ipac.stk.tools;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;

import ipac.stk.io.ChunkFile;
import ipac.stk.io.ChunkIndex;
import ipac.stk.json.JSONOutput;
import ipac.stk.json.JSONWritable;

/**
 * Simple utility class for printing a JSON representation of one
 * or more chunk files to standard out.
 */
public final class InspectChunkIndexFiles {
  private InspectChunkIndexFiles() { }

  private static Options buildOptions() {
    Options opts = new Options();
    Option help = OptionBuilder.withLongOpt("help")
      .withDescription("Print usage information")
      .create("h");
    Option condensed = OptionBuilder.withLongOpt("condensed")
      .withDescription("Produce condensed (rather than pretty) JSON output")
      .create("c");
    Option verbose = OptionBuilder.withLongOpt("verbose")
      .withDescription("Produce detailed information about each file")
      .create("v");
    opts.addOption(help);
    opts.addOption(condensed);
    opts.addOption(verbose);
    return opts;
  }

  private static void printUsage(Options options, OutputStream out) {
    HelpFormatter help = new HelpFormatter();
    PrintWriter writer = new PrintWriter(out);
    final int width = 80;
    final int leftPad = 4;
    final int descPad = 8;
    help.printHelp(
      writer, width,
      "inspect_ci_files [options] <path 1> ... <path N>",
      "Print information about one or more chunk files " +
      "or chunk index files to standard out",
      options, leftPad, descPad, "");
    writer.flush();
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Options opts = buildOptions();
    GenericOptionsParser parser = new GenericOptionsParser(conf, opts, args);
    CommandLine cmd = parser.getCommandLine();
    if (cmd.hasOption("help")) {
      printUsage(opts, System.out);
      System.exit(0);
    }
    boolean verbose = cmd.hasOption("verbose");
    JSONOutput.Options fmtopt = JSONOutput.PRETTY_ASCII;
    if (cmd.hasOption("condensed")) {
      fmtopt = JSONOutput.ASCII;
    }
    JSONOutput out = new JSONOutput(System.out, fmtopt);
    String[] remArgs = parser.getRemainingArgs();
    List<String> names = new ArrayList<String>();
    for (String name : remArgs) {
      if (name.endsWith(".ci") || name.endsWith(".cf")) {
        names.add(name);
      }
    }
    if (names.size() != 1) {
      out.array();
    }
    for (String name : names) {
      Path p = new Path(name);
      if (name.endsWith(".cf")) {
        ChunkFile f = new ChunkFile(p, p.getFileSystem(conf));
        f.writeJSON(out, verbose);
      } else {
        ChunkIndex i = new ChunkIndex(p, p.getFileSystem(conf));
        i.writeJSON(out, verbose);
      }
    }
    out.finishAll();
    System.out.write('\n');
    System.out.flush();
  }
}
