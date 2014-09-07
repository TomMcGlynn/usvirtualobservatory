package skyview.geometry.sampler;

import skyview.geometry.Sampler;
import skyview.survey.Image;

import skyview.executive.Settings;

 
/** 
 * The class implements a fast flux conserving resampling
 * based on the Sutherland-Hodgman clipping algorithm.
 * <p>
 * Consider an original image of data in some projection and
 * a new map of an overlapping region with some
 * projection, coordinate system, etc.  Assume that the flux
 * within a given pixel in the original image is constant
 * over the area of the pixel.
 * The flux within each pixel in the new map should be
 * the integral of the flux in the region occupied by
 * each pixel in the map.
 * 
 * <ul>
 * <li> Find all of the corners of the pixels in resampled
 * map and project the positions of these corners into
 * the projection of the original image. These should
 * be scaled such that the coordinates run from 0 to inWidth and inHeight
 * where inWidth and inHeight are the dimensions of the original image.
 * I.e., in these coordinate each pixel in the original image
 * is a unit square.  This step is done prior to
 * call the primary ClipSampler methods in this class.  Note
 * that the corners of the pixels are required rather than
 * the pixel centers.
 * 
 * <li>For each pixel in the resampled map, find a bounding
 * box in the original image's coordinates.  This is used
 * to find a rectangle of candidate pixels in the original image that
 * may contribute flux to this pixel in the resampled map.
 * 
 * <li>For each candidate image pixel clip the resampled pixel
 * to the image pixels boundaries.
 * 
 * <li>Calculate the area of the clipped region.  This is easy since
 * the clipped region is a convex polygon and we have the vertices.
 * Triangulating the polygon allows us to calculate its area.
 * 
 * <li>Add a flux to the resampled pixel equal to the area of the
 * clipped resampled pixel times the flux in the original map pixel
 * 
 * <li> Repeat for all candidate original pixels
 * 
 * <li> Go to the next resampling pixel.
 * </ul>
 * 
 * <p>
 * The instance methods of this class are not thread-safe, however
 * it is possible to generate a separate ClipSampler object for
 * each thread to resample the same input image.
 * <p>
 * Developed by Tom McGlynn, NASA/GSFC
 * October 3, 2002
 */
public class ClipSampler extends Sampler {
    
    double[] depthArray;
    
    public void setOutput(Image outImage) {
	super.setOutput(outImage);
	depthArray = new double[outImage.getDepth()];
    }
    
    
    public String getName() {
	return "ClipSampler";
    }
    
    public String getDescription() {
	return "Sample by using output pixels as clipping rectangles on input image";
    }
    
    /** Drizzle offset */
    protected double drizzOffset=0;
    
    /** Drizzle Area */
    protected double drizzArea=1;
    
    /** The area (in original image pixels) overlapped by
     *  the current pixel.
     */
    private double tArea;
    
    /** Is this an intensive quantity? */
    private boolean intensive = false;
    
    
    public void setIntensive(boolean intensive) {
	this.intensive = intensive;
    }
    
    
    /** Set the drizzle factor for sampling
     *  @param drizzle The drizzle factor should range from 0 to 1 and
     *                 indicates the length of the side of the pixel
     *                 inside the original image pixel in which the
     *                 flux is assumed to be contained.
     */
    
    public void setDrizzle(double drizzle) {
	
	if (drizzle < 0) {
	    drizzle = 0;
	}
	if (drizzle > 1) {
	    drizzle = 1;
	}
	
	drizzArea = drizzle*drizzle;
	drizzOffset = (1-drizzle)/2;
    }
    
    public ClipSampler() {
	if (Settings.has("ClipIntensive")) {
	    setIntensive(true);
	}
	if (Settings.has("ClipDrizzle")) {
	    double driz = Double.parseDouble(Settings.get("ClipDrizzle"));
	    if (driz >= 0 && driz <= 1) {
		setDrizzle(driz);
	    }
	}
    }
	
    
    /** Calculate the area of a convex polygon.
     * This function calculates the area of a convex polygon
     * by deconvolving the polygon into triangles and summing
     * the areas of the consituents.  The user provides the
     * coordinates of the vertices of the polygon in sequence
     * along the circumference (in either direction and starting
     * at any point).
     * 
     * Only distinct vertices should be given, i.e., the
     * first vertex should not be repeated at the end of the list.     *
     * @param	n	The number of vertices in the polygon.
     * @param   x	The x coordinates of the vertices
     * @param   y	The y coordinates of teh vertices
     * @return		The area of the polygon.
     */
    private static double convexArea(int n, double[] x, double[] y) {
	
	double area = 0;
	
	for(int i=1; i<n-1; i += 1) {
	    
	    area += triangleArea(x[0],y[0], x[i], y[i], x[i+1], y[i+1]);
	}
	
	return area;
    }
    
