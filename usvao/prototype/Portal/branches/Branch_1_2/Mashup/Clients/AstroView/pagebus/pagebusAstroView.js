
function nullListener(subject, message, data ) 
{ 
	return; 
}

function Publish(subject, msg)
{
	var message = {InMsg:msg};	
	window.parent.PageBus.publish(subject, message)		
}
 
function SubscribeNoCache(subject, listener)
{	
	var subscription = window.parent.PageBus.subscribe(subject, null, listener, null);
	return subscription;
}

function Subscribe(subject, listener)
{
    var settings = { PageBus: { cache: true } };

	var subscription;
    if (listener == null)
    {
    	subscription = window.parent.PageBus.subscribe(subject, null, nullListener, settings );
	}
	else
	{
	    subscription = window.parent.PageBus.subscribe(subject, null, listener, settings );
	}
	return subscription;
}

function Store(subject, msg)
{		
	var message = {InMsg:msg};
	window.parent.PageBus.store(subject, message);	
}

function Query(subject, listener)
{		
	window.parent.PageBus.query(subject, null, listener, null);
}

function onAstroViewMessage(subject, message, subscriberData)
{
	getFlexApp('AstroView').onAstroViewMessage(message.InMsg);
}

function getFlexApp(appName) 
{ 
    if (typeof Ext != 'undefined')
    {
    	return Ext.get(appName).dom.children[0];
    }
    else
    {
		if (navigator.appName.indexOf ("Microsoft") !=-1) {
			return window[appName];
		} else {
	        return document[appName];
		}
	}
}