////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// bind
////////////////////////
function bind( scope, fn )
{
    return function ()
    {
        fn.apply( scope, arguments );
    };
};

////////////////////////
// isString
////////////////////////
function isString(s)
{
    return typeof(s) === 'string' || s instanceof String;
};

////////////////////////
// trim
////////////////////////
function trim(s)
{
    return s.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};

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

// Diamond Radius and Viewport Far Plane
const RADIUS = 1000;

// Survey URLs
const galexurl = "http://mastproxyvm1.stsci.edu/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
const dssurl = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
//var dssurl = "http://mastproxyvm1.stsci.edu/wwtweb/dss.aspx?q=[LEVEL],[TX],[TY]";
    
////////////////////////
// AstroBasicView()
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
            
    /////////////////
    // createScene()
    /////////////////
    this.createScene = function() 
    {
        // Camera
        this.camera = new THREE.PerspectiveCamera( FOV_LEVEL_MAX, window.innerWidth / window.innerHeight, 1, RADIUS+1 );
        this.camera.position.set( 0, 0, 0 );
        this.camera.eulerOrder = 'YXZ';
        
        // Cntroller
        this.controller = new ASTROVIEW.CameraController(this.camera, this.container, FOV_LEVEL_MIN, FOV_LEVEL_MAX, RADIUS);

        // Scene
        this.scene = new THREE.Scene();
        this.scene.add( this.camera );
        
        // Renderer
        this.renderer = this.createRenderer(this.rendertype);
        this.renderer.setSize( window.innerWidth, window.innerHeight );
        this.container.appendChild( this.renderer.domElement );
        
        // DiamondSphere
        this.diamondSphere = new ASTROVIEW.DiamondSphere();
        this.scene.add(this.diamondSphere);
        
        // Stats
        this.stats = new Stats();
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.top = '0px';
        this.container.appendChild(this.stats.domElement);
    }
    
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
        // Update this.camera Position
        this.controller.render();
        
        // Get active ZoomLevel
        var zlevel = this.controller.getZoomLevel();
        
        // Get latest frustum
        var frustum = this.controller.getFrustum();
        
        // Render each Diamond 
        for (var i=0; i<this.diamondSphere.children.length; i++)
        {
            var dm = this.diamondSphere.children[i];	// ASTROVIEW.DiamondMesh
            dm.render(this.baseurl, zlevel, frustum);
        }
        
        // Render the this.scene
        this.renderer.render(this.scene, this.camera);
        
        // Update the this.stats
        this.stats.update();
        
        // Request another animation frame
        requestAnimationFrame(bind(this, this.renderScene));
    }
    
    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
        document.addEventListener( "keypress" , bind(this, this.onKeyPress), false);
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
        for (var l=0; l<ASTROVIEW.lid; l++)
        {
            var layer = this.readLayer(l);
            if (layer)
            {
                alert("layer [" + layer.lid + "]:\n" + this.toJson(layer));
            }
        }
    }
    
    //////////////////////////
    // 'D' : onDeletLayer() 
    //////////////////////////
    this.onDeleteLayer = function()
    {
        for (var l=0; l<ASTROVIEW.lid; l++)
        {
            var layer = this.readLayer(l);
            if (layer)
            {
                return this.deleteLayer(l);
            }
        }
        return "";
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
        
        var attribs = {"color":"0xff0000"};
        var name = "catalogTest";
        var layer = {"type":"catalog", "name":name, "attribs":attribs, "rows":rows};
        this.createLayer(layer);
    }
    
    var redBox = "   Polygon J2000 5.0 -5.0 5.0 5.0 355.0 5.0 355.0 -5.0";
    var acsM101 = "  Polygon J2000 210.75890230 54.38019650 210.79889830 54.32921760 210.84012320 54.34291740 210.80016510 54.39391000      Polygon J2000  210.79858090 54.32875470 210.75693070 54.38001520 210.71409030 54.36600310 210.75577980 54.31475730";
    //var galexM101 = "POLYGON ICRS 211.43464545 54.358924 211.43122163 54.42425429 211.4209877 54.48886881 211.40405577 54.55205962 211.38061136 54.6131344 211.35091133 54.671424 211.31528107 54.72628978 211.27411097 54.77713063 211.22785208 54.82338952 211.17701123 54.86455962 211.12214545 54.90018988 211.06385585 54.92988991 211.00278107 54.95333432 210.93959026 54.97026625 210.87497574 54.98050018 210.80964545 54.983924 210.74431516 54.98050018 210.67970064 54.97026625 210.61650983 54.95333432 210.55543505 54.92988991 210.49714545 54.90018988 210.44227967 54.86455962 210.39143882 54.82338952 210.34517993 54.77713063 210.30400983 54.72628978 210.26837957 54.671424 210.23867954 54.6131344 210.21523513 54.55205962 210.1983032 54.48886881 210.18806927 54.42425429 210.18464545 54.358924 210.18806927 54.29359371 210.1983032 54.22897919 210.21523513 54.16578838 210.23867954 54.1047136 210.26837957 54.046424 210.30400983 53.99155822 210.34517993 53.94071737 210.39143882 53.89445848 210.44227967 53.85328838 210.49714545 53.81765812 210.55543505 53.78795809 210.61650983 53.76451368 210.67970064 53.74758175 210.74431516 53.73734782 210.80964545 53.733924 210.87497574 53.73734782 210.93959026 53.74758175 211.00278107 53.76451368 211.06385585 53.78795809 211.12214545 53.81765812 211.17701123 53.85328838 211.22785208 53.89445848 211.27411097 53.94071737 211.31528107 53.99155822 211.35091133 54.046424 211.38061136 54.1047136 211.40405577 54.16578838 211.4209877 54.22897919 211.43122163 54.29359371 211.43464545 54.358924";
    var galexM101 = "CIRCLE ICRS 210.80964545 54.35892400 0.625";
    var galexNGC5474 = "CIRCLE ICRS 211.25948613 53.67449567 0.625";
    var test00 = "CIRCLE ICRS 0 0 1.0";
    var test045 = "CIRCLE ICRS 0 45 1.0";
    var test080 = "CIRCLE ICRS 0 80 1.0";
    
    ////////////////////////////
    // 'F' : onFootprintLayer() 
    ////////////////////////////
    this.onFootprintLayer = function()
    {      
        rows = {"footprint":test00};
        attribs = {"color":"0xff0000"};
        name = "test00";
        layer = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
        
        rows = {"footprint":test045};
        attribs = {"color":"0xff0000"};
        name = "test045";
        layer = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
        
        rows = {"footprint":test080};
        attribs = {"color":"0xff0000"};
        name = "test080";
        layer = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
        
        var rows = {"footprint":acsM101};
        var attribs = {"color":"0xff0000"};
        var name = "acsM101";
        var layer = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
        
        rows = {"footprint":galexNGC5474};
        attribs = {"color":"0xff0000"};
        name = "galexNGC5474";
        layer = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
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
        var layer = {"type":"image", "attribs":attribs, "rows":[rows]};
        this.createLayer(layer);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////				
    //
    // AsroView API
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////
    // createLayer() 
    //////////////////////////
    this.createLayer = function(layer)
    {
        var avlayer = null;
        if (layer && layer.type && isString(layer.type))
        {
            var type = trim(layer.type.toLowerCase());
            switch (type)
            {
                case "footprint":
                {
                    avlayer = new ASTROVIEW.FootprintLayer(layer, RADIUS-100);
                    break;
                }
                case "catalog":
                {
                    avlayer = new ASTROVIEW.CatalogLayer(this.camera, this.renderer, layer, RADIUS-100);
                    break;
                }
                case "image":
                {
                    console.debug("createImageLayer");
                    //sphere = createImageLayer(layer);
                    break;
                }
            }
        }
        
        // Add new AstroView Layer to the Scene
        if (avlayer)
        {
            this.scene.add(avlayer);
        }
        
        // Return the Layer ID
        return (avlayer ? avlayer.lid : "");
    }
    
    //////////////////////////
    // readLayer() 
    //////////////////////////
    this.readLayer = function(lid)
    {
        var avlayer = this.scene.getChildByName(lid, true);
        return (avlayer ? avlayer.layer : "");
    }
    
    //////////////////////////
    // udpateLayer() 
    //////////////////////////
    this.udpateLayer = function(layer)
    {
        var avlayer = null;
        if (layer.lid)
        {
            avlayer = this.scene.getChildByName(layer.lid, true);
            if (avlayer)
            {
                this.scene.remove(avlayer);
                this.createLayer(layer);
            }
        }
        return (avlayer != null);
    }
    
    //////////////////////////
    // deleteLayer() 
    //////////////////////////
    this.deleteLayer = function(lid)
    {
        var avlayer = this.scene.getChildByName(lid, true);
        if (avlayer)
        {
            this.scene.remove(avlayer);
        }
        return (avlayer ? avlayer.lid : "");
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
/////////////////////////////
// AstroviewLayer()
/////////////////////////////
ASTROVIEW.AstroviewLayer = function ( layer )
{
    THREE.Object3D.call( this );
    if (layer)
    {
        //
        // NOTE: We must ensure the Layer ID matches everwhere: 
        // layer: The object contains that properties of a layer.
        // AstroviewLayer: The actual 3D Object added to the Scene.
        // Object3D.name: main key for extracting Objects from the Scene using scene.getObjectByName()
        //
        this.layer = layer;
        this.lid = ASTROVIEW.lid++;
        this.layer.lid = this.lid;
        this.name = this.lid;
        
        ASTROVIEW.lid++;
    }
};

ASTROVIEW.AstroviewLayer.prototype = new THREE.Object3D();
ASTROVIEW.AstroviewLayer.prototype.constructor = ASTROVIEW.AstroviewLayer;
		
/////////////////////////////
// raDecToVertex()
/////////////////////////////
ASTROVIEW.AstroviewLayer.prototype.raDecToVertex = function(ra, dec, radius)
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

ASTROVIEW.lid = 0;// Minimum Zoom/Fov Level
const ZOOM_LEVEL_MIN = 4;
const ZOOM_LEVEL_MAX = 12;
const FOV_LEVEL_MAX = 30;
const FOV_LEVEL_MIN = 0.00001;
    
ASTROVIEW.CameraController = function ( camera, container, fovmin, fovmax, radius)
{
	this.camera = camera;
	this.container = ( container !== undefined ) ? container : document;
    this.fovmin = (fovmin !== undefined) ? fovmin : FOV_LEVEL_MIN;
    this.fovmax = (fovmax !== undefined) ? fovmax : FOV_LEVEL_MAX;
    this.radius = radius;
              
    //
    // Fov ===> ZoomLevel Lookup Table
    //
    // Zoom Level:         1,  2,  3,  4,   5,   6,   7,   8,    9,     10,    11,     12,
    this.zoomLevelTable = [30, 30, 30, 30, 10.0, 5.0, 2.5, 1.25, 0.75, 0.355, 0.125, 0.0625, 0.00001];
    this.zoomLevel = ZOOM_LEVEL_MIN;
    
    // Camera Position
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = fovmax;
    this.lastFov;
    
    this.zoomSpeed = 0.1;  // How fast you zoom on each mouse wheel
    this.panSpeed = 0.2;   // How fast you pan on each mouse move.
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.down = new THREE.Vector2();
    this.last = new THREE.Vector2();
    this.delta = new THREE.Vector2();
    this.mouseDown = false;
    
    // Postion Events
    this.windowHalfX = window.innerWidth/2.0;
    this.windowHalfY = window.innerHeight/2.0;
    this.screenv = new THREE.Vector3();
    this.worldv = new THREE.Vector3();
    this.radec = {};

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
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
         
        this.updateRaDec(event);
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
        
        // Debug Info on mouse click position
        this.dumpRaDec();
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
                var rotationX = (event.touches[ 0 ].pageY - this.last.y) * TO_RADIANS * (this.camera.fov/(fovmax));
                var rotationY = (event.touches[ 0 ].pageX - this.last.x) * TO_RADIANS * (this.camera.fov/(fovmax));
                
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
        this.cameraRotationX = camera.rotation.x + deltax;
        this.cameraRotationY = camera.rotation.y + deltay;
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
        
        if (this.camera.rotation.y != this.cameraRotationY)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
           
        // Update the Zoom
        if (this.camera.fov !== this.cameraFov)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
            this.camera.updateProjectionMatrix();
        }
    }
    
    /////////////////////////
    // getZoomLevel()
    /////////////////////////  
    this.getZoomLevel = function()
    {
        // Determine new zoomLevel *only* if the Fov has changed
        if (this.lastFov !== this.camera.fov)
        {
            this.lastFov = this.camera.fov;
            var i=ZOOM_LEVEL_MIN-1;
            while (this.camera.fov <= this.zoomLevelTable[i])
            {
                i++;
            }
            if (this.zoomLevel !== i)
            {
                this.zoomLevel = i;
                console.debug("ZoomLevel: " + this.zoomLevel + " Fov:" + this.camera.fov);
            }
        }
        
        return this.zoomLevel;
    }
    
    /////////////////////////
    // moveTo()
    /////////////////////////  
    this.moveTo = function(ra, dec, zlevel)
    {
        console.debug("moveTo: [" + ra + ", " + dec + ", " +  zlevel + "]");
        
        // Check optional zlevel param
        if (!zlevel || zlevel > ZOOM_LEVEL_MAX) zlevel = 10;
        
        // point camera to ra
        this.camera.rotation.y = this.cameraRotationY = ra * TO_RADIANS;
        
        // point camera to dec
        this.camera.rotation.x = this.cameraRotationX = dec * TO_RADIANS;
        
        // now zoom in to max field of view for zlevel
        this.camera.fov = this.cameraFov = (this.zoomLevelTable[zlevel-1] + this.zoomLevelTable[zlevel])/2.0;
        this.camera.updateProjectionMatrix();
    }
    
    this.getFrustum = function()
    {
        this.frustum.setFromMatrix( new THREE.Matrix4().multiply( camera.projectionMatrix, camera.matrixWorldInverse ) );
        return this.frustum;
    }
    
    /////////////////////////
    // updateRaDec()
    /////////////////////////  
    this.updateRaDec = function(event)
    {
        this.screenv.x = event.clientX;
        this.screenv.y = event.clientY;
        this.screenv.z = 1.0; 

        this.screenToWorld(this.screenv, this.worldv, this.camera, this.projector);
        this.worldv.normalize();
        this.worldToRaDec(this.worldv, this.radec);
        this.worldv.multiplyScalar(this.radius);
    }
    
    this.dumpRaDec = function()
    {
       console.debug(" [screen x,y]:" + this.screenv.x + ", " + this.screenv.y +
                     " [world x,y,z]:" + this.worldv.x.toFixed(3) + "," + this.worldv.y.toFixed(3) + "," + this.worldv.z.toFixed(3) + 
                     " [ra,dec]: " + this.radec.ra.toFixed(3) + ", " + this.radec.dec.toFixed(3));
    }
    
    this.vToString = function(v)
    {
        return  "[x,y,z]: [" + v.x + "," + v.y + "," + v.z + "]";
    }

    this.screenToWorld = function(screenv, worldv, camera, projector)
    {
        worldv.x = screenv.x;
        worldv.y = screenv.y;
        worldv.z = screenv.z;
        
        worldv.x = worldv.x / this.windowHalfX - 1;
        worldv.y = - worldv.y / this.windowHalfY + 1;
        projector.unprojectVector( worldv, camera );
    }
    
    this.worldToScreen = function(worldv, screenv, camera, projector)
    {
        screenv.x = worldv.x;
        screenv.y = worldv.y;
        screenv.z = worldv.z;
        
        projector.projectVector( screenv, camera );
        screenv.x = ( screenv.x + 1 ) * this.windowHalfX;
        screenv.y = ( - screenv.y + 1) * this.windowHalfY;
    }
    
    this.worldToRaDec = function(worldv, radec)
    {
        // Store the ra, dec values
        radec.dec = Math.asin(worldv.y) * TO_DEGREES;			
        radec.ra = Math.atan2(-worldv.x, -worldv.z) * TO_DEGREES;
        if (radec.ra < 0) radec.ra += 360.0;
        
        // Convert ra, dec to the Sexagesimal String Equivalent
        radec.sra = this.deghms(radec.ra);
        radec.sdec = this.degdms(radec.dec);
    }
    
    this.degdms = function(degrees)
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
    
    this.deghms = function(degrees)
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
        var s = "000" + num;
        return s.substr(s.length-size);
    }
		
    this.normalizeV = function(v) 
    {
        var mag = v.x * v.x +
                  v.y * v.y +
                  v.z * v.z;
        
        if (mag != 0.0)
        {
            mag = 1.0/Math.sqrt(mag);
            v.x *= mag;
            v.y *= mag;
            v.z *= mag;
        }  
    }
    
    this.scaleV = function(v, radius)
    {
        v.x *= radius;
        v.y *= radius;
        v.z *= radius;
    };
			   
    ///////////////
    // Main
    ///////////////
    this.addEvents();
};
/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( camera, renderer, layer, radius )
{ 
    ASTROVIEW.AstroviewLayer.call( this, layer );
    
    if (layer && layer.rows && radius && renderer)
    {
        this.camera = camera;
        this.radius = radius;
    
        //
        // NOTE: The Particle API is different for WebGLRenderer vs. CanvasRenderer
        //       so we separate the code accordingly.
        //
        if (layer.rows && layer.rows.length > 0)
        {
            if (renderer instanceof THREE.WebGLRenderer)
            {
                this.addCatalogObjectsWebGL(layer.rows);
            }
            else if (renderer instanceof THREE.CanvasRenderer)
            {
                this.addCatalogObjectsCanvas(layer.rows);
            }  
        }
    }
};

