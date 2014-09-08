#! /usr/bin/env python
#
# Unit tests for matching input points and ellipses against a chunk index
import os.path
import sys
# Enable importing from the STK bin directory
stk_bin_dir = os.path.abspath(os.path.join(os.path.dirname(__file__),
                                           os.path.pardir, 'bin'))
sys.path.append(stk_bin_dir)
from contextlib import nested
from ipac.stk.hadoop_utils import Hadoop, FileCleaner
from itertools import product
import json
import math
import numpy
import optparse
import os
import pdb
import random
import re
import shutil
import socket
import subprocess
from textwrap import dedent
import time
import unittest


MAX_MATCHES = 8 # max no. of matches to generate per test position
MATCH_ACCURACY = 0.000001/3600.0 # 1 microarcsec in degrees

def clampPhi(phi):
    """Clamps input angle to lie in the range [-90.0, 90.0].
    """
    if phi > 90.0:
        return 90.0
    elif phi < -90.0:
        return -90.0
    else:
        return phi

def point_in_box(minTheta, maxTheta, minPhi, maxPhi):
    """Returns spherical coordinates (theta, phi) for a point chosen
    approximately uniformly at random from within the specified
    longitude/latitude angle box.
    """
    # pick theta
    if minTheta <= maxTheta:
      theta = minTheta + random.random() * (maxTheta - minTheta)
      if theta > maxTheta:
        theta = maxTheta;
    else:
      # wrap-around
      m = minTheta - 360.0
      theta = m + random.random() * (maxTheta - m);
      if theta < 0:
        theta += 360.0
    # pick phi
    minZ = math.sin(math.radians(minPhi))
    maxZ = math.sin(math.radians(maxPhi))
    z = minZ + random.random() * (maxZ - minZ)
    phi = math.degrees(math.asin(z))
    if phi < minPhi:
        phi = minPhi
    elif phi > maxPhi:
        phi = maxPhi
    return theta, phi

def spherical_coords(v):
    """Returns spherical coordinates in degrees for the input cartesian 3-vector.
    """
    x = v[0]
    y = v[1]
    z = v[2]
    d2 = x * x + y * y
    if d2 == 0.0:
        theta = 0.0
    else:
        theta = math.degrees(math.atan2(y, x))
        if theta < 0.0:
            theta += 360.0
    if z == 0.0:
        phi = 0.0
    else:
        phi = math.degrees(math.atan2(z, math.sqrt(d2)))
    return (theta, phi)

def east_of(p):
    theta = math.radians(p[0]);
    return numpy.array([-math.sin(theta),
                        math.cos(theta),
                        0.0], dtype=numpy.float64)

def north_of(p):
    theta = math.radians(p[0]);
    phi = math.radians(p[1]);
    sp = math.sin(phi);
    return numpy.array([-math.cos(theta) * sp,
                        -math.sin(theta) * sp,
                        math.cos(phi)], dtype=numpy.float64);

def unit_vec(p):
    theta = math.radians(p[0])
    phi = math.radians(p[1])
    cp = math.cos(phi)
    return numpy.array([math.cos(theta) * cp,
                        math.sin(theta) * cp,
                        math.sin(phi)], dtype=numpy.float64)

def perturb_axis(p, sigma, positionAngle):
    """ Randomly perturbs ``p`` according to a gaussian distribution
    having a standard deviation of ``sigma`` degrees. The resulting
    point is additionally constrained to lie at a position angle of
    ``pa`` degrees relative to ``p``.
    """
    pos = unit_vec(p)
    n = north_of(p)
    e = east_of(p)
    pa = math.radians(positionAngle)
    m = math.radians(sigma * random.gauss(0.0, sigma))
    pp = (e * math.sin(pa) + n * math.cos(pa)) * math.sin(m) + pos * math.cos(m)
    # convert back to spherical coordinates
    return spherical_coords(pp)

