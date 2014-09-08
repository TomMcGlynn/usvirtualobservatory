/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.vospace.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Transfer job description java bean
 * @author Dmitry Mishin
 */
public class JobDescription implements Serializable {
	private static final long serialVersionUID = 4846684191233497876L;

	/** The job states enumeration */
	public enum STATE { PENDING, RUN, ERROR, COMPLETED};

	/** The job directions enumeration */
	public enum DIRECTION { PULLTOVOSPACE, PULLFROMVOSPACE, PUSHTOVOSPACE, PUSHFROMVOSPACE, LOCAL};
	
	/** The job target **/
	private String target;

	/** The job direction **/
	private DIRECTION direction;

	/** The local direction target**/
	private String directionTarget;

	public String getDirectionTarget() {
		return directionTarget;
	}
	public void setDirectionTarget(String directionTarget) {
		this.directionTarget = directionTarget;
	}
	/** The job views **/
	private ArrayList<String> views = new ArrayList<String>();

	/** The job protocols **/
	private HashMap<String, String> protocols = new HashMap<String, String>();

	/** The job xml notation **/
	private String jobXmlDescription;

	/** The job identifier **/
	private String id;

	/** The job start time **/
	private Date startTime;

	/** The job end time **/
	private Date endTime;

	/** The job state **/
	private STATE state = STATE.PENDING;
	
	/** The job owner username **/
	private String username;

	/** The job note (error message) **/
	private String note;
	
	private boolean keepBytes;

	public boolean isKeepBytes() {
		return keepBytes;
	}
	public void setKeepBytes(boolean keepBytes) {
		this.keepBytes = keepBytes;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public DIRECTION getDirection() {
		return direction;
	}
	public void setDirection(DIRECTION direction) {
		this.direction = direction;
	}
	public ArrayList<String> getViews() {
		return views;
	}
	public void addView(String view) {
		views.add(view);
	}
	public void addProtocol(String protocol, String endpoint) {
		if(null == endpoint) endpoint = "";
		this.protocols.put(protocol, endpoint);
	}
	public HashMap<String, String> getProtocols() {
		return protocols;
	}
	public String getJobXmlDescription() {
		return jobXmlDescription;
	}
	public void setJobXmlDescription(String jobXmlDescription) {
		this.jobXmlDescription = jobXmlDescription;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public JobDescription.STATE getState() {
		return state;
	}
	public void setState(JobDescription.STATE state) {
		this.state = state;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("target: "+getTarget()+"; ");
		buf.append("direction: "+getDirection()+"; ");
		for(String prot: getProtocols().keySet()){
			buf.append("protocol: "+prot+", "+getProtocols().get(prot)+"; ");
		}
		buf.append("direction: "+getDirection()+"; ");
		return buf.toString();
	}

}
