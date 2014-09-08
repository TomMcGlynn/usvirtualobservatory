package cfa.vo.sedlib;

import cfa.vo.sedlib.common.SedException;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;


/**
 * This class describes a collection of segments. It provides methods
 * to access segments and augment the segments in the collection.
 * 
 */

public class Sed 
{
    protected List<Segment> segmentList;
    protected String namespace;

    static Logger logger = Logger.getLogger ("cfa.vo.sedlib");

    /**
     * Add a segment to the Sed. The routine also
     * verifies that the incoming segment flux axis qualities match
     * the flux axis qualities from existing segments. Null qualities
     * are always assumed to be valid and matching.
     * @param segment
     *   Segment
     *
     * @throws SedException
     */
    public void addSegment (Segment segment) throws SedException
    {
        this.addSegment (segment, 0);
    }


    /**
     * Add a segment to the Sed at particular offset. The routine also
     * verifies that the incoming segment flux axis qualities match
     * the flux axis qualities from existing segments. Null qualities
     * are always assumed to be valid and matching.
     * @param segment
     *   Segment
     * @param offset
     *   int
     *
     * @throws SedException
     */
    public void addSegment (Segment segment, int offset) throws SedException
    {
        String ucd = null;
        String unit = null;
        List<Point> points;

        if (this.segmentList == null)
            this.segmentList = new ArrayList<Segment>();

        // loop through the segments to find a ucd or unit
        for (Segment currSegment : this.segmentList)
        {
            points = currSegment.getPointsFromData ();
            if (points == null)
                continue;

            // loop through all the points in the data and try to find
            // a ucd and unit value
            for (Point point : points)
            {
                if (point.isSetFluxAxis () && point.getFluxAxis ().isSetValue ())
                {
                    if (point.getFluxAxis ().getValue ().isSetUcd ())
                        ucd = point.getFluxAxis ().getValue ().getUcd ();
                    if (point.getFluxAxis ().getValue ().isSetUcd ())
                        unit = point.getFluxAxis ().getValue ().getUnit ();
                }

                // these should all be the same so all we need it for one point
                if ((ucd != null) && (unit != null))
                    break;
            }

            // these should all be the same so all we need it for one point
            if ((ucd != null) && (unit != null))
                break;
        }

        // go through the points for the input segment and verify that the ucd
        // and units are the same and match the existing qualities
        points = segment.getPointsFromData ();
        if (points != null)
        {
            String currUcd;
            String currUnit;
            for (Point point : points)
            {
                if (point.isSetFluxAxis () && point.getFluxAxis ().isSetValue ())
                {
                    currUcd = point.getFluxAxis ().getValue ().getUcd ();
                    currUnit = point.getFluxAxis ().getValue ().getUnit ();

                    // if the default ucd and unit are null -- we still
                    // should verify all the pionts have the same ucd and unit
                    if (ucd == null)
                        ucd = currUcd;

                    if (unit == null)
                        unit = currUnit;

                    if ((ucd != null) && (currUcd != null) && !ucd.equalsIgnoreCase(currUcd))
                        throw new SedException ("The current flux axis ucd, "+ucd+", does not match the incoming ucd, "+currUcd);
                    if ((unit != null) && (currUnit != null) && !unit.equalsIgnoreCase(currUnit))
                        throw new SedException ("The current flux axis unit, "+unit+", does not match the incoming unit, "+currUnit);

                }
            }
        }

        segmentList.add (offset, segment);
    }


    /**
     * Add a list of segments to the Sed. The routine also
     * verifies that the incoming segment flux axis qualities match
     * the flux axis qualities from existing segments. Null qualities
     * are always assumed to be valid and matching.
     * @param segments
     *   List<{@link Segment}>
     *
     * @throws SedException
     */
    public void addSegment (List<Segment> segments) throws SedException
    {
        this.addSegment (segments, 0);
    }

    /**
     * Add a list of segments to the Sed starting at particular offset. The routine
     * also verifies that the incoming segment flux axis qualities match
     * the flux axis qualities from existing segments. Null qualities
     * are always assumed to be valid and matching.
     * @param segments
     *   Segment
     * @param offset
     *   int
     *
     * @throws SedException
     */
    public void addSegment (List<Segment> segments, int offset) throws SedException
    {
        for (int ii=0; ii<segments.size (); ii++)
            this.addSegment (segments.get(ii), ii+offset);
    }


    /**
     * Remove a segment from the Sed.
     * @param segment
     *   int
     *   
     */
    public void removeSegment (int segment)
    {
        if ((this.segmentList == null) || (this.segmentList.isEmpty ()))
            logger.warning ("There are no segments in this Sed");

        if ((segment < 0) || (segment > segmentList.size ()))
            logger.warning ("The specified segment is outside the range of existing segments.");

        segmentList.remove (segment);
    }

    /**
     * Get the number of segments in the Sed.
     * @return int
     *
     */
    public int getNumberOfSegments ()
    {
        if (this.segmentList == null)
            return 0;
        return this.segmentList.size ();
    }

    /**
     * Get the specified of segment from the Sed. If the list is empty or the
     * the specified segment does not exist, then null is returned.
     * @param segment
     *   int
     * @return 
     *    {@link Segment}
     *
     */
    public Segment getSegment (int segment)
    {
        if (this.segmentList == null)
            return null;
        if ((segment < 0) || (segment > segmentList.size ()))
            return null;

        return this.segmentList.get (segment);
    }


    /**
     * Sets the namespace associated with this Sed
     *
     * @param namespace
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNamespace (String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * Gets the namespace associated with this Sed
     *
     * @return 
     *     {@link String }
     *
     */
    public String getNamespace ()
    {
        return this.namespace;
    }

    public boolean isSetNamespace()
    {
        return (this.namespace!= null);
    }

}
