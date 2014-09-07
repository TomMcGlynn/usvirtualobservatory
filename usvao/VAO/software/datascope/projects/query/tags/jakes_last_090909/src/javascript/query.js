// This function converts simple column constraints
// into a filter that can be applied to a VOTABLE XML document
//
// T.McGlynn 9/12/2007

function xslt(indices, constraints, types, scales) {
   

   var xslStart='<?xml version="1.0" encoding="UTF-8"?>\n'+
      '<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" '+
      '   xmlns:d="namespace for powers of 10"\n'+
      '   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" \n'+
      '   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"\n'+
      '   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"\n'+
      '   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"\n'+
      '   exclude-result-prefixes="vo v1 v2 v3"\n'+
      '   version="1.0" >\n'+
      '\n'+
      '<xsl:template name="Scientific">\n'+
      '  <xsl:param name="Num"/>\n'+
      '  <xsl:param name="Factor">1</xsl:param>\n'+
      '  <xsl:variable name="Esplit">\n'+
      '    <xsl:choose>\n'+
      '      <xsl:when test="contains($Num,\'E\')">E</xsl:when>\n'+
      '      <xsl:otherwise>e</xsl:otherwise>\n'+
      '    </xsl:choose>\n'+
      '  </xsl:variable>\n'+
      '  <xsl:variable name="result">\n'+
      '    <xsl:choose>\n'+
      '      <xsl:when test="not(contains($Num,$Esplit))">\n'+
      '        <xsl:value-of select="$Num"/>\n'+
      '      </xsl:when>\n'+
      '      <xsl:otherwise>\n'+
      '        <xsl:variable name="m" select="substring-before($Num,$Esplit)"/>\n'+
      '        <xsl:variable name="e" select="substring-after($Num,$Esplit)"/>\n'+
      '        <xsl:choose>\n'+
      '          <xsl:when test="substring($e,1,1)=\'+\'">\n'+
      '            <xsl:call-template name="Scientific_Helper">\n'+
      '              <xsl:with-param name="m" select="$m"/>\n'+
      '              <xsl:with-param name="e" select="substring($e,2)"/>\n'+
      '            </xsl:call-template>\n'+
      '          </xsl:when>\n'+
      '          <xsl:otherwise>\n'+
      '            <xsl:call-template name="Scientific_Helper">\n'+
      '              <xsl:with-param name="m" select="$m"/>\n'+
      '              <xsl:with-param name="e" select="$e"/>\n'+
      '            </xsl:call-template>\n'+
      '          </xsl:otherwise>\n'+
      '        </xsl:choose>\n'+
      '      </xsl:otherwise>\n'+
      '    </xsl:choose>\n'+
      '  </xsl:variable>\n'+
      '  <xsl:value-of select="format-number(number($result)*number($Factor),\'#0.#####\')"/>\n'+
      '</xsl:template>\n'+
      '\n'+
      '<xsl:template name="Scientific_Helper">\n'+
      '  <xsl:param name="m"/>\n'+
      '  <xsl:param name="e"/>\n'+
      '  <xsl:choose>\n'+
      '    <xsl:when test="not(number($e)) or $e = 0">\n'+
      '      <xsl:value-of select="$m"/>\n'+
      '    </xsl:when>\n'+
      '    <xsl:when test="$e &gt; 0">\n'+
      '      <xsl:variable name="factor">1<xsl:call-template name="Nzeros">\n'+
      '          <xsl:with-param name="N" select="$e"/>\n'+
      '        </xsl:call-template>\n'+
      '      </xsl:variable>\n'+
      '      <xsl:value-of select="$m * $factor"/>\n'+
      '    </xsl:when>\n'+
      '    <xsl:otherwise>\n'+
      '      <xsl:variable name="factor">1<xsl:call-template name="Nzeros">\n'+
      '          <xsl:with-param name="N" select="-$e"/>\n'+
      '        </xsl:call-template>\n'+
      '      </xsl:variable>\n'+
      '      <xsl:value-of select="$m div $factor"/>\n'+
      '    </xsl:otherwise>\n'+
      '  </xsl:choose>\n'+
      '</xsl:template>\n'+
      '\n'+
      '<xsl:template name="Nzeros">\n'+
      '  <xsl:param name="N"/>\n'+
      '  <xsl:choose>\n'+
      '    <xsl:when test="$N &lt; 2">0</xsl:when>\n'+
      '    <xsl:otherwise>\n'+
      '      <xsl:variable name="Nhalf" select="floor($N div 2)"/>\n'+
      '      <xsl:variable name="shalf">\n'+
      '        <xsl:call-template name="Nzeros">\n'+
      '          <xsl:with-param name="N" select="$Nhalf"/>\n'+
      '        </xsl:call-template>\n'+
      '      </xsl:variable>\n'+
      '      <xsl:choose>\n'+
      '        <xsl:when test="2*$Nhalf = $N">\n'+
      '          <xsl:value-of select="concat($shalf,$shalf)"/>\n'+
      '        </xsl:when>\n'+
      '        <xsl:otherwise>\n'+
      '          <xsl:value-of select="concat($shalf,$shalf,\'0\')"/>\n'+
      '        </xsl:otherwise>\n'+
      '      </xsl:choose>\n'+
      '    </xsl:otherwise>\n'+
      '  </xsl:choose>\n'+
      '</xsl:template>\n'+
      '\n'+
      '<xsl:template match="TABLEDATA|vo:TABLEDATA|v1:TABLEDATA|v2:TABLEDATA|v3:TABLEDATA">\n'+
      '  <PARAM datatype="int" name="VOV:TotalCount" value="{count(//TR)}" />\n'+
      '  <xsl:copy>\n'+
      '    <xsl:for-each select="TR|vo:TR|v1:TR|v2:TR|v3:TR">\n'+
      '      <xsl:variable name="fields" select="(TD|vo:TD|v1:TD|v2:TD|v3:TD)" />\n'+
      '      <xsl:variable name="include">\n'+
      '        <xsl:call-template name="rowMatch">\n'+
      '          <xsl:with-param name="fields" select="$fields" />\n'+
      '        </xsl:call-template>\n'+
      '      </xsl:variable>\n'+
      '      <xsl:if test="$include &gt; 0">\n'+
      '        <xsl:copy>\n'+
      '          <xsl:apply-templates select="@*|node()" />\n'+
      '        </xsl:copy>\n'+
      '      </xsl:if>\n'+
      '    </xsl:for-each>\n'+
      '  </xsl:copy>\n'+
      '</xsl:template>\n'+
      '\n'+
      '<xsl:template match="@*|node()">\n'+
      '  <xsl:copy>\n'+
      '    <xsl:apply-templates select="@*|node()"/>\n'+
      '  </xsl:copy>\n'+
      '</xsl:template>\n'+
      '\n'+
      '<xsl:variable name="lc" select="\'abcdefghijklmnopqrstuvwxyz\'" />\n'+
      '<xsl:variable name="uc" select="\'ABCDEFGHIJKLMNOPQRSTUVWXYZ\'" />\n'+
      '<xsl:template name="rowMatch">\n'+ 
      '  <xsl:param name="fields" />\n';
   
   var xslEnd=
      '    <xsl:otherwise>0</xsl:otherwise> \n' +
      '  </xsl:choose>\n'+
      '</xsl:template>\n' +
      '<xsl:template match="/">\n' +
      '  <xsl:apply-templates />\n' +
      '</xsl:template>\n' +
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
//   constraint = trim(constraint);
   if (constraint.length == 0) {
      return null;
   }
   if (constraint.substring(0,1) == '!') {
      var negate = true;
      constraint = constraint.substring(1);
   } else {
      negate = false;
   }
// ignore an initial = sign
// ! and != are the same
   if (constraint.substring(0,1) == '=') {
      constraint = constraint.substring(1);
   }
   if (constraint.length == 0) {
      return null;
   }
   if (isChar) {
      charConstraint(index, constraint, negate, defs, cons);
   } else {
      numConstraint(index, constraint, negate, scale, defs, cons);
   }
}