def perturb(p, sigma):
    """Perturbs the point p (given in spherical coordinates) according to a
    gaussian distribution having a standard deviation of ``sigma`` degrees.
    """
    return perturb_axis(p, sigma, random.random() * 360.0)

def distance(p1, p2):
    # halversine distance formula
    sinDTheta = math.sin(0.5 * math.radians(p2[0] - p1[0]))
    sinDPhi = math.sin(0.5 * math.radians(p2[1] - p1[1]))
    cosP1CosP2 = math.cos(math.radians(p1[1])) * math.cos(math.radians(p2[1]))
    a = sinDPhi * sinDPhi + cosP1CosP2 * sinDTheta * sinDTheta
    b = math.sqrt(a)
    c = 1.0 if b > 1.0 else b
    return math.degrees(2.0 * math.asin(c))

def max_alpha(radius, centerPhi):
    assert radius >= 0.0 and radius <= 180.0
    assert centerPhi >= -90.0 and centerPhi <= 90.0
    POLE_EPSILON = 0.01
    if radius == 0.0:
        return 0.0
    if abs(centerPhi) + radius > 90.0 - POLE_EPSILON:
        return 180.0
    y  = math.sin(math.radians(radius))
    c1 = math.cos(math.radians(centerPhi - radius))
    c2 = math.cos(math.radians(centerPhi + radius))
    x  = math.sqrt(abs(c1 * c2))
    return math.degrees(abs(math.atan(y / x)));

def points_on_circle(c, r, n):
    """Generates an n-gon lying on the circle with center c and
    radius r. Vertices are equi-spaced.
    """
    north = north_of(c)
    east = east_of(c)
    c = unit_vec(c)
    points = []
    sr = math.sin(math.radians(r))
    cr = math.cos(math.radians(r))
    aoff = random.uniform(0.0, 2.0 * math.pi)
    for i in xrange(n):
        a = 2.0 * i * math.pi / n
        sa = math.sin(a + aoff)
        ca = math.cos(a + aoff)
        p = (ca * north[0] + sa * east[0],
             ca * north[1] + sa * east[1],
             ca * north[2] + sa * east[2])
        points.append(spherical_coords((cr * c[0] + sr * p[0],
                                        cr * c[1] + sr * p[1],
                                        cr * c[2] + sr * p[2])))
    return points

def points_on_ellipse(c, smaa, smia, ang, n):
    """Generates points lying on the ellipse with center c,
    semi-major/semi-minor axis lengths smaa/smia, and major axis
    angle (E of N) ang.
    """
    north = north_of(c)
    east = east_of(c)
    c = unit_vec(c)
    points = []
    sa = math.sin(math.radians(ang))
    ca = math.cos(math.radians(ang))
    smaa = math.radians(smaa)
    smia = math.radians(smia)
    aoff = random.uniform(0.0, 2.0 * math.pi)
    for i in xrange(n):
        a = aoff + 2.0 * i * math.pi / n
        x = smaa * math.cos(a)
        y = smia * math.sin(a)
        # rotate x, y by a
        nc = ca * x - sa * y
        ec = sa * x + ca * y
        cc = math.sqrt(abs(1.0 - nc * nc - ec * ec))
        points.append(spherical_coords((cc * c[0] + nc * north[0] + ec * east[0],
                                        cc * c[1] + nc * north[1] + ec * east[1],
                                        cc * c[2] + nc * north[2] + ec * east[2])))
    return points

