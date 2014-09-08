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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

public class XMLObject {

	
	private static Logger logger = Logger.getLogger(XMLObject.class);
	private VTDNav vn;
	private AutoPilot ap;
	private XMLModifier xm;
	protected String PREFIX;

	/**
	 * Construct a XMLObject from the byte array
	 * @param req The byte array containing the Node
	 */
	public XMLObject(byte[] bytes) {
		try {
			VTDGen vg = new VTDGen();
			vg.setDoc(bytes);
			vg.parse(true);
			vn = vg.getNav();
			ap = new AutoPilot();
			xm = new XMLModifier();
			ap.declareXPathNameSpace("vos", "http://www.ivoa.net/xml/VOSpace/v2.0");
			ap.declareXPathNameSpace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			ap.declareXPathNameSpace("uws", "http://www.ivoa.net/xml/UWS/v1.0");
			PREFIX = getNamespacePrefix();
			if (!validStructure())
				throw new BadRequestException("Invalid node representation");
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Return the values of the items identified by the specified XPath expression
	 * @param expression The XPath expression identifying the items to retrieve
	 * @return the values of the items identified by the XPath expression
	 */
	public String[] xpath(String expression) {
		try {
			ap.bind(vn);
			ArrayList<String> elements = new ArrayList<String>();
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				if (vn.getTokenType(result) == VTDNav.TOKEN_ATTR_NAME) {
					elements.add(vn.toNormalizedString(result + 1));
				} else {
					int t = vn.getText();
					if (t > 0) 
						elements.add(vn.toNormalizedString(t));
				}
			}
			ap.resetXPath();
			return elements.toArray(new String[0]);
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Return the items identified by the specified XPath expression
	 * @param expression The XPath expression identifying the items to retrieve
	 * @return the items identified by the XPath expression as a string
	 */
	public String[] item(String expression) {
		try {
			ArrayList<String> items = new ArrayList<String>();
			ap.bind(vn);
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				items.add(new String(vn.getElementFragmentNs().toBytes()));
			}
			ap.resetXPath();
			return items.toArray(new String[0]);
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Update the value of the text identified by the XPath expression with the specified string
	 * @param expression The XPath expression identifying the text to be replaced
	 * @param value The new text value 
	 */
	public void replace(String expression, String value) {
		try {
			ap.bind(vn);
			xm.bind(vn);
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				if (vn.getTokenType(result) == VTDNav.TOKEN_ATTR_NAME) {
					xm.updateToken(result + 1, value);
				} else {
					int t = vn.getText();
					if (t > 0)
						xm.updateToken(t, value);
				}
			}
			vn = xm.outputAndReparse();
			ap.resetXPath();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Remove the items identified by the specified XPath expression
	 * @param expression The XPath expression identifying the items to remove
	 */
	public void remove(String expression) {
		try {
			ap.bind(vn);
			xm.bind(vn);
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				xm.remove();
			}
			vn = xm.outputAndReparse();
			ap.resetXPath();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Add the item identified by the specified XPath expression
	 * @param expression The XPath expression identifying where to add the item
	 * @param item The item to add
	 */
	public void add(String expression, String item) {
		try {
			ap.bind(vn);
			xm.bind(vn);
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				xm.insertAfterElement(item);
			}
			vn = xm.outputAndReparse();
			ap.resetXPath();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}


	/**
	 * Add the item identified by the specified XPath expression
	 * @param expression The XPath expression identifying where to add the item
	 * @param item The item to add
	 */
	public void addChild(String expression, String item) {
		try {
			ap.bind(vn);
			xm.bind(vn);
			ap.selectXPath(expression);
			int result = -1;
			while ((result = ap.evalXPath()) != -1) {
				xm.insertAfterHead(item);
			}
			vn = xm.outputAndReparse();
			ap.resetXPath();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Check whether the specified item exists
	 * @param expression The XPath expression identifying the item to check
	 * @return whether the specified item exists or not
	 */
	public boolean has(String expression) {
		try {
			boolean has = false;
			ap.bind(vn);
			ap.selectXPath(expression);
			if (ap.evalXPath() != -1)
				has = true;
			ap.resetXPath();
			return has;
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Validate the structure of the document
	 */
	public boolean validStructure() {
		return true;
	}

	/**
	 * Get a byte array corresponding to the object
	 * @return a byte array corresponding to the object
	 */
	public byte[] getBytes() {
		return vn.getXML().getBytes();
	}

	/**
	 * Get the namespace prefix used for the object
	 * @return the namespace prefix used for the object
	 */
	public String getNamespacePrefix() {
		try {
			return vn.getPrefixString(1);
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Get a string representation of the object
	 * @return a string representation of the object
	 */
	public String toString() {
		return new String(getBytes());
	}
}
