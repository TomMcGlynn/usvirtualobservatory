
function loadAstroViewWindow()
{ 
	var win = Ext.widget('window', {
	    title: "AstroView",
	    layout: 'fit',
	    width: 1200,
	    height: 600,
	    x: 20,
	    y: 20,
	    resizable: true,
	    items: {
	        id: "AstroView",
	        xtype: 'flash',
	        url: 'AstroView.swf'
	    }
	});
	win.show();
}
