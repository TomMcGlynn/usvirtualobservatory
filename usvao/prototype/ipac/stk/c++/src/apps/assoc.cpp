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



/** \file
    \brief Spatial matching tool.
    \author Serge Monkewitz
  */
#include "ipac/stk/config.h"
#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif

#include <cmath>
#include <cstdlib>
#include <algorithm>
#include <exception>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <sstream>
#include <vector>

#include "boost/filesystem.hpp"
#include "boost/lexical_cast.hpp"
#include "boost/program_options.hpp"
#include "boost/regex.hpp"
#include "boost/shared_ptr.hpp"
#include "boost/timer.hpp"

#include "ipac/stk/assoc/ChunkIndexMatcher.h"
#include "ipac/stk/assoc/constants.h"
#include "ipac/stk/assoc/Matchables.h"
#include "ipac/stk/assoc/units.h"
#include "ipac/stk/except.h"
#include "ipac/stk/json.h"
#include "ipac/stk/table/IPACTableReader.h"
#include "ipac/stk/table/Record.h"
#include "ipac/stk/util/string.h"

using std::cerr;
using std::cout;
using std::endl;
using std::exit;
using std::ios;
using std::ios_base;
using std::make_pair;
using std::min;
using std::max;
using std::numeric_limits;
using std::ofstream;
using std::ostream;
using std::ostringstream;
using std::pair;
using std::setw;
using std::sort;
using std::sort_heap;
using std::sqrt;
using std::streamsize;
using std::string;
using std::vector;

using boost::shared_ptr;

using namespace ipac::stk::assoc;
using ipac::stk::json::FormattingOptions;
using ipac::stk::json::JSONOutput;
using ipac::stk::json::Value;
using ipac::stk::table::IPACTableReader;
using ipac::stk::table::Record;

namespace except = ipac::stk::except;
namespace fs = boost::filesystem;
namespace po = boost::program_options;
namespace util = ipac::stk::util;

