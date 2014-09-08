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
    \brief  Class for dealing with memory regions allocated via \c mmap().
            Currently only read-only memory is supported.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_UTIL_MAPPEDMEMORY_H_
#define IPAC_STK_UTIL_MAPPEDMEMORY_H_

#include "ipac/stk/config.h"
#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif
#include <sys/types.h>
#include <sys/mman.h>

#include <algorithm>
#include <utility>

#include "macros.h"


namespace ipac { namespace stk { namespace util {

/** RAII class for memory mapped regions.
    Support is currently limited to read-only regions.
  */
class MappedMemory {
public:
  enum Advice {
    NORMAL     = MADV_NORMAL,      ///< Prefer the default I/O strategy.
    SEQUENTIAL = MADV_SEQUENTIAL,  ///< Expect sequential page access.
    RANDOM     = MADV_RANDOM,      ///< Expect random page access.
    WILLNEED   = MADV_WILLNEED,    ///< Prefetch pages please.
    DONTNEED   = MADV_DONTNEED     ///< No need to keep data in memory.
  };

  MappedMemory();
  ~MappedMemory();

  void const * getData() const {
    return _data;
  }
  void * getData() {
    return _data;
  }
  bool isValid() const {
    return _data != MAP_FAILED;
  }
  size_t getSize() const {
    return _size;
  }
  size_t getOffset() const {
    return _offset;
  }
  void advise(Advice advice) const throw();
  void advise(Advice advice, size_t offset, size_t size) const throw();

  void swap(MappedMemory & other) {
    std::swap(_data, other._data);
    std::swap(_size, other._size);
    std::swap(_offset, other._offset);
  }

  static MappedMemory * mapReadOnly(int fd, size_t offset, size_t size);

  static std::pair<size_t, size_t> const round(size_t offset, size_t size);

private:
  MappedMemory(void * data, size_t offset, size_t size);

  void * _data;
  size_t _offset;
  size_t _size;

  IPAC_STK_NONCOPYABLE(MappedMemory);
};

inline void swap(MappedMemory & m1, MappedMemory & m2) {
  m1.swap(m2);
}

}}} // namespace ipac::stk::util

#endif // IPAC_STK_UTIL_MAPPEDMEMORY_H_
