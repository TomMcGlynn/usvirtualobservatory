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
    \brief  Class for individual chunk files.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_CHUNKFILE_H_
#define IPAC_STK_ASSOC_CHUNKFILE_H_

#include "ipac/stk/config.h"

#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif

#include <vector>
#include <utility>

#include "boost/filesystem.hpp"
#include "boost/scoped_ptr.hpp"
#include "boost/tuple/tuple.hpp"

#include "Eigen/Core"

#include "ipac/stk/util/FileDescriptor.h"
#include "ipac/stk/util/macros.h"
#include "ipac/stk/util/MappedMemory.h"
#include "ChunkIndex.h"


namespace ipac { namespace stk { namespace assoc {

class Matchable;

/** Class representing chunk files consisting of spatially binned tuples.
    Each tuple corresponds to a UTF-8 string with individual fields separated
    by null ('\0') characters or to binary (unique integer key, longitude,
    latitude) tuples. Individual bins are optionally compressed using
    zlib (http://www.zlib.net).
  */
class ChunkFile {
public:
  /** Magic bytes identifying a file as a chunk file.
    */
  static int64_t const MAGIC = 0xc0ffeefeedLL;
  /** Chunk file format version number.
    */
  static int32_t const VERSION = 1;

  typedef boost::tuple<int32_t, int32_t, int32_t> LaneRange;

  /** A single entry in a chunk file.
    */
  struct Entry {
    Eigen::Vector4d v;
    int64_t uik;
    double theta;
    double phi;
    std::string data;
  };

  /** Functor for comparing an Entry object and a latitude angle.
    */
  struct EntryPhiComparator {
    bool operator()(Entry const & e, double phi) const {
      return e.phi < phi;
    }
    bool operator()(double phi, Entry const & e) const {
      return phi < e.phi;
    }
  };

  typedef std::pair<std::vector<Entry>::const_iterator,
                    std::vector<Entry>::const_iterator> EntryBounds;

  /** A chunk-file lane. Data is stored in one of two formats:
      null delimited UTF-8 fields, or binary form. In the UTF-8
      format, the first field is a unique integer key and the second
      and third are longitude and latitude angles in decimal degrees.
      An arbitrary number of additional fields may follow.

      The binary form stores only a unique integer key and position.
      The key is a zero-encoded 64 bit integer; both latitude
      and longitude are converted to fixed point 64 bit integers,
      delta-coded, and finally stored as zero-encoded 64 bit integers.

      In both cases, lane data may optionally have been compressed with
      \link http://www.zlib.net zlib \endlink; the benefits of this
      are greatest for the UTF-8 format.
    */
  struct Lane {
    Lane() :
      laneOffset(0),
      laneLength(0),
      population(0)
    { }

    Lane(int64_t laneOff, int32_t laneLen, int32_t pop) :
      laneOffset(laneOff),
      laneLength(laneLen),
      population(pop)
    { }

    ~Lane() { }

    int64_t laneOffset;         ///< Offset of lane in chunk file.
    int32_t laneLength;         ///< Size of lane in bytes, uncompressed.
    int32_t population;         ///< Number of entries in lane.
  };

  ChunkFile(boost::filesystem::path const & path);
  ~ChunkFile();

  void match(std::vector<ChunkIndex::LocatedMatchable>::iterator begin,
             std::vector<ChunkIndex::LocatedMatchable>::iterator end,
             int maxMatches);
  void release();

  /** Returns the size in bytes of the underlying chunk file.
    */
  size_t getFileSize() const {
    return _fileSize;
  }

  int32_t getStripe() const {
    return _stripe;
  }

  int32_t getChunk() const {
    return _stripe;
  }

private:
  /** Offset of trailer offset relative to the end of the chunk file.
    */
  static off_t const TRAILER_OFF_OFF = 20;
  /** Minimum number of bytes in trailer.
    */
  static size_t const MIN_TRAILER_SIZE = 16;
  /** Minimum trailer offset relative to the end of the chunk file.
    */
  static off_t const MIN_TRAILER_OFF = MIN_TRAILER_SIZE + TRAILER_OFF_OFF;

  boost::filesystem::path _path;
  size_t _fileSize;
  ipac::stk::util::FileDescriptor _fd;
  boost::scoped_ptr<ipac::stk::util::MappedMemory> _data;
  int32_t _numS;
  int32_t _stripe;
  int32_t _chunk;
  int32_t _overlapSS;
  int64_t _numEntries;
  int64_t _numOverlapEntries;
  bool _binary;
  bool _zipped;
  std::vector<int32_t> _ssLanes;
  std::vector<int32_t> _overlapLanes;
  std::vector<std::vector<Lane> > _lanes;
  std::vector<Entry> _entries;

  size_t acquire();
  void find(double minPhi, double maxPhi, EntryBounds & bounds) const;
  void readLane(Lane const & lane,
                unsigned char const * buf,
                unsigned char const * end);
  void requestResidency(std::vector<LaneRange> const & laneRanges);
  void matchLane(std::vector<ChunkIndex::LocatedMatchable> & v,
                 int32_t subStripe,
                 int32_t lane,
                 int maxMatches);

  IPAC_STK_NONCOPYABLE(ChunkFile);
};

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_CHUNKFILE_H_
