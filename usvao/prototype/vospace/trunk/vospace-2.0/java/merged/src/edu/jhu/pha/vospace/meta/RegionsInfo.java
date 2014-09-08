/*******************************************************************************
 * Copyright (c) 2012, Johns Hopkins University
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
package edu.jhu.pha.vospace.meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

public class RegionsInfo {
	private HashSet<RegionDescription> regions = new HashSet<RegionDescription>();
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public HashSet<RegionDescription> getRegions() {
		return regions;
	}

	public void setRegions(HashSet<RegionDescription> regions) {
		this.regions = regions;
	}

	public byte[] toJson() {
		try {
			return mapper.writeValueAsBytes(this.regions);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e, "Error serializing account info to JSON");
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e, "Error serializing account info to JSON");
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e, "Error serializing account info to JSON");
		}
	}
	
	public static class RegionDescription {
		private String id;
		private String url;
		private String display;
		private boolean isDefaultRegion;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getDisplay() {
			return display;
		}
		public void setDisplay(String display) {
			this.display = display;
		}
		public boolean isDefaultRegion() {
			return isDefaultRegion;
		}
		public void setDefault(boolean isDefaultRegion) {
			this.isDefaultRegion = isDefaultRegion;
		}
	}
}
