// objet to load data from the HLA catalog interface into the plotting data model

function webplot_hstcat() {

    this.plots = [];

    this.addPlot = function(plot) {
        this.plots.push(plot);
        if (this.data) this.replot(plot);
    }
    
	this.setImage = function(ra, dec, r) {
	    var file = "http://archtest.stsci.edu/hst/hla_cat/search.php";
		var plotter = this;
		$.ajax({
			url: file,
			data: {action: "Search",
			    outputformat: 'VOTable',
			    ordercolumn1: "",
			    selectedColumnsCsv:'matchid,matchra,matchdec,d,fluxaper2,apmag,totmag,ci,flags,ang_sep',
			    max_records:"50000",
			    makedistinct:"on",
			    RA: ra, 
	            DEC: dec, 
	            radius: r},
			type: "GET",
			datatype: "text",
			success: function(xml){
				plotter.xml2jsonjs("HST Catalog at " + ra + "," + dec,xml);
			}
		});
	};
	



	this.xml2jsonjs= function(title,xml) {
		var json = $.xml2json(xml);

		var fields = json.RESOURCE.TABLE.FIELD;
		var f = new Array();
		var cols = new Array();
		for (var i = 0; i < fields.length; i++) {
			var id = fields[i].name;
			var unit = fields[i].ucd;
			f.push(new Metadata(id,unit));
			cols.push(new Array());
		}

		var rows = json.RESOURCE.TABLE.DATA.TABLEDATA.TR;
		for (var i = 0; i < rows.length; i++) {
			var cs = rows[i].TD;
			for (var j = 0; j < cs.length; j++) {
				cols[j][i] = cs[j];
			}
		}
		
		xcol = 0;
		ycol = 1;
		
		for (var i = 2; i < f.length; i++) {
		    if (xcol == 0) {
		        if (f[i].title.toLowerCase().indexOf("apmag") != -1 || f[i].title.toLowerCase().indexOf("totmag") != -1 || f[i].title.toLowerCase().indexOf("magap") != -1) {
		            xcol = i;
		        }
		    }
		    else if (ycol == 1) {
		        if (f[i].title.toLowerCase().indexOf("apmag") != -1 || f[i].title.toLowerCase().indexOf("totmag") != -1 || f[i].title.toLowerCase().indexOf("magap") != -1) {
		            ycol = i;
		        }
		    }
		    else break;
		}

		this.data = new plotData(title,cols,f,xcol,ycol);
		this.replotAll();
	};
	
	this.replotAll = function() {
	    for (i = 0; i < this.plots.length; i++) {
            this.replot(this.plots[i]);
		}
	};
    
    this.replot = function(plot) {
        plot.setData(this.data);
		plot.showAllData();
		plot.redraw();    
	};
}