namespace {

bool jsonOutput = false;
bool verbose = false;

FormattingOptions const & getFormattingOptions() {
  return jsonOutput ? JSONOutput::PRETTY_ASCII : JSONOutput::IPAC_SVC;
}

// -- Error reporting --------

void printError(string const & msg) {
  JSONOutput o(cout, getFormattingOptions());
  o.object();
  o.pair("stat", "ERROR");
  o.pair("msg", msg);
  o.close();
  cout << endl;
}

void printError(Value const & properties,
                except::Exception const & ex)
{
  JSONOutput o(cout, getFormattingOptions());
  o.object();
  o.pair("stat", "ERROR");
  o.pair("msg", ex.getMessage());
  o.key("except");
  o.object();
  o.pair("type", ex.getTypeName());
  except::SystemError const * se =
    dynamic_cast<except::SystemError const *>(&ex);
  if (se != 0) {
    o.pair("errno", se->getErrno());
  }
  o.pair("file", ex.getFile());
  o.pair("line", ex.getLine());
  o.pair("func", ex.getFunction());
  o.close();
  if (properties.size() > 0) {
    o.pair("props", properties);
  }
  o.close();
  cout << endl;
}

// -- Creating program option spec and basic input verification --------

void buildOptions(po::options_description & options) {
  po::options_description general("General options", 100);
  general.add_options()
    ("help,h", po::bool_switch(), "print usage help")
    ("json,j", po::bool_switch(),
     "Print output in JSON format instead of IPAC SVC format")
    ("verbose,v", po::bool_switch(),
     "Print detailed statistics about execution time and match results");

  po::options_description input("Input", 100);
  input.add_options()
    ("table,t", po::value<string>(),
      "Input table containing circles or ellipses.")
    ("input-format,f", po::value<string>()->default_value("ipac"),
      "Input table format; supported formats are: 'ipac'")
    ("chunk-index,i", po::value<string>(),
      "Chunk index file for data-set to associate with")
    ("theta,l", po::value<string>()->default_value("ra"),
      "Name(s), index or value of longitude angle column in the input table.  "
      "In the absence of an angular unit specification, degrees are assumed.")
    ("phi,L", po::value<string>()->default_value("dec"),
      "Name(s), index or value of latitude angle column in the input table.  "
      "In the absence of an angular unit specification, degrees are assumed.")
    ("semi-minor-axis,m", po::value<string>(),
      "Name(s), index or value of semi-minor axis length column in the input "
      "table.  In the absence of an angular unit specification, arcsec are "
      "assumed.")
    ("semi-major-axis,M", po::value<string>(),
      "Name(s), index or  value of semi-major axis length column in the input "
      "table.  In the absence of an angular unit specification, arcsec are "
      "assumed.  If --semi-minor-axis is equal to --semi-major-axis or "
      "--axis-ratio is omitted/unavailable or set to 1, then search regions "
      "are circular rather than elliptical.")
    ("axis-ratio,a", po::value<string>(),
      "Name(s), index or value of a column in the input table that specifies "
      "the minor to major axis length ratio, a unit-less quantity that must "
      "be <= 1.")
    ("axis-angle,A", po::value<string>(),
      "Name(s), index or value of major axis angle column in the input table; "
      "ignored if search regions are circular.  If unspecified or missing "
      "from the input table, defaults to 0 for elliptical search regions.  "
      "In the absence of an angular unit specification, degrees are assumed.")
    ("case-insensitive,I", po::bool_switch(),
     "Indicates that input table column names are case-insensitive")
    ("max-extent,e", po::value<string>(),
     "Maximum extent (radius/semi-major axis length) of input "
     "circles/ellipses. An optional angular unit suffix is allowed; if "
     "omitted, arcsec are assumed. Note that the chunk index to match "
     "against also has an upper bound on the search extents it can support. "
     "The maximum actually enforced is therefore the minimum of the user "
     "supplied and chunk index maxima.");

  po::options_description output("Output", 100);
  output.add_options()
    ("match-table,T", po::value<string>(),
      "Match output file name")
    ("max-matches,K", po::value<int>()->default_value(-1),
      "Only record the K closest matches for each record in the input "
      "table.  A negative value indicates that all matches are to be "
      "recorded and a value of zero will cause no matches to be recorded.  "
      "Note that the total number of matches is always reported.  By default, "
      "all matches are recorded.")
    ("output-format,F", po::value<string>()->default_value("ipac"),
      "Output table format; supported formats are: 'ipac'")
    ("rowid,r", po::value<string>(),
      "The name of a column containing an identifier for each row "
      "in the input table. If this column is not present, a sequential "
      "integer id is assigned to each input row, starting from 1. Note that "
      "this column name must be listed in the --columns / --no-match-columns "
      "specification or it will not appear in the match / no-match output "
      "table. The rowid column is automatically recorded in the bad row "
      "output table.")
    ("columns,c", po::value<string>()->default_value("*"),
      "Comma separated list of input table column names or 1-based indexes "
      "to output in the match table; specify * to output all columns.  Use "
      "'original_name_or_index AS new_name' to rename columns, where the "
      "AS keyword is case-insensitive.  For example, "
      "--columns='cntr, ra as ra1, 3 AS dec1' will output columns cntr, "
      "ra, and the 3d column from the input table as columns named "
      "cntr, ra1, and dec1 in the match table.")
    ("index-columns,C", po::value<string>()->default_value("*"),
      "Comma separated list of chunk index column names or 1-based indexes "
      "to output in the match table; specify * to output all available "
      "columns.  Use 'original_name_or_index AS new_name' to rename "
      "columns.  Note that columns 1, 2, and 3 always correspond to "
      "a unique integer key, longitude angle, and latitude angle.")
    ("prefix,p", po::value<string>()->default_value(""),
      "Prefix for columns in the input table")
    ("index-prefix,P", po::value<string>()->default_value(""),
      "Prefix for columns in the input index")
    ("sort,s", po::bool_switch(),
      "Sort matches by match distance prior to output.")
    ("bad-rows,b", po::value<string>(),
      "Skip bad rows and store them to the specified file. These are rows "
      "with illegal values, e.g. values that are out of bounds or do not "
      "correspond to the column data type.");

  po::options_description unmatched("Unmatched record output", 100);
  unmatched.add_options()
    ("no-match-table,n", po::value<string>(),
      "Name of output file for unmatched table records.")
    ("no-match-columns,N", po::value<string>()->default_value("*"),
      "Comma separated list of input table column names or 1-based indexes "
      "to output for unmatched table records; specify * to output all "
      "columns.  Use 'original_name_or_index AS new_name' to rename columns.");

  options.add(general).add(input).add(output).add(unmatched);
}

void requireOption(po::variables_map const & vm, string const & opt) {
  if (vm.count(opt) != 1 && !vm[opt].defaulted()) {
    printError("The --" + opt + " option must be specified exactly once.");
    exit(EXIT_FAILURE);
  }
}

void verifyOptions(po::variables_map const & vm,
                   po::options_description const & options,
                   char const * const program) {
  if (vm["help"].as<bool>()) {
    cout << program << " [options]\n"
"\n"
"    Matches a table of circles/ellipses with a spatially indexed data set.\n"
"\n"
"    For each circles/ellipse, all index entries (or the K-closest entries)\n"
"    within the circles/ellipse are written to a match output table.  In\n"
"    addition to allowing columns from the input table and index files\n"
"    to be selected for output, the following columns are computed for each\n"
"    match:\n"
"\n"
"    - dist : Angular separation between circle/ellipse center\n"
"             and index entry (arcsec).\n"
"    - pang : Position angle between circle/ellipse center\n"
"             and index entry (E of N, deg).\n"
"    - nm   : Total number of index entries matching circle/ellipse.\n"
"\n"
"    A collection of input options (--theta, --phi, --semi-minor-axis,\n"
"    --semi-major-axis, --axis-ratio, --axis-angle) allow the user to\n"
"    specify the various search parameters.  In general, these can either\n"
"    correspond to constant values, or to columns in the user specified\n"
"    input table.  Numerical values may optionally be suffixed with an\n"
"    angular unit specification; if not present, parameter specific defaults\n"
"    apply.  Columns can be specified using :n, where n is a 1 based column\n"
"    index, or as a comma separated list of potential column names, where\n"
"    the first name corresponding to an existing input column identifies the\n"
"    column from which values will be read.  Default units are assumed\n"
"    unless the column has a unit specification.\n"
"\n"
"    To create spatially indexed data sets from database table dumps,\n"
"    use the chunk_index.py utility.\n"
"\n";
    cout << options;
    cout << endl;
    exit(EXIT_SUCCESS);
  }
  requireOption(vm, "table");
  requireOption(vm, "chunk-index");
  if (vm.count("match-table") != 1) {
    if (vm["max-matches"].as<int>() != 0) {
      printError("--match-table is a required parameter unless "
                 "--max-matches=0.");
      exit(EXIT_FAILURE);
    }
  }
  // For now, only IPAC tables are supported as inputs.
  string fmt = vm["input-format"].as<string>();
  if (fmt != "ipac") {
    printError(fmt + " is not a supported --input-format");
    exit(EXIT_FAILURE);
  }
  // For now, output using IPAC ASCII format only.
  fmt = vm["output-format"].as<string>();
  if (fmt != "ipac") {
    printError(fmt + " is not a supported --output-format");
    exit(EXIT_FAILURE);
  }
}

// -- Parsing output column specs and writing out results --------

shared_ptr<ofstream> const openFileForWriting(string const & path) {
  shared_ptr<ofstream> f(new ofstream(path.c_str()));
  if (f->fail()) {
    throw IPAC_STK_EXCEPT(except::IOError, 0, "Failed to open file " +
                          path + " for writing");
  }
  f->exceptions(ofstream::eofbit | ofstream::failbit | ofstream::badbit);
  f->fill(' ');
  f->setf(ios::left);
  return f;
}

class CIOutColumn {
public:
  CIOutColumn(string const & name, ChunkIndex::Column const & col) :
    _name(name), _column(col)
  {
    size_t w = max(name.size(), static_cast<size_t>(col.getWidth()));
    w = max(w, col.getType().size());
    _width = static_cast<int>(max(w, col.getUnits().size()));
  }
  string const & getName() const { return _name; }
  string const & getType() const { return _column.getType(); }
  string const & getUnits() const { return _column.getUnits(); }
  string const & getNull() const { return NULL_VALUE; }
  int getWidth() const { return _width; }
  ChunkIndex::Column const & getColumn() const { return _column; }

