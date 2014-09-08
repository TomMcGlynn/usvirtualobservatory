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
//const galexurl = "http://mastproxyvm1.stsci.edu/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
//const dssurl = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs directly to Apache Server
//const galexurl = "http://mastproxyvm1.stsci.edu:8080/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
//const dssurl = "http://mastproxyvm1.stsci.edu:8080/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs through Mashup Proxy
const galexurl = "http://mastdev.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=http://mastproxyvm1.stsci.edu:8080/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
const dssurl = "http://mastdev.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=http://mastproxyvm1.stsci.edu:8080/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
    
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
/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( camera, scene, container, renderer )
{
	// Pull out camera and container from AstroBasicView
	this.camera = camera;
	this.scene = scene;
	this.container = ( container !== undefined ) ? container : document;
	this.renderer = renderer;
	
	// Initialize Field of View Ranges
	this.fovmax = ASTROVIEW.FOV_LEVEL_MAX;
	this.fovmin = ASTROVIEW.FOV_LEVEL_MIN;
	
	// Initialize Zoom Level
    this.zlevel = ASTROVIEW.ZOOM_LEVEL_MIN;
	this.vizlevel = ASTROVIEW.ZOOM_LEVEL_MIN;

    //
    // Fov ===> Zoom Level Lookup Table
    //
    //                      Fov Value       Zoom Level
	//                      --------------  ----------
    this.zoomLookupTable =  [this.fovmax+1,	// 0
							 this.fovmax+1,	// 1
							 this.fovmax+1,	// 2
							 this.fovmax+1,	// 3
							 10,		    // 4
							 5,			    // 5
							 2.5,		    // 6
							 1.25,		    // 7
							 0.75,		    // 8
							 0.355,		    // 9
							 0.125,		    // 10
							 0.0625,	    // 11
							 0.00001];	    // 12
	
	this.visibleLayers = "4, 5, 6, 7, 8, 9, 10";
	//this.visibleLayers = "4, 6, 8, 10";

    // Camera Position
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = this.fovmax;    
    this.zoomSpeed = 0.1;  // How fast you zoom on each mouse wheel
    this.panSpeed = 0.2;   // How fast you pan on each mouse move.
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
	this.frustumMatrix = new THREE.Matrix4();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.down = new THREE.Vector2();
    this.last = new THREE.Vector2();
    this.delta = new THREE.Vector2();
    this.mouseDown = false;
    
    // Position Events
    this.windowHalfX = window.innerWidth/2.0;
    this.windowHalfY = window.innerHeight/2.0;
	
	// Current Mouse Position in ALL Coordinate Systems
	this.mouse = {};
	this.mouse.screen = new THREE.Vector3();
	this.mouse.normal = new THREE.Vector3();
	this.mouse.world = new THREE.Vector3();
	this.mouse.layer  = new THREE.Vector3();
	this.mouse.ra = 0.0;
	this.mouse.dec = 0.0;
	this.mouse.sra = "";
	this.mouse.sdec = "";

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
		// Resize Events
		window.addEventListener( 'resize',  bind(this, this.onResize),  false );
		
        // Mouse Events
        this.container.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        this.container.addEventListener( 'mousemove', bind(this, this.onMouseMove), false );
        this.container.addEventListener( 'mousedown', bind(this, this.onMouseDown), false );
        this.container.addEventListener( 'mouseup',   bind(this, this.onMouseUp),   false );
        
        // Touch Events
        this.container.addEventListener( 'touchstart', bind(this, this.onTouchStart), false );
        this.container.addEventListener( 'touchmove',  bind(this, this.onTouchMove),  false );
        this.container.addEventListener( 'touchend',   bind(this, this.onTouchEnd),   false );
        
        // Mouse Wheel Events: Need both for WebKit and FF
        window.addEventListener('DOMMouseScroll', bind(this, this.onMouseWheel), false);
        window.addEventListener('mousewheel',     bind(this, this.onMouseWheel), false);
        
        // Keypress events
        document.addEventListener( "keypress" , bind(this, this.onKeyPress), false);
        //document.onkeypress=bind(this, this.onKeyPress);
    }
	
	/////////////////////////
    // Resize Event
    /////////////////////////
	this.onResize = function( event )
    {
		this.renderer.setSize( window.innerWidth, window.innerHeight );
		this.camera.aspect = window.innerWidth / window.innerHeight;
		this.camera.updateProjectionMatrix();
	}
      
    /////////////////////////
    // Mouse Events
    /////////////////////////
    this.onMouseDown = function(event)
    {
        event.preventDefault();
        this.mouseDown=true;
        
        this.last.x = event.clientX;
        this.last.y = event.clientY;
        this.down.x = event.clientX;
        this.down.y = event.clientY;		
    }
    
    this.onMouseMove = function(event)
    {
        event.preventDefault();
        
        if (this.mouseDown)
        {
            var rotationX = (event.clientY - this.last.y) * TO_RADIANS * (this.camera.fov/(this.fovmax) * this.panSpeed);
            var rotationY = (event.clientX - this.last.x) * TO_RADIANS * (this.camera.fov/(this.fovmax) * this.panSpeed);
            
            this.setCameraRotationDeltaXY(rotationX, rotationY);
                
            this.last.x = event.clientX;
            this.last.y = event.clientY;
        }
         
        this.updatePosition(event);
    }
        
    this.onMouseUp = function(event)
    {
        event.preventDefault();
        this.mouseDown=false;
        
        // Stop Rotation if User clicked single point
        if (event.clientX == this.down.x &&
            event.clientY == this.down.y)
        {
            this.setCameraRotationDeltaXY(0, 0); // Stop spinning rotation
        }
        
        // Debug Info on mouse click mouse
        this.dumpPosition();
		
		// Check for Object Selection
		this.updateSelected();
    }
    
    this.onMouseOut = function(event)
    {
        event.preventDefault();
        this.mouseDown = false;
    }
    
    this.onMouseWheel = function(event)
    {		  
        // Get wheel direction for both WebKit or FF
        var delta = ((typeof event.wheelDelta != "undefined") ? (-event.wheelDelta) : event.detail );
        this.zoom(delta);
    }
    
    /////////////////////////
    // Keyboard Events
    /////////////////////////
    this.onKeyPress = function(event)
    {
        var unicode=event.keyCode? event.keyCode : event.charCode;
        //alert("onKeyPress: " + unicode); // find the char code		
        switch(unicode)
        {
            case 37:  	this.rotateRight(); break;
            case 39: 	this.rotateLeft(); break;
            case 38: 	this.rotateDown(); break;
            case 40: 	this.rotateUp(); break;
            case 105: 	this.zoomIn(); break;
            case 111:  	this.zoomOut(); break;
        }
    }
    
    /////////////////////////
    // Touch Events
    /////////////////////////
    this.onTouchStart = function(event)
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
            this.mouseDown=true;

            this.last.x = event.touches[ 0 ].pageX;
            this.last.y = event.touches[ 0 ].pageY;
            
            this.down.x = event.touches[ 0 ].pageX;
            this.down.y = event.touches[ 0 ].pageY;
        }
    }
    
    this.onTouchMove = function( event )
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
        
            if (this.mouseDown)
            {
                var rotationX = (event.touches[ 0 ].pageY - this.last.y) * TO_RADIANS * (this.camera.fov/(this.fovmax));
                var rotationY = (event.touches[ 0 ].pageX - this.last.x) * TO_RADIANS * (this.camera.fov/(this.fovmax));
                
                this.setCameraRotationDeltaXY(rotationX, rotationY);
                    
                this.last.x = event.touches[ 0 ].pageX;
                this.last.y = event.touches[ 0 ].pageY;
            }
        }
    }
    
    this.onTouchEnd = function(event)
    {
        if ( event.touches.length == 1 )
        {
            event.preventDefault();
            this.mouseDown=false;
            
            // Stop Rotation if User tapped single point
            if (event.touches[ 0 ].pageX == this.down.x &&
                event.touches[ 0 ].pageY == this.down.y)
            {
                this.setCameraRotationDeltaXY(0, 0);
            }
        }
    }
    
    /////////////////////////
    // Actions
    /////////////////////////   
    this.rotateLeft = function()
    {
        var deltaY = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateRight = function()
    {
        var deltaY = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(0, deltaY);
    }
    
    this.rotateUp = function()
    {
        var deltaX = -.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.rotateDown = function()
    {
        var deltaX = +.02 * this.camera.fov/(this.fovmax);
        this.setCameraRotationDeltaXY(deltaX, 0);
    }
    
    this.setCameraRotationDeltaXY = function(deltax, deltay)
    {
        // Update the Target Rotation(s)
        this.cameraRotationX = this.camera.rotation.x + deltax;
        this.cameraRotationY = this.camera.rotation.y + deltay;
    }
    
    this.zoom = function(delta)
    {
        (delta > 0 ? this.zoomOut() : this.zoomIn());
    }
    
    this.zoomIn = function()
    {
        var deltaFov = this.cameraFov * this.zoomSpeed;
        this.cameraFov -= deltaFov;
        this.cameraFov = (this.cameraFov < this.fovmmin ? this.fovmmin : this.cameraFov);
    }
    
    this.zoomOut = function()
    {
        var deltaFov = this.cameraFov * this.zoomSpeed;
        this.cameraFov += deltaFov;
        this.cameraFov = (this.cameraFov > this.fovmax ? this.fovmax : this.cameraFov);
    }
		
    this.moveTo = function(ra, dec, zlevel)
    {
        console.debug("moveTo: [" + ra + ", " + dec + ", " +  zlevel + "]");
        
        // Check optional zlevel param
        if (!zlevel) zlevel = 10;
        
        // point camera to ra, dec
        this.camera.rotation.y = this.cameraRotationY = ra * TO_RADIANS;
        this.camera.rotation.x = this.cameraRotationX = dec * TO_RADIANS;
		
		// Set the Zoom Level
		this.zoomTo(zlevel);
	}
	
	this.zoomTo = function(zlevel)
	{
        // now zoom in to max field of view for zlevel
		if (zlevel > 0 && zlevel < this.zoomLookupTable.length)
		{
			this.camera.fov = this.cameraFov = (this.zoomLookupTable[zlevel-1] + this.zoomLookupTable[zlevel])/2.0;
			this.camera.updateProjectionMatrix();
		}
    }
    
    /////////////////////////
    // render()
    /////////////////////////  
    this.render = function()
    {
        // Bounds Check on RotationX (Declination)
        if (this.cameraRotationX > RADIANS_90) this.cameraRotationX = RADIANS_90;
        if (this.cameraRotationX < -RADIANS_90) this.cameraRotationX = -RADIANS_90;
        
        // Rotate the Camera
        if (this.camera.rotation.x !== this.cameraRotationX)
        {
            this.camera.rotation.x += (this.cameraRotationX - this.camera.rotation.x) * 0.5;
        }
        
        if (this.camera.rotation.y !== this.cameraRotationY)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
           
        // Update the Field of View
        if (this.camera.fov !== this.cameraFov)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
			this.camera.updateProjectionMatrix();
		}
		
		//
		// Update the frustum matrix, and zoom level(s) 
		//
		this.updateController();
    }
	
	/////////////////////////
    // updateController()
    /////////////////////////
	this.updateController = function()
    {
		this.updateFrustumMatrix();
		this.updateZoomLevel();
		this.updateVisibleZoom();
	}
	
	/////////////////////////
    // updateFrustumMatrix()
    ///////////////////////// 
    this.updateFrustumMatrix = function()
    {
		// Only update the frustum matrix if the camera has changed
		this.frustumMatrix.multiply(this.camera.projectionMatrix, this.camera.matrixWorldInverse);
		this.frustum.setFromMatrix( this.frustumMatrix );
    }
	
    /////////////////////////
    // updateZoomLevel()
    /////////////////////////
    this.updateZoomLevel = function()
    {
		var i;
		
		// Search Zoom Lookup Table until:
		// Camera fov is within specified range AND zoom layer is visible 
		for (i=1; i<this.zoomLookupTable.length; i++)
		{
			if (this.zoomLookupTable[i] <= this.camera.fov)
			{
				break;										
			}
		}
		
		if (this.zlevel !== i)
		{
			this.zlevel = i;
			console.debug("*** Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel + " ***");
		}
    }
	
	this.getZoomLevel = function()
    {
		return this.zlevel;
	}
	
	///////////////////////////
    // updateVisibleZoom()
    ///////////////////////////
    this.updateVisibleZoom = function()
    {
		var zlevel = this.zlevel;
		do 
		{
			if (this.visibleLayers.indexOf(zlevel) != -1)
			{
				break;
			}
		} while (--zlevel > 0);
		
		if (this.vizlevel != zlevel)
		{
			this.vizlevel = zlevel;
			console.debug("*** Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel + " ***");
		}
    }
	
	this.getVisibleZoom = function()
    {
		return this.vizlevel;
	}
	
	/////////////////////////
    // updateSelected()
    ///////////////////////// 
	this.updateSelected = function( )
	{
		/*
		event.preventDefault();
		var vector = new THREE.Vector3( mouse.x, mouse.y, 0.5 );
		projector.unprojectVector( vector, camera );
		var ray = new THREE.Ray( camera.mouse, vector.subSelf( camera.mouse ).normalize() );
		*/
		
		this.selected = null;
	
		if (this.scene)
		{
			for (var i=0; i<this.scene.children.length; i++)
			{
				var child = this.scene.children[i];
				if (child instanceof ASTROVIEW.GraphicsLayer)
				{
					this.selected = child.getSelected(this.mouse, this);
					if (this.selected && this.selected.length > 0)
					{
						break;
					}
				}
			}
		}
		
		if ( this.selected && this.selected.length > 0 )
		{		
			console.debug("Got Selected:" + this.selected.length);
		}
		else
		{
			console.debug("Crap, No Selection.");
		}
	}
    
    /////////////////////////
    // updatePosition()
    /////////////////////////  
    this.updatePosition = function(event)
    {
		// Store Screen Vector
        this.mouse.screen.x = event.clientX;
        this.mouse.screen.y = event.clientY;
        this.mouse.screen.z = 1.0; 

		// Screen ===> World (Normalized)
        this.screenToWorld(this.mouse.screen, this.mouse.normal);
        this.mouse.normal.normalize();
		
		// World (Normalized) ===> World (Scaled)
		this.mouse.world.copy(this.mouse.normal);
        this.mouse.world.multiplyScalar(ASTROVIEW.RADIUS_SPHERE);
		
		// World (Normalized) ===> Layer (Scaled)
		this.mouse.layer.copy(this.mouse.normal);
        this.mouse.layer.multiplyScalar(ASTROVIEW.RADIUS_LAYER);
		
		// World (Normalized) ===> RA/DEC
        this.worldToRaDec(this.mouse.normal, this.mouse);

        // RA/DEC ===> Sexagesimal (String Representation)
        this.mouse.sra = this.degToHMS(this.mouse.ra);
        this.mouse.sdec = this.degToDMS(this.mouse.dec);
    }
    
    this.dumpPosition = function()
    {
       console.debug("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
					 " [layer x,y,z: " + this.mouse.layer.x.toFixed(3) + "," + this.mouse.layer.y.toFixed(3) + "," + this.mouse.layer.z.toFixed(3) + "]" + 
                     " [ra,dec: " + this.mouse.ra.toFixed(3) + "," + this.mouse.dec.toFixed(3) + "]" +
					 " [fov: " + this.camera.fov.toFixed(3) + "]" +
					 " [zoom/visible: " + this.zlevel + "/" + this.vizlevel + "]" );
    }
		
    this.screenToWorld = function(screen, world)
    {
		world.copy(screen);   
        world.x = world.x / this.windowHalfX - 1;
        world.y = - world.y / this.windowHalfY + 1;
        this.projector.unprojectVector( world, this.camera );
    }
    
    this.worldToScreen = function(world, screen)
    {
		screen.copy(world);
        this.projector.projectVector( screen, this.camera );
        screen.x = ( screen.x + 1 ) * this.windowHalfX;
        screen.y = ( - screen.y + 1) * this.windowHalfY;
    }
    
    this.worldToRaDec = function(world, coord)
    {
        // Store the ra, dec values
        coord.dec = Math.asin(world.y) * TO_DEGREES;			
        coord.ra = Math.atan2(-world.x, -world.z) * TO_DEGREES;
        if (coord.ra < 0) coord.ra += 360.0;
    }
    
    this.degToDMS = function(degrees)
    {        
        var deg = Math.abs(degrees);
        var deg_floor = Math.floor(deg);
        var min = 60 * (deg - deg_floor);
        var min_floor = Math.floor(min);
        var sec = 60 * (min - min_floor);
        
        var dms = ((degrees < 0.0) ? '-' : '+') +
                  this.pad(deg_floor, 2) + ":" +
                  this.pad(min_floor, 2)  + ":" +
                  this.pad(sec.toFixed(3), 2);
        
        return dms;
    }
    
    this.degToHMS = function(degrees)
    {
        var hours = degrees/15.0;
        var hours_floor = Math.floor(hours);
        var min = 60 * (hours - hours_floor);
        var min_floor = Math.floor(min);
        var sec = 60 * (min - min_floor);
        
        var hms = this.pad(hours_floor, 2) + ":" +
                  this.pad(min_floor, 2) + ":" +
                  this.pad(sec.toFixed(3), 2);
        
        return hms;
    }
    
    this.pad = function (num, size)
    {
		var s = num.toString();
		
		switch (size - s.length)
		{
			case 1: s = '0' + s; break;
			case 2: s = '00' + s; break;
			case 3: s = '000' + s; break;
			case 4: s = '0000' + s; break;
			case 5: s = '00000' + s; break;
			case 6: s = '000000' + s; break;
			case 7: s = '0000000' + s; break;
			case 8: s = '00000000' + s; break;
		}
        return s;
    }
			   
    ///////////////
    // Main
    ///////////////
    this.addEvents();
};

//////////////////////////
// DiamondGeometry()
//////////////////////////
ASTROVIEW.DiamondGeometry = function(uv, tx, ty, quadrant, color, zlevel, did, radius)
{
    THREE.Geometry.call( this );

    this.uv = uv;	
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.color = color;
    this.zlevel = zlevel;
    this.did = did;                                         // Diamond ID: 0 - 3
    this.tid = "[" + zlevel + "," + tx + "," + ty + "]";    // Toast ID: [zoom, tx, ty]
    this.radius = radius;
    this.dynamic = true;
	
	// Geometry Center and Radius: must be initialized to null to be computed once only
	this.gcenter = null;
	this.gradius = null;
    
    // Create this.vertices[] array from uv[] unit vector3 array
    if (uv && uv.length == 4)
    {
        for (var i=0; i<4; i++)
        {
			// Create new Vertex from the cloned Unit Vector.  Then scale it to the Sphere Radius
			var v = new THREE.Vertex(uv[i].clone());
			v.position.multiplyScalar(radius);   
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length == 4)
    {
        var fuv = this.getFaceUV(quadrant);		// get Face uv[] array based on quadrant
        this.createFaces(this.vertices, fuv);	// create this.faces[] based on this.vertices[] and Face UV[]
        this.computeCentroids();                // inherited call
    }
};

ASTROVIEW.DiamondGeometry.prototype = new THREE.Geometry();
ASTROVIEW.DiamondGeometry.prototype.constructor = ASTROVIEW.DiamondGeometry;

//////////////////////////
// clean()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.clean = function()
{
	if (this.faces && this.faces.length > 1)
	{
		this.cleanFace(this.faces[0]);
		this.cleanFace(this.faces[1]);
	}
	this.faces = undefined;
	this.uv = undefined;
	this.vertices = undefined;
	this.faceVertexUvs = undefined;
}

ASTROVIEW.DiamondGeometry.prototype.cleanFace = function(face)
{
	face.normal = undefined;
	face.vertexNormals = undefined;
	face.vertexColors = undefined;
	face.vertexTangents = undefined;
	face.centroid = undefined;
}

//////////////////////////
// createFaces()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.createFaces = function(v, fuv)
{
    if ((v && v.length == 4) && (fuv && fuv.length == 4))
    {
        var normal = new THREE.Vector3( 0, 0, 1 );
        
        //
        // Create 'Upper' Face 0 (v0 -> v3 -> v1)
        //  
        var face0 = new THREE.Face3( 0, 3, 1);
        face0.normal.copy( normal );
        face0.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face0 );
        this.faceVertexUvs[ 0 ].push( [ fuv[0], fuv[3], fuv[1] ]);
        
        //
        // Create 'Lower' Face 1 (v2 -> v1 -> v3)
        //
        var face1 = new THREE.Face3( 2, 1, 3 );
        face1.normal.copy( normal );
        face1.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face1 );
        this.faceVertexUvs[ 0 ].push( [ fuv[2], fuv[1], fuv[3] ]);
    }
};

const UV_00 = new THREE.UV( 0, 0 );
const UV_01 = new THREE.UV( 0, 1 );
const UV_10 = new THREE.UV( 1, 0 );
const UV_11 = new THREE.UV( 1, 1 );

const FUV_QUADRANT_I   = [UV_10, UV_11, UV_01, UV_00];
const FUV_QUADRANT_II  = [UV_00, UV_10, UV_11, UV_01];
const FUV_QUADRANT_III = [UV_01, UV_00, UV_10, UV_11];
const FUV_QUADRANT_IV  = [UV_11, UV_01, UV_00, UV_10];

//////////////////////////
// createFaceUV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getFaceUV = function(quadrant)
{	
    switch (quadrant)
    {
        case "I"  : return FUV_QUADRANT_I;
        case "II" : return FUV_QUADRANT_II;
        case "III": return FUV_QUADRANT_III;
        case "IV" : return FUV_QUADRANT_IV;
		default   : return null;
	}
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.inFrustum = function(frustum)
{
	// Calculate geometric center and radius of the diamond 'sphere' and see if it overlaps the frustum
	var center = this.getDiamondCenter();
	var radius = this.getDiamondRadius();
	return (this.sphereInFrustum(center, radius, frustum) || this.verticesInFrustum(frustum));
	
	/*
	if (this.faces && this.faces.length == 2)
	{
		return (this.faceInFrustum(this.faces[0], frustum) ||
				this.faceInFrustum(this.faces[1], frustum));
	}
	return false;
	*/
};

//////////////////////////
// getDiamondCenter()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getDiamondCenter = function()	
{
	if (!this.gcenter)
	{
		if (!this.vertices || this.vertices.length !== 4) return null;
		
		var v0 = this.vertices[0].position;
		var v1 = this.vertices[1].position;
		var v2 = this.vertices[2].position;
		var v3 = this.vertices[3].position;
		
		var x = (v0.x + v1.x + v2.x + v3.x)/4.0;
		var y = (v0.y + v1.y + v2.y + v3.y)/4.0;
		var z = (v0.z + v1.z + v2.z + v3.z)/4.0;
		this.gcenter = new THREE.Vector3(x, y, z);
	}
	return this.gcenter;
};

//////////////////////////
// getDiamondRadius()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getDiamondRadius = function()	
{
	if (!this.gradius)
	{
		if (!this.vertices || this.vertices.length !== 4) return null;
		if (!this.gcenter) this.getDiamondCenter();
		
		var v0 = this.vertices[0].position;
		var v1 = this.vertices[1].position;
		var v2 = this.vertices[2].position;
		var v3 = this.vertices[3].position;
		
		var dv0 = this.gcenter.distanceTo(v0);
		var dv1 = this.gcenter.distanceTo(v1);
		var dv2 = this.gcenter.distanceTo(v2);
		var dv3 = this.gcenter.distanceTo(v3);
		
		this.gradius = Math.max(dv0, dv1, dv2, dv3);
	}

    return this.gradius;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.sphereInFrustum = function(centerv, radius, frustum)
{
    var distance;
    var planes = frustum.planes;
    for ( var i=0; i<6; i++ )
    {
        distance = planes[i].x * centerv.x +
                   planes[i].y * centerv.y +
                   planes[i].z * centerv.z +
                   planes[i].w;
        
        if (distance < -radius)
        {
            return false;
        }
    }

    return true;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.faceInFrustum = function(face, frustum)
{
	THREE.Face3
	
	if (!face.centroid) this.computeCentroids();
	var radius = this.getFaceRadius(face);
	
	var distance;
	var planes = frustum.planes;
	for ( var i=0; i<6; i++ )
	{
		distance = planes[i].x * face.centroid.x +
				   planes[i].y * face.centroid.y +
				   planes[i].z * face.centroid.z +
				   planes[i].w;
		
		if (distance < -radius)
		{
			return false;
		}
	}

    return true;
};

ASTROVIEW.DiamondGeometry.prototype.getFaceRadius = function(face)
{
	if (!face.__radius)
	{
		var v0 = this.vertices[face.a].position;
		var v1 = this.vertices[face.b].position;
		var v2 = this.vertices[face.c].position;
		
		var dv0 = face.centroid.distanceTo(v0);
		var dv1 = face.centroid.distanceTo(v1);
		var dv2 = face.centroid.distanceTo(v2);
		
		face.__radius = Math.max(dv0, dv1, dv2);			
	}
	
	return face.__radius;
}

//////////////////////////
// verticesInFrustum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.verticesInFrustum = function(frustum)
{
    for (var i=0; i<this.vertices.length; i++)
    {
        var v3=this.vertices[i].position;
        if (this.pointInFrustum(v3, frustum))
        {
            return true;
        }
    }
    return false;
};

//////////////////////////
// pointInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.pointInFrustum = function(v3, frustum)  // THREE.Vector3, THREE.Frustum
{
    var planes = frustum.planes;
    
    for (var p=0; p<6; p++ )
    {
        //
        // Note that for an arbitary point (x,y,z), the distance to the plane is:
        // d = A*x + B*y + C*z + D
        //
        if ((planes[p].x * v3.x) + (planes[p].y * v3.y) + (planes[p].z * v3.z) + planes[p].w <= 0)
        {
            return false;
        }
    }
    return true;
};
		
//////////////////////////
// expandDiamond()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.expandDiamond = function(depth, zlevel, radius) 
{
    // Store default args
    if (!depth) depth = 1;
    if (!zlevel) zlevel = this.zlevel + 1;
    if (!radius) radius = this.radius;
    
    //
    //      Expand each Diamond by number of 'depth' passed in, starting at 'zlevel'
    //
    //      NOTE: Diamond.uv[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //	 	Make new points on the Diamond (a, b, c, d, e):
    //
    //      a = (uv0+uv1)/2
    //      b = (uv0+uv2)/2
    //      c = (uv2+uv3)/2
    //      d = (uv3+uv0)/2
    //      e = (uv3+uv1)/2
    //
    //          uv0
    //          /\		    
    //         /  \
    //      d / D0 \ a         Construct 4 new diamonds    
    //       /\    /\            D0 = [uv0,   a,   e,   d]       
    //      /  \ e/  \           D1 = [  a, uv1,   b,   e]       
    // uv3 / D3 \/ D1 \ uv1      D2 = [  e,   b, uv2,   c]       
    //     \    /\    /          D3 = [  d,   e,   c, uv3]      
    //      \  /  \  /
    //     c \/ D2 \/ b
    //        \    /
    //         \  /
    //          \/
    //          uv2
    //	
    //  NOTE: The above perspective is from INSIDE the sphere with v0 pointing true NORTH
    //	
    
    //
    // Initialize the outArray which is used as input for looping below
    //
    var outArray = [];
    outArray.push(this); 			
    
    //
    // Repeat for each level:
    //
    // Copy the existing output array to the input array
    // Loop through the diamonds in the input array and expand them
    // Store the expanded diamonds on the output array
    //
    // Net affect is only the final level of expanded diamonds are in the outArray
    // Add this expanded level as children to the current instance
    //
    for (var l=1; l<depth+1; l++)
    { 	
        var loopArray = outArray;
        outArray = [];	
        
        // 
        // Loop through all diamands to expand
        //	
        for (var i=0; i<loopArray.length; i++)
        {			
            var dg = loopArray[i];	// ASTROVIEW.DiamondGeometry
            var uv0 = dg.uv[0];
            var uv1 = dg.uv[1];
            var uv2 = dg.uv[2];
            var uv3 = dg.uv[3];
            
            //
            //	 Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = new THREE.Vector3().add(uv0, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var b = new THREE.Vector3().add(uv1, uv2).multiplyScalar(0.5).normalize();   // THREE.Vector3 
            var c = new THREE.Vector3().add(uv2, uv3).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var d = new THREE.Vector3().add(uv3, uv0).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var e = new THREE.Vector3().add(uv3, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            
            //
            //   Construct new diamonds
            //
            //	 [uv0,  a,   e,   d]
            //	 [ a, uv1,   b,   e]
            //	 [ e,   b, uv2,   c]
            //   [ d,   e,   c, uv3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.DiamondGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, 0)
                    break;
                } 
            }
            
            //
            // Add the diamonds to the returned array
            //
            outArray.push(dg0);
            outArray.push(dg1);
            outArray.push(dg2);
            outArray.push(dg3);
        }
    }	
    
    return outArray;
};/////////////////////////
// DiamondMesh
/////////////////////////
ASTROVIEW.DiamondMesh = function( geometry, material )
{
    if (!material)
    {
        material = ASTROVIEW.DefaultMaterial;
    }

	THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = geometry.tid;
    this.flipSided = true;
    this.doubleSided = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image
    this.defaulturl = ASTROVIEW.DefaultTextureUrl;
	
	// Image Texture Properties
	this.texture = null;
};

ASTROVIEW.DiamondMesh.prototype = new THREE.Mesh();
ASTROVIEW.DiamondMesh.prototype.constructor = ASTROVIEW.DiamondMesh;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.DefaultMaterialUrl = "../AstroView/textures/Diamond.png";
ASTROVIEW.DefaultMaterial = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.DefaultMaterialUrl) } );

