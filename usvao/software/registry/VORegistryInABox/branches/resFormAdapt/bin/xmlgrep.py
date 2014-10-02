#! /usr/bin/env python
#
import sys, os.path, re

def fail(message):
    print "%s: %s" % (sys.argv[0], message)
    sys.exit(1)

def main():

    if len(sys.argv) < 2:  fail("missing element name")
    if len(sys.argv) < 3:  fail("missing file name")
    if not os.path.exists(sys.argv[2]):  fail("file not found")

    startel = re.compile(r"<%s( [^>])?>" % sys.argv[1])
    endel = re.compile(r"</%s>" % sys.argv[1])
    startcomm = re.compile(r"<!--")
    endcomm = re.compile(r"-->")

    f = None
    try: 
        f = open(sys.argv[2], 'r')

        found = False
        skip = False
        for line in f:
            if skip:
                m = endcomm.search(line)
                if m:
                    line = line[m.end():]
                    skip = False
                else:
                    continue

            if not found:
                cs = startcomm.search(line)
                if cs:
                    ce = endcomm.search(line[cs.start():])
                    if ce:
                        line = line[0:cs.start()] + line[ce.end():] 
                    else:
                        line = line[:cs.start()] 
                        skip = True
                        continue

                m = startel.search(line)
                if m:
                    line = line[m.end():]
                    found = True

            if found:
                m = endel.search(line)
                if m:
                    print line[:m.start()]
                    found = False
                    # break
    except:
        fail(sys.exc_info()[1])
    finally:
        if f is not None: f.close()


if __name__ == '__main__':
    main()