    /** Calculate the area of an arbitrary triangle.
     *  Use the vector formula
     *     A = 1/2 sqrt(X^2 Y^2 - (X-Y)^2)
     *  where X and Y are vectors describing two sides
     *  of the triangle.
     * 
     *  @param x0	x-coordinate of first vertex
     *  @param y0       y-coordinate of first vertex
     *  @param x1       x-coordinate of second vertex
     *  @param y1       y-coordinate of second vertex
     *  @param x2       x-coordinate of third vertex
     *  @param y2       y-coordinate of third vertex
     * 
     *  @return         Area of the triangle.
     */
    
    private static double triangleArea(double x0, double y0, 
				      double x1, double y1, 
				      double x2, double y2) {
	
	// Convert vertices to vectors.
	double a = x0-x1;
	double b = y0-y1;
	double e = x0-x2;
	double f = y0-y2;
	
	double area=  (a*a+b*b)*(e*e+f*f) - (a*e+b*f)*(a*e+b*f);
	if (area <= 0) {
	    return 0; // Roundoff presumably!
	} else {
	    return Math.sqrt(area)/2;
	}
    }
    
    
    /** Clip a polygon to a half-plane bounded by a vertical line.
     *  Users can flip the input axis order to clip by a horizontal line.
     *  This is the central operation in the Sutherland-Hodgeman algorithm.
     * 
     *  This function uses pre-allocated arrays for
     *  output so that no new objects are generated
     *  during a call.
     *  
     *  @param 	n	Number of vertices in the polygon
     *  @param  x	X coordinates of the vertices
     *  @param  y	Y coordinates of the vertices
     *  @param  nx	New X coordinates
     *  @param  ny      New Y coordinates
     *  @param  val	Value at which clipping is to occur.
     *  @param  dir     Direction for which data is to be
     *                  clipped.  true-> clip below val, false->clip above val.
     * 
     *  @return         The number of new vertices.
     * 
     */    
    private static int lineClip(int n, 
			       double[] x, double[] y, 
			       double[] nx, double[] ny,
                               double val, boolean dir) {
	
	int	nout=0;
	
	// Need to handle first segment specially
	// since we don't want to duplicate vertices.
	boolean last = inPlane(x[n-1], val, dir);
	
	for (int i=0; i < n; i += 1) {
	    
	    if (last) {
		
		if (inPlane(x[i], val, dir)) {
		    // Both endpoints in, just add the new point
		    nx[nout] = x[i];
		    ny[nout] = y[i];
		    nout    += 1;
		} else {
		    double ycross;
		    // Moved out of the clip region, add the point we moved out
		    if (i == 0) {
		        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[0]-x[n-1]);
		    } else {
		        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
		    }
		    nx[nout] = val;
		    ny[nout] = ycross;
		    nout    += 1;
		    last     = false;
		}
		
	    } else {
		
		if (inPlane(x[i], val, dir)) {
		    // Moved into the clip region.  Add the point
		    // we moved in, and the end point.
		    double ycross;
		    if (i == 0) {
		        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[i]-x[n-1]);
		    } else {
		        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
		    }
		    nx[nout]  = val;
		    ny[nout] = ycross;
		    nout += 1;
		    
		    nx[nout] = x[i];
		    ny[nout] = y[i];
		    nout += 1;
		    last     = true;
		    
		} else {
		    // Segment entirely clipped.
		}
	    }
	}
	return nout;
    }
    
    /**
     * Is the test value on the on the proper side of a line.
     * 
     * @param test	Value to be tested
     * @param divider	Critical value
     * @param direction True if values greater than divider are 'in'
     *                  False if smaller values are 'in'.
     * @return          Is the value on the desired side of the divider?
     */
    private static boolean inPlane(double test, double divider, boolean direction) {
		
        // Note that since we always include
	// points on the dividing line as 'in'.  Not sure
	// if this is important though...
	 
	if (direction) {
	    return test >= divider;
	} else {
	    return test <= divider;
	}
    }
    
    // Intermediate storage used by rectClip.
    // The maximum number of vertices we will get if we start with
    // a convex quadrilateral is 12, but we use larger
    // arrays in case this routine is used is some other context.
    // If we were good we'd be checking this when we add points in
    // the clipping process.
    
    private double[] rcX0 = new double[100];
    private double[] rcX1 = new double[100];
    private double[] rcY0 = new double[100];
    private double[] rcY1 = new double[100];
    
    /** Clip a polygon by a non-rotated rectangle.
     * 
     *  This uses a simplified version of the Sutherland-Hodgeman polygon
     *  clipping method.  We assume that the region to be clipped is
     *  convex.  This implies that we will not need to worry about
     *  the clipping breaking the input region into multiple
     *  disconnected areas.
     *    [Proof: Suppose the resulting region is not convex.  Then
     *     there is a line between two points in the region that
     *     crosses the boundary of the clipped region.  However the
     *     clipped boundaries are all lines from one of the two
     *     figures we are intersecting.  This would imply that
     *     this line crosses one of the boundaries in the original
     *     image.  Hence either the original polygon or the clipping
     *     region would need to be non-convex.]
     * 
     *  Private arrays are used for intermediate results to minimize
     *  allocation costs.
     * 
     *  @param n	Number of vertices in the polygon.
     *  @param x	X values of vertices
     *  @param y        Y values of vertices
     *  @param nx	X values of clipped polygon
     *  @param ny       Y values of clipped polygon
     * 
     *  @param          minX Minimum X-value
     *  @param		minY Minimum Y-value
     *  @param          maxX MAximum X-value
     *  @param          maxY Maximum Y-value
     * 
     *  @return		Number of vertices in clipped polygon.
     */
    private int rectClip(int n, double[] x, double[] y, double[] nx, double[] ny,
			       double minX, double minY, double maxX, double maxY) {
	
	int nCurr;
	
	// lineClip is called four times, once for each constraint.
	// Note the inversion of order of the arguments when
	// clipping vertically.
	// 
	
	nCurr = lineClip(n, x, y, rcX0, rcY0, minX, true);

	if (nCurr > 0) {
	    nCurr = lineClip(nCurr, rcX0, rcY0, rcX1, rcY1, maxX, false);
	    
	    if (nCurr > 0) {
		nCurr = lineClip(nCurr, rcY1, rcX1, rcY0, rcX0, minY, true);
		
		if (nCurr > 0) {
		    nCurr = lineClip(nCurr, rcY0, rcX0, ny, nx, maxY, false);
		}
	    }
	}
	
	// We don't need to worry that we might not have set the output arrays.
        // If nCurr == 0, then it doesn't matter that
	// we haven't set nx and ny.  And if it is then we've gone
	// all the way and they are already set.
	 
	return nCurr;
    }

    /** Debugging routine that prints a list of vertices.
     *  @param n The number of vertices in the polygon
     *  @param x X coordinates
     *  @param y Y coordinates
     */
    protected static void printVert(int n, double[] x,double[] y, String label) {
	
	for (int i=0; i<n; i += 1) {
	    System.out.println(label+"   "+x[i]+"  "+y[i]);
	}
    }
	
    // Intermediate storage used by ClipSampler 
    private double psX1[] = new double[12];
    private double psY1[] = new double[12];
    
    
    /** Implement the sample function of the Sampler class.
     *  This function samples a single pixel.
     *  @param coords  The coordinates of the corners of the
     *                 pixel given as a double[2][4] array.
     *  @return The sample value
     */
    
    private double[][] corners = new double[2][4];
    public void sample(int pix) {
	double[][] in = outImage.getCorners(pix);
	trans.transform(in, corners);
	samplePixel(pix, corners[0], corners[1]);
    }
    
    /** Return the weight associated with the last sampling.
     *  For the ClipSampler, the weight can be associated with
     *  the 'area' of the pixel.
     */
    public double weight() {
	return tArea;
    }

    
    /** Sample a single map pixel.
     * 
     *  @param x The x values of the corners of the pixel [4]
     *  @param y The y values of the corners of the pixel [4]
     * 
     *  @return  The total flux in the resampled pixel.
     */
    public void samplePixel(int pix, double[] x, double[] y) {
	
	for (int z=0; z<depthArray.length; z += 1) {
	    depthArray[z] = 0;
	}
	
	// Find a bounding box for the pixel coordinates.
	double minX = x[0];
	double maxX = minX; 
	double minY = y[0];
	double maxY = minY;
	
	for (int k=1; k<4; k += 1) {
	    
	    // Note that a value can't simultaneously
	    // update the minimum and maximum...
	    if (x[k] < minX) {
		minX = x[k];
	    } else if (x[k] > maxX) {
		maxX = x[k];
	    }
	    if (y[k] < minY) {
		minY = y[k];
	    } else if (y[k] > maxY) {
		maxY = y[k];
	    }
	    
	}
	
			
	// Round the extrema of the pixel coordinates to
	// integer values.
	minX = Math.floor(minX);
	maxX = Math.ceil(maxX);
	
	minY = Math.floor(minY);
	maxY = Math.ceil(maxY);
	
	
	// Check to see if pixel is entirely off original image.
	// If so we don't need to do anything further.
	if (maxX <= 0 || minX >= inWidth || maxY <= 0 || minY >= inHeight) {
	    tArea  = 0;
	    return;
	}
	
	// Check if the resampling pixel is entirely enclosed in
	// the image pixel.  Need to check this before
	// we 'clip' our bounding box.  This check
	// should significantly increase the speed
	// for oversampling images, but it will cause
	// a tiny slowdown when the sampling pixels are as large
	// or larger than the original pixels.
	// 
	// We're doing equalities with
	// double values, but they are guaranteed to be
	// integers.
	double xArea = convexArea(4, x, y);
	
	if (minX == maxX-1 && minY == maxY-1) {
	    
	    tArea = xArea;
	    for (int z=0; z<inDepth; z += 1) {
		if (intensive) {
		    tArea = 1;
		}
	        outImage.setData(pix+z*outWidth*outHeight, tArea*inImage.getData((int)minX + ((int)minY)*inWidth + z*inWidth*inHeight));
	    }
	    return;
	}
	
	
	// Clip the bounding box to the original image dimensions
	if (maxX >= inWidth) {
	    maxX = inWidth;
	}
	
	if (minX < 0) {
	    minX = 0;
	}
	
	if (maxY >= inHeight) {
	    maxY = inHeight;
	}
	
	if (minY < 0) {
	    minY = 0;
	}		
	
	
	// Loop over the potentially overlapping pixels.
	// 
	double value=0;
	tArea=0;
	for (int n=(int)minY; n<(int)maxY; n += 1) {
	    
	    // the vmin/max values are the areas in
	    // which the 'flux' of a given pixel is treated
	    // as being.
	    // 
	    // We could probably handle the drizzle=0 case better.
	    
	    double vminY = n + drizzOffset;
	    double vmaxY = n + 1 - drizzOffset;
	    
	    
	    for (int m=(int)minX; m<(int)maxX; m += 1) {
		
		double vminX = m + drizzOffset;
		double vmaxX = m + 1 - drizzOffset;
		
		
		// Clip the quadrilaterel given by the coordinates
		// of the resampling pixel, to this particular
		// image pixel.
		int nv = rectClip(4, x, y, psX1, psY1,
				  vminX,vminY, vmaxX,vmaxY);
		
		// If there is no overlap we won't get any
		// vertices back in the clipped set.
		if (nv > 0) {
		    
		    // Calculate the area of the clipped pixel and compare
		    // it to the area in which the flux of the original
		    // pixel is found.  The returned area should
		    // never be greater than the drizzArea.
		    double area, factor;
		    if (drizzArea > 0) {
		        area   = convexArea(nv, psX1, psY1);
			factor = area/drizzArea;
		    } else {
			// Handle the case where all of the flux of he
			// pixel is in the central point (drizzle=0)
			// So we know the entirel original pixel
			// gets added here.
			area   = 1;
			factor = 1;
		    }
		    
		    tArea += area;
		    
		    for (int z=0; z<inDepth; z += 1) {
		        depthArray[z] += factor*inImage.getData(m+n*inWidth+z*inWidth*inHeight);
		    }
		}
	    }  
	}
	/** If the output pixel goes over the edge
	 *  of the original data, scale luminosity appropriately.
	 */
	if (tArea > 0) {
	    double rat = xArea*drizzArea/tArea;
	    for (int z=0; z<inDepth; z += 1) {
		depthArray[z] *= rat;
	    }
	}
	// For intensive quantities convert from sum to weighted average.
	// We just need to divide by the total area of the output pixel.
	if (intensive) {
	    double area = convexArea(4, x, y);
	    if (area > 0) {
		for (int z=0; z<inDepth; z += 1) {
		    depthArray[z] /= area;
		}
	    } else {
		for (int z=0; z<inDepth; z += 1) {
		    depthArray[z] = 0;
		}
	    }
	}
	for (int z=0; z<depthArray.length; z += 1) {
	    outImage.setData(pix + z*outWidth*outHeight, depthArray[z]);
	}
	      
    }
}