/////////////////////////
// render()
/////////////////////////
ASTROVIEW.DiamondMesh.prototype.render = function( av )
{   
	var frustum = av.controller.frustum;
	var vizlevel = av.controller.getVisibleZoom();
		
	// Update our Texture Visibility, based on Active Zoom Level
	this.setVisible(vizlevel === this.geometry.zlevel);
		
	// Check if we are in the Viewing Frustum
    if (this.inFrustum(frustum))
    {	
        if (vizlevel === this.geometry.zlevel)
        {	
			// CASE I: Inside the Frustum, my level is visible, Load my image, Remove any Children (for Zoom Out)
					
			// If survey url has changed, load new image
			if (av.baseurl !== this.baseurl)
			{
				var url = new String(av.baseurl);
				var dg = this.geometry; // DiamondGeometry	
				this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
				this.cleanTexture(av);
				this.loadTexture(this.imageurl);
				this.baseurl = av.baseurl;
			}
		
			// Remove all Children, when View State is IDLE
			if (av.viewState === ASTROVIEW.VIEW_STATE_IDLE)
			{
				if (this.children.length > 0)
				{
					this.removeDiamondChildren(av);
				}
			}
        }
        else if (vizlevel > this.geometry.zlevel)
		{
			// CASE II: In the Frustum, Zoom In Occured, Expand my Children, Render Children
	
			// Expand Children (If necessary)
			if (this.children.length == 0)
			{
				this.expandDiamondChildren();
			}
						
			// Render Children
			if (this.children.length > 0)
			{
				this.renderDiamondChildren(av);
			}
		}
    }
	else	
	{
		// CASE III: Not in the Viewing Frustum,
		// Remove all Children, when View State is IDLE
		if (av.viewState === ASTROVIEW.VIEW_STATE_IDLE)
		{
			if (this.children.length > 0)
			{
				this.removeDiamondChildren(av);
			}
		}
	}
};

