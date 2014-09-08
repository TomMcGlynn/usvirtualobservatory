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
    \brief  IPAC ASCII table reader implementation.
    \author Serge Monkewitz
  */
#include "ipac/stk/table/IPACTableReader.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <utility>

#include "boost/algorithm/string/predicate.hpp"
#include "boost/regex.hpp"

#include "ipac/stk/util/macros.h"
#include "ipac/stk/util/string.h"


namespace ipac { namespace stk { namespace table {

using std::map;
using std::max;
using std::min;
using std::numeric_limits;
using std::pair;
using std::sort;
using std::string;
using std::swap;
using std::vector;
using ipac::stk::util::MappedMemory;

namespace except = ipac::stk::except;
namespace fs = boost::filesystem;
namespace util = ipac::stk::util;


IPAC_STK_STATIC_ASSERT(
  (IPACTableReader::MIN_BLOCK_SIZE &
    (IPACTableReader::MIN_BLOCK_SIZE - 1)) == 0,
  "ipac::stk::table::IPACTableReader::MIN_BLOCK_SIZE is not a power of 2!"
);
IPAC_STK_STATIC_ASSERT(
  (IPACTableReader::MAX_BLOCK_SIZE &
    (IPACTableReader::MAX_BLOCK_SIZE - 1)) == 0,
  "ipac::stk::table::IPACTableReader::MAX_BLOCK_SIZE is not a power of 2!"
);
IPAC_STK_STATIC_ASSERT(
  IPACTableReader::MIN_BLOCK_SIZE > 0,
  "ipac::stk::table::IPACTableReader::MIN_BLOCK_SIZE must be > 0!"
);
IPAC_STK_STATIC_ASSERT(
  IPACTableReader::MAX_BLOCK_SIZE >= IPACTableReader::MIN_BLOCK_SIZE,
  "ipac::stk::table::IPACTableReader::MAX_BLOCK_SIZE must be >= "
  "ipac::stk::table::IPACTableReader::MIN_BLOCK_SIZE!"
);
IPAC_STK_STATIC_ASSERT(
  IPACTableReader::MAX_BLOCK_SIZE < 0x80000000,
  "ipac::stk::table::IPACTableReader::MAX_BLOCK_SIZE must fit in 32 bits!"
);


namespace {

boost::regex const NAME_REGEX("[a-zA-Z_]+[a-zA-Z0-9_]*");
boost::regex const TYPE_REGEX("[a-zA-Z0-9() \v\t\f]*");
boost::regex const UNITS_REGEX("[a-zA-Z0-9+-.*^()/\\[\\] \v\t\f]*");
boost::regex const KEY_VALUE_REGEX("^\\\\([a-zA-Z0-9_]+)\\s*=\\s*(\\S*)\\s*$");

/** \internal
    Returns a pointer to the first line terminator character in the given
    string, or \c end if there isn't one.  The "\r", "\n", and "\r\n" strings
    are all recognized as line terminators.
  */
inline char const * endOfLine(char const * const str,
                              char const * const end) {
  char const * next = str;
  while (next < end && *next != '\n' && *next != '\r') {
    ++next;
  }
  return next;
}

/** \internal
    Returns a pointer to the first character in the line following the line
    terminated at \c next, or \c end if there isn't one.  The "\r", "\n", and
    "\r\n" strings are all recognized as line terminators.
  */
inline char const * eatNewline(char const * const str,
                               char const * const end) {
  if (str >= end) {
    return end;
  }
  if (str[0] == '\r') {
    if (str + 1 < end && str[1] == '\n') {
      return str + 2;
    }
  }
  return str + 1;
}

/** \internal
    Returns a vector of delimiter character indexes in the given string.
  */
vector<size_t> delimiters(string const & s, char c) {
  vector<size_t> v;
  for (size_t i = s.find_first_of(c, 0); i != string::npos;
       i = s.find_first_of(c, i + 1)) {
    v.push_back(i);
  }
  return v;
}

/** \internal
    Returns the power of 2 between \c xmin and \c xmax nearest to \c x.
  */
size_t roundPow2(size_t x, size_t xmin, size_t xmax) {
  size_t y = x;
  if (y > xmax) {
    y = xmax;
  }
  if (y < xmin) {
    y = xmin;
  }
  if ((y & (y - 1)) == 0) {
    // y is already a power of 2
    return y;
  }
  y |= y >> 1;
  y |= y >> 2;
  y |= y >> 4;
  y |= y >> 8;
  y |= y >> 16;
  ++y;
  return (y - x < x - (y >> 1)) ? y : (y >> 1);
}

/** \internal
    Returns the floor of the binary logarithm of \c i (a 32 bit integer).
  */
int floorLog2(size_t i) {
  int hb = 0;
  if (i >= 65536) {
    i >>= 16;
    hb += 16;
  }
  if (i >= 256) {
    i >>= 8;
    hb += 8;
  }
  if (i >= 16) {
    i >>= 4;
    hb += 4;
  }
  if (i >= 4) {
    i >>= 2;
    hb += 2;
  }
  if (i >= 2) {
    hb +=  1;
  }
  return hb;
}

/** \internal
    Returns the ceiling of \c a / \c b.
  */
size_t ceilDiv(size_t a, size_t b) {
  size_t quo = a / b;
  return (a % b == 0) ? quo : quo + 1;
}

} // namespace


// -- IPACTableReader::Column --------

IPACTableReader::Column::Column() :
  _name(), _type(), _units(), _null(), _index(-1), _begin(0), _end(0) { }

IPACTableReader::Column::Column(string const & name, string const & type,
                                string const & units, string const & null,
                                size_t index, size_t begin, size_t end) :
  _name(name), _type(type), _units(units), _null(null),
  _index(index), _begin(begin), _end(end)
{
  if (begin == 0) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "IPAC ASCII table column '" + name +
                          "' begins on first character of each line");
  }
  if (begin > end) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter,
                          "Invalid IPAC ASCII table column parameters");
  }
  if (!isValidName(name)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Invalid IPAC ASCII "
                          "table column name: '" + name + "'");
  }
  if (!isValidType(type)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Invalid IPAC ASCII "
                          "table column type: '" + type + "'");
  }
  if (!isValidUnits(units)) {
    throw IPAC_STK_EXCEPT(except::InvalidParameter, "Invalid IPAC ASCII "
                          "table column units: '" + units + "'");
  }
}

