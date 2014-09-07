var DMfields = new Object;
/*

Old metadata.stem ...
XMM BSS|XMM-Newton Bright Serendipitous Survey|CONE|NASA/GSFC HEASARC|Catalog|Survey Source|X-ray|ivo://nasa.heasarc/xmmbss|NOT PROVIDED
IRASSSC|IRAS Serendipitous Survey Catalog|CONE|NASA/GSFC HEASARC|Catalog|Survey Source|Infrared|ivo://nasa.heasarc/irasssc|NOT PROVIDED
IRAS PSC|IRAS Point Source Catalog, Version 2.0|CONE|NASA/GSFC HEASARC|Catalog|Survey Source|Infrared|ivo://nasa.heasarc/iraspsc|NOT PROVIDED
DSS1R|Digitized Sky Survey 1 - Red|SIAP/CUTOUT|Space Telescope Science|Survey|Surveys,Stars,Galaxies|Optical|ivo://archive.stsci.edu/dss/dss1red|Palomar Observatory, UK Southern Schmidt Survey, A
OpenCluster|New Optically Visible Open Clusters and Candidates Catalog|CONE|NASA/GSFC HEASARC|Catalog|Star Cluster|Optical|ivo://nasa.heasarc/openclust|NOT PROVIDED
WBH 6cmGP|New Catalog of Compact 6cm Sources in the Galactic Plane|CONE|NASA/GSFC HEASARC|Catalog|Survey Source|Radio|ivo://nasa.heasarc/wbhgp6cm|NOT PROVIDED
M101CXO|M 101 Chandra X-Ray Point Source Catalog|CONE|NASA/GSFC HEASARC|Catalog|Survey Source|X-ray|ivo://nasa.heasarc/m101cxo|NOT PROVIDED

New metadata.stem ...
J/A+AS/111/229|CCD meas. of visual binaries (Abad+, 1995)|ConeSearch|CDS|Catalog|Positional_Data,Binaries:eclipsing|Optical|ivo://CDS.VizieR/J/A+AS/111/229|CDS|http://vizier.u-strasbg.fr/cgi-bin/VizieR-2?-sou
rce=J/A+AS/111/229
Wood/Bin|Wood Interacting Binaries Catalog|ConeSearch|NASA/GSFC HEASARC|Catalog|Star|Optical|ivo://nasa.heasarc/woodebcat|NASA/GSFC HEASARC|http://heasarc.gsfc.nasa.gov/cgi-bin/vo/cone/coneGet.pl?table=woodeb
cat&
J/MNRAS/365/439|JHK photometry for UKIRT faint galaxies (Christopher+, 2006)|ConeSearch|CDS|Catalog|Galaxies|Optical,Infrared|ivo://CDS.VizieR/J/MNRAS/365/439|CDS|http://vizier.u-strasbg.fr/cgi-bin/VizieR-2?-
source=J/MNRAS/365/439
Taylor|Pulsar Catalog|ConeSearch|NASA/GSFC HEASARC|Catalog|Pulsar|Radio|ivo://nasa.heasarc/pulsar|NASA/GSFC HEASARC|http://heasarc.gsfc.nasa.gov/cgi-bin/vo/cone/coneGet.pl?table=pulsar&
J/A+A/397/997|Hipparcos red stars (Platais+, 2003)|ConeSearch|CDS|Catalog|Photometry:wide-band,Velocities,Stars:late-type|Optical,Infrared|ivo://CDS.VizieR/J/A+A/397/997|CDS|http://vizier.u-strasbg.fr/cgi-bin
/VizieR-2?-source=J/A+A/397/997

metadata.stem example entry
J/A+A/424/545
|Optically faint obscured quasars (Padovani+, 2004)
|ConeSearch
|CDS
|Catalog
|AGN,QSOs
|X-ray
|ivo://CDS.VizieR/J/A+A/424/545
|CDS
|http://vizier.u-strasbg.fr/cgi-bin/VizieR-2?-source=J/A+A/424/545
*/
/*
DMfields["ShortName"]   = 0
DMfields["Title"]       = 1
DMfields["ServiceType"] = 2
DMfields["Publisher"]   = 3
DMfields["Type"]        = 4
DMfields["Subject"]     = 5
DMfields["CoverageSpectral"] = 6
DMfields["Identifier"]  = 7
DMfields["Facility"]    = 8
DMfields["ServiceURL"]    = 9
*/
DMfields["shortName"]   = 0
DMfields["title"]       = 1
DMfields["capabilityClass"] = 2
DMfields["publisher"]   = 3
DMfields["type"]        = 4
DMfields["subject"]     = 5
DMfields["waveband"] = 6
DMfields["identifier"]  = 7
DMfields["publisher"]    = 8
DMfields["accessURL"]    = 9

