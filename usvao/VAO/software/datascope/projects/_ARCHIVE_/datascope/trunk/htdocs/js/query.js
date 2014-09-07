// This function converts simple column constraints
// into a filter that can be applied to a VOTABLE XML document
//
// T.McGlynn 9/12/2007

function xslt(indices, constraints, types) {

    var xsl1='<?xml version="1.0" encoding="UTF-8"?>\n'+
            '<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" '+
	    'xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo" '+
            'version="1.0">\n'+
            '<xsl:variable name="lc" select="\'abcdefghijklmnopqrstuvwxyz\'" />\n'+
            '<xsl:variable name="uc" select="\'ABCDEFGHIJKLMNOPQRSTUVWXYZ\'" />\n'+
            '<xsl:template match="TR|vo:TR">\n'
    var xsl2='<xsl:copy>\n'+
             '<xsl:apply-templates select="@*|node()"/>\n'+
             '</xsl:copy>\n'+
             '</xsl:if>\n'+
             '</xsl:template>\n'+
             '<xsl:template match="@*|node()">\n'+
             '<xsl:copy>\n'+
             '<xsl:apply-templates select="@*|node()"/>\n'+
             '</xsl:copy>\n'+
             '</xsl:template>\n'+
             '</xsl:stylesheet>\n'

    if (indices.length > 0) {
        var all = new Array()
	for (var i=0; i<indices.length; i += 1) {
	    var con = makeXSLConstraint(indices[i], constraints[i], types[i]);
	    if (con != null) {
	        all.push(con);
	    }
	}
	if (all.length) {
	    var full = all.join(" and ");
	    var xslgen = '<xsl:if test="'+full+'">\n'
	    return xsl1+xslgen+xsl2
	} else {
	    return null;
	}
    } else {
        return null
    }
}

// Convert a single constraint into appropriate XSLT filter elements.
function makeXSLConstraint(index, constraint, isChar) {
    if (constraint.length == 0) {
        return null;
    }
    if (constraint.substring(0,1) == '=') {
        constraint = constraint.substring(1);
    }
    if (constraint.length == 0) {
        return null;
    }
    if (isChar) {
        return charConstraint(index, constraint);
    } else {
        return numConstraint(index, constraint);
    }
}

// Handle a constraint on a character column
function charConstraint(index, constraint) {
    constraint = constraint.toUpperCase();
    if (constraint.indexOf('*') >= 0 ) {
        return wildCardConstraint(index, constraint);
    } else {
        return stdCharConstraint(index, constraint);
    }
}
   
function wildCardConstraint(index, constraint) {

    var initial = false;
    var final   = false;
	
    if (constraint.substring(0,1) == "*") {
	initial = true;
	constraint = constraint.substring(1);
    }
    if (constraint.substring(constraint.length-1) == '*') {
	final = true;
	constraint = constraint.substring(0,constraint.length-1);
    }
	
    if (constraint.length == 0) {
	return null;
    }
    var fields = constraint.split('\*')
    var out    = new Array()
	
    out.push("position() = "+index)
	
    for (var i=0; i<fields.length; i += 1) {
	if (i == 0 && !initial) {
	    out.push("starts-with(translate(normalize-space(string()), $lc, $uc),'" + fields[i] + "')");
	    
	} else if (i == fields.length-1 && !final) {
	    out.push("contains(translate(normalize-space(string()), $lc, $uc),'"+fields[i]+"')");
	    out.push("string-length(substring-after(translate(normalize-space(string()), $lc, $uc),'"+fields[i]+"'))=0");
	    
        } else {
	    out.push("contains(translate(string(), $lc, $uc), '"   + fields[i] + "')");
	}
	if (i > 0) {
	    out.push("string-length(substring-after(translate(string(), $lc, $uc), '"  +fields[i]   + "')) &lt; " +
		     "string-length(substring-after(translate(string(), $lc, $uc), '" + fields[i-1] + "'))")
	}
    }
    return "(TD|vo:TD)[" + out.join(" and ") + "]" ;
}

function stdCharConstraint(index, constraint) {
    constraint = trim(constraint);
    return "translate(normalize-space(TD["+index+"]), $lc, $uc)='"+constraint+"'";
//    return "TD[position()="+index+" and translate(normalize-space(value()), $lc, $uc)='"+constraint+"']";
}
	

function rangeConstraint(index, constraint) {

    var fields=constraint.split("\.\.", 2);
    if (fields[0].length == 0 || fields[1].length == 0) {
	return null;
    }
    var con =  "TD["+index+"] &gt;=" +fields[0]+" and "+
	       "TD["+index+"] &lt;=" +fields[1]+"";
    return con;
}

function numConstraint(index, constraint) {
	
    if (constraint.indexOf("..") > 0) {
        return rangeConstraint(index, constraint);
	
    } else {
        if (constraint.substring(0,1) == ">" ) {
	    constraint = constraint.replace(">", "&gt;")
	} else if (constraint.substring(0,1) == "<" ) {
	    constraint = constraint.replace("<", "&lt;")
	} else if (constraint.substring(0,1) != "=") {
	    constraint = "=" + constraint;
	} 
        constraint = "TD["+index+"]"+constraint
	return constraint;
    }
}