def build_points(to_assoc, to_index, radius):
    """Builds test data for point within circle matches.
    """
    assert radius > 0.0 and radius < 5.0
    with nested(open(to_assoc, 'wb+'), open(to_index, 'wb+')) as (assoc_file, index_file):
        # Write header for assoc_file
        hdr_fmt = str.format(
            '|{{0:10}}|{{1:24}}|{{2:24}}|{{3:24}}|{{4:{0}}}|\n',
            MAX_MATCHES * 11)
        fmt = str.format(
            ' {{0[0]:10}} {{0[1][0]:24.17}} {{0[1][1]:24.17}} {{0[2]:24.17}} {{1:{0}}} \n',
            MAX_MATCHES * 11)
        idx_fmt = '{0[0]}|{0[1][0]!r}|{0[1][1]!r}|\n'
        assoc_file.write(str.format(hdr_fmt, 'cntr', 'ra', 'dec',
                                    'smaa', 'matches'))
        assoc_file.write(str.format(hdr_fmt, 'int', 'double', 'double',
                                    'double', 'char'))
        assoc_file.write(str.format(hdr_fmt, '', 'deg', 'deg', 'deg', ''))
        assoc_file.write(str.format(hdr_fmt, '', '', '', '', ''))
        # Divide unit sphere into latitude angle stripes
        phiMin = -90.0
        phiMax = 90.0
        i = 0
        deltaPhi = 4.0 * radius;
        phi = phiMin
        while phi < phiMax:
            centerPhi = clampPhi(max(abs(phi), abs(phi + deltaPhi)))
            deltaTheta = max_alpha(4.0 * radius, centerPhi)
            theta = 0.0
            # Divide latitude angle stripes into boxes (by longitude angle)
            while theta < 360.0 - 2.0 * deltaTheta:
                # Create a random point inside a sub-region of each box
                # such that a circle of the given radius centered on that
                # point is guaranteed not to cross the box boundaries
                if theta == 0.0:
                    # make sure longitude angle wrap-around is tested
                    p = point_in_box(360.0 - 0.125 * deltaTheta,
                                     0.125 * deltaTheta,
                                     clampPhi(phi + deltaPhi * 0.38),
                                     clampPhi(phi + deltaPhi * 0.62))
                else:
                    p = point_in_box(theta + deltaTheta * 0.38,
                                     theta + deltaTheta * 0.62,
                                     clampPhi(phi + deltaPhi * 0.38),
                                     clampPhi(phi + deltaPhi * 0.62))
                r = random.uniform(0.1 * radius, radius)
                assoc = (i, p, r)
                i += 1

                # Generate matches
                nm = random.randint(0, MAX_MATCHES)
                cp_in = points_on_circle(p, r - MATCH_ACCURACY, nm)
                cp_out = points_on_circle(p, r + MATCH_ACCURACY, nm)
                matches = ','.join(map(str, xrange(i, i + len(cp_in))))
                entries = [(i + j, e) for j, e in enumerate(cp_in)]
                i += len(cp_in)
                entries.extend([(i + j, e) for j, e in enumerate(cp_out)])
                i += len(cp_out)
                # write out test ellipse and all its matches
                assoc_file.write(str.format(fmt, assoc, matches))
                # write out points to index
                for e in entries:
                    index_file.write(str.format(idx_fmt, e))
                theta += deltaTheta
            phi += deltaPhi