ASTROVIEW.DiamondMesh.prototype.loadTexture = function(url)
{
	// Create Texture from Image
    this.texture = new THREE.Texture( new Image() );

    this.texture.image.onload = bind(this, this.onLoad);
    this.texture.image.onerror = bind(this, this.onError);
    
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
}

ASTROVIEW.DiamondMesh.prototype.onLoad = function(event)
{
    console.debug(this.name + " onLoad() url: " + this.texture.image.src);
    this.texture.needsUpdate = true;
	
	// Create Material from Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture } );
}

ASTROVIEW.DiamondMesh.prototype.setVisible = function(visible)
{
	if (this.material) this.material.opacity = (visible ? 1.0 : 0.0);
}

ASTROVIEW.DiamondMesh.prototype.onError = function(event)
{
    console.debug(this.name + " *** onError() url: " + this.texture.image.src);
    this.loadTexture(this.defaulturl);
}

ASTROVIEW.DiamondMesh.prototype.updateOpacity = function()
{
    if (this.material)
    {
        this.material.opacity = this.opacity;

        /*** Gradual Opacity ***
        this.material.opacity += (this.opacity - this.material.opacity) * 0.5;
        if (Math.abs(this.material.opacity - this.opacity) < 0.1)
        {
            this.material.opacity = this.opacity;
        }
        */
    }
}