IPACTableReader::Column::~Column() {}

bool IPACTableReader::Column::isValidName(string const & name) {
  return boost::regex_match(name, NAME_REGEX);
}

bool IPACTableReader::Column::isValidType(string const & type) {
  return boost::regex_match(type, TYPE_REGEX);
}

bool IPACTableReader::Column::isValidUnits(string const & units) {
  return boost::regex_match(units, UNITS_REGEX);
}

bool operator==(IPACTableReader::Column const & c1,
                IPACTableReader::Column const & c2)
{
  return c1.getIndex() == c2.getIndex() &&
         c1.getName() == c2.getName() &&
         c1.getType() == c2.getType() &&
         c1.getUnits() == c2.getUnits() &&
         c1.getNull() == c2.getNull();
}


// -- IPACTableReader::Metadata --------

bool IPACTableReader::Metadata::StringComparator::operator()(
  std::string const & s1,
  std::string const & s2
) const {
  return ic ? boost::algorithm::ilexicographical_compare(s1, s2) :
              boost::algorithm::lexicographical_compare(s1, s2);
}

IPACTableReader::Metadata::Metadata(fs::path const & path,
                                    char const * const data,
                                    size_t const size,
                                    bool const caseInsensitive) :
  _path(path),
  _columnMap(StringComparator(caseInsensitive)),
  _keyValueMap(),
  _comments(),
  _header(),
  _dataStart(0),
  _lineLength(0),
  _icase(caseInsensitive)
{
  if (size <= 0) {
    throw IPAC_STK_EXCEPT(except::Format, "IPAC table file " +
                          path.file_string() + " is empty");
  }
  char const * line = data;
  char const * const end = data + size;

  // Read comment lines and key value pairs
  while (*line == '\\' && line != end) {
    char const * next = endOfLine(line, end);
    if (next > line) {
      boost::cmatch m;
      if (boost::regex_match(line, next, m, KEY_VALUE_REGEX)) {
        string key(m[1].first, static_cast<size_t>(m[1].second - m[1].first));
        string value(m[2].first,
                     static_cast<size_t>(m[2].second - m[2].first));
        _keyValueMap.insert(pair<string, string>(key, value));
      } else {
        _comments.push_back(string(line, static_cast<size_t>(next - line)));
      }
    }
    line = eatNewline(next, end);
  }

  // Read header lines
  vector<string> headerLines;
  while (*line == '|' && line != end) {
    char const * next = endOfLine(line, end);
    char const * eh = next;
    while (eh > line && std::isspace(eh[-1])) {
      --eh;
    }
    headerLines.push_back(string(line, eh - line));
    line = eatNewline(next, end);
  }
  if (headerLines.size() < 1 || headerLines.size() > 4) {
    throw IPAC_STK_EXCEPT(except::Format, "expecting 1 to 4 header lines in " +
                          path.file_string());
  }
  _dataStart = static_cast<size_t>(line - data);
  // Ensure header lines are of reasonable and consistent length,
  // and that all column delimiters except the last line up.
  size_t headerLength = headerLines[0].size(); // does not include new-line
  if (headerLength > static_cast<size_t>(numeric_limits<int>::max())) {
    throw IPAC_STK_EXCEPT(except::Format, path.file_string() +
                          ": header line too long.");
  } else if (headerLength < 2) {
    throw IPAC_STK_EXCEPT(except::Format, path.file_string() +
                          ": header line missing column delimiter ('|').");
  } else if (headerLines[0].at(headerLength - 1) != '|') {
    throw IPAC_STK_EXCEPT(except::Format, path.file_string() +
                          ": header line not terminated by column "
                          "delimiter ('|').");
  }
  vector<size_t> pipes = delimiters(headerLines[0], '|');
  for (size_t i = 1; i < headerLines.size(); ++i) {
    if (headerLines[i].at(headerLines[i].size() - 1) != '|') {
      throw IPAC_STK_EXCEPT(except::Format, path.file_string() +
                            ": header line not terminated by column "
                            "delimiter ('|').");
    } else if (pipes != delimiters(headerLines[i], '|')) {
      throw IPAC_STK_EXCEPT(except::Format, path.file_string() +
                            ": two or more header lines have an inconsistent "
                            "numbers of column delimiters ('|' characters) or "
                            "have inconsistent column delimiter positions: "
                            "'|' characters must align vertically.");
    }
  }
  map<string, string>::const_iterator fixlen = _keyValueMap.find("fixlen");
  if (fixlen != _keyValueMap.end() && fixlen->second == "T") {
    // Fixed-length rows: read a single data line to determine line length
    line = endOfLine(line, end);
    line = eatNewline(line, end);
    _lineLength = static_cast<size_t>(line - data) - _dataStart;
  }
  // Build map of column names to column metadata
  vector<string> names = util::split(headerLines[0], '|');
  names.pop_back();
  vector<string> types(names.size(), string());
  vector<string> units(names.size(), string());
  vector<string> nulls(names.size(), string());
  switch (headerLines.size()) {
    case 4:  nulls = util::split(headerLines[3], '|'); nulls.pop_back();
    case 3:  units = util::split(headerLines[2], '|'); units.pop_back();
    case 2:  types = util::split(headerLines[1], '|'); types.pop_back();
    default: break;
  }
  for (size_t i = 1; i < names.size(); ++i) {
    size_t b = pipes[i - 1] + 1;
    size_t e = pipes[i];
    string name = util::trim(names[i], "\t\v\f ");
    Column c(name,
             util::trim(types[i], "\t\v\f "),
             util::trim(units[i], "\t\v\f "),
             util::trim(nulls[i], "\t\v\f "),
             i, b, e);
    if (_columnMap.insert(pair<string, Column>(name, c)).second == false) {
      throw IPAC_STK_EXCEPT(except::Format, path.file_string() + ": table "
                            "contains more than one column named " + name);
    }
  }
  // Copy header bytes
  string header(data, _dataStart);
  swap(header, _header);
}

