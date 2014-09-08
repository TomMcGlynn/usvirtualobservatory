#! /usr/bin/env python
#
# Unit tests for chunk index generation, both incremental and one-shot

import os.path
import sys
# Enable importing from the STK bin directory
stk_bin_dir = os.path.abspath(os.path.join(os.path.dirname(__file__),
                                           os.path.pardir, 'bin'))
sys.path.append(stk_bin_dir)
from chunk_index_diff import chunk_index_diff
from ipac.stk.hadoop_utils import Hadoop, FileCleaner
from itertools import chain, product, repeat
import optparse
import os
import pdb
import random
import shutil
import socket
import subprocess
from textwrap import dedent
import time
import unittest


def remove_dirs(*args):
    for d in args:
        if os.path.exists(d):
            shutil.rmtree(d)


class ChunkIndexTestCase(unittest.TestCase):
    """Test case for chunk index generation.
    """
    @staticmethod
    def getOutRootAndProject():
        host_pid = socket.gethostname() + '-' + str(os.getpid())
        project = 'stk-test-' + host_pid
        out_root = os.path.abspath(os.path.join(os.path.dirname(__file__),
                                                'out', 'chunk_index',
                                                host_pid))
        return out_root, project
        
    def setUp(self):
        self.namenode = os.environ['STK_TEST_HADOOP_NAME_NODE']
        self.jobtracker = os.environ['STK_TEST_HADOOP_JOB_TRACKER']
        self.local = os.environ['STK_TEST_LOCAL'] == 'True'
        self.preserve = os.environ['STK_TEST_PRESERVE'] == 'True'
        self.out_root, self.project = ChunkIndexTestCase.getOutRootAndProject()
        self.in_dir = os.path.abspath(os.path.join(
            os.path.dirname(__file__), 'data'))
        prefix_dir = lambda p: os.path.join(self.in_dir, p)
        self.in_tables_v1 = map(prefix_dir, ['iraspsc_330_30.tbl',
                                             'iraspsc_0_60.tbl',
                                             'iraspsc_30_90.tbl',
                                             'iraspsc_60_120.tbl',
                                             'iraspsc_90_150.tbl',
                                             'iraspsc_120_180.tbl',
                                             'iraspsc_150_210.tbl',
                                             'iraspsc_180_240.tbl',
                                             'iraspsc_210_270.tbl',
                                             'iraspsc_240_300.tbl',
                                             'iraspsc_270_330.tbl',
                                             'iraspsc_300_360.tbl',
                                            ])
        self.in_tables_v2 = map(prefix_dir, ['iraspsc_330_30.tbl.2',
                                             'iraspsc_0_60.tbl.2',
                                             'iraspsc_30_90.tbl.2',
                                             'iraspsc_60_120.tbl.2',
                                             'iraspsc_90_150.tbl.2',
                                             'iraspsc_120_180.tbl.2',
                                             'iraspsc_150_210.tbl.2',
                                             'iraspsc_180_240.tbl.2',
                                             'iraspsc_210_270.tbl.2',
                                             'iraspsc_240_300.tbl.2',
                                             'iraspsc_270_330.tbl.2',
                                             'iraspsc_300_360.tbl.2',
                                            ])
        self.chunk_index = os.path.join(stk_bin_dir, 'chunk_index.py')
        self.iras_columns = 'pscname,semimajor,semiminor,posang,nhcon,' +\
                            'fnu_12,fnu_25,fnu_60,fnu_100,fqual_12,' +\
                            'fqual_25,fqual_60,fqual_100,cc_12,cc_25,' +\
                            'cc_60,cc_100,var,disc,confuse,glon,glat,' +\
                            'elon,elat,ra,dec,cntr,x,y,z,spt_ind'
        self.iras_types = 'char,int,int,int,int,double,double,double,' +\
                          'double,int,int,int,int,char,char,char,char,' +\
                          'int,char,char,double,double,double,double,' +\
                          'double,double,int,double,double,double,int'
        self.iras_units = ',arcsec,arcsec,deg,,Jy,Jy,Jy,Jy,,,,,,,,,,,,' +\
                          'degrees,degrees,degrees,degrees,degrees,'+\
                          'degrees,,,,,'
        # Make sure output directory exists
        if not os.path.exists(self.out_root):
            os.makedirs(self.out_root)
        # Decompress input tables if necessary
        for p in chain(self.in_tables_v1, self.in_tables_v2):
            if not os.path.exists(p):
                pc = p + '.bz2'
                if not os.path.exists(pc):
                    raise RuntimeError(str.format(
                        'Test data file {0} does not exist and {1} is not ' +
                        'available for decompression', p, pc))
                print '  Decompressing ' + pc
                sys.stdout.flush()
                subprocess.check_call(['bzip2', '-d', '-k', pc])

    def get_dataset_dir(self, ds):
        return os.path.join(self.out_root, self.project, ds)

    def get_dataset(self, tag, kind, binary, zipped):
        ds = str.format('{0}_{1}{2}{3}', tag, kind, '_bin' if binary else '',
                        '_zip' if zipped else '')
        return ds, self.get_dataset_dir(ds)

    def _args(self, dataset, binary, zipped, use_cluster):
        """Returns an array of common arguments for chunk_index.py.
        """
        args = [self.chunk_index]
        if use_cluster:
            args.extend(['-n', self.namenode, '-j', self.jobtracker])
        args.extend(['-l', os.path.join(self.out_root, 'test.log'),
                     '-L', os.path.join(self.out_root, 'test.hadoop.log')])
        args.append('-i')
        if binary:
            args.append('-b')
        if zipped:
            args.append('-z')
        return args

    def _create(self, input_tables, dataset,
                kind, binary, zipped, use_cluster):
        args = self._args(dataset, binary, zipped, use_cluster)
        args.append('--kind=' + kind)
        args.extend(['-t', self.iras_types, '-u', self.iras_units, 'create',
                     self.project, dataset, self.out_root, self.iras_columns])
        if isinstance(input_tables, (list, tuple)):
            args.extend(input_tables)
        else:
            args.append(input_tables)
        with open('/dev/null', 'wb+') as dev_null:
            subprocess.check_call(args, stdout=dev_null.fileno(),
                                  stderr=dev_null.fileno())

    def _add(self, input_tables, dataset, load_id, load_version,
             kind, binary, zipped, use_cluster):
        args = self._args(dataset, binary, zipped, use_cluster)
        args.append('--kind=' + kind)
        args.extend(['-t', self.iras_types, '-u', self.iras_units, 'add',
                     self.project, dataset, self.out_root, str(load_id),
                     str(load_version), self.iras_columns])
        if isinstance(input_tables, (list, tuple)):
            args.extend(input_tables)
        else:
            args.append(input_tables)
        with open('/dev/null', 'wb+') as dev_null:
            subprocess.check_call(args, stdout=dev_null.fileno(), 
                                  stderr=dev_null.fileno())

    def _remove(self, dataset, load_id, load_version, use_cluster):
        args = [self.chunk_index]
        if use_cluster:
            args.extend(['-n', self.namenode, '-j', self.jobtracker])
        args.extend(['-l', os.path.join(self.out_root, 'test.log'),
                     '-L', os.path.join(self.out_root, 'test.hadoop.log')])
        args.extend(['remove', self.project, dataset,
                     self.out_root, str(load_id)])
        if load_version != None:
            args.append(str(load_version))
        with open('/dev/null', 'wb+') as dev_null:
            subprocess.check_call(args, stdout=dev_null.fileno(), 
                                  stderr=dev_null.fileno())

    def _run_actions(self, kinds, ref_tables, actions, tag,
                     should_fail=False, diff=True):
        ft = (False, True)
        for kind, binary, zipped in product(kinds, ft, ft):
            dirs = self.get_dataset(tag + '[_ref|_r]', kind, binary, zipped)[1]
            ref, ref_dir = self.get_dataset(tag + '_ref', kind, binary, zipped)
            ds, ds_dir = self.get_dataset(tag, kind, binary, zipped)
            ds_r, ds_dir_r = self.get_dataset(tag + '_r', kind, binary, zipped)
            where = str.format('({0}, {1}, {2}); see {3}, {4} ' +
                               'for log files and index/chunk files',
                               kind, 'binary' if binary else 'text',
                               'zipped' if zipped else 'unzipped',
                               os.path.join(self.out_root, 'test*.log'), dirs)
            remove_dirs(ref_dir, ds_dir, ds_dir_r)
            cleaner = FileCleaner(preserve=self.preserve)
            try:
                cleaner.add_local(ds_dir, on_err=should_fail,
                                  on_success=not should_fail)
                if not self.local:
                    cleaner.add_local(ds_dir_r, on_err=should_fail,
                                      on_success=not should_fail)
                if ref_tables != None and len(ref_tables) > 0:
                    cleaner.add_local(ref_dir, on_err=should_fail,
                                      on_success=not should_fail)
                    self._create(ref_tables, ref, kind, binary, zipped, False)
                for load, version, table in actions:
                    if table != None:
                        self._add(table, ds, load, version,
                                  kind, binary, zipped, False)
                        if not self.local:
                            self._add(table, ds_r, load, version,
                                      kind, binary, zipped, True)
                    else:
                        self._remove(ds, load, version, False)
                        if not self.local:
                            self._remove(ds_r, load, version, True)
                if diff:
                    if not self.local:
                        msg = chunk_index_diff(
                            os.path.join(ds_dir, 'current', 'merge.ci'),
                            os.path.join(ds_dir_r, 'current', 'merge.ci'))
                        if msg != None:
                            raise RuntimeError('Locally/cluster generated ' +
                                               'indexes differ ' + where +
                                               ': ' + msg)
                    if ref_tables != None and len(ref_tables) > 0:
                        msg = chunk_index_diff(
                            os.path.join(ds_dir, 'current', 'merge.ci'),
                            os.path.join(ref_dir, 'index.ci'))
                        if msg != None:
                            raise RuntimeError('Incrementally generated and ' +
                                               'one-shot indexes differ ' +
                                               where + ': ' + msg)
            except subprocess.CalledProcessError:
                cleaner.cleanup(after_error=True)
                if not should_fail:
                    raise RuntimeError('Chunk index generation unexpectedly ' +
                                       'failed ' + where + '.')
                continue
            except:
                cleaner.cleanup(after_error=True)
                raise
            cleaner.cleanup(after_error=False)
            if should_fail:
                raise RuntimeError('Chunk index generation unexpectedly ' +
                                   'succeeded ' + where + '.')

    def testCreate(self):
        """Test case for one-shot chunk index creation.
        """
        kinds = ('tiny', 'small', 'medium', 'large')
        ft = (False, True)
        in_table = self.in_tables_v1[0]
        for kind, binary, zipped in product(kinds, ft, ft):
            dirs = self.get_dataset('create[_r]', kind, binary, zipped)[1]
            ds, ds_dir = self.get_dataset('create', kind, binary, zipped)
            ds_r, ds_dir_r = self.get_dataset('create_r', kind, binary, zipped)
            where = str.format('({0}, {1}, {2}); see {3}, {4} ' +
                               'for log files and index/chunk files',
                               kind, 'binary' if binary else 'text',
                               'zipped' if zipped else 'unzipped',
                               os.path.join(self.out_root, 'test*.log'), dirs)
            remove_dirs(ds_dir, ds_dir_r)
            cleaner = FileCleaner(preserve=self.preserve)
            try:
                cleaner.add_local(ds_dir, on_err=False, on_success=True)
                self._create(in_table, ds, kind, binary, zipped, False)
                if not self.local:
                    cleaner.add_local(ds_dir_r, on_err=False, on_success=True)
                    self._create(in_table, ds_r, kind, binary, zipped, True)
                    # Compare resulting indexes
                    msg = chunk_index_diff(os.path.join(ds_dir, 'index.ci'),
                                           os.path.join(ds_dir_r, 'index.ci'))
                    if msg != None:
                        cleaner.clear()
                        self.fail('Locally/cluster generated indexes differ ' +
                                  where + ': ' + msg)
            except subprocess.CalledProcessError:
                self.fail('Chunk index creation failed ' + where + '.')
            cleaner.cleanup(after_error=False)

    def testAdd(self):
        """Test case for adding loads and new versions of loads
        to chunk indexes.
        """
        kinds = ('tiny', 'small')
        # Tests a series of three index adds, where each load overlaps
        # the previous one and the data spans the 0/360 longitude angle
        # discontinuity. Compares the results to those obtained with
        # one shot index generation.
        in_tables = self.in_tables_v1[:3]
        actions = [(i, 1, t) for i, t in enumerate(in_tables)]
        self._run_actions(kinds, in_tables, actions, 'add1')
        # Tests a series of 6 index adds using the 3 loads above and
        # then replacing each load with a second version.
        in_tables = self.in_tables_v2[:3]
        actions.extend([(i, 2, t) for i, t in enumerate(in_tables)])
        self._run_actions(kinds, in_tables, actions, 'add2')

    def testRemove(self):
        """Test case for removing loads and load versions
        from chunk indexes.
        """
        kinds = ('tiny', 'small')
        # Tests that an add followed by a remove of a load fails
        actions = [(0, 1, self.in_tables_v1[0]), (0, None, None)]
        self._run_actions(kinds, None, actions, 'rem1', True, False)
        # Tests that a sequence of two adds followed by removes of
        # those loads fails.
        actions = [(0, 1, self.in_tables_v1[0]),
                   (1, 1, self.in_tables_v1[1]),
                   (0, 1, None),
                   (1, 1, None)]
        self._run_actions(kinds, None, actions, 'rem2', True, False)
        # Tests that a sequence of 3 adds for different loads,
        # followed by 3 adds of different versions of those loads,
        # followed by removes of the 3 original versions yields the
        # same index as one generated from the 3 new load versions.
        in_tables_v1 = self.in_tables_v1[:3]
        in_tables_v2 = self.in_tables_v2[:3]
        actions = [(i, 1, t) for i, t in enumerate(in_tables_v1)]
        actions.extend([(i, 2, t) for i, t in enumerate(in_tables_v2)])
        actions.extend([(i, 1, None) for i in xrange(3)])
        self._run_actions(kinds, in_tables_v2, actions, 'rem3')
        # Tests that a sequence of 3 adds for different loads,
        # followed by 3 adds of different versions of those loads,
        # followed by removes of the 3 new versions yields the
        # same index as one generated from the 3 original load
        # versions.
        actions = [(i, 1, t) for i, t in enumerate(in_tables_v1)]
        actions.extend([(i, 2, t) for i, t in enumerate(in_tables_v2)])
        actions.extend([(i, 2, None) for i in xrange(3)])
        self._run_actions(kinds, in_tables_v1, actions, 'rem4')
        # Tests that a sequence of 3 adds for different loads,
        # followed by an add of a different version for one load,
        # followed by a remove of that entire load yields the same
        # index as one generated from the other 2 loads.
        in_tables = [in_tables_v1[0], in_tables_v1[2]]
        actions = [(i, 1, t) for i, t in enumerate(in_tables_v1)]
        actions.append((1, 2, in_tables_v2[1]))
        actions.append((1, None, None))
        self._run_actions(kinds, in_tables, actions, 'rem5')

    def testAll(self):
        """Test case for random sequences of adds and removes.
        Compares the resulting chunk indexes to those obtained
        with one shot chunk index creation. 
        """
        v1 = zip(xrange(len(self.in_tables_v1)), repeat(1), self.in_tables_v1)
        v2 = zip(xrange(len(self.in_tables_v2)), repeat(2), self.in_tables_v2)
        v2_loads = random.sample(v2, len(v2) / 2)
        adds = v1 + v2_loads
        random.shuffle(adds)
        removes = []
        available_removes = []
        for i, (load, version, table) in enumerate(adds):
            if random.random() < 0.25:
                removes.append((i, load, version))
        actions = []
        j = 0
        for i in xrange(len(adds)):
            if j < len(removes) and removes[j][0] < i:
                available_removes.append(removes[j])
                j += 1
            actions.append(adds[i])
            if random.random() < 0.4 and len(available_removes) > 0:
                k = random.randrange(len(available_removes))
                _, load, version = available_removes[k]
                del available_removes[k]
                actions.append((load, version, None))
        # Construct list of tables that should be used for one shot creation
        one_shot = {}
        for load, version, table in actions:
            if table == None: # a remove
                table, prev = one_shot[load]
                if prev == None:
                    del one_shot[load]
                else:
                    one_shot[load] = (prev, None)
            else: # an add
                if load in one_shot:
                    prev = one_shot[load][0]
                    one_shot[load] = (table, prev)
                else:
                    one_shot[load] = (table, None)
        in_tables = [t[0] for t in one_shot.values()]
        kinds = ('tiny', 'small')
        self._run_actions(kinds, in_tables, actions, 'all')