ASTROVIEW.DiamondMesh.prototype.expandDiamondChildren = function()
{
    var dga = this.geometry.expandDiamond();
    for (i=0; i<dga.length; i++)
    {
        var dm = new ASTROVIEW.DiamondMesh( dga[i] );
        this.add(dm);
    }
};

ASTROVIEW.DiamondMesh.prototype.renderDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
	{
		var dm = this.children[i];
		dm.render(av);
	}
};

ASTROVIEW.DiamondMesh.prototype.removeDiamondChildren = function( av )
{
	var names = "";
    while (this.children.length > 0)
	{
		var dm = this.children[0];
		if (dm.children.length > 0) dm.removeDiamondChildren(av);
		names += dm.name + " ";
		dm.clean(av);
		this.remove(dm);	// NOTE: Decrements this.children.length
	}
	this.children = [];
	console.debug(this.name + " Removed Children Diamonds : " + names);
};

ASTROVIEW.DiamondMesh.prototype.clean = function( av )
{
	// Remove the Texture, Image and Material
	this.cleanTexture(av);
	
	// Remove geometry and mesh from WebGL Graphics Memory 
	if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
	{
		if (this.geometry) av.renderer.deallocateObject(this.geometry);		
		av.renderer.deallocateObject(this);
	}
	
	// Remove all Object References
	if (this.geometry)
	{
		this.geometry.clean();
		this.geometry = undefined;
	}
}

