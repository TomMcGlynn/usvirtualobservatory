#*************************************************************************
#
#  Copyright (c) 2014, California Institute of Technology, Pasadena,
#  California, under cooperative agreement 0834235 between the California
#  Institute of Technology and the National Science  Foundation/National
#  Aeronautics and Space Administration.
#
#  All rights reserved.
#
#  Redistribution and use in source and binary forms, with or without
#  modification, are permitted provided that the following conditions
#  of this BSD 3-clause license are met:
#
#  1. Redistributions of source code must retain the above copyright
#  notice, this list of conditions and the following disclaimer.
#
#  2. Redistributions in binary form must reproduce the above copyright
#  notice, this list of conditions and the following disclaimer in the
#  documentation and/or other materials provided with the distribution.
#
#  3. Neither the name of the copyright holder nor the names of its
#  contributors may be used to endorse or promote products derived from
#  this software without specific prior written permission.
#
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
.
#  This software was developed by the Infrared Processing and Analysis
#  Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
#  funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
#  501(c)(3) organization registered in the District of Columbia and a
#  collaborative effort of the Association of Universities for Research
#  in Astronomy (AURA) and the Associated Universities, Inc. (AUI).
#
#************************************************************************/



#! /usr/bin/env python
#
# Script for comparing two chunk indexes.
import glob
import optparse
import os.path
import subprocess
import sys

def get_chunks(index):
    index_dir = os.path.dirname(index)
    pattern = os.path.join(index_dir, 'stripe_???', 'chunk_???_???.cf')
    relativize = lambda p: os.path.relpath(p, index_dir)
    return set(map(relativize, glob.glob(pattern)))

def chunk_index_diff(index1, index2):
    with open('/dev/null', 'wb+') as dev_null:
        retcode = subprocess.call(['diff', '-b', index1, index2],
                                  stdout=dev_null.fileno(),
                                  stderr=dev_null.fileno(), close_fds=True)
    if retcode != 0:
        return str.format('Chunk index files {0} and {1} differ',
                          index1, index2)
    chunks = get_chunks(index1)
    if chunks != get_chunks(index2):
        return str.format('Chunk indexes {0} and {1} have different coverage',
                          index1, index2)
    index1_dir = os.path.dirname(index1)
    index2_dir = os.path.dirname(index2)
    for chunk in chunks:
        chunk1 = os.path.join(index1_dir, chunk)
        chunk2 = os.path.join(index2_dir, chunk)
        with open('/dev/null', 'wb+') as dev_null:
            retcode = subprocess.call(['diff', '-b', chunk1, chunk2],
                                      stdout=dev_null.fileno(),
                                      stderr=dev_null.fileno(), close_fds=True)
        if retcode != 0:
            return str.format('Chunk files {0} and {1} differ', chunk1, chunk2)
    return None

def main():
    usage = "usage:  %prog <index file 1> <index file 2>"
    parser = optparse.OptionParser(usage)
    (opts, inputs) = parser.parse_args()
    if len(inputs) != 2:
        parser.error("Not enough arguments")
    msg = chunk_index_diff(inputs[0], inputs[1])
    if msg != None:
        print >>sys.stderr, msg
        return 2
    return 0

if __name__ == "__main__":
    main()    