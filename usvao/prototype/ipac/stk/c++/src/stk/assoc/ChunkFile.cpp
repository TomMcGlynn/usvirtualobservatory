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
    \brief  ChunkFile class implementation.
    \author Serge Monkewitz
  */
#include "ipac/stk/assoc/ChunkFile.h"
#include "ipac/stk/config.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>

#include <cerrno>
#include <cmath>
#include <algorithm>
#include <fstream>
#include <limits>

#if HAVE_ZLIB_H
# include "zlib.h"
#endif
#if HAVE_INTEL_IPP
# include "ippvm.h"
# include "boost/shared_ptr.hpp"
#else
# include "boost/scoped_array.hpp"
#endif

#include "ipac/stk/assoc/constants.h"
#include "ipac/stk/assoc/Matchable.h"
#include "ipac/stk/except.h"
#include "ipac/stk/util/hadoop/io.h"
#include "ipac/stk/util/macros.h"


namespace ipac { namespace stk { namespace assoc {

using std::cos;
using std::max;
using std::min;
using std::numeric_limits;
using std::pair;
using std::sin;
using std::string;
using std::swap;
using std::vector;
using boost::tuple;
using boost::make_tuple;
using Eigen::Vector4d;
using ipac::stk::util::MappedMemory;

namespace except = ipac::stk::except;
namespace hadoop = ipac::stk::util::hadoop;
namespace util = ipac::stk::util;

namespace {

/** \internal
    Merge threshold for reads: if two read requests are separated
    by less than this number of bytes, a single request spanning the
    gap is issued.
  */
size_t const READ_GAP_MERGE_THRESHOLD = 16384;


void computeCenterVec(double const * __restrict thetaRad,
                      double const * __restrict phiRad,
                      double * __restrict x,
                      double * __restrict y,
                      double * __restrict z,
                      double * __restrict tmp,
                      int n)
{
#if HAVE_INTEL_IPP
  ippsSinCos_64f_A53(thetaRad, y, x, n);
  ippsSinCos_64f_A53(phiRad, z, tmp, n);
  ippsMul_64f_I(tmp, x, n);
  ippsMul_64f_I(tmp, y, n);
#else
  for (int i = 0; i < n; ++i) {
    x[i] = cos(thetaRad[i]);
    y[i] = sin(thetaRad[i]);
  }
  for (int i = 0; i < n; ++i) {
    tmp[i] = cos(phiRad[i]);
    z[i]   = sin(phiRad[i]);
  }
  for (int i = 0; i < n; ++i) {
    x[i] *= tmp[i];
    y[i] *= tmp[i];
  }
#endif
}

/** \internal
    Reads the contents of a binary lane, storing the results in \c entries.
  */
void readBinaryLane(size_t const numEntries,
                    unsigned char const * buf,
                    unsigned char const * const bufEnd,
                    vector<ChunkFile::Entry> & entries)
{
  entries.resize(numEntries);
  size_t rem = numEntries % DOUBLE_BUF_GRANULARITY;
  size_t n = numEntries;
  if (rem != 0) {
    n += DOUBLE_BUF_GRANULARITY - rem;
  }
#if HAVE_INTEL_IPP
  boost::shared_ptr<double> dbuf(::ippsMalloc_64f(static_cast<int>(n * 6)),
                                 ::ippsFree);
  if (!dbuf) {
    throw std::bad_alloc("ippsMalloc() failed");
  }
#else
  boost::scoped_array<double> dbuf(new double[n * 6]);
#endif
  int64_t thetaRef = 0;
  int64_t phiRef = 0;
  double * theta = dbuf.get() + 0 * n;
  double * phi   = dbuf.get() + 1 * n;
  double * x     = dbuf.get() + 2 * n;
  double * y     = dbuf.get() + 3 * n;
  double * z     = dbuf.get() + 4 * n;
  double * tmp   = dbuf.get() + 5 * n;
  for (size_t i = 0; i < numEntries; ++i) {
    entries[i].uik = hadoop::readVInt64(buf, &buf);
    int64_t thetaFix = thetaRef + hadoop::readVInt64(buf, &buf);
    int64_t phiFix = phiRef + hadoop::readVInt64(buf, &buf);
    if (buf > bufEnd) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "Lane data overflowed lane boundaries");
    }
    thetaRef = thetaFix;
    phiRef = phiFix;
    theta[i] = RAD_PER_FIX * static_cast<double>(thetaFix);
    phi[i] = RAD_PER_FIX * static_cast<double>(phiFix);
    entries[i].theta = DEG_PER_FIX * static_cast<double>(thetaFix);
    entries[i].phi = DEG_PER_FIX * static_cast<double>(phiFix);
  }
  computeCenterVec(theta, phi, x, y, z, tmp, static_cast<int>(numEntries));
  for (size_t i = 0; i < numEntries; ++i) {
    entries[i].v = Vector4d(x[i], y[i], z[i], 0.0);
  }
}

/** \internal
    Reads the contents of a textual lane, storing the results in \c entries.
  */
void readTextLane(size_t const numEntries,
                  unsigned char const * buf,
                  unsigned char const * const bufEnd,
                  vector<ChunkFile::Entry> & entries)
{
  entries.resize(numEntries);
  size_t rem = numEntries % DOUBLE_BUF_GRANULARITY;
  size_t n = numEntries;
  if (rem != 0) {
    n += DOUBLE_BUF_GRANULARITY - rem;
  }
#if HAVE_INTEL_IPP
  // Use the IPP (32 byte) aligned memory allocation function
  boost::shared_ptr<double> dbuf(::ippsMalloc_64f(static_cast<int>(n * 6)),
                                 ::ippsFree);
  if (!dbuf) {
    throw std::bad_alloc("ippsMalloc() failed");
  }
#else
  boost::scoped_array<double> dbuf(new double[n * 6]);
#endif
  double * theta = dbuf.get() + 0 * n;
  double * phi   = dbuf.get() + 1 * n;
  double * x     = dbuf.get() + 2 * n;
  double * y     = dbuf.get() + 3 * n;
  double * z     = dbuf.get() + 4 * n;
  double * tmp   = dbuf.get() + 5 * n;
  for (size_t i = 0; i < numEntries; ++i) {
    int32_t len = hadoop::readVInt32(buf, &buf);
    if (len <= 0) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "Negative or zero record length in lane");
    }
    if (buf + len > bufEnd) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "Lane data overflowed lane boundaries");
    }
    entries[i].data.replace(0u, entries[i].data.size(),
                            reinterpret_cast<char const *>(buf),
                            static_cast<size_t>(len));
    string const & s = entries[i].data;
    size_t thetaOff = s.find_first_of('\0', 0);
    if (thetaOff == string::npos || thetaOff + 1 >= static_cast<size_t>(len)) {
      throw IPAC_STK_EXCEPT(except::Format, "Missing longitude angle "
                            "field in chunk file entry");
    }
    // extract unique integer id
    errno = 0;
    long long ival = ::strtoll(s.c_str(), 0, 0);
    if (errno != 0 ||
        ival > numeric_limits<int64_t>::max() ||
        ival < numeric_limits<int64_t>::min()) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "String to integer conversion failed");
    }
    entries[i].uik = static_cast<int64_t>(ival);
    size_t phiOff = s.find_first_of('\0', thetaOff + 1);
    if (phiOff == string::npos || phiOff + 1 >= s.size()) {
      throw IPAC_STK_EXCEPT(except::Format, "Missing latitude angle "
                            "field in chunk file entry");
    }
    // extract longitude angle
    entries[i].theta = std::strtod(s.c_str() + (thetaOff + 1), 0);
    if (errno != 0) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "String to double conversion failed");
    }
    theta[i] = entries[i].theta * RAD_PER_DEG;
    // extract latitude angle
    entries[i].phi = std::strtod(s.c_str() + (phiOff + 1), 0);
    if (errno != 0) {
      throw IPAC_STK_EXCEPT(except::Format,
                            "String to double conversion failed");
    }
    phi[i] = entries[i].phi * RAD_PER_DEG;
    buf += len;
  }
  computeCenterVec(theta, phi, x, y, z, tmp, static_cast<int>(numEntries));
  for (size_t i = 0; i < numEntries; ++i) {
    entries[i].v = Vector4d(x[i], y[i], z[i], 0.0);
  }
}

