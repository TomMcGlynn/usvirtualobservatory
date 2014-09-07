#! /usr/bin/env python
#
import sys, os, re
varre = re.compile(r'\${([^}]*)}')

prefix=None
if len(sys.argv) > 1:
    prefix = sys.argv[1]

# sys.stdout.write("basedir=%s\n" % basedir)

for line in sys.stdin:
    if re.match(r'^\s*#', line) or re.match(r'^\s*$', line):
        continue
    if line.find('=') >= 0:
        name, val = line.split('=', 1)
        val = val.strip()
        name = re.sub(r'\.', '_', name)
        if prefix:  name = prefix + name

        pos = 0
        varmatch = varre.search(val, pos)
        while varmatch:
            pos = varmatch.end()
            propname = varmatch.group(1)
            varname = re.sub(r'\.', '_', propname)
            val = re.sub(r'\${%s}' % propname, '${%s}' % varname, val)
            varmatch = varre.search(val, pos)
        val = re.sub(r'\\=', '=', val)
        if val[0] != '"':
            val = '"%s"' % val

        print "%s=%s" % (name, val)

    else:
        sys.stdout.write(line)

            
