function Colorbar(bwidth, bheight) {
	this.colors;
	this.values;
	this.nelem;
	this.labels;
	if (bwidth == null) {
		bwidth = 400;
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

		var index = new Array(values.length);
		index[0] = values[0];
		if (index[0] < 0) {
			index[0] = 0;
		}
		for ( var i = 1; i < index.length; i += 1) {
			index[i] = index[i - 1];
			if (values[i] > 0) {
				index[i] += values[i];
			}
		}

		var max = index[index.length - 1];
		if (max == 0) {
			return "";
		}

		var str = "<table width='" + bwidth
				+ "' noborder cellpadding=0 cellspacing=0><tr>";
		var curr = 0;
		for ( var j = 0; j < index.length; j += 1) {
			var bound = Math.round(nelem * index[j] / max);
			for (; curr < bound; curr += 1) {
				str += "<td height=" + bheight + " bgcolor='" + colors[j]
						+ "'/>";
			}
		}
		str += "</tr></table>";
		if (labels != null && labels.length == values.length) {

			str += "<table width='400' noborder><tr>";
			for ( var k = 0; k < labels.length; k += 1) {
				str += "<td align=center><font color='" + colors[k] + "'>"
						+ labels[k] + "(" + values[k] + ")" + "</font></td>";
			}
			str += "</tr></table>";
		}
		return str;
	}
	this.setElements = osetElements;
	this.setCounts = osetCounts;
	this.setColors = osetColors;
	this.getBar = ogetBar;
	this.setLabels = osetLabels;
}
