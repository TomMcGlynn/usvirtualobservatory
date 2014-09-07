package skyview.boundary.imagefinder;

import skyview.survey.Image;
import skyview.process.ImageFinder;

import skyview.geometry.Sampler;
import skyview.geometry.Transformer;
import skyview.geometry.WCS;

/** This class finds the best images to be used for sampling using
 *  a recursive rectangle algorithm.
 * <ul>
 *   <li> The output image is sampled in a rectangular grid with
 *        the samples spaced no more than half the image size of the
 *        input images.
 *   <li> For each primary rectangle, the best image of any of the candidate
 *        input images for each corner pixel in the rectangle is determined.
 *   <ul>
 *      <li> If the output pixel does not deproject to the celestial
 *           sphere (e.g., it may be from an Aitoff projection outside
 *           of the region representing data), then the pixel is marked
 *           as not physical and no checking against the input images is performed.
 *      <li> The corner is projected into each candidate image.
 *      <li> If the corner is within the candidate image, this
 *           candidate is marked as a candidate if another level
 *           of recursion is needed.
 *      <li> If the distance from the corner to any of the edges
 *           is greater than for any previous input image, then
 *           this image is considered the best candidate so far.
 *      <li> After all candidate input images are considered the
 *           best candidate is returned, or the pixel is marked
 *           as not being available on any input image if the output
 *           pixel did not project to the interior of any image.
 *   </ul>
 *   <li> If all four corners of a rectangle project to the same image
 *        (or if all four are non-physical, or not present on an input image),
 *        then this value is copied to all pixels within the rectangle.
 *   <li> Otherwise, if the rectangle can be split then it is split
 *        into subrectangles and the check is performed on the sub-rectangles.
 *   <li> Whenever a best image for a pixel is determined, that result
 *        is saved, so that only the 'new' corners in rectangles need to
 *        examined.
 *   <li> When checking the split rectangles, only images that overlapped
 *        at least one of the four corners in the previous level are considered.
 *  </ul>
 */
public class Corner extends ImageFinder {
    
    /** Transformation temporaries */
    double[] t2 = new double[2];
    double[] t3 = new double[3];
    
    /** Do strict tests? */
    boolean strictGeometry = false;
    
    /** Is a given image used in the transformation */
    boolean[] imageUsed;
    
    /** The transformation from the output pixels to the celestial sphere */
    Transformer fromOut;
    
    /** The output image */
    Image output;
    
    /** Find the best image for each output pixel.
     * @input An array of images that may be sampled to get the output image.
     * @input The output image.  In this routine we are interested in its
     *        geometry, not its data.
     * @return An index array which for each pixel in the output image
     *         gives the best image to sample.  Note that this has dimension
     *         int[nx*ny] where nx changes most rapidly.  The values of the index
     *         array can be:
     *         <ul>
     *           <li>  &gt;= 0: The pixel is best indexed with the given image.
     *           <li>  -1: [internal] The best image for this pixel has not yet been determined.
     *           <li>  -2: This pixel is not on any of the input images.
     *           <li>  -3: This pixel does not represent a physical coordinate.
     *           <li>  -4: [in other methods] this pixel has already been processed.
     *         </ul>
     */
    public int[] findImages(Image[] input, Image output) {
	
	int np       = output.getWidth()*output.getHeight();
	imageUsed    = new boolean[input.length];  // set to false on initialization.
	
	try {
	    this.fromOut = output.getTransformer().inverse();
	} catch(Exception e) {
	    throw new Error("In findImages: Unexpected transformation error:"+e);
	}
	this.output  = output;
	
	// Define an array that gives the input image for each pixel.
	int[]  img = new int[np];
        java.util.Arrays.fill(img, -1);
	
	
	// We are going to look at the corners of rectangles and see
	// if all four corners are best fit by a given image.  If so
	// we assume all pixels in the rectangle are so fit.  If not
	// we divvy up the rectangle and try again, using only the
	// images that overlapped one of the four corners.
	// We need to make the initial sampling dense enough that we
	// don't lose eligible images.
	
	// Let's get the relative scales of the input and output images
	
	
	double sin  = input[0].getWCS().getScale();
	double sout = output.getWCS().getScale();
	
	// Find the smaller dimension of the input images.
	int    nx = input[0].getWidth();
	int    ny = input[0].getHeight();
	
	if (ny < nx) {
	    nx = ny;
	}
	
	// Don't go more than half the size of an input image between checks.
	int maxDelta = (int)(nx*sin/(2*sout));
	if (maxDelta < 1) {
	    maxDelta = 1;
	}
	
	// At this top level we assume all images are eligible.
	boolean[] valid = new boolean[input.length];
	java.util.Arrays.fill(valid, true);
	
	int mx = output.getWidth();
	int my = output.getHeight();
	
	// Loop over the image grid with sufficient
	// resolution to catch all input images
	for (int i=0; i<mx; i += maxDelta) {
	    for (int j=0; j<my; j += maxDelta) {
		
		int ip = i + maxDelta-1;
		if (ip >= mx) {
		    ip = mx-1;
		}
		int jp = j+maxDelta-1;
		if (jp >= my) {
		    jp = my-1;
		}
		
	        // Check the rectangle
		checkRectangle(input, valid, img, i, ip, j, jp, mx);
	    }
	}
	
	// Let the user know how many images are actually used in the resampling.
	int count = 0;
	for (int i=0; i<input.length; i += 1) {
	    if (imageUsed[i]) {
		count += 1;
	    }
	}
	if (count > 0) {
	    return img;
	} else {
	    return null;
	}
    }
    
