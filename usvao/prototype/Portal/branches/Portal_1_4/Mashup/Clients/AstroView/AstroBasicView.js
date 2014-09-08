////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Constants
////////////////////////
// Diamond Radius and Viewport Far Plane
ASTROVIEW.RADIUS = 1000;
ASTROVIEW.RADIUS_SPHERE    = 1000;
ASTROVIEW.RADIUS_GRAPHICS  =  920;

// Zoom Levels
ASTROVIEW.ZOOM_LEVEL_MIN = 4;
ASTROVIEW.ZOOM_LEVEL_MAX = 12;

// Field of View Levels
ASTROVIEW.FOV_LEVEL_MAX = 30;
ASTROVIEW.FOV_LEVEL_MIN = 0.00001;

// redraw Rate
ASTROVIEW.TIMER_TICKS_ACTIVE = 40;
ASTROVIEW.TIMER_TICKS_IDLE   = 300;

// View State
ASTROVIEW.VIEW_STATE_ACTIVE = "ACTIVE";
ASTROVIEW.VIEW_STATE_IDLE = "IDLE";

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

////////////////////////
// Math Constants
////////////////////////
PI2 = Math.PI * 2.0;
RADIANS_10 = Math.PI/18.0;
RADIANS_30 = Math.PI/6.0;
RADIANS_90 = Math.PI/2.0;
RADIANS_360 = Math.PI*2.0;
TO_RADIANS = Math.PI/180.0;
TO_DEGREES = 180.0/Math.PI;

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
galex_mastproxyvm1_url = "http://mastproxyvm1.stsci.edu/images/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
dss_mastproxyvm1_url = "http://mastproxyvm1.stsci.edu/images/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs directly to IIS Server
galex_direct_url = "http://masttest.stsci.edu/surveys/toast/dss2/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
dss_direct_url = "http://masttest.stsci.edu/surveys/toast/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";

// Survey URLs through Mashup Proxy
galex_proxy_url = "http://masttest.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=" + galex_direct_url;
dss_proxy_url = "http://masttest.stsci.edu/portal/Mashup/MashupTest.asmx/MashupTestHttpProxy?url=" + dss_direct_url;
    
