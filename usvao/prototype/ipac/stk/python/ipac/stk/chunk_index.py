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
import csv
import glob
from itertools import groupby, imap
import logging
import os
import os.path
import re
import sqlite3 as sql
import sys
from textwrap import dedent

from ipac.stk.hadoop_utils import FileCleaner
from ipac.stk.utils import ExAndTimeLogger, Flusher

__all__ = ['ChunkIndexer']

def _is_empty_dir(directory):
    """Tests to see if ``directory`` is empty.
    """
    walker = os.walk(directory)
    root, dirs, files = next(walker)
    assert root == directory
    return len(dirs) == 0 and len(files) == 0

def _is_incremental_dir(directory):
    """Tests to see if ``directory`` is empty or looks like an incremental
    chunk index directory.
    """
    walker = os.walk(directory)
    root, dirs, files = next(walker)
    assert root == directory
    if len(dirs) == 0 and len(files) == 0:
        return True
    if len(files) != 1 or files[0] != 'dataset.db':
        return False
    ok = ['loads', 'merges']
    if len(dirs) == 3:
        ok.append('current')
    elif len(dirs) != 2:
        return False
    return all(map(lambda d: d in ok, dirs))

def _chunk_name(sc):
    return str.format('chunk_{0[0]:03d}_{0[1]:03d}.cf', sc)

def _stripe_dir(sc):
    return str.format('stripe_{0[0]:03d}', sc)

def _chunk_path(directory, sc):
    stripe_dir = _stripe_dir(sc)
    chunk_name = _chunk_name(sc)
    return os.path.join(directory, stripe_dir, chunk_name)

def _link(from_path, to_path):
    if not os.path.exists(to_path):
        raise RuntimeError(str.format(
            'Cannot create link {0}: target {1} does not exist.',
            from_path, to_path))
    if os.path.exists(from_path):
        raise RuntimeError(str.format(
            'Cannot create link to {1}: link {0} already exists.',
            from_path, to_path))
    os.link(to_path, from_path)

def _link_chunks(to_dir, from_dir, chunks):
    stripe = None
    for sc in chunks:
        if stripe != sc[0]:
            stripe = sc[0]
            d = os.path.join(from_dir, _stripe_dir(sc))
            if not os.path.exists(d):
                os.makedirs(d)
        _link(_chunk_path(from_dir, sc), _chunk_path(to_dir, sc))

def _link_new_chunks(from_dir, to_link):
    s = None
    for stripe, chunk, to_path in to_link:
        sc = (stripe, chunk)
        if s != stripe:
            s = stripe
            d = os.path.join(from_dir, _stripe_dir(sc))
            if not os.path.exists(d):
                os.makedirs(d)
        from_path = _chunk_path(from_dir, sc)
        _link(from_path, to_path)

def _mark_byproducts(dirs, cleaner):
    if isinstance(dirs, (str, unicode)):
        cleaner.add_local(
            list(glob.glob(os.path.join(dirs, 'part-*'))),
            on_err=False, on_success=True)
        cleaner.add_local(
            list(glob.glob(os.path.join(dirs, 'column-widths-*'))),
            on_err=False, on_success=True)
    else:
        for d in dirs:
            _mark_byproducts(d, cleaner)

def _fix_permissions(directory):
    """Changes permissions of all files in ``directory`` to 0644,
    and of all sub-directories to 0755.
    """
    os.chmod(directory, 0755)
    for root, dirs, files in os.walk(directory):
        for d in dirs:
            os.chmod(os.path.join(root, d), 0755)
        for f in files:
            os.chmod(os.path.join(root, f), 0644)