ASTROVIEW.CatalogLayer.prototype = new ASTROVIEW.AstroviewLayer();
ASTROVIEW.CatalogLayer.prototype.constructor = ASTROVIEW.CatalogLayer;

ASTROVIEW.CatalogLayer.circle = THREE.ImageUtils.loadTexture("textures/circle.png");
ASTROVIEW.CatalogLayer.square = THREE.ImageUtils.loadTexture("textures/square.png");

/////////////////////////////
// addCatalogObjectsWebGL()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.addCatalogObjectsWebGL = function(rows)
{
    // Load the sprite  
    this.material = new THREE.ParticleBasicMaterial({
        size: 28,
        map: ASTROVIEW.CatalogLayer.circle,
        transparent:true,
        color: 0xff0000
        //vertexColors: true // NOTE: Specified Color per Particle
    });
           
    // Create the particle geometry to hold each particle vertex
    var geometry = new THREE.Geometry();
    
    // Create colors[] array for Specified Color per Particle
    var colors = [];
    
    // Convert each RA/DEC coord to a Vertex and add to the Geometry
    for (var i=0; i<rows.length; i++)
    {
        var row = rows[i];
        
        // Check all mixed case variations for RA and DEC values
        var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
        var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
        
        if (ra != null && dec != null)
        {
            var v = this.raDecToVertex(ra, dec, this.radius);
            geometry.vertices.push(v);
            //colors.push(new THREE.Color(0xffff00));  // NOTE: Specified Color per Particle
        }
    }
    
    // Store the colors array
    //geometry.colors = colors;     // NOTE: Specified Color per Particle

    // Create the particle system
    var ps = new THREE.ParticleSystem(geometry, this.material);
    
    // Add it to the scene
    this.add(ps);  
};

