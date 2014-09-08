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
package edu.caltech.vao.vospace.xml;

import java.util.ArrayList;

public class View  {

    private XMLObject view;

    /**
     * Construct a View from the byte array
     * @param bytes The byte array containing the View
     */
    public View(byte[] bytes)  {
	view = new XMLObject(bytes);
    }

    /**
     * Construct a View from the string representation
     * @param bytes The string containing the view
     */
    public View(String bytes)  {
	view = new XMLObject(bytes.getBytes());
    }

    /**
     * Get the params of the view
     * @return The params of the view
     */
    public Param[] getParam()  {
	ArrayList<Param> params = new ArrayList<Param>();
	for (String param : view.item("/vos:view/vos:param")) {
	    params.add(new Param(param));
	} 
	return params.toArray(new Param[0]);
    }

    /**
     * Get the URI of the view
     * @return The URI of the view
     */
    public String getURI()  {
	return view.xpath("/vos:view/@uri")[0];
    }

    /**
     * Does the view provide access to the original data content?
     * @return whether the view provides access to the original data content
     */
    public boolean isOriginal()  {
	 String isOriginal = view.xpath("/vos:view/@original")[0];
	 return Boolean.valueOf(isOriginal).booleanValue();
    }

    /**
     * Get a string representation of the view
     * @return a string representation of the view
     */
    public String toString() {
	return view.toString();
    }

}
