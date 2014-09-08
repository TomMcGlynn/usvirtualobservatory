package cfa.vo.sedlib;

/**
 * <p>Java class for intParam complex type.
 * 
 * 
 */
public class IntParam
    extends Param
{

    protected String unit;

    public IntParam () {};

    public IntParam (IntParam param)
    {
        super (param);
        this.unit = param.unit;
    }

    public IntParam (String value)
    {
        super (value);

        try
        {
            Integer.parseInt(value);
        }
        catch (Exception e)
        {
            this.value = null;
        }

    }

    public IntParam (String value, String name, String ucd, String unit)
    {
        super (value, name, ucd);
        this.unit = unit;

        try
        {
            Integer.parseInt (value);
        }
        catch (Exception e)
        {
            this.value = null;
        }

    }

    public IntParam (Integer value)
    {
        super (value.toString ());
    }

    public IntParam (Integer value, String name, String ucd, String unit)
    {
        this(value.toString (), name, ucd, unit);
    }


    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     either null or
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }


    public boolean isSetUnit() {
        return (this.unit!= null);
    }

    /**
     * Gets the value of the value property cast to the data type
     *
     * @return
     *     either null or a Integer cast as Object
     *     {@link String }
     *
     */
    public Object getCastValue() {
        if (value != null)
            return Integer.valueOf(value);
        return null;
    }


}