IPACTableReader::Metadata::~Metadata() {}

bool IPACTableReader::Metadata::hasColumn(string const & name) const {
  return _columnMap.find(name) != _columnMap.end();
}

IPACTableReader::Column const & IPACTableReader::Metadata::getColumn(
  string const & name) const
{
  map<string, Column>::const_iterator i = _columnMap.find(name);
  if (i == _columnMap.end()) {
    throw IPAC_STK_EXCEPT(except::NotFound, _path.file_string() +
                          " does not contain a column named " + name);
  }
  return i->second;
}

vector<IPACTableReader::Column> IPACTableReader::Metadata::getColumns(
  vector<string> const & names) const
{
  vector<Column> columns;
  for(vector<string>::const_iterator i(names.begin()), e(names.end());
      i != e; ++i) {
    columns.push_back(getColumn(*i));
  }
  return columns;
}

vector<IPACTableReader::Column> IPACTableReader::Metadata::getColumns() const {
  vector<Column> columns;
  for(map<string, Column>::const_iterator i(_columnMap.begin()),
      e(_columnMap.end()); i != e; ++i) {
    columns.push_back(i->second);
  }
  // return columns in order
  sort(columns.begin(), columns.end());
  return columns;
}

bool IPACTableReader::Metadata::hasKey(string const & key) const {
  return _keyValueMap.find(key) != _keyValueMap.end();
}