/////////////////////////////
// addCatalogObjectsCanvas()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.addCatalogObjectsCanvas = function(rows)
{
    // Create Canvas Particle Material
    this.material = new THREE.ParticleCanvasMaterial(
    {
        color: 0xffff00,
        program: bind(this, this.drawCircle)
    });
			
	// Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene
    for (var i=0; i<rows.length; i++)
    {
        var row = rows[i];
        
       // Check all mixed case variations for RA and DEC values
        var ra = row.ra != null ? row.ra : (row.RA != null ? row.RA : (row.Ra != null ? row.Ra : null));
        var dec = row.dec != null ? row.dec : (row.DEC != null ? row.DEC : (row.Dec != null ? row.Dec : null));
        
        if (ra != null && dec != null)
        {
            var v = this.raDecToVertex(ra, dec, this.radius);
            var p = new THREE.Particle( this.material );
            p.position.x = v.position.x;
            p.position.y = v.position.y;
            p.position.z = v.position.z;
            this.add(p);
        }
    }
};

/////////////////////////////
// drawCircle()
/////////////////////////////
ASTROVIEW.CatalogLayer.prototype.drawCircle = function (context)
{
    var scale = this.camera.fov/FOV_LEVEL_MAX;
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    // context.arc(centerX, centerY, radius, startingAngle, endingAngle, counterclockwise);
    context.arc( 0, 0, 3, 0, PI2, true );
    context.closePath();
    context.stroke();
};




