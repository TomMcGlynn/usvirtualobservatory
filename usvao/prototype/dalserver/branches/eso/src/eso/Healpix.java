package eso;
import java.util.ArrayList;
import gov.fnal.eag.healpix.PixTools;
import javax.vecmath.Vector3d;
import org.eso.vos.intersection.Point;
import org.eso.vos.intersection.Polygon;
import org.eso.vos.intersection.Ray;

/**
 * Manage Healpix
 * @author Jean-Christophe Malapert
 * @version 0.1
 */
public class Healpix extends PixTools
{
    
    /**
     * Construstor
     *
     */
    public Healpix()
    {
        
    }
    
    /**
     * Constructor from a pixel area
     * @param area Pixel's area
     */
    public Healpix(double area)
    {
        double pow = Math.floor(0.5*Math.log(4*180*180/Math.PI/area/12)/Math.log(2));
        this.nSide = (int)Math.pow(2, pow);
        this.nPix = 12 * this.nSide * this.nSide;
        this.nRing = 4*this.nSide-1;
    }
    
    /**
     * Constructor from nSide parameter
     * @see http://healpix.jpl.nasa.gov/html/intronode3.htm
     * @param nSide Grid resolution
     */
    public Healpix(long nSide)
    {
        this.nSide = nSide;
        this.nPix = 12*nSide*nSide;
        this.nRing = 4*nSide-1;
    }
    
    
    private long nSide, nPix, nRing;
    
    /**
     * Get nSide number
     * @return nSide number
     */
    public long getnSide()
    {
        return this.nSide;
    }
    
    
    /**
     * Get nPix number
     * @return nPix number
     */
    public long getnPix()
    {
        return this.nPix;
    }
    
    /**
     * get ring number
     * @return ring number
     */
    public long getnRing()
    {
        return this.nRing;
    }
    
    
    /** Compute 3d vector from input coordinates ra and dec
     *
     * @param ra
     * @param dec
     * @return
     */
    public Vector3d ang2Vect(double ra, double dec)
    {
        return this.Ang2Vec(Math.toRadians(90 - dec), Math.toRadians(ra));
    }
    
    /**
     * Sort pixels
     * @param listPixels List of pixels
     * @return Array[value,number]
     */
    public ArrayList<Long[]> rangePixels(ArrayList listPixels)
    {
        ArrayList<Long[]> list = new ArrayList<Long[]>();
        ArrayList listInOut = new ArrayList();
        long firstValue=Long.valueOf(listPixels.get(0).toString());
        long incr = 0;
        int step=0;
        for(int i=1;i<listPixels.size();i++)
        {
            if((firstValue+i-step) == Long.valueOf(listPixels.get(i).toString()))
            {
                incr++;
            }
            else
            {
                list.add(new Long[]{firstValue,incr});
                firstValue = Long.valueOf(listPixels.get(i).toString());
                step=step+1+(int) incr;
                incr = 0;
            }
        }
        list.add(new Long[]{firstValue,incr});
        return list;
    }
    
    /**
     * Retrieves the pixel numbers intersecting with a polygon following ra and dec lines
     * @param decmin Minimum declination
     * @param decmax Maximum declination
     * @param ra polygon center of the rigth ascension
     * @param dra delta ra [ra-dphi,ra+dphi]
     * @return pixel numbers
     */
    public ArrayList queryPolygon(double decmin, double decmax, double ra, double dra)
    {
        long ringMin = this.RingNum(this.nSide, Math.sin(Math.toRadians(decmax)) );
        long ringMax = this.RingNum(this.nSide, Math.sin(Math.toRadians(decmin)));
        ArrayList pixelsInRing=null;
        ArrayList pixels = null;
        
        for(long i=ringMin;i<=ringMax;i++)
        {
            pixelsInRing = this.InRing(this.nSide, i, Math.toRadians(ra) , Math.toRadians(dra), false);
            if(pixels == null)
                pixels = pixelsInRing;
            else
                pixels.addAll(pixelsInRing);
        }
        return pixels;
    }
    
    /** Retrieves the pixels numbers intersecting the Cone obtained from the 3d vector calculated using input ra and dec
     *
     * @param vector
     * @param radius
     * @return
     */
    public ArrayList<Long[]> intersectAgainstCone(Vector3d vector,double radius)
    {
        return this.query_disc(this.nSide,vector,Math.toRadians(radius),0,1);
    }
    
    /** Build the query in healpix range
     *
     * @param listRange
     * @return
     */
    public StringBuilder clauseWhere(ArrayList <Long[]> listRange)
    {
        listRange = this.rangePixels(listRange);
        StringBuilder requestBetween = new StringBuilder();
        StringBuilder requestIn = new StringBuilder("healpix_id in (");
        boolean firstBetween=true;
        boolean firstIn=true;
        for(int i=0;i<listRange.size();i++)
        {
            Long[] range = listRange.get(i);
            if(range[1]>0)
            {
                if(firstBetween)
                {
                    requestBetween.append(" ( healpix_id between ");
                    requestBetween.append(range[0]);
                    requestBetween.append(" and ");
                    requestBetween.append(range[0]+range[1]);
                }
                else
                {
                    requestBetween.append(" OR healpix_id between ");
                    requestBetween.append(range[0]);
                    requestBetween.append(" and ");
                    requestBetween.append(range[0]+range[1]);
                }
                firstBetween = false;
            }
            else
            {
                if(firstIn)
                {
                    requestIn.append(range[0]);
                }
                else
                {
                    requestIn.append(",");
                    requestIn.append(range[0]);
                }
                firstIn = false;
            }
        }
        StringBuilder clauseWhere = new StringBuilder();
        if(firstIn)
        {
            if(firstBetween)
            {
            }
            else
            {
                requestBetween.append(")");
                clauseWhere.append(requestBetween);
            }
        }
        else
        {
            requestIn.append(")");
            clauseWhere.append(requestIn);
            if (firstBetween)
            {
            }
            else
            {
                requestBetween.append(")");
                clauseWhere.append(" OR ");
                clauseWhere.append(requestBetween);
            }
        }
        return clauseWhere;
    }
    
