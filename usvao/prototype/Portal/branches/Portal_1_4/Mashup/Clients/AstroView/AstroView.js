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

/////////////////////////
// CameraController
/////////////////////////
ASTROVIEW.CameraController = function ( av, camera, scene, canvas, renderers )
{
    // Save the objects we need from the AstroBasicView
    this.av = av;
    this.camera = camera;
    this.scene = scene;
    this.canvas = canvas;
    this.renderers = renderers;

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
    this.zoomLookupTable =  [this.fovmax+1, // 0
                            this.fovmax+1,  // 1
                            this.fovmax+1,  // 2
                            this.fovmax+1,  // 3
                            10,             // 4
                            5,              // 5
                            2.5,            // 6
                            1.25,           // 7
                            0.75,           // 8
                            0.355,          // 9
                            0.125,          // 10
                            0.0625,         // 11
                            0.00001];       // 12
 
    //this.visibleLayers = "4, 5, 6, 7, 8, 9, 10, 11, 12";
    this.visibleLayers = "4, 6, 8, 10, 12";
 
    // Camera mouse
    this.cameraRotationX = 0;
    this.cameraRotationY = 0;
    
    // Fov
    this.cameraFov = this.fovmax;    
    this.mouseZoomInFactor = 0.1;   // How fast you zoom in each mouse wheel
    this.mouseZoomOutFactor = 0.1;  // How fast you zoom out each mouse wheel
    this.mouseMoveFactor = 0.2;      // How fast you move/rotate scene on each mouse move.
    this.mouseLineDistance = 4.0;
    this.mouseParticleDistance = 6.0;
   
    this.touchZoomInFactor = 0.2;   // How fast you zoom on each touch gesture
    this.touchZoomOutFactor = 1.0;  // How fast you zoom on each touch gesture
    this.touchMoveFactor = 0.1;     // How fast you move/rotate scene on each touch move.
    this.touchLineDistance = 12.0;
    this.touchParticleDistance = 18.0;
    
    // Utility Classes needed for Transformations
    this.frustum = new THREE.Frustum();
    this.frustumMatrix = new THREE.Matrix4();
    this.projector = new THREE.Projector();
    
    // Mouse Events
    this.delta = new THREE.Vector2();
 
    // Current mouse position in ALL Coordinate Systems
    this.mouse = {};
    this.mouse.screen   = new THREE.Vector3();
    this.mouse.start    = new THREE.Vector3();
    this.mouse.last     = new THREE.Vector3();
    this.mouse.normal   = new THREE.Vector3();
    this.mouse.world    = new THREE.Vector3();
    this.mouse.graphics = new THREE.Vector3();

    this.mouse.down = false;
    this.mouse.over = false;
    this.mouse.scale = 1.0;

    // RA/DEC
    this.mouse.ra = 0.0;
    this.mouse.dec = 0.0;
    this.mouse.sra = "";
    this.mouse.sdec = "";

    ////////////////////////
    // addEvents()
    ////////////////////////
    this.addEvents = function()
    {        
        // Mouse Events
        this.canvas.addEventListener( 'mouseout',  bind(this, this.onMouseOut),  false );
        this.canvas.addEventListener( 'mouseover', bind(this, this.onMouseOver),  false );
        this.canvas.addEventListener( 'mousemove', bind(this, this.onMouseMove), false );
        this.canvas.addEventListener( 'mousedown', bind(this, this.onMouseDown), false );
        this.canvas.addEventListener( 'mouseup',   bind(this, this.onMouseUp),   false );
        
        // Touch Events
        this.canvas.addEventListener( 'touchstart', bind(this, this.onTouchStart), false );
        this.canvas.addEventListener( 'touchmove',  bind(this, this.onTouchMove),  false );
        this.canvas.addEventListener( 'touchend',   bind(this, this.onTouchEnd),   false );
        
        // Gesture Events
        this.canvas.addEventListener( 'gesturestart', bind(this, this.onGestureStart), false );
        this.canvas.addEventListener( 'gesturechange', bind(this, this.onGestureChange), false );
        this.canvas.addEventListener( 'gestureend', bind(this, this.onGestureEnd), false );
        
        // Mouse Wheel Events: Need both for WebKit and FF
        this.canvas.addEventListener('DOMMouseScroll', bind(this, this.onMouseWheel), false);
        this.canvas.addEventListener('mousewheel',     bind(this, this.onMouseWheel), false);
        
        // Keypress events
        document.addEventListener( "keypress" , bind(this, this.onKeyPress), false);
    }
 
    /////////////////////////
    // Mouse Events
    /////////////////////////
    this.onMouseDown = function(event)
    {
        this.mouseDown(event, this.mouseMoveFactor);
    }
        
    this.onMouseMove = function(event)
    {
        this.mouseMove(event, this.mouseMoveFactor);
    }
    
    this.onMouseUp = function(event)
    {
        this.mouseUp(event, this.mouseMoveFactor, this.mouseLineDistance, this.mouseParticleDistance);
    }
         
    this.onMouseOver = function(event)
    {
        event.preventDefault();
        this.mouse.over = true;
    }
    
    this.onMouseOut = function(event)
    {
        event.preventDefault();
        this.mouse.down = false;
        this.mouse.over = false;
    }
    
    this.onMouseWheel = function(event)
    {          
        // Get wheel direction for both WebKit or FF
        var delta = ((typeof event.wheelDelta != "undefined") ? (-event.wheelDelta) : event.detail );
        this.zoom(delta);
    }
    
    /////////////////////////
    // Touch Events
    /////////////////////////
    this.onTouchStart = function(event)
    {    
        this.mouseDown(event, this.touchMoveFactor);
    }
    
    this.onTouchMove = function( event )
    {   
        this.mouseMove(event, this.touchMoveFactor);
    }
    
    this.onTouchEnd = function(event)
    {
        this.mouseUp(event, this.touchMoveFactor, this.touchLineDistance, this.touchParticleDistance);
    }
    
    /////////////////////////
    // Move Actions
    /////////////////////////
    this.mouseDown = function (event, mouseSpeed)
    {
        event.preventDefault();
        this.updateMouse(event);
        this.mouse.down=true;
        this.mouse.start.x = this.mouse.last.x = this.mouse.screen.x;
        this.mouse.start.y = this.mouse.last.y = this.mouse.screen.y;        
    }
    
    this.mouseMove = function(event, mouseSpeed)
    {
        event.preventDefault();
    
        if (this.mouse.down)
        {
            this.updateMouse(event);
         
            var speed = (this.camera.fov/this.fovmax) * mouseSpeed;
            var rotationX = (this.mouse.screen.y - this.mouse.last.y) * TO_RADIANS * speed;
            var rotationY = (this.mouse.screen.x - this.mouse.last.x) * TO_RADIANS * speed;
            
            this.setCameraRotationDeltaXY(rotationX, rotationY);
                
            this.mouse.last.x = this.mouse.screen.x;
            this.mouse.last.y = this.mouse.screen.y;
        }
        
        // Check for Object Selection
        //var hits = this.getSelected();
    }
        
    this.selectStats = {};
    this.mouseUp = function(event, mouseSpeed, lineDistance, particleDistance)
    {
        event.preventDefault();        
        this.mouse.down=false;
        
        // Stop Rotation if User clicked single point
        if (this.mouse.last.x == this.mouse.start.x &&
            this.mouse.last.y == this.mouse.start.y)
        {
            this.setCameraRotationDeltaXY(0, 0); // Stop any spinning rotation
            
            // Debug Info on mouse click mouse
            this.dumpMouse();   
         
            // Check for Object Selection
            var start = new Date().getTime(); 
            var selected = this.getSelected(lineDistance, particleDistance);
            this.selectStats.getSelected = new Date().getTime() - start;
         
            // Send Hits Event
            if (selected && selected.length > 0) 
            {
                var start = new Date().getTime(); 
                this.sendAstroViewSelectEvent(selected);
                this.selectStats.sendAstroViewEvent = new Date().getTime() - start;
            }
         
            console.log("SELECT STATS: " + ASTROVIEW.toJson(this.selectStats));
        }
    }
    
    /////////////////////////
    // updateMouse()
    /////////////////////////  
    this.updateMouse = function(event)
    {  
        // Event ====> Screen (local component coords)
        this.eventToScreen(event, this.mouse.screen);

        // Screen ===> World (Normalized Unit Sphere)
        this.screenToNormal(this.mouse.screen, this.mouse.normal);

        // World (Normalized) ===> Graphics (Scaled to Graphics Radius)
        this.mouse.graphics.copy(this.mouse.normal).multiplyScalar(ASTROVIEW.RADIUS_GRAPHICS);      

        // World (Normalized) ===> World (Scaled to Diamond Radius)
        this.mouse.world.copy(this.mouse.normal).multiplyScalar(ASTROVIEW.RADIUS_SPHERE);

        // World (Normalized) ===> RA/DEC
        this.worldToRaDec(this.mouse.normal, this.mouse);

        // RA/DEC ===> Sexagesimal (String Representation)
        this.mouse.sra = this.degToHMS(this.mouse.ra);
        this.mouse.sdec = this.degToDMS(this.mouse.dec);
    }
    
    this.eventToScreen = function(event, screen)
    {    
        if (event.touches && event.touches.length > 0) // Works in Safari on iPad/iPhone
        {   
            var e = event.touches[ 0 ];
            var rect = (this.canvas.getBoundingClientRect ? this.canvas.getBoundingClientRect() : null);

            if (e.clientX && e.clientY && rect)
            {
                screen.set( e.clientX - rect.left, 
                            e.clientY - rect.top,
                            1.0);
            }
            else
            {
                console.error("CameraController:eventToScreen() Unable to determine canvas coordinates using event.touches[]");
            }
        }
        else if (event.offsetX && event.offsetY) // Works in Chrome / Safari (except on iPad/iPhone)
        {  
            screen.set(event.offsetX, event.offsetY, 1.0);
        }
        else if (event.layerX && event.layerY) // Works in Firefox
        {  
            screen.set(event.layerX, event.layerY, 1.0);
        }
        else
        {
            console.error ("CameraController:eventToScreen() Unable to determine canvas coordinates using event.");
        }
    }
    
    /////////////////////////
    // Keyboard Events
    /////////////////////////
    this.onKeyPress = function(event)
    {
        if (this.mouse.over)
        {
            var unicode=event.keyCode? event.keyCode : event.charCode;
            //alert("onKeyPress: " + unicode); // find the char code        
            switch(unicode)
            {
                case 37:    this.rotateRight(); break;
                case 39:    this.rotateLeft(); break;
                case 38:    this.rotateDown(); break;
                case 40:    this.rotateUp(); break;
                case 105:   this.zoomIn(this.mouseZoomInFactor); break;
                case 111:   this.zoomOut(this.mouseZoomOutFactor); break;
            }
        }
    }
       
    /////////////////////////
    // Gesture Events
    /////////////////////////
    this.onGestureStart = function( event )
    {
        if (event.scale)
        {
            event.preventDefault();          
            this.mouse.scale = event.scale;
        }
    }
    
    this.onGestureChange = function( event )
    {
        if (event.scale)
        {    
            event.preventDefault();

            var scaleDelta = event.scale - this.mouse.scale;
            if (scaleDelta > 0)
            {
                this.zoomIn(scaleDelta * this.touchZoomInFactor);
            }
            else if (scaleDelta < 0)
            {
                this.zoomOut(-scaleDelta * this.touchZoomOutFactor);
            }
         
            this.mouse.scale = event.scale;
        }
    }
    
    this.onGestureEnd = function( event )
    {
        if (event.scale)
        {
            event.preventDefault();
         
            var scaleDelta = event.scale - this.mouse.scale;
            if (scaleDelta > 0)
            {
                this.zoomIn(scaleDelta * this.touchZoomInFactor);
            }
            else if (scaleDelta < 0)
            {
                this.zoomOut(-scaleDelta * this.touchZoomOutFactor);
            }
         
            this.mouse.scale = event.scale;
        }
    }
    
    /////////////////////////
    // dumpMouse()
    /////////////////////////  
    this.dumpMouse = function()
    {
       console.debug("[screen x,y: " + this.mouse.screen.x + "," + this.mouse.screen.y + "]" + 
                     " [world x,y,z: " + this.mouse.world.x.toFixed(3) + "," + this.mouse.world.y.toFixed(3) + "," + this.mouse.world.z.toFixed(3) + "]" +
                     " [ra,dec: " + this.mouse.ra.toFixed(3) + "," + this.mouse.dec.toFixed(3) + "]" +
                     " [sra,sdec: " + this.mouse.sra + "," + this.mouse.sdec + "]" );
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
        (delta > 0 ? this.zoomOut(this.mouseZoomOutFactor) : this.zoomIn(this.mouseZoomInFactor));
    }
    
    this.zoomIn = function(zoomFactor)
    {
        var deltaFov = this.cameraFov * zoomFactor;
        this.cameraFov -= deltaFov;
        this.cameraFov = (this.cameraFov < this.fovmmin ? this.fovmmin : this.cameraFov);
    }
    
    this.zoomOut = function(zoomFactor)
    {
        var deltaFov = this.cameraFov * zoomFactor;
        this.cameraFov += deltaFov;
        this.cameraFov = (this.cameraFov > this.fovmax ? this.fovmax : this.cameraFov);
    }
     
    this.moveTo = function(ra, dec, zlevel)
    {        
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
    // updateController()
    /////////////////////////
    this.updateController = function()
    {
        var cameraChanged = this.updateCamera();
        var sizeChanged = this.updateSize();
        this.updateFrustumMatrix();
        this.updateZoomLevel();
        this.updateVisibleLevel();
        return (cameraChanged || sizeChanged);
    }
 
    /////////////////////////
    // updateCamera()
    /////////////////////////  
    this.cameraPosition = "";
    this.updateCamera = function()
    {  
        // Bounds Check on RotationX (Declination)
        if (this.cameraRotationX > RADIANS_90) this.cameraRotationX = RADIANS_90;
        if (this.cameraRotationX < -RADIANS_90) this.cameraRotationX = -RADIANS_90;
        
        // Update the Rotation
        if (Math.abs(this.camera.rotation.x - this.cameraRotationX) > .001)
        {
            this.camera.rotation.x += (this.cameraRotationX - this.camera.rotation.x) * 0.5;
        }
        else
        {
            this.camera.rotation.x = this.cameraRotationX;
        }
        
        if (Math.abs(this.camera.rotation.y - this.cameraRotationY) > .001)
        {
            this.camera.rotation.y += (this.cameraRotationY - this.camera.rotation.y) * 0.5;
        }
        else
        {
            this.camera.rotation.y = this.cameraRotationY;
        }
           
        // Update the Field of View
        if (Math.abs(this.camera.fov - this.cameraFov) > .001)
        {
            this.camera.fov += (this.cameraFov - this.camera.fov) * 0.5;
            this.camera.updateProjectionMatrix();
        }
        else
        {
            this.camera.fov = this.cameraFov;
        }
     
        // Check if camera has changed since last update
        var cameraPosition = this.camera.rotation.x + ":" +
                            this.camera.rotation.y + ":" +
                            this.camera.fov;
        var cameraChanged = (cameraPosition != this.cameraPosition);
        this.cameraPosition = cameraPosition;
     
        // Return if the Scene Viewport has changed
        return (cameraChanged);
    }
    
    /////////////////////////
    // updateSize 
    /////////////////////////   
    this.parentNode = null; 
    this.updateSize = function( )
    {    
        var sizeChanged = false;
        var width=-1; var height=-1;

        // Get the first Canvas Parent that contains a width and height value (only need to do this once)
        if (!this.parentNode)
        {     
            var p = this.canvas.parentNode;
            while (p)
            {  
                if (p.style && p.style.width && p.style.height)
                {
                    this.parentNode = p;
                    break;
                }
                p = p.parentNode;       
            }
        }

        // Get parent node's width and height
        if (this.parentNode)
        {
            width = parseInt(this.parentNode.style.width);  // removes 'px' on the end
            height = parseInt(this.parentNode.style.height); // removes 'px' on the end
        }

        // Use the window if all else fails
        if (width === -1 && height === -1)
        {
            width = window.innerWidth;
            height = window.innerHeight;
        }

        // Update ALL the canvas elements with new width and height     
        if (width != this.canvas.width || height != this.canvas.height)
        {   
            if (this.renderers && this.renderers.length > 0)
            {
                for (var i=0; i<this.renderers.length; i++)
                {
                    this.renderers[i].setSize(width, height);
                }
            }

            this.camera.aspect = width/height;
            this.camera.updateProjectionMatrix();

            sizeChanged = true;
        }

        return sizeChanged;
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
    // ZoomLevel()
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
            console.debug("AstroView: Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel);
        }
    }
 
    this.getZoomLevel = function()
    {
        return this.zlevel;
    }
 
    ///////////////////////////
    // VisibleLevel()
    ///////////////////////////
    this.updateVisibleLevel = function()
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
            console.debug("AstroView: Zoom Level: " + this.zlevel + " Visible Level: " + this.vizlevel);
        }
    }
 
    this.getVisibleLevel = function()
    {
        return this.vizlevel;
    }
 
    /////////////////////////
    // getSelected()
    /////////////////////////  
    this.getSelected = function(lineDistance, particleDistance)
    { 
        var selected = null;
        
        // Calculate the Distance from 3D mouse point on sphere to each line segment
        if (this.scene && this.scene.children)
        {
            for (var i=0; i<this.scene.children.length; i++)
            {
                var layer = this.scene.children[i];
                if (layer instanceof ASTROVIEW.GraphicsLayer)
                {
                    var hits = layer.getHits(this.mouse, this, lineDistance, particleDistance);
                    console.log ("CameraController: getSelected() layer : " + layer.name + " hits[] " + (hits ? hits.length : 0));
                    if (hits && hits.length)
                    {  
                        if (selected == null) selected = [];
                        for (var h=0; h<hits.length; h++)
                        {
                            selected.push(hits[h]);
                        }  
                    }
                }
            }
        }
     
        return selected;
    }
    
    /////////////////////////////
    // sendAstroViewSelectEvent()
    /////////////////////////////
    this.sendAstroViewSelectEvent = function(hits)
    {  
        var dict = {};
        var layers = null;
        for (var i=0; i<hits.length; i++) 
        {
            var hit = hits[i];
            if (hit && hit.userData && hit.userData.name && hit.userData.rows)
            {
                var layer = dict[hit.userData.name];
                if (layer && layer.rows)  // Found existing layer, add the rows from the hit item.
                {   
                    for (var r=0; r<hit.userData.rows.length; r++)
                    {
                        layer.rows.push(hit.userData.rows[r]);
                    }
                }   
                else // New layer item, add it to our layers list
                {   
                    layer = hit.userData;                       
                    dict[layer.name] = layer;
                    if (layers == null) layers = [];
                    layers.push(layer);
                }
            }
        }
     
        //
        // Notify AstroView of the Selected Layers
        //
        if (layers && this.av)
        {
            this.av.sendEvent('AstroView.Objects.Selected', layers);
        }
    }   
     
    this.tempv = new THREE.Vector3();   // Temp Vector used below
    this.screenToNormal = function(screen, normal)
    {
        this.tempv.set( (screen.x/this.canvas.width)*2-1,
                       -(screen.y/this.canvas.height)*2+1, 
                        1.0);
        this.projector.unprojectVector(this.tempv, this.camera);
        this.tempv.normalize();  
        normal.copy(this.tempv);  
    }
    
    this.worldToScreen = function(world, screen)
    {
        this.tempv.copy(world).normalize();
        this.normalToScreen(this.tempv, screen);
    }
    
    this.normalToScreen = function(normal, screen)
    {
        this.tempv.copy(normal);
        this.projector.projectVector(this.tempv, this.camera);
        screen.x = Math.round((this.tempv.x + 1)/2.0 * this.canvas.width);
        screen.y = Math.round(-(this.tempv.y - 1)/2.0 * this.canvas.height);
        screen.z = 0.0;
    }
    
    this.worldToRaDec = function(world, coord)
    {
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

/////////////////////////
// Diamond
/////////////////////////
ASTROVIEW.Diamond = function( geometry, material )
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
    this.defaulturl = ASTROVIEW.DefaultUrl;
 
    // Image Texture Properties
    this.texture = null;
};

