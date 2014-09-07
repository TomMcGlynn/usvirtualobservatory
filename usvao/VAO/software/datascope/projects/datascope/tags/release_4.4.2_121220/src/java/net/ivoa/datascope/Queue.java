package net.ivoa.datascope;

import java.util.ArrayList;

/**
 * This class implements a list of services to be run one at a time. It is
 * assumed that the queue services only one host id.
 */
public class Queue implements Runnable {

	ArrayList<Service> services = new ArrayList<Service>();
	String host;
	Filter hostFilter;
	double ra, dec, size;
	private int failures;

	public Queue(String host, double ra, double dec, double size) {
		this.host = host;
		hostFilter = Filter.factory(host);
		this.ra = ra;
		this.dec = dec;
		this.size = size;
	}

	/*
	 * Adds a service. If two versions of the same service are found, the one
	 * with the latest version number is kept, and the other is removed. The
	 * removed service is returned.
	 */
	public Service addService(Service newService) {
		int index = services.indexOf(newService);
		if (index >= 0) {
			Service dupSv = services.get(index);
			if (dupSv.getVersion() < newService.getVersion()
					|| (dupSv.getVersion() == newService.getVersion() && dupSv
							.getInterfaceVersion() < newService
							.getInterfaceVersion())) {
				services.set(index, newService);
				return dupSv;
			}
			return newService;
		}

		services.add(newService);

		return null;
	}

	public void run() {
		DS.log("Normal start for queue " + host + " with " + services.size()
				+ " requests.");
		double fudge = 0;
		String filterName = null;

		if (hostFilter != null) {
			DS.log("Invoking host filter:" + hostFilter);
			hostFilter.invoke(ra, dec, size);
			fudge = hostFilter.fudge();
			filterName = hostFilter.getClass().getName();
		}

		for (int i = 0; i < services.size(); i += 1) {

			Service s = services.get(i);

			if ((hostFilter != null) && (s.getROR() < fudge)) {

				String trueID = s.getID();
				int index = trueID.indexOf("#");
				if (index >= 0) {
					trueID = trueID.substring(0, index);
				}

				int cnt = hostFilter.count(trueID);

				DS.log("True ID is " + trueID + " count " + cnt);

				if (cnt == 0) {
					s.setMessage("|Filtered by " + filterName);
					s.setFiltered(true);
					s.setStatus("FILTERED");
					continue;
				}
			}
			s.updateURL(ra, dec, size);
			s.invoke();
			if (s.isError() && ++failures >= DS.getMaxSiteFailures()) {
				abort("Queue aborted due to unresponsive service site.");
				break;
			}
		}
		DS.log("Normal exit for queue:" + host);
	}

	private void abort(String e) {
		// Give up on this queue, mark remaining services as having errors.
		for (Service s : services) {
			if (s.getStatus().equals("PENDING")) {
				s.setError();
				s.setMessage("-1|" + e);
			}
		}
	}
}