ASTROVIEW.DiamondMesh.prototype.cleanTexture = function( av )
{		
	// Texture is created from Image
	if (this.texture)
	{
		// Remove texture from WebGL Graphics Memory 
		if (av.renderer && av.renderer instanceof THREE.WebGLRenderer)
		{
			av.renderer.deallocateTexture(this.texture);
		}
		
		if (this.texture.image)
		{
			if (this.texture.image.onload) this.texture.image.onload = undefined;
			if (this.texture.image.onerror) this.texture.image.onerror = undefined;
			this.texture.image = undefined;
		}
		this.texture = undefined;
	}
}

ASTROVIEW.DiamondMesh.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
};
/////////////////////////////
// DiamondSphere()
/////////////////////////////
ASTROVIEW.DiamondSphere = function (  )
{ 
    THREE.Object3D.call( this );
    this.name = "DiamondSphere"
    this.createDiamondSphere();
};

ASTROVIEW.DiamondSphere.prototype = new THREE.Object3D();
ASTROVIEW.DiamondSphere.prototype.constructor = ASTROVIEW.DiamondSphere;

/////////////////////////////
// createDiamondSphere3D()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondSphere = function()
{
    //
    // 3D Quadrant (I, II, III, IV) ====> TOAST [tx,ty] mapping
    //
    ///////////////////////////////////////////////////
    //
    //                        -X
    //                         ^                    
    //                IV       |      III
    //              [0,0]      |     [1,0]
    //                         |
    //   <)  +Z <--------------+------------> -Z
    //  eye                    |
    //                I        |      II
    //              [0,1]      |     [1,1]
    //                         V  
    //                        +X
    //
    ////////////////////////////////////////////////////
    var zlevel = 1;
    
    //
    // STEP 1: Create Unit Vectors for the Unit Sphere
    //
    var YY = new THREE.Vector3(0.0,  1.0,  0.0); // +Y
    var _Y = new THREE.Vector3(0.0, -1.0,  0.0); // -Y
                    
    var XX = new THREE.Vector3( 1.0,  0.0,  0.0); // +X
    var _X = new THREE.Vector3(-1.0,  0.0,  0.0); // -X	

    var ZZ = new THREE.Vector3(0.0,  0.0,  1.0); // +Z
    var _Z = new THREE.Vector3(0.0,  0.0, -1.0); // +Z
    
    //
    // STEP 2: Create the Top Level DiamondGeometry located in each 3D Qudrant (I, II, III, IV),
    //         mapped to a TOAST Image Coordinate [tx, ty] as shown in the Mapping above.
    //
    // Quadrant I: [+X,+Z] ===> TOAST: [0,1] 
    var dgI = new ASTROVIEW.DiamondGeometry([YY, XX, _Y, ZZ], 0, 1, "I", 0x0000ff, zlevel, 1, ASTROVIEW.RADIUS);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.DiamondGeometry([YY, _Z, _Y, XX], 1, 1, "II", 0x00ff00, zlevel, 0, ASTROVIEW.RADIUS);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.DiamondGeometry([YY, _X, _Y, _Z], 1, 0, "III", 0xff0000, zlevel, 2, ASTROVIEW.RADIUS);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.DiamondGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", 0xffff00, zlevel, 3, ASTROVIEW.RADIUS);
    
    //
    // STEP 3: Expand Each Top Level DiamondGeometry Object to Level 4 Array of DiamondGeometry[] objects
    //
    var depth = ASTROVIEW.ZOOM_LEVEL_MIN-1;	    // expand 3 more levels...
    var zlevel = ASTROVIEW.ZOOM_LEVEL_MIN;	    // ...to zlevel '4'

    var daI = dgI.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);			// Quadrant I
    var daII = dgII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);		    // Quadrant II
    var daIII = dgIII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);		// Quadrant III
    var daIV = dgIV.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);		    // Quadrant IV		
    
    //
    // STEP 4: Create DiamondMesh objects from the DiamondGeometry[] array and
    //         add them as children to the DiamondSphere
    //
    this.createDiamondMeshMaterial(daI, null, zlevel); 
    this.createDiamondMeshMaterial(daII, null, zlevel);
    this.createDiamondMeshMaterial(daIII, null, zlevel);
    this.createDiamondMeshMaterial(daIV, null, zlevel);
    
    /*
     //
     // Create DiamondMesh(s) with Test Image Material
     //
    var matI = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_1.PNG' ) } );
    var matII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_1.PNG' ) } );
    var matIII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_0.PNG' ) } );
    var matIV = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_0.PNG' ) } );
    
    this.createDiamondMeshMaterial(daI, matI, zlevel); 
    this.createDiamondMeshMaterial(daII, matII, zlevel);
    this.createDiamondMeshMaterial(daIII, matIII, zlevel);
    this.createDiamondMeshMaterial(daIV, matIV, zlevel);
    */
     
    /*
     //
     // Create DiamondMesh(s) with Color Material
     //
    this.createDiamondMeshColor(daI, dgI.color, true, zlevel); 
    this.createDiamondMeshColor(daII, dgII.color, true, zlevel); 
    this.createDiamondMeshColor(daIII, dgIII.color, true, zlevel); 
    cthis.reateDiamondsColor(daIV, dgIV.color, true, zlevel); 
    */
    
    /*
    //
    // Create DiamondMesh(s) with URL Material
    //
    url = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
    this.createDiamondMeshUrl(daI, url, zlevel); 
    this.createDiamondMeshUrl(daII, url, zlevel); 
    this.createDiamondMeshUrl(daIII, url, zlevel); 
    cthis.reateDiamondsUrl(daIV, url, zlevel); 
    */
}

/////////////////////////////
// createDiamondMeshMaterial()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshMaterial = function(diamonds, material, zlevel)
{
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
            this.createDiamond(dg, material);
        }
    }
}

/////////////////////////////
// createDiamondMeshColor()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshColor = function(diamonds, color, wireframe, zlevel)
{
    var material = new THREE.MeshBasicMaterial( { color: color, wireframe: wireframe } );
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
             this.addDiamond(dg, material);
        }
    }
}

/////////////////////////////
// createDiamondMeshUrl()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondMeshUrl = function(diamonds, baseurl, zlevel)
{
    var url = new String(baseurl);

    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        if (dg.zlevel === zlevel)
        {
            var imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);
            var material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(imageurl) } );
            this.addDiamond(dg, material);
        }
    }
}


/////////////////////////////
// createDiamond()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamond = function(dg, material)
{
    var dm = new ASTROVIEW.DiamondMesh( dg, material );
    this.add(dm);
}
/////////////////////////////
// GraphicsLayer
/////////////////////////////
ASTROVIEW.GraphicsLayer = function ( av, layerData)
{
    THREE.Object3D.call( this );
    if (av && layerData)
    {
        //
        // NOTE: We must ensure the Layer ID matches everwhere: 
        // layerData: The object contains the original data properties of the GraphicsLayer.
        // GraphicsLayer: The actual 3D Object added to the Scene.
        // Object3D.name: main key for extracting Objects from the Scene using scene.getObjectByName()
        //
        this.layerData = layerData;
        this.lid = ASTROVIEW.lid++;
        this.layerData.lid = this.lid;
        this.name = this.lid;
		
		// Load AstroView Objects
		this.camera = av.camera;
		this.renderer = av.renderer;
			
		// Initialize Temporary Points used for determining line distances below
		this.A = {};
		this.A.world = new THREE.Vector3();
		this.A.screen = new THREE.Vector3();
		
		this.B = {};
		this.B.world = new THREE.Vector3();
		this.B.screen = new THREE.Vector3();
    }
};

ASTROVIEW.GraphicsLayer.prototype = new THREE.Object3D();
ASTROVIEW.GraphicsLayer.prototype.constructor = ASTROVIEW.GraphicsLayer;

//////////////////////////////////
// Image Textures (for particles)
//////////////////////////////////
ASTROVIEW.GraphicsLayer.plus = THREE.ImageUtils.loadTexture("../AstroView/textures/plus11.png");
ASTROVIEW.GraphicsLayer.circle = THREE.ImageUtils.loadTexture("../AstroView/textures/circle11.png");
ASTROVIEW.GraphicsLayer.square = THREE.ImageUtils.loadTexture("../AstroView/textures/square11.png");
ASTROVIEW.GraphicsLayer.stop = THREE.ImageUtils.loadTexture("../AstroView/textures/stop11.png");
ASTROVIEW.GraphicsLayer.diamond = THREE.ImageUtils.loadTexture("../AstroView/textures/diamond11.png");

