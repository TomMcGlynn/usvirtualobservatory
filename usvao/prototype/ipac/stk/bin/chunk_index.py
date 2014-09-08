#*************************************************************************
#
#  Copyright (c) 2014, California Institute of Technology, Pasadena,
#  California, under cooperative agreement 0834235 between the California
#  Institute of Technology and the National Science  Foundation/National
#  Aeronautics and Space Administration.
#
#  All rights reserved.
#
#  Redistribution and use in source and binary forms, with or without
#  modification, are permitted provided that the following conditions
#  of this BSD 3-clause license are met:
#
#  1. Redistributions of source code must retain the above copyright
#  notice, this list of conditions and the following disclaimer.
#
#  2. Redistributions in binary form must reproduce the above copyright
#  notice, this list of conditions and the following disclaimer in the
#  documentation and/or other materials provided with the distribution.
#
#  3. Neither the name of the copyright holder nor the names of its
#  contributors may be used to endorse or promote products derived from
#  this software without specific prior written permission.
#
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
.
#  This software was developed by the Infrared Processing and Analysis
#  Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
#  funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
#  501(c)(3) organization registered in the District of Columbia and a
#  collaborative effort of the Association of Universities for Research
#  in Astronomy (AURA) and the Associated Universities, Inc. (AUI).
#
#************************************************************************/



#! /usr/bin/env python
#
# High level script for managing the HDFS file copies and map-reduce
# jobs required to create/incrementally update chunk index files.

import logging, logging.handlers
import optparse
import os
import os.path
import sys
from textwrap import dedent

from ipac.stk.hadoop_utils import Hadoop
from ipac.stk.chunk_index import ChunkIndexer


# -- Command line interface --------

hadoop_options = {
  'tiny':   ['-S', '1',
             '-s', '18', # 10 deg
             '-w', '2.5',
             '-o', '20.0',
            ],
  'small':  ['-S', '6',
             '-s', '6', # 5 deg
             '-w', '0.5',
             '-o', '5.0',
            ],
  'medium': ['-S', '8',
             '-s', '30',     # 0.75 deg
             '-w', '0.0834', # 5 arcmin
             '-o', '0.1667', # 10 arcmin
            ],
  'large':  ['-S', '15',
             '-s', '40',     # 0.3 deg
             '-w', '0.0167', # 1 arcmin
             '-o', '0.05',   # 3 arcmin
            ],
}

def build_options(conf, columns):
    """Builds a list of command line options for chunk generation.
    """
    options = list(hadoop_options[conf.size])
    if conf.num_stripes != None:
       options[1] = str(conf.num_stripes)
    if conf.num_sub_stripes_per_stripe != None:
       options[3] = str(conf.num_sub_stripes_per_stripe)
    if conf.lane_width != None:
       options[5] = repr(conf.lane_width)
    if conf.overlap != None:
        options[7] = repr(conf.overlap)
    if conf.binary:
        options.append('-b')
    if conf.zipped:
        options.append('-z')
    if conf.ignore_trailing:
        options.append('-i')
    if conf.wrap_theta:
        options.append('-W')
    if conf.columns:
        options.extend(['-C', conf.columns])
    if conf.type_spec:
        options.extend(['-t', conf.type_spec])
    if conf.unit_spec:
        options.extend(['-u', conf.unit_spec])
    options.extend(['-c', columns,
                    '-K', conf.uik_column,
                    '-T', conf.theta_column,
                    '-P', conf.phi_column,
                    '-d', conf.delimiter,
                    '-e', conf.escape,
                   ])
    return options