//////////////////////////
// DiamondGeometry()
//////////////////////////
ASTROVIEW.DiamondGeometry = function(u, tx, ty, quadrant, color, zlevel, did, radius)
{
    THREE.Geometry.call( this );

    this.u = u;	
    this.tx = tx;
    this.ty = ty;
    this.quadrant = quadrant;
    this.color = color;
    this.zlevel = zlevel;
    this.did = did;                                                 // Diamond ID: 0 - 3
    this.tid = "[z,x,y]:[" + zlevel + "," + tx + "," + ty + "]";    // Toast ID: [zoom, tx, ty]
    this.radius = radius;
    this.diamonds=[];
    this.dynamic = true;
    
    // Create this.vertices[] array from u[] unit vector array
    if (u && u.length == 4)
    {
        for (var i=0; i<4; i++)
        {
            var v = this.scaleV(u[i], radius);
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length == 4)
    {
        var uv = this.createUV(quadrant);		// create uv[] array based on quadrant
        this.createFaces(this.vertices, uv);	// create this.faces[] based on this.vertices[] and uv[]
        this.computeCentroids();                // inherited call
    }
};

ASTROVIEW.DiamondGeometry.prototype = new THREE.Geometry();
ASTROVIEW.DiamondGeometry.prototype.constructor = ASTROVIEW.DiamondGeometry;

//////////////////////////
// createFaces()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.createFaces = function(v, uv)
{
    if ((v && v.length == 4) && (uv && uv.length == 4))
    {
        var normal = new THREE.Vector3( 0, 0, 1 );
        
        //
        // Create 'Upper' Face 0 (v0 -> v3 -> v1)
        //  
        var face0 = new THREE.Face3( 0, 3, 1);
        face0.normal.copy( normal );
        face0.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face0 );
        this.faceVertexUvs[ 0 ].push( [ uv[0], uv[3], uv[1] ]);
        
        //
        // Create 'Lower' Face 1 (v2 -> v1 -> v3)
        //
        var face1 = new THREE.Face3( 2, 1, 3 );
        face1.normal.copy( normal );
        face1.vertexNormals.push( normal.clone(), normal.clone(), normal.clone());
        this.faces.push( face1 );
        this.faceVertexUvs[ 0 ].push( [ uv[2], uv[1], uv[3] ]);
    }
};