ASTROVIEW.Diamond.prototype = new THREE.Mesh();
ASTROVIEW.Diamond.prototype.constructor = ASTROVIEW.Diamond;

/////////////////////////
// Constants
/////////////////////////
ASTROVIEW.DefaultUrl = "../AstroView/Diamond.png";
ASTROVIEW.DefaultMaterial = new THREE.MeshBasicMaterial( { map: THREE.ImageUtils.loadTexture(ASTROVIEW.DefaultUrl) } );

/////////////////////////
// renderScene()
/////////////////////////
ASTROVIEW.Diamond.prototype.renderScene = function( av )
{   
    this.av = av;
    var frustum = av.controller.frustum;
    var vizlevel = av.controller.getVisibleLevel();
 
    // Update Visibility if we are in the Viewing Frustum
    this.visible = this.inFrustum(frustum);
    if (this.visible)
    {    
        if (vizlevel == this.geometry.zlevel)
        {    
            //
            // CASE I: We are in the Frustum, my level is active, Load my image, Remove any Children (for Zoom Out)
            //
                 
            // If survey url has changed, load new image
            if (av.baseurl !== this.baseurl)
            {
                var url = new String(av.baseurl);
                var dg = this.geometry; // DiamondGeometry  
                this.imageurl = url.replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty).replace("[LEVEL]", dg.zlevel).replace("[TX]", dg.tx).replace("[TY]", dg.ty);      
                this.cleanTexture(av);
                this.loadTexture(this.imageurl, av );
                this.baseurl = av.baseurl;
            }
     
            // Remove all Children below current vizible Level
            if (this.children.length > 0)
            {
                this.removeDiamondChildren(av);
            }
        }
        else if (vizlevel > this.geometry.zlevel)
        {
            //
            // CASE II: We are in the Frustum, Zoom In Occured, Expand my Children, renderScene Children
            //
            if (this.children.length == 0)
            {
                this.expandDiamondChildren();
            }
                     
            // renderScene Children
            if (this.children.length > 0)
            {
                this.renderDiamondChildren(av);
            }
        }
     
        // Update Material Opacity based on active Zoom Level
        if (ASTROVIEW.DiamondTimerCount == 0)
        {
            this.setOpacity(vizlevel == this.geometry.zlevel);
        }
    }
    else    
    {
        //
        // CASE III: We are Not in the Viewing Frustum, Remove all Children,
        //
        if (this.children.length > 0)
        {
            this.removeDiamondChildren(av);
        }
    }
};