/** \internal
    Collects the lane ranges spanned by the given Matchable objects.
  */
void collectLanes(vector<ChunkFile::LaneRange> & lanes,
                  vector<ChunkIndex::LocatedMatchable>::iterator const begin,
                  vector<ChunkIndex::LocatedMatchable>::iterator const end)
{
  ChunkIndex::LaneRange r = begin->first;
  ChunkFile::LaneRange cur(r.getSubStripe(), r.getMinLane(), r.getMaxLane());
  vector<ChunkIndex::LocatedMatchable>::iterator i(begin);
  for (++i; i != end; ++i) {
    ChunkIndex::LaneRange r = i->first;
    if (cur.get<0>() != r.getSubStripe() || cur.get<2>() < r.getMinLane() - 1) {
      lanes.push_back(cur);
      cur = make_tuple(r.getSubStripe(), r.getMinLane(), r.getMaxLane());
      continue;
    }
    cur.get<2>() = r.getMaxLane();
  }
  lanes.push_back(cur);
}

/** \internal
    Removes matchables with a maximum lane number less than \c lane from \c v.
  */
void removeBelow(vector<ChunkIndex::LocatedMatchable> & v, int32_t lane) {
  typedef vector<ChunkIndex::LocatedMatchable>::iterator Iter;
  Iter s(v.begin());
  for (Iter i(v.begin()), e(v.end()); i != e; ++i) {
    if (i->first.getMaxLane() >= lane) {
      if (i != s) {
        *s = *i;
      }
      ++s;
    }
  }
  v.erase(s, v.end());
}

} // namespace