  void setWidth(int width) {
    _width = width;
  }
private:
  static string const NULL_VALUE;
  string _name;
  int _width;
  ChunkIndex::Column _column;
};

string const CIOutColumn::NULL_VALUE;

class IPACOutColumn {
public:
  IPACOutColumn(string const & name, IPACTableReader::Column const & col) :
    _name(name), _column(col)
  {
    _width = max(static_cast<int>(name.size()), col.getWidth());
  }
  string const & getName() const { return _name; }
  string const & getType() const { return _column.getType(); }
  string const & getUnits() const { return _column.getUnits(); }
  string const & getNull() const { return _column.getNull(); }
  int getWidth() const { return _width; }
  IPACTableReader::Column const & getColumn() const { return _column; }
private:
  string _name;
  int _width;
  IPACTableReader::Column _column;
};

boost::regex const COL_REGEX("^\\s*(\\S+)\\s*$");
boost::regex const COL_AS_REGEX("^\\s*(\\S+)\\s+[aA][sS]\\s+(\\S+)\\s*$");

/** Maps a column specification to a vector of output columns.
  */
vector<IPACOutColumn> const parseColumnSpec(
  IPACTableReader::Metadata const & meta,
  string const & columnSpec,
  string const & prefix,
  string const & rowid
) {
  typedef vector<IPACTableReader::Column>::const_iterator InColIter;
  typedef vector<string>::const_iterator StringIter;

  vector<IPACOutColumn> out;
  vector<IPACTableReader::Column> in(meta.getColumns());
  if (util::trim(columnSpec) == "*") {
    if (!meta.hasColumn(rowid) && rowid.size() > 0) {
      out.push_back(IPACOutColumn(
        prefix + rowid,
        IPACTableReader::Column(rowid, "long", "", "", -1, 1, 13)));
    }
    for (InColIter i(in.begin()), e(in.end()); i != e; ++i) {
      out.push_back(IPACOutColumn(prefix + i->getName(), *i));
    }
  } else {
    vector<string> cols = util::split(columnSpec, ',');
    for (StringIter i(cols.begin()), e(cols.end()); i != e; ++i) {
      boost::smatch results;
      string name;
      string inSpec;
      if (boost::regex_match(*i, results, COL_AS_REGEX)) {
        name = results.str(2);
        inSpec = results.str(1);
      } else if (boost::regex_match(*i, results, COL_REGEX)) {
        inSpec = results.str(1);
        name = inSpec;
      } else {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, *i + " is not a valid "
                              "column name, index or renaming expression");
      }
      if (!meta.hasColumn(inSpec)) {
        if (inSpec == rowid) {
          out.push_back(IPACOutColumn(
            prefix + name,
            IPACTableReader::Column(rowid, "long", "", "", -1, 1, 13)));
          continue;
        } else {
          int which = 0;
          try {
            which = boost::lexical_cast<int>(inSpec) - 1;
          } catch (boost::bad_lexical_cast & blc) {
            throw IPAC_STK_EXCEPT(except::InvalidParameter,
                                  meta.getPath().file_string() + " does not "
                                  " contain column " + inSpec);
          }
          if (which < 0 || which >= static_cast<int>(in.size())) {
            throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
              "%s does not contain a column with index %d: the number of "
              "available columns is %d.", meta.getPath().file_string().c_str(),
              which + 1, static_cast<int>(in.size())));
          }
          inSpec = in[static_cast<size_t>(which)].getName();
          name = inSpec;
        }
      }
      out.push_back(IPACOutColumn(prefix + name, meta.getColumn(inSpec)));
    }
  }
  return out;
}

/** Maps a column specification to a vector of output columns.
  */