def main():
    parser = optparse.OptionParser("%prog [options]")
    parser.add_option(
        "-n", "--name-node", type="string", dest="namenode",
        default="hdfs://ares-cl.ipac.caltech.edu:9000", help=dedent("""\
        Hadoop name node. This identifies the hadoop name node (HDFS
        file metadata server) to use when running test cases on a cluster,
        and is expected to be of the form hdfs://<host name>:<port number> .
        The default is %default .
        """))
    parser.add_option(
        "-j", "--job-tracker", type="string", dest="jobtracker",
        default="ares-cl.ipac.caltech.edu:9001", help=dedent("""\
        Hadoop map-reduce job tracker node. This identifies the hadoop
        job tracker to use for launching test cases on a cluster, and is
        expected to be of the form <host name>:<port number> .
        The default is %default .
        """))
    parser.add_option(
        "-l", "--local", action="store_true", dest="local", default=False,
        help=dedent("""\
        When this option is specified, test cases are run only on the local
        machine, and not on a map-reduce cluster. The --name-node and
        --job-tracker options may still be specified, but are ignored.
        This significantly speeds up test case runs because it avoids
        copies to and from HDFS, and because the test inputs are too
        small to generate more than a few input splits; however, using it
        means that locally generated and cluster generated results are not
        checked for equality.
        """))
    parser.add_option(
        "-s", "--seed", type="long", dest="seed",
        help="Specifies the seed value for the random number generator.")
    parser.add_option(
        "-p", "--preserve", action="store_true", dest="preserve",
        default=False, help=dedent("""\
        When this option is specified, files generated by test cases are
        always preserved. Normally, files produced by failed test
        cases are preserved, those produced by successful test cases
        are removed.
        """))

    opts, inputs = parser.parse_args()
    if len(inputs) != 0:
        parser.error('Command line contains extraneous arguments')
    seed = opts.seed
    if seed == None:
        seed = long(time.time())
    print str.format('Seeding random number generator with {0}', seed)
    sys.stdout.flush()
    random.seed(seed)
    os.environ['STK_TEST_HADOOP_NAME_NODE'] = opts.namenode
    os.environ['STK_TEST_HADOOP_JOB_TRACKER'] = opts.jobtracker
    os.environ['STK_TEST_LOCAL'] = str(opts.local)
    os.environ['STK_TEST_PRESERVE'] = str(opts.preserve)
    suite = unittest.makeSuite(ChunkIndexTestCase)
    run = unittest.TextTestRunner().run(suite)
    if run.wasSuccessful() and not opts.preserve:
        # no failures: delete the output directory
        out_root, project = ChunkIndexTestCase.getOutRootAndProject()
        shutil.rmtree(out_root)
        if not opts.local:
            # delete HDFS output directory
            hadoop = Hadoop(opts.namenode, opts.jobtracker)
            hadoop.remove_files(['/chunk_index/' + project], True)
        sys.exit(0)
    sys.exit(1)

if __name__ == '__main__':
    main()
