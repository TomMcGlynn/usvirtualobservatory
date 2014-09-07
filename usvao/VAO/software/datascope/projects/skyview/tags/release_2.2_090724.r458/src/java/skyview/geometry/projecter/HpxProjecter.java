package skyview.geometry.projecter;

/** This class implements the HEALPix map projection
 *  as defined by Calabretta and Roukema.  This defines
 *  a 2-d representation for the HEALPix 1-D pixelization.
 */

import skyview.geometry.Projecter;
import skyview.geometry.Deprojecter;
import skyview.geometry.Transformer;

public class HpxProjecter extends Projecter {
    
    // The parameters H and K define the type
    // of projection.  The standard HEALPIX
    // projection has H=4 and K=3.
    private final double H      = 4;
    private final double K      = 3;
    
    // The boundary between the polar and equatorial regions
    // of the projection.
    private final double thetaC = Math.asin(2./3.);
    private final double yC     = Math.PI*(K-1)/H;
     
    
    /** Get the name of the compontent */
    public String getName() {
	return "HpxProjecter";
    }
    /** Get a description of the component */
    public String getDescription() {
	return "Project HEALPix pixelization";
    }
	       
    /** Project a point from the sphere to the plane.
     *  We have tried to do a literal translation of
     *  the equations in Calbretta and Roukema including
     *  operations that we could omit. 
     * 
     *  @param sphere a double[3] unit vector
     *  @param plane  a double[2] preallocated vector.
     */
    public final void transform(double[] sphere, double[] plane) {
	
	if (Double.isNaN(sphere[2]) || sphere[2] < 0) {
	    plane[0] = Double.NaN;
	    plane[1] = Double.NaN;
	} else {
	    double phi   = Math.atan2(sphere[1], sphere[0]);
	    double theta = Math.asin(sphere[2]);
	    
	    if (Math.abs(theta) <= thetaC) {
		plane[0] = phi;
		plane[1] = Math.PI/2 * K/H * sphere[2];
		
	    } else {
		
		double sigma = Math.sqrt(K*(1-Math.abs(sphere[2])));
		double omega = 0;
		if (K%2 == 1 || theta > 0) {
		    omega = 1;
		}
		double phi_c = -Math.PI + 
		          (2*Math.floor( (phi+Math.PI)*H/(2*Math.PI) + 
					  (1-omega)/2 )
			     + omega ) * Math.PI/4;
		
		plane[0] = phi_c + (phi-phi_c)*sigma;
		plane[1] = Math.PI/H*((K+1)/2-sigma);
		if (theta < 0) {
		    plane[1] = -plane[1];
		}
	    }
	}
    }
    
    /** Get the inverse transformation */
    public Deprojecter inverse() {
	return new HpxDeprojecter();
    }
    
    /** Is this an inverse of some other transformation? */
    public boolean isInverse(Transformer t) {
	return t.getName().equals("HpxDeprojecter");
    }
    public class HpxDeprojecter extends Deprojecter {
	
	/** Get the name of the component */
	public String getName() {
	    return "HpxDeprojecter";
	}
	
	/** Get a description of the component */
	public String getDescription() {
	    return "Transform from HEALPix plane to sphere";
	}
	
	/** Get the inverse transformation */
	public Projecter inverse() {
	    return HpxProjecter.this;
	}
        /** Is this an inverse of some other transformation? */
        public boolean isInverse(Transformer t) {
	    return t.getName().equals("HpxProjecter");
        }
    
        /** Deproject a point from the plane to the sphere.
         *  @param plane a double[2] vector in the tangent plane.
         *  @param spehre a preallocated double[3] vector.
         */
        public final void  transform(double[] plane, double[] sphere) {
	
	    
	    if (Double.isNaN(plane[0])) {
	        sphere[0] = Double.NaN;
	        sphere[1] = Double.NaN;
	        sphere[2] = Double.NaN;
	    
	    } else {
		
		double phi;
		if (Math.abs(plane[1]) <= yC) {
		    
		    phi = plane[0];
		    sphere[2] = plane[1]*H/K;
		    
		} else {
		    double omega = 0;
		    if (K%2 == 1 || sphere[2] > 0) {
			omega = 1;
		    }
		    double sigma =  (K+1)/2 - Math.abs(plane[1]*H)/Math.PI;
		    
		    double x_c   = -Math.PI + 
		                (2*Math.floor((plane[0]+Math.PI)*H/(2*Math.PI) +
				      + (1-omega)/2) + omega)*Math.PI/H;
		    
		    phi   = x_c + (plane[0]-x_c)/sigma;
		    sphere[2]    = 1-sigma*sigma/K;
		    if (plane[1] < 0) {
			sphere[2] = -sphere[2];
		    }
		}
		sphere[1] = Math.sin(phi);
	        sphere[0] = Math.cos(phi);
	    }
        }
    }
}
