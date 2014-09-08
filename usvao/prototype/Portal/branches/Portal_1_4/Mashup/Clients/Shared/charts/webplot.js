/*
Array.prototype.min = function() {
	var r = this[0];
	this.forEach(function(v,i,a){if (v<r) r=v;});
	return r;
};

Array.prototype.max = function() {
	var r = this[0];
	this.forEach(function(v,i,a){if (v>r) r=v;});
	return r;
};
*/

CanvasRenderingContext2D.prototype.clear = 
	CanvasRenderingContext2D.prototype.clear || function (preserveTransform) {
	this.setTransform(1, 0, 0, 1, 0, 0);
	this.clearRect(0, 0, this.canvas.width, this.canvas.height);
	this.restore();
};
// the object to hold on to what is to be plotted
function plotData(title, data, metadata, defaultX, defaultY) {
	this.title = title;
	this.selected = [];
	this.xcalcmeta = null;
	this.ycalcmeta = null;
	this.xcalc = [];
	this.ycalc = [];
	this.getXData = function() {
	    if (this.xcalc.length == 0) 
		    return this.dataarray[this.xColumn];
		return this.xcalc;
	};
	this.getYData = function() {
		if (this.ycalc.length == 0) 
		    return this.dataarray[this.yColumn];
		return this.ycalc;
	};
	this.getSelected = function() {
		return this.selected;
	};
	this.getXMeta = function() {
	    if (this.xcalc.length > 0) return this.xcalcmeta;
		return this.metadata[this.xColumn];
	};
	this.getYMeta = function() {
	    if (this.ycalc.length > 0) return this.ycalcmeta;
		return this.metadata[this.yColumn];
	};
	this.setData = function(data, metadata){
		this.setData(data,metadata,0,1);
	};
	this.setData = function(data,metadata, defaultX, defaultY){
		this.dataarray = data;
		this.metadata = metadata;
		this.xColumn = defaultX;
		this.yColumn = defaultY;
		this.defaultX = defaultX;
		this.defaultY = defaultY;
        this.deselectAll();
        this.xMin = 1*Ext.Array.min(this.getXData());
        this.xMax = 1*Ext.Array.max(this.getXData());
		
		this.yMin = 1*Ext.Array.min(this.getYData());
		this.yMax = 1*Ext.Array.max(this.getYData());
		

	};
	this.setXColumn = function(col) {
		this.xColumn = col;
		this.xcalc = [];
		this.xcalcmeta=null;
		this.xMin = 1*Ext.Array.min(this.getXData());
		this.xMax = 1*Ext.Array.max(this.getXData());

	}
	this.setYColumn = function(col) {
		this.yColumn = col;
		this.ycalc = [];
		this.ycalcmeta=null;
		this.yMin = 1*Ext.Array.min(this.getYData());
		this.yMax = 1*Ext.Array.max(this.getYData());

	}
	this.calcXColumn = function(x1, funct, x2) {
	    this.xColumn = -1;
	    this.xcalc = this.doColumCalc(x1,funct,x2);
	    this.xMin = 1*Ext.Array.min(this.getXData());
	    this.xMax = 1*Ext.Array.max(this.getXData());
		this.xcalcmeta = new Metadata(this.metadata[x1].title+funct+this.metadata[x2].title,this.metadata[x1].unit+funct+this.metadata[x2].unit);
	}
	this.calcYColumn = function(y1, funct, y2) {
	    this.yColumn = -1;
	    this.ycalc = this.doColumCalc(y1,funct,y2);
	    this.yMin = 1*Ext.Array.min(this.getYData());
		this.yMax = 1*Ext.Array.max(this.getYData());
		this.ycalcmeta = new Metadata(this.metadata[y1].title+funct+this.metadata[y2].title,this.metadata[y1].unit+funct+this.metadata[y2].unit);
	}
	
	this.doColumCalc = function(x1,funct,x2) {
	    xa1 = this.dataarray[x1];
	    xa2 = this.dataarray[x2];
	    ret = [];
	    for (i = 0; i < xa1.length; i++) {
	        switch(funct)  {
	            case '+':
	                ret[i] = xa1[i] + xa2[i];
	                break;
	            case '-':
	                ret[i] = xa1[i] - xa2[i];
	                break;
	            case '*':
	                ret[i] = xa1[i] * xa2[i];
	                break;
	            case '/':
	                ret[i] = xa1[i] / xa2[i];
	                break;
	        }
	    }
	    return ret;
	}
	
	this.selectByRange = function(xmin,ymin,xmax,ymax) {
        if (xmin > xmax) {
            a = xmin;
            xmin = xmax;
            xmax = a;
        }
        if (ymin > ymax) {
            a = ymin;
            ymin = ymax;
            ymax = a;
        }
        //alert(xmin+","+ymin+" " + xmax+","+ymax);
	    for (i = 0; i < this.getXData().length; i++) {
	        x=this.getXData()[i];
	        y=this.getYData()[i];
	        val = x >= xmin && x <= xmax && y >= ymin && y <= ymax;
	        this.setSelectedPoint(i,val);
	    }
	}
	this.setSelectedPoint = function(n,val) {
	    this.selected[i]=val;
	}
	this.deselectAll = function () {
	    for (i = 0; i < this.getXData().length; i++) {
	        this.selected[i]=false;
		}
	}
	
	this.getNearestPoint = function(x,y) {
	    xarray=this.getXData();
	    yarray=this.getYData();
	    bestr = Number.MAX_VALUE; 
	    best = -1;
	    for (col=0; col < xarray.length; col++) {
	        dx=xarray[col]-x;
	        dy=yarray[col]-y;
	        r=dx*dx+dy*dy;
	        if (bestr > r) {
	            bestr = r
	            best = col;
	        }
	    }
	    return best;
	} 
	
	this.setData(data,metadata,defaultX,defaultY);
}
// the metadata object for each column of data
function Metadata(title,unit) {
	this.title = title;
	this.unit = unit;
}