vector<CIOutColumn> const parseColumnSpec(ChunkIndex const & index,
                                          string const & columnSpec,
                                          string const & prefix)
{
  typedef vector<ChunkIndex::Column>::const_iterator InColIter;
  typedef vector<string>::const_iterator StringIter;

  vector<CIOutColumn> out;
  vector<ChunkIndex::Column> in(index.getColumns());
  if (util::trim(columnSpec) == "*") {
    for (InColIter i(in.begin()), e(in.end()); i != e; ++i) {
      out.push_back(CIOutColumn(prefix + i->getName(), *i));
    }
  } else {
    vector<string> cols = util::split(columnSpec, ',');
    for (StringIter i(cols.begin()), e(cols.end()); i != e; ++i) {
      boost::smatch results;
      string name;
      string inSpec;
      if (boost::regex_match(*i, results, COL_AS_REGEX)) {
        name = results.str(2);
        inSpec = results.str(1);
      } else if (boost::regex_match(*i, results, COL_REGEX)) {
        inSpec = results.str(1);
        name = inSpec;
      } else {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, *i + " is not a valid "
                              "column name, index or renaming expression");
      }
      if (!index.hasColumn(inSpec)) {
        int which = 0;
        try {
          which = boost::lexical_cast<int>(inSpec) - 1;
        } catch (boost::bad_lexical_cast & blc) {
          throw IPAC_STK_EXCEPT(except::InvalidParameter,
                                index.getPath().file_string() + " does not "
                                " contain column " + inSpec);
        }
        if (which < 0 || which >= static_cast<int>(in.size())) {
          throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
            "%s does not contain a column with index %d: the number of "
            "available columns is %d.", index.getPath().file_string().c_str(),
            which + 1, static_cast<int>(in.size())));
        }
        inSpec = in[static_cast<size_t>(which)].getName();
        name = inSpec;
      }
      out.push_back(CIOutColumn(prefix + name, index.getColumn(inSpec)));
    }
  }
  return out;
}

void massageBinaryIndexColumns(vector<CIOutColumn> & columns) {
  typedef vector<CIOutColumn>::iterator Iter;
  // Don't have string data available for binary index columns,
  // so print out theta/phi to double precision limits.
  for (Iter i(columns.begin()), e(columns.end()); i != e; ++i) {
    if (i->getColumn().getIndex() == 1 || i->getColumn().getIndex() == 2) {
      i->setWidth(max(i->getWidth(), 24));
    }
  }
}

string const formatColumnList(string const & list) {
  vector<string> names(util::split(list, ','));
  string s;
  switch (names.size()) {
    case 0:
      break;
    case 1:
      s = "'" + names[0] + "'";
      break;
    case 2:
      s = "'" + names[0] + "' or '" + names[1] + "'";
      break;
    default:
      s = "'" + names[0] + "'";
      for (size_t i = 1; i < names.size(); ++i) {
        if (i == names.size() - 1) {
          s += ", or '";
        } else {
          s += ", '";
        }
        s += names[i] + "'";
      }
  }
  return s;
}

struct Variables {
  Variables(po::variables_map const & vm,
            IPACTableReader const & reader,
            ChunkIndexMatcher const & matcher,
            Value & props);
  ~Variables();

  shared_ptr<IPACColOrVal> theta;
  shared_ptr<IPACColOrVal> phi;
  shared_ptr<IPACColOrVal> smia;
  shared_ptr<IPACColOrVal> smaa;
  shared_ptr<IPACColOrVal> ratio;
  shared_ptr<IPACColOrVal> angle;
  shared_ptr<IPACColOrVal> radius;
  vector<Record> rows;
  shared_ptr<vector<Record> > badRows;
  shared_ptr<ofstream> badRowTable;
  shared_ptr<ofstream> matchTable;
  shared_ptr<ofstream> noMatchTable;
  vector<IPACOutColumn> tableColumns;
  vector<IPACOutColumn> noMatchColumns;
  vector<CIOutColumn> indexColumns;
  string prefix;
  string rowid;
  size_t numMatched;
  size_t numUnmatched;
  size_t numMatches;
  size_t numRecordedMatches;
  size_t numBadRows;
  double minDist;
  double maxDist;
  double sumDist;
  double sumSquareDist;
  double maxRadius;
  int maxMatches;
  bool elliptical;
  bool binary;
  bool sortByDist;
};

