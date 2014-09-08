package cfa.vo.sedlib;

/**
 * <p>Java class for doubleParam complex type.
 * 
 * 
 */
public class DoubleParam
    extends Param
{

    protected String unit;

    public DoubleParam () {};

    public DoubleParam (DoubleParam param)
    {
        super (param);
        this.unit = param.unit;
    }

    public DoubleParam (String value, String name, String ucd, String unit)
    {
        super (value, name, ucd);
        this.unit = unit;
        
        if (value.equalsIgnoreCase("NaN"))
            System.out.println ("FOUND NAN");
        
        try
        {
            Double.parseDouble (value);
        }
        catch (Exception e)
        {
            this.value = null;
        }
    } 

    public DoubleParam (String value)
    {
        super (value);

        if (value.equalsIgnoreCase("NaN"))
            System.out.println ("FOUND NAN");

        try
        {
            Double.parseDouble (value);
        }
        catch (Exception e)
        {
            this.value = null;
        }

    }

    public DoubleParam (Double value)
    {
        super (value.toString ());
    }

    public DoubleParam (Double value, String name, String ucd, String unit)
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
     * Gets the value of the value property cast to the data type.
     *
     * @return
     *     either null or a Double cast as Object
     *     {@link Double }
     *
     */
    public Object getCastValue() {
        if (this.value != null)
            return new Double(this.value);
        return null;
    }


}