// -- ChunkFile --------

/** Creates a new ChunkFile by reading the trailer of the given file.

    \param[in] path   A chunk file path.
  */
ChunkFile::ChunkFile(boost::filesystem::path const & path) :
  _path(path),
  _fileSize(0),
  _fd(),
  _data(),
  _numS(0),
  _stripe(0),
  _chunk(0),
  _overlapSS(0),
  _numEntries(0),
  _numOverlapEntries(0),
  _binary(false),
  _zipped(false),
  _ssLanes(),
  _overlapLanes(),
  _lanes(),
  _entries()
{
  _fileSize = acquire();
  if (_fileSize < static_cast<size_t>(TRAILER_OFF_OFF)) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read trailer for "
                          "chunk file " + path.file_string() +
                          " : file is too small");
  }
  unsigned char const * buf =
    static_cast<unsigned char const *>(_data->getData());
  size_t off = _fileSize - static_cast<size_t>(TRAILER_OFF_OFF);
  int64_t trailerOffset = hadoop::readInt64(buf + off);
  if (hadoop::readInt32(buf + (off + 8)) != VERSION) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read chunk file " +
                          path.file_string() + " : file format version "
                          "mismatch");
  }
  if (hadoop::readInt64(buf + (off + 12)) != MAGIC) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read chunk file " +
                          path.file_string() + " : missing magic bytes");
  }
  if (static_cast<size_t>(trailerOffset + MIN_TRAILER_OFF) > _fileSize) {
    throw IPAC_STK_EXCEPT(except::Format, "Could not read chunk file " +
                          path.file_string() + " : invalid trailer offset");
  }
  buf += trailerOffset;
  // Read file trailer
  _numS = hadoop::readVInt32(buf, &buf);
  _stripe = hadoop::readVInt32(buf, &buf);
  _chunk = hadoop::readVInt32(buf, &buf);
  _numEntries = hadoop::readVInt64(buf, &buf);
  _numOverlapEntries = hadoop::readVInt64(buf, &buf);
  _overlapSS = hadoop::readVInt32(buf, &buf);
  int32_t numSS = hadoop::readVInt32(buf, &buf);
  if (numSS <= 0) {
    throw IPAC_STK_EXCEPT(except::Format, "Chunk file " +
                          path.file_string() + " has negative or zero "
                          "sub-stripe count");
  }
  _zipped = hadoop::readBool(buf);
  ++buf;
  _binary = hadoop::readBool(buf);
  ++buf;
  // Read lane details
  _ssLanes.reserve(numSS);
  _overlapLanes.reserve(numSS);
  _lanes.reserve(numSS);
  int64_t lastOff = 0;
  int32_t lastLen = 0;
  for (int32_t i = 0; i < numSS; ++i) {
    _ssLanes.push_back(hadoop::readVInt32(buf, &buf));
    _overlapLanes.push_back(hadoop::readVInt32(buf, &buf));
    int32_t numLanes = hadoop::readVInt32(buf, &buf);
    if (numLanes != _ssLanes.back() + 2 * _overlapLanes.back()) {
      throw IPAC_STK_EXCEPT(except::Format, "Chunk file " +
                            path.file_string() + " has an inconsistent "
                            "sub-stripe lane count");
    }
    _lanes.push_back(vector<Lane>());
    _lanes.back().reserve(numLanes + 1);
    for (int32_t j = 0; j < numLanes; ++j) {
      int32_t pop = hadoop::readVInt32(buf, &buf);
      int32_t len = hadoop::readVInt32(buf, &buf);
      int64_t off = hadoop::readVInt64(buf, &buf);
      if (pop < 0) {
        throw IPAC_STK_EXCEPT(except::Format,
                              "Negative number of lane entries");
      }
      if (len < 0) {
        throw IPAC_STK_EXCEPT(except::Format, "Negative lane length");
      }
      if ((len == 0 && pop != 0) || (pop == 0 && len != 0)) {
        throw IPAC_STK_EXCEPT(except::Format, "Invariant violated: lane "
                              "length is 0 if and only if lane contains "
                              "no entries");
      }
      if (off < lastOff) {
        throw IPAC_STK_EXCEPT(except::Format, "Decreasing lane offsets");
      }
      if (!_zipped && off - lastOff != lastLen) {
        throw IPAC_STK_EXCEPT(except::Format, "Difference in lane offsets "
                              "does not match lane length");
      }
      _lanes.back().push_back(Lane(off, len, pop));
      lastOff = off;
      lastLen = len;
    }
    // add a dummy lane at the end of the array to record the ending
    // offset of the last lane.
    int64_t off = hadoop::readVInt64(buf, &buf);
    if (!_zipped && off - lastOff != lastLen) {
      throw IPAC_STK_EXCEPT(except::Format, "Difference in lane offsets "
                            "does not match lane length");
    }
    _lanes.back().push_back(Lane(off, 0, 0));
  }
}

