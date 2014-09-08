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



from contextlib import nested
from itertools import imap
import logging
import os
import os.path
import shutil
import subprocess
import sys
from textwrap import dedent

from ipac.stk.utils import ExAndTimeLogger, Flusher

__all__ = ['Hadoop', 'FileCleaner']


def _check_abs(remote_paths):
    """Raises a RuntimeError if the flattened elements of ``remote_paths``
    do not begin with a '/'.
    """
    if isinstance(remote_paths, (str, unicode)):
        if (not remote_paths.startswith('/') and
            not remote_paths.startswith('hdfs://')):
            raise RuntimeError(str.format(
                'HDFS path {0} is not absolute', remote_paths))
    else:
        for p in remote_paths:
            _check_abs(p)

class Hadoop(object):
    """Utility class for interacting with HDFS and
    submitting STK map-reduce jobs to a cluster.
    """
    def __init__(self, name_node='file:///', job_tracker='local',
                 mr_props=None, hadoop_log_file=None):
        # Locate hadoop executable
        if 'HADOOP_HOME' not in os.environ:
            raise RuntimeError('HADOOP_HOME environment variable is not set.')
        self._hadoop_home = os.environ['HADOOP_HOME']
        self._hadoop = os.path.join(self._hadoop_home, 'bin', 'hadoop')
        if not os.path.isfile(self._hadoop):
            raise RuntimeError(str.format(
                'Failed to find hadoop executable in HADOOP_HOME ({0})',
                self._hadoop_home))
        # Locate map-reduce driver scripts
        stk_bin_dir = os.path.abspath(os.path.join(
            os.path.dirname(__file__), os.path.pardir,
            os.path.pardir, os.path.pardir, 'bin'))
        self._mr_create_chunks = os.path.join(stk_bin_dir, 'mr_create_chunks')
        self._mr_merge_chunks = os.path.join(stk_bin_dir, 'mr_merge_chunks')
        if not all(map(os.path.isfile, (self._mr_create_chunks,
                                        self._mr_merge_chunks))):
            raise RuntimeError(str.format(dedent("""\
                Failed to find mr_create_chunks and/or mr_merge_chunks
                driver script in {0}"""), stk_bin_dir))
        self._name_node = name_node
        self._job_tracker = job_tracker
        self._local = self._name_node == 'file:///'
        if not self._local:
            if not self._name_node.startswith('hdfs://'):
                self._name_node = 'hdfs://' + self._name_node
            if self._name_node.endswith('/'):
                self._name_node = self._name_node[:-1]
        # Extract M-R properties from conf
        self._mr_props = []
        if mr_props != None:
            for kv in mr_props:
                self._mr_props.append('-D')
                self._mr_props.append(kv)
        # Setup logging
        self._log = logging.getLogger('hadoop')
        if hadoop_log_file == None:
            self._hadoop_log = sys.stdout
        else:
            self._hadoop_log = open(hadoop_log_file, 'ab+')

    def is_local(self):
        return self._local

    def name_node(self):
        return self._name_node

    def job_tracker(self):
        return self._job_tracker

    def copy_to_hdfs(self, local_files, remote_dir):
        """Copies a list of local files to a remote HDFS directory.

        The local file list may contain entries from disparate directories.
        However, the file names must be unique.
        """
        assert not self.is_local()
        if len(set(imap(os.path.basename, local_files))) != len(local_files):
            raise RuntimeError(str.format(
                'Input file list {0} contains duplicate file names',
                local_files))
        _check_abs(remote_dir)
        self._log.info(str.format(
            'Copying {0} to {1}{2}', ', '.join(local_files),
            self._name_node, remote_dir))
        with nested(ExAndTimeLogger('Local FS -> HDFS copy', self._log),
                    Flusher(self._hadoop_log)):
            command = [self._hadoop, 'fs', '-fs', self._name_node, '-put']
            command.extend(local_files)
            command.append(remote_dir)
            self._log.debug(str.format('Running command {0}', command))
            subprocess.check_call(command, stdout=self._hadoop_log,
                                  stderr=self._hadoop_log)

    def copy_from_hdfs(self, remote_dir, local_dir):
        """Recursively copies a directory from HDFS to the local file system.
        ``remote_dir`` must be absolute.
        """
        assert not self.is_local()
        _check_abs(remote_dir)
        if not remote_dir.endswith('/'):
            remote_dir = remote_dir + '/'
        self._log.info(str.format(
            'Copying {0}{1} to {2}', self._name_node, remote_dir, local_dir))
        command = [self._hadoop, 'fs', '-fs', self._name_node,
                   '-get', remote_dir, local_dir]
        self._log.debug(str.format('Running command {0}', command))
        with nested(ExAndTimeLogger('HDFS -> local FS copy', self._log),
                    Flusher(self._hadoop_log)):
            subprocess.check_call(command, stdout=self._hadoop_log,
                                  stderr=self._hadoop_log)

    def remove_files(self, remote_paths, recursive=False):
        """Removes files and/or directories from HDFS. All remote
        paths must be absolute. Failure to remove a file that does
        not exist is not considered an error; any other failures
        encountered will result in an exception being raised.
        """
        assert not self.is_local()
        _check_abs(remote_paths)
        self._log.info(str.format('Removing files {0} on {1}',
                                  ', '.join(remote_paths), self._name_node))
        command = [self._hadoop, 'fs', '-fs', self._name_node,
                   '-rmr' if recursive else '-rm']
        command.extend(remote_paths)
        self._log.debug(str.format('Running command {0}', command))
        with nested(ExAndTimeLogger('File removal', self._log),
                    Flusher(self._hadoop_log)):
            retcode = subprocess.call(command, stdout=self._hadoop_log,
                                      stderr=self._hadoop_log)
            if retcode not in (0, 255):
                raise subprocess.CalledProcessError(retcode, command)

    def make_dir(self, remote_dir):
        """Creates an HDFS directory. Necessary parent directories are
        automatically created.
        """
        assert not self.is_local()
        _check_abs(remote_dir)
        command = [self._hadoop, 'fs', '-fs', self._name_node,
                   '-mkdir', remote_dir]
        self._log.info(str.format('Creating directory {0}{1}',
                                  self._name_node, remote_dir))
        self._log.debug(str.format('Running command {0}', command))
        with nested(ExAndTimeLogger('Directory creation', self._log),
                    Flusher(self._hadoop_log)):
            subprocess.check_call(command, stdout=self._hadoop_log,
                                  stderr=self._hadoop_log)

    def run_create_chunks(self, remote_paths, remote_out_dir, options):
        """Runs a chunk index creation job with the specified option list.
        """
        if not self.is_local():
            _check_abs(remote_paths)
            _check_abs(remote_out_dir)
        self._log.info('Running create_chunks map-reduce job')
        command = [self._mr_create_chunks, '-fs', self._name_node,
                   '-jt', self._job_tracker]
        command.extend(self._mr_props)
        command.extend(options)
        command.extend(remote_paths)
        command.append(remote_out_dir)
        self._log.debug(str.format('Running command {0}', command))
        with nested(ExAndTimeLogger('create_chunks', self._log),
                    Flusher(self._hadoop_log)):
            subprocess.check_call(command, stdout=self._hadoop_log,
                                  stderr=self._hadoop_log)

    def run_merge_chunks(self, merge_chunks_file, merge_indexes_file,
                         output_dir):
        """Runs a chunk index merging job.
        """
        if not self.is_local():
            _check_abs(merge_chunks_file)
            _check_abs(merge_indexes_file)
            _check_abs(output_dir)
        self._log.info('Running merge_chunks map-reduce job')
        command = [self._mr_merge_chunks, '-fs', self._name_node, '-jt',
                   self._job_tracker]
        command.extend(self._mr_props)
        command.extend([merge_chunks_file, merge_indexes_file, output_dir])
        self._log.debug(str.format('Running command {0}', command))
        with nested(ExAndTimeLogger('merge_chunks', self._log),
                    Flusher(self._hadoop_log)):
            subprocess.check_call(command, stdout=self._hadoop_log,
                                  stderr=self._hadoop_log)


