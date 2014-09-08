 
function Publish(sub,msg)
{
	var message = {InMsg:msg};	
	window.parent.PageBus.publish(sub, message);		
}
 
function Subscribe(sub,listener)
{	
	var subscription = window.parent.PageBus.subscribe(sub,null,listener,null);
	return subscription;
}

function Store(sub, msg)
{		
	var message = {InMsg:msg};
	window.parent.PageBus.store(sub, message);	
}

function Query(sub, listener)
{		
	window.parent.PageBus.query(sub, null,listener,null);
}
 
function getFlexApp(appName) 
{
//	if (navigator.appName.indexOf ("Microsoft") !=-1) {
//		return window[appName];
//	} else {
//        return document[appName];
//	}
	return Ext.get('AstroView').dom.children[0];
}

function onPagebusMessage(subject, message, subscriberData)
{
	getFlexApp('AstroView').onPagebusMessage(message.InMsg);
}


