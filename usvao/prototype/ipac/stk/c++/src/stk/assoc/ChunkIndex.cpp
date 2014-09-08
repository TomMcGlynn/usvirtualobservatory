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
    \brief  ChunkIndex class implementation.
    \author Serge Monkewitz
  */
#include "ipac/stk/assoc/ChunkIndex.h"
#include "ipac/stk/config.h"

#include <stdio.h>

#include <algorithm>
#include <cmath>
#include <ios>
#include <fstream>

#include "Eigen/Core"

#include "ipac/stk/assoc/Matchable.h"
#include "ipac/stk/except.h"
#include "ipac/stk/util/hadoop/io.h"


namespace ipac { namespace stk { namespace assoc {

using std::floor;
using std::ifstream;
using std::ios;
using std::make_pair;
using std::map;
using std::max;
using std::min;
using std::pair;
using std::sort;
using std::string;
using std::vector;

using Eigen::Vector2d;

namespace except = ipac::stk::except;
namespace fs = boost::filesystem;
namespace hadoop = ipac::stk::util::hadoop;

// -- ChunkIndex::Column --------

boost::regex const ChunkIndex::Column::NAME_REGEX(
  "[a-zA-Z_]+[a-zA-Z_0-9]*");
boost::regex const ChunkIndex::Column::TYPE_REGEX(
  "c(?:har)?|date|d(?:ouble)?|i(?:nt(?:eger)?)?|l(?:ong)?|r(?:eal)?");
boost::regex const ChunkIndex::Column::UNITS_REGEX(
  "[a-zA-Z0-9+-.*^()/\\[\\] \t]*");

ChunkIndex::Column::Column() :
  _name(), _type(), _units(), _width(0), _index(-1)
{ }

/** Creates a new column with the given name, type, units, index and
    maximum width.
  */
ChunkIndex::Column::Column(std::string const & name,
                           std::string const & type,
                           std::string const & units,
                           int32_t width,
                           int32_t index) :
  _name(name),
  _type(type),
  _units(units),
  _width(width),
  _index(index)
{
  if (!isValidName(name)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Invalid column name: " + name);
  }
  if (!isValidType(type)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Invalid column type: " + type);
  }
  if (!isValidUnits(units)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Invalid column unit string: " + units);
  }
  if (index < 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Negative column index");
  }
  if (width < 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Negative column width");
  }
}

ChunkIndex::Column::~Column() { }

/** Returns \c true if \c name is a valid column name.
  */
bool ChunkIndex::Column::isValidName(std::string const & name) {
  return boost::regex_match(name.begin(), name.end(), NAME_REGEX);
}

/** Returns \c true if \c type is a valid column data-type.
  */
bool ChunkIndex::Column::isValidType(std::string const & type) {
  if (type.size() == 0) {
    return true;
  }
  return boost::regex_match(type.begin(), type.end(), TYPE_REGEX);
}

/** Returns \c true if \c units is a valid column unit specification.
  */
bool ChunkIndex::Column::isValidUnits(std::string const & units) {
  if (units.size() == 0) {
    return true;
  }
  return boost::regex_match(units.begin(), units.end(), UNITS_REGEX);
}

// -- ChunkIndex::SubStripe --------

ChunkIndex::SubStripe::SubStripe(double invWidth,
                                 int32_t lanes,
                                 int32_t lanesPerC,
                                 int32_t overlap) :
  _invWidth(invWidth),
  _lanes(lanes),
  _lanesPerC(lanesPerC),
  _overlap(overlap)
{ }

ChunkIndex::SubStripe::~SubStripe() { }


// -- ChunkIndex --------

int64_t const ChunkIndex::MAGIC = 0x0000c0ffeec0ffeeLL;

ChunkIndex::ChunkIndex(fs::path const & path) :
  _path(path),
  _columns(),
  _population(),
  _overlapPopulation(),
  _subStripes()
{
  if (!fs::exists(path) || !fs::is_regular_file(path)) {
    throw IPAC_STK_EXCEPT(except::IOError, 0, path.file_string() +
                          " does not exist or is not a regular file");
  }
  ifstream in(path.file_string().c_str(), ios::in | ios::binary);
  if (in.fail()) {
    throw IPAC_STK_EXCEPT(except::IOError, 0, "Failed to open file " +
                          path.file_string() + " for reading");
  }
  in.exceptions(ifstream::eofbit | ifstream::failbit | ifstream::badbit);
  if (hadoop::readInt64(in) != MAGIC) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read chunk index file " +
                          path.file_string() + " : missing magic bytes");
  }
  if (hadoop::readInt32(in) != VERSION) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read chunk index file " +
                          path.file_string() + " : file format version "
                          "mismatch");
  }

  _numS = hadoop::readVInt32(in);
  if (_numS < 1 || _numS > MAX_STRIPES) {
    throw IPAC_STK_EXCEPT(except::Format, except::message("chunk index file "
                          "%s contains an invalid number of stripes; the "
                          "number must be between 1 and %d.",
                          path.file_string().c_str(),
                          static_cast<int>(MAX_STRIPES)));
  }
  _numSSPerS = hadoop::readVInt32(in);
  if (_numSSPerS < 1) {
    throw IPAC_STK_EXCEPT(except::Format, except::message("chunk index file "
                          "%s stripes contain no sub-stripes",
                          path.file_string().c_str()));
  }
  _overlapSS = hadoop::readVInt32(in);
  if (_overlapSS < 0 || _numSSPerS + 2 * _overlapSS > MAX_SS_PER_STRIPE) {
    throw IPAC_STK_EXCEPT(except::Format, except::message("chunk index file "
                          "%s contains an invalid number of sub-stripes per "
                          "stripe; the number must be between 1 and %d.",
                          path.file_string().c_str(),
                          static_cast<int>(MAX_SS_PER_STRIPE)));
  }
  _ssHeight = hadoop::readDouble(in);
  _invSSHeight = hadoop::readDouble(in);
  _laneWidthDeg = hadoop::readDouble(in);
  _overlapDeg = hadoop::readDouble(in);
  _zipped = hadoop::readBool(in);
  _binary = hadoop::readBool(in);
  // read in columns
  int32_t numColumns = hadoop::readVInt32(in);
  for (int32_t i = 0; i < numColumns; ++i) {
    string name = hadoop::readString(in);
    string type = hadoop::readString(in);
    string units = hadoop::readString(in);
    int32_t width = hadoop::readVInt32(in);
    int32_t index = hadoop::readVInt32(in);
    pair<map<string, Column>::iterator, bool> ins = _columns.insert(
      make_pair(name, Column(name, type, units, width, index)));
    if (!ins.second) {
      throw IPAC_STK_EXCEPT(except::Format, "chunk index file " +
                            path.file_string() + " contains more than one "
                            "column named " + name);
    }
  }
  if (_columns.size() < static_cast<size_t>(MIN_COLUMNS) ||
      _columns.size() > static_cast<size_t>(MAX_COLUMNS)) {
      throw IPAC_STK_EXCEPT(except::Format, except::message("chunk index "
                            "file %s has an invalid number of columns: the "
                            "number must be between %d and %d.",
                            path.file_string().c_str(),
                            static_cast<int>(MIN_COLUMNS),
                            static_cast<int>(MAX_COLUMNS)));
  }
  // read in chunk populations
  _population.reserve(_numS);
  _overlapPopulation.reserve(_numS);
  for (int32_t i = 0; i < _numS; ++i) {
    int32_t numChunks = hadoop::readVInt32(in);
    if (numChunks < 1 || numChunks > MAX_CHUNKS_PER_STRIPE) {
      throw IPAC_STK_EXCEPT(except::Format, "chunk index file " +
                            path.file_string() + " contains an invalid "
                           "number of chunks per stripe");
    }
    _population.push_back(vector<int64_t>());
    _overlapPopulation.push_back(vector<int64_t>());
    _population.back().reserve(numChunks);
    _overlapPopulation.back().reserve(numChunks);
    for (int32_t j = 0; j < numChunks; ++j) {
      int64_t p = hadoop::readVInt64(in);
      int64_t op = hadoop::readVInt64(in);
      if (p < 0 || op < 0) {
        throw IPAC_STK_EXCEPT(except::Format, "chunk index file " +
                              path.file_string() + " contains a negative "
                             "chunk population count");
      }
      _population.back().push_back(p);
      _overlapPopulation.back().push_back(op);
    }
  }
  // read in sub-stripe details
  _subStripes.reserve(_numS * _numSSPerS);
  for (int32_t i = 0; i < _numS; ++i) {
    int32_t const numChunks = _population[i].size();
    for (int32_t j = 0; j < _numSSPerS; ++j) {
      int32_t lanesPerC = hadoop::readVInt32(in);
      int32_t overlap = hadoop::readVInt32(in);
      int32_t lanes = lanesPerC * numChunks;
      double  invWidth = hadoop::readDouble(in);
      if (lanesPerC <= 0 || overlap < 0) {
        throw IPAC_STK_EXCEPT(except::Format, "chunk index file " +
                              path.file_string() + " contains a negative "
                              " or zero lane count");
      }
      if (lanesPerC + 2 * overlap > MAX_LANES_PER_CSS) {
        throw IPAC_STK_EXCEPT(except::Format, "chunk index file " +
                              path.file_string() + " contains too many "
                              "lanes per sub-stripe per chunk");
      }
      _subStripes.push_back(SubStripe(invWidth, lanes, lanesPerC, overlap));
    }
  }
}

