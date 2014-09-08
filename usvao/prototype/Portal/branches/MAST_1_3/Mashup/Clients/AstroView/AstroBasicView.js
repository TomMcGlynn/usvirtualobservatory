////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Constants
////////////////////////
// Diamond Radius and Viewport Far Plane
ASTROVIEW.RADIUS = 1000;
ASTROVIEW.RADIUS_SPHERE = 1000;
ASTROVIEW.RADIUS_LAYER  = 980;

// Zoom Levels
ASTROVIEW.ZOOM_LEVEL_MIN = 4;
ASTROVIEW.ZOOM_LEVEL_MAX = 12;

// Field of View Levels
ASTROVIEW.FOV_LEVEL_MAX = 30;
ASTROVIEW.FOV_LEVEL_MIN = 0.00001;

// Refresh Rate
ASTROVIEW.TIMER_TICKS_ACTIVE = 40;
ASTROVIEW.TIMER_TICKS_IDLE   = 300;

// View State
ASTROVIEW.VIEW_STATE_ACTIVE = "ACTIVE";
ASTROVIEW.VIEW_STATE_IDLE = "IDLE";

////////////////////////
// Math Constants
////////////////////////
const PI2 = Math.PI * 2.0;
const RADIANS_10 = Math.PI/18.0;
const RADIANS_30 = Math.PI/6.0;
const RADIANS_90 = Math.PI/2.0;
const RADIANS_360 = Math.PI*2.0;
const TO_RADIANS = Math.PI/180.0;
const TO_DEGREES = 180.0/Math.PI;

////////////////////////
// Utility Methods
////////////////////////

////////////////////////
// bind()
////////////////////////
function bind( scope, fn )
{
    return function ()
    {
        fn.apply( scope, arguments );
    };
};

////////////////////////
// isString()
////////////////////////
function isString(s)
{
    return typeof(s) === 'string' || s instanceof String;
};

////////////////////////
// trim()
////////////////////////
function trim(s)
{
    return s.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};

////////////////////////
// startTimer()
////////////////////////
var lastTime = 0;
startTimer = function(callback, ticks)
{
    var currTime = new Date().getTime();
    var timeToCall = Math.max(0, ticks - (currTime - lastTime));
    var id = window.setTimeout(function() { callback(); }, timeToCall);
    lastTime = currTime + timeToCall;
    return id;
};

////////////////////////
// stopTimer()
////////////////////////
stopTimer = function(id)
{
    window.clearTimeout(id); 
};

// Survey URLs through Varnish Cache
const galex_mastproxyvm1_url = "http://mastproxyvm1.stsci.edu/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
const dss_mastproxyvm1_url = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs directly to IIS Server
const galex_masttest_url = "http://masttest.stsci.edu/surveys/toast/dss2/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
const dss_masttest_url = "http://masttest.stsci.edu/surveys/toast/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs through Mashup Proxy
const galexurl = "http://mastdev.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=" + galex_masttest_url;
const dssurl = "http://mastdev.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=" + dss_masttest_url;
    
