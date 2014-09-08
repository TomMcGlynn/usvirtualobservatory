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
    \brief  Class for matching against a chunk index.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_CHUNKINDEXMATCHER_H_
#define IPAC_STK_ASSOC_CHUNKINDEXMATCHER_H_

#include <vector>

#include "boost/filesystem.hpp"
#include "boost/scoped_ptr.hpp"
#include "boost/shared_ptr.hpp"
#include "boost/unordered_map.hpp"

#include "ChunkFile.h"
#include "ChunkIndex.h"
#include "Matchable.h"


namespace ipac { namespace stk { namespace assoc {

/** Class for matching vectors of Matchable objects to a chunk index file.
  */
class ChunkIndexMatcher {
public:
  ChunkIndexMatcher(boost::filesystem::path const & chunkIndexPath);
  ~ChunkIndexMatcher();

  /** Matches a vector of objects (of a Matchable sub-type) to a chunk index.
      May be called repeatedly.
    */
  template <typename MatchableType>
  void match(std::vector<MatchableType> & inputs, int maxMatches) {
    typedef typename std::vector<MatchableType>::iterator Iter;
    if (inputs.size() == 0) {
      return;
    }
    _inputs.reserve(inputs.size());
    _located.reserve(inputs.size());
    assert(_inputs.size() == 0 && _located.size() == 0 &&
           "Internal vectors not cleared before "
           "ChunkIndexMatcher::match() call");
    for (Iter i(inputs.begin()), e(inputs.end()); i != e; ++i) {
      _inputs.push_back(&(*i));
    }
    match(maxMatches);
    _inputs.clear();
  }

  ChunkIndex const & getChunkIndex() const {
    return *_index;
  }

private:
  typedef boost::unordered_map<int32_t, boost::shared_ptr<ChunkFile> > HashMap;
  boost::scoped_ptr<ChunkIndex> _index;
  HashMap _chunks;
  std::vector<Matchable *> _inputs;
  std::vector<ChunkIndex::LocatedMatchable> _located;

  void match(int maxMatches);
  void matchChunk(std::vector<ChunkIndex::LocatedMatchable>::iterator begin,
                  std::vector<ChunkIndex::LocatedMatchable>::iterator end,
                  int maxMatches);
};

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_CHUNKINDEXMATCHER_H_