    /** Set a strict geometry. 
     *  If a strict geometry is requested in this class,
     *  the best image for each pixel will be determined rather
     *  than assuming that a rectangle with the same best
     *  image at each corner has the same best pixel in the
     *  interior.
     *  <p>
     *  This was seen to be necessary in rotated CSC projections,
     *  but otherwise does not seem to be an issue.
     *  <p>
     *  Note that even though each pixel will be checked,
     *  the recursive approach may still be useful, since
     *  the number of images checked at each level in the recursion
     *  will be reduced.
     */
    public void setStrict(boolean flag) {
	strictGeometry = flag;
    }
    
    /** See if all of the best match for each corner of a rectangle is the same.
     *  @param input The array of input images.
     *  @param valid Should this image be considered at this level of the recursion?
     *  @param img   The index array.
     *  @param x0    The minimum x in the rectangle.
     *  @param x1    The maximum x in the rectangle.
     *  @param y0    The minimum y in the rectangle.
     *  @param y1    The maximum y in the rectangle.
     *  @param mx    The number of pixels in a row in the output image.
     */
    private void checkRectangle(Image[] input, boolean[] valid, int[] img,
				int x0, int x1, int y0, int y1, int mx) {
	
	int p00 = x0 + y0*mx;
	int p01 = x1 + y0*mx;
	int p10 = x0 + y1*mx;
	int p11 = x1 + y1*mx;
	int i00;
	int i01;
	int i10;
	int i11;
	
	// These are the flags to use for the next recursion
	// We don't include an image unless it shows up in
	// one of the corners.
	boolean[] newValid = new boolean[valid.length];
	java.util.Arrays.fill(newValid, false);
	  
	
	// Only check corners when we don't already have a value there.
	if (img[p00] == -1) {
	    i00 = bestFit(input, p00, valid, newValid);
	    img[p00] = i00;
	} else {
	    i00 = img[p00];
	}
	if (img[p01] == -1) {
	    i01 = bestFit(input, p01, valid, newValid);
	    img[p01] = i01;
	} else {
	    i01 = img[p01];
	}
	if (img[p10] == -1) {
	    i10 = bestFit(input, p10, valid, newValid);
	    img[p10] = i10;
	} else {
	    i10 = img[p10];
	}
	if (img[p11] == -1) {
	    i11 = bestFit(input, p11, valid, newValid);
	    img[p11] = i11;
	} else {
	    i11 = img[p11];
	}
	
	if ( !strictGeometry  && (i00 == i01) && (i00 == i10) && (i00 == i11) ) {
	    
	    // All the corners match the same image, so fill in the rectangle.
	    // We'll rewrite the corners, but that's OK.
	    
	    for (int i = x0; i <= x1; i += 1) {
		for (int j=y0; j <= y1; j += 1) {
		    img[i+j*mx] = i00;
		}
	    }
	    
	} else {
	    
	    int dx = x1-x0;
	    int dy = y1-y0;
	    
	    if (dx < 2 && dy < 2) {
		// We were looking at two or four adjacent pixels so
		// they should be filled in..
		return;
	    }
	    
	    if (dx < 2) {
		
		checkRectangle(input, newValid, img, x0, x1, y0,         y0+dy/2, mx);
		checkRectangle(input, newValid, img, x0, x1, y0+dy/2+1,  y1,      mx);
		
	    } else if (dy < 2) {
		
		checkRectangle(input, newValid, img, x0,        x0+dx/2, y0, y1, mx);
		checkRectangle(input, newValid, img, x0+dx/2+1, x1,      y0, y1, mx);
		
	    } else {
		
		checkRectangle(input, newValid, img, x0,        x0+dx/2, y0,        y0+dy/2, mx);
		checkRectangle(input, newValid, img, x0+dx/2+1, x1,      y0,        y0+dy/2, mx);
		checkRectangle(input, newValid, img, x0,        x0+dx/2, y0+dy/2+1, y1,      mx);
		checkRectangle(input, newValid, img, x0+dx/2+1, x1,      y0+dy/2+1, y1,      mx);
	    }
	}
    }
    