ASTROVIEW.Diamond.prototype.loadTexture = function(url)
{    
    // Create Texture from Image
    this.texture = new THREE.Texture( new Image() );

    this.texture.image.onload = bind(this, this.onLoad);
    this.texture.image.onerror = bind(this, this.onError);
    
    this.texture.image.crossOrigin = '';
    this.texture.image.src = url;
}
    
ASTROVIEW.Diamond.prototype.onLoad = function(event)
{    
    // Create Material from Texture
    this.material = new THREE.MeshBasicMaterial( { map: this.texture, overdraw: true } );
    this.texture.needsUpdate = true; 
    
    // Request full render
    this.av.render();
}

ASTROVIEW.Diamond.prototype.onError = function(event)
{
    console.debug(this.name + " *** onError() url: " + this.texture.image.src);
  
    // Create Blank Texture from Default Image Url
    this.loadTexture(this.defaulturl);
    
    // Request full render
    this.av.render();
}

ASTROVIEW.Diamond.prototype.setOpacity = function(visible)
{   
    if (this.material) 
    {
        this.material.opacity = (visible ? 1.0 : 0.0);
    }
}

ASTROVIEW.Diamond.prototype.expandDiamondChildren = function()
{
    // expandGeometry (depth, zlevel, radius)
    var geoms = this.geometry.expandDiamond();
    for (i=0; i<geoms.length; i++)
    {
        var ddd = new ASTROVIEW.Diamond( geoms[i] );
        this.add(ddd);
    }
};