def main():
    usage = """\
usage:  %prog [options] create <project> <dataset> <output dir>
                        <columns> <input file 1> <input file 2> ...

        %prog [options] add <project> <dataset> <output dir>
                        <load id> <load version>
                        <columns> <input file 1> <input file 2> ...

        %prog [options] remove <project> <dataset> <output dir>
                        <load id> [<load version>]

        Creates and merges chunk index files to allow for efficient
        incremental index generation.

        %prog create:
        -------------

        Creates a chunk index for the specified data set. This option
        should be used for one-shot indexing operations, that is, for
        datasets that will not be incrementally loaded.

        The data comes from a list of CSV files <input file 1>,
        <input file 2> ....  The <columns> argument should be set to a
        comma separated list of the column names present in the input
        files. These must contain at least 3 columns: a unique integer key
        column with values between -2^63 and 2^63 - 1, and longitude and
        latitude angle columns. The latter are both expected to have
        floating point values in decimal degrees. Longitude angles are
        expected to lie in the range [0.0, 360.0) degrees, and latitude
        angles in the range [-90.0, 90.0] degrees.

        The input files are indexed, resulting in a chunk index file
        named 'index.ci' and a set of chunk files stored in
        '<output dir>/<project>/<dataset>/'.

        %prog add:
        ----------

        Adds a new version (<load_version>) of a load identified by
        <load_id> to the system. The load may or may not have previous
        versions available; it is an error to attempt to overwrite an
        existing version of a load.

        First, '<output dir>/<project>/<dataset>' is examined to see if
        it has been prepped for incremental chunk index generation.
        If not, two sub-directories are created: 'loads' to hold chunk
        indexes for individual loads, and 'merges' to hold chunk indexes
        that are composites of one or more loads. Finally, an sqlite3
        database for metadata tracking is created in
        '<output dir>/<project>/<dataset>/dataset.db'.

        The load data comes from a list of CSV files <input file 1>,
        <input file 2> ...., where <columns> should be set to a
        comma separated list of the column names in the input files.
        Data requirements are the same as for %prog create.

        The input files are indexed, resulting in a chunk index file
        named 'index.ci' and a set of chunk files stored in
        '<output dir>/<project>/<dataset>/loads/<load id>/<load version>/'.
        A record of each generated chunk is also stored in the
        metadata database.

        Next, the most recent version of every load is combined into
        a merge chunk index named 'merge.ci'. It is stored in the
        '<output dir>/<project>/<dataset>/merges/<merge id>/' directory,
        where <merge id> is a serial counter tracked in dataset.db.
        <merge id> is incremented on every load/rollback operation for
        the dataset.

        The merging step intelligently reuses previously generated
        chunk files were it can, saving on both space and time. In
        particular, a merge M will hard link to chunk files in merge M-1
        and the new load for any chunks not merged during the creation of M.

        %prog remove:
        -------------

        Removes a particular version of a load, or an entire load. This
        is an irreversible operation unless the input files for the
        deleted load are available (in which case they can loaded back in
        with %prog add).

        First, the database.db metadata database is updated to indicate
        the removal. Next, a new merge chunk index consisting of the most
        recent loads is created, as for %prog add. If a non-current
        version of a load is being removed, then this step is skipped.

        Finally, relevant load chunk and chunk index files are deleted.

        Requirements:
        -------------

        Running this script requires a Hadoop 0.20.1 install to be
        available. The HADOOP_HOME environment variable must be set
        to the location of the install or the script will fail.
        """
    parser = optparse.OptionParser(usage)

    general = optparse.OptionGroup(parser, "General options")
    general.add_option(
        "-n", "--name-node", type="string", dest="name_node",
        default="file:///", help=dedent("""\
        Hadoop name node. A value of 'file:///' in tandem with
        --job-tracker=local allows chunk indexing operations to be run
        locally without access to a hadoop cluster. Otherwise, the value
        is expected to be of the form hdfs://<host name>:<port number>,
        identifying a hadoop name node (HDFS file server) to use when
        generating chunk indexes.
        """))
    general.add_option(
        "-j", "--job-tracker", type="string", dest="job_tracker",
        default="local", help=dedent("""\
        Hadoop map-reduce job tracker node. A value of 'local' in tandem
        with --name-node=file:/// allows chunk indexing operations to be run
        locally without access to a hadoop cluster. Otherwise, the value
        is expected to be of the form <host name>:<port number>,
        identifying a hadoop job tracker to use for launching distributed
        chunk index generation.
        """))
    general.add_option(
        "-l", "--log_file", type="string", dest="log_file",
        help=dedent("""\
        Name of output log file. If unspecified, output is logged
        to standard out. If the file ends with a .conf extension,
        the file is assumed to be a standard python logging
        configuration file instead of the actual output log file.
        """))
    general.add_option(
        "-L", "--hadoop_log_file", type="string", dest="hadoop_log_file",
        help=dedent("""\
        Name of hadoop output log file. If unspecified, hadoop output is
        logged to standard out.
        """))
    general.add_option(
        "-D", "--define", type="string", dest="defines", action="append",
        help=dedent("""\
        Values should be in the <key>=<value> format. This option can be
        used as many times as necessary to pass a set of properties through
        to hadoop.
        """))
    parser.add_option_group(general)

    addcreate = optparse.OptionGroup(
        parser, "Options for chunk generation (add/create)")
    addcreate.add_option(
        "-o", "--overlap", type="float", dest="overlap",
        help="Chunk overlap radius (deg).")
    addcreate.add_option(
        "-w", "--lane-width", type="float", dest="lane_width",
        help="Minimum longitude angle width of a lane in a chunk (deg).")
    addcreate.add_option(
        "-S", "--num-stripes", type="int", dest="num_stripes",
        help="The number of latitude angle stripes in the chunk index.")
    addcreate.add_option(
        "-s", "--num-ss-per-stripe", type="int",
        dest="num_sub_stripes_per_stripe", help=dedent("""\
        Number of latitude angle sub-stripes per stripe in the chunk
        index."""))
    addcreate.add_option(
        "-U", "--uik-column", type="string", dest="uik_column",
        default="cntr", help=dedent("""\
        Name or index (0 based) of the longitude angle (e.g. right ascension)
        column in the input CSV files; defaults to %default."""))
    addcreate.add_option(
        "-T", "--theta-column", type="string", dest="theta_column",
        default="ra", help=dedent("""\
        Name or index (0 based) of the longitude angle (e.g. right ascension)
        column in the input CSV files; defaults to %default."""))
    addcreate.add_option(
        "-P", "--phi-column", type="string", dest="phi_column",
        default="dec", help=dedent("""\
        Name or index (0 based) of the latitude angle (e.g. declination)
        column in the input CSV files; defaults to %default."""))
    addcreate.add_option(
        "-k", "--kind", type="choice",
        choices=["tiny", "small", "medium", "large"], dest="size",
        default="small", help=dedent("""\
        The kind of data-set (in terms of size) being indexed. This determines
        reasonable defaults for the various chunk indexing parameters. As a
        rough guide: 'tiny' sets are those with < ~2e5 entries, 'small' sets
        have < ~2e7 entries and 'medium' sets < ~2e9 entries. 'large' is for
        even larger data-sets. Each of these size classes has an associated
        default overlap radius (override with --overlap), lane-width (override
        with --lane-width), number of stripes and sub-stripes per stripe
        (override with --num-stripes and --num-sub-stripes-per-stripe). The
        most commonly useful override is --overlap, which dictates the maximum
        search radius for spatial matches that can be performed using the chunk
        index. The default overlap radii follow. 'tiny': 20.0 deg, 'small': 5.0
        deg, 'medium': 0.1667 deg (10 arcmin), 'large': 0.05 deg (3 arcmin).
        By default, --kind is %default."""))
    addcreate.add_option(
        "-C", "--columns", type="string", dest="columns",
        help=dedent("""\
        A comma separated list of columns (names or 0-based indices) to
        include in the chunk files.  By default, only the unique integer
        key and longitude/latitude angle columns are included.
        """))
    addcreate.add_option(
        "-t", "--type-spec", type="string", dest="type_spec",
        help=dedent("""\
        An optional comma separated list of IPAC ASCII data type strings
        for columns in the input file(s).
        """))
    addcreate.add_option(
        "-u", "--unit-spec", type="string", dest="unit_spec",
        help=dedent("""\
        An optional comma separated list of unit strings for columns in
        the input file(s).
        """))
    addcreate.add_option(
        "-d", "--delimiter", type="string", dest="delimiter", default="|",
        help=dedent("""\
        The ASCII character used to delimit fields in the input records.
        The default is %default.
        """))
    addcreate.add_option(
        "-e", "--escape", type="string", dest="escape", default="\\",
        help=dedent("""\
        The ASCII character used to escape delimiter characters occurring
        inside fields. The default is %default.
        """))
    addcreate.add_option(
        "-i", "--ignore-trailing", action="store_true", dest="ignore_trailing",
        help=dedent("""\
        Ignore the trailing delimiter character in a record. If unspecified,
        a trailing delimiter indicates a trailing empty field.
        """))
    addcreate.add_option(
        "-W", "--wrap-theta", action="store_true", dest="wrap_theta",
        help="Range reduce longitude angles to lie in range [0, 360) deg.")
    addcreate.add_option(
        "-b", "--binary", action="store_true", dest="binary",
        help=dedent("""\
        When this option is specified, the --columns option is ignored,
        and the only columns stored are the unique integer key, longitude,
        and latitude columns.  Furthermore, values are stored in binary
        form to save space and IO bandwidth.
        """))
    addcreate.add_option(
        "-z", "--zipped", action="store_true", dest="zipped",
        help=dedent("""\
        When this option is specified, individual lanes in a chunk are
        compressed with zlib (http://www.zlib.net/).
        """))

    parser.add_option_group(addcreate)

    (conf, inputs) = parser.parse_args()

    # Input validation
    if len(inputs) < 5:
        parser.error("Not enough arguments")
    command = inputs[0]
    if command not in ('create', 'add', 'remove'):
        parser.error(str.format("Invalid chunk indexing operation {0}",
                     command))
    # Setup logging
    if conf.log_file != None:
        if os.path.splitext(conf.log_file)[1] == ".conf":
            logging.config.fileConfig(conf.log_file)
        else:
            logging.basicConfig(
                filename=conf.log_file, level=logging.DEBUG,
                format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    else:
        logging.basicConfig(
            stream=sys.stdout, level=logging.DEBUG,
            format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    try:
        hadoop = Hadoop(conf.name_node, conf.job_tracker,
                        conf.defines, conf.hadoop_log_file)
        incremental = command != 'create'
        indexer = ChunkIndexer(inputs[1], inputs[2], inputs[3], incremental)
        if command == 'create':
            if len(inputs) < 6:
                parser.error("Not enough arguments")
            options = build_options(conf, inputs[4])
            indexer.create_chunks(hadoop, inputs[5:], options)
        elif command == 'add':
            if len(inputs) < 8:
                parser.error("Not enough arguments")
            options = build_options(conf, inputs[6])
            indexer.add_chunks(hadoop, inputs[4], inputs[5],
                               inputs[7:], options)
        elif command == 'remove':
            if len(inputs) not in (5,6):
                parser.error("Invalid number of arguments")
            load_version = inputs[5] if len(inputs) == 6 else None
            indexer.remove_load(hadoop, inputs[4], load_version)
    finally:
        logging.shutdown()


if __name__ == "__main__":
    main()

