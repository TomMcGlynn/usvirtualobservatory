package net.nvo;

import java.util.ArrayList;
import java.io.PrintStream;

import net.ivoa.util.Settings;

/** Generate a Footer for a web page */
public class Footer {

    private ArrayList<String> outputElements = new ArrayList<String>();
    private String elementSeparator = "<hr noshade>\n";
   
    
    public Footer() { 
	addElement(
     "<table width='100%' border=0><tr>" +
       "<td align=center><a href=http://www.nsf.gov><img src="+Settings.get("DOCBase")+"/images/nsflogo_64x.gif width=64 height=64 border=0 alt='NSF Home'></a></div></td>" +
       "<td align=center><a href=http://www.nasa.gov><img src="+Settings.get("DOCBase")+"/images/nasa_logo.gif width=72 height=60 border=0 alt='NASA home'></a></td>"+
       "<td align=center>Developed with the support of the National Science Foundation under Cooperative Agreement AST0122449 with The Johns Hopkins University.</p>"+
	   "<p>The NVO is a member of the International Virtual Observatory Alliance.</p></td>"+
       "<td align=center><a href=http://www.ivoa.net><img src="+Settings.get("DOCBase")+"/images/IVOAlogo.gif width=80 height=44 border=0 alt='IVOA Home'></a></td>"+
      "</tr></table>");
	addElement(
     " Hosted by the <A HREF=http://universe.gsfc.nasa.gov/>Astrophysics Science Division</A> "+
     " <br> and the <A HREF=http://heasarc.gsfc.nasa.gov/> "+
     "  High Energy Astrophysics Science Archive Research Center (HEASARC)</A> "+
     " at <A HREF=http://www.nasa.gov/>NASA/</A> <A HREF=http://www.gsfc.nasa.gov/>GSFC</A><br> "+
     " <p> HEASARC Director: "+
     " <A HREF=http://heasarc.gsfc.nasa.gov/docs/bios/white.html>Dr. Nicholas E. White</A>, "+
     " <br><br> "+ 
     " HEASARC Associate Director: Dr. Roger Brissenden, "+
     " <br>Responsible NASA Official: "+
     " <rno><a href=http://heasarc.gsfc.nasa.gov/docs/nospam/panewman.html>Phil Newman</a> " +
     "</rno><p>"+
    " <table> <tr> <td class=tiny> <a href=/banner.html>Privacy, Security, Notices</a></td> </tr> </table>"
		   );
    }
    
    public Footer(String element) {
	addElement(element);
    }
    
    public void addElement(String element) {
	outputElements.add(outputElements.size(), element);
    }
    
    public void setSeparator(String separator) {
	elementSeparator = separator;
    }
	
    public void print(java.io.PrintStream out) {
	out.print(elementSeparator);
	out.print(elementSeparator);
	for(String entry: outputElements) {
	    out.print(entry);
	    out.print(elementSeparator);
        }
	out.print ("</BODY><HTML>\n");
    }
}