// Material Defaults
ASTROVIEW.GraphicsLayer.DEFAULT_COLOR = 0xff00ff;
ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL = 'stop';
ASTROVIEW.GraphicsLayer.DEFAULT_SIZE = 38;
ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH = 1;

/////////////////////////////
// clean()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.clean = function()
{
	if (this.renderer instanceof THREE.WebGLRenderer)
	{
		for (var i=0; i<this.children.length; i++)
		{
			var object = this.children[i];
			this.renderer.deallocateObject(object);
		}
	}
	this.camera = undefined;
	this.renderer = undefined;
}


/////////////////////////////
// alert()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.alert = function()
{
	if (!this.alertOnce)
	{
		alert(msg);
		this.alertOnce = true;
	}
}

/////////////////////////////
//
//    Graphics Creation 
//
/////////////////////////////
// createPolyline()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createPolyline = function(polyline, row)
{
	// Create line material used for all lines
	if (!this.material)
	{
		this.material = this.createMaterialLine(this.layerData.attribs);
	}
        	
    var coords = polyline.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        //
        // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
        //
        var geometry = new THREE.Geometry();
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
                                        
            // Convert ra, dec to vertex and add to geometry.vertices[] array
            var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
            geometry.vertices.push(v);
        } // end for each coord position
        
        // Close the Polygon
        var v = new THREE.Vertex( geometry.vertices[0].position.clone() );
        geometry.vertices.push(v);
            
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.row = row;
        this.add(line);     					
    }
    else
    {
        alert("Footprint Coords Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + footprint, "Unable to Create Footprint");
    }
};

/////////////////////////////
// createCircle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createCircle = function(circle, row)
{
	// Create line material used for all lines
	if (!this.material)
	{
		this.material = this.createMaterialLine(this.layerData.attribs);
	}
        
    //
    // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
    //
    var coords = circle.split(/\s+/);
    if (coords.length == 3)
    {
        // Extract RA, DEC and RADIUS from coord Array
        var ra = Number(coords[0]);
        var dec = Number(coords[1]);
        var radius = Number(coords[2]);
        var geometry = new THREE.Geometry(); 
        
        // Create Circle storing vertices in geometry.vertices[] 
        this.createCircleGeometry(ra, dec, radius, geometry);
        
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.row = row;
        this.add(line);	
    }
    else
    {
        alert("Footprint Circle Error.\n\n Expecting 3 values containing [RA] [DEC] [RADIUS], actual length: " + coords.length + "\n\n" + circle, "Unable to Create Footprint");
    }
}

/////////////////////////////
// createCircleGeometry()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createCircleGeometry = function(ra, dec, radius, geometry)
{               
    // Build circle as polygon with 60 vertices (is this good enough???)
    for (var i=0; i<=60; i++)
    {
        var angle = 6.0*i*TO_RADIANS;
        var y = dec + radius*Math.sin(angle);
        var x = ra + (radius*Math.cos(angle)/Math.cos(y*TO_RADIANS));
        
        // Add the new point to the geometry array
        var v = this.raDecToVertex(x, y, ASTROVIEW.RADIUS_LAYER);
        geometry.vertices.push(v);
    }
}

/////////////////////////////
// createParticle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticle = function(ra, dec, row, symbol)
{
	if (this.renderer instanceof THREE.WebGLRenderer)
	{
		this.createParticleWebGL(ra, dec, row, symbol);
	}
	else if (this.renderer instanceof THREE.CanvasRenderer)
	{
		this.createParticleCanvas(ra, dec, row, symbol);
	}  
}

/////////////////////////////
// createParticleSystem()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleSystem = function()
{
	// Create the particle system for WebGL only
	if (this.particles && this.material)
	{
		var ps = new THREE.ParticleSystem(this.particles, this.material);
		this.add(ps);
	} 
}

/////////////////////////////
// hasParticleSystem()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.hasParticleSystem = function()
{
	return (this.particles && this.particles.vertices && this.particles.vertices.length > 0);
}
		
/////////////////////////////
// createParticleWebGL()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleWebGL = function(ra, dec, row, symbol)
{
	// Create Material, if not already done
	if (!this.material)
	{
		this.material = this.createMaterialParticleWebGL(this.layerData.attribs);
	}
           
    // Create the particle geometry to hold each particle vertex
	if (!this.particles)
	{
		this.particles = new THREE.Geometry();
	}
    
    // Create colors[] array for Specified Color per Particle
    //var colors = [];
    
    // Convert each RA/DEC coord to a Vertex and add to the Geometry       
	if (ra != null && dec != null)
	{
		var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
		v.row = row;
		this.particles.vertices.push(v);
		//colors.push(new THREE.Color(0xffff00));  // NOTE: Specified Color per Particle
	}
    
    // Store the colors array
    //geometry.colors = colors;     // NOTE: Specified Color per Particle
};

/////////////////////////////
// createParticleCanvas()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleCanvas = function(ra, dec, row, symbol)
{
    // Create Canvas Particle Material
	if (!this.material)
	{
		this.material = this.createMaterialParticleCanvas(this.layerData.attribs);
	}
			
	// Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene  
	if (ra != null && dec != null)
	{
		var v = this.raDecToVertex(ra, dec, ASTROVIEW.RADIUS_LAYER);
		var p = new THREE.Particle( this.material );
		p.row = row;
		p.position.x = v.position.x;
		p.position.y = v.position.y;
		p.position.z = v.position.z;
		this.add(p);
	}
};

/////////////////////////////////
// 
//         Materials
//
/////////////////////////////////
// createMaterialLine()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialLine = function(attribs)
{
	var material;
	
	var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
	var linewidth = (attribs && attribs.linewidth ? attribs.linewidth : ASTROVIEW.GraphicsLayer.DEFAULT_LINE_WIDTH);
	material = new THREE.LineBasicMaterial( { color: color, linewidth: linewidth } );
	
	return material;
}
	
/////////////////////////////////
// createMaterialParticleWebGL()
/////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialParticleWebGL = function(attribs)
{
	var material;
	
	var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
	var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);
	var size = (attribs && attribs.size ? attribs.size : ASTROVIEW.GraphicsLayer.DEFAULT_SIZE);
	
	switch (symbol)
	{
		case 'circle':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.circle,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'square':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.square,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'plus':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.plus,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'stop':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.stop,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
			
		case 'diamond':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.diamond,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		case 'diamond':
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.diamond,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
		
		default:
			material = new THREE.ParticleBasicMaterial({
				size: size,
				map: ASTROVIEW.GraphicsLayer.square,
				transparent:true,
				color: color
				//vertexColors: true // NOTE: Specified Color per Particle
			});
			break;
	}
	
	return material;
}

//////////////////////////////////
// createMaterialParticleCanvas()
//////////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createMaterialParticleCanvas = function(attribs)
{
	var material;
	
	var color = (attribs && attribs.color ? attribs.color : ASTROVIEW.GraphicsLayer.DEFAULT_COLOR);
	var symbol = (attribs && attribs.symbol ? attribs.symbol : ASTROVIEW.GraphicsLayer.DEFAULT_SYMBOL);
	
	switch (symbol)
	{
		case 'circle':
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.circle)
			});
			break;
		
		case 'square':
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.square)
			});
			break;
		
		case 'plus':
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.plus)
			});
			break;
		
		case 'diamond':
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.diamond)
			});
			break;
		
		case 'stop':
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.stop)
			});
			break;
		
		default:
			material = new THREE.ParticleCanvasMaterial(
			{
				color: color,
				program: bind(this, this.square)
			});
			break;
	}
	
	return material;
}

/////////////////////////////
//
//    Canvas Rendering 
//
/////////////////////////////
// circle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.circle = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    // context.arc(centerX, centerY, radiusLayer, startingAngle, endingAngle, counterclockwise);
    context.arc( 0, 0, 4, 0, PI2, true );
    context.closePath();
    context.stroke();
};