ChunkFile::~ChunkFile() { }

/** Releases the system resources normally held by this class; these
    constitute a file descriptor, virtual address space, and memory for
    lane data. The semantics of the class are unaffected; if access to
    any of these becomes necessary after calling release(), then the
    underlying chunk file is reopened and mapped again. Note that header
    contents are not re-read; these are cached for the lifetime of the
    ChunkFile object.

    \return   Number of bytes of memory
  */
void ChunkFile::release() {
  _data.reset();
  _fd.reset();
}

/** \internal
    Ensures the underlying chunk file is open and memory mapped.
    
    \return   0 if the underlying chunk file was already open and memory
              mapped; otherwise, the size (in bytes) of the chunk file.
  */
size_t ChunkFile::acquire() {
  if (_data) {
    return 0;
  }
  util::FileDescriptor fd(::open(_path.file_string().c_str(), O_RDONLY));
  if (fd.get() == -1) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to open() file " +
                          _path.file_string());
  }
  struct ::stat buf;
  if (::fstat(fd.get(), &buf) != 0) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to fstat() file " +
                          _path.file_string());
  }
  if (buf.st_size <= 0 ||
      static_cast<uintmax_t>(buf.st_size) > numeric_limits<size_t>::max()) {
    throw IPAC_STK_EXCEPT(except::Format, _path.file_string() + ": file is "
                          "empty, too large, or of indeterminate size");
  }
  size_t size = static_cast<size_t>(buf.st_size);
  // Memory map entire file in read only mode
  boost::scoped_ptr<MappedMemory> data(
    MappedMemory::mapReadOnly(fd.get(), 0, size));
  if (!data->isValid()) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to mmap() file " +
                          _path.file_string());
  }
  data->advise(MappedMemory::DONTNEED, 0, size);
  // Commit state
  swap(fd, _fd);
  swap(data, _data);
  return size;
}

