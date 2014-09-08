package cfa.vo.sedlib;

/**
 * <p>Java class for param complex type.
 * 
 * 
 */
public class Param {

    protected String value;
    protected String name;
    protected String ucd;

    public Param () {}

    public Param (Param param)
    {
        this.value = param.value;
        this.name = param.name;
        this.ucd = param.ucd;
    }

    public Param (String value, String name, String ucd)
    {
       this.value = value;
       this.name = name;
       this.ucd = ucd;
    }

    public Param (String value)
    {
       this.value = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     either null or
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the value of the value property cast to the data type
     *
     * @return
     *     either null or String cast as Object
     *     {@link String }
     *
     */
    public Object getCastValue() {
        return value;
    }


    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSetValue() {
        return (this.value!= null);
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

}