Variables::Variables(po::variables_map const & vm,
                     IPACTableReader const & reader,
                     ChunkIndexMatcher const & matcher,
                     Value & props) :
  theta(new IPACColOrVal(vm, "theta", reader, RAD_PER_DEG)),
  phi(new IPACColOrVal(vm, "phi", reader, RAD_PER_DEG)),
  smia(new IPACColOrVal(vm, "semi-minor-axis", reader, RAD_PER_ARCSEC)),
  smaa(new IPACColOrVal(vm, "semi-major-axis", reader, RAD_PER_ARCSEC)),
  ratio(new IPACColOrVal(vm, "axis-ratio", reader)),
  angle(new IPACColOrVal(vm, "axis-angle", reader, 0.0, RAD_PER_DEG)),
  radius(),
  rows(),
  badRows(),
  matchTable(),
  noMatchTable(),
  tableColumns(),
  noMatchColumns(),
  indexColumns(),
  prefix(vm["prefix"].as<string>()),
  rowid(),
  numMatched(0),
  numUnmatched(0),
  numMatches(0),
  numRecordedMatches(0),
  numBadRows(0),
  minDist(numeric_limits<double>::max()),
  maxDist(numeric_limits<double>::min()),
  sumDist(0.0),
  sumSquareDist(0.0),
  maxRadius(matcher.getChunkIndex().getOverlapDeg() * RAD_PER_DEG),
  maxMatches(vm["max-matches"].as<int>()),
  elliptical(true),
  binary(matcher.getChunkIndex().isBinary()),
  sortByDist(vm["sort"].as<bool>())
{
  if (vm.count("rowid") != 0) {
    rowid = vm["rowid"].as<string>();
    if (reader.getMetadata().hasColumn(rowid)) {
      rowid = ""; // don't generate rowid
    }
  }
  if (vm.count("max-extent") != 0) {
    static boost::regex const VALUE_REGEX(
      "^\\s*([+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?)\\s*(.*)$");
    string maxExtent = vm["max-extent"].as<string>();
    boost::smatch m;
    if (boost::regex_match(maxExtent, m, VALUE_REGEX)) {
      double val = boost::lexical_cast<double>(m.str(1));
      double rpv = radiansPer(m.str(2), RAD_PER_ARCSEC);
      if (rpv == 0.0) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, "The --max-extent "
            "option value " + maxExtent + " has an invalid unit suffix.");
      }
      maxRadius = min(maxRadius, val * rpv);
    } else {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, "The --max-extent "
          "option value " + maxExtent + " is not a valid number.");
    }
  }
  string p = reader.getMetadata().getPath().file_string();
  if (!theta->isAvailable()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
      "Required column %s not found in input table %s",
      formatColumnList(vm["theta"].as<string>()).c_str(), p.c_str()));
  } else if (theta->isConstant()) {
    double v = theta->getConstantValue();
    if (v < 0.0 || v >= 2.0 * PI) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "The longitude angle for search circle/ellipse centers "
        "must be in range [0, 360); got %.17g deg", v * DEG_PER_RAD));
    }
  }
  if (!phi->isAvailable()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
      "Required column %s not found in input table %s",
      formatColumnList(vm["phi"].as<string>()).c_str(), p.c_str()));
  } else if (phi->isConstant()) {
    double v = phi->getConstantValue();
    if (v < -0.5 * PI || v > 0.5 * PI) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "The latitude angle for search circle/ellipse centers "
        "must be in range [-90, 90]; got %.17g deg", v * DEG_PER_RAD));
    }
  }
  if (vm.count("semi-minor-axis") == 0 && vm.count("semi-major-axis") == 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "At least one of "
                          "the --semi-minor-axis and --semi-major-axis "
                          "options must be specified");
  }
  if (!smia->isAvailable() && !smaa->isAvailable()) {
    if (vm.count("semi-minor-axis") == 0) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "Semi-major axis length column named %s not found in input table %s",
        formatColumnList(vm["semi-major-axis"].as<string>()).c_str(),
        p.c_str()));
    } else {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "Semi-minor axis length column named %s not found in input table %s",
        formatColumnList(vm["semi-minor-axis"].as<string>()).c_str(),
        p.c_str()));
    }
  }
  if (smia->isAvailable() && smaa->isAvailable() && ratio->isAvailable()) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "At most two of the "
      "--semi-minor-axis, --semi-major-axis, and --axis-ratio options may "
      "be specified.");
  }
  if (ratio->isAvailable()) {
    if (ratio->isConstant()) {
      double r = ratio->getConstantValue();
      if (r == 1.0) {
        elliptical = false;
        radius = smia->isAvailable() ? smia : smaa;
      } else if (r <= 0.0 || r > 1.0) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
          "The minor to major axis length ratio %g is negative, "
          "zero or greater than 1", r));
      } else if (smia->isAvailable() && smia->isConstant()) {
        double m = smia->getConstantValue();
        if (m < 0.0 || m / r > maxRadius) {
          throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
            "The specified semi-minor axis length %g arcsec and minor to "
            "major axis length ratio %g result in search ellipses with "
            "semi-major axis lengths greater than the allowed maximum of %g "
            "arcsec", m * ARCSEC_PER_RAD, r, maxRadius * ARCSEC_PER_RAD));
        }
      }
    }
  } else if (!smia->isAvailable()) {
    elliptical = false;
    radius = smaa;
  } else if (!smaa->isAvailable()) {
    elliptical = false;
    radius = smia;
  } else if (*smia == *smaa) {
    elliptical = false;
    radius = smaa;
  }
  if (angle->isConstant()) {
    double v = angle->getConstantValue();
    if (v < -2.0 * PI || v > 2.0 * PI) {
      throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
        "Major axis angle must be in range [-360, 360]; got %.17g deg",
        v * DEG_PER_RAD));
    }
  }
  props.add("theta", JSONOutput::toValue(*theta));
  props.add("phi", JSONOutput::toValue(*phi));
  if (elliptical) {
    if (smaa->isAvailable()) {
      if (smaa->isConstant()) {
        double v = smaa->getConstantValue();
        if (v < 0.0 || v > maxRadius) {
          throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
            "The specified semi-major axis length %g arcsec is negative "
            "or greater than the allowed maximum of %g arcsec",
            v * ARCSEC_PER_RAD, maxRadius * ARCSEC_PER_RAD));
        }
      }
      props.add("semi-major-axis", JSONOutput::toValue(*smaa));
    }
    if (smia->isAvailable()) {
      props.add("semi-minor-axis", JSONOutput::toValue(*smia));
    }
    if (ratio->isAvailable()) {
      props.add("axis-ratio", JSONOutput::toValue(*ratio));
    }
    props.add("axis-angle", JSONOutput::toValue(*angle));
  } else {
    if (radius->isConstant()) {
      double v = radius->getConstantValue();
      if (v < 0.0 || v > maxRadius) {
        throw IPAC_STK_EXCEPT(except::InvalidParameter, except::message(
          "The specified search radius %g arcsec is negative "
          "or greater than the allowed maximum of %g arcsec",
          v * ARCSEC_PER_RAD, maxRadius * ARCSEC_PER_RAD));
      }
    }
    props.add("radius", JSONOutput::toValue(*radius));
  }
  // Parse output column specs
  tableColumns = parseColumnSpec(reader.getMetadata(),
                                 vm["columns"].as<string>(),
                                 prefix, rowid);
  noMatchColumns = parseColumnSpec(reader.getMetadata(),
                                   vm["no-match-columns"].as<string>(),
                                   "", rowid);
  indexColumns = parseColumnSpec(matcher.getChunkIndex(),
                                 vm["index-columns"].as<string>(),
                                 vm["index-prefix"].as<string>());
  if (matcher.getChunkIndex().isBinary()) {
    massageBinaryIndexColumns(indexColumns);
  }
  // open output files
  if (vm.count("bad-rows") != 0) {
    badRows = shared_ptr<vector<Record> >(new vector<Record>());
    badRowTable = openFileForWriting(vm["bad-rows"].as<string>());
  }
  if (maxMatches != 0) {
    matchTable = openFileForWriting(vm["match-table"].as<string>());
  }
  if (vm.count("no-match-table") != 0) {
    noMatchTable = openFileForWriting(vm["no-match-table"].as<string>());
  }
}