DMfields["index"]       = 10
DMfields["node"]        = 11
var nodeArray = new Array;

var ParseNodeCount = 0

function ParseNode(label,  element, value, nodeLevel) {

   var elemIndex = DMfields[element]
   var isNode    = false
   var children  = null
   var active    = false
   var count     = 0
   var nodeIndex = ParseNodeCount;
   var nodeOpen  = true
   nodeArray[ParseNodeCount] = this
   ParseNodeCount += 1

   function match(resource) {

      var val = resource[elemIndex]
      var status
      status =  val.match(value);

      if (status != null) {
         if (isNode) {
            for (var i=0; i<children.length; i += 1) {
               if (children[i].match(resource)) {
                  count += 1
                  return true
               }
            }
            return false;
         } else {
            addResource(resource)
            count += 1
            return true
         }
      } else {
         return false;
      }
   }

   function clean() {
      if (isNode) {
         for (i=0; i<children.length; i += 1) {
            children[i].clean();
         }
      } else {
         children = null
      }
      count = 0
   }

   function toggleOpen() {
      nodeOpen = ! nodeOpen
   }

   function print() {
      if (count <= 0) {
         return "";
      }
      var str = printPrefix(label, nodeLevel, nodeOpen);
      if (nodeOpen) {
         if (isNode) {
            for (var i=0; i<children.length; i += 1) {
               str += children[i].print();
            }
         } else {
            if (nodeLevel == 1) {
               str += "<tr><td></td>";
            }
            for (var i=0; i<children.length; i += 1) {
               if (i > 0 && i%5 == 0) {
                  str += "</tr><tr><td></td>";
               }
               str += matchEntry(children[i]);
            }
         }
      }
      str += printSuffix(nodeLevel, nodeOpen);
      return str;
   }

   function printPrefix(label, nodeLevel) {
      if (label == null || nodeLevel == 0) {
         return "";
      }
      var chng = "<a href='javascript: void changeNode("+nodeIndex+")'>"
      if (nodeOpen) {
         chng += "<img border='0' title='Close this category' alt='contract' src='@URL_PATH@@IMG_PATH@/minus.gif' /></font>"
         //	    chng += "<img border='0' title='Close this category' alt='contract' src='/vo/datascope/images/minus.gif' /></font>"
      } else {
         chng += "<img border='0' title='Open this category' alt='expand' src='@URL_PATH@@IMG_PATH@/plus.gif' />("+count+")"
         //	    chng += "<img border='0' title='Open this category' alt='expand' src='/vo/datascope/images/plus.gif' />("+count+")"
      }
      chng += "</a> "
      if (nodeLevel == 1) {
         return "<tr> <th colspan='6' class='center'>"+chng+label+"</tr>";
      } else if (nodeLevel == 2) {
         return "<tr><th class='right'>"+chng+label+"</th>";
      } else {
         return "<td>"+label+"</td>"
      }
   }

   function printSuffix(nodeLevel) {
      return "</tr>";
   }

   function addResource(resource) {
      if (children == null) {
         children = new Array;
      }
      if (isNode) {
         alert("Error in ParseTree: Mixed node type");
      }
      resource[DMfields["node"]] = nodeIndex
      children[children.length] = resource;
   }

   function addNode(node) {
      if (children == null) {
         children = new Array;
         isNode     = true;
      }
      children[children.length] = node
   }
   this.match = match;
   this.print = print;
   this.addResource = addResource;
   this.addNode     = addNode;
   this.toggleOpen  = toggleOpen;
   this.clean       = clean;
}


