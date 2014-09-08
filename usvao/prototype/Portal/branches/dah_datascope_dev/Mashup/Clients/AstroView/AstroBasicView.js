////////////////////////
// ASTROVIEW namespace
////////////////////////
var ASTROVIEW = ASTROVIEW || {};

////////////////////////
// Constants
////////////////////////
// Diamond Radius and Viewport Far Plane
ASTROVIEW.RADIUS           = 1000;
ASTROVIEW.FOV_LEVEL_MAX    = 30;

// redraw Rate
ASTROVIEW.TIMER_TICKS_ACTIVE = 20;
ASTROVIEW.TIMER_TICKS_IDLE   = 80;

// View State
ASTROVIEW.VIEW_STATE_ACTIVE = "ACTIVE";
ASTROVIEW.VIEW_STATE_IDLE = "IDLE";

// Set mobile browser
ASTROVIEW.MOBILE = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

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
this.timerTicks = undefined;
this.timerID = undefined;
startTimer = function(callback, ticks)
{
    if (this.timerID) window.clearTimeout(this.timerID); 
    this.timerID = window.setInterval(callback, ticks);
    this.timerTicks = ticks;
};

////////////////////////
// stopTimer()
////////////////////////
stopTimer = function(id)
{
    if (this.timerID) window.clearTimeout(this.timerID); 
    this.timerID = undefined;
    this.timerTicks = undefined;
};

// Proxy
proxy_url = "http://masttest.stsci.edu/portal/Mashup/MashupQuery.asmx/MashupTestHttpProxy?url=";

// DUMMY
healpix_dummy_url = "http://dummyimage.com/256x256/" + "ff0000" + "/000000%26text=%5B" + "[LEVEL]" + ":" + "[PIXEL12]" + "," + "[PIXEL]" + "%5D"; 
healpix_proxy_dummy_url = proxy_url + healpix_dummy_url;

toast_dummy_url = "http://dummyimage.com/256x256/" + "ff0000" + "/000000%26text=%5B" + "[LEVEL]" + ":" + "[TX]" + "," + "[TY]" + "%5D"; 
toast_proxy_dummy_url = proxy_url + toast_dummy_url;

// TOAST
toast_galex_url = "http://mastimg.stsci.edu/surveys/toast/galex/[LEVEL]/[TX]/galex_[LEVEL]_[TX]_[TY].jpg";
toast_dss_url = "http://mastimg.stsci.edu/surveys/toast/dss2/[LEVEL]/[TX]/dss2_[LEVEL]_[TX]_[TY].jpg";
toast_proxy_galex_url = proxy_url + toast_galex_url;
toast_proxy_dss_url = proxy_url + toast_dss_url;

// HEALPIX
healpix_dss_url = "http://mastimg.stsci.edu/surveys/healpix/dss/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
aladin_dss_url = "http://alasky.u-strasbg.fr/DssColor/Norder[LEVEL]/Dir[DIR]/Npix[PIXEL].jpg";
healpix_iras_url = "http://alasky.u-strasbg.fr/IRISColor/Norder3/Dir0/Npix[PIXEL].jpg";
healpix_proxy_iras_url = proxy_url + healpix_iras_url;
healpix_aladin_dss_url = proxy_url + aladin_dss_url;

//
// SURVEYS []
//
// TOAST 
ASTROVIEW.surveys = [];

ASTROVIEW.surveys["toast"] = {
    "type": "toast",
    "baseurl":  toast_dss_url, // test_url, //
    
    "zoomLevel": [{"fov":     30, "level":4},
                  {"fov":     20, "level":4},
                  {"fov":     10, "level":5},
                  {"fov":      5, "level":6},
                  {"fov":    2.5, "level":7},
                  {"fov":   1.25, "level":8},
                  {"fov":   0.75, "level":9},
                  {"fov":  0.355, "level":10},
                  {"fov":  0.125, "level":11},
                  {"fov": 0.0625, "level":12},
                  {"fov":0.00001, "level":12}]
}

