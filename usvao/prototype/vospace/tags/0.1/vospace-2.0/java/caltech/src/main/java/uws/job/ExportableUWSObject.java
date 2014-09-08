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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import uws.UWSException;

/**
 * <P>This class defines the methods that an object of the UWS pattern must implement to be written in XML.</P>
 * <P>The {@link ExportableUWSObject#getXMLContent(boolean)} method must be implemented. It is the most important method of this class
 * because it returns the XML representation of this UWS object.</P>
 * <P>Some UWS objects contain some parameters that must be accessed in XML. Thus the {@link ExportableUWSObject#getXMLContent(String)}
 * method returns the XML representation of the specified member of this UWS object. By default its parameter <i>member</i> is ignored:
 * the returned XML is exactly the same than the {@link ExportableUWSObject#getXMLContent()} method.</P>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 09/2010
 */
public abstract class ExportableUWSObject implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the XML representation of this UWS object.<BR />
	 * This method calls the method {@link ExportableUWSObject#getXMLContent(boolean)} with the <i>false</i> value.
	 * 
	 * @return				An XML string.
	 * 
	 * @throws UWSException	If any error occurs during the XML translation.
	 * 
	 * @see ExportableUWSObject#getXMLContent(boolean)
	 */
	public final String getXMLContent() throws UWSException {
		return getXMLContent(false);
	}
	
	/**
	 * <P>Gets the XML representation of the specified member of this UWS object.</P>
	 * <P><b><u>Warning:</u> by default this method calls the {@link ExportableUWSObject#getXMLContent()}
	 * method (that is to say: the parameter <i>member</i> is ignored) !
	 * So you must override this method if you want the XML representation of some members.</b></P>
	 * 
	 * @param member		The name of the member/attribute/parameter to display.
	 * @return				An XML string.
	 * 
	 * @throws UWSException	If any error occurs during the XML translation.
	 */
	public String getXMLContent(String member) throws UWSException {
		return getXMLContent(false, member);
	}
	
	/**
	 * <P>Gets the XML representation of the specified member of this UWS object.</P>
	 * <P><b><u>Warning:</u> by default this method calls the {@link ExportableUWSObject#getXMLContent(boolean)}
	 * method (that is to say: the parameter <i>member</i> is ignored) !
	 * So you must override this method if you want the XML representation of some members.</b></P>
	 * 
	 * @param rootNode		Indicates whether the object for which the XML content is wanted is the root node of the final XML file.
	 * @param member		The name of the member/attribute/parameter to display.
	 * @return				An XML string.
	 * 
	 * @throws UWSException	If any error occurs during the XML translation.
	 */
	public String getXMLContent(boolean rootNode, String member) throws UWSException {
		return getXMLContent(rootNode);
	}
	
	/**
	 * Gets the XML representation of this UWS object.
	 * 
	 * @param rootNode		Indicates whether the object for which the XML content is wanted is the root node of the final XML file.
	 * @return				An XML string.
	 * 
	 * @throws UWSException	If any error occurs during the XML translation.
	 */
	public abstract String getXMLContent(boolean rootNode) throws UWSException;
	
	/**
	 * Writes the XML content of this UWS object into the given OutputStream.
	 * 
	 * @param output				The stream in which the XML representation of this UWS object must be written.
	 * @param xsltPath				Path of the XSLT style sheet if any.
	 * 
	 * @throws UWSException			If <i>output</i> is <i>null</i>, if there is an error during the writing of the XML file or 
	 * 								if an error occurs while getting the XML content.
	 * 
	 * @see	ExportableUWSObject#writeXMLContent(OutputStream, String, String)
	 */
	public final void writeXMLContent(OutputStream output, String xsltPath) throws UWSException {
		writeXMLContent(output, null, xsltPath);
	}
	
	/**
	 * Writes the XML representation of the specified member of this UWS object into the given OutputStream.
	 * 
	 * @param output				The stream in which the XML representation of this UWS object must be written.
	 * @param member				Name of the part of this object to display.
	 * @param xsltPath				Path of the XSLT style sheet if any.
	 * 
	 * @throws UWSException			If <i>output</i> is <i>null</i>, if there is an error during the writing of the XML file or 
	 * 								if an error occurs while getting the XML content.
	 * 
	 * @see	ExportableUWSObject#getXMLContent(boolean)
	 * @see	ExportableUWSObject#getXMLContent(boolean, String)
	 */
	public void writeXMLContent(OutputStream output, String member, String xsltPath) throws UWSException {
		if (output == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "Impossible to write the XML content of this object ("+toString()+") because the given stream is null !");
		
		try{
			// Write the XML header:
			String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			if (xsltPath != null && xsltPath.trim().length() > 0)
				xmlHeader += "<?xml-stylesheet type=\"text/xsl\" href=\""+xsltPath+"\"?>\n";
			output.write(xmlHeader.getBytes());
			
			// Get the XML content and write it in the given stream:
			if (member != null && member.trim().length() > 0)
				output.write(getXMLContent(true, member).getBytes());
			else
				output.write(getXMLContent(true).getBytes());
			output.flush();
		}catch(IOException ex){
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "Impossible to write the XML content of this object ("+toString()+") !");
		}
	}
}
