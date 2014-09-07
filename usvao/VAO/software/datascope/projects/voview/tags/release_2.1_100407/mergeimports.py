#! /usr/stsci/pyssg/Python/bin/python
# Work around bug that prevents Safari from handling xsl:import correctly
# by constructing a file with the imports directly incorporated.
#
# This does handle multiple and nested imports, but it probably does not
# catch all the possible overrides when there are duplicated templates or
# params among the imported files.  It should be OK for the simple case
# where each file imports zero or more other files.
#
# Does not handle xsl:include
#
# R. White, 2007 November 21

import sys, getopt

if '/usr/local/hla/lib/python' not in sys.path:
	sys.path.append('/usr/local/hla/lib/python')

# Various alternative ways to get ElementTree
# Borrowed from pesterfish

try:
	import xml.etree.ElementTree as ET
except ImportError:
	import elementtree.ElementTree as ET

xsl = '{http://www.w3.org/1999/XSL/Transform}'
ET._namespace_map[xsl[1:-1]] = "xsl"

def applyimports(etree, importedfiles=None):

	"""Given input containing the XSL ElementTree, return modified XSL string
	incorporating the imported files with overrides applied

	importedfiles parameter is used in recursive calls to track files that have
	already been included
	"""

	if importedfiles is None:
		importedfiles = {}

	# find the xsl:imports
	imports = etree.findall(xsl+'import')
	# priority is set by "last-is-first" rule
	imports.reverse()
	ietrees = []
	for el in imports:
		ifile = el.get('href')
		if ifile:
			if not importedfiles.has_key(ifile):
				# read the imported XSL file (and any nested imports)
				importedfiles[ifile] = 1
				try:
					ii = applyimports(ET.fromstring(open(ifile).read()),
						importedfiles=importedfiles)
				except SyntaxError, e:
					raise SyntaxError("%s: %s" % (ifile,str(e)))
				except IOError, e:
					raise SyntaxError(str(e))
				ietrees.append(ii)
		else:
			raise SyntaxError("Found an xsl:import without href value")
		# remove the xsl:import node
		etree.remove(el)

	# look for templates that are overridden
	tag = xsl+'template'
	templates = etree.findall(tag)
	tkeys = {}
	for i, el in enumerate(templates):
		key = (el.get('name'), el.get('match'))
		tkeys[key] = 1
	# remove the overridden templates from the imported files
	for elem in ietrees:
		elements = elem.findall(tag)
		for e in elements:
			key = (e.get('name'), e.get('match'))
			if tkeys.has_key(key):
				elem.remove(e)
			else:
				# add this template to the list not to be overridden
				tkeys[key] = 1

	# override top-level script parameters and variables
	taglist = [xsl+'param', xsl+'variable']
	tkeys = {}
	for el in etree:
		if el.tag in taglist:
			key = el.get('name')
			tkeys[key] = 1
	# remove the overridden params from the imported files
	for ietree in ietrees:
		for el in ietree:
			key = el.get('name')
			if el.tag in taglist and tkeys.has_key(key):
				ietree.remove(el)
			else:
				tkeys[key] = 1

	# insert the edited, imported stylesheets
	# note this undoes the reversal
	for ietree in ietrees:
		for i in range(len(ietree)):
			etree.insert(i,ietree[i])

	# Hack: add back the other namespaces, which ElementTree omits
	etree.attrib['xmlns:vo'] = "http://www.ivoa.net/xml/VOTable/v1.1" 
	etree.attrib['xmlns:v1'] = "http://vizier.u-strasbg.fr/VOTable"
	etree.attrib['xmlns:v2'] = "http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
	etree.attrib['xmlns:v3'] = "http://www.ivoa.net/xml/VOTable/v1.0"
	return etree

def usage(msg=None):
	print >> sys.stderr, """Usage: %s [-o output-file] [input-file]
	to write file with imports included

If input-file is omitted, reads from stdin.
If output-file is omitted, writes to stdout.
""" % sys.argv[0]
	if msg:
		print >> sys.stderr, "error:", msg
	sys.exit(1)

if __name__ == "__main__":
	try:
		optlist, args = getopt.getopt(sys.argv[1:], "o:h")
	except getopt.GetoptError, e:
		usage(str(e))
	if len(args) > 1:
		usage("Only one input file can be specified")
	output = None
	for opt, value in optlist:
		if opt == "-o":
			output = value
		elif opt == "-h":
			usage()
		else:
			usage("Unknown option "+opt)

	if args:
		inname = args[0]
		fin = open(inname)
	else:
		inname = "stdin"
		fin = sys.stdin

	try:
		etree = applyimports(ET.fromstring(fin.read()))
		if output:
			fout = open(output,"w")
		else:
			fout = sys.stdout
		print >> fout, ET.tostring(etree, encoding="UTF-8")
		fout.close()
	except SyntaxError, e:
		raise SyntaxError("%s: %s" % (inname,str(e)))
