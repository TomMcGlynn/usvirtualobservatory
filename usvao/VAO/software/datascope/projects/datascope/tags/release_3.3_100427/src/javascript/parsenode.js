var DMfields = new Object;

DMfields["shortName"] = 0;
DMfields["title"] = 1;
DMfields["capabilityClass"] = 2;
DMfields["publisher"] = 3;
DMfields["type"] = 4;
DMfields["subject"] = 5;
DMfields["waveband"] = 6;
DMfields["identifier"] = 7;
DMfields["facility"] = 8;
DMfields["index"] = 9;
DMfields["node"] = 10;
var nodeArray = new Array;

var ParseNodeCount = 0;

function ParseNode(label, element, value, nodeLevel) {

	var elemIndex = DMfields[element];
	var isNode = false;
	this.children = null;
	var count = 0;
	var nodeIndex = ParseNodeCount;
	var nodeOpen = true;
	nodeArray[ParseNodeCount] = this;
	ParseNodeCount += 1;

	this.match = function(resource) {
		var val = resource[elemIndex];
		var status;
		status = val.match(value);

		if (status != null) {
			if (isNode) {
				for ( var i = 0; i < this.children.length; i += 1) {
					if (this.children[i].match(resource)) {
						count += 1;
						return true;
					}
				}
				return false;

			} else {
				this.addResource(resource);
				count += 1;
				return true;
			}

		} else {
			return false;
		}
	};

	this.clean = function() {

		if (isNode) {
			for (var i = 0; i < this.children.length; i += 1) {
				this.children[i].clean();
			}

		} else {
			this.children = null;
		}

		count = 0;
	};

	this.toggleOpen = function() {
		nodeOpen = !nodeOpen;
	};

	function printPrefix(label, nodeLevel) {
		if (label == null || nodeLevel == 0) {
			return "";
		}

		var chng = "<a href='javascript: void changeNode(" + nodeIndex + ")'>";
		if (nodeOpen) {
			chng += "<img border=0 title='Close this category' alt='contract' src=/vo/datascope/images/minus.gif></font>";
		} else {
			chng += "<img border=0 title='Open this category' alt='expand' src=/vo/datascope/images/plus.gif>("
					+ count + ")";
		}
		chng += "</a> ";

		if (nodeLevel == 1) {
			return "<tr> <th colspan=6 class=center>" + chng + label + "</tr>";
		} else if (nodeLevel == 2) {
			return "<tr><th class=right>" + chng + label + "</th>";
		} else {
			return "<td>" + label + "</td>";
		}
	}

	function printSuffix(nodeLevel) {
		return "</tr>";
	}

	this.print = function() {

		if (count <= 0) {
			return "";
		}

		var str = printPrefix(label, nodeLevel, nodeOpen);

		if (nodeOpen) {
			if (isNode) {
				for ( var i = 0; i < this.children.length; i += 1) {
					str += this.children[i].print();
				}
			} else {
				if (nodeLevel == 1) {
					str += "<tr><td></td>";
				}
				for ( var j = 0; j < this.children.length; j += 1) {
					if (j > 0 && j % 5 == 0) {
						str += "</tr><tr><td></td>";
					}
					str += matchEntry(this.children[j]);
				}
			}
		}

		str += printSuffix(nodeLevel, nodeOpen);
		return str;
	};

	this.addResource = function(resource) {

		if (this.children == null) {
			this.children = new Array;
		}

		if (isNode) {
			alert("Error in ParseTree: Mixed node type");
		}
		resource[DMfields["node"]] = nodeIndex;

		this.children[this.children.length] = resource;
	};

	this.addNode = function(node) {

		if (this.children == null) {
			this.children = new Array;
			isNode = true;
		}

		this.children[this.children.length] = node;
	};
}

function getParseTree() {

	var root = new ParseNode(null, "identifier", ".*", 0);
	var node = new ParseNode("Major Multiwavelength Services", "shortName",
			"^(ADS|NED|Simbad|SkyView)", 1);

	root.addNode(node);

	node = new ParseNode(
			"Images (Data in one or more <a href=http://fits.gsfc.nasa.gov>FITS</a> files)",
			"capabilityClass", "SimpleImageAccess", 1);

	node.addNode(new ParseNode("Multi", "waveband", ",", 2));
	node.addNode(new ParseNode("Optical", "waveband", "Optical", 2));
	node.addNode(new ParseNode("Radio ", "waveband", "Radio", 2));
	node.addNode(new ParseNode("Infrared", "waveband", "Infrared", 2));
	node
			.addNode(new ParseNode("UV ", "waveband",
					"(UV|Ultraviolet)", 2));
	node.addNode(new ParseNode("X-ray", "waveband", "X-ray", 2));
	node
			.addNode(new ParseNode("Gamma-ray", "waveband",
					"Gamma-ray", 2));
	node.addNode(new ParseNode("Other", "waveband", ".*", 2));

	root.addNode(node);

	node = new ParseNode(null, "capabilityClass", "ConeSearch", 0);
	var subn = new ParseNode(
			"Lists of Observations (Data in one <a href=http://us-vo.org/VOTable>VOTable</a>)",
			"type", "Archive", 1);

	subn.addNode(new ParseNode("Multi", "waveband", ",", 2));
	subn.addNode(new ParseNode("Optical", "waveband", "Optical", 2));
	subn.addNode(new ParseNode("Radio", "waveband", "Radio", 2));
	subn.addNode(new ParseNode("Infrared", "waveband", "Infrared", 2));
	subn
			.addNode(new ParseNode("UV", "waveband",
					"(UV|Ultraviolet)", 2));
	subn.addNode(new ParseNode("X-ray", "waveband", "X-ray", 2));
	subn
			.addNode(new ParseNode("Gamma-ray", "waveband",
					"Gamma-ray", 2));
	subn.addNode(new ParseNode("Other", "waveband", ".*", 2));

	node.addNode(subn);

	subn = new ParseNode(
			"Catalogs of Objects (Data in one <a href=http://us-vo.org/VOTable>VOTable</a>)",
			"type", "Catalog", 1);

	subn.addNode(new ParseNode("Surveys", "subject", "Survey", 2));
	subn.addNode(new ParseNode("Galaxies", "subject", "Galax", 2));
	subn.addNode(new ParseNode("Stars", "subject", "Star", 2));
	subn.addNode(new ParseNode("Other objects", "subject", ".*", 2));

	node.addNode(subn);

	node.addNode(new ParseNode("Other tables", "type", ".*", 1));
	root.addNode(node);

	return root;
}

function changeNode(index) {
	nodeArray[index].toggleOpen();
	matchTreePrint(parseTree);
	return false;
}
