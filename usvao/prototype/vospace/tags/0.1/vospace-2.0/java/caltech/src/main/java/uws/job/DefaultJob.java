/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2010 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

package uws.job;

import java.util.Date;
import java.util.Map;

import uws.UWSException;

/**
 * <p>This is the default implementation of {@link AbstractJob}.</p>
 * 
 * <p>The execution of a direct instance of this class does nothing. It is a job with no work !</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 09/2010
 * 
 * @see AbstractJob
 */
public class DefaultJob extends AbstractJob {
	private static final long serialVersionUID = 1L;

	public DefaultJob(Map<String, String> lstParam) throws UWSException {
		super(lstParam);
	}

	public DefaultJob(String ownerID, Map<String, String> lstParam)	throws UWSException {
		super(ownerID, lstParam);
	}

	public DefaultJob(String jobName, String userId, long maxDuration, Date destructTime, Map<String, String> lstParam)	throws UWSException {
		super(jobName, userId, maxDuration, destructTime, lstParam);
	}

	/**
	 * DOES NOTHING !
	 * 
	 * @see uws.job.AbstractJob#jobWork()
	 */
	@Override
	protected void jobWork() throws UWSException, InterruptedException { ; }

}
