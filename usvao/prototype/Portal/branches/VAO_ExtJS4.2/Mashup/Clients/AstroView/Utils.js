////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Utility Methods
////////////////////////

//////////////////////////
// toJson() 
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
};

//////////////////////////
// fromJson() 
//////////////////////////
ASTROVIEW.fromJson = function(str) 
{  
    if (str === "") str = '""';  
    eval("var p=" + str + ";");  
    return p;  
};


//////////////////////////
// fromXml() 
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
};

////////////////////////
// ASTROVIEW.isString()
////////////////////////
ASTROVIEW.isString = function(s)
{
    return typeof(s) === 'string' || s instanceof String;
};

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
};