/////////////////////////////
// square()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.square = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
	//context.strokeRect(x, y, width, height)
    context.strokeRect( -4, -4, 9, 9);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4,  0);
	context.lineTo(  4,  0);
	context.moveTo(  0,  4);
	context.lineTo(  0, -4);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// diamond()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.diamond = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -5, -1);
	context.lineTo( -5,  1);
	context.lineTo( -1,  5);
	context.lineTo(  1,  5);
	context.lineTo(  5,  1);
	context.lineTo(  5, -1);
	context.lineTo(  1, -5);
	context.lineTo( -1, -5);
	context.lineTo( -5, -1);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// stop()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.stop = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -5, -2);
	context.lineTo( -5,  2);
	context.lineTo( -2,  5);
	context.lineTo(  2,  5);
	context.lineTo(  5,  2);
	context.lineTo(  5, -2);
	context.lineTo(  2, -5);
	context.lineTo( -2, -5);
	context.lineTo( -5, -2);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = this.camera.fov/ASTROVIEW.FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4,  0);
	context.lineTo(  4,  0);
	context.moveTo(  0,  4);
	context.lineTo(  0, -4);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// raDecToVertex()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.raDecToVertex = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vertex(new THREE.Vector3(x, y, z));
    return v;
};

/////////////////////////////
//
//     Object Selection
//
/////////////////////////////
// getSelected()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.getSelected = function(mouse, controller)
{
	// Validate input
	if (!controller || !mouse) return null;
	
	var D = 99999.99;
	var distance;
	for (var i=0; i<this.children.length; i++)
	{
		var object = this.children[i];
		if (object instanceof THREE.Particle)
		{
			distance = this.distanceToParticle2D(mouse, object, controller);
		}
		else if (object instanceof THREE.ParticleSystem)
		{
			distance = this.distanceToParticleSystem2D(mouse, object, controller);
		}
		else if (object instanceof THREE.Line)
		{
			distance = this.distanceToLine2D(mouse, object, controller);
		}
		D = (distance < D ? distance : D);
	}
	console.debug("D [" + this.layerData.name + "] : " + D);
	return null;		
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticle2D = function (mouse, particle, controller)
{
	// Convert Particle World ===> Screen  (clear out Z)
	controller.worldToScreen(particle.position, this.A.screen);
	this.A.screen.z = 0;
	
	// Copy selection mouse in Screen Coordinates (clear out Z)
	this.B.screen.copy(mouse.screen);
	this.B.screen.z = 0;
	
	// Calculate the distance in Screen Coordinates
	var D = this.A.screen.distanceTo(this.B.screen);
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticleSystem2D = function (mouse, ps, controller)
{
	var D = 99999.99;
	
	if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return null;
	
	var length = ps.geometry.vertices.length;
	for (var i=0; i<length; i++)
	{
		var v = ps.geometry.vertices[i];
		controller.worldToScreen(v.position, this.A.screen);
		this.A.screen.z = 0;
		
		// Copy selection mouse in Screen Coordinates (clear out Z)
		this.B.screen.copy(mouse.screen);
		this.B.screen.z = 0;
		
		// Calculate the distance in Screen Coordinates
		var distance = this.A.screen.distanceTo(this.B.screen);
		D = (distance < D ? distance : D);
	}

	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToLine2D = function (mouse, line, controller)
{
	var D = 99999.99;
	
	// validate input
	if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) return null;
	
	var vertices = line.geometry.vertices;
	for (var i=0; i<vertices.length-1; i++)
	{
		this.A.world = vertices[i].position;
		this.B.world = vertices[i+1].position;
		
		controller.worldToScreen(this.A.world, this.A.screen);
		controller.worldToScreen(this.B.world, this.B.screen);
		
		var inside = this.insideBoundingBox(mouse.screen, this.A.screen, this.B.screen);
		if (inside)
		{
			var distance = this.distanceToLinePoints2D(mouse.screen, this.A.screen, this.B.screen, controller);
			D = (distance < D ? distance : D);
		}
	}
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.insideBoundingBox = function (P, A, B, margin)
{
	if (!margin) margin = 4;
	
	this.minx = (A.x < B.x ? A.x : B.x) - margin;
	this.maxx = (A.x > B.x ? A.x : B.x) + margin;
	this.miny = (A.y < B.y ? A.y : B.y) - margin;
	this.maxy = (A.y > B.y ? A.y : B.y) + margin;
	
	return (this.minx <= P.x && P.x <= this.maxx &&
			this.miny <= P.y && P.y <= this.maxy);
}

ASTROVIEW.GraphicsLayer.prototype.distanceToLinePoints2D = function (P, A, B, controller)
{
	// From Wikipedia: http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
	var normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
	var distance = Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
	return distance;
}

/////////////////////////
// 3D Distance Routines
/////////////////////////
ASTROVIEW.GraphicsLayer.prototype.distanceToParticle3D = function (mouse, particle)
{
	var D = mouse.layer.distanceTo(particle.position);
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToParticleSystem3D = function (mouse, ps)
{
	var D = 99999.99;
	
	if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return D;
	
	var length = ps.geometry.vertices.length;
	for (var i=0; i<length; i++)
	{
		var v = ps.geometry.vertices[i];
		var distance = mouse.layer.distanceTo(v.position);
		D = (distance < D ? distance : D);
	}
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToPolyline3D = function (mouse, line)
{
	var D = 99999.99;
	
	// validate input
	if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) return null;
	
	var length = line.geometry.vertices.length;
	for (var i=0; i<length-1; i++)
	{
		var v1 = line.geometry.vertices[i];
		var v2 = line.geometry.vertices[i+1];
		var distance = this.distancePointToLineSegment3D(mouse.layer, v1.position, v2.position);
		D = (distance < D ? distance : D);
	}
	return D;
}

ASTROVIEW.GraphicsLayer.prototype.distanceToLineSegment3D = function (x0, x1, x2)
{
	//
	// From: http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
	//
	//      |(x0-x1) x (x0-x2)|
	// D = -------------------
	//          |x2-x1|
	//
	// (All input arguments are assumed to be THREE.Vector3)
	
	this.x0_x1.sub(x0, x1);
	this.x0_x2.sub(x0, x2);
	this.x2_x1.sub(x2, x1);
	
	var num = (this.numerator.cross(this.x0_x1, this.x0_x2)).length();	
	var den = this.x2_x1.length();
	var D = num/den;
	
	return D;
}

ASTROVIEW.lid = 0;/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );
    
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {
		this.createParticles(layerData.rows);
	}
	
	// NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.CatalogLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.CatalogLayer.prototype.constructor = ASTROVIEW.CatalogLayer;

/////////////////////////////
// createParticles()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.createParticles = function(rows)
{
	//
	// NOTE: The Particle API is different for WebGLRenderer vs. CanvasRenderer
	//       so we separate the code accordingly.
	//
	if (rows && rows.length > 0)
	{
		for (var i=0; i<rows.length; i++)
		{
			// Check all mixed case variations for RA and DEC values
			var row = rows[i];
			var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
			var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
			
			// Add the particle at specified location
			this.createParticle(ra, dec, row);
		}
	}
}/////////////////////////////
// FootprintLayer()
/////////////////////////////
ASTROVIEW.FootprintLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );
    
    //
    // For each footprint in the layerData object, create a THREE.Line Object
    // and add it as a child to this layerData
    //
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {             
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            this.createFootprint(row);
        }
    }
           
    // NOTE: 
    // If any Particles were created (for WebGL Only),
    // we need to add them to the scene as a Particle System, 
    if (this.hasParticleSystem())
    {
        this.createParticleSystem();
    }
};

ASTROVIEW.FootprintLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.FootprintLayer.prototype.constructor = ASTROVIEW.FootprintLayer;

/////////////////////////////
// createFootprint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(row)
{
    // Validate input
    if (row == undefined || row.footprint == undefined) return;

    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var type = "";
    var fplist = null;
    
    // Trim, Upper Case, Remove all parens: () 
    var upper = trim(row.footprint.toUpperCase());
    var fp = upper.replace(/\(/g,' ').replace(/\)/g, ' ');
    
    // Split the footprint string
    if (fp.indexOf(ASTROVIEW.POLYGON_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_J2000_RE);
        type = ASTROVIEW.POLYGON_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POLYGON_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POLYGON_ICRS_RE);
        type = ASTROVIEW.POLYGON_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_ICRS_RE);
        type = ASTROVIEW.CIRCLE_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.CIRCLE_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.CIRCLE_J2000_RE);
        type = ASTROVIEW.CIRCLE_J2000;
    }
    else if (fp.indexOf(ASTROVIEW.POINT_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POINT_J2000_RE);
        type = ASTROVIEW.POINT_J2000;
    }
    
    // If we found no valid fplist, bail
    if (fplist == null || fplist.length < 2)
    {
        this.alert("Illegal Footprint:" + footprint + "\n\nExpected Syntax: \n\n POLYGON [J2000|ICRS] [ra dec]\n CIRCLE [J2000|ICRS] [ra dec]\n POINT [ra dec]", "Illegal Footprint");
        return;
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolygons(fplist, row);
            break;
        }
        case ASTROVIEW.CIRCLE_ICRS:
        case ASTROVIEW.CIRCLE_J2000:
        {
            this.createCircles(fplist, row);
            break;
        }
        case ASTROVIEW.POINT_J2000:
        {
            this.createPoints(fplist, row);
            break;
        }
        default:
        {
            this.alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolygons()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolygons = function(fplist, row)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var polyline = trim(fplist[i]);
        if (polyline.length > 0)
        {
            this.createPolyline(polyline, row);     // GraphicsLayer.createPolyline()
        }
    }
}

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(fplist, row)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var circle = trim(fplist[i]);
        if (circle.length > 0)
        {
            this.createCircle(circle, row);         // GraphicsLayer.createCircle()
        }
    }
}

