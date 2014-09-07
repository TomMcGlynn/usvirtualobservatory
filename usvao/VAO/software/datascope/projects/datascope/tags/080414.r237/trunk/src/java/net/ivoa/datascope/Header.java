package net.ivoa.datascope;

import java.util.ArrayList;
import java.io.PrintStream;

public class Header {
    
    private ArrayList<String> cssFiles = new ArrayList<String>();
    private ArrayList<String> jsFiles  = new ArrayList<String>();
    
    private String bannerTitle = "National Virtual Observatory DataScope";
    private ArrayList<String> bannerTokens = new ArrayList<String>();
    
    private String pageTitle = "";
    
    private String onLoad = null;

    public  Header() {
	bannerTitle = "National Virtual Observatory DataScope";
	addToken("http://us-vo.org/projects/tools.cfm", "VO Tools and Services");
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
    
    public void printBanner(PrintStream out) {
	
	int nb = bannerTokens.size();
        out.println(
          "<a href='#content' title='Skip navigation'> </a>"+
          "<table width='100%'>"+
            "<tr>"+
              "<td width='12%'><div align='center'><a href='http://www.us-vo.org'><img src='"+DS.getURLBase()            +
		    "images/NVOwords_150pixels.jpg' width='162' height='86' border='0' alt='NVO Home' /></a><br></div></td> " +
	      "<td colspan='"+nb+"' bgcolor='24386d' align='center'><h1 class='style1'>"+bannerTitle+"</h1></td>"            +
              "<td width='11%' align='right'><div align='center'>Hosted by:<br>HEASARC<br>NASA/GSFC</div></td>"            +
              "</tr><tr></table>");
	out.println("<table width='100%'><tr bgcolor='#6ba5d7'>"                + 
              "<td align='center'><a href='http://www.us-vo.org'>NVO Home</a></td>" +
	      "<td align='center'><a href='"+DS.getURLBase()+"helpInc.html'>Help</td>");
	for (String token: bannerTokens) {
	    out.println("<td align='center'>"+token+"</td>");
	}
	out.println("<td align='center'><a href='http://us-vo.org/feedback/index.cfm'>NVO Feedback</a></td>");
	out.println("</tr></table><a name='content'></a>");
    }
    
    public void printHTMLHeader(PrintStream out) {
	
	String title = pageTitle;
	if (title == null || title.length() == 0) {
	    title = "DataScope page";
	}
	out.println("<html><head><title>"+title+"</title>");
	
	for(String sheet: cssFiles) {
	    out.println("<link rel='StyleSheet' href='"+sheet+"' type='text/css' />");
	}
	for (String script: jsFiles) {
	    out.println("<script language='JavaScript' src='"+script+"'></script>");
	}
	
	String v = "";
	if (onLoad != null) {
	    v = "onload=\""+onLoad+"\"";
	}
	out.print("</head><body "+v+">");
    }
    
    public static void printHTTP(PrintStream out) {
	out.println("Content-type: text/html\n");
    }
}
