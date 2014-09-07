/*
	Requires:
		jquery.js
		jquery.flot.js

	Originally based on some code found in ...
		http://aegis.ucolick.org/products/MTR/explore.html
	but seriously altered
*/

function Graph(options) {
	var self =  this;	/* remember where we came from */
	this.points = [];
	this.data = [];
	this.points_selected = [];
	this.indices_selected = [];
	this.options = jQuery.extend({
		color:	'#000000',	// doesnt seem to work
		points:    { 
			show: true,
			fillColor: '#00FF00' 
		},
		grid: { clickable: true },
		selection: { mode: "xy" }
	},options);

	this.selectxslt =
		'<?xml version="1.0" encoding="UTF-8"?>\n'+
		'<xsl:stylesheet version="1.0"' +
		'	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"' +
		'	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" ' +
		'	xmlns:v1="http://vizier.u-strasbg.fr/VOTable"' +
		'	xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"' +
		'	xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"' +
		'	xsl:exclude-result-prefixes="vo v1 v2 v3">\n' +
		'<xsl:output method="html" />\n' +
		'<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|v2:FIELD|v3:FIELD" />\n' +
		'<xsl:template match="/" >\n' +
		'	<xsl:for-each select="$fieldlist">\n' +
		'  	<xsl:if test="contains(\'|float|int|double|\',concat(\'|\',@datatype,\'|\'))" >\n' +  
		'			<option name="{@name}"><xsl:value-of select="@name" /></option>\n' +
		'		</xsl:if>\n' +
		'	</xsl:for-each>\n' +
		'</xsl:template>\n' +
		'</xsl:stylesheet>\n';

	this.getColumns = function() {
		try {
			var xsltp      = new XSLTProcessor();
			var xsltDom    = Sarissa.getDomDocument();
			xsltDom        = (new DOMParser()).parseFromString(this.selectxslt, "text/xml");  
			xsltp.importStylesheet(xsltDom);
			var newDoc     = xsltp.transformToFragment(opener.rd.filter.getDocument(), document);
			var opts = new XMLSerializer().serializeToString(newDoc);
		} catch  (e) {
			alert("Exception creating Graph:"+e);
		}
		return opts;
	};

	this.setSelectors = function () {
		var numericColumns = this.getColumns();
		$('select#x').html(numericColumns);
		$('select#y').html(numericColumns);
		$('select.axis').each(function() {
			$(this).change( function() {
				self.getDataFromXML();
			});
		});
	};

	this.getDataFromXML = function() {
		var dataxslt =
			'<?xml version="1.0" encoding="UTF-8"?>\n'+
			'<xsl:stylesheet version="1.0"' +
			'	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"' +
			'	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" ' +
			'	xmlns:v1="http://vizier.u-strasbg.fr/VOTable"' +
			'	xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"' +
			'	xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"' +
			'	xsl:exclude-result-prefixes="vo v1 v2 v3">\n' +
			'<xsl:output method="text" />\n' +
			'<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|v2:FIELD|v3:FIELD" />\n' +
			'<xsl:template match="/" >\n' +
			'	<xsl:value-of select="string(\'[\')"    />\n' +
			'	<xsl:text>&#xA;</xsl:text>\n' +
			'	<xsl:for-each select="//TR|//vo:TR|//v1:TR|//v2:TR|//v3:TR">\n' +
			'		<xsl:value-of select="string(\'{\')" />\n' +
			'		<xsl:for-each select="TD|vo:TD|v1:TD|v2:TD|v3:TD">\n' +
			'			<xsl:variable name="column" select="position()"/>\n' +
			'			<xsl:variable name="fname" select="$fieldlist[position()=$column]/@name"/>\n' +
			'			<xsl:variable name="ucd" select="$fieldlist[position()=$column]/@ucd"/>\n' +
			'			<xsl:if test="contains(\'/ID_MAIN/\',concat(\'/\',$ucd,\'/\')) or '+
			'					contains(\'/'+$("select#x").val()+'/'+$("select#y").val()+'/\',concat(\'/\',$fname,\'/\'))" >\n' +
			'				<xsl:value-of select="$fname" />\n' +
			'				<xsl:value-of select="string(\':\')" />\n' +
			'				<xsl:value-of select="string(\'&quot;\')" />\n' +
			'				<xsl:value-of select="." />\n' +
			'				<xsl:value-of select="string(\'&quot;\')" />\n' +
			'				<xsl:value-of select="string(\',\')" />\n' +
			'			</xsl:if>\n' +
			'		</xsl:for-each>\n' +
			'		<xsl:value-of select="string(\'}\')" />\n' +
			'		<xsl:if test="position() != last()">, </xsl:if>\n' +
			'		<xsl:text>&#xA;</xsl:text>\n' +
			'	</xsl:for-each>\n' +
			'	<xsl:value-of select="string(\']\')" />\n' +
			'</xsl:template>\n' +
			'</xsl:stylesheet>\n';

		try {
			var xsltp      = new XSLTProcessor();
			var xsltDom    = Sarissa.getDomDocument();
			xsltDom        = (new DOMParser()).parseFromString(dataxslt, "text/xml");  
			xsltp.importStylesheet(xsltDom);
			var newDoc     = xsltp.transformToFragment(opener.rd.filter.getDocument(), document);
			//	eval is not the best solution.  Apparently, eval is evil! 
			// but this returns a javascript data structure so it works well here.
			var d = eval(new XMLSerializer().serializeToString(newDoc));
		} catch  (e) {
			alert("Exception creating Graph:"+e);
		}

		if ( d ) {
			this.data = d;
			x=$("select#x").val();
			y=$("select#y").val();
			this.points = [];
			for ( var i = 0; i < d.length; i++) 
				this.points.push( [ parseFloat(d[i][x] ), parseFloat(d[i][y] ) ] );
			this.draw();
		}
	};

	this.draw = function(){
		this.points_selected = [];
		for ( var i = 0; i < self.indices_selected.length; i++) 
			this.points_selected.push( [ self.points[self.indices_selected[i]][0], self.points[self.indices_selected[i]][1] ] );
		if ( this.points ) {
			$.plot($(this.options.target), [
					{data:this.points}
					,{data:this.points_selected}
				] , this.options
			);

			if ( $('#selected').length ) {
				$('#selected').html("<table>");
				$('#selected > table').append( "<thead>" );
				$('#selected > table > thead').append( "<tr><th colspan='2'>Selected:</th></tr>" );
//				$('#selected > table > thead').append( "<tr><th>Key</th><th>Value</th></tr>" );
				$('#selected > table').append( "<tbody>" );
//				for (var key in this.data[this.point_selected]) {
//					$('#selected > table > tbody').append( "<tr><td>"+key+"</td>" +
//						"<td>"+this.data[this.point_selected][key]+"</td></tr>" );
//$(opener.document.body).find("table.data tr td.unique_id:contains("+this.data[this.point_selected]['unique_id']+")").parent().addClass('selectedimage');

				for(var i = 0; i < this.points_selected.length; i++) {
//						$('#selected > table > tbody').append( "<tr><td>"+this.data[this.indices_selected[i]]['unique_id']+"</td></tr>" );
$(opener.document.body).find("table.data tr td.unique_id:contains("+this.data[this.indices_selected[i]]['unique_id']+")").parent().addClass('selectedimage');
				}
			}
		} else {
			alert('No data to draw!');
		}
	};

	$(this.options.target).bind("plotclick", function (e, pos) {
		// the values are in pos.x and pos.y
		var min = Math.pow(pos.x - self.points[0][0],2) + Math.pow(pos.y - self.points[0][1],2);
		var ix = 0;
		for(var i = 1; i < self.points.length; i++) {
			var dist = Math.pow(pos.x - self.points[i][0],2) + Math.pow(pos.y - self.points[i][1],2);
			if(min > dist) { 
				min = dist;
				ix = i;
				if(min === 0.0) { break; }
			} 
		}
		if ( self.indices_selected.unique_push( ix ) ) self.draw();
	});

	$(this.options.target).bind("selected", function(event,area){
		// x1 <= x2
		// y1 <= y2
		var newpoints = 0;
		for(var i = 0; i < self.points.length; i++) {
			if ( ( self.points[i][0] < area.x2 )
				&& ( self.points[i][0] > area.x1 )
				&& ( self.points[i][1] > area.y1 )
				&& ( self.points[i][1] < area.y2 ) ) {
				if ( self.indices_selected.unique_push ( i ) )
					newpoints++;
			}
		}
		if (newpoints) self.draw();
	});

	this.setSelectors();
	this.getDataFromXML();
};

Array.prototype.contains = function (element) {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == element) {
			return i+1;	// return 1 greater because can be at 0 which is actually false
		}
	}
	return false;
};

Array.prototype.unique_push = function(element) {
	if ( !this.contains(element) ) {
		this.push(element);
		return true;
	} else {
		return false;
	}
};

Array.prototype.remove = function(element) {
	if ( where = this.contains(element) ) {
		this.splice(where-1,1);
	}
};
