package cfa.vo.sedlib;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for arrayOfPoint complex type.
 * 
 * 
 */
public class ArrayOfPoint
    extends ArrayOfGenPoint
{

    protected List<Point> point;


    /**
     * Gets the point list.
     *
     * @return List<Point>
     *   either null or List<Point>
     *   {@link Point}
     *
     */
    public List<Point> getPoint() {
        return this.point;
    }

    /**
     * Creates the point list if one does not exist.
     *
     * @return List<Point>
     *   {@link Point}
     *
     */
    public List<Point> createPoint() {
        if (this.point == null) {
            this.point = new ArrayList<Point>();
        }
        return this.point;
    }
    public boolean isSetPoint() {
        return (this.point!= null);
    }

    /**
     * Sets the point list to a new list
     *
     * @param point
     *     allowed object is List<Point>
     *     {@link Point }
     *
     */
    public void setPoint(List<Point> point) {
        this.point = point;
    }

    /**
     * Gets the length of the point list.
     *
     */
    public int getLength()
    {
        if (this.point == null)
            return 0;
        return this.point.size ();
    }


}
