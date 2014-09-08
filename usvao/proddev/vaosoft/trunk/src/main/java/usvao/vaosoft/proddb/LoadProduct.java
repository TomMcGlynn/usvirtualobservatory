package usvao.vaosoft.proddb;


/**
 * an interface for allowing raw product data to be loaded.
 */
public interface LoadProduct {

    /**
     * The required index in the input String array for the product name.
     */
    int NAME = 0;

    /**
     * The required index in the input String array for the product version.
     */
    int VERSION = 1;

    /**
     * The required index in the input String array for the identifier of 
     * the organization responsible for releases of the product.
     */
    int ORG = 2;

    /**
     * The required index in the input String array for the directory where
     * the product is installed.
     */
    int HOME = 3;

    /**
     * The required index in the input String array for the name of the file
     * or directory whose existance can be used of proof that it is installed.
     */
    int PROOF = 4;

    /**
     * The required index in the input String array for extended product
     * properties.  Any existing elements appearing after this index will
     * also be assumed to contain encoded property data.
     */
    int PROPS = 5;

    /**
     * load data about a product.  The data is provided as an array of 
     * Strings.  What each element represents is defined by the int
     * constants in this class.  
     */
    public void loadProduct(String[] data);

}