class ChunkIndexer(object):
    """Class for generating and merging chunk indexes using the
    hadoop map-reduce framework.
    """
    def __init__(self, project, dataset, output_dir, incremental):
        self._project = project
        self._dataset = dataset
        self._incremental = incremental
        self._directory = os.path.abspath(
            os.path.join(output_dir, project, dataset))
        self._log = logging.getLogger()
        if not os.path.exists(self._directory):
            if incremental:
                os.makedirs(self._directory, 0755)
        elif os.path.isfile(self._directory):
            raise RuntimeError(str.format(
                '{0} exists but is a file, not a directory', self._directory))
        elif not incremental:
            raise RuntimeError(str.format(dedent("""\
                Cannot create new chunk index file in {0}:
                directory already exists"""), self._directory))
        if incremental:
            if not _is_incremental_dir(self._directory):
                raise RuntimeError(str.format(dedent("""\
                    Directory {0} is non-empty or does not have the expected
                    sub-structure for an incremental chunk index"""),
                    self._directory))
            loads = os.path.join(self._directory, 'loads')
            merges = os.path.join(self._directory, 'merges')
            db = os.path.join(self._directory, 'dataset.db')
            cleaner = FileCleaner()
            try:
                if not os.path.exists(loads):
                    os.mkdir(loads)
                cleaner.add_local(loads)
                if not os.path.exists(merges):
                    os.mkdir(merges)
                cleaner.add_local(merges)
                cleaner.add_local(db)
                self._con = sql.connect(db)
                # Create metadata tables if they do not exist
                self._con.execute("""
                    CREATE TABLE IF NOT EXISTS Loads (
                      latest INTEGER PRIMARY KEY AUTOINCREMENT,
                      load_id TEXT NOT NULL,
                      load_version TEXT NOT NULL
                    );
                    """)
                self._con.execute("""
                    CREATE INDEX IF NOT EXISTS idx_iv_Loads
                      ON Loads(load_id, load_version);
                    """)
                self._con.execute("""
                    CREATE VIEW IF NOT EXISTS LatestLoads AS
                        SELECT a.load_id, a.load_version
                        FROM Loads AS a LEFT OUTER JOIN Loads AS b
                          ON a.load_id = b.load_id AND b.latest > a.latest
                        WHERE b.load_id IS NULL;
                    """)
                self._con.execute("""
                    CREATE TABLE IF NOT EXISTS Merges (
                      merge_id INTEGER PRIMARY KEY AUTOINCREMENT
                    );
                    """)
                self._con.execute("""
                    CREATE TABLE IF NOT EXISTS Chunks (
                      load_id TEXT NOT NULL,
                      load_version TEXT NOT NULL,
                      stripe INTEGER NOT NULL,
                      chunk INTEGER NOT NULL
                    );
                    """)
                self._con.execute("""
                    CREATE INDEX IF NOT EXISTS idx_sc_Chunks
                      ON Chunks(stripe, chunk);
                    """)
            except Exception, e:
                cleaner.cleanup(True, self._log, True)
                raise e

    def _check_incremental(self, incremental):
        if incremental != self._incremental:
            kind = 'incremental' if self._incremental else 'non-incremental'
            raise RuntimeError(str.format(
                'Unsupported operation on {0} chunk-index stored in {1}',
                kind, self._directory))

    def _contains_load(self, cursor, load_id, load_version):
        """Returns True if this incremental data-set already contains
        a load with the specified id and version.
        """
        self._check_incremental(True)
        if load_version == None:
            cursor.execute(
                'SELECT COUNT(*) FROM Loads WHERE load_id=?', (load_id,))
        else:
            cursor.execute(
                """SELECT COUNT(*) FROM Loads
                   WHERE load_id=? AND load_version=?""",
                (load_id, load_version))
        n = cursor.fetchone()[0]
        return n > 0

    def _is_latest(self, cursor, load_id, load_version):
        """Returns True if the given version of a load is the latest
        version of that load.
        """
        self._check_incremental(True)
        cursor.execute(
            """SELECT COUNT(*) FROM LatestLoads
               WHERE load_id=? AND load_version=?""",
            (load_id, load_version))
        n = cursor.fetchone()[0]
        return n == 1

    def _create(self, hadoop, cleaner, files, options,
                in_dir, out_dir, dest_dir, clean_out_dir):
        cleaner.add_local(dest_dir)
        with ExAndTimeLogger('Chunk index creation', self._log):
            try:
                if hadoop.is_local():
                    hadoop.run_create_chunks(map(os.path.abspath, files),
                                             dest_dir, options)
                else:
                    # Copy input files to HDFS
                    hadoop.remove_files([in_dir], True)
                    hadoop.make_dir(in_dir)
                    cleaner.add_remote(in_dir, on_success=True)
                    hadoop.copy_to_hdfs(files, in_dir)
                    # Run chunk creation job
                    hadoop.run_create_chunks([in_dir], out_dir, options)
                    cleaner.add_remote(out_dir)
                    if clean_out_dir:
                        cleaner.add_remote(out_dir, on_success=True)
                    # Copy output files to local fs
                    hadoop.copy_from_hdfs(out_dir, dest_dir)
                _fix_permissions(dest_dir)
            except Exception, e:
                cleaner.cleanup(True, self._log, True)
                raise e

    def create_chunks(self, hadoop, files, options):
        """Creates a chunk index for this dataset.
        """
        self._check_incremental(False)
        out_dir = '/'.join(['/chunk_index', self._project, self._dataset])
        in_dir = '/'.join(['/chunk_index', self._project, 'raw',
                           self._dataset])
        cleaner = FileCleaner(hadoop)
        self._create(hadoop, cleaner, files, options, in_dir, out_dir,
                     self._directory, not hadoop.is_local())
        # Clean up unwanted byproducts
        _mark_byproducts(self._directory, cleaner)
        cleaner.cleanup(False, self._log, True)

    def _write_index_mf(self, hadoop, cursor, index_merge_file):
        self._log.info('Writing out chunk index merge file')
        with open(index_merge_file, 'wb') as imf:
            latest = cursor.execute(
                'SELECT load_id, load_version FROM LatestLoads')
            for i, v in latest:
                if hadoop.is_local():
                    imf.write(os.path.join(self._directory, 'loads',
                                           i, v, 'index.ci'))
                else:
                    imf.write('/'.join([hadoop.name_node(), 'chunk_index',
                                        self._project, self._dataset, 'loads',
                                        i, v, 'index.ci']))
                imf.write('\n')

    def _write_chunk_mf(self, hadoop, load_id, cursor, chunk_merge_file):
        def _local_path(t):
            return os.path.join(self._directory, 'loads', t[2], t[3],
                                _stripe_dir(t), _chunk_name(t))
        def _hdfs_path(t):
            return '/'.join([
                hadoop.name_node(), 'chunk_index', self._project,
                self._dataset, 'loads', t[2], t[3], _stripe_dir(t),
                _chunk_name(t)])
        self._log.info('Writing out chunk merge file')
        cursor.execute("""
            SELECT c.stripe, c.chunk, c.load_id, c.load_version
            FROM Chunks AS c
            INNER JOIN
              (SELECT DISTINCT stripe, chunk FROM Chunks WHERE load_id = ?)
              AS lc ON c.stripe = lc.stripe AND c.chunk = lc.chunk
            INNER JOIN LatestLoads AS latest
              ON c.load_id = latest.load_id AND
                 c.load_version = latest.load_version
            ORDER BY c.stripe, c.chunk;
            """, (load_id,))
        chunks = cursor.fetchall()
        link_to = []
        with open(chunk_merge_file, 'wb') as cmf:
            writer = csv.writer(cmf, delimiter=',')
            keyfun = lambda t: (t[0], t[1])
            pathfun = _local_path if hadoop.is_local() else _hdfs_path
            for k, g in groupby(chunks, keyfun):
                tuples = set(g)
                if len(tuples) == 1:
                    t = tuples.pop()
                    link_to.append((k[0], k[1], _local_path(t)))
                else:
                    writer.writerow(map(pathfun, tuples))
        return link_to

    def _merge(self, hadoop, cleaner, cursor,
               load_id, load_version, local_load_dir):
        # Retrieve the next merge_id
        cursor.execute('INSERT INTO Merges DEFAULT VALUES')
        cursor.fetchone()
        cursor.execute('SELECT MAX(merge_id) FROM Merges')
        merge_id = cursor.fetchone()[0]
        merge_dir = '/'.join(['/chunk_index', self._project,
                              self._dataset, 'merge'])
        local_merge_dir = os.path.join(self._directory, 'merges',
                                       str(merge_id))
        if os.path.exists(local_merge_dir):
            raise RuntimeError(str.format(dedent("""\
                Cannot overwrite an existing merge: merge directory
                {0} already exists"""), local_merge_dir))
        cleaner.add_local(local_merge_dir)
        cursor.execute('SELECT COUNT(*) FROM LatestLoads')
        num_loads = cursor.fetchone()[0]
        if num_loads == 0:
            raise RuntimeError(str.format(dedent("""\
                Removing load {0} version {1} would result in an
                empty chunk index - if this is the desired end state,
                simply delete {2}"""), load_id, load_version,
                self._directory))
        elif num_loads == 1:
            # Nothing to merge with - link directly to load chunks
            os.makedirs(local_merge_dir)
            cursor.execute('SELECT load_id, load_version FROM LatestLoads')
            lv = cursor.fetchone();
            local_load_dir = os.path.join(self._directory, 'loads',
                                          lv[0], lv[1])
            with ExAndTimeLogger('Chunk file linking', self._log):
                cursor.execute(
                    """SELECT stripe, chunk
                       FROM Chunks
                       WHERE load_id=? AND load_version=?
                       ORDER BY stripe""",
                    (lv[0], lv[1]))
                _link_chunks(local_load_dir, local_merge_dir,
                             cursor.fetchall())
                os.link(os.path.join(local_load_dir, 'index.ci'),
                        os.path.join(local_merge_dir, 'merge.ci'))
        else:
            # Write out indexes to merge
            index_merge_file = os.path.join(self._directory, 'index.mrg')
            self._write_index_mf(hadoop, cursor, index_merge_file)
            # Write out chunks to merge
            chunk_merge_file = os.path.join(self._directory, 'chunk.mrg')
            to_link = self._write_chunk_mf(hadoop, load_id, cursor,
                                           chunk_merge_file)
            # Merge chunks
            if hadoop.is_local():
                hadoop.run_merge_chunks(chunk_merge_file,
                                        index_merge_file,
                                        local_merge_dir)
            else:
                # Copy merge control files to HDFS
                mf_dir = '/'.join(['/chunk_index',
                                   self._project,
                                   self._dataset])
                lvs = load_version if load_version != None else ''
                load_dir = '/'.join([mf_dir, 'loads', load_id, lvs])
                hadoop.remove_files([merge_dir,
                                     mf_dir + '/chunk.mrg',
                                     mf_dir + '/index.mrg'], True)
                hadoop.copy_to_hdfs([chunk_merge_file,
                                     index_merge_file], mf_dir)
                hadoop.run_merge_chunks(mf_dir + '/chunk.mrg',
                                        mf_dir + '/index.mrg',
                                        merge_dir)
                cleaner.add_remote([merge_dir,
                                    mf_dir + '/chunk.mrg',
                                    mf_dir + '/index.mrg'], on_success=True)
                # Copy results to local FS
                hadoop.copy_from_hdfs(merge_dir, local_merge_dir)
            # Move merge control files to merge directory
            os.rename(chunk_merge_file, os.path.join(local_merge_dir, 'chunk.mrg'))
            os.rename(index_merge_file, os.path.join(local_merge_dir, 'index.mrg'))
            with ExAndTimeLogger('Chunk file linking', self._log):
                self._log.info('Linking un-merged chunks')
                _link_new_chunks(local_merge_dir, to_link)
                self._log.info('Finding chunks unaffected by add/remove')
                cursor.execute(
                    """SELECT MAX(merge_id) FROM Merges
                       WHERE merge_id < ?""", (merge_id,))
                last_merge = cursor.fetchone()[0]
                link_to_dir = os.path.join(self._directory, 'merges',
                                           str(last_merge))
                cursor.execute("""
                    SELECT DISTINCT c.stripe, c.chunk
                    FROM Chunks AS c
                    INNER JOIN LatestLoads AS cur
                        ON c.load_id = cur.load_id AND
                           c.load_version = cur.load_version     
                    LEFT OUTER JOIN
                        (SELECT stripe, chunk from Chunks WHERE load_id=?)
                        AS c2 ON (c.stripe = c2.stripe AND c.chunk = c2.chunk)
                    WHERE c2.stripe IS NULL
                    ORDER BY c.stripe
                    """, (load_id,))
                self._log.info('Linking unaffected chunks')
                _link_chunks(link_to_dir, local_merge_dir, cursor.fetchall())
        _fix_permissions(local_merge_dir)
        return local_merge_dir

    def add_chunks(self, hadoop, load_id, load_version, files, options):
        """Creates a chunk index for a load and merges it with
        previously existing data.
        """
        self._check_incremental(True)
        assert load_id != None and isinstance(load_id, (str, unicode))
        assert (load_version != None and
                isinstance(load_version, (str, unicode)))
        if self._contains_load(self._con.cursor(), load_id, load_version):
            raise RuntimeError(str.format(dedent("""\
                Cannot overwrite an existing load: load
                (load_id, load_version) = ({0}, {1}) already exists"""),
                load_id, load_version))

        in_dir = '/'.join(['/chunk_index', self._project, 'raw',
                           self._dataset])
        load_dir = '/'.join(['/chunk_index', self._project, self._dataset,
                             'loads', load_id, load_version])
        local_load_dir = os.path.join(self._directory, 'loads',
                                      load_id, load_version)
        cleaner = FileCleaner(hadoop)
        with nested(ExAndTimeLogger('Incremental chunk index load', self._log),
                    self._con):
            cur = self._con.cursor();
            # Record the load
            cur.execute(
                'INSERT INTO Loads(load_id, load_version) VALUES(?,?)',
                (load_id, load_version))
            cur.fetchone()
            try:
                if os.path.exists(local_load_dir):
                    raise RuntimeError(str.format(dedent("""\
                        Cannot overwrite an existing load: load directory
                        {0} already exists"""), local_load_dir))
                # Create chunk index for the load
                self._create(hadoop, cleaner, files, options,
                             in_dir, load_dir, local_load_dir, False)
                # Load chunks into metadata database
                self._log.info('Loading chunk metadata into database')
                to_load = set()
                pat = re.compile(r'^chunk_([0-9]+)_([0-9]+).cf$')
                num_chunks = 0
                for root, dirs, files in os.walk(local_load_dir):
                    for f in files:
                        m = pat.match(f)
                        if m != None:
                            stripe = int(m.group(1))
                            chunk = int(m.group(2))
                            to_load.add((load_id, load_version, stripe, chunk))
                if len(to_load) == 0:
                    raise RuntimeError('No chunk files generated!')
                for t in to_load:
                    cur.execute('INSERT INTO Chunks VALUES (?, ?, ?, ?)', t)
                    cur.fetchone()
                local_merge_dir = self._merge(
                    hadoop, cleaner, cur,
                    load_id, load_version, local_load_dir)
            except:
                cleaner.cleanup(True, self._log, True)
                raise
        # Update/create current symlink to point to latest merge
        current = os.path.join(self._directory, 'current')
        if os.path.exists(current):
            os.unlink(current)
        os.symlink(os.path.relpath(local_merge_dir, self._directory), current)
        # Avoid filling up HDFS and to remove unwanted byproducts
        _mark_byproducts((local_load_dir, local_merge_dir), cleaner)
        cleaner.cleanup(False, self._log, True)

    def remove_load(self, hadoop, load_id, load_version=None):
        """Removes either a version of a load or an entire
        load from an incrementally generated chunk index.
        """
        self._check_incremental(True)
        if not self._contains_load(self._con.cursor(), load_id, load_version):
            if load_version == None:
                print >>sys.stderr, str.format(
                    'Load {0} does not exist: nothing to remove', load_id)
            else:
                print >>sys.stderr, str.format(
                    'Load version {0} {1} does not exist: nothing to remove',
                    load_id, load_version)
            sys.exit(1)
        load_dir = '/'.join(['/chunk_index', self._project, self._dataset,
                             'loads'])
        cleaner = FileCleaner(hadoop)
        local_merge_dir = None
        with nested(ExAndTimeLogger('Incremental load removal', self._log),
                    self._con):
            cur = self._con.cursor();
            do_merge = True
            to_remove = [(load_id, load_version)]
            if load_version != None:
                if not self._is_latest(cur, load_id, load_version):
                    do_merge = False
                cur.execute(
                    'DELETE FROM Loads WHERE load_id=? AND load_version=?',
                    (load_id, load_version))
                cur.fetchone();
            else:
                cur.execute(
                    """SELECT load_id, load_version FROM Loads
                       WHERE load_id=?""", (load_id,))
                to_remove = cur.fetchall()
                cur.execute('DELETE FROM Loads WHERE load_id=?', (load_id,))
                cur.fetchone();
            for i, v in to_remove:
                cleaner.add_local(
                    os.path.join(self._directory, 'loads', i, v),
                    on_err=False, on_success=True)
                if not hadoop.is_local():
                    cleaner.add_remote('/'.join([load_dir, i, v]),
                                       on_err=False, on_success=True)
            try:
                if do_merge:
                    local_load_dir = os.path.join(
                        self._directory, 'loads', load_id,
                        load_version if load_version != None else '')
                    local_merge_dir = self._merge(
                        hadoop, cleaner, cur,
                        load_id, load_version, local_load_dir)
                    _mark_byproducts(local_merge_dir, cleaner)
                if load_version != None:
                    cur.execute("""
                        DELETE FROM Chunks
                        WHERE load_id=? AND load_version=?
                        """, (load_id, load_version))
                else:
                    cur.execute('DELETE FROM Chunks WHERE load_id=?',
                                (load_id,))
                cur.fetchone();
            except:
                cleaner.cleanup(True, self._log, True)
                raise
        cleaner.cleanup(False, self._log, False)
        if local_merge_dir != None:
            current = os.path.join(self._directory, 'current')
            if os.path.exists(current):
                os.unlink(current)
            os.symlink(os.path.relpath(local_merge_dir, self._directory),
                       current)

