function renderGraph() {
   try {
      if (top.twin) {
         top.twin.close();
      }
      top.twin = window.open(null, "Graph", "WIDTH=900,HEIGHT=700,resizable,scrollbars,toolbar,menubar,status");
      // @URL_PATH@ needed so long as /cgi-bin/ link exists
      top.twin.document.write("<head><title>Flot Graph</title>\n" +
         "<link rel='StyleSheet' href='@CSS_PATH@vo_graph.css' type='text/css'>\n" +
         "<script language='javascript' type='text/javascript' src='@JS_PATH@sarissa.js'></script>\n" +
         "<script language='javascript' type='text/javascript' src='@JS_PATH@jquery.js'></script>\n" +
         "<script language='javascript' type='text/javascript' src='@JS_PATH@jquery.flot.js'></script>\n" +
         "<script language='javascript' type='text/javascript' src='@JS_PATH@jquery.vo.graph.js'></script>\n" +
         "<!--[if IE]><script language='javascript' type='text/javascript' src='@JS_PATH@excanvas.pack.js'></script><![endif]-->\n" +
         "<script id='source' language='javascript' type='text/javascript'>\n" +
         "  $(function () {\n" +
         "     var myGraph = new Graph({target: '#placeholder'});\n" +
         "  });\n" +
         "</script>\n" +
         "</head>\n<body>\n" +
         "<div id='wrapper'>\n" +
         "<div id='y_div'>Y:<select id='y' name='y' class='axis'></select></div><div id='placeholder'></div>\n" +
         "<center style='clear: left'>X:<select id='x' name='x' class='axis'></select></center>\n" +
         "<center><h2 style='clear: left'>This component is currently in development.</h2></center>\n" +
         "</div>\n" +
         "<ul>\n"+
         "<li>Single click to select the nearest point</li>\n"+
         "<li>Down and drag to select all points within box</li>\n"+
         "</ul>\n"+
         "<p>Possible features to be implemented ...</p>\n"+
         "<ul>\n"+
         "<li>Logarithm</li>\n"+
         "<li>Deselect</li>\n"+
         "<li>padding on edge of graph</li>\n"+
         "<li>Export all selected as VOTABLE</li>\n"+
         "</ul>\n"+
         "</body>\n" +
         "</html>\n" +
      "");
      top.twin.document.close();
      top.twin.focus();
   } catch  (e) {
      alert("Exception creating Graph:"+e);
   }
}