string const & IPACTableReader::Metadata::getKey(string const & key) const {
  map<string, string>::const_iterator i = _keyValueMap.find(key);
  if (i == _keyValueMap.end()) {
    throw IPAC_STK_EXCEPT(except::NotFound, _path.file_string() +
                          " does not contain a key named " + key);
  }
  return i->second;
}


// -- IPACTableReader --------

/** Creates a new IPACTableReader object for the given file, performing
    reads in blocks of \c blockSize bytes.
  */
IPACTableReader::IPACTableReader(fs::path const & path,
                                 size_t const blockSize,
                                 size_t const indexGranularity,
                                 bool const caseInsensitive) :
  _fd(), _data(), _metadata(0), _resident(),
  _rowIndex(), _granularityLog2(roundPow2(indexGranularity, 64, 65536)),
  _size(0), _blockSize(0), _blockSizeLog2(0), _numBlocks(0), _curBlock(0),
  _rowId(0), _rowOffset(0), _numRows(0), _numRowsValid(false)
{
  util::FileDescriptor fd(::open(path.file_string().c_str(), O_RDONLY));
  if (fd.get() == -1) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to open() file " +
                          path.file_string());
  }
  struct ::stat buf;
  if (::fstat(fd.get(), &buf) != 0) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to fstat() file " +
                          path.file_string());
  }
  if (buf.st_size <= 0 ||
      static_cast<uintmax_t>(buf.st_size) > numeric_limits<size_t>::max()) {
    throw IPAC_STK_EXCEPT(except::Format, path.file_string() + ": file is "
                          "empty, too large, or of indeterminate size");
  }
  _size = static_cast<size_t>(buf.st_size);
  // Memory map entire file in read only mode
  boost::scoped_ptr<MappedMemory> data(
    MappedMemory::mapReadOnly(fd.get(), 0, _size));
  if (!data->isValid()) {
    throw IPAC_STK_EXCEPT(except::IOError, errno, "failed to mmap() file " +
                          path.file_string());
  }
  _blockSize = roundPow2(blockSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
  _blockSizeLog2 = floorLog2(_blockSize);
  // Advise the OS that the first 2 blocks of the file will be needed soon
  data->advise(MappedMemory::WILLNEED, 0, 2 * _blockSize);
  data->advise(MappedMemory::DONTNEED, 2 * _blockSize, _size - 2 * _blockSize);
  // Read and parse the table header
  boost::scoped_ptr<Metadata> metadata(new Metadata(
    path, static_cast<char const *>(data->getData()), _size, caseInsensitive));
  _rowOffset = metadata->getDataStart();
  size_t firstBlock = _rowOffset >> _blockSizeLog2;
  _numBlocks = ceilDiv(_size, _blockSize);
  if (metadata->isFixedWidth()) {
    _numRows = ceilDiv(_size - metadata->getDataStart(),
                       metadata->getLineLength());
    _numRowsValid = true;
  }
  vector<bool> resident(_numBlocks, false);
  // Mark first two blocks resident
  for (size_t i = 0; i < 2 && i < _numBlocks; ++i) {
    resident[i] = true;
  }
  vector<size_t> rowIndex;
  rowIndex.push_back(metadata->getDataStart());
  // Commit state
  swap(_resident, resident);
  swap(_rowIndex, rowIndex);
  swap(_metadata, metadata);
  swap(_data, *data);
  swap(_fd, fd);
  // Set the current block to the first data block
  setCurBlock(firstBlock, MappedMemory::WILLNEED);
}

IPACTableReader::~IPACTableReader() {}

void IPACTableReader::markResident(size_t const fromBlock,
                                   size_t const toBlock) {
  for (size_t i = fromBlock; i < min(toBlock, _numBlocks); ++i) {
    _resident[i] = true;
  }
}

