package usvao.vaosoft.proddb;

import usvao.vaosoft.proddb.version.BasicVersionHandler;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import static usvao.vaosoft.proddb.LoadProduct.*;

/**
 * a Product line featuring fast in-memory lookups.  
 */
public class BasicProductLine extends ProductLine implements LoadProduct {

    VersionHandler vh = null;
    SortedMap<String, String[]> versions = null;
    HashMap<String, String> tags = null;

    public static final String LATEST = "latest";

    protected BasicProductLine(String prodname, String org, VersionHandler vh,
                               StackConfig config, List<String> preferredTags,
                               SortedMap<String, String[]> data, 
                               HashMap<String, String> tags)
    {
        super(prodname, org, config, preferredTags);
        if (vh == null) vh = config.getVersionHandler();
        this.vh = vh;

        versions = data;
        this.tags = tags;
    }

    public BasicProductLine(String prodname, String org, VersionHandler vh,
                            StackConfig config)
    {
        this(prodname, org, vh, config, null, 0);
    }

    public BasicProductLine(String prodname, String org, VersionHandler vh,
                            StackConfig config, List<String> preferredTags,
                            int initTagCount)
    {
        this(prodname, org, vh, config, preferredTags, null, null);
        versions = new TreeMap<String, String[]>(this.vh.getComparator());
        if (initTagCount <= 0) initTagCount = 4;
        tags = new HashMap<String, String>(initTagCount);
    }

    /**
     * return true if this line includes a given version
     */
    public boolean hasVersion(String version) { 
        return versions.containsKey(version);
    }

    void addVersion(String version, String[] data) {
        versions.put(version, data);
    }

    void addVersion(String version, String home, String proof, String props) {
        String [] data = { home, proof, props };
        addVersion(version, data);
    }

    /**
     * load data about a product.  The data is provided as an array of 
     * Strings.  What each element represents is defined by the int
     * constants of the LoadProduct interface.  This implementation will
     * ignore any data with the wrong product name.
     */
    public void loadProduct(String[] data) {
        if (data[NAME] == null || data[NAME].length() == 0 || 
            data[NAME].equals(getProductName())) 
        {
            String props = null;
            int pi = PROPS;
            while (pi < data.length && data[pi] == null) pi++;
            if (pi < data.length) {
                StringBuffer buf = new StringBuffer(data[pi]);
                for(pi++; pi < data.length; pi++) 
                    buf.append('\t').append(data[pi]);
                props = buf.toString();
            }
            
            addVersion(data[VERSION], data[HOME], data[PROOF], props);
        }
    }

    void tagVersion(String tag, String version) {
        tags.put(tag, version);
    }

    /**
     * return the number of products in this database
     */
    public int getCount() { return versions.size(); }

    /**
     * return the ordered set of the names of the versions in this 
     * ProductLine.
     */
    public Set<String> versionSet() { return versions.keySet(); }

    /**
     * return a list of products that match the given name and version 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, all versions matching the 
     *                              name and platform will be returned.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the versions appropriate for this platform.
     * @return ProductDB  a set of matching products.  
     */
    public ProductLine matchVersions(String versionConstraint,
                                     String platform) 
    {
        if (versionConstraint == null || versionConstraint.length() == 0)
            return this;

        VersionMatcher vm = vh.getMatcher(versionConstraint);
        SortedMap<String, String[]> sub = null;
        if (vm instanceof VersionRangeMatcher) 
            sub = subrange((VersionRangeMatcher) vm);
        else 
            sub = pick(vm);

        return new BasicProductLine(name, org, vh, config, prefTags, sub, tags);
    }

    private SortedMap<String, String[]> pick(VersionMatcher rm) {
        SortedMap<String, String[]> out = 
            new TreeMap<String, String[]>(vh.getComparator());
        Iterator<Map.Entry<String, String[]> > it = 
            versions.entrySet().iterator();
        Map.Entry<String, String[]> entry = null;
        while (it.hasNext()) {
            entry = it.next();
            if (rm.matches(entry.getKey())) 
                out.put(entry.getKey(), entry.getValue());
        }

        return out;
    }

    private SortedMap<String, String[]> subrange(VersionRangeMatcher rm) {
        if (rm.getMinVersion() == null && rm.getMaxVersion() == null) 
            return versions;

        SortedMap<String, String[]> out = versions;
        if (rm.getMinVersion() != null) 
            out = getMinRange(out, rm.getMinVersion(), rm.isInclusiveMin());
        if (rm.getMaxVersion() != null)
            out = getMaxRange(out, rm.getMaxVersion(), rm.isInclusiveMax());

        return out;
    }