//////////////////////////
// createUV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.createUV = function(quadrant)
{	
    var uv = new Array(4);
    switch (quadrant)
    {
        case "I":
        {
            uv[0] =  new THREE.UV( 1, 0 ); 
            uv[1] =  new THREE.UV( 1, 1 ); 
            uv[2] =  new THREE.UV( 0, 1 ); 
            uv[3] =  new THREE.UV( 0, 0 ); 
            break;
        }
        case "II":
        {
            uv[0] =  new THREE.UV( 0, 0 ); 
            uv[1] =  new THREE.UV( 1, 0 ); 
            uv[2] =  new THREE.UV( 1, 1 ); 
            uv[3] =  new THREE.UV( 0, 1 ); 	
            break;
        }
        case "III":
        {
            uv[0] =  new THREE.UV( 0, 1 ); 
            uv[1] =  new THREE.UV( 0, 0 ); 
            uv[2] =  new THREE.UV( 1, 0 ); 
            uv[3] =  new THREE.UV( 1, 1 ); 
            break;
        }
        case "IV":
        {
            uv[0] =  new THREE.UV( 1, 1 ); 
            uv[1] =  new THREE.UV( 0, 1 ); 
            uv[2] =  new THREE.UV( 0, 0 ); 
            uv[3] =  new THREE.UV( 1, 0 ); 
            break;
        }
	}
    return uv;
};

//////////////////////////
// inFrustrum() 
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.inFrustum = function(frustum)
{  
    var gcenter = this.getGeometryCenter();
    var gradius = this.getGeometryRadius();
    
    if (this.sphereInFrustum(gcenter, gradius, frustum))
    {
        return true;
    }
    else if (this.verticesInFrustum(frustum))
    {
        return true;
    }
    else
    {
        return false;
    }
};

//////////////////////////
// getCenter()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getGeometryCenter = function()	
{
    return this.midpointV(this.vertices[0], this.vertices[2]);
};

//////////////////////////
// getRadius()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getGeometryRadius = function()	
{
    if (!this.gradius)
    {
        var v0 = this.vertices[0].position;
        var v1 = this.vertices[1].position;
        var v2 = this.vertices[2].position;
        var v3 = this.vertices[3].position;
        
        var dv0v1 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v1);
        var dv0v2 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v2);
        var dv0v3 = new THREE.Vector3(v0.x, v0.y, v0.z).distanceTo(v3);
        var dv1v2 = new THREE.Vector3(v1.x, v1.y, v1.z).distanceTo(v2);
        var dv1v3 = new THREE.Vector3(v1.x, v1.y, v1.z).distanceTo(v3);
        var dv2v3 = new THREE.Vector3(v2.x, v2.y, v2.z).distanceTo(v3);
        
        this.gradius = Math.max(dv0v1, dv0v2, dv0v3, dv1v2, dv1v3, dv2v3)/2.5;
    }
    return this.gradius;
};

//////////////////////////
// sphereInFrustrum()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.sphereInFrustum = function(center, radius, frustum)
{
    var distance;
    var planes = frustum.planes;
    for ( var i=0; i<6; i++ )
    {
        distance = planes[i].x * center.position.x +
                   planes[i].y * center.position.y +
                   planes[i].z * center.position.z +
                   planes[i].w;
        
        if (distance < -radius)
        {
            return false;
        }
    }

    return true;
};

