package cfa.vo.sedlib;

import cfa.vo.sedlib.common.SedConstants;

import java.util.logging.Logger;
import java.util.List;

/**
 * This class describes a spectral segment. It provides accessors to
 * components of the segments. It also contains methods to access
 * and manipulate the data directly.
 * 
 * 
 */
public class Segment
    extends Group
{
    static Logger logger = Logger.getLogger ("cfa.vo.sedlib");
	
    protected Target target;
    protected Characterization _char;
    protected CoordSys coordSys;
    protected Curation curation;
    protected DataID dataID;
    protected DerivedData derived;
    protected ArrayOfParam customParams;
    protected TextParam type;
    protected TextParam timeSI;
    protected TextParam spectralSI;
    protected TextParam fluxSI;
    protected ArrayOfGenPoint data;


    /**
     * Gets the value of the target property.
     * 
     * @return
     *     either null or
     *     {@link Target }
     *     
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Creates target property if one does not exist.
     *
     * @return
     *     {@link Target }
     *
     */
    public Target createTarget() {
        if (this.target == null)
           this.setTarget (new Target ());
        return this.target;
    }


    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link Target }
     *     
     */
    public void setTarget(Target value) {
        this.target = value;
    }

    public boolean isSetTarget() {
        return (this.target!= null);
    }

    /**
     * Gets the value of the char property.
     * 
     * @return
     *     either null or
     *     {@link Characterization }
     *     
     */
    public Characterization getChar() {
        return _char;
    }

    /**
     * Creates _char property if one does not exist.
     *
     * @return
     *     {@link Characterization }
     *
     */
    public Characterization createChar() {
        if (this._char == null)
           this.setChar (new Characterization ());
        return this._char;
    }


    /**
     * Sets the value of the char property.
     * 
     * @param value
     *     allowed object is
     *     {@link Characterization }
     *     
     */
    public void setChar(Characterization value) {
        this._char = value;
    }

    public boolean isSetChar() {
        return (this._char!= null);
    }

    /**
     * Gets the value of the coordSys property.
     * 
     * @return
     *     either null or
     *     {@link CoordSys }
     *     
     */
    public CoordSys getCoordSys() {
        return coordSys;
    }

    /**
     * Creates coordSys property if one does not exist.
     *
     * @return
     *     {@link CoordSys }
     *
     */
    public CoordSys createCoordSys() {
        if (this.coordSys == null)
           this.setCoordSys (new CoordSys ());
        return this.coordSys;
    }


    /**
     * Sets the value of the coordSys property.
     * 
     * @param value
     *     allowed object is
     *     {@link CoordSys }
     *     
     */
    public void setCoordSys(CoordSys value) {
        this.coordSys = value;
    }

    public boolean isSetCoordSys() {
        return (this.coordSys!= null);
    }

    /**
     * Gets the value of the curation property.
     * 
     * @return
     *     either null or
     *     {@link Curation }
     *     
     */
    public Curation getCuration() {
        return curation;
    }

    /**
     * Creates curation property if one does not exist.
     *
     * @return
     *     {@link Curation }
     *
     */
    public Curation createCuration() {
        if (this.curation == null)
           this.setCuration (new Curation ());
        return this.curation;
    }

    /**
     * Sets the value of the curation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Curation }
     *     
     */
    public void setCuration(Curation value) {
        this.curation = value;
    }

    public boolean isSetCuration() {
        return (this.curation!= null);
    }

    /**
     * Gets the value of the dataID property.
     * 
     * @return
     *     either null or
     *     {@link DataID }
     *     
     */
    public DataID getDataID() {
        return dataID;
    }

    /**
     * Creates dataID property if one does not exist.
     *
     * @return
     *     {@link DataID }
     *
     */
    public DataID createDataID() {
        if (this.dataID == null)
           this.setDataID (new DataID ());
        return this.dataID;
    }


    /**
     * Sets the value of the dataID property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataID }
     *     
     */
    public void setDataID(DataID value) {
        this.dataID = value;
    }

    public boolean isSetDataID() {
        return (this.dataID!= null);
    }

    /**
     * Gets the value of the derived property.
     * 
     * @return
     *     either null or
     *     {@link DerivedData }
     *     
     */
    public DerivedData getDerived() {
        return derived;
    }

    /**
     * Creates derived property if one does not exist.
     *
     * @return
     *     {@link DerivedData }
     *
     */
    public DerivedData createDerived() {
        if (this.derived == null)
           this.setDerived (new DerivedData ());
        return this.derived;
    }


    /**
     * Sets the value of the derived property.
     * 
     * @param value
     *     allowed object is
     *     {@link DerivedData }
     *     
     */
    public void setDerived(DerivedData value) {
        this.derived = value;
    }

    public boolean isSetDerived() {
        return (this.derived!= null);
    }

    /**
     * Gets the value of the customParams property.
     * 
     * @return
     *     either null or
     *     {@link ArrayOfParam }
     *     
     */
    public ArrayOfParam getCustomParams() {
        return customParams;
    }

    /**
     * Creates customParams property if one does not exist.
     *
     * @return
     *     {@link ArrayOfParam }
     *
     */
    public ArrayOfParam createCustomParams() {
        if (this.customParams == null)
           this.setCustomParams (new ArrayOfParam ());
        return this.customParams;
    }


    /**
     * Sets the value of the customParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfParam }
     *     
     */
    public void setCustomParams(ArrayOfParam value) {
        this.customParams = value;
    }

    public boolean isSetCustomParams() {
        return (this.customParams!= null);
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     either null or
     *     {@link TextParam }
     *     
     */
    public TextParam getType() {
        return type;
    }

    /**
     * Creates type property if one does not exist.
     *
     * @return
     *     {@link TextParam }
     *
     */
    public TextParam createType() {
        if (this.type == null)
           this.setType (new TextParam ());
        return this.type;
    }


    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextParam }
     *     
     */
    public void setType(TextParam value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    /**
     * Gets the length of the point list.
     * 
     * @return int
     *     
     */
    public int getLength() {
        return data.getLength ();
    }

    /**
     * Gets the value of the timeSI property.
     * 
     * @return
     *     either null or
     *     {@link TextParam }
     *     
     */
    public TextParam getTimeSI() {
        return timeSI;
    }

    /**
     * Creates timeSI property if one does not exist.
     *
     * @return
     *     {@link TextParam }
     *
     */
    public TextParam createTimeSI() {
        if (this.timeSI == null)
           this.setTimeSI (new TextParam ());
        return this.timeSI;
    }


    /**
     * Sets the value of the timeSI property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextParam }
     *     
     */
    public void setTimeSI(TextParam value) {
        this.timeSI = value;
    }

    public boolean isSetTimeSI() {
        return (this.timeSI!= null);
    }

    /**
     * Gets the value of the spectralSI property.
     * 
     * @return
     *     either null or
     *     {@link TextParam }
     *     
     */
    public TextParam getSpectralSI() {
        return spectralSI;
    }

    /**
     * Creates spectralSI property if one does not exist.
     *
     * @return
     *     {@link TextParam }
     *
     */
    public TextParam createSpectralSI() {
        if (this.spectralSI == null)
           this.setSpectralSI (new TextParam ());
        return this.spectralSI;
    }


    /**
     * Sets the value of the spectralSI property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextParam }
     *     
     */
    public void setSpectralSI(TextParam value) {
        this.spectralSI = value;
    }

    public boolean isSetSpectralSI() {
        return (this.spectralSI!= null);
    }

    /**
     * Gets the value of the fluxSI property.
     * 
     * @return
     *     either null or
     *     {@link TextParam }
     *     
     */
    public TextParam getFluxSI() {
        return fluxSI;
    }

    /**
     * Creates fluxSI property if one does not exist.
     *
     * @return
     *     {@link TextParam }
     *
     */
    public TextParam createFluxSI() {
        if (this.fluxSI == null)
           this.setFluxSI (new TextParam ());
        return this.fluxSI;
    }


    /**
     * Sets the value of the fluxSI property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextParam }
     *     
     */
    public void setFluxSI(TextParam value) {
        this.fluxSI = value;
    }

    public boolean isSetFluxSI() {
        return (this.fluxSI!= null);
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     {@link ArrayOfGenPoint }
     *     
     */
    public ArrayOfGenPoint getData() {
        return this.data;
    }

    /**
     * Creates data property if one does not exist.
     * The data property will be an ArrayOfPoint. Use
     * setData to set the list to ArrayOfFlatPoint
     *
     * @return
     *     {@link ArrayOfPoint }
     *
     */
    public ArrayOfGenPoint createData() {
        if (this.data == null)
           this.setData (new ArrayOfPoint ());
        return this.data;
    }


    boolean isPoint()
    {
        return (this.data instanceof ArrayOfPoint);
    }


    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfPoint }
     *     {@link ArrayOfFlatPoint }
     *     
     */
    public void setData(ArrayOfGenPoint value) {
        this.data = value;
    }

    public boolean isSetData() {
        return (this.data!= null);
    }

 
    /**
     * Gets the value of the dataModel property.
     * 
     * @return
     *     {@link String }
     *     
     */   
    public String getDataModel () {
        return SedConstants.DATAMODEL;
    }

    
    /**
     * Sets the values of the spectral axis. The first n values of the
     * are set in the spectral axis. If the array is larger then the
     * number of points, then the extra values are ignored.
     *
     * @param values
     *   double[]
     *
     */
    public void setSpectralAxisValues (double values[]) 
    {
        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return;
        }
        
        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (ii == values.length)
                break;

            if (point.isSetSpectralAxis ())
            {
                DoubleParam paramValue = point.getSpectralAxis ().createValue ();
                paramValue.setValue (Double.toString (values[ii]));
            }
        }
    }

    /**
     * Gets the values of the spectral axis. If no data exists then
     * null is returned. For points where the spectral axis is not set
     * a NaN value is used.
     *
     * @return 
     *    either null or double[]
     *
     */
    public double[] getSpectralAxisValues ()
    {
        double []values = null;

        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return null;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return null;

        values = new double[points.size ()];
        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
        	
            try 
            {
            	values[ii]= (Double)point.getSpectralAxis ().getValue ().getCastValue();
            }
            catch (NullPointerException exp)
            {
            	values[ii] = Double.NaN;
            }
        }
        return values;
    }

    /**
     * Sets the values of the flux axis. The first n values of the
     * are set in the flux axis. If the array is larger then the
     * number of points, the extra values are ignored.
     *
     * @param values
     *   double[]
     *
     */
    public void setFluxAxisValues (double values[])
    {
        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (ii == values.length)
                break;

            if (point.isSetFluxAxis ())
            {
                DoubleParam paramValue = point.getFluxAxis ().createValue ();
                paramValue.setValue (Double.toString (values[ii]));
            }
        }
    }

    /**
     * Gets the values of the flux axis. If no data exists then
     * null is returned. For points where the flux axis is not set
     * a NaN value is used.
     *
     * @return 
     *   either null or double[]
     *
     */
    public double[] getFluxAxisValues ()
    {
        double []values = null;

        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return null;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return null;

        values = new double[points.size ()];
        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            try 
            {
            	values[ii]= (Double)point.getSpectralAxis ().getValue ().getCastValue();
            }
            catch (NullPointerException exp)
            {
            	values[ii] = Double.NaN;
            }
        }

        return values;
    }

    /**
     * Sets the units of the spectral axis. 
     *
     * @param units
     *    {@link String}
     *
     */
    public void setSpectralAxisUnits (String units)
    {
        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (point.isSetSpectralAxis ())
            {
                DoubleParam paramValue = point.getSpectralAxis ().getValue ();
                if (paramValue != null)
                    paramValue.setUnit (units);
            }
        }
    }

    /**
     * Gets the units of the spectral axis. If no data exists then
     * null is returned. 
     *
     * @return 
     *    either null or 
     *    {@link String}
     *
     */
    public String getSpectralAxisUnits ()
    {
        String units = null;

        if (data == null)
            return null;

        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return null;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return null;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (point.isSetSpectralAxis () && point.getSpectralAxis ().isSetValue ())
            {
                DoubleParam paramValue = point.getSpectralAxis ().getValue ();
             
                if (paramValue.isSetUnit ())
                {
                    units = paramValue.getUnit ();
                    break;
                }
            }
        }

        return units;
    }


    /**
     * Sets the units of the flux axis.
     *
     * @param units
     *    {@link String}
     *
     */
    public void setFluxAxisUnits (String units)
    {
        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (point.isSetFluxAxis ())
            {
                DoubleParam paramValue = point.getFluxAxis ().getValue ();
                if (paramValue != null)
                    paramValue.setUnit (units);
            }
        }
    }

    /**
     * Gets the units of the flux axis. If no data exists then
     * null is returned.
     *
     * @return
     *    either null or
     *    {@link String}
     *
     */
    public String getFluxAxisUnits ()
    {
        String units = null;

        if (data == null)
            return null;


        if (data instanceof ArrayOfFlatPoint)
        {
            logger.warning ("Flat points are not currently supported.");
            return null;
        }

        List<Point> points = this.getPointsFromData ();

        if (points == null)
            return null;

        for (int ii=0; ii<points.size (); ii++)
        {
        	Point point = points.get(ii);
            if (point.isSetFluxAxis () && point.getFluxAxis ().isSetValue ())
            {
                DoubleParam paramValue = point.getFluxAxis ().getValue ();

                if (paramValue.isSetUnit ())
                {
                    units = paramValue.getUnit ();
                    break;
                }
            }

        }

        return units;
    }


    /**
     * Gets the list of points from the data. This assumes points are
     * Point types and not FlatPoint. If the type is FlatPoint null is
     * returned;
     *
     * @return 
     *    either null or List<Point>
     *    {@link Point}
     *
     */
    protected List<Point> getPointsFromData ()
    {
        if (data == null)
            return null;

        if (data instanceof ArrayOfFlatPoint)
            return null;

        return ((ArrayOfPoint)data).getPoint ();
    }
}