ASTROVIEW.Diamond.prototype.renderDiamondChildren = function( av )
{
    for (var i=0; i<this.children.length; i++)
 {
     var ddd = this.children[i];
     ddd.renderScene(av);
 }
};

ASTROVIEW.Diamond.prototype.removeDiamondChildren = function( av )
{
    var names = "";
    while (this.children.length > 0)
    {
        var ddd = this.children[0];
        if (ddd.children.length > 0) ddd.removeDiamondChildren(av);
        names += ddd.name + " ";
        ddd.clean(av);
        this.remove(ddd);   // NOTE: Decrements this.children.length
    }
    this.children = [];
    //console.debug(this.name + " Removed Children Diamonds : " + names);
};

ASTROVIEW.Diamond.prototype.clean = function( av )
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

ASTROVIEW.Diamond.prototype.cleanTexture = function( av )
{        
    // Texture is created from Image
    if (this.texture)
    {
        // Remove texture from WebGL Graphics Memory 
        if (av && av.renderer && av.renderer instanceof THREE.WebGLRenderer)
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

ASTROVIEW.Diamond.prototype.inFrustum = function( frustum )
{
    return this.geometry.inFrustum(frustum);
}

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
            var v = uv[i].clone();
            v.multiplyScalar(radius);   
            this.vertices.push(v);
        }
    }
    
    if (this.vertices && this.vertices.length == 4)
    {
        var fuv = this.getFaceUV(quadrant);      // get Face uv[] array based on quadrant
        this.createFaces(this.vertices, fuv);    // create this.faces[] based on this.vertices[] and Face UV[]
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
};