ChunkIndex::~ChunkIndex() { }

/** Returns \c true if the specified chunk is empty.

    \throw ipac::stk::except::OutOfBounds
      If \c stripe or \c chunk consitute invalid chunk coordinates for
      this chunk index.
  */
bool ChunkIndex::isEmpty(int32_t stripe, int32_t chunk) const {
  if (stripe < 0 || stripe >= _numS) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds,
                          "Stripe number is out of range");
  }
  int numC = _population[stripe].size();
  if (chunk < 0 || chunk >= numC) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds,
                          "Chunk number is out of range");
  }
  int64_t n = _population[stripe][chunk] + _overlapPopulation[stripe][chunk];
  return n == 0;
}

/** Returns \c true if this chunk index has a column with the given name.
  */
bool ChunkIndex::hasColumn(std::string const & name) const {
  return _columns.find(name) != _columns.end();
}

/** Returns the column with the given name or throws an exception.

    \throw ipac::stk::except::NotFound
      If \c name does not correspond to a column in the chunk index.
  */
ChunkIndex::Column const & ChunkIndex::getColumn(
  std::string const & name) const
{
  map<string, Column>::const_iterator i = _columns.find(name);
  if (i == _columns.end()) {
    throw IPAC_STK_EXCEPT(except::NotFound, "Chunk index " +
                          _path.file_string() + " does not have a column "
                          "named " + name);
  }
  return i->second;
}

