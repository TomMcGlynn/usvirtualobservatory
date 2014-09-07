package net.nvo;

import java.util.ArrayList;
import java.io.PrintStream;

import net.ivoa.util.Settings;

public class Header {
    
    private ArrayList<String> cssFiles = new ArrayList<String>();
    private ArrayList<String> jsFiles  = new ArrayList<String>();
    
    private String bannerTitle = "National Virtual Observatory DataScope";
    private String rightBox    = "";
    
    private ArrayList<String> bannerTokens = new ArrayList<String>();
    
    private String pageTitle = "";
    
    private String onLoad = null;

    public  Header() {
	bannerTitle = "National Virtual Observatory DataScope";
	rightBox    = Settings.get("RightBoxText");
	if (rightBox == null) {
	    rightBox = "";
	}
    }
    
    public void setBannerTitle(String banner) {
	bannerTitle = banner;
    }
    
    public void setTitle(String title) {
	pageTitle = title;
    }
    
    public void setOnLoad(String action) {
	onLoad = action;
    }
    
    public void addJavaScript(String file) {
	jsFiles.add(file);
    }
    
    public void addCSSFile(String file) {
	cssFiles.add(file);
    }
    
    public void addToken(String token) {
	bannerTokens.add(token);
    }
    
    public void addToken(String url, String token) {
	bannerTokens.add("<a href='"+url+"'>"+token+"</a>");
    }
    
    public void addToken(String url, String token, String title) {
	bannerTokens.add("<a href='"+url+"' title='"+title+"'>"+token+"</a>");
    }
    
    public void setRightBox(String input) {
	this.rightBox = input;
    }
    
    public void printBanner(PrintStream out) {
	
	int nb = bannerTokens.size();
        out.println(
          "<a href='#content' title='Skip navigation'> </a>"+
          "<table width='100%'>"+
            "<tr>"+
              "<td width='12%'><div align=center><a href='http://www.us-vo.org'><img src='"+Settings.get("DocBase")    +
		    "/images/NVOwords_150pixels.jpg' width=162 height=86 border=0 alt='NVO Home'></a><br></div></td> " +
	      "<td colspan="+nb+" bgcolor=24386d align=center><h1 class='style1'>"+bannerTitle+"</h1></td>"            +
              "<td width='11%' align=right>"+rightBox +"</td>"                                                         +
              "</tr><tr></table>");
	out.println("<table width='100%'><tr bgcolor='#6ba5d7'>"                + 
              "<td align=center><a href=http://www.us-vo.org>NVO Home</a></td>");
	for (String token: bannerTokens) {
	    out.println("<td align=center>"+token+"</td>");
	}
	out.println("<td align=center><a href=http://us-vo.org/feedback/index.cfm>NVO Feedback</a></td>"+
      	      "<td align=center><a href="+Settings.get("DocBase")+"/helpInc.html>Help</td>");
	out.println("</tr></table><a name=content></a>");
    }
    
    public void printHTMLHeader(PrintStream out) {
	
	String title = pageTitle;
	if (title == null || title.length() == 0) {
	    title = "DataScope page";
	}
	out.println("<HTML><HEAD><TITLE>"+title+"</TITLE>");
	
	for(String sheet: cssFiles) {
	    out.println("<LINK REL=StyleSheet HREF='"+sheet+"' TYPE=text/css>");
	}
	for (String script: jsFiles) {
	    out.println("<SCRIPT language=JavaScript src='"+script+"'></SCRIPT>");
	}
	
	String v = "";
	if (onLoad != null) {
	    v = "onload=\""+onLoad+"\"";
	}
	out.print("</HEAD><BODY "+v+">");
    }
    
    public static void printHTTP(PrintStream out) {
	out.println("Content-type: text/html\n");
    }
}
