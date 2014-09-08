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
    \brief  Abstract class for primitives that can
            participate in a spatial match.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_ASSOC_MATCHABLE_H_
#define IPAC_STK_ASSOC_MATCHABLE_H_

#include <utility>

#include "ipac/stk/table/Record.h"
#include "ChunkFile.h"
#include "ChunkIndex.h"
#include "SphericalBox.h"


namespace ipac { namespace stk { namespace assoc {

/** Abstract base class for spatially matchable entities.
  */
class Matchable {
public:

  /** Stores a match for a Matchable.
    */
  struct Match {
    ChunkFile::Entry entry;
    double distance;
    double positionAngle;
  };
  
  explicit Matchable(ipac::stk::table::Record const & data);

  virtual ~Matchable();

  /** Returns the data this Matchable was built from.
    */
  ipac::stk::table::Record const & getData() const {
    return _data;
  }

  //@{
  /** Returns the spherical bounding box for this Matchable.
    */
  SphericalBox const & getEnvelope() const {
    return _envelope;
  }
  SphericalBox & getEnvelope() {
    return _envelope;
  }
  //@}

  /** Tests whether \c candidate matches this Matchable. If so, the
      distance and position angle between this Matchable and \c candidate
      are computed and stored in \c candidate, and \c true is returned.

      \param[inout] candidate   The position to test against this Matchable.
      \return                   \c true if \c candidate matches this Matchable.
    */
  virtual bool matches(Match & candidate) const = 0;

  /** Adds \c match to the list of matches being tracked for this Matchable.
      Assumes that \c match.distance has been computed.
    */
  void addMatch(Match const & match, int const maxMatches) {
    _numMatches += 1;
    if (maxMatches < 0) {
      _matches.push_back(match);
    } else if (maxMatches > 0) {
      // Maintain a heap of the maxMatches closest matches.
      if (_matches.size() < static_cast<size_t>(maxMatches)) {
        _matches.push_back(match);
        std::push_heap(_matches.begin(), _matches.end());
      } else if (match.distance < _matches[0].distance) {
        std::pop_heap(_matches.begin(), _matches.end());
        _matches.back() = match;
        std::push_heap(_matches.begin(), _matches.end());
      }
    }
  }

  /** Returns the list of matches for this Matchable. This may not
      include all matches: if a maximum match count K was specified,
      then only the K closest matches are returned.
    */
  std::vector<Match> const & getMatches() const {
    return _matches;
  }

  /** Returns the list of matches for this Matchable. This may not
      include all matches: if a maximum match count K was specified,
      then only the K closest matches are returned. In that case
      the vector returned is a heap, and can be sorted on match
      distance by calling \c std::sort_heap().
    */
  std::vector<Match> & getMatches() {
    return _matches;
  }

  /** Returns the total number of matches for this Matchable. */
  size_t getNumMatches() const {
    return _numMatches;
  }

private:
  SphericalBox _envelope;
  ipac::stk::table::Record _data;
  std::vector<Match> _matches;
  size_t _numMatches;
};

inline bool operator<(Matchable::Match const & m1,
                      Matchable::Match const & m2)
{
  return m1.distance < m2.distance;
}

/** Functor for comparing pointers to Matchable objects by comparing the
    minimum latitude angles of their bounding boxes.
  */
struct MatchablePtrMinPhiComparator {
  bool operator()(Matchable const * m1, Matchable const * m2) const {
    return m1->getEnvelope().getMinPhi() < m2->getEnvelope().getMaxPhi();
  }
};

}}} // namespace ipac::stk::assoc

#endif // IPAC_STK_ASSOC_MATCHABLE_H_