function OldRegistrygetParseTree() {

   var root = new ParseNode(null, "Identifier", ".*", 0);
   var node = new ParseNode("Major Multiwavelength Services", 
      "ShortName", "^(ADS|NED|Simbad|SkyView)", 1);

   root.addNode(node)

   node = new ParseNode("Images (Data in one or more <a href='http://fits.gsfc.nasa.gov'>FITS</a> files)",
      "ServiceType", "SIAP", 1);
   node.addNode(new ParseNode("Multi",    "CoverageSpectral", ",",        2))
   node.addNode(new ParseNode("Optical",  "CoverageSpectral", "Optical",  2))
   node.addNode(new ParseNode("Radio ",   "CoverageSpectral", "Radio",    2))
   node.addNode(new ParseNode("Infrared", "CoverageSpectral", "Infrared", 2))
   node.addNode(new ParseNode("UV ",      "CoverageSpectral", "(UV|Ultraviolet)",       2))
   node.addNode(new ParseNode("X-ray",    "CoverageSpectral", "X-ray",    2))
   node.addNode(new ParseNode("Gamma-ray","CoverageSpectral", "Gamma-ray",2))
   node.addNode(new ParseNode("Other",    "CoverageSpectral", ".*",       2))

   root.addNode(node);

   node = new ParseNode(null, "ServiceType", "CONE", 0);
   var subn = new ParseNode("Lists of Observations (Data in one <a href='http://us-vo.org/VOTable'>VOTable</a>)",
      "Type", "Archive", 1)
   subn.addNode(new ParseNode("Multi"    , "CoverageSpectral", ",",         2))
   subn.addNode(new ParseNode("Optical"  , "CoverageSpectral", "Optical",   2))
   subn.addNode(new ParseNode("Radio"    , "CoverageSpectral", "Radio",     2))
   subn.addNode(new ParseNode("Infrared" , "CoverageSpectral", "Infrared",  2))
   subn.addNode(new ParseNode("UV"       , "CoverageSpectral", "(UV|Ultraviolet)",        2))
   subn.addNode(new ParseNode("X-ray"    , "CoverageSpectral", "X-ray",     2))
   subn.addNode(new ParseNode("Gamma-ray", "CoverageSpectral", "Gamma-ray", 2))
   subn.addNode(new ParseNode("Other"    , "CoverageSpectral", ".*",        2))

   node.addNode(subn);

   subn = new ParseNode("Catalogs of Objects (Data in one <a href='http://us-vo.org/VOTable'>VOTable</a>)",
      "Type", "Catalog", 1)

   subn.addNode(new ParseNode("Surveys"  , "Subject", "Survey",   2))
   subn.addNode(new ParseNode("Galaxies" , "Subject", "Galax",    2))
   subn.addNode(new ParseNode("Stars"    , "Subject", "Star",     2))
   subn.addNode(new ParseNode("Other objects","Subject",".*",     2))

   node.addNode(subn);

   node.addNode(new ParseNode("Other tables", "Type", ".*", 1))
   root.addNode(node);

   return root;
}

function getParseTree() {

   var root = new ParseNode(null, "identifier", ".*", 0);
   var node = new ParseNode("Major Multiwavelength Services", 
      "shortName", "^(ADS|NED|Simbad|SkyView)", 1);

   root.addNode(node)

   node = new ParseNode("Images (Data in one or more <a href='http://fits.gsfc.nasa.gov'>FITS</a> files)",
      "capabilityClass", "SIAP", 1);
   node.addNode(new ParseNode("Multi",    "waveband", ",",        2))
   node.addNode(new ParseNode("Optical",  "waveband", "Optical",  2))
   node.addNode(new ParseNode("Radio ",   "waveband", "Radio",    2))
   node.addNode(new ParseNode("Infrared", "waveband", "Infrared", 2))
   node.addNode(new ParseNode("UV ",      "waveband", "(UV|Ultraviolet)",       2))
   node.addNode(new ParseNode("X-ray",    "waveband", "X-ray",    2))
   node.addNode(new ParseNode("Gamma-ray","waveband", "Gamma-ray",2))
   node.addNode(new ParseNode("Other",    "waveband", ".*",       2))

   root.addNode(node);

   node = new ParseNode(null, "capabilityClass", "ConeSearch", 0);
   var subn = new ParseNode("Lists of Observations (Data in one <a href='http://us-vo.org/VOTable'>VOTable</a>)",
      "type", "Archive", 1)
   subn.addNode(new ParseNode("Multi"    , "waveband", ",",         2))
   subn.addNode(new ParseNode("Optical"  , "waveband", "Optical",   2))
   subn.addNode(new ParseNode("Radio"    , "waveband", "Radio",     2))
   subn.addNode(new ParseNode("Infrared" , "waveband", "Infrared",  2))
   subn.addNode(new ParseNode("UV"       , "waveband", "(UV|Ultraviolet)",        2))
   subn.addNode(new ParseNode("X-ray"    , "waveband", "X-ray",     2))
   subn.addNode(new ParseNode("Gamma-ray", "waveband", "Gamma-ray", 2))
   subn.addNode(new ParseNode("Other"    , "waveband", ".*",        2))

   node.addNode(subn);

   subn = new ParseNode("Catalogs of Objects (Data in one <a href='http://us-vo.org/VOTable'>VOTable</a>)",
      "type", "Catalog", 1)

   subn.addNode(new ParseNode("Surveys"  , "subject", "Survey",   2))
   subn.addNode(new ParseNode("Galaxies" , "subject", "Galax",    2))
   subn.addNode(new ParseNode("Stars"    , "subject", "Star",     2))
   subn.addNode(new ParseNode("Other objects","subject",".*",     2))

   node.addNode(subn);

   node.addNode(new ParseNode("Other tables", "type", ".*", 1))
   root.addNode(node);

   return root;
}

function changeNode(index) {
   nodeArray[index].toggleOpen()
   matchTreePrint(parseTree)
   return false
}