/** \internal
    Sets the current block to \c next and applies \c advice to
    \c next and its successor.
    
    \param[in] block    The block to make current.
    \param[in] advice   The advice to apply to \c block and its successor.
    \param[in] all      If \c true and \c block is ahead of the current block,
                        then  all blocks from the current one up to \c block
                        are freed.
  */
void IPACTableReader::setCurBlock(size_t const block,
                                  MappedMemory::Advice const advice) throw() {
  if (block == _curBlock) {
    return;
  }
  // Free unneeded resident blocks
  if (block > 0) {
    for (size_t i = 0; i < block - 1 && i < _numBlocks; ++i) {
      if (_resident[i]) {
        _data.advise(MappedMemory::DONTNEED, i * _blockSize, _blockSize);
        _resident[i] = false;
      }
    }
  }
  for (size_t i = block + 2; i < _numBlocks; ++i) {
    if (_resident[i]) {
      _data.advise(MappedMemory::DONTNEED, i * _blockSize, _blockSize);
      _resident[i] = false;
    }
  }
  if (block >= _numBlocks) {
    return;
  }
  if (!_resident[block]) {
    _data.advise(advice, block * _blockSize, _blockSize);
    _resident[block] = true;
  }
  if (block + 1 < _numBlocks && !_resident[block + 1]) {
    _data.advise(advice, (block + 1) * _blockSize, _blockSize);
    _resident[block + 1] = true;
  }
  _curBlock = block;
}

/** \internal
    Scans from the last index entry towards the end of the table looking for
    \c untilRow.  If \c untilRow is reached, the current row and row offset
    are updated.  If the end of the table is reached, the table row count is
    set.

    To be called for variable width tables only!
  */
void IPACTableReader::scan(size_t const untilRow) {
  size_t numRows = (_rowIndex.size() - 1) << _granularityLog2;
  size_t block = _rowIndex.back() >> _blockSizeLog2;
  char const * line = static_cast<char const *>(_data.getData());
  char const * const end = line + _size;
  line += _rowIndex.back();
  setCurBlock(block, MappedMemory::SEQUENTIAL);
  try {
    // Scan through the table one line at a time
    while (line < end && numRows < untilRow) {
      line = endOfLine(line, end);
      line = eatNewline(line, end);
      ++numRows;
      size_t nextOffset = static_cast<size_t>(
        line - static_cast<char const *>(_data.getData()));
      if (numRows == (_rowIndex.size() << _granularityLog2)) {
        _rowIndex.push_back(nextOffset);
      }
      size_t nextBlock = nextOffset >> _blockSizeLog2;
      if (nextBlock > block) {
        // Landed in a different block
        markResident(block, nextBlock);
        setCurBlock(nextBlock, MappedMemory::SEQUENTIAL);
      }
    }
  } catch (std::exception &) {
    setCurBlock(_rowOffset >> _blockSizeLog2, MappedMemory::WILLNEED);
    throw;
  }
  if (line == end) {
    // If the scan reached the end of the file, take the opportunity
    // to set the row count.
    _numRows = numRows;
    _numRowsValid = true;
  } else if (numRows == untilRow) {
    _rowId = numRows;
    _rowOffset = static_cast<size_t>(
      line - static_cast<char const *>(_data.getData()));
  }
  setCurBlock(_rowOffset >> _blockSizeLog2, MappedMemory::WILLNEED);
}

/** Returns the number of rows in the table.  When rows are variable length,
    this may involve a scanning the entire table; only as yet unscanned
    blocks are actually read.  Furthermore, the row count is cached, so
    multiple calls to this function result in at most one table scan.

    \return   The number of rows in the table.
  */
size_t IPACTableReader::getNumRows() {
  if (_numRowsValid) {
    // Always reached for fixed width tables
    return _numRows;
  }
  scan(numeric_limits<size_t>::max());
  return _numRows;
}

/** Seeks to the specified row; a subsequent call to getRows() will
    return rows starting at \c rowId.  When rows are variable length,
    this may involve scanning the table until the given row is found.

    \param[in] rowId  The row to seek to.

    \throw ipac::except::OutOfBounds
      If the specified row doesn't exist in the table.
  */