function webplot(divName) {

	
	mouseAction = function(event) {

		cursorContext = event.target.getContext('2d');
		plot = event.data.plot;
		
		if (!plot.data) return;

		pos = getMouse(event,event.target);
		var x = pos['x'];
		var y = pos['y'];
		var width = event.target.width;
		var height = event.target.height;


        if (event.type == 'mousedown' && event.altKey) {
            pt = plot.data.getNearestPoint(plot.xToData(x),plot.yToData(y));
            str = '';
            for(i = 0; i < plot.data.metadata.length; i++) {
                lab = plot.data.metadata[i].title + '(' + plot.data.metadata[i].unit+')';
                val = plot.data.dataarray[i][pt];
                str += lab + ": " + val + '\n';
            }
            
            alert(str);
            return;
        }

		if (event.type == 'mousedown') {
			plot.dragging = true;			
			plot.startx = x;
			plot.starty = y;
			plot.dragging = true;

		}

		if (event.type == 'mouseleave') {
   			cursorContext.clear();
		    setCursorByID('auto');
		}
		if (event.type == 'mouseup') {
			if (plot.dragging && (x != plot.startx && y != plot.starty)) {
				if (!event.shiftKey) {
				    plot.setZoom(plot.xToData(x),plot.yToData(y),plot.xToData(plot.startx),plot.yToData(plot.starty));
				} else {
				    plot.data.selectByRange(plot.xToData(x),plot.yToData(y),plot.xToData(plot.startx),plot.yToData(plot.starty));
				    plot.redraw();
				}
			}
			plot.dragging = false;
		}
		if (event.type == 'mousemove' || event.type == 'mouseup') {

			cursorContext.clear();
            if ($('#'+plot.configId).is(':visible')) return;
			cursorContext.strokeStyle = "rgb(200,0, 0)";

			cursorContext.lineWidth=1.0;
			cursorContext.beginPath();
			cursorContext.moveTo(x,0);
			cursorContext.lineTo(x,height);
			cursorContext.stroke();
			cursorContext.moveTo(0,y);
			cursorContext.lineTo(width,y);
			cursorContext.stroke();
			cursorContext.font = '10pt Arial';
			cursorContext.fillStyle = "red";
			cursorContext.textAlign = "right";
			ypos=30;
			xpos=width - 15;
			pos = ' ' + formatNumber(plot.xToData(x),plot.xmin,plot.xmax)+ ' , ' + formatNumber(plot.yToData(y),plot.ymin,plot.ymax);
			cursorContext.fillText(pos,xpos,ypos); 


			if (plot.dragging) {

				if (event.shiftKey) {
				    cursorContext.strokeStyle = "rgb(200,0,200)";
				} else {
				    cursorContext.strokeStyle = "rgb(0,200,200)";
				}
				cursorContext.strokeRect(plot.startx,plot.starty,x-plot.startx,y-plot.starty);
			}
		}
		if (event.type == "mouseenter") {
		    setCursorByID('crosshair');
		}

	};


	plotMouseAction = function(event) {
		plot = event.data.plot;

		if (plot.data.xMin == 0 || plot.data.xMax == 0 ||  plot.data.xMin / plot.data.xMax <= 0) 
		    $("#"+plot.xConfigLog).attr("disabled", true);
		else 
		    $("#"+plot.xConfigLog).removeAttr("disabled");
		if (plot.data.yMin == 0 || plot.data.yMax == 0 ||  plot.data.yMin / plot.data.yMax <= 0) 
		    $("#"+plot.yConfigLog).attr("disabled", true);
		else 
		    $("#"+plot.yConfigLog).removeAttr("disabled");
		if ($('#'+plot.configId).is(':visible')) {
			$('#'+plot.configId).hide();
		} else {
			$('#'+plot.configId).show();
		}
	};
	



	this.busy = function() {
		$("#"+this.waitingId).show();
		setCursorByID('progress');
	};
	this.done = function() {
		$("#"+this.waitingId).hide();
		setCursorByID('auto');
	};


	
	// these are the methods the config panel uses to change the view
	
	this.setZoom = function(xmin,ymin,xmax,ymax) {
		if (xmin < xmax) {
			this.xmin = xmin;
			this.xmax = xmax;
		} else {
			this.xmin = xmax;
			this.xmax = xmin;		
		}
		if (ymin < ymax) {
			this.ymin = ymin;
			this.ymax = ymax;
		} else {
			this.ymin = ymax;
			this.ymax = ymin;		
		}
		
		this.zoomed=true;
		$("#"+this.xminText).val(this.xmin);
		$("#"+this.xmaxText).val(this.xmax);
		$("#"+this.yminText).val(this.ymin);
		$("#"+this.ymaxText).val(this.ymax);
		this.redraw();
	};
	
	this.acceptConfig = function() {
		$("#"+this.configId).hide();
		this.zoomed = true;

		
		xsel = $('#'+this.xSelectId + " option:selected").val();
		ysel = $('#'+this.ySelectId + " option:selected").val();
		xlog = $('#'+this.xConfigLog).is(':checked');
		ylog = $('#'+this.yConfigLog).is(':checked');
		
		this.setXLog(xlog);
		this.setYLog(ylog);
		
		replotit = false;		
		if (xsel == 'xmath' || xsel != this.data.xColumn) {
		    if(xsel == 'xmath') {
		        x1 = $('#'+this.xFirstMathSelectId + " option:selected").val();
		        x2 = $('#'+this.xSecondMathSelectId + " option:selected").val();
		        f = $('#'+this.xFunctionMathSelectId + " option:selected").val();
		        this.data.calcXColumn(x1,f,x2);
		    } else {
			    this.data.setXColumn(xsel);
			}
		    replotit = true;
		} 
		
		if (ysel == 'ymath' || ysel != this.data.yColumn) {
			if(ysel == 'ymath') {
		        y1 = $('#'+this.yFirstMathSelectId + " option:selected").val();
		        y2 = $('#'+this.ySecondMathSelectId + " option:selected").val();
		        f = $('#'+this.yFunctionMathSelectId + " option:selected").val();
		        this.data.calcYColumn(y1,f,y2);
		    } else {
			    this.data.setYColumn(ysel);
			}
       		replotit = true;
		} 
		if (replotit) this.showAllData();
		this.redraw();
		this.lastXmin = $("#"+this.xminText).val();
		this.lastXmax = $("#"+this.xmaxText).val();
		this.lastYmin = $("#"+this.yminText).val();
		this.lastYmax = $("#"+this.ymaxText).val();

	};

    this.acceptMathConfig = function() {
        $('#'+this.mathBoxId).hide();
    }

	this.cancelConfig = function() {
		$("#"+this.configId).hide();
		$('#'+this.xSelectId).val(this.data.xColumn);
		$('#'+this.ySelectId).val(this.data.yColumn);
		$("#"+this.xminText).val(this.lastXmin);
		$("#"+this.xmaxText).val(this.lastXmax);
		$("#"+this.yminText).val(this.lastYmin);
		$("#"+this.ymaxText).val(this.lastYmax);
		$('#'+this.xConfigLog).prop("checked", this.xlog);
		$('#'+this.xConfigLog).prop("checked", this.ylog);

		this.redraw();
	};
	
	this.cancelMathConfig = function() {
	    // should get the value here and figure out which column to reset but for now reset them all
	    $('#'+this.xSelectId).val(this.data.xColumn);
		$('#'+this.ySelectId).val(this.data.yColumn);
	    $('#'+this.mathBoxId).hide();
    }
	
	setConfigOptions = function(event) {
		plot = event.data.plot;

		xsel = $('#'+plot.xSelectId + " option:selected").val();
		ysel = $('#'+plot.ySelectId + " option:selected").val();
		if (xsel == 'xmath') {
            $('#'+plot.xMathTableId).show();
        } else {
            $('#'+plot.xMathTableId).hide();
        }		    
		if (ysel == 'ymath') {
            $('#'+plot.yMathTableId).show();
        } else {
            $('#'+plot.yMathTableId).hide();
        }		    
    };
	this.revertZoom = function() {
		$("#"+this.configId).hide();
		$('#'+this.xSelectId).val(this.data.xColumn);
		$('#'+this.ySelectId).val(this.data.yColumn);
		this.showAllData();
		this.redraw();
		this.lastXmin = $("#"+this.xminText).val();
		this.lastXmax = $("#"+this.xmaxText).val();
		this.lastYmin = $("#"+this.yminText).val();
		this.lastYmax = $("#"+this.ymaxText).val();
	};


    this.showAllData = function() {
        dx = Math.abs(0.02*(this.data.xMax-this.data.xMin));
        $("#"+this.xminText).val(this.data.xMin-dx);
 		$("#"+this.xmaxText).val(this.data.xMax+dx);
        dy = Math.abs(0.02*(this.data.yMax-this.data.yMin));
 		$("#"+this.yminText).val(this.data.yMin-dy);
 		$("#"+this.ymaxText).val(this.data.yMax+dy);
 	}

    // redraw the plot with current columns zoom and log settings
	this.redraw = function() {



        this.xmin = 1*$("#"+this.xminText).val();
        this.xmax = 1*$("#"+this.xmaxText).val();
        this.ymin = 1*$("#"+this.yminText).val();
        this.ymax = 1*$("#"+this.ymaxText).val();

		this.xscale = (this.xmax - this.xmin) / this.plotWidth; 
		this.yscale = (this.xmax - this.xmin) / this.plotHeight;


		frameContext = $('#'+this.frameId)[0].getContext('2d');
		frameContext.clear();

		pointsContext = $('#'+this.pointsId)[0].getContext('2d');
		pointsContext.clear();

		cursorContext = $('#'+this.cursorId)[0].getContext('2d');
		cursorContext.clear();

		fitContext = $('#'+this.fitId)[0].getContext('2d');
		fitContext.clear();

		// start drawing on layer1


		frameContext.font = '10pt Arial';
		frameContext.fillStyle = "blue";
		plottitle = $("#title").text();
		frameContext.textAlign = "left";
		ypos=30;
		xpos=this.pad+15;


		frameContext.fillText(this.data.title,xpos,ypos); 
		ypos += 15;
		frameContext.fillText("Drag to zoom",xpos,ypos); 
        ypos += 15;
		frameContext.fillText("Shift+Drag to select points",xpos,ypos);

		this.drawAxes();

		this.drawTicks(this.xmin,this.xmax, this.xscale,7,true);
		this.drawTicks(this.ymin,this.ymax, this.yscale, 4,false);

		this.plotdata();
		this.done();
	};

    // geometric transformations from data to screen
	this.xToData = function(x){
	    w = this.plotWidth;
        val = this.screenToData(x/w,this.xmin,this.xmax,this.xlog)
        return val;
    };
	this.yToData = function(y){
	    h = this.plotHeight;
	    val = (this.screenToData(y/h,this.ymax,this.ymin,this.ylog));
        return val; 
	};
	this.screenToData = function(sc,min,max, log){
		if (log) {
		    if (min == 0 || max == 0) return null;
		    lm = log10(min);
		    lmm = log10(max);
		    n = lm + sc*(lmm - lm);
		    n = Math.pow(10.0,n);
		} else {
    		n = min + sc*(max-min);
    	}
		return n;
	};

	this.xToScreen = function(x){
	    w = this.plotWidth;
        val = this.dataToScreen(x,this.xmin,this.xmax, this.xlog);
        return w * val;
	};
	this.yToScreen = function(y){
        h = this.plotHeight;
        val = this.dataToScreen(y,this.ymax,this.ymin,this.ylog);
        return h * val;
	};
	this.dataToScreen = function(n,min,max,log) {
	    if (log) {
	        if (n == 0) return null;
	        lm = log10(min);
		    lmm = log10(max);
	        sc = (log10(n)-lm)/(lmm-lm);
	    } else {
		    sc = (n-min)/(max - min);
		}
		return sc;
	};

	this.xArrayToScreen = function(array) {
		var newArray = new Array();
		for (var i=0; i < array.length; i++) {
			newArray[i]=this.xToScreen(array[i]);
		}
		return newArray;
	};
	this.yArrayToScreen = function(array) {
		var newArray = new Array();
		for (var i=0; i < array.length; i++) {
			newArray[i]=this.yToScreen(array[i]);
		}
		return newArray;
	};
	this.pointToScreen = function(x,y) {
		return [this.xToScreen(x),this.yToScreen(y)];
	};


    this.setXLog = function(bool) {
        this.xlog = bool;
    }

    this.setYLog = function(bool) {
        this.ylog = bool;
    }
    // draws the points in the plot canvas (does not plot axes)
	this.plotdata = function() {
		pointsContext = $('#'+this.pointsId)[0].getContext('2d');

		pointsContext.lineWidth=1.0;
		x = this.xArrayToScreen(this.data.getXData());
		y = this.yArrayToScreen(this.data.getYData());
		s = this.data.getSelected();
		var asize = x.length;

        pointsContext.fillStyle = "#8ED6FF";
        for (var i=0; i<asize; i++) {
            drawPointHere(x[i],y[i],s[i],0);

        }
	};
	
	var colors = ["aqua","black","green","purple","orange","magenta","darkslategrey"];

		
	drawPointHere = function(x,y,s,type) {

        if (type < colors.length) pointsContext.fillStyle = colors[type];
        else pointsContext.fillStyle = colors[0];
//         pointsContext.fillStyle=pointsContext.strokeStyle;
        pointsContext.beginPath();
        if (s) pointsContext.fillStyle = "red";
	    switch(type){
	        case 0:
	            pointsContext.arc(x, y, 3, 0 , 2 * Math.PI, false);
	            pointsContext.fill();
                pointsContext.stroke();
                pointsContext.closePath();
                break;
            case 1:
                pointsContext.moveTo(x,y-3);
                pointsContext.lineTo(x,y+3);
                pointsContext.stroke();
                pointsContext.closePath();
                pointsContext.beginPath();
                pointsContext.moveTo(x-3,y);
                pointsContext.lineTo(x+3,y);
                pointsContext.stroke();
                pointsContext.closePath();
                break;
            case 2:
                pointsContext.moveTo(x-3,y-3);
                pointsContext.lineTo(x+3,y+3);
                pointsContext.stroke();
                pointsContext.closePath();
                pointsContext.beginPath();
                pointsContext.moveTo(x-3,y+3);
                pointsContext.lineTo(x+3,y-3);
                pointsContext.stroke();
                pointsContext.closePath();
                break;
            case 3:
                pointsContext.moveTo(x-3,y-3);
                pointsContext.lineTo(x,y+3);
                pointsContext.lineTo(x+3,y-3);
                pointsContext.lineTo(x-3,y-3);
                pointsContext.fill();
                pointsContext.stroke();
                pointsContext.closePath();
                break;
            default:
                pointsContext.rect(x-3,y-3,6,6);
                pointsContext.fill();
                pointsContext.stroke();
                pointsContext.closePath();
                break;
	    }

	}

    // draws the tickmarks and axes labels on in the frame canvas
	this.drawTicks = function(min ,max, scale, numticks, isX) {
		// draw horizontal axes tick marks
		// use screen coordinates

		var dx = Math.abs((max-min)/2);
		var prec = log10(dx);
		prec = prec - (prec % 1)  ;
		prec = Math.pow(10,prec);
		
		// if you have zero...it has to be a major point
		hasZero = (min <= 0 && max >=0);
		


		var ntix = 2*dx/prec;

		while (ntix < 4) {
			prec /= 2;
			ntix *= 2;
		}
		while (ntix >= 8) {
			prec *= 2;
			ntix /= 2;
		}
		var ticks = [];
		small = min;
		large = max;
		if (large < small) {
			t = large;
			large=small;
			small = t;
		}

		if (!hasZero) {
			var minpt = min - (min % prec);
			for (var i = 0; i <= 20; i++) {

				var tick = minpt + prec * (i-10);
				ticks[i] = tick;
			}
		} else {
			for (var i = 0; i < 12; i++) {
				var tick = i * prec;
				ticks[i] = tick;
			}
			for (var i = 0; i < 12; i++) {
				var tick = -i * prec;
				ticks[i+12] = tick;
			}
		}

		var pos = [];
		// remember the frameContext has the x padding so insert the pad offset
		for (var i = 0; i < ticks.length; i++) {
			if (isX) pos[i] = this.xToScreen(ticks[i]) + this.pad;
			else pos[i] = this.yToScreen(ticks[i]);
		}
		var nMinor = 5;
		if (prec % 2 == 0) nMinor = 4;
		
		var dprec = parseInt(log10(dx)+1);
		if (dprec < 1) log10(dx)/+1;
		if (dprec == 0) dprec =1;

		frameContext=$('#'+this.frameId)[0].getContext('2d');
		height = this.plotHeight;
		width = this.plotWidth;
		frameContext.font = '12pt Arial';
		frameContext.fillStyle = "blue";
		frameContext.textAlign = "center";

		if (isX) {
			title = this.data.getXMeta().title+" ("+this.data.getXMeta().unit+")";
			for (var i = 0; i < pos.length; i++) {
				if (pos[i] > this.pad && pos[i] < width + this.pad) {
					frameContext.beginPath();
					frameContext.moveTo(pos[i],0);
					frameContext.lineTo(pos[i],10);
					frameContext.stroke();
					frameContext.closePath();
					frameContext.beginPath();
					frameContext.moveTo(pos[i],height);
					frameContext.lineTo(pos[i],height-10);
					frameContext.stroke();
					frameContext.closePath();
					frameContext.fillText(""+formatNumber(ticks[i],plot.xmin,plot.xmax),pos[i],height+20);
				}
				var minc = prec / nMinor;
				for (var j = 1; j <= nMinor; j++) {
					var pos2 = this.xToScreen(ticks[i]+j*minc) + this.pad;
					if (pos2 > this.pad && pos2 < width+this.pad) {
						frameContext.beginPath();
						frameContext.moveTo(pos2,0);
						frameContext.lineTo(pos2,5);
						frameContext.stroke();
						frameContext.closePath();
						frameContext.beginPath();
						frameContext.moveTo(pos2,height);
						frameContext.lineTo(pos2,height-5);
						frameContext.stroke();
						frameContext.closePath();
					}
				}

			}
			frameContext.textAlign = "center";
			frameContext.fillText(title,(width+this.pad)/2,height+40);
		} else {
			title = this.data.getYMeta().title +" ("+this.data.getYMeta().unit+")";
			var minc = prec / nMinor;
			for (var i = 0; i < pos.length; i++) {
				if (pos[i] > 0 && pos[i] < height) {
					frameContext.beginPath();
					frameContext.moveTo(this.pad,pos[i]);
					frameContext.lineTo(this.pad+10,pos[i]);
					frameContext.stroke();
					frameContext.closePath();
					frameContext.beginPath();
					frameContext.moveTo(width+this.pad,pos[i]);
					frameContext.lineTo(width-10+this.pad,pos[i]);
					frameContext.stroke();
					frameContext.closePath();
					frameContext.stroke();
					frameContext.closePath();
					frameContext.rotate(-1*Math.PI/2);
					frameContext.fillText(formatNumber(ticks[i],plot.ymin,plot.ymax),-pos[i],40);
					frameContext.rotate(Math.PI/2);
				}
				for (var j = 1; j <= nMinor; j++) {
					var pos2 = this.yToScreen(ticks[i]+j*minc);
					if (pos2 > 0 && pos2 < height ) {
						frameContext.beginPath();
						frameContext.moveTo(this.pad,pos2);
						frameContext.lineTo(this.pad+5,pos2);
						frameContext.stroke();
						frameContext.closePath();
						frameContext.beginPath();
						frameContext.moveTo(width+this.pad,pos2);
						frameContext.lineTo(width-5+this.pad,pos2);
						frameContext.stroke();
						frameContext.closePath();
					}
				}


			}
			frameContext.textAlign = "center";
			frameContext.rotate(-1*Math.PI/2);
			frameContext.fillText(title,-height/2,15);
			frameContext.rotate(Math.PI/2);

		}

	};
    // draws the box for the plot...no ticks or axes labels
	this.drawAxes = function() {
		// draw 4 sided box for plot axes
		frameContext=$('#'+this.frameId)[0].getContext('2d');
		h=$('#'+this.frameId).height()-this.pad;
		w=$('#'+this.frameId).width();

		frameContext.beginPath();
		frameContext.lineWidth=1.0;
		frameContext.moveTo(this.pad,0);
		frameContext.lineTo(this.pad,h);
		frameContext.stroke();
		frameContext.beginPath();
		frameContext.lineWidth=1.0;
		frameContext.moveTo(this.pad,0);
		frameContext.lineTo(w,0);
		frameContext.stroke();
		frameContext.beginPath();
		frameContext.lineWidth=1.0;
		frameContext.moveTo(w,0);
		frameContext.lineTo(w,h);
		frameContext.stroke();
		frameContext.beginPath();
		frameContext.lineWidth=1.0;
		frameContext.moveTo(this.pad,h);
		frameContext.lineTo(w,h);
		frameContext.stroke();
		frameContext.closePath();
	};
    // sets the datamodel and configures the config panel with the column information
	this.setData = function(data) {
	    this.data = data;
	    var xSelector = $('#' + this.xSelectId),
            ySelector = $('#' + this.ySelectId),
	        xFirstMathSelector = $('#' + this.xFirstMathSelectId),
            xSecondMathSelector = $('#' + this.xSecondMathSelectId),
            yFirstMathSelector = $('#' + this.yFirstMathSelectId),
            ySecondMathSelector = $('#' + this.ySecondMathSelectId);

	    xSelector[0].options.length = 0;
	    ySelector[0].options.length = 0;
	    xFirstMathSelector[0].options.length = 0;
	    xSecondMathSelector[0].options.length = 0;
	    yFirstMathSelector[0].options.length = 0;
	    ySecondMathSelector[0].options.length = 0;
		for (var i = 0; i < data.metadata.length; i++) {
			md = data.metadata[i];
			title = md.title;
			$('#'+this.xFirstMathSelectId).append("<option value="+i+">"+title+"</option>");	
			$('#'+this.xSecondMathSelectId).append("<option value="+i+">"+title+"</option>");	
			$('#'+this.yFirstMathSelectId).append("<option value="+i+">"+title+"</option>");	
			$('#'+this.ySecondMathSelectId).append("<option value="+i+">"+title+"</option>");	
			if (i == data.defaultX)
				$('#'+this.xSelectId).append($("<option selected = \"selected\" value="+i+">"+title+"</option>"));				
			else 	
				$('#'+this.xSelectId).append($("<option value="+i+">"+title+"</option>"));
			
			if (i == data.defaultY)
				$('#'+this.ySelectId).append($("<option selected = \"selected\" value="+i+">"+title+"</option>"));					
			else 	
				$('#'+this.ySelectId).append($("<option value="+i+">"+title+"</option>"));	
   		}
        $xmath = $("<option value=xmath>Combine Columns</option>");
        $ymath = $("<option value=ymath>Combine Columns</option>");
        $('#'+this.xSelectId).append($xmath);
        $('#'+this.ySelectId).append($ymath);  
	};

	


// here we build the interface....
	
	
	this.pad = 50;
	this.xprec = 0;
	this.yprec = 0;
	
	this.xlog=false;
	this.ylog=false;

	this.mainDiv = divName;
	insertDiv=$("#"+divName);
	if (this.mainDiv==null) {
		alert("Div of name " + divName + " not found");
		return;
	}

	if (insertDiv.width() < 600) {
		insertDiv.width(600);
	}
	if (insertDiv.height() < 400) {
		insertDiv.height(400);
	}
	insertDiv.position('relative');

	this.frameId = divName+'FrameCanvas';
	$frameCanvas=$( '<canvas/>', {id:this.frameId, position: 'absolute', left: '0px', top: '0px'} );
	$frameCanvas.css("zIndex","1");

	wx = insertDiv.width()-this.pad;  
	wy = insertDiv.height()-this.pad;  

    this.plotWidth = wx;
    this.plotHeight = wy;
	this.pointsId = divName+"PointsCanvas";
	$pointsCanvas=$( '<canvas />', {id: this.pointsId, style: "position: absolute; left: "+this.pad+"px; top: 0px; "} );

	this.cursorId = divName+"CursorCanvas";
	$cursorCanvas=$( '<canvas />', {id: this.cursorId, 
		style: "position: absolute; left: "+this.pad+"px; top: 0px; cursor: 'crosshair'; "} );
	$cursorCanvas.css("zIndex","4");

	this.fitId = divName+"FitCanvas";
	$fitCanvas=$( '<canvas />', {id: this.fitId, style: "position: absolute; left: "+this.pad+"px; top: 0px; "} );
	$fitCanvas.css("zIndex","3");

	this.configButtonId = divName + "ConfigButton";
	//$configImg = $('<img/>',{id: this.configButtonId, style: "position: absolute; left: 0px; top: " + wy + "px; ", src: "images/config.jpg"});
	//$configImg.css("zIndex","1");

	this.waitingId = divName + "waitingId";
	$waitingImage = $('<img/>',{id: this.waitingId, style: "position: absolute; left: "+(wx/2-70)+"px; top: " + wy/2 + "px; ", src: "images/ajax-loader.gif"});
	$waitingImage.css("zIndex","1");

	this.configId = divName + "Config";
	this.xminText = divName + "xMinText";
	this.xmaxText = divName + "xMaxText";
	this.xConfigReverse = divName + "xConfigReverse";	
	this.xConfigLog = divName + "xConfigLog";	
	this.configOK = divName + "xConfigOK";	
	this.configReset = divName + "xConfigReset";	
	this.configCancel = divName + "xConfigCancel";


	this.yminText = divName + "yMinText";
	this.ymaxText = divName + "yMaxText";
	this.yConfigReverse = divName + "yConfigReverse";	
	this.yConfigLog = divName + "yConfigLog";	
	
	$xminBox = $("<input/>",{id: this.xminText, type: 'number'});
	$xmaxBox = $("<input/>",{id: this.xmaxText, type: 'number'});
	$yminBox = $("<input/>",{id: this.yminText, type: 'number'});
	$ymaxBox = $("<input/>",{id: this.ymaxText, type: 'number'});
	$xLogBox = $("<input/>",{id: this.xConfigLog, type: 'checkbox'});
	$yLogBox = $("<input/>",{id: this.yConfigLog, type: 'checkbox'});
	$xminBox.keydown(checkNumberOnly);
	$xmaxBox.keydown(checkNumberOnly);
	$yminBox.keydown(checkNumberOnly);
	$ymaxBox.keydown(checkNumberOnly);
    
    this.moreDataId = divName + "moreData";
    //$moreData = $("<button>+</button>",{id: this.moreDataId});

	this.xSelectId = divName + "XSelector";
	this.ySelectId = divName + "YSelector";
	
	$configDiv = $('<div/>',{id: this.configId, style: "position: absolute; border: 2px solid #a1a1a1; background-color:#b0c4de;"});
	$xSelector = $("<select/>",{id: this.xSelectId});
	$ySelector = $("<select/>",{id: this.ySelectId});
	$xSelector.change({plot: this}, setConfigOptions);
	$ySelector.change({plot: this}, setConfigOptions);
	$configTable = $("<table/>" );
	$row = $("<tr/>");
	$configTable.append($row);
	$row.append("<td>X Data:</td>",$("<td/>").append($xSelector));
	$row.append("<td>Y Data:</td>",$("<td/>").append($ySelector));
	//$row.append($moreData);
	
	
	this.xMathTableId = divName + "xMathTableId";
	this.xFirstMathSelectId = divName + "xFirstMathId";
	this.xFunctionMathSelectId = divName + "xFunctionMathId";
	this.xSecondMathSelectId = divName + "xSecondMathId";
	
	$xFirstMathSelector = $("<select/>",{id: this.xFirstMathSelectId});
	$xFunctionMathSelector = $("<select/>",{id: this.xFunctionMathSelectId});
	$xSecondMathSelector = $("<select/>",{id: this.xSecondMathSelectId});
	$xMathTable = $("<table />",{id: this.xMathTableId});
	$row = $("<tr align=right/>");
    $row.append("<td/>");
    $row.append($("<td align=right/>").append($xFirstMathSelector));
	$xMathTable.append($row);
	$row = $("<tr align=right/>");
	$row.append($("<td align=right/>").append($xFunctionMathSelector));
	$row.append($("<td align=right>").append($xSecondMathSelector));
  	$xMathTable.append($row);
    	
    $xFunctionMathSelector.append("<option>+</option>");
    $xFunctionMathSelector.append("<option>-</option>");
    $xFunctionMathSelector.append("<option>*</option>");
    $xFunctionMathSelector.append("<option>/</option>");
   	
   	   	
   	$crow = $("<tr  align=right/>");
   	$configTable.append($crow);
   	$crow.append($("<td colspan=2/>").append($xMathTable));

   	$xMathTable.hide();
	
	this.yMathTableId = divName + "yMathTableId";
	this.yFirstMathSelectId = divName + "yFirstMathId";
	this.yFunctionMathSelectId = divName + "yFunctionMathId";
	this.ySecondMathSelectId = divName + "ySecondMathId";
	
	$yFirstMathSelector = $("<select/>",{id: this.yFirstMathSelectId});
	$yFunctionMathSelector = $("<select/>",{id: this.yFunctionMathSelectId});
	$ySecondMathSelector = $("<select/>",{id: this.ySecondMathSelectId});
	$yMathTable = $("<table />",{id: this.yMathTableId});
	$row = $("<tr align=right/>");
    $row.append("<td/>");
    $row.append($("<td align=right/>").append($yFirstMathSelector));
	$yMathTable.append($row);
	$row = $("<tr align=right/>");
	$row.append($("<td align=right/>").append($yFunctionMathSelector));
	$row.append($("<td align=right>").append($ySecondMathSelector));
  	$yMathTable.append($row);
    	
    $yFunctionMathSelector.append("<option>+</option>");
    $yFunctionMathSelector.append("<option>-</option>");
    $yFunctionMathSelector.append("<option>*</option>");
    $yFunctionMathSelector.append("<option>/</option>");
   	

   	$crow.append($("<td colspan=2/>").append($yMathTable));

   	$yMathTable.hide();	
	
	$row = $("<tr/>");
	$configTable.append($row);
	$row.append("<td>X Min:</td>",$("<td/>").append($xminBox));
	$row.append("<td>Y Min:</td>",$("<td/>").append($yminBox));
	$row = $("<tr/>");
	$configTable.append($row);
	$row.append("<td>X Max:</td>",$("<td/>").append($xmaxBox));
	$row.append("<td>Y Max:</td>",$("<td/>").append($ymaxBox));
	$row = $("<tr/>");
	$configTable.append($row);
	$row.append($("<td colspan=2>X Log Scale:</td>").append($xLogBox));
	$row.append($("<td colspan=2>Y Log Scale:</td>").append($yLogBox));
	
	
	
	$configDiv.append($configTable);
	
	
	
	$("#" + divName + "XSel").append($xSelector);
	$("#" + divName + "YSel").append($ySelector);
	$("#" + divName + "xmin").append($xminBox);
	$("#" + divName + "xmax").append($xmaxBox);
	$("#" + divName + "xmin").append($yminBox);
	$("#" + divName + "xmax").append($ymaxBox);
	
	
	$configDiv.append("<button id="+this.configOK+">Accept Changes</button>","<button id="+this.configReset+">Reset Zoom</button>", "<button id="+this.configCancel+">Revert Changes</button>");
	$configDiv.css("zIndex","10");
	$configDiv.hide();





	// I assume that if we empty now we have loaded a canvas element and just not inserted it so we can remove whatever non-canvas warning is in the div
	$('#'+this.mainDiv).empty();

    $configDiv.offset({ top: wy/2, left: wx/2-180})
	$frameCanvas[0].width = wx+this.pad;
	$frameCanvas[0].height = wy+this.pad;
	$frameCanvas[0].style.width = (wx+this.pad) +'px';
	$frameCanvas[0].style.height = (wy+this.pad)+ 'px';
	$pointsCanvas[0].width = wx;
	$pointsCanvas[0].height = wy;
	$pointsCanvas[0].style.width = wx +'px';
	$pointsCanvas[0].style.height = wy+ 'px';
	$cursorCanvas[0].width = wx;
	$cursorCanvas[0].height = wy;
	$cursorCanvas[0].style.width = wx +'px';
	$cursorCanvas[0].style.height = wy+ 'px';
	$fitCanvas[0].width = wx;
	$fitCanvas[0].height = wy;
	$fitCanvas[0].style.width = wx +'px';
	$fitCanvas[0].style.height = wy+ 'px';


	$('#'+this.mainDiv).append($frameCanvas);
	$('#'+this.mainDiv).append($pointsCanvas);
	$('#'+this.mainDiv).append($cursorCanvas);
	$('#'+this.mainDiv).append($fitCanvas);
	//$('#'+this.mainDiv).append($configImg);
	$('#'+this.mainDiv).append($waitingImage);
	$('#'+this.mainDiv).append($configDiv);
	
	$("#"+this.configOK).click({plot: this},function(event){
		event.data.plot.acceptConfig();
		});
	$("#"+this.configReset).click({plot: this},function(event){		
		event.data.plot.revertZoom();
		});	
	$("#"+this.configCancel).click({plot: this},function(event){
		event.data.plot.cancelConfig();
		});


	$("#"+this.cursorId).mousedown({plot: this}, mouseAction);
	$("#"+this.cursorId).mouseup({plot: this}, mouseAction);
	$("#"+this.cursorId).mousemove({plot: this}, mouseAction);
	$("#"+this.cursorId).mouseleave({plot: this}, mouseAction);
	$("#"+this.cursorId).mouseenter({plot: this}, mouseAction);
	$("#"+this.cursorId).mouseover({plot: this}, mouseAction);
	
	$("#"+this.frameId).dblclick({plot: this}, plotMouseAction);
	$("#"+this.configButtonId).click({plot: this}, plotMouseAction);
}