//////////////////////////
// getDiamondCenter()
//////////////////////////
ASTROVIEW.DiamondGeometry.prototype.getDiamondCenter = function()    
{
    if (!this.gcenter)
    {
        if (!this.vertices || this.vertices.length !== 4) return null;
     
        var v0 = this.vertices[0];
        var v1 = this.vertices[1];
        var v2 = this.vertices[2];
        var v3 = this.vertices[3];
     
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
     
        var v0 = this.vertices[0];
        var v1 = this.vertices[1];
        var v2 = this.vertices[2];
        var v3 = this.vertices[3];
     
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
    // THREE.Face3
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
        var v0 = this.vertices[face.a];
        var v1 = this.vertices[face.b];
        var v2 = this.vertices[face.c];

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
        var v3=this.vertices[i];
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
    if (!radius) radius = this.radius - 10;
    
    //
    //      Expand each Diamond by number of 'depth' passed in, starting at 'zlevel'
    //
    //      NOTE: Diamond.uv[4] vector is the 4 corners of the diamond on the Unit Sphere
    // 
    //       Make new points on the Diamond (a, b, c, d, e):
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
            var dg = loopArray[i];   // ASTROVIEW.DiamondGeometry
            var uv0 = dg.uv[0];
            var uv1 = dg.uv[1];
            var uv2 = dg.uv[2];
            var uv3 = dg.uv[3];
            
            //
            //    Make new mid-points on the diamond AND Normalize them to the Unit Sphere
            //
            var a = new THREE.Vector3().add(uv0, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var b = new THREE.Vector3().add(uv1, uv2).multiplyScalar(0.5).normalize();   // THREE.Vector3 
            var c = new THREE.Vector3().add(uv2, uv3).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var d = new THREE.Vector3().add(uv3, uv0).multiplyScalar(0.5).normalize();   // THREE.Vector3
            var e = new THREE.Vector3().add(uv3, uv1).multiplyScalar(0.5).normalize();   // THREE.Vector3
            
            //
            //   Construct new diamonds
            //
            //    [uv0,  a,   e,   d]
            //    [ a, uv1,   b,   e]
            //    [ e,   b, uv2,   c]
            //    [ d,   e,   c, uv3]
            //
            var x=1;y=1;
            var dg0, dg1, dg2, dg3; // ASTROVIEW.DiamondGeometry
            switch (dg.quadrant)
            { 
                case "I":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, radius); // (x, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, radius); // (x, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, radius); // (0, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, radius); // (0, 0)
                    break;
                }
                case "II":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 0, radius); // (0, 0)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, radius); // (x, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 2, radius); // (x, y)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, radius); // (0, y)
                    break;
                }
                case "III":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, radius); // (0, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 1, radius); // (0, 0)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, radius); // (x, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 3, radius); // (x, y)
                    break;
                }
                case "IV":
                {
                    dg0 = new ASTROVIEW.DiamondGeometry([uv0, a,   e,   d], dg.tx*2+x, dg.ty*2+y, dg.quadrant, dg.color, zlevel, 0, radius); // (x, y)
                    dg1 = new ASTROVIEW.DiamondGeometry([a, uv1,   b,   e], dg.tx*2,   dg.ty*2+y, dg.quadrant, dg.color, zlevel, 1, radius); // (0, y)
                    dg2 = new ASTROVIEW.DiamondGeometry([e,   b, uv2,   c], dg.tx*2,   dg.ty*2,   dg.quadrant, dg.color, zlevel, 2, radius); // (0, 0)
                    dg3 = new ASTROVIEW.DiamondGeometry([d,   e,   c, uv3], dg.tx*2+x, dg.ty*2,   dg.quadrant, dg.color, zlevel, 3, radius); // (x, 0)
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
    var depth = ASTROVIEW.ZOOM_LEVEL_MIN-1;      // expand 3 more levels...
    var zlevel = ASTROVIEW.ZOOM_LEVEL_MIN;       // ...to zlevel '4'

    var daI = dgI.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);            // Quadrant I
    var daII = dgII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);          // Quadrant II
    var daIII = dgIII.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);        // Quadrant III
    var daIV = dgIV.expandDiamond(depth, zlevel, ASTROVIEW.RADIUS);          // Quadrant IV      
    
    //
    // STEP 4: Create Diamond objects from the DiamondGeometry[] array and
    //         add them as children to the DiamondSphere
    //
    this.createDiamondMeshMaterial(daI, null, zlevel); 
    this.createDiamondMeshMaterial(daII, null, zlevel);
    this.createDiamondMeshMaterial(daIII, null, zlevel);
    this.createDiamondMeshMaterial(daIV, null, zlevel);
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
    var ddd = new ASTROVIEW.Diamond( dg, material );
    this.add(ddd);
}

