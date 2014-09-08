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



import time
import traceback as tb

__all__ = ['flatten', 'ExAndTimeLogger', 'Flusher']


def flatten(l, ltypes=(list, tuple)):
    ltype = type(l)
    l = list(l)
    i = 0
    while i < len(l):
        while isinstance(l[i], ltypes):
            if not l[i]:
                l.pop(i)
                i -= 1
                break
            else:
                l[i:i + 1] = l[i]
        i += 1
    return ltype(l)


class ExAndTimeLogger(object):
    """Context manager which logs exceptions and runtime of a block of code.
    """
    def __init__(self, message, logger):
        self.start_time = time.time()
        self.message = message
        self.logger = logger

    def __enter__(self):
        pass

    def __exit__(self, exc_type, exc_value, traceback):
        if self.logger == None:
            return False
        run_time = time.time() - self.start_time
        if exc_type == None:
            self.logger.info(str.format(
                '{0} finished in {1:.3f} sec', self.message, run_time))
        else:
            e = ''.join(tb.format_exception(exc_type, exc_value,
                                            traceback));
            self.logger.error(str.format(
                '{0} failed in {1:.3f} sec: {2}', self.message, run_time, e))
        return False


class Flusher(object):
    """Context manager that flushes a list of streams on exit.
    """
    def __init__(self, streams, swallow=False):
        self.swallow = swallow
        if isinstance(streams, (list, tuple)):
            self.streams = streams
        else:
            self.streams = [streams]

    def __enter__(self):
        pass

    def __exit__(self, exc_type, exc_value, traceback):
        try:
            for s in self.streams:
                s.flush()
        except:
            if not swallow:
                raise
        return False

