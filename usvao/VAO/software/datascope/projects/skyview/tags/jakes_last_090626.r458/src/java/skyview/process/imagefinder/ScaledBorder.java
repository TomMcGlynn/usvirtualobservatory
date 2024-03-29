package skyview.process.imagefinder;

/** This class works almost exactly like
 *  the BorderImageFinder, however when calculating
 *  the distance from the edge of the image, the distance
 *  is scaled to diagonal dimension of the image.
 */
public class ScaledBorder extends Border {

	
    /** Given a point at x,y in an image of size a,b
     *  in the rectangle 0,a 0,b
     *  find the minimum distance to the edge.  We
     *  assume that x,y is contained in the rectangle.
     *  If x,y is outside the rectangle, then this
     *  should return a negative number.  
     *  In this version the distance is scaled according
     *  to the size of the image.  Unfortunately we cannot
     *  assume that this is constant.  This ImageFinder is 
     *  called when we have images with substantially different
     *  sizes in the survey.
     */
    protected double minDist(double x, double y, double a, double b) {	
	return  Math.min(Math.min(x, a-x), Math.min(y, b-y))/Math.sqrt(a*a+b*b);
    }
}