//////////////////////////
// pointsInFrustrum()
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
    //      NOTE: Diamond.u[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //	 	Make new points on the Diamond (a, b, c, d, e):
    //
    //      a = (u0+u1)/2
    //      b = (u0+u2)/2
    //      c = (u2+u3)/2
    //      d = (u3+u0)/2
    //      e = (u3+u1)/2
    //
    //         u0
    //         /\		    
    //        /  \
    //     d / D0 \ a         Construct 4 new diamonds    
    //      /\    /\            D0 = [u0, a,  e,  d]       
    //     /  \ e/  \           D1 = [ a,u1,  b,  e]       
    // u3 / D3 \/ D1 \ u1       D2 = [ e, b, u2,  c]       
    //    \    /\    /          D3 = [ d, e,  c, u3]      
    //     \  /  \  /
    //    c \/ D2 \/ b
    //       \    /
    //        \  /
    //         \/
    //         u2
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
            var dg = loopArray[i];
            var u = dg.u;
            
            //
            //	 Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = this.midpointV(u[0], u[1]); a = this.normalizeV(a);   // THREE.Vertex
            var b = this.midpointV(u[1], u[2]); b = this.normalizeV(b);   // THREE.Vertex
            var c = this.midpointV(u[2], u[3]); c = this.normalizeV(c);   // THREE.Vertex
            var d = this.midpointV(u[3], u[0]); d = this.normalizeV(d);   // THREE.Vertex
            var e = this.midpointV(u[3], u[1]); e = this.normalizeV(e);   // THREE.Vertex
            
            //
            //   Construct new diamonds
            //
            //	 [u0,  a,  e,  d]
            //	 [ a, u1,  b,  e]
            //	 [ e,  b, u2,  c]
            //   [ d,  e,  c, u3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.DiamondGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (x, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (0, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (x, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([u[0], a,  e,  d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, this.radius); // (x, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, u[1],  b,  e], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, this.radius); // (0, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,  b, u[2],  c], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, this.radius); // (0, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,  e,  c, u[3]], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, this.radius); // (x, 0)
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
};

//////////////////////////
// scaleV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.scaleV = function(v, radius)
{
    var v3 = new THREE.Vector3(v.position.x, v.position.y, v.position.z);
    v3.x *= radius;
    v3.y *= radius;
    v3.z *= radius;
    return new THREE.Vertex(v3); 
};


//////////////////////////
// midpointV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.midpointV = function(va, vb)
{
    var v3 = new THREE.Vector3((va.position.x + vb.position.x) * 0.5,
                               (va.position.y + vb.position.y) * 0.5,
                               (va.position.z + vb.position.z) * 0.5);			
    return new THREE.Vertex(v3);  
};

//////////////////////////
// normalizeV()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.normalizeV = function(v) 
{
    var v3 = new THREE.Vector3(v.position.x, v.position.y, v.position.z);
    var mag = v.position.x * v.position.x +
              v.position.y * v.position.y +
              v.position.z * v.position.z;
    
    if (mag != 0.0)
    {
        mag = 1.0/Math.sqrt(mag);
        v3.x *= mag;
        v3.y *= mag;
        v3.z *= mag;
    }
    return new THREE.Vertex(v3);
};   
ASTROVIEW.DiamondMesh = function( geometry, material )
{
    if (!material)
    {
        material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture("textures/Diamond.png") } );
    }

	THREE.Mesh.call( this, geometry, material );
    
    // Inherited Properties
    this.name = "DiamondMesh:" + geometry.tid;
    this.flipSided = true;
    this.doubleSided = false;
    
    // Additional Properties
    this.baseurl = "";      // Base URL Template that must be encoded to become imageurl
    this.imageurl = "";     // Actual URL to load remote image
    this.defaulturl = "textures/Diamond.png";
    
    // Initial opacity of texture
    this.opacity = 1.0;
};

ASTROVIEW.DiamondMesh.prototype = new THREE.Mesh();
ASTROVIEW.DiamondMesh.prototype.constructor = ASTROVIEW.DiamondMesh;

ASTROVIEW.DiamondMesh.prototype.render = function( baseurl, zlevel, frustum )
{
    // If we not in the frustum, do nothing
    if (this.inFrustum(frustum))
    {
        // If this is my zoom level, make myself visible and load image
        if (zlevel === this.geometry.zlevel)
        {
            // My level is now visible, start fading in
            this.opacity = 1.0;

            // Don't turn on opacity until image is loaded
            if (this.baseurl !== baseurl)
            {               
                var url = new String(baseurl);
                var dg = this.geometry; // DiamondGeometry
                
                this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
                this.loadTexture(this.imageurl);                
                this.baseurl = baseurl;
            }
        }
        else 
        {
            // My level is no longer visible, start fading out
            this.opacity = 0.0;         
            
            // If current zoom level is greater than me, ensure I have expanded my children 
            if (zlevel > this.geometry.zlevel)
            {
                if (this.children.length == 0)
                {
                    this.expandDiamondChildren();
                }
            }
        }
        
        // Render all my diamond children
        if (this.children.length > 0)
        {
            for (var i=0; i<this.children.length; i++)
            {
                var dm = this.children[i];
                dm.render(baseurl, zlevel, frustum);
            }
        }
        
        this.updateOpacity();
    }
};

ASTROVIEW.DiamondMesh.prototype.loadTexture = function(url)
{
    this.opacity = 0.0; // Loading new image, start fading out old texture
    
    this.image = new Image();
    this.texture = new THREE.Texture( this.image );

    this.image.onload = bind(this, this.onLoad);
    this.image.onerror = bind(this, this.onError);
    
    this.image.crossOrigin = '';
    this.image.src = url;
}

ASTROVIEW.DiamondMesh.prototype.onLoad = function(event)
{
    console.debug(this.name + " onload url: " + this.imageurl);
    this.texture.needsUpdate = true;
    this.material = new THREE.MeshBasicMaterial( { map: this.texture } );
    
    this.opacity = 1.0; // New image is loaded, start fading in new texture
}