////////////////////////
// AstroBasicView
////////////////////////
ASTROVIEW.AstroBasicView = function ( div, rendertype, avcontainer )
{
    // Create element if not specified
    if (!div)
    {
        div = document.createElement('div');
        document.body.appendChild(div);
    }
    this.div = div;
    
    // Set render type if not specified
    if (!rendertype)
    {
        rendertype = "canvas";
    }
    this.rendertype = rendertype;
    
    // Store the AstroView Container
    this.avcontainer = avcontainer;
        
    // Set Default Toast Survey URL(s)
    this.baseurl = dss_direct_url;
       
    // Core 3D objects
    this.scene = null; 
    this.camera = null;
    this.canvas = null;
    this.controller = null;
    
    // Renderers
    //this.memoryRenderer = null;
    this.graphicsRenderer = null;
    this.diamondRenderer = null;
    this.renderers = null;
   
    // Scenes
    this.diamondScene = null;
    this.graphicsScene = null;
    this.selectScene = null;
    this.scenes = null;
    
    // Rendering Objects
    this.diamondSphere = null;
   
    // Rendering Stats
    this.stats = null;
    
    // Animation Ticks
    this.ticks = ASTROVIEW.TIMER_TICKS_ACTIVE;
    
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
            
        // Create Renderers (one for each scene) 
        this.renderers = this.createRenderers(this.rendertype);
        if (!this.renderers || this.renderers.length == 0)
        {
            alert ("AstroView: Unable to create renderer(s) of type: " + this.rendertype);
            console.error("AstroView: Unable to create renderer(s) of type: " + this.rendertype);
            return;
        }
        
        // Create Scenes (one for each renderer) 
        this.scenes = this.createScenes(this.rendertype);
        
        // Create Camera Controller
        this.canvas = this.selectRenderer.domElement;
        this.scene = this.graphicsScene;    // Scene Used to find selected objects
        console.log("AstroBasicView: using event canvas = " + this.canvas.id);
        this.controller = new ASTROVIEW.CameraController( this,
                                                          this.camera, 
                                                          this.scene, 
                                                          this.canvas, 
                                                          this.renderers);
        // Stats
        this.stats = new Stats();
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.top = '0px';
        this.div.appendChild(this.stats.domElement);
    }

    ////////////////////
    // createRenderers()
    ////////////////////
    this.createRenderers = function(rendertype)
    {     
        var renderers = [];
        
        // NOTE: We try to create a WebGL Renderer and fallback to the Canvas Renderer.   
        switch (rendertype)
        {
            case "webgl":
            {
                if (!this.diamondRenderer)
                {
                    try{
                        // Create Diamond Renderer (1)
                        this.diamondRenderer = new THREE.WebGLRenderer();
                        this.diamondRenderer.setSize( window.innerWidth, window.innerHeight ); 
                        renderers.push(this.diamondRenderer);
                        
                        // Create Graphics Renderer (2)
                        this.graphicsRenderer = new THREE.WebGLRenderer();
                        this.graphicsRenderer.setSize( window.innerWidth, window.innerHeight ); 
                        renderers.push(this.graphicsRenderer);   
                        
                        // Create Select Renderer (3)
                        this.selectRenderer = new THREE.WebGLRenderer();
                        this.selectRenderer.setSize( window.innerWidth, window.innerHeight ); 
                        renderers.push(this.selectRenderer); 
                        
                        break;
                    } catch(error) {this.diamondRenderer=null; this.renderType = "canvas";}
                }
            }
            case "canvas":
            {
                if (!this.diamondRenderer)
                {    
                    try{   
                        // Create Memory Renderer (0)
                        //this.memoryRenderer = new THREE.CanvasRenderer();
                        //this.memoryRenderer.setSize( window.innerWidth, window.innerHeight );
                        //this.memoryRenderer.autoClear = false;
                        //renderers.push(this.memoryRenderer);
                                     
                        // Create Diamond Renderer (1)
                        this.diamondRenderer = new THREE.CanvasRenderer();
                        this.diamondRenderer.setSize( window.innerWidth, window.innerHeight );
                        renderers.push(this.diamondRenderer);
                        
                        // Create Graphics Renderer (2)
                        this.graphicsRenderer = new THREE.CanvasRenderer();
                        this.graphicsRenderer.setSize( window.innerWidth, window.innerHeight );
                        this.graphicsRenderer.autoClear = false;
                        renderers.push(this.graphicsRenderer);
                        
                        // Create Selection Renderer (3)
                        this.selectRenderer = new THREE.CanvasRenderer();
                        this.selectRenderer.setSize( window.innerWidth, window.innerHeight );
                        renderers.push(this.selectRenderer);
                     
                        break;
                    } catch(error) {this.diamondRenderer=null;}
                }
            }
        }
        
        // Set Canvas Properties so they stack up in the browser as Image Rendering Layers
        var zIndex = 1;
        if (this.diamondRenderer && this.diamondRenderer.domElement)
        {       
            var canvas = this.diamondRenderer.domElement;                     
            canvas.id = "diamondCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas );      
        }
        
        if (this.graphicsRenderer && this.graphicsRenderer.domElement)
        {                           
            var canvas = this.graphicsRenderer.domElement;
            canvas.id = "graphicsCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        if (this.selectRenderer && this.selectRenderer.domElement)
        {                           
            var canvas = this.selectRenderer.domElement;
            canvas.id = "selectCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        // Return all renderers
        return renderers; 
    }
    
    this.createScenes = function(renderType)
    {
        var scenes = [];
        
        // 
        // NOTE: HACK HERE for line below:
        // this.camera.parent = undefined;
        //
        // To work around (THREE.Object3D) unnecessary behavior:  
        // THREE.Object3D wants to perform a removal of the camera from the previous Scene if camera.parent is (!=undefined).
        // So we temporarily set it to 'undefined' to avoid its removal from the previous Scene.  
        //
        
        // Diamond Scene
        this.camera.parent = undefined;  
        this.diamondScene = (renderType == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.diamondScene.add (this.camera);
        this.diamondScene.name = "DiamondScene";   
        this.diamondSphere = new ASTROVIEW.DiamondSphere();
        this.diamondScene.add(this.diamondSphere);
        scenes.push(this.diamondScene);
             
        // Graphics Scene 
        this.camera.parent = undefined;  
        this.graphicsScene = (renderType == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.graphicsScene.add(this.camera);
        this.graphicsScene.name = "GraphicsScene";
        scenes.push(this.graphicsScene);
        
        // Select Scene
        this.camera.parent = undefined;
        this.selectScene = (renderType == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.selectScene.add(this.camera);
        this.selectScene.name = "SelectScene";
        scenes.push(this.selectScene);
        
        return scenes;
    }

    ////////////////////////
    // renderScene()
    ////////////////////////
    this.renderScene = function() 
    {     
        // Update the Camera Position 
        var cameraChanged = this.controller.updateController();
        
        // Update Each Diamond in the View Frustum, using the new Canvas Size & Camera Position
        this.diamondSphere.renderScene(this);
                
        // Render Scene based on internal renderer 
        if (this.diamondRenderer instanceof THREE.CanvasRenderer)
        {     
            this.renderCanvas(cameraChanged); 
        }
        else if (this.diamondRenderer instanceof THREE.WebGLRenderer) 
        {
            this.renderWebGL(cameraChanged);
        }  
        
        // Update the stats window
        this.stats.update();
        
        // Update the refresh rate based on Active Mouse and Current Render State
        if (this.mouseOver) 
            this.ticks = ASTROVIEW.TIMER_TICKS_ACTIVE;
        else if (this.renderState == "IDLE")
            this.ticks = ASTROVIEW.TIMER_TICKS_IDLE;
        else
            this.ticks = ASTROVIEW.TIMER_TICKS_ACTIVE;
        
        // Request another animation frame
        startTimer(bind(this, this.renderScene), this.ticks);
    } 
    
    ////////////////////////
    // renderWebGL()
    ////////////////////////
    this.renderWebGL = function (cameraChanged)
    {
        this.diamondRenderer.render(this.diamondScene, this.camera);
        this.graphicsRenderer.render(this.graphicsScene, this.camera);
        this.selectRenderer.render(this.selectScene, this.camera);

        // Update the render State
        if (!cameraChanged)
        {
            this.renderState = "IDLE";
        }
    }
    
    var renderOrder = {"CAMERA":1, "DIAMOND":2, "GRAPHICS":3, "RENDER":4, "SELECT":5, "STATS":6, "IDLE":7};
    this.renderState = "CAMERA";
    this.render = function(state)
    {
        if (!state || state == "") state = "DIAMOND";
        // Set the rendering state ONLY if it precedes the current state
        if (renderOrder[state] < renderOrder[this.renderState])
        {
            this.renderState = state;
        }
    }
    
    ////////////////////////
    // renderCanvas()
    ////////////////////////
    this.renderCount = 0;
    this.renderStart = 0;
    this.renderEnd = 0;
    this.projectGraphics = false;
    this.RENDER_SIZE = 200;
    this.renderStats = null;
    
    this.renderCanvas = function(cameraChanged) 
    {
        if (cameraChanged) this.renderState = "CAMERA";
        var start = new Date().getTime();
        
        switch (this.renderState)
        { 
            case "CAMERA":
            {
                // (1) Camera Changed: Redraw the background image(s)
                this.diamondRenderer.render(this.diamondScene, this.camera);
                this.clearRenderer(this.graphicsRenderer);
                this.clearRenderer(this.selectRenderer);
                this.updateRenderStats(this.renderState, start);
                this.renderState = "DIAMOND";
                break;
            }
            case "DIAMOND":
            {
                // (2) Diamond: Redraw the background image(s) (again)              
                this.diamondRenderer.render(this.diamondScene, this.camera);
                this.updateRenderStats(this.renderState, start);
                this.renderState = "GRAPHICS";
                break; 
            }
            case "GRAPHICS":
            {   
                // (3) Project Graphics:
                //     This converts all 3D graphics coordinates to 2D screen coordinates.
                //     It also allows us to get the number of graphics objects in the scene.  
                //     No Graphics Rendering Takes Place Yet!
                this.clearRenderer(this.graphicsRenderer);
                this.renderCount = this.graphicsRenderer.render(this.graphicsScene, this.camera, true); 
                if (this.renderCount > 0)
                {
                    this.RENDER_SIZE = 200;
                    this.renderStart = 0;
                    this.renderEnd = (this.renderCount >= this.RENDER_SIZE ? this.RENDER_SIZE-1 : this.renderCount-1);
                }
                this.updateRenderStats(this.renderState, start);
                this.renderState = (this.renderCount > 0 ? "RENDER" : "SELECT");
                break;
            }
            case "RENDER":
            {
                // (4) Render Graphics: Render the next chunk of projected graphics objects to the offscreen canvas
                if (this.renderCount > 0)
                {               
                    this.graphicsRenderer.render(this.graphicsScene, this.camera, false, this.renderStart, this.renderEnd); 
                    this.renderCount -= this.RENDER_SIZE;
                    this.renderStart += this.RENDER_SIZE;
                    this.RENDER_SIZE *= 5;              // Scale up graphics rendering size to complete in fewer iterations.
                    this.renderEnd += this.RENDER_SIZE;
                }
                
                // Refresh the Graphics Canvas (Working around iPad partial draw problem here)
                this.refreshRenderer(this.graphicsRenderer);
           
                this.updateRenderStats(this.renderState, start);
                this.renderState = (this.renderCount > 0 ? "RENDER" : "SELECT");
                break;
            }
            case "SELECT":
            {
                // (5) Render Graphics: Render the next chunk of projected graphics objects to the offscreen canvas
                this.selectRenderer.render(this.selectScene, this.camera);
                this.updateRenderStats(this.renderState, start);
                this.renderState = "STATS";
                break;
            }
            case "STATS":
            {
                // (6) Dump Render Stats
                console.log("RENDER STATS: " + ASTROVIEW.toJson(this.renderStats));
                this.renderStats = null;
                this.renderState = "IDLE";
                break;
            }
            case "IDLE":
            {
                break;
            }
        }
    }
    
    ////////////////////////
    // updateRenderStats()
    ////////////////////////
    this.updateRenderStats = function(state, start)
    {
        if (!this.renderStats) this.renderStats = {};
        if (!this.renderStats[state])
        {
            this.renderStats[state] = [];
        }
        this.renderStats[state].push(new Date().getTime() - start);
    }
    
    ////////////////////////
    // clearRenderer()
    ////////////////////////   
    this.clearRenderer = function (renderer)
    {
        var canvas = renderer.domElement;
        c = canvas.getContext("2d");    
        c.save();
        c.setTransform(1, 0, 0, 1, 0, 0);
        c.clearRect(0, 0, canvas.width, canvas.height);
        c.restore();        
    }
    
    ////////////////////////
    // refreshRenderer()
    ////////////////////////   
    this.refreshRenderer = function (renderer)
    {
        var image = renderer.domElement;
        var ctx = renderer.domElement.getContext('2d');
        if (ctx && image)
        {
            ctx.drawImage(image, 0, 0);
        }      
    }
    
    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {
        document.addEventListener( 'keypress' , bind(this, this.onKeyPress), false);
        
        // Add Events attached to <Canvas> Element
        this.canvas.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        this.canvas.addEventListener( 'mouseover',  bind(this, this.onMouseOver),  false );
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////               
    //
    // Events
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    this.mouseOver = true;
    this.onKeyPress = function( event )
    {        
        if (this.mouseOver)
        {
            var unicode=event.keyCode? event.keyCode : event.charCode;
            //alert("onKeyPress: " + unicode); // find the char code        
            switch(unicode)
            {
                // Testing of Viewer
                case  98:   this.onBoxLayer(); break;       // 'B'
                case  99:   this.onCatalogLayer(); break;   // 'C'
                case 100:   this.onDeleteLayer(); break;    // 'D'
                case 102:   this.onFootprintLayer(); break; // 'F'
                case 105:   this.onImageLayer(); break;     // 'I'
                case 109:   this.onMove(); break;           // 'M'
                case 114:   this.onReadLayer(); break;      // 'R'
                case 115:   this.onSelectLayer(); break;    // 'S'
                case 117:   this.onUrl(); break;            // 'U'
                case 118:   this.onVisibility(); break;     // 'V'
                case 122:   this.onZero(); break;           // 'Z'
            }
        }
    }
    
    this.onMouseOut = function (event)
    {
        this.mouseOver = false;
    }
    
    this.onMouseOver = function (event)
    {
        this.mouseOver = true;
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
        if (this.scene.getChildByName(this.graphicsScene.name))
        {
            this.scene.remove(this.graphicsScene);
        }
        else
        {
            this.scene.add(this.graphicsScene);
        }
    }    

    // [210.8023, 54.3490]
    // [202.4842, 47.2306]
    // [148.8882, 69.0653]
    // [23.4620, 30.6602] 
    
    var x=0;
    var ra =     [0, 210.8023, 202.4842, 148.8882, 23.4620];
    var dec =    [0, 54.3490, 47.2306, 69.0653, 30.6602];
    var level =  [4, 10, 10, 10];
    
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
    // 'Z' : onZero() 
    //////////////////////////
    this.onZero = function()
    {
        var coord={"ra":0, "dec":0, "zoom":4};
        this.moveTo(coord);
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
                alert("layerData [" + layerData.lid + "]:\n" + ASTROVIEW.toJson(layerData));
            }
        }
    }
    
    //////////////////////////
    // 'S' : onSelectLayer() 
    //////////////////////////
    this.onSelectLayer = function()
    {        
        rows = {"footprint":redBox};
        attribs = {"color":"0xffff00"};
        name = "yellowBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows], "canvas":"selectCanvas"};
        layerNames.push (this.createLayer(layerData));
    }
    
    //////////////////////////
    // 'D' : onDeletLayer() 
    //////////////////////////
    this.onDeleteLayer = function()
    {
        for (var i in layerNames)
        {   
            var name = layerNames[i];
            this.deleteLayer(name);
        }
        layerNames = [];
    }
    
    ////////////////////////////
    // 'B' : onBoxLayer() 
    ////////////////////////////
    this.onBoxLayer = function()
    {       
        var rows = {"footprint":smallRedBox};
        var attribs = {"color":"0xff0000"};
        var name = "smallRedBox";
        var layerData = {"name":name, "type":"footprint", "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
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
        layerNames.push (this.createLayer(layerData));
    }
    
    var smallRedBox = "Polygon J2000 1.0 -1.0 1.0 1.0 359.0 1.0 359.0 -1.0";
    var redBox = "Polygon J2000 5.0 -5.0 5.0 5.0 355.0 5.0 355.0 -5.0";
    
    var yellowPoints = "Position ICRS 5.0 -5.0 Position ICRS 5.0 5.0 Position ICRS 355.0 5.0 Postion ICRS 355.0 -5.0  ";   
    var redPoints = "Position ICRS 6.0 -6.0 Position ICRS 6.0 6.0 Position ICRS 354.0 6.0 Postion ICRS 354.0 -6.0  "; 
    var greenPoints = "Position ICRS 7.0 -7.0 Position ICRS 7.0 7.0 Position ICRS 353.0 7.0 Postion ICRS 353.0 -7.0  ";  
    var bluePoints = "Position ICRS 8.0 -8.0 Position ICRS 8.0 8.0 Position ICRS 352.0 8.0 Postion ICRS 352.0 -8.0  ";  
    var cyanPoints = "Position ICRS 9.0 -9.0 Position ICRS 9.0 9.0 Position ICRS 351.0 9.0 Postion ICRS 351.0 -9.0  ";  

    var acsM101 = "Polygon J2000 210.75890230 54.38019650 210.79889830 54.32921760 210.84012320 54.34291740 210.80016510 54.39391000      Polygon J2000  210.79858090 54.32875470 210.75693070 54.38001520 210.71409030 54.36600310 210.75577980 54.31475730";
    var greenPlus = "Polygon J2000 1.0 -0.0 359.0 0.0 Polygon J2000 0.0 1.0 0.0 -1.0";
    
    //var galexM101 = "POLYGON ICRS 211.43464545 54.358924 211.43122163 54.42425429 211.4209877 54.48886881 211.40405577 54.55205962 211.38061136 54.6131344 211.35091133 54.671424 211.31528107 54.72628978 211.27411097 54.77713063 211.22785208 54.82338952 211.17701123 54.86455962 211.12214545 54.90018988 211.06385585 54.92988991 211.00278107 54.95333432 210.93959026 54.97026625 210.87497574 54.98050018 210.80964545 54.983924 210.74431516 54.98050018 210.67970064 54.97026625 210.61650983 54.95333432 210.55543505 54.92988991 210.49714545 54.90018988 210.44227967 54.86455962 210.39143882 54.82338952 210.34517993 54.77713063 210.30400983 54.72628978 210.26837957 54.671424 210.23867954 54.6131344 210.21523513 54.55205962 210.1983032 54.48886881 210.18806927 54.42425429 210.18464545 54.358924 210.18806927 54.29359371 210.1983032 54.22897919 210.21523513 54.16578838 210.23867954 54.1047136 210.26837957 54.046424 210.30400983 53.99155822 210.34517993 53.94071737 210.39143882 53.89445848 210.44227967 53.85328838 210.49714545 53.81765812 210.55543505 53.78795809 210.61650983 53.76451368 210.67970064 53.74758175 210.74431516 53.73734782 210.80964545 53.733924 210.87497574 53.73734782 210.93959026 53.74758175 211.00278107 53.76451368 211.06385585 53.78795809 211.12214545 53.81765812 211.17701123 53.85328838 211.22785208 53.89445848 211.27411097 53.94071737 211.31528107 53.99155822 211.35091133 54.046424 211.38061136 54.1047136 211.40405577 54.16578838 211.4209877 54.22897919 211.43122163 54.29359371 211.43464545 54.358924";
    var galexM101 = "CIRCLE ICRS 210.80964545 54.35892400 0.625";
    var galexNGC5474 = "CIRCLE ICRS 211.25948613 53.67449567 0.625";
    var circle00 = "CIRCLE ICRS 0 0 1.0";
    var circle045 = "CIRCLE ICRS 0 45 1.0";
    var circle080 = "CIRCLE ICRS 0 80 1.0";
    
    var layerNames = [];
    ////////////////////////////
    // 'F' : onFootprintLayer() 
    ////////////////////////////
    this.onFootprintLayer = function()
    {
        rows = {"footprint":greenPlus};
        attribs = {"color":"0x00ff00"};
        name = "greenPlus";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":yellowPoints};
        attribs = {"color":"0xffff00", "symbol":"square"};
        name = "yellowPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":redPoints};
        attribs = {"color":"0xff0000", "symbol":"stop"};
        name = "redPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":greenPoints};
        attribs = {"color":"0x00ff00", "symbol":"diamond"};
        name = "greenPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":bluePoints};
        attribs = {"color":"0x0000ff", "symbol":"circle"};
        name = "bluePoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));     
        
        rows = {"footprint":cyanPoints};
        attribs = {"color":"0x00ffff", "symbol":"plus"};
        name = "cyanPoints";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));         
        
        rows = {"footprint":redBox};
        attribs = {"color":"0xff0000"};
        name = "redBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle00};
        attribs = {"color":"0x00ff00"};
        name = "circle00";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle045};
        attribs = {"color":"0x0000ff"};
        name = "circle045";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":circle080};
        attribs = {"color":"0x00ffff"};
        name = "circle080";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        var rows = {"footprint":acsM101};
        var attribs = {"color":"0xff0000"};
        var name = "acsM101";
        var layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
        
        rows = {"footprint":galexNGC5474};
        attribs = {"color":"0x5555ff"};
        name = "galexNGC5474";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows]};
        layerNames.push (this.createLayer(layerData));
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
        layerNames.push (this.createLayer(layerData));
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////               
    //
    // AsroView API
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
        
    //////////////////////////
    // init() 
    //////////////////////////
    this.init = function(avcontainer)
    {
        if (typeof avcontainer != 'undefined')  // check container passed in
        {
            this.avcontainer = avcontainer;
            console.log("AstroBasicView:init() Complete: using avcontainer passed in."); 
        }
        else if (typeof AstroViewContainer != 'undefined')  // check for global container
        {
            this.avcontainer = AstroViewContainer;
            console.log("AstroBasicView:init() Complete: using AstroViewContainer.");
        }
        else
        {
            console.log("AstroBasicView:init() Failed: avcontainer is undefined.");
        }
    }
    
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
                    console.error("AstroBasicView:createImageLayer() not implemented yet");
                    //sphere = createImageLayer(layerData);
                    break;
                }
            }
        }
        
        // Add new AstroView layer to the Scene
        if (layer)
        {
            if (layer.layerData.canvas && layer.layerData.canvas.toLowerCase() == "selectcanvas")
            {
                this.selectScene.add(layer);
                this.render("SELECT");
            }
            else
            {
                this.graphicsScene.add(layer);
                this.render("GRAPHICS");
            }
        }
        
        // Return the Layer ID
        console.log("AstroBasicView: createLayer() " + (layer ? layer.name : "?"));
        return (layer ? layer.name : "");
    }
    
    //////////////////////////
    // hasLayer() 
    //////////////////////////
    this.hasLayer = function (name)
    {
        var layer = this.graphicsScene.getChildByName(name, true) ||
                    this.selectScene.getChildByName(name, true);
        console.log("AstroBasicView: hasLayer() " + name + " : " + (layer != null));
        return (layer != null);
    }
             
    //////////////////////////
    // readLayer() 
    //////////////////////////
    this.readLayer = function(name)
    {
        console.log("AstroBasicView: readLayer() " + name);
        var layer = this.graphicsScene.getChildByName(name, true) ||
                    this.selectScene.getChildByName(name, true);
        return (layer ? layer.layerData : "");
    }
    
    //////////////////////////
    // udpateLayer() 
    //////////////////////////
    this.udpateLayer = function(name, layerData)
    {
        return (true);
    }
    
    //////////////////////////
    // deleteLayer() 
    //////////////////////////
    this.deleteLayer = function(name)
    {
        // Try to remove from Graphics Scene First
        var layer = this.graphicsScene.getChildByName(name, true);
        if (layer) 
        {
            layer.clean();
            this.graphicsScene.remove(layer, true);
            this.render("GRAPHICS");
        }

        // Try to remove from Select Scene Second
        if (!layer)
        {
            layer = this.selectScene.getChildByName(name, true);
            if (layer)
            {
                layer.clean();
                this.selectScene.remove(layer, true);
                this.render("SELECT");
            }
        }

        console.log("AstroBasicView: deleteLayer() " + name + " : " + (layer != null));
        return (layer ? layer.name : "");
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
    // sendEvent() 
    //////////////////////////
    this.sendEvent = function (type, objects)
    {
        if (this.avcontainer && this.avcontainer.onAstroViewEvent)
        {
            var msg = {"type" : type, "objects" : objects};
            this.avcontainer.onAstroViewEvent(msg);
        }
        else
        {
            console.log ("sendAstroViewEvent() Not Sending Event: " + type + ". Missing onAstroViewEvent() callback.");
        }
    }
            
    ///////////////
    // Main
    ///////////////
    console.debug("Go AstroView (Javascript) !!!");
    this.createScene();
    this.addEvents();
    this.renderScene();
    this.sendEvent("AstroView.Initialization.Complete");
};