/////////////////////////////
// renderScene()
/////////////////////////////
ASTROVIEW.DiamondSphere.prototype.renderScene = function( av )
{
    if (this.children && this.children.length > 0)
    {
        for (var i=0; i<this.children.length; i++)
        {
            var ddd = this.children[i]; // ASTROVIEW.Diamond
            if (ddd instanceof ASTROVIEW.Diamond)
            {
                ddd.renderScene(av);
            }
        }
    }
}

/////////////////////////////
// GraphicsLayer
/////////////////////////////
ASTROVIEW.GraphicsLayer = function (av, layerData)
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
        this.av = av;
        this.layerData = layerData;
        this.renderer = av.renderers[0];
        
        this.lid = ASTROVIEW.lid++;
        this.name = (layerData.name ? layerData.name : "GraphicsLayer_" + this.lid); 
        
        // Ensure LayerData Object is correctly linked to Us for future reference.
        this.layerData.lid = this.lid;
        this.layerData.name = this.name;
         
        // Graphics Objects
        this.material = null;
        this.particles = null;

        // Initialize Temporary Points used for determining Point to Line distances below
        this.A = {};
        this.A.world = new THREE.Vector3();
        this.A.screen = new THREE.Vector3();

        this.B = {};
        this.B.world = new THREE.Vector3();
        this.B.screen = new THREE.Vector3();

        this.minx = null;
        this.maxx = null;
        this.miny = null;
        this.maxy = null;
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
ASTROVIEW.GraphicsLayer.DEFAULT_SIZE = 31;
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
 
    // Remove all Object References that we created
    this.av = undefined;
    this.layerData = undefined;
    this.renderer = undefined;

    this.material = undefined;
    this.particles = undefined;

    this.A = undefined;
    this.B = undefined;

    this.minx = undefined;
    this.maxx = undefined;
    this.miny = undefined;
    this.maxy = undefined;
}

/////////////////////////////
// alert()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.alert = function(msg)
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
ASTROVIEW.GraphicsLayer.prototype.createPolyline = function(polyline, userData)
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
            var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
            geometry.vertices.push(v);
        } // end for each coord position
        
        // Close the Polygon
        var v = geometry.vertices[0].clone();
        geometry.vertices.push(v);
            
        // Create new Line from the geometry and material
        var line = new THREE.Line( geometry, this.material, THREE.LineStrip );
        line.userData = userData;
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
ASTROVIEW.GraphicsLayer.prototype.createCircle = function(circle, userData)
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
        line.userData = userData;
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
        var v = this.raDecToVector3(x, y, ASTROVIEW.RADIUS_GRAPHICS);
        geometry.vertices.push(v);
    }
}

/////////////////////////////
// createParticle()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticle = function(ra, dec, userData)
{
    if (this.renderer instanceof THREE.WebGLRenderer)
    {
        this.createParticleWebGL(ra, dec, userData);
    }
    else if (this.renderer instanceof THREE.CanvasRenderer)
    {
        this.createParticleCanvas(ra, dec, userData);
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
ASTROVIEW.GraphicsLayer.prototype.createParticleWebGL = function(ra, dec, userData)
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

    // Convert each RA/DEC coord to a Vertex and add to the Geometry       
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
        v.userData = userData;
        this.particles.vertices.push(v);
    }
};

/////////////////////////////
// createParticleCanvas()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.createParticleCanvas = function(ra, dec, userData)
{
    // Create Canvas Particle Material
    if (!this.material)
    {
        this.material = this.createMaterialParticleCanvas(this.layerData.attribs);
    }
     
    // Convert each RA/DEC coord to a Vertex, create a Particle Object and add to the scene  
    if (ra != null && dec != null)
    {
        var v = this.raDecToVector3(ra, dec, ASTROVIEW.RADIUS_GRAPHICS);
        var p = new THREE.Particle( this.material );
        p.userData = userData;
        p.position = v;
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
                color: color});
            break;

        case 'square':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
            break;

        case 'plus':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.plus,
                transparent:true,
                color: color});
            break;

        case 'stop':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.stop,
                transparent:true,
                color: color});
            break;

        case 'diamond':
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.diamond,
                transparent:true,
                color: color});
            break;

        default:
            material = new THREE.ParticleBasicMaterial({
                size: size,
                map: ASTROVIEW.GraphicsLayer.square,
                transparent:true,
                color: color});
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
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.9;
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
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    //context.strokeRect(x, y, width, height)
    context.strokeRect( -3, -3, 7, 7);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -3,  0);
    context.lineTo(  3,  0);
    context.moveTo(  0,  3);
    context.lineTo(  0, -3);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// diamond()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.diamond = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4, -1);
    context.lineTo( -4,  1);
    context.lineTo( -1,  4);
    context.lineTo(  1,  4);
    context.lineTo(  4,  1);
    context.lineTo(  4, -1);
    context.lineTo(  1, -4);
    context.lineTo( -1, -4);
    context.lineTo( -4, -1);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// stop()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.stop = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
    context.scale(scale, scale);
    context.lineWidth = 0.8;
    context.beginPath();
    context.moveTo( -4, -2);
    context.lineTo( -4,  2);
    context.lineTo( -2,  4);
    context.lineTo(  2,  4);
    context.lineTo(  4,  2);
    context.lineTo(  4, -2);
    context.lineTo(  2, -4);
    context.lineTo( -2, -4);
    context.lineTo( -4, -2);
    context.closePath();
    context.stroke();
};

/////////////////////////////
// plus()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.plus = function (context)
{
    var scale = (this.av ? this.av.camera.fov/ASTROVIEW.FOV_LEVEL_MAX : 1.0);
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
// raDecToVector3()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.raDecToVector3 = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vector3(x, y, z);
    return v;
};