ASTROVIEW.DiamondMesh.prototype.updateOpacity = function()
{
    if (this.material)
    {
        this.material.opacity = this.opacity;
        
        //
        // Gradually change Image Opacity
        //
        /*
        this.material.opacity += (this.opacity - this.material.opacity) * 0.5;
        if (Math.abs(this.material.opacity - this.opacity) < 0.1)
        {
            this.material.opacity = this.opacity;
        }
        */
    }
}

ASTROVIEW.DiamondMesh.prototype.onError = function(event)
{
    console.debug(this.name + " *** onerror url: " + this.imageurl);
    this.loadTexture(this.defaulturl);
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
    var YY = new THREE.Vertex( new THREE.Vector3(0.0,  1.0,  0.0)); // +Y
    var _Y = new THREE.Vertex( new THREE.Vector3(0.0, -1.0,  0.0)); // -Y
                    
    var XX = new THREE.Vertex( new THREE.Vector3( 1.0,  0.0,  0.0)); // +X
    var _X = new THREE.Vertex( new THREE.Vector3(-1.0,  0.0,  0.0)); // -X	

    var ZZ = new THREE.Vertex( new THREE.Vector3(0.0,  0.0,  1.0)); // +Z
    var _Z = new THREE.Vertex( new THREE.Vector3(0.0,  0.0, -1.0)); // +Z
    
    //
    // STEP 2: Create the Top Level DiamondGeometry located in each 3D Qudrant (I, II, III, IV),
    //         mapped to a TOAST Image Coordinate [tx, ty] as shown in the Mapping above.
    //
    // Quadrant I: [+X,+Z] ===> TOAST: [0,1] 
    var dgI = new ASTROVIEW.DiamondGeometry([YY, XX, _Y, ZZ], 0, 1, "I", 0x0000ff, zlevel, 1, RADIUS);
    
    // Quadrant II: [-Z,+X] ===> TOAST: [1,1]
    var dgII = new ASTROVIEW.DiamondGeometry([YY, _Z, _Y, XX], 1, 1, "II", 0x00ff00, zlevel, 0, RADIUS);
    
    // Quadrant III: [-X,-Z] ===> TOAST: [1,0] 
    var dgIII = new ASTROVIEW.DiamondGeometry([YY, _X, _Y, _Z], 1, 0, "III", 0xff0000, zlevel, 2, RADIUS);
    
    // Quadrant IV: [+Z,-X] ===> TOAST: [0,0] 
    var dgIV = new ASTROVIEW.DiamondGeometry([YY, ZZ, _Y, _X], 0, 0, "IV", 0xffff00, zlevel, 3, RADIUS);
    
    //
    // STEP 3: Expand Each Top Level DiamondGeometry Object to Level 4 Array of DiamondGeometry[] objects
    //
    var depth = ASTROVIEW.CameraController.ZOOM_LEVEL_MIN-1;	// expand 3 more levels...
    var zlevel = ASTROVIEW.CameraController.ZOOM_LEVEL_MIN;	    // ...to zlevel '4'

    var daI = dgI.expandDiamond(depth, zlevel, RADIUS);			// Quadrant I
    var daII = dgII.expandDiamond(depth, zlevel, RADIUS);		// Quadrant II
    var daIII = dgIII.expandDiamond(depth, zlevel, RADIUS);		// Quadrant III
    var daIV = dgIV.expandDiamond(depth, zlevel, RADIUS);		// Quadrant IV		
    
    //
    // STEP 4: Create DiamondMesh objects from the DiamondGeometry[] array and
    //         add them as children to the DiamondSphere
    //
    this.createDiamondsMaterial(daI, null); 
    this.createDiamondsMaterial(daII, null);
    this.createDiamondsMaterial(daIII, null);
    this.createDiamondsMaterial(daIV, null);
    
    /*
     //
     // Create DiamondMesh(s) with Test Image Material
     //
    var matI = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_1.PNG' ) } );
    var matII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_1.PNG' ) } );
    var matIII = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_1_0.PNG' ) } );
    var matIV = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture( 'textures/TOAST_0_0.PNG' ) } );
    
    this.createDiamondsMaterial(daI, matI); 
    this.createDiamondsMaterial(daII, matII);
    this.createDiamondsMaterial(daIII, matIII);
    this.createDiamondsMaterial(daIV, matIV);
    */
     
    /*
     //
     // Create DiamondMesh(s) with Color Material
     //
    this.createDiamondsColor(daI, dgI.color, true); 
    this.createDiamondsColor(daII, dgII.color, true);
    this.createDiamondsColor(daIII, dgIII.color, true);
    cthis.reateDiamondsColor(daIV, dgIV.color, true);
    */
    
    /*
    //
    // Create DiamondMesh(s) with URL Material
    //
    url = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
    this.createDiamondsUrl(daI, url); 
    this.createDiamondsUrl(daII, url);
    this.createDiamondsUrl(daIII, url);
    cthis.reateDiamondsUrl(daIV, url);
    */
}

/////////////////////////////
// createDiamondsColor()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsColor = function(diamonds, color, wireframe)
{	
    for (i=0; i<diamonds.length; i++)
    {
        var material = new THREE.MeshBasicMaterial( { color: color, wireframe: wireframe } );
        var dg = diamonds[i];
        this.addDiamond(dg, material);
    }
}

/////////////////////////////
// createDiamondsUrl()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsUrl = function(diamonds, baseurl)
{
    var url = new String(baseurl);

    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        var imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);
        var material = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(imageurl) } );
        this.addDiamond(dg, material);
    }
}

