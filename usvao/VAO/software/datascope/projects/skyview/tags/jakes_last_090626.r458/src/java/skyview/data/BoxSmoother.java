package skyview.data;

import skyview.process.Processor;
import skyview.executive.Settings;
import skyview.executive.SettingsUpdater;
import skyview.survey.Image;
import skyview.geometry.Sampler;
import skyview.geometry.DepthSampler;

import nom.tam.fits.Header;

/** Do a box car smoothing of an image */
public class BoxSmoother implements Processor, SettingsUpdater {

    /** Width of box */
    private int nx = 1;
    
    /* Height of box */
    private int ny = 1;
    
    /** Width of image */
    private int width;
    /** Height of image */
    private int height;
    
    /** Depth of image */
    private int depth;

    /** Image data */
    private double[] data;
    
    /** Working data */
    private double[] xdata;
    
    public String getName() {
	return "Smoother("+nx+","+ny+")";
    }
    
    public String getDescription() {
	return "Box car smoother";
    }
    
    /** Update the settings associated with this smoother */
    public void updateSettings() {
	if (Settings.has("smooth")  && Settings.get("smooth").length() > 0) {
	    String[] upd = Settings.getArray("postprocessor");
	    String cname = this.getClass().getName();
	    for (int i=0; i<upd.length; i += 1) {
		if (cname.equals(upd[i])) {
		    return;
		}
	    }
	    // Put smooth before other postprocessors.
	    if (Settings.has("postprocessor")) {
		Settings.put("postprocessor", cname+","+Settings.get("postprocessor"));
	    } else {
	        Settings.put("postprocessor", cname);
	    }
	}
    }
    
    /** Use as a postprocessor */
    public void process(Image[] inputs, Image output,
			int[] selector, Sampler samp, DepthSampler dsamp) {
	
	String[] smoothPar = Settings.getArray("smooth");
	try {
	    if (smoothPar.length == 1 && smoothPar[0].length() > 0) {
	        nx = Integer.parseInt(smoothPar[0]);
		ny = nx;
	    } else if (smoothPar.length > 1) {
		nx = Integer.parseInt(smoothPar[0].trim());
		ny = Integer.parseInt(smoothPar[1].trim());
	    } else {
	        nx = 3;
	        ny = 3;
	    }
	} catch (Exception e) {
	    System.err.println("Error parsing smooth parameters:"+Settings.get("smooth"));
	    return;
	}
	
        data   = output.getDataArray();
	width  = output.getWidth();
	height = output.getHeight();
	depth  = output.getDepth();
	if (depth <= 0) {
	    depth = 1;
	}
	smooth();
    }
    
    /** Smooth an image directly */
    public static void smooth(Image img, int boxWidth, int boxHeight) {
	smooth(img.getDataArray(), img.getWidth(), img.getHeight(), 
	       img.getDepth(), boxWidth, boxHeight
	       );
    }
    
    public static void smooth(double[] data, int imageWidth, int imageHeight,
			      int imageDepth, int boxWidth, int boxHeight) {
    
	BoxSmoother bs = new BoxSmoother();
	bs.data   = data;
	bs.width  = imageWidth;
	bs.height = imageHeight;
	bs.depth  = imageDepth;
	bs.nx     = boxWidth;
	bs.ny     = boxHeight;
	bs.smooth();
    }
	
    /** Smooth the current image according to the prescribed size of the box */
    public void smooth() {
	
	xdata = new double[data.length];
	if (nx < 1 || ny < 1 || (nx == 1 && ny == 1)) {
	    return;
	}
	if (ny % 2 == 0) {
	    ny += 1;
	}
	
	if (nx % 2 == 0) {
	    nx += 1;
	}
	int dy = ny/2;
	
	// Handle the bottom rows
	for (int irow=0; irow < dy; irow += 1) {
	    doRow(0, irow+dy, irow);
	}
	
	// Handle the middle rows
	for (int irow=dy; irow < height-dy; irow += 1) {
	    doRow(irow-dy, irow+dy, irow);
	}
	
	// Handle the top rows
	for (int irow=height-dy; irow<height; irow += 1) {
	    doRow(irow-dy, height-1, irow);
	}
	
	// Copy smoothed image back
	System.arraycopy(xdata, 0, data, 0, data.length);
    }
    
    /** Smooth a single row. */
    private void doRow(int start, int end, int to) {
	
	int dx = nx/2;
	
	int z    = 0;
	int cols = end-start+1;
	
	for (int iz=0; iz<depth; iz += 1) {
	    
	    double sum   = 0;
	    double count = 0;
	    
	    for (int i=0; i < dx; i += 1) {
		sum   += add(z, start, end, i);
		count += cols;
	    }
	    for (int i=0; i<dx; i += 1) {
		sum   += add(z, start, end, i+dx);
		count += cols;
		xdata[z + width*to + i] = sum/count;
	    }
	    count += cols;
	    for (int i=dx; i<width-dx; i += 1) {
		sum += add(z, start, end, i+dx);
		xdata[z + width*to + i] = sum/count;
		sum -= add(z, start, end, i-dx);
	    }
	    for (int i=width-dx; i<width; i += 1) {
		count -= cols;
		xdata[z + width*to + i] = sum/count;
		sum  -= add(z, start, end, i-dx);
	    }
	}
    }
    
    /** Get the data along a vertical edge of the box. */
    private double add(int z, int rowStart, int rowEnd, int col) {
	double sum = 0;
	for (int i=rowStart; i<=rowEnd; i += 1) {
	    sum += data[z+i*width+col];
	}
	return sum;
    }
    
    /** Add information about the smoothing to the FITS header */
    public void updateHeader(Header h) {
	try {
	    h.insertHistory("");
	    h.insertHistory("Smoothed with BoxSmoother:"+this.getClass().getName());
	    h.insertHistory("    Box:"+nx+","+ny);
	    h.insertHistory("");
	} catch (Exception e) {}
    }
}