/** Returns the columns with the given names or throws an exception.

    \throw ipac::stk::except::NotFound
      If one of the names does not correspond to a column in the chunk index.
  */
std::vector<ChunkIndex::Column> const ChunkIndex::getColumns(
  std::vector<std::string> const & names) const
{
  vector<Column> columns;
  for(vector<string>::const_iterator i(names.begin()), e(names.end());
      i != e; ++i) {
    columns.push_back(getColumn(*i));
  }
  return columns;
}

/** Returns a list of all columns in this chunk index. Columns are returned
    according to their storage ordering in chunk index records.
  */
std::vector<ChunkIndex::Column> const ChunkIndex::getColumns() const {
  vector<Column> columns;
  for(map<string, Column>::const_iterator i(_columns.begin()),
      e(_columns.end()); i != e; ++i) {
    columns.push_back(i->second);
  }
  // return columns in order
  sort(columns.begin(), columns.end());
  return columns;
}

/** Computes the lane ranges occupied by each Matchable in the given range,
    then performs an indirect sort of the Matchable objects.
  */
void ChunkIndex::locate(vector<Matchable *>::const_iterator const begin,
                        vector<Matchable *>::const_iterator const end,
                        vector<LocatedMatchable> & located) const
{
  for (vector<Matchable *>::const_iterator m(begin); m != end;) {
    Matchable * mp = *m;
    ++m;
#if HAVE_BUILTIN_PREFETCH
    // prefetch memory for next matchable
    if (m != end) {
      __builtin_prefetch(static_cast<void *>(*m), 0, 1);
    }
#endif
    // center of bounding box yields chunk for matchable
    SphericalBox const & box = mp->getEnvelope();
    Vector2d center(box.getCenter());
    int32_t const numSS = static_cast<int32_t>(_subStripes.size());
    int32_t subStripe = floor((center.y() + 90.0) * _invSSHeight);
    int32_t maxSS = floor((box.getMaxPhi() + 90.0) * _invSSHeight);
    int32_t minSS = floor((box.getMinPhi() + 90.0) * _invSSHeight);
    subStripe = min(subStripe, numSS - 1);
    maxSS = min(maxSS, numSS - 1);
    minSS = min(minSS, numSS - 1);
    minSS -= subStripe;
    maxSS -= subStripe;
    int32_t const stripe = subStripe / _numSSPerS;
    int32_t const sscCen = subStripe % _numSSPerS;
    int32_t const sscOff = stripe > 0 ? _overlapSS : 0;
    assert(sscCen + minSS + sscOff >= 0 &&
           "sub-stripe overflowed stripe overlap");
    assert(sscCen + maxSS < numSS + (stripe < _numS - 1 ? _overlapSS : 0) &&
           "sub-stripe overflowed stripe overlap");
    bool lanesWrap = _population[stripe].size() == 1;
    int32_t chunk = -1;
    for (int32_t i = minSS; i <= maxSS; ++i) {
      // loop over sub-stripes covered by bounding box,
      // computing a lane range for each sub-stripe.
      int32_t ssc = sscCen + i;
      SubStripe const & ss = _subStripes[
        min(max(ssc, 0), _numSSPerS - 1) + stripe * _numSSPerS];
      ssc += sscOff;
      if (i == minSS) {
        // compute chunk
        int32_t centerLane = floor(center.x() * ss._invWidth);
        centerLane = min(centerLane, ss._lanes - 1);
        chunk = centerLane / ss._lanesPerC;
        assert(chunk < static_cast<int32_t>(_population[stripe].size()) &&
               "chunk number out of bounds for stripe");
      }
      int32_t const maxlc = ss._lanesPerC + 2 * ss._overlap;
      int32_t minLane = floor(box.getBegTheta() * ss._invWidth);
      int32_t maxLane = floor(box.getEndTheta() * ss._invWidth);
      minLane = min(minLane, ss._lanes - 1) - chunk * ss._lanesPerC + ss._overlap;
      maxLane = min(maxLane, ss._lanes - 1) - chunk * ss._lanesPerC + ss._overlap;
      if (box.wraps()) {
        // minLane >= maxLane
        if (lanesWrap) {
          // only one chunk in stripe
          if (minLane == maxLane) {
            located.push_back(std::pair<LaneRange, Matchable *>(
              LaneRange(stripe, chunk, ssc, 0, maxlc - 1), mp));
          } else {
            located.push_back(std::pair<LaneRange, Matchable *>(
              LaneRange(stripe, chunk, ssc, 0, maxLane), mp));
            located.push_back(std::pair<LaneRange, Matchable *>(
              LaneRange(stripe, chunk, ssc, minLane, maxlc - 1), mp));
          }
          continue;
        } else if (chunk == 0) {
          // first chunk in stripe
          minLane -= ss._lanes;
        } else {
          // last chunk in stripe
          maxLane += ss._lanes;
        }
      }
      assert(minLane >= 0 && "Lane-range overflows overlap lanes");
      assert(maxLane < maxlc && "Lane-range overflows overlap lanes");
      located.push_back(std::pair<LaneRange, Matchable *>(
        LaneRange(stripe, chunk, ssc, minLane, maxLane), mp));
    }
  }
}

/** Returns the relative path of the file for the chunk with the given
    coordinates. The path is relative to the chunk index directory.
  */
fs::path const ChunkIndex::chunkPath(int32_t stripe, int32_t chunk) {
  char p[32];
  ::snprintf(p, sizeof(p), "stripe_%03d/chunk_%03d_%03d.cf",
             static_cast<int>(stripe), static_cast<int>(stripe),
             static_cast<int>(chunk));
  return fs::path(p);
}

/** Returns the path of the file for the chunk with the given coordinates.

    \throw ipac::stk::except::OutOfBounds
      If \c stripe or \c chunk consitute invalid chunk coordinates for
      this chunk index.
  */
fs::path const ChunkIndex::getChunkPath(int32_t stripe, int32_t chunk) const {
  if (stripe < 0 || stripe >= _numS) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds,
                          "Stripe number is out of range");
  }
  int numC = _population[stripe].size();
  if (chunk < 0 || chunk >= numC) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds,
                          "Chunk number is out of range");
  }
  return _path.parent_path() / chunkPath(stripe, chunk);
}

}}} // namespace ipac::stk::assoc