void IPACTableReader::seek(size_t const rowId) {
  if (_metadata->isFixedWidth()) {
    // Fixed width rows, _numRows is always valid
    if (rowId >= _numRows) {
      throw IPAC_STK_EXCEPT(except::OutOfBounds, "rowId must be between "
                            "0 and " + _numRows);
    }
    _rowId = rowId;
    _rowOffset = rowId * _metadata->getLineLength() +
                 _metadata->getDataStart();
    setCurBlock(_rowOffset >> _blockSizeLog2, MappedMemory::WILLNEED);
    return;
  }
  // Variable length rows - if the number of rows is already known
  // and rowId is out of bounds, fail fast.
  if (_numRowsValid && rowId >= _numRows) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds,
                          "rowId must be between 0 and " + (_numRows - 1));
  }
  size_t i = rowId >> _granularityLog2;
  if (i >= _rowIndex.size()) {
    scan(rowId);
    // If the end of the table was reached, rowId is out of bounds
    if (_numRowsValid && rowId >= _numRows) {
      throw IPAC_STK_EXCEPT(except::OutOfBounds,
                            "rowId must be between 0 and " + (_numRows - 1));
    }
  } else {
    _rowOffset = _rowIndex[i];
    _rowId = i << _granularityLog2;
    size_t block = _rowOffset >> _blockSizeLog2;
    setCurBlock(block, MappedMemory::WILLNEED);
    char const * line = static_cast<char const *>(_data.getData());
    char const * const end = line + _size;
    line += _rowOffset;
    while (line < end && _rowId < rowId) {
      line = endOfLine(line, end);
      line = eatNewline(line, end);
      ++_rowId;
    }
    _rowOffset = static_cast<size_t>(
      line - static_cast<char const *>(_data.getData()));
    size_t newBlock = _rowOffset >> _blockSizeLog2;
    if (block != newBlock) {
      markResident(block, newBlock);
      setCurBlock(newBlock, MappedMemory::WILLNEED);
    }
  }
}

/** Returns the next row in the table as a (begin, end) pair, where
    \c begin points to the first character in the row, and \c end points
    to the character following the last character in the row.  Newline
    character(s) are not returned as part of the row.
    
    \throw ipac::except::OutOfBounds
      If there are no more rows in the table.
  */
Record const IPACTableReader::nextRow() {
  if (!hasNext()) {
    throw IPAC_STK_EXCEPT(except::OutOfBounds, "No more rows to return in " +
                          getMetadata().getPath().file_string());
  }
  size_t rowBlock = _rowOffset >> _blockSizeLog2;
  if (rowBlock != _curBlock) {
    setCurBlock(rowBlock, MappedMemory::WILLNEED);
  }
  char const * line = static_cast<char const *>(_data.getData()) + _rowOffset;
  char const * end = static_cast<char const *>(_data.getData()) + _size;
  char const * eol = endOfLine(line, end);
  _rowOffset = static_cast<size_t>(
    eatNewline(eol, end) - static_cast<char const *>(_data.getData()));
  _rowId += 1;
  if (_rowId == (_rowIndex.size() << _granularityLog2)) {
    _rowIndex.push_back(_rowOffset);
  }
  markResident(_curBlock, _rowOffset >> _blockSizeLog2);
  return Record(_rowId - 1, line, eol);
}

/** Returns approximately a blocks worth of rows as (begin, end) pairs, where
    \c begin points to the first character in the row, and \c end points to
    the character following the last character in the row.  Newline
    character(s) are not returned as part of rows. If there are no more rows
    in the table, then \c rows is emptied.
    
    \param[out] rows  The vector to store row (begin, end) pairs in.
  */
void IPACTableReader::nextRows(vector<Record> & rows) {
  rows.clear();
  if (!hasNext()) {
    return;
  }
  size_t startBlock = _rowOffset >> _blockSizeLog2;
  setCurBlock(startBlock, MappedMemory::WILLNEED);
  char const * line = static_cast<char const *>(_data.getData()) + _rowOffset;
  char const * const end = static_cast<char const *>(_data.getData()) + _size;
  while (line < end) {
    char const * eol = endOfLine(line, end);
    rows.push_back(Record(_rowId, line, eol));
    line = eatNewline(eol, end);
    size_t rowOffset = static_cast<size_t>(
      line - static_cast<char const *>(_data.getData()));
    ++_rowId;
    if (_rowId == (_rowIndex.size() << _granularityLog2)) {
      _rowIndex.push_back(rowOffset);
    }
    size_t nextBlock = rowOffset >> _blockSizeLog2;
    if (nextBlock != startBlock) {
      markResident(startBlock, nextBlock);
      break;
    }
  }
  _rowOffset = static_cast<size_t>(
    line - static_cast<char const *>(_data.getData()));
}

}}} // namespace ipac::stk::table

