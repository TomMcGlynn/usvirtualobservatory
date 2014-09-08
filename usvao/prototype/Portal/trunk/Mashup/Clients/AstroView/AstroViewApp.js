////////////////////////
// ASTROVIEW Namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Math Constants
////////////////////////
PI2 = Math.PI * 2.0;
TO_RADIANS = Math.PI/180.0;
TO_DEGREES = 180.0/Math.PI;

RADIANS_10 = 10.0 * TO_RADIANS;
RADIANS_30 = 30.0 * TO_RADIANS;
RADIANS_90 = 90.0 * TO_RADIANS;
RADIANS_100 = 100.0 * TO_RADIANS;
RADIANS_180 = 180.0 * TO_RADIANS;
RADIANS_360 = 360.0 * TO_RADIANS;

////////////////////////
// Constants
////////////////////////
// Diamond Radius and Viewport Far Plane
ASTROVIEW.RADIUS           = 1000;
ASTROVIEW.FOV_LEVEL_MAX    = 30;

// Refresh Rate
ASTROVIEW.TIMER_TICKS_ACTIVE = 10;
ASTROVIEW.TIMER_TICKS_IDLE   = 40;

//////////////////////////
// ASTROVIEW.MOBILE
//////////////////////////
ASTROVIEW.MOBILE = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

//////////////////////////
// ASTROVIEW.log() 
//////////////////////////
ASTROVIEW.debug = false;
ASTROVIEW.log = function(msg)
{
    if (ASTROVIEW.debug)
        console.log("AstroView: " + msg);
}

//////////////////////////
// ASTROVIEW.toJson() 
//////////////////////////
ASTROVIEW.toJson = function(obj) 
{  
    var t = typeof (obj);  
    if (t != "object" || obj === null) 
    {  
        // simple data type  
        if (t == "string") obj = '"'+obj+'"';  
            return String(obj);  
    }  
    else 
    {  
        // recurse array or object  
        var n, v, json = [], arr = (obj && obj.constructor == Array);  
        for (n in obj) 
        {  
            v = obj[n]; 
            t = typeof(v);  
            if (t == "string") 
                v = '"'+v+'"';  
            else if (t == "object" && v !== null) 
                v = JSON.stringify(v);  
            json.push((arr ? "" : '"' + n + '":') + String(v));  
        }  
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");  
    }  
}

//////////////////////////
// ASTROVIEW.fromJson() 
//////////////////////////
ASTROVIEW.fromJson = function(str) 
{  
    if (str === "") str = '""';  
    eval("var p=" + str + ";");  
    return p;  
}


//////////////////////////
// ASTROVIEW.fromXml() 
//////////////////////////
ASTROVIEW.fromXml = function(xml) 
{  
    var xmlDoc;
    if (window.DOMParser)
    {
        parser=new DOMParser();
        xmlDoc=parser.parseFromString(xml,"text/xml");
    }
    else // Internet Explorer
    {
        xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async=false;
        xmlDoc.loadXML(xml); 
    }
    return xmlDoc;
}

////////////////////////
// ASTROVIEW.bind()
////////////////////////
ASTROVIEW.bind = function( scope, fn )
{
    return function ()
    {
        fn.apply( scope, arguments );
    };
}

////////////////////////
// ASTROVIEW.isString()
////////////////////////
ASTROVIEW.isString = function(s)
{
    return typeof(s) === 'string' || s instanceof String;
}

////////////////////////
// ASTROVIEW.isNumber()
////////////////////////
ASTROVIEW.isNumber = function(n) 
{
    return !isNaN(parseFloat(n)) && isFinite(n);
}

////////////////////////
// ASTROVIEW.trim()
////////////////////////
ASTROVIEW.trim = function(s)
{
    return s.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}