def build_ellipses(to_assoc, to_index, radius):
    """Builds test data for point within ellipse matches.
    """
    assert radius > 0.0 and radius < 5.0
    with nested(open(to_assoc, 'wb+'), open(to_index, 'wb+')) as (assoc_file, index_file):
        # Write header for assoc_file
        hdr_fmt = str.format(
            '|{{0:10}}|{{1:24}}|{{2:24}}|{{3:24}}|{{4:24}}|{{5:24}}|{{6:{0}}}|\n',
            MAX_MATCHES * 11)
        fmt = str.format(
            ' {{0[0]:10}} {{0[1][0]:24.17}} {{0[1][1]:24.17}} {{0[2]:24.17}} {{0[3]:24.17}} {{0[4]:24.17}} {{1:{0}}} \n',
            MAX_MATCHES * 11)
        idx_fmt = '{0[0]}|{0[1][0]!r}|{0[1][1]!r}|\n'
        assoc_file.write(str.format(hdr_fmt, 'cntr', 'ra', 'dec', 'smaa', 'smia', 'pa', 'matches'))
        assoc_file.write(str.format(hdr_fmt, 'int', 'double', 'double', 'double', 'double', 'double', 'char'))
        assoc_file.write(str.format(hdr_fmt, '', 'deg', 'deg', 'deg', 'deg', 'deg', ''))
        assoc_file.write(str.format(hdr_fmt, '', '', '', '', '', '', ''))
        # Divide unit sphere into latitude angle stripes
        phiMin = -90.0
        phiMax = 90.0
        i = 0
        deltaPhi = 4.0 * radius;
        phi = phiMin
        while phi < phiMax:
            centerPhi = clampPhi(max(abs(phi), abs(phi + deltaPhi)))
            deltaTheta = max_alpha(4.0 * radius, centerPhi)
            theta = 0.0
            # Divide latitude angle stripes into boxes (by longitude angle)
            while theta < 360.0 - 2.0 * deltaTheta:
                # Create a random point inside a sub-region of each box
                # such that a circle of the given radius centered on that
                # point is guaranteed not to cross the box boundaries
                if theta == 0.0:
                    # make sure longitude angle wrap-around is tested
                    p = point_in_box(360.0 - 0.125 * deltaTheta,
                                     0.125 * deltaTheta,
                                     clampPhi(phi + deltaPhi * 0.38),
                                     clampPhi(phi + deltaPhi * 0.62))
                else:
                    p = point_in_box(theta + deltaTheta * 0.38,
                                     theta + deltaTheta * 0.62,
                                     clampPhi(phi + deltaPhi * 0.38),
                                     clampPhi(phi + deltaPhi * 0.62))

                tmp = 0.5 * abs(random.gauss(0.0, 1.0)) + 0.5;
                smaa = radius * (1.0 if tmp > 1.0 else tmp)
                smia = smaa * random.uniform(0.01, 0.99)
                pa = random.uniform(0.0, 180.0)
                assoc = (i, p, smaa, smia, pa)
                i += 1
                # Generate matches
                nm = random.randint(0, MAX_MATCHES)
                ep_in = points_on_ellipse(p, smaa - MATCH_ACCURACY, smia - MATCH_ACCURACY, pa, nm)
                ep_out = points_on_ellipse(p, smaa + MATCH_ACCURACY, smia + MATCH_ACCURACY, pa, nm)
                matches = ','.join(map(str, xrange(i, i + len(ep_in))))
                entries = [(i + j, e) for j, e in enumerate(ep_in)]
                i += len(ep_in)
                entries.extend([(i + j, e) for j, e in enumerate(ep_out)])
                i += len(ep_out)
                # write out test ellipse and all its matches
                assoc_file.write(str.format(fmt, assoc, matches))
                # write out points to index
                for e in entries:
                    index_file.write(str.format(idx_fmt, e))
                theta += deltaTheta
            phi += deltaPhi