Variables::~Variables() { }

template <typename OutColumn>
void writeNames(vector<OutColumn> const & columns, ofstream & out) {
  typedef typename vector<OutColumn>::const_iterator Iter;
  for (Iter i(columns.begin()), end(columns.end()); i != end; ++i) {
    out << '|' << setw(i->getWidth()) << i->getName();
  }
}

template <typename OutColumn>
void writeTypes(vector<OutColumn> const & columns, ofstream & out) {
  typedef typename vector<OutColumn>::const_iterator Iter;
  for (Iter i(columns.begin()), end(columns.end()); i != end; ++i) {
    out << '|' << setw(i->getWidth()) << i->getType();
  }
}

template <typename OutColumn>
void writeUnits(vector<OutColumn> const & columns, ofstream & out) {
  typedef typename vector<OutColumn>::const_iterator Iter;
  for (Iter i(columns.begin()), end(columns.end()); i != end; ++i) {
    out << '|' << setw(i->getWidth()) << i->getUnits();
  }
}

template <typename OutColumn>
void writeNulls(vector<OutColumn> const & columns, ofstream & out) {
  typedef typename vector<OutColumn>::const_iterator Iter;
  for (Iter i(columns.begin()), end(columns.end()); i != end; ++i) {
    out << '|' << setw(i->getWidth()) << i->getNull();
  }
}

void outputTableColumns(Record const & record,
                        vector<IPACOutColumn> const & columns,
                        string const & rowid,
                        ostream & out)
{
  typedef vector<IPACOutColumn>::const_iterator Iter;
  for (Iter c(columns.begin()), e(columns.end()); c != e; ++c) {
    out.put(' ');
    if (c->getColumn().getName() == rowid) {
      out << setw(c->getWidth()) << record.rowid + 1;
    } else {
      streamsize sz = static_cast<streamsize>(c->getColumn().getWidth());
      char const * const beg = record.begin + c->getColumn().begin();
      if (beg + sz > record.end) {
        sz = record.end - beg;
      }
      out.write(beg, sz);
      streamsize nspace = c->getWidth() - sz;
      for (; nspace > 0; --nspace) {
        out.put(' ');
      }
    }
  }
}

int32_t maxIndex(vector<CIOutColumn> const & columns) {
  typedef vector<CIOutColumn>::const_iterator Iter;
  int32_t m = 0;
  for (Iter i(columns.begin()), e(columns.end()); i != e; ++i) {
    m = max(m, i->getColumn().getIndex());
  }
  return m;
}

void tokenize(std::string const & data,
              size_t maxTokens,
              vector<pair<int, int> > & tokens)
{
  tokens.clear();
  if (maxTokens > 0) {
    char const * const str = data.c_str();
    char const * s = str;
    char const * next = str;
    while (true) {
      while (*next != '\0') {
        ++next;
      }
      tokens.push_back(make_pair(static_cast<int>(s - str),
                                 static_cast<int>(next - s)));
      if (tokens.size() == maxTokens) {
        break;
      }
      ++next;
      s = next;
    }
  }
}

void outputBinaryIndexColumns(ChunkFile::Entry const & entry,
                              vector<CIOutColumn> const & columns,
                              ostream & out)
{
  typedef vector<CIOutColumn>::const_iterator Iter;
  char buf[32];
  for (Iter c(columns.begin()), e(columns.end()); c != e; ++c) {
    int i = c->getColumn().getIndex();
    int nspace = c->getWidth() + 1;
    int n = 0;
    switch (i) {
      case 0: // uik
        n = snprintf(buf, sizeof(buf), "%lld",
                     static_cast<long long>(entry.uik));
        break;
      case 1: // theta
        n = snprintf(buf, sizeof(buf), "%24.17g", entry.theta);
        break;
      case 2: // phi
        n = snprintf(buf, sizeof(buf), "%24.17g", entry.phi);
        break;
      default:
        ; // should never happen
    }
    nspace -= n;
    for (; nspace > 0; --nspace) {
      out.put(' ');
    }
    out.write(buf, n);
  }
}

