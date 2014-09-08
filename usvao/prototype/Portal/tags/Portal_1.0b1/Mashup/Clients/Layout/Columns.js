Ext.require(['*']);

Ext.onReady(function(){

   // NOTE: This is an example showing simple state management. During development,
   // it is generally best to disable state management as dynamically-generated ids
   // can change across page loads, leading to unpredictable results.  The developer
   // should ensure that stable state ids are set for stateful components in real apps.
   
   	Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

   	var viewport = Ext.create('Ext.Viewport', {
   	layout:'fit',
	items: {
	    title: 'Fit Panel',
	    border: false,

	    layout:'border',
	    items:[{
	            region:'west',
	            id:'west-panel',
	            title:'West',
	            split:true,
	            width: 300,
	            minSize: 175,
	            maxSize: 400,
	            collapsible: true,
	            margins:'35 0 5 5',
	            cmargins:'35 5 5 5',
	            layout:'accordion',
	            layoutConfig:{
	                animate:true
	            }
	        },{
	            region:'center',
	            id:'center-panel',
	            title:'Center',
	            split:true,
	            width: 300,
	            minSize: 175,
	            maxSize: 400,
	            collapsible: true,
	            collapseDirection: 'left',
	            margins:'35 0 5 5',
	            cmargins:'35 5 5 5',
	            layout:'accordion',
	            layoutConfig:{
	                animate:true
	            }
	        },{
	            region:'east',
	            id:'east-panel',
	            title:'East',
	            split:true,
	            width: 400,
	            minSize: 175,
	            collapsible: true,
	            margins:'35 0 5 5',
	            cmargins:'35 5 5 5',
	            layout:'accordion',
	            layoutConfig:{
	                animate:true,
	            }
	        }]
	     }
    });
});  