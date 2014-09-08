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
    \brief  Class for top-level chunk index details.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_CHUNKINDEX_H_
#define IPAC_STK_ASSOC_CHUNKINDEX_H_

#include "ipac/stk/config.h"

#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif

#include <cassert>
#include <map>
#include <string>
#include <vector>
#include <utility>

#include "boost/filesystem.hpp"
#include "boost/regex.hpp"

#include "ipac/stk/util/macros.h"


namespace ipac { namespace stk { namespace assoc {

class Matchable;

/** A chunk index is a top-level index file that records information
    about a set of chunk files that tile the sky. These files can be
    binary, storing only a unique integer key, longitude angle, and
    latitude angle for each record, or textual, in which case an arbitrary
    set of fields can be stored. Textual chunk files store field data as
    a UTF-8 string consisting of null delimited field value UTF-8 sub-strings.
    The first three fields in a record are always the unique integer key,
    longitude angle, and latitude angle; their values are never null/empty.
    The longitude and latitude angles must be in decimal degrees.

    \p
    During chunk index creation, input points are mapped to location
    integers. A location integer consists of stripe number, chunk number,
    sub-stripe number and a lane number. For each point an overlap bit is
    also stored; it indicates whether or not the location is an overlap
    location. The stripe number identifies a latitude angle range and the
    chunk number a longitude range within the stripe; together, they identify
    a particular chunk (i.e. a longitude/latitude angle box). Input positions
    are bucket sorted into chunks and each chunk is stored in a single file.

    \p
    Each chunk is further broken up into sub-stripes (smaller latitude
    angle ranges) and lanes within each sub-stripe (smaller longitude
    angle ranges). Points within a chunk are bucket-sorted by sub-stripe
    and lane; within a lane, points are sorted by their scaled latitude
    angles and finally by unique integer key.

    \p
    Chunks and sub-stripes also contain all positions within the angular
    overlap distance of their boundaries; these are marked as overlap
    locations. They allow spatial searches to be performed without accessing
    data from neighboring chunks or sub-stripes so long as the match extents
    are sufficiently small. A direct consequence is that chunk files can be
    distributed across multiple nodes and spatial searches can be
    performed in parallel with no inter-node communication.
  */
class ChunkIndex {
public:
  static int64_t const MAGIC;
  static int32_t const VERSION = 1;

  /** Metadata for columns in the index.
    */
  class Column {
  public:
    static boost::regex const NAME_REGEX;
    static boost::regex const TYPE_REGEX;
    static boost::regex const UNITS_REGEX;

    Column();
    Column(std::string const & name,
           std::string const & type,
           std::string const & units,
           int32_t width,
           int32_t index);
    ~Column();

    std::string const & getName() const {
      return _name;
    }
    std::string const & getType() const {
      return _type;
    }
    std::string const & getUnits() const {
      return _units;
    }
    int32_t getWidth() const {
      return _width;
    }
    int32_t getIndex() const {
      return _index;
    }

    static bool isValidName(std::string const & name);
    static bool isValidType(std::string const & type);
    static bool isValidUnits(std::string const & units);

  private:
    std::string _name;
    std::string _type;
    std::string _units;
    int32_t _width;
    int32_t _index;

    friend inline bool operator<(Column const & c1, Column const & c2) {
      return c1._index < c2._index;
    }
  };

  /** A range of lanes in a sub-stripe of a chunk.
    */
  class LaneRange {
  public:
    static int32_t const CHUNK_ID_MASK = 0x1ffff;
    static int32_t const CHUNK_MASK = 0x1ff;
    static int32_t const CHUNK_SHIFT = 41;
    static int32_t const STRIPE_MASK = 0xff;
    static int32_t const STRIPE_SHIFT = 50;
    static int32_t const SUB_STRIPE_MASK = 0x1ff;
    static int32_t const SUB_STRIPE_SHIFT = 32;
    static int32_t const LANE_MASK = 0xffff;
    static int32_t const MIN_LANE_SHIFT = 16;

    LaneRange() : _range(0) { }

    LaneRange(int32_t stripe,
              int32_t chunk,
              int32_t subStripe,
              int32_t minLane,
              int32_t maxLane)
    {
      set(stripe, chunk, subStripe, minLane, maxLane);
    }

    int32_t getChunkId() const {
      return (_range >> CHUNK_SHIFT) & CHUNK_ID_MASK;
    }
    int32_t getStripe() const {
      return (_range >> STRIPE_SHIFT) & STRIPE_MASK;
    }
    int32_t getChunk() const {
      return (_range >> CHUNK_SHIFT) & CHUNK_MASK;
    }
    int32_t getSubStripe() const {
      return (_range >> SUB_STRIPE_SHIFT) & SUB_STRIPE_MASK;
    }
    int32_t getMinLane() const {
      return (_range >> MIN_LANE_SHIFT) & LANE_MASK;
    }
    int32_t getMaxLane() const {
      return _range & LANE_MASK;
    }