class AssocTestCase(unittest.TestCase):
    """Test case for associating points/ellipses with points in a
    chunk index.
    """
    @staticmethod
    def getOutRootAndProject():
        host_pid = socket.gethostname() + '-' + str(os.getpid())
        project = 'stk-test-' + host_pid
        out_root = os.path.abspath(os.path.join(os.path.dirname(__file__),
                                                'out', 'assoc', host_pid))
        return out_root, project
        
    def setUp(self):
        self.radius = float(os.environ['STK_TEST_RADIUS'])
        self.max_radius = { 'tiny': 20.0,
                            'small': 5.0,
                            'medium': 0.1667,
                            'large': 0.05,
                          }
        self.preserve = os.environ['STK_TEST_PRESERVE'] == 'True'
        self.out_root, self.project = AssocTestCase.getOutRootAndProject()
        self.in_dir = os.path.abspath(os.path.join(
            os.path.dirname(__file__), 'data'))
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
                          'degrees,degrees,degrees,degrees,degrees,' +\
                          'degrees,,,,,'        
        self.assoc = os.path.join(stk_bin_dir, 'assoc')
        # Make sure output directory exists
        if not os.path.exists(self.out_root):
            os.makedirs(self.out_root)
        # Decompress required input files if necessary
        tables = [os.path.join(self.in_dir, 'iraspsc_330_30.ipac.tbl'),
                  os.path.join(self.in_dir, 'iraspsc_330_30.tbl'),
                 ]
        for p in tables:
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

    def _create(self, input_tables, dataset, kind, binary, zipped, iras):
        args = [self.chunk_index]
        args.extend(['-l', os.path.join(self.out_root, 'test.log'),
                     '-L', os.path.join(self.out_root, 'test.hadoop.log')])
        args.append('-i')
        if binary:
            args.append('-b')
        if zipped:
            args.append('-z')

        args.append('--kind=' + kind)
        if iras:
            args.extend(['-t', self.iras_types, '-u', self.iras_units,
                         'create', self.project, dataset, self.out_root,
                         self.iras_columns])
        else:
            args.extend(['-t', 'long,double,double', '-u', ',deg,deg',
                         'create', self.project, dataset, self.out_root,
                         'cntr,ra,dec'])
        if isinstance(input_tables, (list, tuple)):
            args.extend(input_tables)
        else:
            args.append(input_tables)
        with open('/dev/null', 'wb+') as dev_null:
            subprocess.check_call(args, stdout=dev_null.fileno(),
                                  stderr=dev_null.fileno())

    def _assoc(self, chunk_index, assoc_table,
               match_table, no_match_table, circular):
        args = [self.assoc, '-j', '-i', chunk_index, '-t', assoc_table,
                '-T', match_table, '-n', no_match_table,
                '-M', 'smaa', '-C', 'cntr', '-c', 'cntr,matches',
                '-N', 'cntr,matches', '-p', 'u_', '-P', 'i_']
        if not circular:
            args.extend(['-m', 'smia', '-A', 'pa'])
        print 'Running ' + ' '.join(args)
        sys.stdout.flush()
        p = subprocess.Popen(args, stdout=subprocess.PIPE)
        ouput = p.communicate()[0]
        results = json.loads(ouput)
        if results['stat'] != 'OK':
            raise RuntimeError(str.format('Call to {0} failed: {1}',
                                          self.assoc, results['msg']))
        elif p.returncode != 0:
            raise RuntimeError(str.format(
                'Call to {0} resulted in non-zero exit code', self.assoc))

    def _verify(self, match_table, no_match_table):
        # check that the no-match table doesn't contain any records
        # with known matches
        with open(no_match_table, 'rb') as nmf:
            for line in nmf:
                if line.startswith('|') or line.startswith('\\'):
                    continue
                results = re.findall(r'\S+', line)
                if len(results) != 1:
                    raise RuntimeError(str.format(
                        'Generated test position {0} has non-empty known ' +
                        'match list {1}, but is recorded in no-match table {2}.',
                        results[0], results[1], no_match_table))
        # check that the match table contains the expected results
        with open(match_table, 'rb') as mf:
            last_cntr = None
            matches = None
            for line in mf:
                if line.startswith('|') or line.startswith('\\'):
                    continue
                results = re.findall(r'\S+', line)
                cntr = int(results[0])
                if len(results) == 5:
                    raise RuntimeError(str.format(
                        'Invalid output in match table {0} - check if ' +
                        'test position {1} has an empty known match ' +
                        'list but is recorded in the match table anyway.',
                        match_table, cntr))
                elif len(results) != 6:
                    raise RuntimeError(str.format(
                        'Invalid output in match table {0} - expecting 6 ' +
                        'columns per line', match_table))
                if cntr != last_cntr:
                    last_cntr = cntr
                    matches = set(map(int, results[1].split(',')))
                    nm = int(results[4])
                    if len(matches) != nm:
                        raise RuntimeError(str.format(
                            'Generated test position {0} was matched {1} ' +
                            'times; expected {2} matches ({3}).',
                            last_cntr, nm, len(matches), results[1]))
                m = int(results[5])
                if m not in matches:
                    raise RuntimeError(str.format(
                        'Generated test position {0} matches position {1} ' +
                        'which does not appear in the known match-list {2}.',
                        last_cntr, m, results[1]))

    def testCircles(self):
        """Checks that a set of circles with known matches are correctly
        associated. Results must be accurate to within MATCH_ACCURACY deg
        for the test to pass.
        """
        # Create test data
        kinds = ('tiny', 'small', 'medium',) #'large')
        ft = (False, True)
        for kind in kinds:
            assoc_table = os.path.join(self.out_root,
                                       str.format('circles_{0}.tbl', kind))
            index_table = os.path.join(self.out_root,
                                       str.format('idx_circles_{0}.tbl', kind))
            build_points(assoc_table, index_table,
                         min(self.radius, self.max_radius[kind]))
            for binary, zipped in product(ft, ft):
                ds, ds_dir = self.get_dataset('circles', kind, binary, zipped)
                match_table = os.path.join(ds_dir, 'circles_match.tbl')
                no_match_table = os.path.join(ds_dir, 'circles_no_match.tbl')
                cleaner = FileCleaner(preserve=self.preserve)
                try:
                    cleaner.add_local(ds_dir, on_err=False, on_success=True)
                    self._create(index_table, ds, kind, binary, zipped, False)
                    self._assoc(os.path.join(ds_dir, 'index.ci'), assoc_table,
                                match_table, no_match_table, True)
                    self._verify(match_table, no_match_table)
                except:
                    cleaner.cleanup(after_error=True)
                    print str.format(
                        'Association failed ({0}, {1}, {2}); see {3}, {4} ' +
                        'for log files, generated test data and index/chunk ' +
                        'files.', kind, 'binary' if binary else 'text',
                        'zipped' if zipped else 'unzipped', self.out_root,
                        ds_dir)
                    sys.stdout.flush()
                    raise
                cleaner.cleanup(after_error=False)

    def testEllipses(self):
        """Checks that a set of ellipses with known matches are correctly
        associated. Results must be accurate to within MATCH_ACCURACY deg
        for the test to pass.
        """
        # Create test data
        kinds = ('tiny', 'small', 'medium',) #'large')
        ft = (False, True)
        for kind in kinds:
            assoc_table = os.path.join(self.out_root,
                                       str.format('ellipses_{0}.tbl', kind))
            index_table = os.path.join(self.out_root,
                                       str.format('idx_ellipses_{0}.tbl', kind))
            build_ellipses(assoc_table, index_table,
                           min(self.radius, self.max_radius[kind]))
            for binary, zipped in product(ft, ft):
                ds, ds_dir = self.get_dataset('ellipses', kind, binary, zipped)
                match_table = os.path.join(ds_dir, 'ellipses_match.tbl')
                no_match_table = os.path.join(ds_dir, 'ellipses_no_match.tbl')
                cleaner = FileCleaner(preserve=self.preserve)
                try:
                    cleaner.add_local(ds_dir, on_err=False, on_success=True)
                    self._create(index_table, ds, kind, binary, zipped, False)
                    self._assoc(os.path.join(ds_dir, 'index.ci'), assoc_table,
                                match_table, no_match_table, False)
                    self._verify(match_table, no_match_table)
                except:
                    cleaner.cleanup(after_error=True)
                    print str.format(
                        'Association failed ({0}, {1}, {2}); see {3}, {4} ' +
                        'for log files, generated test data and index/chunk ' +
                        'files.', kind, 'binary' if binary else 'text',
                        'zipped' if zipped else 'unzipped', self.out_root,
                        ds_dir)
                    sys.stdout.flush()
                    raise
                cleaner.cleanup(after_error=False)

    def testIRAS(self):
        """Associates a part of the IRAS PSC with itself using a match radius
        of MATCH_ACCURACY arcsec, checking that each input point is matched
        exactly once.
        """
        assoc_table = os.path.join(self.in_dir, 'iraspsc_330_30.ipac.tbl')
        index_table = os.path.join(self.in_dir, 'iraspsc_330_30.tbl')
        kinds = ('tiny', 'small', 'medium', 'large')
        ft = (False, True)
        for kind, binary, zipped in product(kinds, ft, ft):
            ds, ds_dir = self.get_dataset('identity', kind, binary, zipped)
            match_table = os.path.join(ds_dir, 'identity_match.tbl')
            no_match_table = os.path.join(ds_dir, 'identity_no_match.tbl')
            cleaner = FileCleaner(preserve=self.preserve)
            try:
                cleaner.add_local(ds_dir, on_err=False, on_success=True)
                self._create(index_table, ds, kind, binary, zipped, True)
                args = [self.assoc, '-j', '-i',
                        os.path.join(ds_dir, 'index.ci'), '-t', assoc_table,
                        '-T', match_table, '-n', no_match_table,
                        '-M', repr(MATCH_ACCURACY) + ' deg', '-c', 'cntr',
                        '-C', 'cntr', '-N', 'cntr', '-p', 'u_',
                        '-P', 'i_']
                print 'Running ' + ' '.join(args)
                sys.stdout.flush()
                p = subprocess.Popen(args, stdout=subprocess.PIPE)
                ouput = p.communicate()[0]
                results = json.loads(ouput)
                if results['stat'] != 'OK':
                    raise RuntimeError(str.format('Call to {0} failed: {1}',
                                                  self.assoc, results['msg']))
                elif p.returncode != 0:
                    raise RuntimeError(str.format(
                        'Call to {0} resulted in non-zero exit code', self.assoc))
                # check that no-match table contains no entries
                with open(no_match_table, 'rb') as nmf:
                    for line in nmf:
                        if line.startswith('|') or line.startswith('\\'):
                            continue
                        raise RuntimeError(str.format(
                            'IRAS PSC source {0} did not match itself',
                            line.strip()))
                # check that each PSC source matches only itself
                with open(match_table, 'rb') as mf:
                    for line in mf:
                        if line.startswith('|') or line.startswith('\\'):
                            continue
                        results = re.findall(r'\S+', line)
                        if len(results) != 5:
                            raise RuntimeError(str.format(
                                'Invalid output in match table {0} - ' +
                                'expecting 5 columns per line', match_table))
                        nm = int(results[3])
                        cntr = int(results[0])
                        if nm != 1 or cntr != int(results[4]):
                            raise RuntimeError(str.format(
                                'IRAS PSC source {0} has more than one ' +
                                'match or does not match itself.', cntr))
            except:
                cleaner.cleanup(after_error=True)
                print str.format(
                    'Association failed ({0}, {1}, {2}); see {3}, ' +
                    '{4} for log files, generated test data and index/chunk ' +
                    'files.', kind, 'binary' if binary else 'text',
                    'zipped' if zipped else 'unzipped', self.out_root, ds_dir)
                sys.stdout.flush()
                raise
            cleaner.cleanup(after_error=False)

def main():
    parser = optparse.OptionParser("%prog [options]")
    parser.add_option(
        "-r", "--radius", type="float", dest="radius", default=2.0,
        help=dedent("""\
        Specifies the maximum search radius (degrees) to use when
        generating random test data."""))
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
    random.seed(seed)
    os.environ['STK_TEST_RADIUS'] = repr(opts.radius)
    os.environ['STK_TEST_PRESERVE'] = str(opts.preserve)
    suite = unittest.makeSuite(AssocTestCase)
    run = unittest.TextTestRunner().run(suite)
    if run.wasSuccessful() and not opts.preserve:
        # no failures: delete the output directory
        out_root, project = AssocTestCase.getOutRootAndProject()
        shutil.rmtree(out_root)
        sys.exit(0)
    sys.exit(1)

if __name__ == '__main__':
    main()