// HEALPIX
ASTROVIEW.surveys["healpix"] = {
    "type": "healpix",
    "baseurl":  healpix_dss_url, 
    
    "zoomLevel": [{"fov":     30, "level":2},
                  {"fov":     20, "level":2},
                  {"fov":     15, "level":3},
                  {"fov":     10, "level":4},
                  {"fov":      5, "level":5},
                  {"fov":    2.5, "level":6},
                  {"fov":   1.25, "level":7},
                  {"fov":   0.75, "level":7},
                  {"fov":  0.355, "level":7},
                  {"fov":  0.125, "level":7},
                  {"fov": 0.0625, "level":7},
                  {"fov":0.00001, "level":7}]
}

// ALADIN
ASTROVIEW.surveys["aladin"] = {
    "type": "healpix",
    "baseurl":  healpix_aladin_dss_url, 
    
    "zoomLevel": [{"fov":     30, "level":3},
                  {"fov":     23, "level":5},
                  {"fov":      9, "level":6},
                  {"fov":   0.75, "level":7},
                  {"fov":  0.250, "level":8},
                  {"fov": 0.0625, "level":9},
                  {"fov":0.00001, "level":9}]
}

// DUMMY
ASTROVIEW.surveys["test"] = {
    "type": "toast",
    "baseurl":  toast_proxy_dummy_url, 
           
   "zoomLevel": [{"fov":     30, "level":4},
                  {"fov":     20, "level":4},
                  {"fov":     10, "level":5},
                  {"fov":      5, "level":6},
                  {"fov":    2.5, "level":7},
                  {"fov":   1.25, "level":8},
                  {"fov":   0.75, "level":9},
                  {"fov":  0.355, "level":10},
                  {"fov":  0.125, "level":11},
                  {"fov": 0.0625, "level":12},
                  {"fov":0.00001, "level":12}]
}
     
