// This function converts simple column constraints
// into a filter that can be applied to a VOTABLE XML document
//
// T.McGlynn 9/12/2007

function xslt(dir, indices, constraints, types, scales) {

    var xslStart='<?xml version="1.0" encoding="UTF-8"?>\n'+
            '<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" '+
            '  version="1.0">\n'+
            '    <xsl:import href="'+dir+'/basefilter.xsl" />' +
            '    <xsl:variable name="lc" select="\'abcdefghijklmnopqrstuvwxyz\'" />\n'+
            '    <xsl:variable name="uc" select="\'ABCDEFGHIJKLMNOPQRSTUVWXYZ\'" />\n'+
            '    <xsl:template name="rowMatch">\n'+ 
            '       <xsl:param name="fields" />\n';
	    
    var xslEnd=
            '       <xsl:otherwise>0</xsl:otherwise> \n' +
            '    </xsl:choose></xsl:template>\n' +
      
            '    <xsl:template match="/">\n' +
            '       <xsl:apply-imports />\n' +
            '    </xsl:template>\n' +
            '</xsl:stylesheet>';

    
    var varDefs = new Array();
    var cons    = new Array();
    
    if (indices.length > 0) {
	for (var i=0; i<indices.length; i += 1) {
	    makeXSLConstraint(indices[i], constraints[i], types[i], scales[i], varDefs, cons);
	}
	if (cons.length > 0) {
	    var xsl = xslStart;
	    for (var i in varDefs) {
	        xsl += varDefs[i];
	    }
	    xsl += '  <xsl:choose>\n';
	    xsl += '    <xsl:when test=\"' + cons.join(" and ") + '">1</xsl:when>\n';
	    xsl += xslEnd;
	    return xsl;
	} else {
	    return null;
	}
    } else {
        return null
    }
}

// Convert a single constraint into appropriate XSLT filter elements.
function makeXSLConstraint(index, constraint, isChar, scale, defs, cons) {
    if (constraint.substring(0,1) == '=') {
        constraint = constraint.substring(1);
    }
    if (constraint.length > 0) {
        if (isChar) {
            charConstraint(index, constraint, defs, cons);
        } else {
            numConstraint(index, constraint, scale, defs, cons);
	}
    }
}

// Handle a constraint on a character column
function charConstraint(index, constraint, defs, cons) {
    constraint = constraint.toUpperCase();
    if (constraint.indexOf('*') >= 0 ) {
        wildCardConstraint(index, constraint, defs, cons);
    } else {
        stdCharConstraint(index, constraint, defs, cons);
    }
}
   
function wildCardConstraint(index, constraint, defs, cons) {

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
    cons.push("$fields[" + out.join(" and ") + "]") ;
}

function stdCharConstraint(index, constraint, defs, cons) {
    constraint = trim(constraint);
    cons.push( "translate(normalize-space($fields["+index+"]), $lc, $uc)='"+constraint+"'");
}
	

function rangeConstraint(index, constraint, scale, defs, cons) {

    var fields=constraint.split("\.\.", 2);
    if (fields[0].length == 0 || fields[1].length == 0) {
	return null;
    }
    
    var min = deSci(fields[0], scale);
    var max = deSci(fields[1], scale);
    
    var cn = 'col'+index;
    defs.push(makeVar(cn, index));
    cn = '$' + cn;
    cons.push(cn +" &gt;= " +min + " and  "+cn + "&lt;=" +max); 
}

// Convert sexagesimal to decimal
function deSex(num, scale) {
    if (num.match('\:')) {
        var fields = num.split('\:');
        if (fields.length <= 0) {
            return 0;
        }
        var coeff = scale;
	if (fields[0].indexOf('-') >= 0) {
	    coeff = -coeff;
	}
        var val = 0;
        for (var i=0; i<fields.length; i += 1) {
            val += Math.abs(fields[i])*coeff;
	    coeff /= 60.;
        }
        return val;
    } else {
        return num;
    }
}
    
// Convert scientific notation to simple decimal.    
function deSci(num, scale) {
    if (scale) {
        num = deSex(num, scale);
    }
    var str = ""+num;
    
    if (str.match(/e/i)) {
    
        str = str.replace(/^ */, "");
	var c1 = str.substring(0,1);
	
	var sign = "";
	
	if (c1 == "+" || c1 == "-") {
	    sign = c1;
	    str = str.substring(1);
        }
    
        var flds = str.split(/[eE]/);
	var res =  flds[0]*Math.pow(10,flds[1]);
	
	var integ;
	var frac;
	
	var mant = flds[0];
	var exp  = flds[1];
	
	if (exp.length == 0) {
	    exp  = "0";
	}
	if (mant.length == 0) {
	    mant = "0";
	}
	
	var pnt = mant.indexOf(".");
	
	if (pnt < 0) {
	    integ = mant;
            frac  = "";
	} else if (pnt == 0) {
	    integ = "";
	    frac = mant.substring(1);
	} else {
	    integ = mant.substring(0,pnt);
	    frac  = mant.substring(pnt+1);
	}
	
	var res;
	exp = new Number(exp).valueOf();
	if (exp == 0) {
	    res = mant;
	} else if (exp > 0) {
	    if (exp > frac.length) {
	        res = integ+frac+zeroes(exp-frac.length);
	    } else {
	        res = integ + frac.substring(0, exp) + "."+
		    frac.substring(exp);
	    }
	} else {
	    if (-exp > integ.length) {
	        res = "."+zeroes(-exp - integ.length)+integ+frac;
	    } else {
	        res = integ.substring(0,integ.length+exp)+"." +
		      integ.substring(integ.length+exp)+frac;
	    }
	}
	res = sign+res;
	return res;
    } else {
        return num;
    }
}

function numConstraint(index, constraint, scale, defs, cons) {
	
    var op = "";
    if (constraint.indexOf("..") > 0) {
        return rangeConstraint(index, constraint, scale, defs, cons);
	
    } else {
    
        var cn = 'col' + index;
        defs.push(makeVar(cn, index));
        cn = '$' + cn;
    
        var op = '=';
        
        if (constraint.substring(0,1) == ">" ) {
	    op = "&gt;";
	    constraint = constraint.substring(1);
	} else if (constraint.substring(0,1) == "<" ) {
	    op = "&lt;";
	    constraint = constraint.substring(1);
	}
	if (constraint.substring(0,1) ==  "=") {
	    constraint = constraint.substring(1);
	}
        constraint = deSci(constraint, scale);
	
	cons.push(cn + op + constraint);
    }
}

function makeVar(name, index) {
    return '<xsl:variable name="'+name+'">\n'+
           '<xsl:call-template name="SciNum">\n'+
	   '<xsl:with-param name="num" select="$fields['+index+']" />\n'+
	   '</xsl:call-template></xsl:variable>\n';
}

function zeroes(len) {
    var x = "";
    for (var i = 0; i<len; i += 1) {
        x += "0";
    }
    return x;
}
