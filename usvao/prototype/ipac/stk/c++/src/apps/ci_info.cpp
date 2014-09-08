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
    \brief Spatial matching tool.
    \author Serge Monkewitz
  */
#include "ipac/stk/config.h"
#if HAVE_STDINT_H
# include <stdint.h>
#elif HAVE_INTTYPES_H
# include <inttypes.h>
#else
# error Standard integer types not available
#endif

#include <cstdlib>
#include <exception>
#include <iostream>
#include <vector>

#include "boost/filesystem.hpp"
#include "boost/program_options.hpp"
#include "boost/shared_ptr.hpp"

#include "ipac/stk/assoc/ChunkIndex.h"
#include "ipac/stk/except.h"
#include "ipac/stk/json.h"

using std::cerr;
using std::cout;
using std::endl;
using std::exit;
using std::string;
using std::vector;

using namespace ipac::stk::assoc;
using ipac::stk::json::FormattingOptions;
using ipac::stk::json::JSONOutput;
using ipac::stk::json::Value;

namespace except = ipac::stk::except;
namespace fs = boost::filesystem;
namespace po = boost::program_options;

namespace {

// -- Error reporting --------

void printError(string const & msg, bool json) {
  JSONOutput o(cout, json ? JSONOutput::PRETTY_ASCII : JSONOutput::IPAC_SVC);
  o.object();
  o.pair("stat", "ERROR");
  o.pair("msg", msg);
  o.close();
  cout << endl;
}

void printError(except::Exception const & ex, bool json) {
  JSONOutput o(cout, json ? JSONOutput::PRETTY_ASCII : JSONOutput::IPAC_SVC);
  o.object();
  o.pair("stat", "ERROR");
  o.pair("msg", ex.getMessage());
  o.key("except");
  o.object();
  o.pair("type", ex.getTypeName());
  except::SystemError const * se =
    dynamic_cast<except::SystemError const *>(&ex);
  if (se != 0) {
    o.pair("errno", se->getErrno());
  }
  o.pair("file", ex.getFile());
  o.pair("line", ex.getLine());
  o.pair("func", ex.getFunction());
  o.close(2);
  cout << endl;
}

// -- Creating program option spec and basic input verification --------

void buildOptions(po::options_description & options) {
  options.add_options()
    ("help,h", po::bool_switch(), "print usage help")
    ("json,j", po::bool_switch(),
     "Print output in JSON format instead of IPAC SVC format")
    ("chunk-index,i", po::value<string>(),
      "Chunk index file to return information for");
}

void verifyOptions(po::variables_map const & vm,
                   po::options_description const & options,
                   char const * const program) {
  if (vm["help"].as<bool>()) {
    cout << program << " [options] <chunk_index>\n\n"
        "    Returns information about a chunk index.\n\n";
    cout << options;
    cout << endl;
    exit(EXIT_SUCCESS);
  }
  if (vm.count("chunk-index") != 1 && !vm["chunk-index"].defaulted()) {
    printError("The --chunk-index option must be specified exactly once.",
               vm["json"].as<bool>());
    exit(EXIT_FAILURE);
  }
}

} // namespace


int main(int const argc, char ** argv) {
  typedef vector<ChunkIndex::Column>::const_iterator ColIter;
  Value info = Value::map();
  Value columns = Value::map();
  bool json = false;
  try {
    // Build and parse program options
    po::options_description options;
    buildOptions(options);
    po::variables_map vm;
    po::store(po::parse_command_line(argc, argv, options), vm);
    po::notify(vm);
    json = vm["json"].as<bool>();
    verifyOptions(vm, options, argv[0]);
    // Read chunk index header, and construct a Value containing
    // the chunk index information to print out.
    ChunkIndex ci(fs::path(vm["chunk-index"].as<string>()));
    info.add("overlapDeg", ci.getOverlapDeg());
    info.add("zipped", ci.isZipped());
    info.add("binary", ci.isBinary());
    vector<ChunkIndex::Column> ciCols = ci.getColumns();
    for (ColIter i = ciCols.begin(), e = ciCols.end(); i != e; ++i) {
      Value col = Value::map();
      col.add("name", i->getName());
      col.add("type", i->getType());
      col.add("units", i->getUnits());
      col.add("index", i->getIndex());
      col.add("width", i->getWidth());
      columns.add(i->getName(), col);
    }
    info.add("columns", columns);
    JSONOutput o(cout, json ? JSONOutput::PRETTY_ASCII : JSONOutput::IPAC_SVC);
    o.value(info);
    cout << endl;
    return EXIT_SUCCESS;
  } catch (except::Exception const & e) {
    printError(e, json);
  } catch (std::exception const & e) {
    printError("Caught " + string(typeid(e).name()) + " : " +
               string(e.what()), json);
  }
  return EXIT_FAILURE;
}