/////////////////////////////
// createPoints()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoints = function(fplist, row)
{
    for (var i=0; i<fplist.length; i++)
    {
        var point = trim(fplist[i]);
        if (point.length > 0)
        {
            this.createPoint(point, row);
        }
    }
}

/////////////////////////////
// createPoint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoint = function(point, row)
{
    var coords = point.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
            this.createParticle(ra, dec, row, 'plus');     // GraphicsLayer.createParticle()
        }
    }
    else
    {
        this.alert("Footprint POINT Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + point, "Unable to Create Footprint");
    }
}

/////////////////////////////
// Footprint Constants
/////////////////////////////
ASTROVIEW.POLYGON_J2000 = 'POLYGON J2000';
ASTROVIEW.POLYGON_ICRS  = 'POLYGON ICRS';

ASTROVIEW.POLYGON_J2000_RE = new RegExp(ASTROVIEW.POLYGON_J2000 + "\\s+");
ASTROVIEW.POLYGON_ICRS_RE  = new RegExp(ASTROVIEW.POLYGON_ICRS + "\\s+");

ASTROVIEW.CIRCLE_J2000  = "CIRCLE J2000";
ASTROVIEW.CIRCLE_ICRS   = "CIRCLE ICRS";

ASTROVIEW.CIRCLE_J2000_RE = new RegExp(ASTROVIEW.CIRCLE_J2000 + "\\s+");
ASTROVIEW.CIRCLE_ICRS_RE  = new RegExp(ASTROVIEW.CIRCLE_ICRS + "\\s+");

ASTROVIEW.POINT_J2000  = "POINT";
ASTROVIEW.POINT_J2000_RE = new RegExp(ASTROVIEW.POINT_J2000 + "\\s+");/////////////////////////////
// ImageLayer()
/////////////////////////////
ASTROVIEW.ImageLayer = function ( layerData, radius )
{ 
    THREE.GraphicsLayer.call( this );
    
    if (layerData && layerData.rows && layerData.rows.length > 0)
    {    
        //
        // For each image in the layerData object, create a Plane Object with Image textured to it,
        // and add it as a child to this Layer
        //
		for (var i=0; i<layerData.rows.length; i++)
		{
			var row = layerData.rows[i];
			if (row.image)
			{
				this.addImage(row.image);
			}
		}
    }
};

ASTROVIEW.ImageLayer.prototype = new ASTROVIEW.GraphicsLayer();
ASTROVIEW.ImageLayer.prototype.constructor = ASTROVIEW.ImageLayer;

ASTROVIEW.ImageLayer.prototype.addFootprint = function(footprint)
{
    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var polygons = [];
    var footprintUpper = trim(footprint.toUpperCase());
    if (footprintUpper.indexOf("POLYGON J2000 ") >= 0)
    {
        polygons = footprintUpper.split("POLYGON J2000 ");
    }
    else if (footprintUpper.indexOf("POLYGON ICRS ") >= 0)
    {
        polygons = footprintUpper.split("POLYGON ICRS ");
    }
    else if (footprintUpper.indexOf("CIRCLE ICRS ") >= 0)
    {
        polygons = footprintUpper.split("CIRCLE ICRS ");
    }
    
    // If we found no valid polygons, bail
    if (polygons == null || polygons.length == 0)
    {
        alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [RA] [DEC]' or 'Polygon ICRS [RA] [DEC]' \n\n" + sSTCS, "Unable to Create Footprint");
        return;
    }
    
    // Create material used for all lines
    if (!this.material)
    {
        this.material = new THREE.LineBasicMaterial( { color: 0xff0000, linewidth: 1 } );
    }
    
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            var coords = polygon.split(" ");
            if (coords.length > 0 && coords.length%2 == 0)
            {
                //
                // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
                //
                var geometry = new THREE.Geometry();
                for (var j=0; j<coords.length; j+=2)
                {
                    // Extract RA and DEC from coord Array
                    var ra = coords[j];
                    var dec = coords[j+1];
                                                
                    // Convert ra, dec to vertex and add to geometry.vertices[] array
                    var v = this.raDecToVertex(ra, dec, this.radius);
                    geometry.vertices.push(v);
                } // end for each coord position
                
                // Close the Polygon
                var v = new THREE.Vertex( geometry.vertices[0].position.clone() );
                geometry.vertices.push(v);
                    
                // Create new Line from the geometry and material
                var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
                this.add(line);	        					
            }
            else
            {
                alert("Footprint Coords Error.\n\n Expecting Even Number of [RA] [DEC] values, actual length: " + coords.length + "\n\n" + footprint, "Unable to Create Footprint");
            }
        }
    } // for each polygon
}
		
ASTROVIEW.ImageLayer.prototype.raDecToVertex = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vertex(new THREE.Vector3(x, y, z));
    return v;
}// stats.js r8 - http://github.com/mrdoob/stats.js
var Stats=function(){var h,a,n=0,o=0,i=Date.now(),u=i,p=i,l=0,q=1E3,r=0,e,j,f,b=[[16,16,48],[0,255,255]],m=0,s=1E3,t=0,d,k,g,c=[[16,48,16],[0,255,0]];h=document.createElement("div");h.style.cursor="pointer";h.style.width="80px";h.style.opacity="0.9";h.style.zIndex="10001";h.addEventListener("mousedown",function(a){a.preventDefault();n=(n+1)%2;n==0?(e.style.display="block",d.style.display="none"):(e.style.display="none",d.style.display="block")},!1);e=document.createElement("div");e.style.textAlign=
"left";e.style.lineHeight="1.2em";e.style.backgroundColor="rgb("+Math.floor(b[0][0]/2)+","+Math.floor(b[0][1]/2)+","+Math.floor(b[0][2]/2)+")";e.style.padding="0 0 3px 3px";h.appendChild(e);j=document.createElement("div");j.style.fontFamily="Helvetica, Arial, sans-serif";j.style.fontSize="9px";j.style.color="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";j.style.fontWeight="bold";j.innerHTML="FPS";e.appendChild(j);f=document.createElement("div");f.style.position="relative";f.style.width="74px";f.style.height=
"30px";f.style.backgroundColor="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";for(e.appendChild(f);f.children.length<74;)a=document.createElement("span"),a.style.width="1px",a.style.height="30px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+b[0][0]+","+b[0][1]+","+b[0][2]+")",f.appendChild(a);d=document.createElement("div");d.style.textAlign="left";d.style.lineHeight="1.2em";d.style.backgroundColor="rgb("+Math.floor(c[0][0]/2)+","+Math.floor(c[0][1]/2)+","+Math.floor(c[0][2]/2)+")";d.style.padding=
"0 0 3px 3px";d.style.display="none";h.appendChild(d);k=document.createElement("div");k.style.fontFamily="Helvetica, Arial, sans-serif";k.style.fontSize="9px";k.style.color="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";k.style.fontWeight="bold";k.innerHTML="MS";d.appendChild(k);g=document.createElement("div");g.style.position="relative";g.style.width="74px";g.style.height="30px";g.style.backgroundColor="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";for(d.appendChild(g);g.children.length<74;)a=document.createElement("span"),
a.style.width="1px",a.style.height=Math.random()*30+"px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+c[0][0]+","+c[0][1]+","+c[0][2]+")",g.appendChild(a);return{domElement:h,update:function(){i=Date.now();m=i-u;s=Math.min(s,m);t=Math.max(t,m);k.textContent=m+" MS ("+s+"-"+t+")";var a=Math.min(30,30-m/200*30);g.appendChild(g.firstChild).style.height=a+"px";u=i;o++;if(i>p+1E3)l=Math.round(o*1E3/(i-p)),q=Math.min(q,l),r=Math.max(r,l),j.textContent=l+" FPS ("+q+"-"+r+")",a=Math.min(30,30-l/
100*30),f.appendChild(f.firstChild).style.height=a+"px",p=i,o=0}}};