////////////////////////
// AstroBasicView
////////////////////////
ASTROVIEW.AstroBasicView = function ( container, rendertype )
{
    // Create container if not specified
    if (!container)
    {
        container = document.createElement('div');
        document.body.appendChild(container);
    }
    this.container = container;
    
    // Set render type if not specified
    if (!rendertype)
    {
        rendertype = "webgl";
    }
    this.rendertype = rendertype;
       
    // Core 3D objects
    this.stats = null;
    this.scene = null; 
    this.renderer = null;
    this.camera = null;
    this.controller = null;
    
    // scene objects
    this.diamondSphere = null;
    
    // Toast Survey URL(s)
    this.baseurl = dssurl;
    
    // Animation Ticks
    this.ticks = ASTROVIEW.TIMER_TICKS_ACTIVE;
    
    // View State
    this.viewState = ASTROVIEW.VIEW_STATE_ACTIVE;
            
    /////////////////
    // createScene()
    /////////////////
    this.createScene = function() 
    {
        // Camera
        this.camera = new THREE.PerspectiveCamera(ASTROVIEW.FOV_LEVEL_MAX,
                                                  window.innerWidth / window.innerHeight,
                                                  1,
                                                  ASTROVIEW.RADIUS+1 );
        this.camera.position.set( 0, 0, 0 );
        this.camera.eulerOrder = 'YXZ';
        this.camera.name = "PerspectiveCamera";
        
        // Scene
        this.scene = new THREE.Scene();
        this.scene.add( this.camera );
        
        // Renderer
        this.renderer = this.createRenderer(this.rendertype);
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        this.container.appendChild( this.renderer.domElement );
                
        // Camera Controller
        this.controller = new ASTROVIEW.CameraController( this.camera, this.scene, this.container, this.renderer );
        
        // DiamondSphere
        this.diamondSphere = new ASTROVIEW.DiamondSphere();
        this.scene.add(this.diamondSphere);
        
        // Stats
        this.stats = new Stats();
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.top = '0px';
        this.container.appendChild(this.stats.domElement);
    }

    ////////////////////
    // createRenderer()
    ////////////////////
    this.createRenderer = function(rendertype)
    {
        var renderer=null;
        
        switch (rendertype)
        {
            case "webgl":
            {
                if (!renderer)
                {
                    try{
                        console.debug("Trying THREE.WebGLRenderer()...");
                        renderer = new THREE.WebGLRenderer();
                        break;
                    } catch(error) {renderer=null;}
                }
            }
            case "canvas":
            {
                if (!renderer)
                {	
                    try{
                        console.debug("Trying THREE.CanvasRenderer()...");
                        renderer = new THREE.CanvasRenderer();
                        break;
                    } catch(error) {renderer=null;}
                }
            }
            case "svg":
            {
                if (!renderer)
                {
                    try{
                        console.debug("Trying THREE.SVGRenderer...");
                        renderer = new THREE.SVGRenderer();
                        break;
                    } catch(error) {renderer=null;}
                }
            }
        }
        
        if (!renderer)
        {
            alert("Bummer. Unable to create a renderer: " + rendertype);
            console.debug("Bummer. Unable to create a renderer: " + rendertype);
        }
        
        return renderer;
    }

    ////////////////////////
    // renderScene()
    ////////////////////////
    this.renderScene = function() 
    {
        // Update the camera Position
        this.controller.render();
        
        // Render each Diamond 
        for (var i=0; i<this.diamondSphere.children.length; i++)
        {
            var dm = this.diamondSphere.children[i];	// ASTROVIEW.DiamondMesh
            dm.render(this);
        }
        
        // Render the scene
        this.renderer.render(this.scene, this.camera);
        
        // Update the stats window
        this.stats.update();
        
        // Request another animation frame
        startTimer(bind(this, this.renderScene), this.ticks);
    }
    
    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
        document.addEventListener( 'keypress' , bind(this, this.onKeyPress), false);
        document.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        document.addEventListener( 'mouseover',  bind(this, this.onMouseOver),  false );
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////				
    //
    // Events
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    this.onKeyPress = function( event )
    {
        var unicode=event.keyCode? event.keyCode : event.charCode;
        //alert("onKeyPress: " + unicode); // find the char code		
        switch(unicode)
        {
            // Testing of Viewer
            case  99:   this.onCatalogLayer(); break;	// 'C'
            case 100:   this.onDeleteLayer(); break;	// 'D'
            case 102:   this.onFootprintLayer(); break;	// 'F'
            case 105:   this.onImageLayer(); break;		// 'I'
            case 109:   this.onMove(); break;			// 'M'
            case 114:   this.onReadLayer(); break;		// 'R'
            case 117:   this.onUrl(); break;			// 'U'
            case 118:   this.onVisibility(); break;		// 'V'
        }
    }
    
    this.onMouseOut = function (event)
    {
        this.viewState = ASTROVIEW.VIEW_STATE_IDLE;
        this.ticks = ASTROVIEW.TIMER_TICKS_IDLE;
    }
    
    this.onMouseOver = function (event)
    {
        this.viewState = ASTROVIEW.VIEW_STATE_ACTIVE;
        this.ticks = ASTROVIEW.TIMER_TICKS_ACTIVE;
    }
    
    //////////////////////////
    // 'U' : onUrl() 
    //////////////////////////
    this.onUrl = function()
    {
        this.baseurl = (this.baseurl === dssurl ? galexurl : dssurl);
    }
    
    //////////////////////////
    // 'V' : onVisibility() 
    //////////////////////////
    this.onVisibility = function()
    {
        if (this.scene.getChildByName("DiamondSphere"))
        {
            this.scene.remove(this.diamondSphere);
        }
        else
        {
            this.scene.add(this.diamondSphere);
        }
    }	

    // [210.8023, 54.3490]
    // [202.4842, 47.2306]
    // [148.8882, 69.0653]
    // [23.4620, 30.6602] 
    
    var x=0;
    var ra =     [210.8023, 202.4842, 148.8882, 23.4620];
    var dec =    [54.3490, 47.2306, 69.0653, 30.6602];
    var level =  [10, 10, 10];
    
    //////////////////////////
    // 'M' : onMove() 
    //////////////////////////
    this.onMove = function()
    {
        var coord={"ra":ra[x], "dec":dec[x], "zoom":level[x]};
        this.moveTo(coord);
        x = (x+1) % ra.length;
    }
    
    //////////////////////////
    // 'R' : onReadLayer() 
    //////////////////////////
    this.onReadLayer = function()
    {
        for (var lid=0; lid<ASTROVIEW.lid; lid++)
        {
            var layerData = this.readLayer(lid);
            if (layerData)
            {
                alert("layerData [" + layerData.lid + "]:\n" + this.toJson(layerData));
            }
        }
    }
    
    //////////////////////////
    // 'D' : onDeletLayer() 
    //////////////////////////
    this.onDeleteLayer = function()
    {
        for (var lid=0; lid<ASTROVIEW.lid; lid++)
        {
            var layerData = this.readLayer(lid);
            if (layerData)
            {
                this.deleteLayer(lid);
            }
        }
    }
    
    ////////////////////////////
    // 'C' : onCatalogLayer() 
    ////////////////////////////
    this.onCatalogLayer = function()
    {
        var rows = [{"ra":0, "DEC":0},
                    {"ra":-1,"DEC":-1},
                    {"ra":-1,"DEC":1},
                    {"ra":1, "DEC":1},
                    {"ra":1, "DEC":-1},
                    {"RA":210.8023, "DEC":54.3490},
                    {"RA":202.4842, "DEC":47.2306},
                    {"RA":148.8882, "DEC":69.0653},
                    {"RA":23.4620, "DEC":30.6602}];
        
        var attribs = {"color":"0xff0000", "symbol":"square"};
        var name = "catalogTest";
        var layerData = {"type":"catalog", "name":name, "attribs":attribs, "rows":rows};
        this.createLayer(layerData);
    }
    
    var redBox = "   Polygon J2000 5.0 -5.0 5.0 5.0 355.0 5.0 355.0 -5.0";
    var yellowPoints = " POINT(5.0 -5.0) POINT( 5.0 5.0) POINT( 355.0 5.0 )   POINT(355.0 -5.0) ";   
    var acsM101 = "  Polygon J2000 210.75890230 54.38019650 210.79889830 54.32921760 210.84012320 54.34291740 210.80016510 54.39391000      Polygon J2000  210.79858090 54.32875470 210.75693070 54.38001520 210.71409030 54.36600310 210.75577980 54.31475730";
    
    //var galexM101 = "POLYGON ICRS 211.43464545 54.358924 211.43122163 54.42425429 211.4209877 54.48886881 211.40405577 54.55205962 211.38061136 54.6131344 211.35091133 54.671424 211.31528107 54.72628978 211.27411097 54.77713063 211.22785208 54.82338952 211.17701123 54.86455962 211.12214545 54.90018988 211.06385585 54.92988991 211.00278107 54.95333432 210.93959026 54.97026625 210.87497574 54.98050018 210.80964545 54.983924 210.74431516 54.98050018 210.67970064 54.97026625 210.61650983 54.95333432 210.55543505 54.92988991 210.49714545 54.90018988 210.44227967 54.86455962 210.39143882 54.82338952 210.34517993 54.77713063 210.30400983 54.72628978 210.26837957 54.671424 210.23867954 54.6131344 210.21523513 54.55205962 210.1983032 54.48886881 210.18806927 54.42425429 210.18464545 54.358924 210.18806927 54.29359371 210.1983032 54.22897919 210.21523513 54.16578838 210.23867954 54.1047136 210.26837957 54.046424 210.30400983 53.99155822 210.34517993 53.94071737 210.39143882 53.89445848 210.44227967 53.85328838 210.49714545 53.81765812 210.55543505 53.78795809 210.61650983 53.76451368 210.67970064 53.74758175 210.74431516 53.73734782 210.80964545 53.733924 210.87497574 53.73734782 210.93959026 53.74758175 211.00278107 53.76451368 211.06385585 53.78795809 211.12214545 53.81765812 211.17701123 53.85328838 211.22785208 53.89445848 211.27411097 53.94071737 211.31528107 53.99155822 211.35091133 54.046424 211.38061136 54.1047136 211.40405577 54.16578838 211.4209877 54.22897919 211.43122163 54.29359371 211.43464545 54.358924";
    var galexM101 = "CIRCLE ICRS 210.80964545 54.35892400 0.625";
    var galexNGC5474 = "CIRCLE ICRS 211.25948613 53.67449567 0.625";
    var circle00 = "CIRCLE ICRS 0 0 1.0";
    var circle045 = "CIRCLE ICRS 0 45 1.0";
    var circle080 = "CIRCLE ICRS 0 80 1.0";
    
    ////////////////////////////
    // 'F' : onFootprintLayer() 
    ////////////////////////////
    this.onFootprintLayer = function()
    {
        rows = {"footprint":yellowPoints};
        attribs = {"color":"0xffff00", "symbol":"square"};
        name = "yellowPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        rows = {"footprint":redBox};
        attribs = {"color":"0xff0000"};
        name = "redBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        rows = {"footprint":circle00};
        attribs = {"color":"0x00ff00"};
        name = "circle00";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        rows = {"footprint":circle045};
        attribs = {"color":"0x0000ff"};
        name = "circle045";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        rows = {"footprint":circle080};
        attribs = {"color":"0x00ffff"};
        name = "circle080";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        var rows = {"footprint":acsM101};
        var attribs = {"color":"0xff0000"};
        var name = "acsM101";
        var layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
        
        rows = {"footprint":galexNGC5474};
        attribs = {"color":"0x5555ff"};
        name = "galexNGC5474";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
    }
    
    //////////////////////////
    // 'I' : onImageLayer() 
    //////////////////////////
    this.onImageLayer = function()
    {
        var rows = {"url":"http://archive.stsci.edu/pub/stpr/hs-2009-29-b-large-jpg.jpg", 
                    "ul":"204.29002 -29.83628",
                    "ur":"204.24048 -29.84262",
                    "ll":"204.29741 -29.87950",
                    "lr":"204.24784 -29.88586"};
        var attribs = {"color":"0x00ff00"};
        var layerData = {"type":"image", "attribs":attribs, "rows":[rows]};
        this.createLayer(layerData);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////				
    //
    // AsroView API
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////
    // createLayer() 
    //////////////////////////
    this.createLayer = function(layerData)
    {
        var layer = null;
        if (layerData && layerData.type && isString(layerData.type))
        {
            var type = trim(layerData.type.toLowerCase());
            switch (type)
            {
                case "footprint":
                {
                    layer = new ASTROVIEW.FootprintLayer( this, layerData );
                    break;
                }
                case "catalog":
                {
                    layer = new ASTROVIEW.CatalogLayer( this, layerData );
                    break;
                }
                case "image":
                {
                    console.debug("createImageLayer");
                    //sphere = createImageLayer(layerData);
                    break;
                }
            }
        }
        
        // Add new AstroView layer to the Scene
        if (layer)
        {
            this.scene.add(layer);
        }
        
        // Return the Layer ID
        return (layer ? layer.lid : "");
    }
    
    //////////////////////////
    // readLayer() 
    //////////////////////////
    this.readLayer = function(lid)
    {
        var layer = this.scene.getChildByName(lid, true);
        return (layer ? layer.layerData : "");
    }
    
    //////////////////////////
    // udpateLayer() 
    //////////////////////////
    this.udpateLayer = function(layerData)
    {
        var layer = null;
        if (layerData.lid)
        {
            layer = this.scene.getChildByName(layerData.lid, true);
            if (layer)
            {
                this.scene.remove(layer);
                this.createLayer(layerData);
            }
        }
        return (layer != null);
    }
    
    //////////////////////////
    // deleteLayer() 
    //////////////////////////
    this.deleteLayer = function(lid)
    {
        var layer = this.scene.getChildByName(lid, true);
        if (layer)
        {
            this.scene.remove(layer);
            layer.clean();
        }
        return (layer ? layer.lid : "");
    }
    
    //////////////////////////
    // moveTo() 
    //////////////////////////
    this.moveTo = function(coord)
    {
        // Check all mixed case variations for RA and DEC values
        var ra = coord.ra != null ? coord.ra : (coord.RA != null ? coord.RA : (coord.Ra != null ? coord.Ra : null));
        var dec = coord.dec != null ? coord.dec : (coord.DEC != null ? coord.DEC : (coord.Dec != null ? coord.Dec : null));
        var zoom = coord.zoom != null ? coord.zoom : (coord.ZOOM != null ? coord.ZOOM : (coord.Zoom != null ? coord.Zoom : null));
        
        if (ra != null && dec != null)
        {
            this.controller.moveTo(ra, dec, zoom);
        }
    }
    
    //////////////////////////
    // go() 
    //////////////////////////
    this.go = function(msg)
    {
        alert("AstroBasicView:go() msg=" + msg);
        return msg;
    }
    
    //////////////////////////
    // toJson() 
    //////////////////////////
    this.toJson = function (obj) 
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
    // fromJson() 
    //////////////////////////
    this.fromJson = function (str) 
    {  
        if (str === "") str = '""';  
        eval("var p=" + str + ";");  
        return p;  
    }
			   
    ///////////////
    // Main
    ///////////////
    console.debug("Go AstroView!!!");
    this.createScene();
    this.addEvents();
    this.renderScene();
};
