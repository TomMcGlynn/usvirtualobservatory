#!/usr/bin/python
#
import utcdate, re, sys, os
from optparse import OptionParser

updatedre = re.compile(r"""( updated=['"])([^'"]*)(['"])""")
createdre = re.compile(r"""( created=['"])([^'"]*)(['"])""")
datere = re.compile(r'(\d{4}-\d\d-\d\d)( )(\d)')

def filter(istrm, setcreated=False, ostrm=sys.stdout):
    inres = False
    dt = utcdate.now()

    for line in istrm:
        if line.find('<ri:Resource') >= 0:  inres = True
        cm = createdre.search(line)
        if inres:
            if cm:
                if setcreated:
                    line = createdre.sub(r'\g<1>%s\3' % dt, line, 1)
                elif cm.group(2).find('T') < 0:
                    line = \
                        createdre.sub(r'\g<1>%s\3' % \
                                          datere.sub(r'\1T\3',cm.group(2)),
                                      line)
                                                            
            cm = updatedre.search(line)
            if inres and cm:
                line = updatedre.sub(r'\g<1>%s\3' % dt, line, 1)

            if line.find('>') >= 0:  inres = False

        ostrm.write(line)

class OptionParserWithErrorHandler(OptionParser):
    """
    an extension of the standard optparse.OptionParser that adds an
    error handling function
    """
    def error(self, msg, exitval=None, ostrm=sys.stderr):
        show = ""
        if self.prog is not None:
            show += "%s: " % prog
        show += msg
        print >> ostrm, show
        if exitval is not None:
            sys.exit(exitval)

def makeCLI():
    """return a configured OptionParser instance for the updatetime script"""
    usage="usage: %prog [-hcf -d out_dir -D date] file [file...]"
    desc="""update the modification date for one or more VOResource records.  
If more than one file is given, an output directory (via -d) is required. 
If the given filename is "-", standard input will be read. 
         """
    cl = OptionParserWithErrorHandler(usage=usage, description=desc)
    cl.add_option("-c", "--set-creation", action="store_true", dest="creation",
                  default=False, help="also set the creation time")
    cl.add_option("-f", "--force", action="store_true", dest="force",
                  default=False, 
                  help="overwrite existing files (when -d is used")
    cl.add_option("-d", "--output-directory", action="store", dest="outdir",
                  default=None, 
                  help="write updated files to given directory (with same name as input files)")
    cl.add_option("-D", "--use-date", action="store", dest="usedate",
                  default=None, 
                  help="update dates with the given UTC VOResource-legal date")
                  
    return cl

legalDateRe = re.compile(r'^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(.\d*)?$')
def legalDate(date):
    """return True if the give date is a VOResource-legal date"""
    return legalDateRe.match(date)

def main():
    cli = makeCLI();
    (cli.opts, cli.args) = cli.parse_args()

    if cli.opts.usedate and not legalDate(cli.opts.usedate):
        cli.error("Not a VOResource-legal date: '%s'" % cli.opts.usedate, 1)

    if cli.opts.outdir: 
        if not os.path.exists(cli.opts.outdir):
            cli.error("%s: requested output directory does not exist" % 
                      cli.opts.outdir, 2)
        elif not os.path.isdir(cli.opts.outdir) or \
             not os.access(cli.opts.outdir, os.W_OK):
            cli.error("%s: not a directory with write permission" % 
                      cli.opts.outdir, 2)
    elif len(cli.args) > 1:
        cli.error("Need -d to process multiple files", 2)
        sys.exit(2)
    elif len(cli.args) == 0:
        cli.error("Warning: no files given")

    failed = []
    for infile in cli.args:
        if infile == "-":
            istrm = sys.stdin
        elif not os.path.exists(infile):
            failed.append(infile)
            cli.error("%s: file not found (skipping...)" % infile)
            continue
        else:
            istrm = open(infile, 'r')

        if cli.opts.outdir:
            if infile == "-":
                infile = "stdin.xml"
            outfile = os.path.join(cli.opts.outdir, infile)
            if not cli.opts.force and os.path.exists(outfile):
                failed.append(infile)
                cli.error("%s: file exists; won't overwrite without --force (skipping)" % outfile)
                continue
            if not os.path.exists(os.path.dirname(outfile)):
                os.makedirs(os.path.dirname(outfile))
            ostrm = open(outfile, 'w')

        else:
            ostrm = sys.stdout

        filter(istrm, cli.opts.creation, ostrm)

    if len(failed) > 0:
        if len(cli.args) > 1:
            cli.error("Failed to process %i input files" % len(failed), 3)
        else:
            cli.error("Failed to process input file", 3)

if __name__ == '__main__':
    main()
    sys.exit(0)


        