/////////////////////////////
//
//     Object Selection
//
/////////////////////////////
// getHits()
/////////////////////////////
ASTROVIEW.GraphicsLayer.prototype.getHits = function(mouse, controller, lineDistance, particleDistance)
{
    // Validate input
    if (!controller || !mouse) return null;

    var hits = null;
    var d, distance;
    for (var i=0; i<this.children.length; i++)
    {
        var object = this.children[i];
        if (object instanceof THREE.Particle)
        {
            d = this.particleDistance2D(mouse, object, controller);
            distance = (particleDistance ? particleDistance : 6.0);
        }
        else if (object instanceof THREE.ParticleSystem)
        {
            d = this.particleDistanceSystem2D(mouse, object, controller);
            distance = (particleDistance ? particleDistance : 6.0);
        }
        else if (object instanceof THREE.Line)
        {
            d = this.lineDistance2D(mouse, object, controller);
            distance = (lineDistance ? lineDistance : 4.0);
        }

        if (d <= distance) 
        {
            if (hits == null) hits = [];
            hits.push(object);
        }
    }

    return hits;        
}

ASTROVIEW.GraphicsLayer.prototype.particleDistance2D = function (mouse, particle, controller)
{
    // Convert Particle World ===> Screen  (clear out Z)
    this.A.world = particle.position;
    controller.worldToScreen(this.A.world, this.A.screen);
    this.A.screen.z = 0;

    // Calculate the distance in Screen Coordinates
    var distance = mouse.screen.distanceTo(this.A.screen);
    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.particleDistanceSystem2D = function (mouse, ps, controller)
{
    var distance = 9999.9999;

    if (!ps || !ps.geometry || !ps.geometry.vertices || ps.geometry.vertices.length == 0) return null;

    var length = ps.geometry.vertices.length;
    for (var i=0; i<length; i++)
    {
        this.A.world = ps.geometry.vertices[i];
        controller.worldToScreen(this.A.world, this.A.screen);
        this.A.screen.z = 0;

        // Calculate the distance in Screen Coordinates
        var d = mouse.screen.distanceTo(this.A.screen);
        if (d < distance) distance = d;
    }

    return distance;
}

ASTROVIEW.GraphicsLayer.prototype.lineDistance2D = function (mouse, line, controller)
{
    var distance = 9999.9999;

    // validate input
    if (!line || !line.geometry || !line.geometry.vertices || line.geometry.vertices.length == 0) 
        return null;

    var vertices = line.geometry.vertices;
    for (var i=0; i<vertices.length-1; i++)
    {
        this.A.world = vertices[i];
        this.B.world = vertices[i+1];

        controller.worldToScreen(this.A.world, this.A.screen);
        controller.worldToScreen(this.B.world, this.B.screen);

        // Check if Line Segement Points are on top of each other (Length == 0)
        var d;
        if (this.A.screen.x == this.B.screen.x && this.A.screen.y == this.B.screen.y)
        {
            d = mouse.screen.distanceTo(this.A.screen);
        }
        else // Line Segment Length > 0
        {
            var inside = this.insideBoundingBox(mouse.screen, this.A.screen, this.B.screen);
            if (inside)
            {
                d = this.lineDistancePoints2D(mouse.screen, this.A.screen, this.B.screen, controller);
            }
        }

        // Update minimum distance
        distance = (d < distance ? d : distance);
    }
    return distance;
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

ASTROVIEW.GraphicsLayer.prototype.lineDistancePoints2D = function (P, A, B, controller)
{
    // From Wikipedia: http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    var normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
    var distance = Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
    return distance;
}

ASTROVIEW.lid = 0;

/////////////////////////////
// CatalogLayer()
/////////////////////////////
ASTROVIEW.CatalogLayer = function ( av, layerData )
{ 
    ASTROVIEW.GraphicsLayer.call( this, av, layerData );

    if (layerData && layerData.rows && layerData.rows.length > 0)
    {
        this.createParticles(layerData.rows, layerData.name);
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
ASTROVIEW.CatalogLayer.prototype.createParticles = function(rows, name)
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
            var userData = {"rows" : [row], "name" : name};
            this.createParticle(ra, dec, userData); // invokes GraphicsLayer.createParticle()
        }
    }
}

/////////////////////////////
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
        var dict = [];  // Array of Footprints to prevent creating duplicates       
        for (var i=0; i<layerData.rows.length; i++)
        {
            var row = layerData.rows[i];
            if (row.hasOwnProperty("footprint"))
            {
                var footprint = row["footprint"];
             
                // Check for Duplicate Footprint
                if (dict[footprint])
                {
                    var userData = dict[footprint];
                    userData.rows.push(row);
                }
                else // New Footprint
                {
                    var userData = {"rows" : [row], "name" : layerData.name};
                    dict[footprint] = userData;
                    this.createFootprint(row, userData);
                }
            }
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
ASTROVIEW.FootprintLayer.prototype.createFootprint = function(row, userData)
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
    else if (fp.indexOf(ASTROVIEW.POSITION_ICRS) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_ICRS_RE);
        type = ASTROVIEW.POSITION_ICRS;
    }
    else if (fp.indexOf(ASTROVIEW.POSITION_J2000) >= 0)
    {
        fplist = fp.split(ASTROVIEW.POSITION_J2000_RE);
        type = ASTROVIEW.POSITION_J2000;
    }
    
    // If we found no valid fplist, bail
    if (fplist == null || fplist.length < 2)
    {
        this.alert("Illegal Footprint:" + fp + "\n\nExpected Syntax: \n\n POLYGON [J2000|ICRS] [ra dec]\n CIRCLE [J2000|ICRS] [ra dec]\n POSITION [J2000|ICRS] [ra dec]", "Illegal Footprint");
        return;
    }
    
    switch (type)
    {
        case ASTROVIEW.POLYGON_J2000:
        case ASTROVIEW.POLYGON_ICRS:
        {
            this.createPolygons(fplist, userData);
            break;
        }
        case ASTROVIEW.CIRCLE_J2000:
        case ASTROVIEW.CIRCLE_ICRS:
        {
            this.createCircles(fplist, userData);
            break;
        }
        case ASTROVIEW.POSITION_J2000:
        case ASTROVIEW.POSITION_ICRS:
        {
            this.createPoints(fplist, userData);
            break;
        }
        default:
        {
            this.alert("Footprint Syntax Error\n\n   Valid Syntax: 'POLYGON J2000|ICRS [values]' 'POSITION J2000|ICRS [values]' or 'CIRCLE J2000|ICRS [values]'\n\n" + sSTCS, "Unable to Create Footprint");
        }
    }
};

