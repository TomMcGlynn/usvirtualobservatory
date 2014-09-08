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
    \brief  MappedMemory class implementation.
    \author Serge Monkewitz
  */
#include "ipac/stk/util/MappedMemory.h"

#include <cerrno>
#include <unistd.h>

#include "ipac/stk/except.h"

// _POSIX_C_SOURCE > 2 and -DEXTENSIONS hides this on Solaris
#if (defined(sun) || defined(_sun)) && \
    ((defined(_POSIX_C_SOURCE) && _POSIX_C_SOURCE > 2) || defined (__EXTENSIONS__))
  extern "C" int madvise(void *addr, size_t len, int behav);
#endif


namespace ipac { namespace stk { namespace util {

using std::make_pair;
using std::pair;

namespace except = ipac::stk::except;

namespace {

/// Round x down to the nearest multiple of pageSize; assumes pageSize > 0.
template <typename Integer>
Integer roundDown(Integer const x, int const pageSize) {
  Integer quo = x / pageSize;
  Integer multiple = quo * pageSize;
  // The rounding direction of x / pageSize may be
  // implementation defined when x < 0
  return multiple > x ? multiple - pageSize : multiple;
}

// Round x up to the nearest multiple of pageSize; assumes pageSize > 0.
template <typename Integer>
Integer roundUp(Integer const x, int const pageSize) {
  Integer quo = x / pageSize;
  Integer multiple = quo * pageSize;
  // The rounding direction of x / pageSize may be
  // implementation defined when x < 0
  return multiple < x ? multiple + pageSize : multiple;
}

int const PAGE_SIZE = ::getpagesize();

} // namespace


/** Creates a MappedMemory object corresponding to a region with invalid
    address and zero size. This can be used to relinquish ownership of a
    memory region:
    \code
    MappedMemory m = ...
    MappedMemory().swap(m);
    \endcode
  */
MappedMemory::MappedMemory() : _data(MAP_FAILED), _offset(0), _size(0) {}

/** Takes ownership of a memory region allocated with \c mmap(). The address
    of the region as well as its size must be a multiple of the memory page
    size, as returned by \c getpagesize().

    \param[in]  data    Address of memory region; must be a multiple of the
                        memory page size.
    \param[in]  offset  Offset within memory region, identifies the first
                        byte of interest (not necessarily page-aligned)
                        within the memory region.
    \param[in]  size    Size of memory region; must be a multiple of the
                        memory page size.
  */
MappedMemory::MappedMemory(void * data, size_t offset, size_t size)
  : _data(MAP_FAILED), _offset(0), _size(0)
{
  if (data != MAP_FAILED && reinterpret_cast<size_t>(data) % PAGE_SIZE != 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "memory region is not "
                          "page aligned");
  }
  if (data != MAP_FAILED && size % PAGE_SIZE != 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "memory region size "
                          "is not a multiple of the page size");
  }
  _data = data;
  _size = size;
  _offset = offset;
}

MappedMemory::~MappedMemory() {
  if (_data != MAP_FAILED && _size != 0) {
    ::munmap(_data, _size);
    _data = MAP_FAILED;
    _size = 0;
  }
}

/** Creates a new read-only memory region mapped from the file with the given
    descriptor. The allocated region will begin at the first multiple of the
    memory page size less than or equal to \c offset, and will consist of the
    smallest number of pages necessary to completely contain the requested range
    of bytes.

    \param[in]  fd      Descriptor of the file to map.
    \param[in]  offset  Offset of the first byte to map.
    \param[in]  size    Number of bytes to map.

    \return A new read read-only memory region corresponding to the given
            range of bytes from the given file.

    \throw  ipac::except::InvalidParameter
            If \c size is zero or too large.
    \throw  ipac::except::OSError
            If the requested region cannot be memory mapped.
  */
MappedMemory * MappedMemory::mapReadOnly(int fd, size_t offset, size_t size) {
  if (size == 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Zero byte memory mapping request");
  }
  size_t end = offset + size;
  if (end - 1 < offset) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Memory mapping region "
                          "size is too large (size + offset overflowed)");
  }
  size_t mapBegin = roundDown(offset, PAGE_SIZE);
  size_t mapEnd = roundUp(end, PAGE_SIZE);
  size_t mapSize = mapEnd - mapBegin;

  // The order in which the new operator and contructor arguments are
  // evaluated is undefined by the C++ standard. So, to avoid resource leaks,
  // construct a temporary on the stack and swap with a dynamically allocated
  // MappedMemory object.
  MappedMemory tmp(::mmap(0, mapSize, PROT_READ, MAP_SHARED | MAP_NORESERVE,
                          fd, mapBegin), offset - mapBegin, mapSize);
  if (tmp.getData() == MAP_FAILED) {
    throw IPAC_STK_EXCEPT(except::OSError, errno, "mmap() failed");
  }
  MappedMemory * region = new MappedMemory();
  region->swap(tmp);
  return region;
}

/** Advises the OS about the expected access pattern for this memory region.
    The OS is free to completely ignore such advice, but accurately providing
    it can improve application performance.

    \param[in]  advice  I/O access pattern hint.
  */
void MappedMemory::advise(MappedMemory::Advice advice) const throw() {
  advise(advice, _size, 0);
}

/** Advises the OS about the expected access pattern for a subset of this
    memory region. The OS is free to completely ignore such advice, but
    accurately providing it can improve application performance. The
    specified sub-region is padded to a multiple of the page size and clipped
    to the boundaries of the underlying memory region.

    \param[in]  advice  I/O access pattern hint.
    \param[in]  offset  Offset of memory sub-region for which
                        \c advice is valid.
    \param[in]  size    Size of memory sub-region for which
                        \c advice is valid.
  */
void MappedMemory::advise(MappedMemory::Advice advice, size_t offset,
                          size_t size) const throw() {
  if (_data != MAP_FAILED && _size != 0 && size > 0) {
    size_t adviceEnd;
    size_t end = offset + size;
    size_t adviceBegin = roundDown(offset, PAGE_SIZE);
    if (end - 1 < offset) {
      adviceEnd = _size;
    } else {
      adviceEnd = roundUp(end, PAGE_SIZE);
      if (adviceEnd > _size) {
        adviceEnd = _size;
      }
    }
    if (adviceEnd > adviceBegin) {
      ::madvise(static_cast<unsigned char *>(_data) + adviceBegin,
                adviceEnd - adviceBegin, advice);
    }
  }
}

/** Rounds the given range of memory locations to the smallest page aligned
    range completely enclosing the input range.

    \param[in] offset  Offset of first memory location.
    \param[in] size    Size of range in bytes.

    \return A pair \code (start, end) \endcode where \c start is the index
            of the first byte in the smallest page aligned range enclosing
            \code (offset, offset + size) \endcode and \c end is the index
            of the byte following the last byte in the aligned range.
  */
pair<size_t, size_t> const MappedMemory::round(size_t offset,
                                               size_t size) {
  size_t end = size + offset;
  if (size > 0 && end - 1 < offset) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Memory region size is "
                          "too large (size + offset overflowed)");
  }
  return make_pair(roundDown(offset, PAGE_SIZE), roundUp(end, PAGE_SIZE));
}

}}} // namespace ipac::stk::util
