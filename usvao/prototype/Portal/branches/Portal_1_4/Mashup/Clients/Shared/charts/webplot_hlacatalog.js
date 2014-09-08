// objet to load data from the HLA catalog interface into the plotting data model

function webplot_hlacatalog() {

    this.plots = [];

    this.addPlot = function(plot) {
        this.plots.push(plot);
        if (this.data) this.replot(plot);
    }
    
	this.setImage = function(imageName, catType, catName) {
	    var file="http://hladev.stsci.edu/HLA/Catalogs/HLAcat.aspx";
		var plotter = this;
		$.ajax({
			url: file,
			data: {CATALOG: catType, 
	            FORMAT: "VOTABLE", 
	            MAXOBJ: "40000", 
	            IMAGE: imageName },
			type: "GET",
			datatype: "text",
			success: function(xml){
				plotter.xml2jsonjs(imageName + " " + catName,xml);
			}
		});
	};
	



	this.xml2jsonjs= function(title,xml) {
		var json = $.xml2json(xml);

		var fields = json.RESOURCE.TABLE.FIELD;
		var f = new Array();
		var cols = new Array();
		for (var i = 0; i < fields.length; i++) {
			var id = fields[i].ID;
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