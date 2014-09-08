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
    \brief  IPAC ASCII table reader class.
    \author Serge Monkewitz
  */
#ifndef IPAC_STK_TABLE_IPACTABLEREADER_H_
#define IPAC_STK_TABLE_IPACTABLEREADER_H_

#include <stdlib.h>

#include <cctype>
#include <cerrno>

#include <algorithm>
#include <limits>
#include <map>
#include <string>
#include <vector>

#include "boost/filesystem.hpp"
#include "boost/scoped_ptr.hpp"

#include "ipac/stk/except.h"
#include "ipac/stk/util.h"
#include "Record.h"


namespace ipac { namespace stk { namespace table {

/** \brief  Class for reading both fixed and variable width IPAC table files.

    IO is performed using memory-mapping in conjunction with the \c madvise()
    system call; pages are requested in blocks with a power-of-2 size between
    64 KiB and 64MiB. The reader is optimized for sequential reads of a
    blocks worth of rows, and will read-ahead of the current block by 1
    full block. Methods for accessing 1 row at a time and for seeking
    within the table are also provided.
    
    Pointers to rows returned by nextRow() and nextRows() remain valid for
    the lifetime of the IPACTableReader instance; however, due to the
    memory-mapping strategy employed, only 1 or 2 blocks worth of the file
    will typically be resident in physical memory. Accesses outside of the
    currently resident block(s) will therefore result in page misses. This
    can be avoided to some extent by raising the block size, but for very
    large tables, processing should be arranged to operate block by block.
  */
class IPACTableReader {
public:

  static size_t const MIN_BLOCK_SIZE = 65536; ///< 64KiB
  static size_t const MAX_BLOCK_SIZE = 67108864; ///< 64MiB
  static size_t const INDEX_GRANULARITY = 1024; ///< 1024 rows

  /** Immutable descriptor for a single column in an IPAC ASCII table.
    */
  class Column {
  public:
    Column();
    Column(std::string const & name, std::string const & type,
           std::string const & units, std::string const & null,
           size_t index, size_t begin, size_t end);
    ~Column();