// Handle a constraint on a character column
function charConstraint(index, constraint, negate, defs, cons) {
   constraint = constraint.toUpperCase();
   if (constraint.indexOf('*') >= 0 ) {
      wildCardConstraint(index, constraint, negate, defs, cons);
   } else {
      stdCharConstraint(index, constraint, negate, defs, cons);
   }
}

function wildCardConstraint(index, constraint, negate, defs, cons) {
/*
      negate not yet implemented
*/   
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

function stdCharConstraint(index, constraint, negate, defs, cons) {
   constraint = trim(constraint);
   if (negate) {
      cons.push( "translate(normalize-space($fields["+index+"]), $lc, $uc)!='"+constraint+"'");
   } else {
      cons.push( "translate(normalize-space($fields["+index+"]), $lc, $uc)='"+constraint+"'");
   }
}


function rangeConstraint(index, constraint, negate, scale, defs, cons) {
   var fields=constraint.split("\.\.", 2);
   if (fields[0].length == 0 || fields[1].length == 0) {
      return null;
   }
   var min = deSci(fields[0], scale);
   var max = deSci(fields[1], scale);
   var cn = 'col'+index;
   defs.push(makeVar(cn, index));
   cn = '$' + cn;
   if (negate) {
      cons.push(cn +" &lt; " +min + " and  "+cn + "&gt;" +max); 
   } else {
      cons.push(cn +" &gt;= " +min + " and  "+cn + "&lt;=" +max); 
   }
}

function numConstraint(index, constraint, negate, scale, defs, cons) {
   
   var op = "";
   if (constraint.indexOf("..") > 0) {
      return rangeConstraint(index, constraint, negate, scale, defs, cons);
      
   } else {
      var cn = 'col' + index;
      defs.push(makeVar(cn, index));
      cn = '$' + cn;
      var op;   //    = '=';
      
      if (negate) {   // I don't know why you would type !>= instead of <
// possible ops after a ! removed: >, >=, <, <=, =
// what about <> ? Or !<> ?
         if (constraint.substring(0,2) == ">=" ) {
            op = "&lt;";
            constraint = constraint.substring(2);
         } else if (constraint.substring(0,1) == ">" ) {
            op = "&lt;=";
            constraint = constraint.substring(1);
         } else if (constraint.substring(0,2) == "<=" ) {
            op = "&gt;";
            constraint = constraint.substring(2);
         } else if (constraint.substring(0,1) == "<" ) {
            op = "&gt;=";
            constraint = constraint.substring(1);
         } else if (constraint.substring(0,1) == "=" ) {
            op = "!=";
            constraint = constraint.substring(1);
         } else {
            op = "!=";
         }
//alert( "op:"+op+":\nconstraint:"+constraint+":");
//         if (constraint.substring(0,1) ==  "=") {
//            op += "=";
//            constraint = constraint.substring(1);
//         }
      } else {
         if (constraint.substring(0,1) == ">" ) {
            op = "&gt;";
            constraint = constraint.substring(1);
         } else if (constraint.substring(0,1) == "<" ) {
            op = "&lt;";
            constraint = constraint.substring(1);
         } else {
            op = "=";
         }
         if (constraint.substring(0,1) ==  "=") {
            op += "=";
            constraint = constraint.substring(1);
         }
      }
      // constraint MUST just be a number at this point!
      constraint = deSci(constraint, scale);
      cons.push(cn + op + constraint);
   }
}

function makeVar(name, index) {
   return '<xsl:variable name="'+name+'">\n'+
      '<xsl:call-template name="Scientific">\n'+
      '<xsl:with-param name="Num" select="$fields['+index+']" />\n'+
      '</xsl:call-template>\n'+
//      '<xsl:call-template name="SciNum">\n'+
//      '<xsl:with-param name="num" select="$fields['+index+']" />\n'+
//      '</xsl:call-template>\n'+
//'<xsl:value-of select="$fields['+index+']" />\n'+
      '</xsl:variable>\n';
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

function zeroes(len) {
   var x = "";
   for (var i = 0; i<len; i += 1) {
      x += "0";
   }
   return x;
}