/** \internal
    Requests memory residency for all of the specified lanes that
    have not already been read.
  */
void ChunkFile::requestResidency(vector<LaneRange> const & laneRanges) {
  typedef pair<size_t, size_t> MemRange;
  typedef vector<LaneRange>::const_iterator LRIter;

  MemRange r(0, 0);
  bool first = true;
  for (LRIter i(laneRanges.begin()), ei(laneRanges.end()); i != ei; ++i) {
    vector<Lane> const & lane = _lanes[i->get<0>()];
    for (int32_t j = i->get<1>(), ej = i->get<2>(); j <= ej; ++j) {
      size_t off = lane[j].laneOffset;
      size_t end = lane[j + 1].laneOffset;
      if (end > off) {
        MemRange p = MappedMemory::round(off, end - off);
        if (first) {
          r = p;
          first = false;
        } else {
          // Should p be merged with r?
          if (p.first <= r.second ||
              p.first - r.second <= READ_GAP_MERGE_THRESHOLD) {
            r.second = p.second;
          } else {
            // no: notify the OS that pages in r will be read sequentially soon
            _data->advise(MappedMemory::SEQUENTIAL, r.first, r.second - r.first);
            r = p;
          }
        }
      }
    }
  }
  if (r.second > r.first) {
    _data->advise(MappedMemory::SEQUENTIAL, r.first, r.second - r.first);
  }
}

/** \internal
    Finds the entries in this lane within the given latitude angle range.
  */
void ChunkFile::find(double minPhi,
                     double maxPhi,
                     ChunkFile::EntryBounds & bounds) const
{
  bounds.first = std::lower_bound(_entries.begin(), _entries.end(), minPhi,
                                  EntryPhiComparator());
  bounds.second = std::upper_bound(bounds.first, _entries.end(), maxPhi,
                                   EntryPhiComparator());
}

/** \internal
    Reads in a lane.

    \param[in] lane   The lane to read.
    \param[in] buf    Pointer to memory containing lane data.
    \param[in] end    Pointer to the byte following the last lane data byte.
  */
void ChunkFile::readLane(Lane const & lane,
                         unsigned char const * buf,
#if HAVE_ZLIB_H
                         unsigned char const * end) {
#else
                         unsigned char const *) {
#endif
  if (_zipped) {
#if HAVE_ZLIB_H
    boost::scoped_array<unsigned char> buffer(
      new unsigned char[lane.laneLength]);
    ::uLongf len = static_cast< ::uLongf>(lane.laneLength);
    ::uLongf size = len;
    int stat = ::uncompress(buffer.get(), &len, buf,
                            static_cast< ::uLongf>(end - buf));
    if (stat != Z_OK) {
      throw IPAC_STK_EXCEPT(except::RuntimeError, "zlib uncompress() "
                            "call failed with error code " + stat);
    }
    if (len != size) {
      throw IPAC_STK_EXCEPT(except::RuntimeError, "zlib uncompress() call "
                            "did not produce the expected number of bytes");
    }
    unsigned char * b = buffer.get();
    if (_binary) {
      readBinaryLane(static_cast<size_t>(lane.population),
                     b, b + lane.laneLength, _entries);
    } else {
      readTextLane(static_cast<size_t>(lane.population),
                   b, b + lane.laneLength, _entries);
    }
#else
  throw IPAC_STK_EXCEPT(except::NotSupported, "decompression of zlib "
                        "compressed chunk files not supported; recompile the "
                        "association code with zlib support and try again");
#endif
  } else {
    if (_binary) {
      readBinaryLane(static_cast<size_t>(lane.population),
                     buf, buf + lane.laneLength, _entries);
    } else {
      readTextLane(static_cast<size_t>(lane.population),
                   buf, buf + lane.laneLength, _entries);
    }
  }
}