class FileCleaner(object):
    """Class which maintains a list of local and
    remote directories to clean up.
    """
    def __init__(self, hadoop=None, logger=None, preserve=False):
        self._local_paths = set()
        self._remote_paths = set()
        self._hadoop = hadoop if isinstance(hadoop, Hadoop) else Hadoop()
        self._logger = logger
        self._preserve = preserve

    def add_local(self, paths, on_err=True, on_success=False):
        if isinstance(paths, (str, unicode)):
            self._local_paths.add((paths, (on_err, on_success)))
        else:
            self._local_paths = self._local_paths | set(
                [(p, (on_err, on_success)) for p in paths])

    def add_remote(self, paths, on_err=True, on_success=False, name_node=None):
        if name_node == None:
            name_node = self._hadoop.name_node()
        if isinstance(paths, (str, unicode)):
            self._remote_paths.add((name_node + paths, (on_err, on_success)))
        else:
            self._remote_paths = self._remote_paths | set(
                [(name_node + p, (on_err, on_success)) for p in paths])

    def cleanup(self, after_error, logger=None, swallow=True):
        if self._preserve:
            return
        logger = logger or self._logger
        ex = None
        failed = set()
        for p, when in self._local_paths:
            if not when[0 if after_error else 1]:
                continue
            try:
                if os.path.isfile(p):
                    os.remove(p)
                elif os.path.isdir(p):
                    shutil.rmtree(p)
            except Exception, e:
                failed.add((p, when))
                ex = ex or e
        self.local_paths = failed
        failed = set()
        for p, when in self._remote_paths:
            if not when[0 if after_error else 1]:
                continue
            try:
                self._hadoop.remove_files([p], True)
            except Exception, e:
                failed.add((p, when))
                ex = ex or e
        self._remote_paths = failed
        if ex != None:
            if logger != None:
                try:
                    message = str(self)
                    if len(message) != 0:
                        logger.warn(message)
                except:
                    pass
            if not swallow:
                raise ex

    def clear(self):
        self._local_paths.clear()
        self._remote_paths.clear()

    def __str__(self):
        if len(self._remote_paths) == 0 and len(self._local_paths) == 0:
            return ''
        try:
            message = dedent("""\
                An attempt to clean up intermediate files has been made,
                but may not have competed successfully. Please remove these
                files before trying again. The following files and/or
                directories should be deleted if they still exist:""")
            if len(self._local_paths) != 0:
                message += '\n  '.join(map(lambda t: t[0], self._local_paths))
            if len(self._remote_paths) != 0:
                message += '\n  '.join(map(lambda t: t[0], self._remote_paths))
            return message
        except:
            return ''