// just change the cursor to some style
function setCursorByID(cursorStyle) {
	//	sets cursor style (within either canvas layer)
	$("body").css("cursor", cursorStyle);
}
// an number formatter that sets the text of the value to something relevant given the data range
formatNumber = function(x,min,max) {
	lx = log10(Math.abs(max));
	lxx = log10(Math.abs(min));
	if (lx < lxx) lx = lxx;
	dx =  log10(Math.abs(max-min));
	prec = 3 + parseInt(lx - dx);
	
	if (x == 0) return 0;
	prec++;
	if (prec == 0) prec = 1;
	absx = log10(Math.abs(x));
	if (isNaN(x)) { 
		newx = 0;
	} else {
		if ( prec > 4 || (absx > 3 || absx < -2))  {
			newx = x.toExponential(prec);
			newx = parseFloat(newx).toExponential();
		} else {
			size = 1;
			newx = x.toPrecision(prec);
			newx = parseFloat(newx).toPrecision();
		} 
	}
	return newx;
};

// log base 10 for a number...should be a prototype but I'm lazy
var logof10=Math.log(10);
log10 = function(x) {
	return Math.log(x)/logof10;
};

//Creates an object with x and y defined,
//set to the mouse position relative to the state's canvas
//If you wanna be super-correct this can be tricky,
//we have to worry about padding and borders
//takes an event and a reference to the canvas
getMouse = function(e, canvas) {
	var element = canvas, offsetX = 0, offsetY = 0, mx, my;

//	Compute the total offset. It's possible to cache this if you want
	if (element.offsetParent !== undefined) {
		do {
			offsetX += element.offsetLeft;
			offsetY += element.offsetTop;
		} while ((element = element.offsetParent));
	}

//	Add padding and border style widths to offset
//	Also add the <html> offsets in case there's a position:fixed bar (like the stumbleupon bar)
//	This part is not strictly necessary, it depends on your styling
//	offsetX += stylePaddingLeft + styleBorderLeft + htmlLeft;
//	offsetY += stylePaddingTop + styleBorderTop + htmlTop;

	mx = e.pageX - offsetX;
	my = e.pageY - offsetY;

//	We return a simple javascript object with x and y defined
	return {x: mx, y: my};
};

// accepts only numerically valid entries into a text box
checkNumberOnly = function(e) {
	
    var keyPressed;
    if (!e) var e = window.event;
    if (e.keyCode) keyPressed = e.keyCode;
    else if (e.which) keyPressed = e.which;
    var hasDecimalPoint = (($(this).val().split('.').length-1)>0);
	var hasExponent = (($(this).val().toLowerCase().split('e').length-1)>0);
    if ( keyPressed == 46 || keyPressed == 8 ||((keyPressed == 190||keyPressed == 110)&&(!hasDecimalPoint)) || (keyPressed== 69 && !hasExponent)|| keyPressed == 9 || keyPressed == 27 || keyPressed == 13 ||
            // Allow: Ctrl+A
           (keyPressed == 65 && e.ctrlKey === true) ||
            // Allow: home, end, left, right
           (keyPressed >= 35 && keyPressed <= 39)) {
                // let it happen, don't do anything
                return;
       }
       else {
           // Ensure that it is a number and stop the keypress
           if ( (e.shiftKey && keyPressed != 69) || (keyPressed < 48 || keyPressed > 57) && (keyPressed < 96 || keyPressed > 105 ) && keyPressed != 109 && keyPressed !=69) {
               e.preventDefault();
           }
       }
};