/////////////////////////////
// createDiamondsMaterial()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.createDiamondsMaterial = function(diamonds, material)
{
    for (i=0; i<diamonds.length; i++)
    {
        var dg = diamonds[i];
        this.createDiamond(dg, material);
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
// FootprintLayer()
/////////////////////////////
ASTROVIEW.FootprintLayer = function ( layer, radius )
{ 
    ASTROVIEW.AstroviewLayer.call( this, layer );
    
    if (layer && layer.rows)
    {
        this.radius = radius;
    
        //
        // For each footprint in the layer object, create a THREE.Line Object
        // and add it as a child to this Layer
        //
        if (layer.rows)
        {
            for (var i=0; i<layer.rows.length; i++)
            {
                var row = layer.rows[i];
                if (row.footprint)
                {
                    this.createFootprint(row.footprint);
                }
            }
        }
    }
};

ASTROVIEW.FootprintLayer.prototype = new ASTROVIEW.AstroviewLayer();
ASTROVIEW.FootprintLayer.prototype.constructor = ASTROVIEW.FootprintLayer;

/////////////////////////////
// createFootprint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(footprint)
{
    //
    // Split incoming string to extract [RA] [DEC] pairs
    //
    var type = "";
    var polygons = [];
    var footprintUpper = trim(footprint.toUpperCase());
    if (footprintUpper.indexOf(ASTROVIEW.POLYGON_J2000) >= 0)
    {
        polygons = footprintUpper.split(/POLYGON J2000\s+/);
        type = ASTROVIEW.POLYGON_J2000;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.POLYGON_ICRS) >= 0)
    {
        polygons = footprintUpper.split(/POLYGON ICRS\s+/);
        type = ASTROVIEW.POLYGON_ICRS;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.CIRCLE_ICRS) >= 0)
    {
        polygons = footprintUpper.split(/CIRCLE ICRS\s+/);
        type = ASTROVIEW.CIRCLE_ICRS;
    }
    else if (footprintUpper.indexOf(ASTROVIEW.CIRCLE_J2000) >= 0)
    {
        polygons = footprintUpper.split(/CIRCLE J2000\s+/);
        type = ASTROVIEW.CIRCLE_J2000;
    }
    
    // If we found no valid polygons, bail
    if (polygons == null || polygons.length < 2)
    {
        alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + footprint, "Unable to Create Footprint");
        return;
    }
    
    // Create material used for all lines
    if (!this.material)
    {
        this.material = new THREE.LineBasicMaterial( { color: 0xff0000, linewidth: 1 } );
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolylines(polygons);
            break;
        }
        case ASTROVIEW.CIRCLE_ICRS:
        case ASTROVIEW.CIRCLE_J2000:
        {
            this.createCircles(polygons);
            break;
        }
        default:
        {
            alert("Footprint Syntax Error\n\n   Valid Syntax: 'Polygon J2000 [values]' or 'Polygon ICRS [values]' or 'CIRCLE ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolylines()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolylines = function(polygons)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            var coords = polygon.split(/\s+/);
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
};

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(polygons)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<polygons.length; i++)
    {
        var polygon = trim(polygons[i]);
        if (polygon.length > 0)
        {
            //
            // Create Array of 3D Vertices from coordinate values storing them in geometry.vertices[] 
            //
            var coords = polygon.split(/\s+/);
            if (coords.length == 3)
            {
                // Extract RA, DEC and RADIUS from coord Array
                var ra = Number(coords[0]);
                var dec = Number(coords[1]);
                var radius = Number(coords[2]);
                var geometry = new THREE.Geometry(); 
                
                // Create Circle storing vertices in geometry.vertices[] 
                this.createCircle(ra, dec, radius, geometry);
                
                // Create new Line from the geometry and material
                var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
                this.add(line);	
            }
            else
            {
                alert("Footprint Circle Error.\n\n Expecting 3 values containing [RA] [DEC] [RADIUS], actual length: " + coords.length + "\n\n" + polygon, "Unable to Create Footprint");
            }
        }
    } // for each polygon[]
}

/////////////////////////////
// createCircle()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircle = function(ra, dec, radius, geometry)
{               
    // Build circle as polygon with 60 vertices (is this good enough???)
    for (var i=0; i<=60; i++)
    {
        var angle = 6.0*i*TO_RADIANS;
        var y = dec + radius*Math.sin(angle);
        var x = ra + (radius*Math.cos(angle)/Math.cos(y*TO_RADIANS));
        
        // Add the new point to the geometry array
        var v = this.raDecToVertex(x, y, this.radius);
        geometry.vertices.push(v);
    }
}

ASTROVIEW.POLYGON_J2000 = "POLYGON J2000";
ASTROVIEW.POLYGON_ICRS  = "POLYGON ICRS";
ASTROVIEW.CIRCLE_J2000  = "CIRCLE J2000";
ASTROVIEW.CIRCLE_ICRS   = "CIRCLE ICRS";
ASTROVIEW.ImageLayer = function ( layer, radius )
{ 
    THREE.Object3D.call( this );
    
    if (layer)
    {
        this.name = (layer.lid ? layer.lid : "ImageLayer");
        this.layer = layer;
    
        //
        // For each footprint in the layer object, create a THREE.Line Object
        // and add it as a child to this Layer
        //
        if (layer.rows)
        {
            for (var i=0; i<layer.rows.length; i++)
            {
                var row = layer.rows[i];
                if (row.image)
                {
                    this.addImage(row.image);
                }
            }
        }
    }
};

ASTROVIEW.ImageLayer.prototype = new THREE.Object3D();
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