/** \internal
    Spatially matches a single lane of this chunk file.
  */
void ChunkFile::matchLane(vector<ChunkIndex::LocatedMatchable> & v,
                          int32_t subStripe,
                          int32_t lane,
                          int maxMatches)
{
  typedef vector<ChunkIndex::LocatedMatchable>::iterator LMIter;
  typedef vector<Entry>::const_iterator EntryIter;
  if (_lanes[subStripe][lane].laneLength == 0) {
    return;
  }
  unsigned char const * laneBeg =
    static_cast<unsigned char const *>(_data->getData()) +
    _lanes[subStripe][lane].laneOffset;
  unsigned char const * laneEnd =
    static_cast<unsigned char const *>(_data->getData()) +
    _lanes[subStripe][lane + 1].laneOffset;
  readLane(_lanes[subStripe][lane], laneBeg, laneEnd);
  Matchable::Match match;
  EntryBounds bounds;
  for(LMIter i(v.begin()), ie(v.end()); i != ie; ++i) {
    Matchable * m = i->second;
    SphericalBox const & box = m->getEnvelope();
    find(box.getMinPhi(), box.getMaxPhi(), bounds);
    for (EntryIter j(bounds.first); j != bounds.second; ++j) {
      match.entry = *j;
      if (m->matches(match)) {
        m->addMatch(match, maxMatches);
      }
    }
  }
  _entries.clear();
}

/** Spatially matches the given list of Matchable objects to the positions
    in this chunk file. Assumes that input matchables have been sorted and
    all belong to this chunk.
  */
void ChunkFile::match(vector<ChunkIndex::LocatedMatchable>::iterator const begin,
                      vector<ChunkIndex::LocatedMatchable>::iterator const end,
                      int maxMatches)
{
  if (begin == end) {
    return;
  }
  // Find the range of lanes spanned by the input matchables
  vector<LaneRange> laneRanges;
  collectLanes(laneRanges, begin, end);
  // Tell OS to read-ahead for relevant pages
  acquire();
  requestResidency(laneRanges);
  // Loop over lanes, matching one lane at a time
  vector<ChunkIndex::LocatedMatchable> v;
  vector<ChunkIndex::LocatedMatchable>::iterator i = begin;
  int32_t subStripe = -1;
  int32_t lane = -1;
  while (true) {
    // advance one lane, discard matchables no longer intersecting the lane
    ++lane;
    removeBelow(v, lane);
    if (i != end) {
      int32_t nextSubStripe = i->first.getSubStripe();
      int32_t nextLane = i->first.getMinLane();
      if (v.empty() || (subStripe == nextSubStripe && nextLane == lane)) {
        subStripe = nextSubStripe;
        lane = nextLane;
#if HAVE_BUILTIN_PREFETCH
        // hack: theoretically, the Matchable memory footprint is unknown
        // at this point but in practice implementations span ~3 64 byte
        // cachelines on a 64 bit machine.
        unsigned char * addr = reinterpret_cast<unsigned char *>(i->second);
        __builtin_prefetch(static_cast<void *>(addr), 0, 3);
        __builtin_prefetch(static_cast<void *>(addr + 64), 0, 3);
        __builtin_prefetch(static_cast<void *>(addr + 128), 0, 3);
#endif
        v.push_back(*i);
        for (++i; i != end; ++i) {
          if (i->first.getSubStripe() != subStripe ||
              i->first.getMinLane() != lane) {
            break;
          }
#if HAVE_BUILTIN_PREFETCH
          unsigned char * addr = reinterpret_cast<unsigned char *>(i->second);
          __builtin_prefetch(static_cast<void *>(addr), 0, 3);
          __builtin_prefetch(static_cast<void *>(addr + 64), 0, 3);
          __builtin_prefetch(static_cast<void *>(addr + 128), 0, 3);
#endif
          v.push_back(*i);
        }
      }
    } else if (v.empty()) {
      break;
    }
    matchLane(v, subStripe, lane, maxMatches);
  }
}

}}} // namespace ipac::stk::assoc
