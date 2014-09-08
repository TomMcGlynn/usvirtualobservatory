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
ASTROVIEW.TIMER_TICKS_ACTIVE = 10;
ASTROVIEW.TIMER_TICKS_IDLE   = 40;

// View State
ASTROVIEW.VIEW_STATE_ACTIVE = "ACTIVE";
ASTROVIEW.VIEW_STATE_IDLE = "IDLE";

// Set mobile browser
ASTROVIEW.MOBILE = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

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
// AstroBasicView
////////////////////////
ASTROVIEW.AstroBasicView = function ( config )
{  
    //
    // Set Config Defaults, if Not specified
    //
    if (!config) config = {};
    this.config = config;
    
    // Create element if not specified
    if (!this.config.div)
    {
        this.config.div = document.createElement('div');
        document.body.appendChild(this.config.div);
    }
    
    // Set properties on the parent div 
    this.config.div.id = "divAstroView";
    this.config.div.style.backgroundColor = "#222222";

    // Validate Config RenderType
    if (!this.config.rendertype)
    {
        this.config.rendertype = "canvas";
    }
    if (this.config.rendertype !== "canvas" && this.config.rendertype !== "webgl")
    {
        alert ("Unknown Render Type: " + config.rendertype)
    }
    
    // Load initial survey
    if (!this.config.surveytype)
    {
        this.config.surveytype = "DSS";
    }
    
    // Save Config Properties
    this.config.avcontainer = (config.avcontainer ? config.avcontainer : null);
    this.config.debug = (config.debug ? config.debug : false);
       
    // Core 3D objects
    this.scene = null; 
    this.camera = null;
    this.canvas = null;
    this.controller = null;
    this.projector = new THREE.Projector();

    // Renderers
    this.renderers = null;
    this.diamondRenderer = null;
    this.bufferRenderer = null;
    this.graphicsRenderer = null;
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
    this.spinLayerClock = null;
    this.spinLayerCounter = null;
    this.gridLayer = null;
    this.radecView = null;
   
    // Rendering Stats
    this.stats = null;
    
    // Animation Timer
    this.timer = new ASTROVIEW.Timer();
    this.zoomInTimer = new ASTROVIEW.Timer();
    this.zoomOutTimer = new ASTROVIEW.Timer();

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
        this.renderers = this.createRenderers(this.config.rendertype);
        if (!this.renderers || this.renderers.length == 0)
        {
            alert ("AstroView: Unable to create renderer(s) of type: " + this.config.rendertype);
            console.error("AstroView: Unable to create renderer(s) of type: " + this.config.rendertype);
            return;
        }
        else
        {   
            // Set default canvas to the last renderer created
            var last = this.renderers.length-1;
            this.canvas = this.renderers[last].domElement;
        }
        
        // Create Scenes (one for each renderer) 
        this.scenes = this.createScenes(this.config.rendertype);
        if (!this.scenes || this.scenes.length == 0 || !this.graphicsScene)
        {
            alert ("AstroView: Unable to create scenes(s) of type: " + this.config.rendertype);
            console.error("AstroView: Unable to create scenes(s) of type: " + this.config.rendertype);
            return;
        }
        else
        {   
            // Set default scene to the graphics Scene
            this.scene = this.graphicsScene;
        }
        
        // Create Grid Layer (invisible by default)
        this.gridLayer = this.createGridLayer();
        this.gridLayer.visible = false;
        
        // Create Spin Indicator Layers (invisible by default)
        this.spinLayerClock = this.createSpinLayer("SpinLayerClock", "spinClock");
        this.spinLayerClock.visible = false;
        this.spinLayerCounter = this.createSpinLayer("SpinLayerCounter", "spinCounter");
        this.spinLayerCounter.visible = false;
        
        // Create Cross Point Layer
        this.crossLayer = this.createCrossLayer();
        this.crossLayer.visible = true;
        
        // Create Camera Controller
        this.controller = new ASTROVIEW.CameraController( this );
                                                  
        if (this.config.debug)
        {
            // Create Stats View [Debug Only]
            this.stats = new Stats();
            this.stats.domElement.style.position = 'absolute';
            this.stats.domElement.style.top = '4px';
            this.stats.domElement.style.left = '4px';
            this.config.div.appendChild(this.stats.domElement);
        
            // Add Keyboard Icon [Debug Only]
            this.keyboardIcon = new Image();
            this.keyboardIcon.id = 'imgKeyboard';
            this.keyboardIcon.src = '../../Clients/AstroView/keyboard.png';
            this.keyboardIcon.style.position = 'absolute';
            this.keyboardIcon.style.top = '60px';
            this.keyboardIcon.style.left = '4px';
            this.keyboardIcon.style.zIndex = 10;
            this.keyboardIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onKeyboardClick),  false );
            this.config.div.appendChild( this.keyboardIcon );
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
                } catch(error) {this.config.rendertype = "canvas";}    // Try to create Canvas Renderer as fallback
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
            // Buffer Renderer (Used for all graphics off screen rendering)   
            this.bufferRenderer = new THREE.CanvasRendererMobile();
            this.bufferRenderer.setSize( window.innerWidth, window.innerHeight );
            this.bufferRenderer.autoClear = false;
            this.bufferRenderer.sortElements = false;
            renderers.push(this.bufferRenderer);
            
            // Graphics Renderer
            this.graphicsRenderer = new THREE.CanvasRendererMobile();
            this.graphicsRenderer.setSize( window.innerWidth, window.innerHeight );
            this.graphicsRenderer.autoClear = false;
            this.graphicsRenderer.sortElements = false;
            renderers.push(this.graphicsRenderer);
            
            // Select Renderer
            this.selectRenderer = new THREE.CanvasRendererMobile();
            this.selectRenderer.setSize( window.innerWidth, window.innerHeight );
            this.selectRenderer.autoClear = false;
            this.selectRenderer.sortElements = false;
            renderers.push(this.selectRenderer);
            
            // Cross Renderer
            this.crossRenderer = new THREE.CanvasRendererMobile();
            this.crossRenderer.setSize( window.innerWidth, window.innerHeight );
            this.crossRenderer.autoClear = false;
            this.crossRenderer.sortElements = false;
            renderers.push(this.crossRenderer);
            
            // Hover Renderer
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
            this.config.div.appendChild( canvas );      
        }
        
        if (this.graphicsRenderer && this.graphicsRenderer.domElement)
        {                           
            var canvas = this.graphicsRenderer.domElement;
            canvas.id = "GraphicsCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.selectRenderer && this.selectRenderer.domElement)
        {                           
            var canvas = this.selectRenderer.domElement;
            canvas.id = "SelectCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.crossRenderer && this.crossRenderer.domElement)
        {                           
            var canvas = this.crossRenderer.domElement;
            canvas.id = "CrossCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        if (this.hoverRenderer && this.hoverRenderer.domElement)
        {                           
            var canvas = this.hoverRenderer.domElement;
            canvas.id = "HoverCanvas";
            canvas.style.zIndex=zIndex++;
            canvas.style.position = 'absolute';
            canvas.style.top = '0px';
            canvas.style.left = '0px';
            this.config.div.appendChild( canvas ); 
        }
        
        // Return all renderers created
        return renderers; 
    }
    
    this.createScenes = function(rendertype)
    {
        var scenes = [];
        
        //////////////////////////////////////////////////////////////////////////////////////
        // CAMERA PARENT NOTE: 
        // To work around (THREE.Object3D) unnecessary THREE behavior, we do the following HACK:
        //
        // this.camera.parent = undefined;
        //
        // THREE.Object3D wants to perform a removal of the camera from the previous Scene if camera.parent is (!=undefined).
        // So we temporarily set it to 'undefined' to avoid its removal from the previous Scene.  
        //////////////////////////////////////////////////////////////////////////////////////
             
        // Diamond Scene
        this.camera.parent = undefined;  // See CAMERA PARENT NOTE (above)
        this.diamondScene = (rendertype == "canvas" ? new THREE.CanvasScene() : new THREE.Scene());
        this.diamondScene.add (this.camera);
        this.diamondScene.name = "DiamondScene";  
        this.diamondScene.matrixAutoUpdate = false;
        scenes.push(this.diamondScene);
             
        // Create appropriate child Sphere based on survey type: 'toast' vs. 'healpix'      
        if (this.survey.type == "toast")
        {   
            // Create Toast Sphere 
            this.toastSphere = new ASTROVIEW.ToastSphere(ASTROVIEW.RADIUS, rendertype);  
            this.toastSphere.matrixAutoUpdate = true;
            this.toastSphere.name = "ToastSphere";
            this.diamondScene.add(this.toastSphere);
        }
        else if (this.survey.type == "healpix")
        {
            // Create Healpix Sphere   
            this.healpixSphere = new ASTROVIEW.HealpixSphere(ASTROVIEW.RADIUS, rendertype);
            this.healpixSphere.matrixAutoUpdate = true;
            this.healpixSphere.name = "HealpixSphere";
            this.diamondScene.add(this.healpixSphere);   
        }
             
        // Graphics Scene 
        this.camera.parent = undefined;  // See CAMERA PARENT NOTE (above)
        this.graphicsScene = new THREE.CanvasScene();
        this.graphicsScene.add(this.camera);
        this.graphicsScene.name = "GraphicsScene";
        this.graphicsScene.matrixAutoUpdate = false;
        scenes.push(this.graphicsScene);
        
        // Select Scene
        this.camera.parent = undefined; // See CAMERA PARENT NOTE (above)
        this.selectScene = new THREE.CanvasScene();
        this.selectScene.add(this.camera);
        this.selectScene.name = "SelectScene";
        this.selectScene.matrixAutoUpdate = false;
        scenes.push(this.selectScene);
        
        // Cross Scene
        this.camera.parent = undefined; // See CAMERA PARENT NOTE (above)
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
   
        // Request another animation frame, if ticks is different
        if (ticks != this.timer.ticks)
        {
            this.timer.start(ASTROVIEW.bind(this, this.renderScene), ticks);
        }
    }
    
    /////////////////////////////
    // clearScene()
    /////////////////////////////
    this.clearScene = function()
    {
        if (this.toastSphere) 
            this.toastSphere.clearScene(this);
        if (this.healpixSphere) 
            this.healpixSphere.clearScene(this);
    }
    
    ////////////////////////
    // renderSceneWebGL()
    ////////////////////////
    this.renderSceneWebGL = function (ri)
    {
        this.diamondRenderer.render(this.diamondScene, this.camera);
        this.graphicsRenderer.render(this.graphicsScene, this.camera);
        this.crossRenderer.render(this.crossScene, this.camera);

        // Reset render State
        ri.state = "IDLE";
    }
    
    ////////////////////////
    // Render Info
    ////////////////////////
    this.ri = {};
    var o=0; // order
    this.ri.order = {"CAMERA":          o++, 
                     "DIAMOND":         o++, 
                     "CROSS":           o++, 
                     "GRAPHICS":        o++, 
                     "RENDER_GRAPHICS": o++, 
                     "SELECT":          o++,  
                     "RENDER_SELECT":   o++,                      
                     "COMPLETE":        o++, 
                     "IDLE":            o++};
                       
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
        var time = ((ri.state != "COMPLETE" && ri.state != "IDLE") ? new Date().getTime(): undefined);
        var state = ri.state;
        
        switch (ri.state)
        { 
            case "CAMERA":   // (0): Clear camera existing Canvas Layer(s)
            {
                this.clearRenderer(this.graphicsRenderer); 
                this.clearRenderer(this.selectRenderer); 

                ri.all = true;
                ri.state = "CROSS";
                // Drop into "CROSS"
            }
            case "CROSS":   // (1) Cross: Render the select cross hair
            {    
                this.clearRenderer(this.crossRenderer); 
                this.crossRenderer.render(this.crossScene, this.camera); 
                if (this.gridLayer.visible) this.drawGridLabels(this.crossRenderer);
                
                ri.state = (ri.all ? "DIAMOND" : "COMPLETE");
                if (!ri.all) break;
                // Drop into "DIAMOND"
            }  
            case "DIAMOND":  // (2) Diamond: Render the background diamonds 
            {
                this.diamondRenderer.render(this.diamondScene, this.camera);
                ri.state = (ri.all ? "GRAPHICS" : "COMPLETE");
                break;
            }        
            case "GRAPHICS":  // (3) Project Graphics
            {   
                // This converts camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Delayed rendering of Graphics Objects Takes Place in 'RENDER_GRAPHICS' State
                this.clearRenderer(this.bufferRenderer);
                this.clearRenderer(this.graphicsRenderer); 

                ri.count = this.bufferRenderer.render(this.graphicsScene, this.camera, true); 

                if (ri.count > 0)
                {
                    ri.RENDER_SIZE = 200;
                    ri.start = 0;
                    ri.end = (ri.count >= ri.RENDER_SIZE ? ri.RENDER_SIZE-1 : ri.count-1);
                }      
                // Drop into "RENDER_GRAPHICS"
            }
            case "RENDER_GRAPHICS":  // (4) Render Projected Graphics
            {      
                if (ri.count > 0)
                {   
                    this.bufferRenderer.render(this.graphicsScene, this.camera, false, ri.start, ri.end); 
                    ri.count -= ri.RENDER_SIZE;
                    ri.start += ri.RENDER_SIZE;
                    ri.RENDER_SIZE *= 5;              // Scale up graphics size to complete in fewer iterations.
                    ri.end += ri.RENDER_SIZE;
                }
                
                // Blast Offscreen Buffer to the Screen (Graphics Canvas)
                var ctx = this.graphicsRenderer.domElement.getContext('2d');
                ctx.drawImage(this.bufferRenderer.domElement, 0 , 0);
                                
                ri.state = (ri.count > 0 ? "RENDER_GRAPHICS" : (ri.all ? "SELECT" : "COMPLETE"));
                break;
            }
            case "SELECT":   // (5) Project and Render Select Graphics
            {         
                // This converts camera 3D graphics coordinates to 2D screen coordinates.
                // This allows us to get the number of graphics objects in the scene.  
                // Delayed rendering of Graphics Objects Takes Place in 'RENDER_GRAPHICS' State
                this.clearRenderer(this.bufferRenderer);
                this.clearRenderer(this.selectRenderer); 

                ri.count = this.bufferRenderer.render(this.selectScene, this.camera, true); 

                if (ri.count > 0)
                {
                    ri.RENDER_SIZE = 200;
                    ri.start = 0;
                    ri.end = (ri.count >= ri.RENDER_SIZE ? ri.RENDER_SIZE-1 : ri.count-1);
                }
                // Drop into "RENDER_SELECT"
            }
            case "RENDER_SELECT":  // (6) Render Projected Graphics
            {      
                if (ri.count > 0)
                {   
                    this.bufferRenderer.render(this.selectScene, this.camera, false, ri.start, ri.end); 
                    ri.count -= ri.RENDER_SIZE;
                    ri.start += ri.RENDER_SIZE;
                    ri.RENDER_SIZE *= 5;              // Scale up graphics size to complete in fewer iterations.
                    ri.end += ri.RENDER_SIZE;
                }
                
                // Blast Offscreen Buffer to the Screen (Select Canvas)
                var ctx = this.selectRenderer.domElement.getContext('2d');
                ctx.drawImage(this.bufferRenderer.domElement, 0 , 0);
                                
                ri.state = (ri.count > 0 ? "RENDER_SELECT" :"COMPLETE");
                break;
            }
            case "COMPLETE":  // (7) Dump Render Stats
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
         
                ri.state = "IDLE";
                // Drop into "IDLE"
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
            // Update Render Stats
            var et = new Date().getTime() - time;
            if (state != ri.logstate) ri.log += " " + state + ":";
            ri.log += et + ",";
            ri.logstate = state;
        }
    }
    
    this.drawGridLabels = function(renderer)
    {
        var rax = this.getRaScale();
        var decx = this.controller.getDecScale();
             
        this.drawGridLabelsRa(renderer, rax);
        this.drawGridLabelsDec(renderer, decx);
    }
    
    this.getRaScale = function() 
    {
        var zindex = this.controller.getZoomIndex();
        var rotx = Math.abs(this.controller.camera.rotation.x) * TO_DEGREES;
        if (rotx > 70) zindex--;
        if (rotx > 80) zindex--;
        if (rotx > 85) zindex--;
        if (rotx > 86) zindex--;
        if (rotx > 87) zindex--;
        if (rotx > 88) zindex--;
        if (rotx > 89) zindex--;
        if (rotx > 89.6) zindex-=1;
        if (rotx > 89.8) zindex-=1;
        if (rotx > 89.9) zindex-=1;
        if (zindex < 0) zindex = 0;  // Sanity check
        
        var rax = this.controller.getRaScaleFromZoomIndex(zindex);
        return rax;
    }
    
    this.drawGridLabelsRa = function(renderer, degrees)
    {
        if (!degrees) degrees = 5;                  // If not specified, Draw Tick Mark every 5 degrees
        
        // Use Math.round() for whole number intervals    
        var round = (parseInt(degrees) == degrees); 
        
        // Determine value for toFixed() size used below
        var array = String(degrees).split(".");
        var fixed = (array.length == 2 ? array[1].length : 0);
        // Rotate the RA Labels all the time.  There are just too many cases where they clobber each other.
        var rotate = true;  
        
        var ctx = renderer.domElement.getContext('2d');
        var width = renderer.domElement.clientWidth;
        var height = renderer.domElement.clientHeight;
        var sx = renderer.domElement.width/width;
        var sy = renderer.domElement.height/height;
        
        // Calculate RA/DEC for every horizontal pixel value, draw tickmark if multiple of 10 
        var decreasing = true;  
        var coord = new ASTROVIEW.Coord(this.controller);
        coord.mod = 999; 
        var lastcoord;
        
        var lastx = 0;
        for (var x=-20; x<width+20; x++)
        {
            coord.screen.x = x;
            coord.screen.y = height;
            coord.updateAllCoords();
            coord.mod = Math.abs(coord.radec.ra%degrees);
            
            if (lastcoord)
            {                
                if (decreasing) // Downward slope toward tick mark
                {
                    if (coord.mod >= lastcoord.mod)  // Found the bottom: This is a tick mark !!!
                    {
                        decreasing = false;

                        // Only draw this tickmark and label if we have 30 pixels since last one
                        lastcoord.radec.ra -= lastcoord.mod;
                        var text = lastcoord.raToString(this.radecView.format, fixed, round);
                        this.drawLabelRa(ctx, lastcoord.screen.x, lastcoord.screen.y, text, sx, sy, rotate);
                        lastx = lastcoord.screen.x;
                    }
                }
                else if (coord.mod < lastcoord.mod) // Look for apex
                {
                    decreasing = true;
                }
            }
            else  // Save very first lastcoord
            {
                lastcoord = new ASTROVIEW.Coord(this.controller);
            }
            lastcoord.copy(coord);
            lastcoord.mod = coord.mod;
        }
    }
    
    this.drawGridLabelsDec = function(renderer, degrees)
    {
        if (!degrees) degrees = 5;                  // If not specified, Draw Tick Mark every 5 degrees
        // Use Math.round() for whole number intervals    
        var round = parseInt(degrees) == degrees;  
        // Determine toFixed() size used below
        var array = String(degrees).split(".");
        var fixed = (array.length == 2 ? array[1].length : 0);
        
        var ctx = renderer.domElement.getContext('2d');
        var width = renderer.domElement.clientWidth;
        var height = renderer.domElement.clientHeight;
        var sx = renderer.domElement.width/width;
        var sy = renderer.domElement.height/height;
        
        // Determine Pole Position
        var pole = new ASTROVIEW.Coord(this.controller); 
        pole.north = this.controller.rotation.x > 0;
        pole.normal.set(0, (pole.north ? 1.0 : -1.0), 0);
        pole.normalToScreen();
        
        // Set Pole Visible flag, used many times below
        pole.visible = (pole.screen.y >= 0 && pole.screen.y <= height)
        
        // Draw Pole Tick Mark, if visible in the viewport
        if (pole.visible)
        {
             var text = (pole.north ? "+90" : "-90");
             this.drawLabelDec(ctx, 0, pole.screen.y, text, sx, sy);
        }
        
        // Calculate RA/DEC for every horizontal pixel value, draw tickmark if multiple of 10 
        var decreasing = true;  
        var coord = new ASTROVIEW.Coord(this.controller);
        coord.mod = 999; 
        var lastcoord;
        
        for (var y=-20; y<height; y++)
        {
            coord.screen.x = 0;
            coord.screen.y = y;
            coord.updateAllCoords();
            coord.mod = Math.abs(coord.radec.dec%degrees);
            
            if (lastcoord)
            {                
                if (decreasing) // Downward slope toward tick mark
                {
                    if (coord.mod >= lastcoord.mod)  // Found the bottom: This is a tick mark !!!
                    {
                        decreasing = false;
                        
                        // Skip any Tick Marks above/below the Poles
                        var renderLine = true;
                        if (pole.visible)
                        {
                            if (pole.north)
                                renderLine = (lastcoord.screen.y > pole.screen.y+20);
                            else
                                renderLine = (lastcoord.screen.y < pole.screen.y-20);
                        }
                        
                        // Render the Tick Mark + Label
                        if (renderLine)
                        {
                            var text = lastcoord.decToString(this.radecView.format, fixed, round);
                            if (parseFloat(text) == 0) text = "0"; 
                            this.drawLabelDec(ctx, lastcoord.screen.x, lastcoord.screen.y, text, sx, sy);
                        }
                    }
                }
                else if (coord.mod < lastcoord.mod) // Look for apex
                {
                    decreasing = true;
                }
            }
            else  // Save very first lastcoord
            {
                lastcoord = new ASTROVIEW.Coord(this.controller);
            }
            lastcoord.copy(coord);
            lastcoord.mod = coord.mod;
        }
    }
    
    this.drawLabelRa = function(c, x, y, text, sx, sy, rotate)
    {
        c.save();
        c.scale(sx, sy);
        c.translate(x, y);
        
        c.lineWidth = .8;
        c.strokeStyle = '#1BFF00';
        c.fillStyle = '#1B9900';
        
        c.beginPath();
        c.moveTo(   0, -18);
        c.lineTo(   0,   0);
        c.closePath();
        
        c.translate(2, (rotate ? -18 : -8));
        c.rotate((rotate ? -0.6 : 0.0));
        c.font = '10pt Verdana';
        c.fillText(text, 0, 0);
        
        c.stroke();
        c.restore();
    }
       
    this.drawLabelDec = function(c, x, y, text, sx, sy)
    {
        c.save();
        c.scale(sx, sy);
        c.translate(x, y);

        c.lineWidth = .8;
        c.strokeStyle = '#1BFF00';
        c.fillStyle = '#1B9900';
        
        c.beginPath();
        c.moveTo(   0,  0);
        c.lineTo(   34, 0);
        c.closePath();
        
        c.font = '10pt Verdana';
        c.fillText(text, 4, 14);
        
        c.stroke();
        c.restore();
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
        // NOTE: Leave this as Width/Height: clientWidth, height does not work on Mobile
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
        if (this.config.debug) document.addEventListener( 'keypress' , ASTROVIEW.bind(this, this.onKeyPress), false);
        
        // Add Events attached to <Canvas> Element
        this.canvas.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onMouseOut),  false );
        this.canvas.addEventListener( 'mouseover',  ASTROVIEW.bind(this, this.onMouseOver),  false );
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////               
    //
    // Events
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////
    this.mouseOver = true;
    this.onKeyPress = function( event )
    {        
        if (event.ctrlKey)
        {
            var key = this.getChar(event);
            if (key && ASTROVIEW.trim(key) !== "")
            {
                event.preventDefault();
                this.onKey(key, event);
            }
        }
    }
    
    this.getChar = function( event ) 
    {
        if (event.keyIdentifier) // Chrome
        {
            return String.fromCharCode(parseInt(event.keyIdentifier.replace("U+",""),16)); 
        } 
        else // The Rest
        {
            return String.fromCharCode(event.which);
        } 
    }
    
    this.onKey = function( key, event )
    {          
        switch(key.toUpperCase())
        {
            // Testing of Viewer
            case  '1':   this.onM101(); break;           // '1'
            case  'A':   this.onActiveRate(); break;     // 'A'
            case  'B':   this.onBoxLayer(); break;       // 'B'
            case  'C':   this.onCatalogLayer(); break;   // 'C'
            case  'D':   this.onDeleteLayer(); break;    // 'D'
            case  'F':   this.onFootprintLayer(); break; // 'F'
            case  'G':   this.onGrid(); break;           // 'G'
            case  'H':   this.onHelp(); break;           // 'H'
            case  'I':   this.onImageLayer(); break;     // 'I'
            case  'M':   this.onMove(); break;           // 'M'
            case  'R':   this.onRotate(); break;         // 'R'
            case  'S':   this.onStats(); break;          // 'S'
            case  'T':   this.onTestLayer(); break;      // 'T'
            case  'U':   this.onUrl(); break;            // 'U'
            case  'V':   this.onVisibile(); break;       // 'V'
            case  'Z':   this.onZ(event); break;         // 'Z'
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
        var key = prompt("Enter Key Value (H for Help):");
        this.onKey(key, event);
    }
    
    this.createRaDecControls = function()
    {
        // Create RA/DEC View, specifying mobile or desktop layout
        this.radecView = new ASTROVIEW.RaDecView(this);   
        this.radecView.div.style.zIndex = 10;
        this.config.div.appendChild( this.radecView.div ); 
    }
    
    this.createZoomControls = function()
    {
        // Create Zoom In Icon 
        this.zoomInIcon = new Image();
        this.zoomInIcon.id = 'imgZoomIn';
        this.zoomInIcon.src = '../../Clients/AstroView/plus.png';
        this.zoomInIcon.style.position = 'absolute';
        this.zoomInIcon.style.bottom = '40px';
        this.zoomInIcon.style.left = '-10px';
        this.zoomInIcon.style.zIndex = 10;
        this.zoomInIcon.addEventListener( 'mousedown',  ASTROVIEW.bind(this, this.onZoomInDown),  false );
        this.zoomInIcon.addEventListener( 'mouseup',  ASTROVIEW.bind(this, this.onZoomInStop),  false );
        this.zoomInIcon.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onZoomInStop),  false );
        
        this.zoomInIcon.title = "Zoom In";
        this.config.div.appendChild( this.zoomInIcon );
        
        // Create Zoom Out Icon 
        this.zoomOutIcon = new Image();
        this.zoomOutIcon.id = 'imgZoomOut';
        this.zoomOutIcon.src = '../../Clients/AstroView/minus.png';
        this.zoomOutIcon.style.position = 'absolute';
        this.zoomOutIcon.style.bottom = '0px';
        this.zoomOutIcon.style.left = '-10px';
        this.zoomOutIcon.style.zIndex = 10;
        this.zoomOutIcon.addEventListener( 'mousedown',  ASTROVIEW.bind(this, this.onZoomOutDown),  false );
        this.zoomOutIcon.addEventListener( 'mouseup',  ASTROVIEW.bind(this, this.onZoomOutStop),  false );
        this.zoomOutIcon.addEventListener( 'mouseout',  ASTROVIEW.bind(this, this.onZoomOutStop),  false );

        this.zoomOutIcon.title = "Zoom Out";
        this.config.div.appendChild( this.zoomOutIcon );
    }
        
    this.onZoomInDown = function (event)
    {
        this.controller.zoomIn();
        this.zoomInTimer.start(ASTROVIEW.bind(this, this.onZoomInDown), 200); 
    }
    
    this.onZoomInStop = function (event)
    {
        this.zoomInTimer.stop();
    } 
     
    this.onZoomOutDown = function (event)
    {
        this.controller.zoomOut();
        this.zoomOutTimer.start(ASTROVIEW.bind(this, this.onZoomOutDown), 200); 
    }
      
    this.onZoomOutStop = function (event)
    {
        this.zoomOutTimer.stop(); 
    }
    
    this.createSettingsControls = function()
    {
        if (typeof jo === 'undefined') return;    // If jo library is not loaded return;

        // Create Settings Icon 
        this.settingsIcon = new Image();
        this.settingsIcon.id = 'imgSettings';
        this.settingsIcon.src = '../../Clients/AstroView/gear.png';
        this.settingsIcon.style.position = 'absolute';
        this.settingsIcon.style.bottom = '4px';
        this.settingsIcon.style.right = '10px';
        this.settingsIcon.style.zIndex = 10;
        this.settingsIcon.addEventListener( 'click',  ASTROVIEW.bind(this, this.onSettingsClick),  false );
        this.settingsIcon.title = "Settings Menu";
        this.config.div.appendChild( this.settingsIcon );
        
        // Create Settings Properties
        var av = this;
        
        var surveyNames = this.getSurveyNames(); 

        this.settings = new joRecord({
            search: ' ',
            grid: false,
            radec: true,
            crosshair: true,
            survey: 0,
            surveyNames: surveyNames
        });
    
        // Create Settings Popup
        jo.load();
        this.screen = new joScreen();
        this.screen.setContainer(this.config.div.id);

        joCache.set("popup", function() {
        
            var input, radec, crosshair, grid, survey, close, go;
            var popup = [
                new joTitle("AstroView Settings"),
                new joGroup([
                    new joCaption("<b>Move To:</b>"),
                    new joFlexrow([
                         input = new joInput(av.settings.link("search")).setStyle("flex"),
                         go = new joButton("&nbsp Go &nbsp").setStyle("noflex"),
                    ]),
                    new joFlexrow([
                        new joLabel(" Grid Lines:").setStyle("left"),
                        grid = new joToggle(av.settings.link("grid")).setLabels(["Off", "On"])
                    ]),
                    new joFlexrow([
                        new joLabel(" [RA, DEC]:").setStyle("left"),
                        radec = new joToggle(av.settings.link("radec")).setLabels(["Off", "On"])
                    ]),
                    new joFlexrow([
                        new joLabel(" Crosshair:").setStyle("left"),
                        crosshair = new joToggle(av.settings.link("crosshair")).setLabels(["Off", "On"])
                    ]),
                    new joLabel("<b>Survey:</b>"),
                    new joFlexrow([
                        survey = new joOption(av.settings.getProperty("surveyNames"), av.settings.link("survey"))
                    ]),
                ]),
                close = new joButton("Close")
            ];
            
            // Wire Up Settings Callbacks
            joEvent.on(input.container, "keydown", function(e)
            {
                var evt = e || window.event;
                // "e" is the standard event object (FF, Chrome, Safari, Opera), "window.event" (IE)
                
                // If we got [CR] move to named position
                if ( evt.keyCode === 13 ) { 
                    go.focus();
                    var name = av.settings.getProperty("search");
                    console.log("onKeyDown(): Got [CR] name = " + name);
                    if (name && ASTROVIEW.trim(name).length > 0)
                        av.moveToName(name);
                    input.focus();
                    return false;  // Disable form submission
                }
            }, this);
                     
            go.selectEvent.subscribe(function() 
            {
                go.focus();
                var name = av.settings.getProperty("search");
                if (name && ASTROVIEW.trim(name).length > 0)
                    av.moveToName(name);
                go.blur();
            });
            
            grid.changeEvent.subscribe(function() 
            {
                if (av.gridLayer.visible != av.settings.getProperty("grid"))
                {
                    av.gridLayer.visible = av.settings.getProperty("grid");
                    av.render("CROSS");
                }
            });
            
            radec.changeEvent.subscribe(function() 
            {
                av.radecView.div.style.display = (av.settings.getProperty("radec") ? 'inherit' : 'none');
            });
            
            crosshair.changeEvent.subscribe(function() 
            {
                if (av.crossLayer.visible != av.settings.getProperty("crosshair"))
                {
                    av.crossLayer.visible = av.settings.getProperty("crosshair");
                    av.render("CROSS");
                }
            });
                     
            survey.selectEvent.subscribe(function() 
            {
                var index = av.settings.getProperty("survey");  // Returns index into surveyNames array
                var surveyNames = av.settings.getProperty("surveyNames");
                if (surveyNames && surveyNames[index])
                {
                   av.loadSurveyName(surveyNames[index]);
                }
            });
            
            close.selectEvent.subscribe(function() 
            {
                av.screen.hidePopup();
            });
            
            return popup;
        });
        
        // Init the popup to current settings
        popup = joCache.get("popup");
    }
   
    this.onSettingsClick = function (event)
    {
        var popup = joCache.get("popup");
        this.screen.showPopup(popup);
    }
    
    this.moveToName = function (name)
    {
        console.log("moveToName: " + name);
        var ename = encodeURIComponent(name);
        var url = ASTROVIEW.urls.name.mastresolver + ename;
        var av = this;
        var file = joFile(url, function(data, error) {
            if (error) 
            {
                alert("Error while trying to resolve name:" + name + 
                      "<br>Error: " + error + 
                      "<br>Url: " + url);
            }
            else
            {
                var xmlDoc = ASTROVIEW.fromXml(data);
                var radec = av.xmlDocToCoord(xmlDoc);

                // If valid coord, move to the location
                if (radec && radec.sra && radec.sdec)
                {
                    if (av.controller.getZoomLevel() < 10) radec.zoom = 10;
                    av.moveTo(radec);
                    av.screen.hidePopup();
                }
                else
                {
                    alert("Unable to resolve name : " + name +
                        "\n\nExamples:\n\n" +
                        "Names:\n" + 
                        "   M101, NGC45, Orion, Dubhe\n\n" +
                        "Catalogs:\n" + 
                        "   BD+19 706\n" +
                        "   png 000.8-07.6\n" +
                        "   2MASS J04215943+1932063\n" +
                        "   TYC 1272-470-1\n\n" +
                        "Coordinates:\n" + 
                        "   14 03 12.6 54 20 56.7\n" + 
                        "   14:03.210 54:20.945\n" + 
                        "   14h03m12.6s +54d20m56.7s\n" + 
                        "   g102.0373+59.7711\n" + 
                        "   180.468 -18.866\n");
                }
            }
        }); // jofile()
    }
 
    this.xmlDocToCoord = function(xmlDoc)
    {
        var radec;
        if (xmlDoc && xmlDoc.firstChild && xmlDoc.firstChild.localName == "resolvedItems")
        {   
            var resolvedItems = xmlDoc.firstChild;
            if (resolvedItems.firstChild && resolvedItems.firstChild.localName == "resolvedCoordinate")
            {
                var resolvedCoordinate = resolvedItems.firstChild;
                if (resolvedCoordinate.childNodes && resolvedCoordinate.childNodes.length > 0)
                {
                    for (var i=0; i<resolvedCoordinate.childNodes.length; i++)
                    {
                        var child = resolvedCoordinate.childNodes[i];
                        if (child.localName && child.localName == "ra")
                        {
                            if (!radec) radec = {}; 
                            radec.sra = child.textContent;
                            radec.ra = parseFloat(radec.sra); 
                            if (radec.sdec) break;
                        }
                        if (child.localName && child.localName == "dec")
                        {
                            if (!radec) radec = {}; 
                            radec.sdec = child.textContent;
                            radec.dec = parseFloat(radec.sdec); 
                            if (radec.sra) break;
                        }
                    }
                }
            }
        }
        return radec;
    }
    
    this.getSurveyNames = function()
    {
        var names = [];
        for (var id in ASTROVIEW.surveys) 
        {
            if (ASTROVIEW.surveys[id].visible)
            {
                names.push(ASTROVIEW.surveys[id].name);
            }
        }
        return names;
    }
    
    this.loadSurveyName = function(surveyName)
    {
        var found = false;
        for (var id in ASTROVIEW.surveys) 
        {
            if (ASTROVIEW.surveys[id].name == surveyName)
            {
                this.loadSurvey(ASTROVIEW.surveys[id]);
                found = true;
            }
        }
        
        if (!found)
        {
            alert("Unable to load Survey Name: " + surveyName + "\n\n" + 
                  "Verify that survey is defined and visible in Surveys.js");
        }
    }
    
    this.loadSurvey = function(survey)
    {
        console.log("loadSurvey():" + survey.name);   
        this.clearScene();
        this.survey = survey;
        if (this.controller)
        {
            this.controller.loadSurvey(survey);
        }
        this.render("DIAMOND");
    }
    
    //////////////////////////
    // 'H' : onHelp() 
    //////////////////////////
    this.onHelp = function()
    {
         alert( "'1': M101\n" +
                "'A': ActiveRate\n" +
                "'B': BoxLayer\n" +
                "'C': CatalogLayer\n" +
                "'D': DeleteLayer\n" +
                "'F': FootprintLayer\n" +
                "'H': Help\n" +
                "'I': ImageLayer\n" +
                "'M': Move\n" +
                "'R': Rotate\n" +
                "'S': Stats\n" +
                "'T': TestLayer\n" +
                "'U': Url\n" +
                "'V': Visibile\n" +
                "'Z': (+/-) Z Axis [Shift]\n");
    }
    
    //////////////////////////
    // '1' : onM101() 
    //////////////////////////
    this.onM101 = function()
    {
         if (ASTROVIEW.M101)
         {
            var radec={"ra":210.8023, "dec":54.3490, "zoom":10};
            this.moveTo(radec);
            var layerData = ASTROVIEW.M101;
            layerNames.push (this.createLayer(layerData));
         }
    }
    
    //////////////////////////
    // 'G' : onGrid() 
    //////////////////////////
    this.onGrid = function()
    {
         this.showGrid();
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
    var radecs = [{"ra":0, "dec":90},
                  {"ra":165.93196467,   "dec":61.75103469,  "zoom":10}, 
                  {"ra":84.05338894,    "dec":-1.20191914,  "zoom":10},      
                  {"ra":148.8882,       "dec":69.0653,      "zoom":10},       
                  {"ra":23.4620,        "dec":30.6602,      "zoom":10},
                  {"ra":210.8023,       "dec":54.3490,      "zoom":10},
                  {"ra":202.4842,       "dec":47.2306,      "zoom":10},
                  {"ra":0,              "dec":0,            "zoom":10}];
    var x=0;
    this.onMove = function()
    {
        this.moveTo(radecs[x]);
        x = (x+1) % radecs.length;
    }
    
    //////////////////////////
    // 'Z' : onZ() 
    //////////////////////////
    this.onZ = function(event)
    {
        this.camera.position.z += (event.shiftKey ? -100 : 100);
        this.render();
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
    // createGridLayer() 
    //////////////////////////////////
    this.createGridLayer = function()
    {   
        var layer = this.getLayer("GridLayer");
        if (!layer)
        {
            var layerData = {
               "name":"GridLayer",
               "type":"footprint",
               "canvas":"crossCanvas",
               "attribs":{"color":"1BEE00","stroke":1,"symbol":"square"},
               "rows":[]
            }
            
            // Create the Horizontal Lines for Right Ascension
            for (var ra = 0; ra<=170; ra+=10)
            {
                var row = {"footprint": "POLYGON ICRS "};
                var rac = ra+180;
                for (var dec = -90; dec<=90; dec+=10)
                {
                    row.footprint += " " + ra + " " + dec;
                }
                for (var dec = 80; dec>=-90; dec-=10)
                {
                    row.footprint += " " + rac + " " + dec;
                }
                layerData.rows.push(row);
            }
            
            // Create the Vertical Lines for Declination
            for (var dec = -80; dec<=80; dec+=10)
            {
                var row = {"footprint": "POLYGON ICRS "};
                for (var ra = 0; ra<=360; ra+=10)
                {
                    row.footprint += " " + ra + " " + dec;
                }
                layerData.rows.push(row);
            }
            this.createLayer(layerData);
            layer = this.getLayer("GridLayer");
        }
        return layer;
    }
    
    ///////////////////////////////////
    // createSpinLayer() 
    //////////////////////////////////
    this.createSpinLayer = function(name, symbol)
    {      
        var layer = this.getLayer(name);
        if (!layer)
        { 
            rows = [{"footprint": "Position ICRS 0 90" }, {"footprint": "Position ICRS 0 -90"}];
            attribs = {"color":"0xFFFF00", "symbol":symbol, "size":100};
            layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":rows, "canvas":"crossCanvas"};
            this.createLayer(layerData);
            layer = this.getLayer(name);
        }
        return layer;
    }
    
    ///////////////////////////////////
    // createCrossLayer() 
    //////////////////////////////////
    this.createCrossLayer = function()
    {      
        var layer = this.getLayer("CrossLayer");
        if (!layer)
        { 
            rows = [{"footprint": "Position ICRS 0 0" }];
            attribs = {"color":"0xff00ff", "symbol":"cross", "size":100};
            name = "CrossLayer";
            layerData = {"type":"footprint", "name":name, "attribs":attribs, "rows":rows, "canvas":"crossCanvas"};
            this.createLayer(layerData);
            layer = this.getLayer(name);
        }
        return layer;
    }
      
    ///////////////////////////////////
    // showGrid() 
    //////////////////////////////////
    this.showGrid = function(visible)
    {      
        if (this.gridLayer)
        {
            this.gridLayer.visible = (visible ? visbile : !this.gridLayer.visible);
            this.render("CROSS");
        }
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
            this.config.avcontainer = avcontainer;
            console.log("AstroBasicView:init() Complete: using avcontainer passed in."); 
        }
        else if (typeof AstroViewContainer != 'undefined')  // check for global container
        {
            this.config.avcontainer = AstroViewContainer;
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
        if (layerData && layerData.type && ASTROVIEW.isString(layerData.type))
        {
            var type = ASTROVIEW.trim(layerData.type.toLowerCase());
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
        
        // Try to remove from Cross Scene Third
        if (!layer)
        {
            layer = this.crossScene.getChildByName(name, true);
            if (layer)
            {
                layer.clean();
                this.crossScene.remove(layer, true);
                this.render("CROSS");
            }
        }

        console.log("AstroBasicView: deleteLayer() " + name + " : " + (layer != null));
        return (layer ? layer.name : "");
    }
    
    //////////////////////////
    // moveTo() 
    //////////////////////////
    this.moveTo = function(radec)
    {
        this.controller.moveTo(radec);   
        this.render("CAMERA");
    }
    
    //////////////////////////
    // sendEvent() 
    //////////////////////////
    this.sendEvent = function (type, objects)
    {
        if (this.config.avcontainer && this.config.avcontainer.onAstroViewEvent)
        {
            var msg = {"type" : type, "objects" : objects};
            this.config.avcontainer.onAstroViewEvent(msg);
        }
        else
        {
            console.log ("sendAstroViewEvent() Not Sending Event: " + type + ". Missing onAstroViewEvent() callback.");
        }
    }
            
    ///////////////
    // Main
    ///////////////
    console.log("Go AstroView Javascript!" + (this.config.debug ? " [debug]" : ""));
    this.loadSurveyName(this.config.surveytype);

    // Create Controls
    this.createRaDecControls();
    this.createSettingsControls();
    if (!ASTROVIEW.MOBILE) this.createZoomControls();
        
    // Create Scene 
    this.createScene();
    this.addEvents();
    this.renderScene();
    this.moveTo({"ra": 0, "dec": 0});
    this.sendEvent("AstroView.Initialization.Complete");
};

