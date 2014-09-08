package cfa.vo.sedlib;

/**
 * <p>Java class for field complex type.
 * 
 * 
 */
public class Field {

    protected String name;
    protected String unit;
    protected String ucd;
    protected String utype;

    public Field () {};

    public Field (String name, String ucd, String unit, String utype)
    {
       this.unit = unit;
       this.name = name;
       this.ucd = ucd;
       this.utype = utype;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     either null or
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
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
     * Gets the value of the ucd property.
     * 
     * @return
     *     either null or
     *     {@link String }
     *     
     */
    public String getUcd() {
        return ucd;
    }

    /**
     * Sets the value of the ucd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUcd(String value) {
        this.ucd = value;
    }

    public boolean isSetUcd() {
        return (this.ucd!= null);
    }

    /**
     * Gets the value of the utype property.
     *
     * @return
     *     either null or
     *     {@link String }
     *
     */
    public String getUtype() {
        return utype;
    }

    /**
     * Sets the value of the utype property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUtype(String value) {
        this.utype = value;
    }

    public boolean isSetUtype() {
        return (this.utype!= null);
    }


}