void outputTextIndexColumns(ChunkFile::Entry const & entry,
                            vector<CIOutColumn> const & columns,
                            vector<pair<int, int> > & extents,
                            int maxIndex,
                            ostream & out)
{
  typedef vector<CIOutColumn>::const_iterator Iter;
  tokenize(entry.data, maxIndex, extents);
  for (Iter c(columns.begin()), e(columns.end()); c != e; ++c) {
    out.put(' ');
    pair<int, int> const & ce = extents[c->getColumn().getIndex()];
    int nspace = c->getWidth();
    if (ce.second == 0) {
      out.write("null", 4);
      nspace -= 4;
    } else {
      out.write(entry.data.c_str() + ce.first,
                static_cast<streamsize>(ce.second));
      nspace -= ce.second;
    }
    for (; nspace > 0; --nspace) {
      out.put(' ');
    }
  }
}

template <typename MatchableType>
void outputMatches(vector<MatchableType> & records,
                   bool writeHeader,
                   Variables & vars)
{
  typedef typename vector<MatchableType>::iterator RecIter;
  typedef vector<Matchable::Match>::const_iterator MatchIter;
  char buf[64];
  ofstream * out = vars.matchTable.get();
  if (writeHeader && out != 0) {
    *out << "\\fixlen = T\n";
    // 1st line: names
    writeNames(vars.tableColumns, *out);
    *out << "|dist         |pang       |nm          ";
    writeNames(vars.indexColumns, *out);
    *out << "|\n";
    // 2nd line: data types
    writeTypes(vars.tableColumns, *out);
    *out << "|double       |double     |int         ";
    writeTypes(vars.indexColumns, *out);
    *out << "|\n";
    // 3d line: units
    writeUnits(vars.tableColumns, *out);
    *out << "|arcsec       |deg        |            ";
    writeUnits(vars.indexColumns, *out);
    *out << "|\n";
    // 4th line: null representation
    writeNulls(vars.tableColumns, *out);
    *out << "|null         |null       |            ";
    writeNulls(vars.indexColumns, *out);
    *out << "|\n";
  }
  vector<pair<int, int> > extents;
  int32_t maxParseColumns = maxIndex(vars.indexColumns) + 1;
  extents.reserve(static_cast<size_t>(maxParseColumns));
  ostringstream oss;
  double minDist = numeric_limits<double>::max();
  double maxDist = numeric_limits<double>::min();
  double sumDist = 0.0;
  double sumSquareDist = 0.0;
  // Loop over input records
  for (RecIter i(records.begin()), ie(records.end()); i != ie; ++i) {
    vars.numMatches += i->getNumMatches();
    if (i->getNumMatches() > 0) {
      ++vars.numMatched;
    } else {
      ++vars.numUnmatched;
    }
    vector<Matchable::Match> & matches = i->getMatches();
    if (matches.size() == 0) {
      continue;
    }
    vars.numRecordedMatches += matches.size();
    string tableCols;
    if (out) {
      ostringstream oss;
      outputTableColumns(i->getData(), vars.tableColumns, vars.rowid, oss);
      tableCols = oss.str();
      if (vars.sortByDist && out != 0) {
        if (vars.maxMatches < 0) {
          sort(matches.begin(), matches.end());
        } else {
          sort_heap(matches.begin(), matches.end());
        }
      }
    }
    // Loop over matches
    for (MatchIter m(matches.begin()), me(matches.end()); m != me; ++m) {
      if (m->distance < minDist) {
        minDist = m->distance;
      }
      if (m->distance > maxDist) {
        maxDist = m->distance;
      }
      sumDist += m->distance;
      sumSquareDist += m->distance * m->distance;
      if (out != 0) {
        *out << tableCols;
        int n = snprintf(buf, sizeof(buf), " %13.6f %11.6f %12llu",
                         m->distance * ARCSEC_PER_RAD,
                         m->positionAngle * DEG_PER_RAD,
                         static_cast<unsigned long long>(i->getNumMatches()));
        out->write(buf, n);
        if (vars.binary) {
          outputBinaryIndexColumns(m->entry, vars.indexColumns, *out);
        } else {
          outputTextIndexColumns(m->entry, vars.indexColumns, extents,
                                 maxParseColumns, *out);
        }
        out->write(" \n", 2);
      }
    }
  }
  vars.minDist = min(vars.minDist, minDist);
  vars.maxDist = max(vars.maxDist, maxDist);
  vars.sumDist += sumDist;
  vars.sumSquareDist += sumSquareDist;
}

template <typename MatchableType>
void outputNoMatches(vector<MatchableType> const & matches,
                     bool writeHeader,
                     Variables & vars)
{
  typedef typename vector<MatchableType>::const_iterator Iter;
  ofstream * out = vars.noMatchTable.get();
  if (!out) {
    return;
  }
  if (writeHeader) {
    *out << "\\fixlen = T\n";
    writeNames(vars.noMatchColumns, *out);
    *out << "|\n";
    writeTypes(vars.noMatchColumns, *out);
    *out << "|\n";
    writeUnits(vars.noMatchColumns, *out);
    *out << "|\n";
    writeNulls(vars.noMatchColumns, *out);
    *out << "|\n";
  }
  for (Iter i(matches.begin()), e(matches.end()); i != e; ++i) {
    if (i->getNumMatches() != 0) {
      continue;
    }
    outputTableColumns(i->getData(), vars.noMatchColumns, vars.rowid, *out);
    out->write(" \n", 2);
  }
}