////////////////////////
// AstroBasicView
////////////////////////
ASTROVIEW.AstroBasicView = function ( config )
{
    // Set Config Defaults, if Not specified
    if (!config) config = {};
    
    // Create element if not specified
    if (!config.div)
    {
        config.div = document.createElement('div');
        document.body.appendChild(config.div);
    }
    this.div = config.div;

    if (!config.rendertype)
    {
        config.rendertype = "canvas";
    }
    this.renderType = config.rendertype.toLowerCase();
    
    if (!config.surveytype)
    {
        config.surveytype = "toast";
    }
    
    // Validate Config Properties
    if (!ASTROVIEW.surveys[config.surveytype])
    {
        alert ("Unknown Survey Type: " + config.surveytype)
    }
    if (config.rendertype !== "canvas" && config.rendertype !== "webgl")
    {
        alert ("Unknown Render Type: " + config.rendertype)
    }
    
    // Save Config Properties
    this.avcontainer = (config.avcontainer ? config.avcontainer : null);
    this.debug = (config.debug ? config.debug : false);
    
    // Load initial survey
    this.survey = ASTROVIEW.surveys[config.surveytype];
        
    // Set Default Survey URL(s)
    this.baseurl = this.survey.baseurl;
       
    // Core 3D objects
    this.scene = null; 
    this.camera = null;
    this.canvas = null;
    this.controller = null;
    
    // Renderers
    this.renderers = null;
    this.diamondRenderer = null;
    this.graphicsRenderer = null;
    this.selectRenderer = null;
    this.crossRenderer = null;
    this.hoverRenderer = null;
   
    // Scenes
    this.diamondScene = null;
    this.graphicsScene = null;
    this.selectScene = null;
    this.crossScene = null;
    this.scenes = null;
    
    // Scene Objects
    this.toastSphere = null;
    this.healpixSphere = null;
    this.crossLayer = null;
    this.radecView = null;
   
    // Rendering Stats
    this.stats = null;

    /////////////////
    // createScene()
    /////////////////
    this.createScene = function() 
    {
        // Create Camera
        this.camera = new THREE.PerspectiveCamera(ASTROVIEW.FOV_LEVEL_MAX,
                                                  window.innerWidth / window.innerHeight,
                                                  1,
                                                  ASTROVIEW.RADIUS*10);
        this.camera.position.set( 0, 0, 0 );
        this.camera.eulerOrder = 'YXZ';
        this.camera.name = "PerspectiveCamera";
            
        // Create Renderers (one for each scene) 
        this.renderers = this.createRenderers(this.renderType);
        if (!this.renderers || this.renderers.length == 0)
        {
            alert ("AstroView: Unable to create renderer(s) of type: " + this.renderType);
            console.error("AstroView: Unable to create renderer(s) of type: " + this.renderType);
            return;
        }
        else
        {   
            // Set default canvas to the last renderer created
            var last = this.renderers.length-1;
            this.canvas = this.renderers[last].domElement;
        }
        
        // Create Scenes (one for each renderer) 
        this.scenes = this.createScenes(this.renderType);
        if (!this.scenes || this.scenes.length == 0 || !this.graphicsScene)
        {
            alert ("AstroView: Unable to create scenes(s) of type: " + this.renderType);
            console.error("AstroView: Unable to create scenes(s) of type: " + this.renderType);
            return;
        }
        else
        {   
            // Set default scene to the graphics Scene
            this.scene = this.graphicsScene;
        }

        // Create RA/DEC View, specifying mobile or desktop layout
        this.radecView = new ASTROVIEW.RaDecView(this.div, ASTROVIEW.MOBILE);     
        
        // Create Selection Point Layer
        this.crossLayer = this.createCrossLayer();
        
        // Create Camera Controller
        this.controller = new ASTROVIEW.CameraController( this );
                                                          
        // Create Stats View
        if (this.debug)
        {
            this.stats = new Stats();
            this.stats.domElement.style.position = 'absolute';
            this.stats.domElement.style.bottom = '4px';
            this.stats.domElement.style.left = '4px';
            this.div.appendChild(this.stats.domElement);
        }
        
        // Add Keyboard Icon [Debugging Only]
        if (this.debug)
        {
            this.keyboard = new Image();
            this.keyboard.id = 'keyboardImage';
            this.keyboard.src = '../../Clients/AstroView/keyboard.png';
            this.keyboard.style.position = 'absolute';
            this.keyboard.style.bottom = '4px';
            this.keyboard.style.right = '4px';
            this.keyboard.style.zIndex = 1000;
            this.keyboard.addEventListener( 'click',  bind(this, this.onKeyboardClick),  false );
            this.div.appendChild( this.keyboard );
        }
    }

    ////////////////////
    // createRenderers()
    ////////////////////
    this.createRenderers = function(rendertype)
    {     
        var renderers = [];
        
        //
        // (1) Create the DiamondRenderer
        //
        switch (rendertype)
        {
            // NOTE: We try to create a WebGL Renderer and fallback to the Canvas Renderer.   
            case "webgl":
            {
                try{
                    // Create WegGL Diamond Renderer
                    this.diamondRenderer = new THREE.WebGLRenderer();
                    this.diamondRenderer.setSize( window.innerWidth, window.innerHeight ); 
                    this.diamondRenderer.setPolygonOffset(true, 1.0, 1.0);
                    renderers.push(this.diamondRenderer);
                    
                    break;
                } catch(error) {this.renderType = "canvas";}    // Try to create Canvas Renderer as fallback
            }
            case "canvas":
            {  
                try{   
                    // Create Canvas Diamond Renderer
                    this.diamondRenderer = new THREE.CanvasRendererMobile();
                    this.diamondRenderer.setSize( window.innerWidth, window.innerHeight );
                    this.diamondRenderer.sortElements = false;
                    renderers.push(this.diamondRenderer);
                    
                    break;
                } catch(error) {this.diamondRenderer=null;}            
            }         
        }
        
        //
        // Create all the other layers:
        //
        // Graphics Layer
        // Select Layer
        // Cross Layer
        // Hover Layer
        //
        if (this.diamondRenderer != null)
        {           
            // (2) Create Graphics Renderer
            this.graphicsRenderer = new THREE.CanvasRendererMobile();
            this.graphicsRenderer.setSize( window.innerWidth, window.innerHeight );
            this.graphicsRenderer.autoClear = false;
            this.graphicsRenderer.sortElements = false;
            renderers.push(this.graphicsRenderer);
            
            // (3) Create Selection Renderer
            this.selectRenderer = new THREE.CanvasRendererMobile();
            this.selectRenderer.setSize( window.innerWidth, window.innerHeight );
            this.graphicsRenderer.autoClear = false;
            this.selectRenderer.sortElements = false;
            renderers.push(this.selectRenderer);
            
            // (4) Create Cross Renderer
            this.crossRenderer = new THREE.CanvasRendererMobile();
            this.crossRenderer.setSize( window.innerWidth, window.innerHeight );
            this.crossRenderer.autoClear = false;
            this.crossRenderer.sortElements = false;
            renderers.push(this.crossRenderer);
            
            // (5) Create Cross Renderer
            this.hoverRenderer = new THREE.CanvasRendererMobile();
            this.hoverRenderer.setSize( window.innerWidth, window.innerHeight );
            this.hoverRenderer.autoClear = false;
            this.hoverRenderer.sortElements = false;
            renderers.push(this.hoverRenderer);                 
        }
        
        //
        // Set Canvas Properties so they stack up in the browser as separate Layers
        //
        var zIndex = 1;
        if (this.diamondRenderer && this.diamondRenderer.domElement)
        {       
            var canvas = this.diamondRenderer.domElement;                     
            canvas.id = "DiamondCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas );      
        }
        
        if (this.graphicsRenderer && this.graphicsRenderer.domElement)
        {                           
            var canvas = this.graphicsRenderer.domElement;
            canvas.id = "GraphicsCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        if (this.selectRenderer && this.selectRenderer.domElement)
        {                           
            var canvas = this.selectRenderer.domElement;
            canvas.id = "SelectCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        if (this.crossRenderer && this.crossRenderer.domElement)
        {                           
            var canvas = this.crossRenderer.domElement;
            canvas.id = "CrossCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        if (this.hoverRenderer && this.hoverRenderer.domElement)
        {                           
            var canvas = this.hoverRenderer.domElement;
            canvas.id = "HoverCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.div.appendChild( canvas ); 
        }
        
        // Return all renderers created
        return renderers; 
    }
    
    this.createScenes = function(renderType)
    {
        var scenes = [];
        
        // 
        // NOTE: To work around (THREE.Object3D) unnecessary THREE behavior, we do the following UGLY Statement:
        //
        // this.camera.parent = undefined;
        //
        // THREE.Object3D wants to perform a removal of the camera from the previous Scene if camera.parent is (!=undefined).
        // So we temporarily set it to 'undefined' to avoid its removal from the previous Scene.  
        //
             
        // Diamond Scene
        this.camera.parent = undefined;  // See NOTE Above
        this.diamondScene = (renderType == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.diamondScene.add (this.camera);
        this.diamondScene.name = "DiamondScene";  
        this.diamondScene.matrixAutoUpdate = false;
        scenes.push(this.diamondScene);
             
        // Create appropriate child Sphere based on survey type: 'toast' vs. 'healpix'      
        if (this.survey.type == "toast")
        {   
            // Create Toast Sphere 
            this.toastSphere = new ASTROVIEW.ToastSphere(ASTROVIEW.RADIUS, renderType);  
            this.toastSphere.matrixAutoUpdate = true;
            this.toastSphere.name = "ToastSphere";
            this.diamondScene.add(this.toastSphere);
        }
        else if (this.survey.type == "healpix")
        {
            // Create Healpix Sphere   
            this.healpixSphere = new ASTROVIEW.HealpixSphere(ASTROVIEW.RADIUS, renderType);
            this.healpixSphere.matrixAutoUpdate = true;
            this.healpixSphere.name = "HealpixSphere";
            this.diamondScene.add(this.healpixSphere);   
        }
             
        // Graphics Scene 
        this.camera.parent = undefined;  // See NOTE Above
        this.graphicsScene = new THREE.CanvasScene();
        this.graphicsScene.add(this.camera);
        this.graphicsScene.name = "GraphicsScene";
        this.graphicsScene.matrixAutoUpdate = false;
        scenes.push(this.graphicsScene);
        
        // Select Scene
        this.camera.parent = undefined; // See NOTE Above
        this.selectScene = new THREE.CanvasScene();
        this.selectScene.add(this.camera);
        this.selectScene.name = "SelectScene";
        this.selectScene.matrixAutoUpdate = false;
        scenes.push(this.selectScene);
        
        // Cross Scene
        this.camera.parent = undefined; // See NOTE Above
        this.crossScene = new THREE.CanvasScene();
        this.crossScene.add(this.camera);
        this.crossScene.name = "CrossScene";
        this.crossScene.matrixAutoUpdate = false;
        scenes.push(this.crossScene);
        
        return scenes;
    }

    /////////////////////////////
    // renderScene()
    /////////////////////////////
    this.renderScene = function() 
    {     
        // Update the Camera Position 
        var cameraChanged = this.controller.updateController();
        
        // Reset the Rendering State to "CAMERA" if the Camera Changed
        if (cameraChanged) this.render("CAMERA");
        
        // Update Each Diamond Sphere in the View Frustum, using the latest Canvas Size & Camera Position
        if (this.toastSphere) 
            this.toastSphere.renderScene(this);
        if (this.healpixSphere) 
            this.healpixSphere.renderScene(this);
          
        // Render Scene contents to HTML5 Canvas using WebGL or Canvas Context passing the Rendering Info      
        this.renderSceneCanvas(this.ri);
        
        // Update the stats window
        if (this.stats) this.stats.update();
        
        // Update the refresh rate based on Active Mouse and Current Render State
        var ticks = (this.ri.state == "IDLE" ? ASTROVIEW.TIMER_TICKS_IDLE : ASTROVIEW.TIMER_TICKS_ACTIVE);
   
        // Request another animation frame, if interval is different
        if (ticks != this.timerTicks)
        {
            startTimer(bind(this, this.renderScene), ticks);
            this.timerTicks = ticks;
        }
    } 
    
    ////////////////////////
    // renderSceneWebGL()
    ////////////////////////
    this.renderSceneWebGL = function (ri)
    {
        this.diamondRenderer.render(this.diamondScene, this.camera);
        this.graphicsRenderer.render(this.graphicsScene, this.camera);
        this.selectRenderer.render(this.selectScene, this.camera);
        this.crossRenderer.render(this.crossScene, this.camera);

        // Reset render State
        ri.state = "IDLE";
    }
    
    ////////////////////////
    // Render Info
    ////////////////////////
    this.ri = {};
    var o=0; // order
    this.ri.order = {"CAMERA":    o++, 
                     "DIAMOND":   o++, 
                     "CROSS":     o++, 
                     "GRAPHICS":  o++, 
                     "RENDER": o++, 
                     "SELECT":    o++,                      
                     "COMPLETE":  o++, 
                     "IDLE":      o++};
                       
    this.ri.state = "CAMERA";
    this.ri.queue = [];
    
    this.ri.count = 0;
    this.ri.start = 0;
    this.ri.end = 0;
    this.ri.RENDER_SIZE = 200;
    
    this.ri.log = "";
    this.ri.logstate = "";

    ////////////////////////
    // render()
    ////////////////////////
    this.render = function(state)
    {
        // If canvas id was not specified, render camera layers
        if (!state || state == "") state = "CAMERA";
        
        if (this.ri.order.hasOwnProperty(state))
        {
            if (state == "CAMERA")
            {
                this.ri.state = state;
                this.ri.queue = [];
            }
            else if (this.ri.queue.indexOf(state) < 0)
            {
                this.ri.queue.push(state);
            }
        }
        else
        {
            console.error ("AstroView: Invalid render state passed to render(): " + state);
        }
    }
    
    ////////////////////////
    // renderSceneCanvas()
    ////////////////////////
    this.renderSceneCanvas = function(ri) 
    {   
        // If current state is before COMPLETE, we capture performance stats
        var time = (ri.order[ri.state] < ri.order["COMPLETE"] ? new Date().getTime(): undefined);
        var state = ri.state;
        
        switch (ri.state)
        { 
            case "CAMERA":   // (0): Clear camera existing Canvas Layer(s)
            {
                if (!this.graphicsRenderer.isClear) this.clearRenderer(this.graphicsRenderer); this.graphicsRenderer.isClear = true;
                if (!this.selectRenderer.isClear) this.clearRenderer(this.selectRenderer); this.selectRenderer.isClear = true; 
                ri.all = true;
                ri.state = "CROSS";
            }
            case "CROSS":   // (1) Cross: Render the select cross hair
            {    
                this.clearRenderer(this.crossRenderer); 
                this.crossRenderer.render(this.crossScene, this.camera); 
                ri.state = (ri.all ? "DIAMOND" : "COMPLETE");
                if (!ri.all) break;
            }  
            case "DIAMOND":  // (2) Diamond: Render the background diamond image(s)  
            {
                this.diamondRenderer.render(this.diamondScene, this.camera);
                ri.state = (ri.all ? "GRAPHICS" : "COMPLETE");
                break;
            }        
            case "GRAPHICS":  // (3) Project Graphics
            {   
                // This converts camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Delayed rendering of Graphics Objects Takes Place in 'RENDER' State
                if (!this.graphicsRenderer.isClear) this.clearRenderer(this.graphicsRenderer); this.graphicsRenderer.isClear = true;
                ri.count = this.graphicsRenderer.render(this.graphicsScene, this.camera, true); 
                if (ri.count > 0)
                {
                    ri.RENDER_SIZE = 200;
                    ri.start = 0;
                    ri.end = (ri.count >= ri.RENDER_SIZE ? ri.RENDER_SIZE-1 : ri.count-1);
                }
                
                ri.state = (ri.count > 0 ? "RENDER" : (ri.all ? "SELECT" : "COMPLETE"));
                break;
            }
            case "RENDER":  // (4) Render Projected Graphics
            {      
                if (ri.count > 0)
                {               
                    this.graphicsRenderer.render(this.graphicsScene, this.camera, false, ri.start, ri.end); 
                    this.graphicsRenderer.isClear = false;
                    ri.count -= ri.RENDER_SIZE;
                    ri.start += ri.RENDER_SIZE;
                    ri.RENDER_SIZE *= 5;              // Scale up graphics ri size to complete in fewer iterations.
                    ri.end += ri.RENDER_SIZE;
                }
                                
                ri.state = (ri.count > 0 ? "RENDER" : (ri.all ? "SELECT" : "COMPLETE"));
                break;
            }
            case "SELECT":   // (5) Project and Render Select Graphics
            {         
                // Project camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Render the select graphics, if any exist.
                if (!this.selectRenderer.isClear) this.clearRenderer(this.selectRenderer); this.selectRenderer.isClear = true;
                var count = this.selectRenderer.render(this.selectScene, this.camera, true);  
                if (count > 0)
                {
                    this.selectRenderer.render(this.selectScene, this.camera, false, 0, count); 
                    this.selectRenderer.isClear = false; 
                }
                ri.state = "COMPLETE";
                break;
            }
            case "COMPLETE":  // (6) Dump Render Stats
            {  
                if (ri && ri.log)
                {
                    console.log("RENDER STATS:" + ri.log);
                    ri.log = "";
                    ri.logstate = "";
                }    
                
                // Reset Render All Flag
                ri.all = false;   
                   
                // NOTE: We Pre-compute all the Scene Vertices by simulating an Object Selection.
                // This will dramatically improve object Selection Performance on Mobile Platforms
                this.controller.getSelected();  
                
                ri.state = "IDLE"; // no break here
            }
            case "IDLE":
            {
                // Check Render Queue: Pop next value off 
                if (ri.queue.length > 0) ri.state = ri.queue.shift();
                break;
            } 
        }
        
        // Update Render Stats
        if (time)
        {
            var et = new Date().getTime() - time;
            if (state != ri.logstate) ri.log += " " + state + ":";
            ri.log += et + ",";
            ri.logstate = state;
        }
    }
    
    ////////////////////////
    // updateRenderInfo()
    ////////////////////////
    this.updateRenderInfo = function(ri)
    {
        var et = new Date().getTime() - ri.stats.time;
        ri.stats.log += ri.stats.state + ":" + et + ", ";
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
        // Add Key Listener for Debug Shortcuts (debug only)
        if (this.debug) document.addEventListener( 'keypress' , bind(this, this.onKeyPress), false);
        
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
            var key = String.fromCharCode(unicode);
            this.onKey(key);
        }
    }
    
    this.onKey = function( key )
    {          
        //alert("onKeyPress: " + unicode); // find the char code        
        switch(key.toUpperCase())
        {
            // Testing of Viewer
            case  '1':   this.onM101(); break;           // '1'
            case  'A':   this.onActiveRate(); break;     // 'A'
            case  'B':   this.onBoxLayer(); break;       // 'B'
            case  'C':   this.onCatalogLayer(); break;   // 'C'
            case  'D':   this.onDeleteLayer(); break;    // 'D'
            case  'F':   this.onFootprintLayer(); break; // 'F'
            case  'I':   this.onImageLayer(); break;     // 'I'
            case  'M':   this.onMove(); break;           // 'M'
            case  'R':   this.onRotate(); break;         // 'R'
            case  'S':   this.onStats(); break;          // 'S'
            case  'T':   this.onTestLayer(); break;      // 'T'
            case  'U':   this.onUrl(); break;            // 'U'
            case  'V':   this.onVisibile(); break;       // 'V'
            case  'X':   this.onX(); break;              // 'X'
            case  'Y':   this.onY(); break;              // 'Y'
            case  'Z':   this.onZero(); break;           // 'Z'
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
    
    this.onKeyboardClick = function (event)
    {
        var key = prompt("Enter Key Value");
        this.onKey(key);
    }
    
    //////////////////////////
    // '1' : onM101() 
    //////////////////////////
    this.onM101 = function()
    {
         if (ASTROVIEW.M101)
         {
            var coord={"ra":210.8023, "dec":54.3490, "zoom":10};
            this.moveTo(coord);
            var layerData = ASTROVIEW.M101;
            layerNames.push (this.createLayer(layerData));
         }
    }
    
    //////////////////////////
    // 'A' : onActiveRate() 
    //////////////////////////
    this.onActiveRate = function()
    {
         var active = prompt("Enter Active Rate in MS:");
         ASTROVIEW.TIMER_TICKS_ACTIVE = active;
    }
    
    //////////////////////////
    // 'U' : onUrl() 
    //////////////////////////
    this.onUrl = function()
    {
        this.render("DIAMOND");
    }
    
    //////////////////////////
    // 'V' : onVisibile() 
    //////////////////////////
    this.onVisibile = function()
    {
        this.toastSphere.visible = !this.toastSphere.visible;
        this.healpixSphere.visible = !this.healpixSphere.visible;
        this.render("DIAMOND");
    }    
    
    //////////////////////////
    // 'M' : onMove() 
    //////////////////////////
    var coords = [{"ra":148.8882, "dec":69.0653, "zoom":10},       
                  {"ra":23.4620,  "dec":30.6602, "zoom":10},
                  {"ra":210.8023, "dec":54.3490, "zoom":10},
                  {"ra":202.4842, "dec":47.2306, "zoom":10},
                  {"ra":0      ,  "dec":0      , "zoom":10}];
    var x=0;
    this.onMove = function()
    {
        this.moveTo(coords[x]);
        x = (x+1) % coords.length;
    }
    
    //////////////////////////
    // 'X' : onX() 
    //////////////////////////
    this.onX = function()
    {
        this.camera.position.z += 100;
        this.render();
    }
    
    //////////////////////////
    // 'Y' : onY() 
    //////////////////////////
    this.onY = function()
    {
        this.camera.position.z -= 100;
        this.render();
    }
    
    //////////////////////////
    // 'Z' : onZero() 
    //////////////////////////
    this.onZero = function()
    {
        var coord={"ra":0, "dec":0};
        this.moveTo(coord);
    }
    
    //////////////////////////
    // 'S' : onStats() 
    //////////////////////////
    this.onStats = function()
    {
        this.controller.stats();
    }
    
    //////////////////////////
    // 'R' : onRotate() 
    //////////////////////////
    this.onRotate = function()
    {
        //var sphere = (this.healpixSphere.visibile ? this.healpixSphere : this.toastSphere);
        this.healpixSphere.rotation.y += (10.0 * TO_RADIANS);
        this.healpixSphere.updateMatrix();
        this.render("CAMERA");
    }
    
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
      
    var smallRedBox = "Polygon J2000 1.0 -1.0 1.0 1.0 359.0 1.0 359.0 -1.0";
    var redBox = "Polygon J2000 5.0 -5.0 5.0 5.0 355.0 5.0 355.0 -5.0";
    
    var crossPoints = "Position ICRS 0 0";   
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
         
    ///////////////////////////////////
    // 'S' : createCrossLayer() 
    //////////////////////////////////
    this.createCrossLayer = function()
    {      
        var layer = this.getLayer("CrossLayer");
        if (!layer)
        { 
            rows = {"footprint": "Position ICRS 0 0" };
            attribs = {"color":"0xff00ff", "symbol":"cross", "size":100};
            name = "CrossLayer";
            layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows], "canvas":"crossCanvas"};
            this.createLayer(layerData);
            layer = this.getLayer(name);
        }
        return layer;
    }
    
    //////////////////////////
    // 'T' : onTestLayer() 
    //////////////////////////
    this.onTestLayer = function()
    {        
        rows = {"footprint":redBox};
        attribs = {"color":"0xffff00"};
        name = "yellowBox";
        layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":[rows], "canvas":"selectCanvas"};
        layerNames.push (this.createLayer(layerData));
    }
    
    ////////////////////////////
    // 'C' : onCatalogLayer() 
    ////////////////////////////
    this.onCatalogLayer = function()
    { 
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
    }
    
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
            else if (layer.layerData.canvas && layer.layerData.canvas.toLowerCase() == "crosscanvas")
            {
                this.crossScene.add(layer);
                this.render("CROSS");
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
        var layer = this.getLayer(name);
        console.log("AstroBasicView: hasLayer() " + name + " : " + (layer != null));
        return (layer != null);
    }
             
    //////////////////////////
    // readLayer() 
    //////////////////////////
    this.readLayer = function(name)
    {
        console.log("AstroBasicView: readLayer() " + name);
        var layer = this.getLayer(name);
        return (layer ? layer.layerData : "");
    }
    
    //////////////////////////
    // getLayer() 
    //////////////////////////
    this.getLayer = function(name)
    {
        console.log("AstroBasicView: getLayer() " + name);
        var layer = this.graphicsScene.getChildByName(name, true) ||
                    this.selectScene.getChildByName(name, true) ||
                    this.crossScene.getChildByName(name, true);
        return (layer);
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
        this.controller.moveTo(coord);   
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
    console.log("Go AstroView Javascript!" + (this.debug ? " [debug]" : ""));
    this.createScene();
    this.addEvents();
    this.renderScene();
    this.onZero();
    this.sendEvent("AstroView.Initialization.Complete");
};

