package net.ivoa.datascope;

import com.jezhumble.javasysmon.OsProcess;
import com.jezhumble.javasysmon.ProcessVisitor;

public class PidFinder implements ProcessVisitor {
	private int searchPid;
	private boolean pidFound = false;

	public PidFinder(int pid) {
		searchPid = pid;
	}

	public boolean visit(OsProcess process, int level) {
		if( process.processInfo().getPid() == searchPid ){
			pidFound = true;
		}
		return false;
	}

	public boolean found() {
		return pidFound;
	}
}