void outputBadRows(IPACTableReader const & reader,
                   bool writeHeader,
                   Variables & vars)
{
  typedef vector<Record>::const_iterator Iter;
  ofstream * out = vars.badRowTable.get();
  if (!out) {
    return;
  }
  size_t w = vars.rowid.size();
  if (w > 0) {
    w = max(w, static_cast<size_t>(12));
  }
  if (writeHeader) {
    vector<IPACTableReader::Column> cols = reader.getMetadata().getColumns();
    if (w > 0) {
      cols.insert(cols.begin(), IPACTableReader::Column(
                  vars.rowid, "long", "", "", -1, 1, 1 + w));
    }
    writeNames(cols, *out);
    *out << "|\n";
    writeTypes(cols, *out);
    *out << "|\n";
    writeUnits(cols, *out);
    *out << "|\n";
    writeNulls(cols, *out);
    *out << "|\n";
  }
  vector<Record> * bad = vars.badRows.get();
  vars.numBadRows += bad->size();
  for (Iter i(bad->begin()), e(bad->end()); i != e && out != 0; ++i) {
    if (w > 0) {
      *out << ' ' << setw(w) << i->rowid + 1;
    }
    out->write(i->begin, static_cast<streamsize>(i->end - i->begin));
    out->put('\n');
  }
}

} // namespace


int main(int const argc, char ** argv) {
  Value props = Value::map();
  try {
    boost::timer total;
    boost::timer t;

    // Build and parse program options
    po::options_description options;
    buildOptions(options);
    po::variables_map vm;
    po::store(po::parse_command_line(argc, argv, options), vm);
    po::notify(vm);
    jsonOutput = vm["json"].as<bool>();
    verbose = vm["verbose"].as<bool>();
    verifyOptions(vm, options, argv[0]);

    // Initialization
    IPACTableReader reader(fs::path(vm["table"].as<string>()), 16777216,
                           1024, vm["case-insensitive"].as<bool>());
    ChunkIndexMatcher matcher(fs::path(vm["chunk-index"].as<string>()));
    Variables vars(vm, reader, matcher, props);
    bool writeHeader = true;
    double inputTime = 0.0;
    double buildTime = 0.0;
    double matchTime = 0.0;
    double outputTime = 0.0;
    if (verbose) {
      props.add("max-radius", vars.maxRadius);
      props.add("max-matches", vars.maxMatches);
      props.add("init-time", t.elapsed());
    }
    Value warnings;
    if (vars.elliptical) {
      EllipseBuilder builder(vars.theta, vars.phi, vars.angle,
                             vars.smaa, vars.smia, vars.ratio);
      vector<MatchableEllipse> ellipses;
      while(reader.hasNext()) {
        t.restart();
        reader.nextRows(vars.rows);
        inputTime += t.elapsed();
        t.restart();
        builder.build(vars.rows, vars.maxRadius, vars.badRows.get(), ellipses);
        buildTime += t.elapsed();
        t.restart();
        matcher.match(ellipses, vars.maxMatches);
        matchTime += t.elapsed();
        t.restart();
        outputMatches(ellipses, writeHeader, vars);
        outputNoMatches(ellipses, writeHeader, vars);
        outputBadRows(reader, writeHeader, vars);
        outputTime += t.elapsed();
        writeHeader = false;
      }
      warnings = builder.getWarnings();
    } else {
      CircleBuilder builder(vars.theta, vars.phi, vars.radius);
      vector<MatchableCircle> circles;
      while(reader.hasNext()) {
        t.restart();
        reader.nextRows(vars.rows);
        inputTime += t.elapsed();
        t.restart();
        builder.build(vars.rows, vars.maxRadius, vars.badRows.get(), circles);
        buildTime += t.elapsed();
        t.restart();
        matcher.match(circles, vars.maxMatches);
        matchTime += t.elapsed();
        t.restart();
        outputMatches(circles, writeHeader, vars);
        outputNoMatches(circles, writeHeader, vars);
        outputBadRows(reader, writeHeader, vars);
        outputTime += t.elapsed();
        writeHeader = false;
      }
      warnings = builder.getWarnings();
    }
    if (vars.matchTable) {
      vars.matchTable->flush();
    }
    if (vars.noMatchTable) {
      vars.noMatchTable->flush();
    }
    if (vars.badRowTable) {
      vars.badRowTable->flush();
    }
    props.add("num-matched", vars.numMatched);
    props.add("num-unmatched", vars.numUnmatched);
    props.add("num-matches", vars.numMatches);
    props.add("num-recorded-matches", vars.numRecordedMatches);
    props.add("num-bad-rows", vars.numBadRows);
    if (vars.numRecordedMatches > 0) {
      props.add("min-match-dist", vars.minDist * ARCSEC_PER_RAD);
      props.add("max-match-dist", vars.maxDist * ARCSEC_PER_RAD);
      props.add("avg-match-dist", ARCSEC_PER_RAD *
                vars.sumDist / vars.numRecordedMatches);
      props.add("rms-match-dist", ARCSEC_PER_RAD * 
                sqrt(vars.sumSquareDist / vars.numRecordedMatches));
    }
    if (verbose) {
      props.add("input-time", inputTime);
      props.add("build-time", buildTime);
      props.add("match-time", matchTime);
      props.add("output-time", outputTime);
      props.add("total-time", total.elapsed());
    }

    JSONOutput out(cout, getFormattingOptions());
    out.object();
    out.pair("stat", "OK");
    out.pair("msg", "Success");
    out.pair("props", props);
    out.pair("warnings", warnings);
    out.close();
    cout << endl;
    return EXIT_SUCCESS;
  } catch (except::Exception const & e) {
    printError(props, e);
  } catch (std::exception const & e) {
    printError("Caught " + string(typeid(e).name()) + " : " + string(e.what()));
  }
  return EXIT_FAILURE;
}