    /** Returns the value of the column as an \c Integer.
      */
    template <typename Integer>
    Integer getInteger(Record const & rec) const {
      char const * colEnd = end(rec);
      if (!colEnd) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': value is null, contains only whitespace, "
          "or crosses column boundaries",
          static_cast<unsigned long long>(rec.rowid + 1), _name.c_str()));
      }
      errno = 0;
      char * endp = 0;
      long long value = ::strtoll(rec.begin + _begin, &endp, 0);
      if (errno != 0 ||
          value > std::numeric_limits<Integer>::max() ||
          value < std::numeric_limits<Integer>::min()) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': string to integer conversion failed",
          static_cast<unsigned long long>(rec.rowid + 1), _name.c_str()));
      }
      if (endp < colEnd) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': column either does not contain a valid "
          "integer, consists of two or more values, or includes extraneous "
          "characters", static_cast<unsigned long long>(rec.rowid + 1),
          _name.c_str()));
      }
      return static_cast<Integer>(value);
    }

    /** Returns the value of the column as a \c double.
      */
    double getDouble(Record const & rec) const {
      char const * colEnd = end(rec);
      if (!colEnd) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': value is null, contains only whitespace, "
          "or crosses column boundaries",
          static_cast<unsigned long long>(rec.rowid + 1), _name.c_str()));
      }
      errno = 0;
      char * endp = 0;
      double value = std::strtod(rec.begin + _begin, &endp);
      if (errno != 0) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': string to floating point conversion failed",
          static_cast<unsigned long long>(rec.rowid + 1), _name.c_str()));
      }
      if (endp < colEnd) {
        throw IPAC_STK_EXCEPT(ipac::stk::except::Format, except::message(
          "Row %llu column '%s': column either does not contain a valid "
          "floating point number, consists of two or more values, or "
          "includes extraneous characters",
          static_cast<unsigned long long>(rec.rowid + 1), _name.c_str()));
      }
      return value;
    }

    std::string const & getName() const {
      return _name;
    }
    std::string const & getType() const {
      return _type;
    }
    std::string const & getUnits() const {
      return _units;
    }
    std::string const & getNull() const {
      return _null;
    }
    int getIndex() const {
      return _index;
    }
    int getWidth() const {
      return _end - _begin;
    }
    int begin() const {
      return _begin;
    }
    int end() const {
      return _end;
    }
    static bool isValidName(std::string const & name);
    static bool isValidType(std::string const & type);
    static bool isValidUnits(std::string const & units);

  private:
    /** Returns \c 0 if the value of the column is null, all whitespace,
        or data is present at column boundary positions. Otherwise, a
        pointer to the character following the last non-whitespace character
        in the column value is returned.
      */
    char const * end(Record const & rec) const {
      char const * colBegin = rec.begin + _begin;
      if (colBegin >= rec.end) {
        return 0;
      }
      if (std::isspace(colBegin[-1]) == 0) {
        return 0;
      }
      char const * colEnd = rec.end;
      if (rec.begin + _end < rec.end) {
        colEnd = rec.begin + _end;
        if (std::isspace(*colEnd) == 0) {
          return 0;
        }
      }
      while (colBegin < colEnd && std::isspace(*colBegin)) {
        ++colBegin;
      }
      while (colEnd > colBegin && std::isspace(colEnd[-1])) {
        --colEnd;
      }
      if (colBegin == colEnd) {
        return 0;
      }
      size_t n = static_cast<size_t>(colEnd - colBegin); 
      return _null.compare(0, _null.size(), colBegin, n) == 0 ? 0 : colEnd;
    }

    std::string _name;
    std::string _type;
    std::string _units;
    std::string _null;
    int _index; ///< Index of the column within the table.
    int _begin; ///< Index of the first character in the column.
    int _end;   ///< Index of the character following the last column character.
  };

  /** Container for IPAC ASCII table metadata.
    */
  class Metadata {
  public:
    Metadata(boost::filesystem::path const & path,
             char const * const data,
             size_t const size,
             bool caseInsensitive);
    ~Metadata();

    /** Returns \c true if rows are fixed width.
      */
    bool isFixedWidth() const {
      return _lineLength > 0;
    }

    /** Returns \c true if table column names are case insensitive.
      */
    bool isCaseInsensitive() const {
      return _icase;
    }

    /** Returns the row length in bytes, or 0 if rows are variable length.
      */
    size_t getLineLength() const {
      return _lineLength;
    }

    /** Returns the offset in bytes of the first row.
      */
    size_t getDataStart() const {
      return _dataStart;
    }

    /** Returns the path of the file being read.
      */
    boost::filesystem::path const & getPath() const {
      return _path;
    }

    /** Returns the entire IPAC table header as a string.
      */
    std::string const & getHeader() const {
      return _header;
    }

    bool hasColumn(std::string const & name) const;
    Column const & getColumn(std::string const & name) const;
    std::vector<Column> getColumns(std::vector<std::string> const & names) const;
    std::vector<Column> getColumns() const;

    bool hasKey(std::string const & key) const;
    std::string const & getKey(std::string const & key) const;

  private:
    struct StringComparator {
      bool ic;
      StringComparator(bool icase) : ic(icase) { }
      bool operator()(std::string const & s1, std::string const & s2) const;
    };
    
    boost::filesystem::path _path;
    std::map<std::string, Column, StringComparator> _columnMap;
    std::map<std::string, std::string> _keyValueMap;
    std::vector<std::string> _comments;
    std::string _header;
    size_t _dataStart;
    size_t _lineLength;
    bool _icase;
  };

  IPACTableReader(boost::filesystem::path const & path,
                  size_t blockSize=MIN_BLOCK_SIZE,
                  size_t indexGranularity=INDEX_GRANULARITY,
                  bool caseInsensitive=false);
  ~IPACTableReader();

  /** Returns the 0-based index of the next row to return from nextRow().
    */
  size_t getRowId() const {
    return _rowId;
  }

  /** Returns metadata for the table being read.
    */
  Metadata const & getMetadata() const {
    return *_metadata;
  }

  /** Returns the size of the table file in bytes.
    */
  size_t size() const {
    return _size;
  }

  /** Returns \c true if a call to nextRow() will return more rows.
    */
  bool hasNext() const {
    return _rowOffset < _size;
  }

  size_t getNumRows();
  void seek(size_t rowId);
  Record const nextRow();
  void nextRows(std::vector<Record> & rows);

private:
  ipac::stk::util::FileDescriptor _fd;
  ipac::stk::util::MappedMemory _data;
  boost::scoped_ptr<Metadata> _metadata;
  std::vector<bool> _resident; ///< Bitmask of resident blocks
  std::vector<size_t> _rowIndex; ///< Offset of every 2^N-th row in the file
  size_t _granularityLog2; ///< Base-2 logarithm of row index granularity.
  size_t _size;       ///< Size of the table file in bytes
  size_t _blockSize;  ///< Power-of-2 block size
  int _blockSizeLog2; ///< Base-2 logarithm of the block size
  size_t _numBlocks;  ///< Number of data blocks in table file
  size_t _curBlock;   ///< Current block index
  size_t _rowId;      ///< Index of next row to fetch
  size_t _rowOffset;  ///< Offset of \c _rowId in table file
  size_t _numRows;    ///< Total number of rows in table
  bool _numRowsValid; ///< \c true if \c _numRows is valid

  void markResident(size_t fromBlock, size_t toBlock);
  void setCurBlock(size_t block,
                   ipac::stk::util::MappedMemory::Advice advice) throw();
  void scan(size_t untilRow);

  IPAC_STK_NONCOPYABLE(IPACTableReader);
};

inline bool operator<(IPACTableReader::Column const & c1,
                      IPACTableReader::Column const & c2)
{
  return c1.getIndex() < c2.getIndex();
}

bool operator==(IPACTableReader::Column const & c1,
                IPACTableReader::Column const & c2);

inline bool operator!=(IPACTableReader::Column const & c1,
                       IPACTableReader::Column const & c2)
{
  return !(c1 == c2);
}

}}} // namespace ipac::stk::table

#endif // IPAC_STK_TABLE_IPACTABLEREADER_H_

