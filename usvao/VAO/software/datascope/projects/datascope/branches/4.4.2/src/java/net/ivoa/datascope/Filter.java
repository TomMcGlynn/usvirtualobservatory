package net.ivoa.datascope;

/** Filter classes a resources that tell whether
 *  other resources have data at a given
 *  location.  Filters normally apply to
 *  all services at a given host.
 */

import java.util.HashMap;

public abstract class Filter {

	/** This method creates an appropriate filter given a host name */
	public static Filter factory(String host) {

		host = host.toLowerCase();
		if (host.equals("heasarc.gsfc.nasa.gov")) {
			return new HeasarcFilter();
		} else if (host.equals("vizier.u-strasbg.fr")) {
			return new VizierFilter();
		} else {
			return null;
		}
	}

	/** This method invokes the filter for the given RA, Dec and Size */
	public abstract void invoke(double ra, double dec, double size);

	/**
	 * What was the count for the given id? If the id does not seem to be
	 * handled, a 1 will be returned.
	 */
	public abstract int count(String id);

	/**
	 * How much was added to the size field when doing the query of the service.
	 */
	public abstract double fudge();

	/**
	 * Get back a hash of identification string and the counts associated with
	 * them. Note that the keys will normally not be the full ivorn of the
	 * services.
	 */
	public abstract HashMap<String, Integer> getCounts();

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Usage: java net.ivoa.Filter type ra dec size");
		}

		Filter f = (Filter) Class.forName(
				"net.ivoa.datascope." + args[0] + "Filter").newInstance();
		f.invoke(Double.parseDouble(args[1]), Double.parseDouble(args[2]),
				Double.parseDouble(args[3]));
		HashMap<String, Integer> counts = f.getCounts();
		for (String key : counts.keySet()) {
			System.out.println(key + " : " + counts.get(key));
		}
	}
}