    private SortedMap<String, String[]> 
        getMinRange(SortedMap<String, String[]> in, String min, boolean incl)
    {
        SortedMap<String, String[]> out = in.tailMap(min);
        if (! incl && out.size() > 0 && min.equals(out.firstKey())) {
            Iterator<String> it = out.keySet().iterator();
            it.next();
            if (! it.hasNext()) return new TreeMap<String, String[]>();
            out = in.tailMap(it.next());
        }
        return out;
    }
    
    private SortedMap<String, String[]> 
        getMaxRange(SortedMap<String, String[]> in, String max, boolean incl)
    {
        if (incl) {
            SortedMap<String, String[]> tail = in.tailMap(max);
            if (tail.size() > 0 && max.equals(tail.firstKey())) {
                Iterator<String> it = tail.keySet().iterator();
                it.next();
                if (! it.hasNext()) return in;
                max = it.next();
            }
        }
        return in.headMap(max);
    }
    
    /**
     * return the product matching the exact constraints
     * @param version    the exact version of a product 
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public Product getVersion(String version, String platform) {
        String[] data = versions.get(version);
        if (data == null) return null;

        return makeProduct(name, version, org, config, data);
    }

    private Product makeProduct(String name, String version, String org, 
                                StackConfig stack, String[] data) 
    {
        Properties props = BasicProduct.parseProperties(data, 2);
        return new BasicProduct(name, version, org, data[0], data[1], 
                                stack, props);
    }
                              
    /**
     * return the product matching the exact constraints
     * @param tag        the product that is has this tag assigned to it.
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public Product getVersionByTag(String tag, String platform) {
        String version = tags.get(tag);
        if (version == null) {
            if (tag.equals(LATEST))
                return getLatest(platform);
            return null;
        }
        return getVersion(version, platform);
    }
                                            
    /**
     * return a version of a product that matches the given constraints 
     * and with the most preferred tag. 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, the preferred version will 
     *                              be picked from all available versions.
     * @param tags   a list of preferred tags used to select from the 
     *                  versions selected by versionConstraint.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     */
    public Product selectVersion(String versionConstraint, List<String> tags, 
                                 String platform)
    {
        if (tags == null) tags = prefTags;
        ProductLine line = matchVersions(versionConstraint, platform);
        Product out = null;
        if (tags != null) {
            Iterator<String> it = tags.iterator();
            while (out == null && it.hasNext()) {
                out = line.getVersionByTag(it.next(), platform);
            }
        }
        if (out == null) out = line.getLatest(platform);
        return out;
    }

    
                                 
                                 
    /**
     * return the latest version in this product line appropriate for the 
     * given platform.
     */
    public Product getLatest(String platform) {
        if (getCount() == 0) return null;
        return getVersion(versions.lastKey(), platform);
    }

    /**
     * return an iterator for iterating through the raw product data
     */
    public Iterator<String[]> rawIterator() { return new RawIterator(); }

    class RawIterator implements Iterator<String[]> {
        Iterator<Map.Entry<String,String[]> > di = 
            versions.entrySet().iterator();
        public boolean hasNext() { return di.hasNext(); }
        public String[] next() {
            Map.Entry<String,String[]> entry = di.next();
            int i = 0;
            String[] data = new String[PROPS+1];
            data[NAME] = getProductName();
            data[ORG] = getOrg();
            data[VERSION] = entry.getKey();
            data[HOME] = entry.getValue()[0];
            data[PROOF] = entry.getValue()[1];
            data[PROPS] = entry.getValue()[2];
            return data;
        }
        public void remove() { 
            throw new UnsupportedOperationException("ProductDB iterators not editable");
        }
    }

    
    public static void main(String[] args) {
        if (args.length < 1) return;

        StackConfig config = null;
        try { 
            config = new VAOSoft1StackConfig(new File("/sw/vao"), null);
        }
        catch (ConfigurationException ex) {
            throw new InternalError("config error: " + ex.getMessage());
        }
        VersionHandler vh = new BasicVersionHandler();

        String constraint = args[0];
        BasicProductLine bpl = new BasicProductLine("prod", "com", vh, config);
        if (args.length > 1) {
            for(int i=1; i < args.length; i++) 
                bpl.addVersion(args[i], null, "etc", null );
        }
        else {
            bpl.addVersion("1.0.0", null, "etc", null );
            bpl.addVersion("1.0.1", null, "etc", null );
            bpl.addVersion("1.0.5", null, "etc", null );
            bpl.addVersion("1.1.0rc3", null, "etc", null);
            bpl.addVersion("1.1.0", null, "etc", null);
        }

        ProductLine spl = bpl.matchVersions(constraint, null);
        Iterator<String> it = spl.versionSet().iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }


}