    public ArrayList<Point> getHealpixVectors(double[] convex)
    {
        // List of healpix vectors       
        ArrayList<Point> listHealpixVectors = new ArrayList<Point>();
        for(int i=0;i<convex.length;i=i+2) {
            listHealpixVectors.add(new Point(this.Ang2Vec(Math.toRadians(90 - (convex[i+1])), Math.toRadians(convex[i]))));
        }
        return listHealpixVectors;
    }
    
    
    private ArrayList transform2FuckingArray(ArrayList<Point> listHealpixVectors) {
        ArrayList list = new ArrayList();
        for (int i=0 ; i<listHealpixVectors.size(); i++) {
            list.set(i,listHealpixVectors.get(i).vector());
        }
        return list;
    }
    
    
    public ArrayList getListPixels(ArrayList<Point> listHealpixVectors)
    {
        ArrayList listPixels = new ArrayList();              
        
        
        Vector3d v1v0 = new Vector3d();
        Vector3d v2v0 = new Vector3d();
        Vector3d crossVect = new Vector3d();
        
        v1v0.sub(listHealpixVectors.get(1).vector(), listHealpixVectors.get(0).vector());
        v2v0.sub(listHealpixVectors.get(2).vector(), listHealpixVectors.get(0).vector());
        crossVect.cross(v1v0,v2v0);
        double d=crossVect.dot(listHealpixVectors.get(0).vector());
        
        if (d>0)
        {
            Polygon polygon = new Polygon(listHealpixVectors);
            
                /* Bug in healpix library for the query_polygon
                 * at the pole. Test if the FOV is at the pole and
                 * performs another query using RING
                 */
            
            // South pole is in the FOV
            if(polygon.intersection(new Ray(new Point(0,0,-1.1),new Point(0,0,0))))
            {
                long ringMax = this.RingNum(this.getnSide(),-1);
                long ringMin = 0;
                double z = listHealpixVectors.get(0).vector().z;
                
                // Both north pole and south pole in the FOV
                // We compute the ringMin
                if(polygon.intersection(new Ray(new Point(0,0,1.1),new Point(0,0,0))))
                {
                    ringMin = this.RingNum(this.getnSide(),1);
                }
                else
                {
                    for(int index=1;index<listHealpixVectors.size();index++)
                    {
                        if(listHealpixVectors.get(index).vector().z > z)
                            z = listHealpixVectors.get(index).vector().z;
                    }
                    
                    ringMin = this.RingNum(this.getnSide(),z);
                }
                
                // We store all the sky between ringMin and ringMax
                for(long i=ringMin;i<ringMax;i++)
                {
                    listPixels.addAll(this.InRing(this.getnSide(),i,0,Math.toRadians(180),false));
                }
                
            }
            // Only north pole is in the FOV
            else if(polygon.intersection(new Ray(new Point(0,0,1),new Point(0,0,0))))
            {
                long ringMin = this.RingNum(this.getnSide(),1);
                double z = listHealpixVectors.get(0).vector().z;
                for(int index=1;index<listHealpixVectors.size();index++)
                {
                    if(listHealpixVectors.get(index).vector().z < z)
                        z = listHealpixVectors.get(index).vector().z;
                }
                long ringMax = this.RingNum(this.getnSide(),z);
                for(long i=ringMin;i<ringMax;i++)
                {
                    listPixels.addAll(this.InRing(this.getnSide(),i,0,Math.toRadians(180),false));
                }
            }
            // Pole not in the FOV
            // Normal query hoping query_polygon works well
            else
            {
                try
                {
                    ArrayList listv = transform2FuckingArray(listHealpixVectors);
                    listPixels = this.query_polygon(this.getnSide(), listv, 0, 1);
                }
                catch (Exception e)
                {
                    throw new IllegalAccessError("Query Polygon for Healpix failed");
                }
            }
            
        }
        else
        {
            listPixels = this.query_disc(this.getnSide(), crossVect, Math.acos(d), 0, 1);
        }
        
        return listPixels;
    }
    
    private ArrayList <Vector3d> vectorListHealpix(double[] box, boolean mode_convex)
    {
        Healpix healpix = new Healpix();
        ArrayList <Vector3d> list = null;
        if(!mode_convex)
        {
            list = new ArrayList <Vector3d>(4);
            // 1st point (ramin,decmin)
            list.add(healpix.Ang2Vec(Math.toRadians(90 - (box[2])), Math.toRadians(box[0])));
            // 2nd point (ramin.decmax
            list.add(healpix.Ang2Vec(Math.toRadians(90 - box[3]), Math.toRadians(box[0])));
            // 3rd point (ramax,decmax)
            list.add(healpix.Ang2Vec(Math.toRadians(90 - box[3]), Math.toRadians(box[1])));
            // 4th point (ramax,decmin)
            list.add(healpix.Ang2Vec(Math.toRadians(90 - box[2]), Math.toRadians(box[1])));
        }
        else
        {
            list = new ArrayList <Vector3d>(box.length);
            for(int i=0;i<box.length;i=i+2)
                list.add(healpix.Ang2Vec(Math.toRadians(90 - (box[i+1])), Math.toRadians(box[i])));
        }
        return list;
    }
}