    /** Find the best image to use for a given unit vector.
     *  @param input  The array of input images.
     *  @param pix    The output pixel we are testing (pix = x + width*y)
     *  @param valid  Should we test this image
     *  @param within Mark any images were the pixel is within the image.
     * 
     *  @return The best image, or special values.
     */
    private int bestFit(Image[] input, int pix, boolean[] valid, boolean[] within) {
	
	double[] tp = output.getCenter(pix);
	
	
//	System.err.println("Bestfit:"+pix+" "+tp[0]+" "+tp[1]);
	fromOut.transform(tp,t3);
	
//	System.err.println("   -> "+t3[0]+" "+t3[1]+" "+t3[2]);
//	double[] crd = skyview.geometry.Util.coord(t3);
//	System.err.println("       "+Math.toDegrees(crd[0])+","+Math.toDegrees(crd[1]));
	// mx is the the greatest distance from an edge that we have found
	// so far.
	double  mx    = 0;
	
	// We start by assuming that the position is not in any of the images.
	int best      = -2;
	
	// We might be off the projection...
	if (Double.isNaN(t3[0])) {
	    return -3;
	}
	
	// Check each image in turn.
	for (int i=0; i< input.length; i += 1) {
	    
	    input[i].getTransformer().transform(t3,t2);
//	    System.err.println("     ["+i+ "] -> "+t2[0]+" "+t2[1]);
	    
	    double nx = input[i].getWidth();
	    double ny = input[i].getHeight();
	    
	    double tx = t2[0];
	    double ty = t2[1];
	    
	    // Is the position within the image?
	    if (tx >= 0 && ty >= 0 && tx <= nx && ty <= ny) {
		
		// Note that this image overlaps this pixel
		// so we will consider this image if we divvy up
		// the rectangle further.
		within[i] = true;
		
		// Look for the minimum distance from the position
		// to the edge of the image.
		double mn= tx;
		if (ty < mn) {
		    mn = ty;
		}
		if (ny - ty < mn) {
		    mn = ny-ty;
		}
		if (nx - tx < mn) {
		    mn = nx-tx;
		}
		
		// If this minimum distance is greater than for any previous
		// image, then this is the best choice so far!
		if (mn > mx) {
		    mx = mn;
		    best = i;
		}
	    }
	}
	
	// Note that this image will be used.
	if (best >= 0) {
	    imageUsed[best] = true;
	}
	
	return best;
    }
    
    /** Debugging output */
    private void printOut(int[] arr, int mx) {
	
	
	int off = 0;
	while (off < arr.length) {
	    
	    if (arr[off] < 0) {
	        System.err.print(" "+arr[off]);
	    } else {
		System.err.print("  "+arr[off]);
	    }
	    off += 1;
	    if (off % mx == 0) {
		System.err.println("");
	    }
	}
    }
}