/////////////////////////////
// createPolygons()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPolygons = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Line Shape for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var polyline = trim(fplist[i]);
        if (polyline.length > 0)
        {
            this.createPolyline(polyline, userData);     // GraphicsLayer.createPolyline()
        }
    }
}

/////////////////////////////
// createCircles()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createCircles = function(fplist, userData)
{
    //
    // We have a valid polygon array...lets create the 3D Circle for each polygon
    //
    for (var i=0; i<fplist.length; i++)
    {
        var circle = trim(fplist[i]);
        if (circle.length > 0)
        {
            this.createCircle(circle, userData);         // GraphicsLayer.createCircle()
        }
    }
}

/////////////////////////////
// createPoints()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoints = function(fplist, userData)
{
    for (var i=0; i<fplist.length; i++)
    {
        var point = trim(fplist[i]);
        if (point.length > 0)
        {
            this.createPoint(point, userData);
        }
    }
}

/////////////////////////////
// createPoint()
/////////////////////////////
ASTROVIEW.FootprintLayer.prototype.createPoint = function(point, userData)
{
    var coords = point.split(/\s+/);
    if (coords.length > 0 && coords.length%2 == 0)
    {
        for (var j=0; j<coords.length; j+=2)
        {
            // Extract RA and DEC from coord Array
            var ra = coords[j];
            var dec = coords[j+1];
            this.createParticle(ra, dec, userData);     // GraphicsLayer.createParticle()
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

ASTROVIEW.POSITION_J2000 = "POSITION J2000";
ASTROVIEW.POSITION_ICRS  = "POSITION ICRS";

ASTROVIEW.POSITION_J2000_RE = new RegExp(ASTROVIEW.POSITION_J2000 + "\\s+");
ASTROVIEW.POSITION_ICRS_RE = new RegExp(ASTROVIEW.POSITION_ICRS + "\\s+");

/////////////////////////////
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
                    var v = this.raDecToVector3(ra, dec, this.radius);
                    geometry.vertices.push(v);
                } // end for each coord position
                
                // Close the Polygon
                var v = geometry.vertices[0].clone();
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
     
ASTROVIEW.ImageLayer.prototype.raDecToVector3 = function(ra, dec, radius)
{
    var decRadians = dec*TO_RADIANS;
    var raRadians = ra*TO_RADIANS;
    var r = Math.cos(decRadians)*radius;
    
    var y = Math.sin(decRadians)*radius;
    var x = -Math.sin(raRadians)*r;
    var z = -Math.cos(raRadians)*r;

    var v = new THREE.Vector3(x, y, z);
    return v;
}

// stats.js r8 - http://github.com/mrdoob/stats.js
var Stats=function(){var h,a,n=0,o=0,i=Date.now(),u=i,p=i,l=0,q=1E3,r=0,e,j,f,b=[[16,16,48],[0,255,255]],m=0,s=1E3,t=0,d,k,g,c=[[16,48,16],[0,255,0]];h=document.createElement("div");h.style.cursor="pointer";h.style.width="80px";h.style.opacity="0.9";h.style.zIndex="10001";h.addEventListener("mousedown",function(a){a.preventDefault();n=(n+1)%2;n==0?(e.style.display="block",d.style.display="none"):(e.style.display="none",d.style.display="block")},!1);e=document.createElement("div");e.style.textAlign=
"left";e.style.lineHeight="1.2em";e.style.backgroundColor="rgb("+Math.floor(b[0][0]/2)+","+Math.floor(b[0][1]/2)+","+Math.floor(b[0][2]/2)+")";e.style.padding="0 0 3px 3px";h.appendChild(e);j=document.createElement("div");j.style.fontFamily="Helvetica, Arial, sans-serif";j.style.fontSize="9px";j.style.color="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";j.style.fontWeight="bold";j.innerHTML="FPS";e.appendChild(j);f=document.createElement("div");f.style.position="relative";f.style.width="74px";f.style.height=
"30px";f.style.backgroundColor="rgb("+b[1][0]+","+b[1][1]+","+b[1][2]+")";for(e.appendChild(f);f.children.length<74;)a=document.createElement("span"),a.style.width="1px",a.style.height="30px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+b[0][0]+","+b[0][1]+","+b[0][2]+")",f.appendChild(a);d=document.createElement("div");d.style.textAlign="left";d.style.lineHeight="1.2em";d.style.backgroundColor="rgb("+Math.floor(c[0][0]/2)+","+Math.floor(c[0][1]/2)+","+Math.floor(c[0][2]/2)+")";d.style.padding=
"0 0 3px 3px";d.style.display="none";h.appendChild(d);k=document.createElement("div");k.style.fontFamily="Helvetica, Arial, sans-serif";k.style.fontSize="9px";k.style.color="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";k.style.fontWeight="bold";k.innerHTML="MS";d.appendChild(k);g=document.createElement("div");g.style.position="relative";g.style.width="74px";g.style.height="30px";g.style.backgroundColor="rgb("+c[1][0]+","+c[1][1]+","+c[1][2]+")";for(d.appendChild(g);g.children.length<74;)a=document.createElement("span"),
a.style.width="1px",a.style.height=Math.random()*30+"px",a.style.cssFloat="left",a.style.backgroundColor="rgb("+c[0][0]+","+c[0][1]+","+c[0][2]+")",g.appendChild(a);return{domElement:h,update:function(){i=Date.now();m=i-u;s=Math.min(s,m);t=Math.max(t,m);k.textContent=m+" MS ("+s+"-"+t+")";var a=Math.min(30,30-m/200*30);g.appendChild(g.firstChild).style.height=a+"px";u=i;o++;if(i>p+1E3)l=Math.round(o*1E3/(i-p)),q=Math.min(q,l),r=Math.max(r,l),j.textContent=l+" FPS ("+q+"-"+r+")",a=Math.min(30,30-l/
100*30),f.appendChild(f.firstChild).style.height=a+"px",p=i,o=0}}};

