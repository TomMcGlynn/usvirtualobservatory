function Colorbar(bwidth, bheight) 
{
	
	this.colors;
	this.values;
	this.nelem;
	this.labels;
	if (bwidth == null) {
		bwidth = 300;
	}
	if (bheight == null) {
		bheight = 5;
	}

	function osetElements(elem) {
		nelem = Math.round(elem);
	}

	function osetColors(newColors) {
		colors = newColors;
	}

	function osetCounts(counts) {
		values = counts;
	}

	function osetLabels(xlabels) {
		labels = xlabels;
	}

	function ogetBar() {

		if (colors == null || values == null || nelem == null) {
			return "";
		}
		if (colors.length != values.length || colors.length < 1 || nelem <= 0) {
			return "";
		}
		
		var str = "<table width='" + bwidth
				+ "' noborder  cellspacing=0><tr>";
		var curr = 0;		
		var cu = 0;
	        for (; curr < values[0]; curr += 1) 
	 	{
				str += "<td height=" + bheight + " bgcolor='" + colors[0]
						+ "'/>";
		}
		for (; cu < values[1]; cu++)
		{
				str += "<td height=" + bheight + " bgcolor='" + colors[1]
						+ "'/>";
		}
		str += "</tr></table>";
		return str;
	}
	this.setElements = osetElements;
	this.setCounts = osetCounts;
	this.setColors = osetColors;
	this.getBar = ogetBar;
	this.setLabels = osetLabels;
}
function loadColorbar()
{
	var statsvalue = document.getElementById('no').getAttribute("stats");
	var array = statsvalue.split(':');
	var statLabels = [
                  "<a href='javascript: void 0' onclick='return chng(\"matches\")'>Data found</a>",
                  "<a href='javascript: void 0' onclick='return chng(\"nonmatches\")'>No data </a>",
		 "<a href='javascript: void 0' onclick='return chng(\"nonmatches\")'>Error data </a>",
	          		
                  ];

	var bar = new Colorbar(200,6);
	
	var total = (array[0]*1) + (array[1]*1);
	//alert ("C is: " + total);
	bar.setColors(["green", "violet","blue"]);
	bar.setElements(100);
	bar.setLabels(statLabels);
	var pass = 100*(array[0]*1/total);
        bar.setCounts([array[0],array[1],'0']);
        if (pass)
        {
	  var t = "<tr><td>"+bar.getBar()  +  pass.toFixed(2) + "% passing</td></tr>";
	  setText('no',t);
        }
}
function setText(id,text) 
{
	var elem = document.getElementById(id);
	elem.innerHTML = text;	
}