    void set(int32_t stripe,
             int32_t chunk,
             int32_t subStripe,
             int32_t minLane,
             int32_t maxLane)
    {
      assert(stripe >= 0 && stripe <= STRIPE_MASK &&
             "stripe number out of bounds");
      assert(chunk >= 0 && chunk <= CHUNK_MASK &&
             "chunk number out of bounds");
      assert(subStripe >= 0 && subStripe <= SUB_STRIPE_MASK &&
             "sub-stripe number out of bounds");
      assert(minLane >= 0 && minLane <= LANE_MASK &&
             "lane number out of bounds");
      assert(maxLane >= 0 && maxLane <= LANE_MASK &&
             "lane number out of bounds");
      _range = (static_cast<int64_t>(stripe) << STRIPE_SHIFT) |
               (static_cast<int64_t>(chunk) << CHUNK_SHIFT) |
               (static_cast<int64_t>(subStripe) << SUB_STRIPE_SHIFT) |
               (minLane << MIN_LANE_SHIFT) |
               maxLane;
    }

  private:
    int64_t _range;

    friend inline bool operator<(LaneRange const & r1, LaneRange const & r2) {
      return r1._range < r2._range;
    }
    friend inline bool operator==(LaneRange const & r1, LaneRange const & r2) {
      return r1._range == r2._range;
    }
    friend inline bool operator!=(LaneRange const & r1, LaneRange const & r2) {
      return r1._range != r2._range;
    }
  };

  /** A Matchable that has been located by a ChunkIndex.
    */
  typedef std::pair<LaneRange, Matchable *> LocatedMatchable;

  ChunkIndex(boost::filesystem::path const & path);
  ~ChunkIndex();

  boost::filesystem::path const getPath() const {
    return _path;
  }

  bool isBinary() const {
    return _binary;
  }

  bool isZipped() const {
    return _zipped;
  }

  double getOverlapDeg() const {
    return _overlapDeg;
  }

  bool isEmpty(int32_t stripe, int32_t chunk) const;

  /// \name Accessing column metadata
  //@{
  bool hasColumn(std::string const & name) const;
  Column const & getColumn(std::string const & name) const;
  std::vector<Column> const getColumns(
    std::vector<std::string> const & names) const;
  std::vector<Column> const getColumns() const;
  //@}

  void locate(std::vector<Matchable *>::const_iterator begin,
              std::vector<Matchable *>::const_iterator end,
              std::vector<LocatedMatchable> & located) const;

  /// \name Obtaining chunk file names
  //@{
  static boost::filesystem::path const chunkPath(
    int32_t stripe, int32_t chunk);
  boost::filesystem::path const getChunkPath(
    int32_t stripe, int32_t chunk) const;
  boost::filesystem::path const getChunkPath(LaneRange const & range) const {
    return getChunkPath(range.getStripe(), range.getChunk());
  }
  //@}

private:
  static int32_t const MAX_STRIPES = 180;
  static int32_t const MAX_SS_PER_STRIPE = 512;
  static int32_t const MAX_CHUNKS_PER_STRIPE = 360;
  static int32_t const MAX_LANES_PER_CSS = 65536;
  static int32_t const MIN_COLUMNS = 3;
  static int32_t const MAX_COLUMNS = 8192;

  /** \internal
      Sub-stripe metadata.
    */
  struct SubStripe {
    double _invWidth;   ///< Inverse of the lane width (degrees).
    int32_t _lanes;     ///< Lanes per sub-stripe, not including overlap lanes.
    int32_t _lanesPerC; ///< Lanes per chunk sub-stripe, not including overlap.
    int32_t _overlap;   ///< Half the overlap lane count for a chunk sub-stripe.

    SubStripe(double invWidth,
              int32_t lanes,
              int32_t lanesPerC,
              int32_t overlap);
    ~SubStripe();
  };

  int32_t _numS;        ///< Number of stripes.
  int32_t _numSSPerS;   ///< Number of sub-stripes per stripe.
  int32_t _overlapSS;   ///< Number of overlap sub-stripes on one side of a stripe.
  double _ssHeight;     ///< Sub-stripe height, not including overlap (deg).
  double _invSSHeight;  ///< The inverse of \c ssHeight.
  double _laneWidthDeg; ///< Desired lane width.
  double _overlapDeg;   ///< Angular chunk (and sub-stripe) overlap radius.
  bool _zipped;         ///< \c true iff lanes are zlib-compressed.
  bool _binary;         ///< \c true iff lane entries are binary.

  boost::filesystem::path _path;
  std::map<std::string, Column> _columns;
  std::vector<std::vector<int64_t> > _population;
  std::vector<std::vector<int64_t> > _overlapPopulation;
  std::vector<SubStripe> _subStripes;

  IPAC_STK_NONCOPYABLE(ChunkIndex);
};

inline bool operator<(ChunkIndex::LocatedMatchable const & m1,
                      ChunkIndex::LocatedMatchable const & m2)
{
  return m1.first < m2.first;
}

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_CHUNKINDEX_H_
